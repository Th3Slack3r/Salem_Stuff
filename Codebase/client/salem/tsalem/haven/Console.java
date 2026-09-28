package haven;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class Console {
  public Console() {
    this.commands = new TreeMap<>();
    this.dirs = new LinkedList<>();
    clearout();
  }
  
  private static Map<String, Command> scommands = new TreeMap<>();
  
  private final Map<String, Command> commands;
  
  private final Collection<Directory> dirs;
  
  public PrintWriter out;
  
  public static void setscmd(String name, Command cmd) {
    synchronized (scommands) {
      scommands.put(name, cmd);
    } 
  }
  
  public void setcmd(String name, Command cmd) {
    synchronized (this.commands) {
      this.commands.put(name, cmd);
    } 
  }
  
  public Map<String, Command> findcmds() {
    Map<String, Command> ret = new TreeMap<>();
    synchronized (scommands) {
      ret.putAll(scommands);
    } 
    synchronized (this.commands) {
      ret.putAll(this.commands);
    } 
    synchronized (this.dirs) {
      for (Directory dir : this.dirs) {
        Map<String, Command> cmds = dir.findcmds();
        ret.putAll(cmds);
      } 
    } 
    return ret;
  }
  
  public void add(Directory dir) {
    synchronized (this.dirs) {
      this.dirs.add(dir);
    } 
  }
  
  public Command findcmd(String name) {
    return findcmds().get(name);
  }
  
  public void run(String[] args) throws Exception {
    if (args.length < 1)
      return; 
    Command cmd = findcmd(args[0]);
    if (cmd == null)
      throw new Exception(args[0] + ": no such command"); 
    cmd.run(this, args);
  }
  
  public void run(String cmdl) throws Exception {
    if (cmdl != null && cmdl.startsWith(":"))
      cmdl = cmdl.replace(":", ""); 
    run(Utils.splitwords(cmdl));
  }
  
  public void clearout() {
    this.out = new PrintWriter(new Writer() {
          public void write(char[] b, int o, int c) {}
          
          public void close() {}
          
          public void flush() {}
        });
  }
  
  public static interface Directory {
    Map<String, Console.Command> findcmds();
  }
  
  public static interface Command {
    void run(Console param1Console, String[] param1ArrayOfString) throws Exception;
  }
}
