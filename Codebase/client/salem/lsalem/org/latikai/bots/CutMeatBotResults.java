package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "cutmeat",
   step = "results"
)
class CutMeatBotResults extends BotState {
   public CutMeatBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return ui.gui.maininv.countOccurences("") > (Integer)bot.raw_data ? BotState.initializeStack("cutmeat", "start") : null;
   }
}
