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
 bot = "drossing",
 step = "start"
)
class DrossingBotStart extends BotState {
    
    // 1. ADDED: Instance of the helper class
    private static final BotStuff botHelper = new BotStuff(); 
    
 public DrossingBotStart() {
 }

 @Override
 public Stack<BotState> update(UI ui, Bot bot) {
        
        List<Gob> fields = this.getFields(ui);
        
        // Handle case where getFields returns null (e.g., no fields selected)
        if (fields == null) {
            return BotState.initializeStack("drossing", "end");
        }
        
     Collections.sort(fields, new Bot.GridLocation.OfField());
     bot.raw_data = new DrossingState(fields);
        
        GItem gi = null;
        WItem wi = null;

        try {
            // 2. MODIFIED: Replaced ui.gui.maininv.getFirst("dross")
            gi = botHelper.getFirstMainInvItem(ui.gui, "dross");
            
            // 3. MODIFIED: Replaced ui.gui.maininv.wmap.get(gi)
            wi = botHelper.getMainInvWItem(ui.gui, gi);
            
            if (gi == null || wi == null) {
                ui.message("[Drossing] Could not find Dross in inventory!", GameUI.MsgType.ERROR);
                return BotState.initializeStack("drossing", "end");
            }

            // 4. MODIFIED: Replaced ui.gui.inHand("")
            if (botHelper.inHand(ui.gui, "")) {
                // 5. MODIFIED: Replaced ui.gui.maininv.drop(...)
         botHelper.dropMainInvItem(ui.gui, Coord.z, wi.c);
       } else {
         gi.wdgmsg("take", new Object[]{Coord.z});
       }
            
        } catch (Exception e) {
            ui.message("[Drossing] Reflection Error in start step: " + e.getMessage(), GameUI.MsgType.ERROR);
            return BotState.initializeStack("drossing", "end");
        }
        

   return BotState.initializeStack("drossing", "put");
 }

 protected List<Gob> getFields(UI ui) {
   if (!FlatnessTool.hasInstance(ui)) {
    ui.message("[Drossing] No fields selected!", GameUI.MsgType.INFO);
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
    ui.message("[Drossing] Detected " + nrfields + " fields", GameUI.MsgType.INFO);
    return fields;
   }
 }
}