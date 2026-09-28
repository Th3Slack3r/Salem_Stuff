package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "relogging",
   step = "login"
)
class ReloggingBotLogin extends BotState {
   public ReloggingBotLogin() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (System.currentTimeMillis() > (Long)bot.raw_data) {
         Object[] args = new Object[]{"lo", "cs"};
         if (ui.gui != null) {
            System.out.println("\t[ReloggingBot] Logging out again.");
            ui.gui.wdgmsg("act", args);
            bot.raw_data = System.currentTimeMillis() + 1500000L;
            return BotState.initializeStack("relogging", "start");
         }
      }

      return null;
   }
}
