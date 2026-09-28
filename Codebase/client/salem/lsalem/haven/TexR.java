package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

@Resource.LayerName("tex")
public class TexR extends Resource.Layer implements Resource.IDLayer<Integer> {
   private transient byte[] img;
   private transient byte[] mask;
   private final transient TexL tex;
   private final Coord off;
   private final Coord sz;
   public final int id;

   public TexR(Resource res, byte[] rbuf) {
      res.getClass();
      super();
      Message buf = new Message(0, rbuf);
      this.id = buf.int16();
      this.off = new Coord(buf.uint16(), buf.uint16());
      this.sz = new Coord(buf.uint16(), buf.uint16());
      this.tex = new TexR.Real();
      int minfilter = -1;
      int magfilter = -1;

      while (!buf.eom()) {
         int t = buf.uint8();
         switch (t) {
            case 0:
               this.img = buf.bytes(buf.int32());
               break;
            case 1:
               int ma = buf.uint8();
               this.tex.mipmap(new Mipmapper[]{Mipmapper.avg, Mipmapper.avg, Mipmapper.rnd, Mipmapper.cnt, Mipmapper.dav}[ma]);
               break;
            case 2:
               int magf = buf.uint8();
               magfilter = new int[]{9728, 9729}[magf];
               break;
            case 3:
               int minf = buf.uint8();
               minfilter = new int[]{9728, 9729, 9984, 9986, 9985, 9987}[minf];
               break;
            case 4:
               this.mask = buf.bytes(buf.int32());
               break;
            default:
               throw new Resource.LoadException("Unknown texture data part " + t + " in " + res.name, this.getres());
         }
      }

      if (magfilter == -1) {
         magfilter = 9729;
      }

      if (minfilter == -1) {
         minfilter = this.tex.mipmap == null ? 9729 : 9987;
      }

      this.tex.magfilter(magfilter);
      this.tex.minfilter(minfilter);
   }

   public TexGL tex() {
      return this.tex;
   }

   public Integer layerid() {
      return this.id;
   }

   @Override
   public void init() {
   }

   private class Real extends TexL {
      private Real() {
         super(TexR.this.sz);
      }

      private BufferedImage rd(byte[] data) {
         try {
            return ImageIO.read(new ByteArrayInputStream(data));
         } catch (IOException var3) {
            throw new RuntimeException("Invalid image data in " + TexR.this.getres().name, var3);
         }
      }

      @Override
      protected BufferedImage fill() {
         if (TexR.this.mask == null) {
            return this.rd(TexR.this.img);
         } else {
            BufferedImage col = this.rd(TexR.this.img);
            BufferedImage mask = this.rd(TexR.this.mask);
            Coord sz = Utils.imgsz(mask);
            BufferedImage ret = TexI.mkbuf(sz);
            Graphics g = ret.createGraphics();
            g.drawImage(col, 0, 0, sz.x, sz.y, null);
            Raster mr = mask.getRaster();
            if (mr.getNumBands() != 1) {
               throw new RuntimeException("Invalid separated alpha data in " + TexR.this.getres().name);
            } else {
               WritableRaster rr = ret.getRaster();

               for (int y = 0; y < sz.y; y++) {
                  for (int x = 0; x < sz.x; x++) {
                     rr.setSample(x, y, 3, mr.getSample(x, y, 0));
                  }
               }

               g.dispose();
               return ret;
            }
         }
      }

      @Override
      protected void fill(GOut g) {
         try {
            super.fill(g);
         } catch (Loading var3) {
            throw RenderList.RLoad.wrap(var3);
         }
      }

      @Override
      public String toString() {
         return "TexR(" + TexR.this.getres().name + ", " + TexR.this.id + ")";
      }
   }
}
