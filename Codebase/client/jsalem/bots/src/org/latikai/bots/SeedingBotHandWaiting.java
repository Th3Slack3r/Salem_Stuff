package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
 bot = "seeding",
 step = "put"
)
class SeedingBotHandWaiting extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff();
    
 public SeedingBotHandWaiting() {
 }

@Override
 public Stack<BotState> update(UI ui, Bot bot) {
   if (!botHelper.inHand(ui.gui, "maize") && !botHelper.inHand(ui.gui, "seeds")) {
    return null;
   } else {
    // MODIFIED: Call the helper method on the botHelper instance
    int seedcount = botHelper.countHandSeeds(ui.gui); 
            
    boolean enough_seeds = seedcount > 12;
            
     if (enough_seeds) {
       if (!((SeedingState)bot.raw_data).fields.isEmpty()) {
         Gob field = ((SeedingState)bot.raw_data).fields.remove(0);
         ui.gui.map.wdgmsg("itemact", new Object[]{field.sc, field.rc, ui.modflags(), (int)field.id, field.rc, -1});
         ((SeedingState)bot.raw_data).handcount = seedcount;
         return BotState.initializeStack("seeding", "results");
       } else {
         ui.message("[Seeding] Finished seeding on all fields.", GameUI.MsgType.INFO);
         return BotState.initializeStack("seeding", "end");
       }
     } else {
                // 4. NOTE: ui.gui.hand is a non-static collection you can iterate over, 
                // but if 'ui.gui.hand' itself causes a "non-static variable" error, 
                // you would need to adjust this loop to pass ui.gui to another helper method.
                // Based on previous errors, this loop should be fine if ui.gui is the GameUI instance.
       for (GItem gii : ui.gui.hand) {
         gii.wdgmsg("drop", new Object[]{Coord.z});
       }

       return BotState.initializeStack("seeding", "wait_hand_drop");
     }
     }
 }
}