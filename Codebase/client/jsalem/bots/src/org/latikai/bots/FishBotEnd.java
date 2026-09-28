package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "fish",
   step = "end"
)
class FishBotEnd extends BotState {
   public FishBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
