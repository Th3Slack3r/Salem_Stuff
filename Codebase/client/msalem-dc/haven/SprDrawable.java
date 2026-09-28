package haven;

public class SprDrawable extends Drawable {
   Sprite spr = null;

   public SprDrawable(Gob gob, Sprite spr) {
      super(gob);
      this.spr = spr;
   }

   @Override
   public void setup(RenderList rl) {
      rl.add(this.spr, null);
   }

   @Override
   public void ctick(int dt) {
      this.spr.tick(dt);
   }

   @Override
   public Resource.Neg getneg() {
      return null;
   }
}
