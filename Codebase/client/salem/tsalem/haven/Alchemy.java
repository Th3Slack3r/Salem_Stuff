package haven;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Alchemy extends ItemInfo.Tip {
  public static final Color[] colors = new Color[] { new Color(192, 192, 255), new Color(6, 250, 55), new Color(230, 102, 47), new Color(225, 68, 255) };
  
  public static final String[] names = new String[] { "Æther", "Mercury", "Sulphur", "Lead" };
  
  public static final String[] tcolors;
  
  public final double[] a;
  
  static {
    String[] buf = new String[colors.length];
    for (int i = 0; i < colors.length; i++) {
      buf[i] = String.format("%d,%d,%d", new Object[] { Integer.valueOf(colors[i].getRed()), Integer.valueOf(colors[i].getGreen()), Integer.valueOf(colors[i].getBlue()) });
    } 
    tcolors = buf;
  }
  
  public Alchemy(ItemInfo.Owner owner, double aether, double merc, double sulf, double lead) {
    super(owner);
    this.a = new double[] { aether, merc, sulf, lead };
  }
  
  public BufferedImage longtip() {
    String[] arrayOfString = new String[4];
    for (int i = 0; i < 4; i++) {
      arrayOfString[i] = String.format("%s: $col[%s]{%.2f}", new Object[] { names[i], tcolors[i], Double.valueOf(this.a[i] * 100.0D) });
    } 
    return (RichText.render(String.format("%s\n  (%s, %s, %s)", (Object[])arrayOfString), 0, new Object[0])).img;
  }
  
  public BufferedImage smallmeter() {
    double max = 0.0D;
    for (int i = 0; i < 4; i++)
      max = Math.max(this.a[i], max); 
    BufferedImage buf = TexI.mkbuf(new Coord((int)(max * 50.0D), 12));
    Graphics g = buf.getGraphics();
    for (int j = 0; j < 4; j++) {
      g.setColor(colors[j]);
      g.fillRect(0, j * 3, (int)(this.a[j] * 50.0D), 3);
    } 
    g.dispose();
    return buf;
  }
  
  public double purity() {
    return this.a[0];
  }
  
  public String toString() {
    return String.format("%f-%f-%f-%f", new Object[] { Double.valueOf(this.a[0]), Double.valueOf(this.a[1]), Double.valueOf(this.a[2]), Double.valueOf(this.a[3]) });
  }
  
  public Color color() {
    return colors[0];
  }
}
