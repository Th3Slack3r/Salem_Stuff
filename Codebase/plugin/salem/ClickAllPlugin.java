package haven.plugins;

import haven.Button;
import haven.CheckBox;
import haven.Coord;
import haven.FlowerMenu;
import haven.GItem;
import haven.Glob;
import haven.Gob;
import haven.Label;
import haven.Loading;
import haven.MapView;
import haven.ResDrawable;
import haven.Resource;
import haven.Text;
import haven.TextEntry;
import haven.UI;
import haven.Utils;
import haven.WItem;
import haven.Widget;
import haven.Window;
import haven.FlowerMenu.Petal;
import haven.GameUI.MsgType;
import haven.Glob.Pagina;
import haven.MapView.Camera;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ClickAllPlugin extends Plugin {
   public static final String REEDS = "gfx/invobjs/herbs/reeds";
   public static final String RAGS = "gfx/invobjs/rags";
   public static final String PAPERSCREEN = "gfx/invobjs/paperscreenempty";
   public static final String SOGGY_RAG_PULP = "gfx/invobjs/soggyragpulp";
   public static final String SOGGY_RAG_BALL = "gfx/invobjs/soggyragball";
   public static final String TRIPHAMMER = "gfx/terobjs/triphammer";
   public static int maxRepeats = 0;
   public static int currentRepeats = 0;
   public static boolean isRunning = false;
   public static boolean signalToStop = false;
   public static float distMod = 0.0F;
   public static ClickAllPlugin instance;
   public static boolean doJobThreadRunning = false;
   public static boolean autoCloseWindow = false;
   public static ClickAllPlugin.ChooseActionWindow chooseActionWindow = null;
   public static final String CLICK_ALL_PLUGIN_OPTION_ONE = "CLICK_ALL_PLUGIN_OPTION_ONE";
   private static boolean bigInvConfirmed = false;
   private static long tStamp = System.currentTimeMillis();
   private static boolean lastInvSizeResult = false;

   public void load(UI ui) {
      Glob glob = ui.sess.glob;
      Collection<Pagina> p = glob.paginae;
      p.add(glob.paginafor(Resource.load("paginae/add/clickall")));
      XTendedPaginae.registerPlugin("clickall", this);
   }

   public void execute(final UI ui) {
      instance = this;
      if (doJobThreadRunning) {
         ui.message("[ClickAllPlugin] A HelperWindow Task is still running, trying to stop it now...", MsgType.INFO);
         signalToStop = true;
      } else {
         if (isRunning) {
            signalToStop = true;
         } else {
            isRunning = true;
            new Thread(
                  new Runnable() {
                     @Override
                     public void run() {
                        String itemResName = "";
                        if (ui.gui.hand.isEmpty()) {
                           try {
                              MapView mapView = ui.gui.map;
                              Camera cam = mapView.camera;
                              Class<?> clazz = Class.forName("haven.MapView$FreeCam");
                              if (clazz.isInstance(cam)) {
                                 Field field = clazz.getDeclaredField("dist");
                                 field.setAccessible(true);
                                 float value = field.getFloat(cam);
                                 ui.message("[ClickAllPlugin] value was: " + value + " and will be set to: " + ClickAllPlugin.distMod, MsgType.INFO);
                                 field.setFloat(cam, ClickAllPlugin.distMod);
                                 if (ClickAllPlugin.distMod > -5.0F) {
                                    ClickAllPlugin.distMod--;
                                 } else {
                                    ClickAllPlugin.distMod = 0.0F;
                                 }
                              } else {
                                 ClickAllPlugin.ChooseActionWindow.getChooseActionWindowInstance(ui);
                                 ui.message("[ClickAllPlugin] Your hand (cursor) is empty, take an Item into your hand and try again,", MsgType.INFO);
                                 ui.message("[ClickAllPlugin] or chose a task from the new helper window.", MsgType.INFO);
                              }
                           } catch (Exception var9) {
                              ui.message("[ClickAllPlugin] error: " + var9.toString(), MsgType.INFO);
                              ClickAllPlugin.printStackTraceToSystemChat(var9);
                           }
                        } else {
                           for (GItem item : ui.gui.hand) {
                              itemResName = item.resname();
                              ui.message("[ClickAllPlugin] Hand contains: " + itemResName, MsgType.INFO);
                           }

                           boolean isProvi = ClickAllPlugin.isThisProvi();
                           if (ui.gui.maininv.getSameName("", true).size() > 1023
                              || ui.gui.maininv.getSameName("", true).size() == 200 && !ClickAllPlugin.isBigInv()) {
                              ui.message("[ClickAllPlugin] Your inventory is full, please free up at least one space!", MsgType.INFO);
                              ClickAllPlugin.isRunning = false;
                              ClickAllPlugin.signalToStop = false;
                              return;
                           }

                           ClickAllPlugin.currentRepeats = 0;
                           ui.message(
                              "[ClickAllPlugin] Dropping item in hand to inventory and clicking all items with the same name, please wait...", MsgType.INFO
                           );
                           ClickAllPlugin.dropItem();
                           List<WItem> items = ui.gui.maininv.getSameName("", true);

                           for (int i = items.size() - 1; !ClickAllPlugin.signalToStop && i < items.size() && i >= 0; i--) {
                              WItem subject = items.get(i);
                              if (subject.item.resname().equals(itemResName)) {
                                 subject.mousedown(Coord.z, 3);
                                 ClickAllPlugin.currentRepeats++;
                                 if (ClickAllPlugin.maxRepeats > 0 && ClickAllPlugin.currentRepeats >= ClickAllPlugin.maxRepeats) {
                                    ClickAllPlugin.signalToStop = true;
                                    break;
                                 }

                                 try {
                                    for (int j = 0; j < 5000; j++) {
                                       ClickAllPlugin.sleep(10);
                                       if (ClickAllPlugin.hasFMenu(ui)) {
                                          break;
                                       }
                                    }

                                    for (int jx = 0; jx < 5000; jx++) {
                                       ClickAllPlugin.sleep(10);
                                       if (!ClickAllPlugin.hasFMenu(ui)) {
                                          break;
                                       }
                                    }
                                 } catch (Exception var10) {
                                 }
                              }

                              if (ClickAllPlugin.signalToStop) {
                                 break;
                              }
                           }

                           ClickAllPlugin.signalToStop = true;
                           ui.message("[ClickAllPlugin] Done with clicking!", MsgType.INFO);
                        }

                        if (ClickAllPlugin.signalToStop) {
                           ui.message("[ClickAllPlugin] Exiting...", MsgType.INFO);

                           try {
                              Thread.sleep(1000L);
                           } catch (InterruptedException var8) {
                           }

                           ui.message("[ClickAllPlugin] Stopped.", MsgType.INFO);
                        }

                        ClickAllPlugin.isRunning = false;
                        ClickAllPlugin.signalToStop = false;
                     }
                  },
                  "Click all"
               )
               .start();
         }
      }
   }

   public static Widget[] getWidgetChildren(Widget w) {
      ArrayList<Widget> result = new ArrayList<>();
      if (w.child != null) {
         Widget widget = w.child;
         synchronized (widget) {
            for (Widget wdg = w.child; wdg != null; wdg = wdg.next) {
               result.add(wdg);
            }
         }
      }

      return result.toArray(new Widget[result.size()]);
   }

   public static FlowerMenu getContextMenu(UI ui) {
      for (Widget w : getWidgetChildren(ui.root)) {
         if (w instanceof FlowerMenu) {
            return (FlowerMenu)w;
         }
      }

      return null;
   }

   public static boolean hasFMenu(UI ui) {
      return getContextMenu(ui) != null;
   }

   public static void doReeds(UI ui) {
      clickItemOnItem("gfx/invobjs/herbs/reeds", ui);
   }

   public static void doRags(UI ui) {
      clickItemOnItem("gfx/invobjs/rags", ui);
   }

   public static void doPaperScreens(UI ui) {
      clickItemOnItem("gfx/invobjs/soggyragpulp", "gfx/invobjs/paperscreenempty", ui);
   }

   public static void doHammerSoggyRagBalls(UI ui) {
      clickItemOnItem("gfx/invobjs/soggyragball", null, ui, new String[]{"gfx/terobjs/triphammer"});
   }

   public static void clickItemOnItem(String itemName1, UI ui) {
      clickItemOnItem(itemName1, null, ui, null);
   }

   public static void clickItemOnItem(String itemName1, String itemName2, UI ui) {
      clickItemOnItem(itemName1, itemName2, ui, null);
   }

   public static void clickItemOnItem(String itemName1, String itemName2, UI ui, String[] gobName) {
      clickItemOnItem(itemName1, itemName2, ui, gobName, null, false);
   }

   public static void clickItemOnItem(String itemName1, String itemName2, UI ui, String[] gobName, String[] itemName3, boolean noWaitForEmptyHand) {
      String logNameItem1 = itemName1.split("/")[itemName1.split("/").length - 1];
      List<WItem> item1List = getItemWithName(itemName1);
      List<WItem> item2List = null;
      Gob closestGobWithName = null;
      if (gobName != null) {
         closestGobWithName = getClosestGobWithName(ui, gobName);
      }

      boolean dropItem = false;
      if (itemName2 != null) {
         item2List = getItemWithName(itemName2);
      }

      ui.message("[ClickAllPlugin] doing the " + logNameItem1 + "s... ", MsgType.INFO);
      ui.message("[ClickAllPlugin] " + logNameItem1 + ": " + item1List.size(), MsgType.INFO);
      if (item1List.size() < 1) {
         ui.message("[ClickAllPlugin] not enough " + logNameItem1, MsgType.INFO);
      } else {
         Iterator<WItem> rIt = item1List.iterator();
         Iterator<WItem> rIt2;
         if (itemName2 != null) {
            rIt2 = item2List.iterator();
         } else {
            rIt2 = rIt;
         }

         while (rIt.hasNext() && !signalToStop) {
            WItem next = rIt.next();
            if (takeItem(next, dropItem)) {
               if (closestGobWithName != null) {
                  clickGobAndWaitForProgress(ui, closestGobWithName);
                  waitForNotItemInHand("gfx/invobjs/soggyragball");
                  dropItem = true;
               } else if (rIt2.hasNext()) {
                  next = rIt2.next();
                  clickItem(next);
               } else {
                  dropItem();
               }
            } else {
               ui.message("[ClickAllPlugin] failed to take: " + logNameItem1, MsgType.INFO);
            }
         }

         try {
            sleep(200);
            if (!ui.gui.hand.isEmpty()) {
               dropItem();
            }
         } catch (Exception var14) {
            ui.message("[ClickAllPlugin] error 5: " + var14.getMessage(), MsgType.INFO);
            printStackTraceToSystemChat(var14);
         }
      }
   }

   public static List<WItem> getItemWithName(String s) {
      return UI.instance.gui.maininv.getSameName(s, true);
   }

   public static boolean takeItem(WItem item, boolean dropItem) {
      try {
         String name = item.item.resname();
         if (dropItem) {
            int x = item.server_c.x;
            int y = item.server_c.y;
            UI.instance.wdgmsg(UI.instance.gui.maininv, "drop", new Object[]{new Coord(x, y)});
         } else {
            item.item.wdgmsg("take", new Object[]{Coord.z});
         }

         int counter = 0;

         while (counter < 500) {
            if (!UI.instance.gui.hand.isEmpty() && ((GItem)UI.instance.gui.hand.iterator().next()).resname().contains(name)) {
               return true;
            }

            counter++;
            sleep(10);
         }
      } catch (Exception var5) {
         UI.instance.message("[ClickAllPlugin] error 1" + var5.getMessage(), MsgType.INFO);
         printStackTraceToSystemChat(var5);
      }

      return false;
   }

   private static void dropItem() {
      UI ui = UI.instance;
      boolean[][] grid = new boolean[getInvX()][getInvY()];
      int x = 0;
      int y = 0;
      boolean dontDrop = false;
      List<WItem> items = ui.gui.maininv.getSameName("", true);

      for (int i = items.size() - 1; i < items.size() && i >= 0; i--) {
         WItem subject = items.get(i);
         x = subject.server_c.x;
         y = subject.server_c.y;
         if (y < getInvY() && x < getInvX()) {
            grid[x][y] = true;
         } else {
            dontDrop = true;
         }
      }

      label53:
      for (int ix = 0; ix < getInvY() && !dontDrop; ix++) {
         for (int j = 0; j < getInvX(); j++) {
            if (!grid[j][ix]) {
               ui.wdgmsg(ui.gui.maininv, "drop", new Object[]{new Coord(j, ix)});
               sleep(300);
               break label53;
            }
         }
      }

      if (dontDrop) {
         ui.message("[ClickAllPlugin] Could not find free space in inventory, stopping...", MsgType.INFO);
         signalToStop = true;
      }

      int countForHand = 0;

      while (countForHand < 51 && !ui.gui.hand.isEmpty()) {
         if (++countForHand >= 50) {
            ui.message("[ClickAllPlugin] Failed to drop item to inventory...", MsgType.INFO);
            signalToStop = true;
            break;
         }

         sleep(100);
      }
   }

   private static void clickItem(WItem wItem) {
      wItem.item.wdgmsg("itemact", new Object[]{2});
      int counter = 0;

      while (counter < 500) {
         boolean didClick = false;
         if (UI.instance.gui.hand.isEmpty()) {
            break;
         }

         if (!didClick) {
            try {
               FlowerMenu flowerMenu = getContextMenu(UI.instance);
               Field field = flowerMenu.getClass().getDeclaredField("opts");
               field.setAccessible(true);
               Petal[] opts = (Petal[])field.get(flowerMenu);

               for (Petal petal : opts) {
                  if (petal.name.contains("Roll") || petal.name.contains("Weave")) {
                     flowerMenu.choose(petal);
                     didClick = true;
                     break;
                  }
               }
            } catch (Exception var10) {
            }
         }

         counter++;
         sleep(10);
      }
   }

   public static void test(Method method) {
   }

   public static void sleep(int i) {
      int remaining = i;
      int wait = 0;

      while (!signalToStop && remaining > 0) {
         if (remaining > 50) {
            remaining -= 50;
            wait = 50;
         } else {
            wait = remaining;
            remaining = 0;
         }

         try {
            Thread.sleep(wait);
         } catch (Exception var4) {
         }
      }
   }

   public static void runInThread(String name, final Object arg) {
      if (doJobThreadRunning) {
         UI.instance.message("[ClickAllPlugin] can not start new task until last one finished", MsgType.INFO);
      } else {
         doJobThreadRunning = true;
         final Method method = getMethod(name, arg);
         new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                  if (arg != null) {
                     method.invoke(ClickAllPlugin.instance, arg);
                  } else {
                     method.invoke(ClickAllPlugin.instance);
                  }
               } catch (Exception var5) {
                  UI.instance.message("[ClickAllPlugin] error 2" + var5.getMessage(), MsgType.INFO);
                  ClickAllPlugin.printStackTraceToSystemChat(var5);
               } finally {
                  if (ClickAllPlugin.signalToStop) {
                     UI.instance.message("[ClickAllPlugin] closed the HelperWindow Task ", MsgType.INFO);
                  }

                  ClickAllPlugin.doJobThreadRunning = false;
                  ClickAllPlugin.signalToStop = false;
               }
            }
         }).start();
         if (autoCloseWindow) {
            chooseActionWindow.btnnew.click();
         }
      }
   }

   public static Method getMethod(String name, Object arg) {
      try {
         return instance.getClass().getDeclaredMethod(name, arg.getClass());
      } catch (Exception var4) {
         UI.instance.message("[ClickAllPlugin] error 3" + var4.getMessage(), MsgType.INFO);
         printStackTraceToSystemChat(var4);
         return null;
      }
   }

   private static Gob getClosestGobWithName(UI ui, String[] name) {
      Collection<Gob> gobs = ui.sess.glob.oc.getGobs();
      double distance = 0.0;
      Gob closest_gob = null;
      Iterator<Gob> gobs_iterator = gobs.iterator();
      Gob current_gob = null;
      Coord player_location = ui.gui.map.player().rc;

      while (gobs_iterator.hasNext()) {
         current_gob = gobs_iterator.next();
         Coord gob_location = current_gob.rc;
         ResDrawable rd = null;
         String nm = "";

         try {
            rd = (ResDrawable)current_gob.getattr(ResDrawable.class);
            if (rd != null) {
               nm = ((Resource)rd.res.get()).name;
            }
         } catch (Loading var14) {
         }

         if (checkNames(nm, name)) {
            double this_distance = gob_location.dist(player_location);
            if (this_distance < distance || closest_gob == null) {
               closest_gob = current_gob;
               distance = this_distance;
            }
         }
      }

      return closest_gob;
   }

   private static boolean checkNames(String nm, String[] name) {
      if (name == null) {
         return false;
      } else {
         for (String string : name) {
            if (nm.contains(string)) {
               return true;
            }
         }

         return false;
      }
   }

   private static void clickGobAndWaitForProgress(UI ui, Gob closest_gob) {
      ui.wdgmsg(ui.gui.map, "itemact", new Object[]{closest_gob.sc, closest_gob.rc, 1, (int)closest_gob.id, closest_gob.rc, -1});
      int waitingCount = 0;

      while (!signalToStop) {
         sleep(1000);
         if (getProgress() != -1 || ++waitingCount >= 15) {
            break;
         }
      }

      waitingCount = 0;

      while (!signalToStop && getProgress() != -1) {
         if (++waitingCount >= 15) {
            break;
         }

         sleep(1000);
      }
   }

   private static int getProgress() {
      try {
         return UI.instance.gui.prog;
      } catch (Exception var1) {
         return -1;
      }
   }

   private static boolean waitForNotItemInHand(String itemName) {
      int counter = 0;

      while (counter < 500) {
         if (UI.instance.gui.hand.isEmpty()) {
            return true;
         }

         if (!((GItem)UI.instance.gui.hand.iterator().next()).resname().contains(itemName)) {
            return true;
         }

         counter++;
         sleep(10);
      }

      return false;
   }

   public static void printStackTraceToSystemChat(Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      String[] split = sw.toString().split("\n");

      for (String string : split) {
         UI.instance.message("[ClickAllPlugin] " + string, MsgType.INFO);
      }
   }

   public static boolean getCheckboxValueFromConfig() {
      boolean value = getPrefB("CLICK_ALL_PLUGIN_OPTION_ONE");
      autoCloseWindow = value;
      return value;
   }

   public static boolean getPrefB(String name) {
      try {
         Method method = Utils.class.getMethod("getprefb", String.class, boolean.class);
         method.setAccessible(true);
         Object result = method.invoke(null, name, false);
         if (result != null && result instanceof Boolean) {
            return (Boolean)result;
         }
      } catch (Exception var3) {
      }

      return false;
   }

   public static void saveCheckBoxValueToConfig(boolean value) {
      autoCloseWindow = value;
      setPrefB("CLICK_ALL_PLUGIN_OPTION_ONE", value);
   }

   public static void setPrefB(String name, boolean value) {
      try {
         Method method = Utils.class.getMethod("setprefb", String.class, boolean.class);
         method.setAccessible(true);
         method.invoke(null, name, value);
      } catch (Exception var3) {
      }
   }

   private static int getInvX() {
      return isBigInv() ? 32 : 20;
   }

   private static int getInvY() {
      return isBigInv() ? 32 : 10;
   }

   private static int getInvSize() {
      return isBigInv() ? 1024 : 200;
   }

   private static boolean isBigInv() {
      if (isThisProvi()) {
         bigInvConfirmed = true;
      }

      if (bigInvConfirmed) {
         return true;
      } else {
         long now = System.currentTimeMillis();
         if (now > tStamp + 2000L) {
            tStamp = now;

            try {
               List<WItem> itemsAll = UI.instance.gui.maininv.getSameName("", true);
               boolean result = itemsAll.size() > 200;
               if (anyItemOutside20x10(itemsAll)) {
                  result = true;
               }

               lastInvSizeResult = result;
               if (result) {
                  bigInvConfirmed = true;
               }

               return result;
            } catch (Exception var4) {
               return false;
            }
         } else {
            return lastInvSizeResult;
         }
      }
   }

   private static boolean anyItemOutside20x10(List<WItem> items) {
      int x = 0;
      int y = 0;

      for (int i = items.size() - 1; i < items.size() && i >= 0; i--) {
         WItem subject = items.get(i);
         x = subject.server_c.x;
         y = subject.server_c.y;
         if (x > 20 || y > 10) {
            return true;
         }
      }

      return false;
   }

   private static boolean isThisProvi() {
      boolean returnValue = false;

      try {
         Method m = Utils.class.getDeclaredMethod("isNewServer", null);
         m.setAccessible(true);
         boolean result = (Boolean)m.invoke(null);
         returnValue = !result;
      } catch (Exception var3) {
      }

      return returnValue;
   }

   static class ChooseActionWindow extends Window {
      Button btnnew = null;
      private TextEntry textEntry;

      public static ClickAllPlugin.ChooseActionWindow getChooseActionWindowInstance(UI ui) {
         if (ClickAllPlugin.chooseActionWindow != null && ClickAllPlugin.chooseActionWindow.ui == ui) {
            ClickAllPlugin.chooseActionWindow.btnnew.click();
            return null;
         } else {
            if (ClickAllPlugin.chooseActionWindow != null) {
               ClickAllPlugin.chooseActionWindow.destroy();
            } else {
               ClickAllPlugin.chooseActionWindow = new ClickAllPlugin.ChooseActionWindow(
                  new Coord(100, 100), Coord.z, UI.instance.gui, "ClickAllPlugin Helper Window"
               );
               ClickAllPlugin.chooseActionWindow.setfocus(ClickAllPlugin.chooseActionWindow.btnnew);
            }

            ClickAllPlugin.chooseActionWindow.pack();
            return ClickAllPlugin.chooseActionWindow;
         }
      }

      public ChooseActionWindow(Coord c, Coord sz, Widget parent, String cap) {
         super(new Coord(250, 100), new Coord(520, 650), parent, cap);
         this.justclose = true;
         int coordX = 0;
         int coordY = 0;
         int width = 210;
         new Label(new Coord(10, coordY), this, "max ClickAll repeats: ");
         this.textEntry = new TextEntry(new Coord(120, coordY), 50, this, "" + ClickAllPlugin.maxRepeats) {
            protected void changed() {
               if (super.text.length() > 0) {
                  int value = 0;

                  try {
                     value = Integer.parseInt(super.text);
                  } catch (Exception var3) {
                  }

                  if (value <= 0) {
                     ClickAllPlugin.maxRepeats = 0;
                  } else {
                     ClickAllPlugin.maxRepeats = value;
                  }
               }
            }

            public void activate(String text) {
               ClickAllPlugin.ChooseActionWindow.getChooseActionWindowInstance(this.ui);
               super.activate(text);
            }
         };
         String lt = "Choose Action:";
         coordY += 35;
         new Label(new Coord(10, coordY), this, lt);
         coordY += 25;
         new Button(new Coord(10, coordY), width, this, "Weave Reeds") {
            public void click() {
               this.ui.message("[ClickAllPlugin] Weave Reeds", MsgType.INFO);
               ClickAllPlugin.runInThread("doReeds", this.ui);
            }
         };
         coordY += 35;
         new Button(new Coord(10, coordY), width, this, "Make Balls of Rags") {
            public void click() {
               this.ui.message("[ClickAllPlugin] Make Balls of Rags", MsgType.INFO);
               ClickAllPlugin.runInThread("doRags", this.ui);
            }
         };
         coordY += 35;
         new Button(new Coord(10, coordY), width, this, "Hammer Soggy Rag Balls") {
            public void click() {
               this.ui.message("[ClickAllPlugin] Hammer Soggy Rag Balls", MsgType.INFO);
               ClickAllPlugin.runInThread("doHammerSoggyRagBalls", this.ui);
            }
         };
         coordY += 35;
         new Button(new Coord(10, coordY), width, this, "Fill Paper Screens with Pulp") {
            public void click() {
               this.ui.message("[ClickAllPlugin] Fill Paper Screens with Pulp", MsgType.INFO);
               ClickAllPlugin.runInThread("doPaperScreens", this.ui);
            }
         };
         coordY += 35;
         this.btnnew = new Button(new Coord(10, coordY), width, this, "Close") {
            public void click() {
               super.click();
               ClickAllPlugin.chooseActionWindow = null;
            }
         };
         coordY += 35;
         (new CheckBox(new Coord(10, coordY), this, "Auto-close this Window") {
            {
               this.tooltip = Text.render("If checked, this window will be closed upon starting a task from it.");
            }

            public void changed(boolean val) {
               super.changed(val);
               ClickAllPlugin.saveCheckBoxValueToConfig(val);
            }
         }).a = ClickAllPlugin.getCheckboxValueFromConfig();
      }

      public void wdgmsg(Widget sender, String msg, Object... args) {
         if (sender != this.btnnew && sender != this.cbtn) {
            super.wdgmsg(sender, msg, args);
         } else {
            ClickAllPlugin.chooseActionWindow.ui.destroy(ClickAllPlugin.chooseActionWindow);
            ClickAllPlugin.chooseActionWindow = null;
         }
      }
   }
}
