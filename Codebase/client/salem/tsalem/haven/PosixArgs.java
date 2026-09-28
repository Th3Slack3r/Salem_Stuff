package haven;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class PosixArgs {
  private final List<Arg> parsed;
  
  public String[] rest;
  
  public String arg = null;
  
  private static class Arg {
    private final char ch;
    
    private final String arg;
    
    private Arg(char ch, String arg) {
      this.ch = ch;
      this.arg = arg;
    }
  }
  
  private PosixArgs() {
    this.parsed = new ArrayList<>();
  }
  
  public static PosixArgs getopt(String[] argv, int start, String desc) {
    PosixArgs ret = new PosixArgs();
    List<Character> fl = new ArrayList<>(), fla = new ArrayList<>();
    List<String> rest = new ArrayList<>();
    for (int i = 0; i < desc.length(); ) {
      char ch = desc.charAt(i++);
      if (i < desc.length() && desc.charAt(i) == ':') {
        i++;
        fla.add(Character.valueOf(ch));
        continue;
      } 
      fl.add(Character.valueOf(ch));
    } 
    boolean acc = true;
    for (int j = start; j < argv.length; ) {
      String arg = argv[j++];
      if (acc && arg.equals("--"))
        acc = false; 
      if (acc && arg.charAt(0) == '-') {
        for (int o = 1; o < arg.length(); ) {
          char ch = arg.charAt(o++);
          if (fl.contains(Character.valueOf(ch))) {
            ret.parsed.add(new Arg(ch, null));
            continue;
          } 
          if (fla.contains(Character.valueOf(ch))) {
            if (o < arg.length()) {
              ret.parsed.add(new Arg(ch, arg.substring(o)));
              continue;
            } 
            if (j < argv.length) {
              ret.parsed.add(new Arg(ch, argv[j++]));
              continue;
            } 
            System.err.println("option requires an argument -- '" + ch + "'");
            return null;
          } 
          System.err.println("invalid option -- '" + ch + "'");
          return null;
        } 
        continue;
      } 
      rest.add(arg);
    } 
    ret.rest = rest.<String>toArray(new String[0]);
    return ret;
  }
  
  public static PosixArgs getopt(String[] argv, String desc) {
    return getopt(argv, 0, desc);
  }
  
  public Iterable<Character> parsed() {
    return new Iterable<Character>() {
        public Iterator<Character> iterator() {
          return new Iterator<Character>() {
              private int i = 0;
              
              public boolean hasNext() {
                return (this.i < PosixArgs.this.parsed.size());
              }
              
              public Character next() {
                if (this.i >= PosixArgs.this.parsed.size())
                  throw new NoSuchElementException(); 
                PosixArgs.Arg a = PosixArgs.this.parsed.get(this.i++);
                PosixArgs.this.arg = a.arg;
                return Character.valueOf(a.ch);
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
      };
  }
}
