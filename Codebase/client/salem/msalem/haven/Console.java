package haven;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class Console {
   private static Map<String, Console.Command> scommands = new TreeMap<>();
   private Map<String, Console.Command> commands = new TreeMap<>();
   private Collection<Console.Directory> dirs = new LinkedList<>();
   public PrintWriter out;

   public Console() {
      this.clearout();
   }

   public static void setscmd(String name, Console.Command cmd) {
      synchronized (scommands) {
         scommands.put(name, cmd);
      }
   }

   public void setcmd(String name, Console.Command cmd) {
      synchronized (this.commands) {
         this.commands.put(name, cmd);
      }
   }

   public Map<String, Console.Command> findcmds() {
      Map<String, Console.Command> ret = new TreeMap<>();
      synchronized (scommands) {
         ret.putAll(scommands);
      }

      synchronized (this.commands) {
         ret.putAll(this.commands);
      }

      synchronized (this.dirs) {
         for (Console.Directory dir : this.dirs) {
            Map<String, Console.Command> cmds = dir.findcmds();
            ret.putAll(cmds);
         }

         return ret;
      }
   }

   public void add(Console.Directory dir) {
      synchronized (this.dirs) {
         this.dirs.add(dir);
      }
   }

   public Console.Command findcmd(String name) {
      return this.findcmds().get(name);
   }

   public void run(String[] args) throws Exception {
      if (args.length >= 1) {
         Console.Command cmd = this.findcmd(args[0]);
         if (cmd == null) {
            throw new Exception(args[0] + ": no such command");
         } else {
            cmd.run(this, args);
         }
      }
   }

   public void run(String cmdl) throws Exception {
      this.run(Utils.splitwords(cmdl));
   }

   public void clearout() {
      this.out = new PrintWriter(new Writer() {
         @Override
         public void write(char[] b, int o, int c) {
         }

         @Override
         public void close() {
         }

         @Override
         public void flush() {
         }
      });
   }

   public interface Command {
      void run(Console var1, String[] var2) throws Exception;
   }

   public interface Directory {
      Map<String, Console.Command> findcmds();
   }
}
