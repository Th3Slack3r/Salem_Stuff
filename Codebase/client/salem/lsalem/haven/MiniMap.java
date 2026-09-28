package haven;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.imageio.ImageIO;

public class MiniMap extends Widget {
   static Map<String, Tex> grids = new WeakHashMap<>();
   static Set<String> loading = new HashSet<>();
   static MiniMap.Loader loader = new MiniMap.Loader();
   public static final Tex bg = Resource.loadtex("gfx/hud/mmap/ptex");
   public static final Tex nomap = Resource.loadtex("gfx/hud/mmap/nomap");
   public static final Resource plx = Resource.load("gfx/hud/mmap/x");
   MapView mv;

   public MiniMap(Coord c, Coord sz, Widget parent, MapView mv) {
      super(c, sz, parent);
      this.mv = mv;
   }

   public static Tex getgrid(final String nm) {
      return AccessController.doPrivileged(new PrivilegedAction<Tex>() {
         public Tex run() {
            synchronized (MiniMap.grids) {
               if (MiniMap.grids.containsKey(nm)) {
                  return MiniMap.grids.get(nm);
               } else {
                  MiniMap.loader.req(nm);
                  return null;
               }
            }
         }
      });
   }

   @Override
   public void draw(GOut g) {
      Coord tc = this.mv.cc.div(MCache.tilesz);
      Coord ulg = tc.div(MCache.cmaps);

      while (ulg.x * MCache.cmaps.x - tc.x + this.sz.x / 2 > 0) {
         ulg.x--;
      }

      while (ulg.y * MCache.cmaps.y - tc.y + this.sz.y / 2 > 0) {
         ulg.y--;
      }

      boolean missing = false;
      g.image(bg, Coord.z);

      label95:
      for (int y = ulg.y; y * MCache.cmaps.y - tc.y + this.sz.y / 2 < this.sz.y; y++) {
         for (int x = ulg.x; x * MCache.cmaps.x - tc.x + this.sz.x / 2 < this.sz.x; x++) {
            Coord cg = new Coord(x, y);
            MCache.Grid grid;
            synchronized (this.ui.sess.glob.map.req) {
               synchronized (this.ui.sess.glob.map.grids) {
                  grid = this.ui.sess.glob.map.grids.get(cg);
                  if (grid == null) {
                     this.ui.sess.glob.map.request(cg);
                  }
               }
            }

            if (grid != null) {
               if (grid.mnm == null) {
                  missing = true;
                  break label95;
               }

               Tex tex = getgrid(grid.mnm);
               if (tex != null) {
                  g.image(tex, cg.mul(MCache.cmaps).add(tc.inv()).add(this.sz.div(2)));
               }
            }
         }
      }

      if (missing) {
         g.image(nomap, Coord.z);
      } else if (!plx.loading) {
         synchronized (this.ui.sess.glob.party.memb) {
            for (Party.Member m : this.ui.sess.glob.party.memb.values()) {
               Coord ptc;
               try {
                  ptc = m.getc();
               } catch (MCache.LoadingMap var14) {
                  ptc = null;
               }

               if (ptc != null) {
                  ptc = ptc.div(MCache.tilesz).add(tc.inv()).add(this.sz.div(2));
                  g.chcolor(m.col.getRed(), m.col.getGreen(), m.col.getBlue(), 128);
                  g.image(plx.layer(Resource.imgc).tex(), ptc.add(plx.layer(Resource.negc).cc.inv()));
                  g.chcolor();
               }
            }
         }
      }

      super.draw(g);
   }

   static class Loader implements Runnable {
      Thread me = null;

      private InputStream getreal(String nm) throws IOException {
         URL url = new URL(Config.mapurl, nm + ".png");
         URLConnection c = url.openConnection();
         c.addRequestProperty("User-Agent", "Haven/1.0");
         return c.getInputStream();
      }

      private InputStream getcached(String nm) throws IOException {
         if (ResCache.global == null) {
            throw new FileNotFoundException("No resource cache installed");
         } else {
            return ResCache.global.fetch("mm/" + nm);
         }
      }

      @Override
      public void run() {
         while (true) {
            try {
               String grid;
               synchronized (MiniMap.grids) {
                  grid = null;
                  Iterator img = MiniMap.loading.iterator();
                  if (img.hasNext()) {
                     String cg = (String)img.next();
                     grid = cg;
                  }
               }

               if (grid != null) {
                  try {
                     InputStream in;
                     try {
                        in = this.getcached(grid);
                     } catch (FileNotFoundException var34) {
                        in = this.getreal(grid);
                     }

                     BufferedImage img;
                     try {
                        img = ImageIO.read(in);
                     } finally {
                        Utils.readtileof(in);
                        in.close();
                     }

                     TexI var41 = new TexI(img);
                     synchronized (MiniMap.grids) {
                        MiniMap.grids.put(grid, var41);
                        MiniMap.loading.remove(grid);
                     }
                  } catch (IOException var35) {
                     synchronized (MiniMap.grids) {
                        MiniMap.grids.put(grid, null);
                        MiniMap.loading.remove(grid);
                     }
                  }
                  continue;
               }
            } finally {
               synchronized (this) {
                  this.me = null;
               }
            }

            return;
         }
      }

      void start() {
         synchronized (this) {
            if (this.me == null) {
               this.me = new HackThread(this, "Minimap loader");
               this.me.setDaemon(true);
               this.me.start();
            }
         }
      }

      void req(String nm) {
         synchronized (MiniMap.grids) {
            if (!MiniMap.loading.contains(nm)) {
               MiniMap.loading.add(nm);
               this.start();
            }
         }
      }
   }
}
