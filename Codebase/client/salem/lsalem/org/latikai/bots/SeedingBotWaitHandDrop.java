package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
   bot = "seeding",
   step = "wait_hand_drop"
)
class SeedingBotWaitHandDrop extends BotState {
   public SeedingBotWaitHandDrop() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (!ui.gui.inHand("")) {
         GItem g = ui.gui.maininv.getFirst("seeds-");
         if (g == null) {
            g = ui.gui.maininv.getFirst("maize");
         }

         WItem w = ui.gui.maininv.wmap.get(g);
         if (ui.gui.inHand("")) {
            ui.gui.maininv.drop(Coord.z, w.c);
         } else {
            g.wdgmsg("take", new Object[]{Coord.z});
         }

         return BotState.initializeStack("seeding", "put");
      } else {
         return null;
      }
   }
}
