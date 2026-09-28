package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class PreOp extends Expression {
   public final Expression op;

   public PreOp(Expression op) {
      this.op = op;
   }

   public PreOp process(Context ctx) {
      try {
         return (PreOp)this.getClass().getConstructor(Expression.class).newInstance(this.op.process(ctx));
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

   public static class Neg extends PreOp {
      @Override
      public String form() {
         return "-";
      }

      public Neg(Expression op) {
         super(op);
      }
   }
}
