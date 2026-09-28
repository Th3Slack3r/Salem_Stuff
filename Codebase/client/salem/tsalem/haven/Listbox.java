package haven;

import java.awt.Color;

public abstract class Listbox<T> extends ListWidget<T> {
  public final int h;
  
  public final Scrollbar sb;
  
  public static final Color sell = new Color(52, 35, 36);
  
  public static final Color selr = new Color(178, 93, 91);
  
  public static final Color overr = new Color(189, 239, 137, 33);
  
  public Color bgcolor = Color.BLACK;
  
  private T over;
  
  public Listbox(Coord c, Widget parent, int w, int h, int itemh) {
    super(c, (new Coord(w, h * itemh)).add(Window.fbox.bisz()), parent, itemh);
    this.h = h;
    this.sb = new Scrollbar(new Coord(this.sz.x - (Window.fbox.br.sz()).x, (Window.fbox.bt.sz()).y), this.sz.y - (Window.fbox.bt.sz()).y - (Window.fbox.bb.sz()).y, this, 0, 0);
  }
  
  protected void drawsel(GOut g) {
    drawsel(g, sell, selr);
  }
  
  protected void drawsel(GOut g, Color left, Color right) {
    g.chcolor(255, 255, 0, 128);
    g.poly2(new Object[] { Coord.z, left, new Coord(0, g.sz.y), left, g.sz, right, new Coord(g.sz.x, 0), right });
    g.chcolor();
  }
  
  public void draw(GOut g) {
    this.sb.max = listitems() - this.h;
    if (this.bgcolor != null) {
      g.chcolor(Color.BLACK);
      g.frect(Coord.z, this.sz);
    } 
    g.chcolor();
    Window.fbox.draw(g, Coord.z, this.sz);
    Coord off = Window.fbox.btloff();
    int n = listitems();
    for (int i = 0; i < this.h; i++) {
      int idx = i + this.sb.val;
      if (idx >= n)
        break; 
      T item = listitem(idx);
      int w = this.sz.x - (Window.fbox.bl.sz()).x - (Window.fbox.br.sz()).x - (this.sb.vis() ? this.sb.sz.x : 0);
      GOut ig = g.reclip(off.add(0, i * this.itemh), new Coord(w, this.itemh));
      if (item == this.sel) {
        drawsel(ig);
      } else if (item == this.over) {
        drawsel(ig, sell, overr);
      } 
      drawitem(ig, item);
    } 
    super.draw(g);
  }
  
  public boolean mousewheel(Coord c, int amount) {
    this.sb.ch(amount);
    return true;
  }
  
  protected void itemclick(T item, int button) {
    if (button == 1)
      change(item); 
  }
  
  public T itemat(Coord c) {
    c = c.sub(Window.fbox.btloff());
    int idx = c.y / this.itemh + this.sb.val;
    if (idx < 0 || idx >= listitems())
      return null; 
    return listitem(idx);
  }
  
  public boolean mousedown(Coord c, int button) {
    if (super.mousedown(c, button))
      return true; 
    T item = itemat(c);
    if (item == null && button == 1) {
      change((T)null);
    } else if (item != null) {
      itemclick(item, button);
    } 
    return true;
  }
  
  public void mousemove(Coord c) {
    if (c.isect(Coord.z, this.sz)) {
      this.over = itemat(c);
    } else {
      this.over = null;
    } 
  }
}
