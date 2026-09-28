package haven;

public class SprDrawable extends Drawable {
  Sprite spr = null;
  
  public SprDrawable(Gob gob, Sprite spr) {
    super(gob);
    this.spr = spr;
  }
  
  public void setup(RenderList rl) {
    rl.add(this.spr, null);
  }
  
  public void ctick(int dt) {
    this.spr.tick(dt);
  }
  
  public Resource.Neg getneg() {
    return null;
  }
  
  public Resource getres() {
    return null;
  }
}
