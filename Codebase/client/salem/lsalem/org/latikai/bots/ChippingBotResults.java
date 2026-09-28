package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "chipping",
   step = "results"
)
class ChippingBotResults extends BotState {
   boolean startedharvesting = false;

   public ChippingBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (ui.gui.prog < 0 && this.startedharvesting) {
         List<Gob> boulders = (List<Gob>)bot.raw_data;
         if (!boulders.isEmpty()) {
            boulders.sort(new Bot.PlayerCloseness.ToGob(ui));
            Gob boulder = boulders.remove(0);
            ui.wdgmsg(ui.gui.map, "click", boulder.sc, boulder.rc, 3, 0, 0, (int)boulder.id, boulder.rc, 0, -1);
            return BotState.initializeStack("chipping", "flowermenu");
         } else {
            ui.message("[Chipping] All boulders chipped - done!", GameUI.MsgType.INFO);
            return BotState.initializeStack("chipping", "end");
         }
      } else {
         if (ui.gui.prog > 0 && !this.startedharvesting) {
            this.startedharvesting = true;
         }

         return null;
      }
   }
}
