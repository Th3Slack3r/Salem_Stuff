package haven.glsl;

public class IVec2Cons extends Expression {
  public static final IVec2Cons z = new IVec2Cons(new Expression[] { IntLiteral.z, IntLiteral.z });
  
  public static final IVec2Cons u = new IVec2Cons(new Expression[] { IntLiteral.u, IntLiteral.u });
  
  public final Expression[] els;
  
  public IVec2Cons(Expression... els) {
    if (els.length < 1 || els.length > 2)
      throw new RuntimeException("Invalid number of arguments for ivec2: " + els.length); 
    this.els = els;
  }
  
  public IVec2Cons process(Context ctx) {
    Expression[] nels = new Expression[this.els.length];
    for (int i = 0; i < this.els.length; i++)
      nels[i] = this.els[i].process(ctx); 
    return new IVec2Cons(nels);
  }
  
  public void output(Output out) {
    out.write("ivec2(");
    this.els[0].output(out);
    for (int i = 1; i < this.els.length; i++) {
      out.write(", ");
      this.els[i].output(out);
    } 
    out.write(")");
  }
}
