package haven.glsl;

public class ShaderContext extends Context {
  public final ProgramContext prog;
  
  public ShaderContext(ProgramContext prog) {
    this.prog = prog;
  }
}
