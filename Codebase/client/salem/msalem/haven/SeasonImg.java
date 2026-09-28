package haven;

public class SeasonImg extends Widget {
   private static final Tex[] seasons = new Tex[]{
      Resource.loadtex("gfx/hud/coldsnap"), Resource.loadtex("gfx/hud/everbloom"), Resource.loadtex("gfx/hud/bloodmoon")
   };
   private double t = 0.0;

   public SeasonImg(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
   }

   @Override
   public void draw(GOut g) {
      Tex t = seasons[this.ui.sess.glob.season];
      g.image(t, this.sz.sub(t.sz()).div(2));
   }
}
