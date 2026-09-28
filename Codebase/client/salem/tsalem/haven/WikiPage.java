package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.net.URLDecoder;
import java.util.LinkedList;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.ender.wiki.HtmlDraw;
import org.ender.wiki.Item;
import org.ender.wiki.Request;
import org.ender.wiki.Wiki;

public class WikiPage extends SIWidget implements Request.Callback, HyperlinkListener {
  private static final RichText.Foundry wpfnd = new RichText.Foundry(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(28), TextAttribute.FOREGROUND, Color.WHITE });
  
  private HtmlDraw hd;
  
  private String name = null;
  
  private Item current = null;
  
  private long last = 1L;
  
  int count = 0;
  
  private TexI loading = null;
  
  private final LinkedList<Item> history = new LinkedList<>();
  
  public WikiPage(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
  }
  
  public void draw(GOut g) {
    long now = System.currentTimeMillis();
    if (this.hd != null && now - this.last > 1000L && this.count < 5) {
      this.last = now;
      this.count++;
      redraw();
    } 
    super.draw(g);
    if (this.loading != null)
      g.aimage(this.loading, this.parent.sz.div(2), 0.5D, 0.5D); 
  }
  
  public void draw(BufferedImage buf) {
    if (!this.visible)
      return; 
    if (this.hd == null)
      return; 
    this.hd.setWidth(this.sz.x);
    presize();
    this.hd.get(buf, this.sz.x, this.sz.y);
    loading((String)null);
  }
  
  private void loading(String name) {
    if (this.loading != null)
      this.loading.dispose(); 
    if (name == null) {
      this.loading = null;
    } else {
      BufferedImage img = (wpfnd.render(String.format("$b{Loading '%s'...}", new Object[] { name }))).img;
      this.loading = new TexI(Utils.outline2(img, Color.BLACK, true));
    } 
  }
  
  public boolean mousedown(Coord c, int button) {
    if (this.hd != null)
      this.hd.mouseup(c.x, c.y, button); 
    return false;
  }
  
  public void wiki_item_ready(Item item) {
    go_to(item, true);
  }
  
  public void go_to(Item item, boolean store) {
    if (item == null)
      return; 
    this.name = item.name;
    if (store && this.current != null)
      this.history.push(this.current); 
    this.current = item;
    if (this.hd != null)
      this.hd.destroy(); 
    this.hd = new HtmlDraw(item.content, this);
    if (this.parent instanceof Scrollport.Scrollcont) {
      Scrollport.Scrollcont sc = (Scrollport.Scrollcont)this.parent;
      Scrollport sp = (Scrollport)sc.parent;
      sp.bar.ch(-sp.bar.val);
    } 
    presize();
    this.last = System.currentTimeMillis();
    this.count = 0;
  }
  
  public void back() {
    if (!this.history.isEmpty())
      go_to(this.history.pop(), false); 
  }
  
  public void open(String text, boolean search) {
    if (this.hd != null)
      this.hd.destroy(); 
    this.hd = null;
    this.name = text;
    loading(this.name);
    if (search) {
      Wiki.search(this.name, this);
    } else {
      Wiki.get(this.name, this);
    } 
  }
  
  public void hyperlinkUpdate(HyperlinkEvent ev) {
    try {
      String path = ev.getURL().getPath();
      if (path.toLowerCase().contains("index.php")) {
        this.name = path.substring(path.lastIndexOf("index.php/") + 10);
      } else {
        this.name = path.substring(path.lastIndexOf("/") + 1);
      } 
      this.name = URLDecoder.decode(this.name, "UTF-8");
      System.out.println(String.format("Link: '%s', name: '%s'", new Object[] { path, this.name }));
      open(this.name, false);
    } catch (Exception exception) {}
  }
  
  public void presize() {
    int h = (this.hd == null) ? 100 : this.hd.getHeight();
    resize(new Coord(this.parent.sz.x, h));
    if (this.parent instanceof Scrollport.Scrollcont)
      ((Scrollport.Scrollcont)this.parent).update(); 
  }
}
