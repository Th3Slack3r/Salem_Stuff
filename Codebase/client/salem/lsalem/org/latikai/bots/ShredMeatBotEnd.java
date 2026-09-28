package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "shredmeat",
   step = "end"
)
class ShredMeatBotEnd extends BotState {
   public ShredMeatBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
