package org.latikai.bots;

import haven.ChatUI;
import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.Resource;
import haven.UI; // Required for Resource.Loading exception
import java.util.List;
import java.util.Stack;

@BotAnnotation(
 bot = "clay",
 step = "results"
)
class ClayBotResults extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 boolean startedharvesting = false;

 public ClayBotResults() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
       
       // 2. MODIFIED: Replace ui.gui.inventoriesFull with botHelper.inventoriesFull
   if (botHelper.inventoriesFull(ui.gui, -7000)) {
    ui.message("[Clay] inventory is full.", GameUI.MsgType.INFO);
    ui.wdgmsg(ui.gui.map, "click", ui.gui.map.player().sc, ui.gui.map.player().rc, 1, 0);

    for (Gob.Overlay ol : ui.gui.map.player().ols) {
      try {
        Class<?> htfxClass = Class.forName("haven.res.lib.HomeTrackerFX");
        if (ol.spr.getClass().equals(htfxClass)) {
          Object htfx = htfxClass.cast(ol.spr);
          Coord c = (Coord)htfxClass.getField("c").get(htfx);
          ui.wdgmsg(ui.gui.map, "click", ui.gui.map.player().sc, c, 1, 0);
        }
      } catch (Exception e) {}
    }

    return BotState.initializeStack("clay", "end");
   } else {
          
          // 3. MODIFIED: Replace ui.gui.countInventory with botHelper.countInventory
          boolean starteddigging = false;
          try {
              starteddigging = botHelper.countInventory(ui.gui, "clay") > (Integer)bot.raw_data;
          } catch (Resource.Loading e) {
              ui.message("[ClayBot] Failed to count clay: " + e.getMessage(), GameUI.MsgType.ERROR);
              return null;
          }
          
          // 4. NOTE: ui.gui.prog is a non-static field we cannot wrap easily. 
          // Assuming 'prog' is a public field in GameUI, this line should compile.
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