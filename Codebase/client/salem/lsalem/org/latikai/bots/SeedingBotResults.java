package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "seeding",
   step = "results"
)
class SeedingBotResults extends BotState {
   public SeedingBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (((SeedingState)bot.raw_data).handcount > SeedingState.countHandSeeds(ui)) {
         bot.botSleep(50);
         return BotState.initializeStack("seeding", "put");
      } else {
         return null;
      }
   }
}
