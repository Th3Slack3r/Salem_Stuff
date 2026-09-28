package haven;

import java.awt.Color;
import java.util.List;

public class Avaview extends PView {
   public static final Tex missing = Resource.loadtex("gfx/hud/equip/missing");
   public static final Coord dasz = missing.sz();
   public long avagob;
   private Composited comp;
   private List<Composited.MD> cmod = null;
   private List<Composited.ED> cequ = null;
   private final String camnm;
   private boolean missed = false;
   private Camera cam = null;
   private Composite lgc = null;

   public Avaview(Coord c, Coord sz, Widget parent, long avagob, String camnm) {
      super(c, sz, parent);
      this.camnm = camnm;
      this.avagob = avagob;
   }

   private Composite getgcomp() {
      Gob gob = this.ui.sess.glob.oc.getgob(this.avagob);
      if (gob == null) {
         return null;
      } else {
         Drawable d = gob.getattr(Drawable.class);
         if (!(d instanceof Composite)) {
            return null;
         } else {
            Composite gc = (Composite)d;
            return gc.comp == null ? null : gc;
         }
      }
   }

   private void initcomp(Composite gc) {
      if (this.comp == null || this.comp.skel != gc.comp.skel) {
         this.comp = new Composited(gc.comp.skel);
      }
   }

   private Camera makecam(Composite gc, String camnm) {
      if (this.comp == null) {
         throw new Loading();
      } else {
         Skeleton.BoneOffset bo = gc.base.get().layer(Skeleton.BoneOffset.class, camnm);
         if (bo == null) {
            throw new Loading();
         } else {
            GLState.Buffer buf = new GLState.Buffer(null);
            bo.forpose(this.comp.pose).prep(buf);
            return new LocationCam(buf.get(PView.loc));
         }
      }
   }

   protected Camera camera() {
      Composite gc = this.getgcomp();
      if (gc == null) {
         throw new Loading();
      } else {
         this.initcomp(gc);
         if (this.cam == null || gc != this.lgc) {
            this.cam = this.makecam(this.lgc = gc, this.camnm);
         }

         return this.cam;
      }
   }

   @Override
   protected void setup(RenderList rl) {
      Composite gc = this.getgcomp();
      if (gc == null) {
         this.missed = true;
      } else {
         this.initcomp(gc);
         if (gc.comp.cmod != this.cmod) {
            this.comp.chmod(this.cmod = gc.comp.cmod);
         }

         if (gc.comp.cequ != this.cequ) {
            this.comp.chequ(this.cequ = gc.comp.cequ);
         }

         rl.add(this.comp, null);
         rl.add(new DirLight(Color.WHITE, Color.WHITE, Color.WHITE, new Coord3f(1.0F, 1.0F, 1.0F).norm()), null);
      }
   }

   @Override
   public void tick(double dt) {
      if (this.comp != null) {
         this.comp.tick((int)(dt * 1000.0));
      }
   }

   @Override
   public void draw(GOut g) {
      this.missed = false;

      try {
         super.draw(g);
      } catch (Loading var3) {
         this.missed = true;
      }

      if (this.missed) {
         g.image(missing, Coord.z, this.sz);
      }
   }
}
