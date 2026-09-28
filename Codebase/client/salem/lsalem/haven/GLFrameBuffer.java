package haven;

import javax.media.opengl.GL2;

public class GLFrameBuffer extends GLState {
   public static final GLState.Slot<GLFrameBuffer> slot = new GLState.Slot<>(GLState.Slot.Type.SYS, GLFrameBuffer.class, HavenPanel.global);
   private final GLFrameBuffer.Attachment[] color;
   private final GLFrameBuffer.Attachment depth;
   private final GLFrameBuffer.RenderBuffer altdepth;
   private GLFrameBuffer.FBO fbo;
   private final int[] bufmask;

   public GLFrameBuffer(GLFrameBuffer.Attachment[] color, GLFrameBuffer.Attachment depth) {
      this.color = color;
      this.bufmask = new int[this.color.length];
      if (depth == null) {
         if (this.color.length == 0) {
            throw new RuntimeException("Cannot create a framebuffer with neither color nor depth");
         }

         this.altdepth = new GLFrameBuffer.RenderBuffer(color[0].sz(), 6402);
         this.depth = new GLFrameBuffer.AttachRBO(this.altdepth);
      } else {
         this.altdepth = null;
         this.depth = depth;
      }
   }

   private static GLFrameBuffer.Attachment[] javaIsRetarded(TexGL[] color) {
      GLFrameBuffer.Attachment[] ret = new GLFrameBuffer.Attachment[color.length];

      for (int i = 0; i < color.length; i++) {
         ret[i] = new GLFrameBuffer.Attach2D(color[i]);
      }

      return ret;
   }

   public GLFrameBuffer(TexGL[] color, TexGL depth) {
      this(javaIsRetarded(color), depth == null ? null : new GLFrameBuffer.Attach2D(depth));
   }

   public GLFrameBuffer(TexGL color, TexGL depth) {
      this(color == null ? new TexGL[0] : new TexGL[]{color}, depth);
   }

   public Coord sz() {
      return this.depth.sz();
   }

   @Override
   public void apply(GOut g) {
      GL2 gl = g.gl;
      synchronized (this) {
         if (this.fbo != null && this.fbo.gl != gl) {
            this.dispose();
         }

         if (this.fbo != null) {
            gl.glBindFramebuffer(36160, this.fbo.id);
         } else {
            this.fbo = new GLFrameBuffer.FBO(gl);
            gl.glBindFramebuffer(36160, this.fbo.id);

            for (int i = 0; i < this.color.length; i++) {
               this.color[i].attach(g, this, 36064 + i);
            }

            this.depth.attach(g, this, 36096);
            if (this.color.length == 0) {
               gl.glDrawBuffer(0);
               gl.glReadBuffer(0);
            } else if (this.color.length > 1) {
               for (int i = 0; i < this.color.length; i++) {
                  this.bufmask[i] = 36064 + i;
               }

               gl.glDrawBuffers(this.color.length, this.bufmask, 0);
            }

            GOut.checkerr(gl);
            int st = gl.glCheckFramebufferStatus(36160);
            if (st != 36053) {
               throw new RuntimeException("FBO failed completeness test: " + st);
            }
         }
      }

      gl.glViewport(0, 0, this.sz().x, this.sz().y);
   }

   public void mask(GOut g, int id, boolean flag) {
      int nb = flag ? 36064 + id : 0;
      if (this.bufmask[id] != nb) {
         this.bufmask[id] = nb;
         g.gl.glDrawBuffers(this.color.length, this.bufmask, 0);
      }
   }

   @Override
   public void unapply(GOut g) {
      GL2 gl = g.gl;
      gl.glBindFramebuffer(36160, 0);
      gl.glViewport(g.root().ul.x, g.root().ul.y, g.root().sz.x, g.root().sz.y);
   }

   @Override
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

      if (this.altdepth != null) {
         this.altdepth.dispose();
      }
   }

   public static class Attach2D extends GLFrameBuffer.Attachment {
      public final TexGL tex;
      public final int level;

      public Attach2D(TexGL tex, int level) {
         this.tex = tex;
         this.level = level;
      }

      public Attach2D(TexGL tex) {
         this(tex, 0);
      }

      @Override
      public void attach(GOut g, GLFrameBuffer fbo, int point) {
         g.gl.glFramebufferTexture2D(36160, point, 3553, this.tex.glid(g), this.level);
      }

      @Override
      public Coord sz() {
         return this.tex.sz();
      }
   }

   public static class AttachMS extends GLFrameBuffer.Attachment {
      public final TexMS tex;

      public AttachMS(TexMS tex) {
         this.tex = tex;
      }

      @Override
      public void attach(GOut g, GLFrameBuffer fbo, int point) {
         g.gl.glFramebufferTexture2D(36160, point, 37120, this.tex.glid(g), 0);
      }

      @Override
      public Coord sz() {
         return new Coord(this.tex.w, this.tex.h);
      }
   }

   public static class AttachRBO extends GLFrameBuffer.Attachment {
      public final GLFrameBuffer.RenderBuffer rbo;

      public AttachRBO(GLFrameBuffer.RenderBuffer rbo) {
         this.rbo = rbo;
      }

      @Override
      public void attach(GOut g, GLFrameBuffer fbo, int point) {
         g.gl.glFramebufferRenderbuffer(36160, point, 36161, this.rbo.glid(g.gl));
      }

      @Override
      public Coord sz() {
         return this.rbo.sz;
      }
   }

   public abstract static class Attachment {
      public abstract void attach(GOut var1, GLFrameBuffer var2, int var3);

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

   public static class FBO extends GLObject {
      public final int id;

      public FBO(GL2 gl) {
         super(gl);
         int[] buf = new int[1];
         gl.glGenFramebuffers(1, buf, 0);
         this.id = buf[0];
         GOut.checkerr(gl);
      }

      @Override
      protected void delete() {
         int[] buf = new int[]{this.id};
         this.gl.glDeleteFramebuffers(1, buf, 0);
         GOut.checkerr(this.gl);
      }
   }

   public static class RenderBuffer {
      public final Coord sz;
      public final int samples;
      public final int fmt;
      private GLFrameBuffer.RenderBuffer.RBO rbo;

      public RenderBuffer(Coord sz, int fmt, int samples) {
         this.sz = sz;
         this.fmt = fmt;
         this.samples = samples;
      }

      public RenderBuffer(Coord sz, int fmt) {
         this(sz, fmt, 1);
      }

      public int glid(GL2 gl) {
         if (this.rbo != null && this.rbo.gl != gl) {
            this.dispose();
         }

         if (this.rbo == null) {
            this.rbo = new GLFrameBuffer.RenderBuffer.RBO(gl);
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
            GOut.checkerr(gl);
         }

         @Override
         protected void delete() {
            int[] buf = new int[]{this.id};
            this.gl.glDeleteRenderbuffers(1, buf, 0);
            GOut.checkerr(this.gl);
         }
      }
   }
}
