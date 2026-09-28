package haven;

import java.awt.event.KeyEvent;

public class HotkeyListWindow extends Window {
  static final String title = "List of Hotkeys";
  
  private static HotkeyListWindow instance;
  
  public HotkeyListWindow(Coord c, Widget parent) {
    super(c, new Coord(300, 100), parent, "List of Hotkeys");
    init_components();
    toggle();
    pack();
  }
  
  private final void init_components() {
    new Label(Coord.z, this, "Mouse controls");
    int y = 0;
    int x1 = 30, x2 = 120;
    int step = 15, big_step = 30;
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+left click");
    new Label(new Coord(120, y), this, "Drop from inventory.");
    y += 15;
    new Label(new Coord(30, y), this, "Shift+left click");
    new Label(new Coord(120, y), this, "Transfer between inventories.");
    y += 15;
    new Label(new Coord(30, y), this, "Shift+alt+left click");
    new Label(new Coord(120, y), this, "Transfer all similar items between inventories.");
    y += 15;
    new Label(new Coord(30, y), this, "Shift+scrollwheel");
    new Label(new Coord(120, y), this, "Transfer between inventories (also construction sign slots).");
    y += 15;
    new Label(new Coord(30, y), this, "Shift+right click");
    new Label(new Coord(120, y), this, "Interact with object and take similar item from main inventory.");
    y += 30;
    new Label(new Coord(0, y), this, "Window hotkeys");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+E");
    new Label(new Coord(120, y), this, "Equipment window");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+T");
    new Label(new Coord(120, y), this, "Study window");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+I/Tab");
    new Label(new Coord(120, y), this, "Inventory window");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+P");
    new Label(new Coord(120, y), this, "Town window");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+C");
    new Label(new Coord(120, y), this, "Toggle chat size");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+B");
    new Label(new Coord(120, y), this, "Kin window");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+O");
    new Label(new Coord(120, y), this, "Option window");
    y += 15;
    new Label(new Coord(30, y), this, "Alt+S/Prnt Scrn");
    new Label(new Coord(120, y), this, "Screenshot");
    y += 30;
    new Label(new Coord(0, y), this, "Custom client features");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+F/Ctrl+L");
    new Label(new Coord(120, y), this, "Flatness Tool");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+A");
    new Label(new Coord(120, y), this, "Inventory abacus");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+X");
    new Label(new Coord(120, y), this, "Cartographer (unfinished)");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+D");
    new Label(new Coord(120, y), this, "Darkness indicator");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+N");
    new Label(new Coord(120, y), this, "Toggle forced non-darkness display");
    y += 15;
    new Label(new Coord(30, y), this, "Alt+R");
    new Label(new Coord(120, y), this, "Toggle radius display (braziers, mining supports,...)");
    y += 15;
    new Label(new Coord(30, y), this, "Alt+C");
    new Label(new Coord(120, y), this, "Open the crafting window");
    y += 15;
    new Label(new Coord(30, y), this, "Alt+F");
    new Label(new Coord(120, y), this, "Open the filter window");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+Z");
    new Label(new Coord(120, y), this, "Toggle tile centering");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+R");
    new Label(new Coord(120, y), this, "Toggle the toolbelt");
    y += 15;
    new Label(new Coord(30, y), this, "Ctrl+G");
    new Label(new Coord(120, y), this, "Toggle the backpack");
    y += 30;
    new Label(new Coord(0, y), this, "Handy console commands");
    y += 15;
    new Label(new Coord(30, y), this, ":fs 0/1");
    new Label(new Coord(120, y), this, "Set fullscreen (buggy!)");
    y += 15;
    new Label(new Coord(30, y), this, ":act lo");
    new Label(new Coord(120, y), this, "Log out if allowed");
    y += 15;
    new Label(new Coord(30, y), this, ":act lo cs");
    new Label(new Coord(120, y), this, "Log out to character selection if allowed");
    y += 15;
    new Label(new Coord(30, y), this, ":lo");
    new Label(new Coord(120, y), this, "Force disconnect, even if not allowed (at your own risk!)");
    int xx0 = 450, xx1 = 480, xx2 = 570;
    y = 0;
    new Label(new Coord(450, 0), this, "Taipions Features");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+SHIFT+F/L");
    new Label(new Coord(570, y), this, "Flatness Tool with Static=ON, Flatness Tool close");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+1");
    new Label(new Coord(570, y), this, "Toggle invert SHIFT");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+2");
    new Label(new Coord(570, y), this, "Toggle render distance");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+3");
    new Label(new Coord(570, y), this, "Toggle hide gobs");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+4");
    new Label(new Coord(570, y), this, "Toggle FPS display");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+5");
    new Label(new Coord(570, y), this, "Toggle quick minimal render distance");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+6");
    new Label(new Coord(570, y), this, "Toggle display hitboxes");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+7");
    new Label(new Coord(570, y), this, "Toggle custom SHIFT-itemAct");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+8");
    new Label(new Coord(570, y), this, "Toggle auto-replace-container from backpack/inventory");
    y += 15;
    new Label(new Coord(480, y), this, "CTRL+9");
    new Label(new Coord(570, y), this, "Toggle all audio mute");
    y += 30;
    new Label(new Coord(450, y), this, "Taipions console commands");
    y += 15;
    new Label(new Coord(480, y), this, ":debug");
    new Label(new Coord(570, y), this, "Output action-messages sent to the server to System Chat");
    y += 15;
    new Label(new Coord(480, y), this, ":help");
    new Label(new Coord(570, y), this, "Output a list of console commands to System Chat");
    y += 15;
    new Label(new Coord(480, y), this, ":cam [camname]");
    new Label(new Coord(570, y), this, "Activate a cam (with parameters), see :help for details");
    y += 15;
    new Label(new Coord(480, y), this, ":inv");
    new Label(new Coord(570, y), this, "Make things invisible, see :help for details");
    y += 15;
    new Label(new Coord(480, y), this, ":count");
    new Label(new Coord(570, y), this, "Count anything anywhere, see :help for details");
    y += 15;
    new Label(new Coord(480, y), this, ":rad");
    new Label(new Coord(570, y), this, "Single radius display, see :help for details");
    y += 15;
    new Label(new Coord(480, y), this, ":mining");
    new Label(new Coord(570, y), this, "Mining helper, see :help for details");
    y += 15;
    new Label(new Coord(480, y), this, ":x show ...");
    new Label(new Coord(570, y), this, "Shows everything you can do that matches ...");
    y += 15;
    new Label(new Coord(480, y), this, ":x ...");
    new Label(new Coord(570, y), this, "Calls ... (shortest match), recipe, ability, anything");
    y += 15;
    new Label(new Coord(480, y), this, ":h");
    new Label(new Coord(570, y), this, "Toggle hide (parts) of the UI, \":h help\" for details");
    y += 15;
    new Label(new Coord(480, y), this, ":p");
    new Label(new Coord(570, y), this, "Toggle hide the player character");
    y += 15;
    new Label(new Coord(480, y), this, ":offset x y");
    new Label(new Coord(570, y), this, "Set offset for placing ghost objects to x/y");
    y += 15;
    new Label(new Coord(480, y), this, ":placegrid x");
    new Label(new Coord(570, y), this, "Set grid granularity for placing ghost objects to x");
    y += 15;
    new Label(new Coord(480, y), this, ":space");
    new Label(new Coord(570, y), this, "Calls SmartSpace, for use as custom hotkey");
    y += 15;
    new Label(new Coord(480, y), this, ":animaloutput");
    new Label(new Coord(570, y), this, "Measuring animal stats, see client patch notes for details");
    y += 15;
    new Label(new Coord(480, y), this, ":flatgridsize x");
    new Label(new Coord(570, y), this, "Sets static grid size for CTRL+SHIFT+F to x");
    y += 15;
    new Label(new Coord(480, y), this, ":npcrgb r g b");
    new Label(new Coord(570, y), this, "Set radar rgb of npcs");
    y += 15;
    new Label(new Coord(480, y), this, ":belt");
    new Label(new Coord(570, y), this, "Call quick belt action, see client patch notes for details");
    y += 15;
    new Label(new Coord(480, y), this, ":tbalert");
    new Label(new Coord(570, y), this, "Toggle on/off alert for blooming thornflowers");
    y += 15;
    new Label(new Coord(480, y), this, ":tbcolour r g b");
    new Label(new Coord(570, y), this, "Set colour for blooming thornflowers");
  }
  
  public static HotkeyListWindow instance(UI ui) {
    if (instance == null || instance.ui != ui)
      instance = new HotkeyListWindow(new Coord(100, 100), ui.gui); 
    return instance;
  }
  
  public static void close() {
    if (instance != null) {
      instance.ui.destroy(instance);
      instance = null;
    } 
  }
  
  public void toggle() {
    this.visible = !this.visible;
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (key == '\n' || key == '\033') {
      close();
      return true;
    } 
    return super.type(key, ev);
  }
  
  public void wdgmsg(Widget wdg, String msg, Object... args) {
    if (wdg == this.cbtn) {
      this.ui.destroy(this);
    } else {
      super.wdgmsg(wdg, msg, args);
    } 
  }
  
  public void tick(double dt) {
    if (!this.visible)
      return; 
    super.tick(dt);
  }
}
