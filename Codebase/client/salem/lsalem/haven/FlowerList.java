package haven;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;

public class FlowerList extends Scrollport {
   private final IBox box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");

   public FlowerList(Coord c, Widget parent) {
      super(c, new Coord(200, 250), parent);
      int i = 0;

      for (Entry<String, Boolean> entry : Config.AUTOCHOOSE.entrySet()) {
         new FlowerList.Item(new Coord(0, 25 * i++), entry.getKey(), this.cont);
      }

      this.update();
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (msg.equals("changed")) {
         String name = (String)args[0];
         boolean val = (Boolean)args[1];
         synchronized (Config.AUTOCHOOSE) {
            Config.AUTOCHOOSE.put(name, val);
         }

         Config.saveAutochoose();
      } else if (msg.equals("delete")) {
         String name = (String)args[0];
         synchronized (Config.AUTOCHOOSE) {
            Config.AUTOCHOOSE.remove(name);
         }

         Config.saveAutochoose();
         this.ui.destroy(sender);
         this.update();
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   public void add(String name) {
      if (name != null && !name.isEmpty() && !Config.AUTOCHOOSE.containsKey(name)) {
         synchronized (Config.AUTOCHOOSE) {
            Config.AUTOCHOOSE.put(name, true);
         }

         Config.saveAutochoose();
         new FlowerList.Item(new Coord(0, 0), name, this.cont);
         this.update();
      }
   }

   private void update() {
      LinkedList<String> order = new LinkedList<>(Config.AUTOCHOOSE.keySet());
      Collections.sort(order);

      for (Widget wdg = this.cont.lchild; wdg != null; wdg = wdg.prev) {
         int i = order.indexOf(((FlowerList.Item)wdg).name);
         wdg.c.y = 25 * i;
      }

      this.cont.update();
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      this.box.draw(g, Coord.z, this.sz);
   }

   private static class Item extends Widget {
      public final String name;
      private final CheckBox cb;
      private boolean highlight = false;
      private boolean a = false;

      public Item(Coord c, String name, Widget parent) {
         super(c, new Coord(200, 25), parent);
         this.name = name;
         this.cb = new CheckBox(new Coord(3, 3), this, name);
         this.cb.a = Config.AUTOCHOOSE.get(name);
         this.cb.canactivate = true;
         new IButton(new Coord(178, 5), this, Window.cbtni[0], Window.cbtni[1], Window.cbtni[2]);
      }

      @Override
      public void draw(GOut g) {
         if (this.highlight) {
            g.chcolor(255, 255, 0, 128);
            g.poly2(Coord.z, Listbox.selr, new Coord(0, this.sz.y), Listbox.selr, this.sz, Listbox.overr, new Coord(this.sz.x, 0), Listbox.overr);
            g.chcolor();
         }

         super.draw(g);
      }

      @Override
      public void mousemove(Coord c) {
         this.highlight = c.isect(Coord.z, this.sz);
         super.mousemove(c);
      }

      @Override
      public boolean mousedown(Coord c, int button) {
         if (super.mousedown(c, button)) {
            return true;
         } else if (button != 1) {
            return false;
         } else {
            this.a = true;
            this.ui.grabmouse(this);
            return true;
         }
      }

      @Override
      public boolean mouseup(Coord c, int button) {
         if (this.a && button == 1) {
            this.a = false;
            this.ui.grabmouse(null);
            if (c.isect(new Coord(0, 0), this.sz)) {
               this.click();
            }

            return true;
         } else {
            return false;
         }
      }

      private void click() {
         this.cb.a = !this.cb.a;
         this.wdgmsg("changed", new Object[]{this.name, this.cb.a});
      }

      @Override
      public void wdgmsg(Widget sender, String msg, Object... args) {
         if (msg.equals("ch")) {
            this.wdgmsg("changed", new Object[]{this.name, args[0]});
         } else if (msg.equals("activate")) {
            this.wdgmsg("delete", new Object[]{this.name});
         } else {
            super.wdgmsg(sender, msg, args);
         }
      }
   }
}
