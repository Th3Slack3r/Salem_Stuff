package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class LBinOp extends Expression {
  public final LValue lhs;
  
  public final Expression rhs;
  
  public LBinOp(LValue lhs, Expression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public LBinOp process(Context ctx) {
    try {
      return getClass().getConstructor(new Class[] { LValue.class, Expression.class }).newInstance(new Object[] { this.lhs.process(ctx), this.rhs.process(ctx) });
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
    this.lhs.output(out);
    out.write(" " + form() + " ");
    this.rhs.output(out);
    out.write(")");
  }
  
  public abstract String form();
  
  public static class Assign extends LBinOp {
    public String form() {
      return "=";
    }
    
    public Assign(LValue l, Expression r) {
      super(l, r);
    }
  }
  
  public static class AAdd extends LBinOp {
    public String form() {
      return "+=";
    }
    
    public AAdd(LValue l, Expression r) {
      super(l, r);
    }
  }
  
  public static class ASub extends LBinOp {
    public String form() {
      return "-=";
    }
    
    public ASub(LValue l, Expression r) {
      super(l, r);
    }
  }
  
  public static class AMul extends LBinOp {
    public String form() {
      return "*=";
    }
    
    public AMul(LValue l, Expression r) {
      super(l, r);
    }
  }
  
  public static class ADiv extends LBinOp {
    public String form() {
      return "/=";
    }
    
    public ADiv(LValue l, Expression r) {
      super(l, r);
    }
  }
}
