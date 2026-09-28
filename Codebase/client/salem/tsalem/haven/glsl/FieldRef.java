package haven.glsl;

public class FieldRef extends Expression {
  public final Expression val;
  
  public final String el;
  
  public FieldRef(Expression val, String el) {
    this.val = val;
    this.el = el;
  }
  
  public FieldRef process(Context ctx) {
    return new FieldRef(this.val.process(ctx), this.el);
  }
  
  public void output(Output out) {
    out.write("(");
    this.val.output(out);
    out.write(".");
    out.write(this.el);
    out.write(")");
  }
}
