package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Map.Entry;

public class CharWnd extends Window {
   private static final Color selcol = new Color(15396788);
   private static final Color defcol = new Color(11907220);
   private static final Coord SZ_FULL = new Coord(640, 360);
   public static final Map<String, String> attrnm;
   public static final List<String> attrorder;
   public final Map<String, CharWnd.Attr> attrs = new HashMap<>();
   public final CharWnd.SkillList csk;
   public final CharWnd.SkillList nsk;
   public final Widget attrwdgs;
   public int tmexp;
   public boolean skavail;
   private final CharWnd.SkillInfo ski;
   private final Label tmexpl;
   public static final Color GREEN = new Color(11202218);
   public static final Color GRAY = new Color(11250314);
   public static final Color RED = new Color(12428195);
   public static final Color METER_BORDER = new Color(133, 92, 62, 255);
   public static final Color METER_BACK = new Color(28, 28, 28, 255);
   public static final Color REQ_ENOUGH = new Color(10032662);
   public static final Color REQ_NOT_ENOUGH = new Color(16726586);
   public static final Color FILL = new Color(27299);
   public static final Color FILL_ENOUGH = new Color(436479);
   public static final Color FILL_FULL = new Color(5153568);
   public static final Color FILL_PRESSED = new Color(7246243);
   public static final Color GAIN_FULL = new Color(7053392);
   public static final Color GAIN_ENOUGH = new Color(10524480);
   public static final Color GAIN_SMALL = new Color(10708817);
   private static final BufferedImage[] pbtn = new BufferedImage[]{
      Resource.loadimg("gfx/hud/skills/plusu"),
      Resource.loadimg("gfx/hud/skills/plusd"),
      Resource.loadimg("gfx/hud/skills/plush"),
      PUtils.monochromize(Resource.loadimg("gfx/hud/skills/plusu"), new Color(192, 192, 192)),
      PUtils.glowmask(PUtils.glowmask(Resource.loadimg("gfx/hud/skills/plusu").getRaster()), 4, new Color(32, 255, 32))
   };
   private Collection<CharWnd.Skill> acccsk;
   private Collection<CharWnd.Skill> accnsk;

   public static String attrbyname(String name) {
      for (Entry<String, String> entry : attrnm.entrySet()) {
         if (entry.getValue().equals(name)) {
            return entry.getKey();
         }
      }

      return null;
   }

   public static int[] sortattrs(final String[] attrs) {
      Integer[] o = new Integer[attrs.length];

      for (int i = 0; i < o.length; i++) {
         o[i] = new Integer(i);
      }

      Arrays.sort(o, new Comparator<Integer>() {
         public int compare(Integer a, Integer b) {
            return CharWnd.attrorder.indexOf(attrs[a]) - CharWnd.attrorder.indexOf(attrs[b]);
         }
      });
      int[] r = new int[o.length];

      for (int i = 0; i < o.length; i++) {
         r[i] = o[i];
      }

      return r;
   }

   public Color[] attrcols(String[] attrs) {
      Color[] c = new Color[attrs.length];
      int i = 0;

      for (String attrn : attrs) {
         c[i] = Color.WHITE;
         CharWnd.Attr attr = this.attrs.get(attrn);
         if (attr != null && attr.exp == attr.cap) {
            c[i] = RED;
         } else if (this.ski.cur != null) {
            for (int j = 0; j < this.ski.cur.costa.length; j++) {
               String costa = this.ski.cur.costa[j];
               int costv = this.ski.cur.costv[j];
               if (costa.equals(attrn)) {
                  if (this.attrs.get(costa).exp < costv) {
                     c[i] = GREEN;
                  } else {
                     c[i] = GRAY;
                  }
                  break;
               }
            }
         }

         i++;
      }

      return c;
   }

   private void checkexp() {
      this.skavail = false;

      for (CharWnd.Skill sk : this.nsk.skills) {
         if (sk.afforded() == 0) {
            this.skavail = true;
            break;
         }
      }
   }

