package haven;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class GLFrameBuffer extends GLState {
  public static final GLState.Slot<GLFrameBuffer> slot = new GLState.Slot<>(GLState.Slot.Type.SYS, GLFrameBuffer.class, new GLState.Slot[] { HavenPanel.global });
  
  private final Attachment[] color;
  
  private final Attachment depth;
  
  private final RenderBuffer altdepth;
  
  private FBO fbo;
  
  private final int[] bufmask;
  
  public static class FBO extends GLObject {
    public final int id;
    
    public FBO(GL2 gl) {
      super(gl);
      int[] buf = new int[1];
      gl.glGenFramebuffers(1, buf, 0);
      this.id = buf[0];
      GOut.checkerr((GL)gl);
    }
    
    protected void delete() {
      int[] buf = { this.id };
      this.gl.glDeleteFramebuffers(1, buf, 0);
      GOut.checkerr((GL)this.gl);
    }
  }
  
  public static class RenderBuffer {
    public final Coord sz;
    
    public final int samples;
    
    public final int fmt;
    
    private RBO rbo;
    
    public RenderBuffer(Coord sz, int fmt, int samples) {
      this.sz = sz;
      this.fmt = fmt;
      this.samples = samples;
    }
    
    public RenderBuffer(Coord sz, int fmt) {
      this(sz, fmt, 1);
    }
    
    public int glid(GL2 gl) {
      if (this.rbo != null && this.rbo.gl != gl)
        dispose(); 
      if (this.rbo == null) {
        this.rbo = new RBO(gl);
        gl.glBindRenderbuffer(36161, this.rbo.id);
        if (this.samples <= 1) {
          gl.glRenderbufferStorage(36161, this.fmt, this.sz.x, this.sz.y);
        } else {
          gl.glRenderbufferStorageMultisample(36161, this.samples, this.fmt, this.sz.x, this.sz.y);
        } 
      } 
      return this.rbo.id;
    }
    
    public void dispose() {
      synchronized (this) {
        if (this.rbo != null) {
          this.rbo.dispose();
          this.rbo = null;
        } 
      } 
    }
    
    public static class RBO extends GLObject {
      public final int id;
      
      public RBO(GL2 gl) {
        super(gl);
        int[] buf = new int[1];
        gl.glGenRenderbuffers(1, buf, 0);
        this.id = buf[0];
        GOut.checkerr((GL)gl);
      }
      
      protected void delete() {
        int[] buf = { this.id };
        this.gl.glDeleteRenderbuffers(1, buf, 0);
        GOut.checkerr((GL)this.gl);
      }
    }
  }
  
  public static abstract class Attachment {
    public abstract void attach(GOut param1GOut, GLFrameBuffer param1GLFrameBuffer, int param1Int);
    
    public abstract Coord sz();
    
    public static GLFrameBuffer.Attach2D mk(TexGL tex) {
      return new GLFrameBuffer.Attach2D(tex);
    }
    
    public static GLFrameBuffer.AttachMS mk(TexMS tex) {
      return new GLFrameBuffer.AttachMS(tex);
    }
    
    public static GLFrameBuffer.AttachRBO mk(GLFrameBuffer.RenderBuffer rbo) {
      return new GLFrameBuffer.AttachRBO(rbo);
    }
  }
  
  public static class Attach2D extends Attachment {
    public final TexGL tex;
    
    public final int level;
    
    public Attach2D(TexGL tex, int level) {
      this.tex = tex;
      this.level = level;
    }
    
    public Attach2D(TexGL tex) {
      this(tex, 0);
    }
    
    public void attach(GOut g, GLFrameBuffer fbo, int point) {
      g.gl.glFramebufferTexture2D(36160, point, 3553, this.tex.glid(g), this.level);
    }
    
    public Coord sz() {
      return this.tex.sz();
    }
  }
  
  public static class AttachMS extends Attachment {
    public final TexMS tex;
    
    public AttachMS(TexMS tex) {
      this.tex = tex;
    }
    
    public void attach(GOut g, GLFrameBuffer fbo, int point) {
      g.gl.glFramebufferTexture2D(36160, point, 37120, this.tex.glid(g), 0);
    }
    
    public Coord sz() {
      return new Coord(this.tex.w, this.tex.h);
    }
  }
  
  public static class AttachRBO extends Attachment {
    public final GLFrameBuffer.RenderBuffer rbo;
    
    public AttachRBO(GLFrameBuffer.RenderBuffer rbo) {
      this.rbo = rbo;
    }
    
    public void attach(GOut g, GLFrameBuffer fbo, int point) {
      g.gl.glFramebufferRenderbuffer(36160, point, 36161, this.rbo.glid(g.gl));
    }
    
    public Coord sz() {
      return this.rbo.sz;
    }
  }
  
  public GLFrameBuffer(Attachment[] color, Attachment depth) {
    this.color = color;
    this.bufmask = new int[this.color.length];
    if (depth == null) {
      if (this.color.length == 0)
        throw new RuntimeException("Cannot create a framebuffer with neither color nor depth"); 
      this.altdepth = new RenderBuffer(color[0].sz(), 6402);
      this.depth = new AttachRBO(this.altdepth);
    } else {
      this.altdepth = null;
      this.depth = depth;
    } 
  }
  
  private static Attachment[] javaIsRetarded(TexGL[] color) {
    Attachment[] ret = new Attachment[color.length];
    for (int i = 0; i < color.length; i++)
      ret[i] = new Attach2D(color[i]); 
    return ret;
  }
  
  public GLFrameBuffer(TexGL[] color, TexGL depth) {
    this(javaIsRetarded(color), (depth == null) ? null : new Attach2D(depth));
  }
  
  public GLFrameBuffer(TexGL color, TexGL depth) {
    this((color == null) ? new TexGL[0] : new TexGL[1], depth);
  }
  
  public Coord sz() {
    return this.depth.sz();
  }
  
  public void apply(GOut g) {
    GL2 gl = g.gl;
    synchronized (this) {
      if (this.fbo != null && this.fbo.gl != gl)
        dispose(); 
      if (this.fbo == null) {
        this.fbo = new FBO(gl);
        gl.glBindFramebuffer(36160, this.fbo.id);
        int i;
        for (i = 0; i < this.color.length; i++)
          this.color[i].attach(g, this, 36064 + i); 
        this.depth.attach(g, this, 36096);
        if (this.color.length == 0) {
          gl.glDrawBuffer(0);
          gl.glReadBuffer(0);
        } else if (this.color.length > 1) {
          for (i = 0; i < this.color.length; i++)
            this.bufmask[i] = 36064 + i; 
          gl.glDrawBuffers(this.color.length, this.bufmask, 0);
        } 
        GOut.checkerr((GL)gl);
        int st = gl.glCheckFramebufferStatus(36160);
        if (st != 36053)
          throw new RuntimeException("FBO failed completeness test: " + st); 
      } else {
        gl.glBindFramebuffer(36160, this.fbo.id);
      } 
    } 
    gl.glViewport(0, 0, (sz()).x, (sz()).y);
  }
  
  public void mask(GOut g, int id, boolean flag) {
    int nb = flag ? (36064 + id) : 0;
    if (this.bufmask[id] != nb) {
      this.bufmask[id] = nb;
      g.gl.glDrawBuffers(this.color.length, this.bufmask, 0);
    } 
  }
  
  public void unapply(GOut g) {
    GL2 gl = g.gl;
    gl.glBindFramebuffer(36160, 0);
    gl.glViewport((g.root()).ul.x, (g.root()).ul.y, (g.root()).sz.x, (g.root()).sz.y);
  }
  
  public void prep(GLState.Buffer buf) {
    buf.put(slot, this);
  }
  
  public void dispose() {
    synchronized (this) {
      if (this.fbo != null) {
        this.fbo.dispose();
        this.fbo = null;
      } 
    } 
    if (this.altdepth != null)
      this.altdepth.dispose(); 
  }
}
