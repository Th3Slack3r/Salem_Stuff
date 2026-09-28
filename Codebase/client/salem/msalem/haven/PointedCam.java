package haven;

public class PointedCam extends Camera {
   Coord3f base = Coord3f.o;
   float dist = 5.0F;
   float e;
   float a;

   public PointedCam() {
      super(Matrix4f.identity());
   }

   @Override
   public Matrix4f fin(Matrix4f p) {
      this.update(compute(this.base, this.dist, this.e, this.a));
      return super.fin(p);
   }

   public static Matrix4f compute(Coord3f base, float dist, float e, float a) {
      return makexlate(new Matrix4f(), new Coord3f(0.0F, 0.0F, -dist))
         .mul1(makerot(new Matrix4f(), new Coord3f(-1.0F, 0.0F, 0.0F), (float) (Math.PI / 2) - e))
         .mul1(makerot(new Matrix4f(), new Coord3f(0.0F, 0.0F, -1.0F), (float) (Math.PI / 2) + a))
         .mul1(makexlate(new Matrix4f(), base.inv()));
   }
}
