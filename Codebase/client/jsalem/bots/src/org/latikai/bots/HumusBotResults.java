package org.latikai.bots;

import haven.GameUI;
import haven.Resource; // Required for ui.message and MsgType
import haven.UI; // Required for the exception handling
import java.util.Stack;

@BotAnnotation(
 bot = "humus",
 step = "results"
)
class HumusBotResults extends BotState {
 
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
    boolean walking = false;

 public HumusBotResults() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        
        int current_count = -1; // Initialize to an error/default state
        int previous_count = ((HumusState)bot.raw_data).inventory_count;

        // 2. MODIFIED: Use try-catch block for the helper method call
        try {
            // Get the current inventory count using the helper method
            current_count = botHelper.countInventory(ui.gui, "");
        } catch (Resource.Loading e) {
            // Handle the exception
            ui.message("[HumusResults] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
            return null; // Stop the bot on failure
        }
        
        // 3. APPLY LOGIC: Use the safe count for comparison
        // Original logic: current_count < previous_count ? initializeStack("put") : null;
    return current_count < previous_count ? BotState.initializeStack("humus", "put") : null;
 }
}