package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "drossing",
   step = "put"
)
class DrossingBotHandWaiting extends BotState {
   public DrossingBotHandWaiting() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (ui.gui.inHand("dross")) {
         if (!((DrossingState)bot.raw_data).fields.isEmpty()) {
            Gob field = ((DrossingState)bot.raw_data).fields.remove(0);
            ui.gui.map.wdgmsg("itemact", new Object[]{field.sc, field.rc, 1, (int)field.id, field.rc, -1});
            ((DrossingState)bot.raw_data).inventorycount = ui.gui.countInventory("");
            return BotState.initializeStack("drossing", "results");
         } else {
            ui.message("[Drossing] Finished putting dross on all fields.", GameUI.MsgType.INFO);
            return BotState.initializeStack("drossing", "end");
         }
      } else {
         return null;
      }
   }
}
