package haven;

import java.awt.image.BufferedImage;

public abstract class SIWidget extends Widget {
  private Tex surf = null;
  
  public SIWidget(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
  }
  
  protected abstract void draw(BufferedImage paramBufferedImage);
  
  public BufferedImage draw() {
    BufferedImage buf = TexI.mkbuf(this.sz);
    draw(buf);
    return buf;
  }
  
  public void draw(GOut g) {
    if (this.surf == null)
      this.surf = new TexI(draw()); 
    g.image(this.surf, Coord.z);
  }
  
  public void redraw() {
    if (this.surf != null)
      this.surf.dispose(); 
    this.surf = null;
  }
  
  public void resize(Coord sz) {
    super.resize(sz);
    redraw();
  }
}
