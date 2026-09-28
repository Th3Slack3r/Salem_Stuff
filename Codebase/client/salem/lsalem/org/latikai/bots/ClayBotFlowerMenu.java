package org.latikai.bots;

import haven.Coord;
import haven.Resource;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "clay",
   step = "awaiting_cursor"
)
class ClayBotFlowerMenu extends BotState {
   public ClayBotFlowerMenu() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Resource curs = ui.root.cursor;
      if (curs.name.contains("dig")) {
         ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, (Coord)bot.raw_data, 1, 0});
         bot.raw_data = ui.gui.countInventory("clay");
         return BotState.initializeStack("clay", "results");
      } else {
         return null;
      }
   }
}
