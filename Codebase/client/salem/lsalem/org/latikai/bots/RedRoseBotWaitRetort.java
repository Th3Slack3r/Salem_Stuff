package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Inventory;
import haven.Loading;
import haven.UI;
import haven.Widget;
import haven.Window;
import java.util.Stack;

@BotAnnotation(
   bot = "redrose",
   step = "wait_distiller"
)
class RedRoseBotWaitRetort extends BotState {
   public RedRoseBotWaitRetort() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Widget window = null;

      for (Widget w : ui.widgets.values()) {
         if (Inventory.class.isInstance(w)) {
            Window wp = ((Inventory)w).getparent(Window.class);
            if (wp.cap.text.contains("Dist")) {
               try {
                  GItem dehydrated_rose = ((Inventory)w).getFirst("dehydrated");
                  if (dehydrated_rose != null) {
                     ((RedRoseState)bot.raw_data).inventory_count = ui.gui.countInventory("");
                     dehydrated_rose.wdgmsg("transfer", new Object[]{Coord.z});
                     bot.botSleep(200);
                     return BotState.initializeStack("redrose", "wait_dehydrated");
                  }

                  if (((Inventory)w).wmap.size() >= 2) {
                     ui.message("[RedRose] This distiller has no dehydrated rose and no space - skipping to the next one", GameUI.MsgType.INFO);
                     return BotState.initializeStack("redrose", "find_distiller");
                  }

                  GItem gi = ui.gui.maininv.getFirst("redrose");
                  if (gi != null) {
                     ((RedRoseState)bot.raw_data).inventory_count = ui.gui.countInventory("");
                     gi.wdgmsg("transfer", new Object[]{Coord.z});
                     bot.botSleep(200);
                     ui.message("[RedRose] This distiller has no dehydrated rose but a free spot. Placing red rose.", GameUI.MsgType.INFO);
                     return BotState.initializeStack("redrose", "wait_fresh");
                  }

                  ui.message("[RedRose] Aborting! Not enough fresh roses.", GameUI.MsgType.ERROR);
                  return BotState.initializeStack("redrose", "end");
               } catch (Loading var9) {
               }
            }
         }
      }

      return null;
   }
}
