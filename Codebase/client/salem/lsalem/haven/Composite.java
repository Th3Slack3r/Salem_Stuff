package haven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Composite extends Drawable {
   public static final float ipollen = 0.2F;
   public final Indir<Resource> base;
   public Composited comp;
   private Collection<ResData> nposes = null;
   private Collection<ResData> tposes = null;
   private boolean retainequ = false;
   private float tptime;
   private WrapMode tpmode;
   public int pseq;
   private List<Composited.MD> nmod;
   private List<Composited.ED> nequ;
   boolean show_radius = false;
   List<Gob.Overlay> radii = new ArrayList<>();

   public Composite(Gob gob, Indir<Resource> base) {
      super(gob);
      this.base = base;
   }

   private void init() {
      if (this.comp == null) {
         this.comp = new Composited(this.base.get().layer(Skeleton.Res.class).s);
         String name = this.base.get().name;
         this.radii.addAll(ColoredRadius.getRadii(name, this.gob));
      }
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
   public void setup(RenderList rl) {
      try {
         this.init();
      } catch (Loading var3) {
         return;
      }

      this.checkRadius();
      rl.add(this.comp, null);
   }

   private List<Skeleton.PoseMod> loadposes(Collection<ResData> rl, Skeleton skel) {
      List<Skeleton.PoseMod> mods = new ArrayList<>(rl.size());

      for (ResData dat : rl) {
         mods.add(skel.mkposemod(this.gob, dat.res.get(), dat.sdt));
      }

      return mods;
   }

   private List<Skeleton.PoseMod> loadposes(Collection<ResData> rl, Skeleton skel, WrapMode mode) {
      List<Skeleton.PoseMod> mods = new ArrayList<>(rl.size());

      for (ResData dat : rl) {
         for (Skeleton.ResPose p : dat.res.get().layers(Skeleton.ResPose.class)) {
            mods.add(p.forskel(this.gob, skel, mode == null ? p.defmode : mode));
         }
      }

      return mods;
   }

   private void updequ() {
      this.retainequ = false;
      if (this.nmod != null) {
         this.comp.chmod(this.nmod);
         this.nmod = null;
      }

      if (this.nequ != null) {
         this.comp.chequ(this.nequ);
         this.nequ = null;
      }
   }

   @Override
   public void ctick(int dt) {
      if (this.comp != null) {
         if (this.nposes != null) {
            try {
               Composited.Poses np = this.comp.new Poses(this.loadposes(this.nposes, this.comp.skel));
               np.set(0.2F);
               this.nposes = null;
            } catch (Loading var5) {
            }
         } else if (this.tposes != null) {
            try {
               final Composited.Poses cp = this.comp.poses;
               Composited var10003 = this.comp;
               this.comp.getClass();
               Composited.Poses np = new Composited.Poses(var10003, this.loadposes(this.tposes, this.comp.skel, this.tpmode)) {
                  {
                     x0.getClass();
                  }

                  @Override
                  protected void done() {
                     cp.set(0.2F);
                     Composite.this.updequ();
                  }
               };
               np.limit = this.tptime;
               np.set(0.2F);
               this.tposes = null;
               this.retainequ = true;
            } catch (Loading var4) {
            }
         } else if (!this.retainequ) {
            this.updequ();
         }

         if (!Config.remove_animations) {
            this.comp.tick(dt);
         }
      }
   }

   @Override
   public Resource.Neg getneg() {
      return this.base.get().layer(Resource.negc);
   }

   @Override
   public Skeleton.Pose getpose() {
      this.init();
      return this.comp.pose;
   }

   public void chposes(Collection<ResData> poses, boolean interp) {
      if (this.tposes != null) {
         this.tposes = null;
      }

      this.nposes = poses;
   }

   @Deprecated
   public void chposes(List<Indir<Resource>> poses, boolean interp) {
      this.chposes(ResData.wrap(poses), interp);
   }

   public void tposes(Collection<ResData> poses, WrapMode mode, float time) {
      this.tposes = poses;
      this.tpmode = mode;
      this.tptime = time;
   }

   @Deprecated
   public void tposes(List<Indir<Resource>> poses, WrapMode mode, float time) {
      this.tposes(ResData.wrap(poses), mode, time);
   }

   public void chmod(List<Composited.MD> mod) {
      this.nmod = mod;
   }

   public void chequ(List<Composited.ED> equ) {
      this.nequ = equ;
   }
}
