package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.Resource;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "block",
 step = "flowermenu"
)
class BlockBotFlowerMenu extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public BlockBotFlowerMenu() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   Widget mapchild = ui.root.child;

   while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
    mapchild = mapchild.next;
   }

   if (mapchild != null) {
    FlowerMenu fm = (FlowerMenu)mapchild;
           
           Object splitOpt = null;
           
     try {
               // Use helper to get the options (like ChippingBot)
               Object[] opts = botHelper.getFlowerMenuOpts(fm);

               // Find the "Split" option using helper method
               if (opts != null && opts.length > 0) {
                   for (Object opt : opts) {
                       if (opt != null) {
                           // Use helper to get the name (handles $Petal vs $MenuOpt)
                           String optName = botHelper.getFlowerMenuOptionName(opt);
                           if ("Split".equals(optName)) {
                               splitOpt = opt;
                               break;
                           }
                       }
                   }
               }
               
               // 3. MODIFIED: Check for the option and visibility
       if (splitOpt != null && ui.rwidgets.containsKey(fm)) {
                   
                   // Use helper to call package-private choose method
         botHelper.chooseFlowerMenuOption(fm, splitOpt);
                   
                   // 4. MODIFIED: Replaced ui.gui.maininv.countOccurences with botHelper method
         bot.raw_data = botHelper.countMainInvOccurences(ui.gui, "chops");
                   
         return BotState.initializeStack("block", "results");
       }
               
      } catch (Resource.Loading e) {
                // Catch Resource.Loading from the count method
                ui.message("[Block] Failed to count chops: " + e.getMessage(), GameUI.MsgType.ERROR);
                return null;
      } catch (Exception var6) {
                // Catch Reflection/General Exception (the original catch block)
                ui.message("[Block] Reflection/General Error: " + var6.getMessage(), GameUI.MsgType.ERROR);
                return null;
             }
   }

   return null;
 }
}