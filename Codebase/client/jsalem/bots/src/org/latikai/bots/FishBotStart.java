package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.UI;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "fish",
   step = "start"
)
class FishBotStart extends BotState {

   private static final BotStuff botHelper = new BotStuff();

   public FishBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Coord> tiles = Bot.getTiles(ui, "", "Fish", true);
      if (tiles != null && !tiles.isEmpty()) {
         Coord loc = tiles.get(0);
         bot.raw_data = loc;

         // Use botHelper for cursor name check (like ChippingBot pattern)
         String cursorName = botHelper.getCursorName(ui);
         if (!cursorName.contains("fish")) {
            botHelper.act(ui.gui, "fish");
         }

         bot.botSleep(500);
         return BotState.initializeStack("fish", "awaiting_cursor");
      } else {
         ui.message("[Fish] No fishing tiles selected. Exiting.", GameUI.MsgType.INFO);
         return BotState.initializeStack("fish", "end");
      }
   }
}
