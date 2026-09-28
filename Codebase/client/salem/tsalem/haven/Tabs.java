package haven;

import java.util.Collection;
import java.util.LinkedList;

public class Tabs {
  private final Coord c;
  
  private final Coord sz;
  
  private final Widget parent;
  
  public Tab curtab = null;
  
  public Collection<Tab> tabs = new LinkedList<>();
  
  public Tabs(Coord c, Coord sz, Widget parent) {
    this.c = c;
    this.sz = sz;
    this.parent = parent;
  }
  
  public class Tab extends Widget {
    public Tabs.TabButton btn;
    
    public Tab() {
      super(Tabs.this.c, Tabs.this.sz, Tabs.this.parent);
      if (Tabs.this.curtab == null) {
        Tabs.this.curtab = this;
      } else {
        hide();
      } 
      Tabs.this.tabs.add(this);
    }
    
    public Tab(Coord bc, int bw, String text) {
      this();
      this.btn = new Tabs.TabButton(bc, Integer.valueOf(bw), text, this);
    }
  }
  
  public class TabButton extends Button {
    public final Tabs.Tab tab;
    
    private TabButton(Coord c, Integer w, String text, Tabs.Tab tab) {
      super(c, w, Tabs.this.parent, text);
      this.tab = tab;
    }
    
    public void click() {
      Tabs.this.showtab(this.tab);
    }
  }
  
  public void showtab(Tab tab) {
    Tab old = this.curtab;
    if (old != null)
      old.hide(); 
    if ((this.curtab = tab) != null)
      this.curtab.show(); 
    changed(old, tab);
  }
  
  public void changed(Tab from, Tab to) {}
}
