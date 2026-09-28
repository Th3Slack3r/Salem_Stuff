package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Resource;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
 bot = "clay",
 step = "awaiting_cursor"
)
class ClayBotFlowerMenu extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public ClayBotFlowerMenu() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   // Use botHelper for cursor name check (like ChippingBot pattern)
   String cursorName = botHelper.getCursorName(ui);
   if (cursorName.contains("dig")) {
    ui.gui.map.wdgmsg("click", new Object[]{ui.gui.map.player().sc, (Coord)bot.raw_data, 1, 0});
          
          // 2. MODIFIED: Replaced ui.gui.countInventory with botHelper.countInventory
          // and wrapped it in a try-catch block to handle the Resource.Loading exception.
          try {
      bot.raw_data = botHelper.countInventory(ui.gui, "clay");
          } catch (Resource.Loading e) {
              ui.message("[ClayBot] Failed to count clay inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
              // Set raw_data to a known error state or previous value if possible, 
              // for now, we leave it unchanged and return null or handle failure.
              return null; // Stop the bot on failure to count inventory
          }
          
     return BotState.initializeStack("clay", "results");
   } else {
    return null;
   }
 }
}
