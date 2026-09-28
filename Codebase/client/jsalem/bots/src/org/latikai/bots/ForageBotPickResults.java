package org.latikai.bots;

import haven.GameUI;
import haven.Resource; // Required for exception handling
import haven.UI;   // Required for error messages
import java.util.Stack;

@BotAnnotation(
 bot = "forage",
 step = "pickresults"
)
class ForageBotPickResults extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 boolean processing = false;

 public ForageBotPickResults() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        
        int current_count = -1; // Initialize count
        
        // 2. MODIFIED: Use try-catch block for the helper method call
        try {
            // Call the method from BotStuff, passing ui.gui
            current_count = botHelper.countInventory(ui.gui, "");
        } catch (Resource.Loading e) {
            // Handle the exception
            ui.message("[ForagePickResults] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
            return null; // Stop the bot on failure
        }
        
        // 3. APPLY LOGIC: Compare current count with the previous count stored in raw_data
   return current_count > (Integer)bot.raw_data ? BotState.initializeStack("forage", "start") : null;
 }
}