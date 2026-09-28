package org.latikai.bots;

import haven.Coord;
import haven.UI;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "mine",
   step = "start"
)
class MineBotStart extends BotState {
   public MineBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Coord> tiles = Bot.getTiles(ui, "mine-", "Mine", true);
      if (tiles == null) {
         return BotState.initializeStack("mine", "end");
      } else {
         MineState state = new MineState();
         state.tiles = tiles;
         state.tiles.sort(new Bot.GridLocation.OfLoc());
         state.last_loc1 = ui.gui.map.player().rc;
         bot.raw_data = state;
         if (!ui.root.cursor.name.contains("mine")) {
            ui.gui.act("mine");
         }

         bot.botSleep(500);
         return BotState.initializeStack("mine", "awaiting_cursor");
      }
   }
}
