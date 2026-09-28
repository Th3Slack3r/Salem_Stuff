package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.font.TextMeasurer;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;

public class RichText extends Text {
   public static final RichText.Parser std;
   public static final RichText.Foundry stdf;
   public final RichText.Part parts;

   private RichText(String text, BufferedImage img, RichText.Part parts) {
      super(text, img);
      this.parts = parts;
   }

   public RichText.Part partat(Coord c) {
      for (RichText.Part p = this.parts; p != null; p = p.next) {
         if (c.x >= p.x && c.y >= p.y && c.x < p.x + p.width() && c.y < p.y + p.height()) {
            return p;
         }
      }

      return null;
   }

   public AttributedCharacterIterator attrat(Coord c) {
      RichText.Part p = this.partat(c);
      if (p != null && p instanceof RichText.TextPart) {
         RichText.TextPart tp = (RichText.TextPart)p;
         AttributedCharacterIterator attr = tp.ti();
         attr.setIndex(tp.charat(c).getCharIndex());
         return attr;
      } else {
         return null;
      }
   }

   public Object attrat(Coord c, Attribute attr) {
      AttributedCharacterIterator ai = this.attrat(c);
      return ai == null ? null : ai.getAttribute(attr);
   }

   public static Map<? extends Attribute, ?> fillattrs2(Map<? extends Attribute, ?> def, Object... attrs) {
      Map<Attribute, Object> a;
      if (def == null) {
         a = new HashMap<>();
      } else {
         a = new HashMap<>((Map<? extends Attribute, ? extends Object>)def);
      }

      for (int i = 0; i < attrs.length; i += 2) {
         a.put((Attribute)attrs[i], attrs[i + 1]);
      }

      return a;
   }

   public static Map<? extends Attribute, ?> fillattrs(Object... attrs) {
      return fillattrs2(null, attrs);
   }

   private static Map<? extends Attribute, ?> fixattrs(Map<? extends Attribute, ?> attrs) {
      Map<Attribute, Object> ret = new HashMap<>();

      for (Entry<? extends Attribute, ?> e : attrs.entrySet()) {
         if (e.getKey() == TextAttribute.SIZE) {
            ret.put(e.getKey(), ((Number)e.getValue()).floatValue());
         } else {
            ret.put(e.getKey(), e.getValue());
         }
      }

      return ret;
   }

   public static RichText render(String text, int width, Object... extra) {
      return stdf.render(text, width, extra);
   }

   public static void main(String[] args) throws Exception {
      String cmd = args[0].intern();
      if (cmd == "render") {
         Map<Attribute, Object> a = new HashMap<>((Map<? extends Attribute, ? extends Object>)std.defattrs);
         PosixArgs opt = PosixArgs.getopt(args, 1, "aw:f:s:");
         boolean aa = false;
         int width = 0;

         for (char c : opt.parsed()) {
            if (c == 'a') {
               aa = true;
            } else if (c == 'f') {
               a.put(TextAttribute.FAMILY, opt.arg);
            } else if (c == 'w') {
               width = Integer.parseInt(opt.arg);
            } else if (c == 's') {
               a.put(TextAttribute.SIZE, Integer.parseInt(opt.arg));
            }
         }

         RichText.Foundry fnd = new RichText.Foundry(a);
         fnd.aa = aa;
         RichText t = fnd.render(opt.rest[0], width);
         OutputStream out = new FileOutputStream(opt.rest[1]);
         ImageIO.write(t.img, "PNG", out);
         out.close();
      } else if (cmd == "pagina") {
         PosixArgs opt = PosixArgs.getopt(args, 1, "aw:");
         boolean aa = false;
         int width = 0;

         for (char cx : opt.parsed()) {
            if (cx == 'a') {
               aa = true;
            } else if (cx == 'w') {
               width = Integer.parseInt(opt.arg);
            }
         }

         RichText.Foundry fnd = new RichText.Foundry();
         fnd.aa = aa;
         Resource res = Resource.load(opt.rest[0]);
         res.loadwaitint();
         Resource.Pagina p = res.layer(Resource.pagina);
         if (p == null) {
            throw new Exception("No pagina in " + res + ", loaded from " + res.source);
         }

         RichText t = fnd.render(p.text, width);
         OutputStream out = new FileOutputStream(opt.rest[1]);
         ImageIO.write(t.img, "PNG", out);
         out.close();
      }
   }

