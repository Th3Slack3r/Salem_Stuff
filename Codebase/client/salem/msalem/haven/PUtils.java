package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import javax.imageio.ImageIO;

public class PUtils {
   public static final PUtils.Convolution box = new PUtils.Convolution() {
      @Override
      public double cval(double td) {
         return td >= -0.5 && td < 0.5 ? 1.0 : 0.0;
      }

      @Override
      public double support() {
         return 0.5;
      }
   };

   public static Coord imgsz(BufferedImage img) {
      return new Coord(img.getWidth(), img.getHeight());
   }

   public static Coord imgsz(Raster img) {
      return new Coord(img.getWidth(), img.getHeight());
   }

   public static WritableRaster byteraster(Coord sz, int bands) {
      return Raster.createInterleavedRaster(0, sz.x, sz.y, bands, null);
   }

   public static WritableRaster alpharaster(Coord sz) {
      return byteraster(sz, 1);
   }

   public static WritableRaster imgraster(Coord sz) {
      return byteraster(sz, 4);
   }

   public static WritableRaster copy(Raster src) {
      int w = src.getWidth();
      int h = src.getHeight();
      int b = src.getNumBands();
      WritableRaster ret = Raster.createInterleavedRaster(0, w, h, b, null);
      int[] buf = new int[w * h];

      for (int i = 0; i < b; i++) {
         ret.setSamples(0, 0, w, h, i, src.getSamples(0, 0, w, h, i, buf));
      }

      return ret;
   }

   public static BufferedImage rasterimg(WritableRaster img) {
      return new BufferedImage(TexI.glcm, img, false, null);
   }

