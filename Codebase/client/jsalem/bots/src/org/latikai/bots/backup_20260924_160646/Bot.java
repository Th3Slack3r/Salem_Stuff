package org.latikai.bots;

import haven.Coord;
import haven.FlatnessTool;
import haven.GItem;
import haven.GameUI;
import haven.Gob;
import haven.MCache;
import haven.Makewindow;
import haven.ResDrawable;
import haven.Resource;
import haven.UI;
import haven.Widget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.Map;

public class Bot {
   final Stack<BotState> state_stack = new Stack<>();
   public Object raw_data;
   public String[] arguments;
   private long sleeptime = 0L;
   private static final BotStuff botHelper = new BotStuff();

   public final boolean type(String name) {
      if (this.state_stack != null && !this.state_stack.empty()) {
         BotAnnotation ba = (BotAnnotation)this.state_stack.get(0).getClass().getAnnotation(BotAnnotation.class);
         return ba.bot().equals(name);
      } else {
         return false;
      }
   }

   public Makewindow currentlyCrafting(UI ui, String name) throws Resource.Loading {
      boolean window = false;
      Makewindow mw = null;

      for(Widget w : ui.widgets.values()) {
         if (w.getClass().equals(Makewindow.class)) {
            mw = (Makewindow)w;
         }
      }

      if (mw != null) {
         try {
            Collection<Makewindow.Spec> outputs = botHelper.getMakewindowOutputs(mw);
            for(Makewindow.Spec s : outputs) {
               if (s.res.get().name.equals(name)) {
                  window = true;
                  break;
               }
            }
         } catch (Exception var7) {
            System.err.println("Reflection error accessing Makewindow outputs: " + var7.getMessage());
            mw = null;
         }
      }

      if (!window) {
         mw = null;
      }

      return mw;
   }

   public final void updateBot(UI ui) {
      this.updateBot(ui, true);
   }

   public final void updateBot(UI ui, boolean enforce_sleep) {
      if (System.currentTimeMillis() >= this.sleeptime || !enforce_sleep) {
         Stack<BotState> next_state = ((BotState)this.state_stack.lastElement()).update(ui, this);
         if (next_state != null) {
            this.state_stack.pop();
            this.state_stack.addAll(next_state);
            BotAnnotation ba = (BotAnnotation)((BotState)this.state_stack.lastElement()).getClass().getAnnotation(BotAnnotation.class);
            if (this.state_stack.size() > 1 && ba.step().equals("end")) {
               this.state_stack.pop();
            }

            this.updateBot(ui, true);
         }
      }

   }

   public void passArgument(String[] args) {
      this.arguments = args;
   }

   public static Bot getBot(String[] commands) {
      BotState botstate = BotState.getBotState(commands[1], "start");
      if (botstate != null) {
         Bot b = new Bot();
         b.state_stack.push(botstate);
         b.passArgument((String[])Arrays.copyOfRange(commands, 2, commands.length));
         return b;
      } else {
         return null;
      }
   }

   public boolean isRunning() {
      if (this.state_stack != null && !this.state_stack.empty()) {
         BotAnnotation ba = (BotAnnotation)((BotState)this.state_stack.firstElement()).getClass().getAnnotation(BotAnnotation.class);
         return !ba.step().equals("end");
      } else {
         return false;
      }
   }

   public boolean gotWater(UI ui) {
      boolean gotwater = false;
      for(GItem g : ui.gui.hand) {
         if (g.res.get().name.contains("bucket-water")) {
            gotwater = true;
            break;
         }
      }

      return gotwater;
   }

   public void botSleep(int base_time) {
      int actual_time = (int)(Math.random() * (double)base_time / 10.0) + base_time;
      this.sleeptime = System.currentTimeMillis() + (long)actual_time;
   }

   public Gob getClosestGob(UI ui, String name) {
      Gob pl = ui.gui.map.player();
      double mindist = -1.0;
      Gob closestinstance = null;

      for(Gob g : ui.sess.glob.oc) {
         ResDrawable rd = (ResDrawable)g.getattr(ResDrawable.class);
         if (rd != null && rd.res.get().name.contains(name)) {
            Coord plloc = pl.rc;
            Coord goloc = g.rc;
            double dist = goloc.dist(plloc);
            if (dist < mindist || mindist < 0.0) {
               mindist = dist;
               closestinstance = g;
            }
         }
      }

      return closestinstance;
   }

