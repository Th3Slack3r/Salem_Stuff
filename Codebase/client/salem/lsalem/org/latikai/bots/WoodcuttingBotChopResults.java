package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "woodcutting",
   step = "chopresults"
)
class WoodcuttingBotChopResults extends BotState {
   boolean processing = false;

   public WoodcuttingBotChopResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (!this.processing) {
         if (ui.gui.prog > 0) {
            this.processing = true;
         }
      } else if (ui.gui.prog < 0) {
         ui.gui.act("destroy");
         return BotState.initializeStack("woodcutting", "getcursor");
      }

      return null;
   }
}
