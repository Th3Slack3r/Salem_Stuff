package org.latikai.bots;

import haven.FlowerMenu;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "cottonharvest",
   step = "flowermenu"
)
class CottonHarvestBotFlowerMenu extends BotState {
   public CottonHarvestBotFlowerMenu() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Widget mapchild = ui.root.child;

      while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
         mapchild = mapchild.next;
      }

      if (mapchild != null) {
         FlowerMenu fm = (FlowerMenu)mapchild;
         if (fm.opts.length > 0 && "Harvest".equals(fm.opts[1].name) && ui.rwidgets.containsKey(fm)) {
            try {
               fm.choose(fm.opts[1]);
               ((HarvestState)bot.raw_data).inventorycount = ui.gui.countInventory("");
               return BotState.initializeStack("cottonharvest", "results");
            } catch (Exception var6) {
            }
         }
      }

      return null;
   }
}
