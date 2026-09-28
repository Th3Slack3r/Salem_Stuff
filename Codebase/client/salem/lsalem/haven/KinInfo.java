package haven;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class KinInfo extends GAttrib {
   public static final BufferedImage vlg = Resource.loadimg("gfx/hud/vilind");
   public static final Text.Foundry nfnd = new Text.Foundry("SansSerif", 10);
   public String name;
   public int group;
   public int type;
   public long seen = 0L;
   private Tex rnm = null;
   final PView.Draw2D fx = new PView.Draw2D() {
      @Override
      public void draw2d(GOut g) {
         if (KinInfo.this.gob.sc != null) {
            Coord sc = KinInfo.this.gob.sc.add(new Coord(KinInfo.this.gob.sczu.mul(15.0F)));
            if (sc.isect(Coord.z, g.sz)) {
               long now = System.currentTimeMillis();
               if (KinInfo.this.seen == 0L) {
                  KinInfo.this.seen = now;
               }

               int tm = (int)(now - KinInfo.this.seen);
               Color show = null;
               boolean auto = (KinInfo.this.type & 1) == 0;
               if (Config.isShowNames) {
                  show = Color.WHITE;
               } else if (auto && tm < 7500) {
                  show = Utils.clipcol(255, 255, 255, 255 - 255 * tm / 7500);
               }

               if (show != null) {
                  Tex t = KinInfo.this.rendered();
                  g.chcolor(show);
                  g.aimage(t, sc, 0.5, 1.0);
                  g.chcolor();
               }
            } else {
               KinInfo.this.seen = 0L;
            }
         }
      }
   };

   public KinInfo(Gob g, String name, int group, int type) {
      super(g);
      this.name = name;
      this.group = group;
      this.type = type;
   }

   public void update(String name, int group, int type) {
      this.name = name;
      this.group = group;
      this.type = type;
      this.rnm = null;
   }

   public Tex rendered() {
      if (this.rnm == null) {
         boolean hv = (this.type & 2) != 0;
         BufferedImage nm = null;
         if (this.name.length() > 0) {
            nm = Utils.outline2(nfnd.render(this.name, BuddyWnd.gc[this.group]).img, Utils.contrast(BuddyWnd.gc[this.group]));
         }

         int w = 0;
         int h = 0;
         if (nm != null) {
            w += nm.getWidth();
            if (nm.getHeight() > h) {
               h = nm.getHeight();
            }
         }

         if (hv) {
            w += vlg.getWidth() + 1;
            if (vlg.getHeight() > h) {
               h = vlg.getHeight();
            }
         }

         if (w == 0) {
            this.rnm = new TexIM(new Coord(1, 1));
         } else {
            BufferedImage buf = TexI.mkbuf(new Coord(w, h));
            Graphics g = buf.getGraphics();
            int x = 0;
            if (hv) {
               g.drawImage(vlg, x, h / 2 - vlg.getHeight() / 2, null);
               x += vlg.getWidth() + 1;
            }

            if (nm != null) {
               g.drawImage(nm, x, h / 2 - nm.getHeight() / 2, null);
               x += nm.getWidth();
            }

            g.dispose();
            this.rnm = new TexI(buf);
         }
      }

      return this.rnm;
   }
}