   public static WritableRaster imggrow(WritableRaster img, int rad) {
      int h = img.getHeight();
      int w = img.getWidth();
      int[] buf = new int[w * h];
      int o = 0;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int m = 0;
            int u = Math.max(0, y - rad);
            int b = Math.min(h - 1, y + rad);
            int l = Math.max(0, x - rad);
            int r = Math.min(w - 1, x + rad);

            for (int y2 = u; y2 <= b; y2++) {
               for (int x2 = l; x2 <= r; x2++) {
                  m = Math.max(m, img.getSample(x2, y2, 0));
               }
            }

            buf[o++] = m;
         }
      }

      img.setSamples(0, 0, w, h, 0, buf);
      return img;
   }

   public static WritableRaster imgblur(WritableRaster img, int rad, double var) {
      int h = img.getHeight();
      int w = img.getWidth();
      double[] gk = new double[rad * 2 + 1];

      for (int i = 0; i <= rad; i++) {
         gk[rad + i] = gk[rad - i] = Math.exp(-0.5 * Math.pow(i / var, 2.0));
      }

      double s = 0.0;

      for (double cw : gk) {
         s += cw;
      }

      s = 1.0 / s;

      for (int i = 0; i <= rad * 2; i++) {
         gk[i] *= s;
      }

      int[] buf = new int[w * h];

      for (int band = 0; band < img.getNumBands(); band++) {
         int o = 0;

         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               double v = 0.0;
               int l = Math.max(0, x - rad);
               int r = Math.min(w - 1, x + rad);
               int x2 = l;

               for (int ks = l - (x - rad); x2 <= r; ks++) {
                  v += img.getSample(x2, y, band) * gk[ks];
                  x2++;
               }

               buf[o++] = (int)v;
            }
         }

         img.setSamples(0, 0, w, h, band, buf);
         o = 0;

         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               double v = 0.0;
               int u = Math.max(0, y - rad);
               int b = Math.min(h - 1, y + rad);
               int y2 = u;

               for (int ks = u - (y - rad); y2 <= b; ks++) {
                  v += img.getSample(x, y2, band) * gk[ks];
                  y2++;
               }

               buf[o++] = (int)v;
            }
         }

         img.setSamples(0, 0, w, h, band, buf);
      }

      return img;
   }

   public static WritableRaster alphadraw(WritableRaster dst, Raster alpha, Coord ul, Color col) {
      int r = col.getRed();
      int g = col.getGreen();
      int b = col.getBlue();
      int ba = col.getAlpha();
      int w = alpha.getWidth();
      int h = alpha.getHeight();

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int a = alpha.getSample(x, y, 0) * ba / 255;
            int dx = x + ul.x;
            int dy = y + ul.y;
            dst.setSample(dx, dy, 0, (r * a + dst.getSample(dx, dy, 0) * (255 - a)) / 255);
            dst.setSample(dx, dy, 1, (g * a + dst.getSample(dx, dy, 1) * (255 - a)) / 255);
            dst.setSample(dx, dy, 2, (b * a + dst.getSample(dx, dy, 2) * (255 - a)) / 255);
            dst.setSample(dx, dy, 3, Math.max(ba * a / 255, dst.getSample(dx, dy, 3)));
         }
      }

      return dst;
   }

   public static WritableRaster blit(WritableRaster dst, Raster src, Coord off) {
      int w = src.getWidth();
      int h = src.getHeight();
      int b = src.getNumBands();

      for (int y = 0; y < h; y++) {
         int dy = y + off.y;

         for (int x = 0; x < w; x++) {
            int dx = x + off.x;

            for (int i = 0; i < b; i++) {
               dst.setSample(dx, dy, i, src.getSample(x, y, i));
            }
         }
      }

      return dst;
   }

   public static WritableRaster gayblit(WritableRaster dst, int dband, Coord doff, Raster src, int sband, Coord soff) {
      if (doff.x < 0) {
         soff = soff.add(-doff.x, 0);
         doff = doff.add(-doff.x, 0);
      }

      if (doff.y < 0) {
         soff = soff.add(0, -doff.x);
         doff = doff.add(0, -doff.x);
      }

      int w = Math.min(src.getWidth() - soff.x, dst.getWidth() - doff.x);
      int h = Math.min(src.getHeight() - soff.y, dst.getHeight() - doff.y);

      for (int y = 0; y < h; y++) {
         int sy = y + soff.y;
         int dy = y + doff.y;

         for (int x = 0; x < w; x++) {
            int sx = x + soff.x;
            int dx = x + doff.x;
            dst.setSample(dx, dy, dband, dst.getSample(dx, dy, dband) * src.getSample(sx, sy, sband) / 255);
         }
      }

      return dst;
   }

   public static WritableRaster alphablit(WritableRaster dst, Raster src, Coord off) {
      int w = src.getWidth();
      int h = src.getHeight();

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int a = src.getSample(x, y, 3);
            int dx = x + off.x;
            int dy = y + off.y;
            dst.setSample(dx, dy, 0, (src.getSample(x, y, 0) * a + dst.getSample(dx, dy, 0) * (255 - a)) / 255);
            dst.setSample(dx, dy, 1, (src.getSample(x, y, 1) * a + dst.getSample(dx, dy, 1) * (255 - a)) / 255);
            dst.setSample(dx, dy, 2, (src.getSample(x, y, 2) * a + dst.getSample(dx, dy, 2) * (255 - a)) / 255);
            dst.setSample(dx, dy, 3, Math.max(src.getSample(x, y, 3), dst.getSample(dx, dy, 3)));
         }
      }

      return dst;
   }

   public static WritableRaster colmul(WritableRaster img, Color col) {
      int w = img.getWidth();
      int h = img.getHeight();
      int[] bm = new int[]{col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha()};

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            for (int b = 0; b < 4; b++) {
               img.setSample(x, y, b, img.getSample(x, y, b) * bm[b] / 255);
            }
         }
      }

      return img;
   }

   public static WritableRaster copyband(WritableRaster dst, int dband, Coord doff, Raster src, int sband, Coord soff, Coord sz) {
      dst.setSamples(doff.x, doff.y, sz.x, sz.y, dband, src.getSamples(soff.x, soff.y, sz.x, sz.y, sband, (int[])null));
      return dst;
   }

   public static WritableRaster copyband(WritableRaster dst, int dband, Coord doff, Raster src, int sband) {
      return copyband(dst, dband, doff, src, sband, Coord.z, imgsz(src));
   }

   public static WritableRaster copyband(WritableRaster dst, int dband, Raster src, int sband) {
      return copyband(dst, dband, Coord.z, src, sband);
   }

   public static WritableRaster blurmask(Raster img, int grad, int brad, Color col) {
      Coord marg = new Coord(grad + brad, grad + brad);
      Coord sz = imgsz(img).add(marg.mul(2));
      return alphadraw(imgraster(sz), imgblur(imggrow(copyband(alpharaster(sz), 0, marg, img, 3), grad), brad, brad), Coord.z, col);
   }

   public static WritableRaster blurmask2(Raster img, int grad, int brad, Color col) {
      return alphablit(blurmask(img, grad, brad, col), img, new Coord(grad + brad, grad + brad));
   }

   public static WritableRaster glowmask(Raster img) {
      Coord sz = imgsz(img);
      int nb = img.getNumBands();
      WritableRaster ret = alpharaster(sz);
      float[] hsv = new float[3];
      float max = 0.0F;

      for (int y = 0; y < sz.y; y++) {
         for (int x = 0; x < sz.x; x++) {
            Color.RGBtoHSB(img.getSample(x, y, 0), img.getSample(x, y, 1), img.getSample(x, y, 2), hsv);
            float a = nb > 3 ? img.getSample(x, y, 3) / 255.0F : 1.0F;
            float val = (1.0F - hsv[1]) * hsv[2] * a;
            max = Math.max(max, val);
         }
      }

      float imax = 1.0F / max;

      for (int y = 0; y < sz.y; y++) {
         for (int x = 0; x < sz.x; x++) {
            Color.RGBtoHSB(img.getSample(x, y, 0), img.getSample(x, y, 1), img.getSample(x, y, 2), hsv);
            float a = nb > 3 ? img.getSample(x, y, 3) / 255.0F : 1.0F;
            float val = (1.0F - hsv[1]) * hsv[2] * a;
            ret.setSample(x, y, 0, Math.min(Math.max((int)(Math.sqrt(val * imax) * 255.0), 0), 255));
         }
      }

      return ret;
   }

   public static BufferedImage glowmask(Raster img, int grad, Color col) {
      Coord sz = imgsz(img);
      Coord off = new Coord(grad, grad);
      WritableRaster buf = imgraster(sz.add(off.mul(2)));

      for (int i = 0; i < grad; i++) {
         alphadraw(buf, img, off, col);
         imgblur(buf, 2, 2.0);
      }

      return rasterimg(buf);
   }

   public static void dumpband(Raster img, int band) {
      int w = img.getWidth();
      int h = img.getHeight();

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            System.err.print((char)(97 + img.getSample(x, y, band) * 25 / 255));
         }

         System.err.println();
      }
   }

   public static BufferedImage monochromize(BufferedImage img, Color col) {
      Coord sz = Utils.imgsz(img);
      BufferedImage ret = TexI.mkbuf(sz);
      Raster src = img.getRaster();
      WritableRaster dst = ret.getRaster();
      boolean hasalpha = src.getNumBands() == 4;

      for (int y = 0; y < sz.y; y++) {
         for (int x = 0; x < sz.x; x++) {
            int r = src.getSample(x, y, 0);
            int g = src.getSample(x, y, 1);
            int b = src.getSample(x, y, 2);
            int a = hasalpha ? src.getSample(x, y, 3) : 255;
            int max = Math.max(r, Math.max(g, b));
            int min = Math.min(r, Math.min(g, b));
            int val = (max + min) / 2;
            dst.setSample(x, y, 0, col.getRed() * val / 255);
            dst.setSample(x, y, 1, col.getGreen() * val / 255);
            dst.setSample(x, y, 2, col.getBlue() * val / 255);
            dst.setSample(x, y, 3, col.getAlpha() * a / 255);
         }
      }

      return ret;
   }

   public static BufferedImage convolvedown(BufferedImage img, Coord tsz, PUtils.Convolution filter) {
      Raster in = img.getRaster();
      int w = in.getWidth();
      int h = in.getHeight();
      int nb = in.getNumBands();
      double xf = (double)w / tsz.x;
      double ixf = 1.0 / xf;
      double yf = (double)h / tsz.y;
      double iyf = 1.0 / yf;
      double[] ca = new double[nb];
      WritableRaster buf = byteraster(new Coord(tsz.x, h), nb);
      double support = filter.support();
      double[] cf = new double[tsz.x * (int)Math.ceil(2.0 * support * xf + 2.0)];
      int[] cl = new int[tsz.x];
      int[] cr = new int[tsz.x];
      int x = 0;

      for (int ci = 0; x < tsz.x; x++) {
         int si = ci;
         double wa = 0.0;
         cl[x] = Math.max((int)Math.floor((x + 0.5 - support) * xf), 0);
         cr[x] = Math.min((int)Math.ceil((x + 0.5 + support) * xf), w - 1);

         for (int sx = cl[x]; sx <= cr[x]; sx++) {
            double tx = (sx + 0.5) * ixf - x - 0.5;
            double fw = filter.cval(tx);
            wa += fw;
            cf[ci++] = fw;
         }

         for (double var44 = 1.0 / wa; si < ci; si++) {
            cf[si] *= var44;
         }
      }

      for (int y = 0; y < h; y++) {
         int xx = 0;

         for (int ci = 0; xx < tsz.x; xx++) {
            for (int b = 0; b < nb; b++) {
               ca[b] = 0.0;
            }

            for (int sx = cl[xx]; sx <= cr[xx]; sx++) {
               double fw = cf[ci++];

               for (int b = 0; b < nb; b++) {
                  ca[b] += in.getSample(sx, y, b) * fw;
               }
            }

            for (int b = 0; b < nb; b++) {
               buf.setSample(xx, y, b, Utils.clip((int)ca[b], 0, 255));
            }
         }
      }

      WritableRaster res = byteraster(tsz, nb);
      double[] cfx = new double[tsz.y * (int)Math.ceil(2.0 * support * yf + 2.0)];
      cr = new int[tsz.y];
      int[] cd = new int[tsz.y];
      int y = 0;

      for (int ci = 0; y < tsz.y; y++) {
         int si = ci;
         double wa = 0.0;
         cr[y] = Math.max((int)Math.floor((y + 0.5 - support) * yf), 0);
         cd[y] = Math.min((int)Math.ceil((y + 0.5 + support) * yf), h - 1);

         for (int sy = cr[y]; sy <= cd[y]; sy++) {
            double ty = (sy + 0.5) * iyf - y - 0.5;
            double fw = filter.cval(ty);
            wa += fw;
            cfx[ci++] = fw;
         }

         for (double var51 = 1.0 / wa; si < ci; si++) {
            cfx[si] *= var51;
         }
      }

      for (int xx = 0; xx < tsz.x; xx++) {
         int yx = 0;

         for (int ci = 0; yx < tsz.y; yx++) {
            for (int b = 0; b < nb; b++) {
               ca[b] = 0.0;
            }

            for (int sy = cr[yx]; sy <= cd[yx]; sy++) {
               double fw = cfx[ci++];

               for (int b = 0; b < nb; b++) {
                  ca[b] += buf.getSample(xx, sy, b) * fw;
               }
            }

            for (int b = 0; b < nb; b++) {
               res.setSample(xx, yx, b, Utils.clip((int)ca[b], 0, 255));
            }
         }
      }

      return new BufferedImage(img.getColorModel(), res, false, null);
   }

   public static void main(String[] args) throws Exception {
      PUtils.Convolution[] filters = new PUtils.Convolution[]{
         box, new PUtils.Hanning(1.0), new PUtils.Hanning(2.0), new PUtils.Hamming(1.0), new PUtils.Lanczos(2.0), new PUtils.Lanczos(3.0)
      };
      BufferedImage in = ImageIO.read(new File("/tmp/e.jpg"));
      Coord tsz = new Coord(300, 300);

      for (int i = 0; i < filters.length; i++) {
         long start = System.nanoTime();
         BufferedImage out = convolvedown(in, tsz, filters[i]);
         System.err.println(System.nanoTime() - start);
         ImageIO.write(out, "PNG", new File("/tmp/barda" + i + ".png"));
      }
   }

   public static class BlurFurn extends Text.Imager {
      public final int grad;
      public final int brad;
      public final Color col;

      public BlurFurn(Text.Furnace bk, int grad, int brad, Color col) {
         super(bk);
         this.grad = grad;
         this.brad = brad;
         this.col = col;
      }

      @Override
      public BufferedImage proc(Text text) {
         return PUtils.rasterimg(PUtils.blurmask2(text.img.getRaster(), this.grad, this.brad, this.col));
      }
   }

   public interface Convolution {
      double cval(double var1);

      double support();
   }

   public static class Hamming implements PUtils.Convolution {
      private final double sz;

      public Hamming(double sz) {
         this.sz = sz;
      }

      @Override
      public double cval(double td) {
         if (td == 0.0) {
            return 1.0;
         } else if (!(td < -this.sz) && !(td > this.sz)) {
            double tdp = td * Math.PI;
            return Math.sin(tdp) / tdp * (0.54 + 0.46 * Math.cos(tdp / this.sz));
         } else {
            return 0.0;
         }
      }

      @Override
      public double support() {
         return this.sz;
      }
   }

   public static class Hanning implements PUtils.Convolution {
      private final double sz;

      public Hanning(double sz) {
         this.sz = sz;
      }

      @Override
      public double cval(double td) {
         if (td == 0.0) {
            return 1.0;
         } else if (!(td < -this.sz) && !(td > this.sz)) {
            double tdp = td * Math.PI;
            return Math.sin(tdp) / tdp * (0.5 + 0.5 * Math.cos(tdp / this.sz));
         } else {
            return 0.0;
         }
      }

      @Override
      public double support() {
         return this.sz;
      }
   }

   public static class Lanczos implements PUtils.Convolution {
      private final double sz;

      public Lanczos(double sz) {
         this.sz = sz;
      }

      @Override
      public double cval(double td) {
         if (td == 0.0) {
            return 1.0;
         } else if (!(td < -this.sz) && !(td > this.sz)) {
            double tdp = td * Math.PI;
            double wtdp = tdp / this.sz;
            return Math.sin(tdp) / tdp * (Math.sin(wtdp) / wtdp);
         } else {
            return 0.0;
         }
      }

      @Override
      public double support() {
         return this.sz;
      }
   }
}
