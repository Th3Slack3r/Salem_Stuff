package haven;

import java.util.ArrayList;
import java.util.List;
import org.latikai.bots.Bot;
import org.latikai.bots.BotManager;

public class RemoteUI implements UI.Receiver, UI.Runner, BotManager {
   Session sess;
   Session ret;
   UI ui;
   private List<Bot> bots;

   public RemoteUI(Session sess) {
      this.sess = sess;
      this.bots = new ArrayList<>();
      Widget.initnames();
   }

   @Override
   public void rcvmsg(int id, String name, Object... args) {
      Message msg = new Message(1);
      msg.adduint16(id);
      msg.addstring(name);
      msg.addlist(args);
      this.sess.queuemsg(msg);
   }

   public void ret(Session sess) {
      synchronized (this.sess) {
         this.ret = sess;
         this.sess.notifyAll();
      }
   }

   @Override
   public void startBot(String[] commands) {
      if (commands.length >= 2) {
         Bot b = Bot.getBot(commands);
         if (b != null) {
            this.bots.add(b);
            b.updateBot(this.ui);
         }
      }
   }

   @Override
   public void stopBot(String[] commands) {
      if (commands.length == 2) {
         for (int i = this.bots.size() - 1; i >= 0; i--) {
            if (this.bots.get(i).type(commands[1])) {
               this.bots.remove(i);
               this.ui.message("[BotManager] Removed bot of type " + commands[1], GameUI.MsgType.INFO);
            }
         }
      } else if (commands.length == 1) {
         for (int ix = this.bots.size() - 1; ix >= 0; ix--) {
            this.bots.remove(ix);
            this.ui.message("[BotManager] Removed all bots", GameUI.MsgType.INFO);
         }
      }
   }

   @Override
   public void updateBots() {
      for (int i = this.bots.size() - 1; i >= 0; i--) {
         Bot b = this.bots.get(i);
         boolean errored = false;

         try {
            b.updateBot(this.ui);
         } catch (Exception var5) {
            System.out.println("Stopping bot of type " + b.getClass().getName() + " because of a runtime error.");
            var5.printStackTrace(System.out);
            errored = true;
         }

         if (!b.isRunning() || errored) {
            this.bots.remove(i);
         }
      }
   }

   @Override
   public Session run(UI ui) throws InterruptedException {
      this.ui = ui;
      ui.setreceiver(this);
      ui.setbotmanager(this);

      while (true) {
         Message msg;
         while ((msg = this.sess.getuimsg()) == null) {
            this.updateBots();
            synchronized (this.sess) {
               if (this.ret != null) {
                  this.sess.close();
                  return this.ret;
               }

               if (!this.sess.alive()) {
                  return null;
               }

               this.sess.wait(50L);
            }
         }

         if (msg.type == 0) {
            int id = msg.uint16();
            String type = msg.string();
            int parent = msg.uint16();
            Object[] pargs = msg.list();
            Object[] cargs = msg.list();
            ui.newwidget(id, type, parent, pargs, cargs);
         } else if (msg.type == 1) {
            int id = msg.uint16();
            String name = msg.string();
            Object[] args = msg.list();
            ui.uimsg(id, name, args);
            this.checkvents(name, args);
         } else if (msg.type == 2) {
            int id = msg.uint16();
            ui.destroy(id);
         }
      }
   }

   private void checkvents(String name, Object[] args) {
      if (name.equals("prog") && args.length == 0) {
         this.progressComplete();
      }
   }

   private void progressComplete() {
      try {
         if (Config.autosift && UI.isCursor("gfx/hud/curs/sft")) {
            MapView map = UI.instance.gui.map;
            Gob player = map.player();
            map.wdgmsg(map, "click", new Object[]{player.sc, player.rc, 1, 0});
         }
      } catch (Exception var3) {
      }
   }
}
