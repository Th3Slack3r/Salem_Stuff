package haven;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

public class MenuGrid extends Widget {
   public static final Tex bg = Resource.loadtex("gfx/hud/invsq");
   public static final Coord bgsz = bg.sz().add(-1, -1);
   public final Glob.Pagina next = this.paginafor(Resource.load("gfx/hud/sc-next").loadwait());
   public final Glob.Pagina bk = this.paginafor(Resource.load("gfx/hud/sc-back").loadwait());
   public static final RichText.Foundry ttfnd = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
   private static Coord gsz = new Coord(4, 4);
   private Glob.Pagina cur;
   private Glob.Pagina pressed;
   private Glob.Pagina dragging;
   private Glob.Pagina[][] layout = new Glob.Pagina[gsz.x][gsz.y];
   private int curoff = 0;
   private int pagseq = 0;
   private boolean loading = true;
   private Map<Character, Glob.Pagina> hotmap = new TreeMap<>();
   private static Comparator<Glob.Pagina> sorter = new Comparator<Glob.Pagina>() {
      public int compare(Glob.Pagina a, Glob.Pagina b) {
         Resource.AButton aa = a.act();
         Resource.AButton ab = b.act();
         if (aa.ad.length == 0 && ab.ad.length > 0) {
            return -1;
         } else {
            return aa.ad.length > 0 && ab.ad.length == 0 ? 1 : aa.name.compareTo(ab.name);
         }
      }
   };
   private static Map<Glob.Pagina, Tex> glowmasks = new WeakHashMap<>();
   private Glob.Pagina curttp = null;
   private boolean curttl = false;
   private Text curtt = null;
   private long hoverstart;

   private boolean cons(Glob.Pagina p, Collection<Glob.Pagina> buf) {
      Glob.Pagina[] cp = new Glob.Pagina[0];
      Collection<Glob.Pagina> close = new HashSet<>();
      Collection<Glob.Pagina> open;
      synchronized (this.ui.sess.glob.paginae) {
         open = new LinkedList<>();

         for (Glob.Pagina pag : this.ui.sess.glob.paginae) {
            if (pag.newp == 2) {
               pag.newp = 0;
               pag.fstart = 0L;
            }

            open.add(pag);
         }

         for (Glob.Pagina pag : this.ui.sess.glob.pmap.values()) {
            if (pag.newp == 2) {
               pag.newp = 0;
               pag.fstart = 0L;
            }
         }
      }

      boolean ret = true;

      while (!open.isEmpty()) {
         Iterator<Glob.Pagina> iter = open.iterator();
         Glob.Pagina pagx = iter.next();
         iter.remove();

         try {
            Resource r = pagx.res();
            Resource.AButton ad = r.layer(Resource.action);
            if (ad == null) {
               throw new MenuGrid.PaginaException(pagx);
            }

            Glob.Pagina parent = this.paginafor(ad.parent);
            if (pagx.newp != 0 && parent != null && parent.newp == 0) {
               parent.newp = 2;
               parent.fstart = parent.fstart == 0L ? pagx.fstart : Math.min(parent.fstart, pagx.fstart);
            }

            if (parent == p) {
               buf.add(pagx);
            } else if (parent != null && !close.contains(parent) && !open.contains(parent)) {
               open.add(parent);
            }

            close.add(pagx);
         } catch (Loading var12) {
            ret = false;
         }
      }

      return ret;
   }

   public MenuGrid(Coord c, Widget parent) {
      super(c, Inventory.invsz(gsz), parent);
   }

