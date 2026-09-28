package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.WItem;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "repotting",
   step = "pickresults"
)
class RepottingBotResults extends BotState {
   boolean startedharvesting = false;

   public RepottingBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      PotState state = (PotState)bot.raw_data;
      if (PotState.needsPicking(state.current())) {
         return null;
      } else if (state.hasNext()) {
         Gob pot = state.next();
         state.contents.add(PotState.getContents(pot));
         ui.wdgmsg(ui.gui.map, "click", pot.sc, pot.rc, 3, 0, 0, (int)pot.id, pot.rc, 0, -1);
         bot.botSleep(50);
         return BotState.initializeStack("repotting", "flowermenu");
      } else {
         ui.message("[Repotting] All pots picked!", GameUI.MsgType.INFO);
         this.filterContents(state.contents);
         state.reverse();
         state.index = state.pots.size();
         GItem gi = ui.gui.maininv.getFirst("humus");
         WItem wi = ui.gui.maininv.wmap.get(gi);
         if (ui.gui.inHand("")) {
            ui.gui.maininv.drop(Coord.z, wi.c);
         } else {
            gi.wdgmsg("take", new Object[]{Coord.z});
         }

         return BotState.initializeStack("repotting", "humus_wait");
      }
   }

   void filterContents(List<String> contents) {
      for (int i = 0; i < contents.size(); i++) {
         String name = contents.get(i);
         if (name.contains("beaming") || name.contains("jalapeno") || name.contains("redcap")) {
            if (i > 0) {
               contents.set(i, contents.get(i - 1));
            } else {
               contents.set(i, contents.get(i + 1));
            }
         }
      }
   }
}
