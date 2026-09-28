package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "drossing",
   step = "results"
)
class DrossingBotResults extends BotState {
   boolean walking = false;

   public DrossingBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (((DrossingState)bot.raw_data).inventorycount > ui.gui.countInventory("")) {
         bot.botSleep(50);
         return BotState.initializeStack("drossing", "put");
      } else {
         return null;
      }
   }
}
