package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class BinOp extends Expression {
   public final Expression lhs;
   public final Expression rhs;

   public BinOp(Expression lhs, Expression rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
   }

   public BinOp process(Context ctx) {
      try {
         return (BinOp)this.getClass().getConstructor(Expression.class, Expression.class).newInstance(this.lhs.process(ctx), this.rhs.process(ctx));
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

   public static class And extends BinOp {
      @Override
      public String form() {
         return "&&";
      }

      public And(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Div extends BinOp {
      @Override
      public String form() {
         return "/";
      }

      public Div(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Eq extends BinOp {
      @Override
      public String form() {
         return "==";
      }

      public Eq(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Ge extends BinOp {
      @Override
      public String form() {
         return ">=";
      }

      public Ge(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Gt extends BinOp {
      @Override
      public String form() {
         return ">";
      }

      public Gt(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Le extends BinOp {
      @Override
      public String form() {
         return "<=";
      }

      public Le(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Lt extends BinOp {
      @Override
      public String form() {
         return "<";
      }

      public Lt(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Ne extends BinOp {
      @Override
      public String form() {
         return "!=";
      }

      public Ne(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Or extends BinOp {
      @Override
      public String form() {
         return "||";
      }

      public Or(Expression l, Expression r) {
         super(l, r);
      }
   }

   public static class Sub extends BinOp {
      @Override
      public String form() {
         return "-";
      }

      public Sub(Expression l, Expression r) {
         super(l, r);
      }
   }
}
