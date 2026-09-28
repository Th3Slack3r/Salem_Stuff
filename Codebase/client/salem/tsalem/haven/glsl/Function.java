package haven.glsl;

import java.util.LinkedList;
import java.util.List;

public abstract class Function {
  public final Symbol name;
  
  public final List<Parameter> pars = new LinkedList<>();
  
  public Function(Symbol name) {
    this.name = name;
  }
  
  public static class Def extends Function {
    public final Type type;
    
    public final Block code;
    
    private boolean fin = false;
    
    public Def(Type type, Symbol name) {
      super(name);
      this.type = type;
      this.code = new Block(new Statement[0]);
    }
    
    public Def(Type type) {
      this(type, new Symbol.Gen());
    }
    
    private class Definition extends Toplevel {
      private final Block code;
      
      private Definition(Context ctx) {
        this.code = Function.Def.this.code.process(ctx);
      }
      
      public Definition process(Context ctx) {
        throw new RuntimeException();
      }
      
      public void output(Output out) {
        Function.Def.this.prototype(out);
        out.write("\n");
        this.code.output(out);
      }
      
      private Function.Def fun() {
        return Function.Def.this;
      }
    }
    
    private class Call extends Expression {
      private final Expression[] params;
      
      private Call(Expression... params) {
        this.params = params;
      }
      
      public Call process(Context ctx) {
        Function.Def.this.define(ctx);
        Expression[] proc = new Expression[this.params.length];
        for (int i = 0; i < this.params.length; i++)
          proc[i] = this.params[i].process(ctx); 
        return new Call(proc);
      }
      
      public void output(Output out) {
        out.write(Function.Def.this.name);
        out.write("(");
        if (this.params.length > 0) {
          this.params[0].output(out);
          for (int i = 1; i < this.params.length; i++) {
            out.write(", ");
            this.params[i].output(out);
          } 
        } 
        out.write(")");
      }
    }
    
    protected void cons() {}
    
    public void define(Context ctx) {
      if (!this.fin) {
        cons();
        this.fin = true;
      } 
      for (Toplevel tl : ctx.fundefs) {
        if (tl instanceof Definition && ((Definition)tl).fun() == this)
          return; 
      } 
      ctx.fundefs.add(new Definition(ctx));
    }
    
    public void prototype(Output out) {
      out.write(this.type.name(out.ctx));
      out.write(" ");
      out.write(this.name);
      out.write("(");
      boolean f = true;
      for (Function.Parameter par : this.pars) {
        if (!f)
          out.write(", "); 
        f = false;
        switch (par.dir) {
          case OUT:
            out.write("out ");
            break;
          case INOUT:
            out.write("inout ");
            break;
        } 
        out.write(par.type.name(out.ctx));
        out.write(" ");
        out.write(par.name);
      } 
      out.write(")");
    }
    
    public Expression call(Expression... params) {
      ckparams(params);
      return new Call(params);
    }
    
    public Type type(Expression... params) {
      return this.type;
    }
    
    public void code(Statement stmt) {
      this.code.add(stmt);
    }
    
    public void code(Expression expr) {
      this.code.add(expr);
    }
  }
  
  public static class Builtin extends Function {
    private final Type type;
    
    public Builtin(Type type, Symbol name, int nargs) {
      super(name);
      this.type = type;
      for (int i = 0; i < nargs; i++)
        param(Function.PDir.IN, null); 
    }
    
    private class Call extends Expression {
      private final Expression[] params;
      
      private Call(Expression... params) {
        this.params = params;
      }
      
      public Call process(Context ctx) {
        Expression[] proc = new Expression[this.params.length];
        for (int i = 0; i < this.params.length; i++)
          proc[i] = this.params[i].process(ctx); 
        return new Call(proc);
      }
      
      public void output(Output out) {
        out.write(Function.Builtin.this.name);
        out.write("(");
        if (this.params.length > 0) {
          this.params[0].output(out);
          for (int i = 1; i < this.params.length; i++) {
            out.write(", ");
            this.params[i].output(out);
          } 
        } 
        out.write(")");
      }
    }
    
    public Type type(Expression... params) {
      if (this.type == null)
        throw new NullPointerException("type"); 
      return this.type;
    }
    
    public Expression call(Expression... params) {
      ckparams(params);
      return new Call(params);
    }
    
    public static final Builtin sin = new Builtin(null, new Symbol.Fix("sin"), 1);
    
    public static final Builtin cos = new Builtin(null, new Symbol.Fix("cos"), 1);
    
    public static final Builtin tan = new Builtin(null, new Symbol.Fix("tan"), 1);
    
    public static final Builtin asin = new Builtin(null, new Symbol.Fix("asin"), 1);
    
    public static final Builtin acos = new Builtin(null, new Symbol.Fix("acos"), 1);
    
