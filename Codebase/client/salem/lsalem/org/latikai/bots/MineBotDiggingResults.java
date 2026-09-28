package org.latikai.bots;

import haven.Composite;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "mine",
   step = "mineresults"
)
class MineBotDiggingResults extends BotState {
   public MineBotDiggingResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      MineState state = (MineState)bot.raw_data;
      if (ui.root.cursor.name.contains("mine")) {
         ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, state.last_loc1, 3, 0});
      }

      Gob player = ui.gui.map.player();
      Composite comp = player.getattr(Composite.class);
      int equcount = comp.comp.equ.size();
      if (comp.comp.poses.stat) {
         ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, state.last_loc1, 3, 0});
         state.last_loc1 = state.last_loc2;
         bot.botSleep(500);
         return BotState.initializeStack("mine", "droppingresults");
      } else {
         return null;
      }
   }
}