   private void updlayout() {
      synchronized (this.ui.sess.glob.paginae) {
         List<Glob.Pagina> cur = new ArrayList<>();
         this.loading = !this.cons(this.cur, cur);
         Collections.sort(cur, sorter);
         int i = this.curoff;
         this.hotmap.clear();

         for (int y = 0; y < gsz.y; y++) {
            for (int x = 0; x < gsz.x; x++) {
               Glob.Pagina btn = null;
               if (this.cur != null && x == gsz.x - 1 && y == gsz.y - 1) {
                  btn = this.bk;
               } else if (cur.size() > gsz.x * gsz.y - 1 && x == gsz.x - 2 && y == gsz.y - 1) {
                  btn = this.next;
               } else if (i < cur.size()) {
                  Resource.AButton ad = cur.get(i).act();
                  if (ad.hk != 0) {
                     this.hotmap.put(Character.toUpperCase(ad.hk), cur.get(i));
                  }

                  btn = cur.get(i++);
               }

               this.layout[x][y] = btn;
            }
         }

         this.pagseq = this.ui.sess.glob.pagseq;
      }
   }

   private static Text rendertt(Resource res, boolean withpg) {
      Resource.AButton ad = res.layer(Resource.action);
      Resource.Pagina pg = res.layer(Resource.pagina);
      String tt = ad.name;
      int pos = tt.toUpperCase().indexOf(Character.toUpperCase(ad.hk));
      if (pos >= 0) {
         tt = tt.substring(0, pos) + "$col[255,255,0]{" + tt.charAt(pos) + "}" + tt.substring(pos + 1);
      } else if (ad.hk != 0) {
         tt = tt + " [" + ad.hk + "]";
      }

      if (withpg && pg != null) {
         tt = tt + "\n\n" + pg.text;
      }

      return ttfnd.render(tt, 300);
   }

   private Tex glowmask(Glob.Pagina pag) {
      Tex ret = glowmasks.get(pag);
      if (ret == null) {
         ret = new TexI(PUtils.glowmask(PUtils.glowmask(pag.res().layer(Resource.imgc).img.getRaster()), 4, new Color(32, 255, 32)));
         glowmasks.put(pag, ret);
      }

      return ret;
   }

   @Override
   public void draw(GOut g) {
      long now = System.currentTimeMillis();
      Inventory.invsq(g, Coord.z, gsz);

      for (int y = 0; y < gsz.y; y++) {
         for (int x = 0; x < gsz.x; x++) {
            Coord p = Inventory.sqoff(new Coord(x, y));
            Glob.Pagina btn = this.layout[x][y];
            if (btn != null) {
               Tex btex = btn.img.tex();
               g.image(btex, p);
               if (btn.meter > 0) {
                  double m = btn.meter / 1000.0;
                  if (btn.dtime > 0) {
                     m += (1.0 - m) * (now - btn.gettime) / btn.dtime;
                  }

                  m = Utils.clip(m, 0.0, 1.0);
                  g.chcolor(255, 255, 255, 128);
                  g.fellipse(p.add(Inventory.isqsz.div(2)), Inventory.isqsz.div(2), 90, (int)(90.0 + 360.0 * m));
                  g.chcolor();
               }

               if (btn.newp != 0) {
                  if (btn.fstart == 0L) {
                     btn.fstart = now;
                  } else {
                     double ph = (now - btn.fstart) / 1000.0 - (x + y * gsz.x) * 0.15 % 1.0;
                     if (ph < 1.25) {
                        g.chcolor(255, 255, 255, (int)(255.0 * (Math.cos(ph * Math.PI * 2.0) * -0.5 + 0.5)));
                        g.image(this.glowmask(btn), p.sub(4, 4));
                        g.chcolor();
                     } else {
                        g.chcolor(255, 255, 255, 128);
                        g.image(this.glowmask(btn), p.sub(4, 4));
                        g.chcolor();
                     }
                  }
               }

               if (btn == this.pressed) {
                  g.chcolor(new Color(0, 0, 0, 128));
                  g.frect(p, btex.sz());
                  g.chcolor();
               }
            }
         }
      }

      super.draw(g);
      if (this.dragging != null) {
         final Tex dt = this.dragging.img.tex();
         this.ui.drawafter(new UI.AfterDraw() {
            @Override
            public void draw(GOut g) {
               g.image(dt, MenuGrid.this.ui.mc.add(dt.sz().div(2).inv()));
            }
         });
      }
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      Glob.Pagina pag = this.bhit(c);
      long now = System.currentTimeMillis();
      if (pag != null && pag.act() != null) {
         if (prev != this) {
            this.hoverstart = now;
         }

         boolean ttl = now - this.hoverstart > 500L;
         if (pag != this.curttp || ttl != this.curttl) {
            this.curtt = rendertt(pag.res(), ttl);
            this.curttp = pag;
            this.curttl = ttl;
         }

         return this.curtt;
      } else {
         this.hoverstart = now;
         return "";
      }
   }

