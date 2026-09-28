package haven;

public class Following extends Moving {
   long tgt;
   public double lastv = 0.0;
   Indir<Resource> xfres;
   String xfname;
   GLState xf = null;
   GLState lpxf = null;
   Gob lxfb = null;
   Skeleton.Pose lpose = null;

   public Following(Gob gob, long tgt, Indir<Resource> xfres, String xfname) {
      super(gob);
      this.tgt = tgt;
      this.xfres = xfres;
      this.xfname = xfname;
   }

   @Override
   public Coord3f getc() {
      Gob tgt = this.gob.glob.oc.getgob(this.tgt);
      return tgt == null ? this.gob.getrc() : tgt.getc();
   }

   @Override
   public double getv() {
      Gob tgt = this.gob.glob.oc.getgob(this.tgt);
      if (tgt != null) {
         Moving mv = tgt.getattr(Moving.class);
         if (mv == null) {
            this.lastv = 0.0;
         } else {
            this.lastv = mv.getv();
         }
      }

      return this.lastv;
   }

   public Gob tgt() {
      return this.gob.glob.oc.getgob(this.tgt);
   }

   private Skeleton.Pose getpose(Gob tgt) {
      return tgt == null ? null : tgt.getattr(Drawable.class).getpose();
   }

   public GLState xf() {
      synchronized (this) {
         Gob tgt = this.tgt();
         Skeleton.Pose cpose = this.getpose(tgt);
         GLState pxf = xf(tgt);
         if (this.xf == null || cpose != this.lpose || this.lpxf != pxf) {
            if (tgt == null) {
               this.xf = null;
               this.lpose = null;
               this.lxfb = null;
               this.lpxf = null;
               return null;
            }

            Skeleton.BoneOffset bo = this.xfres.get().layer(Skeleton.BoneOffset.class, this.xfname);
            if (bo == null) {
               throw new RuntimeException("No such boneoffset in " + this.xfres.get() + ": " + this.xfname);
            }

            if (pxf != null) {
               this.xf = GLState.compose(pxf, bo.forpose(cpose));
            } else {
               this.xf = GLState.compose(tgt.loc, bo.forpose(cpose));
            }

            this.lpxf = pxf;
            this.lxfb = tgt;
            this.lpose = cpose;
         }
      }

      return this.xf;
   }

   public static GLState xf(Gob gob) {
      if (gob == null) {
         return null;
      } else {
         Following flw = gob.getattr(Following.class);
         return flw == null ? null : flw.xf();
      }
   }
}
