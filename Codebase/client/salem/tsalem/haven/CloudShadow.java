package haven;

import haven.glsl.Add;
import haven.glsl.Block;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.If;
import haven.glsl.LValue;
import haven.glsl.MiscLib;
import haven.glsl.Phong;
import haven.glsl.Pick;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Statement;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.ValBlock;
import haven.glsl.Variable;

public class CloudShadow extends GLState {
  public static final GLState.Slot<CloudShadow> slot = new GLState.Slot<>(GLState.Slot.Type.DRAW, CloudShadow.class, new GLState.Slot[] { Light.lighting });
  
  public final TexGL tex;
  
  public DirLight light;
  
  public Coord3f vel;
  
  public float scale;
  
  public float a = 0.5F;
  
  public float w = 1.0F;
  
  public float t = 0.4F;
  
  public float s = 1.0F;
  
  public CloudShadow(TexGL tex, DirLight light, Coord3f vel, float scale) {
    this.tex = tex;
    this.light = light;
    this.vel = vel;
    this.scale = scale;
  }
  
  public static final Uniform tsky = new Uniform(Type.SAMPLER2D);
  
  public static final Uniform cdir = new Uniform(Type.VEC2);
  
  public static final Uniform cvel = new Uniform(Type.VEC2);
  
  public static final Uniform cscl = new Uniform(Type.FLOAT);
  
  public static final Uniform cthr = new Uniform(Type.VEC4);
  
  private static final ShaderMacro[] shaders = new ShaderMacro[] { new ShaderMacro() {
        public void modify(ProgramContext prog) {
          final Phong ph = (Phong)prog.getmod(Phong.class);
          if (ph == null || !ph.pfrag)
            return; 
          prog.fctx.uniform.getClass();
          final ValBlock.Value shval = new ValBlock.Value(prog.fctx.uniform, Type.FLOAT) {
              public Expression root() {
                Add add = Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)Cons.add(new Expression[] { (Expression)Cons.pick((LValue)MiscLib.fragmapv.ref(), "xy"), (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick((LValue)MiscLib.fragmapv.ref(), "z"), (Expression)CloudShadow.cdir.ref() }) }), (Expression)CloudShadow.cscl.ref() }), (Expression)Cons.mul(new Expression[] { (Expression)CloudShadow.cvel.ref(), (Expression)MiscLib.globtime.ref() }) });
                Pick pick = Cons.pick(Cons.texture2D((Expression)CloudShadow.tsky.ref(), (Expression)add), "r");
                Variable.Global.Ref ref = CloudShadow.cthr.ref();
                return (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { Cons.smoothstep((Expression)Cons.pick((Expression)ref, "x"), (Expression)Cons.pick((Expression)ref, "y"), (Expression)pick), (Expression)Cons.pick((Expression)ref, "w") }), (Expression)Cons.pick((Expression)ref, "z") });
              }
              
              protected void cons2(Block blk) {
                this.var = (Variable)new Variable.Global(Type.FLOAT);
                blk.add((Expression)Cons.ass(this.var, this.init));
              }
            };
          shval.force();
          ph.dolight.mod(new Runnable() {
                public void run() {
                  ph.dolight.dcalc.add((Statement)new If((Expression)Cons.eq((Expression)MapView.amblight.ref(), ph.dolight.i), Cons.stmt((Expression)Cons.amul((LValue)ph.dolight.dl.var.ref(), shval.ref()))), ph.dolight.dcurs);
                }
              }0);
        }
      } };
  
  private GLState.TexUnit sampler;
  
  public ShaderMacro[] shaders() {
    return shaders;
  }
  
  public boolean reqshaders() {
    return true;
  }
  
  public void reapply(GOut g) {
    int u = g.st.prog.cuniform(tsky);
    if (u >= 0) {
      g.gl.glUniform1i(u, this.sampler.id);
      float zf = 1.0F / (this.light.dir[2] + 1.1F);
      float xd = -this.light.dir[0] * zf, yd = -this.light.dir[1] * zf;
      g.gl.glUniform2f(g.st.prog.uniform(cdir), xd, yd);
      g.gl.glUniform2f(g.st.prog.uniform(cvel), this.vel.x, this.vel.y);
      g.gl.glUniform1f(g.st.prog.uniform(cscl), this.scale);
      float lthr = this.a * (1.0F - this.w);
      g.gl.glUniform4f(g.st.prog.uniform(cthr), lthr, lthr + this.w, this.t, this.s - this.t);
    } 
  }
  
  public void apply(GOut g) {
    this.sampler = TexGL.lbind(g, this.tex);
    reapply(g);
  }
  
  public void unapply(GOut g) {
    this.sampler.act();
    g.gl.glBindTexture(3553, 0);
    this.sampler.free();
    this.sampler = null;
  }
  
  public void prep(GLState.Buffer buf) {
    buf.put(slot, this);
  }
}
