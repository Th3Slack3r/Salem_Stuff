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
   step = "start"
)
class WoodcuttingBotStart extends BotState {
   public WoodcuttingBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      List<Gob> trees = this.getTrees(ui);
      if (trees != null && trees.size() > 0) {
         Gob tree = trees.get(0);
         ui.wdgmsg(ui.gui.map, "click", tree.sc, tree.rc, 3, 0, 0, (int)tree.id, tree.rc, 0, -1);
         return ui.root.cursor.name.contains("kreuz") ? null : BotState.initializeStack("woodcutting", "flowermenu");
      } else {
         return BotState.initializeStack("woodcutting", "end");
      }
   }

   protected List<Gob> getTrees(UI ui) {
      if (!FlatnessTool.hasInstance(ui)) {
         ui.message("[Woodcutting] No trees selected!", GameUI.MsgType.INFO);
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
         List<Gob> trees = new ArrayList<>();
         synchronized (ui.sess.glob.oc) {
            for (Gob g : gobs) {
               ResDrawable rd = g.getattr(ResDrawable.class);
               boolean istree = rd != null && rd.res.get().name.contains("tree");
               boolean isselected = g.rc.div(11).isect(c1, c2.add(c1.inv()));
               if (istree && isselected) {
                  trees.add(g);
               }
            }
         }

         int nrtrees = trees.size();
         ui.message("[Woodcutting] Detected " + nrtrees + " trees", GameUI.MsgType.INFO);
         return trees;
      }
   }
}
