package haven;

import javax.media.opengl.GL2;

public class GLVertexArray extends GLObject {
  public final int id;
  
  public GLVertexArray(GL2 gl) {
    super(gl);
    int[] buf = new int[1];
    gl.glGenVertexArrays(1, buf, 0);
    this.id = buf[0];
  }
  
  protected void delete() {
    int[] buf = { this.id };
    this.gl.glDeleteVertexArrays(1, buf, 0);
  }
}
