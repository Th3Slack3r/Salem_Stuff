package org.latikai.bots;

import haven.Gob;
import haven.UI;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "redrose",
   step = "start"
)
class RedRoseBotStart extends BotState {
   public RedRoseBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Gob> tables = Bot.getGobs(ui, "alchemytable", "RedRose", false);
      bot.raw_data = new RedRoseState();
      ((RedRoseState)bot.raw_data).tables = tables.iterator();
      return BotState.initializeStack("redrose", "find_table");
   }
}
