package haven.glsl;

public class Mat3Cons extends Expression {
  public final Expression[] els;
  
  public Mat3Cons(Expression... els) {
    if (els.length < 1 || els.length > 9)
      throw new RuntimeException("Invalid number of arguments for mat3: " + els.length); 
    this.els = els;
  }
  
  public Mat3Cons process(Context ctx) {
    Expression[] nels = new Expression[this.els.length];
    for (int i = 0; i < this.els.length; i++)
      nels[i] = this.els[i].process(ctx); 
    return new Mat3Cons(nels);
  }
  
  public void output(Output out) {
    out.write("mat3(");
    this.els[0].output(out);
    for (int i = 1; i < this.els.length; i++) {
      out.write(", ");
      this.els[i].output(out);
    } 
    out.write(")");
  }
}
