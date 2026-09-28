package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
 bot = "lime",
 step = "chippingresults"
)
class LimeBotChippingResults extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff();
    
 boolean startedharvesting = false;

 public LimeBotChippingResults() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   LimeState state = (LimeState)bot.raw_data;
       
       int prog = -1;
       boolean isInvFull = false;

       try {
           // 2. MODIFIED: Replaced ui.gui.inventoriesFull(...) with helper method
           isInvFull = botHelper.inventoriesFull(ui.gui, -3000);
           
           // 3. MODIFIED: Replaced ui.gui.prog with helper method
           prog = botHelper.getGameUIProg(ui.gui);
           
       } catch (Exception e) {
           ui.message("[LimeChippingResults] Reflection Error: " + e.getMessage(), GameUI.MsgType.ERROR);
           // On error, stop the bot to prevent infinite loops
           return BotState.initializeStack("lime", "end");
       }
       
       // 4. APPLY LOGIC: Use the retrieved values (prog and isInvFull)
       
   if (isInvFull) {
    ui.message("[Lime] Inventory full! Bot stopping.", GameUI.MsgType.INFO);
    ui.wdgmsg(ui.gui.map, "click", ui.gui.map.player().sc, ui.gui.map.player().rc, 1, 0);

    for (Gob.Overlay ol : ui.gui.map.player().ols) {
      try {
        Class<?> htfxClass = Class.forName("haven.res.lib.HomeTrackerFX");
        if (ol.spr.getClass().equals(htfxClass)) {
          Object htfx = htfxClass.cast(ol.spr);
          Coord c = (Coord)htfxClass.getField("c").get(htfx);
          ui.wdgmsg(ui.gui.map, "click", ui.gui.map.player().sc, c, 1, 0);
        }
      } catch (Exception e) {}
    }

    return BotState.initializeStack("lime", "end");
   } else if (prog < 0 && state.startedchipping) {
    System.out.println("Finished chipping, digging next boulder");
    return BotState.initializeStack("lime", "start");
   } else {
    if (prog >= 0) {
      state.startedchipping = true;
    }

    return null;
   }
 }
}