package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "rabbithunter",
   step = "end"
)
public class RabbitHunterEnd extends BotState {
   public RabbitHunterEnd() {
      System.out.println("[DEBUG] RabbitHunterEnd constructor called");
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}
