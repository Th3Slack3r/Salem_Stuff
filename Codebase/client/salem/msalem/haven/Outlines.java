package haven;

import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.For;
import haven.glsl.Function;
import haven.glsl.If;
import haven.glsl.LValue;
import haven.glsl.Macro1;
import haven.glsl.MiscLib;
import haven.glsl.ProgramContext;
import haven.glsl.Return;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Type;
import haven.glsl.Uniform;
import java.awt.Color;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class Outlines implements Rendered {
   private boolean symmetric;
   private static final Uniform snrm = new Uniform(Type.SAMPLER2D);
   private static final Uniform sdep = new Uniform(Type.SAMPLER2D);
   private static final Uniform msnrm = new Uniform(Type.SAMPLER2DMS);
   private static final Uniform msdep = new Uniform(Type.SAMPLER2DMS);
   private static final ShaderMacro[][] shaders = new ShaderMacro[4][];

   @Override
   public void draw(GOut g) {
   }

   private static ShaderMacro shader(final boolean symmetric, final boolean ms) {
      return new ShaderMacro() {
         Color color = Color.BLACK;
         Coord[] points = new Coord[]{new Coord(-1, 0), new Coord(1, 0), new Coord(0, -1), new Coord(0, 1)};
         Function ofac = new Function.Def(Type.FLOAT) {
            {
               Expression sample = this.param(Function.PDir.IN, Type.INT).ref();
               Expression tc = Tex2D.texcoord.ref();
               LValue ret = this.code.local(Type.FLOAT, Cons.l(0.0)).ref();
               Expression lnrm = this.code
                  .local(Type.VEC3, Cons.mul(Cons.sub(Cons.pick(sample(true, tc, sample, Coord.z), "rgb"), Cons.l(0.5)), Cons.l(2.0)))
                  .ref();
               Expression ldep = this.code.local(Type.FLOAT, Cons.pick(sample(false, tc, sample, Coord.z), "z")).ref();
               LValue dh = this.code.local(Type.FLOAT, Cons.l(2.0E-4)).ref();
               LValue dl = this.code.local(Type.FLOAT, Cons.l(-2.0E-4)).ref();

               for (int i = 0; i < points.length; i++) {
                  Expression cdep = Cons.pick(sample(false, tc, sample, points[i]), "z");
                  cdep = Cons.sub(ldep, cdep);
                  cdep = this.code.local(Type.FLOAT, cdep).ref();
                  this.code.add(Cons.stmt(Cons.ass(dh, Cons.max(dh, cdep))));
                  this.code.add(Cons.stmt(Cons.ass(dl, Cons.min(dl, cdep))));
               }

               if (symmetric) {
                  this.code.add(Cons.aadd(ret, Cons.smoothstep(Cons.l(5.0), Cons.l(6.0), Cons.max(Cons.div(dh, Cons.neg(dl)), Cons.div(dl, Cons.neg(dh))))));
               } else {
                  this.code.add(Cons.aadd(ret, Cons.smoothstep(Cons.l(5.0), Cons.l(6.0), Cons.div(dh, Cons.neg(dl)))));
               }

               for (int i = 0; i < points.length; i++) {
                  Expression cnrm = Cons.mul(Cons.sub(Cons.pick(sample(true, tc, sample, points[i]), "rgb"), Cons.l(0.5)), Cons.l(2.0));
                  if (symmetric) {
                     this.code.add(Cons.aadd(ret, Cons.sub(Cons.l(1.0), Cons.abs(Cons.dot(lnrm, cnrm)))));
                  } else {
                     cnrm = this.code.local(Type.VEC3, cnrm).ref();
                     this.code
                        .add(
                           new If(
                              Cons.gt(Cons.pick(Cons.cross(lnrm, cnrm), "z"), Cons.l(0.0)),
                              Cons.stmt(Cons.aadd(ret, Cons.sub(Cons.l(1.0), Cons.abs(Cons.dot(lnrm, cnrm)))))
                           )
                        );
                  }
               }

               this.code.add(new Return(Cons.smoothstep(Cons.l(0.4), Cons.l(0.6), Cons.min(ret, Cons.l(1.0)))));
            }
         };
         Function msfac = new Function.Def(Type.FLOAT) {
            {
               LValue ret = this.code.local(Type.FLOAT, Cons.l(0.0)).ref();
               LValue i = this.code.local(Type.INT, null).ref();
               this.code.add(new For(Cons.ass(i, Cons.l(0)), Cons.lt(i, FBConfig.numsamples.ref()), Cons.linc(i), Cons.stmt(Cons.aadd(ret, ofac.call(i)))));
               this.code.add(new Return(Cons.div(ret, FBConfig.numsamples.ref())));
            }
         };

         Expression sample(boolean nrm, Expression c, Expression s, Coord o) {
            if (ms) {
               Expression ctc = Cons.ivec2(Cons.floor(Cons.mul(c, MiscLib.screensize.ref())));
               if (!o.equals(Coord.z)) {
                  ctc = Cons.add(ctc, Cons.ivec2(o));
               }

               return Cons.texelFetch((nrm ? Outlines.msnrm : Outlines.msdep).ref(), ctc, s);
            } else {
               Expression ctc = c;
               if (!o.equals(Coord.z)) {
                  ctc = Cons.add(c, Cons.mul(Cons.vec2(o), MiscLib.pixelpitch.ref()));
               }

               return Cons.texture2D((nrm ? Outlines.snrm : Outlines.sdep).ref(), ctc);
            }
         }

         @Override
         public void modify(ProgramContext prog) {
            prog.fctx.fragcol.mod(new Macro1<Expression>() {
               public Expression expand(Expression in) {
                  Expression of = !ms ? ofac.call(Cons.l(-1)) : msfac.call();
                  return Cons.vec4(Cons.col3(color), Cons.mix(Cons.l(0.0), Cons.l(1.0), of));
               }
            }, 0);
         }
      };
   }

   public Outlines(boolean symmetric) {
      this.symmetric = symmetric;
   }

   @Override
   public boolean setup(RenderList rl) {
      final PView.ConfContext ctx = (PView.ConfContext)rl.state().get(PView.ctx);
      final RenderedNormals nrm = ctx.data(RenderedNormals.id);
      final boolean ms = ctx.cfg.ms > 1;
      ctx.cfg.tdepth = true;
      ctx.cfg.add(nrm);
      rl.prepc(Rendered.postfx);
      rl.add(new Rendered.ScreenQuad(), new States.AdHoc(shaders[(this.symmetric ? 2 : 0) | (ms ? 1 : 0)]) {
         private GLState.TexUnit tnrm;
         private GLState.TexUnit tdep;

         @Override
         public void reapply(GOut g) {
            GL2 gl = g.gl;
            gl.glUniform1i(g.st.prog.uniform(!ms ? Outlines.snrm : Outlines.msnrm), this.tnrm.id);
            gl.glUniform1i(g.st.prog.uniform(!ms ? Outlines.sdep : Outlines.msdep), this.tdep.id);
         }

         @Override
         public void apply(GOut g) {
            GL gl = g.gl;
            if (!ms) {
               this.tnrm = g.st.texalloc(g, ((GLFrameBuffer.Attach2D)nrm.tex).tex);
               this.tdep = g.st.texalloc(g, ((GLFrameBuffer.Attach2D)ctx.cur.depth).tex);
            } else {
               this.tnrm = g.st.texalloc(g, ((GLFrameBuffer.AttachMS)nrm.tex).tex);
               this.tdep = g.st.texalloc(g, ((GLFrameBuffer.AttachMS)ctx.cur.depth).tex);
            }

            this.reapply(g);
         }

         @Override
         public void unapply(GOut g) {
            GL gl = g.gl;
            this.tnrm.ufree();
            this.tnrm = null;
            this.tdep.ufree();
            this.tdep = null;
         }
      });
      return false;
   }

   static {
      shaders[0] = new ShaderMacro[]{shader(false, false)};
      shaders[1] = new ShaderMacro[]{shader(false, true)};
      shaders[2] = new ShaderMacro[]{shader(true, false)};
      shaders[3] = new ShaderMacro[]{shader(true, true)};
   }
}
