package org.latikai.bots;

import haven.GameUI;    // Required for ui.message/MsgType
import haven.Resource;  // Required for the exception handling
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
 bot = "cottonharvest",
 step = "results"
)
class CottonHarvestBotResults extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 boolean startedharvesting = false;

 public CottonHarvestBotResults() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        
        int current_count = -1; // Initialize count
        int previous_count = ((HarvestState)bot.raw_data).inventorycount;

        // 2. MODIFIED: Use try-catch block for the helper method call
        try {
            // Call the method from BotStuff, passing ui.gui
            current_count = botHelper.countInventory(ui.gui, "");
        } catch (Resource.Loading e) {
            // Handle the exception
            ui.message("[CottonHarvestResults] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
            return null; // Stop the bot on failure
        }
        
        // 3. APPLY LOGIC: Use the safe current_count for comparison
    if (previous_count < current_count) {
      if (!((HarvestState)bot.raw_data).fields.isEmpty()) {
       Gob field = ((HarvestState)bot.raw_data).fields.remove(0);
       ui.wdgmsg(ui.gui.map, "click", field.sc, field.rc, 3, 0, 0, (int)field.id, field.rc, 0, -1);
       return BotState.initializeStack("cottonharvest", "flowermenu");
      } else {
       ui.message("[CottonHarvest] All fields harvested - done!", GameUI.MsgType.INFO);
       return BotState.initializeStack("cottonharvest", "end");
      }
    } else {
      return null;
    }
 }
}