package org.latikai.bots;

import haven.FlowerMenu;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "cutmeat",
   step = "flowermenu"
)
class CutMeatBotFlowerMenu extends BotState {
   public CutMeatBotFlowerMenu() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Widget mapchild = ui.root.child;

      while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
         mapchild = mapchild.next;
      }

      if (mapchild != null) {
         FlowerMenu fm = (FlowerMenu)mapchild;
         if (fm.opts.length > 0 && "Slice".equals(fm.opts[0].name) && ui.rwidgets.containsKey(fm)) {
            try {
               fm.choose(fm.opts[0]);
               bot.raw_data = ui.gui.maininv.countOccurences("");
               return BotState.initializeStack("cutmeat", "results");
            } catch (Exception var6) {
            }
         }
      }

      return null;
   }
}
