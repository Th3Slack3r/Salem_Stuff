package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Button extends SIWidget {
  static final BufferedImage bl = Resource.loadimg("gfx/hud/buttons/tbtn/left");
  
  static final BufferedImage br = Resource.loadimg("gfx/hud/buttons/tbtn/right");
  
  static final BufferedImage ut = Resource.loadimg("gfx/hud/buttons/tbtn/utex");
  
  static final Color defcol = new Color(248, 240, 193);
  
  static final Text.Foundry tf = new Text.Foundry(new Font("Sans", 0, 11), defcol);
  
  public static final int h = ut.getHeight();
  
  public static final int pad = bl.getWidth() + br.getWidth();
  
  public Text text;
  
  public BufferedImage cont;
  
  boolean a = false;
  
  public Object Info;
  
  @RName("btn")
  public static class $Btn implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Button(c, (Integer)args[0], parent, (String)args[1]);
    }
  }
  
  @RName("ltbtn")
  public static class $LTBtn implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return Button.wrapped(c, ((Integer)args[0]).intValue(), parent, (String)args[1]);
    }
  }
  
  public static Button wrapped(Coord c, int w, Widget parent, String text) {
    Button ret = new Button(c, Integer.valueOf(w), parent, tf.renderwrap(text, w - 10));
    return ret;
  }
  
  public Button(Coord c, Integer w, Widget parent, String text) {
    super(c, new Coord(w.intValue(), h), parent);
    this.text = tf.render(text);
    this.cont = this.text.img;
  }
  
  public Button(Coord c, Integer w, Widget parent, Text text) {
    super(c, new Coord(w.intValue(), h), parent);
    this.text = text;
    this.cont = text.img;
  }
  
  public Button(Coord c, Integer w, Widget parent, BufferedImage cont) {
    super(c, new Coord(w.intValue(), h), parent);
    this.cont = cont;
  }
  
  public void draw(BufferedImage buf) {
    Graphics g = buf.getGraphics();
    int iw = this.sz.x - pad;
    int x;
    for (x = 0; x < iw; x += ut.getWidth()) {
      int w = Math.min(ut.getWidth(), iw - x), ix = x + bl.getWidth();
      g.drawImage(ut, ix, 0, ix + w, ut.getHeight(), 0, 0, w, ut.getHeight(), null);
    } 
    g.drawImage(bl, 0, 0, null);
    g.drawImage(br, this.sz.x - br.getWidth(), 0, null);
    Coord tc = this.sz.div(2).add(Utils.imgsz(this.cont).div(2).inv());
    if (this.a)
      tc = tc.add(1, 1); 
    g.drawImage(this.cont, tc.x, tc.y, null);
  }
  
  public Coord contentsz() {
    return new Coord((this.text.sz()).x + pad, h);
  }
  
  public void change(String text, Color col) {
    this.text = tf.render(text, col);
    this.cont = this.text.img;
    redraw();
  }
  
  public void change(String text) {
    change(text, defcol);
  }
  
  public void change(Color col) {
    if (col == null)
      col = defcol; 
    change(this.text.text, col);
  }
  
  public void click() {
    wdgmsg("activate", new Object[0]);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "ch") {
      if (args.length > 1) {
        change((String)args[0], (Color)args[1]);
      } else {
        change((String)args[0]);
      } 
    } else {
      super.uimsg(msg, args);
    } 
  }
  
  public boolean mousedown(Coord c, int button) {
    if (button != 1)
      return false; 
    this.a = true;
    redraw();
    this.ui.grabmouse(this);
    return true;
  }
  
  public boolean mouseup(Coord c, int button) {
    if (this.a && button == 1) {
      this.a = false;
      redraw();
      this.ui.grabmouse(null);
      if (c.isect(new Coord(0, 0), this.sz))
        click(); 
      return true;
    } 
    return false;
  }
}
