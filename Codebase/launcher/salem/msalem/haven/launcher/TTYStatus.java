package haven.launcher;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

public class TTYStatus implements Status {
   private static final char[] progc = "|/-\\".toCharArray();
   private static final int INTERVAL = 50;
   private final PrintStream out;
   private String curmsg = "";
   private int prog;
   private long lastprog;

   public TTYStatus(PrintStream out) {
      this.out = out;
   }

   private static PrintStream tty() throws IOException {
      return new PrintStream(new BufferedOutputStream(Files.newOutputStream(Utils.path("/dev/tty"))));
   }

   public TTYStatus() throws IOException {
      this(tty());
   }

   @Override
   public void dispose() {
      this.out.close();
   }

   private void reprint(String text) {
      this.out.print("\r" + text + "\u001b[K");
      this.out.flush();
   }

   @Override
   public void message(String text) {
      this.reprint(this.curmsg = text + " ");
      this.prog = 0;
      this.lastprog = 0L;
   }

   @Override
   public void transfer(long size, long cur) {
      long now = System.currentTimeMillis();
      if (now - this.lastprog > 50L) {
         this.reprint(String.format("%s(%d%%)", this.curmsg, 100L * cur / size));
         this.lastprog = now;
      }
   }

   @Override
   public void progress() {
      long now = System.currentTimeMillis();
      if (now - this.lastprog > 50L) {
         this.reprint(String.format("%s%c", this.curmsg, progc[this.prog++ % progc.length]));
         this.lastprog = now;
      }
   }

   @Override
   public void close() {
      this.reprint("");
      this.curmsg = "";
   }

   @Override
   public boolean command(String[] argv, Config cfg, Config.Environment env) {
      String var4 = argv[0];
      byte var5 = -1;
      switch (var4.hashCode()) {
         case 110371416:
            if (var4.equals("title")) {
               var5 = 0;
            }
         default:
            switch (var5) {
               case 0:
                  if (argv.length < 2) {
                     throw new RuntimeException("usage: title TITLE");
                  }

                  String title = Config.expand(argv[1], env);
                  this.out.printf("Launching %s...\n", title);
                  this.out.flush();
                  return true;
               default:
                  return false;
            }
      }
   }

   @Override
   public void error(Throwable exc) {
      exc.printStackTrace(this.out);
      this.out.flush();
   }
}
