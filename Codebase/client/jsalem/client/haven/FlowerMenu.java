package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

public class FlowerMenu extends Widget {
   public static final Tex pbgl = Resource.loadtex("gfx/hud/fpl");
   public static final Tex pbgm = Resource.loadtex("gfx/hud/fpm");
   public static final Tex pbgr = Resource.loadtex("gfx/hud/fpr");
   static Color ptc = new Color(248, 240, 193);
   static Text.Foundry ptf = new Text.Foundry(new Font("SansSerif", 0, 12));
   static int ph = pbgm.sz().y;
   static int ppl = 8;
   FlowerMenu.Petal[] opts;

   private static void organize(FlowerMenu.Petal[] opts) {
      int l = 1;
      int p = 0;
      int i = 0;
      int lr = -1;

      for (int var5 = 0; var5 < opts.length; var5++) {
         if (lr == -1) {
            lr = 75 + 50 * (l - 1);
         }

         opts[var5].ta = (Math.PI / 2) - p * ((Math.PI * 2) / (l * ppl));
         opts[var5].tr = lr;
         if (++p >= ppl * l) {
            l++;
            p = 0;
            lr = -1;
         }
      }
   }

   public FlowerMenu(Coord c, Widget parent, String... options) {
      super(c, Coord.z, parent);
      this.opts = new FlowerMenu.Petal[options.length];

      for (int i = 0; i < options.length; this.opts[i].num = i++) {
         this.opts[i] = new FlowerMenu.Petal(options[i]);
      }

      organize(this.opts);
      this.ui.grabmouse(this);
      this.ui.grabkeys(this);
      new FlowerMenu.Opening();
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
         new FlowerMenu.Chosen(this.opts[args[0]]);
         this.ui.grabmouse(null);
         this.ui.grabkeys(null);
      }
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
            super(0.25);
         }
      
         @Override
         public void ntick(double s) {
            for (FlowerMenu.Petal p : FlowerMenu.this.opts) {
               p.move(p.ta, p.tr * (1.0 + s));
               p.a = 1.0 - s;
            }
         
            if (s == 1.0) {
               Config.saveAutoChoose();
               FlowerMenu.this.ui.destroy(FlowerMenu.this);
            }
         }
      }

   public class Chosen extends Widget.NormAnim {
      FlowerMenu.Petal chosen;

      Chosen(FlowerMenu.Petal c) {
         super(0.75);
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
            super(0.25);
         }
      
         @Override
         public void ntick(double s) {
            for (FlowerMenu.Petal p : FlowerMenu.this.opts) {
               p.move(p.ta, p.tr * (2.0 - s));
               p.a = s;
            }
         
            // Auto-choose "Open" option if configured
            boolean autoopen = false;
            if (FlowerMenu.AUTOCHOOSE != null) {
               for (Map.Entry<String, Boolean> entry : FlowerMenu.AUTOCHOOSE.entrySet()) {
                  if (entry.getValue()) {
                     String name = null;
                     for (FlowerMenu.Petal petal : FlowerMenu.this.opts) {
                        if (petal.name != null && petal.name.equals(entry.getKey())) {
                           name = petal.name;
                           break;
                        }
                     }
                     if (name != null && name.toLowerCase().contains("open")) {
                        autoopen = true;
                        break;
                     }
                  }
               }
            }
         
            if (autoopen) {
               for (FlowerMenu.Petal petal : FlowerMenu.this.opts) {
                  if (petal.name != null && petal.name.toLowerCase().contains("open")) {
                     FlowerMenu.this.choose(petal);
                     break;
                  }
               }
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
         private Petal autochoose = null;

         public Petal(String name) {
            super(Coord.z, Coord.z, FlowerMenu.this);
            this.name = name;
            this.text = new TexI(Utils.outline2(FlowerMenu.ptf.render(name, FlowerMenu.ptc).img, Utils.contrast(FlowerMenu.ptc)));;
            this.sz = new Coord(this.text.sz().x + 25, FlowerMenu.ph);
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
