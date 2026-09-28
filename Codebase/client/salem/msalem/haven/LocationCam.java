package haven;

public class LocationCam extends Camera {
   private static final Matrix4f base = makerot(new Matrix4f(), new Coord3f(0.0F, 0.0F, 1.0F), (float) (Math.PI / 2))
      .mul1(makerot(new Matrix4f(), new Coord3f(0.0F, 1.0F, 0.0F), (float) (Math.PI / 2)));
   public final Location.Chain loc;
   private Matrix4f ll;

   private LocationCam(Location.Chain loc, Matrix4f lm) {
      super(base.mul(rxinvert(lm)));
      this.ll = lm;
      this.loc = loc;
   }

   public LocationCam(Location.Chain loc) {
      this(loc, loc.fin(Matrix4f.id));
   }

   @Override
   public Matrix4f fin(Matrix4f p) {
      Matrix4f lm = this.loc.fin(Matrix4f.id);
      if (lm != this.ll) {
         this.update(base.mul(rxinvert(this.ll = lm)));
      }

      return super.fin(p);
   }
}
