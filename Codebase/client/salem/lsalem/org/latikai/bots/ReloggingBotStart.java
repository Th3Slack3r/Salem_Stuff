package org.latikai.bots;

import haven.Charlist;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "relogging",
   step = "start"
)
class ReloggingBotStart extends BotState {
   public ReloggingBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (bot.raw_data == null) {
         bot.raw_data = 0L;
      }

      if (System.currentTimeMillis() > (Long)bot.raw_data) {
         for (Widget w : ui.rwidgets.keySet()) {
            if (w.getClass().equals(Charlist.class)) {
               Charlist c = (Charlist)w;
               Charlist.Char chosen = c.chars.get(0);
               System.out.println("\t[ReloggingBot] Logging in.");
               c.wdgmsg("play", new Object[]{chosen.name});
               bot.raw_data = System.currentTimeMillis() + 10000L;
               return BotState.initializeStack("relogging", "login");
            }
         }
      }

      return null;
   }
}
