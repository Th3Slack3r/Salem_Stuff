package org.latikai.bots;

import haven.Config;
import haven.Coord;
import haven.GItem;
import haven.GameUI;
import haven.Loading;
import haven.UI;
import java.util.Stack;

@BotAnnotation(
   bot = "redrose",
   step = "wait_dehydrated"
)
class RedRoseBotWaitDehydrated extends BotState {
   public RedRoseBotWaitDehydrated() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      int oldcount = ((RedRoseState)bot.raw_data).inventory_count;
      if (ui.gui.countInventory("") > oldcount) {
         if (Config.headless) {
            System.out.println("\t[RedRose] Got dehydrated rose");
         }

         try {
            GItem gi = ui.gui.maininv.getFirst("redrose");
            if (gi != null) {
               ((RedRoseState)bot.raw_data).inventory_count = ui.gui.countInventory("");
               gi.wdgmsg("transfer", new Object[]{Coord.z});
               bot.botSleep(200);
               return BotState.initializeStack("redrose", "wait_fresh");
            }

            ui.message("[RedRose] Aborting! Not enough fresh roses.", GameUI.MsgType.ERROR);
            return BotState.initializeStack("redrose", "end");
         } catch (Loading var5) {
         }
      }

      return null;
   }
}
