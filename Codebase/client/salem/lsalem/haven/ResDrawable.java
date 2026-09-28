package haven;

import java.util.ArrayList;
import java.util.List;

public class ResDrawable extends Drawable {
   public final Indir<Resource> res;
   Message sdt;
   public Sprite spr = null;
   int delay = 0;
   boolean show_radius = false;
   List<Gob.Overlay> radii = new ArrayList<>();

   public ResDrawable(Gob gob, Indir<Resource> res, Message sdt) {
      super(gob);
      this.res = res;
      this.sdt = sdt;

      try {
         this.init();
      } catch (Loading var5) {
      }
   }

   public ResDrawable(Gob gob, Resource res) {
      this(gob, res.indir(), new Message(0));
   }

   public void init() {
      if (this.spr == null) {
         this.spr = Sprite.create(this.gob, this.res.get(), this.sdt.clone());
         String name = this.res.get().name;
         this.radii.addAll(ColoredRadius.getRadii(name, this.gob));
      }
   }

   @Override
   public void setup(RenderList rl) {
      try {
         this.init();
      } catch (Loading var3) {
         return;
      }

      this.checkRadius();
      this.spr.setup(rl);
   }

   private void checkRadius() {
      if (this.show_radius != Config.show_radius) {
         this.show_radius = Config.show_radius;
         this.gob.ols.removeAll(this.radii);
         if (this.show_radius) {
            this.gob.ols.addAll(this.radii);
         }
      }
   }

   @Override
   public void ctick(int dt) {
      if (this.spr == null) {
         this.delay += dt;
      } else {
         this.spr.tick(this.delay + dt);
         this.delay = 0;
      }
   }

   @Override
   public void dispose() {
      if (this.spr != null) {
         this.spr.dispose();
      }
   }

   @Override
   public Resource.Neg getneg() {
      return this.res.get().layer(Resource.negc);
   }

   @Override
   public Skeleton.Pose getpose() {
      this.init();
      return this.spr instanceof SkelSprite ? ((SkelSprite)this.spr).pose : null;
   }
}
