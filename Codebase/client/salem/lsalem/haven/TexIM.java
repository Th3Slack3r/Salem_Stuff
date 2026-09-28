package haven;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class TexIM extends TexI {
   WritableRaster buf;
   Graphics2D cg = null;
   Throwable cgc;

   public TexIM(Coord sz) {
      super(sz);
      this.clear();
   }

   public Graphics2D graphics() {
      if (this.cg != null) {
         throw new RuntimeException("Multiple TexIM Graphics created (" + Thread.currentThread().getName() + ")", this.cgc);
      } else {
         this.cgc = new Throwable("Current Graphics created (on " + Thread.currentThread().getName() + ")");
         return this.cg = this.back.createGraphics();
      }
   }

   public void update() {
      this.cg.dispose();
      this.cg = null;
      this.dispose();
   }

   public void clear() {
      this.buf = Raster.createInterleavedRaster(0, this.tdim.x, this.tdim.y, 4, null);
      this.back = new BufferedImage(glcm, this.buf, false, null);
   }
}
