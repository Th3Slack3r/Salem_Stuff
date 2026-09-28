package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
 bot = "seeding",
 step = "wait_hand_drop"
)
class SeedingBotWaitHandDrop extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public SeedingBotWaitHandDrop() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
       
       // 2. MODIFIED: First call to inHand
   if (!botHelper.inHand(ui.gui, "")) {
           
    GItem g = ui.gui.maininv.getFirst("seeds-");
    if (g == null) {
      g = ui.gui.maininv.getFirst("maize");
    }

    WItem w = ui.gui.maininv.wmap.get(g);
           
           // 3. MODIFIED: Second call to inHand
    if (botHelper.inHand(ui.gui, "")) {
      ui.gui.maininv.drop(Coord.z, w.c);
    } else {
      g.wdgmsg("take", new Object[]{Coord.z});
    }

    return BotState.initializeStack("seeding", "put");
   } else {
    return null;
   }
 }
}
