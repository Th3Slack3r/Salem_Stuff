package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class Tempers extends SIWidget {
   static final RichText.Foundry tmprfnd = new RichText.Foundry(
      TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, TextAttribute.FOREGROUND, new Color(32, 32, 64), TextAttribute.SIZE, 12
   );
   public static final BufferedImage[] bg = new BufferedImage[]{
      Resource.loadimg("gfx/hud/tempers/bg1"),
      Resource.loadimg("gfx/hud/tempers/bg2"),
      Resource.loadimg("gfx/hud/tempers/bg3"),
      Resource.loadimg("gfx/hud/tempers/bg4"),
      Resource.loadimg("gfx/hud/tempers/bg5"),
      Resource.loadimg("gfx/hud/tempers/bg6"),
      Resource.loadimg("gfx/hud/tempers/bg7"),
      Resource.loadimg("gfx/hud/tempers/bg8"),
      Resource.loadimg("gfx/hud/tempers/bg9"),
      Resource.loadimg("gfx/hud/tempers/bg10")
   };
   public static final BufferedImage[] bars;
   public static final BufferedImage[] sbars;
   public static final BufferedImage[] fbars;
   public static final BufferedImage lcap = Resource.loadimg("gfx/hud/tempers/lcap");
   public static final BufferedImage rcap = Resource.loadimg("gfx/hud/tempers/rcap");
   public static final BufferedImage[] gbtni = new BufferedImage[]{
      Resource.loadimg("gfx/hud/tempers/gbtn"), Resource.loadimg("gfx/hud/tempers/gbtn"), Resource.loadimg("gfx/hud/tempers/gbtn")
   };
   public static final Coord boxc = new Coord(96, 0);
   public static final Coord boxsz = new Coord(339, 62);
   public static final Color[] colors = new Color[]{new Color(255, 64, 64), new Color(0, 128, 255), new Color(255, 255, 64), new Color(160, 160, 160)};
   public static final String[] tcolors;
   static final Color softc = new Color(168, 128, 200);
   static final Color foodc = new Color(192, 160, 0);
   static final Coord[] mc = new Coord[]{new Coord(295, 11), new Coord(235, 11), new Coord(235, 35), new Coord(295, 35)};
   static final String[] anm = new String[]{"blood", "phlegm", "ybile", "bbile"};
   static final String[] rnm = new String[]{"Blood", "Phlegm", "Yellow Bile", "Black Bile"};
   int[] soft = new int[4];
   int[] hard = new int[4];
   int[] lmax = new int[4];
   int insanity = 0;
   public boolean gavail = true;
   Tex tt = null;
   public Widget gbtn;
   private Tex[] texts = null;
   private FoodInfo lfood;

   public Tempers(Coord c, Widget parent) {
      super(c, PUtils.imgsz(bg[0]), parent);
   }

   @Override
   public void tick(double dt) {
      int[] max = new int[4];

      for (int i = 0; i < 4; i++) {
         max[i] = this.ui.sess.glob.cattr.get(anm[i]).comp;
         if (max[i] == 0) {
            return;
         }

         if (max[i] != this.lmax[i]) {
            this.redraw();
            this.texts = null;
            this.tt = null;
         }
      }

      this.lmax = max;
      if (this.gavail && this.gbtn == null) {
         this.gbtn = new IButton(Coord.z, this.parent, gbtni[0], gbtni[1], gbtni[2]) {
            {
               if (!Tempers.this.visible) {
                  this.hide();
               }

               (new Widget.NormAnim(0.25) {
                  @Override
                  public void ntick(double a) {
                     double f = Math.abs(1.0 - 6.0 * Math.pow(a, 2.0) + 5.0 * Math.pow(a, 3.0));
                     c = new Coord(Tempers.this.c.x + (Tempers.this.sz.x - sz.x) / 2, (int)(Tempers.this.c.y + Tempers.boxsz.y - f * sz.y));
                  }
               }).ntick(0.0);
            }

            @Override
            public void reqdestroy() {
               new Widget.NormAnim(0.25) {
                  @Override
                  public void ntick(double a) {
                     c = new Coord(Tempers.this.c.x + (Tempers.this.sz.x - sz.x) / 2, (int)(Tempers.this.c.y + Tempers.boxsz.y - a * sz.y));
                     if (a == 1.0) {
                        destroy();
                     }
                  }
               };
            }

            @Override
            public void click() {
               this.<GameUI>getparent(GameUI.class).act("gobble");
            }

            @Override
            public void presize() {
               this.c = new Coord(Tempers.this.c.x + (Tempers.this.sz.x - this.sz.x) / 2, Tempers.this.c.y + Tempers.boxsz.y);
            }
         };
         this.raise();
         this.ui.gui.updateRenderFilter();
      } else if (!this.gavail && this.gbtn != null) {
         this.gbtn.reqdestroy();
         this.gbtn = null;
      }

      FoodInfo food = null;
      if (this.ui.lasttip instanceof WItem.ItemTip) {
         try {
            food = ItemInfo.find(FoodInfo.class, ((WItem.ItemTip)this.ui.lasttip).item().info());
         } catch (Loading var6) {
         }
      }

      if (this.lfood != food) {
         this.lfood = food;
         this.redraw();
      }
   }

   @Override
   public void show() {
      super.show();
      if (this.gbtn != null) {
         this.gbtn.show();
      }
   }

   @Override
   public void hide() {
      super.hide();
      if (this.gbtn != null) {
         this.gbtn.hide();
      }
   }

   public static WritableRaster rmeter(Raster tex, int val, int max) {
      int w = 1 + Utils.clip(val, 0, max) * (tex.getWidth() - 1) / Math.max(max, 1);
      WritableRaster bar = PUtils.copy(tex);
      PUtils.gayblit(bar, 3, new Coord(w - rcap.getWidth(), 0), rcap.getRaster(), 0, Coord.z);

      for (int y = 0; y < bar.getHeight(); y++) {
         for (int x = w; x < bar.getWidth(); x++) {
            bar.setSample(x, y, 3, 0);
         }
      }

      return bar;
   }

   public static WritableRaster lmeter(Raster tex, int val, int max) {
      int w = 1 + Utils.clip(val, 0, max) * (tex.getWidth() - 1) / Math.max(max, 1);
      WritableRaster bar = PUtils.copy(tex);
      PUtils.gayblit(bar, 3, new Coord(bar.getWidth() - w, 0), lcap.getRaster(), 0, Coord.z);

      for (int y = 0; y < bar.getHeight(); y++) {
         for (int x = 0; x < bar.getWidth() - w; x++) {
            bar.setSample(x, y, 3, 0);
         }
      }

      return bar;
   }

   private WritableRaster rfmeter(FoodInfo food, int t) {
      return PUtils.alphablit(
         rmeter(fbars[t].getRaster(), this.soft[t] + food.tempers[t], this.lmax[t]), rmeter(sbars[t].getRaster(), this.soft[t], this.lmax[t]), Coord.z
      );
   }

   private WritableRaster lfmeter(FoodInfo food, int t) {
      return PUtils.alphablit(
         lmeter(fbars[t].getRaster(), this.soft[t] + food.tempers[t], this.lmax[t]), lmeter(sbars[t].getRaster(), this.soft[t], this.lmax[t]), Coord.z
      );
   }

   @Override
   public void draw(BufferedImage buf) {
      WritableRaster dst = buf.getRaster();
      PUtils.blit(dst, bg[this.insanity].getRaster(), Coord.z);
      if (this.lfood != null) {
         PUtils.alphablit(dst, this.rfmeter(this.lfood, 0), mc[0]);
         PUtils.alphablit(dst, this.lfmeter(this.lfood, 1), mc[1].sub(bars[1].getWidth() - 1, 0));
         PUtils.alphablit(dst, this.lfmeter(this.lfood, 2), mc[2].sub(bars[2].getWidth() - 1, 0));
         PUtils.alphablit(dst, this.rfmeter(this.lfood, 3), mc[3]);
      } else {
         if (this.soft[0] > this.hard[0]) {
            PUtils.alphablit(dst, rmeter(sbars[0].getRaster(), this.soft[0], this.lmax[0]), mc[0]);
         }

         if (this.soft[1] > this.hard[1]) {
            PUtils.alphablit(dst, lmeter(sbars[1].getRaster(), this.soft[1], this.lmax[1]), mc[1].sub(bars[1].getWidth() - 1, 0));
         }

         if (this.soft[2] > this.hard[2]) {
            PUtils.alphablit(dst, lmeter(sbars[2].getRaster(), this.soft[2], this.lmax[2]), mc[2].sub(bars[2].getWidth() - 1, 0));
         }

         if (this.soft[3] > this.hard[3]) {
            PUtils.alphablit(dst, rmeter(sbars[3].getRaster(), this.soft[3], this.lmax[3]), mc[3]);
         }
      }

      PUtils.alphablit(dst, rmeter(bars[0].getRaster(), this.hard[0], this.lmax[0]), mc[0]);
      PUtils.alphablit(dst, lmeter(bars[1].getRaster(), this.hard[1], this.lmax[1]), mc[1].sub(bars[1].getWidth() - 1, 0));
      PUtils.alphablit(dst, lmeter(bars[2].getRaster(), this.hard[2], this.lmax[2]), mc[2].sub(bars[2].getWidth() - 1, 0));
      PUtils.alphablit(dst, rmeter(bars[3].getRaster(), this.hard[3], this.lmax[3]), mc[3]);
   }

   public void updinsanity(int n) {
      if (this.insanity != n) {
         String direction = this.insanity > n ? "decreased" : "increased";
         GameUI.MsgType type = this.insanity > n ? GameUI.MsgType.GOOD : GameUI.MsgType.BAD;
         this.ui.gui.message(String.format("Your madness %s to level %d!", direction, n), type);
      }

      this.insanity = n;
      this.redraw();
      this.tt = null;
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      if (Config.show_tempers) {
         if (this.texts == null) {
            this.texts = new TexI[4];

            for (int i = 0; i < 4; i++) {
               String str = String.format(
                  "%s / %s / %s", Utils.fpformat(this.hard[i], 3, 1), Utils.fpformat(this.soft[i], 3, 1), Utils.fpformat(this.lmax[i], 3, 1)
               );
               this.texts[i] = text(str);
            }
         }

         g.aimage(this.texts[0], mc[0].add(bars[0].getWidth() / 2, bars[0].getHeight() / 2 - 1), 0.5, 0.5);
         g.aimage(this.texts[1], mc[1].add(-bars[1].getWidth() / 2, bars[1].getHeight() / 2 - 1), 0.5, 0.5);
         g.aimage(this.texts[2], mc[2].add(-bars[2].getWidth() / 2, bars[2].getHeight() / 2 - 1), 0.5, 0.5);
         g.aimage(this.texts[3], mc[3].add(bars[3].getWidth() / 2, bars[3].getHeight() / 2 - 1), 0.5, 0.5);
      }
   }

   public void upds(int[] n) {
      this.texts = null;
      this.soft = n;
      this.redraw();
      this.tt = null;
   }

   public void updh(int[] n) {
      this.texts = null;
      this.hard = n;
      this.redraw();
      this.tt = null;
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      return bg[this.insanity].getRaster().getSample(c.x, c.y, 3) > 128 ? true : super.mousedown(c, button);
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      if (!c.isect(boxc, boxsz)) {
         return null;
      } else {
         if (this.tt == null) {
            StringBuilder buf = new StringBuilder();

            for (int i = 0; i < 4; i++) {
               buf.append(
                  String.format(
                     "%s: %s/%s/%s\n", rnm[i], Utils.fpformat(this.hard[i], 3, 1), Utils.fpformat(this.soft[i], 3, 1), Utils.fpformat(this.lmax[i], 3, 1)
                  )
               );
            }

            buf.append(String.format("Madness level: %d", this.insanity));
            this.tt = RichText.render(buf.toString(), 0).tex();
         }

         return this.tt;
      }
   }

   public static TexI text(String str) {
      return new TexI(Utils.outline2(tmprfnd.render(str).img, new Color(240, 240, 240), false));
   }

   public int[] getValues() {
      return this.hard;
   }

   static {
      int n = anm.length;
      BufferedImage[] b = new BufferedImage[n];
      BufferedImage[] s = new BufferedImage[n];
      BufferedImage[] f = new BufferedImage[n];

      for (int i = 0; i < n; i++) {
         b[i] = Resource.loadimg("gfx/hud/tempers/" + anm[i]);
         s[i] = PUtils.monochromize(b[i], softc);
         f[i] = PUtils.monochromize(b[i], foodc);
      }

      bars = b;
      sbars = s;
      fbars = f;
      String[] buf = new String[colors.length];

      for (int i = 0; i < colors.length; i++) {
         buf[i] = String.format("%d,%d,%d", colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue());
      }

      tcolors = buf;
   }
}
