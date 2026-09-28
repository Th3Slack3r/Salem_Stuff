package haven;

import java.awt.Color;

public class VMeter extends Widget {
   private static final Coord C2 = new Coord(1, 0);
   static Tex bg = Resource.loadtex("gfx/hud/vm-frame");
   static Tex fg = Resource.loadtex("gfx/hud/vm-tex");
   Color cl;
   int amount;
   private Tex amt = null;

   public VMeter(Coord c, Widget parent, int amount, Color cl) {
      super(c, bg.sz().add(2, 12), parent);
      this.amount = amount;
      this.cl = cl;
   }

   private Tex amt() {
      if (this.amt == null) {
         this.amt = Text.render(String.format("%d", this.amount)).tex();
      }

      return this.amt;
   }

   @Override
   public void draw(GOut g) {
      g.image(bg, C2);
      g.chcolor(this.cl);
      int h = this.sz.y - 18;
      h = h * this.amount / 100;
      g.image(fg, C2, new Coord(0, this.sz.y - 15 - h), this.sz.add(0, h));
      g.chcolor();
      g.aimage(this.amt(), new Coord(this.sz.x / 2, this.sz.y), 0.5, 1.0);
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "set") {
         this.amount = (Integer)args[0];
         this.amt = null;
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
