package org.latikai.bots;

import haven.Coord;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
   bot = "sell",
   step = "start"
)
class SellBotStart extends BotState {
   public SellBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      for (WItem w : ui.gui.maininv.wmap.values()) {
         w.mousedown(Coord.z, 1);

         try {
            Thread.sleep(50L);
         } catch (InterruptedException var6) {
         }
      }

      return BotState.initializeStack("sell", "end");
   }
}