   static {
      Map<Attribute, Object> a = new HashMap<>();
      a.put(TextAttribute.FAMILY, "SansSerif");
      a.put(TextAttribute.SIZE, 10);
      std = new RichText.Parser(a);
      stdf = new RichText.Foundry(std);
   }

   public static class FormatException extends RuntimeException {
      public FormatException(String msg) {
         super(msg);
      }
   }

   public static class Foundry {
      private RichText.Parser parser;
      private RichText.RState rs;
      public boolean aa = false;

      public Foundry(RichText.Parser parser) {
         this.parser = parser;
         BufferedImage junk = TexI.mkbuf(new Coord(10, 10));
         Graphics2D g = junk.createGraphics();
         this.rs = new RichText.RState(g.getFontRenderContext());
      }

      public Foundry(Map<? extends Attribute, ?> defattrs) {
         this(new RichText.Parser(defattrs));
      }

      public Foundry(Object... attrs) {
         this(new RichText.Parser(attrs));
      }

      private static Map<? extends Attribute, ?> xlate(Font f, Color defcol) {
         Map<Attribute, Object> attrs = new HashMap<>();
         attrs.put(TextAttribute.FONT, f);
         attrs.put(TextAttribute.FOREGROUND, defcol);
         return attrs;
      }

      public Foundry(Font f, Color defcol) {
         this(xlate(f, defcol));
      }

      public RichText.Foundry aa(boolean aa) {
         this.aa = aa;
         return this;
      }

      private static void aline(List<RichText.Part> line, int y) {
         int mb = 0;

         for (RichText.Part p : line) {
            int cb = p.baseline();
            if (cb > mb) {
               mb = cb;
            }
         }

         for (RichText.Part px : line) {
            px.y = y + mb - px.baseline();
         }
      }

      private static RichText.Part layout(RichText.Part fp, int w) {
         List<RichText.Part> line = new LinkedList<>();
         int x = 0;
         int y = 0;
         int mw = 0;
         int lh = 0;
         RichText.Part lp = null;

         for (RichText.Part p = fp; p != null; p = p.next) {
            boolean lb = p instanceof RichText.Newline;

            while (true) {
               p.x = x;
               int pw = p.width();
               int ph = p.height();
               if (w <= 0 || p.x + pw <= w) {
                  lp = p;
                  line.add(p);
                  if (ph > lh) {
                     lh = ph;
                  }

                  x += pw;
                  if (x > mw) {
                     mw = x;
                  }

                  if (lb) {
                     aline(line, y);
                     x = 0;
                     y += lh;
                     lh = 0;
                     line = new LinkedList<>();
                  }
                  break;
               }

               p = p.split(w - x);
               if (lp == null) {
                  fp = p;
               } else {
                  lp.next = p;
               }

               lb = true;
            }
         }

         aline(line, y);
         return fp;
      }

      private static Coord bounds(RichText.Part fp) {
         Coord sz = new Coord(0, 0);

         for (RichText.Part p = fp; p != null; p = p.next) {
            int x = p.x + p.width();
            int y = p.y + p.height();
            if (x > sz.x) {
               sz.x = x;
            }

            if (y > sz.y) {
               sz.y = y;
            }
         }

         return sz;
      }

      public RichText render(String text, int width, Object... extra) {
         Map<? extends Attribute, ?> extram = null;
         if (extra.length > 0) {
            extram = RichText.fillattrs(extra);
         }

         RichText.Part fp = this.parser.parse(text, extram);
         fp.prepare(this.rs);
         fp = layout(fp, width);
         Coord sz = bounds(fp);
         if (sz.x < 1) {
            sz = sz.add(1, 0);
         }

         if (sz.y < 1) {
            sz = sz.add(0, 1);
         }

         BufferedImage img = TexI.mkbuf(sz);
         Graphics2D g = img.createGraphics();
         if (this.aa) {
            Utils.AA(g);
         }

         for (RichText.Part p = fp; p != null; p = p.next) {
            p.render(g);
         }

         return new RichText(text, img, fp);
      }

