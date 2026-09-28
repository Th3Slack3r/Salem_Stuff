package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "lime",
 step = "flowermenu"
)
class LimeBotFlowermenu extends BotState {

    private static final BotStuff botHelper = new BotStuff();

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

           Object chipOpt = null;

     try {
               // Use helper to get the options (like ChippingBot)
               Object[] opts = botHelper.getFlowerMenuOpts(fm);

               // Find the "Chip stone" option using helper method
               if (opts != null && opts.length > 0) {
                   for (Object opt : opts) {
                       if (opt != null) {
                           // Use helper to get the name (handles $Petal vs $MenuOpt)
                           String optName = botHelper.getFlowerMenuOptionName(opt);
                           if ("Chip stone".equals(optName)) {
                               chipOpt = opt;
                               break;
                           }
                       }
                   }
               }

               // Check for the option and call choose if found
       if (chipOpt != null) {

                   // Use helper to call package-private choose method
         botHelper.chooseFlowerMenuOption(fm, chipOpt);

         System.out.println("Initiating lime chipping");
         return BotState.initializeStack("lime", "chippingresults");
       }

      } catch (Exception var6) {
                ui.message("[LimeFM] FlowerMenu reflection error: " + var6.getMessage(), GameUI.MsgType.ERROR);
             }
   }

   return null;
 }
}