    public static final Builtin atan = new Builtin(null, new Symbol.Fix("atan"), 1);
    
    public static final Builtin pow = new Builtin(null, new Symbol.Fix("pow"), 2);
    
    public static final Builtin exp = new Builtin(null, new Symbol.Fix("exp"), 1);
    
    public static final Builtin log = new Builtin(null, new Symbol.Fix("log"), 1);
    
    public static final Builtin exp2 = new Builtin(null, new Symbol.Fix("exp2"), 1);
    
    public static final Builtin log2 = new Builtin(null, new Symbol.Fix("log2"), 1);
    
    public static final Builtin sqrt = new Builtin(null, new Symbol.Fix("sqrt"), 1);
    
    public static final Builtin inversesqrt = new Builtin(null, new Symbol.Fix("inversesqrt"), 1);
    
    public static final Builtin abs = new Builtin(null, new Symbol.Fix("abs"), 1);
    
    public static final Builtin sign = new Builtin(null, new Symbol.Fix("sign"), 1);
    
    public static final Builtin floor = new Builtin(null, new Symbol.Fix("floor"), 1);
    
    public static final Builtin ceil = new Builtin(null, new Symbol.Fix("ceil"), 1);
    
    public static final Builtin fract = new Builtin(null, new Symbol.Fix("fract"), 1);
    
    public static final Builtin mod = new Builtin(null, new Symbol.Fix("mod"), 2);
    
    public static final Builtin min = new Builtin(null, new Symbol.Fix("min"), 2);
    
    public static final Builtin max = new Builtin(null, new Symbol.Fix("max"), 2);
    
    public static final Builtin clamp = new Builtin(null, new Symbol.Fix("clamp"), 3);
    
    public static final Builtin mix = new Builtin(null, new Symbol.Fix("mix"), 3);
    
    public static final Builtin step = new Builtin(null, new Symbol.Fix("step"), 2);
    
    public static final Builtin smoothstep = new Builtin(null, new Symbol.Fix("smoothstep"), 3);
    
    public static final Builtin length = new Builtin(Type.FLOAT, new Symbol.Fix("length"), 1);
    
    public static final Builtin distance = new Builtin(Type.FLOAT, new Symbol.Fix("distance"), 2);
    
    public static final Builtin dot = new Builtin(Type.FLOAT, new Symbol.Fix("dot"), 2);
    
    public static final Builtin cross = new Builtin(Type.VEC3, new Symbol.Fix("cross"), 2);
    
    public static final Builtin normalize = new Builtin(null, new Symbol.Fix("normalize"), 1);
    
    public static final Builtin reflect = new Builtin(null, new Symbol.Fix("reflect"), 2);
    
    public static final Builtin transpose = new Builtin(null, new Symbol.Fix("transpose"), 1);
    
    public static final Builtin texture2D = new Builtin(Type.VEC4, new Symbol.Fix("texture2D"), 2);
    
    public static final Builtin texture3D = new Builtin(Type.VEC4, new Symbol.Fix("texture3D"), 2);
    
    public static final Builtin textureCube = new Builtin(Type.VEC4, new Symbol.Fix("textureCube"), 2);
    
    public static final Builtin texelFetch = new Builtin(Type.VEC4, new Symbol.Fix("texelFetch"), 3);
  }
  
  public enum PDir {
    IN, OUT, INOUT;
  }
  
  public static class Parameter extends Variable {
    public final Function.PDir dir;
    
    private Parameter(Function.PDir dir, Type type, Symbol name) {
      super(type, name);
      this.dir = dir;
    }
  }
  
  public Parameter param(PDir dir, Type type, Symbol name) {
    Parameter ret = new Parameter(dir, type, name);
    this.pars.add(ret);
    return ret;
  }
  
  public Parameter param(PDir dir, Type type, String prefix) {
    return param(dir, type, new Symbol.Gen(prefix));
  }
  
  public Parameter param(PDir dir, Type type) {
    return param(dir, type, new Symbol.Gen());
  }
  
  public Function param1(PDir dir, Type type) {
    param(dir, type);
    return this;
  }
  
  void ckparams(Expression... params) {
    if (params.length != this.pars.size())
      throw new RuntimeException(String.format("Wrong number of arguments to %s; expected %d, got %d", new Object[] { this.name, Integer.valueOf(this.pars.size()), Integer.valueOf(params.length) })); 
    int i = 0;
    for (Parameter par : this.pars) {
      if ((par.dir == PDir.OUT || par.dir == PDir.INOUT) && !(params[i] instanceof LValue))
        throw new RuntimeException(String.format("Must have l-value for %s parameter %d to %s", new Object[] { par.dir, Integer.valueOf(i), this.name })); 
      i++;
    } 
  }
  
  public abstract Type type(Expression... paramVarArgs);
  
  public abstract Expression call(Expression... paramVarArgs);
}
