package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "block",
   step = "results"
)
class BlockBotResults extends BotState {
   public BlockBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return ui.gui.maininv.countOccurences("chops") > (Integer)bot.raw_data ? BotState.initializeStack("block", "start") : null;
   }
}
