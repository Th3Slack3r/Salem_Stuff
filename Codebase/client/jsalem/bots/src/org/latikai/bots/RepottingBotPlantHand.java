package org.latikai.bots;

import haven.GItem;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "repotting",
   step = "planting_wait_hand"
)
class RepottingBotPlantHand extends BotState {
   public RepottingBotPlantHand() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      PotState state = (PotState)bot.raw_data;
      boolean gotplant = false;

      for (GItem g : ui.gui.hand) {
         if (g.res.get().name.contains(state.currentContent())) {
            gotplant = true;
            break;
         }
      }

      if (gotplant) {
         Gob pot = state.current();
         ui.gui.map.wdgmsg("itemact", new Object[]{pot.sc, pot.rc, 1, (int)pot.id, pot.rc, -1});
         return BotState.initializeStack("repotting", "planting_wait_pot");
      } else {
         return null;
      }
   }
}
