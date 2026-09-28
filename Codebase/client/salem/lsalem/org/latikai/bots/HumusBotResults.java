package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "humus",
   step = "results"
)
class HumusBotResults extends BotState {
   boolean walking = false;

   public HumusBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return ui.gui.countInventory("") < ((HumusState)bot.raw_data).inventory_count ? BotState.initializeStack("humus", "put") : null;
   }
}
