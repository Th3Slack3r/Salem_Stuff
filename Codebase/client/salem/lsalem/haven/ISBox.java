package haven;

import java.awt.Color;
import java.awt.Font;

public class ISBox extends Widget implements DTarget {
   static Tex bg = Resource.loadtex("gfx/hud/bosq");
   static Text.Foundry lf = new Text.Foundry(new Font("SansSerif", 0, 18), Color.WHITE);
   private Resource res;
   private Text label;
   private int rem = 0;
   private int av = 0;

   private void setlabel(int rem, int av, int bi) {
      this.rem = rem;
      this.av = av;
      this.label = lf.renderf("%d/%d/%d", new Object[]{rem, av, bi});
   }

   public ISBox(Coord c, Widget parent, Resource res, int rem, int av, int bi) {
      super(c, bg.sz(), parent);
      this.res = res;
      this.setlabel(rem, av, bi);
   }

   @Override
   public void draw(GOut g) {
      g.image(bg, Coord.z);
      if (!this.res.loading) {
         Tex t = this.res.layer(Resource.imgc).tex();
         Coord dc = new Coord(6, bg.sz().y / 2 - t.sz().y / 2);
         g.image(t, dc);
      }

      g.image(this.label.tex(), new Coord(40, bg.sz().y / 2 - this.label.tex().sz().y / 2));
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      return !this.res.loading && this.res.layer(Resource.tooltip) != null ? this.res.layer(Resource.tooltip).t : null;
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (button != 1) {
         return false;
      } else {
         if (this.ui.modshift ^ this.ui.modctrl) {
            int dir = this.ui.modctrl ? -1 : 1;
            int all = dir > 0 ? this.rem : this.av;
            int k = this.ui.modmeta ? all : 1;

            for (int i = 0; i < k; i++) {
               this.wdgmsg("xfer2", new Object[]{dir, 1});
            }
         } else {
            this.wdgmsg("click", new Object[0]);
         }

         return true;
      }
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      if (amount < 0) {
         this.wdgmsg("xfer2", new Object[]{-1, this.ui.modflags()});
      }

      if (amount > 0) {
         this.wdgmsg("xfer2", new Object[]{1, this.ui.modflags()});
      }

      return true;
   }

   @Override
   public boolean drop(Coord cc, Coord ul) {
      this.wdgmsg("drop", new Object[0]);
      return true;
   }

   @Override
   public boolean iteminteract(Coord cc, Coord ul) {
      this.wdgmsg("iact", new Object[0]);
      return true;
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "chnum") {
         this.setlabel((Integer)args[0], (Integer)args[1], (Integer)args[2]);
      } else {
         super.uimsg(msg, args);
      }
   }

   static {
      lf.aa = true;
   }

   @Widget.RName("isbox")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new ISBox(c, parent, Resource.load((String)args[0]), (Integer)args[1], (Integer)args[2], (Integer)args[3]);
      }
   }
}
