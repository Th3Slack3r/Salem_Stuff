package org.latikai.bots;

import haven.GameUI;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "mine",
   step = "droppingresults"
)
class MineBotDroppingResults extends BotState {

   public MineBotDroppingResults() {
   }

   private boolean waitedAfterDrop = false;

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      ui.message("[Mine] DroppingResults state entered", GameUI.MsgType.INFO);

      if (!waitedAfterDrop) {
         // Just wait a bit for the drop to complete, like ChippingBot does
         ui.message("[Mine] Waiting for drop to complete...", GameUI.MsgType.INFO);
          bot.botSleep(500);
         waitedAfterDrop = true;
         return null;
      }

      // Go back to start to find next tile
      ui.message("[Mine] Drop complete, looking for next tile...", GameUI.MsgType.INFO);
      return BotState.initializeStack("mine", "start");
   }
}
