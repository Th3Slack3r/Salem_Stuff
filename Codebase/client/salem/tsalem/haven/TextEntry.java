package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

public class TextEntry extends Widget {
  public static final Text.Foundry fnd = new Text.Foundry(new Font("SansSerif", 0, 12), Color.WHITE);
  
  public static final int defh = fnd.height() + 2;
  
  public LineEdit buf;
  
  public int sx;
  
  public boolean pw = false;
  
  public String text;
  
  private Text.Line tcache = null;
  
  public static final IBox box = Window.tbox;
  
  public static final int toffx = (box.bl.sz()).x + 1;
  
  public static final int wmarg = (box.bl.sz()).x + (box.br.sz()).x + 3;
  
  @RName("text")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      if (args[0] instanceof Coord)
        return new TextEntry(c, (Coord)args[0], parent, (String)args[1]); 
      return new TextEntry(c, ((Integer)args[0]).intValue(), parent, (String)args[1]);
    }
  }
  
  public void settext(String text) {
    this.buf.setline(text);
  }
  
  public void rsettext(String text) {
    this.buf = new LineEdit(this.text = text) {
        protected void done(String line) {
          TextEntry.this.activate(line);
        }
        
        protected void changed() {
          TextEntry.this.text = this.line;
          TextEntry.this.changed();
        }
      };
  }
  
  public void uimsg(String name, Object... args) {
    if (name == "settext") {
      settext((String)args[0]);
    } else if (name == "get") {
      wdgmsg("text", new Object[] { this.buf.line });
    } else if (name == "pw") {
      this.pw = (((Integer)args[0]).intValue() == 1);
    } else {
      super.uimsg(name, args);
    } 
  }
  
  protected void drawbg(GOut g) {
    g.chcolor(0, 0, 0, 255);
    g.frect(Coord.z, this.sz);
    g.chcolor();
  }
  
  protected void drawbb(GOut g) {
    g.image(box.bt, new Coord((box.ctl.sz()).x, 0), new Coord(this.sz.x - (box.ctr.sz()).x - (box.ctl.sz()).x, (box.bt.sz()).y));
    g.image(box.bb, new Coord((box.cbl.sz()).x, this.sz.y - (box.bb.sz()).y), new Coord(this.sz.x - (box.cbr.sz()).x - (box.cbl.sz()).x, (box.bb.sz()).y));
  }
  
  protected void drawfb(GOut g) {
    g.image(box.bl, new Coord(0, (box.ctl.sz()).y), new Coord((box.bl.sz()).x, this.sz.y - (box.cbl.sz()).y - (box.ctl.sz()).y));
    g.image(box.br, new Coord(this.sz.x - (box.br.sz()).x, (box.ctr.sz()).y), new Coord((box.br.sz()).x, this.sz.y - (box.cbr.sz()).y - (box.ctr.sz()).y));
    g.image(box.ctl, Coord.z);
    g.image(box.ctr, new Coord(this.sz.x - (box.ctr.sz()).x, 0));
    g.image(box.cbl, new Coord(0, this.sz.y - (box.cbl.sz()).y));
    g.image(box.cbr, new Coord(this.sz.x - (box.cbr.sz()).x, this.sz.y - (box.cbr.sz()).y));
  }
  
  protected Text.Line render_text(String text) {
    return fnd.render(text);
  }
  
  public void draw(GOut g) {
    String dtext;
    super.draw(g);
    if (this.pw) {
      dtext = "";
      for (int i = 0; i < this.buf.line.length(); i++)
        dtext = dtext + "*"; 
    } else {
      dtext = this.buf.line;
    } 
    if (dtext.length() > 2340) {
      dtext = dtext.substring(0, 2340);
      this.buf.point = 2340;
    } 
    drawbg(g);
    drawbb(g);
    if (this.tcache == null || !this.tcache.text.equals(dtext)) {
      this.tcache = render_text(dtext);
      if (!this.pw)
        settext(this.tcache.text); 
    } 
    int cx = this.tcache.advance(this.buf.point);
    if (cx < this.sx)
      this.sx = cx; 
    if (cx > this.sx + this.sz.x - wmarg)
      this.sx = cx - this.sz.x - wmarg; 
    int ty = (this.sz.y - (this.tcache.sz()).y) / 2;
    g.image(this.tcache.tex(), new Coord(toffx - this.sx, ty));
    if (this.hasfocus && System.currentTimeMillis() % 1000L > 500L) {
      int lx = toffx + cx - this.sx + 1;
      g.line(new Coord(lx, ty + 1), new Coord(lx, ty + (this.tcache.sz()).y - 1), 1.0D);
    } 
    drawfb(g);
  }
  
  public TextEntry(Coord c, Coord sz, Widget parent, String deftext) {
    super(c, sz, parent);
    rsettext(deftext);
    setcanfocus(true);
  }
  
  public TextEntry(Coord c, int w, Widget parent, String deftext) {
    this(c, new Coord(w, defh), parent, deftext);
  }
  
  protected void changed() {}
  
  public void activate(String text) {
    if (this.canactivate)
      wdgmsg("activate", new Object[] { text }); 
  }
  
  public boolean type(char c, KeyEvent ev) {
    return this.buf.key(ev);
  }
  
  public boolean keydown(KeyEvent e) {
    this.buf.key(e);
    return true;
  }
  
  public boolean mousedown(Coord c, int button) {
    this.parent.setfocus(this);
    if (this.tcache != null)
      this.buf.point = this.tcache.charat(c.x + this.sx); 
    return true;
  }
}
