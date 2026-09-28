package haven;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class Equipory extends Widget {
   public static final Equipory.Box[] boxen = new Equipory.Box[]{
      new Equipory.Box(new Coord(250, 0), Resource.loadtex("gfx/hud/inv/head"), 0),
      new Equipory.Box(new Coord(50, 70), Resource.loadtex("gfx/hud/inv/face"), 0),
      new Equipory.Box(new Coord(250, 70), Resource.loadtex("gfx/hud/inv/shirt"), 0),
      new Equipory.Box(new Coord(300, 70), Resource.loadtex("gfx/hud/inv/torsoa"), 0),
      new Equipory.Box(new Coord(50, 0), Resource.loadtex("gfx/hud/inv/keys"), 0),
      new Equipory.Box(new Coord(50, 210), Resource.loadtex("gfx/hud/inv/belt"), 0),
      new Equipory.Box(new Coord(25, 140), Resource.loadtex("gfx/hud/inv/lhande"), 0),
      new Equipory.Box(new Coord(275, 140), Resource.loadtex("gfx/hud/inv/rhande"), 0),
      null,
      new Equipory.Box(new Coord(0, 0), Resource.loadtex("gfx/hud/inv/wallet"), 0),
      new Equipory.Box(new Coord(0, 210), Resource.loadtex("gfx/hud/inv/coat"), 0),
      new Equipory.Box(new Coord(300, 0), Resource.loadtex("gfx/hud/inv/cape"), 0),
      new Equipory.Box(new Coord(300, 210), Resource.loadtex("gfx/hud/inv/pants"), 0),
      new Equipory.Box(new Coord(100, 0), null, 0),
      new Equipory.Box(new Coord(0, 70), Resource.loadtex("gfx/hud/inv/back"), 0),
      new Equipory.Box(new Coord(250, 210), Resource.loadtex("gfx/hud/inv/feet"), 0),
      new Equipory.Box(new Coord(250, 0), Resource.loadtex("gfx/hud/inv/costumehead"), 1),
      new Equipory.Box(new Coord(50, 70), Resource.loadtex("gfx/hud/inv/costumeface"), 1),
      new Equipory.Box(new Coord(250, 70), Resource.loadtex("gfx/hud/inv/costumeshirt"), 1),
      new Equipory.Box(new Coord(300, 70), Resource.loadtex("gfx/hud/inv/costumetorsoa"), 1),
      new Equipory.Box(new Coord(0, 210), Resource.loadtex("gfx/hud/inv/costumecoat"), 1),
      new Equipory.Box(new Coord(300, 0), Resource.loadtex("gfx/hud/inv/costumecape"), 1),
      new Equipory.Box(new Coord(300, 210), Resource.loadtex("gfx/hud/inv/costumepants"), 1),
      new Equipory.Box(new Coord(250, 210), Resource.loadtex("gfx/hud/inv/costumefeet"), 1)
   };
   public static final Coord isz = isz();
   public final Widget[] tabs = new Widget[2];
   public final WItem[] slots = new WItem[boxen.length];
   private final Map<GItem, WItem[]> wmap = new HashMap<>();

   private static Coord isz() {
      Coord isz = new Coord();

      for (Equipory.Box box : boxen) {
         if (box != null) {
            if (box.c.x + Inventory.sqlite.sz().x > isz.x) {
               isz.x = box.c.x + Inventory.sqlite.sz().x;
            }

            if (box.c.y + Inventory.sqlite.sz().y > isz.y) {
               isz.y = box.c.y + Inventory.sqlite.sz().y;
            }
         }
      }

      return isz;
   }

   public Equipory(Coord c, Widget parent, long gobid) {
      super(c, isz, parent);
      new Avaview(Coord.z, isz, this, gobid, "equcam") {
         @Override
         public boolean mousedown(Coord c, int button) {
            return false;
         }

         @Override
         protected Color clearcolor() {
            return null;
         }
      };
      int bx = 0;
      String s1 = "Equipment";
      String s2 = "Costume";

      for (final int i = 0; i < this.tabs.length; i++) {
         this.tabs[i] = new Widget(Coord.z, this.sz, this);
         this.tabs[i].show(i == 0);
         new Equipory.Boxen(Coord.z, this.tabs[i], i);
         String s3;
         if (i > 0) {
            s3 = s2;
         } else {
            s3 = s1;
         }

         Widget btn = new Button(new Coord(bx, isz.y + 5), 60, this, s3) {
            @Override
            public void click() {
               for (int ix = 0; ix < Equipory.this.tabs.length; ix++) {
                  Equipory.this.tabs[ix].show(ix == i);
               }
            }
         };
         if (i > 0) {
            btn.tooltip = Text.render("Costume");
         } else {
            btn.tooltip = Text.render("Equipment");
         }

         bx = btn.c.x + btn.sz.x + 228;
      }

      this.pack();
   }

   @Override
   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      Widget ret = gettype(type).create(Coord.z, this, cargs);
      if (ret instanceof GItem) {
         GItem g = (GItem)ret;
         WItem[] v = new WItem[pargs.length];

         for (int i = 0; i < pargs.length; i++) {
            int ep = (Integer)pargs[i];
            Equipory.Box box = boxen[ep];
            this.slots[ep] = v[i] = new WItem(box.c.add(Inventory.sqlo), this.tabs[box.tab], g);
         }

         this.wmap.put(g, v);
      }

      return ret;
   }

   @Override
   public void cdestroy(Widget w) {
      super.cdestroy(w);
      if (w instanceof GItem) {
         GItem i = (GItem)w;

         for (WItem v : this.wmap.remove(i)) {
            this.ui.destroy(v);

            for (int s = 0; s < this.slots.length; s++) {
               if (this.slots[s] == v) {
                  this.slots[s] = null;
               }
            }
         }
      }
   }

   @Widget.RName("epry")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         long gobid;
         if (args.length < 1) {
            gobid = parent.getparent(GameUI.class).plid;
         } else {
            gobid = ((Integer)args[0]).intValue();
         }

         return new Equipory(c, parent, gobid);
      }
   }

   public static class Box {
      public final Coord c;
      public final Tex bg;
      public final int tab;

      public Box(Coord c, Tex bg, int tab) {
         this.c = c;
         this.bg = bg;
         this.tab = tab;
      }
   }

   private class Boxen extends Widget implements DTarget {
      final int tab;

      private Boxen(Coord c, Widget parent, int tab) {
         super(c, Equipory.isz, parent);
         this.tab = tab;
      }

      @Override
      public void draw(GOut g) {
         for (int i = 0; i < Equipory.boxen.length; i++) {
            Equipory.Box box = Equipory.boxen[i];
            if (box != null && box.tab == this.tab) {
               g.image(Inventory.sqlite, box.c);
               if (Equipory.this.slots[i] == null && box.bg != null) {
                  g.image(box.bg, box.c.add(Inventory.sqlo));
               }
            }
         }
      }

      @Override
      public boolean drop(Coord cc, Coord ul) {
         ul = ul.add(Inventory.sqlite.sz().div(2));

         for (int i = 0; i < Equipory.boxen.length; i++) {
            Equipory.Box box = Equipory.boxen[i];
            if (box != null && box.tab == this.tab && ul.isect(box.c, Inventory.sqlite.sz())) {
               Equipory.this.wdgmsg("drop", new Object[]{i});
               return true;
            }
         }

         Equipory.this.wdgmsg("drop", new Object[]{-1});
         return true;
      }

      @Override
      public boolean iteminteract(Coord cc, Coord ul) {
         return false;
      }
   }
}
