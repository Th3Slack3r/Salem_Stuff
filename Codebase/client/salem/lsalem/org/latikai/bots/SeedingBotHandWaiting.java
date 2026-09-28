package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "seeding",
   step = "put"
)
class SeedingBotHandWaiting extends BotState {
   public SeedingBotHandWaiting() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (!ui.gui.inHand("maize") && !ui.gui.inHand("seeds")) {
         return null;
      } else {
         int seedcount = SeedingState.countHandSeeds(ui);
         boolean enough_seeds = seedcount > 12;
         if (enough_seeds) {
            if (!((SeedingState)bot.raw_data).fields.isEmpty()) {
               Gob field = ((SeedingState)bot.raw_data).fields.remove(0);
               ui.gui.map.wdgmsg("itemact", new Object[]{field.sc, field.rc, ui.modflags(), (int)field.id, field.rc, -1});
               ((SeedingState)bot.raw_data).handcount = seedcount;
               return BotState.initializeStack("seeding", "results");
            } else {
               ui.message("[Seeding] Finished seeding on all fields.", GameUI.MsgType.INFO);
               return BotState.initializeStack("seeding", "end");
            }
         } else {
            for (GItem gii : ui.gui.hand) {
               gii.wdgmsg("drop", new Object[]{Coord.z});
            }

            return BotState.initializeStack("seeding", "wait_hand_drop");
         }
      }
   }
}
