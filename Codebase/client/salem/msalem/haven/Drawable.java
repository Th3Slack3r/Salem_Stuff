package haven;

public abstract class Drawable extends GAttrib {
   public Drawable(Gob gob) {
      super(gob);
   }

   public abstract void setup(RenderList var1);

   public abstract Resource.Neg getneg();

   public Skeleton.Pose getpose() {
      return null;
   }
}
