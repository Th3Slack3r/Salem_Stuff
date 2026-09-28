package haven.glsl;

public class Vec2Cons extends Expression {
  public static final Vec2Cons z = new Vec2Cons(new Expression[] { FloatLiteral.z, FloatLiteral.z });
  
  public static final Vec2Cons u = new Vec2Cons(new Expression[] { FloatLiteral.u, FloatLiteral.u });
  
  public final Expression[] els;
  
  public Vec2Cons(Expression... els) {
    if (els.length < 1 || els.length > 2)
      throw new RuntimeException("Invalid number of arguments for vec2: " + els.length); 
    this.els = els;
  }
  
  public Vec2Cons process(Context ctx) {
    Expression[] nels = new Expression[this.els.length];
    for (int i = 0; i < this.els.length; i++)
      nels[i] = this.els[i].process(ctx); 
    return new Vec2Cons(nels);
  }
  
  public void output(Output out) {
    out.write("vec2(");
    this.els[0].output(out);
    for (int i = 1; i < this.els.length; i++) {
      out.write(", ");
      this.els[i].output(out);
    } 
    out.write(")");
  }
}
