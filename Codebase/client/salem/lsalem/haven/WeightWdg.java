package haven;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class WeightWdg extends Window {
   static final Tex bg = Resource.loadtex("gfx/hud/bgtex");
   private Tex label;

   public WeightWdg(Coord c, Widget parent) {
      super(c, Coord.z, parent, "weightwdg");
      this.cap = null;
      this.sz = new Coord(100, 30);
   }

   public void update(int weight) {
      if (this.label != null) {
         this.label.dispose();
      }

      int cap = 25000;
      Glob.CAttr ca = this.ui.sess.glob.cattr.get("carry");
      if (ca != null) {
         cap = ca.comp;
      }

      Color color = weight > cap ? Color.RED : Color.WHITE;
      this.label = Text.render(String.format("Weight: %.2f/%.2f kg", weight / 1000.0, cap / 1000.0), color).tex();
      this.sz = this.label.sz().add(Window.swbox.bisz()).add(4, 0);
   }

   @Override
   public void tick(double dt) {
      if (Config.weight_wdg != this.visible) {
         this.show(Config.weight_wdg);
      }
   }

   @Override
   public void draw(GOut g) {
      Coord s = bg.sz();

      for (int y = 0; y * s.y < this.sz.y; y++) {
         for (int x = 0; x * s.x < this.sz.x; x++) {
            g.image(bg, new Coord(x * s.x, y * s.y));
         }
      }

      if (this.label != null) {
         g.aimage(this.label, this.sz.div(2), 0.5, 0.5);
      }

      Window.swbox.draw(g, Coord.z, this.sz);
   }

   @Override
   public boolean type(char key, KeyEvent ev) {
      return false;
   }
}
