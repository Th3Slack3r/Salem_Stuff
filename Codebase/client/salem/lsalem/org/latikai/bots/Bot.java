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
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.Map.Entry;

public class Bot {
   final Stack<BotState> state_stack = new Stack<>();
   public Object raw_data;
   public String[] arguments;
   private long sleeptime = 0L;

   public final boolean type(String name) {
      if (this.state_stack != null && !this.state_stack.empty()) {
         BotAnnotation ba = this.state_stack.get(0).getClass().getAnnotation(BotAnnotation.class);
         return ba.bot().equals(name);
      } else {
         return false;
      }
   }

   public final void updateBot(UI ui) {
      this.updateBot(ui, true);
   }

   public final void updateBot(UI ui, boolean enforce_sleep) {
      if (System.currentTimeMillis() >= this.sleeptime || !enforce_sleep) {
         Stack<BotState> next_state = this.state_stack.lastElement().update(ui, this);
         if (next_state != null) {
            this.state_stack.pop();
            this.state_stack.addAll(next_state);
            BotAnnotation ba = this.state_stack.lastElement().getClass().getAnnotation(BotAnnotation.class);
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
         b.passArgument(Arrays.copyOfRange(commands, 2, commands.length));
         return b;
      } else {
         return null;
      }
   }

   public boolean isRunning() {
      if (this.state_stack != null && !this.state_stack.empty()) {
         BotAnnotation ba = this.state_stack.firstElement().getClass().getAnnotation(BotAnnotation.class);
         return !ba.step().equals("end");
      } else {
         return false;
      }
   }

   public Makewindow currentlyCrafting(UI ui, String name) throws Resource.Loading {
      boolean window = false;
      Makewindow mw = null;

      for (Widget w : ui.widgets.values()) {
         if (w.getClass().equals(Makewindow.class)) {
            mw = (Makewindow)w;
         }
      }

      if (mw != null) {
         for (Makewindow.Spec s : mw.outputs) {
            if (s.res.get().name.equals(name)) {
               window = true;
            }
         }
      }

      if (!window) {
         mw = null;
      }

      return mw;
   }

   public boolean gotWater(UI ui) {
      boolean gotwater = false;

      for (GItem g : ui.gui.hand) {
         if (g.res.get().name.contains("bucket-water")) {
            gotwater = true;
            break;
         }
      }

      return gotwater;
   }

   public void botSleep(int base_time) {
      int actual_time = (int)(Math.random() * base_time / 10.0) + base_time;
      this.sleeptime = System.currentTimeMillis() + actual_time;
   }

   public Gob getClosestGob(UI ui, String name) {
      Gob pl = ui.gui.map.player();
      double mindist = -1.0;
      Gob closestinstance = null;

      for (Gob g : ui.sess.glob.oc.getGobs()) {
         ResDrawable rd = g.getattr(ResDrawable.class);
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
      if (!FlatnessTool.hasInstance(ui) && selection) {
         if (log_prefix != null) {
            ui.message("[" + log_prefix + "] Nothing selected!", GameUI.MsgType.INFO);
         }

         return null;
      } else {
         Coord c1 = Coord.z;
         Coord c2 = Coord.z;
         if (selection) {
            c1 = FlatnessTool.instance(ui).c1;
            c2 = FlatnessTool.instance(ui).c2;
            if (c2.x < c1.x) {
               Coord t = c1;
               c1 = c2;
               c2 = t;
            }
         }

         List<Coord> tiles = new ArrayList<>();
         MCache m = ui.sess.glob.map;

         for (Entry<Coord, MCache.Grid> e : m.grids.entrySet()) {
            MCache.Grid g = e.getValue();

            for (int tileidx = 0; tileidx < g.tiles.length; tileidx++) {
               Resource.Tileset set = m.tileset(g.tiles[tileidx]);
               if (set.getres().name.contains(restriction)) {
                  Coord tc = new Coord(tileidx % MCache.cmaps.x, tileidx / MCache.cmaps.x);
                  Coord tc_absolute = tc.add(g.ul);
                  boolean isselected = tc_absolute.isect(c1, c2.add(c1.inv()).add(1, 1));
                  if (!selection || isselected) {
                     tiles.add(tc_absolute.mul(11).add(5, 5));
                  }
               }
            }
         }

         return tiles;
      }
   }

   protected static List<Coord> getTiles(UI ui, String restriction, String log_prefix) {
      return getTiles(ui, restriction, log_prefix, true);
   }

   protected static List<Gob> getGobs(UI ui, String restriction, String log_prefix) {
      return getGobs(ui, restriction, log_prefix, true);
   }

   protected static List<Gob> getGobs(UI ui, String restriction, String log_prefix, boolean selection) {
      if (!FlatnessTool.hasInstance(ui) && selection) {
         if (log_prefix != null) {
            ui.message("[" + log_prefix + "] Nothing selected!", GameUI.MsgType.INFO);
         }

         return null;
      } else {
         Coord c1 = Coord.z;
         Coord c2 = Coord.z;
         if (selection) {
            c1 = FlatnessTool.instance(ui).c1;
            c2 = FlatnessTool.instance(ui).c2;
            if (c2.x < c1.x) {
               Coord t = c1;
               c1 = c2;
               c2 = t;
            }
         }

         Collection<Gob> gobs = ui.sess.glob.oc.getGobs();
         List<Gob> objects = new ArrayList<>();

         for (Gob g : gobs) {
            ResDrawable rd = g.getattr(ResDrawable.class);
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

         objects.sort(new Comparator<Gob>() {
            public int compare(Gob o1, Gob o2) {
               return o1.rc.compareTo(o2.rc);
            }
         });
         return objects;
      }
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
