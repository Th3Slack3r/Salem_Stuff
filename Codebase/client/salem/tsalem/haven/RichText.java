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
import java.awt.image.ImageObserver;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class RichText extends Text {
  public static final Parser std;
  
  static {
    Map<AttributedCharacterIterator.Attribute, Object> a = new HashMap<>();
    a.put(TextAttribute.FAMILY, "SansSerif");
    a.put(TextAttribute.SIZE, Integer.valueOf(10));
    std = new Parser(a);
  }
  
  public static final Foundry stdf = new Foundry(std);
  
  public final Part parts;
  
  private RichText(String text, BufferedImage img, Part parts) {
    super(text, img);
    this.parts = parts;
  }
  
  private static class RState {
    FontRenderContext frc;
    
    RState(FontRenderContext frc) {
      this.frc = frc;
    }
  }
  
  public static class FormatException extends RuntimeException {
    public FormatException(String msg) {
      super(msg);
    }
  }
  
  public static class Part {
    public Part next = null;
    
    public int x;
    
    public int y;
    
    public RichText.RState rs;
    
    public void append(Part p) {
      if (this.next == null) {
        this.next = p;
      } else {
        this.next.append(p);
      } 
    }
    
    public void prepare(RichText.RState rs) {
      this.rs = rs;
      if (this.next != null)
        this.next.prepare(rs); 
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
    
    public void render(Graphics2D g) {}
    
    public Part split(int w) {
      return null;
    }
  }
  
  public static class Image extends Part {
    public BufferedImage img;
    
    public Image(BufferedImage img) {
      this.img = img;
    }
    
    public Image(Resource res, int id) {
      res.loadwait();
      for (Resource.Image img : res.<Resource.Image>layers(Resource.imgc)) {
        if (img.id == id) {
          this.img = img.img;
          break;
        } 
      } 
      if (this.img == null)
        throw new RuntimeException("Found no image with id " + id + " in " + res.toString()); 
    }
    
    public int width() {
      return this.img.getWidth();
    }
    
    public int height() {
      return this.img.getHeight();
    }
    
    public int baseline() {
      return this.img.getHeight() - 1;
    }
    
    public void render(Graphics2D g) {
      g.drawImage(this.img, this.x, this.y, (ImageObserver)null);
    }
  }
  
  public static class Newline extends Part {
    private final Map<? extends AttributedCharacterIterator.Attribute, ?> attrs;
    
    private LineMetrics lm;
    
    public Newline(Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) {
      this.attrs = attrs;
    }
    
    private LineMetrics lm() {
      if (this.lm == null) {
        Font f;
        if ((f = (Font)this.attrs.get(TextAttribute.FONT)) == null)
          f = new Font(this.attrs); 
        this.lm = f.getLineMetrics("", this.rs.frc);
      } 
      return this.lm;
    }
    
    public int height() {
      return (int)lm().getHeight();
    }
    
    public int baseline() {
      return (int)lm().getAscent();
    }
  }
  
  public static class TextPart extends Part {
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
    
    public TextPart(String str, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) {
      str = Translate.get(str);
      this.str = (str.length() == 0) ? new AttributedString(str) : new AttributedString(str, attrs);
      this.start = 0;
      this.end = str.length();
    }
    
    public TextPart(String str) {
      str = Translate.get(str);
      this.str = new AttributedString(str);
      this.start = 0;
      this.end = str.length();
    }
    
    public AttributedCharacterIterator ti() {
      return this.str.getIterator(null, this.start, this.end);
    }
    
    public void append(RichText.Part p) {
      if (this.next == null) {
        if (p instanceof TextPart) {
          TextPart tp = (TextPart)p;
          this.str = AttributedStringBuffer.concat(new AttributedCharacterIterator[] { ti(), tp.ti() });
          this.end = this.end - this.start + tp.end - tp.start;
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
      if (this.tm == null)
        this.tm = new TextMeasurer(this.str.getIterator(), this.rs.frc); 
      return this.tm;
    }
    
    public TextLayout tl() {
      if (this.tl == null)
        this.tl = tm().getLayout(this.start, this.end); 
      return this.tl;
    }
    
    public float advance(int from, int to) {
      if (from == to)
        return 0.0F; 
      return tm().getAdvanceBetween(this.start + from, this.start + to);
    }
    
    public int width() {
      if (this.start == this.end)
        return 0; 
      return (int)tm().getAdvanceBetween(this.start, this.end);
    }
    
    public int height() {
      if (this.start == this.end)
        return 0; 
      return (int)(tl().getAscent() + tl().getDescent() + tl().getLeading());
    }
    
    public int baseline() {
      if (this.start == this.end)
        return 0; 
      return (int)tl().getAscent();
    }
    
    private RichText.Part split2(int e1, int s2) {
      TextPart p1 = new TextPart(this.str, this.start, e1);
      TextPart p2 = new TextPart(this.str, s2, this.end);
      p1.next = p2;
      p2.next = this.next;
      p2.rs = this.rs;
      return p1;
    }
    
    public RichText.Part split(int w) {
      if (this.end - this.start <= 1)
        return null; 
      int l = this.start, r = this.end;
      do {
        int tw, t = l + (r - l) / 2;
        if (t == l) {
          tw = 0;
        } else {
          tw = (int)tm().getAdvanceBetween(this.start, t);
        } 
        if (tw > w) {
          r = t;
        } else {
          l = t;
        } 
      } while (l < r - 1);
      CharacterIterator it = this.str.getIterator();
      for (int i = l; i >= this.start; i--) {
        if (Character.isWhitespace(it.setIndex(i)))
          return split2(i, i + 1); 
      } 
      if (l == this.start)
        l++; 
      return split2(l, l);
    }
    
    public void render(Graphics2D g) {
      if (this.start == this.end)
        return; 
      tl().draw(g, this.x, this.y + tl().getAscent());
    }
    
    public TextHitInfo charat(float x, float y) {
      return tl().hitTestChar(x, y);
    }
    
    public TextHitInfo charat(Coord c) {
      return charat((c.x - this.x), (c.y - this.y));
    }
  }
  
  public Part partat(Coord c) {
    for (Part p = this.parts; p != null; p = p.next) {
      if (c.x >= p.x && c.y >= p.y && c.x < p.x + p.width() && c.y < p.y + p.height())
        return p; 
    } 
    return null;
  }
  
  public AttributedCharacterIterator attrat(Coord c) {
    Part p = partat(c);
    if (p == null || !(p instanceof TextPart))
      return null; 
    TextPart tp = (TextPart)p;
    AttributedCharacterIterator attr = tp.ti();
    attr.setIndex(tp.charat(c).getCharIndex());
    return attr;
  }
  
  public Object attrat(Coord c, AttributedCharacterIterator.Attribute attr) {
    AttributedCharacterIterator ai = attrat(c);
    if (ai == null)
      return null; 
    return ai.getAttribute(attr);
  }
  
  public static Map<? extends AttributedCharacterIterator.Attribute, ?> fillattrs2(Map<? extends AttributedCharacterIterator.Attribute, ?> def, Object... attrs) {
    Map<AttributedCharacterIterator.Attribute, Object> a;
    if (def == null) {
      a = new HashMap<>();
    } else {
      a = new HashMap<>(def);
    } 
    for (int i = 0; i < attrs.length; i += 2)
      a.put((AttributedCharacterIterator.Attribute)attrs[i], attrs[i + 1]); 
    return a;
  }
  
  public static Map<? extends AttributedCharacterIterator.Attribute, ?> fillattrs(Object... attrs) {
    return fillattrs2(null, attrs);
  }
  
  private static Map<? extends AttributedCharacterIterator.Attribute, ?> fixattrs(Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) {
    Map<AttributedCharacterIterator.Attribute, Object> ret = new HashMap<>();
    for (Map.Entry<? extends AttributedCharacterIterator.Attribute, ?> e : attrs.entrySet()) {
      if (e.getKey() == TextAttribute.SIZE) {
        ret.put(e.getKey(), Float.valueOf(((Number)e.getValue()).floatValue()));
        continue;
      } 
      ret.put(e.getKey(), e.getValue());
    } 
    return ret;
  }
  
  public static class Parser {
    private final Map<? extends AttributedCharacterIterator.Attribute, ?> defattrs;
    
    public Parser(Map<? extends AttributedCharacterIterator.Attribute, ?> defattrs) {
      this.defattrs = RichText.fixattrs(defattrs);
    }
    
    public Parser(Object... attrs) {
      this(RichText.fillattrs2(RichText.std.defattrs, attrs));
    }
    
    public static class PState {
      PeekReader in;
      
      PState(PeekReader in) {
        this.in = in;
      }
    }
    
    private static boolean namechar(char c) {
      return (c == ':' || c == '_' || c == '$' || c == '.' || c == '-' || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'));
    }
    
    protected String name(PeekReader in) throws IOException {
      StringBuilder buf = new StringBuilder();
      while (true) {
        int c = in.peek();
        if (c < 0)
          break; 
        if (namechar((char)c)) {
          buf.append((char)in.read());
          continue;
        } 
        break;
      } 
      if (buf.length() == 0)
        throw new RichText.FormatException("Expected name, got `" + (char)in.peek() + "'"); 
      return buf.toString();
    }
    
    protected Color a2col(String[] args) {
      int r = Integer.parseInt(args[0]);
      int g = Integer.parseInt(args[1]);
      int b = Integer.parseInt(args[2]);
      int a = 255;
      if (args.length > 3)
        a = Integer.parseInt(args[3]); 
      return new Color(r, g, b, a);
    }
    
    protected RichText.Part tag(PState s, String tn, String[] args, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) throws IOException {
      if (tn == "img") {
        Resource res = Resource.load(args[0]);
        int id = -1;
        if (args.length > 1)
          id = Integer.parseInt(args[1]); 
        return new RichText.Image(res, id);
      } 
      Map<AttributedCharacterIterator.Attribute, Object> na = new HashMap<>(attrs);
      if (tn == "font") {
        na.put(TextAttribute.FAMILY, args[0]);
        if (args.length > 1)
          na.put(TextAttribute.SIZE, Float.valueOf(Float.parseFloat(args[1]))); 
      } else if (tn == "size") {
        na.put(TextAttribute.SIZE, Float.valueOf(Float.parseFloat(args[0])));
      } else if (tn == "b") {
        na.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
      } else if (tn == "i") {
        na.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
      } else if (tn == "u") {
        na.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
      } else if (tn == "col") {
        na.put(TextAttribute.FOREGROUND, a2col(args));
      } else if (tn == "bg") {
        na.put(TextAttribute.BACKGROUND, a2col(args));
      } 
      if (s.in.peek(true) != 123)
        throw new RichText.FormatException("Expected `{', got `" + (char)s.in.peek() + "'"); 
      s.in.read();
      return text(s, na);
    }
    
    protected RichText.Part tag(PState s, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) throws IOException {
      String[] args;
      s.in.peek(true);
      String tn = name(s.in).intern();
      if (s.in.peek(true) == 91) {
        s.in.read();
        StringBuilder buf = new StringBuilder();
        while (true) {
          int c = s.in.peek();
          if (c < 0)
            throw new RichText.FormatException("Unexpected end-of-input when reading tag arguments"); 
          if (c == 93) {
            s.in.read();
            break;
          } 
          buf.append((char)s.in.read());
        } 
        args = buf.toString().split(",");
      } else {
        args = new String[0];
      } 
      return tag(s, tn, args, attrs);
    }
    
    protected RichText.Part text(PState s, String text, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) throws IOException {
      return new RichText.TextPart(text, attrs);
    }
    
    protected RichText.Part text(PState s, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) throws IOException {
      RichText.Part buf = new RichText.TextPart("");
      StringBuilder tbuf = new StringBuilder();
      while (true) {
        int c = s.in.read();
        if (c < 0) {
          buf.append(text(s, tbuf.toString(), attrs));
          break;
        } 
        if (c == 10) {
          buf.append(text(s, tbuf.toString(), attrs));
          tbuf = new StringBuilder();
          buf.append(new RichText.Newline(attrs));
          continue;
        } 
        if (c == 125) {
          buf.append(text(s, tbuf.toString(), attrs));
          break;
        } 
        if (c == 36) {
          c = s.in.peek();
          if (c == 36 || c == 123 || c == 125) {
            s.in.read();
            tbuf.append((char)c);
            continue;
          } 
          buf.append(text(s, tbuf.toString(), attrs));
          tbuf = new StringBuilder();
          buf.append(tag(s, attrs));
          continue;
        } 
        tbuf.append((char)c);
      } 
      return buf;
    }
    
    protected RichText.Part parse(PState s, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) throws IOException {
      RichText.Part res = text(s, attrs);
      if (s.in.peek() >= 0)
        throw new RichText.FormatException("Junk left after the end of input: " + (char)s.in.peek()); 
      return res;
    }
    
    public RichText.Part parse(Reader in, Map<? extends AttributedCharacterIterator.Attribute, ?> extra) throws IOException {
      PState s = new PState(new PeekReader(in));
      if (extra != null) {
        Map<AttributedCharacterIterator.Attribute, Object> attrs = new HashMap<>();
        attrs.putAll(this.defattrs);
        attrs.putAll(extra);
        return parse(s, attrs);
      } 
      return parse(s, this.defattrs);
    }
    
    public RichText.Part parse(Reader in) throws IOException {
      return parse(in, (Map<? extends AttributedCharacterIterator.Attribute, ?>)null);
    }
    
    public RichText.Part parse(String text, Map<? extends AttributedCharacterIterator.Attribute, ?> extra) {
      try {
        return parse(new StringReader(text), extra);
      } catch (IOException e) {
        throw new Error(e);
      } 
    }
    
    public RichText.Part parse(String text) {
      return parse(text, (Map<? extends AttributedCharacterIterator.Attribute, ?>)null);
    }
    
    public static String quote(String in) {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < in.length(); i++) {
        char c = in.charAt(i);
        if (c == '$' || c == '{' || c == '}') {
          buf.append('$');
          buf.append(c);
        } else {
          buf.append(c);
        } 
      } 
      return buf.toString();
    }
  }
  
  public static class Foundry {
    private final RichText.Parser parser;
    
    private final RichText.RState rs;
    
    public boolean aa = false;
    
    public Foundry(RichText.Parser parser) {
      this.parser = parser;
      BufferedImage junk = TexI.mkbuf(new Coord(10, 10));
      Graphics2D g = junk.createGraphics();
      this.rs = new RichText.RState(g.getFontRenderContext());
    }
    
    public Foundry(Map<? extends AttributedCharacterIterator.Attribute, ?> defattrs) {
      this(new RichText.Parser(defattrs));
    }
    
    public Foundry(Object... attrs) {
      this(new RichText.Parser(attrs));
    }
    
    private static Map<? extends AttributedCharacterIterator.Attribute, ?> xlate(Font f, Color defcol) {
      Map<AttributedCharacterIterator.Attribute, Object> attrs = new HashMap<>();
      attrs.put(TextAttribute.FONT, f);
      attrs.put(TextAttribute.FOREGROUND, defcol);
      return attrs;
    }
    
    public Foundry(Font f, Color defcol) {
      this(xlate(f, defcol));
    }
    
    public Foundry aa(boolean aa) {
      this.aa = aa;
      return this;
    }
    
    private static void aline(List<RichText.Part> line, int y) {
      int mb = 0;
      for (RichText.Part p : line) {
        int cb = p.baseline();
        if (cb > mb)
          mb = cb; 
      } 
      for (RichText.Part p : line)
        p.y = y + mb - p.baseline(); 
    }
    
    private static RichText.Part layout(RichText.Part fp, int w) {
      List<RichText.Part> line = new LinkedList<>();
      int x = 0, y = 0;
      int mw = 0, lh = 0;
      RichText.Part lp = null;
      for (RichText.Part p = fp; p != null; p = p.next) {
        int pw, ph;
        boolean lb = p instanceof RichText.Newline;
        while (true) {
          p.x = x;
          pw = p.width();
          ph = p.height();
          if (w > 0 && 
            p.x + pw > w) {
            RichText.Part tmp = p.split(w - x);
            if (tmp != null) {
              p = tmp;
              if (lp == null) {
                fp = p;
              } else {
                lp.next = p;
              } 
              lb = true;
              continue;
            } 
          } 
          break;
        } 
        lp = p;
        line.add(p);
        if (ph > lh)
          lh = ph; 
        x += pw;
        if (x > mw)
          mw = x; 
        if (lb) {
          aline(line, y);
          x = 0;
          y += lh;
          lh = 0;
          line = new LinkedList<>();
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
        if (x > sz.x)
          sz.x = x; 
        if (y > sz.y)
          sz.y = y; 
      } 
      return sz;
    }
    
    public RichText render(String text, int width, Object... extra) {
      Map<? extends AttributedCharacterIterator.Attribute, ?> extram = null;
      if (extra.length > 0)
        extram = RichText.fillattrs(extra); 
      RichText.Part fp = this.parser.parse(text, extram);
      fp.prepare(this.rs);
      fp = layout(fp, width);
      Coord sz = bounds(fp);
      if (sz.x < 1)
        sz = sz.add(1, 0); 
      if (sz.y < 1)
        sz = sz.add(0, 1); 
      BufferedImage img = TexI.mkbuf(sz);
      Graphics2D g = img.createGraphics();
      if (this.aa)
        Utils.AA(g); 
      for (RichText.Part p = fp; p != null; p = p.next)
        p.render(g); 
      return new RichText(text, img, fp);
    }
    
    public RichText render(String text) {
      return render(text, 0, new Object[0]);
    }
  }
  
  public static RichText render(String text, int width, Object... extra) {
    return stdf.render(text, width, extra);
  }
  
  public static void main(String[] args) throws Exception {
    String cmd = args[0].intern();
    if (cmd == "render") {
      Map<AttributedCharacterIterator.Attribute, Object> a = new HashMap<>(std.defattrs);
      PosixArgs opt = PosixArgs.getopt(args, 1, "aw:f:s:");
      boolean aa = false;
      int width = 0;
      for (Iterator<Character> iterator = opt.parsed().iterator(); iterator.hasNext(); ) {
        char c = ((Character)iterator.next()).charValue();
        if (c == 'a') {
          aa = true;
          continue;
        } 
        if (c == 'f') {
          a.put(TextAttribute.FAMILY, opt.arg);
          continue;
        } 
        if (c == 'w') {
          width = Integer.parseInt(opt.arg);
          continue;
        } 
        if (c == 's')
          a.put(TextAttribute.SIZE, Integer.valueOf(Integer.parseInt(opt.arg))); 
      } 
      Foundry fnd = new Foundry(a);
      fnd.aa = aa;
      RichText t = fnd.render(opt.rest[0], width, new Object[0]);
      OutputStream out = new FileOutputStream(opt.rest[1]);
      ImageIO.write(t.img, "PNG", out);
      out.close();
    } else if (cmd == "pagina") {
      PosixArgs opt = PosixArgs.getopt(args, 1, "aw:");
      boolean aa = false;
      int width = 0;
      for (Iterator<Character> iterator = opt.parsed().iterator(); iterator.hasNext(); ) {
        char c = ((Character)iterator.next()).charValue();
        if (c == 'a') {
          aa = true;
          continue;
        } 
        if (c == 'w')
          width = Integer.parseInt(opt.arg); 
      } 
      Foundry fnd = new Foundry(new Object[0]);
      fnd.aa = aa;
      Resource res = Resource.load(opt.rest[0]);
      res.loadwaitint();
      Resource.Pagina p = res.<Resource.Pagina>layer(Resource.pagina);
      if (p == null)
        throw new Exception("No pagina in " + res + ", loaded from " + res.source); 
      RichText t = fnd.render(p.text, width, new Object[0]);
      OutputStream out = new FileOutputStream(opt.rest[1]);
      ImageIO.write(t.img, "PNG", out);
      out.close();
    } 
  }
}
