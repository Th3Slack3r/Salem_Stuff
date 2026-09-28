package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "preparepots",
   step = "water_wait"
)
class PreparePotsBotWaterWait extends BotState {
   public PreparePotsBotWaterWait() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (bot.gotWater(ui)) {
         PPotState state = (PPotState)bot.raw_data;
         if (state.hasNext()) {
            Gob pot = state.next();
            ui.gui.map.wdgmsg("itemact", new Object[]{pot.sc, pot.rc, 1, (int)pot.id, pot.rc, -1});
            return BotState.initializeStack("preparepots", "water_results");
         } else {
            ui.message("[PreparePots] Finished watering all pots.", GameUI.MsgType.INFO);
            return BotState.initializeStack("preparepots", "end");
         }
      } else {
         return null;
      }
   }
}
