package org.latikai.bots;

import haven.Coord;
import haven.FlowerMenu;
import haven.GameUI;
import haven.Gob;
import haven.Loading;
import haven.ResDrawable;
import haven.UI;
import haven.Widget;
import java.util.Stack;

@BotAnnotation(
   bot = "loglooter",
   step = "eat"
)
public class LogLooterBotEat extends BotState {

   private static final BotStuff botHelper = new BotStuff();
   private long entered = 0;
   private boolean clickedEat = false;
   private Gob target = null;

   private static final String[] EAT = {
      "grub"
   };

   static boolean isEat(String name) {
      for (String s : EAT) {
         if (name.toLowerCase().contains(s)) return true;
      }
      return false;
   }

   protected static String resName(Gob g) {
      try {
         ResDrawable rd = g.getattr(ResDrawable.class);
         if (rd != null) return rd.res.get().name;
      } catch (Loading l) {}
      return null;
   }

   static Gob findEatTarget(UI ui, Gob player) {
      for (Gob g : ui.sess.glob.oc) {
         double dist = player.rc.dist(g.rc);
         if (dist > 30) continue;
         String nm = resName(g);
         if (nm != null && isEat(nm)) {
            return g;
         }
      }
      return null;
   }

   @Override
   public Stack<BotState> update(UI ui, Bot bot) {
      if (entered == 0) entered = System.currentTimeMillis();
      long now = System.currentTimeMillis();

      if (ui == null || ui.gui == null || ui.gui.map == null || ui.gui.map.player() == null) return null;
      Gob player = ui.gui.map.player();

      if (clickedEat) {
         if (now - entered > 3000) {
            return BotState.initializeStack("loglooter", "start");
         }
         return null;
      }

      if (target == null) {
         target = findEatTarget(ui, player);
         if (target == null) {
            return BotState.initializeStack("loglooter", "start");
         }
         String nm = resName(target);
         ui.message("[LogLooter] Eating: " + (nm == null ? "?" : nm), GameUI.MsgType.INFO);
      }

      double dist = player.rc.dist(target.rc);
      if (dist > 20) {
         if (lastAction + 500 < now) {
            lastAction = now;
            ui.wdgmsg(ui.gui.map, "click", target.rc, target.rc, 1, ui.modflags());
         }
         return null;
      }

      ui.wdgmsg(ui.gui.map, "click", target.sc, target.rc, 3, 0, 0, (int)target.id, target.rc, 0, -1);
      bot.botSleep(80);

      Widget fmWidget = null;
      Widget mapchild = ui.root.child;
      while (mapchild != null && !(mapchild instanceof FlowerMenu)) {
         mapchild = mapchild.next;
      }
      if (mapchild instanceof FlowerMenu) fmWidget = mapchild;
      else {
         for (Widget w : ui.widgets.values()) {
            if (w instanceof FlowerMenu) { fmWidget = w; break; }
         }
      }
      if (fmWidget != null) {
         FlowerMenu fm = (FlowerMenu) fmWidget;
         try {
            Object[] opts = botHelper.getFlowerMenuOpts(fm);
            if (opts != null && opts.length > 0) {
               for (Object opt : opts) {
                  if (opt == null) continue;
                  String optName = botHelper.getFlowerMenuOptionName(opt);
                  if (optName != null && optName.toLowerCase().contains("eat")) {
                     botHelper.chooseFlowerMenuOption(fm, opt);
                     ui.message("[LogLooter] Eat selected.", GameUI.MsgType.INFO);
                     clickedEat = true;
                     return null;
                  }
               }
               botHelper.chooseFlowerMenuOption(fm, opts[0]);
               clickedEat = true;
               return null;
            }
         } catch (Exception e) {}
      }

      if (now - entered > 3000) {
         return BotState.initializeStack("loglooter", "start");
      }
      return null;
   }

   private long lastAction = 0;
}
