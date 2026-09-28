package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "lime",
   step = "awaiting_cursor"
)
class LimeBotWaitCursor extends BotState {

   private static final BotStuff botHelper = new BotStuff();

   public LimeBotWaitCursor() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      // Use botHelper for cursor name check (like ChippingBot pattern)
      String cursorName = botHelper.getCursorName(ui);
      if (cursorName.contains("dig")) {
         ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, ((LimeState)bot.raw_data).lime_location, 1, 0});
         return BotState.initializeStack("lime", "diggingresults");
      } else {
         return null;
      }
   }
}
