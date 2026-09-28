package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StockBin extends Widget implements DTarget {
  static Tex bg = Resource.loadtex("gfx/hud/bosq");
  
  private int rem = 0, bi = 0;
  
  static Text.Foundry lf = new Text.Foundry(new Font("SansSerif", 0, 18), Color.WHITE);
  
  private Indir<Resource> res;
  
  private Tex label;
  
  private final Value value;
  
  private final Button take;
  
  static {
    lf.aa = true;
  }
  
  @RName("spbox")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new StockBin(c, parent, parent.ui.sess.getres(((Integer)args[0]).intValue()), ((Integer)args[1]).intValue(), ((Integer)args[2]).intValue());
    }
  }
  
  private void setlabel(int rem, int bi) {
    this.rem = rem;
    this.bi = bi;
    if (this.label != null)
      this.label.dispose(); 
    this.label = lf.renderf("%d/%d", new Object[] { Integer.valueOf(rem), Integer.valueOf(bi) }).tex();
  }
  
  public StockBin(Coord c, Widget parent, Indir<Resource> res, int rem, int bi) {
    super(c, bg.sz(), parent);
    this.res = res;
    setlabel(rem, bi);
    this.value = new Value(new Coord(125, 27), 35, this, "");
    this.take = new Button(new Coord(165, 27), Integer.valueOf(35), this, "Take");
    this.value.canactivate = true;
    this.take.canactivate = true;
  }
  
  public void draw(GOut g) {
    g.image(bg, Coord.z);
    try {
      Tex t = ((Resource.Image)((Resource)this.res.get()).<Resource.Image>layer(Resource.imgc)).tex();
      Coord dc = new Coord(6, (bg.sz()).y / 2 - (t.sz()).y / 2);
      g.image(t, dc);
    } catch (Loading loading) {}
    g.image(this.label, new Coord(45, (bg.sz()).y / 2 - (this.label.sz()).y / 2));
    super.draw(g);
  }
  
  public Object tooltip(Coord c, Widget prev) {
    try {
      if (((Resource)this.res.get()).layer(Resource.tooltip) != null)
        return ((Resource.Tooltip)((Resource)this.res.get()).layer((Class)Resource.tooltip)).t; 
    } catch (Loading loading) {}
    return null;
  }
  
  public boolean mousedown(Coord c, int button) {
    Coord cc = xlate(this.take.c, true);
    if (c.isect(cc, this.take.sz))
      return this.take.mousedown(c.sub(cc), button); 
    if (button == 1) {
      if ((this.ui.modshift ^ this.ui.modctrl) != 0) {
        int dir = this.ui.modctrl ? -1 : 1;
        int all = (dir > 0) ? Math.min(this.bi - this.rem, this.ui.gui.maininv.wmap.size()) : this.rem;
        int k = this.ui.modmeta ? all : 1;
        transfer(dir, k);
      } else {
        wdgmsg("click", new Object[0]);
      } 
      return true;
    } 
    return false;
  }
  
  public void transfer(int dir, int amount) {
    for (int i = 0; i < amount; i++) {
      wdgmsg("xfer2", new Object[] { Integer.valueOf(dir), Integer.valueOf(1) });
    } 
  }
  
  public boolean mousewheel(Coord c, int amount) {
    if (amount < 0)
      wdgmsg("xfer2", new Object[] { Integer.valueOf(-1), Integer.valueOf(this.ui.modflags()) }); 
    if (amount > 0)
      wdgmsg("xfer2", new Object[] { Integer.valueOf(1), Integer.valueOf(this.ui.modflags()) }); 
    return true;
  }
  
  public boolean drop(Coord cc, Coord ul) {
    wdgmsg("drop", new Object[0]);
    return true;
  }
  
  public boolean iteminteract(Coord cc, Coord ul) {
    wdgmsg("iact", new Object[0]);
    return true;
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.value || sender == this.take) {
      int amount = 0;
      try {
        amount = Integer.parseInt(this.value.text);
      } catch (Exception exception) {}
      if (amount > this.rem)
        amount = this.rem; 
      if (amount > 0)
        transfer(-1, amount); 
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg.equals("chnum")) {
      setlabel(((Integer)args[0]).intValue(), ((Integer)args[1]).intValue());
    } else if (msg.equals("chres")) {
      this.res = this.ui.sess.getres(((Integer)args[0]).intValue());
    } else {
      super.uimsg(msg, args);
    } 
  }
  
  public static class Value extends TextEntry {
    public static final Set<Integer> ALLOWED_KEYS = new HashSet<>(Arrays.asList(new Integer[] { 
            Integer.valueOf(48), Integer.valueOf(49), Integer.valueOf(50), Integer.valueOf(51), Integer.valueOf(52), Integer.valueOf(53), Integer.valueOf(54), Integer.valueOf(55), Integer.valueOf(56), Integer.valueOf(57), 
            Integer.valueOf(96), Integer.valueOf(97), 
            Integer.valueOf(98), Integer.valueOf(99), Integer.valueOf(100), Integer.valueOf(101), Integer.valueOf(102), Integer.valueOf(103), Integer.valueOf(104), Integer.valueOf(105), 
            Integer.valueOf(37), Integer.valueOf(39), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(127) }));
    
    public Value(Coord c, int w, Widget parent, String deftext) {
      super(c, w, parent, deftext);
    }
    
    public boolean type(char c, KeyEvent ev) {
      if (ALLOWED_KEYS.contains(Integer.valueOf(ev.getKeyCode())))
        return super.type(c, ev); 
      this.ui.root.globtype(c, ev);
      return false;
    }
  }
}
