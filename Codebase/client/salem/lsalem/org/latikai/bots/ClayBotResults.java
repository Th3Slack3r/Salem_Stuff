package org.latikai.bots;

import haven.ChatUI;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.res.lib.HomeTrackerFX;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "clay",
   step = "results"
)
class ClayBotResults extends BotState {
   boolean startedharvesting = false;

   public ClayBotResults() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (ui.gui.inventoriesFull(-7000)) {
         ui.message("[Clay] inventory is full.", GameUI.MsgType.INFO);
         ui.wdgmsg(ui.gui.map, "click", ui.gui.map.player().sc, ui.gui.map.player().rc, 1, 0);

         for (Gob.Overlay ol : ui.gui.map.player().ols) {
            if (ol.spr.getClass().equals(HomeTrackerFX.class)) {
               HomeTrackerFX htfx = (HomeTrackerFX)ol.spr;
               ui.wdgmsg(ui.gui.map, "click", ui.gui.map.player().sc, htfx.c, 1, 0);
            }
         }

         return BotState.initializeStack("clay", "end");
      } else {
         boolean starteddigging = ui.gui.countInventory("clay") > (Integer)bot.raw_data;
         if (ui.gui.prog < 0 && starteddigging) {
            bot.botSleep(50);
            return BotState.initializeStack("clay", "start");
         } else {
            if (!starteddigging) {
               List<ChatUI.Channel.Message> sysmsgs = ui.gui.syslog.msgs;
               synchronized (sysmsgs) {
                  ChatUI.Channel.Message lastmsg = sysmsgs.get(sysmsgs.size() - 1);
                  if (lastmsg.text().text.contains("too steep") && System.currentTimeMillis() - lastmsg.time < 300L) {
                     bot.botSleep(50);
                     return BotState.initializeStack("clay", "start");
                  }
               }
            }

            return null;
         }
      }
   }
}
