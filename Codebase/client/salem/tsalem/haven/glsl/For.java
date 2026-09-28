package haven.glsl;

public class For extends Statement {
  public final Expression init;
  
  public final Expression cond;
  
  public final Expression step;
  
  public final Statement body;
  
  public For(Expression init, Expression cond, Expression step, Statement body) {
    this.init = init;
    this.cond = cond;
    this.step = step;
    this.body = body;
  }
  
  public For process(Context ctx) {
    return new For((this.init == null) ? null : this.init.process(ctx), (this.cond == null) ? null : this.cond.process(ctx), (this.step == null) ? null : this.step.process(ctx), this.body.process(ctx));
  }
  
  public void output(Output out) {
    out.write("for(");
    if (this.init != null)
      this.init.output(out); 
    out.write("; ");
    if (this.cond != null)
      this.cond.output(out); 
    out.write("; ");
    if (this.step != null)
      this.step.output(out); 
    out.write(")");
    if (this.body instanceof Block) {
      out.write(" ");
      ((Block)this.body).trail(out, false);
    } else {
      out.write("\n");
      out.indent++;
      out.indent();
      this.body.output(out);
      out.indent--;
    } 
  }
}
