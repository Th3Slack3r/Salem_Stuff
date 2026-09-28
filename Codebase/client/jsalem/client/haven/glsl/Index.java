package haven.glsl;

public class Index extends LValue {
   public final Expression val;
   public final Expression idx;

   public Index(Expression val, Expression idx) {
      this.val = val;
      this.idx = idx;
   }

   public Index process(Context ctx) {
      return new Index(this.val.process(ctx), this.idx.process(ctx));
   }

   @Override
   public void output(Output out) {
      out.write("(");
      this.val.output(out);
      out.write("[");
      this.idx.output(out);
      out.write("])");
   }
}
