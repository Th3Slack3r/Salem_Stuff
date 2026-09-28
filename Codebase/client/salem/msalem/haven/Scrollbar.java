package haven;

public class Scrollbar extends Widget {
   static final Tex schain = Resource.loadtex("gfx/hud/schain");
   static final Tex sflarp = Resource.loadtex("gfx/hud/sflarp");
   public int val;
   public int min;
   public int max;
   private boolean drag = false;

   public Scrollbar(Coord c, int h, Widget parent, int min, int max) {
      super(c.add(-sflarp.sz().x, 0), new Coord(sflarp.sz().x, h), parent);
      this.min = min;
      this.max = max;
      this.val = min;
   }

   public boolean vis() {
      return this.max > this.min;
   }

   @Override
   public void draw(GOut g) {
      if (this.vis()) {
         int cx = sflarp.sz().x / 2 - schain.sz().x / 2;

         for (int y = 0; y < this.sz.y; y += schain.sz().y - 1) {
            g.image(schain, new Coord(cx, y));
         }

         double a = (double)this.val / (this.max - this.min);
         int fy = (int)((this.sz.y - sflarp.sz().y) * a);
         g.image(sflarp, new Coord(0, fy));
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (button != 1) {
         return false;
      } else if (!this.vis()) {
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
         double a = (double)(c.y - sflarp.sz().y / 2) / (this.sz.y - sflarp.sz().y);
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

   public void ch(int a) {
      int val = this.val + a;
      if (val > this.max) {
         val = this.max;
      }

      if (val < this.min) {
         val = this.min;
      }

      if (this.val != val) {
         this.val = val;
         this.changed();
      }
   }

   public void resize(int h) {
      super.resize(new Coord(sflarp.sz().x, h));
   }

   public void move(Coord c) {
      this.c = c.add(-sflarp.sz().x, 0);
   }
}
