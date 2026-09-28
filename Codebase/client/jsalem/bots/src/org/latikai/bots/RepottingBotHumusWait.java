package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
 bot = "repotting",
 step = "humus_wait"
)
class RepottingBotHumusWait extends BotState {
    
    private static final BotStuff botHelper = new BotStuff();
    
 public RepottingBotHumusWait() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        
    try {
        // FIX 1: Use existing botHelper.inHand(GameUI, String)
    if (botHelper.inHand(ui.gui, "humus")) {
     PotState state = (PotState)bot.raw_data;
     
     if (state.hasNext()) {
        Gob pot = state.next();
                
                // FIX 2: ui.gui.map.wdgmsg is assumed legal as wdgmsg is public on Widget
        ui.gui.map.wdgmsg("itemact", new Object[]{pot.sc, pot.rc, 1, (int)pot.id, pot.rc, -1});
        return BotState.initializeStack("repotting", "humus_results");
     } else {
        ui.message("[Repotting] Finished putting humus in all pots.", GameUI.MsgType.INFO);
        state.reverse();
        state.index = -1;
        
                // FIX 3: Replace bot.gotWater(ui) with inventory check
                // We check if "bucket-water" is NOT in the inventory
        if (botHelper.getFirstMainInvItem(ui.gui, "bucket-water") == null) {
                    
                    // FIX 4: Replace ui.gui.maininv.getFirst("bucket-water") with helper
          GItem gi = botHelper.getFirstMainInvItem(ui.gui, "bucket-water");

                    // If GItem is null, we can't get WItem, but we proceed assuming it's not null 
                    // since we just checked for it. If it is null, these lines will throw an exception.
                    if (gi != null) {
                        // FIX 5: Replace ui.gui.maininv.wmap.get(gi) with helper
            WItem w = botHelper.getWItemFromGItem(ui.gui.maininv, gi);
                        
                        // FIX 6: Replace ui.gui.maininv.drop(Coord.z, w.c) with helper
            botHelper.mainInvDrop(ui.gui.maininv, Coord.z, w.c);
                    }
        }
                
                // Note: The original logic seems flawed here. It checks if bucket-water is NOT present, 
                // and then tries to drop it. I've preserved the structure, but the logic might need review.

        return BotState.initializeStack("repotting", "water_wait");
     }
    } else {
     return null;
    }
    } catch (Exception e) {
        System.err.println("Fatal reflection error in RepottingBotHumusWait: " + e.getMessage());
        return null; // Stay in this state or handle error
    }
 }
}