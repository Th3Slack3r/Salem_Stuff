package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.res.lib.HomeTrackerFX;
import java.util.Stack;

@BotAnnotation(
   bot = "lime",
   step = "chippingresults"
)
class LimeBotChippingResults extends BotState {
   boolean startedharvesting = false;

   public LimeBotChippingResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      LimeState state = (LimeState)bot.raw_data;
      if (ui.gui.inventoriesFull(-3000)) {
         ui.message("[Lime] Inventory full! Bot stopping.", GameUI.MsgType.INFO);
         ui.wdgmsg(ui.gui.map, "click", ui.gui.map.player().sc, ui.gui.map.player().rc, 1, 0);

         for (Gob.Overlay ol : ui.gui.map.player().ols) {
            if (ol.spr.getClass().equals(HomeTrackerFX.class)) {
               HomeTrackerFX htfx = (HomeTrackerFX)ol.spr;
               ui.wdgmsg(ui.gui.map, "click", ui.gui.map.player().sc, htfx.c, 1, 0);
            }
         }

         return BotState.initializeStack("lime", "end");
      } else if (ui.gui.prog < 0 && state.startedchipping) {
         System.out.println("Finished chipping, digging next boulder");
         return BotState.initializeStack("lime", "start");
      } else {
         if (ui.gui.prog >= 0) {
            state.startedchipping = true;
         }

         return null;
      }
   }
}
