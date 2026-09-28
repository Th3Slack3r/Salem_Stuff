package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "chopping",
   step = "results"
)
class ChoppingBotResults extends BotState {
   boolean walking = false;

   public ChoppingBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (((ChoppingState)bot.raw_data).inventorycount > ui.gui.countInventory("")) {
         bot.botSleep(50);
         return BotState.initializeStack("chopping", "put");
      } else {
         return null;
      }
   }
}
