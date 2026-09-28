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
 bot = "chopping",
 step = "start"
)
class ChoppingBotStart extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public ChoppingBotStart() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
   List<Gob> fields = this.getFields(ui);
   Collections.sort(fields, new Bot.GridLocation.OfField());
   bot.raw_data = new ChoppingState(fields);
   GItem gi = ui.gui.maininv.getFirst("woodchops");
   WItem wi = ui.gui.maininv.wmap.get(gi);
      
      // 2. MODIFIED: Replaced ui.gui.inHand("") with botHelper.inHand(ui.gui, "")
   if (botHelper.inHand(ui.gui, "")) {
    ui.gui.maininv.drop(Coord.z, wi.c);
   } else {
    gi.wdgmsg("take", new Object[]{Coord.z});
   }

   return BotState.initializeStack("chopping", "put");
 }

 protected List<Gob> getFields(UI ui) {
   if (!FlatnessTool.hasInstance(ui)) {
    ui.message("[Chopping] No fields selected!", GameUI.MsgType.INFO);
    return null;
   } else {
    Coord c1 = FlatnessTool.instance(ui.gui).c1;
    Coord c2 = FlatnessTool.instance(ui.gui).c2;
    if (c2.x < c1.x) {
      Coord t = c1;
      c1 = c2;
      c2 = t;
    }

    java.util.Collection<Gob> gobs = new java.util.ArrayList<>(); for(Gob _g : ui.sess.glob.oc) gobs.add(_g);
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
    ui.message("[Chopping] Detected " + nrfields + " fields", GameUI.MsgType.INFO);
    return fields;
   }
 }
}