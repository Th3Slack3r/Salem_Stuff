package haven.headless;

import haven.BuddyWnd;
import haven.CharWnd;
import haven.Config;
import haven.Coord;
import haven.Fightview;
import haven.GItem;
import haven.GOut;
import haven.GameUI;
import haven.Gobble;
import haven.HelpWnd;
import haven.Indir;
import haven.Inventory;
import haven.LocalMiniMap;
import haven.MapView;
import haven.OverviewTool;
import haven.Polity;
import haven.Resource;
import haven.UI;
import haven.WeightWdg;
import haven.Widget;
import haven.Window;
import haven.res.lib.HomeTrackerFX;
import java.awt.Color;

public class HeadlessGameUI extends GameUI {
   public HeadlessGameUI(Widget wdg, String chrid, long plid) {
      super(wdg, chrid, plid);
   }

   @Override
   public void draw(GOut g) {
   }

   @Override
   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      String place = ((String)pargs[0]).intern();
      if (place == "mapview") {
         Coord cc = (Coord)cargs[0];
         this.map = new MapView(Coord.z, this.sz, this, cc, this.plid);
         this.map.lower();
         if (this.mmap != null) {
            this.ui.destroy(this.mmap);
         }

         if (Config.pclaimv) {
            this.map.enol(0, 1);
         }

         if (Config.tclaimv) {
            this.map.enol(2, 3);
         }

         if (Config.wclaimv) {
            this.map.enol(4);
         }

         this.updateRenderFilter();
         this.mmap = new LocalMiniMap(new Coord(this.sz.x - 250, 15), new Coord(146, 146), this, this.map);
         return this.map;
      } else if (place == "fight") {
         this.fv = (Fightview)gettype(type).create(new Coord(this.sz.x - Fightview.width, 0), this, cargs);
         return this.fv;
      } else if (place == "inv") {
         String nm = pargs.length > 1 ? (String)pargs[1] : null;
         if (this.invwnd == null) {
            this.invwnd = new GameUI.InvWindow(new Coord(100, 100), Coord.z, this, "Inventory", this);
            this.invwnd.hide();
         }

         if (nm == null) {
            Inventory inv = (Inventory)this.invwnd.makechild(type, new Object[0], cargs);
            this.maininv = inv;
            this.weightwdg = new WeightWdg(new Coord(10, 100), this);
            return inv;
         } else {
            return this.invwnd.makechild(type, new Object[]{nm}, cargs);
         }
      } else if (place == "equ") {
         this.equwnd = new GameUI.Hidewnd(new Coord(400, 10), Coord.z, this, "Equipment");
         Widget equ = gettype(type).create(Coord.z, this.equwnd, cargs);
         this.equwnd.pack();
         this.equwnd.hide();
         return equ;
      } else if (place == "hand") {
         GItem g = (GItem)gettype(type).create((Coord)pargs[1], this, cargs);
         this.hand.add(g);
         this.updhand();
         return g;
      } else if (place == "craft") {
         final Widget[] mk = new Widget[]{null};
         this.showCraftWnd();
         if (this.craftwnd != null) {
            mk[0] = gettype(type).create(new Coord(215, 250), this.craftwnd, cargs);
            this.craftwnd.setMakewindow(mk[0]);
            return mk[0];
         } else {
            this.makewnd = new Window(new Coord(350, 100), Coord.z, this, "Crafting") {
               @Override
               public void wdgmsg(Widget sender, String msg, Object... args) {
                  if (sender == this && msg.equals("close")) {
                     mk[0].wdgmsg("close");
                  } else {
                     super.wdgmsg(sender, msg, args);
                  }
               }

               @Override
               public void cdestroy(Widget w) {
                  if (w == mk[0]) {
                     this.ui.destroy(this);
                     HeadlessGameUI.this.makewnd = null;
                  }
               }
            };
            mk[0] = gettype(type).create(Coord.z, this.makewnd, cargs);
            this.makewnd.pack();
            return mk[0];
         }
      } else if (place == "buddy") {
         this.buddies = (BuddyWnd)gettype(type).create(new Coord(187, 50), this, cargs);
         this.buddies.hide();
         return this.buddies;
      } else if (place == "pol") {
         this.polity = (Polity)gettype(type).create(new Coord(500, 50), this, cargs);
         this.polity.hide();
         return this.polity;
      } else if (place == "chr") {
         this.chrwdg = (CharWnd)gettype(type).create(new Coord(100, 50), this, cargs);
         this.chrwdg.hide();
         this.fixattrview(this.chrwdg);
         return this.chrwdg;
      } else if (place == "chat") {
         return this.chat.makechild(type, new Object[0], cargs);
      } else if (place == "party") {
         return gettype(type).create(new Coord(2, 80), this, cargs);
      } else if (place == "misc") {
         if (type.contains("ui/hrtptr")) {
            if (this.hrtptr != null) {
               this.hrtptr.dispose();
               this.hrtptr = null;
            }

            this.hrtptr = new HomeTrackerFX.HTrackWdg(this, gettype(type).create((Coord)pargs[1], this, cargs));
            return this.hrtptr;
         } else {
            return gettype(type).create((Coord)pargs[1], this, cargs);
         }
      } else {
         throw new UI.UIException("Illegal gameui child", type, pargs);
      }
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "err") {
         String err = (String)args[0];
         this.error(err);
      } else if (msg == "prog") {
         if (args.length > 0) {
            this.prog = (Integer)args[0];
         } else {
            this.prog = -1;
         }
      } else if (msg == "setbelt") {
         int slot = (Integer)args[0];
         if (args.length < 2) {
            this.belt[slot] = null;
         } else {
            this.belt[slot] = this.ui.sess.getres((Integer)args[1]);
         }
      } else if (msg == "ins") {
         this.tm.updinsanity((Integer)args[0]);
      } else if (msg == "stm") {
         int[] n = new int[4];

         for (int i = 0; i < 4; i++) {
            n[i] = (Integer)args[i];
         }

         this.tm.upds(n);
      } else if (msg == "htm") {
         int[] n = new int[4];

         for (int i = 0; i < 4; i++) {
            n[i] = (Integer)args[i];
         }

         this.tm.updh(n);
      } else if (msg == "gavail") {
         this.tm.gavail = (Integer)args[0] != 0;
      } else if (msg == "gobble") {
         boolean g = (Integer)args[0] != 0;
         if (g && this.gobble == null) {
            this.tm.hide();
            this.gobble = new Gobble(Coord.z, this);
            this.resize(this.sz);
         } else if (!g && this.gobble != null) {
            this.ui.destroy(this.gobble);
            this.gobble = null;
            this.tm.show();
         }
      } else if (msg == "gtm") {
         int[] n = new int[4];

         for (int i = 0; i < 4; i++) {
            n[i] = (Integer)args[i];
         }

         this.gobble.updt(n);
      } else if (msg == "glvlup") {
         this.gobble.lvlup((Integer)args[0]);
      } else if (msg == "glvls") {
         this.gobble.lcount((Integer)args[0], (Color)args[1]);
      } else if (msg == "gtypemod") {
         this.gobble.typemod(this.ui.sess.getres((Integer)args[0]), ((Integer)args[1]).intValue() / 100.0);
      } else if (msg == "polowner") {
         String o = (String)args[0];
         boolean n = (Integer)args[1] != 0;
         if (o.length() == 0) {
            o = null;
         } else {
            o = o.intern();
         }

         if (o != this.polowner) {
            if (this.map != null) {
               if (o == null) {
                  if (this.polowner != null) {
                     this.map.setpoltext("Leaving " + this.polowner);
                  }
               } else {
                  this.map.setpoltext("Entering " + o);
               }
            }

            this.polowner = o;
         }
      } else if (msg == "dblk") {
         int id = (Integer)args[0];
         this.dblk = id < 0 ? null : this.ui.sess.getres(id);
      } else if (msg == "lblk") {
         int id = (Integer)args[0];
         this.lblk = id < 0 ? null : this.ui.sess.getres(id);
      } else if (msg == "showhelp") {
         Indir<Resource> res = this.ui.sess.getres((Integer)args[0]);
         if (this.help == null) {
            this.help = new HelpWnd(this.sz.div(2).sub(150, 200), this, res);
         } else {
            this.help.res = res;
         }
      } else if (msg == "weight") {
         this.weight = (Integer)args[0];
         if (this.invwnd != null) {
            this.invwnd.updweight();
         }

         if (this.weightwdg != null) {
            this.weightwdg.update(this.weight);
            OverviewTool.instance(this.ui).force_update();
         }
      } else {
         super.uimsg(msg, args);
      }
   }

   @Override
   public void message(String msg, Color msgColor) {
      System.out.println("\t" + msg);
   }

   @Widget.RName("headless_gameui")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         String chrid = (String)args[0];
         int plid = (Integer)args[1];
         return new HeadlessGameUI(parent, chrid, plid);
      }
   }
}
