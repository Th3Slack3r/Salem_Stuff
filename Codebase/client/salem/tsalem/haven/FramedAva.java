package haven;

import java.awt.Color;

public class FramedAva extends Widget {
  public static final IBox box = Window.swbox;
  
  public Color color = new Color(133, 92, 62);
  
  public final Avaview view;
  
  @RName("av")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Avaview(c, Avaview.dasz, parent, ((Integer)args[0]).intValue(), "avacam");
    }
  }
  
  public FramedAva(Coord c, Coord sz, Widget parent, long avagob, String camnm) {
    super(c, sz, parent);
    this.view = new Avaview(box.btloff(), sz.sub(box.bisz()), this, avagob, camnm);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "upd") {
      this.view.avagob = ((Integer)args[0]).intValue();
      return;
    } 
    super.uimsg(msg, args);
  }
  
  public void draw(GOut g) {
    super.draw(g);
    g.chcolor(this.color);
    box.draw(g, Coord.z, this.sz);
  }
  
  public boolean mousedown(Coord c, int button) {
    wdgmsg("click", new Object[] { Integer.valueOf(button) });
    return true;
  }
}
