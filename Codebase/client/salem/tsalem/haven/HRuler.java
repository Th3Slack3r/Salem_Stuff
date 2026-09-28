package haven;

public class HRuler extends Widget {
  public static final Tex ext = Window.fbox.bt;
  
  public HRuler(Coord c, int w, Widget parent) {
    super(c, new Coord(w, (ext.sz()).y), parent);
  }
  
  public void draw(GOut g) {
    g.image(ext, Coord.z, this.sz);
  }
}
