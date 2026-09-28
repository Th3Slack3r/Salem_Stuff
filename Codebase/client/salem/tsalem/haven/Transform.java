package haven;

public abstract class Transform extends GLState {
  private Matrix4f xf;
  
  private Matrix4f lp = null;
  
  private Matrix4f fin;
  
  public Transform(Matrix4f xf) {
    this.xf = xf;
  }
  
  public void update(Matrix4f xf) {
    this.xf = xf;
    this.lp = null;
  }
  
  public Matrix4f fin(Matrix4f p) {
    if (p != this.lp)
      this.fin = (this.lp = p).mul(this.xf); 
    return this.fin;
  }
  
  public static Matrix4f makescale(Matrix4f d, Coord3f s) {
    d.m[15] = 1.0F;
    d.m[14] = 0.0F;
    d.m[13] = 0.0F;
    d.m[12] = 0.0F;
    d.m[11] = 0.0F;
    d.m[9] = 0.0F;
    d.m[8] = 0.0F;
    d.m[7] = 0.0F;
    d.m[6] = 0.0F;
    d.m[4] = 0.0F;
    d.m[3] = 0.0F;
    d.m[2] = 0.0F;
    d.m[1] = 0.0F;
    d.m[0] = s.x;
    d.m[5] = s.y;
    d.m[10] = s.z;
    return d;
  }
  
  public static Matrix4f makexlate(Matrix4f d, Coord3f c) {
    d.m[15] = 1.0F;
    d.m[10] = 1.0F;
    d.m[5] = 1.0F;
    d.m[0] = 1.0F;
    d.m[11] = 0.0F;
    d.m[9] = 0.0F;
    d.m[8] = 0.0F;
    d.m[7] = 0.0F;
    d.m[6] = 0.0F;
    d.m[4] = 0.0F;
    d.m[3] = 0.0F;
    d.m[2] = 0.0F;
    d.m[1] = 0.0F;
    d.m[12] = c.x;
    d.m[13] = c.y;
    d.m[14] = c.z;
    return d;
  }
  
  public static Matrix4f makerot(Matrix4f d, Coord3f axis, float angle) {
    float c = (float)Math.cos(angle), s = (float)Math.sin(angle), C = 1.0F - c;
    float x = axis.x, y = axis.y, z = axis.z;
    d.m[14] = 0.0F;
    d.m[13] = 0.0F;
    d.m[12] = 0.0F;
    d.m[11] = 0.0F;
    d.m[7] = 0.0F;
    d.m[3] = 0.0F;
    d.m[15] = 1.0F;
    d.m[0] = x * x * C + c;
    d.m[4] = y * x * C - z * s;
    d.m[8] = z * x * C + y * s;
    d.m[1] = x * y * C + z * s;
    d.m[5] = y * y * C + c;
    d.m[9] = z * y * C - x * s;
    d.m[2] = x * z * C - y * s;
    d.m[6] = y * z * C + x * s;
    d.m[10] = z * z * C + c;
    return d;
  }
  
  public static Matrix4f rxinvert(Matrix4f m) {
    return m.trim3(1.0F).transpose().mul1(makexlate(new Matrix4f(), new Coord3f(-m.m[12], -m.m[13], -m.m[14])));
  }
  
  public String toString() {
    return getClass().getName() + "(" + this.xf + ")";
  }
}
