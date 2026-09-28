package haven;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Progress extends SIWidget {
  public static final BufferedImage bg = Resource.loadimg("gfx/hud/prog/bg");
  
  public static final BufferedImage fg = Resource.loadimg("gfx/hud/prog/fg");
  
  public static final BufferedImage cap = Resource.loadimg("gfx/hud/prog/cap");
  
  public static final Coord fgc = new Coord(2, 2);
  
  public int prog;
  
  public Progress(Coord c, Widget parent) {
    super(c, PUtils.imgsz(bg), parent);
  }
  
  public void draw(BufferedImage buf) {
    WritableRaster dst = buf.getRaster();
    PUtils.blit(dst, bg.getRaster(), Coord.z);
    int w = this.prog * fg.getWidth() / 100;
    WritableRaster bar = PUtils.copy(fg.getRaster());
    PUtils.gayblit(bar, 3, new Coord(w - cap.getWidth(), 0), cap.getRaster(), 0, Coord.z);
    for (int y = 0; y < bar.getHeight(); y++) {
      for (int x = w; x < bar.getWidth(); x++)
        bar.setSample(x, y, 3, 0); 
    } 
    PUtils.alphablit(dst, bar, fgc);
  }
  
  public void ch(int prog) {
    this.prog = prog;
    redraw();
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "p") {
      ch(((Integer)args[0]).intValue());
    } else {
      super.uimsg(msg, args);
    } 
  }
}
