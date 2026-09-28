package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public class FlowerMenu extends Widget {
   public static final Tex pbgl = Resource.loadtex("gfx/hud/fpl");
   public static final Tex pbgm = Resource.loadtex("gfx/hud/fpm");
   public static final Tex pbgr = Resource.loadtex("gfx/hud/fpr");
   static Color ptc = new Color(248, 240, 193);
   static Text.Foundry ptf = new Text.Foundry(new Font("SansSerif", 0, 12));
   static int ph = pbgm.sz().y;
   static int ppl = 8;
   public FlowerMenu.Petal[] opts;
   private double fast_menu1;
   private double fast_menu2;
   private FlowerMenu.Petal autochoose = null;

   private static Rectangle organize(FlowerMenu.Petal[] opts) {
      int l = 1;
      int p = 0;
      int i = 0;
      int lr = -1;
      Coord min = new Coord(Integer.MAX_VALUE, Integer.MAX_VALUE);
      Coord max = new Coord(Integer.MIN_VALUE, Integer.MIN_VALUE);

      for (int var9 = 0; var9 < opts.length; var9++) {
         if (lr == -1) {
            lr = 75 + 50 * (l - 1);
         }

         FlowerMenu.Petal petal = opts[var9];
         petal.ta = (Math.PI / 2) - p * ((Math.PI * 2) / (l * ppl));
         petal.tr = lr;
         if (++p >= ppl * l) {
            l++;
            p = 0;
            lr = -1;
         }

         Coord tc = Coord.sc(petal.ta, petal.tr).sub(petal.sz.div(2));
         max.x = Math.max(max.x, tc.x + petal.sz.x);
         max.y = Math.max(max.y, tc.y + petal.sz.y);
         min.x = Math.min(min.x, tc.x);
         min.y = Math.min(min.y, tc.y);
      }

      return new Rectangle(min.x, min.y, max.x - min.x, max.y - min.y);
   }

   public FlowerMenu(Coord c, Widget parent, String... options) {
      super(c, Coord.z, parent);
      FlowerMenu.Petal study = null;
      FlowerMenu.Petal split = null;
      if (Config.flower_study) {
         for (int i = 0; i < options.length; i++) {
            if (options[i].equals("Study")) {
               study = new FlowerMenu.Petal(options[i]);
               study.num = i;
               break;
            }
         }

         for (int ix = 0; ix < options.length; ix++) {
            String name = options[ix];
            FlowerMenu.Petal p = new FlowerMenu.Petal(name);
            p.num = ix;
            boolean auto = Config.AUTOCHOOSE.containsKey(name) && Config.AUTOCHOOSE.get(name);
            boolean single = this.ui.modctrl && options.length == 1 && Config.singleItemCTRLChoose;
            if (!this.ui.modshift && (auto || single)) {
               this.autochoose = p;
            }

            this.opts[ix] = p;
         }
      }

      if (study == null) {
         if (split == null) {
            this.opts = new FlowerMenu.Petal[options.length];

            for (int ix = 0; ix < options.length; this.opts[ix].num = ix++) {
               this.opts[ix] = new FlowerMenu.Petal(options[ix]);
            }
         } else {
            this.opts = new FlowerMenu.Petal[]{split};
         }
      } else {
         this.opts = new FlowerMenu.Petal[]{study};
      }

      this.fitscreen(organize(this.opts));
      this.ui.grabmouse(this);
      this.ui.grabkeys(this);
      new FlowerMenu.Opening();
   }

   private void fitscreen(Rectangle rect) {
      Coord ssz = this.ui.gui.sz;
      Coord wsz = new Coord(rect.width, rect.height);
      Coord wc = this.c.add(rect.x, rect.y);
      if (wc.x < 0) {
         this.c.x = this.c.x - wc.x;
      }

      if (wc.y < 0) {
         this.c.y = this.c.y - wc.y;
      }

      if (wc.x + wsz.x > ssz.x) {
         this.c.x = this.c.x - (wc.x + wsz.x - ssz.x);
      }

      if (wc.y + wsz.y > ssz.y) {
         this.c.y = this.c.y - (wc.y + wsz.y - ssz.y);
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (!this.anims.isEmpty()) {
         return true;
      } else {
         if (!super.mousedown(c, button)) {
            this.choose(null);
         }

         return true;
      }
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "cancel") {
         new FlowerMenu.Cancel();
         this.ui.grabmouse(null);
         this.ui.grabkeys(null);
      } else if (msg == "act") {
         new FlowerMenu.Chosen(this.opts[this.get((Integer)args[0])]);
         this.ui.grabmouse(null);
         this.ui.grabkeys(null);
      }
   }

   private int get(int num) {
      int i = 0;

      for (FlowerMenu.Petal p : this.opts) {
         if (p.num == num) {
            return i;
         }

         i++;
      }

      return 0;
   }

   @Override
   public void tick(double dt) {
      if (this.autochoose != null) {
         this.choose(this.autochoose);
         this.autochoose = null;
      }

      super.tick(dt);
   }

   @Override
   public void draw(GOut g) {
      super.draw(g, false);
   }

   @Override
   public boolean type(char key, KeyEvent ev) {
      if (key >= '0' && key <= '9') {
         int opt = key == '0' ? 10 : key - '1';
         if (opt < this.opts.length) {
            this.choose(this.opts[opt]);
         }

         this.ui.grabkeys(null);
         return true;
      } else if (key == 27) {
         this.choose(null);
         this.ui.grabkeys(null);
         return true;
      } else {
         return false;
      }
   }

   public void choose(FlowerMenu.Petal option) {
      if (option == null) {
         this.wdgmsg("cl", new Object[]{-1});
      } else {
         this.wdgmsg("cl", new Object[]{option.num, this.ui.modflags()});
      }
   }

   @Widget.RName("sm")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         if (c.x == -1 && c.y == -1) {
            c = parent.ui.lcc;
         }

         String[] opts = new String[args.length];

         for (int i = 0; i < args.length; i++) {
            opts[i] = (String)args[i];
         }

         return new FlowerMenu(c, parent, opts);
      }
   }

   public class Cancel extends Widget.NormAnim {
      Cancel() {
         super(FlowerMenu.this.fast_menu1);
      }

      @Override
      public void ntick(double s) {
         for (FlowerMenu.Petal p : FlowerMenu.this.opts) {
            p.move(p.ta, p.tr * (1.0 + s));
            p.a = 1.0 - s;
         }

         if (s == 1.0) {
            FlowerMenu.this.ui.destroy(FlowerMenu.this);
         }
      }
   }

   public class Chosen extends Widget.NormAnim {
      FlowerMenu.Petal chosen;

      Chosen(FlowerMenu.Petal c) {
         super(FlowerMenu.this.fast_menu2);
         this.chosen = c;
      }

      @Override
      public void ntick(double s) {
         for (FlowerMenu.Petal p : FlowerMenu.this.opts) {
            if (p == this.chosen) {
               if (s > 0.6) {
                  p.a = 1.0 - (s - 0.6) / 0.4;
               } else if (s < 0.3) {
                  p.move(p.ta, p.tr * (1.0 - s / 0.3));
                  p.a = 1.0;
               }
            } else if (s > 0.3) {
               p.a = 0.0;
            } else {
               p.a = 1.0 - s / 0.3;
               p.move(p.ta - s * Math.PI, p.tr);
            }
         }

         if (s == 1.0) {
            FlowerMenu.this.ui.destroy(FlowerMenu.this);
         }
      }
   }

   public class Opening extends Widget.NormAnim {
      Opening() {
         super(FlowerMenu.this.fast_menu1);
      }

      @Override
      public void ntick(double s) {
         for (FlowerMenu.Petal p : FlowerMenu.this.opts) {
            p.move(p.ta, p.tr * (2.0 - s));
            p.a = s;
         }
      }
   }

   public class Petal extends Widget {
      public String name;
      public double ta;
      public double tr;
      public int num;
      Tex text;
      double a = 1.0;

      public Petal(String name) {
         super(Coord.z, Coord.z, FlowerMenu.this);
         this.name = name;
         this.text = new TexI(Utils.outline2(FlowerMenu.ptf.render(name, FlowerMenu.ptc).img, Utils.contrast(FlowerMenu.ptc)));
         this.sz = new Coord(this.text.sz().x + 25, FlowerMenu.ph);
         if (Config.fast_menu) {
            FlowerMenu.this.fast_menu1 = 0.0;
            FlowerMenu.this.fast_menu2 = 0.0;
         } else {
            FlowerMenu.this.fast_menu1 = 0.25;
            FlowerMenu.this.fast_menu2 = 0.75;
         }
      }

      public void move(Coord c) {
         this.c = c.add(this.sz.div(2).inv());
      }

      public void move(double a, double r) {
         this.move(Coord.sc(a, r));
      }

      @Override
      public void draw(GOut g) {
         g.chcolor(255, 255, 255, (int)(255.0 * this.a));
         g.image(FlowerMenu.pbgl, Coord.z);
         g.image(FlowerMenu.pbgm, new Coord(FlowerMenu.pbgl.sz().x, 0), new Coord(this.sz.x - FlowerMenu.pbgl.sz().x - FlowerMenu.pbgr.sz().x, this.sz.y));
         g.image(FlowerMenu.pbgr, new Coord(this.sz.x - FlowerMenu.pbgr.sz().x, 0));
         g.image(this.text, this.sz.div(2).add(this.text.sz().div(2).inv()));
      }

      @Override
      public boolean mousedown(Coord c, int button) {
         FlowerMenu.this.choose(this);
         return true;
      }
   }
}
