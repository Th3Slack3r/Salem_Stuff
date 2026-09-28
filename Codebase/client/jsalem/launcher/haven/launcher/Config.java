package haven.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Config {
   public static final int MAJOR_VERSION = 1;
   public static final int MINOR_VERSION = 5;
   public final Collection<Resource> include = new ArrayList<>();
   public final Collection<URI> included = new HashSet<>();
   public final Collection<URI> exts = new HashSet<>();
   public final Collection<CommandHandler> mods = new ArrayList<>();
   public Launcher launcher = new JavaLauncher();

   public static int iparcmp(String a, String b) {
      int x;
      try {
         x = Integer.parseInt(a);
      } catch (NumberFormatException var6) {
         x = Integer.MIN_VALUE;
      }

      int y;
      try {
         y = Integer.parseInt(b);
      } catch (NumberFormatException var5) {
         y = Integer.MIN_VALUE;
      }

      return x < y ? -1 : (x > y ? 1 : 0);
   }

   public static List<?> verparse(String ver) {
      List<Object> ret = new ArrayList<>();
      int p = 0;

      while (p < ver.length()) {
         char c = ver.charAt(p++);
         if (c >= '0' && c <= '9') {
            int n = c - '0';

            while (p < ver.length()) {
               c = ver.charAt(p);
               if (c < '0' || c > '9') {
                  break;
               }

               p++;
               n = n * 10 + (c - '0');
            }

            ret.add(n);
         } else {
            StringBuilder buf = new StringBuilder();
            buf.append(c);

            while (true) {
               if (p < ver.length()) {
                  c = ver.charAt(p);
                  if (c < '0' || c > '9') {
                     p++;
                     buf.append(c);
                     continue;
                  }
               }

               ret.add(buf.toString());
               break;
            }
         }
      }

      return ret;
   }

   public static int vparcmp(String a, String b) {
      List<?> x = verparse(a);
      List<?> y = verparse(b);
      int p = 0;

      while (p < x.size() && p < y.size()) {
         Object j = x.get(p);
         Object k = y.get(p);
         p++;
         if (j instanceof Integer && k instanceof String) {
            return -1;
         }

         if (j instanceof String && k instanceof Integer) {
            return 1;
         }

         if (j instanceof Integer && k instanceof Integer) {
            int c = ((Integer)j).compareTo((Integer)k);
            if (c != 0) {
               return c;
            }
         } else if (j instanceof String && k instanceof String) {
            int c = ((String)j).compareTo((String)k);
            if (c != 0) {
               return c;
            }
         }
      }

      if (x.size() > p) {
         return 1;
      } else {
         return y.size() > p ? -1 : 0;
      }
   }

   public static String expand(String s, Config.Environment env) {
      StringBuilder buf = new StringBuilder();
      int p = 0;

      while (p < s.length()) {
         char c = s.charAt(p++);
         if (c == '$') {
            if (p >= s.length()) {
               throw new RuntimeException("unexpected expansion at end-of-line: " + s);
            }

            char x = s.charAt(p++);
            if (x == '$') {
               buf.append('$');
            } else {
               if (x != '{') {
                  throw new RuntimeException("unknown expansion `" + x + "': " + s);
               }

               int p2 = s.indexOf(125, p);
               if (p2 < 0) {
                  throw new RuntimeException("unterminated parameter expansion: " + s);
               }

               String par = s.substring(p, p2);
               p = p2 + 1;
               if (par.startsWith("p:")) {
                  buf.append(System.getProperty(par.substring(2), ""));
               } else {
                  buf.append(env.par.getOrDefault(par, ""));
               }
            }
         } else {
            buf.append(c);
         }
      }

      return buf.toString();
   }

   private void when(String[] words, Config.Environment env) {
      int a = 1;

      while (a < words.length) {
         String w = words[a++];
         if (w.equals(":")) {
            this.command(Arrays.copyOfRange(words, a, words.length), env);
            return;
         }

         if (w.equals("!")) {
            if (a >= words.length) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (!expand(words[a++], env).equals("")) {
               return;
            }
         } else if (w.equals("==")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (!expand(words[a++], env).equals(expand(words[a++], env))) {
               return;
            }
         } else if (w.equals("!=")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (expand(words[a++], env).equals(expand(words[a++], env))) {
               return;
            }
         } else if (w.startsWith("~=")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            int fl = 0;
            if (w.indexOf(105) >= 0) {
               fl |= 2;
            }

            if (!Pattern.compile(words[a++], fl).matcher(expand(words[a++], env)).matches()) {
               return;
            }
         } else if (w.equals(">")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (iparcmp(expand(words[a++], env), expand(words[a++], env)) <= 0) {
               return;
            }
         } else if (w.equals(">=")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (iparcmp(expand(words[a++], env), expand(words[a++], env)) < 0) {
               return;
            }
         } else if (w.equals("<")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (iparcmp(expand(words[a++], env), expand(words[a++], env)) >= 0) {
               return;
            }
         } else if (w.equals("<=")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (iparcmp(expand(words[a++], env), expand(words[a++], env)) > 0) {
               return;
            }
         } else if (w.equals(".>")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (vparcmp(expand(words[a++], env), expand(words[a++], env)) <= 0) {
               return;
            }
         } else if (w.equals(".>=")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (vparcmp(expand(words[a++], env), expand(words[a++], env)) < 0) {
               return;
            }
         } else if (w.equals(".<")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (vparcmp(expand(words[a++], env), expand(words[a++], env)) >= 0) {
               return;
            }
         } else if (w.equals(".<=")) {
            if (a >= words.length - 1) {
               throw new RuntimeException("unexpected `when' operator at end-of-line: " + Arrays.<String>asList(words));
            }

            if (vparcmp(expand(words[a++], env), expand(words[a++], env)) > 0) {
               return;
            }
         } else if (expand(w, env).equals("")) {
            return;
         }
      }

      throw new RuntimeException("unterminated `when' stanza: " + Arrays.<String>asList(words));
   }

   public void add(CommandHandler mod) {
      this.mods.add(mod);
   }

   public void command(String[] words, Config.Environment env) {
      if (words != null && words.length >= 1) {
         for (CommandHandler mod : this.mods) {
            if (mod.command(words, this, env)) {
               return;
            }
         }

         if (!Status.current().command(words, this, env)) {
            if (!this.launcher.command(words, this, env)) {
               String var14 = words[0];
               switch (var14) {
                  case "require":
                     if (words.length < 2) {
                        throw new RuntimeException("usage: require MAJOR.MINOR");
                     }

                     int maj;
                     int min;
                     try {
                        int p = words[1].indexOf(46);
                        if (p < 0) {
                           throw new RuntimeException("usage: require MAJOR.MINOR");
                        }

                        maj = Integer.parseInt(words[1].substring(0, p));
                        min = Integer.parseInt(words[1].substring(p + 1));
                     } catch (NumberFormatException var13) {
                        throw new RuntimeException("usage: require MAJOR.MINOR", var13);
                     }

                     if (maj != MAJOR_VERSION || min > MINOR_VERSION) {
                        throw new Config.InvalidVersionException(maj + "." + min);
                     }
                     break;
                  case "error":
                     if (words.length < 2) {
                        throw new RuntimeException("usage: error MESSAGE");
                     }

                     throw new Config.UserError(words[1]);
                  case "rel":
                     if (words.length < 2) {
                        throw new RuntimeException("usage: rel URI");
                     }

                     try {
                        env.rel(new URI(expand(words[1], env)));
                        break;
                     } catch (URISyntaxException var11) {
                        throw new RuntimeException("usage: rel URL", var11);
                     }
                  case "validate":
                     if (words.length < 2) {
                        throw new RuntimeException("usage: validate VALIDATOR...");
                     }

                     Collection<Validator> nval = new ArrayList<>();

                     for (int i = 1; i < words.length; i++) {
                        Validator v = Validator.parse(expand(words[i], env));
                        if (v != null) {
                           nval.add(v);
                        }
                     }

                     env.val = nval;
                     break;
                  case "include":
                     if (words.length < 2) {
                        throw new RuntimeException("usage: include URL");
                     }

                     try {
                        this.include.add(new Resource(env.rel.resolve(new URI(expand(words[1], env))), env.val).referrer(env.src));
                        break;
                     } catch (URISyntaxException var10) {
                        throw new RuntimeException("usage: include URL", var10);
                     }
                  case "extension":
                     if (words.length < 2) {
                        throw new RuntimeException("usage: extension URL");
                     }

                     URI uri;
                     try {
                        uri = env.rel.resolve(new URI(expand(words[1], env)));
                     } catch (URISyntaxException var9) {
                        throw new RuntimeException("usage: extension URL", var9);
                     }

                     if (!this.exts.contains(uri)) {
                        try {
                           for (Extension ext : Extension.load(new Resource(uri, env.val).referrer(env.src))) {
                              ext.init(this);
                           }
                        } catch (IOException var12) {
                           throw new RuntimeException("could not load extension: " + String.valueOf(uri), var12);
                        }

                        this.exts.add(uri);
                     }
                     break;
                  case "set":
                     if (words.length < 3) {
                        throw new RuntimeException("usage: set VARIABLE VALUE");
                     }

                     Map<String, String> par = new HashMap<>(env.par);
                     par.put(expand(words[1], env), expand(words[2], env));
                     env.par(par);
                     break;
                  case "when":
                     this.when(words, env);
                     break;
                  case "chain":
                     if (words.length < 2) {
                        throw new RuntimeException("usage: chain URL");
                     }

                     try {
                        this.launcher = new ChainLauncher(new Resource(env.rel.resolve(new URI(expand(words[1], env))), env.val).referrer(env.src));
                     } catch (URISyntaxException var8) {
                        throw new RuntimeException("usage: chain URL", var8);
                     }
               }
            }
         }
      }
   }

   public void read(Reader in, Config.Environment env) throws IOException {
      BufferedReader fp = new BufferedReader(in);

      for (String ln = fp.readLine(); ln != null; ln = fp.readLine()) {
         if (ln.length() <= 0 || ln.charAt(0) != '#') {
            this.command(Utils.splitwords(ln), env);
         }
      }
   }

   public static class Environment {
      public static final URI opaque = URI.create("urn:nothing");
      public Collection<Validator> val = Collections.emptyList();
      public Map<String, String> par = Collections.emptyMap();
      public URI rel = opaque;
      public URI src = null;

      public Config.Environment val(Collection<Validator> val) {
         this.val = val;
         return this;
      }

      public Config.Environment par(Map<String, String> par) {
         this.par = par;
         return this;
      }

      public Config.Environment rel(URI rel) {
         this.rel = rel;
         return this;
      }

      public Config.Environment src(URI src) {
         this.src = src;
         return this;
      }

      public static Config.Environment from(Resource res) {
         return new Config.Environment().val(res.val).rel(res.uri).src(res.uri);
      }
   }

   public static class InvalidVersionException extends RuntimeException implements ErrorMessage {
      public final String required;

      public InvalidVersionException(String required) {
         super(String.format("invalid version of launcher; launch file requires %s, this is %d.%d", required, Config.MAJOR_VERSION, Config.MINOR_VERSION));
         this.required = required;
      }

      @Override
      public String usermessage() {
         return String.format(
            "This launcher is outdated; please download the latest version from where you got it. The launch file requires version %s, whereas this launcher is version %d.%d.",
            this.required,
            Config.MAJOR_VERSION,
            Config.MINOR_VERSION
         );
      }
   }

   public static class UserError extends RuntimeException implements ErrorMessage {
      public UserError(String message) {
         super(message);
      }

      @Override
      public String usermessage() {
         return this.getMessage();
      }
   }
}
