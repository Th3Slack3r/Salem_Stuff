package org.latikai.bots;

import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "loglooter",
   step = "end"
)
public class LogLooterBotEnd extends BotState {
   public LogLooterBotEnd() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      return null;
   }
}