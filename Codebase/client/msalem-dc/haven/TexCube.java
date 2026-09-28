package haven;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class TexCube {
   protected TexGL.TexOb t = null;
   private Object idmon = new Object();
   protected int tdim;
   protected final BufferedImage back;
   private static final int[][] order = new int[][]{{3, 1}, {1, 1}, {2, 0}, {2, 2}, {2, 1}, {0, 1}};

   public TexCube(BufferedImage img) {
      Coord sz = Utils.imgsz(img);
      this.tdim = sz.x / 4;
      if (this.tdim * 4 != sz.x) {
         throw new RuntimeException("Cube-mapped texture has width undivisible by 4");
      } else if (this.tdim * 3 != sz.y) {
         throw new RuntimeException("Cube-mapped texture is not 4:3");
      } else {
         this.back = img;
      }
   }

   protected void fill(GOut g) {
      GL gl = g.gl;
      Coord dim = new Coord(this.tdim, this.tdim);

      for (int i = 0; i < order.length; i++) {
         ByteBuffer data = ByteBuffer.wrap(TexI.convert(this.back, dim, new Coord(order[i][0] * this.tdim, order[i][1] * this.tdim), dim));
         gl.glTexImage2D(34069 + i, 0, 6408, this.tdim, this.tdim, 0, 6408, 5121, data);
      }
   }

   private void create(GOut g) {
      GL2 gl = g.gl;
      this.t = new TexGL.TexOb(gl);
      gl.glBindTexture(34067, this.t.id);
      gl.glTexParameteri(34067, 10241, 9728);
      gl.glTexParameteri(34067, 10240, 9729);
      this.fill(g);
      GOut.checkerr(gl);
   }

   public int glid(GOut g) {
      GL gl = g.gl;
      synchronized (this.idmon) {
         if (this.t != null && this.t.gl != gl) {
            this.dispose();
         }

         if (this.t == null) {
            this.create(g);
         }

         return this.t.id;
      }
   }

   public void dispose() {
      synchronized (this.idmon) {
         if (this.t != null) {
            this.t.dispose();
            this.t = null;
         }
      }
   }
}
