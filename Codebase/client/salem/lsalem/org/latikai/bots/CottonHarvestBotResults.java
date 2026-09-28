package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "cottonharvest",
   step = "results"
)
class CottonHarvestBotResults extends BotState {
   boolean startedharvesting = false;

   public CottonHarvestBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (((HarvestState)bot.raw_data).inventorycount < ui.gui.countInventory("")) {
         if (!((HarvestState)bot.raw_data).fields.isEmpty()) {
            Gob field = ((HarvestState)bot.raw_data).fields.remove(0);
            ui.wdgmsg(ui.gui.map, "click", field.sc, field.rc, 3, 0, 0, (int)field.id, field.rc, 0, -1);
            return BotState.initializeStack("cottonharvest", "flowermenu");
         } else {
            ui.message("[CottonHarvest] All fields harvested - done!", GameUI.MsgType.INFO);
            return BotState.initializeStack("cottonharvest", "end");
         }
      } else {
         return null;
      }
   }
}
