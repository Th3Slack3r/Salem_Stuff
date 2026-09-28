package haven.glsl;

public class Placeholder extends Statement {
  public final String comment;
  
  public Placeholder(String comment) {
    this.comment = comment;
  }
  
  public Placeholder() {
    this(null);
  }
  
  public Placeholder process(Context ctx) {
    return this;
  }
  
  public void output(Output out) {
    if (this.comment != null)
      out.write("/* " + this.comment + " */"); 
  }
}
