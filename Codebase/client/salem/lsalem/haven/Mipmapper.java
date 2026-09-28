package haven;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.Random;

public abstract class Mipmapper {
   public static final Mipmapper.Mipmapper3 avg = new Mipmapper.Mipmapper3() {
      @Override
      public byte[] gen4(Coord dim, byte[] data, int fmt) {
         int dst = dim.x * 4;
         dim = dim.div(2);
         boolean lx = false;
         boolean ly = false;
         if (dim.x < 1) {
            dim.x = 1;
            lx = true;
         }

         if (dim.y < 1) {
            dim.y = 1;
            ly = true;
         }

         byte[] ndata = new byte[dim.x * dim.y * 4];
         int[] r = new int[4];
         int[] g = new int[4];
         int[] b = new int[4];
         int[] a = new int[4];
         int na = 0;
         int da = 0;

         for (int y = 0; y < dim.y; y++) {
            for (int x = 0; x < dim.x; x++) {
               r[0] = data[da + 0] & 255;
               g[0] = data[da + 1] & 255;
               b[0] = data[da + 2] & 255;
               a[0] = data[da + 3] & 255;
               if (!lx) {
                  r[1] = data[da + 0 + 4] & 255;
                  g[1] = data[da + 1 + 4] & 255;
                  b[1] = data[da + 2 + 4] & 255;
                  a[1] = data[da + 3 + 4] & 255;
               } else {
                  r[1] = r[0];
                  g[1] = g[0];
                  b[1] = b[0];
                  a[1] = a[0];
               }

               if (!ly) {
                  r[2] = data[da + 0 + dst] & 255;
                  g[2] = data[da + 1 + dst] & 255;
                  b[2] = data[da + 2 + dst] & 255;
                  a[2] = data[da + 3 + dst] & 255;
               } else {
                  r[2] = r[0];
                  g[2] = g[0];
                  b[2] = b[0];
                  a[2] = a[0];
               }

               if (!lx && !ly) {
                  r[3] = data[da + 0 + dst + 4] & 255;
                  g[3] = data[da + 1 + dst + 4] & 255;
                  b[3] = data[da + 2 + dst + 4] & 255;
                  a[3] = data[da + 3 + dst + 4] & 255;
               } else if (!ly) {
                  r[3] = r[2];
                  g[3] = g[2];
                  b[3] = b[2];
                  a[3] = a[2];
               } else {
                  r[3] = r[1];
                  g[3] = g[1];
                  b[3] = b[1];
                  a[3] = a[1];
               }

               int n = 0;
               int cr = 0;
               int cg = 0;
               int cb = 0;

               for (int i = 0; i < 4; i++) {
                  if (a[i] >= 128) {
                     cr += r[i];
                     cg += g[i];
                     cb += b[i];
                     n++;
                  }
               }

               if (n <= 1) {
                  ndata[na + 3] = 0;
               } else {
                  ndata[na + 0] = (byte)(cr / n);
                  ndata[na + 1] = (byte)(cg / n);
                  ndata[na + 2] = (byte)(cb / n);
                  ndata[na + 3] = -1;
               }

               na += 4;
               da += lx ? 4 : 8;
            }

            da += ly ? 0 : dst;
         }

         return ndata;
      }

      @Override
      public byte[] gen3(Coord dim, byte[] data, int fmt) {
         int dst = dim.x * 3;
         dim = dim.div(2);
         boolean lx = false;
         boolean ly = false;
         if (dim.x < 1) {
            dim.x = 1;
            lx = true;
         }

         if (dim.y < 1) {
            dim.y = 1;
            ly = true;
         }

         byte[] ndata = new byte[dim.x * dim.y * 3];
         int[] r = new int[4];
         int[] g = new int[4];
         int[] b = new int[4];
         int na = 0;
         int da = 0;

         for (int y = 0; y < dim.y; y++) {
            for (int x = 0; x < dim.x; x++) {
               r[0] = data[da + 0] & 255;
               g[0] = data[da + 1] & 255;
               b[0] = data[da + 2] & 255;
               if (!lx) {
                  r[1] = data[da + 0 + 3] & 255;
                  g[1] = data[da + 1 + 3] & 255;
                  b[1] = data[da + 2 + 3] & 255;
               } else {
                  r[1] = r[0];
                  g[1] = g[0];
                  b[1] = b[0];
               }

               if (!ly) {
                  r[2] = data[da + 0 + dst] & 255;
                  g[2] = data[da + 1 + dst] & 255;
                  b[2] = data[da + 2 + dst] & 255;
               } else {
                  r[2] = r[0];
                  g[2] = g[0];
                  b[2] = b[0];
               }

               if (!lx && !ly) {
                  r[3] = data[da + 0 + dst + 3] & 255;
                  g[3] = data[da + 1 + dst + 3] & 255;
                  b[3] = data[da + 2 + dst + 3] & 255;
               } else if (!ly) {
                  r[3] = r[2];
                  g[3] = g[2];
                  b[3] = b[2];
               } else {
                  r[3] = r[1];
                  g[3] = g[1];
                  b[3] = b[1];
               }

               int cr = 0;
               int cg = 0;
               int cb = 0;

               for (int i = 0; i < 4; i++) {
                  cr += r[i];
                  cg += g[i];
                  cb += b[i];
               }

               ndata[na + 0] = (byte)(cr / 4);
               ndata[na + 1] = (byte)(cg / 4);
               ndata[na + 2] = (byte)(cb / 4);
               na += 3;
               da += lx ? 3 : 6;
            }

            da += ly ? 0 : dst;
         }

         return ndata;
      }
   };
   public static final Mipmapper rnd = new Mipmapper() {
      @Override
      public byte[] gen4(Coord dim, byte[] data, int fmt) {
         Random rnd = new Random();
         int dst = dim.x * 4;
         dim = dim.div(2);
         boolean lx = false;
         boolean ly = false;
         if (dim.x < 1) {
            dim.x = 1;
            lx = true;
         }

         if (dim.y < 1) {
            dim.y = 1;
            ly = true;
         }

         byte[] ndata = new byte[dim.x * dim.y * 4];
         int[] o = new int[]{0, lx ? 0 : 4, ly ? 0 : dst, lx ? dst : (ly ? 4 : dst + 4)};
         int na = 0;
         int da = 0;

         for (int y = 0; y < dim.y; y++) {
            for (int x = 0; x < dim.x; x++) {
               int so = da + o[rnd.nextInt(4)];
               ndata[na + 0] = data[so + 0];
               ndata[na + 1] = data[so + 1];
               ndata[na + 2] = data[so + 2];
               ndata[na + 3] = data[so + 3];
               na += 4;
               da += lx ? 4 : 8;
            }

            da += ly ? 0 : dst;
         }

         return ndata;
      }
   };
   public static final Mipmapper cnt = new Mipmapper() {
      @Override
      public byte[] gen4(Coord dim, byte[] data, int fmt) {
         int dst = dim.x * 4;
         dim = dim.div(2);
         boolean lx = false;
         boolean ly = false;
         if (dim.x < 1) {
            dim.x = 1;
            lx = true;
         }

         if (dim.y < 1) {
            dim.y = 1;
            ly = true;
         }

         byte[] ndata = new byte[dim.x * dim.y * 4];
         int[] r = new int[4];
         int[] g = new int[4];
         int[] b = new int[4];
         int[] a = new int[4];
         int na = 0;
         int da = 0;

         for (int y = 0; y < dim.y; y++) {
            for (int x = 0; x < dim.x; x++) {
               r[0] = data[da + 0] & 255;
               g[0] = data[da + 1] & 255;
               b[0] = data[da + 2] & 255;
               a[0] = data[da + 3] & 255;
               if (!lx) {
                  r[1] = data[da + 0 + 4] & 255;
                  g[1] = data[da + 1 + 4] & 255;
                  b[1] = data[da + 2 + 4] & 255;
                  a[1] = data[da + 3 + 4] & 255;
               } else {
                  r[1] = r[0];
                  g[1] = g[0];
                  b[1] = b[0];
                  a[1] = a[0];
               }

               if (!ly) {
                  r[2] = data[da + 0 + dst] & 255;
                  g[2] = data[da + 1 + dst] & 255;
                  b[2] = data[da + 2 + dst] & 255;
                  a[2] = data[da + 3 + dst] & 255;
               } else {
                  r[2] = r[0];
                  g[2] = g[0];
                  b[2] = b[0];
                  a[2] = a[0];
               }

               if (!lx && !ly) {
                  r[3] = data[da + 0 + dst + 4] & 255;
                  g[3] = data[da + 1 + dst + 4] & 255;
                  b[3] = data[da + 2 + dst + 4] & 255;
                  a[3] = data[da + 3 + dst + 4] & 255;
               } else if (!ly) {
                  r[3] = r[2];
                  g[3] = g[2];
                  b[3] = b[2];
                  a[3] = a[2];
               } else {
                  r[3] = r[1];
                  g[3] = g[1];
                  b[3] = b[1];
                  a[3] = a[1];
               }

               int n = 0;
               int cr = 0;
               int cg = 0;
               int cb = 0;

               for (int i = 0; i < 4; i++) {
                  if (a[i] >= 128) {
                     cr += r[i];
                     cg += g[i];
                     cb += b[i];
                     n++;
                  }
               }

               if (n <= 1) {
                  ndata[na + 3] = 0;
               } else {
                  cr /= n;
                  cg /= n;
                  cb /= n;
                  int md = -1;
                  int mi = -1;

                  for (int ix = 0; ix < 4; ix++) {
                     if (a[ix] >= 128) {
                        int d = Math.abs(r[ix] - cr) + Math.abs(g[ix] - cg) + Math.abs(b[ix] - cb);
                        if (md == -1 || d > md) {
                           md = d;
                           mi = ix;
                        }
                     }
                  }

                  ndata[na + 0] = (byte)r[mi];
                  ndata[na + 1] = (byte)g[mi];
                  ndata[na + 2] = (byte)b[mi];
                  ndata[na + 3] = -1;
               }

               na += 4;
               da += lx ? 4 : 8;
            }

            da += ly ? 0 : dst;
         }

         return ndata;
      }
   };
   public static final Mipmapper dav = new Mipmapper() {
      @Override
      public byte[] gen4(Coord dim, byte[] data, int fmt) {
         int dst = dim.x * 4;
         dim = dim.div(2);
         boolean lx = false;
         boolean ly = false;
         if (dim.x < 1) {
            dim.x = 1;
            lx = true;
         }

         if (dim.y < 1) {
            dim.y = 1;
            ly = true;
         }

         byte[] ndata = new byte[dim.x * dim.y * 4];
         int[] r = new int[4];
         int[] g = new int[4];
         int[] b = new int[4];
         int[] a = new int[4];
         int na = 0;
         int da = 0;

         for (int y = 0; y < dim.y; y++) {
            for (int x = 0; x < dim.x; x++) {
               r[0] = data[da + 0] & 255;
               g[0] = data[da + 1] & 255;
               b[0] = data[da + 2] & 255;
               a[0] = data[da + 3] & 255;
               if (!lx) {
                  r[1] = data[da + 0 + 4] & 255;
                  g[1] = data[da + 1 + 4] & 255;
                  b[1] = data[da + 2 + 4] & 255;
                  a[1] = data[da + 3 + 4] & 255;
               } else {
                  r[1] = r[0];
                  g[1] = g[0];
                  b[1] = b[0];
                  a[1] = a[0];
               }

               if (!ly) {
                  r[2] = data[da + 0 + dst] & 255;
                  g[2] = data[da + 1 + dst] & 255;
                  b[2] = data[da + 2 + dst] & 255;
                  a[2] = data[da + 3 + dst] & 255;
               } else {
                  r[2] = r[0];
                  g[2] = g[0];
                  b[2] = b[0];
                  a[2] = a[0];
               }

               if (!lx && !ly) {
                  r[3] = data[da + 0 + dst + 4] & 255;
                  g[3] = data[da + 1 + dst + 4] & 255;
                  b[3] = data[da + 2 + dst + 4] & 255;
                  a[3] = data[da + 3 + dst + 4] & 255;
               } else if (!ly) {
                  r[3] = r[2];
                  g[3] = g[2];
                  b[3] = b[2];
                  a[3] = a[2];
               } else {
                  r[3] = r[1];
                  g[3] = g[1];
                  b[3] = b[1];
                  a[3] = a[1];
               }

               int n = 0;
               int cr = 0;
               int cg = 0;
               int cb = 0;

               for (int i = 0; i < 4; i++) {
                  if (a[i] >= 128) {
                     cr += r[i];
                     cg += g[i];
                     cb += b[i];
                     n++;
                  }
               }

               if (n <= 1) {
                  ndata[na + 3] = 0;
               } else {
                  cr /= n;
                  cg /= n;
                  cb /= n;
                  int md = -1;
                  int mi = -1;

                  for (int ix = 0; ix < 4; ix++) {
                     if (a[ix] >= 128) {
                        int d = Math.abs(r[ix] - cr) + Math.abs(g[ix] - cg) + Math.abs(b[ix] - cb);
                        if (md == -1 || d < md) {
                           md = d;
                           mi = ix;
                        }
                     }
                  }

                  ndata[na + 0] = (byte)r[mi];
                  ndata[na + 1] = (byte)g[mi];
                  ndata[na + 2] = (byte)b[mi];
                  ndata[na + 3] = -1;
               }

               na += 4;
               da += lx ? 4 : 8;
            }

            da += ly ? 0 : dst;
         }

         return ndata;
      }
   };
   public static final Mipmapper lanczos = new Mipmapper() {
      final PUtils.Convolution filter = new PUtils.Lanczos(2.0);

      @Override
      public byte[] gen4(Coord dim, byte[] data, int fmt) {
         BufferedImage img = PUtils.rasterimg(
            Raster.createInterleavedRaster(new DataBufferByte(data, data.length), dim.x, dim.y, dim.x * 4, 4, new int[]{0, 1, 2, 3}, null)
         );
         dim = nextsz(dim);
         BufferedImage sm = PUtils.convolvedown(img, dim, this.filter);
         return ((DataBufferByte)sm.getRaster().getDataBuffer()).getData();
      }
   };

   public abstract byte[] gen4(Coord var1, byte[] var2, int var3);

   public static Coord nextsz(Coord dim) {
      Coord ndim = dim.div(2);
      ndim.x = Math.max(ndim.x, 1);
      ndim.y = Math.max(ndim.y, 1);
      return ndim;
   }

   public abstract static class Mipmapper3 extends Mipmapper {
      public abstract byte[] gen3(Coord var1, byte[] var2, int var3);
   }
}