      public RichText render(String text) {
         return this.render(text, 0);
      }
   }

   public static class Image extends RichText.Part {
      public BufferedImage img;

      public Image(BufferedImage img) {
         this.img = img;
      }

      public Image(Resource res, int id) {
         res.loadwait();

         for (Resource.Image img : res.layers(Resource.imgc)) {
            if (img.id == id) {
               this.img = img.img;
               break;
            }
         }

         if (this.img == null) {
            throw new RuntimeException("Found no image with id " + id + " in " + res.toString());
         }
      }

      @Override
      public int width() {
         return this.img.getWidth();
      }

      @Override
      public int height() {
         return this.img.getHeight();
      }

      @Override
      public int baseline() {
         return this.img.getHeight() - 1;
      }

      @Override
      public void render(Graphics2D g) {
         g.drawImage(this.img, this.x, this.y, null);
      }
   }

   public static class Newline extends RichText.Part {
      private Map<? extends Attribute, ?> attrs;
      private LineMetrics lm;

      public Newline(Map<? extends Attribute, ?> attrs) {
         this.attrs = attrs;
      }

      private LineMetrics lm() {
         if (this.lm == null) {
            Font f;
            if ((f = (Font)this.attrs.get(TextAttribute.FONT)) == null) {
               f = new Font(this.attrs);
            }

            this.lm = f.getLineMetrics("", this.rs.frc);
         }

         return this.lm;
      }

      @Override
      public int height() {
         return (int)this.lm().getHeight();
      }

      @Override
      public int baseline() {
         return (int)this.lm().getAscent();
      }
   }

   public static class Parser {
      private final Map<? extends Attribute, ?> defattrs;

      public Parser(Map<? extends Attribute, ?> defattrs) {
         this.defattrs = RichText.fixattrs(defattrs);
      }

      public Parser(Object... attrs) {
         this(RichText.fillattrs2(RichText.std.defattrs, attrs));
      }

      private static boolean namechar(char c) {
         return c == ':' || c == '_' || c == '$' || c == '.' || c == '-' || c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
      }

      protected String name(PeekReader in) throws IOException {
         StringBuilder buf = new StringBuilder();

         while (true) {
            int c = in.peek();
            if (c < 0 || !namechar((char)c)) {
               if (buf.length() == 0) {
                  throw new RichText.FormatException("Expected name, got `" + (char)in.peek() + "'");
               } else {
                  return buf.toString();
               }
            }

            buf.append((char)in.read());
         }
      }

      protected Color a2col(String[] args) {
         int r = Integer.parseInt(args[0]);
         int g = Integer.parseInt(args[1]);
         int b = Integer.parseInt(args[2]);
         int a = 255;
         if (args.length > 3) {
            a = Integer.parseInt(args[3]);
         }

         return new Color(r, g, b, a);
      }

      protected RichText.Part tag(RichText.Parser.PState s, String tn, String[] args, Map<? extends Attribute, ?> attrs) throws IOException {
         if (tn == "img") {
            Resource res = Resource.load(args[0]);
            int id = -1;
            if (args.length > 1) {
               id = Integer.parseInt(args[1]);
            }

            return new RichText.Image(res, id);
         } else {
            Map<Attribute, Object> na = new HashMap<>((Map<? extends Attribute, ? extends Object>)attrs);
            if (tn == "font") {
               na.put(TextAttribute.FAMILY, args[0]);
               if (args.length > 1) {
                  na.put(TextAttribute.SIZE, Float.parseFloat(args[1]));
               }
            } else if (tn == "size") {
               na.put(TextAttribute.SIZE, Float.parseFloat(args[0]));
            } else if (tn == "b") {
               na.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            } else if (tn == "i") {
               na.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
            } else if (tn == "u") {
               na.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            } else if (tn == "col") {
               na.put(TextAttribute.FOREGROUND, this.a2col(args));
            } else if (tn == "bg") {
               na.put(TextAttribute.BACKGROUND, this.a2col(args));
            }

            if (s.in.peek(true) != 123) {
               throw new RichText.FormatException("Expected `{', got `" + (char)s.in.peek() + "'");
            } else {
               s.in.read();
               return this.text(s, na);
            }
         }
      }

