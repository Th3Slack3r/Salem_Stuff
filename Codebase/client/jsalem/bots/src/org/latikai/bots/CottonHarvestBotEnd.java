package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "cottonharvest",
   step = "end"
)
class CottonHarvestBotEnd extends BotState {
   public CottonHarvestBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
