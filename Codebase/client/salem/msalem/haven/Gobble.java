package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Gobble extends SIWidget {
   public static final BufferedImage bg = Resource.loadimg("gfx/hud/tempers/gbg");
   static Text.Foundry tnf = new Text.Foundry(new Font("serif", 1, 16)).aa(true);
   public int[] lev = new int[4];
   public List<Gobble.TypeMod> mods = new ArrayList<>();
   static final Color loc = new Color(0, 128, 255);
   static final Color hic = new Color(0, 128, 64);
   static final BufferedImage[] lobars;
   static final BufferedImage[] hibars;
   private boolean updt = true;
   private Gobble.TypeList typelist;
   private int[] lmax = new int[4];
   private int max;
   private Tex lvlmask;
   private long lvltime;
   private Tex levels;
   private GobbleInfo lfood;
   public static final Collection<String> msgs;

   public Gobble(Coord c, Widget parent) {
      super(c, Utils.imgsz(Tempers.bg[0]), parent);
      this.lcount(0, Color.WHITE);
      this.typelist = new Gobble.TypeList(Coord.z, this.getparent(GameUI.class));
   }

   @Override
   public void destroy() {
      this.typelist.destroy();
      super.destroy();
   }

   @Override
   public void tick(double dt) {
      int max = 0;
      int[] lmax = new int[4];

      for (int i = 0; i < 4; i++) {
         lmax[i] = this.ui.sess.glob.cattr.get(Tempers.anm[i]).base;
         if (lmax[i] == 0) {
            return;
         }

         if (lmax[i] != this.lmax[i]) {
            this.redraw();
         }

         max = Math.max(max, lmax[i]);
      }

      this.lmax = lmax;
      this.max = max;
      GobbleInfo food = null;
      Alchemy ch = null;
      if (this.ui.lasttip instanceof WItem.ItemTip) {
         try {
            food = ItemInfo.find(GobbleInfo.class, ((WItem.ItemTip)this.ui.lasttip).item().info());
            ch = ItemInfo.find(Alchemy.class, ((WItem.ItemTip)this.ui.lasttip).item().info());
         } catch (Loading var8) {
         }
      }

      if (this.lfood != food) {
         this.lfood = food;
         this.redraw();
      }
   }

   private double foodeff(GobbleInfo food) {
      double ret = 1.0;

      for (int t : this.lfood.types) {
         ret *= this.mods.get(t).a;
      }

      return ret;
   }

   private WritableRaster rgmeter(GobbleInfo food, double e, int t) {
      return PUtils.alphablit(
         Tempers.rmeter(hibars[t].getRaster(), this.lev[t] + food.h[t], this.max),
         Tempers.rmeter(lobars[t].getRaster(), this.lev[t] + food.l[t], this.max),
         Coord.z
      );
   }

   private WritableRaster lgmeter(GobbleInfo food, double e, int t) {
      return PUtils.alphablit(
         Tempers.lmeter(hibars[t].getRaster(), this.lev[t] + food.h[t], this.max),
         Tempers.lmeter(lobars[t].getRaster(), this.lev[t] + food.l[t], this.max),
         Coord.z
      );
   }

   @Override
   public void draw(BufferedImage buf) {
      WritableRaster dst = buf.getRaster();
      PUtils.blit(dst, bg.getRaster(), Coord.z);
      PUtils.alphablit(dst, Tempers.rmeter(Tempers.sbars[0].getRaster(), this.lmax[0], this.max), Tempers.mc[0]);
      PUtils.alphablit(dst, Tempers.lmeter(Tempers.sbars[1].getRaster(), this.lmax[1], this.max), Tempers.mc[1].sub(Tempers.bars[1].getWidth() - 1, 0));
      PUtils.alphablit(dst, Tempers.lmeter(Tempers.sbars[2].getRaster(), this.lmax[2], this.max), Tempers.mc[2].sub(Tempers.bars[2].getWidth() - 1, 0));
      PUtils.alphablit(dst, Tempers.rmeter(Tempers.sbars[3].getRaster(), this.lmax[3], this.max), Tempers.mc[3]);
      if (this.lfood != null) {
         double e = this.foodeff(this.lfood);
         PUtils.alphablit(dst, this.rgmeter(this.lfood, e, 0), Tempers.mc[0]);
         PUtils.alphablit(dst, this.lgmeter(this.lfood, e, 1), Tempers.mc[1].sub(Tempers.bars[1].getWidth() - 1, 0));
         PUtils.alphablit(dst, this.lgmeter(this.lfood, e, 2), Tempers.mc[2].sub(Tempers.bars[1].getWidth() - 1, 0));
         PUtils.alphablit(dst, this.rgmeter(this.lfood, e, 3), Tempers.mc[3]);
      }

      PUtils.alphablit(dst, Tempers.rmeter(Tempers.bars[0].getRaster(), this.lev[0], this.max), Tempers.mc[0]);
      PUtils.alphablit(dst, Tempers.lmeter(Tempers.bars[1].getRaster(), this.lev[1], this.max), Tempers.mc[1].sub(Tempers.bars[1].getWidth() - 1, 0));
      PUtils.alphablit(dst, Tempers.lmeter(Tempers.bars[2].getRaster(), this.lev[2], this.max), Tempers.mc[2].sub(Tempers.bars[2].getWidth() - 1, 0));
      PUtils.alphablit(dst, Tempers.rmeter(Tempers.bars[3].getRaster(), this.lev[3], this.max), Tempers.mc[3]);
      StringBuilder tbuf = new StringBuilder();

      for (int i = 0; i < 4; i++) {
         tbuf.append(String.format("%s: %s/%s\n", Tempers.rnm[i], Utils.fpformat(this.lev[i], 3, 1), Utils.fpformat(this.lmax[i], 3, 1)));
      }

      this.tooltip = RichText.render(tbuf.toString(), 0).tex();
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      if (this.lvlmask != null) {
         long now = System.currentTimeMillis();
         if (now - this.lvltime > 1000L) {
            this.lvlmask.dispose();
            this.lvlmask = null;
         } else {
            g.chcolor(255, 255, 255, 255 - (int)(255L * (now - this.lvltime) / 1000L));
            g.image(this.lvlmask, Coord.z);
         }
      }
   }

   public void updt(int[] n) {
      this.lev = n;
      this.redraw();
   }

   public void lvlup(int a) {
      WritableRaster buf = PUtils.imgraster(PUtils.imgsz(bg));
      if (a != 0 && a != 3) {
         PUtils.alphablit(buf, Tempers.lmeter(Tempers.bars[a].getRaster(), 1, 1), Tempers.mc[a].sub(Tempers.bars[a].getWidth() - 1, 0));
      } else {
         PUtils.alphablit(buf, Tempers.rmeter(Tempers.bars[a].getRaster(), 1, 1), Tempers.mc[a]);
      }

      PUtils.imgblur(buf, 2, 2.0);
      this.lvlmask = new TexI(PUtils.rasterimg(buf));
      this.lvltime = System.currentTimeMillis();
   }

   public void lcount(int n, Color c) {
      Text rt = tnf.render(String.format("Gobble Points: %d", n), c);
      this.levels = new TexI(PUtils.rasterimg(PUtils.blurmask2(rt.img.getRaster(), 2, 1, new Color(0, 0, 0))));
   }

   public void typemod(Indir<Resource> t, double a) {
      this.updt = true;

      for (Gobble.TypeMod m : this.mods) {
         if (m.t == t) {
            m.a = a;
            m.ra = null;
            return;
         }
      }

      this.mods.add(new Gobble.TypeMod(t, a));
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      return !c.isect(Tempers.boxc, Tempers.boxsz) ? null : super.tooltip(c, prev);
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "gtm") {
         int[] n = new int[4];

         for (int i = 0; i < 4; i++) {
            n[i] = (Integer)args[i];
         }

         this.updt(n);
      } else if (msg == "glvlup") {
         this.lvlup((Integer)args[0]);
      } else if (msg == "glvls") {
         this.lcount((Integer)args[0], (Color)args[1]);
      } else if (msg == "gtypemod") {
         this.typemod(this.ui.sess.getres((Integer)args[0]), ((Integer)args[1]).intValue() / 100.0);
      } else {
         super.uimsg(msg, args);
      }
   }

   static {
      int n = Tempers.bars.length;
      BufferedImage[] l = new BufferedImage[n];
      BufferedImage[] h = new BufferedImage[n];

      for (int i = 0; i < n; i++) {
         l[i] = PUtils.monochromize(Tempers.bars[i], loc);
         h[i] = PUtils.monochromize(Tempers.bars[i], hic);
      }

      lobars = l;
      hibars = h;
      msgs = Arrays.asList("gtm", "glvlup", "glvls", "gtypemod");
   }

   private class TypeList extends Widget {
      private int nw;

      private TypeList(Coord c, Widget parent) {
         super(c, Coord.z, parent);
      }

      @Override
      public void tick(double dt) {
         if (Gobble.this.updt) {
            this.nw = 0;
            double cm = 0.0;
            int aw = 0;

            for (Gobble.TypeMod m : Gobble.this.mods) {
               if (m.rn == null) {
                  try {
                     BufferedImage img = m.t.get().layer(Resource.imgc).img;
                     String nm = m.t.get().layer(Resource.tooltip).t;
                     Text rt = Gobble.tnf.render(nm);
                     int h = Inventory.sqsz.y;
                     BufferedImage buf = TexI.mkbuf(new Coord(img.getWidth() + 10 + rt.sz().x, h));
                     Graphics g = buf.getGraphics();
                     g.drawImage(img, 0, (h - img.getHeight()) / 2, null);
                     g.drawImage(rt.img, img.getWidth() + 10, (h - rt.sz().y) / 2, null);
                     g.dispose();
                     m.rn = new TexI(PUtils.rasterimg(PUtils.blurmask2(buf.getRaster(), 2, 1, new Color(32, 0, 0))));
                     m.rh = new TexI(PUtils.rasterimg(PUtils.blurmask2(buf.getRaster(), 2, 1, new Color(192, 128, 0))));
                  } catch (Loading var14) {
                  }
               }

               if (m.ra == null) {
                  if (m.a * 100.0 > 10.0) {
                     cm = 10.0;
                  } else {
                     cm = m.a * 100.0;
                  }

                  Text rt = Gobble.tnf
                     .render((int)Math.round(m.a * 100.0) + " ", new Color(255, (int)(255.0 - 25.5 * (cm - 1.0)), (int)(255.0 - 25.5 * (cm - 1.0))));
                  m.ra = new TexI(PUtils.rasterimg(PUtils.blurmask2(rt.img.getRaster(), 2, 1, new Color(0, 0, 0))));
               }

               this.nw = Math.max(this.nw, m.rn.sz().x);
               aw = Math.max(aw, m.ra.sz().x);
            }

            int h = (Inventory.sqsz.y + 5) * Gobble.this.mods.size();
            h += Gobble.this.levels.sz().y + 20;
            this.resize(new Coord(Math.max(this.nw + 20 + aw, Tempers.boxsz.x), h));
            this.c = Gobble.this.parentpos(this.parent).add(Tempers.boxc).add(0, Tempers.boxsz.y + 5);
            Gobble.this.updt = false;
         }
      }

      @Override
      public void draw(GOut g) {
         int tn = 0;
         int y = 0;
         int h = Inventory.sqsz.y;
         boolean[] hl = new boolean[Gobble.this.mods.size()];
         if (Gobble.this.lfood != null) {
            for (int t : Gobble.this.lfood.types) {
               hl[t] = true;
            }
         }

         g.aimage(Gobble.this.levels, new Coord(this.sz.x / 2, y), 0.5, 0.0);
         y += Gobble.this.levels.sz().y + 20;

         for (Gobble.TypeMod m : Gobble.this.mods) {
            if (m.rn != null) {
               g.image(hl[tn] ? m.rh : m.rn, new Coord(0, y));
            }

            if (m.ra != null) {
               g.image(m.ra, new Coord(this.nw + 20, y + (h - m.ra.sz().y) / 2));
            }

            tn++;
            y += h + 5;
         }
      }
   }

   public static class TypeMod {
      public final Indir<Resource> t;
      public double a;
      private Tex rn;
      private Tex rh;
      private Tex ra;

      public TypeMod(Indir<Resource> t, double a) {
         this.t = t;
         this.a = a;
      }
   }
}
