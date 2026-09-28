package haven.headless;

import haven.Charlist;
import haven.Config;
import haven.Console;
import haven.Coord;
import haven.GOut;
import haven.GameUI;
import haven.HackThread;
import haven.Session;
import haven.UI;
import haven.Widget;
import java.awt.Color;

public class HeadlessUI extends UI {
   public HeadlessUI(Session sess) {
      super(Coord.z, sess);
      this.cons = new HeadlessConsole();
   }

   @Override
   public void draw(GOut g) {
   }

   @Override
   public void newwidget(int id, String type, int parent, Object[] pargs, Object... cargs) throws InterruptedException {
      synchronized (this) {
         Widget pwdg = this.widgets.get(parent);
         if (pwdg == null) {
            throw new HeadlessUI.UIException("Null parent widget " + parent + " for " + id, type, cargs);
         } else {
            Widget wdg = pwdg.makechild(type.intern(), pargs, cargs);
            this.bind(wdg, id);
            if (type.contains("gameui")) {
                System.out.println("\tLogged in succesfully.");
                haven.RemoteUI.autoStartCmd = "startbot character";
            }

            if (type.contains("charlist")) {
                System.out.println("\tYou are now in the character list");
                final Widget w = wdg;
                new Thread("char-select") {
                   public void run() {
                      for (int i = 0; i < 30; i++) {
                         try { Thread.sleep(500); } catch (Exception e) { break; }
                         if (w instanceof Charlist) {
                            Charlist cl = (Charlist)w;
                            synchronized (cl.chars) {
                               if (!cl.chars.isEmpty()) {
                                   String name = cl.chars.get(0).name;
                                   System.out.println("\tAuto-selecting character: " + name);
                                   w.wdgmsg("play", name);
                                   return;
                               }
                            }
                         }
                      }
                      System.out.println("\tNo characters found to select");
                   }
                }.start();
             }
         }
      }
   }

   @Override
   protected void removeid(Widget wdg) {
      if (this.rwidgets.containsKey(wdg)) {
         int id = this.rwidgets.get(wdg);
         this.widgets.remove(id);
         this.rwidgets.remove(wdg);
      }

      for (Widget child = wdg.child; child != null; child = child.next) {
         this.removeid(child);
      }
   }

   @Override
   public void destroy(Widget wdg) {
      this.removeid(wdg);
      wdg.reqdestroy();
   }

   @Override
   public void destroy(int id) {
      synchronized (this) {
         if (this.widgets.containsKey(id)) {
            Widget wdg = this.widgets.get(id);
            this.destroy(wdg);
            if (wdg == this.gui) {
                this.gui = null;
             }
         }
      }
   }

   @Override
   public void destroy() {
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      int id;
      synchronized (this) {
         if (!this.rwidgets.containsKey(sender)) {
            throw new HeadlessUI.UIException("Wdgmsg sender (" + sender.getClass().getName() + ") is not in rwidgets", msg, args);
         }

         id = this.rwidgets.get(sender);
      }

      if (this.rcvr != null) {
         this.rcvr.rcvmsg(id, msg, args);
      }
   }

   @Override
   public void uimsg(int id, String msg, Object... args) {
      synchronized (this) {
         Widget wdg = this.widgets.get(id);
         if (wdg != null) {
            wdg.uimsg(msg.intern(), args);
         } else {
            throw new HeadlessUI.UIException("Uimsg to non-existent widget " + id, msg, args);
         }
      }
   }

   @Override
   public int modflags() {
      return 0;
   }

   @Override
   public void message(String str, GameUI.MsgType type) {
      if (this.cons != null && this.gui != null) {
         this.gui.message(str, type);
      }
   }

   @Override
   public void message(String str, Color msgColor) {
      if (this.cons != null && this.gui != null) {
         this.gui.message(str, msgColor);
      }
   }

   protected class HeadlessConsole extends Console {
      protected HeadlessConsole() {
         this.setcmd("q", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               HackThread.tg().interrupt();
            }
         });
         this.setcmd("lo", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               HeadlessUI.this.sess.close();
            }
         });
          this.setcmd("toggle3d", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
            }
          });
         this.setcmd("startbot", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               HeadlessUI.this.bmgr.startBot(args);
            }
         });
         this.setcmd("stopbot", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               HeadlessUI.this.bmgr.stopBot(args);
            }
         });
      }
   }

   public interface HeadlessRunner {
      Session run(HeadlessUI var1) throws InterruptedException;
   }

   public static class UIException extends RuntimeException {
      public String mname;
      public Object[] args;

      public UIException(String message, String mname, Object... args) {
         super(message);
         this.mname = mname;
         this.args = args;
      }
   }
}
