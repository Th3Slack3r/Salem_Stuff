package org.latikai.bots;

import haven.Inventory;
import haven.UI;
import haven.Widget;
import haven.Window;
import java.util.Stack;

@BotAnnotation(
   bot = "redrose",
   step = "wait_table"
)
class RedRoseBotWaitTable extends BotState {
   public RedRoseBotWaitTable() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Widget window = null;

      for (Widget w : ui.widgets.values()) {
         if (Inventory.class.isInstance(w)) {
            Window wp = ((Inventory)w).getparent(Window.class);
            if (wp.cap.text.equals("Alchemy Table")) {
               ((RedRoseState)bot.raw_data).distillers = ((Inventory)w).wmap.keySet().iterator();
               bot.botSleep(200);
               return BotState.initializeStack("redrose", "find_distiller");
            }
         }
      }

      return null;
   }
}
