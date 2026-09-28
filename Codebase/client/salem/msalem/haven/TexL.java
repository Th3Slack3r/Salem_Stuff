package haven;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import javax.media.opengl.GL2;

public abstract class TexL extends TexGL {
   protected Mipmapper mipmap = null;
   private Defer.Future<TexL.Prepared> decode = null;

   protected abstract BufferedImage fill();

   public TexL(Coord sz) {
      super(sz);
      if (sz.x != nextp2(sz.x) || sz.y != nextp2(sz.y)) {
         throw new RuntimeException("TexL does not support non-power-of-two textures");
      }
   }

   public void mipmap(Mipmapper mipmap) {
      this.mipmap = mipmap;
      this.dispose();
   }

   private Defer.Future<TexL.Prepared> prepare() {
      return Defer.later(new Defer.Callable<TexL.Prepared>() {
         public TexL.Prepared call() {
            return TexL.this.new Prepared();
         }
      });
   }

   @Override
   protected void fill(GOut g) {
      if (this.decode == null) {
         this.decode = this.prepare();
      }

      TexL.Prepared prep = this.decode.get();
      this.decode = null;
      GL2 gl = g.gl;
      gl.glPixelStorei(3317, 1);
      Coord cdim = this.tdim;

      for (int i = 0; i < prep.data.length; i++) {
         gl.glTexImage2D(3553, i, 6408, cdim.x, cdim.y, 0, prep.ifmt, 5121, ByteBuffer.wrap(prep.data[i]));
         cdim = Mipmapper.nextsz(cdim);
      }
   }

   private class Prepared {
      BufferedImage img = TexL.this.fill();
      byte[][] data;
      int ifmt;

      private Prepared() {
         if (!Utils.imgsz(this.img).equals(TexL.this.dim)) {
            throw new RuntimeException("Generated TexL image from " + TexL.this + " does not match declared size");
         } else {
            this.ifmt = TexI.detectfmt(this.img);
            LinkedList<byte[]> data = new LinkedList<>();
            if ((this.ifmt == 6407 || this.ifmt == 32992) && TexL.this.mipmap != null && !(TexL.this.mipmap instanceof Mipmapper.Mipmapper3)) {
               this.ifmt = -1;
            }

            if (this.ifmt != 6407 && this.ifmt != 32992) {
               byte[] pixels;
               if (this.ifmt != 6408 && this.ifmt != 32993) {
                  pixels = TexI.convert(this.img, TexL.this.dim);
                  this.ifmt = 6408;
               } else {
                  pixels = ((DataBufferByte)this.img.getRaster().getDataBuffer()).getData();
               }

               data.add(pixels);
               if (TexL.this.mipmap != null) {
                  for (Coord msz = TexL.this.dim; msz.x > 1 || msz.y > 1; msz = Mipmapper.nextsz(msz)) {
                     pixels = TexL.this.mipmap.gen4(msz, pixels, this.ifmt);
                     data.add(pixels);
                  }
               }
            } else {
               byte[] pixelsx = ((DataBufferByte)this.img.getRaster().getDataBuffer()).getData();
               data.add(pixelsx);
               if (TexL.this.mipmap != null) {
                  Coord msz = TexL.this.dim;

                  for (Mipmapper.Mipmapper3 alg = (Mipmapper.Mipmapper3)TexL.this.mipmap; msz.x > 1 || msz.y > 1; msz = Mipmapper.nextsz(msz)) {
                     pixelsx = alg.gen3(msz, pixelsx, this.ifmt);
                     data.add(pixelsx);
                  }
               }
            }

            this.data = data.toArray(new byte[0][]);
         }
      }
   }
}
