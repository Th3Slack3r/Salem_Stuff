package org.latikai.bots;

import haven.FlowerMenu;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "repotting",
 step = "flowermenu"
)
class RepottingBotFlowerMenu extends BotState {
    
    // Ensure BotStuff instance is available
    private static final BotStuff botHelper = new BotStuff();
    
 public RepottingBotFlowerMenu() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   
    try {
        // FIX 1: Replace ui.root.child access using new helper
    Widget mapchild = botHelper.getRootChild(ui);

        // Loop to find FlowerMenu
    while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
     // FIX 2: Replace mapchild.next access using new helper
      mapchild = botHelper.getWidgetNext(mapchild);
    }

    if (mapchild != null) {
     FlowerMenu fm = (FlowerMenu)mapchild;
            
            // USE EXISTING HELPER: Get options array (fm.opts)
            Object[] opts = botHelper.getFlowerMenuOpts(fm);
            
            // FIX 3: Get name from first option object using new helper
            boolean hasPick = opts.length > 0 && "Pick".equals(botHelper.getFlowerMenuOptionName(opts[0]));
            
            // FIX 4: Check ui.rwidgets.containsKey(fm) using new helper
            boolean isRendered = botHelper.uiRWidgetsContains(ui, fm);
            
      if (hasPick && isRendered) {
        try {
                    // USE EXISTING HELPER: fm.choose(fm.opts[0])
          botHelper.chooseFlowerMenuOption(fm, opts[0]);
          return BotState.initializeStack("repotting", "pickresults");
        } catch (Exception var6) {
                    // Log the error during the choose action
                    System.err.println("Error choosing flower menu option: " + var6.getMessage());
        }
      }
    }

    } catch (Exception e) {
        // Log critical reflection failures
        System.err.println("Fatal reflection error in RepottingBotFlowerMenu: " + e.getMessage());
    }

  return null;
 }
}