package haven;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.media.opengl.GL;

public class ILM extends TexRT {
   public static final BufferedImage ljusboll;
   OCache oc;
   TexI lbtex;
   Color amb;

   public ILM(Coord sz, OCache oc) {
      super(sz);
      this.oc = oc;
      this.amb = new Color(0, 0, 0, 0);
      this.lbtex = new TexI(ljusboll);
   }

   protected Color ambcol() {
      return this.amb;
   }

   @Override
   protected boolean subrend(GOut g) {
      GL gl = g.gl;
      gl.glClearColor(255.0F, 255.0F, 255.0F, 255.0F);
      gl.glClear(16384);
      synchronized (this.oc) {
         for (Gob gob : this.oc) {
            if (gob.sc != null) {
               Lumin lum = gob.getattr(Lumin.class);
               if (lum != null) {
                  Coord sc = gob.sc.add(lum.off).add(-lum.sz, -lum.sz);
                  g.image(this.lbtex, sc, new Coord(lum.sz * 2, lum.sz * 2));
               }
            }
         }

         return true;
      }
   }

   @Override
   protected byte[] initdata() {
      return null;
   }

   static {
      int sz = 200;
      int min = 50;
      BufferedImage lb = new BufferedImage(sz, sz, 2);
      Graphics g = lb.createGraphics();

      for (int y = 0; y < sz; y++) {
         for (int x = 0; x < sz; x++) {
            double dx = sz / 2 - x;
            double dy = sz / 2 - y;
            double d = Math.sqrt(dx * dx + dy * dy);
            int gs;
            if (d > sz / 2) {
               gs = 255;
            } else if (d < min) {
               gs = 0;
            } else {
               gs = (int)((d - min) / (sz / 2 - min) * 255.0);
            }

            gs /= 2;
            Color c = new Color(gs, gs, gs, 128 - gs);
            g.setColor(c);
            g.fillRect(x, y, 1, 1);
         }
      }

      ljusboll = lb;
   }
}
