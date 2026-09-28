package haven.glsl;

public abstract class Statement extends Element {
  public static Statement expr(final Expression e) {
    return new Statement() {
        public Statement process(Context ctx) {
          return Statement.expr(e.process(ctx));
        }
        
        public void output(Output out) {
          e.output(out);
          out.write(";");
        }
      };
  }
  
  public abstract Statement process(Context paramContext);
}
