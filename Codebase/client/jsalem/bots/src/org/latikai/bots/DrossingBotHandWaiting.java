package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.Resource;
import haven.UI; // Required for the exception handling
import java.util.Stack;

@BotAnnotation(
 bot = "drossing",
 step = "put"
)
class DrossingBotHandWaiting extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public DrossingBotHandWaiting() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        
       // 2. MODIFIED: Replaced ui.gui.inHand(...) with botHelper.inHand(ui.gui, ...)
   if (botHelper.inHand(ui.gui, "dross")) {
    if (!((DrossingState)bot.raw_data).fields.isEmpty()) {
      Gob field = ((DrossingState)bot.raw_data).fields.remove(0);
      ui.gui.map.wdgmsg("itemact", new Object[]{field.sc, field.rc, 1, (int)field.id, field.rc, -1});
             
             // 3. MODIFIED: Replaced ui.gui.countInventory with botHelper.countInventory
             // and wrapped it in a try-catch block to handle the exception.
             try {
             ((DrossingState)bot.raw_data).inventorycount = botHelper.countInventory(ui.gui, "");
             } catch (Resource.Loading e) {
                 ui.message("[Drossing] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
                 ((DrossingState)bot.raw_data).inventorycount = -1; // Indicate failure
             }
             
      return BotState.initializeStack("drossing", "results");
    } else {
      ui.message("[Drossing] Finished putting dross on all fields.", GameUI.MsgType.INFO);
      return BotState.initializeStack("drossing", "end");
    }
   } else {
    return null;
   }
 }
}
