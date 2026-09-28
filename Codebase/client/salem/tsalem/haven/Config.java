package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.ender.wiki.Wiki;

public class Config {
  public static URI cachebase = geturi("haven.cachebase", "");
  
  public static boolean authcertstrict = Utils.getprop("haven.auth-cert-strict", "off").equals("on");
  
  public static String authuser = Utils.getprop("haven.authuser", null);
  
  public static String authserv = Utils.getprop("haven.authserv", null);
  
  public static String defserv = Utils.getprop("haven.defserv", "127.0.0.1");
  
  public static URL resurl = geturl("haven.resurl", "");
  
  public static URL mapurl = geturl("haven.mapurl", "");
  
  public static URL screenurl = geturl("haven.screenurl", "http://game.salemthegame.com/mt/ss");
  
  public static URL manualurl = geturl("haven.manualurl", "https://salemthegame.wiki");
  
  public static URL storeurl = geturl("haven.storeurl", "");
  
  public static URI storebase = geturi("haven.storebase", "https://game.salemthegame.com/store/client/");
  
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
  
  public static boolean chat_expanded = Utils.getprefb("chat_expanded", false);
  
  public static boolean mainmenu_full = Utils.getprefb("mainmenu_full", false);
  
  public static String currentCharName = "";
  
  public static Map<String, Boolean> AUTOCHOOSE = null;
  
  public static Map<String, Boolean> HIDEGOBS = null;
  
  public static Map<String, Boolean> BUFFNOALERT = null;
  
  public static List<HotkeyList.HotkeyJItem> HOTKEYLIST = null;
  
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
  
  public static boolean big_group_arrow = Utils.getprefb("big_group_arrow", true);
  
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
  
  public static boolean alwaysbright_in = Utils.getprefb("alwaysbright_in", false);
  
  public static boolean alwaysbright_out = Utils.getprefb("alwaysbright_out", false);
  
  public static float brightang_in = Utils.getpreff("brightang_in", 0.0F);
  
  public static float brightang_out = Utils.getpreff("brightang_out", 0.0F);
  
  public static boolean use_old_night_vision = Utils.getprefb("use_old_night_vision", false);
  
  public static float old_night_vision_level = Utils.getpreff("old_night_vision_level", 0.0F);
  
  public static boolean fast_menu = Utils.getprefb("fast_flowers", false);
  
  public static boolean mute_violin = Utils.getprefb("mute_violin", false);
  
  public static boolean mute_xmas_sound = Utils.getprefb("mute_xmas_sound", false);
  
  public static boolean chatlogs = Utils.getprefb("chatlogs", true);
  
  public static final int hotkeynr = 100;
  
  public static ArrayList<Integer> h_char_list = new ArrayList<>();
  
  public static boolean localmm_ridges = Utils.getprefb("localmm_ridges", false);
  
  public static boolean remove_animations = Utils.getprefb("remove_animations", false);
  
  public static boolean borka_radii = Utils.getprefb("borka_radii", false);
  
  public static boolean hide_minimap = Utils.getprefb("hide_minimap", false);
  
  public static boolean hide_tempers = Utils.getprefb("hide_tempers", false);
  
  public static boolean alwayssort = Utils.getprefb("alwayssort", false);
  
  public static boolean pickyalt = Utils.getprefb("pickyalt", false);
  
  public static boolean show_contents_icons = Utils.getprefb("show_contents_icons", true);
  
  public static Map<String, String> contents_icons;
  
  public static boolean limit_transfer_amount = Utils.getprefb("limit_transfer_amounts", true);
  
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
  
  public static boolean weight_wdg_inv_items_nr = Utils.getprefb("weight_wdg_inv_items_nr", true);
  
  public static boolean gobble_meters = Utils.getprefb("gobble_meters", true);
  
  public static final Map<String, String> accounts = new HashMap<>();
  
  public static boolean backpack_locked = Utils.getprefb("backpack_locked", true);
  
  public static boolean singleItemCTRLChoose = Utils.getprefb("singleItemCTRLChoose", true);
  
  public static boolean activateCraftAmount = Utils.getprefb("activate_craft_amount", false);
  
  public static boolean hideSomeGobs = Utils.getprefb("hide_some_gobs", false);
  
