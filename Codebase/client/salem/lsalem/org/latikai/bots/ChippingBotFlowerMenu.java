package org.latikai.bots;

import haven.FlowerMenu;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "chipping",
   step = "flowermenu"
)
class ChippingBotFlowerMenu extends BotState {
   public ChippingBotFlowerMenu() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Widget mapchild = ui.root.child;

      while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
         mapchild = mapchild.next;
      }

      if (mapchild != null) {
         FlowerMenu fm = (FlowerMenu)mapchild;
         if (fm.opts.length > 0 && "Chip stone".equals(fm.opts[0].name) && ui.rwidgets.containsKey(fm)) {
            try {
               fm.choose(fm.opts[0]);
               return BotState.initializeStack("chipping", "results");
            } catch (Exception var6) {
            }
         }
      }

      return null;
   }
}
