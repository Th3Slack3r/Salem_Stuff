package org.latikai.bots;

import haven.FlowerMenu;
import haven.GameUI;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "woodcutting",
 step = "flowermenu"
)
class WoodcuttingBotFlowerMenu extends BotState {
    
    private static final BotStuff botHelper = new BotStuff();
    
 public WoodcuttingBotFlowerMenu() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   
    try {
        // FIX 1: ui.root.child -> botHelper.getRootChild(ui)
    Widget mapchild = botHelper.getRootChild(ui);

        // Loop to find FlowerMenu
    while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
     // FIX 2: mapchild.next -> botHelper.getWidgetNext(mapchild)
      mapchild = botHelper.getWidgetNext(mapchild);
    }

    if (mapchild != null) {
     FlowerMenu fm = (FlowerMenu)mapchild;
            
            // FIX 3: fm.opts -> botHelper.getFlowerMenuOpts(fm)
            Object[] opts = botHelper.getFlowerMenuOpts(fm);
            
            // Check array bounds and option name
            boolean hasChopOption = opts.length > 1 && "Chop".equals(botHelper.getFlowerMenuOptionName(opts[1]));
            
            // FIX 5: ui.rwidgets.containsKey(fm) -> botHelper.uiRWidgetsContains(ui, fm)
            boolean isRendered = botHelper.uiRWidgetsContains(ui, fm);
            
      if (!hasChopOption || !isRendered) {
        ui.message("[Woodcutting] This is not a tree - aborting!", GameUI.MsgType.INFO);
        return BotState.initializeStack("woodcutting", "end");
      }

      try {
                // FIX 6: fm.choose(fm.opts[1]) -> botHelper.chooseFlowerMenuOption(fm, opts[1])
        botHelper.chooseFlowerMenuOption(fm, opts[1]);
        return BotState.initializeStack("woodcutting", "chopresults");
      } catch (Exception var6) {
                System.err.println("Error choosing Chop option: " + var6.getMessage());
      }
    }

    } catch (Exception e) {
        System.err.println("Fatal reflection error in WoodcuttingBotFlowerMenu: " + e.getMessage());
    }

  return null;
 }
}
