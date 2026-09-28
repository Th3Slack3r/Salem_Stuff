package haven.glsl;

public class Varying extends Variable.Global {
  public enum Interpol {
    NORMAL, FLAT, NOPERSPECTIVE, CENTROID;
  }
  
  public Interpol ipol = Interpol.NORMAL;
  
  protected Interpol ipol(Context ctx) {
    return this.ipol;
  }
  
  public Varying(Type type, Symbol name) {
    super(type, name);
  }
  
  private class Def extends Variable.Global.Definition {
    private Def() {}
    
    public void output(Output out) {
      switch (Varying.this.ipol(out.ctx)) {
        case FLAT:
          out.write("flat ");
          break;
        case NOPERSPECTIVE:
          out.write("noperspective ");
          break;
        case CENTROID:
          out.write("centroid ");
          break;
      } 
      out.write("varying ");
      super.output(out);
    }
  }
  
  public void use(Context ctx) {
    if (!defined(ctx))
      ctx.vardefs.add(new Def()); 
  }
}
