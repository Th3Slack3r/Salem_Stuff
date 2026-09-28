package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.WItem;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
 bot = "preparepots",
 step = "start"
)
class PreparePotsBotStart extends BotState {
    
    // Instance of the helper class
    private static final BotStuff botHelper = new BotStuff();
    
 public PreparePotsBotStart() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   List<Gob> pots = Bot.getGobs(ui, "herbpot", "PreparePots");
   pots.sort(new Bot.PlayerCloseness.ToGob(ui));
      
   if (pots != null) {
    if (!pots.isEmpty()) {
      PPotState state = new PPotState();
      state.pots = pots;
      bot.raw_data = state;
             
             GItem gi = null;
             WItem wi = null;
             boolean inHand = false;
             
             try {
                // Get the item and check what's in hand (all throwing Exception)
        gi = botHelper.getFirstMainInvItem(ui.gui, "humus");
                
                if (gi != null) {
                    wi = botHelper.getMainInvWItem(ui.gui, gi);
                } else {
                    ui.message("[PreparePots] Humus not found in inventory. Exiting.", GameUI.MsgType.INFO);
                    return BotState.initializeStack("preparepots", "end");
                }
                
                inHand = botHelper.inHand(ui.gui, "");
                
                // --- THE FIX IS HERE ---
                // If inHand, call dropMainInvItem inside the try block
                if (inHand) {
          botHelper.dropMainInvItem(ui.gui, Coord.z, wi.c);
        } 
                // --- END FIX ---

             } catch (Exception e) {
                 ui.message("[PPotsStart] Reflection Error: " + e.getMessage(), GameUI.MsgType.ERROR);
                 return BotState.initializeStack("preparepots", "end");
             }
             
             // The only logic left outside the try-catch block is the safe wdgmsg call
      if (!inHand) { 
        gi.wdgmsg("take", new Object[]{Coord.z});
      }

      return BotState.initializeStack("preparepots", "humus_wait");
    }

    ui.message("[PreparePots] No gardening pots found. Exiting.", GameUI.MsgType.INFO);
   }

   return BotState.initializeStack("preparepots", "end");
 }
}