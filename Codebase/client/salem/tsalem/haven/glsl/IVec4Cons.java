package haven.glsl;

public class IVec4Cons extends Expression {
  public static final IVec4Cons z = new IVec4Cons(new Expression[] { IntLiteral.z, IntLiteral.z, IntLiteral.z, IntLiteral.z });
  
  public static final IVec4Cons u = new IVec4Cons(new Expression[] { IntLiteral.u, IntLiteral.u, IntLiteral.u, IntLiteral.u });
  
  public final Expression[] els;
  
  public IVec4Cons(Expression... els) {
    if (els.length < 1 || els.length > 4)
      throw new RuntimeException("Invalid number of arguments for ivec4: " + els.length); 
    this.els = els;
  }
  
  public IVec4Cons process(Context ctx) {
    Expression[] nels = new Expression[this.els.length];
    for (int i = 0; i < this.els.length; i++)
      nels[i] = this.els[i].process(ctx); 
    return new IVec4Cons(nels);
  }
  
  public void output(Output out) {
    out.write("ivec4(");
    this.els[0].output(out);
    for (int i = 1; i < this.els.length; i++) {
      out.write(", ");
      this.els[i].output(out);
    } 
    out.write(")");
  }
}
