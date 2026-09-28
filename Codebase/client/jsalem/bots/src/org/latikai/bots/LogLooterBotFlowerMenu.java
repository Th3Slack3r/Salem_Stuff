package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.Inventory;
import haven.UI;
import haven.Widget;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.util.Stack;

@BotAnnotation(
   bot = "loglooter",
   step = "flowermenu"
)
public class LogLooterBotFlowerMenu extends BotState {

   private static final BotStuff botHelper = new BotStuff();
   private long entered = 0;
   private boolean clickedOpen = false;
   private static final String DEBUG = "D:\\temp\\loglooter_debug.txt";

   private static void dbg(String msg) {
      try {
         File f = new File(DEBUG);
         f.getParentFile().mkdirs();
         PrintWriter pw = new PrintWriter(new FileWriter(f, true));
         pw.println(System.currentTimeMillis() + " " + msg);
         pw.flush();
         pw.close();
      } catch (Exception ignore) {}
   }

   private static boolean hasContainer(UI ui) {
      for (Widget w : ui.widgets.values()) {
         if (w instanceof Inventory && w != ui.gui.maininv) return true;
      }
      return false;
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (entered == 0) entered = System.currentTimeMillis();
      long now = System.currentTimeMillis();

      // After clicking Open, wait for the container window to actually appear
      if (clickedOpen) {
         if (hasContainer(ui)) {
            dbg("Container appeared, going to loot");
            return BotState.initializeStack("loglooter", "loot");
         }
         if (now - entered > 5000) {
            dbg("Container never appeared after 5s, going to loot anyway");
            return BotState.initializeStack("loglooter", "loot");
         }
         return null; // keep waiting
      }

      // Find the flower menu
      Widget fmWidget = null;
      Widget mapchild = ui.root.child;
      while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
         mapchild = mapchild.next;
      }
      if (mapchild instanceof FlowerMenu) fmWidget = mapchild;
      else {
         for (Widget w : ui.widgets.values()) {
            if (w instanceof FlowerMenu) { fmWidget = w; break; }
         }
      }
      if (fmWidget != null) {
         FlowerMenu fm = (FlowerMenu) fmWidget;
         try {
            Object[] opts = botHelper.getFlowerMenuOpts(fm);
            if (opts == null || opts.length == 0) { dbg("opts null/empty"); return null; }
            StringBuilder sb = new StringBuilder();
            for (Object opt : opts) {
               if (opt == null) continue;
               String n = botHelper.getFlowerMenuOptionName(opt);
               if (sb.length() > 0) sb.append(", ");
               sb.append(n == null ? "null" : n);
            }
            dbg("FM opts=[" + sb + "]");
            for (Object opt : opts) {
               if (opt == null) continue;
               String optName = botHelper.getFlowerMenuOptionName(opt);
               if (optName != null && optName.toLowerCase().contains("open")) {
                  botHelper.chooseFlowerMenuOption(fm, opt);
                  dbg("CLICKED open — waiting for container");
                  ui.message("[LogLooter] Open selected.", GameUI.MsgType.INFO);
                  clickedOpen = true;
                  return null; // don't advance yet, wait in next tick
               }
            }
            dbg("No open match, clicking first");
            botHelper.chooseFlowerMenuOption(fm, opts[0]);
            clickedOpen = true;
            return null;
         } catch (Exception e) {
            dbg("ERROR: " + e.getClass().getSimpleName() + " " + e.getMessage());
         }
      }
      if (now - entered > 3500) {
         dbg("FM TIMEOUT — no menu appeared");
         return BotState.initializeStack("loglooter", "loot");
      }
      return null;
   }
}
