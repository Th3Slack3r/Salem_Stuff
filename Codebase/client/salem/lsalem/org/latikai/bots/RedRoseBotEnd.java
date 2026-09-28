package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "redrose",
   step = "end"
)
class RedRoseBotEnd extends BotState {
   public RedRoseBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
