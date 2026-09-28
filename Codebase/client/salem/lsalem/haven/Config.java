package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import org.ender.wiki.Wiki;

public class Config {
   public static String authuser = Utils.getprop("haven.authuser", null);
   public static String authserv = Utils.getprop("haven.authserv", null);
   public static String defserv = Utils.getprop("haven.defserv", "127.0.0.1");
   public static URL resurl = geturl("haven.resurl", "");
   public static URL mapurl = geturl("haven.mapurl", "");
   public static URL screenurl = geturl("haven.screenurl", "http://game.salemthegame.com/mt/ss");
   public static URL manualurl = geturl("haven.manualurl", "http://www.salemthegame.com/salemj/index");
   public static URL storeurl = geturl("haven.storeurl", "http://login.salemthegame.com/portal/tostore");
   public static URL regurl = geturl("haven.regurl", "http://login.salemthegame.com/beta/nregister");
   public static boolean dbtext = Utils.getprop("haven.dbtext", "off").equals("on");
   public static boolean bounddb = Utils.getprop("haven.bounddb", "off").equals("on");
   public static boolean profile = Utils.getprop("haven.profile", "off").equals("on");
   public static boolean nolocalres = Utils.getprop("haven.nolocalres", "").equals("yesimsure");
   public static boolean fscache = Utils.getprop("haven.fscache", "on").equals("on");
   public static String resdir = Utils.getprop("haven.resdir", null);
   public static boolean nopreload = Utils.getprop("haven.nopreload", "no").equals("yes");
   public static String loadwaited = Utils.getprop("haven.loadwaited", null);
   public static String allused = Utils.getprop("haven.allused", null);
   public static int mainport = getint("haven.mainport", 1870);
   public static int authport = getint("haven.authport", 1871);
   public static String authmech = Utils.getprop("haven.authmech", "native");
   public static boolean softres = Utils.getprop("haven.softres", "on").equals("on");
   public static byte[] authck = null;
   public static String prefspec = "salem";
   public static final String confid = "";
   public static String userhome = System.getProperty("user.home") + "/Salem";
   public static String pluginfolder = System.getProperty("user.home") + "/Salem/plugins";
   public static String version;
   public static boolean show_tempers = Utils.getprefb("show_tempers", false);
   public static boolean store_map = Utils.getprefb("store_map", true);
   public static boolean radar_icons = Utils.getprefb("radar_icons", true);
   public static boolean autoopen_craftwnd = Utils.getprefb("autoopen_craftwnd", false);
   public static boolean translate = Utils.getprefb("translate", false);
   public static boolean headless = false;
   public static boolean chat_expanded = Utils.getprefb("chat_expanded", false);
   public static boolean mainmenu_full = Utils.getprefb("mainmenu_full", false);
   public static String currentCharName = "";
   public static Map<String, Boolean> AUTOCHOOSE = null;
   static Properties window_props;
   public static Properties options;
   private static Map<String, Object> buildinfo = new HashMap<>();
   public static String authserver_name = Utils.getpref("authserver_name", "Providence");
   public static boolean isUpdate;
   public static boolean isShowNames = true;
   public static boolean timestamp = true;
   public static boolean flower_study = Utils.getprefb("flower_study", false);
   public static boolean pure_mult = Utils.getprefb("pure_mult", false);
   public static boolean blink = Utils.getprefb("blink", false);
   public static boolean autolog = Utils.getprefb("autolog", false);
   public static GLSettings glcfg;
   public static String server;
   protected static boolean shadows = false;
   public static boolean flight = false;
   public static boolean cellshade = false;
   protected static boolean fsaa = false;
   protected static boolean water = false;
   public static boolean center = false;
   public static float camera_field_of_view = Utils.getpreff("camera_field_of_view", 0.5F);
   public static boolean skybox = Utils.getprefb("skybox", true);
   public static float brighten = Utils.getpreff("brighten", 0.0F);
   protected static boolean ss_silent = Utils.getprefb("ss_slent", false);
   protected static boolean ss_compress = Utils.getprefb("ss_compress", true);
   protected static boolean ss_ui = Utils.getprefb("ss_ui", false);
   public static boolean hptr = Utils.getprefb("hptr", false);
   public static boolean menugrid_resets = Utils.getprefb("menugrid_resets", false);
   public static boolean showgobpath = Utils.getprefb("showgobpath", true);
   public static boolean fieldfix = Utils.getprefb("fieldfix", true);
   public static int fieldproducescale = (int)Utils.getpreff("fieldproducescale", 1.0F);
   public static boolean alwaysshowpurity = Utils.getprefb("alwaysshowpurity", false);
   public static boolean alphasort = Utils.getprefb("alphasort", false);
   public static boolean reversesort = Utils.getprefb("reversesort", false);
   public static boolean laptopcontrols = Utils.getprefb("laptopcontrols", false);
   public static boolean raidermodetrees = Utils.getprefb("raidermodetrees", false);
   public static boolean raidermodebraziers = Utils.getprefb("raidermodebraziers", false);
   public static boolean farmermodetrees = Utils.getprefb("farmermodetrees", false);
   public static boolean altprosp = Utils.getprefb("altprosp", false);
   public static boolean pclaimv = Utils.getprefb("pclaimv", false);
   public static boolean tclaimv = Utils.getprefb("tclaimv", false);
   public static boolean wclaimv = Utils.getprefb("wclaimv", true);
   public static boolean hpointv = Utils.getprefb("hpointv", false);
   public static boolean alwaystrack = Utils.getprefb("alwaystrack", false);
   public static boolean slowmin = Utils.getprefb("slowmin", false);
   public static boolean watchguard = Utils.getprefb("watchguard", false);
   public static boolean alwaysbright = Utils.getprefb("alwaysbright", false);
   public static float brightang = Utils.getpreff("brightang", 0.0F);
   public static boolean fast_menu = Utils.getprefb("fast_flowers", false);
   public static boolean mute_violin = Utils.getprefb("mute_violin", false);
   public static boolean chatlogs = Utils.getprefb("chatlogs", true);
   public static final int hotkeynr = 6;
   public static final String[] defhotkeys = new String[]{"A", "Q", "", "", "", ""};
   public static final String[] defcommands = new String[]{"act lo cs", "act lo", "", "", "", ""};
   public static String[] hnames = new String[6];
   public static String[] hcommands = new String[6];
   public static boolean localmm_ridges = Utils.getprefb("localmm_ridges", false);
   public static boolean remove_animations = Utils.getprefb("remove_animations", false);
   public static boolean borka_radii = Utils.getprefb("borka_radii", false);
   public static boolean hide_minimap = Utils.getprefb("hide_minimap", false);
   public static boolean hide_tempers = Utils.getprefb("hide_tempers", false);
   public static boolean alwayssort = Utils.getprefb("alwayssort", false);
   public static boolean pickyalt = Utils.getprefb("pickyalt", false);
   public static boolean show_contents_icons = Utils.getprefb("show_contents_icons", false);
   public static Map<String, String> contents_icons;
   public static boolean show_radius = Utils.getprefb("show_radius", false);
   public static Map<String, ColoredRadius.Cfg> item_radius;
   public static boolean autosift = Utils.getprefb("autosift", false);
   public static boolean autobucket = Utils.getprefb("autobucket", false);
   public static boolean gobpath = Utils.getprefb("gobpath", false);
   public static boolean gobpath_color = Utils.getprefb("gobpath_color", true);
   public static Map<String, GobPath.Cfg> gobPathCfg;
   public static boolean isocam_steps = Utils.getprefb("isocam_steps", true);
   public static boolean auto_drop_bats = Utils.getprefb("auto_drop_bats", false);
   public static boolean weight_wdg = Utils.getprefb("weight_wdg", false);
   public static boolean gobble_meters = Utils.getprefb("gobble_meters", true);
   public static final Map<String, String> accounts = new HashMap<>();
   public static boolean singleItemCTRLChoose = Utils.getprefb("singleItemCTRLChoose", true);

