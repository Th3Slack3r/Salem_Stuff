package haven.glsl;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Block extends Statement {
  public final List<Statement> stmts = new LinkedList<>();
  
  public Block(Statement... stmts) {
    for (Statement s : stmts)
      this.stmts.add(s); 
  }
  
  public static final class Local extends Variable {
    public Local(Type type, Symbol name) {
      super(type, name);
    }
    
    public Local(Type type) {
      this(type, new Symbol.Gen());
    }
    
    public class Def extends Statement {
      private final Expression init;
      
      public Def(Expression init) {
        this.init = init;
      }
      
      public Def process(Context ctx) {
        return new Def((this.init == null) ? null : this.init.process(ctx));
      }
      
      public void output(Output out) {
        out.write(Block.Local.this.type.name(out.ctx));
        out.write(" ");
        out.write(Block.Local.this.name);
        if (this.init != null) {
          out.write(" = ");
          this.init.output(out);
        } 
        out.write(";");
      }
    }
  }
  
  public class Def extends Statement {
    private final Expression init;
    
    public Def(Expression init) {
      this.init = init;
    }
    
    public Def process(Context ctx) {
      return new Def((this.init == null) ? null : this.init.process(ctx));
    }
    
    public void output(Output out) {
      out.write(Block.Local.this.type.name(out.ctx));
      out.write(" ");
      out.write(Block.Local.this.name);
      if (this.init != null) {
        out.write(" = ");
        this.init.output(out);
      } 
      out.write(";");
    }
  }
  
  public void add(Statement stmt, Statement before) {
    if (stmt == null)
      throw new NullPointerException(); 
    if (before == null) {
      this.stmts.add(stmt);
    } else {
      for (ListIterator<Statement> i = this.stmts.listIterator(); i.hasNext(); ) {
        Statement cur = i.next();
        if (cur == before) {
          i.previous();
          i.add(stmt);
          return;
        } 
      } 
      throw new RuntimeException(before + " is not already in block");
    } 
  }
  
  public void add(Statement stmt) {
    add(stmt, (Statement)null);
  }
  
  public void add(Expression expr, Statement before) {
    add(Statement.expr(expr), before);
  }
  
  public void add(Expression expr) {
    add(Statement.expr(expr), (Statement)null);
  }
  
  public Local local(Type type, Symbol name, Expression init, Statement before) {
    Local ret = new Local(type, name);
    ret.getClass();
    add(new Local.Def(init), before);
    return ret;
  }
  
  public Local local(Type type, Symbol name, Expression init) {
    return local(type, name, init, null);
  }
  
  public Local local(Type type, String prefix, Expression init) {
    return local(type, new Symbol.Gen(prefix), init);
  }
  
  public Local local(Type type, Expression init) {
    return local(type, new Symbol.Gen(), init);
  }
  
  public Block process(Context ctx) {
    Block ret = new Block(new Statement[0]);
    for (Statement s : this.stmts)
      ret.add(s.process(ctx)); 
    return ret;
  }
  
  public void trail(Output out, boolean nl) {
    if (this.stmts.isEmpty())
      return; 
    out.write("{\n");
    out.indent++;
    for (Statement s : this.stmts) {
      out.indent();
      s.output(out);
      out.write("\n");
    } 
    out.indent--;
    out.indent();
    out.write("}");
    if (nl)
      out.write("\n"); 
  }
  
  public void output(Output out) {
    out.indent();
    trail(out, true);
  }
}
