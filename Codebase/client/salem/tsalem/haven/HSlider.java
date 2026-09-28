package haven;

import java.awt.image.BufferedImage;

public class HSlider extends Widget {
  public static final Tex sflarp = Resource.loadtex("gfx/hud/sflarp");
  
  public static final Tex schain;
  
  public static final int h = (sflarp.sz()).y;
  
  public int val;
  
  public int min;
  
  public int max;
  
  private boolean drag = false;
  
  static {
    BufferedImage vc = Resource.loadimg("gfx/hud/schain");
    BufferedImage hc = TexI.mkbuf(new Coord(vc.getHeight(), vc.getWidth()));
    for (int y = 0; y < vc.getHeight(); y++) {
      for (int x = 0; x < vc.getWidth(); x++)
        hc.setRGB(y, x, vc.getRGB(x, y)); 
    } 
    schain = new TexI(hc);
  }
  
  public HSlider(Coord c, int w, Widget parent, int min, int max, int val) {
    super(c, new Coord(w, h), parent);
    this.val = val;
    this.min = min;
    this.max = max;
  }
  
  public boolean vis() {
    return (this.max > this.min);
  }
  
  public void draw(GOut g) {
    if (vis()) {
      int cy = ((sflarp.sz()).y - (schain.sz()).y) / 2;
      for (int x = 0; x < this.sz.x; x += (schain.sz()).x)
        g.image(schain, new Coord(x, cy)); 
      int fx = (this.sz.x - (sflarp.sz()).x) * this.val / (this.max - this.min);
      g.image(sflarp, new Coord(fx, 0));
    } 
  }
  
  public boolean mousedown(Coord c, int button) {
    if (button != 1)
      return false; 
    if (!vis())
      return false; 
    this.drag = true;
    this.ui.grabmouse(this);
    mousemove(c);
    return true;
  }
  
  public void mousemove(Coord c) {
    if (this.drag) {
      double a = (c.x - (sflarp.sz()).x / 2) / (this.sz.x - (sflarp.sz()).x);
      if (a < 0.0D)
        a = 0.0D; 
      if (a > 1.0D)
        a = 1.0D; 
      this.val = (int)Math.round(a * (this.max - this.min)) + this.min;
      changed();
    } 
  }
  
  public boolean mouseup(Coord c, int button) {
    if (button != 1)
      return false; 
    if (!this.drag)
      return false; 
    this.drag = false;
    this.ui.grabmouse(null);
    return true;
  }
  
  public void ch(int a) {
    int val = this.val + a;
    if (val > this.max)
      val = this.max; 
    if (val < this.min)
      val = this.min; 
    if (this.val != val) {
      this.val = val;
      changed();
    } 
  }
  
  public void changed() {}
  
  public void resize(int w) {
    resize(new Coord(w, h));
  }
}
