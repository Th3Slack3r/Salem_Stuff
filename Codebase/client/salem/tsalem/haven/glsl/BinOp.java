package haven.glsl;

import java.lang.reflect.InvocationTargetException;

public abstract class BinOp extends Expression {
  public final Expression lhs;
  
  public final Expression rhs;
  
  public BinOp(Expression lhs, Expression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public BinOp process(Context ctx) {
    try {
      return getClass().getConstructor(new Class[] { Expression.class, Expression.class }).newInstance(new Object[] { this.lhs.process(ctx), this.rhs.process(ctx) });
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
  
  public static class Eq extends BinOp {
    public String form() {
      return "==";
    }
    
    public Eq(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class Ne extends BinOp {
    public String form() {
      return "!=";
    }
    
    public Ne(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class Lt extends BinOp {
    public String form() {
      return "<";
    }
    
    public Lt(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class Gt extends BinOp {
    public String form() {
      return ">";
    }
    
    public Gt(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class Le extends BinOp {
    public String form() {
      return "<=";
    }
    
    public Le(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class Ge extends BinOp {
    public String form() {
      return ">=";
    }
    
    public Ge(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class Or extends BinOp {
    public String form() {
      return "||";
    }
    
    public Or(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class And extends BinOp {
    public String form() {
      return "&&";
    }
    
    public And(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class Sub extends BinOp {
    public String form() {
      return "-";
    }
    
    public Sub(Expression l, Expression r) {
      super(l, r);
    }
  }
  
  public static class Div extends BinOp {
    public String form() {
      return "/";
    }
    
    public Div(Expression l, Expression r) {
      super(l, r);
    }
  }
}