   private Glob.Pagina bhit(Coord c) {
      Coord bc = Inventory.sqroff(c);
      return bc.x >= 0 && bc.y >= 0 && bc.x < gsz.x && bc.y < gsz.y ? this.layout[bc.x][bc.y] : null;
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      Glob.Pagina h = this.bhit(c);
      if (button == 1 && h != null) {
         this.pressed = h;
         this.ui.grabmouse(this);
      }

      return true;
   }

   @Override
   public void mousemove(Coord c) {
      if (this.dragging == null && this.pressed != null) {
         Glob.Pagina h = this.bhit(c);
         if (h != this.pressed) {
            this.dragging = this.pressed;
         }
      }
   }

   private Glob.Pagina paginafor(Resource res) {
      return this.ui.sess.glob.paginafor(res);
   }

   private void use(Glob.Pagina r, boolean reset) {
      Collection<Glob.Pagina> sub = new LinkedList<>();
      Collection<Glob.Pagina> cur = new LinkedList<>();
      this.cons(r, sub);
      this.cons(this.cur, cur);
      if (sub.size() > 0) {
         this.cur = r;
         this.curoff = 0;
      } else if (r == this.bk) {
         this.cur = this.paginafor(this.cur.act().parent);
         this.curoff = 0;
      } else if (r == this.next) {
         if (this.curoff + 14 >= cur.size()) {
            this.curoff = 0;
         } else {
            this.curoff += 14;
         }
      } else {
         r.newp = 0;
         this.wdgmsg("act", r.act().ad);
         if (reset) {
            this.cur = null;
            this.curoff = 0;
         }
      }

      this.updlayout();
   }

   @Override
   public void tick(double dt) {
      if (this.loading || this.pagseq != this.ui.sess.glob.pagseq) {
         this.updlayout();
      }
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      Glob.Pagina h = this.bhit(c);
      if (button == 1) {
         if (this.dragging != null) {
            this.ui.dropthing(this.ui.root, this.ui.mc, this.dragging.res());
            this.dragging = this.pressed = null;
         } else if (this.pressed != null) {
            if (this.pressed == h) {
               this.use(h, false);
            }

            this.pressed = null;
         }

         this.ui.grabmouse(null);
      }

      return true;
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "goto") {
         String res = (String)args[0];
         if (res.equals("")) {
            this.cur = null;
         } else {
            this.cur = this.paginafor(Resource.load(res));
         }

         this.curoff = 0;
         this.updlayout();
      }
   }

   @Override
   public boolean globtype(char k, KeyEvent ev) {
      if (k == 27 && this.cur != null) {
         this.cur = null;
         this.curoff = 0;
         this.updlayout();
         return true;
      } else if (k == 'N' && this.layout[gsz.x - 2][gsz.y - 1] == this.next) {
         this.use(this.next, false);
         return true;
      } else {
         Glob.Pagina r = this.hotmap.get(Character.toUpperCase(k));
         if (r != null) {
            this.use(r, true);
            return true;
         } else {
            return false;
         }
      }
   }

   @Widget.RName("scm")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new MenuGrid(c, parent);
      }
   }

   public class PaginaException extends RuntimeException {
      public Glob.Pagina pag;

      public PaginaException(Glob.Pagina p) {
         super("Invalid pagina: " + p.res().name);
         this.pag = p;
      }
   }
}
