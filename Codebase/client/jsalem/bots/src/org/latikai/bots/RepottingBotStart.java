package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "repotting",
   step = "start"
)
class RepottingBotStart extends BotState {
   public RepottingBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Gob> pots = Bot.getGobs(ui, "herbpot", "Repotting");
      pots.sort(new Bot.PlayerCloseness.ToGob(ui));
      if (pots != null) {
         if (!pots.isEmpty()) {
            PotState state = new PotState();
            state.pots = pots;
            bot.raw_data = state;
            Gob pot = state.current();
            state.contents.add(PotState.getContents(pot));
            ui.wdgmsg(ui.gui.map, "click", pot.sc, pot.rc, 3, 0, 0, (int)pot.id, pot.rc, 0, -1);
            bot.botSleep(50);
            return BotState.initializeStack("repotting", "flowermenu");
         }

         ui.message("[Repotting] No gardening pots found. Exiting.", GameUI.MsgType.INFO);
      }

      return BotState.initializeStack("repotting", "end");
   }
}
