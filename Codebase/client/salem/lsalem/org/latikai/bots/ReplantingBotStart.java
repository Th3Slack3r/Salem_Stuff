package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "replanting",
   step = "start"
)
class ReplantingBotStart extends BotState {
   public ReplantingBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      Stack<BotState> stack = BotState.initializeStack("replanting", "finishing");
      stack.push(BotState.getBotState("chopping", "start"));
      stack.push(BotState.getBotState("seeding", "start"));
      stack.push(BotState.getBotState("humus", "start"));
      return stack;
   }
}
