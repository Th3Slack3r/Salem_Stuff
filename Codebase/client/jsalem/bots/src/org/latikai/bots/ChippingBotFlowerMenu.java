package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.UI;
import haven.Widget;    // Required for error messages
import java.util.Stack;  // Required for exception handling

@BotAnnotation(
 bot = "chipping",
 step = "flowermenu"
)
class ChippingBotFlowerMenu extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public ChippingBotFlowerMenu() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   ui.message("[Chipping] Looking for FlowerMenu widget...", GameUI.MsgType.INFO);
   Widget mapchild = ui.root.child;

   while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
    mapchild = mapchild.next;
   }

    if (mapchild != null) {
    ui.message("[Chipping] FlowerMenu found!", GameUI.MsgType.INFO);
    FlowerMenu fm = (FlowerMenu)mapchild;
            
           Object chipOpt = null;
           Object[] opts = null;
            
     try {
        opts = botHelper.getFlowerMenuOpts(fm);
        ui.message("[Chipping] Got " + (opts == null ? 0 : opts.length) + " menu options", GameUI.MsgType.INFO);
        
        if (opts != null && opts.length > 0) {
            for (Object opt : opts) {
                if (opt != null) {
                    String optName = botHelper.getFlowerMenuOptionName(opt);
                    ui.message("[Chipping] Option: " + optName, GameUI.MsgType.INFO);
                    if ("Chip stone".equals(optName)) {
                        chipOpt = opt;
                        break;
                    }
                }
            }
        }

        ui.message("[Chipping] chipOpt=" + (chipOpt != null) + " inRwidgets=" + ui.rwidgets.containsKey(fm), GameUI.MsgType.INFO);

        if (chipOpt != null && ui.rwidgets.containsKey(fm)) {
            botHelper.chooseFlowerMenuOption(fm, chipOpt);
            return BotState.initializeStack("chipping", "results");
        }
    } catch (Exception var6) {
        ui.message("[Chipping] FlowerMenu error: " + var6.getMessage(), GameUI.MsgType.ERROR);
        return null;
    }
   }

   return null;
 }
}