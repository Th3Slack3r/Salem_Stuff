package haven;

import haven.plugins.XTendedPaginae;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
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
import java.util.Map.Entry;
import org.ender.wiki.Item;
import org.ender.wiki.Wiki;

public class MenuGrid extends Widget {
   public static final Tex bg = Resource.loadtex("gfx/hud/invsq");
   public static final Coord bgsz = bg.sz().add(-1, -1);
   private final Glob.Pagina CRAFT;
   public final Glob.Pagina next = this.paginafor(Resource.load("gfx/hud/sc-next").loadwait());
   public final Glob.Pagina bk = this.paginafor(Resource.load("gfx/hud/sc-back").loadwait());
   public static final RichText.Foundry ttfnd = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
   private static Coord gsz = new Coord(4, 4);
   public Glob.Pagina cur;
   public Glob.Pagina pressed;
   public Glob.Pagina dragging;
   public Glob.Pagina[][] layout = new Glob.Pagina[gsz.x][gsz.y];
   private int curoff = 0;
   private int pagseq = 0;
   private boolean loading = true;
   private Map<Character, Glob.Pagina> hotmap = new TreeMap<>();
   public static Comparator<Glob.Pagina> sorter = new Comparator<Glob.Pagina>() {
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
   Item ttitem = null;
   private Tex curtt = null;
   private long hoverstart;

   public boolean cons(Glob.Pagina p, Collection<Glob.Pagina> buf) {
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
      this.ui.mnu = this;
      this.CRAFT = this.paginafor(Resource.load("paginae/act/craft"));
      XTendedPaginae.loadXTendedPaginae(this.ui);
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

   public static BufferedImage getXPgain(String name) {
      try {
         Item itm = Wiki.get(name);
         if (itm != null) {
            Map<String, Integer> props = itm.attgive;
            if (props != null) {
               int n = props.size();
               String[] attrs = new String[n];
               int[] exp = new int[n];
               n = 0;

               for (String attr : props.keySet()) {
                  Integer val = props.get(attr);
                  attrs[n] = attr;
                  exp[n] = val;
                  n++;
               }

               Inspiration i = new Inspiration(null, 0, attrs, exp);
               return i.longtip();
            }
         }
      } catch (Exception var9) {
         var9.printStackTrace(System.out);
      }

      return null;
   }

   public static float[] safeFloat(Float[] input, float[] def) {
      if (input == null) {
         return def;
      } else {
         int n = input.length;
         float[] output = new float[n];

         for (int i = 0; i < n; i++) {
            if (input[i] != null) {
               output[i] = input[i];
            } else {
               output[i] = 0.0F;
            }
         }

         return output;
      }
   }

   public static BufferedImage getFood(String name) {
      try {
         Item itm = Wiki.get(name);
         if (itm != null && itm.food != null) {
            Map<String, Float[]> food = itm.food;
            float[] def = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
            String[] var10000 = new String[]{"Blood", "Phlegm", "Yellow Bile", "Black Bile"};
            float[] heal = safeFloat(food.get("Heals"), def);
            float[] gmax = safeFloat(food.get("GluttonMax"), def);
            float[] gmin = safeFloat(food.get("GluttonMin"), def);
            int[] low = new int[4];
            int[] high = new int[4];
            int[] tempers = new int[4];

            for (int i = 0; i < 4; i++) {
               tempers[i] = (int)(1000.0F * heal[i]);
               high[i] = (int)(1000.0F * gmax[i]);
               low[i] = (int)(1000.0F * gmin[i]);
            }

            FoodInfo fi = new FoodInfo(null, tempers);
            int ft = itm.food_full * 60;
            GobbleInfo gi = new GobbleInfo(null, low, high, new int[0], ft, new LinkedList<>());
            String uses = String.format("Uses: %d\n", itm.food_uses);
            BufferedImage uses_img = RichText.stdf.render(uses).img;
            Color debuff_color = new Color(255, 192, 192);
            Color undebuff_color = new Color(192, 255, 192);
            BufferedImage currimg = ItemInfo.catimgs(3, fi.longtip(), uses_img, gi.longtip());

            for (Entry<String, Integer[]> e : itm.food_reduce.entrySet()) {
               Integer[] values = e.getValue();
               BufferedImage head = RichText.render(String.format("-%d%%", values[0]), debuff_color).img;
               Resource iconres = null;
               if (Wiki.buffmap.containsKey(e.getKey())) {
                  iconres = Resource.load("gfx/invobjs/" + Wiki.buffmap.get(e.getKey()));
               } else {
                  System.out.println("Wiki food translation key not available: " + e.getKey());
               }

               if (iconres != null) {
                  BufferedImage icon = PUtils.convolvedown(iconres.layer(Resource.imgc).img, new Coord(16, 16), GobIcon.filter);
                  String classname = iconres.layer(Resource.tooltip).t;
                  BufferedImage tail = RichText.render(classname + " [" + values[1] + "%]").img;
                  currimg = ItemInfo.catimgs(0, currimg, ItemInfo.catimgsh(0, head, icon, tail));
               } else {
                  String classname = e.getKey() + " [" + values[1] + "%]";
                  BufferedImage tail = RichText.render(classname).img;
                  currimg = ItemInfo.catimgs(0, currimg, ItemInfo.catimgsh(0, head, tail));
               }
            }

            for (Entry<String, Integer[]> e : itm.food_restore.entrySet()) {
               Integer[] valuesx = e.getValue();
               BufferedImage headx = RichText.render(String.format("+%d%%", valuesx[0]), undebuff_color).img;
               Resource iconresx = Resource.load("gfx/invobjs/" + Wiki.buffmap.get(e.getKey()));
               if (iconresx != null) {
                  BufferedImage icon = PUtils.convolvedown(iconresx.layer(Resource.imgc).img, new Coord(16, 16), GobIcon.filter);
                  String classname = iconresx.layer(Resource.tooltip).t;
                  BufferedImage tail = RichText.render(classname + " [" + valuesx[1] + "%]").img;
                  currimg = ItemInfo.catimgs(0, currimg, ItemInfo.catimgsh(0, headx, icon, tail));
               } else {
                  String classname = e.getKey() + " [" + valuesx[1] + "%]";
                  BufferedImage tail = RichText.render(classname).img;
                  currimg = ItemInfo.catimgs(0, currimg, ItemInfo.catimgsh(0, headx, tail));
               }
            }

            return currimg;
         }
      } catch (Loading var27) {
      } catch (Exception var28) {
         var28.printStackTrace(System.out);
      }

      return null;
   }

   public static BufferedImage getArtifact(String name) {
      try {
         Item itm = Wiki.get(name);
         if (itm != null && itm.art_profs != null && itm.art_profs.length != 0) {
            Resource res = Resource.load("ui/tt/slot");
            if (res == null) {
               return null;
            } else {
               ItemInfo.InfoFactory f = res.layer(Resource.CodeEntry.class).get(ItemInfo.InfoFactory.class);
               Session sess = UI.instance.sess;
               int rid = sess.getresid("ui/tt/dattr");
               if (rid == 0) {
                  return null;
               } else {
                  Object[] bonuses = itm.getArtBonuses();
                  bonuses[0] = rid;
                  Object[] args = new Object[4 + itm.art_profs.length];
                  int i = 0;
                  args[i++] = 0;
                  args[i++] = itm.art_pmin;
                  args[i++] = itm.art_pmax;

                  for (String prof : itm.art_profs) {
                     args[i++] = CharWnd.attrbyname(prof);
                  }

                  args[i++] = new Object[]{bonuses};
                  ItemInfo.Tip tip = (ItemInfo.Tip)f.build(sess, args);
                  List<ItemInfo> list = new LinkedList<>();
                  list.add(tip);
                  return tip.longtip();
               }
            }
         } else {
            return null;
         }
      } catch (Exception var13) {
         var13.printStackTrace(System.out);
         return null;
      }
   }

   public static BufferedImage getSlots(String name) {
      try {
         Item itm = Wiki.get(name);
         if (itm != null && itm.cloth_slots != 0) {
            Object[] args = new Object[5];
            int i = 0;
            args[i++] = 0;
            args[i++] = itm.cloth_slots;
            args[i++] = 0;
            args[i++] = 25;
            args[i++] = 0;
            Resource res = Resource.load("ui/tt/slots");
            if (res == null) {
               return null;
            } else {
               ItemInfo.InfoFactory f = res.layer(Resource.CodeEntry.class).get(ItemInfo.InfoFactory.class);
               ItemInfo.Tip tip = (ItemInfo.Tip)f.build(null, args);
               return tip.longtip();
            }
         } else {
            return null;
         }
      } catch (Exception var7) {
         var7.printStackTrace(System.out);
         return null;
      }
   }

   public static Tex rendertt(Resource res, boolean withpg, boolean hotkey) {
      Resource.AButton ad = res.layer(Resource.action);
      Resource.Pagina pg = res.layer(Resource.pagina);
      String tt = ad.name;
      BufferedImage xp = null;
      BufferedImage food = null;
      BufferedImage slots = null;
      BufferedImage art = null;
      if (hotkey) {
         int pos = tt.toUpperCase().indexOf(Character.toUpperCase(ad.hk));
         if (pos >= 0) {
            tt = tt.substring(0, pos) + "$col[255,255,0]{" + tt.charAt(pos) + "}" + tt.substring(pos + 1);
         } else if (ad.hk != 0) {
            tt = tt + " [" + ad.hk + "]";
         }
      }

      if (withpg) {
         if (pg != null) {
            tt = tt + "\n\n" + pg.text;
         }

         xp = getXPgain(ad.name);
         food = getFood(ad.name);
         slots = getSlots(ad.name);
         art = getArtifact(ad.name);
      }

      BufferedImage img = ttfnd.render(tt, 300).img;
      if (xp != null) {
         img = ItemInfo.catimgs(3, img, xp);
      }

      if (food != null) {
         img = ItemInfo.catimgs(3, img, food);
      }

      if (slots != null) {
         img = ItemInfo.catimgs(3, img, slots);
      }

      if (art != null) {
         img = ItemInfo.catimgs(3, img, art);
      }

      return new TexI(img);
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
         Item itm = Wiki.get(pag.res().layer(Resource.action).name);
         if (pag != this.curttp || ttl != this.curttl || itm != this.ttitem) {
            this.ttitem = itm;
            this.curtt = rendertt(pag.res(), ttl, true);
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

   public Glob.Pagina paginafor(String name) {
      for (Glob.Pagina p : this.ui.sess.glob.paginae) {
         Resource res = p.res();
         if (res != null) {
            Resource.AButton act = res.layer(Resource.action);
            if (act != null && name.equals(act.name)) {
               return p;
            }
         }
      }

      return null;
   }

   public void useres(Resource r) {
      this.use(this.paginafor(r));
   }

   public void use(Glob.Pagina r) {
      Collection<Glob.Pagina> sub = new LinkedList<>();
      Collection<Glob.Pagina> cur = new LinkedList<>();
      this.cons(r, sub);
      this.cons(this.cur, cur);
      if (this.isCrafting(r)) {
         this.ui.gui.showCraftWnd();
      }

      this.selectCraft(r);
      if (sub.size() > 0) {
         this.cur = r;
         this.curoff = 0;
      } else if (r == this.bk) {
         this.cur = this.paginafor(this.cur.act().parent);
         this.curoff = 0;
         this.selectCraft(this.cur);
      } else if (r == this.next) {
         int off = gsz.x * gsz.y - 2;
         if (this.curoff + off >= cur.size()) {
            this.curoff = 0;
         } else {
            this.curoff += off;
         }
      } else {
         r.newp = 0;
         if (!this.senduse(r)) {
            return;
         }

         if (Config.menugrid_resets) {
            this.cur = null;
            this.curoff = 0;
         }
      }

      this.updlayout();
   }

   public boolean senduse(Glob.Pagina r) {
      String[] ad = r.act().ad;
      if (ad != null && ad.length >= 1) {
         if (ad[0].equals("@")) {
            if (!XTendedPaginae.useXTended(this.ui, ad)) {
               this.use(null);
            }
         } else {
            this.wdgmsg("act", ad);
         }

         return true;
      } else {
         return false;
      }
   }

   private void selectCraft(Glob.Pagina r) {
      if (r != null) {
         if (this.ui.gui.craftwnd != null) {
            this.ui.gui.craftwnd.select(r, true);
         }
      }
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
               this.use(h);
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
      if (!ev.isAltDown() && !ev.isControlDown() && k != 0) {
         k = (char)ev.getKeyCode();
         if (Character.toUpperCase(k) != k) {
            return false;
         } else if (k == 27 && this.cur != null) {
            this.cur = null;
            this.curoff = 0;
            this.updlayout();
            return true;
         } else if (k == 'N' && this.layout[gsz.x - 2][gsz.y - 1] == this.next) {
            this.use(this.next);
            return true;
         } else {
            Glob.Pagina r = this.hotmap.get(k);
            if (r != null) {
               this.use(r);
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public boolean isCrafting(Glob.Pagina p) {
      return this.isChildOf(p, this.CRAFT) || this.CRAFT == p;
   }

   public boolean isCrafting(Resource res) {
      return this.isCrafting(this.paginafor(res));
   }

   public Glob.Pagina getParent(Glob.Pagina p) {
      if (p == null) {
         return null;
      } else {
         try {
            Resource res = p.res();
            Resource.AButton ad = res.layer(Resource.action);
            return ad == null ? null : this.paginafor(ad.parent);
         } catch (Loading var4) {
            return null;
         }
      }
   }

   public boolean isChildOf(Glob.Pagina item, Glob.Pagina parent) {
      Glob.Pagina p;
      while ((p = this.getParent(item)) != null) {
         if (p == parent) {
            return true;
         }

         item = p;
      }

      return false;
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
