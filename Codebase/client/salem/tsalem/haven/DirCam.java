package haven;

public class DirCam extends Camera {
  static final Coord3f defdir = new Coord3f(0.0F, 0.0F, -1.0F);
  
  Coord3f base = Coord3f.o, dir = defdir;
  
  public DirCam() {
    super(Matrix4f.identity());
  }
  
  public Matrix4f fin(Matrix4f p) {
    update(compute(this.base, this.dir));
    return super.fin(p);
  }
  
  public static Matrix4f compute(Coord3f base, Coord3f dir) {
    Coord3f diff = defdir.cmul(dir);
    float a = (float)Math.asin(diff.abs());
    return Transform.makerot(new Matrix4f(), diff, -a).mul1(Transform.makexlate(new Matrix4f(), base.inv()));
  }
}
