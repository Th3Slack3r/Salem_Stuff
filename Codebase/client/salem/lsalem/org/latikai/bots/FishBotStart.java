package org.latikai.bots;

import haven.Coord;
import haven.UI;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "fish",
   step = "start"
)
class FishBotStart extends BotState {
   public FishBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Coord> tiles = Bot.getTiles(ui, "", "Fish", true);
      if (tiles != null && !tiles.isEmpty()) {
         Coord loc = tiles.get(0);
         bot.raw_data = loc;
         if (!ui.root.cursor.name.contains("fish")) {
            ui.gui.act("fish");
         }

         bot.botSleep(500);
         return BotState.initializeStack("fish", "awaiting_cursor");
      } else {
         return BotState.initializeStack("fish", "end");
      }
   }
}
