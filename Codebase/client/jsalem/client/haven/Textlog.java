package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.util.LinkedList;
import java.util.List;

public class Textlog extends Widget {
   static Tex texpap = Resource.loadtex("gfx/hud/texpap");
   static Tex schain = Resource.loadtex("gfx/hud/schain");
   static Tex sflarp = Resource.loadtex("gfx/hud/sflarp");
   static RichText.Foundry fnd = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 9, TextAttribute.FOREGROUND, Color.BLACK);
   List<Text> lines;
   int maxy;
   int cury;
   int margin = 3;
   boolean sdrag = false;

   @Override
   public void draw(GOut g) {
      Coord dc = new Coord();

      for (dc.y = 0; dc.y < this.sz.y; dc.y = dc.y + texpap.sz().y) {
         for (dc.x = 0; dc.x < this.sz.x; dc.x = dc.x + texpap.sz().x) {
            g.image(texpap, dc);
         }
      }

      g.chcolor();
      int y = -this.cury;
      synchronized (this.lines) {
         for (Text line : this.lines) {
            int dy1 = this.sz.y + y;
            int dy2 = dy1 + line.sz().y;
            if (dy2 > 0 && dy1 < this.sz.y) {
               g.image(line.tex(), new Coord(this.margin, dy1));
            }

            y += line.sz().y;
         }
      }

      if (this.maxy > this.sz.y) {
         int fx = this.sz.x - sflarp.sz().x;
         int cx = fx + sflarp.sz().x / 2 - schain.sz().x / 2;

         for (int var11 = 0; var11 < this.sz.y; var11 += schain.sz().y - 1) {
            g.image(schain, new Coord(cx, var11));
         }

         double a = (double)(this.cury - this.sz.y) / (this.maxy - this.sz.y);
         int fy = (int)((this.sz.y - sflarp.sz().y) * a);
         g.image(sflarp, new Coord(fx, fy));
      }
   }

   public Textlog(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
      this.lines = new LinkedList<>();
      this.maxy = this.cury = 0;
   }

   public void append(String line, Color col) {
      Text rl;
      if (col == null) {
         rl = fnd.render(RichText.Parser.quote(line), this.sz.x - this.margin * 2 - sflarp.sz().x);
      } else {
         rl = fnd.render(RichText.Parser.quote(line), this.sz.x - this.margin * 2 - sflarp.sz().x, TextAttribute.FOREGROUND, col);
      }

      synchronized (this.lines) {
         this.lines.add(rl);
      }

      if (this.cury == this.maxy) {
         this.cury = this.cury + rl.sz().y;
      }

      this.maxy = this.maxy + rl.sz().y;
   }

   public void append(String line) {
      this.append(line, null);
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "apnd") {
         this.append((String)args[0]);
      }
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      this.cury += amount * 20;
      if (this.cury < this.sz.y) {
         this.cury = this.sz.y;
      }

      if (this.cury > this.maxy) {
         this.cury = this.maxy;
      }

      return true;
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      if (button != 1) {
         return false;
      } else {
         int fx = this.sz.x - sflarp.sz().x;
         int cx = fx + sflarp.sz().x / 2 - schain.sz().x / 2;
         if (this.maxy > this.sz.y && c.x >= fx) {
            this.sdrag = true;
            this.ui.grabmouse(this);
            this.mousemove(c);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public void mousemove(Coord c) {
      if (this.sdrag) {
         double a = (double)(c.y - sflarp.sz().y / 2) / (this.sz.y - sflarp.sz().y);
         if (a < 0.0) {
            a = 0.0;
         }

         if (a > 1.0) {
            a = 1.0;
         }

         this.cury = (int)(a * (this.maxy - this.sz.y)) + this.sz.y;
      }
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      if (button == 1 && this.sdrag) {
         this.sdrag = false;
         this.ui.grabmouse(null);
         return true;
      } else {
         return false;
      }
   }

   @Widget.RName("log")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Textlog(c, (Coord)args[0], parent);
      }
   }
}
