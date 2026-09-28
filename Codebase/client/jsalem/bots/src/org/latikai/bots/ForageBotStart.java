package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
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
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public ForageBotStart() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   List<Gob> objects = Bot.getGobs(ui, "terobjs/herbs", null, false);
   objects.removeIf(g -> {
    Coord goloc = g.rc;
    int tile = ui.sess.glob.map.gettile(goloc.div(11));
    Resource tilesetr = ui.sess.glob.map.tilesetr(tile);
    String tilesetname = tilesetr.basename();
    ResDrawable rd = null;
    String nm = "";

// ForageBotStart.java - Inside the update method (~line 42)

    try {
      rd = g.getattr(ResDrawable.class);
      if (rd != null) {
       nm = rd.res.get().name;
      }
    } catch (haven.Resource.Loading var11) { // MODIFIED LINE
    }

    boolean matches_locrestriction = bot.arguments.length > 0 ? tilesetname.contains(bot.arguments[0]) : true;
    boolean matches_typrestriction = bot.arguments.length > 1 ? tilesetname.contains(bot.arguments[1]) : true;
    return !matches_locrestriction || !matches_typrestriction;
   });
   objects.sort(new Bot.PlayerCloseness.ToGob(ui));

       // 2. MODIFIED: Replaced ui.gui.countInventory("") with botHelper method and try-catch
       try {
           bot.raw_data = botHelper.countInventory(ui.gui, "");
       } catch (Resource.Loading e) {
           ui.message("[Forage] Failed to count inventory on start: " + e.getMessage(), GameUI.MsgType.ERROR);
           bot.raw_data = -1; // Use a failure value
       }

   if (objects != null && objects.size() > 0) {
    Gob object = objects.get(0);
    ui.wdgmsg(ui.gui.map, "click", object.sc, object.rc, 3, 0, 0, (int)object.id, object.rc, 0, -1);
    bot.botSleep(50);
    return BotState.initializeStack("forage", "flowermenu");
   } else {
    ui.message("[Forage] no forageables found. Exiting.", GameUI.MsgType.INFO);
    return BotState.initializeStack("forage", "end");
   }
 }
}