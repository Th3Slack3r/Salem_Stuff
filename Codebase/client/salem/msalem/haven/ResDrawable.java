package haven;

public class ResDrawable extends Drawable {
   final Indir<Resource> res;
   Message sdt;
   Sprite spr = null;
   int delay = 0;

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
      }
   }

   @Override
   public void setup(RenderList rl) {
      try {
         this.init();
      } catch (Loading var3) {
         return;
      }

      this.spr.setup(rl);
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
