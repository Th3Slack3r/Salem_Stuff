package haven;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WikiBrowser extends Window implements DTarget2, DropTarget {
   private static final int SEARCH_H = 20;
   public static final RichText.Foundry fnd = new RichText.Foundry(
      new WikiBrowser.WikiParser(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 12, TextAttribute.FOREGROUND, Color.WHITE)
   );
   private static final Coord gzsz = new Coord(15, 15);
   private static final Coord minsz = new Coord(200, 150);
   private static final String OPT_SZ = "_sz";
   private static WikiBrowser instance;
   private Scrollport sp;
   private TextEntry search;
   private Button back;
   private WikiPage page;
   boolean rsm = false;

   public WikiBrowser(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent, "Wiki");
      this.justclose = true;
      this.search = new TextEntry(Coord.z, new Coord(this.asz.x - 30, 20), this, "");
      this.search.canactivate = true;
      this.back = new Button(new Coord(this.asz.x - 20, 0), 20, this, "←") {
         @Override
         public Object tooltip(Coord c, Widget prev) {
            return "Back";
         }
      };
      this.sp = new Scrollport(new Coord(0, 23), this.asz.sub(0, 23), this);
      this.pack();
      this.page = new WikiPage(Coord.z, this.sp.cont.sz, this.sp.cont);
   }

   @Override
   protected void loadOpts() {
      super.loadOpts();
      this.resize(this.getOptCoord("_sz", this.sz));
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (msg.equals("activate")) {
         if (sender == this.search) {
            this.page.open(this.search.text, true);
            return;
         }

         if (sender == this.back) {
            this.page.back();
            return;
         }
      }

      super.wdgmsg(sender, msg, args);
   }

   @Override
   public void resize(Coord sz) {
      super.resize(sz);
      if (this.sp != null) {
         this.sp.resize(sz.sub(0, 23));
      }

      if (this.search != null) {
         this.search.resize(new Coord(sz.x - 25, 20));
      }

      if (this.back != null) {
         this.back.c.x = sz.x - 20;
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (button == 1) {
         this.ui.grabmouse(this);
         this.doff = c;
         if (c.isect(this.sz.sub(gzsz), gzsz)) {
            this.rsm = true;
            return true;
         }
      }

      return super.mousedown(c, button);
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      if (this.rsm) {
         this.ui.grabmouse(null);
         this.rsm = false;
         this.storeOpt("_sz", this.asz);
         return true;
      } else {
         return super.mouseup(c, button);
      }
   }

   @Override
   public void mousemove(Coord c) {
      if (this.rsm) {
         Coord d = c.sub(this.doff);
         this.asz = this.asz.add(d);
         this.asz.x = Math.max(minsz.x, this.asz.x);
         this.asz.y = Math.max(minsz.y, this.asz.y);
         this.doff = c;
         this.resize(this.asz);
      } else {
         super.mousemove(c);
      }
   }

   public static void toggle() {
      if (instance == null) {
         instance = new WikiBrowser(new Coord(300, 200), minsz, UI.instance.gui);
      } else {
         close();
      }
   }

   @Override
   public void destroy() {
      instance = null;
      super.destroy();
   }

   public static void close() {
      if (instance != null) {
         UI ui = UI.instance;
         ui.destroy(instance);
      }
   }

   @Override
   public boolean dropthing(Coord cc, Object thing) {
      if (thing instanceof Resource) {
         Resource res = (Resource)thing;
         String name = null;
         Resource.Tooltip tt = res.layer(Resource.tooltip);
         if (tt != null) {
            name = tt.t;
         } else {
            Resource.AButton ad = res.layer(Resource.action);
            if (ad != null) {
               name = ad.name;
            }
         }

         if (name != null) {
            this.page.open(name, true);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean drop(Coord cc, Coord ul, GItem item) {
      String name = item.name();
      if (name != null) {
         this.page.open(name, true);
      }

      return true;
   }

   @Override
   public boolean iteminteract(Coord cc, Coord ul, GItem item) {
      return false;
   }

   private static class Table extends RichText.Part {
      private static final int PAD_W = 5;
      private static final int PAD_H = 3;
      private int tabc;
      private int w = 1;
      private int h = 0;
      private int lh = 0;
      private int[] twidth;
      private BufferedImage[] tnames;
      private List<BufferedImage[]> rows = new ArrayList<>();

      public Table(String[] args, Map<? extends Attribute, ?> attrs) {
         this.tabc = Integer.parseInt(args[0]);
         this.tnames = new BufferedImage[this.tabc];
         this.twidth = new int[this.tabc];
         int i = 0;

         for (int var6 = 0; var6 < this.tabc; var6++) {
            this.tnames[var6] = WikiBrowser.fnd.render(args[var6 + 1]).img;
            this.twidth[var6] = this.tnames[var6].getWidth() + 10;
            this.lh = Math.max(this.h, this.tnames[var6].getHeight() + 6);
         }

         i = this.tabc + 1;

         while (i < args.length) {
            BufferedImage[] cols = new BufferedImage[this.tabc];

            for (int k = 0; k < this.tabc; i++) {
               cols[k] = WikiBrowser.fnd.render(args[i]).img;
               this.twidth[k] = Math.max(this.twidth[k], cols[k].getWidth() + 10);
               this.lh = Math.max(this.h, cols[k].getHeight() + 6);
               k++;
            }

            this.rows.add(cols);
         }

         for (int var8 = 0; var8 < this.tabc; var8++) {
            this.w = this.w + this.twidth[var8];
         }

         this.h = this.lh * (this.rows.size() + 1);
      }

      @Override
      public int height() {
         return this.h;
      }

      @Override
      public int width() {
         return this.w;
      }

      @Override
      public int baseline() {
         return this.h - 1;
      }

      @Override
      public void render(Graphics2D g) {
         g.setColor(Color.WHITE);
         int cx = this.x;
         int cy = this.y;

         for (int i = 0; i < this.tabc; i++) {
            int cw = this.twidth[i];
            g.drawImage(this.tnames[i], cx + 5, cy + 3, null);
            g.drawRect(cx, cy, cw, this.lh);
            cx += cw;
         }

         int var12 = 1;

         for (BufferedImage[] cols : this.rows) {
            cx = this.x;
            cy = this.y + this.lh * var12;

            for (int j = 0; j < this.tabc; j++) {
               int cw = this.twidth[j];
               g.drawImage(cols[j], cx + 5, cy + 3, null);
               g.drawRect(cx, cy, cw, this.lh);
               cx += cw;
            }

            var12++;
         }
      }
   }

   private static class WikiParser extends RichText.Parser {
      public WikiParser(Object... args) {
         super(args);
      }

      @Override
      protected RichText.Part tag(RichText.Parser.PState s, String tn, String[] args, Map<? extends Attribute, ?> attrs) throws IOException {
         return (RichText.Part)(tn.equals("table") ? new WikiBrowser.Table(args, attrs) : super.tag(s, tn, args, attrs));
      }
   }
}
