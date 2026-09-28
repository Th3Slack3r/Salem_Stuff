package haven.headless;

import haven.Console;
import haven.Session;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

public class HeadlessHavenPanel implements Runnable, Console.Directory {
   HeadlessUI ui;
   Queue<String> commands = new LinkedList<>();
   int fd = 100;

   public HeadlessHavenPanel(HeadlessMainFrame parent) {
      this.newheadlessui(null, parent);
   }

   HeadlessUI newheadlessui(Session sess, HeadlessMainFrame parent) {
      if (this.ui != null) {
         this.ui.destroy();
      }

      this.ui = new HeadlessUI(sess);
      this.ui.cons.add(parent);
      this.ui.cons.add(this);
      return this.ui;
   }

   void dispatch() {
      synchronized (this.commands) {
         String command = null;

         while ((command = this.commands.poll()) != null) {
            try {
               if (command.startsWith(":")) command = command.substring(1);
               this.ui.cons.run(command);
            } catch (Exception var5) {
               System.out.println("\t" + var5.getMessage());
            }
         }
      }
   }

   @Override
   public void run() {
      Thread scannerthread = null;

      try {
         do {
            HeadlessUI ui = this.ui;
            long then = System.currentTimeMillis();
            synchronized (ui) {
               if (ui.sess != null) {
                  ui.sess.glob.ctick();
                  if (scannerthread == null) {
                     scannerthread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                           Scanner in = new Scanner(System.in);

                           while (in.hasNextLine()) {
                              String line = in.nextLine();
                              synchronized (HeadlessHavenPanel.this.commands) {
                                 HeadlessHavenPanel.this.commands.add(line);
                              }
                           }
                        }
                     });
                     scannerthread.start();
                  }
               } else if (scannerthread != null) {
                  scannerthread.interrupt();
                  scannerthread.join();
               }

               this.dispatch();
            }

            long now = System.currentTimeMillis();
            if (now - then < this.fd) {
               synchronized (this.commands) {
                  this.commands.wait(this.fd - (now - then));
               }
            }
         } while (!Thread.interrupted());

         throw new InterruptedException();
      } catch (InterruptedException var21) {
      } finally {
         this.ui.destroy();
         if (scannerthread != null) {
            scannerthread.interrupt();

            try {
               scannerthread.join();
            } catch (InterruptedException var18) {
            }
         }
      }
   }

   @Override
   public Map<String, Console.Command> findcmds() {
      return new HashMap<>();
   }
}
