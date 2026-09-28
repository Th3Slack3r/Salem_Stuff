package org.latikai.bots;

import haven.Coord;
import haven.Resource;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "fish",
   step = "awaiting_cursor"
)
class FishBotWaitCursor extends BotState {
   public FishBotWaitCursor() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Resource curs = ui.root.cursor;
      if (curs.name.contains("fish")) {
         Coord loc = (Coord)bot.raw_data;
         ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, loc, 1, 0});
         bot.botSleep(1500);
         return BotState.initializeStack("fish", "fishresults");
      } else {
         return null;
      }
   }
}
