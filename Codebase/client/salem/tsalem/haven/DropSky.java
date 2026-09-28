package haven;

import haven.glsl.AutoVarying;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.LValue;
import haven.glsl.Macro1;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.VertexContext;
import javax.media.opengl.GL2;

public class DropSky implements Rendered {
  public final TexCube tex;
  
  public DropSky(TexCube tex) {
    this.st = new States.AdHoc(shaders) {
        public boolean reqshaders() {
          return false;
        }
        
        public void reapply(GOut g) {
          g.gl.glUniform1i(g.st.prog.uniform(DropSky.ssky), DropSky.this.tsky.id);
        }
        
        private void papply(GOut g) {
          reapply(g);
        }
        
        private void fapply(GOut g) {
          g.gl.glTexEnvi(8960, 8704, 8448);
          g.gl.glEnable(34067);
        }
        
        public void apply(GOut g) {
          (DropSky.this.tsky = g.st.texalloc()).act();
          g.gl.glBindTexture(34067, DropSky.this.tex.glid(g));
          if (g.st.prog == null) {
            fapply(g);
          } else {
            papply(g);
          } 
        }
        
        private void funapply(GOut g) {
          g.gl.glDisable(34067);
        }
        
        public void unapply(GOut g) {
          DropSky.this.tsky.act();
          if (!g.st.usedprog)
            funapply(g); 
          g.gl.glBindTexture(34067, 0);
          DropSky.this.tsky.free();
          DropSky.this.tsky = null;
        }
      };
    this.tex = tex;
  }
  
  private void vertex(GOut g, GL2 gl, Matrix4f ixf, float[] oc, float x, float y) {
    float[] cc = { x, y, 0.99999F, 1.0F };
    float[] vc = ixf.mul4(cc);
    float iw = 1.0F / vc[3];
    for (int i = 0; i < 4; i++)
      vc[i] = vc[i] * iw; 
    if (g.st.prog == null) {
      gl.glMultiTexCoord3f(33984 + this.tsky.id, vc[0] - oc[0], vc[1] - oc[1], vc[2] - oc[2]);
    } else {
      gl.glTexCoord3f(vc[0] - oc[0], vc[1] - oc[1], vc[2] - oc[2]);
    } 
    gl.glVertex4f(vc[0], vc[1], vc[2], vc[3]);
  }
  
  public void draw(GOut g) {
    g.apply();
    GL2 gl = g.gl;
    Matrix4f mvxf = (new Matrix4f(g.st.cam)).mul1(g.st.wxf);
    Matrix4f pmvxf = ((Projection)g.st.<Projection>cur(PView.proj)).fin(Matrix4f.id).mul(mvxf);
    Matrix4f ixf = pmvxf.invert();
    float[] oc = mvxf.invert().mul4(new float[] { 0.0F, 0.0F, 0.0F, 1.0F });
    float iw = 1.0F / oc[3];
    for (int i = 0; i < 4; i++)
      oc[i] = oc[i] * iw; 
    gl.glBegin(7);
    vertex(g, gl, ixf, oc, -1.05F, -1.05F);
    vertex(g, gl, ixf, oc, 1.05F, -1.05F);
    vertex(g, gl, ixf, oc, 1.05F, 1.05F);
    vertex(g, gl, ixf, oc, -1.05F, 1.05F);
    gl.glEnd();
  }
  
  private static final Uniform ssky = new Uniform(Type.SAMPLERCUBE);
  
  private static final ShaderMacro[] shaders = new ShaderMacro[] { new ShaderMacro() {
        AutoVarying texcoord = new AutoVarying(Type.VEC3) {
            protected Expression root(VertexContext vctx) {
              return (Expression)Cons.pick((LValue)VertexContext.gl_MultiTexCoord[0].ref(), "stp");
            }
          };
        
        public void modify(ProgramContext prog) {
          prog.fctx.fragcol.mod(new Macro1<Expression>() {
                public Expression expand(Expression in) {
                  return (Expression)Cons.mul(new Expression[] { in, Cons.textureCube((Expression)DropSky.access$000().ref(), (Expression)this.this$0.texcoord.ref()) });
                }
              }0);
        }
      } };
  
  private GLState.TexUnit tsky;
  
  private final GLState st;
  
  public boolean setup(RenderList rl) {
    rl.prepo(this.st);
    rl.prepo(States.presdepth);
    return true;
  }
  
  public static class ResSky implements Rendered {
    private DropSky sky;
    
    private Indir<Resource> res;
    
    public double alpha = 1.0D;
    
    public ResSky(Indir<Resource> res) {
      this.res = res;
    }
    
    public void update(Indir<Resource> res) {
      synchronized (this) {
        if (this.res != res) {
          this.sky = null;
          this.res = res;
        } 
      } 
    }
    
    public void draw(GOut g) {}
    
    public boolean setup(RenderList rl) {
      DropSky sky = this.sky;
      if (sky == null)
        synchronized (this) {
          if (this.res != null)
            try {
              this.sky = sky = new DropSky(new TexCube(((Resource.Image)((Resource)this.res.get()).layer((Class)Resource.imgc)).img));
            } catch (Loading loading) {} 
        }  
      if (sky != null) {
        GLState blend = null;
        if (this.alpha < 1.0D)
          blend = new States.ColState(255, 255, 255, (int)(255.0D * this.alpha)); 
        rl.add(sky, blend);
      } 
      return false;
    }
  }
}
