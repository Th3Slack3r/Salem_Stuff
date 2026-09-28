package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.Resource; // Required for the Resource.Loading exception
import java.util.Stack;

@BotAnnotation(
 bot = "chopping",
 step = "put"
)
class ChoppingBotHandWaiting extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public ChoppingBotHandWaiting() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
       
       // 2. MODIFIED: Replaced ui.gui.inHand(...) with botHelper.inHand(ui.gui, ...)
   if (botHelper.inHand(ui.gui, "woodchops")) {
    if (!((ChoppingState)bot.raw_data).fields.isEmpty()) {
      Gob field = ((ChoppingState)bot.raw_data).fields.remove(0);
      ui.gui.map.wdgmsg("itemact", new Object[]{field.sc, field.rc, 1, (int)field.id, field.rc, -1});
      
             // 3. MODIFIED: Replaced ui.gui.countInventory with botHelper.countInventory
             // and wrapped it in a try-catch block to handle the exception.
             try {
             ((ChoppingState)bot.raw_data).inventorycount = botHelper.countInventory(ui.gui, "");
             } catch (Resource.Loading e) {
                 ui.message("[Chopping] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
                 ((ChoppingState)bot.raw_data).inventorycount = -1; // Indicate failure
             }
             
      return BotState.initializeStack("chopping", "results");
    } else {
      ui.message("[Chopping] Finished putting woodchops on all fields.", GameUI.MsgType.INFO);
      return BotState.initializeStack("chopping", "end");
    }
   } else {
    return null;
   }
 }
}