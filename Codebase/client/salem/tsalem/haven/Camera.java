package haven;

public class Camera extends Transform {
  private Matrix4f bk;
  
  public Camera(Matrix4f xf) {
    super(xf);
  }
  
  public void apply(GOut g) {
    this.bk = g.st.cam;
    g.st.cam = fin(g.st.cam);
  }
  
  public void unapply(GOut g) {
    g.st.cam = this.bk;
  }
  
  public void prep(GLState.Buffer b) {
    b.put(PView.cam, this);
  }
}
