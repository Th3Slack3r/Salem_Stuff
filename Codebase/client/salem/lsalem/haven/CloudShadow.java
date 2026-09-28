package haven;

import haven.glsl.Block;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.If;
import haven.glsl.LValue;
import haven.glsl.MiscLib;
import haven.glsl.Phong;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.ValBlock;
import haven.glsl.Variable;

public class CloudShadow extends GLState {
   public static final GLState.Slot<CloudShadow> slot = new GLState.Slot<>(GLState.Slot.Type.DRAW, CloudShadow.class, Light.lighting);
   public final TexGL tex;
   public DirLight light;
   public Coord3f vel;
   public float scale;
   public float a = 0.5F;
   public float w = 1.0F;
   public float t = 0.4F;
   public float s = 1.0F;
   public static final Uniform tsky = new Uniform(Type.SAMPLER2D);
   public static final Uniform cdir = new Uniform(Type.VEC2);
   public static final Uniform cvel = new Uniform(Type.VEC2);
   public static final Uniform cscl = new Uniform(Type.FLOAT);
   public static final Uniform cthr = new Uniform(Type.VEC4);
   private static final ShaderMacro[] shaders = new ShaderMacro[]{
      new ShaderMacro() {
         @Override
         public void modify(ProgramContext prog) {
            final Phong ph = prog.getmod(Phong.class);
            if (ph != null && ph.pfrag) {
               ValBlock var10003 = prog.fctx.uniform;
               prog.fctx.uniform.getClass();
               final ValBlock.Value shval = new ValBlock.Value(var10003, Type.FLOAT) {
                  {
                     x0.getClass();
                  }

                  @Override
                  public Expression root() {
                     Expression tc = Cons.add(
                        Cons.mul(
                           Cons.add(
                              Cons.pick((LValue)MiscLib.fragmapv.ref(), "xy"), Cons.mul(Cons.pick((LValue)MiscLib.fragmapv.ref(), "z"), CloudShadow.cdir.ref())
                           ),
                           CloudShadow.cscl.ref()
                        ),
                        Cons.mul(CloudShadow.cvel.ref(), MiscLib.globtime.ref())
                     );
                     Expression cl = Cons.pick(Cons.texture2D(CloudShadow.tsky.ref(), tc), "r");
                     Expression th = CloudShadow.cthr.ref();
                     return Cons.add(Cons.mul(Cons.smoothstep(Cons.pick(th, "x"), Cons.pick(th, "y"), cl), Cons.pick(th, "w")), Cons.pick(th, "z"));
                  }

                  @Override
                  protected void cons2(Block blk) {
                     this.var = new Variable.Global(Type.FLOAT);
                     blk.add(Cons.ass(this.var, this.init));
                  }
               };
               shval.force();
               ph.dolight
                  .mod(
                     new Runnable() {
                        @Override
                        public void run() {
                           ph.dolight
                              .dcalc
                              .add(
                                 new If(Cons.eq(MapView.amblight.ref(), ph.dolight.i), Cons.stmt(Cons.amul(ph.dolight.dl.var.ref(), shval.ref()))),
                                 ph.dolight.dcurs
                              );
                        }
                     },
                     0
                  );
            }
         }
      }
   };
   private GLState.TexUnit sampler;

   public CloudShadow(TexGL tex, DirLight light, Coord3f vel, float scale) {
      this.tex = tex;
      this.light = light;
      this.vel = vel;
      this.scale = scale;
   }

   @Override
   public ShaderMacro[] shaders() {
      return shaders;
   }

   @Override
   public boolean reqshaders() {
      return true;
   }

   @Override
   public void reapply(GOut g) {
      int u = g.st.prog.cuniform(tsky);
      if (u >= 0) {
         g.gl.glUniform1i(u, this.sampler.id);
         float zf = 1.0F / (this.light.dir[2] + 1.1F);
         float xd = -this.light.dir[0] * zf;
         float yd = -this.light.dir[1] * zf;
         g.gl.glUniform2f(g.st.prog.uniform(cdir), xd, yd);
         g.gl.glUniform2f(g.st.prog.uniform(cvel), this.vel.x, this.vel.y);
         g.gl.glUniform1f(g.st.prog.uniform(cscl), this.scale);
         float lthr = this.a * (1.0F - this.w);
         g.gl.glUniform4f(g.st.prog.uniform(cthr), lthr, lthr + this.w, this.t, this.s - this.t);
      }
   }

   @Override
   public void apply(GOut g) {
      this.sampler = TexGL.lbind(g, this.tex);
      this.reapply(g);
   }

   @Override
   public void unapply(GOut g) {
      this.sampler.act();
      g.gl.glBindTexture(3553, 0);
      this.sampler.free();
      this.sampler = null;
   }

   @Override
   public void prep(GLState.Buffer buf) {
      buf.put(slot, this);
   }
}
