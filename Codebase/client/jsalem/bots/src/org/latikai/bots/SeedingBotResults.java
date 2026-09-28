package org.latikai.bots;

import haven.GameUI;
import haven.UI;    // Required for ui.message/MsgType
import java.util.Stack;  // Required for exception handling

@BotAnnotation(
 bot = "seeding",
 step = "results"
)
class SeedingBotResults extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public SeedingBotResults() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        
        int current_count = -1; // Initialize count
        int previous_count = ((SeedingState)bot.raw_data).handcount;

        // 2. MODIFIED: Use try-catch block for the helper method call
        try {
            // Call the method from BotStuff, passing ui.gui
            current_count = botHelper.countHandSeeds(ui.gui); 
        } catch (Exception e) {
            // Note: countHandSeeds doesn't throw Resource.Loading, but catching Exception is safer
            // if other runtime issues occur. We'll use GameUI.MsgType.ERROR for the log.
            ui.message("[SeedingResults] Failed to count hand seeds: " + e.getMessage(), GameUI.MsgType.ERROR);
            return null; // Stop the bot on failure
        }
        
        // 3. APPLY LOGIC: Use the safe current_count for comparison
    if (previous_count > current_count) {
      bot.botSleep(50);
      return BotState.initializeStack("seeding", "put");
    } else {
      return null;
    }
 }
}
