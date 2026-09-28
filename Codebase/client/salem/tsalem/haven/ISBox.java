package haven;

import java.awt.Color;
import java.awt.Font;

public class ISBox extends Widget implements DTarget {
  static Tex bg = Resource.loadtex("gfx/hud/bosq");
  
  private int rem = 0;
  
  private int av = 0;
  
  static Text.Foundry lf = new Text.Foundry(new Font("SansSerif", 0, 18), Color.WHITE);
  
  private final Resource res;
  
  private Text label;
  
  static {
    lf.aa = true;
  }
  
  @RName("isbox")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new ISBox(c, parent, Resource.load((String)args[0]), ((Integer)args[1]).intValue(), ((Integer)args[2]).intValue(), ((Integer)args[3]).intValue());
    }
  }
  
  private void setlabel(int rem, int av, int bi) {
    this.rem = rem;
    this.av = av;
    this.label = lf.renderf("%d/%d/%d", new Object[] { Integer.valueOf(rem), Integer.valueOf(av), Integer.valueOf(bi) });
  }
  
  public ISBox(Coord c, Widget parent, Resource res, int rem, int av, int bi) {
    super(c, bg.sz(), parent);
    this.res = res;
    setlabel(rem, av, bi);
  }
  
  public void draw(GOut g) {
    g.image(bg, Coord.z);
    if (!this.res.loading) {
      Tex t = ((Resource.Image)this.res.<Resource.Image>layer(Resource.imgc)).tex();
      Coord dc = new Coord(6, (bg.sz()).y / 2 - (t.sz()).y / 2);
      g.image(t, dc);
    } 
    g.image(this.label.tex(), new Coord(40, (bg.sz()).y / 2 - (this.label.tex().sz()).y / 2));
  }
  
  public Object tooltip(Coord c, Widget prev) {
    if (!this.res.loading && this.res.layer(Resource.tooltip) != null)
      return ((Resource.Tooltip)this.res.layer((Class)Resource.tooltip)).t; 
    return null;
  }
  
  public boolean mousedown(Coord c, int button) {
    if (button == 1) {
      if ((this.ui.modshift ^ this.ui.modctrl) != 0) {
        int dir = this.ui.modctrl ? -1 : 1;
        int all = (dir > 0) ? this.rem : this.av;
        int k = this.ui.modmeta ? all : 1;
        for (int i = 0; i < k; i++) {
          wdgmsg("xfer2", new Object[] { Integer.valueOf(dir), Integer.valueOf(1) });
        } 
      } else {
        wdgmsg("click", new Object[0]);
      } 
      return true;
    } 
    return false;
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
  
  public void uimsg(String msg, Object... args) {
    if (msg == "chnum") {
      setlabel(((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), ((Integer)args[2]).intValue());
    } else {
      super.uimsg(msg, args);
    } 
  }
}
