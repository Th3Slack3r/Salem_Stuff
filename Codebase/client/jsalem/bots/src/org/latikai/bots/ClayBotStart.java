package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.MCache;
import haven.UI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

@BotAnnotation(
 bot = "clay",
 step = "start"
)
class ClayBotStart extends BotState {
  
 private static final BotStuff botHelper = new BotStuff(); 
  
public ClayBotStart() {
}

@Override
public Stack<BotState> update(UI ui, Bot bot) {
    
    MCache m;
    Iterator<Entry<Coord, MCache.Grid>> i;
    String cursorName = "";
    Gob pl = ui.gui.map.player();
    double closest = -1.0;
    
    // We remove closestcohghts and closestcopeaks as they were never used
    
 try {
        // FIX: Get MCache instance, grids, and cursor name using helpers
   m = botHelper.getMCache(ui);
        Map<Coord, MCache.Grid> grids = botHelper.getMCacheGrids(m);
        i = grids.entrySet().iterator();
        cursorName = botHelper.getCursorName(ui);
        
        // --- Main Iteration Logic ---
   while (i.hasNext()) {
     Entry<Coord, MCache.Grid> e = i.next();
     MCache.Grid g = e.getValue();
     // Coord plloc is unused, replaced by plloc_absolute
     Coord plloc_absolute = pl.rc.div(11);

     for (int tileidx = 0; tileidx < g.tiles.length; tileidx++) {
                
                // FIX: Use helper for Tileset resource name
       String resName = botHelper.getTilesetResourceName(g.tiles[tileidx], m);
                
       if (resName.contains("clay")) {
         Coord tc = new Coord(tileidx % MCache.cmaps.x, tileidx / MCache.cmaps.x);
         Coord tc_absolute = tc.add(g.ul);
         double thisdist = tc_absolute.dist(plloc_absolute);
         if (thisdist < closest || closest < 0.0) {
          int[] cohghts;
          int[] copeaks;
          try {
                            // FIX: Use helper for MCache.getz (4 calls)
             int[] tmp = new int[]{
              botHelper.getMCacheZ(m, tc_absolute.add(0, 0)), 
                                botHelper.getMCacheZ(m, tc_absolute.add(0, 1)), 
                                botHelper.getMCacheZ(m, tc_absolute.add(1, 0)), 
                                botHelper.getMCacheZ(m, tc_absolute.add(1, 1))
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
            System.out.println("Loading issue for the bot (MCache.getz)!");
            continue;
          }
                        catch (Exception e_reflect) {
                            System.out.println("Reflection error in tile height check: " + e_reflect.getMessage());
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
            closest = thisdist;
            // The original code re-calculates m.getz here, which is redundant 
                         // but we only set raw_data here.
            bot.raw_data = tc_absolute.mul(11.0);
           }
         }
       }
      }
    }
        // --- End Main Iteration Logic ---
        
   } catch (Exception e) {
        ui.message("[Clay] Fatal Reflection Error during map access: " + e.getMessage(), GameUI.MsgType.ERROR);
        return BotState.initializeStack("clay", "end");
    }

 if (closest >= 0.0) {
    // FIX: Use helper for cursor name check
    if (!cursorName.contains("dig")) {
       
      // FIX: Replaced ui.gui.act("dig") with botHelper.act(ui.gui, "dig")
      botHelper.act(ui.gui, "dig");
    }

    return BotState.initializeStack("clay", "awaiting_cursor");
 } else {
  ui.message("[Clay] no clay tiles around!", GameUI.MsgType.INFO);
  return BotState.initializeStack("clay", "end");
 }
}

protected int[] getsortedinds(int[] heights) {
// Original body - no internal access, so it is legal.
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
      
      // Wrap illegal m.getz calls in try-catch
      try {
    int[] heights = new int[]{
      // FIX: Use helper for m.getz(Coord) (5 calls)
      botHelper.getMCacheZ(m, tc.add((int)(offx * 3.0 + 0.5), (int)(offy * 3.0 + 0.5))),
      botHelper.getMCacheZ(m, tc.add((int)(offx + 0.5), (int)(offy * 3.0 + 0.5))),
      botHelper.getMCacheZ(m, tc.add((int)(-offx + 0.5), (int)(offy * 3.0 + 0.5))),
      botHelper.getMCacheZ(m, tc.add((int)(offx * 3.0 + 0.5), (int)(-offy + 0.5))),
      botHelper.getMCacheZ(m, tc.add((int)(offx * 3.0 + 0.5), (int)(offy + 0.5)))
    };
          int besti = 0;

    for (int i = 1; i < heights.length; i++) {
      if (heights[i] > heights[besti]) {
        besti = i;
      }
    }

    return heights[besti];
      } catch (Exception e) {
          // If reflection or loading fails here, return a safe default
          return 0; 
      }
 }
}