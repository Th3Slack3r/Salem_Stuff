package org.latikai.bots;

import haven.GameUI;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "cutmeat",
   step = "results"
)
class CutMeatBotResults extends BotState {

   private static final BotStuff botHelper = new BotStuff();

   public CutMeatBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      try {
         int currentCount = botHelper.countMainInvOccurences(ui.gui, "");
         return currentCount > (Integer)bot.raw_data ? BotState.initializeStack("cutmeat", "start") : null;
      } catch (Exception e) {
         ui.message("[CutMeat] Error counting items: " + e.getMessage(), GameUI.MsgType.ERROR);
         return null;
      }
   }
}