   protected static List<Coord> getTiles(UI ui, String restriction, String log_prefix, boolean selection) {
    Coord c1 = Coord.z, c2 = Coord.z;
    boolean hasSelection = FlatnessTool.hasInstance(ui);

    if (selection && hasSelection) {
        c1 = FlatnessTool.instance(ui.gui).c1;
        c2 = FlatnessTool.instance(ui.gui).c2;
        if (c2.x < c1.x) { Coord t = c1; c1 = c2; c2 = t; }
        if (c2.y < c1.y) { Coord t = c1; c1 = c2; c2 = t; }
    } else if (selection && !hasSelection) {
        if (ui.gui != null && ui.gui.map != null && ui.gui.map.player() != null) {
            Coord playerTile = ui.gui.map.player().rc.div(MCache.tilesz);
            int range = 50;
            c1 = playerTile.add(-range, -range);
            c2 = playerTile.add(range, range);
            if (log_prefix != null) {
                ui.message("[" + log_prefix + "] No selection, scanning " + (range*2) + "x" + (range*2) + " area around player", haven.GameUI.MsgType.INFO);
            }
        } else {
            if (log_prefix != null) {
                ui.message("[" + log_prefix + "] No player position available", haven.GameUI.MsgType.INFO);
            }
            return null;
        }
    }

    List<Coord> tiles = new ArrayList();
    java.util.Set<String> foundNames = new java.util.HashSet<>();

    try {
        MCache m = botHelper.getMCache(ui);
        Map<Coord, MCache.Grid> grids = botHelper.getMCacheGrids(m);

        for (Entry<Coord, MCache.Grid> e : grids.entrySet()) {
            MCache.Grid g = e.getValue();
            int[] tileArray = botHelper.getGridTiles(g);

            for (int tileidx = 0; tileidx < tileArray.length; ++tileidx) {
                String tilesetName = botHelper.getTilesetResourceName(tileArray[tileidx], m);
                Coord tc_absolute = new Coord(tileidx % MCache.cmaps.x, tileidx / MCache.cmaps.x).add(g.ul);

                if (tc_absolute.isect(c1, c2.add(c1.inv()).add(1, 1))) {
                    if (!foundNames.contains(tilesetName)) {
                        ui.message("Found Tile Type: " + tilesetName, haven.GameUI.MsgType.INFO);
                        foundNames.add(tilesetName);
                    }

                    if (tilesetName.contains(restriction)) {
                        tiles.add(tc_absolute.mul(11).add(5, 5));
                    }
                }
            }
        }
    } catch (Exception var15) {
        ui.message("Bot Error: " + var15.getMessage(), haven.GameUI.MsgType.ERROR);
        return null;
    }
    return tiles;
}

   protected static List<Coord> getTiles(UI ui, String restriction, String log_prefix) {
      return getTiles(ui, restriction, log_prefix, true);
   }

   protected static List<Gob> getGobs(UI ui, String restriction, String log_prefix) {
      return getGobs(ui, restriction, log_prefix, true);
   }

   protected static List<Gob> getGobs(UI ui, String restriction, String log_prefix, boolean selection) {
    Coord c1 = Coord.z, c2 = Coord.z;
    boolean hasSelection = FlatnessTool.hasInstance(ui);

    if (selection && hasSelection) {
        c1 = FlatnessTool.instance(ui.gui).c1;
        c2 = FlatnessTool.instance(ui.gui).c2;
        if (c2.x < c1.x) { Coord t = c1; c1 = c2; c2 = t; }
        if (c2.y < c1.y) { Coord t = c1; c1 = c2; c2 = t; }
    } else if (selection && !hasSelection) {
        if (ui.gui != null && ui.gui.map != null && ui.gui.map.player() != null) {
            Coord playerTile = ui.gui.map.player().rc.div(MCache.tilesz);
            int range = 50;
            c1 = playerTile.add(-range, -range);
            c2 = playerTile.add(range, range);
            if (log_prefix != null) {
                ui.message("[" + log_prefix + "] No selection, scanning " + (range*2) + "x" + (range*2) + " area around player", GameUI.MsgType.INFO);
            }
        } else {
            if (log_prefix != null) {
                ui.message("[" + log_prefix + "] No player position available", GameUI.MsgType.INFO);
            }
            return null;
        }
    }

    Collection<Gob> gobs = new java.util.ArrayList<>(); for(Gob g : ui.sess.glob.oc) gobs.add(g);
    List<Gob> objects = new ArrayList();

         for(Gob g : gobs) {
            ResDrawable rd = (ResDrawable)g.getattr(ResDrawable.class);
            boolean isrequired = rd != null && rd.res.get().name.contains(restriction);
            boolean isselected = true;
            if (selection) {
               isselected = g.rc.div(11).isect(c1, c2.add(c1.inv()).add(1, 1));
            }

            if (isrequired && isselected) {
               objects.add(g);
            }
         }

         int nrobjects = objects.size();
         if (log_prefix != null) {
            ui.message("[" + log_prefix + "] Detected " + nrobjects + " objects", GameUI.MsgType.INFO);
         }

         Collections.sort(objects, new Comparator<Gob>() {
            public int compare(Gob o1, Gob o2) {
               return o1.rc.compareTo(o2.rc);
            }
         });
          return objects;
   }

