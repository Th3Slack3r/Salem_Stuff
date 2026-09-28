package haven;

import java.util.List;

public class EquipProxyWdg extends Widget implements DTarget {
  private Coord slotsz;
  
  private int[] slots;
  
  public static boolean stopSwitchingItems;
  
  public EquipProxyWdg(Coord c, int[] slots, Widget parent) {
    super(c, Coord.z, parent);
    setSlots(slots);
  }
  
  public void setSlots(int[] slots) {
    this.slots = slots;
    this.slotsz = new Coord(slots.length, 1);
    this.sz = Inventory.invsz(this.slotsz);
  }
  
  private int slot(Coord c) {
    int slot = (Inventory.sqroff(c)).x;
    if (slot < 0)
      slot = 0; 
    if (slot >= this.slots.length)
      slot = this.slots.length - 1; 
    return this.slots[slot];
  }
  
  public boolean mousedown(Coord c, int button) {
    Equipory e = this.ui.gui.getEquipory();
    if (e != null) {
      WItem w = e.slots[slot(c)];
      if (w != null) {
        if (this.ui.modctrl && 
          slot(c) == 9)
          try {
            int tile = this.ui.sess.glob.map.gettile((this.ui.gui.map.player()).rc.div(11.0D));
            Resource tilesetr = this.ui.sess.glob.map.tilesetr(tile);
            if (tilesetr.name.contains("water") || tilesetr.name.contains("deep")) {
              Utils.msgOut("The Client just prevented you from dropping your purse into water, if you really want to do it, take it and drop it manually!");
              return true;
            } 
          } catch (Exception ex) {
            this.ui.message("[AutoPurseProtection] error: " + ex.toString(), GameUI.MsgType.INFO);
          }  
        w.mousedown(Coord.z, button);
        return true;
      } 
    } 
    return false;
  }
  
  public static boolean mousedown(int slot, int button) {
    UI ui = UI.instance;
    Equipory e = ui.gui.getEquipory();
    if (e != null) {
      WItem w = e.slots[slot];
      if (w != null) {
        w.mousedown(Coord.z, button);
        return true;
      } 
    } 
    return false;
  }
  
  public void draw(GOut g) {
    super.draw(g);
    Equipory e = this.ui.gui.getEquipory();
    if (e != null) {
      int k = 0;
      Inventory.invsq(g, Coord.z, this.slotsz);
      Coord c0 = new Coord(0, 0);
      for (int slot : this.slots) {
        c0.x = k;
        WItem w = e.slots[slot];
        if (w != null) {
          w.draw(g.reclipl(Inventory.sqoff(c0), g.sz));
        } else {
          Tex ebg = (Equipory.boxen[slot]).bg;
          if (ebg != null)
            g.image(ebg, Inventory.sqoff(c0)); 
        } 
        k++;
      } 
    } 
  }
  
  public Object tooltip(Coord c, Widget prev) {
    Equipory e = this.ui.gui.getEquipory();
    if (e != null) {
      WItem w = e.slots[slot(c)];
      if (w != null)
        return w.tooltip(c, (prev == this) ? w : prev); 
    } 
    return super.tooltip(c, prev);
  }
  
  public boolean drop(final Coord cc, Coord ul) {
    Equipory e = this.ui.gui.getEquipory();
    if (e != null) {
      WItem w = e.slots[slot(cc)];
      if (w != null) {
        (new Thread(new Runnable() {
              public void run() {
                EquipProxyWdg.switchItem(EquipProxyWdg.this.slot(cc));
              }
            },  "SwitchItem")).start();
        return true;
      } 
      e.wdgmsg("drop", new Object[] { Integer.valueOf(slot(cc)) });
      return true;
    } 
    return false;
  }
  
  public static boolean drop(int slot) {
    UI ui = UI.instance;
    Equipory e = ui.gui.getEquipory();
    if (e != null) {
      e.wdgmsg("drop", new Object[] { Integer.valueOf(slot) });
      return true;
    } 
    return false;
  }
  
  public boolean iteminteract(Coord cc, Coord ul) {
    Equipory e = this.ui.gui.getEquipory();
    if (e != null) {
      WItem w = e.slots[slot(cc)];
      if (w != null)
        return w.iteminteract(cc, ul); 
    } 
    return false;
  }
  
  public static void switchItem(int slot) {
    UI ui = UI.instance;
    stopSwitchingItems = false;
    boolean[][] grid = new boolean[Utils.getInvX()][Utils.getInvY()];
    int x = 0;
    int y = 0;
    boolean dontDrop = false;
    List<WItem> items = ui.gui.maininv.getSameName("", Boolean.valueOf(true));
    int i;
    for (i = items.size() - 1; i < items.size() && i >= 0; i--) {
      WItem subject = items.get(i);
      x = subject.server_c.x;
      y = subject.server_c.y;
      if (y < Utils.getInvY() && x < Utils.getInvX()) {
        grid[x][y] = true;
      } else {
        dontDrop = true;
      } 
    } 
    label46: for (i = 0; i < Utils.getInvY() && !dontDrop; i++) {
      for (int j = 0; j < Utils.getInvX(); j++) {
        if (!grid[j][i]) {
          ui.wdgmsg(ui.gui.maininv, "drop", new Object[] { new Coord(j, i) });
          waitForCursor(false, true);
          mousedown(slot, 1);
          waitForCursor(false, false);
          if (!stopSwitchingItems) {
            ui.wdgmsg(ui.gui.maininv, "drop", new Object[] { new Coord(j, i) });
            waitForCursor(true, false);
            drop(slot);
          } 
          waitForCursor(false, true);
          int counter = 0;
          do {
            items = ui.gui.maininv.getSameName("", Boolean.valueOf(true));
            for (int ii = items.size() - 1; ii < items.size() && ii >= 0; ii--) {
              WItem subject = items.get(ii);
              x = subject.server_c.x;
              y = subject.server_c.y;
              if (x == j && y == i) {
                subject.item.wdgmsg("take", new Object[] { Coord.z });
                break label46;
              } 
            } 
            sleep(10);
            ++counter;
          } while (counter <= 500);
          break label46;
        } 
      } 
    } 
  }
  
  private static void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException interruptedException) {}
  }
  
  private static void waitForCursor(boolean change, boolean empty) {
    UI ui = UI.instance;
    int count = 0;
    int handID = 0;
    if (change && 
      ui.gui.hand.iterator().hasNext())
      try {
        handID = ((GItem)ui.gui.hand.iterator().next()).wdgid();
      } catch (Exception exception) {} 
    while (!stopSwitchingItems) {
      sleep(10);
      count++;
      if (count > 500)
        return; 
      if (change)
        try {
          if (handID != ((GItem)ui.gui.hand.iterator().next()).wdgid())
            break; 
        } catch (Exception exception) {} 
      if (!change && 
        empty == ui.gui.hand.isEmpty())
        break; 
    } 
  }
}
