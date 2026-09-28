package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
   bot = "preparepots",
   step = "humus_wait"
)
class PreparePotsBotHumusWait extends BotState {
   public PreparePotsBotHumusWait() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (ui.gui.inHand("humus")) {
         PPotState state = (PPotState)bot.raw_data;
         if (state.hasNext()) {
            Gob pot = state.next();
            ui.gui.map.wdgmsg("itemact", new Object[]{pot.sc, pot.rc, 1, (int)pot.id, pot.rc, -1});
            return BotState.initializeStack("preparepots", "humus_results");
         } else {
            ui.message("[PreparePots] Finished putting humus in all pots.", GameUI.MsgType.INFO);
            state.reverse();
            state.index = state.pots.size();
            if (!bot.gotWater(ui)) {
               GItem gi = ui.gui.maininv.getFirst("bucket-water");
               WItem w = ui.gui.maininv.wmap.get(gi);
               ui.gui.maininv.drop(Coord.z, w.c);
            }

            return BotState.initializeStack("preparepots", "water_wait");
         }
      } else {
         return null;
      }
   }
}