      protected RichText.Part tag(RichText.Parser.PState s, Map<? extends Attribute, ?> attrs) throws IOException {
         s.in.peek(true);
         String tn = this.name(s.in).intern();
         String[] args;
         if (s.in.peek(true) == 91) {
            s.in.read();
            StringBuilder buf = new StringBuilder();

            while (true) {
               int c = s.in.peek();
               if (c < 0) {
                  throw new RichText.FormatException("Unexpected end-of-input when reading tag arguments");
               }

               if (c == 93) {
                  s.in.read();
                  args = buf.toString().split(",");
                  break;
               }

               buf.append((char)s.in.read());
            }
         } else {
            args = new String[0];
         }

         return this.tag(s, tn, args, attrs);
      }

      protected RichText.Part text(RichText.Parser.PState s, String text, Map<? extends Attribute, ?> attrs) throws IOException {
         return new RichText.TextPart(text, attrs);
      }

      protected RichText.Part text(RichText.Parser.PState s, Map<? extends Attribute, ?> attrs) throws IOException {
         RichText.Part buf = new RichText.TextPart("");
         StringBuilder tbuf = new StringBuilder();

         while (true) {
            int c = s.in.read();
            if (c < 0) {
               buf.append(this.text(s, tbuf.toString(), attrs));
               break;
            }

            if (c == 10) {
               buf.append(this.text(s, tbuf.toString(), attrs));
               tbuf = new StringBuilder();
               buf.append(new RichText.Newline(attrs));
            } else {
               if (c == 125) {
                  buf.append(this.text(s, tbuf.toString(), attrs));
                  break;
               }

               if (c == 36) {
                  c = s.in.peek();
                  if (c != 36 && c != 123 && c != 125) {
                     buf.append(this.text(s, tbuf.toString(), attrs));
                     tbuf = new StringBuilder();
                     buf.append(this.tag(s, attrs));
                  } else {
                     s.in.read();
                     tbuf.append((char)c);
                  }
               } else {
                  tbuf.append((char)c);
               }
            }
         }

         return buf;
      }

      protected RichText.Part parse(RichText.Parser.PState s, Map<? extends Attribute, ?> attrs) throws IOException {
         RichText.Part res = this.text(s, attrs);
         if (s.in.peek() >= 0) {
            throw new RichText.FormatException("Junk left after the end of input: " + (char)s.in.peek());
         } else {
            return res;
         }
      }

      public RichText.Part parse(Reader in, Map<? extends Attribute, ?> extra) throws IOException {
         RichText.Parser.PState s = new RichText.Parser.PState(new PeekReader(in));
         if (extra != null) {
            Map<Attribute, Object> attrs = new HashMap<>();
            attrs.putAll((Map<? extends Attribute, ? extends Object>)this.defattrs);
            attrs.putAll((Map<? extends Attribute, ? extends Object>)extra);
            return this.parse(s, attrs);
         } else {
            return this.parse(s, this.defattrs);
         }
      }

      public RichText.Part parse(Reader in) throws IOException {
         return this.parse(in, null);
      }

      public RichText.Part parse(String text, Map<? extends Attribute, ?> extra) {
         try {
            return this.parse(new StringReader(text), extra);
         } catch (IOException var4) {
            throw new Error(var4);
         }
      }

      public RichText.Part parse(String text) {
         return this.parse(text, null);
      }

      public static String quote(String in) {
         StringBuilder buf = new StringBuilder();

         for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c != '$' && c != '{' && c != '}') {
               buf.append(c);
            } else {
               buf.append('$');
               buf.append(c);
            }
         }

