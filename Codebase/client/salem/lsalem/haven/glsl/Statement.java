package haven.glsl;

public abstract class Statement extends Element {
   public abstract Statement process(Context var1);

   public static Statement expr(final Expression e) {
      return new Statement() {
         @Override
         public Statement process(Context ctx) {
            return expr(e.process(ctx));
         }

         @Override
         public void output(Output out) {
            e.output(out);
            out.write(";");
         }
      };
   }
}
