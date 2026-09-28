package haven.glsl;

public class Vec3Cons extends Expression {
  public static final Vec3Cons z = new Vec3Cons(new Expression[] { FloatLiteral.z, FloatLiteral.z, FloatLiteral.z });
  
  public static final Vec3Cons u = new Vec3Cons(new Expression[] { FloatLiteral.u, FloatLiteral.u, FloatLiteral.u });
  
  public final Expression[] els;
  
  public Vec3Cons(Expression... els) {
    if (els.length < 1 || els.length > 3)
      throw new RuntimeException("Invalid number of arguments for vec3: " + els.length); 
    this.els = els;
  }
  
  public Vec3Cons process(Context ctx) {
    Expression[] nels = new Expression[this.els.length];
    for (int i = 0; i < this.els.length; i++)
      nels[i] = this.els[i].process(ctx); 
    return new Vec3Cons(nels);
  }
  
  public void output(Output out) {
    out.write("vec3(");
    this.els[0].output(out);
    for (int i = 1; i < this.els.length; i++) {
      out.write(", ");
      this.els[i].output(out);
    } 
    out.write(")");
  }
}
