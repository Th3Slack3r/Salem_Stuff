package haven;

public abstract class Drawable extends GAttrib {
  public Drawable(Gob gob) {
    super(gob);
  }
  
  public abstract void setup(RenderList paramRenderList);
  
  public abstract Resource getres();
  
  public abstract Resource.Neg getneg();
  
  public Skeleton.Pose getpose() {
    return null;
  }
}
