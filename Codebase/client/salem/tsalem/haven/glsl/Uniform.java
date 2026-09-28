package haven.glsl;

import haven.GLState;
import haven.GOut;

public class Uniform extends Variable.Global {
  public Uniform(Type type, Symbol name) {
    super(type, name);
  }
  
  public Uniform(Type type, String infix) {
    this(type, new Symbol.Shared("s_" + infix));
  }
  
  public Uniform(Type type) {
    this(type, new Symbol.Shared());
  }
  
  private class Def extends Variable.Global.Definition {
    private Def() {}
    
    public void output(Output out) {
      if (out.ctx instanceof ShaderContext)
        ((ShaderContext)out.ctx).prog.uniforms.add(Uniform.this); 
      out.write("uniform ");
      super.output(out);
    }
  }
  
  public void use(Context ctx) {
    if (!defined(ctx))
      ctx.vardefs.add(new Def()); 
    if (this.type == Type.SAMPLER2DMS)
      ctx.exts.add("GL_ARB_texture_multisample"); 
  }
  
  public static abstract class AutoApply extends Uniform {
    public final GLState.Slot[] deps;
    
    public AutoApply(Type type, Symbol name, GLState.Slot... deps) {
      super(type, name);
      this.deps = deps;
    }
    
    public AutoApply(Type type, String infix, GLState.Slot... deps) {
      super(type, infix);
      this.deps = deps;
    }
    
    public AutoApply(Type type, GLState.Slot... deps) {
      super(type);
      this.deps = deps;
    }
    
    public abstract void apply(GOut param1GOut, int param1Int);
  }
}