   public static List<Coord> findPath(UI ui, Coord from, Coord to) {
      List<Coord> path = new ArrayList<>();
      MCache map = ui.sess.glob.map;

      Coord fromTile = from.div(MCache.tilesz);
      Coord toTile = to.div(MCache.tilesz);

      if (fromTile.equals(toTile)) {
         path.add(to);
         return path;
      }

      java.util.PriorityQueue<Node> open = new java.util.PriorityQueue<>();
      java.util.Set<Coord> closed = new java.util.HashSet<>();
      java.util.Map<Coord, Coord> cameFrom = new java.util.HashMap<>();
      java.util.Map<Coord, Integer> gScore = new java.util.HashMap<>();

      Node start = new Node(fromTile, 0, heuristic(fromTile, toTile));
      open.add(start);
      gScore.put(fromTile, 0);

      int[] dx = {0, 1, 0, -1, 1, -1, 1, -1};
      int[] dy = {1, 0, -1, 0, 1, -1, -1, 1};

      while (!open.isEmpty()) {
         Coord current = open.poll().pos;
         if (current.equals(toTile)) {
            Coord c = current;
            while (c != null) {
               path.add(0, c.mul(MCache.tilesz).add(MCache.tilesz.div(2)));
               c = cameFrom.get(c);
            }
            return path;
         }
         closed.add(current);

         for (int i = 0; i < dx.length; i++) {
            Coord next = current.add(dx[i], dy[i]);
            if (closed.contains(next)) continue;
            if (!isWalkable(map, next, ui)) continue;

            int tentG = gScore.get(current) + (i < 4 ? 10 : 14);
            if (tentG < gScore.getOrDefault(next, Integer.MAX_VALUE)) {
               cameFrom.put(next, current);
               gScore.put(next, tentG);
               open.add(new Node(next, tentG, heuristic(next, toTile)));
            }
         }
      }
      return path;
   }

   private static int heuristic(Coord a, Coord b) {
      int d = Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
      int s = Math.min(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
      return s * 14 + (d - s) * 10;
   }

   private static boolean isWalkable(MCache map, Coord tc, UI ui) {
      try {
         int tile = map.gettile(tc);
         Resource res = map.tilesetr(tile);
         if (res == null) return false;
         if (res.name.contains("water") || res.name.contains("wall")) return false;

         for (Gob g : ui.sess.glob.oc) {
            Coord gt = g.rc.div(MCache.tilesz);
            if (gt.equals(tc)) {
               try {
                  ResDrawable rd = (ResDrawable)g.getattr(ResDrawable.class);
                  if (rd != null) {
                     String rn = rd.res.get().name;
                     // Block trees via "terobjs/tree"
                     if (rn.contains("boulder") || rn.contains("fence") || rn.contains("palisade") || rn.contains("wall") || rn.contains("gate") || rn.contains("terobjs/tree") || rn.contains("bush")) return false;
                  }
               } catch (Exception e) {}
            }
         }
         return true;
      } catch (Exception e) { return false; }
   }

   private static class Node implements Comparable<Node> {
      Coord pos;
      int f;
      Node(Coord pos, int g, int h) { this.pos = pos; this.f = g + h; }
      public int compareTo(Node o) { return Integer.compare(this.f, o.f); }
   }

   public static class GridLocation {
      public static class OfField implements Comparator<Gob> {
         public int compare(Gob o1, Gob o2) {
            if (o1.rc.x != o2.rc.x) {
               return o1.rc.x - o2.rc.x;
            } else {
               int sign = o1.rc.x / 4 % 2;
               return sign == 0 ? o1.rc.y - o2.rc.y : o2.rc.y - o1.rc.y;
            }
         }
      }

      public static class OfGob implements Comparator<Gob> {
         public int compare(Gob o1, Gob o2) {
            if (o1.rc.x != o2.rc.x) {
               return o1.rc.x - o2.rc.x;
            } else {
               int sign = o1.rc.x % 2;
               return sign == 0 ? o1.rc.y - o2.rc.y : o2.rc.y - o1.rc.y;
            }
         }
      }

      public static class OfLoc implements Comparator<Coord> {
         public int compare(Coord o1, Coord o2) {
            if (o1.x != o2.x) {
               return o1.x - o2.x;
            } else {
               int sign = o1.x % 2;
               return sign == 0 ? o1.y - o2.y : o2.y - o1.y;
            }
         }
      }
   }

   public static class PlayerCloseness {
      public static class ToGob implements Comparator<Gob> {
         Gob player;

         public ToGob(UI ui) {
            this.player = ui.gui.map.player();
         }

         public int compare(Gob o1, Gob o2) {
            return (int)Math.signum(o1.rc.dist(this.player.rc) - o2.rc.dist(this.player.rc));
         }
      }

      public static class ToLoc implements Comparator<Coord> {
         Gob player;

         public ToLoc(UI ui) {
            this.player = ui.gui.map.player();
         }

         public int compare(Coord o1, Coord o2) {
            return (int)Math.signum(o1.dist(this.player.rc) - o2.dist(this.player.rc));
         }
      }
   }
}