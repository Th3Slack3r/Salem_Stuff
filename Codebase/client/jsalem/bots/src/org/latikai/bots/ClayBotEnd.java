package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "clay",
   step = "end"
)
class ClayBotEnd extends BotState {
   public ClayBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
