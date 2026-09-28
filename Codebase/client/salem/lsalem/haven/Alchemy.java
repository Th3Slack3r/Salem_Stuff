package haven;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Alchemy extends ItemInfo.Tip {
   public static final Color[] colors = new Color[]{new Color(192, 192, 255), new Color(6, 250, 55), new Color(230, 102, 47), new Color(225, 68, 255)};
   public static final String[] names = new String[]{"Æther", "Mercury", "Sulphur", "Lead"};
   public static final String[] tcolors;
   public final double[] a;

   public Alchemy(ItemInfo.Owner owner, double aether, double merc, double sulf, double lead) {
      super(owner);
      this.a = new double[]{aether, merc, sulf, lead};
   }

   @Override
   public BufferedImage longtip() {
      Object[] p = new String[4];

      for (int i = 0; i < 4; i++) {
         p[i] = String.format("%s: $col[%s]{%.2f}", names[i], tcolors[i], this.a[i] * 100.0);
      }

      return RichText.render(String.format("%s\n  (%s, %s, %s)", p), 0).img;
   }

   public BufferedImage smallmeter() {
      double max = 0.0;

      for (int i = 0; i < 4; i++) {
         max = Math.max(this.a[i], max);
      }

      BufferedImage buf = TexI.mkbuf(new Coord((int)(max * 50.0), 12));
      Graphics g = buf.getGraphics();

      for (int i = 0; i < 4; i++) {
         g.setColor(colors[i]);
         g.fillRect(0, i * 3, (int)(this.a[i] * 50.0), 3);
      }

      g.dispose();
      return buf;
   }

   public double purity() {
      return this.a[0];
   }

   @Override
   public String toString() {
      return String.format("%f-%f-%f-%f", this.a[0], this.a[1], this.a[2], this.a[3]);
   }

   public Color color() {
      return colors[0];
   }

   static {
      String[] buf = new String[colors.length];

      for (int i = 0; i < colors.length; i++) {
         buf[i] = String.format("%d,%d,%d", colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue());
      }

      tcolors = buf;
   }
}
