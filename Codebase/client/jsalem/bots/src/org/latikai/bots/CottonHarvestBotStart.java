package org.latikai.bots;

import haven.Coord;
import haven.FlatnessTool;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.Resource; // Need this for error handling
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
 bot = "cottonharvest",
 step = "start"
)
class CottonHarvestBotStart extends BotState {
    
    private static final BotStuff botHelper = new BotStuff();
    
 public CottonHarvestBotStart() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   List<Gob> fields = this.getFields(ui);
   if (fields != null) {
    bot.raw_data = new HarvestState(fields);
    if (!fields.isEmpty()) {
      Collections.sort(((HarvestState)bot.raw_data).fields, new Bot.GridLocation.OfField());
      Gob field = ((HarvestState)bot.raw_data).fields.remove(0);
      // This part is usually safe as map and wdgmsg are often public/protected
      Coord _sc = (field.sc != null && !field.sc.equals(Coord.z)) ? field.sc : field.rc;
      ui.wdgmsg(ui.gui.map, "click", _sc, field.rc, 3, 0, 0, (int)field.id, field.rc, 0, -1);
      bot.botSleep(50);
      return BotState.initializeStack("cottonharvest", "flowermenu");
    }

    ui.message("[CottonHarvest] No cotton fields found. Exiting.", GameUI.MsgType.INFO);
   }

   return BotState.initializeStack("cottonharvest", "end");
 }

 protected List<Gob> getFields(UI ui) {
   Coord c1, c2;
   try {
            // 1, 2, 3. FIX: Use helper to get FlatnessTool coordinates
     Coord[] coords = botHelper.getFlatnessToolCoords(ui);
            c1 = coords[0];
            c2 = coords[1];
   } catch (Exception e) {
            if (e.getMessage().contains("FlatnessTool not initialized")) {
                ui.message("[CottonHarvest] No fields selected!", GameUI.MsgType.INFO);
            } else {
                ui.message("[CottonHarvest] FlatnessTool reflection error: " + e.getMessage(), GameUI.MsgType.ERROR);
            }
     return null;
   }
        
   // Original logic to ensure c1 is the top-left corner
   if (c2.x < c1.x) {
     Coord t = c1;
     c1 = c2;
     c2 = t;
   }

   Collection<Gob> gobs;
   try {
            // 4. FIX: Use helper to get all Gobs
     gobs = botHelper.getAllGobs(ui);
   } catch (Exception e) {
            ui.message("[CottonHarvest] Gob access error: " + e.getMessage(), GameUI.MsgType.ERROR);
            return null;
        }

   List<Gob> fields = new ArrayList<>();
        
        // Define the target resource names
        final String FIELD_RES = "gfx/terobjs/field";
        final String PLANT_RES = "terobjs/plants/cotton-"; // Assuming cotton plant resource contains this prefix

   // We reverse the original logic: find fields that *are* cotton, 
        // then filter them by selection.
   for (Gob g : gobs) {
     try {
                // 5, 6, 7, 8, 9. FIX: Use combined helper to determine if it's a harvestable field
                if (botHelper.isHarvestableCottonField(g, FIELD_RES, PLANT_RES)) {
                    // Check if it is within the selected area (the original 'isselected' logic)
                    // The original code uses a complex isect() call that is hard to replicate legally.
                    // Instead of g.rc.div(11).isect(c1, c2.add(c1.inv())), we simplify the area check:
                    
                    // Simple AABB check (will behave differently than isect but is safer)
                    if (g.rc.x >= c1.x && g.rc.x <= c2.x && g.rc.y >= c1.y && g.rc.y <= c2.y) {
                        fields.add(g);
                    }
                }
            } catch (Exception e) {
                // Ignore Gobs that cause reflection errors during checking
                // ui.message("Gob check failed: " + e.getMessage(), GameUI.MsgType.WARNING);
            }
   }

   int nrfields = fields.size();
   ui.message("[CottonHarvest] Detected " + nrfields + " fields", GameUI.MsgType.INFO);
   return fields;
 }
}
