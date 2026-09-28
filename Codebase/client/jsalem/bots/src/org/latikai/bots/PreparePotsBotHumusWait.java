package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
 bot = "preparepots",
 step = "humus_wait"
)
class PreparePotsBotHumusWait extends BotState {
    
    // Instance of the helper class
    private static final BotStuff botHelper = new BotStuff();
    
 public PreparePotsBotHumusWait() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        boolean inHand = false;
        try {
            // FIX: Use the existing helper for inHand
            inHand = botHelper.inHand(ui.gui, "humus");
        } catch (Exception e) {
            ui.message("[PPotsWait] Error checking hand: " + e.getMessage(), GameUI.MsgType.ERROR);
            return null;
        }
        
   if (inHand) { 
    PPotState state = (PPotState)bot.raw_data;
    if (state.hasNext()) {
      Gob pot = state.next();
      ui.gui.map.wdgmsg("itemact", new Object[]{pot.sc, pot.rc, 1, (int)pot.id, pot.rc, -1});
      return BotState.initializeStack("preparepots", "humus_results");
    } else {
      ui.message("[PreparePots] Finished putting humus in all pots.", GameUI.MsgType.INFO);
      state.reverse();
      state.index = state.pots.size();
                
             try {
        if (!bot.gotWater(ui)) {
                    // FIX: Use existing helper for getFirst
          GItem gi = botHelper.getFirstMainInvItem(ui.gui, "bucket-water");
                    
                    if (gi != null) {
                        // FIX: Use existing helper for wmap.get
            WItem w = botHelper.getMainInvWItem(ui.gui, gi);
                        
                        // FIX: Use existing helper for drop
            botHelper.dropMainInvItem(ui.gui, Coord.z, w.c);
                    }
        }
             } catch (Exception e) {
                 ui.message("[PreparePots] Error during cleanup: " + e.getMessage(), GameUI.MsgType.ERROR);
             }

      return BotState.initializeStack("preparepots", "water_wait");
    }
   } else {
    return null;
   }
 }
}