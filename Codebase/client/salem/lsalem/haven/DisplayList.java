package haven;

import javax.media.opengl.GL2;

public class DisplayList extends GLObject {
   public final int id;

   public DisplayList(GL2 gl) {
      super(gl);
      this.id = gl.glGenLists(1);
   }

   @Override
   protected void delete() {
      this.gl.glDeleteLists(this.id, 1);
   }
}
