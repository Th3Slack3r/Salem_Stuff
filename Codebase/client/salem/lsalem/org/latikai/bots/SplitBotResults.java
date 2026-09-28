package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "split",
   step = "results"
)
class SplitBotResults extends BotState {
   public SplitBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return ui.gui.maininv.countOccurences("leaf0") > (Integer)bot.raw_data ? BotState.initializeStack("split", "start") : null;
   }
}
