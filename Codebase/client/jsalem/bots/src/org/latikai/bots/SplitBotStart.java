package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.UI;
import haven.WItem;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "split",
   step = "start"
)
class SplitBotStart extends BotState {
   public SplitBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<WItem> blocks = ui.gui.maininv.getSameName("cabbage0", false);
      if (blocks.size() > 0) {
         blocks.get(0).mousedown(Coord.z, 3);
         bot.botSleep(50);
         return BotState.initializeStack("split", "flowermenu");
      } else {
         ui.message("[SplitBot] Finishing: no more heads left.", GameUI.MsgType.INFO);
         return BotState.initializeStack("split", "end");
      }
   }
}
