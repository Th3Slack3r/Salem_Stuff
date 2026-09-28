package org.latikai.bots;

import haven.Coord;
import haven.GItem;
import haven.Moving;
import haven.UI;
import haven.WItem;
import java.util.Stack;

@BotAnnotation(
   bot = "preparepots",
   step = "water_results"
)
class PreparePotsWaterResults extends BotState {
   boolean walking = false;

   public PreparePotsWaterResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (!this.walking) {
         if (ui.gui.map.player().getattr(Moving.class) != null) {
            this.walking = true;
         }
      } else if (ui.gui.map.player().getattr(Moving.class) == null) {
         if (!bot.gotWater(ui)) {
            GItem gi = ui.gui.maininv.getFirst("bucket-water");
            WItem w = ui.gui.maininv.wmap.get(gi);
            ui.gui.maininv.drop(Coord.z, w.c);
         }

         return BotState.initializeStack("preparepots", "water_wait");
      }

      return null;
   }
}
