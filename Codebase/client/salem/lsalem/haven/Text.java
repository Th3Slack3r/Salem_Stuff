package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Text {
   public static final Text.Foundry std = new Text.Foundry(new Font("SansSerif", 0, 10));
   public final BufferedImage img;
   public final String text;
   private Tex tex;
   public static final Color black = Color.BLACK;
   public static final Color white = Color.WHITE;

   public static int[] findspaces(String text) {
      List<Integer> l = new ArrayList<>();

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (Character.isWhitespace(c)) {
            l.add(i);
         }
      }

      int[] ret = new int[l.size()];

      for (int ix = 0; ix < ret.length; ix++) {
         ret[ix] = l.get(ix);
      }

      return ret;
   }

   protected Text(String text, BufferedImage img) {
      this.text = text;
      this.img = img;
   }

   public Coord sz() {
      return Utils.imgsz(this.img);
   }

   public static Text.Line render(String text, Color c) {
      return std.render(text, c);
   }

   public static Text.Line renderf(Color c, String text, Object... args) {
      return std.render(String.format(text, args), c);
   }

   public static Text.Line render(String text) {
      return render(text, Color.WHITE);
   }

   public Tex tex() {
      if (this.tex == null) {
         this.tex = new TexI(this.img);
      }

      return this.tex;
   }

   public static void main(String[] args) throws Exception {
      String cmd = args[0].intern();
      if (cmd == "render") {
         PosixArgs opt = PosixArgs.getopt(args, 1, "aw:f:s:");
         boolean aa = false;
         String font = "SansSerif";
         int width = 100;
         int size = 10;

         for (char c : opt.parsed()) {
            if (c == 'a') {
               aa = true;
            } else if (c == 'f') {
               font = opt.arg;
            } else if (c == 'w') {
               width = Integer.parseInt(opt.arg);
            } else if (c == 's') {
               size = Integer.parseInt(opt.arg);
            }
         }

         Text.Foundry f = new Text.Foundry(font, size);
         f.aa = aa;
         Text t = f.renderwrap(opt.rest[0], width);
         OutputStream out = new FileOutputStream(opt.rest[1]);
         ImageIO.write(t.img, "PNG", out);
         out.close();
      }
   }

   public static class Foundry extends Text.Furnace {
      private FontMetrics m;
      Font font;
      Color defcol;
      public boolean aa = false;
      private RichText.Foundry wfnd = null;

      public Foundry(Font f, Color defcol) {
         this.font = f;
         this.defcol = defcol;
         BufferedImage junk = TexI.mkbuf(new Coord(10, 10));
         Graphics tmpl = junk.getGraphics();
         tmpl.setFont(f);
         this.m = tmpl.getFontMetrics();
      }

      public Foundry(Font f) {
         this(f, Color.WHITE);
      }

      public Foundry(String font, int psz) {
         this(new Font(font, 0, psz));
      }

      public Foundry(String font, int psz, int style) {
         this(new Font(font, style, psz));
      }

      public Text.Foundry aa(boolean aa) {
         this.aa = aa;
         return this;
      }

      public int height() {
         return this.m.getAscent() + this.m.getDescent();
      }

      private Coord strsize(String text) {
         return new Coord(this.m.stringWidth(text), this.height());
      }

      public Text renderwrap(String text, Color c, int width) {
         if (this.wfnd == null) {
            this.wfnd = new RichText.Foundry(this.font, this.defcol);
         }

         this.wfnd.aa = this.aa;
         text = RichText.Parser.quote(text);
         if (c != null) {
            text = String.format("$col[%d,%d,%d,%d]{%s}", c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha(), text);
         }

         return this.wfnd.render(text, width);
      }

      public Text renderwrap(String text, int width) {
         return this.renderwrap(text, null, width);
      }

      public Text.Line render(String text, Color c) {
         text = Translate.get(text);
         Coord sz = this.strsize(text);
         if (sz.x < 1) {
            sz = sz.add(1, 0);
         }

         BufferedImage img = TexI.mkbuf(sz);
         Graphics g = img.createGraphics();
         if (this.aa) {
            Utils.AA(g);
         }

         g.setFont(this.font);
         g.setColor(c);
         FontMetrics m = g.getFontMetrics();
         g.drawString(text, 0, m.getAscent());
         g.dispose();
         return new Text.Line(text, img, m);
      }

      public Text.Line render(String text) {
         return this.render(text, this.defcol);
      }
   }

   public abstract static class Furnace {
      public abstract Text render(String var1);

      public Text renderf(String fmt, Object... args) {
         return this.render(String.format(fmt, args));
      }
   }

   public abstract static class Imager extends Text.Furnace {
      private final Text.Furnace back;

      public Imager(Text.Furnace back) {
         this.back = back;
      }

      protected abstract BufferedImage proc(Text var1);

      @Override
      public Text render(String text) {
         return new Text(text, this.proc(this.back.render(text)));
      }
   }

   public static class Line extends Text {
      private final FontMetrics m;

      private Line(String text, BufferedImage img, FontMetrics m) {
         super(text, img);
         this.m = m;
      }

      public Coord base() {
         return new Coord(0, this.m.getAscent());
      }

      public int advance(int pos) {
         return this.m.stringWidth(this.text.substring(0, pos));
      }

      public int charat(int x) {
         int l = 0;
         int r = this.text.length() + 1;

         while (true) {
            int p = (l + r) / 2;
            int a = this.advance(p);
            if (a >= x || l >= p) {
               if (a <= x || r <= p) {
                  return p;
               }

               r = p;
            } else {
               l = p;
            }
         }
      }
   }

   public abstract static class UText implements Indir<Text> {
      public final Text.Furnace fnd;
      private Text cur = null;

      public UText(Text.Furnace fnd) {
         this.fnd = fnd;
      }

      protected abstract String text();

      public Text get() {
         String text = this.text();
         if (this.cur == null || !this.cur.text.equals(text)) {
            this.cur = this.fnd.render(text);
         }

         return this.cur;
      }

      public Indir<Tex> tex() {
         return new Indir<Tex>() {
            public Tex get() {
               return UText.this.get().tex();
            }
         };
      }

      public static Text.UText forfield(Text.Furnace fnd, final Object obj, String fn) {
         final Field f;
         try {
            f = obj.getClass().getField(fn);
         } catch (NoSuchFieldException var5) {
            throw new RuntimeException(var5);
         }

         if (f.getType() != String.class) {
            throw new RuntimeException("Not a string field: " + f);
         } else {
            return new Text.UText(fnd) {
               @Override
               public String text() {
                  try {
                     return (String)f.get(obj);
                  } catch (IllegalAccessException var2) {
                     throw new RuntimeException(var2);
                  }
               }
            };
         }
      }

      public static Text.UText forfield(Object obj, String fn) {
         return forfield(Text.std, obj, fn);
      }
   }
}
