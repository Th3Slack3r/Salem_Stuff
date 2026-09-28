package haven;

import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Varying;
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
   protected TexGL.TexOb t = null;
   protected boolean mipmap = false;
   protected boolean centroid = false;
   protected int magfilter = 9728;
   protected int minfilter = 9728;
   protected int wrapmode = 10497;
   protected Coord tdim;
   private final Object idmon = new Object();
   private WeakList.Entry<TexGL> actref;
   private boolean setparams = true;
   public static final ShaderMacro mkcentroid = new ShaderMacro() {
      @Override
      public void modify(ProgramContext prog) {
         Tex2D.get(prog).ipol = Varying.Interpol.CENTROID;
      }
   };
   private final TexGL.TexDraw draw = new TexGL.TexDraw(this);
   private final TexGL.TexClip clip = new TexGL.TexClip(this);

   public static GLState.TexUnit lbind(GOut g, TexGL tex) {
      GLState.TexUnit sampler = g.st.texalloc();
      sampler.act();

      try {
         g.gl.glBindTexture(3553, tex.glid(g));
         return sampler;
      } catch (Loading var4) {
         sampler.free();
         throw var4;
      }
   }

   @Override
   public GLState draw() {
      return this.draw;
   }

   @Override
   public GLState clip() {
      return this.clip;
   }

   public TexGL(Coord sz, Coord tdim) {
      super(sz);
      this.tdim = tdim;
   }

   public TexGL(Coord sz) {
      this(sz, new Coord(nextp2(sz.x), nextp2(sz.y)));
   }

   protected abstract void fill(GOut var1);

   public static int num() {
      synchronized (active) {
         return active.size();
      }
   }

   public static void setallparams() {
      synchronized (active) {
         for (TexGL tex : active) {
            tex.setparams = true;
         }
      }
   }

   protected void setparams(GOut g) {
      GL gl = g.gl;
      gl.glTexParameteri(3553, 10241, this.minfilter);
      gl.glTexParameteri(3553, 10240, this.magfilter);
      if (this.minfilter == 9987 && g.gc.pref.anisotex.val >= 1.0F) {
         gl.glTexParameterf(3553, 34046, Math.max(g.gc.pref.anisotex.val, 1.0F));
      }

      gl.glTexParameteri(3553, 10242, this.wrapmode);
      gl.glTexParameteri(3553, 10243, this.wrapmode);
   }

   private void create(GOut g) {
      GL2 gl = g.gl;
      this.t = new TexGL.TexOb(gl);
      gl.glBindTexture(3553, this.t.id);
      this.setparams(g);

      try {
         this.fill(g);
      } catch (Loading var5) {
         this.t.dispose();
         this.t = null;
         throw var5;
      }

      try {
         GOut.checkerr(gl);
      } catch (GOut.GLOutOfMemoryException var4) {
         throw new RuntimeException("Out of memory when create texture " + this + " of class " + this.getClass().getName(), var4);
      }
   }

   @Override
   public float tcx(int x) {
      return (float)x / this.tdim.x;
   }

   @Override
   public float tcy(int y) {
      return (float)y / this.tdim.y;
   }

   @Deprecated
   public void mipmap() {
      this.mipmap = true;
      this.minfilter = 9987;
      this.dispose();
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
      GL gl = g.gl;
      synchronized (this.idmon) {
         if (this.t != null && this.t.gl != gl) {
            this.dispose();
         }

         if (this.t == null) {
            this.create(g);
            synchronized (active) {
               this.actref = active.add2(this);
            }
         } else if (this.setparams) {
            gl.glBindTexture(3553, this.t.id);
            this.setparams(g);
            this.setparams = false;
         }

         return this.t.id;
      }
   }

   @Override
   public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
      GL2 gl = g.gl;
      g.st.prep(this.draw);
      g.apply();
      GOut.checkerr(gl);
      if (!disableall) {
         gl.glBegin(7);
         float l = (float)ul.x / this.tdim.x;
         float t = (float)ul.y / this.tdim.y;
         float r = (float)br.x / this.tdim.x;
         float b = (float)br.y / this.tdim.y;
         gl.glTexCoord2f(l, t);
         gl.glVertex3i(c.x, c.y, 0);
         gl.glTexCoord2f(r, t);
         gl.glVertex3i(c.x + sz.x, c.y, 0);
         gl.glTexCoord2f(r, b);
         gl.glVertex3i(c.x + sz.x, c.y + sz.y, 0);
         gl.glTexCoord2f(l, b);
         gl.glVertex3i(c.x, c.y + sz.y, 0);
         gl.glEnd();
         GOut.checkerr(gl);
      }
   }

   @Override
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
      gl.glBindTexture(3553, this.glid(g));
      byte[] buf = new byte[this.tdim.x * this.tdim.y * 4];
      gl.glGetTexImage(3553, 0, 6408, 5121, ByteBuffer.wrap(buf));
      s.free();
      GOut.checkerr(gl);
      if (invert) {
         for (int y = 0; y < this.tdim.y / 2; y++) {
            int to = y * this.tdim.x * 4;
            int bo = (this.tdim.y - y - 1) * this.tdim.x * 4;

            for (int o = 0; o < this.tdim.x * 4; bo++) {
               byte t = buf[to];
               buf[to] = buf[bo];
               buf[bo] = t;
               o++;
               to++;
            }
         }
      }

      WritableRaster raster = Raster.createInterleavedRaster(
         new DataBufferByte(buf, buf.length), this.tdim.x, this.tdim.y, 4 * this.tdim.x, 4, new int[]{0, 1, 2, 3}, null
      );
      return new BufferedImage(TexI.glcm, raster, false, null);
   }

   public BufferedImage get(GOut g) {
      return this.get(g, true);
   }

   static {
      Console.setscmd("texdis", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            TexGL.disableall = Integer.parseInt(args[1]) != 0;
         }
      });
   }

   @Material.ResName("tex")
   public static class $tex implements Material.ResCons2 {
      @Override
      public void cons(final Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
         int a = 0;
         final Resource tres;
         final int tid;
         if (args[a] instanceof String) {
            tres = Resource.load((String)args[a], (Integer)args[a + 1]);
            tid = (Integer)args[a + 2];
            a += 3;
         } else {
            tres = res;
            tid = (Integer)args[a];
            a++;
         }

         boolean tclip = true;

         while (a < args.length) {
            String f = (String)args[a++];
            if (f.equals("a")) {
               tclip = false;
            }
         }

         final boolean clip = tclip;
         left.add(new Material.Res.Resolver() {
            @Override
            public void resolve(Collection<GLState> buf) {
               TexR rt = tres.layer(TexR.class, tid);
               Tex tex;
               if (rt != null) {
                  tex = rt.tex();
               } else {
                  Resource.Image img = tres.layer(Resource.imgc, tid);
                  if (img == null) {
                     throw new RuntimeException(String.format("Specified texture %d for %s not found in %s", tid, res, tres));
                  }

                  tex = img.tex();
               }

               buf.add(tex.draw());
               if (clip) {
                  buf.add(tex.clip());
               }
            }
         });
      }
   }

   public static class TexClip extends GLState {
      public static final GLState.Slot<TexGL.TexClip> slot = new GLState.Slot<>(
         GLState.Slot.Type.GEOM, TexGL.TexClip.class, HavenPanel.global, TexGL.TexDraw.slot
      );
      private static final ShaderMacro[] shaders = new ShaderMacro[]{Tex2D.clip};
      public final TexGL tex;
      private GLState.TexUnit sampler;

      public TexClip(TexGL tex) {
         this.tex = tex;
      }

      private void fapply(GOut g) {
         GL2 gl = g.gl;
         TexGL.TexDraw draw = g.st.get(TexGL.TexDraw.slot);
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
         TexGL.TexDraw draw = g.st.get(TexGL.TexDraw.slot);
         if (draw == null) {
            this.sampler = TexGL.lbind(g, this.tex);
         } else if (draw.tex != this.tex) {
            throw new RuntimeException("TexGL does not support different clip and draw textures.");
         }
      }

      @Override
      public void apply(GOut g) {
         if (g.st.prog == null) {
            this.fapply(g);
         } else {
            this.papply(g);
         }

         if (g.gc.pref.alphacov.val) {
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

      @Override
      public void unapply(GOut g) {
         if (!g.st.usedprog) {
            this.funapply(g);
         } else {
            this.punapply(g);
         }

         if (g.gc.pref.alphacov.val) {
            g.gl.glDisable(32926);
            g.gl.glDisable(32927);
         }
      }

      @Override
      public ShaderMacro[] shaders() {
         return shaders;
      }

      @Override
      public int capply() {
         return 100;
      }

      @Override
      public int capplyfrom(GLState from) {
         return from instanceof TexGL.TexClip ? 99 : -1;
      }

      @Override
      public void applyfrom(GOut g, GLState sfrom) {
         TexGL.TexDraw draw = g.st.get(TexGL.TexDraw.slot);
         TexGL.TexDraw old = g.st.old(TexGL.TexDraw.slot);
         if (old == null && draw == null) {
            GL2 gl = g.gl;
            TexGL.TexClip from = (TexGL.TexClip)sfrom;
            from.sampler.act();
            int glid = this.tex.glid(g);
            this.sampler = from.sampler;
            from.sampler = null;
            gl.glBindTexture(3553, glid);
         } else {
            throw new RuntimeException("TexClip is somehow being transition even though there is a TexDraw");
         }
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(slot, this);
      }

      @Override
      public String toString() {
         return "TexClip(" + this.tex + ")";
      }
   }

   public static class TexDraw extends GLState {
      public static final GLState.Slot<TexGL.TexDraw> slot = new GLState.Slot<>(GLState.Slot.Type.DRAW, TexGL.TexDraw.class, HavenPanel.global);
      private static final ShaderMacro[] nshaders = new ShaderMacro[]{Tex2D.mod};
      private static final ShaderMacro[] cshaders = new ShaderMacro[]{Tex2D.mod, TexGL.mkcentroid};
      public final TexGL tex;
      private GLState.TexUnit sampler;

      public TexDraw(TexGL tex) {
         this.tex = tex;
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(slot, this);
      }

      @Override
      public void apply(GOut g) {
         GL2 gl = g.gl;
         this.sampler = TexGL.lbind(g, this.tex);
         if (g.st.prog != null) {
            this.reapply(g);
         } else {
            gl.glTexEnvi(8960, 8704, 8448);
            gl.glEnable(3553);
         }
      }

      @Override
      public void reapply(GOut g) {
         GL2 gl = g.gl;
         gl.glUniform1i(g.st.prog.uniform(Tex2D.tex2d), this.sampler.id);
      }

      @Override
      public void unapply(GOut g) {
         GL2 gl = g.gl;
         this.sampler.act();
         if (!g.st.usedprog) {
            gl.glDisable(3553);
         }

         this.sampler.free();
         this.sampler = null;
      }

      @Override
      public ShaderMacro[] shaders() {
         return this.tex.centroid ? cshaders : nshaders;
      }

      @Override
      public int capply() {
         return 100;
      }

      @Override
      public int capplyfrom(GLState from) {
         return from instanceof TexGL.TexDraw ? 99 : -1;
      }

      @Override
      public void applyfrom(GOut g, GLState sfrom) {
         GL2 gl = g.gl;
         TexGL.TexDraw from = (TexGL.TexDraw)sfrom;
         from.sampler.act();
         int glid = this.tex.glid(g);
         this.sampler = from.sampler;
         from.sampler = null;
         gl.glBindTexture(3553, glid);
         if (g.st.pdirty) {
            this.reapply(g);
         }
      }

      @Override
      public String toString() {
         return "TexDraw(" + this.tex + ")";
      }
   }

   public static class TexOb extends GLObject {
      public final int id;

      public TexOb(GL2 gl) {
         super(gl);
         int[] buf = new int[1];
         gl.glGenTextures(1, buf, 0);
         this.id = buf[0];
      }

      @Override
      protected void delete() {
         int[] buf = new int[]{this.id};
         this.gl.glDeleteTextures(1, buf, 0);
      }
   }
}
