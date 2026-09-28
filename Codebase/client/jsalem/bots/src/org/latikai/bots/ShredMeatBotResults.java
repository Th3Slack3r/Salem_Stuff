package org.latikai.bots;

import haven.GameUI;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "shredmeat",
   step = "results"
)
class ShredMeatBotResults extends BotState {

   private static final BotStuff botHelper = new BotStuff();

   public ShredMeatBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      try {
         int currentCount = botHelper.countMainInvOccurrencesSimple(ui.gui, "");
         return currentCount > (Integer)bot.raw_data ? BotState.initializeStack("shredmeat", "start") : null;
      } catch (Exception e) {
         ui.message("[ShredMeat] Error counting items: " + e.getMessage(), GameUI.MsgType.ERROR);
         return null;
      }
   }
}
