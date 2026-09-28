package org.latikai.bots;

import haven.GameUI;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "split",
   step = "results"
)
class SplitBotResults extends BotState {

   private static final BotStuff botHelper = new BotStuff();

   public SplitBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      try {
         int currentCount = botHelper.countMainInvOccurrencesSimple(ui.gui, "leaf0");
         return currentCount > (Integer)bot.raw_data ? BotState.initializeStack("split", "start") : null;
      } catch (Exception e) {
         ui.message("[Split] Error counting items: " + e.getMessage(), GameUI.MsgType.ERROR);
         return null;
      }
   }
}
