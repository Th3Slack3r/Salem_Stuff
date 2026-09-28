package haven;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

public class HideGobsList extends Scrollport {
  private final IBox box;
  
  public HideGobsList(Coord c, Widget parent) {
    super(c, new Coord(200, 250), parent);
    this.box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
    int i = 0;
    for (Map.Entry<String, Boolean> entry : Config.HIDEGOBS.entrySet())
      new Item(new Coord(0, 25 * i++), entry.getKey(), this.cont); 
    update();
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (msg.equals("changed")) {
      String name = (String)args[0];
      boolean val = ((Boolean)args[1]).booleanValue();
      synchronized (Config.HIDEGOBS) {
        Config.HIDEGOBS.put(name, Boolean.valueOf(val));
      } 
      Config.saveHideGobs();
    } else if (msg.equals("delete")) {
      String name = (String)args[0];
      synchronized (Config.HIDEGOBS) {
        Config.HIDEGOBS.remove(name);
      } 
      Config.saveHideGobs();
      this.ui.destroy(sender);
      update();
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void add(String name) {
    if (name != null && !name.isEmpty() && !Config.HIDEGOBS.containsKey(name)) {
      synchronized (Config.HIDEGOBS) {
        Config.HIDEGOBS.put(name, Boolean.valueOf(true));
      } 
      Config.saveHideGobs();
      new Item(new Coord(0, 0), name, this.cont);
      update();
      OptWnd2.applyHideGobsList();
    } 
  }
  
  private void update() {
    LinkedList<String> order = new LinkedList<>(Config.HIDEGOBS.keySet());
    Collections.sort(order);
    for (Widget wdg = this.cont.lchild; wdg != null; wdg = wdg.prev) {
      int i = order.indexOf(((Item)wdg).name);
      wdg.c.y = 25 * i;
    } 
    this.cont.update();
  }
  
  public void draw(GOut g) {
    super.draw(g);
    this.box.draw(g, Coord.z, this.sz);
  }
  
  private static class Item extends Widget {
    public final String name;
    
    private final CheckBox cb;
    
    private boolean highlight = false;
    
    private boolean a = false;
    
    public Item(Coord c, String name, Widget parent) {
      super(c, new Coord(200, 25), parent);
      this.name = name;
      this.cb = new CheckBox(new Coord(3, 3), this, name);
      this.cb.a = ((Boolean)Config.HIDEGOBS.get(name)).booleanValue();
      this.cb.canactivate = true;
      new IButton(new Coord(178, 5), this, Window.cbtni[0], Window.cbtni[1], Window.cbtni[2]);
    }
    
    public void draw(GOut g) {
      if (this.highlight) {
        g.chcolor(255, 255, 0, 128);
        g.poly2(new Object[] { Coord.z, Listbox.selr, new Coord(0, this.sz.y), Listbox.selr, this.sz, Listbox.overr, new Coord(this.sz.x, 0), Listbox.overr });
        g.chcolor();
      } 
      super.draw(g);
    }
    
    public void mousemove(Coord c) {
      this.highlight = c.isect(Coord.z, this.sz);
      super.mousemove(c);
    }
    
    public boolean mousedown(Coord c, int button) {
      if (super.mousedown(c, button))
        return true; 
      if (button != 1)
        return false; 
      this.a = true;
      this.ui.grabmouse(this);
      return true;
    }
    
    public boolean mouseup(Coord c, int button) {
      if (this.a && button == 1) {
        this.a = false;
        this.ui.grabmouse(null);
        if (c.isect(new Coord(0, 0), this.sz))
          click(); 
        return true;
      } 
      return false;
    }
    
    private void click() {
      this.cb.a = !this.cb.a;
      wdgmsg("changed", new Object[] { this.name, Boolean.valueOf(this.cb.a) });
      if (this.cb.a && Config.hideSomeGobs)
        OptWnd2.applyHideGobsList(); 
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
      if (msg.equals("ch")) {
        wdgmsg("changed", new Object[] { this.name, args[0] });
      } else if (msg.equals("activate")) {
        wdgmsg("delete", new Object[] { this.name });
      } else {
        super.wdgmsg(sender, msg, args);
      } 
    }
  }
}
