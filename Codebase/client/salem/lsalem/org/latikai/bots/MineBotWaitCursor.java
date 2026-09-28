package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Resource;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "mine",
   step = "awaiting_cursor"
)
class MineBotWaitCursor extends BotState {
   public MineBotWaitCursor() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Resource curs = ui.root.cursor;
      if (curs.name.contains("mine")) {
         MineState state = (MineState)bot.raw_data;
         if (state.tiles.isEmpty()) {
            ui.message("[Mine] No mineable tiles available. Exiting.", GameUI.MsgType.INFO);
            return BotState.initializeStack("mine", "end");
         } else {
            Coord next_loc = state.tiles.remove(0);
            state.last_loc2 = next_loc;
            ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, next_loc, 1, 0});
            bot.botSleep(1500);
            return BotState.initializeStack("mine", "mineresults");
         }
      } else {
         return null;
      }
   }
}
