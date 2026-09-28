package haven;

import java.awt.Color;

public class CPButton extends Button {
  private static final Resource csfx = Resource.load("sfx/confirm");
  
  public Object cptip = null;
  
  public boolean s = false;
  
  private long fst;
  
  private TexI glowmask;
  
  @RName("cpbtn")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new CPButton(c, ((Integer)args[0]).intValue(), parent, (String)args[1]);
    }
  }
  
  public CPButton(Coord c, int w, Widget parent, String text) {
    super(c, Integer.valueOf(w), parent, text);
    this.glowmask = null;
  }
  
  public void cpclick() {
    wdgmsg("activate", new Object[0]);
  }
  
  public void draw(GOut g) {
    super.draw(g);
    if (this.s) {
      if (this.glowmask == null)
        this.glowmask = new TexI(PUtils.glowmask(PUtils.glowmask(draw().getRaster()), 10, new Color(255, 64, 0))); 
      double ph = (System.currentTimeMillis() - this.fst) / 1000.0D;
      g.chcolor(255, 255, 255, (int)(128.0D * (Math.cos(ph * Math.PI * 2.0D) * -0.5D + 0.5D)));
      GOut g2 = g.reclipl(new Coord(-10, -10), g.sz.add(20, 20));
      g2.image(this.glowmask, Coord.z);
    } 
  }
  
  public void click() {
    if (!this.s) {
      this.fst = System.currentTimeMillis();
      this.s = true;
      change(this.text.text, new Color(255, 64, 0));
      redraw();
      Audio.play(csfx);
    } else if (System.currentTimeMillis() - this.fst > 1000L) {
      cpclick();
      this.s = false;
      change(this.text.text, Button.defcol);
      redraw();
    } 
  }
  
  public void mousemove(Coord c) {
    super.mousemove(c);
    if (this.s && !c.isect(Coord.z, this.sz)) {
      this.s = false;
      change(this.text.text, Button.defcol);
      redraw();
    } 
  }
  
  public Object tooltip(Coord c, Widget prev) {
    if (this.s && this.cptip != null)
      return this.cptip; 
    return super.tooltip(c, prev);
  }
}
