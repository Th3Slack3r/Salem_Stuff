package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "lime",
   step = "diggingresults"
)
class LimeBotDiggingResults extends BotState {

   private static final BotStuff botHelper = new BotStuff();
   boolean startedharvesting = false;

   public LimeBotDiggingResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      LimeState state = (LimeState)bot.raw_data;
      if (!state.starteddigging) {
         state.starteddigging = ui.gui.prog > 0;
         // Use botHelper for cursor name check (like ChippingBot pattern)
         String cursorName = botHelper.getCursorName(ui);
         if (cursorName.contains("dig")) {
            ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, ui.gui.map.player().rc, 3, 0});
         }
      } else if (ui.gui.prog < 0) {
         ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, state.lime_location.add(-25, 0), 3, 0});
         return BotState.initializeStack("lime", "droppingresults");
      }

      return null;
   }
}
