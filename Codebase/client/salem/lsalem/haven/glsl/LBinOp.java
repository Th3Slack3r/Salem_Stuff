package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class LBinOp extends Expression {
   public final LValue lhs;
   public final Expression rhs;

   public LBinOp(LValue lhs, Expression rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
   }

   public LBinOp process(Context ctx) {
      try {
         return (LBinOp)this.getClass().getConstructor(LValue.class, Expression.class).newInstance(this.lhs.process(ctx), this.rhs.process(ctx));
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
      this.lhs.output(out);
      out.write(" " + this.form() + " ");
      this.rhs.output(out);
      out.write(")");
   }

   public static class AAdd extends LBinOp {
      @Override
      public String form() {
         return "+=";
      }

      public AAdd(LValue l, Expression r) {
         super(l, r);
      }
   }

   public static class ADiv extends LBinOp {
      @Override
      public String form() {
         return "/=";
      }

      public ADiv(LValue l, Expression r) {
         super(l, r);
      }
   }

   public static class AMul extends LBinOp {
      @Override
      public String form() {
         return "*=";
      }

      public AMul(LValue l, Expression r) {
         super(l, r);
      }
   }

   public static class ASub extends LBinOp {
      @Override
      public String form() {
         return "-=";
      }

      public ASub(LValue l, Expression r) {
         super(l, r);
      }
   }

   public static class Assign extends LBinOp {
      @Override
      public String form() {
         return "=";
      }

      public Assign(LValue l, Expression r) {
         super(l, r);
      }
   }
}
