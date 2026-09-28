package haven;

import java.awt.event.KeyEvent;
import java.util.Objects;

public class FlatnessTool extends Window implements MapView.Grabber {
   static final String title = "Area selection";
   static final String defaulttext = "Select area";
   public static float minheight = Float.MAX_VALUE;
   public static float maxheight = -minheight;
   private final Label text;
   private final Label area;
   private final MapView mv;
   Coord sc;
   Coord c1;
   Coord c2;
   MCache.Overlay ol;
   final MCache map;
   private Button btnToggle;
   private boolean grabbed = false;
   private MapView.GrabXL grab;
   private static FlatnessTool instance;

   public FlatnessTool(MapView mv, Coord c, Widget parent) {
      super(c, new Coord(150, 50), parent, "Area selection");
      this.map = this.ui.sess.glob.map;
      this.text = new Label(Coord.z, this, "Select area");
      this.area = new Label(new Coord(0, this.text.sz.y), this, "");
      this.mv = mv;
      Objects.requireNonNull(mv);
      this.grab = new MapView.GrabXL(mv, this) {
         {
            Objects.requireNonNull(x0);
         }

         @Override
         public boolean mmousewheel(Coord cc, int amount) {
            return false;
         }
      };
      this.mv.enol(18);
      this.btnToggle = new Button(new Coord(0, this.area.c.add(this.area.sz).y), 75, this, "Grab");
      this.pack();
   }

   public static FlatnessTool instance(GameUI gui) {
      if (instance == null && gui != null && gui.map != null) {
         instance = new FlatnessTool(gui.map, new Coord(100, 100), gui);
      }

      return instance;
   }

   public static void close() {
      if (instance != null) {
         instance.ui.destroy(instance);
         instance = null;
      }
   }

   public void toggle() {
      this.grabbed = !this.grabbed;
      if (this.grabbed) {
         this.mv.grab(this.grab);
         this.btnToggle.change("Release");
      } else {
         this.mv.release(this.grab);
         this.btnToggle.change("Grab");
      }
   }

   private void checkflatness(Coord c1, Coord c2) {
      if (c1 != null && c2 != null) {
         this.c1 = c1;
         this.c2 = c2;
         c2 = c2.add(1, 1);
         minheight = Float.MAX_VALUE;
         maxheight = -minheight;
         float h = 0.0F;
         Coord sz = c2.sub(c1).abs();
         long n = sz.add(1, 1).mul();
         double mean = 0.0;
         Coord c = new Coord();

         try {
            for (c.x = c1.x; c.x <= c2.x; c.x++) {
               for (c.y = c1.y; c.y <= c2.y; c.y++) {
                  h = this.map.getcz(c.mul(MCache.tilesz));
                  if (h < minheight) {
                     minheight = h;
                  }

                  if (h > maxheight) {
                     maxheight = h;
                  }

                  mean += h;
               }
            }

            mean /= n;
         } catch (MCache.LoadingMap var11) {
            return;
         }

         String text = "";
         if (minheight == maxheight) {
            text = text + "Area is flat.";
         } else {
            text = text + "Area is not flat.";
         }

         text = text + String.format(" Lowest: [%.0f], Heighest: [%.0f], Mean: [%.2f].", minheight, maxheight, mean);
         this.settext(text);
         this.setarea(sz);
         this.pack();
      }
   }

   private void setarea(Coord sz) {
      this.area.settext(String.format("Size: (%d×%d) = %dm²", sz.x, sz.y, sz.mul()));
   }

   @Override
   public void destroy() {
      if (this.grabbed) {
         this.toggle();
      }

      if (this.ol != null) {
         this.ol.destroy();
      }

      this.mv.disol(18);
      this.mv.release(this.grab);
      instance = null;
      super.destroy();
   }

   @Override
   public boolean mmousedown(Coord mc, int button) {
      Coord c = mc.div(MCache.tilesz);
      if (this.ol != null) {
         this.ol.destroy();
      }

      this.ol = this.map.new Overlay(c, c, 262144);
      this.sc = c;
      this.grab.mv = true;
      this.ui.grabmouse(this.mv);
      this.checkflatness(c, c);
      return true;
   }

   @Override
   public boolean mmouseup(Coord mc, int button) {
      this.grab.mv = false;
      this.ui.grabmouse(null);
      return true;
   }

   @Override
   public void mmousemove(Coord mc) {
      if (this.grab.mv) {
         Coord c = mc.div(MCache.tilesz);
         Coord c1 = new Coord(0, 0);
         Coord c2 = new Coord(0, 0);
         if (c.x < this.sc.x) {
            c1.x = c.x;
            c2.x = this.sc.x;
         } else {
            c1.x = this.sc.x;
            c2.x = c.x;
         }

         if (c.y < this.sc.y) {
            c1.y = c.y;
            c2.y = this.sc.y;
         } else {
            c1.y = this.sc.y;
            c2.y = c.y;
         }

         this.ol.update(c1, c2);
         this.checkflatness(c1, c2);
      }
   }

   @Override
   public boolean type(char key, KeyEvent ev) {
      if (key != '\n' && key != 27) {
         return super.type(key, ev);
      } else {
         close();
         return true;
      }
   }

   @Override
   public void wdgmsg(Widget wdg, String msg, Object... args) {
      if (wdg == this.cbtn) {
         this.ui.destroy(this);
      } else if (wdg == this.btnToggle) {
         this.toggle();
      } else {
         super.wdgmsg(wdg, msg, args);
      }
   }

   private final void settext(String text) {
      this.text.settext(text);
   }

   public static void recalcheight() {
      if (instance != null) {
         instance.checkflatness(instance.c1, instance.c2);
      }
   }

   @Override
   public boolean mmousewheel(Coord mc, int amount) {
      return false;
   }
}
