package haven.glsl;

import haven.GOut;
import haven.PView;
import java.io.Writer;
import java.util.Objects;

public class VertexContext extends ShaderContext {
   public final Function.Def main = new Function.Def(Type.VOID, new Symbol.Fix("main"));
   public final ValBlock mainvals = new ValBlock();
   private final OrderList<CodeMacro> code = new OrderList<>();
   public static final Variable gl_Vertex = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_Vertex"));
   public static final Variable gl_Normal = new Variable.Implicit(Type.VEC3, new Symbol.Fix("gl_Normal"));
   public static final Variable gl_Color = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_Color"));
   public static final Variable gl_ModelViewMatrix = new Variable.Implicit(Type.MAT4, new Symbol.Fix("gl_ModelViewMatrix"));
   public static final Variable gl_NormalMatrix = new Variable.Implicit(Type.MAT4, new Symbol.Fix("gl_NormalMatrix"));
   public static final Variable gl_ProjectionMatrix = new Variable.Implicit(Type.MAT4, new Symbol.Fix("gl_ProjectionMatrix"));
   public static final Variable gl_ModelViewProjectionMatrix = new Variable.Implicit(Type.MAT4, new Symbol.Fix("gl_ModelViewProjectionMatrix"));
   public static final Variable gl_Position = new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_Position"));
   public static final Variable[] gl_MultiTexCoord = new Variable[]{
      new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord0")),
      new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord1")),
      new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord2")),
      new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord3")),
      new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord4")),
      new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord5")),
      new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord6")),
      new Variable.Implicit(Type.VEC4, new Symbol.Fix("gl_MultiTexCoord7"))
   };
   public static final Uniform wxf = new Uniform.AutoApply(Type.MAT4, "wxf", PView.loc) {
      @Override
      public void apply(GOut g, int loc) {
         g.gl.glUniformMatrix4fv(loc, 1, false, g.st.wxf.m, 0);
      }
   };
   public static final Uniform cam = new Uniform.AutoApply(Type.MAT4, "cam", PView.cam) {
      @Override
      public void apply(GOut g, int loc) {
         g.gl.glUniformMatrix4fv(loc, 1, false, g.st.cam.m, 0);
      }
   };
   public final ValBlock.Value objv;
   public final ValBlock.Value mapv;
   public final ValBlock.Value eyev;
   public final ValBlock.Value eyen;
   public final ValBlock.Value posv;

   public VertexContext(ProgramContext prog) {
      super(prog);
      this.code.add(new CodeMacro() {
         @Override
         public void expand(Block blk) {
            VertexContext.this.mainvals.cons(blk);
         }
      }, 0);
      ValBlock var10004 = this.mainvals;
      Objects.requireNonNull(this.mainvals);
      this.objv = new ValBlock.Value(var10004, Type.VEC4, new Symbol.Gen("objv")) {
         {
            Objects.requireNonNull(x0);
         }

         @Override
         public Expression root() {
            return VertexContext.gl_Vertex.ref();
         }
      };
      var10004 = this.mainvals;
      Objects.requireNonNull(this.mainvals);
      this.mapv = new ValBlock.Value(var10004, Type.VEC4, new Symbol.Gen("mapv")) {
         {
            Objects.requireNonNull(x0);
            this.softdep(VertexContext.this.objv);
         }

         @Override
         public Expression root() {
            return new Expression() {
               @Override
               public Expression process(Context ctx) {
                  return VertexContext.this.objv.used
                     ? new Mul(VertexContext.wxf.ref(), VertexContext.this.objv.ref()).process(ctx)
                     : new Mul(VertexContext.wxf.ref(), VertexContext.gl_Vertex.ref()).process(ctx);
               }
            };
         }
      };
      var10004 = this.mainvals;
      Objects.requireNonNull(this.mainvals);
      this.eyev = new ValBlock.Value(var10004, Type.VEC4, new Symbol.Gen("eyev")) {
         {
            Objects.requireNonNull(x0);
            this.softdep(VertexContext.this.objv);
            this.softdep(VertexContext.this.mapv);
         }

         @Override
         public Expression root() {
            return new Expression() {
               @Override
               public Expression process(Context ctx) {
                  if (VertexContext.this.mapv.used) {
                     return new Mul(VertexContext.cam.ref(), VertexContext.this.mapv.ref()).process(ctx);
                  } else {
                     return VertexContext.this.objv.used
                        ? new Mul(VertexContext.gl_ModelViewMatrix.ref(), VertexContext.this.objv.ref()).process(ctx)
                        : new Mul(VertexContext.gl_ModelViewMatrix.ref(), VertexContext.gl_Vertex.ref()).process(ctx);
                  }
               }
            };
         }
      };
      var10004 = this.mainvals;
      Objects.requireNonNull(this.mainvals);
      this.eyen = new ValBlock.Value(var10004, Type.VEC3, new Symbol.Gen("eyen")) {
         {
            Objects.requireNonNull(x0);
         }

         @Override
         public Expression root() {
            return new Mul(VertexContext.gl_NormalMatrix.ref(), VertexContext.gl_Normal.ref());
         }
      };
      var10004 = this.mainvals;
      Objects.requireNonNull(this.mainvals);
      this.posv = new ValBlock.Value(var10004, Type.VEC4, new Symbol.Gen("posv")) {
         {
            Objects.requireNonNull(x0);
            this.softdep(VertexContext.this.objv);
            this.softdep(VertexContext.this.mapv);
            this.softdep(VertexContext.this.eyev);
            this.force();
         }

         @Override
         public Expression root() {
            return new Expression() {
               @Override
               public Expression process(Context ctx) {
                  if (VertexContext.this.eyev.used) {
                     return new Mul(VertexContext.gl_ProjectionMatrix.ref(), VertexContext.this.eyev.ref()).process(ctx);
                  } else if (VertexContext.this.mapv.used) {
                     return new Mul(VertexContext.gl_ProjectionMatrix.ref(), VertexContext.cam.ref(), VertexContext.this.mapv.ref()).process(ctx);
                  } else {
                     return VertexContext.this.objv.used
                        ? new Mul(VertexContext.gl_ModelViewProjectionMatrix.ref(), VertexContext.this.objv.ref()).process(ctx)
                        : new Mul(VertexContext.gl_ModelViewProjectionMatrix.ref(), VertexContext.gl_Vertex.ref()).process(ctx);
                  }
               }
            };
         }

         @Override
         protected void cons2(Block blk) {
            this.var = VertexContext.gl_Position;
            blk.add(new LBinOp.Assign(this.var.ref(), this.init));
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
}
