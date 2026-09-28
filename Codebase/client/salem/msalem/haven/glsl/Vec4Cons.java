package haven.glsl;

public class Vec4Cons extends Expression {
   public static final Vec4Cons z = new Vec4Cons(FloatLiteral.z, FloatLiteral.z, FloatLiteral.z, FloatLiteral.z);
   public static final Vec4Cons u = new Vec4Cons(FloatLiteral.u, FloatLiteral.u, FloatLiteral.u, FloatLiteral.u);
   public final Expression[] els;

   public Vec4Cons(Expression... els) {
      if (els.length >= 1 && els.length <= 4) {
         this.els = els;
      } else {
         throw new RuntimeException("Invalid number of arguments for vec4: " + els.length);
      }
   }

   public Vec4Cons process(Context ctx) {
      Expression[] nels = new Expression[this.els.length];

      for (int i = 0; i < this.els.length; i++) {
         nels[i] = this.els[i].process(ctx);
      }

      return new Vec4Cons(nels);
   }

   @Override
   public void output(Output out) {
      out.write("vec4(");
      this.els[0].output(out);

      for (int i = 1; i < this.els.length; i++) {
         out.write(", ");
         this.els[i].output(out);
      }

      out.write(")");
   }
}
