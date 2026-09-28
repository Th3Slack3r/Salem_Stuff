package haven;

import java.awt.image.BufferedImage;

public abstract class SIWidget extends Widget {
   private Tex surf = null;

   public SIWidget(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
   }

   protected abstract void draw(BufferedImage var1);

   public BufferedImage draw() {
      BufferedImage buf = TexI.mkbuf(this.sz);
      this.draw(buf);
      return buf;
   }

   @Override
   public void draw(GOut g) {
      if (this.surf == null) {
         this.surf = new TexI(this.draw());
      }

      g.image(this.surf, Coord.z);
   }

   public void redraw() {
      if (this.surf != null) {
         this.surf.dispose();
      }

      this.surf = null;
   }
}
