package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.Loading;
import haven.UI;
import haven.WItem; // Need to handle Loading exception from getFirstMainInvItem
import java.util.List;
import java.util.Stack;

@BotAnnotation(
 bot = "repotting",
 step = "pickresults"
)
class RepottingBotResults extends BotState {
 boolean startedharvesting = false;
    
    private static final BotStuff botHelper = new BotStuff();

 public RepottingBotResults() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   try {
             PotState state = (PotState)bot.raw_data;
             if (PotState.needsPicking(state.current())) {
        return null;
      } else if (state.hasNext()) {
        Gob pot = state.next();
                // 1. FIX: Use helper for PotState.getContents
        state.contents.add(BotStuff.getPotContents(pot));
                
                // This wdgmsg call on ui.gui.map is assumed legal
        ui.wdgmsg(ui.gui.map, "click", pot.sc, pot.rc, 3, 0, 0, (int)pot.id, pot.rc, 0, -1);
        bot.botSleep(50);
        return BotState.initializeStack("repotting", "flowermenu");
      } else {
        ui.message("[Repotting] All pots picked!", GameUI.MsgType.INFO);
        this.filterContents(state.contents);
        state.reverse();
        state.index = state.pots.size();
                
                // 2. FIX: Use helper for maininv.getFirst
        GItem gi = botHelper.getFirstMainInvItem(ui.gui, "humus");
                
                // If gi is null, we can't proceed
                if (gi == null) {
                    ui.message("[Repotting] Aborting: Humus not found in inventory!", GameUI.MsgType.ERROR);
                    return BotState.initializeStack("repotting", "end");
                }
                
                // 3. FIX: Use helper for maininv.wmap.get
        WItem wi = botHelper.getWItemFromGItem(ui.gui.maininv, gi);
                
                // 4. FIX: Use helper for inHand
        if (botHelper.inHand(ui.gui, "")) {
                    // 5. FIX: Use helper for maininv.drop
          botHelper.mainInvDrop(ui.gui.maininv, Coord.z, wi.c);
        } else {
                    // wdgmsg("take") is assumed legal
          gi.wdgmsg("take", new Object[]{Coord.z});
        }

        return BotState.initializeStack("repotting", "humus_wait");
      }
   } catch (Loading e) {
             // Handle loading exceptions explicitly if necessary
             return null;
      } catch (Exception e) {
             ui.message("[Repotting] Reflection Error: " + e.getMessage(), GameUI.MsgType.ERROR);
             return BotState.initializeStack("repotting", "end");
      }
 }

 void filterContents(List<String> contents) {
   for (int i = 0; i < contents.size(); i++) {
    String name = contents.get(i);
    if (name.contains("beaming") || name.contains("jalapeno") || name.contains("redcap")) {
      if (i > 0) {
       contents.set(i, contents.get(i - 1));
      } else {
       contents.set(i, contents.get(i + 1));
      }
    }
   }
 }
}