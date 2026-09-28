package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "preparepots",
   step = "end"
)
class PreparePotsBotEnd extends BotState {
   public PreparePotsBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
