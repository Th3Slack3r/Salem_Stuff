package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.UI;
import java.util.Iterator;
import java.util.Stack;

@BotAnnotation(
   bot = "redrose",
   step = "find_distiller"
)
class RedRoseBotFindRetort extends BotState {
   public RedRoseBotFindRetort() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Iterator<GItem> distillers = ((RedRoseState)bot.raw_data).distillers;
      if (distillers.hasNext()) {
         GItem distiller = distillers.next();
         distiller.wdgmsg("iact", new Object[]{Coord.z});
         bot.botSleep(200);
         return BotState.initializeStack("redrose", "wait_distiller");
      } else {
         return BotState.initializeStack("redrose", "find_table");
      }
   }
}
