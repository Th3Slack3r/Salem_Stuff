package haven.resutil;

import haven.GLState;
import haven.GOut;
import haven.MeshBuf;
import haven.TexGL;
import haven.glsl.Attribute;
import haven.glsl.AutoVarying;
import haven.glsl.Block;
import haven.glsl.CodeMacro;
import haven.glsl.Cons;
import haven.glsl.Discard;
import haven.glsl.Expression;
import haven.glsl.FragmentContext;
import haven.glsl.If;
import haven.glsl.Macro1;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.ValBlock;
import haven.glsl.Varying;
import haven.glsl.VertexContext;

public class AlphaTex extends GLState {
   public static final GLState.Slot<AlphaTex> slot = new GLState.Slot<>(GLState.Slot.Type.DRAW, AlphaTex.class);
   public static final Attribute clipc = new Attribute(Type.VEC2);
   public static final MeshBuf.LayerID<MeshBuf.Vec2Layer> lclip = new MeshBuf.V2LayerID(clipc);
   private static final Uniform ctex = new Uniform(Type.SAMPLER2D);
   private static final Uniform cclip = new Uniform(Type.FLOAT);
   public final TexGL tex;
   public final float cthr;
   private GLState.TexUnit sampler;
   private static final AutoVarying fc = new AutoVarying(Type.VEC2) {
      {
         this.ipol = Varying.Interpol.CENTROID;
      }

      @Override
      protected Expression root(VertexContext vctx) {
         return AlphaTex.clipc.ref();
      }
   };
   private static final ShaderMacro main = new ShaderMacro() {
      @Override
      public void modify(ProgramContext prog) {
         final ValBlock.Value val = AlphaTex.value(prog.fctx);
         val.force();
         prog.fctx.fragcol.mod(new Macro1<Expression>() {
            public Expression expand(Expression in) {
               return Cons.mul(in, val.ref());
            }
         }, 100);
      }
   };
   private static final ShaderMacro clip = new ShaderMacro() {
      @Override
      public void modify(ProgramContext prog) {
         final ValBlock.Value val = AlphaTex.value(prog.fctx);
         val.force();
         prog.fctx.mainmod(new CodeMacro() {
            @Override
            public void expand(Block blk) {
               blk.add(new If(Cons.lt(Cons.pick(val.ref(), "a"), AlphaTex.cclip.ref()), new Discard()));
            }
         }, -100);
      }
   };
   private static final ShaderMacro[] shnc = new ShaderMacro[]{main};
   private static final ShaderMacro[] shwc = new ShaderMacro[]{main, clip};

   public AlphaTex(TexGL tex, float clip) {
      this.tex = tex;
      this.cthr = clip;
   }

   public AlphaTex(TexGL tex) {
      this(tex, 0.0F);
   }

   private static ValBlock.Value value(FragmentContext fctx) {
      return fctx.uniform.ext(ctex, new ValBlock.Factory() {
         @Override
         public ValBlock.Value make(ValBlock vals) {
            return new ValBlock.Value(vals, Type.VEC4) {
               {
                  x0.getClass();
               }

               @Override
               public Expression root() {
                  return Cons.texture2D(AlphaTex.ctex.ref(), AlphaTex.fc.ref());
               }
            };
         }
      });
   }

   @Override
   public ShaderMacro[] shaders() {
      return this.cthr > 0.0F ? shwc : shnc;
   }

   public boolean reqshader() {
      return true;
   }

   @Override
   public void reapply(GOut g) {
      g.gl.glUniform1i(g.st.prog.uniform(ctex), this.sampler.id);
      if (this.cthr > 0.0F) {
         g.gl.glUniform1f(g.st.prog.uniform(cclip), this.cthr);
      }
   }

   @Override
   public void apply(GOut g) {
      this.sampler = TexGL.lbind(g, this.tex);
      this.reapply(g);
   }

   @Override
   public void unapply(GOut g) {
      this.sampler.ufree();
      this.sampler = null;
   }

   @Override
   public void prep(GLState.Buffer buf) {
      buf.put(slot, this);
   }
}