   private static void loadAccounts() {
      String json = loadFile("accounts.json");
      if (json != null) {
         try {
            Gson gson = new GsonBuilder().create();
            Type collectionType = (new TypeToken<HashMap<String, String>>() {}).getType();
            Map<String, String> tmp = gson.fromJson(json, collectionType);
            accounts.putAll(tmp);
         } catch (Exception var4) {
         }
      }
   }

   public static void storeAccount(String name, String token) {
      synchronized (accounts) {
         accounts.put(name, token);
      }

      saveAccounts();
   }

   public static void removeAccount(String name) {
      synchronized (accounts) {
         accounts.remove(name);
      }

      saveAccounts();
   }

   public static void saveAccounts() {
      synchronized (accounts) {
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         saveFile("accounts.json", gson.toJson(accounts));
      }
   }

   private static void loadAutochoose() {
      String json = loadFile("autochoose.json");
      if (json != null) {
         try {
            Gson gson = new GsonBuilder().create();
            Type collectionType = (new TypeToken<HashMap<String, Boolean>>() {}).getType();
            AUTOCHOOSE = gson.fromJson(json, collectionType);
         } catch (Exception var3) {
         }
      }

      if (AUTOCHOOSE == null) {
         AUTOCHOOSE = new HashMap<>();
         AUTOCHOOSE.put("Pick", false);
         AUTOCHOOSE.put("Open", false);
      }
   }

