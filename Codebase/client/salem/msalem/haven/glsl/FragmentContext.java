package haven.glsl;

import java.io.Writer;
import java.util.Objects;

public class FragmentContext extends ShaderContext {
   public final Function.Def main = new Function.Def(Type.VOID, new Symbol.Fix("main"));
   public final ValBlock mainvals = new ValBlock();
   public final ValBlock uniform = new ValBlock();
   private final OrderList<CodeMacro> code = new OrderList<>();
   public static final Variable gl_FragColor = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_FragColor"));
   public static final Variable gl_FragCoord = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_FragCoord"));
   public static final Variable gl_FragData = new Variable.Implicit(new Array(Type.VEC4), new Symbol.Fix("gl_FragData"));
   private boolean mrt;
   public final ValBlock.Value fragcol;

   public FragmentContext(ProgramContext prog) {
      super(prog);
      this.code.add(new CodeMacro() {
         @Override
         public void expand(Block blk) {
            FragmentContext.this.mainvals.cons(blk);
         }
      }, 0);
      this.code.add(new CodeMacro() {
         @Override
         public void expand(Block blk) {
            FragmentContext.this.uniform.cons(blk);
            FragmentContext.this.main.code.add(new Placeholder("Uniform control up until here."));
         }
      }, -1000);
      this.mrt = false;
      ValBlock var10004 = this.mainvals;
      Objects.requireNonNull(this.mainvals);
      this.fragcol = new ValBlock.Value(var10004, Type.VEC4) {
         {
            Objects.requireNonNull(x0);
            this.force();
         }

         @Override
         public Expression root() {
            return Vec4Cons.u;
         }

         @Override
         protected void cons2(Block blk) {
            LValue tgt;
            if (FragmentContext.this.mrt) {
               tgt = new Index(FragmentContext.gl_FragData.ref(), IntLiteral.z);
            } else {
               tgt = FragmentContext.gl_FragColor.ref();
            }

            blk.add(new LBinOp.Assign(tgt, this.init));
         }
      };
   }

   public void mainmod(CodeMacro macro, int order) {
      this.code.add(macro, order);
   }

   public void construct(Writer out) {
      for (CodeMacro macro : this.code) {
         macro.expand(this.main.code);
      }

      this.main.define(this);
      this.output(new Output(out, this));
   }

   public abstract class FragData extends ValBlock.Value {
      public final int id;

      public FragData(int id) {
         ValBlock var10001 = FragmentContext.this.mainvals;
         Objects.requireNonNull(FragmentContext.this.mainvals);
         super(Type.VEC4);
         this.id = id;
         FragmentContext.this.mrt = true;
         this.force();
      }

      @Override
      protected void cons2(Block blk) {
         blk.add(new LBinOp.Assign(new Index(FragmentContext.gl_FragData.ref(), new IntLiteral(this.id)), this.init));
      }
   }
}
