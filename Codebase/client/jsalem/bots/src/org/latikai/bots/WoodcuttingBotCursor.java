package org.latikai.bots;

import haven.Coord;
import haven.FlatnessTool;
import haven.GameUI;
import haven.Gob;
import haven.ResDrawable;
import haven.UI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "woodcutting",
   step = "getcursor"
)
class WoodcuttingBotCursor extends BotState {

   private static final BotStuff botHelper = new BotStuff();

   public WoodcuttingBotCursor() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      // Use botHelper for cursor name check (like ChippingBot pattern)
      String cursorName = botHelper.getCursorName(ui);
      if (cursorName != null && cursorName.contains("kreuz")) {
         List<Gob> stumps = this.getStumps(ui);
         if (stumps.size() > 0) {
            Gob stump = stumps.get(0);
            ui.wdgmsg(ui.gui.map, "click", stump.sc, stump.rc, 1, 0, 0, (int)stump.id, stump.rc, 0, -1);
            return BotState.initializeStack("woodcutting", "cutresults");
         } else {
            return BotState.initializeStack("woodcutting", "start");
         }
      } else {
         return null;
      }
   }

   protected List<Gob> getStumps(UI ui) {
      if (!FlatnessTool.hasInstance(ui)) {
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
         List<Gob> trees = new ArrayList<>();

         for (Gob g : gobs) {
            ResDrawable rd = g.getattr(ResDrawable.class);
            boolean istree = rd != null && rd.res.get().name.contains("stump");
            boolean isselected = g.rc.div(11).isect(c1, c2.add(c1.inv()));
            if (istree && isselected) {
               trees.add(g);
            }
         }

         int nrtrees = trees.size();
         ui.message("[Woodcutting] Detected " + nrtrees + " stumps", GameUI.MsgType.INFO);
         return trees;
      }
   }
}
