package haven;

import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.Macro1;
import haven.glsl.MiscLib;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Type;
import haven.glsl.Uniform;
import java.util.Collection;
import java.util.LinkedList;

public class FBConfig {
   public final PView.ConfContext ctx;
   public Coord sz;
   public boolean hdr;
   public boolean tdepth;
   public int ms = 1;
   public GLFrameBuffer fb;
   public PView.RenderState wnd;
   public GLFrameBuffer.Attachment[] color;
   public GLFrameBuffer.Attachment depth;
   public GLState state;
   private FBConfig.RenderTarget[] tgts = new FBConfig.RenderTarget[0];
   private FBConfig.ResolveFilter[] res = new FBConfig.ResolveFilter[0];
   private GLState resp;
   private static final ShaderMacro[] nosh = new ShaderMacro[0];
   public static final Uniform numsamples = new Uniform.AutoApply(Type.INT) {
      @Override
      public void apply(GOut g, int loc) {
         g.gl.glUniform1i(loc, ((PView.ConfContext)g.st.get(PView.ctx)).cur.ms);
      }
   };

   public FBConfig(PView.ConfContext ctx, Coord sz) {
      this.ctx = ctx;
      this.sz = sz;
   }

   public boolean cleanp() {
      if (!this.hdr && !this.tdepth && this.ms <= 1) {
         for (int i = 0; i < this.tgts.length; i++) {
            if (this.tgts[i] != null) {
               return false;
            }
         }

         for (FBConfig.ResolveFilter rf : this.res) {
            if (!rf.cleanp()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void create() {
      Collection<GLFrameBuffer.Attachment> color = new LinkedList<>();
      Collection<ShaderMacro> shb = new LinkedList<>();
      Collection<GLState> stb = new LinkedList<>();
      int fmt = this.hdr ? 34842 : 6408;
      if (this.ms <= 1) {
         color.add(GLFrameBuffer.Attachment.mk(new TexE(this.sz, fmt, 6408, 5121)));
      } else {
         color.add(GLFrameBuffer.Attachment.mk(new TexMSE(this.sz, this.ms, fmt, 6408, 5121)));
      }

      GLFrameBuffer.Attachment depth;
      if (this.tdepth) {
         if (this.ms <= 1) {
            depth = GLFrameBuffer.Attachment.mk(new TexE(this.sz, 6402, 6402, 5125));
         } else {
            depth = GLFrameBuffer.Attachment.mk(new TexMSE(this.sz, this.ms, 6402, 6402, 5125));
         }
      } else {
         depth = GLFrameBuffer.Attachment.mk(new GLFrameBuffer.RenderBuffer(this.sz, 6402, this.ms));
      }

      for (int i = 0; i < this.tgts.length; i++) {
         if (this.tgts[i] != null) {
            color.add(this.tgts[i].maketex(this));
            GLState st = this.tgts[i].state(this, i + 1);
            if (st != null) {
               stb.add(st);
            }

            ShaderMacro code = this.tgts[i].code(this, i + 1);
            if (code != null) {
               shb.add(code);
            }
         }
      }

      this.color = color.toArray(new GLFrameBuffer.Attachment[0]);
      this.depth = depth;
      final ShaderMacro[] shaders;
      if (shb.size() < 1) {
         shaders = nosh;
      } else {
         shaders = shb.toArray(new ShaderMacro[0]);
      }

      this.fb = new GLFrameBuffer(this.color, this.depth) {
         @Override
         public ShaderMacro[] shaders() {
            return shaders;
         }

         @Override
         public boolean reqshaders() {
            return shaders.length > 0;
         }
      };
      this.wnd = new PView.RenderState() {
         @Override
         public Coord ul() {
            return Coord.z;
         }

         @Override
         public Coord sz() {
            return FBConfig.this.sz;
         }
      };
      stb.add(this.fb);
      stb.add(this.wnd);
      this.state = GLState.compose(stb.toArray(new GLState[0]));
      if (this.res.length > 0) {
         ShaderMacro[] resp = new ShaderMacro[this.res.length];

         for (int ix = 0; ix < this.res.length; ix++) {
            resp[ix] = this.res[ix].code(this);
         }

         resp = ArrayIdentity.intern(resp);
         this.resp = new States.AdHoc(resp) {
            @Override
            public void apply(GOut g) {
               for (FBConfig.ResolveFilter f : FBConfig.this.res) {
                  f.apply(FBConfig.this, g);
               }
            }

            @Override
            public void unapply(GOut g) {
               for (FBConfig.ResolveFilter f : FBConfig.this.res) {
                  f.unapply(FBConfig.this, g);
               }
            }
         };
      }
   }

   private static <T> boolean hasuo(T[] a, T[] b) {
      label24:
      for (T ae : a) {
         for (T be : b) {
            if (Utils.eq(ae, be)) {
               continue label24;
            }
         }

         return false;
      }

      return true;
   }

   public static boolean equals(FBConfig a, FBConfig b) {
      if (!a.sz.equals(b.sz)) {
         return false;
      } else if (a.hdr != b.hdr || a.tdepth != b.tdepth) {
         return false;
      } else if (a.ms != b.ms) {
         return false;
      } else {
         return !hasuo(a.tgts, b.tgts) || !hasuo(b.tgts, a.tgts) ? false : hasuo(a.res, b.res) && hasuo(b.res, a.res);
      }
   }

   private void subsume(FBConfig last) {
      this.fb = last.fb;
      this.wnd = last.wnd;
      this.color = last.color;
      this.depth = last.depth;
      this.tgts = last.tgts;
      this.res = last.res;
      this.resp = last.resp;
      this.state = last.state;
   }

   public void fin(FBConfig last) {
      if (this.ms <= 1) {
         this.add(new FBConfig.Resolve1());
      } else {
         this.add(new FBConfig.ResolveMS(this.ms));
      }

      if (equals(this, last)) {
         this.subsume(last);
      } else {
         if (last.fb != null) {
            last.fb.dispose();
         }

         if (!this.cleanp()) {
            this.create();
         }
      }
   }

   public void resolve(GOut g) {
      if (this.fb != null) {
         for (FBConfig.ResolveFilter rf : this.res) {
            rf.prepare(this, g);
         }

         g.ftexrect(Coord.z, this.sz, this.resp);
      }
   }

   public FBConfig.RenderTarget add(FBConfig.RenderTarget tgt) {
      if (tgt == null) {
         throw new NullPointerException();
      } else {
         for (FBConfig.RenderTarget p : this.tgts) {
            if (Utils.eq(tgt, p)) {
               return p;
            }
         }

         int i = 0;
         if (i < this.tgts.length) {
            if (this.tgts[i] == null) {
               this.tgts[i] = tgt;
            }

            return tgt;
         } else {
            this.tgts = Utils.extend(this.tgts, i + 1);
            this.tgts[i] = tgt;
            return tgt;
         }
      }
   }

   public FBConfig.ResolveFilter add(FBConfig.ResolveFilter rf) {
      if (rf == null) {
         throw new NullPointerException();
      } else {
         for (FBConfig.ResolveFilter p : this.res) {
            if (Utils.eq(rf, p)) {
               return p;
            }
         }

         int l = this.res.length;
         this.res = Utils.extend(this.res, l + 1);
         this.res[l] = rf;
         return rf;
      }
   }

   public abstract static class RenderTarget {
      public GLFrameBuffer.Attachment tex;

      public GLFrameBuffer.Attachment maketex(FBConfig cfg) {
         return cfg.ms <= 1
            ? (this.tex = GLFrameBuffer.Attachment.mk(new TexE(cfg.sz, 6408, 6408, 5121)))
            : (this.tex = GLFrameBuffer.Attachment.mk(new TexMSE(cfg.sz, cfg.ms, 6408, 6408, 5121)));
      }

      public GLState state(FBConfig cfg, int id) {
         return null;
      }

      public ShaderMacro code(FBConfig cfg, int id) {
         return null;
      }
   }

   private static class Resolve1 implements FBConfig.ResolveFilter {
      private static final Uniform ctex = new Uniform(Type.SAMPLER2D);
      private static final ShaderMacro code = new ShaderMacro() {
         @Override
         public void modify(ProgramContext prog) {
            prog.fctx.fragcol.mod(new Macro1<Expression>() {
               public Expression expand(Expression in) {
                  return Cons.texture2D(FBConfig.Resolve1.ctex.ref(), Tex2D.texcoord.ref());
               }
            }, 0);
         }
      };
      private GLState.TexUnit csmp;

      private Resolve1() {
      }

      @Override
      public void prepare(FBConfig cfg, GOut g) {
      }

      @Override
      public boolean cleanp() {
         return true;
      }

      @Override
      public ShaderMacro code(FBConfig cfg) {
         return code;
      }

      @Override
      public void apply(FBConfig cfg, GOut g) {
         this.csmp = g.st.texalloc(g, ((GLFrameBuffer.Attach2D)cfg.color[0]).tex);
         g.gl.glUniform1i(g.st.prog.uniform(ctex), this.csmp.id);
      }

      @Override
      public void unapply(FBConfig cfg, GOut g) {
         this.csmp.ufree();
         this.csmp = null;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof FBConfig.Resolve1;
      }
   }

   public interface ResolveFilter {
      boolean cleanp();

      void prepare(FBConfig var1, GOut var2);

      ShaderMacro code(FBConfig var1);

      void apply(FBConfig var1, GOut var2);

      void unapply(FBConfig var1, GOut var2);
   }

   private static class ResolveMS implements FBConfig.ResolveFilter {
      private final int samples;
      private static final Uniform ctex = new Uniform(Type.SAMPLER2DMS);
      private final ShaderMacro code = new ShaderMacro() {
         @Override
         public void modify(ProgramContext prog) {
            prog.fctx
               .fragcol
               .mod(
                  new Macro1<Expression>() {
                     public Expression expand(Expression in) {
                        Expression[] texels = new Expression[ResolveMS.this.samples];

                        for (int i = 0; i < ResolveMS.this.samples; i++) {
                           texels[i] = Cons.texelFetch(
                              FBConfig.ResolveMS.ctex.ref(), Cons.ivec2(Cons.floor(Cons.mul(Tex2D.texcoord.ref(), MiscLib.screensize.ref()))), Cons.l(i)
                           );
                        }

                        return Cons.mul(Cons.add(texels), Cons.l(1.0 / ResolveMS.this.samples));
                     }
                  },
                  0
               );
         }
      };
      private GLState.TexUnit csmp;

      private ResolveMS(int samples) {
         this.samples = samples;
      }

      @Override
      public void prepare(FBConfig cfg, GOut g) {
      }

      @Override
      public boolean cleanp() {
         return true;
      }

      @Override
      public ShaderMacro code(FBConfig cfg) {
         return this.code;
      }

      @Override
      public void apply(FBConfig cfg, GOut g) {
         this.csmp = g.st.texalloc(g, ((GLFrameBuffer.AttachMS)cfg.color[0]).tex);
         g.gl.glUniform1i(g.st.prog.uniform(ctex), this.csmp.id);
      }

      @Override
      public void unapply(FBConfig cfg, GOut g) {
         this.csmp.ufree();
         this.csmp = null;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof FBConfig.ResolveMS && ((FBConfig.ResolveMS)o).samples == this.samples;
      }
   }
}
