package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class IButton extends SSWidget {
  BufferedImage up;
  
  BufferedImage down;
  
  BufferedImage hover;
  
  boolean a = false;
  
  boolean h = false;
  
  public boolean recthit = false;
  
  @RName("ibtn")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new IButton(c, parent, Resource.loadimg((String)args[0]), Resource.loadimg((String)args[1]));
    }
  }
  
  public IButton(Coord c, Widget parent, BufferedImage up, BufferedImage down, BufferedImage hover) {
    super(c, Utils.imgsz(up), parent);
    this.up = up;
    this.down = down;
    this.hover = hover;
    render();
  }
  
  public IButton(Coord c, Widget parent, BufferedImage up, BufferedImage down) {
    this(c, parent, up, down, up);
  }
  
  public void render() {
    clear();
    Graphics g = graphics();
    if (this.a) {
      g.drawImage(this.down, 0, 0, null);
    } else if (this.h) {
      g.drawImage(this.hover, 0, 0, null);
    } else {
      g.drawImage(this.up, 0, 0, null);
    } 
    update();
  }
  
  public boolean checkhit(Coord c) {
    if (!c.isect(Coord.z, this.sz))
      return false; 
    if (this.recthit)
      return true; 
    if (this.up.getRaster().getNumBands() < 4)
      return true; 
    return (this.up.getRaster().getSample(c.x, c.y, 3) >= 128);
  }
  
  public void click() {
    wdgmsg("activate", new Object[0]);
  }
  
  public boolean mousedown(Coord c, int button) {
    if (button != 1)
      return false; 
    if (!checkhit(c))
      return false; 
    this.a = true;
    this.ui.grabmouse(this);
    render();
    return true;
  }
  
  public boolean mouseup(Coord c, int button) {
    if (this.a && button == 1) {
      this.a = false;
      this.ui.grabmouse(null);
      if (checkhit(c))
        click(); 
      render();
      return true;
    } 
    return false;
  }
  
  public void mousemove(Coord c) {
    boolean h = checkhit(c);
    if (h != this.h) {
      this.h = h;
      render();
    } 
  }
}