         return buf.toString();
      }

      public static class PState {
         PeekReader in;

         PState(PeekReader in) {
            this.in = in;
         }
      }
   }

   public static class Part {
      public RichText.Part next = null;
      public int x;
      public int y;
      public RichText.RState rs;

      public void append(RichText.Part p) {
         if (this.next == null) {
            this.next = p;
         } else {
            this.next.append(p);
         }
      }

      public void prepare(RichText.RState rs) {
         this.rs = rs;
         if (this.next != null) {
            this.next.prepare(rs);
         }
      }

      public int width() {
         return 0;
      }

      public int height() {
         return 0;
      }

      public int baseline() {
         return 0;
      }

      public void render(Graphics2D g) {
      }

      public RichText.Part split(int w) {
         return this;
      }
   }

   private static class RState {
      FontRenderContext frc;

      RState(FontRenderContext frc) {
         this.frc = frc;
      }
   }

   public static class TextPart extends RichText.Part {
      public AttributedString str;
      public int start;
      public int end;
      private TextMeasurer tm = null;
      private TextLayout tl = null;

      public TextPart(AttributedString str, int start, int end) {
         this.str = str;
         this.start = start;
         this.end = end;
      }

      public TextPart(String str, Map<? extends Attribute, ?> attrs) {
         this(str.length() == 0 ? new AttributedString(str) : new AttributedString(str, attrs), 0, str.length());
      }

      public TextPart(String str) {
         this(new AttributedString(str), 0, str.length());
      }

      public AttributedCharacterIterator ti() {
         return this.str.getIterator(null, this.start, this.end);
      }

      @Override
      public void append(RichText.Part p) {
         if (this.next == null) {
            if (p instanceof RichText.TextPart) {
               RichText.TextPart tp = (RichText.TextPart)p;
               this.str = AttributedStringBuffer.concat(this.ti(), tp.ti());
               this.end = this.end - this.start + (tp.end - tp.start);
               this.start = 0;
               this.next = p.next;
            } else {
               this.next = p;
            }
         } else {
            this.next.append(p);
         }
      }

      public TextMeasurer tm() {
         if (this.tm == null) {
            this.tm = new TextMeasurer(this.str.getIterator(), this.rs.frc);
         }

         return this.tm;
      }

      public TextLayout tl() {
         if (this.tl == null) {
            this.tl = this.tm().getLayout(this.start, this.end);
         }

         return this.tl;
      }

      public float advance(int from, int to) {
         return from == to ? 0.0F : this.tm().getAdvanceBetween(this.start + from, this.start + to);
      }

      @Override
      public int width() {
         return this.start == this.end ? 0 : (int)this.tm().getAdvanceBetween(this.start, this.end);
      }

      @Override
      public int height() {
         return this.start == this.end ? 0 : (int)(this.tl().getAscent() + this.tl().getDescent() + this.tl().getLeading());
      }

      @Override
      public int baseline() {
         return this.start == this.end ? 0 : (int)this.tl().getAscent();
      }

      private RichText.Part split2(int e1, int s2) {
         RichText.TextPart p1 = new RichText.TextPart(this.str, this.start, e1);
         RichText.TextPart p2 = new RichText.TextPart(this.str, s2, this.end);
         p1.next = p2;
         p2.next = this.next;
         p1.rs = p2.rs = this.rs;
         return p1;
      }

      @Override
      public RichText.Part split(int w) {
         int l = this.start;
         int r = this.end;

         do {
            int t = l + (r - l) / 2;
            int tw;
            if (t == l) {
               tw = 0;
            } else {
               tw = (int)this.tm().getAdvanceBetween(this.start, t);
            }

            if (tw > w) {
               r = t;
            } else {
               l = t;
            }
         } while (l < r - 1);

         CharacterIterator it = this.str.getIterator();

         for (int i = l; i >= this.start; i--) {
            if (Character.isWhitespace(it.setIndex(i))) {
               return this.split2(i, i + 1);
            }
         }

         return this.split2(l, l);
      }

      @Override
      public void render(Graphics2D g) {
         if (this.start != this.end) {
            this.tl().draw(g, this.x, this.y + this.tl().getAscent());
         }
      }

      public TextHitInfo charat(float x, float y) {
         return this.tl().hitTestChar(x, y);
      }

      public TextHitInfo charat(Coord c) {
         return this.charat(c.x - this.x, c.y - this.y);
      }
   }
}
