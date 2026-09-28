package haven.resutil;

import haven.GLState;
import haven.GOut;
import haven.Material;
import haven.Material.ResName;
import haven.Resource;
import haven.TexGL;
import haven.TexR;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.Macro1;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Type;
import haven.glsl.Uniform;
import java.util.Collection;
import java.util.List;

public class TexPal extends GLState {
  public static final GLState.Slot<TexPal> slot = new GLState.Slot(GLState.Slot.Type.DRAW, TexPal.class, new GLState.Slot[0]);
  
  public final TexGL tex;
  
  public TexPal(TexGL tex) {
    this.tex = tex;
  }
  
  private static final Uniform ctex = new Uniform(Type.SAMPLER2D);
  
  private static final ShaderMacro[] shaders = new ShaderMacro[] { new ShaderMacro() {
        public void modify(ProgramContext prog) {
          Tex2D.tex2d(prog.fctx).mod(new Macro1<Expression>() {
                public Expression expand(Expression in) {
                  return Cons.texture2D((Expression)TexPal.ctex.ref(), (Expression)Cons.pick(in, "rg"));
                }
              }-100);
        }
      } };
  
  private GLState.TexUnit sampler;
  
  public ShaderMacro[] shaders() {
    return shaders;
  }
  
  public boolean reqshaders() {
    return true;
  }
  
  public void reapply(GOut g) {}
  
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
  
  @ResName("pal")
  public static class $res implements Material.ResCons2 {
    public void cons(final Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
      final Resource tres;
      final int tid, a = 0;
      if (args[a] instanceof String) {
        tres = Resource.load((String)args[a], ((Integer)args[a + 1]).intValue());
        tid = ((Integer)args[a + 2]).intValue();
        a += 3;
      } else {
        tres = res;
        tid = ((Integer)args[a]).intValue();
        a++;
      } 
      left.add(new Material.Res.Resolver() {
            public void resolve(Collection<GLState> buf) {
              TexR rt = (TexR)tres.layer(TexR.class, Integer.valueOf(tid));
              if (rt == null)
                throw new RuntimeException(String.format("Specified texture %d for %s not found in %s", new Object[] { Integer.valueOf(this.val$tid), this.val$res, this.val$tres })); 
              buf.add(new TexPal(rt.tex()));
            }
          });
    }
  }
}
