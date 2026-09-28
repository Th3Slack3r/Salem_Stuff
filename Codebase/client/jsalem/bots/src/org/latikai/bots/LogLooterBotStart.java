package org.latikai.bots;

import haven.Coord;
import haven.GameUI;
import haven.Gob;
import haven.Loading;
import haven.MCache;
import haven.ResDrawable;
import haven.Resource;
import haven.UI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@BotAnnotation(
   bot = "loglooter",
   step = "start"
)
public class LogLooterBotStart extends BotState {

   private static final int TILESZ = 11;
   private static final int REACH = 20;
   private static final BotStuff botHelper = new BotStuff();
   private long lastAction = 0;
   private Gob target = null;
   private static boolean everAnnounced = false;
   private List<Coord> path = null;
   private Coord pathDest = null;
   private int pathIdx = 0;
   private long lastPathCalc = 0;
   private Coord lastPos = null;
   private long lastPosTime = 0;
   private int stuckCount = 0;
   private int totalStuckForTarget = 0;

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      try {
         if (ui == null || ui.gui == null || ui.gui.map == null || ui.gui.map.player() == null) return null;

         if (!everAnnounced) {
            ui.message("[LogLooter] Bot started.", GameUI.MsgType.INFO);
            everAnnounced = true;
         }

         // Check bile levels
         int[] biles = getBiles(ui);
         if (biles == null) return null;

         Gob player = ui.gui.map.player();

         // Blood [0], Phlegm [1], Yellow Bile [2] — stop if any below 10%
         for (int i = 0; i < 3; i++) {
            if (biles[i] < biles[4] * 10 / 100) {
               String name = new String[]{"Blood", "Phlegm", "Yellow Bile"}[i];
               ui.message("[LogLooter] " + name + " low (" + biles[i] + "/" + biles[4] + "), stopping.", GameUI.MsgType.INFO);
               return BotState.initializeStack("loglooter", "end");
            }
         }

         // Black Bile [3] — check thresholds
         int blackPct = biles[3] * 100 / Math.max(biles[4], 1);
         if (blackPct < 10) {
            // Black bile critical — don't study, just loot and eat
            Gob eatTarget = LogLooterBotEat.findEatTarget(ui, player);
            if (eatTarget != null) {
               return BotState.initializeStack("loglooter", "eat");
            }
         } else if (blackPct < 25) {
            // Black bile low — eat beetles/chestnuts instead of studying
            Gob eatTarget = LogLooterBotEat.findEatTarget(ui, player);
            if (eatTarget != null) {
               return BotState.initializeStack("loglooter", "eat");
            }
         } else {
            // Biles healthy — study as normal
            Gob studyTarget = LogLooterBotStudy.findStudyTarget(ui, player);
            if (studyTarget != null) {
               return BotState.initializeStack("loglooter", "study");
            }
         }

         // Check for eat items nearby (grub etc)
         Gob eatTarget2 = LogLooterBotEat.findEatTarget(ui, player);
         if (eatTarget2 != null) {
            return BotState.initializeStack("loglooter", "eat");
         }

         // Pick up any loose valuable items on the ground near us while walking.
         Gob ground = findGroundItem(ui, player);
         if (ground != null) {
            ui.wdgmsg(ui.gui.map, "click", ground.sc, ground.rc, 3, 0, 0, (int)ground.id, ground.rc, 0, -1);
            bot.botSleep(200);
            return null;
         }

         if (target == null) {
            target = findTarget(ui, player, bot);
            if (target == null) {
               ui.message("[LogLooter] No old logs/stumps found, stopping.", GameUI.MsgType.INFO);
               return BotState.initializeStack("loglooter", "end");
            } else {
               String nm = resName(target);
               ui.message("[LogLooter] Targeting: " + (nm == null ? "?" : nm) + " at " + (int)player.rc.dist(target.rc), GameUI.MsgType.INFO);
               // reset path/stuck for new target
               path = null;
               pathDest = null;
               pathIdx = 0;
               totalStuckForTarget = 0;
               stuckCount = 0;
            }
         }

         double dist = player.rc.dist(target.rc);
         if (dist <= REACH) {
            // In range: right-click to open the flower menu.
            markDone(bot, target.id);
            ui.wdgmsg(ui.gui.map, "click", target.sc, target.rc, 3, 0, 0, (int)target.id, target.rc, 0, -1);
            bot.botSleep(80);
            return BotState.initializeStack("loglooter", "flowermenu");
         }

            // Walk toward the target — use pathfinding to go around fences/boulders.
            if (lastAction + 500 < System.currentTimeMillis()) {
               lastAction = System.currentTimeMillis();
               long now = System.currentTimeMillis();
                // Stuck detection: lenient for climbing ridges (5s), stricter for flat ground (3s)
               if (lastPos != null && player.rc.dist(lastPos) < 6) {
                  long stuckLimit = isClimbing(ui) ? 5000 : 3000;
                  if (now - lastPosTime > stuckLimit) {
                    stuckCount++;
                    totalStuckForTarget++;
                    path = null; // force recalc
                    lastPosTime = now;
                    lastPos = player.rc;
                    // If stuck 3 times on same target, blacklist it as unreachable and go to next log
                    if (totalStuckForTarget >= 3) {
                       ui.message("[LogLooter] Stuck on target, skipping.", GameUI.MsgType.INFO);
                       markDone(bot, target.id);
                       target = null;
                       path = null;
                       pathDest = null;
                       totalStuckForTarget = 0;
                       stuckCount = 0;
                       return null;
                    }
                     // Try sidestep immediately on stuck — avoid ridge/water/tree tiles
                      if (stuckCount >= 1) {
                         Coord dir = target.rc.sub(player.rc);
                         Coord side = null;
                         Coord[] tries = new Coord[]{
                            new Coord(-dir.y, dir.x), new Coord(dir.y, -dir.x),
                            new Coord(22,0), new Coord(-22,0), new Coord(0,22), new Coord(0,-22)
                         };
                         for (Coord d : tries) {
                            double len = Math.sqrt(d.x*d.x + d.y*d.y);
                            if (len < 1) continue;
                            Coord norm = new Coord((int)(d.x/len*33), (int)(d.y/len*33));
                            Coord cand = player.rc.add(norm);
                            // Check if candidate tile is walkable (avoid ridges, water, trees)
                            try {
                               MCache map = ui.sess.glob.map;
                               Coord tile = cand.div(MCache.tilesz);
                               int tileId = map.gettile(tile);
                               Resource tileRes = map.tilesetr(tileId);
                               if (tileRes != null && (tileRes.name.contains("ridges") || tileRes.name.contains("water") || tileRes.name.contains("nil"))) {
                                  continue; // skip this direction
                               }
                            } catch (Exception e) {}
                            side = cand;
                            break;
                         }
                        if (side == null) side = player.rc.add((int)(Math.random()*30-15), (int)(Math.random()*30-15));
                        ui.wdgmsg(ui.gui.map, "click", side, side, 1, ui.modflags());
                        bot.botSleep(600);
                        stuckCount = 0;
                        path = null;
                        return null;
                     }
                  }
               } else {
                  lastPos = player.rc;
                  lastPosTime = now;
                  stuckCount = 0;
               }
              Coord next = target.rc;
              boolean needPath = path == null || pathDest == null || !pathDest.equals(target.rc) || now - lastPathCalc > 3000 || pathIdx >= (path != null ? path.size() : 0);
              if (needPath) {
                  try {
                     List<Coord> p = Bot.findPath(ui, player.rc, target.rc);
                     if (p != null && p.size() > 1) {
                        path = p;
                        pathDest = target.rc;
                        pathIdx = 1;
                        lastPathCalc = now;
                     } else {
                        // No path found — check if water is blocking
                        if (hasWaterBetween(ui, player.rc, target.rc)) {
                           ui.message("[LogLooter] You need to turn swimming on.", GameUI.MsgType.INFO);
                        }
                        markDone(bot, target.id);
                        target = null;
                        path = null;
                        pathDest = null;
                        totalStuckForTarget = 0;
                        stuckCount = 0;
                        return null;
                     }
                  } catch (Exception e) {
                     path = null;
                  }
               }
              if (path != null && pathIdx < path.size()) {
                  next = path.get(pathIdx);
                  // Advance waypoint when close enough to current waypoint
                  if (player.rc.dist(next) < 15) {
                     pathIdx++;
                     if (pathIdx < path.size()) next = path.get(pathIdx);
                     else next = target.rc;
                  }
                  ui.wdgmsg(ui.gui.map, "click", next, next, 1, ui.modflags());
               } else if (path != null && pathIdx >= path.size()) {
                  // Reached end of path — try direct click to final position
                  ui.wdgmsg(ui.gui.map, "click", target.rc, target.rc, 1, ui.modflags());
               } else {
                  // No path at all — skip target
                  markDone(bot, target.id);
                  target = null;
                  path = null;
                  pathDest = null;
               }
           }
      } catch (Exception e) {
         if (ui != null) ui.message("[LogLooter] Error: " + e.getMessage(), GameUI.MsgType.ERROR);
      }
      return null;
   }

   protected Gob findTarget(UI ui, Gob player, Bot bot) {
      List<Long> done = (List<Long>) bot.raw_data;
      List<Gob> candidates = new ArrayList<>();
      for (Gob g : ui.sess.glob.oc) {
         if (done != null && done.contains(g.id)) continue;
         String nm = resName(g);
          if (nm != null && (nm.contains("oldtreelog") || nm.contains("oldstump") || nm.contains("myrtleoak"))) {
            candidates.add(g);
         }
      }
      if (candidates.isEmpty()) return null;
      candidates.sort((a, b) -> Double.compare(player.rc.dist(a.rc), player.rc.dist(b.rc)));
      return candidates.get(0);
   }

   protected void markDone(Bot bot, long id) {
      List<Long> done = getDone(bot);
      done.add(id);
      bot.raw_data = done;
   }

   protected List<Long> getDone(Bot bot) {
      Object raw = bot.raw_data;
      if (raw instanceof List) return (List<Long>) raw;
      List<Long> done = new ArrayList<>();
      bot.raw_data = done;
      return done;
   }

   protected Gob findGroundItem(UI ui, Gob player) {
      for (Gob g : ui.sess.glob.oc) {
         if (player.rc.dist(g.rc) > TILESZ * 1.5) continue;
         String nm = resName(g);
         if (nm != null && nm.contains("terobjs/item") && isLoot(nm)) {
            return g;
         }
      }
      return null;
   }

   protected static String resName(Gob g) {
      try {
         ResDrawable rd = g.getattr(ResDrawable.class);
         if (rd != null) return rd.res.get().name;
      } catch (Loading l) {
      }
      return null;
   }

   private static final String[] LOOT = {
        "arrowhead", "porcupine", "silver", "feather",
        "oldtreelog", "oldstump", "treelog", "stump",
        "Rotten Log", "Rotten Stump", "whisperingsnakeskull", "hide-rattler-prep"
     };

   protected static boolean isLoot(String name) {
      for (String s : LOOT) {
         if (name.contains(s)) return true;
      }
      return false;
   }

   private static boolean hasWaterBetween(UI ui, Coord from, Coord to) {
      try {
         MCache map = ui.sess.glob.map;
         Coord a = from.div(MCache.tilesz);
         Coord b = to.div(MCache.tilesz);
         int steps = Math.max(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
         if (steps == 0) return false;
         for (int i = 0; i <= steps; i++) {
            int x = a.x + (b.x - a.x) * i / steps;
            int y = a.y + (b.y - a.y) * i / steps;
            int tile = map.gettile(new Coord(x, y));
            Resource res = map.tilesetr(tile);
            if (res != null && (res.name.contains("water") || res.name.contains("deep") || res.name.contains("watertex"))) {
               return true;
            }
         }
      } catch (Exception e) {}
      return false;
   }

   private static boolean isClimbing(UI ui) {
      try {
         Coord tc = ui.gui.map.player().rc.div(MCache.tilesz);
         MCache map = ui.sess.glob.map;
         int tile = map.gettile(tc);
         Resource res = map.tilesetr(tile);
         if (res != null && res.name.contains("ridges")) return true;
      } catch (Exception e) {}
      return false;
   }

   // Returns [blood, phlegm, ybile, bbile, max] or null if unavailable
   private static int[] getBiles(UI ui) {
      try {
         if (ui.gui == null || ui.gui.tm == null) return null;
         java.lang.reflect.Field hardF = ui.gui.tm.getClass().getDeclaredField("hard");
         java.lang.reflect.Field softF = ui.gui.tm.getClass().getDeclaredField("soft");
         java.lang.reflect.Field lmaxF = ui.gui.tm.getClass().getDeclaredField("lmax");
         hardF.setAccessible(true);
         softF.setAccessible(true);
         lmaxF.setAccessible(true);
         int[] hard = (int[]) hardF.get(ui.gui.tm);
         int[] soft = (int[]) softF.get(ui.gui.tm);
         int[] lmax = (int[]) lmaxF.get(ui.gui.tm);
         int[] result = new int[5];
         for (int i = 0; i < 4; i++) result[i] = hard[i] + soft[i];
         result[4] = lmax[0];
         return result;
      } catch (Exception e) {
         return null;
      }
   }
}