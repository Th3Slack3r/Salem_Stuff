package org.latikai.bots;

import haven.Gob;
import haven.UI;
import java.util.Iterator;
import java.util.Stack;

@BotAnnotation(
   bot = "redrose",
   step = "find_table"
)
class RedRoseBotFindTable extends BotState {
   public RedRoseBotFindTable() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Iterator<Gob> tables = ((RedRoseState)bot.raw_data).tables;
      if (tables.hasNext()) {
         Gob table = tables.next();
         ui.wdgmsg(ui.gui.map, "click", table.sc, table.rc, 3, 0, 0, (int)table.id, table.rc, 0, -1);
         bot.botSleep(200);
         return BotState.initializeStack("redrose", "wait_table");
      } else {
         return BotState.initializeStack("redrose", "end");
      }
   }
}
