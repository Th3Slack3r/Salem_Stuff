package haven;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Equipory extends Widget implements DTarget {
   static Coord[] ecoords = new Coord[]{
      new Coord(250, 0),
      new Coord(50, 70),
      new Coord(250, 70),
      new Coord(300, 70),
      new Coord(50, 0),
      new Coord(50, 210),
      new Coord(25, 140),
      new Coord(275, 140),
      null,
      new Coord(0, 0),
      new Coord(0, 210),
      new Coord(300, 0),
      new Coord(300, 210),
      new Coord(100, 0),
      new Coord(0, 70),
      new Coord(250, 210)
   };
   static Tex[] ebgs = new Tex[]{
      Resource.loadtex("gfx/hud/inv/head"),
      Resource.loadtex("gfx/hud/inv/face"),
      Resource.loadtex("gfx/hud/inv/shirt"),
      Resource.loadtex("gfx/hud/inv/torsoa"),
      Resource.loadtex("gfx/hud/inv/keys"),
      Resource.loadtex("gfx/hud/inv/belt"),
      Resource.loadtex("gfx/hud/inv/lhande"),
      Resource.loadtex("gfx/hud/inv/rhande"),
      null,
      Resource.loadtex("gfx/hud/inv/wallet"),
      Resource.loadtex("gfx/hud/inv/coat"),
      Resource.loadtex("gfx/hud/inv/cape"),
      Resource.loadtex("gfx/hud/inv/pants"),
      null,
      Resource.loadtex("gfx/hud/inv/back"),
      Resource.loadtex("gfx/hud/inv/feet")
   };
   static Coord isz = new Coord();
   private final AttrBonusWdg bonuses;
   private final IButton showbonus;
   private final IButton hidebonus;
   WItem[] slots = new WItem[ecoords.length];
   Map<GItem, WItem[]> wmap = new HashMap<>();
   private EquipOpts opts;
   private List<GItem> checkForDrop = new LinkedList<>();

   public Equipory(Coord c, Widget parent, long gobid) {
      super(c, isz, parent);
      this.bonuses = new AttrBonusWdg(this, new Coord(isz.x, 0));
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
      new Equipory.Boxen();
      this.opts = new EquipOpts(new Coord(200, 100), this.ui.gui);
      this.opts.hide();
      Window p = (Window)parent;
      this.showbonus = new IButton(Coord.z, p, Window.rbtni[0], Window.rbtni[1], Window.rbtni[2]) {
         {
            this.tooltip = Text.render("Show artifice bonuses");
         }

         @Override
         public void click() {
            Equipory.this.toggleBonuses();
         }
      };
      this.showbonus.visible = !this.bonuses.visible;
      p.addtwdg(this.showbonus);
      this.hidebonus = new IButton(Coord.z, p, Window.lbtni[0], Window.lbtni[1], Window.lbtni[2]) {
         {
            this.tooltip = Text.render("Hide artifice bonuses");
         }

         @Override
         public void click() {
            Equipory.this.toggleBonuses();
         }
      };
      this.hidebonus.visible = this.bonuses.visible;
      p.addtwdg(this.hidebonus);
      p.addtwdg(new IButton(Coord.z, p, Window.obtni[0], Window.obtni[1], Window.obtni[2]) {
         {
            this.tooltip = Text.render("Toggle equip shortcuts config");
         }

         @Override
         public void click() {
            Equipory.this.toggleOptions();
         }
      });
      this.pack();
      parent.pack();
   }

   private void toggleBonuses() {
      this.bonuses.toggle();
      this.showbonus.visible = !this.bonuses.visible;
      this.hidebonus.visible = this.bonuses.visible;
      this.pack();
      this.parent.pack();
   }

   private void toggleOptions() {
      if (this.opts != null) {
         this.opts.toggle();
      }
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender instanceof GItem && this.wmap.containsKey(sender) && msg.equals("ttupdate")) {
         this.bonuses.update(this.slots);
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   @Override
   public void tick(double dt) {
      super.tick(dt);

      try {
         if (!this.checkForDrop.isEmpty()) {
            GItem g = this.checkForDrop.get(0);
            if (g.resname().equals("gfx/invobjs/bat")) {
               g.drop = true;
            }

            this.checkForDrop.remove(0);
         }
      } catch (Resource.Loading var4) {
      }
   }

   @Override
   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      Widget ret = gettype(type).create(Coord.z, this, cargs);
      if (ret instanceof GItem) {
         GItem g = (GItem)ret;
         g.sendttupdate = true;
         WItem[] v = new WItem[pargs.length];

         for (int i = 0; i < pargs.length; i++) {
            int ep = (Integer)pargs[i];
            this.slots[ep] = v[i] = new WItem(ecoords[ep].add(Inventory.sqlo), this, g);
         }

         this.wmap.put(g, v);
         if (Config.auto_drop_bats) {
            this.checkForDrop.add(g);
         }
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

         this.bonuses.update(this.slots);
      }
   }

   @Override
   public boolean drop(Coord cc, Coord ul) {
      ul = ul.add(Inventory.sqlite.sz().div(2));

      for (int i = 0; i < ecoords.length; i++) {
         if (ecoords[i] != null && ul.isect(ecoords[i], Inventory.sqlite.sz())) {
            this.wdgmsg("drop", new Object[]{i});
            return true;
         }
      }

      this.wdgmsg("drop", new Object[]{-1});
      return true;
   }

   @Override
   public boolean iteminteract(Coord cc, Coord ul) {
      return false;
   }

   static {
      for (Coord ec : ecoords) {
         if (ec != null) {
            if (ec.x + Inventory.sqlite.sz().x > isz.x) {
               isz.x = ec.x + Inventory.sqlite.sz().x;
            }

            if (ec.y + Inventory.sqlite.sz().y > isz.y) {
               isz.y = ec.y + Inventory.sqlite.sz().y;
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

   private class Boxen extends Widget {
      private Boxen() {
         super(Coord.z, Equipory.isz, Equipory.this);
      }

      @Override
      public void draw(GOut g) {
         for (int i = 0; i < Equipory.ecoords.length; i++) {
            if (Equipory.ecoords[i] != null) {
               g.image(Inventory.sqlite, Equipory.ecoords[i]);
               if (Equipory.this.slots[i] == null && Equipory.ebgs[i] != null) {
                  g.image(Equipory.ebgs[i], Equipory.ecoords[i].add(Inventory.sqlo));
               }
            }
         }
      }
   }
}
