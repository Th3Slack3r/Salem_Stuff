package org.latikai.bots;

import haven.Config;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "redrose",
   step = "wait_fresh"
)
class RedRoseBotWaitFresh extends BotState {
   public RedRoseBotWaitFresh() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      int oldcount = ((RedRoseState)bot.raw_data).inventory_count;
      if (ui.gui.countInventory("") < oldcount) {
         if (Config.headless) {
            System.out.println("\t[RedRose] Put red rose");
         }

         return BotState.initializeStack("redrose", "find_distiller");
      } else {
         return null;
      }
   }
}
