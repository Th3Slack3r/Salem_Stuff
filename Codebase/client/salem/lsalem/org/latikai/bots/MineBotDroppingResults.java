package org.latikai.bots;

import haven.Composite;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "mine",
   step = "droppingresults"
)
class MineBotDroppingResults extends BotState {
   boolean startedharvesting = false;

   public MineBotDroppingResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      MineState state = (MineState)bot.raw_data;
      Gob player = ui.gui.map.player();
      Composite comp = player.getattr(Composite.class);
      if (!comp.comp.poses.stat) {
         ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, state.last_loc2, 1, 0});
         if (!ui.root.cursor.name.contains("mine")) {
            ui.gui.act("mine");
         }

         bot.botSleep(200);
         return BotState.initializeStack("mine", "awaiting_cursor");
      } else {
         return null;
      }
   }
}
