package haven;

import java.awt.event.KeyEvent;

public class HotkeyListWindow extends Window {
   static final String title = "List of Hotkeys";
   private static HotkeyListWindow instance;

   public HotkeyListWindow(Coord c, Widget parent) {
      super(c, new Coord(300, 100), parent, "List of Hotkeys");
      this.init_components();
      this.toggle();
      this.pack();
   }

   private final void init_components() {
      new Label(Coord.z, this, "Mouse controls");
      int y = 0;
      int x1 = 30;
      int x2 = 120;
      int step = 15;
      int big_step = 30;
      int var6;
      new Label(new Coord(x1, var6 = y + step), this, "Ctrl+left click");
      new Label(new Coord(x2, var6), this, "Drop from inventory.");
      new Label(new Coord(x1, y = var6 + step), this, "Shift+left click");
      new Label(new Coord(x2, y), this, "Transfer between inventories.");
      int var8;
      new Label(new Coord(x1, var8 = y + step), this, "Shift+alt+left click");
      new Label(new Coord(x2, var8), this, "Transfer all similar items between inventories.");
      new Label(new Coord(x1, y = var8 + step), this, "Shift+scrollwheel");
      new Label(new Coord(x2, y), this, "Transfer between inventories (also construction sign slots).");
      int var10;
      new Label(new Coord(x1, var10 = y + step), this, "Shift+right click");
      new Label(new Coord(x2, var10), this, "Interact with object and take similar item from main inventory.");
      new Label(new Coord(0, y = var10 + big_step), this, "Window hotkeys");
      int var12;
      new Label(new Coord(x1, var12 = y + step), this, "Ctrl+E");
      new Label(new Coord(x2, var12), this, "Equipment window");
      new Label(new Coord(x1, y = var12 + step), this, "Ctrl+T");
      new Label(new Coord(x2, y), this, "Study window");
      int var14;
      new Label(new Coord(x1, var14 = y + step), this, "Ctrl+I/Tab");
      new Label(new Coord(x2, var14), this, "Inventory window");
      new Label(new Coord(x1, y = var14 + step), this, "Ctrl+P");
      new Label(new Coord(x2, y), this, "Town window");
      int var16;
      new Label(new Coord(x1, var16 = y + step), this, "Ctrl+C");
      new Label(new Coord(x2, var16), this, "Toggle chat size");
      new Label(new Coord(x1, y = var16 + step), this, "Ctrl+B");
      new Label(new Coord(x2, y), this, "Kin window");
      int var18;
      new Label(new Coord(x1, var18 = y + step), this, "Ctrl+O");
      new Label(new Coord(x2, var18), this, "Option window");
      new Label(new Coord(x1, y = var18 + step), this, "Alt+S/Prnt Scrn");
      new Label(new Coord(x2, y), this, "Screenshot");
      int var20;
      new Label(new Coord(0, var20 = y + big_step), this, "Custom client features");
      new Label(new Coord(x1, y = var20 + step), this, "Ctrl+F/Ctrl+L");
      new Label(new Coord(x2, y), this, "Flatness Tool");
      int var22;
      new Label(new Coord(x1, var22 = y + step), this, "Ctrl+Q");
      new Label(new Coord(x2, var22), this, "Locator Tool");
      new Label(new Coord(x1, y = var22 + step), this, "Ctrl+A");
      new Label(new Coord(x2, y), this, "Inventory abacus");
      int var24;
      new Label(new Coord(x1, var24 = y + step), this, "Ctrl+X");
      new Label(new Coord(x2, var24), this, "Cartographer (unfinished)");
      new Label(new Coord(x1, y = var24 + step), this, "Ctrl+D");
      new Label(new Coord(x2, y), this, "Darkness indicator");
      int var26;
      new Label(new Coord(x1, var26 = y + step), this, "Ctrl+N");
      new Label(new Coord(x2, var26), this, "Toggle forced non-darkness display");
      new Label(new Coord(x1, y = var26 + step), this, "Alt+R");
      new Label(new Coord(x2, y), this, "Toggle radius display (braziers, mining supports,...)");
      int var28;
      new Label(new Coord(x1, var28 = y + step), this, "Alt+C");
      new Label(new Coord(x2, var28), this, "Open the crafting window");
      new Label(new Coord(x1, y = var28 + step), this, "Alt+F");
      new Label(new Coord(x2, y), this, "Open the filter window");
      int var30;
      new Label(new Coord(x1, var30 = y + step), this, "Ctrl+Z");
      new Label(new Coord(x2, var30), this, "Toggle tile centering");
      new Label(new Coord(x1, y = var30 + step), this, "Ctrl+R");
      new Label(new Coord(x2, y), this, "Toggle the toolbelt");
      int var32;
      new Label(new Coord(x1, var32 = y + step), this, "Ctrl+G");
      new Label(new Coord(x2, var32), this, "Toggle the backpack");
      new Label(new Coord(0, y = var32 + big_step), this, "Handy console commands");
      int var34;
      new Label(new Coord(x1, var34 = y + step), this, ":fs 0/1");
      new Label(new Coord(x2, var34), this, "Set fullscreen (buggy!)");
      new Label(new Coord(x1, y = var34 + step), this, ":act lo");
      new Label(new Coord(x2, y), this, "Log out if allowed");
      int var36;
      new Label(new Coord(x1, var36 = y + step), this, ":act lo cs");
      new Label(new Coord(x2, var36), this, "Log out to character selection if allowed");
      new Label(new Coord(x1, y = var36 + step), this, ":lo");
      new Label(new Coord(x2, y), this, "Force disconnect, even if not allowed (at your own risk!)");
   }

   public static HotkeyListWindow instance(UI ui) {
      if (instance == null || instance.ui != ui) {
         instance = new HotkeyListWindow(new Coord(100, 100), ui.gui);
      }

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

   @Override
   public void destroy() {
      instance = null;
      super.destroy();
   }

   @Override
   public boolean type(char key, KeyEvent ev) {
      if (key != '\n' && key != 27) {
         return super.type(key, ev);
      } else {
         close();
         return true;
      }
   }

   @Override
   public void wdgmsg(Widget wdg, String msg, Object... args) {
      if (wdg == this.cbtn) {
         this.ui.destroy(this);
      } else {
         super.wdgmsg(wdg, msg, args);
      }
   }
}
