package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.Resource;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "mine",
   step = "awaiting_cursor"
)
class MineBotWaitCursor extends BotState {
   public MineBotWaitCursor() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      try {
         // Null safety checks
         if (ui == null || ui.root == null) {
            return null;
         }
         
         Resource curs = ui.root.cursor;
         if (curs == null) {
            return null;  // Wait for cursor to load
         }
         
         // Better cursor detection with fallback
         boolean isMiningCursor = curs.name != null && curs.name.contains("mine");
         
         if (!isMiningCursor) {
            return null;  // Keep waiting for mining cursor
         }
         
         // Mining cursor is now active - click the tile
         MineState state = (MineState)bot.raw_data;
         if (state == null) {
            ui.message("[Mine] State lost!", GameUI.MsgType.ERROR);
            return BotState.initializeStack("mine", "end");
         }
         
         // Get player for click reference
         Gob player = ui.gui.map.player();
         if (player == null) {
            return null;
         }
         
         // NOW click the tile with mining cursor active
         Coord tileToMine = state.last_loc1;
         if (tileToMine == null || tileToMine.equals(Coord.z)) {
            ui.message("[Mine] Invalid tile to mine!", GameUI.MsgType.ERROR);
            return BotState.initializeStack("mine", "end");
         }
         
         ui.gui.map.wdgmsg("click", new Object[]{
             player.sc,      // Player screen coords
             tileToMine,     // Target tile
             1,              // Single click
             0               // No modifiers
         });
         
         ui.message("[Mine] Mining cursor active, clicking tile and monitoring progress...", GameUI.MsgType.INFO);
         bot.botSleep(1500);
         return BotState.initializeStack("mine", "digging_results");
         
      } catch (Exception e) {
         ui.message("[Mine] Cursor check error: " + e.getMessage(), GameUI.MsgType.ERROR);
         e.printStackTrace();
         return null;  // Retry
      }
   }
}
