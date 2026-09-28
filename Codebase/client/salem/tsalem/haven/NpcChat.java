package haven;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class NpcChat extends Window {
  Textlog out;
  
  List<Button> btns = null;
  
  @RName("npc")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new NpcChat(c, (Coord)args[0], parent, (String)args[1]);
    }
  }
  
  public NpcChat(Coord c, Coord sz, Widget parent, String title) {
    super(c, sz, parent, title);
    this.out = new Textlog(Coord.z, new Coord(sz.x, sz.y), this);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "log") {
      Color col = null;
      if (args.length > 1)
        col = (Color)args[1]; 
      this.out.append((String)args[0], col);
    } else if (msg == "btns") {
      if (this.btns != null) {
        for (Button b : this.btns)
          this.ui.destroy(b); 
        this.btns = null;
      } 
      if (args.length > 0) {
        int y = this.out.sz.y + 3;
        this.btns = new LinkedList<>();
        for (Object text : args) {
          Button b = Button.wrapped(new Coord(0, y), this.out.sz.x, this, (String)text);
          this.btns.add(b);
          y += b.sz.y + 3;
        } 
      } 
      pack();
    } else {
      super.uimsg(msg, args);
    } 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (this.btns != null && this.btns.contains(sender)) {
      wdgmsg("btn", new Object[] { Integer.valueOf(this.btns.indexOf(sender)) });
      return;
    } 
    super.wdgmsg(sender, msg, args);
  }
}
