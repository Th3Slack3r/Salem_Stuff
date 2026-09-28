package haven;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class WeightWdg extends Window {
  static final Tex bg = Resource.loadtex("gfx/hud/bgtex");
  
  private Tex label;
  
  private int sum = 0;
  
  private Integer weight = Integer.valueOf(0);
  
  public WeightWdg(Coord c, Widget parent) {
    super(c, Coord.z, parent, "weightwdg");
    this.cap = null;
    this.sz = new Coord(100, 30);
  }
  
  public void update(Integer weight) {
    if (this.label != null)
      this.label.dispose(); 
    if (weight == null) {
      weight = this.weight;
    } else {
      this.weight = weight;
    } 
    int cap = 25000;
    Glob.CAttr ca = this.ui.sess.glob.cattr.get("carry");
    if (ca != null)
      cap = ca.comp; 
    Color color = (weight.intValue() > cap) ? Color.RED : Color.WHITE;
    if (Config.weight_wdg_inv_items_nr) {
      this.sum = this.ui.gui.maininv.wmap.size();
      String string = String.format("Weight: %.2f/%.2f kg", new Object[] { Double.valueOf(weight.intValue() / 1000.0D), Double.valueOf(cap / 1000.0D) });
      string = string + ",  # " + this.sum;
      this.label = Text.render(string, color).tex();
    } else {
      this.label = Text.render(String.format("Weight: %.2f/%.2f kg", new Object[] { Double.valueOf(weight.intValue() / 1000.0D), Double.valueOf(cap / 1000.0D) }), color).tex();
    } 
    this.sz = this.label.sz().add(Window.swbox.bisz()).add(4, 0);
  }
  
  public void tick(double dt) {
    if (Config.weight_wdg != this.visible)
      show(Config.weight_wdg); 
  }
  
  public void draw(GOut g) {
    Coord s = bg.sz();
    for (int y = 0; y * s.y < this.sz.y; y++) {
      for (int x = 0; x * s.x < this.sz.x; x++)
        g.image(bg, new Coord(x * s.x, y * s.y)); 
    } 
    if (this.label != null)
      g.aimage(this.label, this.sz.div(2), 0.5D, 0.5D); 
    Window.swbox.draw(g, Coord.z, this.sz);
  }
  
  public boolean type(char key, KeyEvent ev) {
    return false;
  }
}
