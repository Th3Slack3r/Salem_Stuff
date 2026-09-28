package haven.glsl;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Block extends Statement {
   public final List<Statement> stmts = new LinkedList<>();

   public Block(Statement... stmts) {
      for (Statement s : stmts) {
         this.stmts.add(s);
      }
   }

   public void add(Statement stmt, Statement before) {
      if (stmt == null) {
         throw new NullPointerException();
      } else if (before == null) {
         this.stmts.add(stmt);
      } else {
         ListIterator<Statement> i = this.stmts.listIterator();

         while (i.hasNext()) {
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
      this.add(stmt, null);
   }

   public void add(Expression expr, Statement before) {
      this.add(Statement.expr(expr), before);
   }

   public void add(Expression expr) {
      this.add(Statement.expr(expr), null);
   }

   public Block.Local local(Type type, Symbol name, Expression init, Statement before) {
      Block.Local ret = new Block.Local(type, name);
      this.add(ret.new Def(init), before);
      return ret;
   }

   public Block.Local local(Type type, Symbol name, Expression init) {
      return this.local(type, name, init, null);
   }

   public Block.Local local(Type type, String prefix, Expression init) {
      return this.local(type, new Symbol.Gen(prefix), init);
   }

   public Block.Local local(Type type, Expression init) {
      return this.local(type, new Symbol.Gen(), init);
   }

   public Block process(Context ctx) {
      Block ret = new Block();

      for (Statement s : this.stmts) {
         ret.add(s.process(ctx));
      }

      return ret;
   }

   public void trail(Output out, boolean nl) {
      if (!this.stmts.isEmpty()) {
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
         if (nl) {
            out.write("\n");
         }
      }
   }

   @Override
   public void output(Output out) {
      out.indent();
      this.trail(out, true);
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

         public Block.Local.Def process(Context ctx) {
            return Local.this.new Def(this.init == null ? null : this.init.process(ctx));
         }

         @Override
         public void output(Output out) {
            out.write(Local.this.type.name(out.ctx));
            out.write(" ");
            out.write(Local.this.name);
            if (this.init != null) {
               out.write(" = ");
               this.init.output(out);
            }

            out.write(";");
         }
      }
   }
}
