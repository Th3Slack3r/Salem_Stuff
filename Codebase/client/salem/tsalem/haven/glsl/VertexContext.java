package haven.glsl;

import haven.GLState;
import haven.GOut;
import haven.PView;
import java.io.Writer;

public class VertexContext extends ShaderContext {
  public final Function.Def main;
  
  public final ValBlock mainvals;
  
  private final OrderList<CodeMacro> code;
  
  public VertexContext(ProgramContext prog) {
    super(prog);
    this.main = new Function.Def(Type.VOID, new Symbol.Fix("main"));
    this.mainvals = new ValBlock();
    this.code = new OrderList<>();
    this.code.add(new CodeMacro() {
          public void expand(Block blk) {
            VertexContext.this.mainvals.cons(blk);
          }
        },  0);
    this.mainvals.getClass();
    this.objv = new ValBlock.Value(this.mainvals, Type.VEC4, new Symbol.Gen("objv")) {
        public Expression root() {
          return VertexContext.gl_Vertex.ref();
        }
      };
    this.mainvals.getClass();
    this.mapv = new ValBlock.Value(this.mainvals, Type.VEC4, new Symbol.Gen("mapv")) {
        public Expression root() {
          return new Expression() {
              public Expression process(Context ctx) {
                if (VertexContext.this.objv.used)
                  return (new Mul(new Expression[] { VertexContext.wxf.ref(), this.this$1.this$0.objv.ref() })).process(ctx); 
                return (new Mul(new Expression[] { VertexContext.wxf.ref(), VertexContext.gl_Vertex.ref() })).process(ctx);
              }
            };
        }
      };
    this.mainvals.getClass();
    this.eyev = new ValBlock.Value(this.mainvals, Type.VEC4, new Symbol.Gen("eyev")) {
        public Expression root() {
          return new Expression() {
              public Expression process(Context ctx) {
                if (VertexContext.this.mapv.used)
                  return (new Mul(new Expression[] { VertexContext.cam.ref(), this.this$1.this$0.mapv.ref() })).process(ctx); 
                if (VertexContext.this.objv.used)
                  return (new Mul(new Expression[] { VertexContext.gl_ModelViewMatrix.ref(), this.this$1.this$0.objv.ref() })).process(ctx); 
                return (new Mul(new Expression[] { VertexContext.gl_ModelViewMatrix.ref(), VertexContext.gl_Vertex.ref() })).process(ctx);
              }
            };
        }
      };
    this.mainvals.getClass();
    this.eyen = new ValBlock.Value(this.mainvals, Type.VEC3, new Symbol.Gen("eyen")) {
        public Expression root() {
          return new Mul(new Expression[] { VertexContext.gl_NormalMatrix.ref(), VertexContext.gl_Normal.ref() });
        }
      };
    this.mainvals.getClass();
    this.posv = new ValBlock.Value(this.mainvals, Type.VEC4, new Symbol.Gen("posv")) {
        public Expression root() {
          return new Expression() {
              public Expression process(Context ctx) {
                if (VertexContext.this.eyev.used)
                  return (new Mul(new Expression[] { VertexContext.gl_ProjectionMatrix.ref(), this.this$1.this$0.eyev.ref() })).process(ctx); 
                if (VertexContext.this.mapv.used)
                  return (new Mul(new Expression[] { VertexContext.gl_ProjectionMatrix.ref(), VertexContext.cam.ref(), this.this$1.this$0.mapv.ref() })).process(ctx); 
                if (VertexContext.this.objv.used)
                  return (new Mul(new Expression[] { VertexContext.gl_ModelViewProjectionMatrix.ref(), this.this$1.this$0.objv.ref() })).process(ctx); 
                return (new Mul(new Expression[] { VertexContext.gl_ModelViewProjectionMatrix.ref(), VertexContext.gl_Vertex.ref() })).process(ctx);
              }
            };
        }
        
        protected void cons2(Block blk) {
          this.var = VertexContext.gl_Position;
          blk.add(new LBinOp.Assign(this.var.ref(), this.init));
        }
      };
  }
  
  public static final Variable gl_Vertex = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_Vertex"));
  
  public static final Variable gl_Normal = new Variable.Implicit(Type.VEC3, new Symbol.Fix("gl_Normal"));
  
  public static final Variable gl_Color = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_Color"));
  
  public static final Variable gl_ModelViewMatrix = new Variable.Implicit(Type.MAT4, new Symbol.Fix("gl_ModelViewMatrix"));
  
  public static final Variable gl_NormalMatrix = new Variable.Implicit(Type.MAT4, new Symbol.Fix("gl_NormalMatrix"));
  
  public static final Variable gl_ProjectionMatrix = new Variable.Implicit(Type.MAT4, new Symbol.Fix("gl_ProjectionMatrix"));
  
  public static final Variable gl_ModelViewProjectionMatrix = new Variable.Implicit(Type.MAT4, new Symbol.Fix("gl_ModelViewProjectionMatrix"));
  
  public static final Variable gl_Position = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_Position"));
  
  public static final Variable[] gl_MultiTexCoord = new Variable[] { new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord0")), new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord1")), new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord2")), new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord3")), new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord4")), new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord5")), new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord6")), new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord7")) };
  
  public static final Uniform wxf = new Uniform.AutoApply(Type.MAT4, "wxf", new GLState.Slot[] { PView.loc }) {
      public void apply(GOut g, int loc) {
        g.gl.glUniformMatrix4fv(loc, 1, false, g.st.wxf.m, 0);
      }
    };
  
  public static final Uniform cam = new Uniform.AutoApply(Type.MAT4, "cam", new GLState.Slot[] { PView.cam }) {
      public void apply(GOut g, int loc) {
        g.gl.glUniformMatrix4fv(loc, 1, false, g.st.cam.m, 0);
      }
    };
  
  public final ValBlock.Value objv;
  
  public final ValBlock.Value mapv;
  
  public final ValBlock.Value eyev;
  
  public final ValBlock.Value eyen;
  
  public final ValBlock.Value posv;
  
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
