package haven;

import java.awt.Color;

public class VMeter extends Widget {
   static Tex bg = Resource.loadtex("gfx/hud/vm-frame");
   static Tex fg = Resource.loadtex("gfx/hud/vm-tex");
   Color cl;
   int amount;

   public VMeter(Coord c, Widget parent, int amount, Color cl) {
      super(c, bg.sz(), parent);
      this.amount = amount;
      this.cl = cl;
   }

   @Override
   public void draw(GOut g) {
      g.image(bg, Coord.z);
      g.chcolor(this.cl);
      int h = this.sz.y - 6;
      h = h * this.amount / 100;
      g.image(fg, new Coord(0, 0), new Coord(0, this.sz.y - 3 - h), this.sz.add(0, h));
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "set") {
         this.amount = (Integer)args[0];
         if (args.length > 1) {
            this.cl = (Color)args[1];
         }
      } else if (msg == "col") {
         this.cl = (Color)args[0];
      } else {
         super.uimsg(msg, args);
      }
   }

   @Widget.RName("vm")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         Color cl;
         if (args.length > 4) {
            cl = new Color((Integer)args[1], (Integer)args[2], (Integer)args[3], (Integer)args[4]);
         } else if (args.length > 3) {
            cl = new Color((Integer)args[1], (Integer)args[2], (Integer)args[3]);
         } else {
            cl = (Color)args[1];
         }

         return new VMeter(c, parent, (Integer)args[0], cl);
      }
   }
}
