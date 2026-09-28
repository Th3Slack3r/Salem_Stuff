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
import haven.glsl.Statement;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.ValBlock;
import haven.glsl.VertexContext;

public class AlphaTex extends GLState {
  public static final GLState.Slot<AlphaTex> slot = new GLState.Slot(GLState.Slot.Type.DRAW, AlphaTex.class, new GLState.Slot[0]);
  
  public static final Attribute clipc = new Attribute(Type.VEC2);
  
  public static final MeshBuf.LayerID<MeshBuf.Vec2Layer> lclip = (MeshBuf.LayerID<MeshBuf.Vec2Layer>)new MeshBuf.V2LayerID(clipc);
  
  private static final Uniform ctex = new Uniform(Type.SAMPLER2D);
  
  private static final Uniform cclip = new Uniform(Type.FLOAT);
  
  public final TexGL tex;
  
  public final float cthr;
  
  private GLState.TexUnit sampler;
  
  public AlphaTex(TexGL tex, float clip) {
    this.tex = tex;
    this.cthr = clip;
  }
  
  public AlphaTex(TexGL tex) {
    this(tex, 0.0F);
  }
  
  private static final AutoVarying fc = new AutoVarying(Type.VEC2) {
      protected Expression root(VertexContext vctx) {
        return (Expression)AlphaTex.clipc.ref();
      }
    };
  
  private static ValBlock.Value value(FragmentContext fctx) {
    return fctx.uniform.ext(ctex, new ValBlock.Factory() {
          public ValBlock.Value make(ValBlock vals) {
            vals.getClass();
            return new ValBlock.Value(vals, Type.VEC4) {
                public Expression root() {
                  return Cons.texture2D((Expression)AlphaTex.ctex.ref(), (Expression)AlphaTex.fc.ref());
                }
              };
          }
        });
  }
  
  private static final ShaderMacro main = new ShaderMacro() {
      public void modify(ProgramContext prog) {
        final ValBlock.Value val = AlphaTex.value(prog.fctx);
        val.force();
        prog.fctx.fragcol.mod(new Macro1<Expression>() {
              public Expression expand(Expression in) {
                return (Expression)Cons.mul(new Expression[] { in, this.val$val.ref() }, );
              }
            },  100);
      }
    };
  
  private static final ShaderMacro clip = new ShaderMacro() {
      public void modify(ProgramContext prog) {
        final ValBlock.Value val = AlphaTex.value(prog.fctx);
        val.force();
        prog.fctx.mainmod(new CodeMacro() {
              public void expand(Block blk) {
                blk.add((Statement)new If((Expression)Cons.lt((Expression)Cons.pick(val.ref(), "a"), (Expression)AlphaTex.cclip.ref()), (Statement)new Discard()));
              }
            }-100);
      }
    };
  
  private static final ShaderMacro[] shnc = new ShaderMacro[] { main };
  
  private static final ShaderMacro[] shwc = new ShaderMacro[] { main, clip };
  
  public ShaderMacro[] shaders() {
    return (this.cthr > 0.0F) ? shwc : shnc;
  }
  
  public boolean reqshader() {
    return true;
  }
  
  public void reapply(GOut g) {
    g.gl.glUniform1i(g.st.prog.uniform(ctex), this.sampler.id);
    if (this.cthr > 0.0F)
      g.gl.glUniform1f(g.st.prog.uniform(cclip), this.cthr); 
  }
  
  public void apply(GOut g) {
    this.sampler = TexGL.lbind(g, this.tex);
    reapply(g);
  }
  
  public void unapply(GOut g) {
    this.sampler.ufree();
    this.sampler = null;
  }
  
  public void prep(GLState.Buffer buf) {
    buf.put(slot, this);
  }
}
