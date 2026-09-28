package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "mine",
   step = "start"
)
class MineBotStart extends BotState {

   public MineBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      // Scan for mineable tiles
      List tiles;
      try {
         tiles = Bot.getTiles(ui, "mine-", "Mine", true);
      } catch (Exception e) {
         ui.message("[Mine] Error scanning tiles: " + e.getMessage(), GameUI.MsgType.ERROR);
         return BotState.initializeStack("mine", "end");
      }

      if (tiles == null || tiles.isEmpty()) {
         ui.message("[Mine] No mineable tiles found. Done!", GameUI.MsgType.INFO);
         return BotState.initializeStack("mine", "end");
      }

      // Validate tiles
      java.util.List<Coord> validTiles = new java.util.ArrayList<>();
      for (Object obj : tiles) {
         Coord tile = (Coord)obj;
         if (tile != null && !tile.equals(Coord.z)) {
            validTiles.add(tile);
         }
      }

      if (validTiles.isEmpty()) {
         ui.message("[Mine] No valid tiles found. Done!", GameUI.MsgType.INFO);
         return BotState.initializeStack("mine", "end");
      }

      final Gob player = ui.gui.map.player();
      if (player == null) {
         return null;
      }

      // Sort by distance (closest first)
      validTiles.sort(new Comparator<Coord>() {
         public int compare(Coord t1, Coord t2) {
            double dist1 = player.rc.dist(t1);
            double dist2 = player.rc.dist(t2);
            return Double.compare(dist1, dist2);
         }
      });

      // Get closest tile
      Coord closestTile = validTiles.get(0);
      ui.message("[Mine] Found " + validTiles.size() + " tiles. Mining closest...", GameUI.MsgType.INFO);

      // Store state
      MineState state = new MineState();
      state.last_loc1 = closestTile;
      state.last_loc2 = closestTile;
      bot.raw_data = state;

      // Activate mining mode and go to cursor wait
      ui.gui.act("mine");
      bot.botSleep(500);
      return BotState.initializeStack("mine", "awaiting_cursor");
   }
}
