package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "shredmeat",
   step = "results"
)
class ShredMeatBotResults extends BotState {
   public ShredMeatBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return ui.gui.maininv.countOccurences("") > (Integer)bot.raw_data ? BotState.initializeStack("shredmeat", "start") : null;
   }
}
