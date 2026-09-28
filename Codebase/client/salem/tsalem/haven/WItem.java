package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WItem extends Widget implements DTarget {
  public static final Resource missing = Resource.load("gfx/invobjs/missing");
  
  private static final Coord hsz = new Coord(24, 24);
  
  private static final Color MATCH_COLOR = new Color(96, 255, 255, 128);
  
  public static final Color CARAT_COLOR = new Color(192, 160, 0);
  
  public final GItem item;
  
  private Tex ltex = null;
  
  private Tex mask = null;
  
  private Resource cmask = null;
  
  private long ts = 0L;
  
  public Coord server_c;
  
  private long gobbleUpdateTime = 0L;
  
  private final int brokenSlots = 0;
  
  private static Map<String, Tex> iconMap = new HashMap<>();
  
  private boolean iconLoadedSuccessfully = false;
  
  public static WItem lastIActItem = null;
  
  private long hoverstart;
  
  private ItemTip shorttip;
  
  private ItemTip longtip;
  
  private List<ItemInfo> ttinfo;
  
  public final AttrCache<Color> olcol;
  
  public final AttrCache<Tex> itemnum;
  
  public final AttrCache<Tex> heurnum;
  
  public final AttrCache<Tex> heurnumNew;
  
  public final AttrCache<Tex> thermnum;
  
  public final AttrCache<Tex> slotfreenum;
  
  public final AttrCache<Tex> slotbrokennum;
  
  public final AttrCache<List<Integer>> heurmeter;
  
  public final AttrCache<Double> gobblemeter;
  
  public final AttrCache<String> contentName;
  
  public final AttrCache<Float> carats;
  
  public final AttrCache<Tex> carats_tex;
  
  public final AttrCache<Alchemy> alch;
  
  public final AttrCache<Tex> purity;
  
  public final AttrCache<Tex> puritymult;
  
  public WItem(Coord c, Widget parent, GItem item) {
    super(c, Inventory.sqsz, parent);
    this.shorttip = null;
    this.longtip = null;
    this.ttinfo = null;
    this.olcol = new AttrCache<Color>() {
        protected Color find(List<ItemInfo> info) {
          GItem.ColorInfo cinf = ItemInfo.<GItem.ColorInfo>find(GItem.ColorInfo.class, info);
          return (cinf == null) ? null : cinf.olcol();
        }
      };
    this.itemnum = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          GItem.NumberInfo ninf = ItemInfo.<GItem.NumberInfo>find(GItem.NumberInfo.class, info);
          if (ninf == null)
            return null; 
          return new TexI(Utils.outline2((Text.render(Integer.toString(ninf.itemnum()), Color.WHITE)).img, Color.DARK_GRAY));
        }
      };
    this.heurnum = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          String num = ItemInfo.getCount(info);
          if (num == null)
            return null; 
          return new TexI(Utils.outline2((Text.render(num, Color.WHITE)).img, Color.DARK_GRAY));
        }
      };
    this.heurnumNew = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          String num = ItemInfo.getCountNoThermal(info);
          if (num == null)
            return null; 
          return new TexI(Utils.outline2((Text.render(num, Color.WHITE)).img, Color.DARK_GRAY));
        }
      };
    this.thermnum = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          String num = ItemInfo.getCount(info);
          if (num == null)
            return null; 
          String thermalNr = "";
          for (ItemInfo ii : info) {
            if (ii instanceof ItemInfo.AdHoc) {
              ItemInfo.AdHoc ah = (ItemInfo.AdHoc)ii;
              if (ah.str != null && ah.str.text != null && ah.str.text.contains("Thermal:"))
                thermalNr = ah.str.text.split(": ")[1]; 
            } 
            if (thermalNr.length() > 0) {
              BufferedImage thermBI = (Text.render(thermalNr, new Color(255, 150, 50))).img;
              return new TexI(Utils.outline2(thermBI, Color.DARK_GRAY, true));
            } 
          } 
          return null;
        }
      };
    this.slotfreenum = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          String num = ItemInfo.getCount(info);
          if (num == null)
            return null; 
          String slotsNr = "";
          int openSlots = 0;
          int sT = 0;
          for (ItemInfo ii : info) {
            if (ii.getClass().getName().equals("ISlots"))
              try {
                Object[] slots = (Object[])Reflect.getFieldValue(ii, "s");
                for (Object slotted : slots) {
                  sT++;
                  if (slotted == null)
                    openSlots++; 
                } 
                slotsNr = "" + openSlots;
              } catch (Exception exception) {} 
            if (openSlots > 0) {
              int broken = 0;
              try {
                int sU = ((Integer)Reflect.getFieldValue(ii, "u")).intValue();
                broken = openSlots - sT - sU;
              } catch (Exception exception) {}
              if (broken > 0)
                openSlots -= broken; 
              if (openSlots > 0) {
                slotsNr = "" + openSlots;
                BufferedImage slotBI = (Text.render(slotsNr, new Color(255, 215, 0))).img;
                return new TexI(Utils.outline2(slotBI, Color.DARK_GRAY, true));
              } 
            } 
          } 
          return null;
        }
      };
    this.slotbrokennum = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          String num = ItemInfo.getCount(info);
          if (num == null)
            return null; 
          String slotsNr = "";
          int broken = 0;
          for (ItemInfo ii : info) {
            if (ii.getClass().getName().equals("ISlots"))
              try {
                int sU = ((Integer)Reflect.getFieldValue(ii, "u")).intValue();
                Object[] slots = (Object[])Reflect.getFieldValue(ii, "s");
                int sF = 0;
                int sT = 0;
                for (Object slotted : slots) {
                  sT++;
                  if (slotted == null)
                    sF++; 
                } 
                broken = sF - sT - sU;
                slotsNr = "" + broken;
              } catch (Exception exception) {} 
            if (broken > 0) {
              BufferedImage slotBI = (Text.render(slotsNr, new Color(255, 25, 0))).img;
              return new TexI(Utils.outline2(slotBI, Color.DARK_GRAY, true));
            } 
          } 
          return null;
        }
      };
    this.heurmeter = new AttrCache<List<Integer>>() {
        protected List<Integer> find(List<ItemInfo> info) {
          return ItemInfo.getMeters(info);
        }
      };
    this.gobblemeter = new AttrCache<Double>() {
        protected Double find(List<ItemInfo> info) {
          return Double.valueOf(ItemInfo.getGobbleMeter(info));
        }
      };
    this.contentName = new AttrCache<String>() {
        protected String find(List<ItemInfo> info) {
          return ItemInfo.getContent(info);
        }
      };
    this.carats = new AttrCache<Float>() {
        protected Float find(List<ItemInfo> info) {
          return ItemInfo.getCarats(info);
        }
      };
    this.carats_tex = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          float c = ((Float)WItem.this.carats.get()).floatValue();
          if (c > 0.0F)
            return new TexI(Utils.outline2((Text.render(String.format("%.2f", new Object[] { Float.valueOf(c) }), WItem.CARAT_COLOR)).img, Color.DARK_GRAY)); 
          return null;
        }
      };
    this.alch = new AttrCache<Alchemy>() {
        protected Alchemy find(List<ItemInfo> info) {
          Alchemy alch = ItemInfo.<Alchemy>find(Alchemy.class, info);
          if (alch == null) {
            ItemInfo.Contents cont = ItemInfo.<ItemInfo.Contents>find(ItemInfo.Contents.class, info);
            if (cont == null)
              return null; 
            alch = ItemInfo.<Alchemy>find(Alchemy.class, cont.sub);
            if (alch == null)
              return null; 
          } 
          return alch;
        }
      };
    this.purity = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          Alchemy a = WItem.this.alch.get();
          if (a != null) {
            String num = String.format("%.2f%%", new Object[] { Double.valueOf(100.0D * a.purity()) });
            Color c = WItem.this.tryGetFoodColor(info, a);
            return new TexI(Utils.outline2((Text.render(num, c)).img, Color.DARK_GRAY));
          } 
          return null;
        }
      };
    this.puritymult = new AttrCache<Tex>() {
        protected Tex find(List<ItemInfo> info) {
          Alchemy a = WItem.this.alch.get();
          if (a != null) {
            String num = String.format("%.2f", new Object[] { Double.valueOf(1.0D + a.purity()) });
            Color c = WItem.this.tryGetFoodColor(info, a);
            return new TexI(Utils.outline2((Text.render(num, c)).img, Color.DARK_GRAY));
          } 
          return null;
        }
      };
    this.item = item;
  }
  
  public WItem(Coord c, Widget parent, GItem item, Coord server_c) {
    this(c, parent, item);
    this.server_c = server_c;
  }
  
  private static Coord upsize(Coord sz) {
    int w = sz.x, h = sz.y;
    if (w % Inventory.sqsz.x != 0)
      w = Inventory.sqsz.x * (w / Inventory.sqsz.x + 1); 
    if (h % Inventory.sqsz.y != 0)
      h = Inventory.sqsz.y * (h / Inventory.sqsz.y + 1); 
    return new Coord(w, h);
  }
  
  public void drawmain(GOut g, Tex tex) {
    g.image(tex, Coord.z);
    if (tex != this.ltex) {
      resize(upsize(tex.sz()));
      this.ltex = tex;
    } 
  }
  
  public static BufferedImage rendershort(List<ItemInfo> info) {
    ItemInfo.Name nm = ItemInfo.<ItemInfo.Name>find(ItemInfo.Name.class, info);
    if (nm == null)
      return null; 
    BufferedImage img = nm.str.img;
    Alchemy ch = ItemInfo.<Alchemy>find(Alchemy.class, info);
    if (ch != null)
      img = ItemInfo.catimgsh(5, new BufferedImage[] { img, ch.smallmeter(), (Text.std.renderf("(%d%% pure)", new Object[] { Integer.valueOf((int)(ch.a[0] * 100.0D)) })).img }); 
    return img;
  }
  
  public static BufferedImage shorttip(List<ItemInfo> info) {
    BufferedImage img = rendershort(info);
    ItemInfo.Contents cont = ItemInfo.<ItemInfo.Contents>find(ItemInfo.Contents.class, info);
    if (cont != null) {
      BufferedImage rc = rendershort(cont.sub);
      if (img != null && rc != null) {
        img = ItemInfo.catimgs(0, new BufferedImage[] { img, rc });
      } else if (img == null && rc != null) {
        img = rc;
      } 
    } 
    if (img == null)
      return null; 
    return img;
  }
  
  public static BufferedImage longtip(GItem item, List<ItemInfo> info) {
    BufferedImage img = ItemInfo.longtip(info);
    Resource.Pagina pg = ((Resource)item.res.get()).<Resource.Pagina>layer(Resource.pagina);
    if (pg != null)
      img = ItemInfo.catimgs(5, new BufferedImage[] { img, (RichText.render(pg.text, 200, new Object[0])).img }); 
    return img;
  }
  
  public BufferedImage longtip(List<ItemInfo> info) {
    return longtip(this.item, info);
  }
  
  public class ItemTip implements Indir<Tex> {
    private final TexI tex;
    
    public ItemTip(BufferedImage img) {
      if (img == null)
        throw new Loading(); 
      this.tex = new TexI(img);
    }
    
    public GItem item() {
      return WItem.this.item;
    }
    
    public Tex get() {
      return this.tex;
    }
  }
  
  public class ShortTip extends ItemTip {
    public ShortTip(List<ItemInfo> info) {
      super(WItem.shorttip(info));
    }
  }
  
  public class LongTip extends ItemTip {
    public LongTip(List<ItemInfo> info) {
      super(WItem.this.longtip(info));
    }
  }
  
  public Object tooltip(Coord c, Widget prev) {
    long now = System.currentTimeMillis();
    if (prev != this)
      if (prev instanceof WItem) {
        long ps = ((WItem)prev).hoverstart;
        if (now - ps < 1000L) {
          this.hoverstart = now;
        } else {
          this.hoverstart = ps;
        } 
      } else {
        this.hoverstart = now;
      }  
    try {
      if (this.item == null)
        return "..."; 
      List<ItemInfo> info = this.item.info();
      if (info.size() < 1)
        return null; 
      if (info != this.ttinfo) {
        this.shorttip = this.longtip = null;
        this.ttinfo = info;
      } 
      if (now - this.hoverstart < 1000L) {
        if (this.shorttip == null)
          this.shorttip = new ShortTip(info); 
        return this.shorttip;
      } 
      if (this.longtip == null || this.ts < GItem.infoUpdated) {
        this.ts = GItem.infoUpdated;
        this.longtip = new LongTip(info);
      } 
      return this.longtip;
    } catch (Loading e) {
      return "...";
    } 
  }
  
  public abstract class AttrCache<T> {
    private List<ItemInfo> forinfo = null;
    
    private T save = null;
    
    public T get() {
      try {
        List<ItemInfo> info = WItem.this.item.info();
        if (info != this.forinfo || this.save == null) {
          this.save = find(info);
          this.forinfo = info;
        } 
      } catch (Loading e) {
        return null;
      } 
      return this.save;
    }
    
    public void reset() {
      this.save = null;
    }
    
    protected abstract T find(List<ItemInfo> param1List);
  }
  
  public void draw(GOut g) {
    try {
      Resource res = this.item.res.get();
      if (res == null || res.layer(Resource.imgc) == null)
        throw new Loading("Somehow the resource is null!"); 
      Tex tex = ((Resource.Image)res.<Resource.Image>layer(Resource.imgc)).tex();
      drawmain(g, tex);
      draw_highlight(g, res, tex);
      if (this.item.num >= 0) {
        g.atext(Integer.toString(this.item.num), tex.sz(), 1.0D, 1.0D);
      } else if (this.itemnum.get() != null) {
        g.aimage(this.itemnum.get(), tex.sz(), 1.0D, 1.0D);
      } else if (this.carats_tex.get() != null) {
        g.aimage(this.carats_tex.get(), tex.sz(), 1.0D, 1.0D);
      } else if (Config.new_numbers_on_item_icons) {
        if (this.heurnumNew.get() != null)
          g.aimage(this.heurnumNew.get(), tex.sz(), 1.0D, 1.0D); 
      } else if (this.heurnum.get() != null) {
        g.aimage(this.heurnum.get(), tex.sz(), 1.0D, 1.0D);
      } 
      if (Config.new_numbers_on_item_icons) {
        if (this.slotfreenum.get() != null)
          g.aimage(this.slotfreenum.get(), tex.sz(), 5.0D, 2.7D); 
        if (this.slotbrokennum.get() != null)
          g.aimage(this.slotbrokennum.get(), tex.sz(), 5.0D, 1.0D); 
        if (this.thermnum.get() != null)
          g.aimage(this.thermnum.get(), tex.sz(), 1.0D, 2.7D); 
      } 
      if (this.item.meter > 0) {
        double a = this.item.meter / 100.0D;
        int r = (int)((1.0D - a) * 255.0D);
        int gr = (int)(a * 255.0D);
        Coord s2 = this.sz.sub(0, 4);
        g.chcolor(r, gr, 0, 255);
        Coord bsz = new Coord(4, (int)(a * s2.y));
        g.frect(s2.sub(bsz).sub(4, 0), bsz);
        g.chcolor();
      } 
      checkContents(g);
      heurmeters(g);
      drawpurity(g);
      this.item.testMatch();
    } catch (Loading e) {
      missing.loadwait();
      g.image(((Resource.Image)missing.<Resource.Image>layer(Resource.imgc)).tex(), Coord.z, this.sz);
    } 
  }
  
  private void draw_highlight(GOut g, Resource res, Tex tex) {
    Color col = this.olcol.get();
    if (col == null && this.item.matched && GItem.filter != null)
      col = MATCH_COLOR; 
    if (col != null) {
      if (this.cmask != res) {
        this.mask = null;
        if (tex instanceof TexI)
          this.mask = ((TexI)tex).mkmask(); 
        this.cmask = res;
      } 
      if (this.mask != null) {
        g.chcolor(col);
        g.image(this.mask, Coord.z);
        g.chcolor();
      } 
    } 
  }
  
  private Color tryGetFoodColor(List<ItemInfo> info, Alchemy alch) {
    GobbleInfo food = ItemInfo.<GobbleInfo>find(GobbleInfo.class, info);
    Color c = alch.color();
    if (food != null) {
      int[] means = new int[4];
      int i_highest = -1, i_nexthighest = -1;
      int lowest_mean = Integer.MAX_VALUE;
      for (int b = 0; b < 4; b++) {
        means[b] = (food.h[b] + food.l[b]) / 2;
        lowest_mean = Math.min(lowest_mean, means[b]);
        if (i_highest < 0 || means[i_highest] < means[b]) {
          i_nexthighest = i_highest;
          i_highest = b;
        } else if (i_nexthighest < 0 || means[i_nexthighest] < means[b]) {
          i_nexthighest = b;
        } 
      } 
      if (means[i_nexthighest] < means[i_highest]) {
        c = Tempers.colors[i_highest];
      } else if (means[i_highest] > lowest_mean) {
        float[] c1 = Tempers.colors[i_highest].getRGBColorComponents(null);
        float[] c2 = Tempers.colors[i_nexthighest].getRGBColorComponents(null);
        float[] fc = new float[3];
        for (int i = 0; i < fc.length; i++)
          fc[i] = (c1[i] + c2[i]) / 2.0F; 
        c = new Color(fc[0], fc[1], fc[2]);
      } 
    } 
    return c;
  }
  
  private void drawpurity(GOut g) {
    if (!Config.alwaysshowpurity && this.ui.modflags() == 0)
      return; 
    Tex img = Config.pure_mult ? this.puritymult.get() : this.purity.get();
    if (img != null)
      g.aimage(img, new Coord(0, this.sz.y), 0.0D, 1.0D); 
  }
  
  private void checkContents(GOut g) {
    if (!Config.show_contents_icons)
      return; 
    String contents = this.contentName.get();
    if (contents == null)
      return; 
    String key = contents;
    Tex tex = null;
    if (iconMap.containsKey(key)) {
      tex = iconMap.get(key);
    } else {
      tex = getContentTex(contents);
      if (tex == null)
        return; 
      if (this.iconLoadedSuccessfully)
        iconMap.put(key, tex); 
    } 
    g.image(tex, Coord.z, hsz);
  }
  
  private Tex getContentTex(String contents) {
    if (Config.contents_icons == null)
      return null; 
    String name = Config.contents_icons.get(contents);
    for (Map.Entry<String, String> entry : Config.contents_icons.entrySet()) {
      if (contents.endsWith(entry.getKey())) {
        name = entry.getValue();
        break;
      } 
    } 
    Tex tex = null;
    if (name != null)
      try {
        Resource res = Resource.load(name);
        tex = new TexI(Utils.outline2(Utils.outline2(((Resource.Image)res.layer((Class)Resource.imgc)).img, Color.BLACK, true), Color.BLACK, true));
        this.iconLoadedSuccessfully = true;
      } catch (Loading e) {
        tex = ((Resource.Image)missing.<Resource.Image>layer(Resource.imgc)).tex();
      }  
    return tex;
  }
  
  private void heurmeters(GOut g) {
    Coord c0 = this.sz.sub(0, 4);
    if (Config.gobble_meters && UI.isCursor("gfx/hud/curs/eat")) {
      Double meter = this.gobblemeter.get();
      if (meter != null && meter.doubleValue() > 0.0D)
        draw_meter(g, 0, c0, meter.doubleValue()); 
    } else {
      List<Integer> meters = this.heurmeter.get();
      if (meters == null)
        return; 
      int k = 0;
      for (Integer meter : meters) {
        double a = meter.intValue() / 100.0D;
        draw_meter(g, k, c0, a);
        k++;
      } 
    } 
  }
  
  private void draw_meter(GOut g, int k, Coord c0, double a) {
    int r = (int)((1.0D - a) * 255.0D);
    int gr = (int)(a * 255.0D);
    g.chcolor(r, gr, 0, 255);
    Coord bsz = new Coord(4, (int)(a * c0.y));
    g.frect(new Coord(bsz.x * k + 1, c0.y - bsz.y), bsz);
    g.chcolor();
  }
  
  public void tick(double dt) {
    if (this.ui.gui.gobble != null) {
      long lastUpdate = 0L;
      if (this.ui.gui.gobble instanceof Gobble) {
        lastUpdate = ((Gobble)this.ui.gui.gobble).lastUpdate;
      } else if (this.ui.gui.gobble instanceof OldGobble) {
        lastUpdate = ((OldGobble)this.ui.gui.gobble).lastUpdate;
      } 
      if (lastUpdate != this.gobbleUpdateTime && lastUpdate > 0L) {
        this.gobbleUpdateTime = lastUpdate;
        this.gobblemeter.reset();
      } 
    } 
    super.tick(dt);
  }
  
  private ArrayList<GItem> getItemsFromInv(Inventory inv) {
    ArrayList<GItem> items = new ArrayList<>();
    GItem current = null;
    for (Widget w = inv.lchild; w != null; w = w.prev) {
      if (w.visible && w instanceof WItem) {
        current = ((WItem)w).item;
        items.add(current);
      } 
    } 
    return items;
  }
  
  private boolean itemIsAContainer(WItem item) {
    if (item.parent instanceof Inventory)
      return itemHasContainerName(item); 
    return false;
  }
  
  private boolean itemHasContainerName(WItem wItem) {
    String name = wItem.item.name();
    return nameIsContainerName(name);
  }
  
  private boolean nameIsContainerName(String name) {
    if (name != null && (name
      .toLowerCase().contains("sack") || name
      .toLowerCase().contains("egg basket") || name
      .toLowerCase().contains("candy basket") || name
      .toLowerCase().contains("nail box") || name
      .toLowerCase().contains("gem box")))
      return true; 
    return false;
  }
  
  public boolean mousedown(Coord c, int btn) {
    lastIActItem = null;
    if (this.ui.modmeta && !this.ui.modshift && btn == 2)
      if (itemIsAContainer(this)) {
        SackTakeXWindow.call(this);
        return true;
      }  
    if (this.ui.modmeta && !this.ui.modshift && (btn == 3 || btn == 1))
      if (itemIsAContainer(this)) {
        openAllSacksLikeThis(btn);
        return true;
      }  
    if (checkXfer(btn))
      return true; 
    if (btn == 1) {
      this.item.wdgmsg("take", new Object[] { c });
      return true;
    } 
    if (btn == 3) {
      this.item.wdgmsg("iact", new Object[] { c });
      lastIActItem = this;
      return true;
    } 
    return false;
  }
  
  public void openAllSacksLikeThis(int btn) {
    openAllSacksLikeThis(btn, 0);
  }
  
  public void openAllSacksLikeThis(final int btn, final int amount) {
    String name = this.item.resname();
    Inventory invTarget = (Inventory)this.parent;
    ArrayList<WItem> items = new ArrayList<>();
    if (this.item.name().toLowerCase().contains("egg basket")) {
      for (Widget wdg = invTarget.lchild; wdg != null; wdg = wdg.prev) {
        if (wdg.visible && wdg instanceof WItem && 
          ((WItem)wdg).item.resname().contains("eggbasket"))
          items.add((WItem)wdg); 
      } 
    } else if (this.item.name().toLowerCase().contains("candy basket")) {
      for (Widget wdg = invTarget.lchild; wdg != null; wdg = wdg.prev) {
        if (wdg.visible && wdg instanceof WItem && 
          ((WItem)wdg).item.resname().contains("candybasket"))
          items.add((WItem)wdg); 
      } 
    } else if (this.item.name().toLowerCase().contains("gem box")) {
      for (Widget wdg = invTarget.lchild; wdg != null; wdg = wdg.prev) {
        if (wdg.visible && wdg instanceof WItem && 
          ((WItem)wdg).item.resname().contains("gembox"))
          items.add((WItem)wdg); 
      } 
    } else if (this.item.name().toLowerCase().contains("nail box")) {
      for (Widget wdg = invTarget.lchild; wdg != null; wdg = wdg.prev) {
        if (wdg.visible && wdg instanceof WItem && 
          ((WItem)wdg).item.resname().contains("nailbox"))
          items.add((WItem)wdg); 
      } 
    } else {
      for (Widget wdg = invTarget.lchild; wdg != null; wdg = wdg.prev) {
        if (wdg.visible && wdg instanceof WItem && 
          ((WItem)wdg).item.resname().equals(name))
          items.add((WItem)wdg); 
      } 
    } 
    Collections.sort(items, new Comparator<WItem>() {
          public int compare(WItem o1, WItem o2) {
            if (o1.server_c.y > o2.server_c.y)
              return 1; 
            if (o1.server_c.y < o2.server_c.y)
              return -1; 
            if (o1.server_c.x > o2.server_c.x)
              return 1; 
            if (o1.server_c.x < o2.server_c.x)
              return -1; 
            return 0;
          }
        });
    for (WItem wItem : items) {
      wItem.item.wdgmsg("iact", new Object[] { wItem.c });
    } 
    if (btn == 1 || btn == 2)
      (new Thread(new Runnable() {
            public void run() {
              long startTime = System.currentTimeMillis();
              ArrayList<Widget> notEmtpy = new ArrayList<>();
              ArrayList<Widget> maybeEmtpy = new ArrayList<>();
              ArrayList<Widget> inventoriesNotEmpty = new ArrayList<>();
              int itemsToTransfer = amount;
              boolean transferring = false;
              if (itemsToTransfer > 0)
                transferring = true; 
              int itemsFound = 0;
              while (startTime + 5000L > System.currentTimeMillis() || transferring) {
                if (btn == 2 && itemsToTransfer > 0) {
                  Utils.sleep(50);
                } else {
                  Utils.sleep(200);
                } 
                try {
                  for (Widget w : UI.instance.widgets.values()) {
                    if (Inventory.class.isInstance(w)) {
                      Window win = w.<Window>getparent(Window.class);
                      if (win != null && (win.cap.text
                        .trim().toLowerCase().contains("sack") || win.cap.text
                        .trim().toLowerCase().contains("eggbasket") || win.cap.text
                        .trim().toLowerCase().contains("candybasket") || win.cap.text
                        .trim().toLowerCase().contains("gembox") || win.cap.text
                        .trim().toLowerCase().contains("nailbox"))) {
                        Inventory inv = (Inventory)w;
                        ArrayList<GItem> itemsFromInv = WItem.this.getItemsFromInv(inv);
                        if (!notEmtpy.contains(win) && (itemsFromInv == null || itemsFromInv.size() == 0)) {
                          if (maybeEmtpy.contains(win)) {
                            win.cbtn.click();
                            continue;
                          } 
                          maybeEmtpy.add(win);
                          continue;
                        } 
                        if (!notEmtpy.contains(win)) {
                          notEmtpy.add(win);
                          inventoriesNotEmpty.add(w);
                        } 
                      } 
                    } 
                  } 
                  if (btn == 2 && itemsToTransfer > 0) {
                    Iterator<Widget> invIt = inventoriesNotEmpty.iterator();
                    while (invIt.hasNext()) {
                      Widget w = invIt.next();
                      Inventory inv = (Inventory)w;
                      ArrayList<GItem> itemsFromInv = WItem.this.getItemsFromInv(inv);
                      int size = itemsFromInv.size();
                      boolean closeLater = false;
                      if (size <= itemsToTransfer)
                        closeLater = true; 
                      Iterator<GItem> it = itemsFromInv.iterator();
                      while (itemsToTransfer > 0 && it.hasNext()) {
                        GItem gItem = it.next();
                        gItem.wdgmsg("transfer", new Object[] { Coord.z });
                        itemsToTransfer--;
                      } 
                      if (closeLater) {
                        ((Window)inv.parent).cbtn.click();
                        invIt.remove();
                      } 
                    } 
                  } 
                  if (itemsToTransfer <= 0)
                    transferring = false; 
                  if (inventoriesNotEmpty.size() <= 0)
                    transferring = false; 
                } catch (Exception exception) {}
              } 
              for (Widget inv : inventoriesNotEmpty) {
                try {
                  ArrayList<GItem> itemsFromInv = WItem.this.getItemsFromInv((Inventory)inv);
                  itemsFound += itemsFromInv.size();
                } catch (Exception e) {
                  Utils.msgLog("error: " + e);
                } 
              } 
              if (amount <= 0)
                Utils.msgLog("Items in total: " + itemsFound); 
            }
          }"CloseEmptySacks")).start(); 
  }
  
  private boolean checkXfer(int button) {
    boolean inv = this.parent instanceof Inventory;
    if (this.ui.modctrl && this.ui.modshift) {
      wdgmsg("transfer-same-no-limit", new Object[] { this.item, Boolean.valueOf((button == 3)) });
      return true;
    } 
    if (this.ui.modshift) {
      if (this.ui.modmeta) {
        if (inv) {
          wdgmsg("transfer-same", new Object[] { this.item, Boolean.valueOf((button == 3)) });
          return true;
        } 
      } else if (button == 1) {
        this.item.wdgmsg("transfer", new Object[] { this.c });
        return true;
      } 
    } else if (this.ui.modctrl) {
      if (this.ui.modmeta) {
        if (inv) {
          wdgmsg("drop-same", new Object[] { this.item, Boolean.valueOf((button == 3)) });
          return true;
        } 
      } else if (button == 1) {
        this.item.wdgmsg("drop", new Object[] { this.c });
        return true;
      } 
    } else if (this.ui.modmeta && 
      inv) {
      wdgmsg("transfer-same-percentage", new Object[] { this.item, Boolean.valueOf((button == 3)) });
      return true;
    } 
    return false;
  }
  
  public boolean drop(Coord cc, Coord ul) {
    return false;
  }
  
  public boolean iteminteract(Coord cc, Coord ul) {
    this.item.wdgmsg("itemact", new Object[] { Integer.valueOf(this.ui.modflags()) });
    return true;
  }
}
