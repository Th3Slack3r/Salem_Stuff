package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "fish",
   step = "fishresults"
)
class FishBotDiggingResults extends BotState {
   public FishBotDiggingResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      bot.botSleep(8500);
      return BotState.initializeStack("fish", "awaiting_cursor");
   }
}
