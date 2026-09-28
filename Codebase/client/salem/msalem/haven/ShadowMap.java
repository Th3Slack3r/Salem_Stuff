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
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.VertexContext;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class ShadowMap extends GLState implements GLState.GlobalState, GLState.Global {
   public static final GLState.Slot<ShadowMap> smap = new GLState.Slot<>(GLState.Slot.Type.DRAW, ShadowMap.class, Light.lighting);
   public DirLight light;
   public final TexE lbuf;
   private final Projection lproj;
   private final DirCam lcam;
   private final FBView tgt;
   private static final Matrix4f texbias = new Matrix4f(0.5F, 0.0F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F, 0.0F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 1.0F);
   private final List<RenderList.Slot> parts = new ArrayList<>();
   private int slidx;
   private Matrix4f txf;
   private final Rendered scene = new Rendered() {
      @Override
      public void draw(GOut g) {
      }

      @Override
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
   public final ShadowMap.Shader shader;
   private final ShaderMacro[] shaders;
   private GLState.TexUnit sampler;

   public ShadowMap(Coord res, float size, float depth, float dthr) {
      this.lbuf = new TexE(res, 6402, 6402, 5125);
      this.lbuf.magfilter = 9729;
      this.lbuf.wrapmode = 10496;
      this.shader = new ShadowMap.Shader(1.0 / res.x, 1.0 / res.y, 4, dthr / depth);
      this.shaders = new ShaderMacro[]{this.shader};
      this.lproj = Projection.ortho(-size, size, -size, size, 1.0F, depth);
      this.lcam = new DirCam();
      this.tgt = new FBView(new GLFrameBuffer((TexGL)null, this.lbuf), GLState.compose(this.lproj, this.lcam));
   }

   public void setpos(Coord3f base, Coord3f dir) {
      this.lcam.base = base;
      this.lcam.dir = dir;
   }

   public void dispose() {
      this.lbuf.dispose();
      this.tgt.dispose();
   }

   @Override
   public void prerender(RenderList rl, GOut g) {
      this.parts.clear();
      Light.LightList ll = null;
      Camera cam = null;

      for (RenderList.Slot s : rl.slots()) {
         if (s.d && s.os.get(smap) == this && s.os.get(Light.lighting) != null) {
            if (ll == null) {
               PView.RenderState rs = s.os.get(PView.wnd);
               cam = s.os.get(PView.cam);
               ll = s.os.get(Light.lights);
            }

            this.parts.add(s);
         }
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

   @Override
   public GLState.Global global(RenderList rl, GLState.Buffer ctx) {
      return this;
   }

   @Override
   public void postsetup(RenderList rl) {
   }

   @Override
   public void postrender(RenderList rl, GOut g) {
   }

   @Override
   public void prep(GLState.Buffer buf) {
      buf.put(smap, this);
   }

   @Override
   public ShaderMacro[] shaders() {
      return this.shaders;
   }

   @Override
   public void apply(GOut g) {
      this.sampler = g.st.texalloc();
      if (g.st.prog != null) {
         GL gl = g.gl;
         this.sampler.act();
         gl.glBindTexture(3553, this.lbuf.glid(g));
         this.reapply(g);
      }
   }

   @Override
   public void reapply(GOut g) {
      GL2 gl = g.gl;
      int mapu = g.st.prog.cuniform(ShadowMap.Shader.map);
      if (mapu >= 0) {
         gl.glUniform1i(mapu, this.sampler.id);
         gl.glUniformMatrix4fv(g.st.prog.uniform(ShadowMap.Shader.txf), 1, false, this.txf.m, 0);
         gl.glUniform1i(g.st.prog.uniform(ShadowMap.Shader.sl), this.slidx);
      }
   }

   @Override
   public void unapply(GOut g) {
      GL gl = g.gl;
      this.sampler.act();
      gl.glBindTexture(3553, 0);
      this.sampler.free();
      this.sampler = null;
   }

   public static class Shader implements ShaderMacro {
      public static final Uniform txf = new Uniform(Type.MAT4);
      public static final Uniform sl = new Uniform(Type.INT);
      public static final Uniform map = new Uniform(Type.SAMPLER2D);
      public static final AutoVarying stc = new AutoVarying(Type.VEC4) {
         @Override
         public Expression root(VertexContext vctx) {
            return Cons.mul(ShadowMap.Shader.txf.ref(), vctx.eyev.depref());
         }
      };
      public final Function.Def shcalc;

      public Shader(final double xd, final double yd, final int res, final double thr) {
         this.shcalc = new Function.Def(Type.FLOAT) {
            {
               LValue sdw = this.code.local(Type.FLOAT, Cons.l(0.0)).ref();
               Expression mapc = this.code
                  .local(Type.VEC3, Cons.div(Cons.pick((LValue)ShadowMap.Shader.stc.ref(), "xyz"), Cons.pick((LValue)ShadowMap.Shader.stc.ref(), "w")))
                  .ref();
               double xr = xd * (res - 1);
               double yr = yd * (res - 1);
               boolean unroll = false;
               if (!unroll) {
                  LValue xo = this.code.local(Type.FLOAT, null).ref();
                  LValue yo = this.code.local(Type.FLOAT, null).ref();
                  this.code
                     .add(
                        new For(
                           Cons.ass(yo, Cons.l(-yr / 2.0)),
                           Cons.lt(yo, Cons.l(yr / 2.0 + yd / 2.0)),
                           Cons.aadd(yo, Cons.l(yd)),
                           new For(
                              Cons.ass(xo, Cons.l(-xr / 2.0)),
                              Cons.lt(xo, Cons.l(xr / 2.0 + xd / 2.0)),
                              Cons.aadd(xo, Cons.l(xd)),
                              new If(
                                 Cons.gt(
                                    Cons.add(
                                       Cons.pick(Cons.texture2D(ShadowMap.Shader.map.ref(), Cons.add(Cons.pick(mapc, "xy"), Cons.vec2(xo, yo))), "z"),
                                       Cons.l(thr)
                                    ),
                                    Cons.pick(mapc, "z")
                                 ),
                                 Cons.stmt(Cons.aadd(sdw, Cons.l(1.0 / (res * res))))
                              )
                           )
                        )
                     );
               } else {
                  for (double yo = -yr / 2.0; yo < yr / 2.0 + yd / 2.0; yo += yd) {
                     for (double xo = -xr / 2.0; xo < xr / 2.0 + xd / 2.0; xo += xd) {
                        this.code
                           .add(
                              new If(
                                 Cons.gt(
                                    Cons.add(
                                       Cons.pick(
                                          Cons.texture2D(ShadowMap.Shader.map.ref(), Cons.add(Cons.pick(mapc, "xy"), Cons.vec2(Cons.l(xo), Cons.l(yo)))), "z"
                                       ),
                                       Cons.l(thr)
                                    ),
                                    Cons.pick(mapc, "z")
                                 ),
                                 Cons.stmt(Cons.aadd(sdw, Cons.l(1.0 / (res * res))))
                              )
                           );
                     }
                  }
               }

               this.code.add(new Return(sdw));
            }
         };
      }

      @Override
      public void modify(ProgramContext prog) {
         final Phong ph = prog.getmod(Phong.class);
         if (ph != null && ph.pfrag) {
            ph.dolight
               .mod(
                  new Runnable() {
                     @Override
                     public void run() {
                        ph.dolight
                           .dcalc
                           .add(
                              new If(Cons.eq(ShadowMap.Shader.sl.ref(), ph.dolight.i), Cons.stmt(Cons.amul(ph.dolight.dl.var.ref(), Shader.this.shcalc.call()))),
                              ph.dolight.dcurs
                           );
                     }
                  },
                  0
               );
         }
      }
   }
}
