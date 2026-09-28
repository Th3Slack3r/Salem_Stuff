package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class WItem extends Widget implements DTarget {
   public static final Resource missing = Resource.load("gfx/invobjs/missing");
   public final GItem item;
   private Tex ltex = null;
   private Tex mask = null;
   private Resource cmask = null;
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
         return ninf == null ? null : new TexI(Utils.outline2(Text.render(Integer.toString(ninf.itemnum()), Color.WHITE).img, Utils.contrast(Color.WHITE)));
      }
   };

   public WItem(Coord c, Widget parent, GItem item) {
      super(c, Inventory.sqsz, parent);
      this.item = item;
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
         img = ItemInfo.catimgs(0, img, RichText.render("\n" + pg.text, 200).img);
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
               if (this.longtip == null) {
                  this.longtip = new WItem.LongTip(info);
               }

               return this.longtip;
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
         if (this.item.num >= 0) {
            g.atext(Integer.toString(this.item.num), tex.sz(), 1.0, 1.0);
         } else if (this.itemnum.get() != null) {
            g.aimage(this.itemnum.get(), tex.sz(), 1.0, 1.0);
         }

         if (this.item.meter > 0) {
            double a = this.item.meter / 100.0;
            g.chcolor(255, 255, 255, 64);
            Coord half = Inventory.isqsz.div(2);
            g.prect(half, half.inv(), half, a * Math.PI * 2.0);
            g.chcolor();
         }

         if (this.olcol.get() != null) {
            if (this.cmask != res) {
               this.mask = null;
               if (tex instanceof TexI) {
                  this.mask = ((TexI)tex).mkmask();
               }

               this.cmask = res;
            }

            if (this.mask != null) {
               g.chcolor(this.olcol.get());
               g.image(this.mask, Coord.z);
               g.chcolor();
            }
         }
      } catch (Loading var7) {
         missing.loadwait();
         g.image(missing.layer(Resource.imgc).tex(), Coord.z, this.sz);
      }
   }

   @Override
   public boolean mousedown(Coord c, int btn) {
      if (btn == 1) {
         if (this.ui.modshift) {
            this.item.wdgmsg("transfer", new Object[]{c, this.ui.modmeta ? -1 : 1});
         } else if (this.ui.modctrl) {
            this.item.wdgmsg("drop", new Object[]{c, this.ui.modmeta ? -1 : 1});
         } else {
            this.item.wdgmsg("take", new Object[]{c});
         }

         return true;
      } else if (btn == 3) {
         this.item.wdgmsg("iact", new Object[]{c});
         return true;
      } else {
         return false;
      }
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
            if (info != this.forinfo) {
               this.save = this.find(info);
               this.forinfo = info;
            }
         } catch (Loading var2) {
            return null;
         }

         return this.save;
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
