package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class LPreOp extends Expression {
  public final LValue op;
  
  public LPreOp(LValue op) {
    this.op = op;
  }
  
  public LPreOp process(Context ctx) {
    try {
      return getClass().getConstructor(new Class[] { LValue.class }).newInstance(new Object[] { this.op.process(ctx) });
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
  
  public static class Inc extends LPreOp {
    public String form() {
      return "++";
    }
    
    public Inc(LValue op) {
      super(op);
    }
  }
  
  public static class Dec extends LPreOp {
    public String form() {
      return "--";
    }
    
    public Dec(LValue op) {
      super(op);
    }
  }
}
