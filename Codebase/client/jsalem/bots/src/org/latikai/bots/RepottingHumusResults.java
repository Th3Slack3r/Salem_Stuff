package org.latikai.bots;

import haven.Moving;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "repotting",
   step = "humus_results"
)
class RepottingHumusResults extends BotState {
   boolean walking = false;

   public RepottingHumusResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (!this.walking) {
         if (ui.gui.map.player().getattr(Moving.class) != null) {
            this.walking = true;
         }
      } else if (ui.gui.map.player().getattr(Moving.class) == null) {
         return BotState.initializeStack("repotting", "humus_wait");
      }

      return null;
   }
}
