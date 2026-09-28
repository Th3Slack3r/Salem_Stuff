package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Loading;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
 bot = "redrose",
 step = "wait_dehydrated"
)
class RedRoseBotWaitDehydrated extends BotState {
    
    private static final BotStuff botHelper = new BotStuff();
    
 public RedRoseBotWaitDehydrated() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   int oldcount = ((RedRoseState)bot.raw_data).inventory_count;
       int currentCount = oldcount;
       
   try {
           // 1. FIX: Use helper for countInventory
           currentCount = botHelper.countInventory(ui.gui, "");
       // FIX: Catch only the parent Exception to resolve the multi-catch error.
       } catch (Exception e) { 
           // If counting fails, we log the error but still proceed with oldcount value
           // which will likely result in a 'null' return if the count hasn't changed.
           ui.message("[RRDW] Error reading inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
       }

   if (currentCount > oldcount) {
    // Removed illegal Config.headless access block
    System.out.println("\t[RedRose] Got dehydrated rose");

    try {
      // Use helper for maininv.getFirst (Throws Exception)
      GItem gi = botHelper.getFirstMainInvItem(ui.gui, "redrose");
             
      if (gi != null) {
       // Use helper for countInventory (Throws Resource.Loading/Exception)
       ((RedRoseState)bot.raw_data).inventory_count = botHelper.countInventory(ui.gui, "");
                
       gi.wdgmsg("transfer", new Object[]{Coord.z});
       bot.botSleep(200);
       return BotState.initializeStack("redrose", "wait_fresh");
      }

      ui.message("[RedRose] Aborting! Not enough fresh roses.", GameUI.MsgType.ERROR);
      return BotState.initializeStack("redrose", "end");
    // Catch the original Loading exception first
    } catch (Loading var5) { 
                // Original code implicitly handles this by continuing
    } catch (Exception e) {
                // Catch any reflection errors from helper methods
                ui.message("[RRDW] Reflection Error: " + e.getMessage(), GameUI.MsgType.ERROR);
                return BotState.initializeStack("redrose", "end");
            }
   }

   return null;
 }
}