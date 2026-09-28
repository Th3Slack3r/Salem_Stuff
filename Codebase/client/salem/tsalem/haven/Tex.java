package haven;

public abstract class Tex {
  protected Coord dim;
  
  public Tex(Coord sz) {
    this.dim = sz;
  }
  
  public Coord sz() {
    return this.dim;
  }
  
  public static int nextp2(int in) {
    int h = Integer.highestOneBit(in);
    return (h == in) ? h : (h * 2);
  }
  
  public abstract void render(GOut paramGOut, Coord paramCoord1, Coord paramCoord2, Coord paramCoord3, Coord paramCoord4);
  
  public abstract float tcx(int paramInt);
  
  public abstract float tcy(int paramInt);
  
  public abstract GLState draw();
  
  public abstract GLState clip();
  
  public void render(GOut g, Coord c) {
    render(g, c, Coord.z, this.dim, this.dim);
  }
  
  public void crender(GOut g, Coord c, Coord ul, Coord sz, Coord tsz) {
    if (tsz.x == 0 || tsz.y == 0)
      return; 
    if (c.x >= ul.x + sz.x || c.y >= ul.y + sz.y || c.x + tsz.x <= ul.x || c.y + tsz.y <= ul.y)
      return; 
    Coord t = new Coord(c);
    Coord uld = new Coord(0, 0);
    Coord brd = new Coord(this.dim);
    Coord szd = new Coord(tsz);
    if (c.x < ul.x) {
      int pd = ul.x - c.x;
      t.x = ul.x;
      uld.x = pd * this.dim.x / tsz.x;
      szd.x -= pd;
    } 
    if (c.y < ul.y) {
      int pd = ul.y - c.y;
      t.y = ul.y;
      uld.y = pd * this.dim.y / tsz.y;
      szd.y -= pd;
    } 
    if (c.x + tsz.x > ul.x + sz.x) {
      int pd = c.x + tsz.x - ul.x + sz.x;
      szd.x -= pd;
      brd.x -= pd * this.dim.x / tsz.x;
    } 
    if (c.y + tsz.y > ul.y + sz.y) {
      int pd = c.y + tsz.y - ul.y + sz.y;
      szd.y -= pd;
      brd.y -= pd * this.dim.y / tsz.y;
    } 
    render(g, t, uld, brd, szd);
  }
  
  public void crender(GOut g, Coord c, Coord ul, Coord sz) {
    crender(g, c, ul, sz, this.dim);
  }
  
  public void dispose() {}
  
  public static final Tex empty = new Tex(Coord.z) {
      public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {}
      
      public float tcx(int x) {
        return 0.0F;
      }
      
      public float tcy(int y) {
        return 0.0F;
      }
      
      public GLState draw() {
        return null;
      }
      
      public GLState clip() {
        return null;
      }
    };
}
