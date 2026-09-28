package haven.glsl;

public abstract class Variable {
  public final Type type;
  
  public final Symbol name;
  
  public Variable(Type type, Symbol name) {
    this.type = type;
    this.name = name;
  }
  
  public class Ref extends LValue {
    public Ref process(Context ctx) {
      return this;
    }
    
    public void output(Output out) {
      out.write(Variable.this.name);
    }
  }
  
  public Ref ref() {
    return new Ref();
  }
  
  public static class Implicit extends Variable {
    public Implicit(Type type, Symbol name) {
      super(type, name);
    }
  }
  
  public static class Global extends Variable {
    public Global(Type type, Symbol name) {
      super(type, name);
    }
    
    public Global(Type type) {
      super(type, new Symbol.Gen());
    }
    
    public class Ref extends Variable.Ref {
      public Ref process(Context ctx) {
        Variable.Global.this.use(ctx);
        return this;
      }
    }
    
    public Ref ref() {
      return new Ref();
    }
    
    public boolean defined(Context ctx) {
      for (Toplevel tl : ctx.vardefs) {
        if (tl instanceof Definition && ((Definition)tl).var() == this)
          return true; 
      } 
      return false;
    }
    
    public void use(Context ctx) {
      if (!defined(ctx))
        ctx.vardefs.add(new Definition()); 
    }
    
    public class Definition extends Toplevel {
      public Definition process(Context ctx) {
        return this;
      }
      
      public void output(Output out) {
        out.write(Variable.Global.this.type.name(out.ctx));
        out.write(" ");
        out.write(Variable.Global.this.name);
        out.write(";\n");
      }
      
      private Variable.Global var() {
        return Variable.Global.this;
      }
    }
  }
}
