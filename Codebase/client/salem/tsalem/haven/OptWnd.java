package haven;

import java.awt.event.KeyEvent;

public class OptWnd extends Window {
  public final Panel main;
  
  public Panel current;
  
  public void chpanel(Panel p) {
    if (this.current != null)
      this.current.hide(); 
    (this.current = p).show();
    pack();
  }
  
  public class PButton extends Button {
    public final OptWnd.Panel tgt;
    
    public final int key;
    
    public PButton(Coord c, int w, Widget parent, String title, int key, OptWnd.Panel tgt) {
      super(c, Integer.valueOf(w), parent, title);
      this.tgt = tgt;
      this.key = key;
    }
    
    public void click() {
      OptWnd.this.chpanel(this.tgt);
    }
    
    public boolean type(char key, KeyEvent ev) {
      if (this.key != -1 && key == this.key) {
        click();
        return true;
      } 
      return false;
    }
  }
  
  public class Panel extends Widget {
    public Panel(Coord sz) {
      super(Coord.z, sz, OptWnd.this);
      this.visible = false;
    }
  }
  
  public OptWnd(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Escape Menu");
    this.main = new Panel(new Coord(200, 140));
    new Button(new Coord(0, 0), Integer.valueOf(200), this.main, "Options") {
        public void click() {
          if (OptWnd2.instance == null) {
            OptWnd2.toggle();
          } else {
            OptWnd2.refresh();
          } 
          this.ui.gui.opts.hide();
        }
      };
    new Button(new Coord(0, 60), Integer.valueOf(200), this.main, "Switch character") {
        public void click() {
          ((GameUI)getparent(GameUI.class)).act(new String[] { "lo", "cs" });
        }
      };
    new Button(new Coord(0, 90), Integer.valueOf(200), this.main, "Log out") {
        public void click() {
          ((GameUI)getparent(GameUI.class)).act(new String[] { "lo" });
        }
      };
    new Button(new Coord(0, 120), Integer.valueOf(200), this.main, "Close") {
        public void click() {
          OptWnd.this.hide();
        }
      };
    chpanel(this.main);
    pack();
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this && msg == "close") {
      hide();
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void show() {
    chpanel(this.main);
    super.show();
  }
}
