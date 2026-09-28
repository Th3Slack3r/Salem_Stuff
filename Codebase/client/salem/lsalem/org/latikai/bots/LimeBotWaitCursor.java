package org.latikai.bots;

import haven.Resource;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "lime",
   step = "awaiting_cursor"
)
class LimeBotWaitCursor extends BotState {
   public LimeBotWaitCursor() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Resource curs = ui.root.cursor;
      if (curs.name.contains("dig")) {
         ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, ((LimeState)bot.raw_data).lime_location, 1, 0});
         return BotState.initializeStack("lime", "diggingresults");
      } else {
         return null;
      }
   }
}
