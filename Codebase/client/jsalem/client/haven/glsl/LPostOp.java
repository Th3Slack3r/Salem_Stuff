package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class LPostOp extends Expression {
   public final LValue op;

   public LPostOp(LValue op) {
      this.op = op;
   }

   public LPostOp process(Context ctx) {
      try {
         return (LPostOp)this.getClass().getConstructor(LValue.class).newInstance(this.op.process(ctx));
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
      this.op.output(out);
      out.write(this.form());
      out.write(")");
   }

   public static class Dec extends LPostOp {
      @Override
      public String form() {
         return "--";
      }

      public Dec(LValue op) {
         super(op);
      }
   }

   public static class Inc extends LPostOp {
      @Override
      public String form() {
         return "++";
      }

      public Inc(LValue op) {
         super(op);
      }
   }
}
