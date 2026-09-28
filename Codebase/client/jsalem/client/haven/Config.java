package haven;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Config {
   public static String authuser = Utils.getprop("haven.authuser", null);
   public static String authserv = Utils.getprop("haven.authserv", null);
   public static String defserv = Utils.getprop("haven.defserv", "127.0.0.1");
   public static URL resurl = geturl("haven.resurl", "");
   public static URL mapurl = geturl("haven.mapurl", "");
   public static URI cachebase = geturi("haven.cachebase", "");
   public static URL screenurl = geturl("haven.screenurl", "");
   public static URL manualurl = geturl("haven.manualurl", "https://salemthegame.wiki");
   public static URL storeurl = geturl("haven.storeurl", "");
   public static URI storebase = geturi("haven.storebase", "");
   public static URL regurl = geturl("haven.regurl", "");
   public static boolean dbtext = Utils.getprop("haven.dbtext", "off").equals("on");
   public static boolean bounddb = Utils.getprop("haven.bounddb", "off").equals("on");
   public static boolean profile = Utils.getprop("haven.profile", "off").equals("on");
   public static boolean nolocalres = Utils.getprop("haven.nolocalres", "").equals("yesimsure");
   public static boolean fscache = Utils.getprop("haven.fscache", "on").equals("on");
   public static boolean authcertstrict = Utils.getprop("haven.auth-cert-strict", "off").equals("on");
   public static String resdir = Utils.getprop("haven.resdir", null);
   public static boolean autosift = false;
   public static boolean headless = false;
   public static final Map<String, Boolean> AUTOCHOOSE = new HashMap<>();
   static {
      // Default: auto-select "Open" from flower menus (Tsalen/Lsalem hacks menu behavior)
      AUTOCHOOSE.put("Open", true);
   }
   static {
      loadAutoChoose();
   }
   
   private static void loadAutoChoose() {
      String json = loadFile("autochoose.json");
      if (json != null) {
         try {
            AUTOCHOOSE = (Map<String, Boolean>) gson.fromJson(json, new TypeToken<Map<String, Boolean>>(){}.getType());
         } catch (Exception e) {
            AUTOCHOOSE = new HashMap<>();
         }
      }
      if (AUTOCHOOSE == null) AUTOCHOOSE = new HashMap<>();
   }
   
   private static void saveAutoChoose() {
      saveFile("autochoose.json", gson.toJson(AUTOCHOOSE));
   }
   public static boolean nopreload = Utils.getprop("haven.nopreload", "no").equals("yes");
   public static String loadwaited = Utils.getprop("haven.loadwaited", null);
   public static String allused = Utils.getprop("haven.allused", null);
   public static int mainport = getint("haven.mainport", 1870);
   public static int authport = getint("haven.authport", 1871);
   public static String authmech = Utils.getprop("haven.authmech", "native");
   public static boolean softres = Utils.getprop("haven.softres", "on").equals("on");
   public static byte[] authck = null;
    public static String prefspec = "salem";
    public static String userhome = System.getProperty("user.home") + "/Salem";
   public static final String confid = "";

   private static int getint(String name, int def) {
      String val = Utils.getprop(name, null);
      return val == null ? def : Integer.parseInt(val);
   }

   private static URL geturl(String name, String def) {
      String val = Utils.getprop(name, def);
      if (val.equals("")) {
         return null;
      } else {
         try {
            return new URL(val);
         } catch (MalformedURLException var4) {
            throw new RuntimeException(var4);
         }
      }
   }

   private static URI geturi(String name, String def) {
      String val = Utils.getprop(name, def);
      if (val.equals("")) {
         return null;
      } else {
         try {
            return new URI(val);
         } catch (URISyntaxException var4) {
            throw new RuntimeException(var4);
         }
      }
   }

   private static void usage(PrintStream out) {
      out.println("usage: haven.jar [OPTIONS] [SERVER[:PORT]]");
      out.println("Options include:");
      out.println("  -h                 Display this help");
      out.println("  -d                 Display debug text");
      out.println("  -P                 Enable profiling");
      out.println("  -U URL             Use specified external resource URL");
      out.println("  -r DIR             Use specified resource directory (or $SALEM_RESDIR)");
      out.println("  -A AUTHSERV[:PORT] Use specified authentication server");
      out.println("  -u USER            Authenticate as USER (together with -C)");
      out.println("  -C HEXCOOKIE       Authenticate with specified hex-encoded cookie");
      out.println("  -m AUTHMECH        Use specified authentication mechanism (`native' or `paradox')");
   }

   public static void cmdline(String[] args) {
      PosixArgs opt = PosixArgs.getopt(args, "hdPU:r:A:u:C:m:");
      if (opt == null) {
         usage(System.err);
         System.exit(1);
      }

      for (char c : opt.parsed()) {
         switch (c) {
            case 'A':
               int p = opt.arg.indexOf(58);
               if (p >= 0) {
                  authserv = opt.arg.substring(0, p);
                  authport = Integer.parseInt(opt.arg.substring(p + 1));
               } else {
                  authserv = opt.arg;
               }
               break;
            case 'C':
               authck = Utils.hex2byte(opt.arg);
               break;
            case 'P':
               profile = true;
               break;
            case 'U':
               try {
                  resurl = new URL(opt.arg);
               } catch (MalformedURLException var6) {
                  System.err.println(var6);
                  System.exit(1);
               }
               break;
            case 'd':
               dbtext = true;
               break;
            case 'h':
               usage(System.out);
               System.exit(0);
               break;
            case 'm':
               authmech = opt.arg;
               break;
            case 'r':
               resdir = opt.arg;
               break;
            case 'u':
               authuser = opt.arg;
         }
      }

      if (opt.rest.length > 0) {
         int p = opt.rest[0].indexOf(58);
         if (p >= 0) {
            defserv = opt.rest[0].substring(0, p);
            mainport = Integer.parseInt(opt.rest[0].substring(p + 1));
         } else {
            defserv = opt.rest[0];
         }
      }
   }

   static {
      String p;
      if ((p = Utils.getprop("haven.authck", null)) != null) {
         authck = Utils.hex2byte(p);
      }
   }
}
