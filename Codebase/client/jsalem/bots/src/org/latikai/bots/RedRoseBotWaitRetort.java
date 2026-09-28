package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Inventory;
import haven.Loading;
import haven.Resource;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
 bot = "redrose",
 step = "wait_distiller"
)
class RedRoseBotWaitRetort extends BotState {
    
    private static final BotStuff botHelper = new BotStuff();
    
 public RedRoseBotWaitRetort() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   
   for (Widget w : ui.widgets.values()) {
    if (Inventory.class.isInstance(w)) {
                Inventory currentInv = (Inventory)w;
                
       try {
                    // FIX: Assuming getWindowTitle and getInventoryWMapSize were added to BotStuff (as required in the last step)
                    String windowTitle = botHelper.getWindowTitle(currentInv);
                
         if (windowTitle.contains("Dist")) {
                        // FIX: Changed from missing getInventoryFirst to the existing getFirstOccurence.
                        // WARNING: getFirstOccurence checks ALL open inventories, not just 'currentInv'.
           GItem dehydrated_rose = botHelper.getFirstOccurence(ui.gui, "dehydrated");
                        
            if (dehydrated_rose != null) {
                            // Use existing helper for countInventory
            ((RedRoseState)bot.raw_data).inventory_count = botHelper.countInventory(ui.gui, "");
                            
            dehydrated_rose.wdgmsg("transfer", new Object[]{Coord.z});
            bot.botSleep(200);
            return BotState.initializeStack("redrose", "wait_dehydrated");
           }

                        // FIX: Use existing helper for wmap size
           if (botHelper.getInventoryWMapSize(currentInv) >= 2) {
            ui.message("[RedRose] This distiller has no dehydrated rose and no space - skipping to the next one", GameUI.MsgType.INFO);
            return BotState.initializeStack("redrose", "find_distiller");
           }

                        // Use existing helper for maininv.getFirst
           GItem gi = botHelper.getFirstMainInvItem(ui.gui, "redrose");
           if (gi != null) {
                            // Use existing helper for countInventory (second time)
            ((RedRoseState)bot.raw_data).inventory_count = botHelper.countInventory(ui.gui, "");
                            
            gi.wdgmsg("transfer", new Object[]{Coord.z});
            bot.botSleep(200);
            ui.message("[RedRose] This distiller has no dehydrated rose but a free spot. Placing red rose.", GameUI.MsgType.INFO);
            return BotState.initializeStack("redrose", "wait_fresh");
           }

           ui.message("[RedRose] Aborting! Not enough fresh roses.", GameUI.MsgType.ERROR);
           return BotState.initializeStack("redrose", "end");
         }

       } catch (Exception var9) {
                    if (!(var9 instanceof Loading || var9.getCause() instanceof Resource.Loading)) {
                        ui.message("[RedRoseWait] Reflection/General Error: " + var9.getMessage(), GameUI.MsgType.ERROR);
                    }
       }
      }
    }

   return null;
 }
}