package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StockBin extends Widget implements DTarget {
   static Tex bg = Resource.loadtex("gfx/hud/bosq");
   static Text.Foundry lf = new Text.Foundry(new Font("SansSerif", 0, 18), Color.WHITE);
   private Indir<Resource> res;
   private Tex label;
   private StockBin.Value value;
   private Button take;
   private int rem = 0;
   private int bi = 0;

   private void setlabel(int rem, int bi) {
      this.rem = rem;
      this.bi = bi;
      if (this.label != null) {
         this.label.dispose();
      }

      this.label = lf.renderf("%d/%d", new Object[]{rem, bi}).tex();
   }

   public StockBin(Coord c, Widget parent, Indir<Resource> res, int rem, int bi) {
      super(c, bg.sz(), parent);
      this.res = res;
      this.setlabel(rem, bi);
      this.value = new StockBin.Value(new Coord(125, 27), 35, this, "");
      this.take = new Button(new Coord(165, 27), 35, this, "Take");
      this.value.canactivate = true;
      this.take.canactivate = true;
   }

   @Override
   public void draw(GOut g) {
      g.image(bg, Coord.z);

      try {
         Tex t = this.res.get().layer(Resource.imgc).tex();
         Coord dc = new Coord(6, bg.sz().y / 2 - t.sz().y / 2);
         g.image(t, dc);
      } catch (Loading var4) {
      }

      g.image(this.label, new Coord(45, bg.sz().y / 2 - this.label.sz().y / 2));
      super.draw(g);
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      try {
         if (this.res.get().layer(Resource.tooltip) != null) {
            return this.res.get().layer(Resource.tooltip).t;
         }
      } catch (Loading var4) {
      }

      return null;
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      Coord cc = this.xlate(this.take.c, true);
      if (c.isect(cc, this.take.sz)) {
         return this.take.mousedown(c.sub(cc), button);
      } else if (button == 1) {
         if (this.ui.modshift ^ this.ui.modctrl) {
            int dir = this.ui.modctrl ? -1 : 1;
            int all = dir > 0 ? Math.min(this.bi - this.rem, this.ui.gui.maininv.wmap.size()) : this.rem;
            int k = this.ui.modmeta ? all : 1;
            this.transfer(dir, k);
         } else {
            this.wdgmsg("click", new Object[0]);
         }

         return true;
      } else {
         return false;
      }
   }

   public void transfer(int dir, int amount) {
      for (int i = 0; i < amount; i++) {
         this.wdgmsg("xfer2", new Object[]{dir, 1});
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
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender != this.value && sender != this.take) {
         super.wdgmsg(sender, msg, args);
      } else {
         int amount = 0;

         try {
            amount = Integer.parseInt(this.value.text);
         } catch (Exception var6) {
         }

         if (amount > this.rem) {
            amount = this.rem;
         }

         if (amount > 0) {
            this.transfer(-1, amount);
         }
      }
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg.equals("chnum")) {
         this.setlabel((Integer)args[0], (Integer)args[1]);
      } else if (msg.equals("chres")) {
         this.res = this.ui.sess.getres((Integer)args[0]);
      } else {
         super.uimsg(msg, args);
      }
   }

   static {
      lf.aa = true;
   }

   @Widget.RName("spbox")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new StockBin(c, parent, parent.ui.sess.getres((Integer)args[0]), (Integer)args[1], (Integer)args[2]);
      }
   }

   private static class Value extends TextEntry {
      private static final Set<Integer> ALLOWED_KEYS = new HashSet<>(
         Arrays.asList(48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 37, 39, 10, 8, 127)
      );

      public Value(Coord c, int w, Widget parent, String deftext) {
         super(c, w, parent, deftext);
      }

      @Override
      public boolean type(char c, KeyEvent ev) {
         if (ALLOWED_KEYS.contains(ev.getKeyCode())) {
            return super.type(c, ev);
         } else {
            this.ui.root.globtype(c, ev);
            return false;
         }
      }
   }
}
