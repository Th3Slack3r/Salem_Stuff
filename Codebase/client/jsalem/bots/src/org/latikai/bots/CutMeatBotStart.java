package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.UI;
import haven.WItem;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "cutmeat",
   step = "start"
)
class CutMeatBotStart extends BotState {
   public CutMeatBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<WItem> steaks = ui.gui.maininv.getSameName("steak", false);
      List<WItem> slabs = ui.gui.maininv.getSameName("slab", false);
      steaks.addAll(slabs);
      if (steaks.size() > 0) {
         steaks.get(0).mousedown(Coord.z, 3);
         bot.botSleep(50);
         return BotState.initializeStack("cutmeat", "flowermenu");
      } else {
         ui.message("[CutMeatBot] Finishing: no more uncut meat left.", GameUI.MsgType.INFO);
         return BotState.initializeStack("cutmeat", "end");
      }
   }
}
