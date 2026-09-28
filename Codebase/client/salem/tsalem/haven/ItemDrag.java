package haven;

public class ItemDrag extends WItem {
  public Coord doff;
  
  public enum TargetType {
    OpenWorldObject, InventoryObject, Undefined;
  }
  
  public static TargetType lastClickTargetType = null;
  
  public ItemDrag(Coord dc, Widget parent, GItem item) {
    super(parent.ui.mc.add(dc.inv()), parent.ui.root, item);
    this.doff = dc;
    this.ui.grabmouse(this);
  }
  
  public void drawmain(GOut g, Tex tex) {
    g.chcolor(255, 255, 255, 128);
    g.image(tex, Coord.z);
    g.chcolor();
  }
  
  public boolean dropon(Widget w, Coord c) {
    if (w instanceof DTarget && (
      (DTarget)w).drop(c, c.add(this.doff.inv())))
      return true; 
    if (w instanceof DTarget2 && (
      (DTarget2)w).drop(c, c.add(this.doff.inv()), this.item))
      return true; 
    for (Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg != this && wdg.visible) {
        Coord cc = w.xlate(wdg.c, true);
        if (c.isect(cc, wdg.sz) && 
          dropon(wdg, c.add(cc.inv())))
          return true; 
      } 
    } 
    return false;
  }
  
  public boolean interact(Widget w, Coord c) {
    if (w instanceof DTarget) {
      if (w instanceof WItem) {
        lastClickTargetType = TargetType.InventoryObject;
      } else if (w instanceof MapView) {
        lastClickTargetType = TargetType.OpenWorldObject;
      } else {
        lastClickTargetType = TargetType.Undefined;
      } 
      if (((DTarget)w).iteminteract(c, c.add(this.doff.inv())))
        return true; 
    } 
    if (w instanceof DTarget2 && (
      (DTarget2)w).iteminteract(c, c.add(this.doff.inv()), this.item))
      return true; 
    for (Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg != this && wdg.visible) {
        Coord cc = w.xlate(wdg.c, true);
        if (c.isect(cc, wdg.sz) && 
          interact(wdg, c.add(cc.inv())))
          return true; 
      } 
    } 
    return false;
  }
  
  public boolean mousedown(Coord c, int button) {
    if (!this.ui.modctrl || button == 3);
    if (MapView.debugtest)
      this.ui.message("test ID", GameUI.MsgType.INFO); 
    if (this.ui.modshift && button == 3)
      MapView.lastShiftRightClick = System.currentTimeMillis(); 
    if (this.ui.modmeta) {
      GameUI gui = UI.instance.gui;
      if (gui != null && gui.map != null)
        return gui.map.mousedown(gui.map.rootxlate(c.add(rootpos())), button); 
    } 
    if (button == 1) {
      this.ui.gui.map.customItemActStop = true;
      dropon(this.parent, c.add(this.c));
    } else if (button == 3) {
      interact(this.parent, c.add(this.c));
    } 
    return false;
  }
  
  public void mousemove(Coord c) {
    this.c = this.c.add(c.add(this.doff.inv()));
  }
  
  private void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException interruptedException) {}
  }
}
