import haven.CheckBox;
import haven.Coord;
import haven.Widget;

class null extends CheckBox {
  null(Coord c, Widget parent, String lbl) {
    super(c, parent, lbl);
    this.a = homestead;
  }
  
  public boolean mousedown(Coord c, int button) {
    if (!this.a) {
      Landwindow.this.wdgmsg("mkhome", new Object[0]);
      set(true);
    } 
    return true;
  }
  
  public void changed(boolean val) {}
}
