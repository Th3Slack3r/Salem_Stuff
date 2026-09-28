package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.Moving;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "mine",
   step = "digging_results"
)
class MineBotDiggingResults extends BotState {
   private static final BotStuff botHelper = new BotStuff();
   private boolean minedStarted = false;
   private boolean exitedMiningMode = false;

   public MineBotDiggingResults() {
   }

   @Override
   public Stack<BotState> update(UI var1, Bot var2) {
      MineState var3 = (MineState)var2.raw_data;
      if (var3 == null) {
         var1.message("[Mine] Invalid state data", GameUI.MsgType.ERROR);
         return BotState.initializeStack("mine", "end");
      }

      Gob var4 = var1.gui.map.player();
      if (var4 == null) {
         return null;
      }

      try {
         // Check if mining progress bar is active
         if (var1.gui.prog > 0 && !this.minedStarted) {
            this.minedStarted = true;
            var1.message("[Mine] Mining in progress...", GameUI.MsgType.INFO);
            return null;
         }

         // Wait until mining is complete (progress bar finished)
         if (this.minedStarted && var1.gui.prog < 0 && !this.exitedMiningMode) {
            // Mining complete - player has boulder in hand, need to exit mining mode
            var1.message("[Mine] Mining complete, exiting mining mode...", GameUI.MsgType.INFO);

            // Calculate drop tile by moving player away from mining location
            // Direction: from mining tile towards player, then further away
            int dirX = var4.rc.x - var3.last_loc1.x;  // Direction away from mine
            int dirY = var4.rc.y - var3.last_loc1.y;

            // Normalize direction and extend it further back
            int magnitude = (int)Math.sqrt(dirX * dirX + dirY * dirY);
            if (magnitude == 0) {
               magnitude = 1;  // Fallback if player is exactly at mining location
            }

            // Calculate drop tile far enough away (50+ units from mining location)
            Coord dropTile = var3.last_loc1.add((dirX / magnitude) * 50, (dirY / magnitude) * 50);

            var1.message("[Mine] Moving to drop location...", GameUI.MsgType.INFO);

            // Right-click on drop tile to exit mining mode and walk there
            var1.gui.map.wdgmsg("click", new Object[]{var4.sc, dropTile, 3, 0});
            var3.drop_location = dropTile;  // Store for later use
            this.exitedMiningMode = true;
            var2.botSleep(500);
            return null;  // Continue in same state
         }

         // After exiting mining mode, wait for player to reach drop tile
          if (this.exitedMiningMode && var1.gui.prog < 0) {
             if (var4.getattr(Moving.class) == null) {
                var1.message("[Mine] Dropping boulder...", GameUI.MsgType.INFO);

                // Refresh player reference to get current position
                Gob player = var1.gui.map.player();
                if (player == null) return null;

                var1.gui.map.wdgmsg("click", new Object[]{
                    player.sc,
                    player.rc,
                    3,
                    0,
                    0,
                    (int)player.id,
                    player.rc,
                    0,
                    -1
                });
                var2.botSleep(1000);

                this.minedStarted = false;
                this.exitedMiningMode = false;

                return BotState.initializeStack("mine", "droppingresults");
             }
             return null;
          }

         // Still waiting for mining to complete
         return null;

      } catch (Exception var9) {
         var1.message("[Mine] Digging error: " + var9.getMessage(), GameUI.MsgType.ERROR);
         var9.printStackTrace();
         return BotState.initializeStack("mine", "droppingresults");
      }
   }
}