  public static String auto_activate_movement_mode = Utils.getpref("auto_activate_movement_mode", "run");
  
  public static int buff_alert_activation_value = Utils.getprefi("buff_alert_activation_value", 10);
  
  public static boolean buff_alert_show_percent = Utils.getprefb("buff_alert_show_percent", true);
  
  public static boolean buff_alert_activate = Utils.getprefb("buff_alert_activate", true);
  
  public static boolean buff_alert_sound_activate = Utils.getprefb("buff_alert_sound_activate", true);
  
  public static boolean buff_alert_show_pie_chart_duration = Utils.getprefb("buff_alert_show_pie_chart_duration", true);
  
  public static boolean buff_alert_show_line_duration = Utils.getprefb("buff_alert_show_line_duration", true);
  
  public static String buff_alert_sound_resource = Utils.getpref("buff_alert_sound_resource", "/res/sfx/invobjs/gemshard");
  
  public static int render_distance_int_value = Utils.getprefi("render_distance_int_value", 20);
  
  public static boolean render_distance_bool_value = Utils.getprefb("render_distance_bool_value", false);
  
  public static boolean auto_door_on = Utils.getprefb("auto_door_on", true);
  
  public static boolean chat_online_colour = Utils.getprefb("chat_online_colour", true);
  
  public static boolean auto_unkin_red = Utils.getprefb("auto_unkin_red", false);
  
  public static int fps_display_offset_y = Utils.getprefi("fps_display_offset_y", 50);
  
  public static int fps_display_offset_x = Utils.getprefi("fps_display_offset_x", 0);
  
  public static boolean fps_display_show = Utils.getprefb("fps_display_show", true);
  
  public static boolean fps_display_only_fps = Utils.getprefb("fps_display_only_fps", true);
  
  public static boolean buff_alert_skip_fnf = Utils.getprefb("buff_alert_skip_fnf", true);
  
  public static boolean buff_alert_skip_crime = Utils.getprefb("buff_alert_skip_crime", true);
  
  public static boolean buff_alert_remember_muting = Utils.getprefb("buff_alert_remember_muting", true);
  
  public static boolean buff_alert_bigger_percentage = Utils.getprefb("buff_alert_bigger_percentage", false);
  
  public static boolean mcache_no_flav = Utils.getprefb("mcache_no_flav", false);
  
  public static boolean mview_dist_small = Utils.getprefb("mview_dist_small", false);
  
  public static boolean two_chat_columns = Utils.getprefb("two_chat_columns", true);
  
  public static boolean floating_text_to_console = Utils.getprefb("floating_text_to_console", true);
  
  public static boolean floating_ttc_add_target_name = Utils.getprefb("floating_ttc_add_target_name", true);
  
  public static boolean mute_system_chat = Utils.getprefb("mute_system_chat", false);
  
  public static boolean mute_log_chat = Utils.getprefb("mute_log_chat", false);
  
  public static boolean ft_filter_fertilizers = Utils.getprefb("ft_filter_fertilizers", false);
  
  public static boolean auto_sync_timers = Utils.getprefb("auto_sync_timers", true);
  
  public static boolean shift_invert_option_checkbox = Utils.getprefb("shift_invert_option_checkbox", false);
  
  public static boolean newbie_protection = Utils.getprefb("newbie_protection", true);
  
  public static boolean newbie_prot_hide_info = Utils.getprefb("newbie_prot_hide_info", false);
  
  public static int custom_fps_target = Utils.getprefi("custom_fps_target", 50);
  
  public static boolean new_numbers_on_item_icons = Utils.getprefb("new_numbers_on_item_icons", true);
  
  public static boolean hitbox_on = Utils.getprefb("hitbox_on", false);
  
  public static boolean custom_shift_itemact = Utils.getprefb("custom_shift_itemact", false);
  
  public static boolean auto_backpack_bucket = Utils.getprefb("auto_backpack_bucket", true);
  
  public static boolean mute_all_audio = Utils.getprefb("mute_all_audio", false);
  
  public static double volume_audio_saved = Utils.getpreff("volume_audio_saved", 100.0F);
  
  public static double volume_music_saved = Utils.getpreff("volume_music_saved", 100.0F);
  
