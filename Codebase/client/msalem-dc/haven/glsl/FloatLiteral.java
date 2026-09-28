package haven.glsl;

public class FloatLiteral extends Expression {
   public static final FloatLiteral z = new FloatLiteral(0.0);
   public static final FloatLiteral u = new FloatLiteral(1.0);
   public static final FloatLiteral n = new FloatLiteral(-1.0);
   public final double val;

   public FloatLiteral(double val) {
      this.val = val;
   }

   public FloatLiteral process(Context ctx) {
      return this;
   }

   @Override
   public void output(Output out) {
      out.write(Double.toString(this.val));
   }
}
