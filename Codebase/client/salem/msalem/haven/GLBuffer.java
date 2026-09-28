package haven;

import javax.media.opengl.GL2;

public class GLBuffer extends GLObject {
   public final int id;

   public GLBuffer(GL2 gl) {
      super(gl);
      int[] buf = new int[1];
      gl.glGenBuffers(1, buf, 0);
      this.id = buf[0];
   }

   @Override
   protected void delete() {
      int[] buf = new int[]{this.id};
      this.gl.glDeleteBuffers(1, buf, 0);
   }
}
