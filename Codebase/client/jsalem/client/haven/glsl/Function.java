package haven.glsl;

import java.util.LinkedList;
import java.util.List;

public abstract class Function {
   public final Symbol name;
   public final List<Function.Parameter> pars = new LinkedList<>();

   public Function(Symbol name) {
      this.name = name;
   }

   public Function.Parameter param(Function.PDir dir, Type type, Symbol name) {
      Function.Parameter ret = new Function.Parameter(dir, type, name);
      this.pars.add(ret);
      return ret;
   }

   public Function.Parameter param(Function.PDir dir, Type type, String prefix) {
      return this.param(dir, type, new Symbol.Gen(prefix));
   }

   public Function.Parameter param(Function.PDir dir, Type type) {
      return this.param(dir, type, new Symbol.Gen());
   }

   public Function param1(Function.PDir dir, Type type) {
      this.param(dir, type);
      return this;
   }

   void ckparams(Expression... params) {
      if (params.length != this.pars.size()) {
         throw new RuntimeException(String.format("Wrong number of arguments to %s; expected %d, got %d", this.name, this.pars.size(), params.length));
      } else {
         int i = 0;

         for (Function.Parameter par : this.pars) {
            if ((par.dir == Function.PDir.OUT || par.dir == Function.PDir.INOUT) && !(params[i] instanceof LValue)) {
               throw new RuntimeException(String.format("Must have l-value for %s parameter %d to %s", par.dir, i, this.name));
            }

            i++;
         }
      }
   }

   public abstract Type type(Expression... var1);

   public abstract Expression call(Expression... var1);

   public static class Builtin extends Function {
      private final Type type;
      public static final Function.Builtin sin = new Function.Builtin(null, new Symbol.Fix("sin"), 1);
      public static final Function.Builtin cos = new Function.Builtin(null, new Symbol.Fix("cos"), 1);
      public static final Function.Builtin tan = new Function.Builtin(null, new Symbol.Fix("tan"), 1);
      public static final Function.Builtin asin = new Function.Builtin(null, new Symbol.Fix("asin"), 1);
      public static final Function.Builtin acos = new Function.Builtin(null, new Symbol.Fix("acos"), 1);
      public static final Function.Builtin atan = new Function.Builtin(null, new Symbol.Fix("atan"), 1);
      public static final Function.Builtin pow = new Function.Builtin(null, new Symbol.Fix("pow"), 2);
      public static final Function.Builtin exp = new Function.Builtin(null, new Symbol.Fix("exp"), 1);
      public static final Function.Builtin log = new Function.Builtin(null, new Symbol.Fix("log"), 1);
      public static final Function.Builtin exp2 = new Function.Builtin(null, new Symbol.Fix("exp2"), 1);
      public static final Function.Builtin log2 = new Function.Builtin(null, new Symbol.Fix("log2"), 1);
      public static final Function.Builtin sqrt = new Function.Builtin(null, new Symbol.Fix("sqrt"), 1);
      public static final Function.Builtin inversesqrt = new Function.Builtin(null, new Symbol.Fix("inversesqrt"), 1);
      public static final Function.Builtin abs = new Function.Builtin(null, new Symbol.Fix("abs"), 1);
      public static final Function.Builtin sign = new Function.Builtin(null, new Symbol.Fix("sign"), 1);
      public static final Function.Builtin floor = new Function.Builtin(null, new Symbol.Fix("floor"), 1);
      public static final Function.Builtin ceil = new Function.Builtin(null, new Symbol.Fix("ceil"), 1);
      public static final Function.Builtin fract = new Function.Builtin(null, new Symbol.Fix("fract"), 1);
      public static final Function.Builtin mod = new Function.Builtin(null, new Symbol.Fix("mod"), 2);
      public static final Function.Builtin min = new Function.Builtin(null, new Symbol.Fix("min"), 2);
      public static final Function.Builtin max = new Function.Builtin(null, new Symbol.Fix("max"), 2);
      public static final Function.Builtin clamp = new Function.Builtin(null, new Symbol.Fix("clamp"), 3);
      public static final Function.Builtin mix = new Function.Builtin(null, new Symbol.Fix("mix"), 3);
      public static final Function.Builtin step = new Function.Builtin(null, new Symbol.Fix("step"), 2);
      public static final Function.Builtin smoothstep = new Function.Builtin(null, new Symbol.Fix("smoothstep"), 3);
      public static final Function.Builtin length = new Function.Builtin(Type.FLOAT, new Symbol.Fix("length"), 1);
      public static final Function.Builtin distance = new Function.Builtin(Type.FLOAT, new Symbol.Fix("distance"), 2);
      public static final Function.Builtin dot = new Function.Builtin(Type.FLOAT, new Symbol.Fix("dot"), 2);
      public static final Function.Builtin cross = new Function.Builtin(Type.VEC3, new Symbol.Fix("cross"), 2);
      public static final Function.Builtin normalize = new Function.Builtin(null, new Symbol.Fix("normalize"), 1);
      public static final Function.Builtin reflect = new Function.Builtin(null, new Symbol.Fix("reflect"), 2);
      public static final Function.Builtin transpose = new Function.Builtin(null, new Symbol.Fix("transpose"), 1);
      public static final Function.Builtin texture2D = new Function.Builtin(Type.VEC4, new Symbol.Fix("texture2D"), 2);
      public static final Function.Builtin texture3D = new Function.Builtin(Type.VEC4, new Symbol.Fix("texture3D"), 2);
      public static final Function.Builtin textureCube = new Function.Builtin(Type.VEC4, new Symbol.Fix("textureCube"), 2);
      public static final Function.Builtin texelFetch = new Function.Builtin(Type.VEC4, new Symbol.Fix("texelFetch"), 3);

