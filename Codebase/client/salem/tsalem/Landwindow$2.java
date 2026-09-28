import haven.BuddyWnd;
import haven.Coord;
import haven.Widget;

class null extends BuddyWnd.GroupSelector {
  null(Coord c, Widget parent, int group) {
    super(c, parent, group);
  }
  
  protected void changed(int group) {
    super.changed(group);
    Landwindow.access$100(Landwindow.this);
  }
}
