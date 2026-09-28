package haven;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.media.opengl.GL2;

public class ILM extends TexRT {
  public static final BufferedImage ljusboll;
  
  OCache oc;
  
  TexI lbtex;
  
  Color amb;
  
  static {
    int sz = 200, min = 50;
    BufferedImage lb = new BufferedImage(200, 200, 2);
    Graphics g = lb.createGraphics();
    for (int y = 0; y < 200; y++) {
      for (int x = 0; x < 200; x++) {
        int gs;
        double dx = (100 - x);
        double dy = (100 - y);
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d > 100.0D) {
          gs = 255;
        } else if (d < 50.0D) {
          gs = 0;
        } else {
          gs = (int)((d - 50.0D) / 50.0D * 255.0D);
        } 
        gs /= 2;
        Color c = new Color(gs, gs, gs, 128 - gs);
        g.setColor(c);
        g.fillRect(x, y, 1, 1);
      } 
    } 
    ljusboll = lb;
  }
  
  public ILM(Coord sz, OCache oc) {
    super(sz);
    this.oc = oc;
    this.amb = new Color(0, 0, 0, 0);
    this.lbtex = new TexI(ljusboll);
  }
  
  protected Color ambcol() {
    return this.amb;
  }
  
  protected boolean subrend(GOut g) {
    GL2 gL2 = g.gl;
    gL2.glClearColor(255.0F, 255.0F, 255.0F, 255.0F);
    gL2.glClear(16384);
    synchronized (this.oc) {
      for (Gob gob : this.oc) {
        if (gob.sc == null)
          continue; 
        Lumin lum = gob.<Lumin>getattr(Lumin.class);
        if (lum == null)
          continue; 
        Coord sc = gob.sc.add(lum.off).add(-lum.sz, -lum.sz);
        g.image(this.lbtex, sc, new Coord(lum.sz * 2, lum.sz * 2));
      } 
    } 
    return true;
  }
  
  protected byte[] initdata() {
    return null;
  }
}
