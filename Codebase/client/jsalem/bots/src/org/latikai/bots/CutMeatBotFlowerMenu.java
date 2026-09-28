package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.Resource;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "cutmeat",
 step = "flowermenu"
)
class CutMeatBotFlowerMenu extends BotState {

    private static final BotStuff botHelper = new BotStuff();

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

           Object sliceOpt = null;

     try {
               // Use helper to get the options (like ChippingBot)
               Object[] opts = botHelper.getFlowerMenuOpts(fm);

               // Find the "Slice" option using helper method
               if (opts != null && opts.length > 0) {
                   for (Object opt : opts) {
                       if (opt != null) {
                           // Use helper to get the name (handles $Petal vs $MenuOpt)
                           String optName = botHelper.getFlowerMenuOptionName(opt);
                           if ("Slice".equals(optName)) {
                               sliceOpt = opt;
                               break;
                           }
                       }
                   }
               }

               // Check for the option and visibility
       if (sliceOpt != null && ui.rwidgets.containsKey(fm)) {

                   // Use helper to call package-private choose method
         botHelper.chooseFlowerMenuOption(fm, sliceOpt);

                   // Use helper to count inventory items
         bot.raw_data = botHelper.countMainInvOccurences(ui.gui, "");

         return BotState.initializeStack("cutmeat", "results");
       }

      } catch (Resource.Loading e) {
                ui.message("[CutMeat] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
                return null;
      } catch (Exception var6) {
                ui.message("[CutMeat] Reflection/General Error: " + var6.getMessage(), GameUI.MsgType.ERROR);
                return null;
             }
   }

   return null;
 }
}
