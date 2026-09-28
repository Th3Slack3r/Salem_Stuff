package haven.glsl;

public class Pick extends Expression {
  public static final String valid = "xyzwrgbastpq";
  
  public final Expression val;
  
  public final char[] el;
  
  public Pick(Expression val, char[] el) {
    for (char c : el) {
      if ("xyzwrgbastpq".indexOf(c) < 0)
        throw new IllegalArgumentException("`" + c + "' is not a valid swizzling component"); 
    } 
    this.val = val;
    this.el = el;
  }
  
  public Pick(Expression val, String el) {
    this(val, el.toCharArray());
  }
  
  public Pick process(Context ctx) {
    return new Pick(this.val.process(ctx), this.el);
  }
  
  public void output(Output out) {
    out.write("(");
    this.val.output(out);
    out.write(".");
    for (char c : this.el)
      out.write(c); 
    out.write(")");
  }
}