   public CharWnd(Coord c, Widget parent) {
      super(c, SZ_FULL, parent, "Character");
      new Label(new Coord(0, 0), this, "Proficiencies:");
      this.attrwdgs = new Widget(new Coord(0, 30), Coord.z, this);
      int y = 0;

      for (String nm : attrorder) {
         this.attrs.put(nm, new CharWnd.Attr(nm, new Coord(0, y), this.attrwdgs));
         y += 20;
      }

      this.attrwdgs.pack();
      y = this.attrwdgs.c.y + this.attrwdgs.sz.y + 15;
      this.tmexpl = new Label(new Coord(0, y + 5), this, "Inspiration: ") {
         Glob.CAttr ac = CharWnd.super.ui.sess.glob.cattr.get("scap");
         Glob.CAttr ar = CharWnd.super.ui.sess.glob.cattr.get("srate");
         int lc = -1;
         int lr = -1;
         Tex tt = null;

         @Override
         public Object tooltip(Coord c, Widget prev) {
            if (this.tt == null || this.ac.comp != this.lc || this.ar.comp != this.lr) {
               this.tt = Text.renderf(Color.WHITE, "Cap: %,d, Rate: %.2f/s", this.lc = this.ac.comp, 3 * (this.lr = this.ar.comp) / 1000.0).tex();
            }

            return this.tt;
         }
      };
      new CharWnd.ScalpScore(new Coord(400, 0), this);
      new CPButton(new Coord(580, y), 40, this, "Reset") {
         {
            this.tooltip = RichText.render("Discard all currently accumulated proficiency points, and reset learning ability to 100%.", 250).tex();
         }

         @Override
         public void cpclick() {
            CharWnd.this.wdgmsg("lreset", new Object[0]);
         }
      };
      new Label(new Coord(270, 0), this, "Skills:");
      Tabs body = new Tabs(new Coord(270, 10), new Coord(180, 335), this) {
         @Override
         public void changed(Tabs.Tab from, Tabs.Tab to) {
            from.btn.change(CharWnd.defcol);
            to.btn.change(CharWnd.selcol);
         }
      };
      Tabs.Tab tab = body.new Tab(new Coord(335, 20), 60, "Learned");
      tab.btn.change(defcol);
      this.csk = new CharWnd.SkillList(new Coord(0, 30), 170, 14, tab) {
         @Override
         public void change(CharWnd.Skill sk) {
            CharWnd.Skill p = this.sel;
            super.change(sk);
            if (sk != null) {
               CharWnd.this.nsk.change(null);
            }

            if (sk != null || p != null) {
               CharWnd.this.ski.setsk(sk);
            }
         }
      };
      tab = body.new Tab(new Coord(270, 20), 60, "Available");
      tab.btn.change(selcol);
      body.showtab(tab);
      this.nsk = new CharWnd.SkillList(new Coord(0, 30), 170, 14, tab) {
         @Override
         protected void drawitem(GOut g, CharWnd.Skill sk) {
            int astate = sk.afforded();
            if (astate == 3) {
               g.chcolor(255, 128, 128, 255);
            } else if (astate == 2) {
               g.chcolor(255, 192, 128, 255);
            } else if (astate == 1) {
               g.chcolor(255, 255, 128, 255);
            } else if (astate == 0 && sk != this.sel) {
               double ph = System.currentTimeMillis() / 1000.0 - sk.listidx * 0.15;
               int cx = (int)(128.0 * (Math.cos(ph * Math.PI * 2.0) * -0.5 + 0.5)) + 127;
               g.chcolor(cx, 255, cx, 255);
            }

            super.drawitem(g, sk);
            g.chcolor();
         }

         @Override
         public void change(CharWnd.Skill sk) {
            CharWnd.Skill p = this.sel;
            super.change(sk);
            if (sk != null) {
               CharWnd.this.csk.change(null);
            }

            if (sk != null || p != null) {
               CharWnd.this.ski.setsk(sk);
            }
         }
      };
      new Button(new Coord(270, 340), 50, this, "Buy") {
         Tex glowmask = new TexI(PUtils.glowmask(PUtils.glowmask(this.draw().getRaster()), 4, new Color(32, 255, 32)));

         @Override
         public void click() {
            if (CharWnd.this.nsk.sel != null) {
               CharWnd.this.wdgmsg("buy", new Object[]{CharWnd.this.nsk.sel.nm});
            }
         }

         @Override
         public void draw(GOut g) {
            super.draw(g);
            if (CharWnd.this.nsk.sel != null && CharWnd.this.nsk.sel.afforded() == 0) {
               double ph = System.currentTimeMillis() / 1000.0;
               g.chcolor(255, 255, 255, (int)(128.0 * (Math.cos(ph * Math.PI * 2.0) * -0.5 + 0.5)));
               GOut g2 = g.reclipl(new Coord(-4, -4), g.sz.add(8, 8));
               g2.image(this.glowmask, Coord.z);
            }
         }
      };
      this.ski = new CharWnd.SkillInfo(new Coord(450, 45), new Coord(190, 278), this);
   }

