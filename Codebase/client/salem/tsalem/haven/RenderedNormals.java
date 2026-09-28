package haven;

import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.FragmentContext;
import haven.glsl.MiscLib;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;

public class RenderedNormals extends FBConfig.RenderTarget {
  private static final IntMap<ShaderMacro[]> shcache = (IntMap)new IntMap<>();
  
  private static ShaderMacro[] code(final int id) {
    ShaderMacro[] ret = shcache.get(id);
    if (ret == null) {
      ret = new ShaderMacro[] { new ShaderMacro() {
            public void modify(final ProgramContext prog) {
              MiscLib.frageyen(prog.fctx);
              prog.fctx.getClass();
              new FragmentContext.FragData(prog.fctx, id) {
                  public Expression root() {
                    return (Expression)Cons.vec4(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)Cons.add(new Expression[] { MiscLib.frageyen(this.val$prog.fctx).depref(), (Expression)Cons.l(1.0D) }), (Expression)Cons.l(0.5D) }), (Expression)Cons.l(1.0D) });
                  }
                };
            }
          } };
      shcache.put(id, ret);
    } 
    return ret;
  }
  
  public static final GLState.Slot<GLState> slot = new GLState.Slot<>(GLState.Slot.Type.SYS, GLState.class, new GLState.Slot[] { GLFrameBuffer.slot, States.presdepth.slot });
  
  public GLState state(final FBConfig cfg, final int id) {
    return new GLState() {
        private final ShaderMacro[] shaders = RenderedNormals.code(id);
        
        public ShaderMacro[] shaders() {
          return this.shaders;
        }
        
        public boolean reqshaders() {
          return true;
        }
        
        public void apply(GOut g) {
          GLFrameBuffer fb = g.st.<GLFrameBuffer>get(GLFrameBuffer.slot);
          if (fb != cfg.fb)
            throw new RuntimeException("Applying normal rendering in illegal framebuffer context"); 
          if (g.st.get(States.presdepth.slot) != null)
            fb.mask(g, id, false); 
        }
        
        public void unapply(GOut g) {
          ((GLFrameBuffer)g.st.<GLFrameBuffer>cur(GLFrameBuffer.slot)).mask(g, id, true);
        }
        
        public void prep(GLState.Buffer buf) {
          buf.put(RenderedNormals.slot, this);
        }
      };
  }
  
  public static final PView.RenderContext.DataID<RenderedNormals> id = new PView.RenderContext.DataID<RenderedNormals>() {
      public RenderedNormals make(PView.RenderContext ctx) {
        return new RenderedNormals();
      }
    };
}
