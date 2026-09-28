package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "replanting",
   step = "finishing"
)
class ReplantingBotFinishing extends BotState {
   public ReplantingBotFinishing() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return BotState.initializeStack("replanting", "end");
   }
}
