package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "forage",
   step = "end"
)
class ForageBotEnd extends BotState {
   public ForageBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
