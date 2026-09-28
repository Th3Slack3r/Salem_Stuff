package haven;

import java.util.HashMap;
import java.util.Map;

public class Inventory extends Widget implements DTarget {
   private static final Tex obt = Resource.loadtex("gfx/hud/inv/obt");
   private static final Tex obr = Resource.loadtex("gfx/hud/inv/obr");
   private static final Tex obb = Resource.loadtex("gfx/hud/inv/obb");
   private static final Tex obl = Resource.loadtex("gfx/hud/inv/obl");
   private static final Tex ctl = Resource.loadtex("gfx/hud/inv/octl");
   private static final Tex ctr = Resource.loadtex("gfx/hud/inv/octr");
   private static final Tex cbr = Resource.loadtex("gfx/hud/inv/ocbr");
   private static final Tex cbl = Resource.loadtex("gfx/hud/inv/ocbl");
   private static final Tex bsq = Resource.loadtex("gfx/hud/inv/sq");
   public static final Coord sqsz = bsq.sz();
   public static final Coord isqsz = new Coord(40, 40);
   public static final Tex sqlite = Resource.loadtex("gfx/hud/inv/sq1");
   public static final Coord sqlo = new Coord(4, 4);
   public static final Tex refl = Resource.loadtex("gfx/hud/invref");
   Coord isz;
   Map<GItem, WItem> wmap = new HashMap<>();
   public int newseq = 0;

   @Override
   public void draw(GOut g) {
      invsq(g, Coord.z, this.isz);

      for (Coord cc = new Coord(0, 0); cc.y < this.isz.y; cc.y++) {
         for (cc.x = 0; cc.x < this.isz.x; cc.x++) {
            invrefl(g, sqoff(cc), isqsz);
         }
      }

      super.draw(g);
   }

   public Inventory(Coord c, Coord sz, Widget parent) {
      super(c, invsz(sz), parent);
      this.isz = sz;
   }

   public static Coord sqoff(Coord c) {
      return c.mul(sqsz).add(ctl.sz());
   }

   public static Coord sqroff(Coord c) {
      return c.sub(ctl.sz()).div(sqsz);
   }

   public static Coord invsz(Coord sz) {
      return sz.mul(sqsz).add(ctl.sz()).add(cbr.sz()).sub(4, 4);
   }

   public static void invrefl(GOut g, Coord c, Coord sz) {
      Coord ul = g.ul.sub(g.ul.div(2)).mod(refl.sz()).inv();
      Coord rc = new Coord();

      for (rc.y = ul.y; rc.y < c.y + sz.y; rc.y = rc.y + refl.sz().y) {
         for (rc.x = ul.x; rc.x < c.x + sz.x; rc.x = rc.x + refl.sz().x) {
            g.image(refl, rc, c, sz);
         }
      }
   }

   public static void invsq(GOut g, Coord c, Coord sz) {
      for (Coord cc = new Coord(0, 0); cc.y < sz.y; cc.y++) {
         for (cc.x = 0; cc.x < sz.x; cc.x++) {
            g.image(bsq, c.add(cc.mul(sqsz)).add(ctl.sz()));
         }
      }

      for (int x = 0; x < sz.x; x++) {
         g.image(obt, c.add(ctl.sz().x + sqsz.x * x, 0));
         g.image(obb, c.add(ctl.sz().x + sqsz.x * x, obt.sz().y + sqsz.y * sz.y - 4));
      }

      for (int y = 0; y < sz.y; y++) {
         g.image(obl, c.add(0, ctl.sz().y + sqsz.y * y));
         g.image(obr, c.add(obl.sz().x + sqsz.x * sz.x - 4, ctl.sz().y + sqsz.y * y));
      }

      g.image(ctl, c);
      g.image(ctr, c.add(ctl.sz().x + sqsz.x * sz.x - 4, 0));
      g.image(cbl, c.add(0, ctl.sz().y + sqsz.y * sz.y - 4));
      g.image(cbr, c.add(cbl.sz().x + sqsz.x * sz.x - 4, ctr.sz().y + sqsz.y * sz.y - 4));
   }

   public static void invsq(GOut g, Coord c) {
      g.image(sqlite, c);
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      if (this.ui.modshift) {
         this.wdgmsg("xfer", new Object[]{amount});
      }

      return true;
   }

   @Override
   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      Coord c = (Coord)pargs[0];
      Widget ret = gettype(type).create(c, this, cargs);
      if (ret instanceof GItem) {
         GItem i = (GItem)ret;
         this.wmap.put(i, new WItem(sqoff(c), this, i));
         this.newseq++;
      }

      return ret;
   }

   @Override
   public void cdestroy(Widget w) {
      super.cdestroy(w);
      if (w instanceof GItem) {
         GItem i = (GItem)w;
         this.ui.destroy(this.wmap.remove(i));
      }
   }

   @Override
   public boolean drop(Coord cc, Coord ul) {
      this.wdgmsg("drop", new Object[]{sqroff(ul.add(isqsz.div(2)))});
      return true;
   }

   @Override
   public boolean iteminteract(Coord cc, Coord ul) {
      return false;
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "sz") {
         this.isz = (Coord)args[0];
         this.resize(invsz(this.isz));
      }
   }

   @Widget.RName("inv")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Inventory(c, (Coord)args[0], parent);
      }
   }
}
