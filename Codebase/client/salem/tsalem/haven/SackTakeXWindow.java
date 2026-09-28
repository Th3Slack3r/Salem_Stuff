package haven;

import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SackTakeXWindow extends Window {
  private static SackTakeXWindow instance;
  
  private final Value value;
  
  private final Button take;
  
  private static Inventory inv;
  
  private static WItem w;
  
  public static void kill() {
    inv = null;
    w = null;
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance != null)
      UI.instance.destroy(instance); 
  }
  
  public static void call(WItem wItem) {
    kill();
    Inventory parent = null;
    if (wItem.parent != null && wItem.parent instanceof Inventory)
      parent = (Inventory)wItem.parent; 
    inv = parent;
    w = wItem;
    instance = new SackTakeXWindow(Coord.z, UI.instance.gui);
  }
  
  public SackTakeXWindow(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Take-Out-Helper");
    this.justclose = true;
    RichText.Foundry skbodfnd = new RichText.Foundry(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(12) });
    skbodfnd.aa = true;
    RichTextBox richTextBox = new RichTextBox(new Coord(5, 5), new Coord(100, 100), this, "Take this amount out of all containers like the one you clicked", skbodfnd);
    this.value = new Value(new Coord(125, 27), 35, this, "");
    this.take = new Button(new Coord(165, 27), Integer.valueOf(35), this, "Take");
    this.value.canactivate = true;
    this.take.canactivate = true;
    pack();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  public static class Value extends TextEntry {
    public static final Set<Integer> ALLOWED_KEYS = new HashSet<>(Arrays.asList(new Integer[] { 
            Integer.valueOf(48), Integer.valueOf(49), Integer.valueOf(50), Integer.valueOf(51), Integer.valueOf(52), Integer.valueOf(53), Integer.valueOf(54), Integer.valueOf(55), Integer.valueOf(56), Integer.valueOf(57), 
            Integer.valueOf(96), Integer.valueOf(97), 
            Integer.valueOf(98), Integer.valueOf(99), Integer.valueOf(100), Integer.valueOf(101), Integer.valueOf(102), Integer.valueOf(103), Integer.valueOf(104), Integer.valueOf(105), 
            Integer.valueOf(37), Integer.valueOf(39), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(127) }));
    
    public Value(Coord c, int w, Widget parent, String deftext) {
      super(c, w, parent, deftext);
    }
    
    public boolean type(char c, KeyEvent ev) {
      if (ALLOWED_KEYS.contains(Integer.valueOf(ev.getKeyCode())))
        return super.type(c, ev); 
      this.ui.root.globtype(c, ev);
      return false;
    }
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.value || sender == this.take) {
      try {
        int amount = Integer.parseInt(this.value.text);
        int mainInvItemCount = this.ui.gui.maininv.getSameName("", Boolean.valueOf(true)).size();
        int freeSlots = Utils.getInvMaxSize() - mainInvItemCount;
        if (freeSlots < amount)
          amount = freeSlots; 
        w.openAllSacksLikeThis(2, amount);
        kill();
      } catch (Exception exception) {}
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void tick(double dt) {
    if (inv == null || !inv.visible || (inv.next == null && inv.prev == null))
      kill(); 
    super.tick(dt);
  }
}
