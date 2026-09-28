package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class LPreOp extends Expression {
   public final LValue op;

   public LPreOp(LValue op) {
      this.op = op;
   }

   public LPreOp process(Context ctx) {
      try {
         return (LPreOp)this.getClass().getConstructor(LValue.class).newInstance(this.op.process(ctx));
      } catch (NoSuchMethodException var3) {
         throw new Error(var3);
      } catch (InstantiationException var4) {
         throw new Error(var4);
      } catch (IllegalAccessException var5) {
         throw new Error(var5);
      } catch (InvocationTargetException var6) {
         throw new Error(var6);
      }
   }

   public abstract String form();

   @Override
   public void output(Output out) {
      out.write("(");
      out.write(this.form());
      this.op.output(out);
      out.write(")");
   }

   public static class Dec extends LPreOp {
      @Override
      public String form() {
         return "--";
      }

      public Dec(LValue op) {
         super(op);
      }
   }

   public static class Inc extends LPreOp {
      @Override
      public String form() {
         return "++";
      }

      public Inc(LValue op) {
         super(op);
      }
   }
}
