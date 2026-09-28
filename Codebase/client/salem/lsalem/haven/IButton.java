package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class IButton extends SSWidget {
   BufferedImage up;
   BufferedImage down;
   BufferedImage hover;
   boolean a = false;
   boolean h = false;
   public boolean recthit = false;

   public IButton(Coord c, Widget parent, BufferedImage up, BufferedImage down, BufferedImage hover) {
      super(c, Utils.imgsz(up), parent);
      this.up = up;
      this.down = down;
      this.hover = hover;
      this.render();
   }

   public IButton(Coord c, Widget parent, BufferedImage up, BufferedImage down) {
      this(c, parent, up, down, up);
   }

   public void render() {
      this.clear();
      Graphics g = this.graphics();
      if (this.a) {
         g.drawImage(this.down, 0, 0, null);
      } else if (this.h) {
         g.drawImage(this.hover, 0, 0, null);
      } else {
         g.drawImage(this.up, 0, 0, null);
      }

      this.update();
   }

   public boolean checkhit(Coord c) {
      if (!c.isect(Coord.z, this.sz)) {
         return false;
      } else if (this.recthit) {
         return true;
      } else {
         return this.up.getRaster().getNumBands() < 4 ? true : this.up.getRaster().getSample(c.x, c.y, 3) >= 128;
      }
   }

   public void click() {
      this.wdgmsg("activate", new Object[0]);
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (button != 1) {
         return false;
      } else if (!this.checkhit(c)) {
         return false;
      } else {
         this.a = true;
         this.ui.grabmouse(this);
         this.render();
         return true;
      }
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      if (this.a && button == 1) {
         this.a = false;
         this.ui.grabmouse(null);
         if (this.checkhit(c)) {
            this.click();
         }

         this.render();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void mousemove(Coord c) {
      boolean h = this.checkhit(c);
      if (h != this.h) {
         this.h = h;
         this.render();
      }
   }

   @Widget.RName("ibtn")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new IButton(c, parent, Resource.loadimg((String)args[0]), Resource.loadimg((String)args[1]));
      }
   }
}
