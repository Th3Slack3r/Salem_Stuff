package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.Resource;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "cottonharvest",
 step = "flowermenu"
)
class CottonHarvestBotFlowerMenu extends BotState {

    private static final BotStuff botHelper = new BotStuff();

 public Stack<BotState> update(UI ui, Bot bot) {
   Widget mapchild = ui.root.child;

   while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
    mapchild = mapchild.next;
   }

   if (mapchild != null) {
    FlowerMenu fm = (FlowerMenu)mapchild;

           Object harvestOpt = null;

     try {
               // Use helper to get the options (like ChippingBot)
               Object[] opts = botHelper.getFlowerMenuOpts(fm);

               // Find the "Harvest" option using helper method
               if (opts != null && opts.length > 1) {
                   for (Object opt : opts) {
                       if (opt != null) {
                           // Use helper to get the name (handles $Petal vs $MenuOpt)
                           String optName = botHelper.getFlowerMenuOptionName(opt);
                           if ("Harvest".equals(optName)) {
                               harvestOpt = opt;
                               break;
                           }
                       }
                   }
               }

       if (harvestOpt != null && ui.rwidgets.containsKey(fm)) {

         botHelper.chooseFlowerMenuOption(fm, harvestOpt);

         ((HarvestState)bot.raw_data).inventorycount = botHelper.countInventory(ui.gui, "");

         return BotState.initializeStack("cottonharvest", "results");
       }

      } catch (Resource.Loading e) {
                ui.message("[CottonHarvest] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
                return null;
      } catch (Exception var6) {
                ui.message("[CottonHarvest] Reflection Error: " + var6.getMessage(), GameUI.MsgType.ERROR);
                return null;
             }
   }

   return null;
 }
}