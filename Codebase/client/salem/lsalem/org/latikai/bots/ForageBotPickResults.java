package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "forage",
   step = "pickresults"
)
class ForageBotPickResults extends BotState {
   boolean processing = false;

   public ForageBotPickResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return ui.gui.countInventory("") > (Integer)bot.raw_data ? BotState.initializeStack("forage", "start") : null;
   }
}
