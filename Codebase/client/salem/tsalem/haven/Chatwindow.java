package haven;

public class Chatwindow extends Window {
  TextEntry in;
  
  Textlog out;
  
  @RName("chat")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Chatwindow(c, (Coord)args[0], parent);
    }
  }
  
  public Chatwindow(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent, "Chat");
    this.in = new TextEntry(new Coord(0, sz.y - 20), new Coord(sz.x, 20), this, "");
    this.in.canactivate = true;
    this.out = new Textlog(Coord.z, new Coord(sz.x, sz.y - 20), this);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "log") {
      this.out.append((String)args[0]);
    } else {
      super.uimsg(msg, args);
    } 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.in && 
      msg == "activate") {
      wdgmsg("msg", new Object[] { args[0] });
      this.in.settext("");
      return;
    } 
    super.wdgmsg(sender, msg, args);
  }
}
