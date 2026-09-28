package org.latikai.bots;

import haven.FlowerMenu;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "shredmeat",
 step = "flowermenu"
)
class ShredMeatBotFlowerMenu extends BotState {
    
    private static final BotStuff botHelper = new BotStuff();
    
 public ShredMeatBotFlowerMenu() {
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
            
            // FIX 4: fm.opts[0].name -> botHelper.getFlowerMenuOptionName(opts[0])
            boolean hasSlice = opts.length > 0 && "Slice".equals(botHelper.getFlowerMenuOptionName(opts[0]));
            
            // FIX 5: ui.rwidgets.containsKey(fm) -> botHelper.uiRWidgetsContains(ui, fm)
            boolean isRendered = botHelper.uiRWidgetsContains(ui, fm);
            
      if (hasSlice && isRendered) {
        try {
                    // FIX 6: fm.choose(fm.opts[0]) -> botHelper.chooseFlowerMenuOption(fm, opts[0])
          botHelper.chooseFlowerMenuOption(fm, opts[0]);
                    
                    // FIX 7: ui.gui.maininv.countOccurences("") -> botHelper.countMainInvOccurrencesSimple(...)
          bot.raw_data = botHelper.countMainInvOccurrencesSimple(ui.gui, "");
                    
          return BotState.initializeStack("shredmeat", "results");
        } catch (Exception var6) {
                    System.err.println("Error during Slice action: " + var6.getMessage());
        }
      }
    }

    } catch (Exception e) {
        System.err.println("Fatal reflection error in ShredMeatBotFlowerMenu: " + e.getMessage());
    }
    
  return null;
 }
}