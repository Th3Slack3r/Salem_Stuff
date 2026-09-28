package haven;

import java.util.ArrayList;
import java.util.List;

public class ResDrawable extends Drawable {
  public final Indir<Resource> res;
  
  Message sdt;
  
  public Sprite spr = null;
  
  int delay = 0;
  
  boolean show_radius = false;
  
  boolean manualRadius = false;
  
  List<Gob.Overlay> radii = new ArrayList<>();
  
  public ResDrawable(Gob gob, Indir<Resource> res, Message sdt) {
    super(gob);
    this.res = res;
    this.sdt = sdt;
    try {
      init();
    } catch (Loading loading) {}
  }
  
  public ResDrawable(Gob gob, Resource res) {
    this(gob, res.indir(), new Message(0));
  }
  
  public Resource getres() {
    return this.res.get();
  }
  
  public void init() {
    if (this.spr != null)
      return; 
    this.spr = Sprite.create(this.gob, this.res.get(), this.sdt.clone());
    String name = ((Resource)this.res.get()).name;
    this.radii.addAll(ColoredRadius.getRadii(name, this.gob));
  }
  
  public void setup(RenderList rl) {
    try {
      init();
    } catch (Loading e) {
      return;
    } 
    checkRadius();
    this.spr.setup(rl);
  }
  
  private void checkRadius() {
    if (this.manualRadius) {
      if (!this.show_radius) {
        this.gob.ols.removeAll(this.radii);
        this.gob.ols.addAll(this.radii);
        this.show_radius = true;
      } 
      return;
    } 
    if (this.show_radius != Config.show_radius) {
      this.show_radius = Config.show_radius;
      this.gob.ols.removeAll(this.radii);
      if (Config.show_radius || this.show_radius)
        this.gob.ols.addAll(this.radii); 
    } 
  }
  
  public void ctick(int dt) {
    if (this.spr == null) {
      this.delay += dt;
    } else {
      this.spr.tick(this.delay + dt);
      this.delay = 0;
    } 
  }
  
  public void dispose() {
    if (this.spr != null)
      this.spr.dispose(); 
  }
  
  public Resource.Neg getneg() {
    return ((Resource)this.res.get()).<Resource.Neg>layer(Resource.negc);
  }
  
  public Skeleton.Pose getpose() {
    init();
    if (this.spr instanceof SkelSprite)
      return ((SkelSprite)this.spr).pose; 
    return null;
  }
}
