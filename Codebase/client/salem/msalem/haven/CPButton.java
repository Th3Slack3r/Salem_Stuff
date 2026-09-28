package haven;

import java.awt.Color;

public class CPButton extends Button {
   private static final Resource csfx = Resource.load("sfx/confirm");
   public Object cptip = null;
   public boolean s = false;
   private long fst;
   private TexI glowmask = null;

   public CPButton(Coord c, int w, Widget parent, String text) {
      super(c, w, parent, text);
   }

   public void cpclick() {
      this.wdgmsg("activate", new Object[0]);
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      if (this.s) {
         if (this.glowmask == null) {
            this.glowmask = new TexI(PUtils.glowmask(PUtils.glowmask(this.draw().getRaster()), 10, new Color(255, 64, 0)));
         }

         double ph = (System.currentTimeMillis() - this.fst) / 1000.0;
         g.chcolor(255, 255, 255, (int)(128.0 * (Math.cos(ph * Math.PI * 2.0) * -0.5 + 0.5)));
         GOut g2 = g.reclipl(new Coord(-10, -10), g.sz.add(20, 20));
         g2.image(this.glowmask, Coord.z);
      }
   }

   @Override
   public void click() {
      if (!this.s) {
         this.fst = System.currentTimeMillis();
         this.s = true;
         this.change(this.text.text, new Color(255, 64, 0));
         this.redraw();
         Audio.play(csfx);
      } else if (System.currentTimeMillis() - this.fst > 1000L) {
         this.cpclick();
         this.s = false;
         this.change(this.text.text, defcol);
         this.redraw();
      }
   }

   @Override
   public void mousemove(Coord c) {
      super.mousemove(c);
      if (this.s && !c.isect(Coord.z, this.sz)) {
         this.s = false;
         this.change(this.text.text, defcol);
         this.redraw();
      }
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      return this.s && this.cptip != null ? this.cptip : super.tooltip(c, prev);
   }

   @Widget.RName("cpbtn")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new CPButton(c, (Integer)args[0], parent, (String)args[1]);
      }
   }
}