   public static void saveAutochoose() {
      synchronized (AUTOCHOOSE) {
         Gson gson = new GsonBuilder().create();
         saveFile("autochoose.json", gson.toJson(AUTOCHOOSE));
      }
   }

   private static void loadGobPathCfg() {
      String json = loadFile("gob_path.json");
      if (json != null) {
         try {
            Gson gson = GobPath.Cfg.getGson();
            Type collectionType = (new TypeToken<HashMap<String, GobPath.Cfg>>() {}).getType();
            gobPathCfg = gson.fromJson(json, collectionType);
         } catch (Exception var3) {
            gobPathCfg = new HashMap<>();
         }
      }
   }

   public static void saveGobPathCfg() {
      Gson gson = GobPath.Cfg.getGson();
      saveFile("gob_path.json", gson.toJson(gobPathCfg));
   }

   private static void loadBuildVersion() {
      InputStream in = Config.class.getResourceAsStream("/buildinfo");

      try {
         try {
            if (in != null) {
               Properties info = new Properties();
               info.load(in);

               for (Entry<Object, Object> e : info.entrySet()) {
                  buildinfo.put((String)e.getKey(), e.getValue());
               }
            }
         } finally {
            if (in != null) {
               in.close();
            }
         }
      } catch (IOException var8) {
         throw new Error(var8);
      }

      version = (String)buildinfo.get("git-rev");
      loadOptions();
      window_props = loadProps("windows.conf");
      Wiki.init(getFile("cache"), 3);

      for (int i = 0; i < 6; i++) {
         String hname = String.format("hotkey%d", i + 1);
         String hcommand = String.format("command%d", i + 1);
         hnames[i] = Utils.getpref(hname, defhotkeys[i]);
         if (hnames[i].length() > 1) {
            hnames[i] = hnames[i].substring(0, 1);
         }

         hcommands[i] = Utils.getpref(hcommand, defcommands[i]);
      }
   }

   public static void toggleRadius() {
      show_radius = !show_radius;
      Utils.setprefb("show_radius", show_radius);
   }

   private static void loadItemRadius() {
      InputStream in = Config.class.getResourceAsStream("/item_radius.json");

      try {
         try {
            if (in != null) {
               Gson gson = new Gson();
               Type collectionType = (new TypeToken<HashMap<String, ColoredRadius.Cfg>>() {}).getType();
               String json = Utils.stream2str(in);
               item_radius = gson.fromJson(json, collectionType);
            }
         } catch (JsonSyntaxException var8) {
         } finally {
            if (in != null) {
               in.close();
            }
         }
      } catch (IOException var10) {
         throw new Error(var10);
      }

      if (item_radius == null) {
         item_radius = new HashMap<>();
      }
   }

   public static void setCharName(String name) {
      currentCharName = name;
      MainFrame.instance.setTitle(name);
   }

   private static void loadOptions() {
      options = loadProps("salem.cfg");
      String ver = options.getProperty("version", "");
      isUpdate = !version.equals(ver) || !getFile("changelog.txt").exists();
      shadows = options.getProperty("shadows", "false").equals("true");
      flight = options.getProperty("flight", "false").equals("true");
      cellshade = options.getProperty("cellshade", "false").equals("true");
      fsaa = options.getProperty("fsaa", "false").equals("true");
      water = options.getProperty("water", "false").equals("true");
      if (isUpdate) {
         saveOptions();
      }
   }

