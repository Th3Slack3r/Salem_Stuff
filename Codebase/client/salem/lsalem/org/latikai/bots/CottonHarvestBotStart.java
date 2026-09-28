package org.latikai.bots;

import haven.Coord;
import haven.FlatnessTool;
import haven.GameUI;
import haven.Gob;
import haven.Rendered;
import haven.ResDrawable;
import haven.StaticSprite;
import haven.UI;
import haven.res.lib.plants.GrowingPlant;
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
            ui.wdgmsg(ui.gui.map, "click", field.sc, field.rc, 3, 0, 0, (int)field.id, field.rc, 0, -1);
            bot.botSleep(50);
            return BotState.initializeStack("cottonharvest", "flowermenu");
         }

         ui.message("[CottonHarvest] No cotton fields found. Exiting.", GameUI.MsgType.INFO);
      }

      return BotState.initializeStack("cottonharvest", "end");
   }

   protected List<Gob> getFields(UI ui) {
      if (!FlatnessTool.hasInstance(ui)) {
         ui.message("[CottonHarvest] No fields selected!", GameUI.MsgType.INFO);
         return null;
      } else {
         Coord c1 = FlatnessTool.instance(ui).c1;
         Coord c2 = FlatnessTool.instance(ui).c2;
         if (c2.x < c1.x) {
            Coord t = c1;
            c1 = c2;
            c2 = t;
         }

         Collection<Gob> gobs = ui.sess.glob.oc.getGobs();
         List<Gob> fields = new ArrayList<>();

         for (Gob g : gobs) {
            ResDrawable rd = g.getattr(ResDrawable.class);
            boolean isfield = rd != null && rd.res.get().name.contains("gfx/terobjs/field");
            boolean isselected = g.rc.div(11).isect(c1, c2.add(c1.inv()));
            boolean isempty = true;
            if (isfield && isselected && rd.spr != null && ((StaticSprite)rd.spr).parts != null) {
               for (Rendered r : ((StaticSprite)rd.spr).parts) {
                  if (r.getClass().equals(GrowingPlant.class)) {
                     isempty = isempty || !((GrowingPlant)r).res.name.contains("terobjs/plants/");
                  }
               }
            }

            if (isfield && isselected && isempty) {
               fields.add(g);
            }
         }

         int nrfields = fields.size();
         ui.message("[CottonHarvest] Detected " + nrfields + " fields", GameUI.MsgType.INFO);
         return fields;
      }
   }
}
