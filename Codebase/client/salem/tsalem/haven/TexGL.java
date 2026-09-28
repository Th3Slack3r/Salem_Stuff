package haven;

import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Varying;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public abstract class TexGL extends Tex {
  public static boolean disableall = false;
  
  private static final WeakList<TexGL> active = new WeakList<>();
  
  protected TexOb t = null;
  
  protected boolean mipmap = false;
  
  protected boolean centroid = false;
  
  protected int magfilter = 9728;
  
  protected int minfilter = 9728;
  
  protected int wrapmode = 10497;
  
  protected Coord tdim;
  
  private final Object idmon = new Object();
  
  private WeakList.Entry<TexGL> actref;
  
  private boolean setparams = true;
  
  public static class TexOb extends GLObject {
    public final int id;
    
    public TexOb(GL2 gl) {
      super(gl);
      int[] buf = new int[1];
      gl.glGenTextures(1, buf, 0);
      this.id = buf[0];
    }
    
    protected void delete() {
      int[] buf = { this.id };
      this.gl.glDeleteTextures(1, buf, 0);
    }
  }
  
  public static final ShaderMacro mkcentroid = new ShaderMacro() {
      public void modify(ProgramContext prog) {
        (Tex2D.get(prog)).ipol = Varying.Interpol.CENTROID;
      }
    };
  
  public static GLState.TexUnit lbind(GOut g, TexGL tex) {
    GLState.TexUnit sampler = g.st.texalloc();
    sampler.act();
    try {
      g.gl.glBindTexture(3553, tex.glid(g));
      return sampler;
    } catch (Loading l) {
      sampler.free();
      throw l;
    } 
  }
  
  public static class TexDraw extends GLState {
    public static final GLState.Slot<TexDraw> slot = new GLState.Slot<>(GLState.Slot.Type.DRAW, TexDraw.class, new GLState.Slot[] { HavenPanel.global });
    
    private static final ShaderMacro[] nshaders = new ShaderMacro[] { Tex2D.mod };
    
    private static final ShaderMacro[] cshaders = new ShaderMacro[] { Tex2D.mod, TexGL.mkcentroid };
    
    public final TexGL tex;
    
    private GLState.TexUnit sampler;
    
    public TexDraw(TexGL tex) {
      this.tex = tex;
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(slot, this);
    }
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      this.sampler = TexGL.lbind(g, this.tex);
      if (g.st.prog != null) {
        reapply(g);
      } else {
        gl.glTexEnvi(8960, 8704, 8448);
        gl.glEnable(3553);
      } 
    }
    
    public void reapply(GOut g) {
      GL2 gl = g.gl;
      gl.glUniform1i(g.st.prog.uniform(Tex2D.tex2d), this.sampler.id);
    }
    
    public void unapply(GOut g) {
      GL2 gl = g.gl;
      this.sampler.act();
      if (!g.st.usedprog)
        gl.glDisable(3553); 
      this.sampler.free();
      this.sampler = null;
    }
    
    public ShaderMacro[] shaders() {
      if (this.tex.centroid)
        return cshaders; 
      return nshaders;
    }
    
    public int capply() {
      return 100;
    }
    
    public int capplyfrom(GLState from) {
      if (from instanceof TexDraw)
        return 99; 
      return -1;
    }
    
    public void applyfrom(GOut g, GLState sfrom) {
      GL2 gl = g.gl;
      TexDraw from = (TexDraw)sfrom;
      from.sampler.act();
      int glid = this.tex.glid(g);
      this.sampler = from.sampler;
      from.sampler = null;
      gl.glBindTexture(3553, glid);
      if (g.st.pdirty)
        reapply(g); 
    }
    
    public String toString() {
      return "TexDraw(" + this.tex + ")";
    }
  }
  
  private final TexDraw draw = new TexDraw(this);
  
  public GLState draw() {
    return this.draw;
  }
  
  public static class TexClip extends GLState {
    public static final GLState.Slot<TexClip> slot = new GLState.Slot<>(GLState.Slot.Type.GEOM, TexClip.class, new GLState.Slot[] { HavenPanel.global, TexGL.TexDraw.slot });
    
    private static final ShaderMacro[] shaders = new ShaderMacro[] { Tex2D.clip };
    
    public final TexGL tex;
    
    private GLState.TexUnit sampler;
    
    public TexClip(TexGL tex) {
      this.tex = tex;
    }
    
    private void fapply(GOut g) {
      GL2 gl = g.gl;
      TexGL.TexDraw draw = g.st.<TexGL.TexDraw>get(TexGL.TexDraw.slot);
      if (draw == null) {
        this.sampler = TexGL.lbind(g, this.tex);
        gl.glTexEnvi(8960, 8704, 34160);
        gl.glTexEnvi(8960, 34161, 7681);
        gl.glTexEnvi(8960, 34176, 34168);
        gl.glTexEnvi(8960, 34192, 768);
        gl.glTexEnvi(8960, 34162, 8448);
        gl.glTexEnvi(8960, 34184, 34168);
        gl.glTexEnvi(8960, 34200, 770);
        gl.glTexEnvi(8960, 34185, 5890);
        gl.glTexEnvi(8960, 34201, 770);
        gl.glEnable(3553);
      } else if (draw.tex != this.tex) {
        throw new RuntimeException("TexGL does not support different clip and draw textures.");
      } 
      gl.glEnable(3008);
    }
    
    private void papply(GOut g) {
      TexGL.TexDraw draw = g.st.<TexGL.TexDraw>get(TexGL.TexDraw.slot);
      if (draw == null) {
        this.sampler = TexGL.lbind(g, this.tex);
      } else if (draw.tex != this.tex) {
        throw new RuntimeException("TexGL does not support different clip and draw textures.");
      } 
    }
    
    public void apply(GOut g) {
      if (g.st.prog == null) {
        fapply(g);
      } else {
        papply(g);
      } 
      if (g.gc.pref.alphacov.val.booleanValue()) {
        g.gl.glEnable(32926);
        g.gl.glEnable(32927);
      } 
    }
    
    private void funapply(GOut g) {
      GL2 gl = g.gl;
      if (g.st.old(TexGL.TexDraw.slot) == null) {
        this.sampler.act();
        gl.glDisable(3553);
        this.sampler.free();
        this.sampler = null;
      } 
      gl.glDisable(3008);
    }
    
    private void punapply(GOut g) {
      GL2 gl = g.gl;
      if (g.st.old(TexGL.TexDraw.slot) == null) {
        this.sampler.act();
        this.sampler.free();
        this.sampler = null;
      } 
    }
    
    public void unapply(GOut g) {
      if (!g.st.usedprog) {
        funapply(g);
      } else {
        punapply(g);
      } 
      if (g.gc.pref.alphacov.val.booleanValue()) {
        g.gl.glDisable(32926);
        g.gl.glDisable(32927);
      } 
    }
    
    public ShaderMacro[] shaders() {
      return shaders;
    }
    
    public int capply() {
      return 100;
    }
    
    public int capplyfrom(GLState from) {
      if (from instanceof TexClip)
        return 99; 
      return -1;
    }
    
    public void applyfrom(GOut g, GLState sfrom) {
      TexGL.TexDraw draw = g.st.<TexGL.TexDraw>get(TexGL.TexDraw.slot), old = g.st.<TexGL.TexDraw>old(TexGL.TexDraw.slot);
      if (old != null || draw != null)
        throw new RuntimeException("TexClip is somehow being transition even though there is a TexDraw"); 
      GL2 gl = g.gl;
      TexClip from = (TexClip)sfrom;
      from.sampler.act();
      int glid = this.tex.glid(g);
      this.sampler = from.sampler;
      from.sampler = null;
      gl.glBindTexture(3553, glid);
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(slot, this);
    }
    
    public String toString() {
      return "TexClip(" + this.tex + ")";
    }
  }
  
  private final TexClip clip = new TexClip(this);
  
  public GLState clip() {
    return this.clip;
  }
  
  public TexGL(Coord sz, Coord tdim) {
    super(sz);
    this.tdim = tdim;
  }
  
  public TexGL(Coord sz) {
    this(sz, new Coord(Tex.nextp2(sz.x), Tex.nextp2(sz.y)));
  }
  
  protected abstract void fill(GOut paramGOut);
  
  public static int num() {
    synchronized (active) {
      return active.size();
    } 
  }
  
  public static void setallparams() {
    synchronized (active) {
      for (TexGL tex : active)
        tex.setparams = true; 
    } 
  }
  
  protected void setparams(GOut g) {
    GL2 gL2 = g.gl;
    gL2.glTexParameteri(3553, 10241, this.minfilter);
    gL2.glTexParameteri(3553, 10240, this.magfilter);
    if (this.minfilter == 9987 && g.gc.pref.anisotex.val.floatValue() >= 1.0F)
      gL2.glTexParameterf(3553, 34046, Math.max(g.gc.pref.anisotex.val.floatValue(), 1.0F)); 
    gL2.glTexParameteri(3553, 10242, this.wrapmode);
    gL2.glTexParameteri(3553, 10243, this.wrapmode);
  }
  
  private void create(GOut g) {
    GL2 gl = g.gl;
    this.t = new TexOb(gl);
    gl.glBindTexture(3553, this.t.id);
    setparams(g);
    try {
      fill(g);
    } catch (Loading l) {
      this.t.dispose();
      this.t = null;
      throw l;
    } 
    try {
      GOut.checkerr((GL)gl);
    } catch (GLOutOfMemoryException e) {
      throw new RuntimeException("Out of memory when create texture " + this + " of class " + getClass().getName(), e);
    } 
  }
  
  public float tcx(int x) {
    return x / this.tdim.x;
  }
  
  public float tcy(int y) {
    return y / this.tdim.y;
  }
  
  @Deprecated
  public void mipmap() {
    this.mipmap = true;
    this.minfilter = 9987;
    dispose();
  }
  
  public void magfilter(int filter) {
    this.magfilter = filter;
    this.setparams = true;
  }
  
  public void minfilter(int filter) {
    this.minfilter = filter;
    this.setparams = true;
  }
  
  public void wrapmode(int mode) {
    this.wrapmode = mode;
    this.setparams = true;
  }
  
  public int glid(GOut g) {
    GL2 gL2 = g.gl;
    synchronized (this.idmon) {
      if (this.t != null && this.t.gl != gL2)
        dispose(); 
      if (this.t == null) {
        create(g);
        synchronized (active) {
          this.actref = active.add2(this);
        } 
      } else if (this.setparams) {
        gL2.glBindTexture(3553, this.t.id);
        setparams(g);
        this.setparams = false;
      } 
      return this.t.id;
    } 
  }
  
  public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
    GL2 gl = g.gl;
    g.st.prep(this.draw);
    g.apply();
    GOut.checkerr((GL)gl);
    if (!disableall) {
      gl.glBegin(7);
      float l = ul.x / this.tdim.x;
      float t = ul.y / this.tdim.y;
      float r = br.x / this.tdim.x;
      float b = br.y / this.tdim.y;
      gl.glTexCoord2f(l, t);
      gl.glVertex3i(c.x, c.y, 0);
      gl.glTexCoord2f(r, t);
      gl.glVertex3i(c.x + sz.x, c.y, 0);
      gl.glTexCoord2f(r, b);
      gl.glVertex3i(c.x + sz.x, c.y + sz.y, 0);
      gl.glTexCoord2f(l, b);
      gl.glVertex3i(c.x, c.y + sz.y, 0);
      gl.glEnd();
      GOut.checkerr((GL)gl);
    } 
  }
  
  public void dispose() {
    synchronized (this.idmon) {
      if (this.t != null) {
        this.t.dispose();
        this.t = null;
        synchronized (active) {
          this.actref.remove();
          this.actref = null;
        } 
      } 
    } 
  }
  
  public BufferedImage get(GOut g, boolean invert) {
    GL2 gl = g.gl;
    g.state2d();
    g.apply();
    GLState.TexUnit s = g.st.texalloc();
    s.act();
    gl.glBindTexture(3553, glid(g));
    byte[] buf = new byte[this.tdim.x * this.tdim.y * 4];
    gl.glGetTexImage(3553, 0, 6408, 5121, ByteBuffer.wrap(buf));
    s.free();
    GOut.checkerr((GL)gl);
    if (invert)
      for (int y = 0; y < this.tdim.y / 2; y++) {
        int to = y * this.tdim.x * 4, bo = (this.tdim.y - y - 1) * this.tdim.x * 4;
        for (int o = 0; o < this.tdim.x * 4; o++, to++, bo++) {
          byte t = buf[to];
          buf[to] = buf[bo];
          buf[bo] = t;
        } 
      }  
    WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(buf, buf.length), this.tdim.x, this.tdim.y, 4 * this.tdim.x, 4, new int[] { 0, 1, 2, 3 }, (Point)null);
    return new BufferedImage(TexI.glcm, raster, false, null);
  }
  
  public BufferedImage get(GOut g) {
    return get(g, true);
  }
  
  @ResName("tex")
  public static class $tex implements Material.ResCons2 {
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
      boolean tclip = true;
      while (a < args.length) {
        String f = (String)args[a++];
        if (f.equals("a"))
          tclip = false; 
      } 
      final boolean clip = tclip;
      left.add(new Material.Res.Resolver() {
            public void resolve(Collection<GLState> buf) {
              Tex tex;
              TexR rt = tres.<Integer, TexR>layer(TexR.class, Integer.valueOf(tid));
              if (rt != null) {
                tex = rt.tex();
              } else {
                Resource.Image img = tres.<Integer, Resource.Image>layer(Resource.imgc, Integer.valueOf(tid));
                if (img != null) {
                  tex = img.tex();
                } else {
                  throw new RuntimeException(String.format("Specified texture %d for %s not found in %s", new Object[] { Integer.valueOf(this.val$tid), this.val$res, this.val$tres }));
                } 
              } 
              buf.add(tex.draw());
              if (clip)
                buf.add(tex.clip()); 
            }
          });
    }
  }
  
  static {
    Console.setscmd("texdis", new Console.Command() {
          public void run(Console cons, String[] args) {
            TexGL.disableall = (Integer.parseInt(args[1]) != 0);
          }
        });
  }
}
