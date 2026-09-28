package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class PreOp extends Expression {
  public final Expression op;
  
  public PreOp(Expression op) {
    this.op = op;
  }
  
  public PreOp process(Context ctx) {
    try {
      return getClass().getConstructor(new Class[] { Expression.class }).newInstance(new Object[] { this.op.process(ctx) });
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    } catch (InstantiationException e) {
      throw new Error(e);
    } catch (IllegalAccessException e) {
      throw new Error(e);
    } catch (InvocationTargetException e) {
      throw new Error(e);
    } 
  }
  
  public void output(Output out) {
    out.write("(");
    out.write(form());
    this.op.output(out);
    out.write(")");
  }
  
  public abstract String form();
  
  public static class Neg extends PreOp {
    public String form() {
      return "-";
    }
    
    public Neg(Expression op) {
      super(op);
    }
  }
}
