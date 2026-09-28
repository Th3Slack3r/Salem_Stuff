package org.latikai.bots;

import haven.Coord;
import haven.FlatnessTool;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.ResDrawable;
import haven.UI;
import haven.WItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "humus",
   step = "start"
)
class HumusBotStart extends BotState {
   public HumusBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Gob> fields = this.getFields(ui);
      Collections.sort(fields, new Bot.GridLocation.OfField());
      bot.raw_data = new HumusState(fields);
      GItem gi = ui.gui.maininv.getFirst("humus");
      WItem wi = ui.gui.maininv.wmap.get(gi);
      if (ui.gui.inHand("")) {
         ui.gui.maininv.drop(Coord.z, wi.c);
      } else {
         gi.wdgmsg("take", new Object[]{Coord.z});
      }

      return BotState.initializeStack("humus", "put");
   }

   protected List<Gob> getFields(UI ui) {
      if (!FlatnessTool.hasInstance(ui)) {
         ui.message("[Humus] No fields selected!", GameUI.MsgType.INFO);
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
            if (isfield && isselected) {
               fields.add(g);
            }
         }

         int nrfields = fields.size();
         ui.message("[Humus] Detected " + nrfields + " fields", GameUI.MsgType.INFO);
         return fields;
      }
   }
}
