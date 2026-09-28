package haven;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class PosixArgs {
   private List<PosixArgs.Arg> parsed;
   public String[] rest;
   public String arg = null;

   private PosixArgs() {
      this.parsed = new ArrayList<>();
   }

   public static PosixArgs getopt(String[] argv, int start, String desc) {
      PosixArgs ret = new PosixArgs();
      List<Character> fl = new ArrayList<>();
      List<Character> fla = new ArrayList<>();
      List<String> rest = new ArrayList<>();
      int i = 0;

      while (i < desc.length()) {
         char ch = desc.charAt(i++);
         if (i < desc.length() && desc.charAt(i) == ':') {
            i++;
            fla.add(ch);
         } else {
            fl.add(ch);
         }
      }

      boolean acc = true;
      int ix = start;

      while (ix < argv.length) {
         String arg = argv[ix++];
         if (acc && arg.equals("--")) {
            acc = false;
         }

         if (acc && arg.charAt(0) == '-') {
            int o = 1;

            while (o < arg.length()) {
               char ch = arg.charAt(o++);
               if (!fl.contains(ch)) {
                  if (!fla.contains(ch)) {
                     System.err.println("invalid option -- '" + ch + "'");
                     return null;
                  }

                  if (o < arg.length()) {
                     ret.parsed.add(new PosixArgs.Arg(ch, arg.substring(o)));
                  } else {
                     if (ix >= argv.length) {
                        System.err.println("option requires an argument -- '" + ch + "'");
                        return null;
                     }

                     ret.parsed.add(new PosixArgs.Arg(ch, argv[ix++]));
                  }
                  break;
               }

               ret.parsed.add(new PosixArgs.Arg(ch, null));
            }
         } else {
            rest.add(arg);
         }
      }

      ret.rest = rest.toArray(new String[0]);
      return ret;
   }

   public static PosixArgs getopt(String[] argv, String desc) {
      return getopt(argv, 0, desc);
   }

   public Iterable<Character> parsed() {
      return new Iterable<Character>() {
         @Override
         public Iterator<Character> iterator() {
            return new Iterator<Character>() {
               private int i = 0;

               @Override
               public boolean hasNext() {
                  return this.i < PosixArgs.this.parsed.size();
               }

               public Character next() {
                  if (this.i >= PosixArgs.this.parsed.size()) {
                     throw new NoSuchElementException();
                  } else {
                     PosixArgs.Arg a = PosixArgs.this.parsed.get(this.i++);
                     PosixArgs.this.arg = a.arg;
                     return a.ch;
                  }
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   private static class Arg {
      private char ch;
      private String arg;

      private Arg(char ch, String arg) {
         this.ch = ch;
         this.arg = arg;
      }
   }
}
