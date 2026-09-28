package haven.plugins;

import haven.Audio;
import haven.Composite;
import haven.Config;
import haven.Glob;
import haven.Gob;
import haven.Loading;
import haven.ResDrawable;
import haven.Resource;
import haven.UI;
import haven.GameUI.MsgType;
import haven.Glob.Pagina;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import org.ender.timer.Timer;

public class PlayerAlertPlugin extends Plugin {
   private static UI ui = null;
   private static boolean isRunning = false;
   private static boolean signalToStop = false;

   public void load(UI ui) {
      Glob glob = ui.sess.glob;
      Collection<Pagina> p = glob.paginae;
      p.add(glob.paginafor(Resource.load("paginae/add/playeralertplugin")));
      XTendedPaginae.registerPlugin("playeralertplugin", this);
   }

   public void execute(UI ui) {
      PlayerAlertPlugin.ui = ui;
      if (!isRunning) {
         isRunning = true;
         new Thread(new Runnable() {
            @Override
            public void run() {
               PlayerAlertPlugin.this.perform_task();
            }
         }, "Pickup Plugin").start();
      } else {
         signalToStop = true;
         ui.message("[PlayerAlertPlugin] Stopping...", MsgType.INFO);
      }
   }

   private void perform_task() {
      this.findPlayers();
      UI.instance.message("Plugin closed", MsgType.INFO);
      isRunning = false;
      signalToStop = false;
   }

   private void findPlayers() {
      while (!signalToStop) {
         try {
            Collection<Gob> gobs = ui.sess.glob.oc.getGobs();
            Iterator<Gob> gobs_iterator = gobs.iterator();
            Gob current_gob = null;
            int bodyCount = 0;

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
               } catch (Loading var13) {
               }

               try {
                  cmp = (Composite)current_gob.getattr(Composite.class);
                  if (cmp != null) {
                     nm = ((Resource)cmp.base.get()).name;
                  }
               } catch (Loading var12) {
               }

               if (nm != null && nm.contains("gfx/borka/body")) {
                  bodyCount++;
               }
            }

            if (bodyCount > 1) {
               ui.message("[PlayerAlertPlugin] Nearby Player detected!!!", MsgType.BAD);

               try {
                  Object file1;
                  try {
                     file1 = new FileInputStream(Config.userhome + "/alertsound.wav");
                  } catch (FileNotFoundException var10) {
                     file1 = Timer.class.getResourceAsStream("/timer.wav");
                  }

                  Audio.play((InputStream)file1, 1.0, 1.0);
               } catch (Exception var11) {
                  ui.message("[PlayerAlertPlugin] Error on the Audio thingy: " + var11.getMessage(), MsgType.INFO);
               }
               break;
            }
         } catch (Exception var14) {
         }

         try {
            Thread.sleep(500L);
         } catch (InterruptedException var9) {
         }
      }
   }
}
