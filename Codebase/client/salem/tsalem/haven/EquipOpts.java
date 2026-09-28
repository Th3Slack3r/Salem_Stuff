package haven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class EquipOpts extends GameUI.Hidewnd {
  private static final Map<Integer, String> slotNames;
  
  private static final List<Integer> slotOrder;
  
  static {
    final List<Integer> ao = new ArrayList<>();
    Map<Integer, String> an = new HashMap<Integer, String>() {
        public String put(Integer k, String v) {
          ao.add(k);
          return super.put(k, v);
        }
      };
    an.put(Integer.valueOf(0), "Head");
    an.put(Integer.valueOf(13), "Neck");
    an.put(Integer.valueOf(6), "Left hand");
    an.put(Integer.valueOf(7), "Right hand");
    an.put(Integer.valueOf(9), "Purse");
    an.put(Integer.valueOf(14), "Back");
    an.put(Integer.valueOf(5), "Belt");
    an.put(Integer.valueOf(4), "Keys");
    slotNames = Collections.unmodifiableMap(an);
    slotOrder = Collections.unmodifiableList(ao);
  }
  
  private final Map<CheckBox, Integer> checkSlots = new HashBMap<>();
  
  private List<Integer> selected;
  
  public EquipOpts(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Proxy CFG");
    int k = 0;
    read();
    for (Iterator<Integer> iterator = slotOrder.iterator(); iterator.hasNext(); ) {
      int slot = ((Integer)iterator.next()).intValue();
      CheckBox checkBox = new CheckBox(new Coord(0, 20 * k++), this, slotNames.get(Integer.valueOf(slot))) {
          public void changed(boolean val) {
            EquipOpts.this.setSlotState(this, val);
          }
        };
      checkBox.a = this.selected.contains(Integer.valueOf(slot));
      this.checkSlots.put(checkBox, Integer.valueOf(slot));
    } 
    pack();
    update();
  }
  
  public Coord contentsz() {
    Coord sz = super.contentsz();
    sz.x = Math.max(sz.x, 100);
    return sz;
  }
  
  public void toggle() {
    show(!this.visible);
    if (this.visible)
      raise(); 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    super.wdgmsg(sender, msg, args);
  }
  
  private void read() {
    this.selected = new LinkedList<>();
    String[] slots = Utils.getpref("equip_proxy_slots", "6;7;9;14;5;4;13").split(";");
    for (String slot : slots) {
      try {
        this.selected.add(Integer.valueOf(Integer.parseInt(slot)));
      } catch (NumberFormatException numberFormatException) {}
    } 
  }
  
  private void setSlotState(CheckBox check, boolean val) {
    int slot = ((Integer)this.checkSlots.get(check)).intValue();
    int k = this.selected.indexOf(Integer.valueOf(slot));
    if (!val && k >= 0) {
      this.selected.remove(k);
    } else if (val && k < 0) {
      this.selected.add(Integer.valueOf(slot));
    } 
    store();
    update();
  }
  
  private void store() {
    String buf = "";
    int n = this.selected.size();
    for (int i = 0; i < n; i++) {
      buf = buf + this.selected.get(i);
      if (i < n - 1)
        buf = buf + ";"; 
    } 
    Utils.setpref("equip_proxy_slots", buf);
  }
  
  private void update() {
    int[] slots = new int[this.selected.size()];
    int k = 0;
    for (Iterator<Integer> iterator = slotOrder.iterator(); iterator.hasNext(); ) {
      int slot = ((Integer)iterator.next()).intValue();
      if (this.selected.contains(Integer.valueOf(slot)))
        slots[k++] = slot; 
    } 
    this.ui.gui.equipProxy.setSlots(slots);
  }
}
