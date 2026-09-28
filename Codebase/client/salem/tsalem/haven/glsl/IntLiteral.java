package haven.glsl;

public class IntLiteral extends Expression {
  public static final IntLiteral z = new IntLiteral(0);
  
  public static final IntLiteral u = new IntLiteral(1);
  
  public static final IntLiteral n = new IntLiteral(-1);
  
  public final int val;
  
  public IntLiteral(int val) {
    this.val = val;
  }
  
  public IntLiteral process(Context ctx) {
    return this;
  }
  
  public void output(Output out) {
    out.write(Integer.toString(this.val));
  }
}
