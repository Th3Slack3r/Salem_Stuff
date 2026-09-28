package haven.glsl;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ValBlock {
  private static final ThreadLocal<Value> processing = new ThreadLocal<>();
  
  private final Collection<Value> values = new LinkedList<>();
  
  private final Map<Object, Value> ext = new IdentityHashMap<>();
  
  public static interface Factory {
    ValBlock.Value make(ValBlock param1ValBlock);
  }
  
  public abstract class Value {
    public final Type type;
    
    public final Symbol name;
    
    public boolean used;
    
    public Variable var;
    
    protected Expression init;
    
    private final Collection<Value> deps = new LinkedList<>();
    
    private final Collection<Value> sdeps = new LinkedList<>();
    
    private final OrderList<Macro1<Expression>> mods = new OrderList<>();
    
    private boolean forced;
    
    public Value(Type type, Symbol name) {
      this.type = type;
      this.name = name;
      ValBlock.this.values.add(this);
    }
    
    public Value(Type type) {
      this(type, new Symbol.Gen());
    }
    
    public void mod(Macro1<Expression> macro, int order) {
      this.mods.add(macro, order);
    }
    
    public Expression modexpr(Expression expr) {
      for (Macro1<Expression> mod : this.mods)
        expr = mod.expand(expr); 
      return expr;
    }
    
    protected void cons1() {
      ValBlock.processing.set(this);
      try {
        this.init = modexpr(root());
      } finally {
        ValBlock.processing.remove();
      } 
    }
    
    protected void cons2(Block blk) {
      this.var = blk.local(this.type, this.name, this.init);
    }
    
    public Expression ref() {
      return new Expression() {
          public Expression process(Context ctx) {
            if (ValBlock.Value.this.var == null)
              throw new IllegalStateException("Value reference processed before being constructed"); 
            return ValBlock.Value.this.var.ref().process(ctx);
          }
        };
    }
    
    public Expression depref() {
      if (ValBlock.processing.get() == null)
        throw new IllegalStateException("Dependent value reference outside construction"); 
      ((Value)ValBlock.processing.get()).depend(this);
      return ref();
    }
    
    public void force() {
      this.forced = true;
    }
    
    public void depend(Value dep) {
      if (!this.deps.contains(dep))
        this.deps.add(dep); 
    }
    
    public void softdep(Value dep) {
      if (!this.sdeps.contains(dep))
        this.sdeps.add(dep); 
    }
    
    public abstract Expression root();
  }
  
  public abstract class Group {
    private final Collection<GValue> values = new LinkedList<>();
    
    private final Collection<ValBlock.Value> deps = new LinkedList<>();
    
    private final Collection<ValBlock.Value> sdeps = new LinkedList<>();
    
    private int state = 0;
    
    public class GValue extends ValBlock.Value {
      public Expression modexpr;
      
      public GValue(Type type, Symbol name) {
        super(type, name);
        for (ValBlock.Value dep : ValBlock.Group.this.deps)
          depend1(dep); 
        for (ValBlock.Value dep : ValBlock.Group.this.sdeps)
          softdep1(dep); 
        ValBlock.Group.this.values.add(this);
      }
      
      public GValue(Type type) {
        this(type, new Symbol.Gen());
      }
      
      protected void cons1() {
        if (ValBlock.Group.this.state < 1) {
          ValBlock.Group.this.cons1();
          ValBlock.Group.this.state = 1;
        } 
        Expression in = ref();
        this.modexpr = modexpr(in);
        if (this.modexpr == in)
          this.modexpr = null; 
      }
      
      protected void cons2(Block blk) {
        if (ValBlock.Group.this.state < 2) {
          ValBlock.Group.this.cons2(blk);
          ValBlock.Group.this.state = 2;
        } 
      }
      
      public void addmods(Block blk) {
        if (this.modexpr != null)
          blk.add(Cons.ass(this.var, this.modexpr)); 
      }
      
      public final Expression root() {
        throw new RuntimeException("root() is not applicable for group values");
      }
      
      private void depend1(ValBlock.Value dep) {
        super.depend(dep);
      }
      
      public void depend(ValBlock.Value dep) {
        ValBlock.Group.this.depend(dep);
      }
      
      private void softdep1(ValBlock.Value dep) {
        super.softdep(dep);
      }
      
      public void softdep(ValBlock.Value dep) {
        ValBlock.Group.this.softdep(dep);
      }
    }
    
    public void depend(ValBlock.Value dep) {
      for (GValue val : this.values)
        val.depend1(dep); 
    }
    
    public void softdep(ValBlock.Value dep) {
      for (GValue val : this.values)
        val.softdep1(dep); 
    }
    
    protected abstract void cons1();
    
    protected abstract void cons2(Block param1Block);
  }
  
  private void use(Value val) {
    if (val.used)
      return; 
    val.used = true;
    for (Value dep : val.deps)
      use(dep); 
    for (Value dep : val.sdeps) {
      if (!dep.mods.isEmpty())
        use(dep); 
    } 
  }
  
  private void add(List<Value> buf, List<Value> closed, Value val) {
    if (buf.contains(val))
      return; 
    if (closed.contains(val))
      throw new RuntimeException("Cyclical value dependencies"); 
    closed.add(val);
    for (Value dep : val.deps)
      add(buf, closed, dep); 
    for (Value dep : val.sdeps) {
      if (dep.used)
        add(buf, closed, dep); 
    } 
    buf.add(val);
  }
  
  public void cons(Block blk) {
    for (Value val : this.values)
      val.cons1(); 
    for (Value val : this.values) {
      if (val.forced)
        use(val); 
    } 
    List<Value> used = new LinkedList<>();
    List<Value> closed = new LinkedList<>();
    for (Value val : this.values) {
      if (val.used)
        add(used, closed, val); 
    } 
    for (Value val : used) {
      val.used = true;
      val.cons2(blk);
    } 
  }
  
  public Value ext(Object id, Factory f) {
    Value val = this.ext.get(id);
    if (val == null)
      this.ext.put(id, val = f.make(this)); 
    return val;
  }
}
