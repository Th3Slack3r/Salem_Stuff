package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
   bot = "repotting",
   step = "planting_wait_pot"
)
class RepottingBotPlantFill extends BotState {
   public RepottingBotPlantFill() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      PotState state = (PotState)bot.raw_data;
      if (PotState.needsPicking(state.current())) {
         if (state.hasNext()) {
            state.next();
            GItem g = ui.gui.maininv.getFirst(state.currentContent());
            boolean inorder = false;

            for (GItem gi : ui.gui.hand) {
               if (gi.res.get().name.contains(state.currentContent())) {
                  inorder = true;
               }
            }

            if (!inorder) {
               if (ui.gui.hand.size() > 0) {
                  WItem w = ui.gui.maininv.wmap.get(g);
                  ui.gui.maininv.drop(Coord.z, w.c);
               } else {
                  g.wdgmsg("take", new Object[]{Coord.z});
               }
            }

            bot.botSleep(250);
            return BotState.initializeStack("repotting", "planting_wait_hand");
         } else {
            ui.message("[Repotting] Planted all pots! Repotting bot finished.", GameUI.MsgType.INFO);
            return BotState.initializeStack("repotting", "end");
         }
      } else {
         return null;
      }
   }
}
