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
   bot = "preparepots",
   step = "start"
)
class PreparePotsBotStart extends BotState {
   public PreparePotsBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Gob> pots = Bot.getGobs(ui, "herbpot", "PreparePots");
      pots.sort(new Bot.PlayerCloseness.ToGob(ui));
      if (pots != null) {
         if (!pots.isEmpty()) {
            PPotState state = new PPotState();
            state.pots = pots;
            bot.raw_data = state;
            GItem gi = ui.gui.maininv.getFirst("humus");
            WItem wi = ui.gui.maininv.wmap.get(gi);
            if (ui.gui.inHand("")) {
               ui.gui.maininv.drop(Coord.z, wi.c);
            } else {
               gi.wdgmsg("take", new Object[]{Coord.z});
            }

            return BotState.initializeStack("preparepots", "humus_wait");
         }

         ui.message("[PreparePots] No gardening pots found. Exiting.", GameUI.MsgType.INFO);
      }

      return BotState.initializeStack("preparepots", "end");
   }
}
