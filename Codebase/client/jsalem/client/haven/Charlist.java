package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Charlist extends Widget {
   public static final Tex bg = Resource.loadtex("gfx/hud/avakort");
   public static final int margin = 1;
   public static final int bmargin = 46;
   public static final BufferedImage[] clu = new BufferedImage[]{
      Resource.loadimg("gfx/hud/login/cluu"), Resource.loadimg("gfx/hud/login/clud"), Resource.loadimg("gfx/hud/login/cluh")
   };
   public static final BufferedImage[] cld = new BufferedImage[]{
      Resource.loadimg("gfx/hud/login/cldu"), Resource.loadimg("gfx/hud/login/cldd"), Resource.loadimg("gfx/hud/login/cldh")
   };
   public int height;
   public int y;
   public IButton sau;
   public IButton sad;
   public List<Charlist.Char> chars = new ArrayList<>();

   public Charlist(Coord c, Widget parent, int height) {
      super(c, new Coord(clu[0].getWidth(), 92 + bg.sz().y * height + 1 * (height - 1)), parent);
      this.height = height;
      this.y = 0;
      this.sau = new IButton(new Coord(0, 0), this, clu[0], clu[1], clu[2]) {
         @Override
         public void click() {
            Charlist.this.scroll(-1);
         }
      };
      this.sad = new IButton(new Coord(0, this.sz.y - cld[0].getHeight() - 1), this, cld[0], cld[1], cld[2]) {
         @Override
         public void click() {
            Charlist.this.scroll(1);
         }
      };
      this.sau.hide();
      this.sad.hide();
   }

   public void scroll(int amount) {
      this.y += amount;
      synchronized (this.chars) {
         if (this.y > this.chars.size() - this.height) {
            this.y = this.chars.size() - this.height;
         }
      }

      if (this.y < 0) {
         this.y = 0;
      }
   }

   @Override
   public void draw(GOut g) {
      Coord cc = new Coord((clu[0].getWidth() - bg.sz().x) / 2, 46);
      synchronized (this.chars) {
         for (Charlist.Char c : this.chars) {
            c.plb.hide();
         }

         for (int i = 0; i < this.height && i + this.y < this.chars.size(); i++) {
            Charlist.Char c = this.chars.get(i + this.y);
            g.image(bg, cc);
            c.plb.show();
            c.plb.c = cc.add(bg.sz()).sub(110, 30);
            g.image(c.nt.tex(), cc.add(15, 10));
            cc = cc.add(0, bg.sz().y + 1);
         }
      }

      super.draw(g);
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      this.scroll(amount);
      return true;
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender instanceof Button) {
         synchronized (this.chars) {
            for (Charlist.Char c : this.chars) {
               if (sender == c.plb) {
                  this.wdgmsg("play", new Object[]{c.name});
               }
            }
         }
      } else if (!(sender instanceof Avaview)) {
         super.wdgmsg(sender, msg, args);
      }
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "add") {
         Charlist.Char c = new Charlist.Char((String)args[0]);
         List<Indir<Resource>> resl = new LinkedList<>();

         for (int i = 1; i < args.length; i++) {
            resl.add(this.ui.sess.getres((Integer)args[i]));
         }

         c.plb = new Button(new Coord(0, 0), 100, this, "Play");
         c.plb.hide();
         synchronized (this.chars) {
            this.chars.add(c);
            if (this.chars.size() > this.height) {
               this.sau.show();
               this.sad.show();
            }
         }
      }
   }

   @Widget.RName("charlist")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Charlist(c, parent, (Integer)args[0]);
      }
   }

   public static class Char {
      static Text.Furnace tf = new Text.Imager(new Text.Foundry(new Font("Serif", 0, 20), Color.WHITE).aa(true)) {
         @Override
         protected BufferedImage proc(Text text) {
            return PUtils.rasterimg(PUtils.blurmask2(text.img.getRaster(), 1, 1, Color.BLACK));
         }
      };
      public String name;
      Text nt;
      Button plb;

      public Char(String name) {
         this.name = name;
         this.nt = tf.render(name);
      }
   }
}
