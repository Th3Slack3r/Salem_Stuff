package haven;

import java.awt.Color;

public class DarknessWnd extends Window {
  Label lbl;
  
  public static DarknessWnd instance;
  
  public DarknessWnd(Widget parent) {
    super(new Coord(300, 200), Coord.z, parent, "Darkness");
    close();
    instance = this;
    this.justclose = true;
    this.lbl = new Label(Coord.z, this, "darknesss tool");
    this.visible = false;
    update();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  public static void close() {
    if (instance != null)
      instance.destroy(); 
  }
  
  public static void update() {
    if (instance == null)
      return; 
    Glob g = instance.ui.sess.glob;
    float b = Color.RGBtoHSB(g.origamb.getRed(), g.origamb.getGreen(), g.origamb.getBlue(), null)[2] * 100.0F;
    instance.lbl.settext(String.format("Angle: %.2f°, Elevation: %.2f°, color: (%d, %d, %d), b: %.2f", new Object[] { Double.valueOf(180.0D * g.lightang / Math.PI), Double.valueOf(180.0D * g.lightelev / Math.PI), Integer.valueOf(g.origamb.getRed()), Integer.valueOf(g.origamb.getGreen()), Integer.valueOf(g.origamb.getBlue()), Float.valueOf(b) }));
    instance.pack();
  }
  
  public static DarknessWnd getInstance() {
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance == null)
      return new DarknessWnd(UI.instance.gui); 
    return instance;
  }
  
  public static void toggle() {
    DarknessWnd wnd = getInstance();
    wnd.visible = !wnd.visible;
  }
}
