package org.latikai.bots;

import haven.FlowerMenu;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "lime",
   step = "flowermenu"
)
class LimeBotFlowermenu extends BotState {
   boolean startedharvesting = false;

   public LimeBotFlowermenu() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Widget mapchild = ui.root.child;

      while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
         mapchild = mapchild.next;
      }

      if (mapchild != null) {
         FlowerMenu fm = (FlowerMenu)mapchild;
         if (fm.opts.length > 0 && "Chip stone".equals(fm.opts[0].name)) {
            fm.choose(fm.opts[0]);
            System.out.println("Initiating lime chipping");
            return BotState.initializeStack("lime", "chippingresults");
         }
      }

      return null;
   }
}
