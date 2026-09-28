package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
   bot = "repotting",
   step = "water_wait"
)
class RepottingBotWaterWait extends BotState {
   public RepottingBotWaterWait() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (bot.gotWater(ui)) {
         PotState state = (PotState)bot.raw_data;
         if (state.hasNext()) {
            Gob pot = state.next();
            ui.gui.map.wdgmsg("itemact", new Object[]{pot.sc, pot.rc, 1, (int)pot.id, pot.rc, -1});
            return BotState.initializeStack("repotting", "water_results");
         } else {
            ui.message("[Repotting] Finished watering all pots.", GameUI.MsgType.INFO);
            state.reverse();
            state.index = state.pots.size() - 1;
            GItem g = ui.gui.maininv.getFirst(state.currentContent());
            if (ui.gui.hand.size() > 0) {
               WItem w = ui.gui.maininv.wmap.get(g);
               ui.gui.maininv.drop(Coord.z, w.c);
            } else {
               g.wdgmsg("take", new Object[]{Coord.z});
            }

            bot.botSleep(250);
            return BotState.initializeStack("repotting", "planting_wait_hand");
         }
      } else {
         return null;
      }
   }
}
