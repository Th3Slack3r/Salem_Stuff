package haven.glsl;

public class Return extends Statement {
   public final Expression rv;

   public Return(Expression rv) {
      this.rv = rv;
   }

   public Return process(Context ctx) {
      return new Return(this.rv.process(ctx));
   }

   @Override
   public void output(Output out) {
      out.write("return ");
      this.rv.output(out);
      out.write(";");
   }
}