   public static void saveOptions() {
      synchronized (options) {
         options.setProperty("version", version);
         options.setProperty("shadows", shadows ? "true" : "false");
         options.setProperty("flight", flight ? "true" : "false");
         options.setProperty("cellshade", cellshade ? "true" : "false");
         options.setProperty("fsaa", fsaa ? "true" : "false");
         options.setProperty("water", water ? "true" : "false");
         saveProps(options, "salem.cfg", "Salem config file");
      }
   }

   public static File getFile(String name) {
      return new File(userhome, name);
   }

   public static File getFile() {
      return new File(userhome);
   }

   private static int getint(String name, int def) {
      String val = Utils.getprop(name, null);
      return val == null ? def : Integer.parseInt(val);
   }

   private static URL forceurl(String val) {
      try {
         return new URL(val);
      } catch (MalformedURLException var2) {
         throw new RuntimeException(var2);
      }
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

   public static synchronized void setWindowOpt(String key, String value) {
      synchronized (window_props) {
         String prev_val = window_props.getProperty(key);
         if (prev_val != null && prev_val.equals(value)) {
            return;
         }

         window_props.setProperty(key, value);
      }

      saveWindowOpt();
   }

   private static Properties loadProps(String name) {
      File f = getFile(name);
      Properties props = new Properties();
      if (!f.exists()) {
         try {
            f.createNewFile();
         } catch (IOException var5) {
            return null;
         }
      }

      try {
         props.load(new FileInputStream(f));
      } catch (IOException var4) {
         System.out.println(var4);
      }

      return props;
   }

   private static void saveProps(Properties props, String name, String comments) {
      try {
         props.store(new FileOutputStream(getFile(name)), comments);
      } catch (IOException var4) {
         System.out.println(var4);
      }
   }

   public static synchronized void setWindowOpt(String key, Boolean value) {
      setWindowOpt(key, value ? "true" : "false");
   }

   public static void saveWindowOpt() {
      synchronized (window_props) {
         saveProps(window_props, "windows.conf", "Window config options");
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

   public static void setglpref(GLSettings pref) {
      glcfg = pref;

      try {
         glcfg.fsaa.set(fsaa);
         glcfg.lshadow.set(shadows);
         glcfg.flight.set(flight);
         glcfg.cel.set(cellshade);
         glcfg.wsurf.set(water);
      } catch (GLSettings.SettingException var2) {
      }
   }

   public static void setBrighten(float val) {
      brighten = val;
      Utils.setpreff("brighten", val);
   }

   public static void setFieldproducescale(int val) {
      fieldproducescale = val;
      Utils.setpreff("fieldproducescale", val);
   }

   public static String loadFile(String name) {
      InputStream inputStream = null;
      File file = getFile(name);
      if (file.exists() && file.canRead()) {
         try {
            inputStream = new FileInputStream(file);
         } catch (FileNotFoundException var14) {
         }
      } else {
         inputStream = Config.class.getResourceAsStream("/" + name);
      }

      if (inputStream != null) {
         String var3;
         try {
            var3 = Utils.stream2str(inputStream);
         } catch (Exception var15) {
            return null;
         } finally {
            try {
               inputStream.close();
            } catch (IOException var13) {
            }
         }

         return var3;
      } else {
         return null;
      }
   }

   public static void saveFile(String name, String data) {
      File file = getFile(name);
      boolean exists = file.exists();
      if (!exists) {
         try {
            new File(file.getParent()).mkdirs();
            exists = file.createNewFile();
         } catch (IOException var11) {
         }
      }

      if (exists && file.canWrite()) {
         PrintWriter out = null;

         try {
            out = new PrintWriter(file);
            out.print(data);
         } catch (FileNotFoundException var10) {
         } finally {
            if (out != null) {
               out.close();
            }
         }
      }
   }

   public static GobPath.Cfg getGobPathCfg(String resname) {
      return gobPathCfg.containsKey(resname) ? gobPathCfg.get(resname) : GobPath.Cfg.def;
   }

   static {
      String p;
      if ((p = Utils.getprop("haven.authck", null)) != null) {
         authck = Utils.hex2byte(p);
      }

      File f = new File(userhome);
      if (!f.exists()) {
         f.mkdirs();
      }

      loadBuildVersion();
      loadOptions();
      window_props = loadProps("windows.conf");
      loadItemRadius();
      loadAutochoose();
      Wiki.init(getFile("cache"), 3);
      loadGobPathCfg();
      loadAccounts();
   }
}
