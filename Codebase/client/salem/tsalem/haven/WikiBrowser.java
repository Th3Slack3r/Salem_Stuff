package haven;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WikiBrowser extends Window implements DTarget2, DropTarget {
  private static final int SEARCH_H = 20;
  
  public static final RichText.Foundry fnd = new RichText.Foundry(new WikiParser(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(12), TextAttribute.FOREGROUND, Color.WHITE }));
  
  private static class WikiParser extends RichText.Parser {
    public WikiParser(Object... args) {
      super(args);
    }
    
    protected RichText.Part tag(RichText.Parser.PState s, String tn, String[] args, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) throws IOException {
      if (tn.equals("table"))
        return new WikiBrowser.Table(args, attrs); 
      return super.tag(s, tn, args, attrs);
    }
  }
  
  private static class Table extends RichText.Part {
    private static final int PAD_W = 5;
    
    private static final int PAD_H = 3;
    
    private final int tabc;
    
    private int w = 1;
    
    private int h = 0;
    
    private int lh = 0;
    
    private final int[] twidth;
    
    private final BufferedImage[] tnames;
    
    private final List<BufferedImage[]> rows = (List)new ArrayList<>();
    
    public Table(String[] args, Map<? extends AttributedCharacterIterator.Attribute, ?> attrs) {
      this.tabc = Integer.parseInt(args[0]);
      this.tnames = new BufferedImage[this.tabc];
      this.twidth = new int[this.tabc];
      int i = 0;
      for (i = 0; i < this.tabc; i++) {
        this.tnames[i] = (WikiBrowser.fnd.render(args[i + 1])).img;
        this.twidth[i] = this.tnames[i].getWidth() + 10;
        this.lh = Math.max(this.h, this.tnames[i].getHeight() + 6);
      } 
      i = this.tabc + 1;
      while (i < args.length) {
        BufferedImage[] cols = new BufferedImage[this.tabc];
        for (int k = 0; k < this.tabc; k++, i++) {
          cols[k] = (WikiBrowser.fnd.render(args[i])).img;
          this.twidth[k] = Math.max(this.twidth[k], cols[k].getWidth() + 10);
          this.lh = Math.max(this.h, cols[k].getHeight() + 6);
        } 
        this.rows.add(cols);
      } 
      for (i = 0; i < this.tabc; i++)
        this.w += this.twidth[i]; 
      this.h = this.lh * (this.rows.size() + 1);
    }
    
    public int height() {
      return this.h;
    }
    
    public int width() {
      return this.w;
    }
    
    public int baseline() {
      return this.h - 1;
    }
    
    public void render(Graphics2D g) {
      g.setColor(Color.WHITE);
      int cx = this.x, cy = this.y;
      int i;
      for (i = 0; i < this.tabc; i++) {
        int cw = this.twidth[i];
        g.drawImage(this.tnames[i], cx + 5, cy + 3, (ImageObserver)null);
        g.drawRect(cx, cy, cw, this.lh);
        cx += cw;
      } 
      i = 1;
      for (BufferedImage[] cols : this.rows) {
        cx = this.x;
        cy = this.y + this.lh * i;
        for (int j = 0; j < this.tabc; j++) {
          int cw = this.twidth[j];
          g.drawImage(cols[j], cx + 5, cy + 3, (ImageObserver)null);
          g.drawRect(cx, cy, cw, this.lh);
          cx += cw;
        } 
        i++;
      } 
    }
  }
  
  private static final Coord gzsz = new Coord(15, 15);
  
  private static final Coord minsz = new Coord(200, 150);
  
  private static final String OPT_SZ = "_sz";
  
  private static WikiBrowser instance;
  
  private Scrollport sp;
  
  private TextEntry search;
  
  private Button back;
  
  private WikiPage page;
  
  boolean rsm = false;
  
  public WikiBrowser(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent, "Wiki");
    this.justclose = true;
    this.search = new TextEntry(Coord.z, new Coord(this.asz.x - 30, 20), this, "");
    this.search.canactivate = true;
    this.back = new Button(new Coord(this.asz.x - 20, 0), Integer.valueOf(20), this, "←") {
        public Object tooltip(Coord c, Widget prev) {
          return "Back";
        }
      };
    this.sp = new Scrollport(new Coord(0, 23), this.asz.sub(0, 23), this);
    pack();
    this.page = new WikiPage(Coord.z, this.sp.cont.sz, this.sp.cont);
  }
  
  protected void loadOpts() {
    super.loadOpts();
    resize(getOptCoord("_sz", this.sz));
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (msg.equals("activate")) {
      if (sender == this.search) {
        this.page.open(this.search.text, true);
        return;
      } 
      if (sender == this.back) {
        this.page.back();
        return;
      } 
    } 
    super.wdgmsg(sender, msg, args);
  }
  
  public void resize(Coord sz) {
    super.resize(sz);
    if (this.sp != null)
      this.sp.resize(sz.sub(0, 23)); 
    if (this.search != null)
      this.search.resize(new Coord(sz.x - 25, 20)); 
    if (this.back != null)
      sz.x -= 20; 
  }
  
  public boolean mousedown(Coord c, int button) {
    if (button == 1) {
      this.ui.grabmouse(this);
      this.doff = c;
      if (c.isect(this.sz.sub(gzsz), gzsz)) {
        this.rsm = true;
        return true;
      } 
    } 
    return super.mousedown(c, button);
  }
  
  public boolean mouseup(Coord c, int button) {
    if (this.rsm) {
      this.ui.grabmouse(null);
      this.rsm = false;
      storeOpt("_sz", this.asz);
      return true;
    } 
    return super.mouseup(c, button);
  }
  
  public void mousemove(Coord c) {
    if (this.rsm) {
      Coord d = c.sub(this.doff);
      this.asz = this.asz.add(d);
      this.asz.x = Math.max(minsz.x, this.asz.x);
      this.asz.y = Math.max(minsz.y, this.asz.y);
      this.doff = c;
      resize(this.asz);
    } else {
      super.mousemove(c);
    } 
  }
  
  public static void toggle() {
    if (instance == null) {
      instance = new WikiBrowser(new Coord(300, 200), minsz, UI.instance.gui);
    } else {
      close();
    } 
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  public static void close() {
    if (instance != null) {
      UI ui = UI.instance;
      ui.destroy(instance);
    } 
  }
  
  public boolean dropthing(Coord cc, Object thing) {
    if (thing instanceof Resource) {
      Resource res = (Resource)thing;
      String name = null;
      Resource.Tooltip tt = res.<Resource.Tooltip>layer(Resource.tooltip);
      if (tt != null) {
        name = tt.t;
      } else {
        Resource.AButton ad = res.<Resource.AButton>layer(Resource.action);
        if (ad != null)
          name = ad.name; 
      } 
      if (name != null)
        this.page.open(name, true); 
      return true;
    } 
    return false;
  }
  
  public boolean drop(Coord cc, Coord ul, GItem item) {
    String name = item.name();
    if (name != null)
      this.page.open(name, true); 
    return true;
  }
  
  public boolean iteminteract(Coord cc, Coord ul, GItem item) {
    return false;
  }
}
