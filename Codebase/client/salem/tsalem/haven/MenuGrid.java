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
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import org.ender.wiki.Item;
import org.ender.wiki.Wiki;

public class MenuGrid extends Widget {
  public static final Tex bg = Resource.loadtex("gfx/hud/invsq");
  
  public static final Coord bgsz = bg.sz().add(-1, -1);
  
  public final Glob.Pagina CRAFT;
  
  public static Glob.Pagina next;
  
  public final Glob.Pagina bk;
  
  public static final RichText.Foundry ttfnd = new RichText.Foundry(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(10) });
  
  private static Coord gsz = new Coord(4, 4);
  
  public Glob.Pagina cur;
  
  public Glob.Pagina pressed;
  
  public Glob.Pagina dragging;
  
  public Glob.Pagina[][] layout;
  
  private int curoff;
  
  private int pagseq;
  
  private boolean loading;
  
  private final Map<Character, Glob.Pagina> hotmap;
  
  @RName("scm")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new MenuGrid(c, parent);
    }
  }
  
  public class PaginaException extends RuntimeException {
    public Glob.Pagina pag;
    
    public PaginaException(Glob.Pagina p) {
      super("Invalid pagina: " + (p.res()).name);
      this.pag = p;
    }
  }
  
  public boolean cons(Glob.Pagina p, Collection<Glob.Pagina> buf) {
    Collection<Glob.Pagina> open;
    Glob.Pagina[] cp = new Glob.Pagina[0];
    Collection<Glob.Pagina> close = new HashSet<>();
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
      Glob.Pagina pag = iter.next();
      iter.remove();
      try {
        Resource r = pag.res();
        Resource.AButton ad = r.<Resource.AButton>layer(Resource.action);
        if (ad == null)
          throw new PaginaException(pag); 
        Glob.Pagina parent = paginafor(ad.parent);
        if (pag.newp != 0 && parent != null && parent.newp == 0) {
          parent.newp = 2;
          parent.fstart = (parent.fstart == 0L) ? pag.fstart : Math.min(parent.fstart, pag.fstart);
        } 
        if (parent == p) {
          buf.add(pag);
        } else if (parent != null && !close.contains(parent) && !open.contains(parent)) {
          open.add(parent);
        } 
        close.add(pag);
      } catch (Loading e) {
        ret = false;
      } 
    } 
    return ret;
  }
  
  public MenuGrid(Coord c, Widget parent) {
    super(c, Inventory.invsz(gsz), parent);
    next = paginafor(Resource.load("gfx/hud/sc-next").loadwait());
    this.bk = paginafor(Resource.load("gfx/hud/sc-back").loadwait());
    this.layout = new Glob.Pagina[gsz.x][gsz.y];
    this.curoff = 0;
    this.pagseq = 0;
    this.loading = true;
    this.hotmap = new TreeMap<>();
    this.curttp = null;
    this.curttl = false;
    this.ttitem = null;
    this.curtt = null;
    this.ui.mnu = this;
    this.CRAFT = paginafor(Resource.load("paginae/act/craft"));
    XTendedPaginae.loadXTendedPaginae(this.ui);
  }
  
  public static Comparator<Glob.Pagina> sorter = new Comparator<Glob.Pagina>() {
      public int compare(Glob.Pagina a, Glob.Pagina b) {
        Resource.AButton aa = a.act(), ab = b.act();
        if (aa.ad.length == 0 && ab.ad.length > 0)
          return -1; 
        if (aa.ad.length > 0 && ab.ad.length == 0)
          return 1; 
        return aa.name.compareTo(ab.name);
      }
    };
  
  private void updlayout() {
    synchronized (this.ui.sess.glob.paginae) {
      List<Glob.Pagina> cur = new ArrayList<>();
      this.loading = !cons(this.cur, cur);
      Collections.sort(cur, sorter);
      int i = this.curoff;
      this.hotmap.clear();
      for (int y = 0; y < gsz.y; y++) {
        for (int x = 0; x < gsz.x; x++) {
          Glob.Pagina btn = null;
          if (this.cur != null && x == gsz.x - 1 && y == gsz.y - 1) {
            btn = this.bk;
          } else if (cur.size() > gsz.x * gsz.y - 1 && x == gsz.x - 2 && y == gsz.y - 1) {
            btn = next;
          } else if (i < cur.size()) {
            Resource.AButton ad = ((Glob.Pagina)cur.get(i)).act();
            if (ad.hk != '\000')
              this.hotmap.put(Character.valueOf(Character.toUpperCase(ad.hk)), cur.get(i)); 
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
            exp[n] = val.intValue();
            n++;
          } 
          Inspiration i = new Inspiration(null, 0, attrs, exp);
          return i.longtip();
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace(System.out);
    } 
    return null;
  }
  
  public static float[] safeFloat(Float[] input, float[] def) {
    if (input == null)
      return def; 
    int n = input.length;
    float[] output = new float[n];
    for (int i = 0; i < n; i++) {
      if (input[i] != null) {
        output[i] = input[i].floatValue();
      } else {
        output[i] = 0.0F;
      } 
    } 
    return output;
  }
  
  public static BufferedImage getFood(String name) {
    try {
      Item itm = Wiki.get(name);
      if (itm != null && itm.food != null) {
        Map<String, Float[]> food = itm.food;
        float[] def = { 0.0F, 0.0F, 0.0F, 0.0F };
        String[] names = { "Blood", "Phlegm", "Yellow Bile", "Black Bile" };
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
        String uses = String.format("Uses: %d\n", new Object[] { Integer.valueOf(itm.food_uses) });
        BufferedImage uses_img = (RichText.stdf.render(uses)).img;
        Color debuff_color = new Color(255, 192, 192);
        Color undebuff_color = new Color(192, 255, 192);
        BufferedImage currimg = ItemInfo.catimgs(3, new BufferedImage[] { fi.longtip(), uses_img, gi.longtip() });
        for (Map.Entry<String, Integer[]> e : (Iterable<Map.Entry<String, Integer[]>>)itm.food_reduce.entrySet()) {
          Integer[] values = e.getValue();
          BufferedImage head = (Text.render(String.format("-%d%%", new Object[] { values[0] }), debuff_color))img;
          Resource iconres = null;
          if (Wiki.buffmap.containsKey(e.getKey())) {
            iconres = Resource.load("gfx/invobjs/" + (String)Wiki.buffmap.get(e.getKey()));
          } else {
            System.out.println("Wiki food translation key not available: " + (String)e.getKey());
          } 
          if (iconres != null) {
            BufferedImage icon = PUtils.convolvedown(((Resource.Image)iconres.layer((Class)Resource.imgc)).img, new Coord(16, 16), GobIcon.filter);
            String str = ((Resource.Tooltip)iconres.layer((Class)Resource.tooltip)).t;
            BufferedImage bufferedImage1 = (Text.render(str + " [" + values[1] + "%]")).img;
            currimg = ItemInfo.catimgs(0, new BufferedImage[] { currimg, ItemInfo.catimgsh(0, new BufferedImage[] { head, icon, bufferedImage1 }) });
            continue;
          } 
          String classname = (String)e.getKey() + " [" + values[1] + "%]";
          BufferedImage tail = (Text.render(classname)).img;
          currimg = ItemInfo.catimgs(0, new BufferedImage[] { currimg, ItemInfo.catimgsh(0, new BufferedImage[] { head, tail }) });
        } 
        for (Map.Entry<String, Integer[]> e : (Iterable<Map.Entry<String, Integer[]>>)itm.food_restore.entrySet()) {
          Integer[] values = e.getValue();
          BufferedImage head = (Text.render(String.format("+%d%%", new Object[] { values[0] }), undebuff_color))img;
          Resource iconres = Resource.load("gfx/invobjs/" + (String)Wiki.buffmap.get(e.getKey()));
          if (iconres != null) {
            BufferedImage icon = PUtils.convolvedown(((Resource.Image)iconres.layer((Class)Resource.imgc)).img, new Coord(16, 16), GobIcon.filter);
            String str = ((Resource.Tooltip)iconres.layer((Class)Resource.tooltip)).t;
            BufferedImage bufferedImage1 = (Text.render(str + " [" + values[1] + "%]")).img;
            currimg = ItemInfo.catimgs(0, new BufferedImage[] { currimg, ItemInfo.catimgsh(0, new BufferedImage[] { head, icon, bufferedImage1 }) });
            continue;
          } 
          String classname = (String)e.getKey() + " [" + values[1] + "%]";
          BufferedImage tail = (Text.render(classname)).img;
          currimg = ItemInfo.catimgs(0, new BufferedImage[] { currimg, ItemInfo.catimgsh(0, new BufferedImage[] { head, tail }) });
        } 
        return currimg;
      } 
    } catch (Loading loading) {
    
    } catch (Exception e) {
      e.printStackTrace(System.out);
    } 
    return null;
  }
  
  public static BufferedImage getArtifact(String name) {
    try {
      Item itm = Wiki.get(name);
      if (itm == null || itm.art_profs == null || itm.art_profs.length == 0)
        return null; 
      Resource res = Resource.load("ui/tt/slot");
      if (res == null)
        return null; 
      ItemInfo.InfoFactory f = ((Resource.CodeEntry)res.<Resource.CodeEntry>layer(Resource.CodeEntry.class)).<ItemInfo.InfoFactory>get(ItemInfo.InfoFactory.class);
      Session sess = UI.instance.sess;
      int rid = sess.getresid("ui/tt/dattr");
      if (rid == 0)
        return null; 
      Object[] bonuses = itm.getArtBonuses();
      bonuses[0] = Integer.valueOf(rid);
      Object[] args = new Object[4 + itm.art_profs.length];
      int i = 0;
      args[i++] = Integer.valueOf(0);
      args[i++] = Integer.valueOf(itm.art_pmin);
      args[i++] = Integer.valueOf(itm.art_pmax);
      for (String prof : itm.art_profs)
        args[i++] = CharWnd.attrbyname(prof); 
      (new Object[1])[0] = bonuses;
      args[i++] = new Object[1];
      ItemInfo.Tip tip = (ItemInfo.Tip)f.build(sess, args);
      List<ItemInfo> list = new LinkedList<>();
      list.add(tip);
      return tip.longtip();
    } catch (Exception e) {
      e.printStackTrace(System.out);
      return null;
    } 
  }
  
  public static BufferedImage getSlots(String name) {
    try {
      Item itm = Wiki.get(name);
      if (itm == null || itm.cloth_slots == 0)
        return null; 
      Object[] args = new Object[5];
      int i = 0;
      args[i++] = Integer.valueOf(0);
      args[i++] = Integer.valueOf(itm.cloth_slots);
      args[i++] = Integer.valueOf(0);
      args[i++] = Integer.valueOf(25);
      args[i++] = Integer.valueOf(0);
      Resource res = Resource.load("ui/tt/slots");
      if (res == null)
        return null; 
      ItemInfo.InfoFactory f = ((Resource.CodeEntry)res.<Resource.CodeEntry>layer(Resource.CodeEntry.class)).<ItemInfo.InfoFactory>get(ItemInfo.InfoFactory.class);
      ItemInfo.Tip tip = (ItemInfo.Tip)f.build(null, args);
      return tip.longtip();
    } catch (Exception e) {
      e.printStackTrace(System.out);
      return null;
    } 
  }
  
  public static Tex rendertt(Resource res, boolean withpg, boolean hotkey) {
    Resource.AButton ad = res.<Resource.AButton>layer(Resource.action);
    Resource.Pagina pg = res.<Resource.Pagina>layer(Resource.pagina);
    String tt = ad.name;
    BufferedImage xp = null, food = null, slots = null, art = null;
    if (hotkey) {
      int pos = tt.toUpperCase().indexOf(Character.toUpperCase(ad.hk));
      if (pos >= 0) {
        tt = tt.substring(0, pos) + "$col[255,255,0]{" + tt.charAt(pos) + "}" + tt.substring(pos + 1);
      } else if (ad.hk != '\000') {
        tt = tt + " [" + ad.hk + "]";
      } 
    } 
    if (withpg) {
      if (pg != null)
        tt = tt + "\n\n" + pg.text; 
      xp = getXPgain(ad.name);
      food = getFood(ad.name);
      slots = getSlots(ad.name);
      art = getArtifact(ad.name);
    } 
    BufferedImage img = (ttfnd.render(tt, 300, new Object[0])).img;
    if (xp != null)
      img = ItemInfo.catimgs(3, new BufferedImage[] { img, xp }); 
    if (food != null)
      img = ItemInfo.catimgs(3, new BufferedImage[] { img, food }); 
    if (slots != null)
      img = ItemInfo.catimgs(3, new BufferedImage[] { img, slots }); 
    if (art != null)
      img = ItemInfo.catimgs(3, new BufferedImage[] { img, art }); 
    return new TexI(img);
  }
  
  private static Map<Glob.Pagina, Tex> glowmasks = new WeakHashMap<>();
  
  private Glob.Pagina curttp;
  
  private boolean curttl;
  
  Item ttitem;
  
  private Tex curtt;
  
  private long hoverstart;
  
  private Tex glowmask(Glob.Pagina pag) {
    Tex ret = glowmasks.get(pag);
    if (ret == null) {
      ret = new TexI(PUtils.glowmask(PUtils.glowmask(((Resource.Image)pag.res().layer((Class)Resource.imgc)).img.getRaster()), 4, new Color(32, 255, 32)));
      glowmasks.put(pag, ret);
    } 
    return ret;
  }
  
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
            double m = btn.meter / 1000.0D;
            if (btn.dtime > 0)
              m += (1.0D - m) * (now - btn.gettime) / btn.dtime; 
            m = Utils.clip(m, 0.0D, 1.0D);
            g.chcolor(255, 255, 255, 128);
            g.fellipse(p.add(Inventory.isqsz.div(2)), Inventory.isqsz.div(2), 90, (int)(90.0D + 360.0D * m));
            g.chcolor();
          } 
          if (btn.newp != 0)
            if (btn.fstart == 0L) {
              btn.fstart = now;
            } else {
              double ph = (now - btn.fstart) / 1000.0D - (x + y * gsz.x) * 0.15D % 1.0D;
              if (ph < 1.25D) {
                g.chcolor(255, 255, 255, (int)(255.0D * (Math.cos(ph * Math.PI * 2.0D) * -0.5D + 0.5D)));
                g.image(glowmask(btn), p.sub(4, 4));
                g.chcolor();
              } else {
                g.chcolor(255, 255, 255, 128);
                g.image(glowmask(btn), p.sub(4, 4));
                g.chcolor();
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
            public void draw(GOut g) {
              g.image(dt, MenuGrid.this.ui.mc.add(dt.sz().div(2).inv()));
            }
          });
    } 
  }
  
  public Object tooltip(Coord c, Widget prev) {
    Glob.Pagina pag = bhit(c);
    long now = System.currentTimeMillis();
    if (pag != null && pag.act() != null) {
      if (prev != this)
        this.hoverstart = now; 
      boolean ttl = (now - this.hoverstart > 500L);
      Item itm = Wiki.get(((Resource.AButton)pag.res().layer((Class)Resource.action)).name);
      if (pag != this.curttp || ttl != this.curttl || itm != this.ttitem) {
        this.ttitem = itm;
        this.curtt = rendertt(pag.res(), ttl, true);
        this.curttp = pag;
        this.curttl = ttl;
      } 
      return this.curtt;
    } 
    this.hoverstart = now;
    return "";
  }
  
  private Glob.Pagina bhit(Coord c) {
    Coord bc = Inventory.sqroff(c);
    if (bc.x >= 0 && bc.y >= 0 && bc.x < gsz.x && bc.y < gsz.y)
      return this.layout[bc.x][bc.y]; 
    return null;
  }
  
  public boolean mousedown(Coord c, int button) {
    Glob.Pagina h = bhit(c);
    if (button == 1 && h != null) {
      this.pressed = h;
      this.ui.grabmouse(this);
    } 
    return true;
  }
  
  public void mousemove(Coord c) {
    if (this.dragging == null && this.pressed != null) {
      Glob.Pagina h = bhit(c);
      if (h != this.pressed)
        this.dragging = this.pressed; 
    } 
  }
  
  private Glob.Pagina paginafor(Resource res) {
    return this.ui.sess.glob.paginafor(res);
  }
  
  public Glob.Pagina paginafor(String name) {
    Set<Glob.Pagina> pags = this.ui.sess.glob.paginae;
    Glob.Pagina secondBest = null;
    String shorterName = name.toLowerCase().substring(0, name.length() - 1);
    for (Glob.Pagina p : pags) {
      Resource res = p.res();
      if (res == null)
        continue; 
      Resource.AButton act = res.<Resource.AButton>layer(Resource.action);
      if (act == null)
        continue; 
      if (act.name != null) {
        String actNameShort = act.name.toLowerCase().trim();
        actNameShort = actNameShort.substring(0, actNameShort.length() - 1);
        if (act.name.toLowerCase().equals(shorterName))
          secondBest = p; 
        if (name.toLowerCase().equals(actNameShort))
          secondBest = p; 
      } 
      if (name.trim().toLowerCase().equals(act.name.trim().toLowerCase()))
        return p; 
    } 
    for (Glob.Pagina p : pags) {
      Resource res = p.res();
      if (res == null)
        continue; 
      Resource.Tooltip tt = res.<Resource.Tooltip>layer(Resource.tooltip);
      if (tt == null)
        continue; 
      if (tt.t != null) {
        String tShort = tt.t.toLowerCase().trim();
        tShort = tShort.substring(0, tShort.length() - 1);
        if (tt.t.toLowerCase().equals(shorterName))
          secondBest = p; 
        if (name.toLowerCase().equals(tShort))
          secondBest = p; 
      } 
      if (name.trim().toLowerCase().equals(tt.t.trim().toLowerCase()))
        return p; 
    } 
    if (secondBest != null)
      return secondBest; 
    this.ui.gui.tm.getAndOpenRecipeByName(null, name, false, false);
    return null;
  }
  
  public void useres(Resource r) {
    use(paginafor(r));
  }
  
  public void use(Glob.Pagina r) {
    Collection<Glob.Pagina> sub = new LinkedList<>(), cur = new LinkedList<>();
    cons(r, sub);
    cons(this.cur, cur);
    if (isCrafting(r))
      this.ui.gui.showCraftWnd(); 
    selectCraft(r);
    if (sub.size() > 0) {
      this.cur = r;
      this.curoff = 0;
    } else if (r == this.bk) {
      this.cur = paginafor((this.cur.act()).parent);
      this.curoff = 0;
      selectCraft(this.cur);
    } else if (r == next) {
      int off = gsz.x * gsz.y - 2;
      if (this.curoff + off >= cur.size()) {
        this.curoff = 0;
      } else {
        this.curoff += off;
      } 
    } else {
      r.newp = 0;
      if (!senduse(r))
        return; 
      if (Config.menugrid_resets) {
        this.cur = null;
        this.curoff = 0;
      } 
    } 
    updlayout();
  }
  
  public boolean senduse(Glob.Pagina r) {
    String[] ad = (r.act()).ad;
    if (ad == null || ad.length < 1)
      return false; 
    if (ad[0].equals("@")) {
      if (!XTendedPaginae.useXTended(this.ui, ad))
        use((Glob.Pagina)null); 
    } else {
      wdgmsg("act", (Object[])ad);
    } 
    return true;
  }
  
  private void selectCraft(Glob.Pagina r) {
    if (r == null)
      return; 
    if (this.ui.gui.craftwnd != null)
      this.ui.gui.craftwnd.select(r, false); 
  }
  
  public void tick(double dt) {
    if (this.loading || this.pagseq != this.ui.sess.glob.pagseq)
      updlayout(); 
  }
  
  public boolean mouseup(Coord c, int button) {
    Glob.Pagina h = bhit(c);
    if (button == 1) {
      if (this.dragging != null) {
        this.ui.dropthing(this.ui.root, this.ui.mc, this.dragging.res());
        this.dragging = this.pressed = null;
      } else if (this.pressed != null) {
        if (this.pressed == h)
          use(h); 
        this.pressed = null;
      } 
      this.ui.grabmouse(null);
    } 
    return true;
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "goto") {
      String res = (String)args[0];
      if (res.equals("")) {
        this.cur = null;
      } else {
        this.cur = paginafor(Resource.load(res));
      } 
      this.curoff = 0;
      updlayout();
    } 
  }
  
  public boolean globtype(char k, KeyEvent ev) {
    if (ev.isAltDown() || ev.isControlDown() || k == '\000')
      return false; 
    k = (char)ev.getKeyCode();
    if (Character.toUpperCase(k) != k)
      return false; 
    if (Utils.isCustomHotkey(ev))
      return false; 
    if (k == '\033' && this.cur != null) {
      this.cur = null;
      this.curoff = 0;
      updlayout();
      return true;
    } 
    if (k == 'N' && this.layout[gsz.x - 2][gsz.y - 1] == next) {
      use(next);
      return true;
    } 
    if (k == '\033' || k == '\033') {
      if (this.ui.gui.opts.visible) {
        this.ui.gui.opts.hide();
      } else {
        this.ui.gui.opts.show();
      } 
      return true;
    } 
    Glob.Pagina r = this.hotmap.get(Character.valueOf(k));
    if (r != null) {
      use(r);
      return true;
    } 
    return false;
  }
  
  public boolean isCrafting(Glob.Pagina p) {
    return (isChildOf(p, this.CRAFT) || this.CRAFT == p);
  }
  
  public boolean isCrafting(Resource res) {
    return isCrafting(paginafor(res));
  }
  
  public Glob.Pagina getParent(Glob.Pagina p) {
    if (p == null)
      return null; 
    try {
      Resource res = p.res();
      Resource.AButton ad = res.<Resource.AButton>layer(Resource.action);
      if (ad == null)
        return null; 
      return paginafor(ad.parent);
    } catch (Loading e) {
      return null;
    } 
  }
  
  public boolean isChildOf(Glob.Pagina item, Glob.Pagina parent) {
    Glob.Pagina p;
    while ((p = getParent(item)) != null) {
      if (p == parent)
        return true; 
      item = p;
    } 
    return false;
  }
}
