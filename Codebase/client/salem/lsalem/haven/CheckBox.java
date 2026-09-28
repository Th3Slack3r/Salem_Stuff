package haven;

public class CheckBox extends Widget {
   public static final Tex box = Resource.loadtex("gfx/hud/chkbox");
   public static final Tex act = Resource.loadtex("gfx/hud/chkboxa");
   public static final Text.Foundry lblf = new Text.Foundry("Sans", 11);
   public boolean a = false;
   public boolean enabled = true;
   Text lbl;

   public CheckBox(Coord c, Widget parent, String lbl) {
      super(c, box.sz(), parent);
      this.lbl = lblf.render(lbl);
      this.sz = new Coord(box.sz().x + 2 + this.lbl.sz().x, Math.max(box.sz().y, this.lbl.sz().y));
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (!this.enabled) {
         return false;
      } else if (button != 1) {
         return false;
      } else {
         this.set(!this.a);
         return true;
      }
   }

   public void set(boolean a) {
      this.a = a;
      this.changed(a);
   }

   @Override
   public void draw(GOut g) {
      if (!this.enabled) {
         g.chcolor(128, 128, 128, 255);
      }

      g.image(this.lbl.tex(), new Coord(box.sz().x + 2, (box.sz().y - this.lbl.sz().y) / 2));
      g.image(this.a ? act : box, Coord.z);
      g.chcolor();
      super.draw(g);
   }

   public void changed(boolean val) {
      if (this.canactivate) {
         this.wdgmsg("ch", new Object[]{this.a});
      }
   }

   @Widget.RName("chk")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         CheckBox ret = new CheckBox(c, parent, (String)args[0]);
         ret.canactivate = true;
         return ret;
      }
   }
}
