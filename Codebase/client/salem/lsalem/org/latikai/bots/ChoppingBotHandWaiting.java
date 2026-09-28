package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "chopping",
   step = "put"
)
class ChoppingBotHandWaiting extends BotState {
   public ChoppingBotHandWaiting() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (ui.gui.inHand("woodchops")) {
         if (!((ChoppingState)bot.raw_data).fields.isEmpty()) {
            Gob field = ((ChoppingState)bot.raw_data).fields.remove(0);
            ui.gui.map.wdgmsg("itemact", new Object[]{field.sc, field.rc, 1, (int)field.id, field.rc, -1});
            ((ChoppingState)bot.raw_data).inventorycount = ui.gui.countInventory("");
            return BotState.initializeStack("chopping", "results");
         } else {
            ui.message("[Chopping] Finished putting woodchops on all fields.", GameUI.MsgType.INFO);
            return BotState.initializeStack("chopping", "end");
         }
      } else {
         return null;
      }
   }
}
