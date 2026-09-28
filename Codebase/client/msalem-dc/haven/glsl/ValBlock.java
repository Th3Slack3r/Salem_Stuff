package haven.glsl;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ValBlock {
   private static final ThreadLocal<ValBlock.Value> processing = new ThreadLocal<>();
   private final Collection<ValBlock.Value> values = new LinkedList<>();
   private final Map<Object, ValBlock.Value> ext = new IdentityHashMap<>();

   private void use(ValBlock.Value val) {
      if (!val.used) {
         val.used = true;

         for (ValBlock.Value dep : val.deps) {
            this.use(dep);
         }

         for (ValBlock.Value dep : val.sdeps) {
            if (!dep.mods.isEmpty()) {
               this.use(dep);
            }
         }
      }
   }

   private void add(List<ValBlock.Value> buf, List<ValBlock.Value> closed, ValBlock.Value val) {
      if (!buf.contains(val)) {
         if (closed.contains(val)) {
            throw new RuntimeException("Cyclical value dependencies");
         } else {
            closed.add(val);

            for (ValBlock.Value dep : val.deps) {
               this.add(buf, closed, dep);
            }

            for (ValBlock.Value dep : val.sdeps) {
               if (dep.used) {
                  this.add(buf, closed, dep);
               }
            }

            buf.add(val);
         }
      }
   }

   public void cons(Block blk) {
      for (ValBlock.Value val : this.values) {
         val.cons1();
      }

      for (ValBlock.Value val : this.values) {
         if (val.forced) {
            this.use(val);
         }
      }

      List<ValBlock.Value> used = new LinkedList<>();
      List<ValBlock.Value> closed = new LinkedList<>();

      for (ValBlock.Value valx : this.values) {
         if (valx.used) {
            this.add(used, closed, valx);
         }
      }

      for (ValBlock.Value valxx : used) {
         valxx.used = true;
         valxx.cons2(blk);
      }
   }

   public ValBlock.Value ext(Object id, ValBlock.Factory f) {
      ValBlock.Value val = this.ext.get(id);
      if (val == null) {
         this.ext.put(id, val = f.make(this));
      }

      return val;
   }

   public interface Factory {
      ValBlock.Value make(ValBlock var1);
   }

   public abstract class Group {
      private final Collection<ValBlock.Group.GValue> values = new LinkedList<>();
      private final Collection<ValBlock.Value> deps = new LinkedList<>();
      private final Collection<ValBlock.Value> sdeps = new LinkedList<>();
      private int state = 0;

      protected abstract void cons1();

      protected abstract void cons2(Block var1);

      public void depend(ValBlock.Value dep) {
         for (ValBlock.Group.GValue val : this.values) {
            val.depend1(dep);
         }
      }

      public void softdep(ValBlock.Value dep) {
         for (ValBlock.Group.GValue val : this.values) {
            val.softdep1(dep);
         }
      }

      public class GValue extends ValBlock.Value {
         public Expression modexpr;

         public GValue(Type type, Symbol name) {
            super(type, name);

            for (ValBlock.Value dep : Group.this.deps) {
               this.depend1(dep);
            }

            for (ValBlock.Value dep : Group.this.sdeps) {
               this.softdep1(dep);
            }

            Group.this.values.add(this);
         }

         public GValue(Type type) {
            this(type, new Symbol.Gen());
         }

         @Override
         protected void cons1() {
            if (Group.this.state < 1) {
               Group.this.cons1();
               Group.this.state = 1;
            }

            Expression in = this.ref();
            this.modexpr = this.modexpr(in);
            if (this.modexpr == in) {
               this.modexpr = null;
            }
         }

         @Override
         protected void cons2(Block blk) {
            if (Group.this.state < 2) {
               Group.this.cons2(blk);
               Group.this.state = 2;
            }
         }

         public void addmods(Block blk) {
            if (this.modexpr != null) {
               blk.add(Cons.ass(this.var, this.modexpr));
            }
         }

         @Override
         public final Expression root() {
            throw new RuntimeException("root() is not applicable for group values");
         }

         private void depend1(ValBlock.Value dep) {
            super.depend(dep);
         }

         @Override
         public void depend(ValBlock.Value dep) {
            Group.this.depend(dep);
         }

         private void softdep1(ValBlock.Value dep) {
            super.softdep(dep);
         }

         @Override
         public void softdep(ValBlock.Value dep) {
            Group.this.softdep(dep);
         }
      }
   }

   public abstract class Value {
      public final Type type;
      public final Symbol name;
      public boolean used;
      public Variable var;
      protected Expression init;
      private final Collection<ValBlock.Value> deps = new LinkedList<>();
      private final Collection<ValBlock.Value> sdeps = new LinkedList<>();
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

      public abstract Expression root();

      public Expression modexpr(Expression expr) {
         for (Macro1<Expression> mod : this.mods) {
            expr = mod.expand(expr);
         }

         return expr;
      }

      protected void cons1() {
         ValBlock.processing.set(this);

         try {
            this.init = this.modexpr(this.root());
         } finally {
            ValBlock.processing.remove();
         }
      }

      protected void cons2(Block blk) {
         this.var = blk.local(this.type, this.name, this.init);
      }

      public Expression ref() {
         return new Expression() {
            @Override
            public Expression process(Context ctx) {
               if (Value.this.var == null) {
                  throw new IllegalStateException("Value reference processed before being constructed");
               } else {
                  return Value.this.var.ref().process(ctx);
               }
            }
         };
      }

      public Expression depref() {
         if (ValBlock.processing.get() == null) {
            throw new IllegalStateException("Dependent value reference outside construction");
         } else {
            ValBlock.processing.get().depend(this);
            return this.ref();
         }
      }

      public void force() {
         this.forced = true;
      }

      public void depend(ValBlock.Value dep) {
         if (!this.deps.contains(dep)) {
            this.deps.add(dep);
         }
      }

      public void softdep(ValBlock.Value dep) {
         if (!this.sdeps.contains(dep)) {
            this.sdeps.add(dep);
         }
      }
   }
}
