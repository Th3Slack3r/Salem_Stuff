package org.latikai.bots;

import haven.GameUI;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "block",
   step = "results"
)
class BlockBotResults extends BotState {

   private static final BotStuff botHelper = new BotStuff();

   public BlockBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      try {
         int currentCount = botHelper.countMainInvOccurences(ui.gui, "chops");
         return currentCount > (Integer)bot.raw_data ? BotState.initializeStack("block", "start") : null;
      } catch (Exception e) {
         ui.message("[Block] Error counting chops: " + e.getMessage(), GameUI.MsgType.ERROR);
         return null;
      }
   }
}
