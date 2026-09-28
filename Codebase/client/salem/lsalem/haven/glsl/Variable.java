package haven.glsl;

public abstract class Variable {
   public final Type type;
   public final Symbol name;

   public Variable(Type type, Symbol name) {
      this.type = type;
      this.name = name;
   }

   public Variable.Ref ref() {
      return new Variable.Ref();
   }

   public static class Global extends Variable {
      public Global(Type type, Symbol name) {
         super(type, name);
      }

      public Global(Type type) {
         super(type, new Symbol.Gen());
      }

      public Variable.Global.Ref ref() {
         return new Variable.Global.Ref();
      }

      public boolean defined(Context ctx) {
         for (Toplevel tl : ctx.vardefs) {
            if (tl instanceof Variable.Global.Definition && ((Variable.Global.Definition)tl).var() == this) {
               return true;
            }
         }

         return false;
      }

      public void use(Context ctx) {
         if (!this.defined(ctx)) {
            ctx.vardefs.add(new Variable.Global.Definition());
         }
      }

      public class Definition extends Toplevel {
         public Variable.Global.Definition process(Context ctx) {
            return this;
         }

         @Override
         public void output(Output out) {
            out.write(Global.this.type.name(out.ctx));
            out.write(" ");
            out.write(Global.this.name);
            out.write(";\n");
         }

         private Variable.Global var() {
            return Global.this;
         }
      }

      public class Ref extends Variable.Ref {
         public Variable.Global.Ref process(Context ctx) {
            Global.this.use(ctx);
            return this;
         }
      }
   }

   public static class Implicit extends Variable {
      public Implicit(Type type, Symbol name) {
         super(type, name);
      }
   }

   public class Ref extends LValue {
      public Variable.Ref process(Context ctx) {
         return this;
      }

      @Override
      public void output(Output out) {
         out.write(Variable.this.name);
      }
   }
}
