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
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

public class Text {
  public static final Foundry std;
  
  public final BufferedImage img;
  
  public String text;
  
  private Tex tex;
  
  public static final Color black = Color.BLACK;
  
  public static final Color white = Color.WHITE;
  
  static {
    std = new Foundry(new Font("SansSerif", 0, 10));
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
      int p, l = 0, r = this.text.length() + 1;
      while (true) {
        p = (l + r) / 2;
        int a = advance(p);
        if (a < x && l < p) {
          l = p;
          continue;
        } 
        if (a > x && r > p) {
          r = p;
          continue;
        } 
        break;
      } 
      return p;
    }
  }
  
  public static int[] findspaces(String text) {
    List<Integer> l = new ArrayList<>();
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (Character.isWhitespace(c))
        l.add(Integer.valueOf(i)); 
    } 
    int[] ret = new int[l.size()];
    for (int j = 0; j < ret.length; j++)
      ret[j] = ((Integer)l.get(j)).intValue(); 
    return ret;
  }
  
  public static abstract class Furnace {
    public abstract Text render(String param1String);
    
    public Text renderf(String fmt, Object... args) {
      return render(String.format(fmt, args));
    }
  }
  
  public static class Foundry extends Furnace {
    private final FontMetrics m;
    
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
    
    public Foundry aa(boolean aa) {
      this.aa = aa;
      return this;
    }
    
    public int height() {
      return this.m.getAscent() + this.m.getDescent();
    }
    
    private Coord strsize(String text) {
      return new Coord(this.m.stringWidth(text), height());
    }
    
    public Text renderwrap(String text, Color c, int width) {
      if (this.wfnd == null)
        this.wfnd = new RichText.Foundry(this.font, this.defcol); 
      this.wfnd.aa = this.aa;
      text = RichText.Parser.quote(text);
      if (c != null)
        text = String.format("$col[%d,%d,%d,%d]{%s}", new Object[] { Integer.valueOf(c.getRed()), Integer.valueOf(c.getGreen()), Integer.valueOf(c.getBlue()), Integer.valueOf(c.getAlpha()), text }); 
      return this.wfnd.render(text, width, new Object[0]);
    }
    
    public Text renderwrap(String text, int width) {
      return renderwrap(text, null, width);
    }
    
    public Text.Line render(String text, Color c) {
      text = Translate.get(text);
      Coord sz = strsize(text);
      if (sz.x < 1)
        sz = sz.add(1, 0); 
      BufferedImage img = TexI.mkbuf(sz);
      Graphics g = img.createGraphics();
      if (this.aa)
        Utils.AA(g); 
      g.setFont(this.font);
      g.setColor(c);
      FontMetrics m = g.getFontMetrics();
      g.drawString(text, 0, m.getAscent());
      g.dispose();
      return new Text.Line(text, img, m);
    }
    
    public Text.Line render(String text) {
      return render(text, this.defcol);
    }
  }
  
  public static abstract class Imager extends Furnace {
    private final Text.Furnace back;
    
    public Imager(Text.Furnace back) {
      this.back = back;
    }
    
    protected abstract BufferedImage proc(Text param1Text);
    
    public Text render(String text) {
      return new Text(text, proc(this.back.render(text)));
    }
  }
  
  public static abstract class UText implements Indir<Text> {
    public final Text.Furnace fnd;
    
    private Text cur = null;
    
    public UText(Text.Furnace fnd) {
      this.fnd = fnd;
    }
    
    public Text get() {
      String text = text();
      if (this.cur == null || !this.cur.text.equals(text))
        this.cur = this.fnd.render(text); 
      return this.cur;
    }
    
    public Indir<Tex> tex() {
      return new Indir<Tex>() {
          public Tex get() {
            return Text.UText.this.get().tex();
          }
        };
    }
    
    public static UText forfield(Text.Furnace fnd, final Object obj, String fn) {
      final Field f;
      try {
        f = obj.getClass().getField(fn);
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      } 
      if (f.getType() != String.class)
        throw new RuntimeException("Not a string field: " + f); 
      return new UText(fnd) {
          public String text() {
            try {
              return (String)f.get(obj);
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            } 
          }
        };
    }
    
    public static UText forfield(Object obj, String fn) {
      return forfield(Text.std, obj, fn);
    }
    
    protected abstract String text();
  }
  
  protected Text(String text, BufferedImage img) {
    this.text = text;
    this.img = img;
  }
  
  public Coord sz() {
    return Utils.imgsz(this.img);
  }
  
  public static Line render(String text, Color c) {
    return std.render(text, c);
  }
  
  public static Line renderL(String text, Color c) {
    Foundry large = new Foundry(new Font("SansSerif", 0, 12));
    return large.render(text, c);
  }
  
  public static Line renderf(Color c, String text, Object... args) {
    return std.render(String.format(text, args), c);
  }
  
  public static Line render(String text) {
    return render(text, Color.WHITE);
  }
  
  public Tex tex() {
    if (this.tex == null)
      this.tex = new TexI(this.img); 
    return this.tex;
  }
  
  public static void main(String[] args) throws Exception {
    String cmd = args[0].intern();
    if (cmd == "render") {
      PosixArgs opt = PosixArgs.getopt(args, 1, "aw:f:s:");
      boolean aa = false;
      String font = "SansSerif";
      int width = 100, size = 10;
      for (Iterator<Character> iterator = opt.parsed().iterator(); iterator.hasNext(); ) {
        char c = ((Character)iterator.next()).charValue();
        if (c == 'a') {
          aa = true;
          continue;
        } 
        if (c == 'f') {
          font = opt.arg;
          continue;
        } 
        if (c == 'w') {
          width = Integer.parseInt(opt.arg);
          continue;
        } 
        if (c == 's')
          size = Integer.parseInt(opt.arg); 
      } 
      Foundry f = new Foundry(font, size);
      f.aa = aa;
      Text t = f.renderwrap(opt.rest[0], width);
      OutputStream out = new FileOutputStream(opt.rest[1]);
      ImageIO.write(t.img, "PNG", out);
      out.close();
    } 
  }
}
