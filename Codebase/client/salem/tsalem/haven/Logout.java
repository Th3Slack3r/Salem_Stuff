package haven;

public class Logout extends Window {
  Button y;
  
  Button n;
  
  public Logout(Coord c, Widget parent) {
    super(c, new Coord(125, 50), parent, "Haven & Hearth");
    new Label(Coord.z, this, "Do you want to log out?");
    this.y = new Button(new Coord(0, 30), Integer.valueOf(50), this, "Yes");
    this.n = new Button(new Coord(75, 30), Integer.valueOf(50), this, "No");
    this.canactivate = true;
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.y) {
      this.ui.sess.close();
    } else if (sender == this.n) {
      this.ui.destroy(this);
    } else if (sender == this) {
      if (msg == "close")
        this.ui.destroy(this); 
      if (msg == "activate")
        this.ui.sess.close(); 
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
}
