package haven.rs;

import haven.Coord;
import haven.Drawn;
import haven.GLConfig;
import haven.GLFrameBuffer;
import haven.GLObject;
import haven.GLSettings;
import haven.GLState;
import haven.GOut;
import haven.HavenPanel;
import haven.TexE;
import haven.TexGL;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

public class GBuffer {
  public final Context ctx;
  
  public final Coord sz;
  
  private final GLFrameBuffer buf;
  
  private final GLState ostate;
  
  public static class Context {
    private final Object dmon = new Object();
    
    public final GLProfile prof;
    
    public final GLAutoDrawable buf;
    
    private final GLState gstate;
    
    private GLConfig glconf;
    
    private GBuffer curdraw;
    
    private GLState.Applier state;
    
    public Context() {
      this.prof = GLProfile.get("GL2");
      GLDrawableFactory df = GLDrawableFactory.getFactory(this.prof);
      this.gstate = new GLState() {
          public void apply(GOut g) {
            GL2 gl = g.gl;
            gl.glColor3f(1.0F, 1.0F, 1.0F);
            gl.glPointSize(4.0F);
            gl.setSwapInterval(1);
            gl.glEnable(3042);
            gl.glBlendFunc(770, 771);
            if (g.gc.havefsaa())
              g.gl.glDisable(32925); 
            GOut.checkerr((GL)gl);
          }
          
          public void unapply(GOut g) {}
          
          public void prep(GLState.Buffer buf) {
            buf.put(HavenPanel.global, this);
          }
        };
      this.buf = (GLAutoDrawable)df.createOffscreenAutoDrawable(null, (GLCapabilitiesImmutable)caps(this.prof), null, 1, 1, null);
      this.buf.addGLEventListener(new GLEventListener() {
            public void display(GLAutoDrawable d) {
              GL2 gl = d.getGL().getGL2();
              GBuffer.Context.this.redraw(gl);
              GLObject.disposeall(gl);
            }
            
            public void init(GLAutoDrawable d) {
              GL2 gl = d.getGL().getGL2();
              GBuffer.Context.this.glconf = GLConfig.fromgl((GL)gl, d.getContext(), d.getChosenGLCapabilities());
              GBuffer.Context.this.glconf.pref = GLSettings.defconf(GBuffer.Context.this.glconf);
              GBuffer.Context.this.glconf.pref.meshmode.val = GLSettings.MeshMode.MEM;
            }
            
            public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {}
            
            public void dispose(GLAutoDrawable d) {
              GL2 gl = d.getGL().getGL2();
              GLObject.disposeall(gl);
            }
          });
    }
    
    protected GLCapabilities caps(GLProfile prof) {
      GLCapabilities ret = new GLCapabilities(prof);
      ret.setDoubleBuffered(true);
      ret.setAlphaBits(8);
      ret.setRedBits(8);
      ret.setGreenBits(8);
      ret.setBlueBits(8);
      ret.setSampleBuffers(true);
      ret.setNumSamples(4);
      return ret;
    }
    
    private void redraw(GL2 gl) {
      this.curdraw.redraw(gl);
    }
  }
  
  public GBuffer(Context ctx, Coord sz) {
    this.ctx = ctx;
    this.sz = sz;
    this.buf = new GLFrameBuffer((TexGL)new TexE(sz, 6408, 6408, 5121), null);
    this.ostate = (GLState)HavenPanel.OrthoState.fixed(new Coord(sz));
  }
  
  private static Context defctx = null;
  
  private Drawn curdraw;
  
  private static Context defctx() {
    synchronized (GBuffer.class) {
      if (defctx == null)
        defctx = new Context(); 
    } 
    return defctx;
  }
  
  public GBuffer(Coord sz) {
    this(defctx(), sz);
  }
  
  protected void redraw(GL2 gl) {
    if (this.ctx.state == null || this.ctx.state.gl != gl)
      this.ctx.state = new GLState.Applier(gl, this.ctx.glconf); 
    GLState.Buffer ibuf = new GLState.Buffer(this.ctx.glconf);
    this.ctx.gstate.prep(ibuf);
    this.ostate.prep(ibuf);
    this.buf.prep(ibuf);
    GOut g = new GOut(gl, this.ctx.buf.getContext(), this.ctx.glconf, this.ctx.state, ibuf, this.sz);
    g.apply();
    gl.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
    gl.glClear(16384);
    this.curdraw.draw(g);
    this.ctx.state.clean();
  }
  
  public void render(Drawn thing) {
    synchronized (this.ctx.dmon) {
      this.curdraw = thing;
      this.ctx.curdraw = this;
      try {
        this.ctx.buf.display();
      } finally {
        this.curdraw = null;
        this.ctx.curdraw = null;
      } 
    } 
  }
  
  public void dispose() {
    this.buf.dispose();
  }
  
  public static void main(String[] args) {
    GBuffer test = new GBuffer(new Coord(250, 250));
    test.render(new Drawn() {
          public void draw(GOut g) {
            g.chcolor(255, 0, 128, 255);
            g.frect(new Coord(50, 50), new Coord(100, 100));
            try {
              ImageIO.write(g.getimage(), "PNG", new File("/tmp/bard.png"));
            } catch (IOException e) {
              throw new RuntimeException(e);
            } 
            GOut.checkerr((GL)g.gl);
          }
        });
  }
}
