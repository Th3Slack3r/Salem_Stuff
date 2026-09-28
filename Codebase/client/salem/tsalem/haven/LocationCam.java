package haven;

public class LocationCam extends Camera {
  private static final Matrix4f base = Transform.makerot(new Matrix4f(), new Coord3f(0.0F, 0.0F, 1.0F), 1.5707964F).mul1(Transform.makerot(new Matrix4f(), new Coord3f(0.0F, 1.0F, 0.0F), 1.5707964F));
  
  public final Location.Chain loc;
  
  private Matrix4f ll;
  
  private LocationCam(Location.Chain loc, Matrix4f lm) {
    super(base.mul(Transform.rxinvert(lm)));
    this.ll = lm;
    this.loc = loc;
  }
  
  public LocationCam(Location.Chain loc) {
    this(loc, loc.fin(Matrix4f.id));
  }
  
  public Matrix4f fin(Matrix4f p) {
    Matrix4f lm = this.loc.fin(Matrix4f.id);
    if (lm != this.ll)
      update(base.mul(Transform.rxinvert(this.ll = lm))); 
    return super.fin(p);
  }
}
