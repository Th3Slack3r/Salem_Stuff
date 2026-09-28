package org.latikai.bots;

import haven.Coord;
import haven.FlowerMenu;
import haven.GItem;
import haven.GameUI;
import haven.Inventory;
import haven.UI;
import haven.Widget;
import java.util.Map;
import java.util.Stack;

@BotAnnotation(
   bot = "loglooter",
   step = "loot"
)
public class LogLooterBotLoot extends BotState {

   private long entered = 0;
   private boolean everTransferred = false;
   private boolean studying = false;
   private boolean eating = false;
   private long actionStart = 0;
   private static final BotStuff botHelper = new BotStuff();

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      long now = System.currentTimeMillis();
      if (entered == 0) {
         entered = now;
         ui.message("[LogLooter] Loot step entered.", GameUI.MsgType.INFO);
      }

      if (lastAction + 400 > now) return null;
      lastAction = now;

      try {
         // If we just right-clicked a study/eat item, wait for flower menu and select action
         if (studying || eating) {
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
               Object[] opts = botHelper.getFlowerMenuOpts(fm);
               if (opts != null && opts.length > 0) {
                  String target = studying ? "study" : "eat";
                  for (Object opt : opts) {
                     if (opt == null) continue;
                     String optName = botHelper.getFlowerMenuOptionName(opt);
                     if (optName != null && optName.toLowerCase().contains(target)) {
                        botHelper.chooseFlowerMenuOption(fm, opt);
                        ui.message("[LogLooter] " + (studying ? "Study" : "Eat") + " selected from container.", GameUI.MsgType.INFO);
                        studying = false;
                        eating = false;
                        return null;
                     }
                  }
                  botHelper.chooseFlowerMenuOption(fm, opts[0]);
                  studying = false;
                  eating = false;
                  return null;
               }
            }
            if (now - actionStart > 3000) {
               studying = false;
               eating = false;
            }
            return null;
         }

         // Scan containers for items
         for (Widget w : ui.widgets.values()) {
            if (!(w instanceof Inventory)) continue;
            Inventory inv = (Inventory) w;
            if (inv == ui.gui.maininv) continue;
            try {
               Map<GItem, ? extends haven.WItem> wmap = inv.wmap;
               if (wmap == null) continue;
               for (Map.Entry<GItem, ? extends haven.WItem> entry : wmap.entrySet()) {
                  try {
                     GItem gi = entry.getKey();
                     haven.WItem wi = entry.getValue();
                     String nm = gi.res.get().name;

                     // Study items: right-click to get menu, then select Study
                     if (LogLooterBotStudy.isStudy(nm)) {
                        wi.mousedown(new Coord(0, 0), 3);
                        ui.message("[LogLooter] Found in container: " + nm + " — studying.", GameUI.MsgType.INFO);
                        studying = true;
                        actionStart = now;
                        return null;
                     }

                     // Eat items: right-click to get menu, then select Eat
                     if (LogLooterBotEat.isEat(nm)) {
                        wi.mousedown(new Coord(0, 0), 3);
                        ui.message("[LogLooter] Found in container: " + nm + " — eating.", GameUI.MsgType.INFO);
                        eating = true;
                        actionStart = now;
                        return null;
                     }

                     // Loot items: transfer to inventory
                     if (LogLooterBotStart.isLoot(nm)) {
                        gi.wdgmsg("transfer", new Object[]{wi.c});
                        everTransferred = true;
                        ui.message("[LogLooter] Took: " + nm, GameUI.MsgType.INFO);
                        return null;
                     }
                  } catch (Exception ignore) {
                  }
               }
            } catch (Exception e) {
               ui.message("[LogLooter] Item scan error: " + e.getMessage(), GameUI.MsgType.ERROR);
            }
         }

         // Wait up to 4000ms for the container inventory to appear
         if (entered != 0 && now - entered > 4000) {
            ui.message("[LogLooter] Moving to next log.", GameUI.MsgType.INFO);
            entered = 0;
            return BotState.initializeStack("loglooter", "start");
         }

         return null;
      } catch (Exception e) {
         if (ui != null) ui.message("[LogLooter] Loot error: " + e.getMessage(), GameUI.MsgType.ERROR);
      }
      return null;
   }

   private long lastAction = 0;
}
