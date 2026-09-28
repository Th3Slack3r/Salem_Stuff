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
   step = "humus_wait"
)
class RepottingBotHumusWait extends BotState {
   public RepottingBotHumusWait() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (ui.gui.inHand("humus")) {
         PotState state = (PotState)bot.raw_data;
         if (state.hasNext()) {
            Gob pot = state.next();
            ui.gui.map.wdgmsg("itemact", new Object[]{pot.sc, pot.rc, 1, (int)pot.id, pot.rc, -1});
            return BotState.initializeStack("repotting", "humus_results");
         } else {
            ui.message("[Repotting] Finished putting humus in all pots.", GameUI.MsgType.INFO);
            state.reverse();
            state.index = -1;
            if (!bot.gotWater(ui)) {
               GItem gi = ui.gui.maininv.getFirst("bucket-water");
               WItem w = ui.gui.maininv.wmap.get(gi);
               ui.gui.maininv.drop(Coord.z, w.c);
            }

            return BotState.initializeStack("repotting", "water_wait");
         }
      } else {
         return null;
      }
   }
}
