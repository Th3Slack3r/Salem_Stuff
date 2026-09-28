package haven;

public class Following extends Moving {
  long tgt;
  
  double lastv = 0.0D;
  
  Indir<Resource> xfres;
  
  String xfname;
  
  GLState xf = null;
  
  GLState lpxf = null;
  
  Gob lxfb = null;
  
  Skeleton.Pose lpose = null;
  
  public Following(Gob gob, long tgt, Indir<Resource> xfres, String xfname) {
    super(gob);
    this.tgt = tgt;
    this.xfres = xfres;
    this.xfname = xfname;
  }
  
  public Coord3f getc() {
    Gob tgt = this.gob.glob.oc.getgob(this.tgt);
    if (tgt == null)
      return this.gob.getrc(); 
    return tgt.getc();
  }
  
  public double getv() {
    Gob tgt = this.gob.glob.oc.getgob(this.tgt);
    if (tgt != null) {
      Moving mv = tgt.<Moving>getattr(Moving.class);
      if (mv == null) {
        this.lastv = 0.0D;
      } else {
        this.lastv = mv.getv();
      } 
    } 
    return this.lastv;
  }
  
  public Gob tgt() {
    return this.gob.glob.oc.getgob(this.tgt);
  }
  
  private Skeleton.Pose getpose(Gob tgt) {
    if (tgt == null)
      return null; 
    return ((Drawable)tgt.<Drawable>getattr(Drawable.class)).getpose();
  }
  
  public GLState xf() {
    synchronized (this) {
      Gob tgt = tgt();
      Skeleton.Pose cpose = getpose(tgt);
      GLState pxf = xf(tgt);
      if (this.xf == null || cpose != this.lpose || this.lpxf != pxf) {
        if (tgt == null) {
          this.xf = null;
          this.lpose = null;
          this.lxfb = null;
          this.lpxf = null;
          return null;
        } 
        Skeleton.BoneOffset bo = ((Resource)this.xfres.get()).<String, Skeleton.BoneOffset>layer(Skeleton.BoneOffset.class, this.xfname);
        if (bo == null)
          throw new RuntimeException("No such boneoffset in " + this.xfres.get() + ": " + this.xfname); 
        if (pxf != null) {
          this.xf = GLState.compose(new GLState[] { pxf, bo.forpose(cpose) });
        } else {
          this.xf = GLState.compose(new GLState[] { tgt.loc, bo.forpose(cpose) });
        } 
        this.lpxf = pxf;
        this.lxfb = tgt;
        this.lpose = cpose;
      } 
    } 
    return this.xf;
  }
  
  public static GLState xf(Gob gob) {
    if (gob == null)
      return null; 
    Following flw = gob.<Following>getattr(Following.class);
    if (flw == null)
      return null; 
    return flw.xf();
  }
}