  public static boolean sort_only_main_inv = Utils.getprefb("sort_only_main_inv", false);
  
  protected static boolean invert_mouse_cam_y = Utils.getprefb("invert_mouse_cam_y", false);
  
  protected static boolean invert_mouse_cam_x = Utils.getprefb("invert_mouse_cam_x", false);
  
  protected static boolean show_discord_on_login = Utils.getprefb("show_discord_on_login", true);
  
  protected static boolean show_patch_notes_always = Utils.getprefb("show_patch_notes_always", false);
  
  public static boolean cache_project = Utils.getprefb("cache_project", false);
  
  public static boolean noloading = Utils.getprefb("noloading", true);
  
  protected static boolean hide_tooltip_on_inv_x = Utils.getprefb("hide_tooltip_on_inv_x", false);
  
  public static long bgm_ts = Utils.getprefl("bgm_ts", 0L);
  
  public static boolean domestic_animal_stats_to_log_chat = Utils.getprefb("domestic_animal_stats_to_log_chat", false);
  
  public static int domestic_animals_stats_offset = Utils.getprefi("domestic_animals_stats_offset", 0);
  
  public static boolean tt_off = Utils.getprefb("tt_off", false);
  
  public static boolean season_change_message_off = Utils.getprefb("season_change_message_off", false);
  
  public static int static_flat_grid_size = Utils.getprefi("static_flat_grid_size", 30);
  
  public static int last_season_code = Utils.getprefi("last_season_code", -1);
  
  public static boolean animal_stat_tranquility = Utils.getprefb("animal_stat_tranquility", true);
  
  public static boolean animal_stat_immunity = Utils.getprefb("animal_stat_immunity", true);
  
  public static boolean animal_stat_metabolism = Utils.getprefb("animal_stat_metabolism", true);
  
  public static boolean animal_stat_size = Utils.getprefb("animal_stat_size", true);
  
  public static boolean animal_stat_productivity = Utils.getprefb("animal_stat_productivity", true);
  
  public static boolean animal_stat_longevity = Utils.getprefb("animal_stat_longevity", true);
  
  public static int npc_colour1 = Utils.getprefi("npc_colour1", 102);
  
  public static int npc_colour2 = Utils.getprefi("npc_colour2", 0);
  
  public static int npc_colour3 = Utils.getprefi("npc_colour3", 102);
  
  public static int thornbush_colour = Utils.getprefi("thornbush_colour", 65280);
  
  public static Color thornbushColour = new Color(thornbush_colour);
  
  public static boolean ring_on_thornbush = Utils.getprefb("ring_on_thornbush", true);
  
  public static boolean smart_space_on_click = Utils.getprefb("smart_space_on_click", true);
  
  public static boolean auto_recipe_on_gob_click = Utils.getprefb("auto_recipe_on_gob_click", true);
  
  public static boolean highlight_claimed_leantos = Utils.getprefb("highlight_claimed_leantos", true);
  
  static {
    String p;
    if ((p = Utils.getprop("haven.authck", null)) != null)
      authck = Utils.hex2byte(p); 
    File f = new File(userhome);
    if (!f.exists())
      f.mkdirs(); 
    loadBuildVersion();
    loadContentsIcons();
    window_props = loadProps("windows.conf");
    loadItemRadius();
    loadAutochoose();
    loadHideGobs();
    loadBuffNoAlert();
    Wiki.init(getFile("cache"), 3);
    loadGobPathCfg();
    loadAccounts();
  }
  
