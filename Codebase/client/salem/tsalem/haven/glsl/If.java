package haven.glsl;

public class If extends Statement {
  public final Expression cond;
  
  public final Statement t;
  
  public final Statement f;
  
  public If(Expression cond, Statement t, Statement f) {
    this.cond = cond;
    this.t = t;
    this.f = f;
  }
  
  public If(Expression cond, Statement t) {
    this(cond, t, null);
  }
  
  public If process(Context ctx) {
    return new If(this.cond.process(ctx), this.t.process(ctx), (this.f == null) ? null : this.f.process(ctx));
  }
  
  public void output(Output out) {
    out.write("if(");
    this.cond.output(out);
    out.write(")");
    if (this.t instanceof Block) {
      Block tb = (Block)this.t;
      out.write(" ");
      tb.trail(out, false);
      if (this.f != null)
        out.write(" else"); 
    } else {
      out.write("\n");
      out.indent++;
      out.indent();
      this.t.output(out);
      out.indent--;
      if (this.f != null) {
        out.write("\n");
        out.indent();
        out.write("else");
      } 
    } 
    if (this.f != null)
      if (this.f instanceof Block) {
        Block fb = (Block)this.f;
        out.write(" ");
        fb.trail(out, false);
      } else {
        out.write("\n");
        out.indent++;
        out.indent();
        this.f.output(out);
        out.indent--;
      }  
  }
}
