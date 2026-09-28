package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.Resource;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "forage",
 step = "flowermenu"
)
class ForageBotFlowerMenu extends BotState {

    private static final BotStuff botHelper = new BotStuff();

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

           Object pickOpt = null;

     try {
               // Use helper to get the options (like ChippingBot)
               Object[] opts = botHelper.getFlowerMenuOpts(fm);

               // Find the "Pick" option using helper method
               if (opts != null && opts.length > 0) {
                   for (Object opt : opts) {
                       if (opt != null) {
                           // Use helper to get the name (handles $Petal vs $MenuOpt)
                           String optName = botHelper.getFlowerMenuOptionName(opt);
                           if ("Pick".equals(optName)) {
                               pickOpt = opt;
                               break;
                           }
                       }
                   }
               }

               // Check for the option and visibility
       if (pickOpt != null && ui.rwidgets.containsKey(fm)) {

                   // Use helper to call package-private choose method
         botHelper.chooseFlowerMenuOption(fm, pickOpt);

         return BotState.initializeStack("forage", "pickresults");
       }

      } catch (Exception var6) {
                ui.message("[Forage] FlowerMenu reflection error: " + var6.getMessage(), GameUI.MsgType.ERROR);
             }
   }

       // 4. MODIFIED: Replaced ui.gui.countInventory("") with botHelper method
       int current_inv_count = -1;
       try {
           current_inv_count = botHelper.countInventory(ui.gui, "");
       } catch (Resource.Loading e) {
           ui.message("[Forage] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
           // If counting fails, prevent starting over and just return null
           return null; 
       }
       
       // Compare current inventory count with the count stored in raw_data
   return current_inv_count > (Integer)bot.raw_data ? BotState.initializeStack("forage", "start") : null;
 }
}