  private static void loadAccounts() {
    String json = loadFile("accounts.json");
    if (json != null)
      try {
        Gson gson = (new GsonBuilder()).create();
        Type collectionType = (new TypeToken<HashMap<String, String>>() {
          
          }).getType();
        Map<String, String> tmp = (Map<String, String>)gson.fromJson(json, collectionType);
        accounts.putAll(tmp);
      } catch (Exception exception) {} 
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
      Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
      saveFile("accounts.json", gson.toJson(accounts));
    } 
  }
  
  private static void loadAutochoose() {
    String json = loadFile("autochoose.json");
    if (json != null)
      try {
        Gson gson = (new GsonBuilder()).create();
        Type collectionType = (new TypeToken<HashMap<String, Boolean>>() {
          
          }).getType();
        AUTOCHOOSE = (Map<String, Boolean>)gson.fromJson(json, collectionType);
      } catch (Exception exception) {} 
    if (AUTOCHOOSE == null) {
      AUTOCHOOSE = new HashMap<>();
      AUTOCHOOSE.put("Pick", Boolean.valueOf(false));
      AUTOCHOOSE.put("Open", Boolean.valueOf(false));
    } 
  }
  
  private static void loadHideGobs() {
    String json = loadFile("hidegobs.json");
    if (json != null)
      try {
        Gson gson = (new GsonBuilder()).create();
        Type collectionType = (new TypeToken<HashMap<String, Boolean>>() {
          
          }).getType();
        HIDEGOBS = (Map<String, Boolean>)gson.fromJson(json, collectionType);
      } catch (Exception exception) {} 
    if (HIDEGOBS == null) {
      HIDEGOBS = new HashMap<>();
      HIDEGOBS.put("field", Boolean.valueOf(false));
      HIDEGOBS.put("tree", Boolean.valueOf(false));
    } 
  }
  
  public static void loadHotkeyList() {
    Map<Integer, HotkeyList.HotkeyJItem> hotkeyItemMap = null;
    try {
      Gson gson = (new GsonBuilder()).create();
      InputStream is = new FileInputStream(new File(userhome, "hotkeylist.json"));
      hotkeyItemMap = (Map<Integer, HotkeyList.HotkeyJItem>)gson.fromJson(Utils.stream2str(is), (new TypeToken<Map<Integer, HotkeyList.HotkeyJItem>>() {
          
          }).getType());
    } catch (Exception exception) {}
    if (hotkeyItemMap == null) {
      HOTKEYLIST = new ArrayList<>();
    } else {
      ArrayList<HotkeyList.HotkeyJItem> hkjiList = new ArrayList<>();
      for (Map.Entry<Integer, HotkeyList.HotkeyJItem> e : hotkeyItemMap.entrySet())
        hkjiList.add(e.getValue()); 
      HOTKEYLIST = hkjiList;
    } 
    ArrayList<Integer> cList = new ArrayList<>();
    for (HotkeyList.HotkeyJItem hkji : HOTKEYLIST) {
      if (!hkji.onOff)
        continue; 
      String key = hkji.key;
      if (key != null && key.length() == 1 && !key.equals(" "))
        cList.add(Integer.valueOf(hkji.key.charAt(0))); 
    } 
    h_char_list = cList;
  }
  
  private static void loadBuffNoAlert() {
    String json = loadFile("buffnoalert.json");
    if (json != null)
      try {
        Gson gson = (new GsonBuilder()).create();
        Type collectionType = (new TypeToken<HashMap<String, Boolean>>() {
          
          }).getType();
        BUFFNOALERT = (Map<String, Boolean>)gson.fromJson(json, collectionType);
      } catch (Exception exception) {} 
    if (BUFFNOALERT == null) {
      BUFFNOALERT = new HashMap<>();
      BUFFNOALERT.put("Full and fed up", Boolean.valueOf(false));
      BUFFNOALERT.put("Crime!", Boolean.valueOf(false));
    } 
  }
  
  public static void saveAutochoose() {
    synchronized (AUTOCHOOSE) {
      Gson gson = (new GsonBuilder()).create();
      saveFile("autochoose.json", gson.toJson(AUTOCHOOSE));
    } 
  }
  
  public static void saveHideGobs() {
    synchronized (HIDEGOBS) {
      Gson gson = (new GsonBuilder()).create();
      saveFile("hidegobs.json", gson.toJson(HIDEGOBS));
    } 
  }
  
  public static void saveHotkeyListAndUpdateUI() {
    saveHotkeyListButNoUpdateUI();
    loadHotkeyList();
    HotkeyList.instance.update();
    OptWnd2.refresh();
  }
  
  public static void saveHotkeyListButNoUpdateUI() {
    synchronized (HOTKEYLIST) {
      Map<Object, Object> hkmap = new HashMap<>();
      for (HotkeyList.HotkeyJItem hotkeyJItem : HOTKEYLIST)
        hkmap.put(hotkeyJItem.id, hotkeyJItem); 
      Gson gson = (new GsonBuilder()).create();
      saveFile("hotkeylist.json", gson.toJson(hkmap));
    } 
  }
  
  public static void saveBuffNoAlert() {
    synchronized (BUFFNOALERT) {
      Gson gson = (new GsonBuilder()).create();
      saveFile("buffnoalert.json", gson.toJson(BUFFNOALERT));
    } 
  }
  
  private static void loadGobPathCfg() {
    String json = loadFile("gob_path.json");
    if (json != null)
      try {
        Gson gson = GobPath.Cfg.getGson();
        Type collectionType = (new TypeToken<HashMap<String, GobPath.Cfg>>() {
          
          }).getType();
        gobPathCfg = (Map<String, GobPath.Cfg>)gson.fromJson(json, collectionType);
      } catch (Exception e) {
        gobPathCfg = new HashMap<>();
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
          for (Map.Entry<Object, Object> e : info.entrySet())
            buildinfo.put((String)e.getKey(), e.getValue()); 
        } 
      } finally {
        if (in != null)
          in.close(); 
      } 
    } catch (IOException e) {
      throw new Error(e);
    } 
    version = (String)buildinfo.get("git-rev");
    loadOptions();
    window_props = loadProps("windows.conf");
    Wiki.init(getFile("cache"), 3);
    String[] defhotkeys = new String[100];
    String[] defcommands = new String[100];
    boolean[] def_ctrl = new boolean[100];
    boolean[] def_shift = new boolean[100];
    boolean[] def_alt = new boolean[100];
    int di = 0;
    defhotkeys[di] = "A";
    defcommands[di] = "act lo cs";
    def_ctrl[di] = true;
    def_shift[di] = true;
    def_alt[di] = false;
    di = 1;
    defhotkeys[di] = "Q";
    defcommands[di] = "act lo";
    def_ctrl[di] = true;
    def_shift[di] = true;
    def_alt[di] = false;
    di = 2;
    defhotkeys[di] = "X";
    defcommands[di] = "x";
    def_ctrl[di] = false;
    def_shift[di] = false;
    def_alt[di] = false;
    for (int i = 2; i < 100; i++) {
      defhotkeys[i] = "";
      defcommands[i] = "";
      def_ctrl[i] = false;
      def_shift[i] = false;
      def_alt[i] = false;
    } 
    loadHotkeyList();
  }
  
  private static void loadContentsIcons() {
    InputStream in = Config.class.getResourceAsStream("/contents_icons.json");
    try {
      try {
        if (in != null) {
          Gson gson = new Gson();
          Type collectionType = (new TypeToken<HashMap<String, String>>() {
            
            }).getType();
          String json = Utils.stream2str(in);
          contents_icons = (Map<String, String>)gson.fromJson(json, collectionType);
        } 
      } catch (JsonSyntaxException jsonSyntaxException) {
      
      } finally {
        if (in != null)
          in.close(); 
      } 
    } catch (IOException e) {
      throw new Error(e);
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
          Type collectionType = (new TypeToken<HashMap<String, ColoredRadius.Cfg>>() {
            
            }).getType();
          String json = Utils.stream2str(in);
          item_radius = (Map<String, ColoredRadius.Cfg>)gson.fromJson(json, collectionType);
        } 
      } catch (JsonSyntaxException jsonSyntaxException) {
      
      } finally {
        if (in != null)
          in.close(); 
      } 
    } catch (IOException e) {
      throw new Error(e);
    } 
    if (item_radius == null)
      item_radius = new HashMap<>(); 
  }
  
  public static void setCharName(String name) {
    currentCharName = name;
    MainFrame.instance.setTitle(name);
  }
  
  private static void loadOptions() {
    options = loadProps("salem.cfg");
    String ver = options.getProperty("version", "");
    isUpdate = (!version.equals(ver) || !getFile("changelog.txt").exists());
    shadows = options.getProperty("shadows", "false").equals("true");
    flight = options.getProperty("flight", "false").equals("true");
    cellshade = options.getProperty("cellshade", "false").equals("true");
    fsaa = options.getProperty("fsaa", "false").equals("true");
    water = options.getProperty("water", "false").equals("true");
    if (isUpdate)
      saveOptions(); 
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
    if (val == null)
      return def; 
    return Integer.parseInt(val);
  }
  
  private static URL forceurl(String val) {
    try {
      return new URL(val);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static URL geturl(String name, String def) {
    String val = Utils.getprop(name, def);
    if (val.equals(""))
      return null; 
    try {
      return new URL(val);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static URI geturi(String name, String def) {
    String val = Utils.getprop(name, def);
    if (val.equals(""))
      return null; 
    try {
      return new URI(val);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static synchronized void setWindowOpt(String key, String value) {
    synchronized (window_props) {
      String prev_val = window_props.getProperty(key);
      if (prev_val != null && prev_val.equals(value))
        return; 
      window_props.setProperty(key, value);
    } 
    saveWindowOpt();
  }
  
  private static Properties loadProps(String name) {
    File f = getFile(name);
    Properties props = new Properties();
    if (!f.exists())
      try {
        f.createNewFile();
      } catch (IOException e) {
        return null;
      }  
    try {
      props.load(new FileInputStream(f));
    } catch (IOException e) {
      System.out.println(e);
    } 
    return props;
  }
  
  private static void saveProps(Properties props, String name, String comments) {
    try {
      props.store(new FileOutputStream(getFile(name)), comments);
    } catch (IOException e) {
      System.out.println(e);
    } 
  }
  
  public static synchronized void setWindowOpt(String key, Boolean value) {
    setWindowOpt(key, value.booleanValue() ? "true" : "false");
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
    for (Iterator<Character> iterator = opt.parsed().iterator(); iterator.hasNext(); ) {
      int p;
      char c = ((Character)iterator.next()).charValue();
      switch (c) {
        case 'h':
          usage(System.out);
          System.exit(0);
        case 'd':
          dbtext = true;
        case 'P':
          profile = true;
        case 'r':
          resdir = opt.arg;
        case 'A':
          p = opt.arg.indexOf(':');
          if (p >= 0) {
            authserv = opt.arg.substring(0, p);
            authport = Integer.parseInt(opt.arg.substring(p + 1));
            continue;
          } 
          authserv = opt.arg;
        case 'U':
          try {
            resurl = new URL(opt.arg);
          } catch (MalformedURLException e) {
            System.err.println(e);
            System.exit(1);
          } 
        case 'u':
          authuser = opt.arg;
        case 'C':
          authck = Utils.hex2byte(opt.arg);
        case 'm':
          authmech = opt.arg;
      } 
    } 
    if (opt.rest.length > 0) {
      int p = opt.rest[0].indexOf(':');
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
      glcfg.fsaa.set(Boolean.valueOf(fsaa));
      glcfg.lshadow.set(Boolean.valueOf(shadows));
      glcfg.flight.set(Boolean.valueOf(flight));
      glcfg.cel.set(Boolean.valueOf(cellshade));
      glcfg.wsurf.set(Boolean.valueOf(water));
    } catch (SettingException settingException) {}
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
      } catch (FileNotFoundException fileNotFoundException) {}
    } else {
      inputStream = Config.class.getResourceAsStream("/" + name);
    } 
    if (inputStream != null)
      try {
        return Utils.stream2str(inputStream);
      } catch (Exception exception) {
        try {
          inputStream.close();
        } catch (IOException iOException) {}
      } finally {
        try {
          inputStream.close();
        } catch (IOException iOException) {}
      }  
    return null;
  }
  
  public static void saveFile(String name, String data) {
    File file = getFile(name);
    boolean exists = file.exists();
    if (!exists)
      try {
        (new File(file.getParent())).mkdirs();
        exists = file.createNewFile();
      } catch (IOException iOException) {} 
    if (exists && file.canWrite()) {
      PrintWriter out = null;
      try {
        out = new PrintWriter(file);
        out.print(data);
      } catch (FileNotFoundException fileNotFoundException) {
      
      } finally {
        if (out != null)
          out.close(); 
      } 
    } 
  }
  
  public static GobPath.Cfg getGobPathCfg(String resname) {
    if (gobPathCfg.containsKey(resname))
      return gobPathCfg.get(resname); 
    return GobPath.Cfg.def;
  }
}
