package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "humus",
   step = "put"
)
class HumusBotHandWaiting extends BotState {
   public HumusBotHandWaiting() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (ui.gui.inHand("humus")) {
         if (!((HumusState)bot.raw_data).fields.isEmpty()) {
            Gob field = ((HumusState)bot.raw_data).fields.remove(0);
            ui.gui.map.wdgmsg("itemact", new Object[]{field.sc, field.rc, 1, (int)field.id, field.rc, -1});
            bot.botSleep(50);
            ((HumusState)bot.raw_data).inventory_count = ui.gui.countInventory("");
            return BotState.initializeStack("humus", "results");
         } else {
            ui.message("[Humus] Finished putting humus on all fields.", GameUI.MsgType.INFO);
            return BotState.initializeStack("humus", "end");
         }
      } else {
         return null;
      }
   }
}
