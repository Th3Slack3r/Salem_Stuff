package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.UI;
import haven.WItem;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "block",
   step = "start"
)
class BlockBotStart extends BotState {
   public BlockBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<WItem> blocks = ui.gui.maininv.getSameName("wblock", false);
      if (blocks.size() > 0) {
         blocks.get(0).mousedown(Coord.z, 3);
         bot.botSleep(50);
         return BotState.initializeStack("block", "flowermenu");
      } else {
         ui.message("[BlockBot] Finishing: no more blocks left.", GameUI.MsgType.INFO);
         return BotState.initializeStack("block", "end");
      }
   }
}
