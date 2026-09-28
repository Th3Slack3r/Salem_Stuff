package org.latikai.bots;

import haven.GameUI;
import haven.Resource;    // Required for ui.message/MsgType
import haven.UI;  // Required for the exception handling
import java.util.Stack;

@BotAnnotation(
 bot = "chopping",
 step = "results"
)
class ChoppingBotResults extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 boolean walking = false;

 public ChoppingBotResults() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        
        int current_count = -1; // Initialize count
        int previous_count = ((ChoppingState)bot.raw_data).inventorycount;

        // 2. MODIFIED: Use try-catch block for the helper method call
        try {
            // Call the method from BotStuff, passing ui.gui
            current_count = botHelper.countInventory(ui.gui, "");
        } catch (Resource.Loading e) {
            // Handle the exception, and log the error.
            ui.message("[ChoppingResults] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
            return null; // Stop the bot on failure
        }
        
        // 3. APPLY LOGIC: Use the safe current_count for comparison
    if (previous_count > current_count) {
      bot.botSleep(50);
      return BotState.initializeStack("chopping", "put");
    } else {
      return null;
    }
 }
}