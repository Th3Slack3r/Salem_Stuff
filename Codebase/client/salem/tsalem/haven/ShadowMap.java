package haven;

import haven.glsl.AutoVarying;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.For;
import haven.glsl.Function;
import haven.glsl.If;
import haven.glsl.LValue;
import haven.glsl.Phong;
import haven.glsl.ProgramContext;
import haven.glsl.Return;
import haven.glsl.ShaderMacro;
import haven.glsl.Statement;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.Variable;
import haven.glsl.VertexContext;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;

public class ShadowMap extends GLState implements GLState.GlobalState, GLState.Global {
  public static final GLState.Slot<ShadowMap> smap = new GLState.Slot<>(GLState.Slot.Type.DRAW, ShadowMap.class, new GLState.Slot[] { Light.lighting });
  
  public DirLight light;
  
  public final TexE lbuf;
  
  private final Projection lproj;
  
  private final DirCam lcam;
  
  private final FBView tgt;
  
  private static final Matrix4f texbias = new Matrix4f(0.5F, 0.0F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F, 0.0F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 1.0F);
  
  private final List<RenderList.Slot> parts = new ArrayList<>();
  
  private int slidx;
  
  private Matrix4f txf;
  
  private final Rendered scene;
  
  public final Shader shader;
  
  private final ShaderMacro[] shaders;
  
  private GLState.TexUnit sampler;
  
  public ShadowMap(Coord res, float size, float depth, float dthr) {
    this.scene = new Rendered() {
        public void draw(GOut g) {}
        
        public boolean setup(RenderList rl) {
          GLState.Buffer buf = new GLState.Buffer(rl.cfg);
          for (RenderList.Slot s : ShadowMap.this.parts) {
            rl.state().copy(buf);
            s.os.copy(buf, GLState.Slot.Type.GEOM);
            rl.add2(s.r, buf);
          } 
          return false;
        }
      };
    this.lbuf = new TexE(res, 6402, 6402, 5125);
    this.lbuf.magfilter = 9729;
    this.lbuf.wrapmode = 10496;
    this.shader = new Shader(1.0D / res.x, 1.0D / res.y, 4, (dthr / depth));
    this.shaders = new ShaderMacro[] { this.shader };
    this.lproj = Projection.ortho(-size, size, -size, size, 1.0F, depth);
    this.lcam = new DirCam();
    this.tgt = new FBView(new GLFrameBuffer((TexGL)null, this.lbuf), GLState.compose(new GLState[] { this.lproj, this.lcam }));
  }
  
  public void setpos(Coord3f base, Coord3f dir) {
    this.lcam.base = base;
    this.lcam.dir = dir;
  }
  
  public void dispose() {
    this.lbuf.dispose();
    this.tgt.dispose();
  }
  
  public void prerender(RenderList rl, GOut g) {
    this.parts.clear();
    Light.LightList ll = null;
    Camera cam = null;
    for (RenderList.Slot s : rl.slots()) {
      if (!s.d)
        continue; 
      if (s.os.get(smap) != this || s.os.get(Light.lighting) == null)
        continue; 
      if (ll == null) {
        PView.RenderState rs = s.os.<PView.RenderState>get(PView.wnd);
        cam = s.os.<Camera>get(PView.cam);
        ll = s.os.<Light.LightList>get(Light.lights);
      } 
      this.parts.add(s);
    } 
    this.slidx = -1;
    for (int i = 0; i < ll.ll.size(); i++) {
      if (ll.ll.get(i) == this.light) {
        this.slidx = i;
        break;
      } 
    } 
    Matrix4f cm = Transform.rxinvert(cam.fin(Matrix4f.id));
    this.txf = texbias.mul(this.lproj.fin(Matrix4f.id)).mul(this.lcam.fin(Matrix4f.id)).mul(cm);
    this.tgt.render(this.scene, g);
  }
  
  public GLState.Global global(RenderList rl, GLState.Buffer ctx) {
    return this;
  }
  
  public void postsetup(RenderList rl) {}
  
  public void postrender(RenderList rl, GOut g) {}
  
  public void prep(GLState.Buffer buf) {
    buf.put(smap, this);
  }
  
  public static class Shader implements ShaderMacro {
    public static final Uniform txf = new Uniform(Type.MAT4);
    
    public static final Uniform sl = new Uniform(Type.INT);
    
    public static final Uniform map = new Uniform(Type.SAMPLER2D);
    
    public static final AutoVarying stc = new AutoVarying(Type.VEC4) {
        public Expression root(VertexContext vctx) {
          return (Expression)Cons.mul(new Expression[] { (Expression)ShadowMap.Shader.txf.ref(), vctx.eyev.depref() });
        }
      };
    
    public final Function.Def shcalc;
    
    public Shader(final double xd, final double yd, final int res, final double thr) {
      this.shcalc = new Function.Def(Type.FLOAT) {
        
        };
    }
    
    public void modify(ProgramContext prog) {
      final Phong ph = (Phong)prog.getmod(Phong.class);
      if (ph == null || !ph.pfrag)
        return; 
      ph.dolight.mod(new Runnable() {
            public void run() {
              ph.dolight.dcalc.add((Statement)new If((Expression)Cons.eq((Expression)ShadowMap.Shader.sl.ref(), ph.dolight.i), Cons.stmt((Expression)Cons.amul((LValue)ph.dolight.dl.var.ref(), ShadowMap.Shader.this.shcalc.call(new Expression[0])))), ph.dolight.dcurs);
            }
          }0);
    }
  }
  
  public ShaderMacro[] shaders() {
    return this.shaders;
  }
  
  public void apply(GOut g) {
    this.sampler = g.st.texalloc();
    if (g.st.prog != null) {
      GL2 gL2 = g.gl;
      this.sampler.act();
      gL2.glBindTexture(3553, this.lbuf.glid(g));
      reapply(g);
    } 
  }
  
  public void reapply(GOut g) {
    GL2 gl = g.gl;
    int mapu = g.st.prog.cuniform(Shader.map);
    if (mapu >= 0) {
      gl.glUniform1i(mapu, this.sampler.id);
      gl.glUniformMatrix4fv(g.st.prog.uniform(Shader.txf), 1, false, this.txf.m, 0);
      gl.glUniform1i(g.st.prog.uniform(Shader.sl), this.slidx);
    } 
  }
  
  public void unapply(GOut g) {
    GL2 gL2 = g.gl;
    this.sampler.act();
    gL2.glBindTexture(3553, 0);
    this.sampler.free();
    this.sampler = null;
  }
}
