package haven;

import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

public class SNoise3 {
   private final byte[] ptab = new byte[256];
   private final double[][] gtab = new double[][]{
      {1.0, 1.0, 0.0},
      {-1.0, 1.0, 0.0},
      {1.0, -1.0, 0.0},
      {-1.0, -1.0, 0.0},
      {1.0, 0.0, 1.0},
      {-1.0, 0.0, 1.0},
      {1.0, 0.0, -1.0},
      {-1.0, 0.0, -1.0},
      {0.0, 1.0, 1.0},
      {0.0, -1.0, 1.0},
      {0.0, 1.0, -1.0},
      {0.0, -1.0, -1.0}
   };

   public SNoise3(Random rnd) {
      for (int i = 0; i < 256; i++) {
         this.ptab[i] = (byte)i;
      }

      for (int i = 0; i < 256; i++) {
         int r = rnd.nextInt(256);
         byte t = this.ptab[i];
         this.ptab[i] = this.ptab[r];
         this.ptab[r] = t;
      }
   }

   public SNoise3(long seed) {
      this(new Random(seed));
   }

   public SNoise3() {
      this(new Random());
   }

   public double get(double r, double x, double y, double z) {
      x /= r;
      y /= r;
      z /= r;
      double s = (x + y + z) / 3.0;
      double i = Math.floor(x + s);
      double j = Math.floor(y + s);
      double k = Math.floor(z + s);
      double sx = (i + j + k) / 6.0;
      s = x - (i - sx);
      double dy = y - (j - sx);
      double dz = z - (k - sx);
      int j1;
      int k1;
      int i2;
      int j2;
      int k2;
      int i1;
      if (s >= dy && dy >= dz) {
         i1 = 1;
         j1 = 0;
         k1 = 0;
         i2 = 1;
         j2 = 1;
         k2 = 0;
      } else if (s >= dz && dz >= dy) {
         i1 = 1;
         j1 = 0;
         k1 = 0;
         i2 = 1;
         j2 = 0;
         k2 = 1;
      } else if (dz >= s && s >= dy) {
         i1 = 0;
         j1 = 0;
         k1 = 1;
         i2 = 1;
         j2 = 0;
         k2 = 1;
      } else if (dz >= dy && dy >= s) {
         i1 = 0;
         j1 = 0;
         k1 = 1;
         i2 = 0;
         j2 = 1;
         k2 = 1;
      } else if (dy >= dz && dz >= s) {
         i1 = 0;
         j1 = 1;
         k1 = 0;
         i2 = 0;
         j2 = 1;
         k2 = 1;
      } else {
         i1 = 0;
         j1 = 1;
         k1 = 0;
         i2 = 1;
         j2 = 1;
         k2 = 0;
      }

      double x1 = s - i1 + 0.16666666666666666;
      double y1 = dy - j1 + 0.16666666666666666;
      double z1 = dz - k1 + 0.16666666666666666;
      double x2 = s - i2 + 0.3333333333333333;
      double y2 = dy - j2 + 0.3333333333333333;
      double z2 = dz - k2 + 0.3333333333333333;
      double x3 = s - 0.5;
      double y3 = dy - 0.5;
      double z3 = dz - 0.5;
      int ip = (int)i;
      int jp = (int)j;
      int kp = (int)k;
      double[] g0 = this.gtab[(this.ptab[ip + this.ptab[jp + this.ptab[kp & 0xFF] & 0xFF] & 0xFF] & 255) % 12];
      double[] g1 = this.gtab[(this.ptab[ip + i1 + this.ptab[jp + j1 + this.ptab[kp + k1 & 0xFF] & 0xFF] & 0xFF] & 255) % 12];
      double[] g2 = this.gtab[(this.ptab[ip + i2 + this.ptab[jp + j2 + this.ptab[kp + k2 & 0xFF] & 0xFF] & 0xFF] & 255) % 12];
      double[] g3 = this.gtab[(this.ptab[ip + 1 + this.ptab[jp + 1 + this.ptab[kp + 1 & 0xFF] & 0xFF] & 0xFF] & 255) % 12];
      double n0 = 0.6 - s * s - dy * dy - dz * dz;
      double n1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
      double n2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
      double n3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
      double v = 0.0;
      if (n0 > 0.0) {
         v += n0 * n0 * n0 * n0 * (g0[0] * s + g0[1] * dy + g0[2] * dz);
      }

      if (n1 > 0.0) {
         v += n1 * n1 * n1 * n1 * (g1[0] * x1 + g1[1] * y1 + g1[2] * z1);
      }

      if (n2 > 0.0) {
         v += n2 * n2 * n2 * n2 * (g2[0] * x2 + g2[1] * y2 + g2[2] * z2);
      }

      if (n3 > 0.0) {
         v += n3 * n3 * n3 * n3 * (g3[0] * x3 + g3[1] * y3 + g3[2] * z3);
      }

      return Math.min(Math.max(v * 32.0, -1.0), 1.0);
   }

   public double getr(double lo, double hi, double r, double x, double y, double z) {
      return (this.get(r, x, y, z) * 0.5 + 0.5) * (hi - lo) + lo;
   }

   public int geti(int lo, int hi, double r, double x, double y, double z) {
      return Math.min((int)((this.get(r, x, y, z) * 0.5 + 0.5) * (hi - lo)), hi - lo - 1) + lo;
   }

   public static void main(String[] args) throws Exception {
      Coord sz = new Coord(512, 512);
      WritableRaster buf = PUtils.imgraster(sz);
      SNoise3 n = new SNoise3(Long.parseLong(args[0]));

      for (int y = 0; y < sz.y; y++) {
         for (int x = 0; x < sz.x; x++) {
            buf.setSample(x, y, 0, n.geti(0, 256, 128.0, x, y, 0.0));
            buf.setSample(x, y, 1, n.geti(0, 256, 128.0, x, y, 1428.0));
            buf.setSample(x, y, 2, n.geti(0, 256, 128.0, x, y, 5291.0));
            buf.setSample(x, y, 3, 255);
         }
      }

      ImageIO.write(PUtils.rasterimg(buf), "PNG", new File(args[1]));
   }
}
