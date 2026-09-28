package org.latikai.bots;

import haven.GameUI;
import haven.Gob;
import haven.UI;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "chipping",
   step = "start"
)
class ChippingBotStart extends BotState {
   public ChippingBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Gob> fields = Bot.getGobs(ui, "bumling", "Chipping");
      if (fields != null) {
         bot.raw_data = fields;
         fields.sort(new Bot.PlayerCloseness.ToGob(ui));
         if (!fields.isEmpty()) {
            Gob field = fields.remove(0);
            ui.wdgmsg(ui.gui.map, "click", field.sc, field.rc, 3, 0, 0, (int)field.id, field.rc, 0, -1);
            bot.botSleep(50);
            return BotState.initializeStack("chipping", "flowermenu");
         }

         ui.message("[Chipping] No boulders found. Exiting.", GameUI.MsgType.INFO);
      }

      return BotState.initializeStack("chipping", "end");
   }
}
