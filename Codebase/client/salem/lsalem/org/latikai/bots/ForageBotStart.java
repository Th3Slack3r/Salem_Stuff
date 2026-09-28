package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.Loading;
import haven.ResDrawable;
import haven.Resource;
import haven.UI;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "forage",
   step = "start"
)
class ForageBotStart extends BotState {
   public ForageBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Gob> objects = Bot.getGobs(ui, "terobjs/herbs", null, false);
      objects.removeIf(g -> {
         Coord goloc = g.rc;
         int tile = ui.sess.glob.map.gettile(goloc.div(11.0));
         Resource tilesetr = ui.sess.glob.map.tilesetr(tile);
         String tilesetname = tilesetr.basename();
         ResDrawable rd = null;
         String nm = "";

         try {
            rd = g.getattr(ResDrawable.class);
            if (rd != null) {
               nm = rd.res.get().name;
            }
         } catch (Loading var11) {
         }

         boolean matches_locrestriction = bot.arguments.length > 0 ? tilesetname.contains(bot.arguments[0]) : true;
         boolean matches_typrestriction = bot.arguments.length > 1 ? tilesetname.contains(bot.arguments[1]) : true;
         return !matches_locrestriction || !matches_typrestriction;
      });
      objects.sort(new Bot.PlayerCloseness.ToGob(ui));
      bot.raw_data = ui.gui.countInventory("");
      if (objects != null && objects.size() > 0) {
         Gob object = objects.get(0);
         ui.wdgmsg(ui.gui.map, "click", object.sc, object.rc, 3, 0, 0, (int)object.id, object.rc, 0, -1);
         bot.botSleep(50);
         return BotState.initializeStack("forage", "flowermenu");
      } else {
         ui.message("[Forage] no forageables found.", GameUI.MsgType.INFO);
         return BotState.initializeStack("forage", "end");
      }
   }
}
