package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "woodcutting",
   step = "end"
)
class WoodcuttingBotEnd extends BotState {
   public WoodcuttingBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