   private void decsklist(Collection<CharWnd.Skill> buf, Object[] args, int a) {
      while (a < args.length) {
         String nm = (String)args[a++];
         Indir<Resource> res = this.ui.sess.getres((Integer)args[a++]);
         int n = 0;

         while (!((String)args[a + n * 2]).equals("")) {
            n++;
         }

         String[] costa = new String[n];
         int[] costv = new int[n];

         for (int i = 0; i < n; i++) {
            costa[i] = (String)args[a + i * 2];
            costv[i] = (Integer)args[a + i * 2 + 1];
         }

         a += n * 2 + 1;
         buf.add(new CharWnd.Skill(nm, res, costa, costv));
      }
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "exp") {
         for (int i = 0; i < args.length; i += 4) {
            String nm = (String)args[i];
            int c = (Integer)args[i + 1];
            int e = (Integer)args[i + 2];
            boolean av = (Integer)args[i + 3] != 0;
            CharWnd.Attr a = this.attrs.get(nm);
            a.cap = c;
            a.exp = e;
            a.rexp = null;
            a.av = av;
         }

         GItem.infoUpdated = System.currentTimeMillis();
         this.checkexp();
      } else if (msg == "csk") {
         boolean acc = (Integer)args[0] != 0;
         Collection<CharWnd.Skill> buf;
         if (this.acccsk != null) {
            buf = this.acccsk;
            this.acccsk = null;
         } else {
            buf = new LinkedList<>();
         }

         this.decsklist(buf, args, 1);
         if (acc) {
            this.acccsk = buf;
         } else {
            this.csk.pop(buf);
         }
      } else if (msg == "nsk") {
         boolean accx = (Integer)args[0] != 0;
         Collection<CharWnd.Skill> bufx;
         if (this.accnsk != null) {
            bufx = this.accnsk;
            this.accnsk = null;
         } else {
            bufx = new LinkedList<>();
         }

         this.decsklist(bufx, args, 1);
         if (accx) {
            this.accnsk = bufx;
         } else {
            this.nsk.pop(bufx);
         }
      } else if (msg == "tmexp") {
         this.tmexp = (Integer)args[0];
         this.tmexpl.settext(String.format("Inspiration: %,d", this.tmexp));
      }
   }

   static {
      final List<String> ao = new ArrayList<>();
      Map<String, String> an = new HashMap<String, String>() {
         public String put(String k, String v) {
            ao.add(k);
            return super.put(k, v);
         }
      };
      an.put("arts", "Arts & Crafts");
      an.put("cloak", "Cloak & Dagger");
      an.put("faith", "Faith & Wisdom");
      an.put("wild", "Flora & Fauna");
      an.put("nail", "Hammer & Nail");
      an.put("hung", "Hunting & Hideworking");
      an.put("law", "Law & Lore");
      an.put("mine", "Mines & Mountains");
      an.put("pots", "Herbs & Sprouts");
      an.put("fire", "Sparks & Embers");
      an.put("stock", "Stocks & Cultivars");
      an.put("spice", "Sugar & Spice");
      an.put("thread", "Thread & Needle");
      an.put("natp", "Natural Philosophy");
      an.put("perp", "Perennial Philosophy");
      attrnm = Collections.unmodifiableMap(an);
      attrorder = Collections.unmodifiableList(ao);
   }

   @Widget.RName("chr")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new CharWnd(c, parent);
      }
   }

   public class Attr extends Widget implements Observer {
      public final Coord imgc = new Coord(0, 1);
      public final Coord nmc = new Coord(17, 1);
      public final Coord vc = new Coord(137, 1);
      public final Coord expc = new Coord(162, 0);
      public final Coord expsz = new Coord(this.sz.x - this.expc.x - 20, this.sz.y);
      public final Coord btnc = new Coord(this.sz.x - 17, 0);
      public final String nm;
      public final Resource res;
      public final Glob.CAttr attr;
      public int exp;
      public int cap = 500;
      public boolean av = false;
      private Text rnm;
      private Text rv;
      private Text rexp;
      private int cv;
      private IButton pb;
      private int a = 0;

      private Attr(String attr, Coord c, Widget parent) {
         super(c, new Coord(257, 15), parent);
         this.nm = attr;
         this.res = Resource.load("gfx/hud/skills/" + this.nm);
         this.res.loadwait();
         Resource.Pagina pag = this.res.layer(Resource.pagina);
         if (pag != null) {
            this.tooltip = RichText.render(pag.text, 300);
         }

         this.attr = this.ui.sess.glob.cattr.get(this.nm);
         this.rnm = Text.render(CharWnd.attrnm.get(attr));
         this.attr.addObserver(this);
         this.pb = new IButton(this.btnc, this, CharWnd.pbtn[0], CharWnd.pbtn[1], CharWnd.pbtn[2]) {
            @Override
            public void draw(GOut g) {
               if (Attr.this.av) {
                  super.draw(g);
                  g = g.reclipl(new Coord(-4, -4), g.sz.add(8, 8));
                  double ph = System.currentTimeMillis() / 1000.0 - Attr.this.c.y * 0.007;
                  g.chcolor(255, 255, 255, (int)(128.0 * (Math.cos(ph * Math.PI * 2.0) * -0.5 + 0.5)));
                  g.image(CharWnd.pbtn[4], Coord.z);
               } else {
                  g.image(CharWnd.pbtn[3], Coord.z);
               }
            }

            @Override
            public void click() {
               Attr.this.buy();
            }
         };
      }

      public void drawmeter(GOut g, Coord c, Coord sz) {
         g.chcolor(CharWnd.METER_BORDER);
         g.frect(c, sz);
         g.chcolor(CharWnd.METER_BACK);
         g.frect(c.add(1, 1), sz.sub(2, 2));
         if (this.ui.lasttip instanceof WItem.ItemTip) {
            try {
               GItem item = ((WItem.ItemTip)this.ui.lasttip).item();
               Inspiration insp = ItemInfo.find(Inspiration.class, item.info());
               if (insp != null) {
                  for (int i = 0; i < insp.attrs.length; i++) {
                     if (insp.attrs[i].equals(this.nm)) {
                        int xp = insp.exp[i] + this.exp;
                        int w = Math.min((sz.x - 2) * xp / this.cap, sz.x - 2);
                        if (xp > this.cap) {
                           g.chcolor(CharWnd.GAIN_ENOUGH);
                        } else {
                           g.chcolor(CharWnd.GAIN_SMALL);
                        }

                        g.frect(c.add(1, 1), new Coord(w, sz.y / 2));
                        break;
                     }
                  }
               }
            } catch (Loading var9) {
            }
         }

         if (this.av) {
            g.chcolor(this.a == 1 ? CharWnd.FILL_PRESSED : CharWnd.FILL_FULL);
         } else {
            g.chcolor(CharWnd.FILL);
         }

         g.frect(c.add(1, 1), new Coord((sz.x - 2) * Math.min(this.exp, this.cap) / this.cap, sz.y - 2));
         if (CharWnd.this.nsk.sel != null) {
            CharWnd.Skill sk = CharWnd.this.nsk.sel;

            for (int ix = 0; ix < sk.costa.length; ix++) {
               if (sk.costa[ix].equals(this.nm)) {
                  int cost = sk.costv[ix];
                  if (!this.av && this.exp >= cost) {
                     g.chcolor(CharWnd.FILL_ENOUGH);
                     g.frect(c.add(1, 1), new Coord((sz.x - 2) * Math.min(this.exp, this.cap) / this.cap, sz.y - 2));
                  }

                  int w = Math.min((sz.x - 2) * sk.costv[ix] / this.cap, sz.x - 2);
                  if (cost > this.attr.base * 100) {
                     g.chcolor(CharWnd.REQ_NOT_ENOUGH);
                  } else {
                     g.chcolor(CharWnd.REQ_ENOUGH);
                  }

                  g.frect(c.add(1, sz.y / 2), new Coord(w, sz.y / 2));
                  break;
               }
            }
         }

         g.chcolor();
         if (this.rexp == null) {
            this.rexp = Text.render(String.format("%d/%d", this.exp, this.cap));
         }

         g.aimage(this.rexp.tex(), c.add(sz.x / 2, 1), 0.5, 0.0);
      }

      @Override
      public void draw(GOut g) {
         g.image(this.res.layer(Resource.imgc).tex(), this.imgc);
         g.image(this.rnm.tex(), this.nmc);
         if (this.attr.comp != this.cv) {
            this.rv = null;
         }

         if (this.rv == null) {
            this.rv = Text.render(String.format("%d", this.cv = this.attr.comp));
         }

         g.image(this.rv.tex(), this.vc);
         this.drawmeter(g, this.expc, this.expsz);
         super.draw(g);
      }

      @Override
      public boolean mousedown(Coord c, int btn) {
         if (btn == 1 && c.isect(this.expc, this.expsz)) {
            if (this.av) {
               this.a = 1;
               this.ui.grabmouse(this);
            }

            return true;
         } else {
            return super.mousedown(c, btn);
         }
      }

      @Override
      public boolean mouseup(Coord c, int btn) {
         if (btn == 1 && this.a == 1) {
            this.a = 0;
            this.ui.grabmouse(null);
            if (c.isect(this.expc, this.expsz)) {
               this.buy();
            }

            return true;
         } else {
            return super.mouseup(c, btn);
         }
      }

      public void buy() {
         CharWnd.this.wdgmsg("sattr", new Object[]{this.nm});
      }

      @Override
      public void update(Observable o, Object arg) {
         int delta = this.attr.comp - (Integer)arg;
         if (delta != 0) {
            this.rexp = null;
            this.ui
               .message(
                  String.format(
                     "Your '%s' profficiency %s to %d (%+d)", CharWnd.attrnm.get(this.nm), delta > 0 ? "increased" : "decreased", this.attr.comp, delta
                  ),
                  delta > 0 ? GameUI.MsgType.GOOD : GameUI.MsgType.BAD
               );
         }
      }
   }

   static class ScalpScore extends Label {
      private static final String format = "Scalp score: %d";
      private long lastupdate = 0L;

      public ScalpScore(Coord c, Widget parent) {
         super(c, parent, "Scalp score: %d");
      }

      @Override
      public void draw(GOut g) {
         long lastupdate = this.ui.sess.glob.cattr_lastupdate;
         if (this.lastupdate < lastupdate) {
            this.doUpdate(lastupdate);
         }

         super.draw(g);
      }

      private void doUpdate(long lastupdate) {
         this.lastupdate = lastupdate;
         int score = 0;
         synchronized (this.ui.sess.glob.cattr) {
            Map<String, Glob.CAttr> cattr = this.ui.sess.glob.cattr;

            for (String bile : Tempers.anm) {
               score += 2 * (this.getAttr(cattr, bile) / 1000 - 5);
            }

            for (String prof : CharWnd.attrorder) {
               score += this.getAttr(cattr, prof) - 5;
            }
         }

         this.settext(String.format("Scalp score: %d", score));
      }

      private int getAttr(Map<String, Glob.CAttr> attrs, String name) {
         return attrs.containsKey(name) ? attrs.get(name).base : 0;
      }
   }

   public class Skill {
      public final String nm;
      public final Indir<Resource> res;
      public final String[] costa;
      public final int[] costv;
      private int listidx;

      private Skill(String nm, Indir<Resource> res, String[] costa, int[] costv) {
         this.nm = nm;
         this.res = res;
         this.costa = costa;
         this.costv = costv;
      }

      private Skill(String nm, Indir<Resource> res) {
         this(nm, res, new String[0], new int[0]);
      }

      public int afforded() {
         int ret = 0;

         for (int i = 0; i < this.costa.length; i++) {
            if (CharWnd.this.attrs.get(this.costa[i]).attr.base * 100 < this.costv[i]) {
               return 3;
            }

            if (CharWnd.this.attrs.get(this.costa[i]).exp < this.costv[i]) {
               ret = Math.max(ret, 2);
            }
         }

         return ret;
      }
   }

   private static class SkillInfo extends RichTextBox {
      static final RichText.Foundry skbodfnd = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 9);
      CharWnd.Skill cur = null;
      boolean d = false;

      public SkillInfo(Coord c, Coord sz, Widget parent) {
         super(c, sz, parent, "", skbodfnd);
      }

      @Override
      public void tick(double dt) {
         if (this.d) {
            try {
               StringBuilder text = new StringBuilder();
               text.append("$img[" + this.cur.res.get().name + "]\n\n");
               text.append("$font[serif,16]{" + this.cur.res.get().layer(Resource.action).name + "}\n\n");
               int[] o = CharWnd.sortattrs(this.cur.costa);
               if (this.cur.costa.length > 0) {
                  for (int i = 0; i < o.length; i++) {
                     int u = o[i];
                     text.append(CharWnd.attrnm.get(this.cur.costa[u]) + ": " + this.cur.costv[u] + "\n");
                  }

                  text.append("\n");
               }

               text.append(this.cur.res.get().layer(Resource.pagina).text);
               this.settext(text.toString());
               this.d = false;
            } catch (Loading var7) {
            }
         }
      }

      public void setsk(CharWnd.Skill sk) {
         this.d = sk != null;
         this.cur = sk;
         this.settext("");
         GItem.infoUpdated = System.currentTimeMillis();
      }

      static {
         skbodfnd.aa = true;
      }
   }

   public static class SkillList extends Listbox<CharWnd.Skill> {
      public CharWnd.Skill[] skills = new CharWnd.Skill[0];
      private boolean loading = false;
      private final Comparator<CharWnd.Skill> skcomp = new Comparator<CharWnd.Skill>() {
         public int compare(CharWnd.Skill a, CharWnd.Skill b) {
            String an = a.nm;
            String bn = b.nm;
            return an.compareTo(bn);
         }
      };

      public SkillList(Coord c, int w, int h, Widget parent) {
         super(c, parent, w, h, 20);
      }

      @Override
      public void tick(double dt) {
         if (this.loading) {
            this.loading = false;
            Arrays.sort(this.skills, this.skcomp);

            for (int i = 0; i < this.skills.length; i++) {
               this.skills[i].listidx = i;
            }
         }
      }

      protected CharWnd.Skill listitem(int idx) {
         return this.skills[idx];
      }

      @Override
      protected int listitems() {
         return this.skills.length;
      }

      protected void drawitem(GOut g, CharWnd.Skill sk) {
         try {
            g.image(sk.res.get().layer(Resource.imgc).tex(), Coord.z, new Coord(20, 20));
            g.atext(sk.res.get().layer(Resource.action).name, new Coord(25, 10), 0.0, 0.5);
         } catch (Loading var4) {
            WItem.missing.loadwait();
            g.image(WItem.missing.layer(Resource.imgc).tex(), Coord.z, new Coord(20, 20));
            g.atext("...", new Coord(25, 10), 0.0, 0.5);
         }
      }

      public void pop(Collection<CharWnd.Skill> nsk) {
         CharWnd.Skill[] skills = nsk.toArray(new CharWnd.Skill[0]);
         this.sb.val = 0;
         this.sb.max = skills.length - this.h;
         this.sel = null;
         this.skills = skills;
         this.loading = true;
      }

      public void change(CharWnd.Skill sk) {
         this.sel = sk;
      }
   }
}
