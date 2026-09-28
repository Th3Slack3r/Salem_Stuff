package org.latikai.bots;

import haven.FlowerMenu;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "forage",
   step = "flowermenu"
)
class ForageBotFlowerMenu extends BotState {
   public ForageBotFlowerMenu() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Widget mapchild = ui.root.child;

      while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
         mapchild = mapchild.next;
      }

      if (mapchild != null) {
         FlowerMenu fm = (FlowerMenu)mapchild;
         if (fm.opts.length > 0 && "Pick".equals(fm.opts[0].name) && ui.rwidgets.containsKey(fm)) {
            try {
               fm.choose(fm.opts[0]);
               return BotState.initializeStack("forage", "pickresults");
            } catch (Exception var6) {
            }
         }
      }

      return ui.gui.countInventory("") > (Integer)bot.raw_data ? BotState.initializeStack("forage", "start") : null;
   }
}