      public Builtin(Type type, Symbol name, int nargs) {
         super(name);
         this.type = type;

         for (int i = 0; i < nargs; i++) {
            this.param(Function.PDir.IN, null);
         }
      }

      @Override
      public Type type(Expression... params) {
         if (this.type == null) {
            throw new NullPointerException("type");
         } else {
            return this.type;
         }
      }

      @Override
      public Expression call(Expression... params) {
         this.ckparams(params);
         return new Function.Builtin.Call(params);
      }

      private class Call extends Expression {
         private final Expression[] params;

         private Call(Expression... params) {
            this.params = params;
         }

         public Function.Builtin.Call process(Context ctx) {
            Expression[] proc = new Expression[this.params.length];

            for (int i = 0; i < this.params.length; i++) {
               proc[i] = this.params[i].process(ctx);
            }

            return Builtin.this.new Call(proc);
         }

         @Override
         public void output(Output out) {
            out.write(Builtin.this.name);
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
   }

   public static class Def extends Function {
      public final Type type;
      public final Block code;
      private boolean fin = false;

      public Def(Type type, Symbol name) {
         super(name);
         this.type = type;
         this.code = new Block();
      }

      public Def(Type type) {
         this(type, new Symbol.Gen());
      }

      protected void cons() {
      }

      public void define(Context ctx) {
         if (!this.fin) {
            this.cons();
            this.fin = true;
         }

         for (Toplevel tl : ctx.fundefs) {
            if (tl instanceof Function.Def.Definition && ((Function.Def.Definition)tl).fun() == this) {
               return;
            }
         }

         ctx.fundefs.add(new Function.Def.Definition(ctx));
      }

      public void prototype(Output out) {
         out.write(this.type.name(out.ctx));
         out.write(" ");
         out.write(this.name);
         out.write("(");
         boolean f = true;

         for (Function.Parameter par : this.pars) {
            if (!f) {
               out.write(", ");
            }

            f = false;
            switch (par.dir) {
               case IN:
               default:
                  break;
               case OUT:
                  out.write("out ");
                  break;
               case INOUT:
                  out.write("inout ");
            }

            out.write(par.type.name(out.ctx));
            out.write(" ");
            out.write(par.name);
         }

         out.write(")");
      }

      @Override
      public Expression call(Expression... params) {
         this.ckparams(params);
         return new Function.Def.Call(params);
      }

      @Override
      public Type type(Expression... params) {
         return this.type;
      }

      public void code(Statement stmt) {
         this.code.add(stmt);
      }

      public void code(Expression expr) {
         this.code.add(expr);
      }

      private class Call extends Expression {
         private final Expression[] params;

         private Call(Expression... params) {
            this.params = params;
         }

         public Function.Def.Call process(Context ctx) {
            Def.this.define(ctx);
            Expression[] proc = new Expression[this.params.length];

            for (int i = 0; i < this.params.length; i++) {
               proc[i] = this.params[i].process(ctx);
            }

            return Def.this.new Call(proc);
         }

         @Override
         public void output(Output out) {
            out.write(Def.this.name);
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

      private class Definition extends Toplevel {
         private final Block code;

         private Definition(Context ctx) {
            this.code = Def.this.code.process(ctx);
         }

         public Function.Def.Definition process(Context ctx) {
            throw new RuntimeException();
         }

         @Override
         public void output(Output out) {
            Def.this.prototype(out);
            out.write("\n");
            this.code.output(out);
         }

         private Function.Def fun() {
            return Def.this;
         }
      }
   }

   public static enum PDir {
      IN,
      OUT,
      INOUT;
   }

   public static class Parameter extends Variable {
      public final Function.PDir dir;

      private Parameter(Function.PDir dir, Type type, Symbol name) {
         super(type, name);
         this.dir = dir;
      }
   }
}
