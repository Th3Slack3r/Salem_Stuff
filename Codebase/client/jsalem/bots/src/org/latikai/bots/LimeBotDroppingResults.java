package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.Moving;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "lime",
   step = "droppingresults"
)
class LimeBotDroppingResults extends BotState {
   boolean startedharvesting = false;

   public LimeBotDroppingResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      LimeState state = (LimeState)bot.raw_data;
      Gob pl = ui.gui.map.player();
      if (!state.startedmoving && pl.getattr(Moving.class) != null) {
         state.startedmoving = true;
      }

      if (pl.getattr(Moving.class) == null && state.startedmoving) {
         Gob lime = bot.getClosestGob(ui, "lime");
         if (lime != null) {
            ui.wdgmsg(ui.gui.map, "click", lime.sc, lime.rc, 3, 0, 0, (int)lime.id, lime.rc, 0, -1);
            return BotState.initializeStack("lime", "flowermenu");
         } else {
            ui.message("[Limebot] No lime boulder found!", GameUI.MsgType.INFO);
            return BotState.initializeStack("lime", "start");
         }
      } else {
         return null;
      }
   }
}
