package haven;

import java.awt.image.BufferedImage;

public class HSlider extends Widget {
   static final Tex sflarp = Resource.loadtex("gfx/hud/sflarp");
   static final Tex schain;
   public int val;
   public int min;
   public int max;
   private boolean drag = false;

   public HSlider(Coord c, int w, Widget parent, int min, int max, int val) {
      super(c, new Coord(w, sflarp.sz().y), parent);
      this.val = val;
      this.min = min;
      this.max = max;
   }

   @Override
   public void draw(GOut g) {
      int cy = (sflarp.sz().y - schain.sz().y) / 2;

      for (int x = 0; x < this.sz.x; x += schain.sz().x) {
         g.image(schain, new Coord(x, cy));
      }

      int fx = (this.sz.x - sflarp.sz().x) * this.val / (this.max - this.min);
      g.image(sflarp, new Coord(fx, 0));
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (button != 1) {
         return false;
      } else {
         this.drag = true;
         this.ui.grabmouse(this);
         this.mousemove(c);
         return true;
      }
   }

   @Override
   public void mousemove(Coord c) {
      if (this.drag) {
         double a = (double)(c.x - sflarp.sz().x / 2) / (this.sz.x - sflarp.sz().x);
         if (a < 0.0) {
            a = 0.0;
         }

         if (a > 1.0) {
            a = 1.0;
         }

         this.val = (int)Math.round(a * (this.max - this.min)) + this.min;
         this.changed();
      }
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      if (button != 1) {
         return false;
      } else if (!this.drag) {
         return false;
      } else {
         this.drag = false;
         this.ui.grabmouse(null);
         return true;
      }
   }

   public void changed() {
   }

   public void resize(int w) {
      super.resize(new Coord(w, sflarp.sz().y));
   }

   static {
      BufferedImage vc = Resource.loadimg("gfx/hud/schain");
      BufferedImage hc = TexI.mkbuf(new Coord(vc.getHeight(), vc.getWidth()));

      for (int y = 0; y < vc.getHeight(); y++) {
         for (int x = 0; x < vc.getWidth(); x++) {
            hc.setRGB(y, x, vc.getRGB(x, y));
         }
      }

      schain = new TexI(hc);
   }
}
