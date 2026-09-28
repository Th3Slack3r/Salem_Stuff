package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;

public class GobIcon extends GAttrib {
   public static final PUtils.Convolution filter = new PUtils.Hanning(1.0);
   private static final Map<Indir<Resource>, Tex> cache = new WeakHashMap<>();
   public final Indir<Resource> res;
   private Tex tex;

   public GobIcon(Gob g, Indir<Resource> res) {
      super(g);
      this.res = res;
   }

   public Tex tex() {
      if (this.tex == null) {
         synchronized (cache) {
            if (!cache.containsKey(this.res)) {
               Resource.Image img = this.res.get().layer(Resource.imgc);
               Tex tex = img.tex();
               if (tex.sz().x <= 20 && tex.sz().y <= 20) {
                  cache.put(this.res, tex);
               } else {
                  BufferedImage buf = img.img;
                  buf = PUtils.rasterimg(PUtils.blurmask2(buf.getRaster(), 1, 1, Color.BLACK));
                  buf = PUtils.convolvedown(buf, new Coord(20, 20), filter);
                  cache.put(this.res, new TexI(buf));
               }
            }

            this.tex = cache.get(this.res);
         }
      }

      return this.tex;
   }
}
