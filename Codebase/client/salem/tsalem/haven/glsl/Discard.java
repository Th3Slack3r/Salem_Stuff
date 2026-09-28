package haven.glsl;

public class Discard extends Statement {
  public Discard process(Context ctx) {
    return this;
  }
  
  public void output(Output out) {
    out.write("discard;");
  }
}
