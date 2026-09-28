package haven.plugins;

import haven.Composite;
import haven.Drawable;
import haven.GItem;
import haven.Glob;
import haven.Gob;
import haven.Loading;
import haven.Moving;
import haven.ResDrawable;
import haven.Resource;
import haven.UI;
import haven.GameUI.MsgType;
import haven.Glob.Pagina;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class BarnHandler extends Plugin {
   private static UI ui = null;
   private static boolean isRunning = false;
   private static boolean signalToStop = false;

   public void load(UI ui) {
      Glob glob = ui.sess.glob;
      Collection<Pagina> p = glob.paginae;
      p.add(glob.paginafor(Resource.load("paginae/add/barnhandler")));
      XTendedPaginae.registerPlugin("barnhandler", this);
   }

   public void execute(UI ui) {
      BarnHandler.ui = ui;
      if (!isRunning) {
         isRunning = true;
         new Thread(new Runnable() {
            @Override
            public void run() {
               BarnHandler.this.perform_task();
            }
         }, "BarnHandler").start();
      } else {
         signalToStop = true;
         ui.message("[BarnHandler] Stopping...", MsgType.INFO);
      }
   }

   private void perform_task() {
      int silverInHand = 0;
      ArrayList<Gob> allKritters = this.getAllKritters();
      silverInHand = this.checkHand();
      ArrayList<String> names = new ArrayList<>();
      if (silverInHand == 0) {
         names = null;
         ui.message("[BarnHandler] Display only sick Animals...", MsgType.INFO);
      } else if (silverInHand == 1) {
         names.add("calf");
         names.add("kid");
         names.add("piglet");
         names.add("lamb");
         ui.message("[BarnHandler] Display only Baby Animals...", MsgType.INFO);
      } else if (silverInHand == 2) {
         names.add("female");
         ui.message("[BarnHandler] Display only Female Animals...", MsgType.INFO);
      } else if (silverInHand == 3) {
         names.add("pmale");
         names.add("wmale");
         names.add("tmale");
         names.add("gmale");
         ui.message("[BarnHandler] Display only Male Animals...", MsgType.INFO);
      }

      this.displayOnly(names, allKritters);
      UI.instance.message("Plugin closed", MsgType.INFO);
      isRunning = false;
      signalToStop = false;
   }

   private void displayOnly(ArrayList<String> names, ArrayList<Gob> allKritters) {
      if (names == null) {
         long tNow = System.currentTimeMillis();
         long lastChange = System.currentTimeMillis();
         int remaining = 0;

         label91:
         while (allKritters.size() > 0 && !signalToStop) {
            if (tNow < System.currentTimeMillis() - 5000L && remaining != allKritters.size()) {
               UI.instance.message("[BarnHandler] Remaining Animals: " + allKritters.size(), MsgType.INFO);
               tNow = System.currentTimeMillis();
               remaining = allKritters.size();
            }

            Iterator<Gob> itti = allKritters.iterator();

            while (itti.hasNext()) {
               try {
                  Gob cGob = itti.next();
                  Moving mov = null;
                  mov = (Moving)cGob.getattr(Moving.class);
                  if (mov != null) {
                     lastChange = System.currentTimeMillis();
                     Composite draw = (Composite)cGob.getattr(Drawable.class);
                     draw.comp = null;
                     itti.remove();
                  } else if (lastChange + 60000L < System.currentTimeMillis()) {
                     break label91;
                  }
               } catch (Exception var16) {
               }
            }

            try {
               Thread.sleep(10L);
            } catch (InterruptedException var15) {
            }
         }
      } else {
         for (Gob gob : allKritters) {
            boolean display = false;
            ResDrawable rd = null;
            Composite cmp = null;
            String nm = "";

            try {
               rd = (ResDrawable)gob.getattr(ResDrawable.class);
               if (rd != null) {
                  nm = ((Resource)rd.res.get()).name;
               }
            } catch (Loading var14) {
            }

            try {
               cmp = (Composite)gob.getattr(Composite.class);
               if (cmp != null) {
                  nm = ((Resource)cmp.base.get()).name;
               }
            } catch (Loading var13) {
            }

            for (String name : names) {
               if (nm.contains(name)) {
                  display = true;
               }
            }

            if (!display) {
               try {
                  Composite draw = (Composite)gob.getattr(Drawable.class);
                  draw.comp = null;
               } catch (Exception var12) {
               }
            }
         }
      }

      UI.instance.message("Exiting Plugin...", MsgType.INFO);
   }

   private int checkHand() {
      for (GItem item : ui.gui.hand) {
         if (item.resname().contains("invobjs/coins") && item.name().contains("silver")) {
            String coins = item.name().split(" ")[0];
            ui.message("[BarnHandler] Coins in Hand: " + coins, MsgType.INFO);
            return Integer.parseInt(coins);
         }
      }

      ui.message("[BarnHandler] Coins in Hand: Hand is empty", MsgType.INFO);
      return 0;
   }

   private ArrayList<Gob> getAllKritters() {
      Collection<Gob> gobs = ui.sess.glob.oc.getGobs();
      Iterator<Gob> gobs_iterator = gobs.iterator();
      Gob current_gob = null;
      ArrayList<Gob> gobsList = new ArrayList<>();
      boolean debug = false;
      debug = true;
      ArrayList<String> names = new ArrayList<>();

      while (gobs_iterator.hasNext()) {
         current_gob = gobs_iterator.next();
         ResDrawable rd = null;
         Composite cmp = null;
         String nm = "";

         try {
            rd = (ResDrawable)current_gob.getattr(ResDrawable.class);
            if (rd != null) {
               nm = ((Resource)rd.res.get()).name;
            }
         } catch (Loading var12) {
         }

         try {
            cmp = (Composite)current_gob.getattr(Composite.class);
            if (cmp != null) {
               nm = ((Resource)cmp.base.get()).name;
            }
         } catch (Loading var11) {
         }

         if (nm.contains("/kritter/")) {
            gobsList.add(current_gob);
            if (debug) {
               if (!names.contains(nm)) {
               }

               names.add(nm);
            }
         }
      }

      UI.instance.message("[BarnHandler] Total Animals visible: " + names.size(), MsgType.INFO);

      while (!signalToStop && names.size() > 0) {
         Iterator<String> itti = names.iterator();
         String cName = null;
         int count = 0;
         if (itti.hasNext()) {
            cName = itti.next();
            count = 1;
            itti.remove();
         }

         while (!signalToStop && itti.hasNext()) {
            String iName = itti.next();
            if (cName.equals(iName)) {
               count++;
               itti.remove();
            }
         }

         if (cName != null) {
            UI.instance.message("[BarnHandler] " + cName + " : " + count, MsgType.INFO);
         }
      }

      return gobsList;
   }
}
