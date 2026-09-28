package haven.glsl;

import java.io.Writer;

public class FragmentContext extends ShaderContext {
  public final Function.Def main;
  
  public final ValBlock mainvals;
  
  public final ValBlock uniform;
  
  private final OrderList<CodeMacro> code;
  
  public FragmentContext(ProgramContext prog) {
    super(prog);
    this.main = new Function.Def(Type.VOID, new Symbol.Fix("main"));
    this.mainvals = new ValBlock();
    this.uniform = new ValBlock();
    this.code = new OrderList<>();
    this.code.add(new CodeMacro() {
          public void expand(Block blk) {
            FragmentContext.this.mainvals.cons(blk);
          }
        },  0);
    this.code.add(new CodeMacro() {
          public void expand(Block blk) {
            FragmentContext.this.uniform.cons(blk);
            FragmentContext.this.main.code.add(new Placeholder("Uniform control up until here."));
          }
        },  -1000);
    this.mrt = false;
    this.mainvals.getClass();
    this.fragcol = new ValBlock.Value(this.mainvals, Type.VEC4) {
        public Expression root() {
          return Vec4Cons.u;
        }
        
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
  
  public static final Variable gl_FragColor = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_FragColor"));
  
  public static final Variable gl_FragCoord = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_FragCoord"));
  
  public static final Variable gl_FragData = new Variable.Implicit(new Array(Type.VEC4), new Symbol.Fix("gl_FragData"));
  
  private boolean mrt;
  
  public final ValBlock.Value fragcol;
  
  public abstract class FragData extends ValBlock.Value {
    public final int id;
    
    public FragData(int id) {
      super(Type.VEC4);
      this.id = id;
      FragmentContext.this.mrt = true;
      force();
    }
    
    protected void cons2(Block blk) {
      blk.add(new LBinOp.Assign(new Index(FragmentContext.gl_FragData.ref(), new IntLiteral(this.id)), this.init));
    }
  }
  
  public void mainmod(CodeMacro macro, int order) {
    this.code.add(macro, order);
  }
  
  public void construct(Writer out) {
    for (CodeMacro macro : this.code)
      macro.expand(this.main.code); 
    this.main.define(this);
    output(new Output(out, this));
  }
}
