package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.Resource; // <-- NEW IMPORT
import haven.UI;
import java.util.Stack;

@BotAnnotation(
    bot = "humus",
    step = "put"
)
class HumusBotHandWaiting extends BotState {
    
    private static final BotStuff botHelper = new BotStuff(); 
    
    public HumusBotHandWaiting() {
    }

    @Override
    public Stack<BotState> update(UI ui, Bot bot) {
        
        if (botHelper.inHand(ui.gui, "humus")) { 
            if (!((HumusState)bot.raw_data).fields.isEmpty()) {
                Gob field = ((HumusState)bot.raw_data).fields.remove(0);
                
                ui.gui.map.wdgmsg("itemact", new Object[]{field.sc, field.rc, 1, (int)field.id, field.rc, -1});
                bot.botSleep(50);
                
                // --- FIX FOR countInventory ERROR ---
                try {
                    ((HumusState)bot.raw_data).inventory_count = botHelper.countInventory(ui.gui, "");
                } catch (Resource.Loading e) {
                    // Handle the exception, usually by logging or halting the bot.
                    // For now, we'll log an error and assume count is zero.
                    ui.message("[Humus] Failed to count inventory: " + e.getMessage(), GameUI.MsgType.ERROR);
                    ((HumusState)bot.raw_data).inventory_count = 0; 
                }
                // ------------------------------------
                
                return BotState.initializeStack("humus", "results");
            } else {
                ui.message("[Humus] Finished putting humus on all fields.", GameUI.MsgType.INFO);
                return BotState.initializeStack("humus", "end");
            }
        } else {
            return null;
        }
    }
}
