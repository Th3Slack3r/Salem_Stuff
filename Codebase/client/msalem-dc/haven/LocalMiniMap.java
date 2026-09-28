package haven;

import haven.resutil.RidgeTile;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LocalMiniMap extends Window {
   public final MapView mv;
   private Coord cc = null;
   private LocalMiniMap.MapTile cur = null;
   private final Map<Coord, Defer.Future<LocalMiniMap.MapTile>> cache = new LinkedHashMap<Coord, Defer.Future<LocalMiniMap.MapTile>>(9, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<Coord, Defer.Future<LocalMiniMap.MapTile>> eldest) {
         if (this.size() > 75) {
            try {
               LocalMiniMap.MapTile t = eldest.getValue().get();
               t.img.dispose();
            } catch (RuntimeException var3) {
            }

            return true;
         } else {
            return false;
         }
      }
   };
   boolean dm;
   boolean rsm;
   Coord gzsz = new Coord(15, 15);
   Coord minsz = new Coord(125, 125);
   Coord off = new Coord(0, 0);
   Coord doff;

   private BufferedImage tileimg(int t, BufferedImage[] texes) throws Loading {
      BufferedImage img = texes[t];
      if (img == null) {
         Resource r = this.ui.sess.glob.map.tilesetr(t);
         if (r == null) {
            return null;
         }

         Resource.Image ir = r.layer(Resource.imgc);
         if (ir == null) {
            return null;
         }

         img = ir.img;
         texes[t] = img;
      }

      return img;
   }

   public BufferedImage drawmap(Coord ul, Coord sz) {
      BufferedImage[] texes = new BufferedImage[256];
      MCache m = this.ui.sess.glob.map;
      BufferedImage buf = TexI.mkbuf(sz);
      Coord c = new Coord();

      for (c.y = 0; c.y < sz.y; c.y++) {
         for (c.x = 0; c.x < sz.x; c.x++) {
            Coord c2 = ul.add(c);

            int t;
            try {
               t = m.gettile(c2);
            } catch (MCache.LoadingMap var11) {
               return null;
            }

            try {
               BufferedImage tex = this.tileimg(t, texes);
               int rgb = 0;
               if (tex != null) {
                  rgb = tex.getRGB(Utils.floormod(c.x, tex.getWidth()), Utils.floormod(c.y, tex.getHeight()));
               }

               buf.setRGB(c.x, c.y, rgb);
            } catch (Loading var12) {
               return null;
            }

            try {
               if (m.gettile(c2.add(-1, 0)) > t || m.gettile(c2.add(1, 0)) > t || m.gettile(c2.add(0, -1)) > t || m.gettile(c2.add(0, 1)) > t) {
                  buf.setRGB(c.x, c.y, Color.BLACK.getRGB());
               }
            } catch (MCache.LoadingMap var13) {
            }
         }
      }

      drawRidges(ul, sz, m, buf, c);
      return buf;
   }

   private static void drawRidges(Coord ul, Coord sz, MCache m, BufferedImage buf, Coord c) {
      for (c.y = 1; c.y < sz.y - 1; c.y++) {
         for (c.x = 1; c.x < sz.x - 1; c.x++) {
            int t = m.gettile(ul.add(c));
            Tiler tl = m.tiler(t);
            if (tl instanceof RidgeTile && ((RidgeTile)tl).ridgep(m, ul.add(c))) {
               for (int y = c.y; y <= c.y + 1; y++) {
                  for (int x = c.x; x <= c.x + 1; x++) {
                     int rgb = buf.getRGB(x, y);
                     rgb = rgb & 0xFF000000 | (rgb & 0xFF0000) >> 17 << 16 | (rgb & 0xFF00) >> 9 << 8 | (rgb & 0xFF) >> 1 << 0;
                     buf.setRGB(x, y, rgb);
                  }
               }
            }
         }
      }
   }

   public LocalMiniMap(Coord c, Coord sz, Widget parent, MapView mv) {
      super(c, sz, parent, "mmap");
      this.mv = mv;
   }

   public Coord p2c(Coord pc) {
      Coord cc = this.cc.add(this.off);
      return pc.div(MCache.tilesz).sub(cc).add(this.sz.div(2));
   }

   public Coord c2p(Coord c) {
      Coord cc = this.cc.add(this.off);
      return c.sub(this.sz.div(2)).add(cc).mul(MCache.tilesz).add(MCache.tilesz.div(2));
   }

   public void drawicons(GOut g) {
      OCache oc = this.ui.sess.glob.oc;
      synchronized (oc) {
         for (Gob gob : oc) {
            try {
               GobIcon icon = gob.getattr(GobIcon.class);
               if (icon != null) {
                  Coord gc = this.p2c(gob.rc);
                  Tex tex = icon.tex();
                  g.image(tex, gc.sub(tex.sz().div(2)));
               }
            } catch (Loading var10) {
            }
         }
      }
   }

   public Gob findicongob(Coord c) {
      OCache oc = this.ui.sess.glob.oc;
      synchronized (oc) {
         for (Gob gob : oc) {
            Gob var10000;
            try {
               GobIcon icon = gob.getattr(GobIcon.class);
               if (icon == null) {
                  continue;
               }

               Coord gc = this.p2c(gob.rc);
               Coord sz = icon.tex().sz();
               if (!c.isect(gc.sub(sz.div(2)), sz)) {
                  continue;
               }

               var10000 = gob;
            } catch (Loading var10) {
               continue;
            }

            return var10000;
         }

         return null;
      }
   }

   @Override
   public void tick(double dt) {
      Gob pl = this.ui.sess.glob.oc.getgob(this.mv.plgob);
      if (pl == null) {
         this.cc = null;
      } else {
         this.cc = pl.rc.div(MCache.tilesz);
      }
   }

   @Override
   public void draw(GOut g) {
      if (this.cc != null) {
         Coord plg = this.cc.div(MCache.cmaps);
         Coord cc = this.cc.add(this.off);
         Coord ulg = cc.div(MCache.cmaps);
         int dy = -cc.y + this.sz.y / 2;
         int dx = -cc.x + this.sz.x / 2;

         while (ulg.x * MCache.cmaps.x + dx > 0) {
            ulg.x--;
         }

         while (ulg.y * MCache.cmaps.y + dy > 0) {
            ulg.y--;
         }

         Coord cg = new Coord();
         synchronized (this.cache) {
            for (cg.y = ulg.y; cg.y * MCache.cmaps.y + dy < this.sz.y; cg.y++) {
               for (cg.x = ulg.x; cg.x * MCache.cmaps.x + dx < this.sz.x; cg.x++) {
                  Defer.Future<LocalMiniMap.MapTile> f = this.cache.get(cg);
                  final Coord tcg = new Coord(cg);
                  final Coord ul = cg.mul(MCache.cmaps);
                  Coord diff = cg.sub(plg).abs();
                  if (f == null && Math.max(diff.x, diff.y) <= 1) {
                     f = Defer.later(new Defer.Callable<LocalMiniMap.MapTile>() {
                        public LocalMiniMap.MapTile call() {
                           BufferedImage img = LocalMiniMap.this.drawmap(ul, MCache.cmaps);
                           return img == null ? null : new LocalMiniMap.MapTile(new TexI(img), ul, tcg);
                        }
                     });
                     this.cache.put(tcg, f);
                  }

                  if (f != null && f.done()) {
                     LocalMiniMap.MapTile mt = f.get();
                     if (mt == null) {
                        this.cache.put(cg, null);
                     } else {
                        Tex img = mt.img;
                        g.image(img, ul.add(cc.inv()).add(this.sz.div(2)));
                     }
                  }
               }
            }
         }

         Coord c0 = this.sz.div(2).sub(cc);
         synchronized (this.ui.sess.glob.party.memb) {
            try {
               Tex tx = MiniMap.plx.layer(Resource.imgc).tex();
               Coord negc = MiniMap.plx.layer(Resource.negc).cc;

               for (Party.Member memb : this.ui.sess.glob.party.memb.values()) {
                  Coord ptc = memb.getc();
                  if (ptc != null) {
                     ptc = c0.add(ptc.div(MCache.tilesz));
                     g.chcolor(memb.col);
                     g.image(tx, ptc.sub(negc));
                     g.chcolor();
                  }
               }
            } catch (Loading var17) {
            }
         }

         this.drawicons(g);
         Window.swbox.draw(g, Coord.z, this.sz);
      }
   }

   private Coord uitomap(Coord c) {
      return c.sub(this.sz.div(2)).add(this.off).mul(MCache.tilesz).add(this.mv.cc);
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      this.parent.setfocus(this);
      this.raise();
      Coord mc = this.uitomap(c);
      Gob gob = this.findicongob(c);
      if (gob != null) {
         this.mv.wdgmsg("click", new Object[]{this.rootpos().add(c), mc, button, this.ui.modflags(), 0, (int)gob.id, gob.rc, 0, -1});
         return true;
      } else if (button == 3) {
         this.dm = true;
         this.ui.grabmouse(this);
         this.doff = c;
         return true;
      } else {
         if (button == 1) {
            if (this.ui.modctrl) {
               this.mv.wdgmsg("click", new Object[]{this.rootpos().add(c), mc, button, 0});
               return true;
            }

            this.ui.grabmouse(this);
            this.doff = c;
            if (c.isect(this.sz.sub(this.gzsz), this.gzsz)) {
               this.rsm = true;
               return true;
            }
         }

         return super.mousedown(c, button);
      }
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      if (button == 2) {
         this.off.x = this.off.y = 0;
         return true;
      } else if (button == 3) {
         this.dm = false;
         this.ui.grabmouse(null);
         return true;
      } else {
         if (this.rsm) {
            this.ui.grabmouse(null);
            this.rsm = false;
         } else {
            super.mouseup(c, button);
         }

         return true;
      }
   }

   @Override
   public void mousemove(Coord c) {
      if (this.dm) {
         Coord d = c.sub(this.doff);
         this.off = this.off.sub(d);
         this.doff = c;
      } else {
         if (this.rsm) {
            Coord d = c.sub(this.doff);
            this.sz = this.sz.add(d);
            this.sz.x = Math.max(this.minsz.x, this.sz.x);
            this.sz.y = Math.max(this.minsz.y, this.sz.y);
            this.doff = c;
         } else {
            super.mousemove(c);
         }
      }
   }

   @Override
   public void wdgmsg(String msg, Object... args) {
   }

   public static class MapTile {
      public final Tex img;
      public final Coord ul;
      public final Coord c;

      public MapTile(Tex img, Coord ul, Coord c) {
         this.img = img;
         this.ul = ul;
         this.c = c;
      }
   }
}
