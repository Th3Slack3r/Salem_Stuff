package haven;

public class LinMove extends Moving {
  public Coord s;
  
  public Coord t;
  
  public int c;
  
  public double a;
  
  public LinMove(Gob gob, Coord s, Coord t, int c) {
    super(gob);
    this.s = s;
    this.t = t;
    this.c = c;
    this.a = 0.0D;
  }
  
  public Coord3f getc() {
    float cx = (this.t.x - this.s.x) * (float)this.a;
    float cy = (this.t.y - this.s.y) * (float)this.a;
    cx += this.s.x;
    cy += this.s.y;
    return new Coord3f(cx, cy, this.gob.glob.map.getcz(cx, cy));
  }
  
  public double getv() {
    if (this.c == 0)
      return 0.0D; 
    return this.s.dist(this.t) / this.c * 0.06D;
  }
  
  public void ctick(int dt) {
    double da = dt / 1000.0D / this.c * 0.06D;
    this.a += da * 0.9D;
    if (this.a > 1.0D)
      this.a = 1.0D; 
  }
  
  public void setl(int l) {
    double a = l / this.c;
    if (a > this.a)
      this.a = a; 
  }
}
