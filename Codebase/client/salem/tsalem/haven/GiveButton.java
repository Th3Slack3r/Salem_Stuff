package haven;

public class GiveButton extends Widget {
  public static Tex bg = Resource.loadtex("gfx/hud/combat/knapp/knapp");
  
  public static Tex fr = Resource.loadtex("gfx/hud/combat/knapp/ram");
  
  public static Tex ol = Resource.loadtex("gfx/hud/combat/knapp/ol");
  
  public static Tex or = Resource.loadtex("gfx/hud/combat/knapp/or");
  
  public static Tex sl = Resource.loadtex("gfx/hud/combat/knapp/sl");
  
  public static Tex sr = Resource.loadtex("gfx/hud/combat/knapp/sr");
  
  int state;
  
  @RName("give")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new GiveButton(c, parent, ((Integer)args[0]).intValue());
    }
  }
  
  public GiveButton(Coord c, Widget parent, int state, Coord sz) {
    super(c, sz, parent);
    this.state = state;
  }
  
  public GiveButton(Coord c, Widget parent, int state) {
    this(c, parent, state, bg.sz());
  }
  
  public void draw(GOut g) {
    if (this.state == 0) {
      g.chcolor(255, 192, 192, 255);
    } else if (this.state == 1) {
      g.chcolor(192, 192, 255, 255);
    } else if (this.state == 2) {
      g.chcolor(192, 255, 192, 255);
    } 
    g.image(bg, Coord.z, this.sz);
    g.chcolor();
    g.image(fr, Coord.z, this.sz);
    if ((this.state & 0x1) != 0) {
      g.image(ol, Coord.z, this.sz);
    } else {
      g.image(sl, Coord.z, this.sz);
    } 
    if ((this.state & 0x2) != 0) {
      g.image(or, Coord.z, this.sz);
    } else {
      g.image(sr, Coord.z, this.sz);
    } 
  }
  
  public boolean mousedown(Coord c, int button) {
    wdgmsg("click", new Object[] { Integer.valueOf(button) });
    return true;
  }
  
  public void uimsg(String name, Object... args) {
    if (name == "ch") {
      this.state = ((Integer)args[0]).intValue();
    } else {
      super.uimsg(name, args);
    } 
  }
}
