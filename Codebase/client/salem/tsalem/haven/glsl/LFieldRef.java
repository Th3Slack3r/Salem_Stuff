package haven.glsl;

public class LFieldRef extends LValue {
  public final LValue val;
  
  public final String el;
  
  public LFieldRef(LValue val, String el) {
    this.val = val;
    this.el = el;
  }
  
  public LFieldRef process(Context ctx) {
    return new LFieldRef(this.val.process(ctx), this.el);
  }
  
  public void output(Output out) {
    out.write("(");
    this.val.output(out);
    out.write(".");
    out.write(this.el);
    out.write(")");
  }
}
