package haven;

public class PointedCam extends Camera {
  Coord3f base = Coord3f.o;
  
  float dist = 5.0F;
  
  float e;
  
  float a;
  
  public PointedCam() {
    super(Matrix4f.identity());
  }
  
  public Matrix4f fin(Matrix4f p) {
    update(compute(this.base, this.dist, this.e, this.a));
    return super.fin(p);
  }
  
  public static Matrix4f compute(Coord3f base, float dist, float e, float a) {
    return Transform.makexlate(new Matrix4f(), new Coord3f(0.0F, 0.0F, -dist)).mul1(Transform.makerot(new Matrix4f(), new Coord3f(-1.0F, 0.0F, 0.0F), 1.5707964F - e)).mul1(Transform.makerot(new Matrix4f(), new Coord3f(0.0F, 0.0F, -1.0F), 1.5707964F + a))
      .mul1(Transform.makexlate(new Matrix4f(), base.inv()));
  }
}
