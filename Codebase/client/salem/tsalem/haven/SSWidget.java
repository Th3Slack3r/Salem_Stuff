package haven;

import java.awt.Graphics;

public class SSWidget extends Widget {
  private final TexIM surf;
  
  public SSWidget(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
    this.surf = new TexIM(sz);
  }
  
  public void draw(GOut g) {
    g.image(this.surf, Coord.z);
  }
  
  public Graphics graphics() {
    Graphics g = this.surf.graphics();
    return g;
  }
  
  public void update() {
    this.surf.update();
  }
  
  public void clear() {
    this.surf.clear();
  }
}
