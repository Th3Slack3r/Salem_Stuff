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
      // Check if the progress bar finished and we were actually harvesting
      if (ui.gui.prog < 0 && this.startedharvesting) {
         
         // 1. Use BotStuff to drop all items matching "rubble"
         // We create a temp instance of BotStuff to access the helper method
         BotStuff helper = new BotStuff();
         helper.dropAllLike(ui.gui, "rubble");

         // 2. Handle the boulder list
         List<Gob> boulders = (List<Gob>)bot.raw_data;
         if (!boulders.isEmpty()) {
            boulders.sort(new Bot.PlayerCloseness.ToGob(ui));
            Gob boulder = boulders.remove(0);
            
            // Click the next boulder
            ui.wdgmsg(ui.gui.map, "click", boulder.sc, boulder.rc, 3, 0, 0, (int)boulder.id, boulder.rc, 0, -1);
            
            // Reset the flag so we wait for the next progress bar to finish
            this.startedharvesting = false; 
            return BotState.initializeStack("chipping", "flowermenu");
         } else {
            ui.message("[Chipping] All boulders chipped - done!", GameUI.MsgType.INFO);
            return BotState.initializeStack("chipping", "end");
         }
      } else {
         // If progress bar is active ( > 0), mark that we have started the action
         if (ui.gui.prog > 0 && !this.startedharvesting) {
            this.startedharvesting = true;
         }
         return null;
      }
   }
}
