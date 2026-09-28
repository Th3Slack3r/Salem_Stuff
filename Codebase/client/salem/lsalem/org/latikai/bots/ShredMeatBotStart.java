package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.UI;
import haven.WItem;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "shredmeat",
   step = "start"
)
class ShredMeatBotStart extends BotState {
   public ShredMeatBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<WItem> steaks = ui.gui.maininv.getSameName("steak", false);
      List<WItem> slabs = ui.gui.maininv.getSameName("slab", false);
      List<WItem> cuts = ui.gui.maininv.getSameName("cut", false);
      steaks.addAll(slabs);
      steaks.addAll(cuts);
      if (steaks.size() > 0) {
         steaks.get(0).mousedown(Coord.z, 3);
         bot.botSleep(50);
         return BotState.initializeStack("shredmeat", "flowermenu");
      } else {
         ui.message("[ShredMeatBot] Finishing: no more unshred meat left.", GameUI.MsgType.INFO);
         return BotState.initializeStack("shredmeat", "end");
      }
   }
}
