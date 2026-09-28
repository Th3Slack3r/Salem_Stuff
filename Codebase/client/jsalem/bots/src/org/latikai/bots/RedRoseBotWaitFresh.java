package org.latikai.bots;

import haven.GameUI;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
bot = "redrose",
step = "wait_fresh"
)
class RedRoseBotWaitFresh extends BotState {
  
  // Instance of the helper class
  private static final BotStuff botHelper = new BotStuff();
  
public RedRoseBotWaitFresh() {
}

@Override
public Stack<BotState> update(UI ui, Bot bot) {
 int oldcount = ((RedRoseState)bot.raw_data).inventory_count;
   int currentCount = oldcount;
   
   try {
     currentCount = botHelper.countInventory(ui.gui, "");
     
   // FIX: Only catch the parent Exception class to resolve the multi-catch subclassing error.
   // This still catches Resource.Loading and all reflection exceptions.
   } catch (Exception e) { 
     // Handle the error
     ui.message("[RedRoseWait] Error counting inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
     return BotState.initializeStack("redrose", "end");
   }
   
 if (currentCount < oldcount) {
  // FIX: Removed the illegal and non-critical 'if (Config.headless)' block
  // If you must keep the logging, try to use a legal logging method if one exists.
  // For now, we only print the message directly:
  System.out.println("\t[RedRose] Put red rose");
  

  return BotState.initializeStack("redrose", "find_distiller");
 } else {
  return null;
 }
}
}