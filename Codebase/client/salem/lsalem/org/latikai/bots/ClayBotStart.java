package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.MCache;
import haven.Resource;
import haven.UI;
import java.util.Iterator;
import java.util.Stack;
import java.util.Map.Entry;

@BotAnnotation(
   bot = "clay",
   step = "start"
)
class ClayBotStart extends BotState {
   public ClayBotStart() {
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      MCache m = ui.sess.glob.map;
      Iterator<Entry<Coord, MCache.Grid>> i = m.grids.entrySet().iterator();
      Gob pl = ui.gui.map.player();
      double closest = -1.0;
      int[] closestcohghts = null;
      int[] closestcopeaks = null;

      while (i.hasNext()) {
         Entry<Coord, MCache.Grid> e = i.next();
         MCache.Grid g = e.getValue();
         Coord plloc = pl.rc.sub(g.ul.mul(11.0)).div(11.0);
         Coord plloc_absolute = pl.rc.div(11.0);

         for (int tileidx = 0; tileidx < g.tiles.length; tileidx++) {
            Resource.Tileset set = m.tileset(g.tiles[tileidx]);
            if (set.getres().name.contains("clay")) {
               Coord tc = new Coord(tileidx % MCache.cmaps.x, tileidx / MCache.cmaps.x);
               Coord tc_absolute = tc.add(g.ul);
               double thisdist = tc_absolute.dist(plloc_absolute);
               if (thisdist < closest || closest < 0.0) {
                  int[] cohghts;
                  int[] copeaks;
                  try {
                     int[] tmp = new int[]{
                        m.getz(tc_absolute.add(0, 0)), m.getz(tc_absolute.add(0, 1)), m.getz(tc_absolute.add(1, 0)), m.getz(tc_absolute.add(1, 1))
                     };
                     cohghts = tmp;
                     int[] tmp2 = new int[]{
                        this.gethighest(m, tc_absolute, new Coord(0, 0)),
                        this.gethighest(m, tc_absolute, new Coord(0, 1)),
                        this.gethighest(m, tc_absolute, new Coord(1, 0)),
                        this.gethighest(m, tc_absolute, new Coord(1, 1))
                     };
                     copeaks = tmp2;
                  } catch (MCache.LoadingMap var25) {
                     System.out.println("Loading issue for the bot!");
                     continue;
                  }

                  int maxdepthdiff = 10;
                  boolean diggable = true;

                  for (int j = 0; j < 4; j++) {
                     if (cohghts[j] < copeaks[j] - maxdepthdiff) {
                        diggable = false;
                     }
                  }

                  if (diggable) {
                     closest = tc_absolute.dist(plloc_absolute);
                     int[] tmp = new int[]{
                        m.getz(tc_absolute.add(0, 0)), m.getz(tc_absolute.add(0, 1)), m.getz(tc_absolute.add(1, 0)), m.getz(tc_absolute.add(1, 1))
                     };
                     bot.raw_data = tc_absolute.mul(11.0);
                  }
               }
            }
         }
      }

      if (closest >= 0.0) {
         if (!ui.root.cursor.name.contains("dig")) {
            ui.gui.act("dig");
         }

         return BotState.initializeStack("clay", "awaiting_cursor");
      } else {
         ui.message("[Clay] no clay tiles around!", GameUI.MsgType.INFO);
         return BotState.initializeStack("clay", "end");
      }
   }

   protected int[] getsortedinds(int[] heights) {
      int[] inds = new int[4];
      inds[0] = 0;
      if (heights[1] > heights[inds[0]]) {
         inds[1] = inds[0];
         inds[0] = 1;
      } else {
         inds[1] = 1;
      }

      if (heights[2] > heights[inds[0]]) {
         inds[2] = inds[1];
         inds[1] = inds[0];
         inds[0] = 2;
      } else if (heights[2] > heights[inds[1]]) {
         inds[2] = inds[1];
         inds[1] = 2;
      } else {
         inds[2] = 2;
      }

      if (heights[3] > heights[inds[0]]) {
         inds[3] = inds[2];
         inds[2] = inds[1];
         inds[1] = inds[0];
         inds[0] = 3;
      } else if (heights[3] > heights[inds[1]]) {
         inds[3] = inds[2];
         inds[2] = inds[1];
         inds[1] = 3;
      } else if (heights[3] > heights[inds[2]]) {
         inds[3] = inds[2];
         inds[2] = 3;
      } else {
         inds[3] = 3;
      }

      return inds;
   }

   protected int gethighest(MCache m, Coord tc, Coord offset) {
      double offx = offset.x - 0.5;
      double offy = offset.y - 0.5;
      int[] heights = new int[]{
         m.getz(tc.add((int)(offx * 3.0 + 0.5), (int)(offy * 3.0 + 0.5))),
         m.getz(tc.add((int)(offx + 0.5), (int)(offy * 3.0 + 0.5))),
         m.getz(tc.add((int)(-offx + 0.5), (int)(offy * 3.0 + 0.5))),
         m.getz(tc.add((int)(offx * 3.0 + 0.5), (int)(-offy + 0.5))),
         m.getz(tc.add((int)(offx * 3.0 + 0.5), (int)(offy + 0.5)))
      };
      int besti = 0;

      for (int i = 1; i < heights.length; i++) {
         if (heights[i] > heights[besti]) {
            besti = i;
         }
      }

      return heights[besti];
   }
}
