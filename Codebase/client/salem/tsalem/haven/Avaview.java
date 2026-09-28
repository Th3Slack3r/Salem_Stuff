package haven;

import java.awt.Color;
import java.util.List;

public class Avaview extends PView {
  public static final Tex missing = Resource.loadtex("gfx/hud/equip/missing");
  
  public static final Coord dasz = missing.sz();
  
  public long avagob;
  
  private Composited comp;
  
  private List<Composited.MD> cmod = null;
  
  private List<Composited.ED> cequ = null;
  
  private final String camnm;
  
  private boolean missed;
  
  private Camera cam;
  
  private Composite lgc;
  
  public Avaview(Coord c, Coord sz, Widget parent, long avagob, String camnm) {
    super(c, sz, parent);
    this.missed = false;
    this.cam = null;
    this.lgc = null;
    this.camnm = camnm;
    this.avagob = avagob;
  }
  
  private Composite getgcomp() {
    Gob gob = this.ui.sess.glob.oc.getgob(this.avagob);
    if (gob == null)
      return null; 
    Drawable d = gob.<Drawable>getattr(Drawable.class);
    if (!(d instanceof Composite))
      return null; 
    Composite gc = (Composite)d;
    if (gc.comp == null)
      return null; 
    return gc;
  }
  
  private void initcomp(Composite gc) {
    if (this.comp == null || this.comp.skel != gc.comp.skel)
      this.comp = new Composited(gc.comp.skel); 
  }
  
  private Camera makecam(Composite gc, String camnm) {
    if (this.comp == null)
      throw new Loading(); 
    Skeleton.BoneOffset bo = ((Resource)gc.base.get()).<String, Skeleton.BoneOffset>layer(Skeleton.BoneOffset.class, camnm);
    if (bo == null)
      throw new Loading(); 
    GLState.Buffer buf = new GLState.Buffer(null);
    bo.forpose(this.comp.pose).prep(buf);
    return new LocationCam(buf.<Location.Chain>get(PView.loc));
  }
  
  protected Camera camera() {
    Composite gc = getgcomp();
    if (gc == null)
      throw new Loading(); 
    initcomp(gc);
    if (this.cam == null || gc != this.lgc)
      this.cam = makecam(this.lgc = gc, this.camnm); 
    return this.cam;
  }
  
  protected void setup(RenderList rl) {
    Composite gc = getgcomp();
    if (gc == null) {
      this.missed = true;
      return;
    } 
    initcomp(gc);
    if (gc.comp.cmod != this.cmod)
      this.comp.chmod(this.cmod = gc.comp.cmod); 
    if (gc.comp.cequ != this.cequ)
      this.comp.chequ(this.cequ = gc.comp.cequ); 
    rl.add(this.comp, null);
    rl.add(new DirLight(Color.WHITE, Color.WHITE, Color.WHITE, (new Coord3f(1.0F, 1.0F, 1.0F)).norm()), null);
  }
  
  public void tick(double dt) {
    if (this.comp != null)
      this.comp.tick((int)(dt * 1000.0D)); 
  }
  
  public void draw(GOut g) {
    this.missed = false;
    try {
      super.draw(g);
    } catch (Loading e) {
      this.missed = true;
    } 
    if (this.missed)
      g.image(missing, Coord.z, this.sz); 
  }
}
