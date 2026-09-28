package haven.glsl;

public class LPick extends LValue {
  public static final String valid = "xyzwrgbastpq";
  
  public final LValue val;
  
  public final char[] el;
  
  public LPick(LValue val, char[] el) {
    for (char c : el) {
      if ("xyzwrgbastpq".indexOf(c) < 0)
        throw new IllegalArgumentException("`" + c + "' is not a valid swizzling component"); 
    } 
    this.val = val;
    this.el = el;
  }
  
  public LPick(LValue val, String el) {
    this(val, el.toCharArray());
  }
  
  public LPick process(Context ctx) {
    return new LPick(this.val.process(ctx), this.el);
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
