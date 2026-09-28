package haven;

public class Location extends Transform {
  public Location(Matrix4f xf) {
    super(xf);
  }
  
  public static class Chain extends GLState {
    public final Location loc;
    
    public final Chain p;
    
    private Matrix4f bk;
    
    private Chain(Location loc, Chain p) {
      this.loc = loc;
      this.p = p;
    }
    
    public Matrix4f fin(Matrix4f o) {
      if (this.p == null)
        return this.loc.fin(o); 
      return this.loc.fin(this.p.fin(o));
    }
    
    public void apply(GOut g) {
      this.bk = g.st.wxf;
      g.st.wxf = fin(g.st.wxf);
    }
    
    public void unapply(GOut g) {
      g.st.wxf = this.bk;
    }
    
    public void prep(GLState.Buffer b) {
      throw new RuntimeException("Location chains should not be applied directly.");
    }
    
    public String toString() {
      String ret = this.loc.toString();
      if (this.p != null)
        ret = ret + " -> " + this.p; 
      return ret;
    }
  }
  
  public void apply(GOut g) {
    throw new RuntimeException("Locations should not be applied directly.");
  }
  
  public void unapply(GOut g) {
    throw new RuntimeException("Locations should not be applied directly.");
  }
  
  public void prep(GLState.Buffer b) {
    Chain p = b.<Chain>get(PView.loc);
    b.put(PView.loc, new Chain(this, p));
  }
  
  public static Location scale(Coord3f c) {
    return new Location(Transform.makescale(new Matrix4f(), c));
  }
  
  public static Location xlate(Coord3f c) {
    return new Location(Transform.makexlate(new Matrix4f(), c));
  }
  
  public static Location rot(Coord3f axis, float angle) {
    return new Location(Transform.makerot(new Matrix4f(), axis.norm(), angle));
  }
  
  public static final Location onlyxl = new Location(Matrix4f.id) {
      private final Matrix4f lp = null;
      
      private Matrix4f fin;
      
      public Matrix4f fin(Matrix4f p) {
        if (p != this.lp) {
          this.fin = Matrix4f.identity();
          this.fin.m[12] = p.m[12];
          this.fin.m[13] = p.m[13];
          this.fin.m[14] = p.m[14];
        } 
        return this.fin;
      }
    };
}
