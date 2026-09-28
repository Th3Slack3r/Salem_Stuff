package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
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
   private long hoverstart;
   private WItem.ItemTip shorttip = null;
   private WItem.ItemTip longtip = null;
   private List<ItemInfo> ttinfo = null;
   public final WItem.AttrCache<Color> olcol = new WItem.AttrCache<Color>() {
      protected Color find(List<ItemInfo> info) {
         GItem.ColorInfo cinf = ItemInfo.find(GItem.ColorInfo.class, info);
         return cinf == null ? null : cinf.olcol();
      }
   };
   public final WItem.AttrCache<Tex> itemnum = new WItem.AttrCache<Tex>() {
      protected Tex find(List<ItemInfo> info) {
         GItem.NumberInfo ninf = ItemInfo.find(GItem.NumberInfo.class, info);
         return ninf == null ? null : new TexI(Utils.outline2(Text.render(Integer.toString(ninf.itemnum()), Color.WHITE).img, Color.DARK_GRAY));
      }
   };
   public final WItem.AttrCache<Tex> heurnum = new WItem.AttrCache<Tex>() {
      protected Tex find(List<ItemInfo> info) {
         String num = ItemInfo.getCount(info);
         return num == null ? null : new TexI(Utils.outline2(Text.render(num, Color.WHITE).img, Color.DARK_GRAY));
      }
   };
   public final WItem.AttrCache<List<Integer>> heurmeter = new WItem.AttrCache<List<Integer>>() {
      protected List<Integer> find(List<ItemInfo> info) {
         return ItemInfo.getMeters(info);
      }
   };
   public final WItem.AttrCache<Double> gobblemeter = new WItem.AttrCache<Double>() {
      protected Double find(List<ItemInfo> info) {
         return ItemInfo.getGobbleMeter(info);
      }
   };
   public final WItem.AttrCache<String> contentName = new WItem.AttrCache<String>() {
      protected String find(List<ItemInfo> info) {
         return ItemInfo.getContent(info);
      }
   };
   public final WItem.AttrCache<Float> carats = new WItem.AttrCache<Float>() {
      protected Float find(List<ItemInfo> info) {
         return ItemInfo.getCarats(info);
      }
   };
   public final WItem.AttrCache<Tex> carats_tex = new WItem.AttrCache<Tex>() {
      protected Tex find(List<ItemInfo> info) {
         float c = WItem.this.carats.get();
         return c > 0.0F ? new TexI(Utils.outline2(Text.render(String.format("%.2f", c), WItem.CARAT_COLOR).img, Color.DARK_GRAY)) : null;
      }
   };
   public final WItem.AttrCache<Alchemy> alch = new WItem.AttrCache<Alchemy>() {
      protected Alchemy find(List<ItemInfo> info) {
         Alchemy alch = ItemInfo.find(Alchemy.class, info);
         if (alch == null) {
            ItemInfo.Contents cont = ItemInfo.find(ItemInfo.Contents.class, info);
            if (cont == null) {
               return null;
            }

            alch = ItemInfo.find(Alchemy.class, cont.sub);
            if (alch == null) {
               return null;
            }
         }

         return alch;
      }
   };
   public final WItem.AttrCache<Tex> purity = new WItem.AttrCache<Tex>() {
      protected Tex find(List<ItemInfo> info) {
         Alchemy a = WItem.this.alch.get();
         if (a != null) {
            String num = String.format("%.2f%%", 100.0 * a.purity());
            Color c = WItem.this.tryGetFoodColor(info, a);
            return new TexI(Utils.outline2(Text.render(num, c).img, Color.DARK_GRAY));
         } else {
            return null;
         }
      }
   };
   public final WItem.AttrCache<Tex> puritymult = new WItem.AttrCache<Tex>() {
      protected Tex find(List<ItemInfo> info) {
         Alchemy a = WItem.this.alch.get();
         if (a != null) {
            String num = String.format("%.2f", 1.0 + a.purity());
            Color c = WItem.this.tryGetFoodColor(info, a);
            return new TexI(Utils.outline2(Text.render(num, c).img, Color.DARK_GRAY));
         } else {
            return null;
         }
      }
   };
   private static Map<String, String> contents_translations = new HashMap<>();

   public WItem(Coord c, Widget parent, GItem item) {
      super(c, Inventory.sqsz, parent);
      contents_translations.put("Yellow Cornmeal", "gfx/invobjs/cornmeal0");
      contents_translations.put("White Cornmeal", "gfx/invobjs/cornmeal1");
      contents_translations.put("Blue Cornmeal", "gfx/invobjs/cornmeal2");
      contents_translations.put("Golden Cornmeal", "gfx/invobjs/cornmeal3");
      contents_translations.put("Oatmeal", "gfx/invobjs/flour0");
      contents_translations.put("Rye Flour", "gfx/invobjs/flour1");
      contents_translations.put("Barley Flour", "gfx/invobjs/flour2");
      contents_translations.put("Wheat Flour", "gfx/invobjs/flour3");
      contents_translations.put("Bonemeal", "gfx/invobjs/bonemeal");
      contents_translations.put("Sugar", "gfx/invobjs/sugar");
      this.item = item;
   }

   public WItem(Coord c, Widget parent, GItem item, Coord server_c) {
      this(c, parent, item);
      this.server_c = server_c;
   }

   private static Coord upsize(Coord sz) {
      int w = sz.x;
      int h = sz.y;
      if (w % Inventory.sqsz.x != 0) {
         w = Inventory.sqsz.x * (w / Inventory.sqsz.x + 1);
      }

      if (h % Inventory.sqsz.y != 0) {
         h = Inventory.sqsz.y * (h / Inventory.sqsz.y + 1);
      }

      return new Coord(w, h);
   }

   public void drawmain(GOut g, Tex tex) {
      g.image(tex, Coord.z);
      if (tex != this.ltex) {
         this.resize(upsize(tex.sz()));
         this.ltex = tex;
      }
   }

   public static BufferedImage rendershort(List<ItemInfo> info) {
      ItemInfo.Name nm = ItemInfo.find(ItemInfo.Name.class, info);
      if (nm == null) {
         return null;
      } else {
         BufferedImage img = nm.str.img;
         Alchemy ch = ItemInfo.find(Alchemy.class, info);
         if (ch != null) {
            img = ItemInfo.catimgsh(5, img, ch.smallmeter(), Text.std.renderf("(%d%% pure)", new Object[]{(int)(ch.a[0] * 100.0)}).img);
         }

         return img;
      }
   }

   public static BufferedImage shorttip(List<ItemInfo> info) {
      BufferedImage img = rendershort(info);
      ItemInfo.Contents cont = ItemInfo.find(ItemInfo.Contents.class, info);
      if (cont != null) {
         BufferedImage rc = rendershort(cont.sub);
         if (img != null && rc != null) {
            img = ItemInfo.catimgs(0, img, rc);
         } else if (img == null && rc != null) {
            img = rc;
         }
      }

      return img == null ? null : img;
   }

   public static BufferedImage longtip(GItem item, List<ItemInfo> info) {
      BufferedImage img = ItemInfo.longtip(info);
      Resource.Pagina pg = item.res.get().layer(Resource.pagina);
      if (pg != null) {
         img = ItemInfo.catimgs(5, img, RichText.render(pg.text, 200).img);
      }

      return img;
   }

   public BufferedImage longtip(List<ItemInfo> info) {
      return longtip(this.item, info);
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      long now = System.currentTimeMillis();
      if (prev != this) {
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
      }

      try {
         if (this.item == null) {
            return "...";
         } else {
            List<ItemInfo> info = this.item.info();
            if (info.size() < 1) {
               return null;
            } else {
               if (info != this.ttinfo) {
                  this.shorttip = this.longtip = null;
                  this.ttinfo = info;
               }

               if (now - this.hoverstart < 1000L) {
                  if (this.shorttip == null) {
                     this.shorttip = new WItem.ShortTip(info);
                  }

                  return this.shorttip;
               } else {
                  if (this.longtip == null || this.ts < GItem.infoUpdated) {
                     this.ts = GItem.infoUpdated;
                     this.longtip = new WItem.LongTip(info);
                  }

                  return this.longtip;
               }
            }
         }
      } catch (Loading var7) {
         return "...";
      }
   }

   @Override
   public void draw(GOut g) {
      try {
         Resource res = this.item.res.get();
         Tex tex = res.layer(Resource.imgc).tex();
         this.drawmain(g, tex);
         this.draw_highlight(g, res, tex);
         if (this.item.num >= 0) {
            g.atext(Integer.toString(this.item.num), tex.sz(), 1.0, 1.0);
         } else if (this.itemnum.get() != null) {
            g.aimage(this.itemnum.get(), tex.sz(), 1.0, 1.0);
         } else if (this.carats_tex.get() != null) {
            g.aimage(this.carats_tex.get(), tex.sz(), 1.0, 1.0);
         } else if (this.heurnum.get() != null) {
            g.aimage(this.heurnum.get(), tex.sz(), 1.0, 1.0);
         }

         if (this.item.meter > 0) {
            double a = this.item.meter / 100.0;
            int r = (int)((1.0 - a) * 255.0);
            int gr = (int)(a * 255.0);
            Coord s2 = this.sz.sub(0, 4);
            g.chcolor(r, gr, 0, 255);
            Coord bsz = new Coord(4, (int)(a * s2.y));
            g.frect(s2.sub(bsz).sub(4, 0), bsz);
            g.chcolor();
         }

         this.checkContents(g);
         this.heurmeters(g);
         this.drawpurity(g);
         this.item.testMatch();
      } catch (Loading var10) {
         missing.loadwait();
         g.image(missing.layer(Resource.imgc).tex(), Coord.z, this.sz);
      }
   }

   private void draw_highlight(GOut g, Resource res, Tex tex) {
      Color col = this.olcol.get();
      if (col == null && this.item.matched && GItem.filter != null) {
         col = MATCH_COLOR;
      }

      if (col != null) {
         if (this.cmask != res) {
            this.mask = null;
            if (tex instanceof TexI) {
               this.mask = ((TexI)tex).mkmask();
            }

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
      GobbleInfo food = ItemInfo.find(GobbleInfo.class, info);
      Color c = alch.color();
      if (food != null) {
         int[] means = new int[4];
         int i_highest = -1;
         int i_nexthighest = -1;
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

            for (int i = 0; i < fc.length; i++) {
               fc[i] = (c1[i] + c2[i]) / 2.0F;
            }

            c = new Color(fc[0], fc[1], fc[2]);
         }
      }

      return c;
   }

   private void drawpurity(GOut g) {
      if (Config.alwaysshowpurity || this.ui.modflags() != 0) {
         Tex img = Config.pure_mult ? this.puritymult.get() : this.purity.get();
         if (img != null) {
            g.aimage(img, new Coord(0, this.sz.y), 0.0, 1.0);
         }
      }
   }

   private void checkContents(GOut g) {
      if (Config.show_contents_icons) {
         String contents = this.contentName.get();
         if (contents != null) {
            Tex tex = this.getContentTex(contents);
            if (tex != null) {
               g.image(tex, Coord.z, hsz);
            }
         }
      }
   }

   private Tex getContentTex(String contents) {
      String name = contents_translations.get(contents.substring(contents.indexOf("of ") + 3));
      Tex tex = null;
      if (name != null && !name.equals("silver")) {
         try {
            Resource res = Resource.load(name);
            tex = new TexI(Utils.outline2(Utils.outline2(res.layer(Resource.imgc).img, Color.BLACK, true), Color.BLACK, true));
         } catch (Loading var5) {
            tex = missing.layer(Resource.imgc).tex();
         }
      }

      return tex;
   }

   private void heurmeters(GOut g) {
      Coord c0 = this.sz.sub(0, 4);
      if (Config.gobble_meters && UI.isCursor("gfx/hud/curs/eat")) {
         Double meter = this.gobblemeter.get();
         if (meter != null && meter > 0.0) {
            this.draw_meter(g, 0, c0, meter);
         }
      } else {
         List<Integer> meters = this.heurmeter.get();
         if (meters == null) {
            return;
         }

         int k = 0;

         for (Integer meter : meters) {
            double a = meter.intValue() / 100.0;
            this.draw_meter(g, k, c0, a);
            k++;
         }
      }
   }

   private void draw_meter(GOut g, int k, Coord c0, double a) {
      int r = (int)((1.0 - a) * 255.0);
      int gr = (int)(a * 255.0);
      g.chcolor(r, gr, 0, 255);
      Coord bsz = new Coord(4, (int)(a * c0.y));
      g.frect(new Coord(bsz.x * k + 1, c0.y - bsz.y), bsz);
      g.chcolor();
   }

   @Override
   public void tick(double dt) {
      if (this.ui.gui.gobble != null && this.ui.gui.gobble.lastUpdate != this.gobbleUpdateTime) {
         this.gobbleUpdateTime = this.ui.gui.gobble.lastUpdate;
         this.gobblemeter.reset();
      }

      super.tick(dt);
   }

   @Override
   public boolean mousedown(Coord c, int btn) {
      if (this.checkXfer(btn)) {
         return true;
      } else if (btn == 1) {
         this.item.wdgmsg("take", new Object[]{c});
         return true;
      } else if (btn == 3) {
         this.item.wdgmsg("iact", new Object[]{c});
         return true;
      } else {
         return false;
      }
   }

   private boolean checkXfer(int button) {
      boolean inv = this.parent instanceof Inventory;
      if (this.ui.modshift) {
         if (this.ui.modmeta) {
            if (inv) {
               this.wdgmsg("transfer-same", new Object[]{this.item, button == 3});
               return true;
            }
         } else if (button == 1) {
            this.item.wdgmsg("transfer", new Object[]{this.c});
            return true;
         }
      } else if (this.ui.modctrl) {
         if (this.ui.modmeta) {
            if (inv) {
               this.wdgmsg("drop-same", new Object[]{this.item, button == 3});
               return true;
            }
         } else if (button == 1) {
            this.item.wdgmsg("drop", new Object[]{this.c});
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean drop(Coord cc, Coord ul) {
      return false;
   }

   @Override
   public boolean iteminteract(Coord cc, Coord ul) {
      this.item.wdgmsg("itemact", new Object[]{this.ui.modflags()});
      return true;
   }

   public abstract class AttrCache<T> {
      private List<ItemInfo> forinfo = null;
      private T save = (T)null;

      public T get() {
         try {
            List<ItemInfo> info = WItem.this.item.info();
            if (info != this.forinfo || this.save == null) {
               this.save = this.find(info);
               this.forinfo = info;
            }
         } catch (Loading var2) {
            return null;
         }

         return this.save;
      }

      public void reset() {
         this.save = null;
      }

      protected abstract T find(List<ItemInfo> var1);
   }

   public class ItemTip implements Indir<Tex> {
      private final TexI tex;

      public ItemTip(BufferedImage img) {
         if (img == null) {
            throw new Loading();
         } else {
            this.tex = new TexI(img);
         }
      }

      public GItem item() {
         return WItem.this.item;
      }

      public Tex get() {
         return this.tex;
      }
   }

   public class LongTip extends WItem.ItemTip {
      public LongTip(List<ItemInfo> info) {
         super(WItem.this.longtip(info));
      }
   }

   public class ShortTip extends WItem.ItemTip {
      public ShortTip(List<ItemInfo> info) {
         super(WItem.shorttip(info));
      }
   }
}
