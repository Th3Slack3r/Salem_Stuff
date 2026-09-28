package haven;

import haven.minimap.ConfigGroup;
import haven.minimap.ConfigMarker;
import haven.minimap.MarkerFactory;
import haven.minimap.RadarConfig;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptWnd2 extends Window {
  public static final RichText.Foundry foundry = new RichText.Foundry(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(10) });
  
  public static OptWnd2 instance = null;
  
  private final CheckBox gob_path_color;
  
  private Tabs body;
  
  private Tabs.Tab radartab;
  
  private static RadarConfig rc = null;
  
  private static MarkerFactory mf = null;
  
  protected static String curcam;
  
  private final Map<String, CamInfo> caminfomap = new HashMap<>();
  
  private final Map<String, String> camname2type = new HashMap<>();
  
  private final Comparator<String> camcomp = new Comparator<String>() {
      public int compare(String a, String b) {
        if (a.startsWith("The "))
          a = a.substring(4); 
        if (b.startsWith("The "))
          b = b.substring(4); 
        return a.compareTo(b);
      }
    };
  
  CheckBox opt_shadow;
  
  CheckBox opt_aa;
  
  CheckBox opt_qw;
  
  CheckBox opt_sb;
  
  CheckBox opt_flight;
  
  CheckBox opt_cel;
  
  CheckBox opt_show_tempers;
  
  static CheckBox invShiftCBox = null;
  
  static CheckBox hitBoxCBox = null;
  
  static CheckBox autoBackpackBucketCBox = null;
  
  static CheckBox customShiftItemActCBox = null;
  
  static CheckBox opt_cSort = null;
  
  public static CheckBox aCB = null;
  
  public static CheckBox aCB2 = null;
  
  public static ArrayList<CheckBox> movBoxList = new ArrayList<>();
  
  private static HSlider audio = null;
  
  private static HSlider music = null;
  
  static TextEntry hotkey_Key = null;
  
  static TextEntry hotkey_Com = null;
  
  private static class CamInfo {
    String name;
    
    String desc;
    
    Tabs.Tab args;
    
    public CamInfo(String name, String desc, Tabs.Tab args) {
      this.name = name;
      this.desc = desc;
      this.args = args;
    }
  }
  
  public OptWnd2(Coord c, Widget parent) {
    super(c, new Coord(680, 360), parent, "Options");
    this.justclose = true;
    this.body = new Tabs(Coord.z, new Coord(610, 360), this) {
        public void changed(Tabs.Tab from, Tabs.Tab to) {
          Utils.setpref("optwndtab", to.btn.text.text);
          from.btn.c.y = 0;
          to.btn.c.y = -2;
        }
      };
    this.body.getClass();
    Widget tab = new Tabs.Tab(this.body, new Coord(0, 0), 60, "General");
    new Button(new Coord(0, 30), Integer.valueOf(125), tab, "Quit") {
        public void click() {
          HackThread.tg().interrupt();
        }
      };
    new Button(new Coord(135, 30), Integer.valueOf(125), tab, "Switch character") {
        public void click() {
          this.ui.gui.act(new String[] { "lo", "cs" });
        }
      };
    new Button(new Coord(270, 30), Integer.valueOf(120), tab, "Log out") {
        public void click() {
          this.ui.gui.act(new String[] { "lo" });
        }
      };
    Widget editbox = new Frame(new Coord(270, 60), new Coord(120, 82), tab);
    new Label(new Coord(15, 10), editbox, "Edit mode:");
    RadioGroup editmode = new RadioGroup(editbox) {
        public void changed(int btn, String lbl) {
          Utils.setpref("editmode", lbl.toLowerCase());
        }
      };
    editmode.add("Emacs", new Coord(30, 30));
    editmode.add("PC", new Coord(30, 55));
    if (Utils.getpref("editmode", "pc").equals("emacs")) {
      editmode.check("Emacs");
    } else {
      editmode.check("PC");
    } 
    Widget fontsizebox = new Frame(new Coord(410, 30), new Coord(200, 112), tab);
    RadioGroup fontsizes = new RadioGroup(fontsizebox) {
        public void changed(int btn, String lbl) {
          int basesize = 12;
          if (lbl.equals("Base size 14")) {
            basesize = 14;
          } else if (lbl.equals("Base size 16")) {
            basesize = 16;
          } else if (lbl.equals("Base size 20")) {
            basesize = 20;
          } 
          OptWnd2.this.ui.gui.chat.setbasesize(basesize);
          Utils.setpreff("chatfontsize", basesize);
        }
      };
    new Label(new Coord(15, 10), fontsizebox, "font size:");
    fontsizes.add("Base size 12", new Coord(95, 10));
    fontsizes.add("Base size 14", new Coord(95, 35));
    fontsizes.add("Base size 16", new Coord(95, 60));
    fontsizes.add("Base size 20", new Coord(95, 85));
    int basesize = (int)Utils.getpreff("chatfontsize", 12.0F);
    fontsizes.check("Base size " + basesize);
    int m = 37;
    int n = 0;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Fast flower menus") {
        public void changed(boolean val) {
          super.changed(val);
          Config.fast_menu = val;
          Utils.setprefb("fast_flowers", val);
        }
      }).a = Config.fast_menu;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Enable tracking on log-in") {
        public void changed(boolean val) {
          super.changed(val);
          Config.alwaystrack = val;
          Utils.setprefb("alwaystrack", val);
        }
      }).a = Config.alwaystrack;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Store minimap") {
        public void changed(boolean val) {
          super.changed(val);
          Config.store_map = val;
          Utils.setprefb("store_map", val);
          if (val)
            this.ui.gui.mmap.cgrid = null; 
        }
      }).a = Config.store_map;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Store chat logs") {
        public void changed(boolean val) {
          super.changed(val);
          Config.chatlogs = val;
          Utils.setprefb("chatlogs", val);
        }
      }).a = Config.chatlogs;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Radar icons") {
        public void changed(boolean val) {
          super.changed(val);
          Config.radar_icons = val;
          Utils.setprefb("radar_icons", val);
        }
      }).a = Config.radar_icons;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Show weight widget") {
        public void changed(boolean val) {
          super.changed(val);
          Config.weight_wdg = val;
          Utils.setprefb("weight_wdg", val);
        }
      }).a = Config.weight_wdg;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Auto open craft window") {
        public void changed(boolean val) {
          super.changed(val);
          Config.autoopen_craftwnd = val;
          Utils.setprefb("autoopen_craftwnd", val);
        }
      }).a = Config.autoopen_craftwnd;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Crafting menu resets") {
        public void changed(boolean val) {
          super.changed(val);
          Config.menugrid_resets = val;
          Utils.setprefb("menugrid_resets", val);
        }
      }).a = Config.menugrid_resets;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Translate") {
        public void changed(boolean val) {
          super.changed(val);
          Config.translate = val;
          Utils.setprefb("translate", val);
        }
      }).a = Config.translate;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Combat radii for people") {
        public void changed(boolean val) {
          super.changed(val);
          Config.borka_radii = val;
          Utils.setprefb("borka_radii", val);
        }
      }).a = Config.borka_radii;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Lower framerate on unfocused instances") {
        public void changed(boolean val) {
          super.changed(val);
          Config.slowmin = val;
          Utils.setprefb("slowmin", val);
        }
      }).a = Config.slowmin;
    m += 25;
    new Button(new Coord(n, m), Integer.valueOf(120), tab, "Frame-Rate Config") {
        public void click() {
          FrameRateOptWnd.toggle();
        }
      };
    n = 250;
    m = 137;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Hide the minimap") {
        public void changed(boolean val) {
          super.changed(val);
          Config.hide_minimap = val;
          Utils.setprefb("hide_minimap", val);
          this.ui.gui.updateRenderFilter();
        }
      }).a = Config.hide_minimap;
    m += 25;
    this.opt_show_tempers = new CheckBox(new Coord(n, m), tab, "Always show humor numbers") {
        public void changed(boolean val) {
          super.changed(val);
          Config.show_tempers = val;
          Utils.setprefb("show_tempers", val);
        }
      };
    this.opt_show_tempers.a = Config.show_tempers;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Hide the humours") {
        public void changed(boolean val) {
          super.changed(val);
          Config.hide_tempers = val;
          Utils.setprefb("hide_tempers", val);
          this.ui.gui.updateRenderFilter();
        }
      }).a = Config.hide_tempers;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Include UI on screenshots") {
        public void changed(boolean val) {
          super.changed(val);
          Config.ss_ui = val;
          Utils.setprefb("ss_ui", val);
        }
      }).a = Config.ss_ui;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Take screenshots silently") {
        public void changed(boolean val) {
          super.changed(val);
          Config.ss_silent = val;
          Utils.setprefb("ss_slent", val);
        }
      }).a = Config.ss_silent;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Compress screenshots") {
        public void changed(boolean val) {
          super.changed(val);
          Config.ss_compress = val;
          Utils.setprefb("ss_compress", val);
        }
      }).a = Config.ss_compress;
    n = 450;
    m = 137;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Remove all animations") {
        public void changed(boolean val) {
          super.changed(val);
          Config.remove_animations = val;
          Utils.setprefb("remove_animations", val);
        }
      }).a = Config.remove_animations;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Show Discord Link on Login") {
        public void changed(boolean val) {
          super.changed(val);
          Config.show_discord_on_login = val;
          Utils.setprefb("show_discord_on_login", val);
        }
      }).a = Config.show_discord_on_login;
    m += 25;
    (new CheckBox(new Coord(n, m), tab, "Always show Patch Notes") {
        public void changed(boolean val) {
          super.changed(val);
          Config.show_patch_notes_always = val;
          Utils.setprefb("show_patch_notes_always", val);
        }
      }).a = Config.show_patch_notes_always;
    curcam = Utils.getpref("defcam", "sortho");
    this.body.getClass();
    tab = new Tabs.Tab(this.body, new Coord(70, 0), 120, "Camera & Graphics");
    new Label(new Coord(10, 30), tab, "Camera type:");
    final RichTextBox caminfo = new RichTextBox(new Coord(10, 225), new Coord(210, 135), tab, "", foundry);
    caminfo.bg = new Color(0, 0, 0, 64);
    addinfo("ortho", "Isometric Cam", "Isometric camera centered on character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", (Tabs.Tab)null);
    addinfo("sortho", "Smooth Isometric Cam", "Isometric camera centered on character with smoothed movement. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", (Tabs.Tab)null);
    addinfo("follow", "Follow Cam", "The camera follows the character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", (Tabs.Tab)null);
    addinfo("sfollow", "Smooth Follow Cam", "The camera smoothly follows the character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", (Tabs.Tab)null);
    addinfo("free", "Freestyle Cam", "You can move around freely within the larger area around character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", (Tabs.Tab)null);
    addinfo("sucky", "Smooth Freestyle Cam", "You can move around freely within the larger area around character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", (Tabs.Tab)null);
    addinfo("farout", "Far Out Cam", "A smooth freestyle cam, zoomed out far, top-down, aligned north, perfect for long distance travel.", (Tabs.Tab)null);
    final Tabs cambox = new Tabs(new Coord(100, 60), new Coord(300, 200), tab);
    RadioGroup cameras = new RadioGroup(tab) {
        public void changed(int btn, String lbl) {
          if (OptWnd2.this.camname2type.containsKey(lbl))
            lbl = (String)OptWnd2.this.camname2type.get(lbl); 
          if (!lbl.equals(OptWnd2.curcam))
            OptWnd2.setcamera(lbl); 
          OptWnd2.CamInfo inf = (OptWnd2.CamInfo)OptWnd2.this.caminfomap.get(lbl);
          if (inf == null) {
            cambox.showtab(null);
            caminfo.settext("");
          } else {
            cambox.showtab(inf.args);
            caminfo.settext(String.format("$size[12]{%s}\n\n$col[200,175,150,255]{%s}", new Object[] { inf.name, inf.desc }));
          } 
        }
      };
    List<String> clist = new ArrayList<>();
    for (String camtype : MapView.camtypes.keySet())
      clist.add(this.caminfomap.containsKey(camtype) ? ((CamInfo)this.caminfomap.get(camtype)).name : camtype); 
    Collections.sort(clist, this.camcomp);
    int j = 25;
    for (String camname : clist) {
      j += 25;
      cameras.add(camname, new Coord(10, j));
    } 
    cameras.check(this.caminfomap.containsKey(curcam) ? ((CamInfo)this.caminfomap.get(curcam)).name : curcam);
    new Button(new Coord(100, 30), Integer.valueOf(120), tab, "Cam Config") {
        public void click() {
          CamControlOptWnd.toggle();
        }
      };
    int k = 240;
    j = 15;
    j += 25;
    this.opt_aa = new CheckBox(new Coord(k, j), tab, "Antialiasing") {
        public void set(boolean val) {
          try {
            Config.glcfg.fsaa.set(Boolean.valueOf(val));
          } catch (SettingException e) {
            val = false;
            ((GameUI)getparent(GameUI.class)).error(e.getMessage());
            return;
          } 
          this.a = val;
          Config.fsaa = val;
          Config.glcfg.save();
          Config.saveOptions();
        }
      };
    this.opt_aa.a = Config.fsaa;
    checkVideoOpt(this.opt_aa, Config.glcfg.fsaa);
    j += 25;
    this.opt_qw = new CheckBox(new Coord(k, j), tab, "Quality water") {
        public void set(boolean val) {
          try {
            Config.glcfg.wsurf.set(Boolean.valueOf(val));
          } catch (SettingException e) {
            val = false;
            ((GameUI)getparent(GameUI.class)).error(e.getMessage());
            return;
          } 
          this.a = val;
          Config.water = val;
          Config.glcfg.save();
          Config.saveOptions();
        }
      };
    this.opt_qw.a = Config.water;
    checkVideoOpt(this.opt_qw, Config.glcfg.wsurf, Text.render("If character textures glitch, try turning Per-pixel lighting on."));
    j += 25;
    this.opt_sb = new CheckBox(new Coord(k, j), tab, "Skybox") {
        public void changed(boolean val) {
          super.changed(val);
          Config.skybox = val;
          Utils.setprefb("skybox", val);
        }
      };
    this.opt_sb.a = Config.skybox;
    j = 15;
    k += 120;
    j += 25;
    this.opt_flight = new CheckBox(new Coord(k, j), tab, "Per-pixel lighting") {
        public void set(boolean val) {
          try {
            Config.glcfg.flight.set(Boolean.valueOf(val));
            if (!val) {
              Config.glcfg.flight.set(Boolean.valueOf(false));
              Config.glcfg.cel.set(Boolean.valueOf(false));
              Config.shadows = OptWnd2.this.opt_shadow.a = false;
              Config.cellshade = OptWnd2.this.opt_cel.a = false;
            } 
          } catch (SettingException e) {
            val = false;
            ((GameUI)getparent(GameUI.class)).error(e.getMessage());
            return;
          } 
          this.a = val;
          Config.flight = val;
          Config.glcfg.save();
          Config.saveOptions();
          OptWnd2.checkVideoOpt(OptWnd2.this.opt_shadow, Config.glcfg.lshadow);
          OptWnd2.checkVideoOpt(OptWnd2.this.opt_cel, Config.glcfg.cel);
        }
      };
    this.opt_flight.a = Config.flight;
    checkVideoOpt(this.opt_flight, Config.glcfg.flight, Text.render("Also known as per-fragment lighting"));
    (new CheckBox(new Coord(k + 120, j), tab, "Invert Mouse Cam Y") {
        public void changed(boolean val) {
          super.changed(val);
          Config.invert_mouse_cam_y = val;
          Utils.setprefb("invert_mouse_cam_y", val);
        }
      }).a = Config.invert_mouse_cam_y;
    (new CheckBox(new Coord(k + 120, j + 25), tab, "Invert Mouse Cam X") {
        public void changed(boolean val) {
          super.changed(val);
          Config.invert_mouse_cam_x = val;
          Utils.setprefb("invert_mouse_cam_x", val);
        }
      }).a = Config.invert_mouse_cam_x;
    j += 25;
    this.opt_shadow = new CheckBox(new Coord(k, j), tab, "Shadows") {
        public void set(boolean val) {
          try {
            Config.glcfg.lshadow.set(Boolean.valueOf(val));
          } catch (SettingException e) {
            val = false;
            ((GameUI)getparent(GameUI.class)).error(e.getMessage());
            return;
          } 
          this.a = val;
          Config.shadows = val;
          Config.glcfg.save();
          Config.saveOptions();
        }
      };
    this.opt_shadow.a = Config.shadows;
    checkVideoOpt(this.opt_shadow, Config.glcfg.lshadow);
    j += 25;
    this.opt_cel = new CheckBox(new Coord(k, j), tab, "Cel-shading") {
        public void set(boolean val) {
          try {
            Config.glcfg.cel.set(Boolean.valueOf(val));
          } catch (SettingException e) {
            val = false;
            ((GameUI)getparent(GameUI.class)).error(e.getMessage());
            return;
          } 
          this.a = val;
          Config.cellshade = val;
          Config.glcfg.save();
          Config.saveOptions();
        }
      };
    this.opt_cel.a = Config.cellshade;
    checkVideoOpt(this.opt_cel, Config.glcfg.cel);
    k = 240;
    j += 30;
    new Label(new Coord(k, j), tab, "Camera FOV:");
    j += 5;
    new HSlider(new Coord(k + 75, j), 200, tab, 0, 1000, (int)(Config.camera_field_of_view * 1000.0F)) {
        public void changed() {
          Config.camera_field_of_view = this.val / 1000.0F;
          Utils.setpreff("camera_field_of_view", this.val / 1000.0F);
          if (this.ui != null && this.ui.gui != null && this.ui.gui.map.camera != null)
            this.ui.gui.map.camera.resized(); 
        }
      };
    j += 20;
    new Label(new Coord(k, j), tab, "Brightness:");
    j += 5;
    new HSlider(new Coord(k + 75, j), 200, tab, 0, 1000, (int)(Config.brighten * 1000.0F)) {
        public void changed() {
          Config.setBrighten(this.val / 1000.0F);
          this.ui.sess.glob.brighten();
        }
      };
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Steplocked isometric camera") {
        public void changed(boolean val) {
          super.changed(val);
          Config.isocam_steps = val;
          Utils.setprefb("isocam_steps", val);
          if (this.ui.gui != null && this.ui.gui.map != null && this.ui.gui.map.camera != null)
            this.ui.gui.map.camera.fixangle(); 
        }
      }).a = Config.isocam_steps;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Blink radar objects") {
        public void changed(boolean val) {
          super.changed(val);
          Config.blink = val;
          Utils.setprefb("blink", val);
        }
      }).a = Config.blink;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Only show one plant per field") {
        public void changed(boolean val) {
          super.changed(val);
          Config.fieldfix = val;
          Utils.setprefb("fieldfix", val);
        }
      }).a = Config.fieldfix;
    j += 25;
    final Label cropscale = new Label(new Coord(k + 10, j), tab, "Crop scaling: x" + Config.fieldproducescale);
    j += 20;
    new HSlider(new Coord(k + 10, j), 170, tab, 0, 9, Config.fieldproducescale - 1) {
        public void changed() {
          Config.setFieldproducescale(this.val + 1);
          cropscale.settext("Crop scaling: x" + Config.fieldproducescale);
        }
      };
    j += 20;
    (new CheckBox(new Coord(k, j), tab, "Show ridges on the map.") {
        public void changed(boolean val) {
          super.changed(val);
          Config.localmm_ridges = val;
          Utils.setprefb("localmm_ridges", val);
        }
      }).a = Config.localmm_ridges;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Alternate prospecting") {
        public void changed(boolean val) {
          super.changed(val);
          Config.altprosp = val;
          Utils.setprefb("altprosp", val);
        }
      }).a = Config.altprosp;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Arrow home pointer") {
        public void changed(boolean val) {
          super.changed(val);
          Config.hptr = val;
          Utils.setprefb("hptr", val);
          this.ui.gui.mainmenu.pv = (Config.hpointv && !val);
        }
      }).a = Config.hptr;
    (new CheckBox(new Coord(k + 200, j), tab, "BIG Group Arrow") {
        public void changed(boolean val) {
          super.changed(val);
          Config.big_group_arrow = val;
          Utils.setprefb("big_group_arrow", val);
        }
      }).a = Config.big_group_arrow;
    (hitBoxCBox = new CheckBox(new Coord(k + 320, j), tab, "HitBox") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.toggleHitBoxes(Boolean.valueOf(val));
        }
      }).a = Config.hitbox_on;
    k += 200;
    j = 145;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Show gob paths") {
        public void changed(boolean val) {
          super.changed(val);
          Config.showgobpath = val;
          Utils.setprefb("showgobpath", val);
        }
      }).a = Config.showgobpath;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Raider mode trees") {
        public void changed(boolean val) {
          super.changed(val);
          Config.raidermodetrees = val;
          Utils.setprefb("raidermodetrees", val);
        }
      }).a = Config.raidermodetrees;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Raider mode braziers") {
        public void changed(boolean val) {
          super.changed(val);
          Config.raidermodebraziers = val;
          Utils.setprefb("raidermodebraziers", val);
        }
      }).a = Config.raidermodebraziers;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Farmer mode trees/bushes") {
        public void changed(boolean val) {
          super.changed(val);
          Config.farmermodetrees = val;
          Utils.setprefb("farmermodetrees", val);
        }
      }).a = Config.farmermodetrees;
    j += 25;
    (new CheckBox(new Coord(k, j), tab, "Laptop mode for the mouse") {
        public void changed(boolean val) {
          super.changed(val);
          Config.laptopcontrols = val;
          Utils.setprefb("laptopcontrols", val);
        }
      }).a = Config.laptopcontrols;
    j += 20;
    new Label(new Coord(k, j), tab, "To move the camera with a laptop,");
    j += 10;
    new Label(new Coord(k, j), tab, "press LMB then drag RMB. Zoom in");
    j += 10;
    new Label(new Coord(k, j), tab, "and out with + and -, and rotate");
    j += 10;
    new Label(new Coord(k, j), tab, "objects like that with shift-alt.");
    this.body.getClass();
    tab = new Tabs.Tab(this.body, new Coord(200, 0), 60, "Audio");
    int y = 30;
    new Label(new Coord(0, y), tab, "Audio volume");
    y += 20;
    audio = new HSlider(new Coord(0, y), 200, tab, 0, 1000, (int)(Audio.volume * 1000.0D)) {
        public void changed() {
          Audio.setvolume(this.val / 1000.0D);
        }
      };
    y += 30;
    new Label(new Coord(0, y), tab, "Music volume");
    y += 20;
    music = new HSlider(new Coord(0, y), 200, tab, 0, 1000, (int)(Music.volume * 1000.0D)) {
        public void changed() {
          Music.setvolume(this.val / 1000.0D);
        }
      };
    y += 30;
    (new CheckBox(new Coord(0, y), tab, "Mute the violin player") {
        public void changed(boolean val) {
          super.changed(val);
          Config.mute_violin = val;
          Utils.setprefb("mute_violin", val);
        }
      }).a = Config.mute_violin;
    y += 30;
    (new CheckBox(new Coord(0, y), tab, "Mute the xmas sound") {
        public void changed(boolean val) {
          super.changed(val);
          Config.mute_xmas_sound = val;
          Utils.setprefb("mute_xmas_sound", val);
        }
      }).a = Config.mute_xmas_sound;
    y += 30;
    (new CheckBox(new Coord(0, y), tab, "Mute ALL sounds and music - hotkey: CTRL + 9") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.toggleMuteAllAudio(Boolean.valueOf(val));
        }
      }).a = Config.mute_all_audio;
    this.body.getClass();
    tab = new Tabs.Tab(this.body, new Coord(270, 0), 120, "Inventory & Items");
    y = 15;
    int x = 0;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Study protection") {
        public void changed(boolean val) {
          super.changed(val);
          Config.flower_study = val;
          Utils.setprefb("flower_study", val);
        }
      }).a = Config.flower_study;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Show item contents as icons") {
        public void changed(boolean val) {
          super.changed(val);
          Config.show_contents_icons = val;
          Utils.setprefb("show_contents_icons", val);
        }
      }).a = Config.show_contents_icons;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Show purity as multiplier") {
        public void changed(boolean val) {
          super.changed(val);
          Config.pure_mult = val;
          Utils.setprefb("pure_mult", val);
        }
      }).a = Config.pure_mult;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Always show purity percentage/multiplier") {
        public void changed(boolean val) {
          super.changed(val);
          Config.alwaysshowpurity = val;
          Utils.setprefb("alwaysshowpurity", val);
        }
      }).a = Config.alwaysshowpurity;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Show gobble meters") {
        public void changed(boolean val) {
          super.changed(val);
          Config.gobble_meters = val;
          Utils.setprefb("gobble_meters", val);
        }
      }).a = Config.gobble_meters;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Limit the transfer amount") {
        public void changed(boolean val) {
          super.changed(val);
          Config.limit_transfer_amount = val;
          Utils.setprefb("limit_transfer_amount", val);
        }
      }).a = Config.limit_transfer_amount;
    y += 25;
    (opt_cSort = new CheckBox(new Coord(0, y), tab, "Enable continuous sorting") {
        public void changed(boolean val) {
          super.changed(val);
          Config.alwayssort = val;
          Utils.setprefb("alwayssort", val);
        }
      }).a = Config.alwayssort;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Sort only Main-Inventory with continuous sorting.") {
        public void changed(boolean val) {
          super.changed(val);
          Config.sort_only_main_inv = val;
          Utils.setprefb("sort_only_main_inv", val);
        }
      }).a = Config.sort_only_main_inv;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Picky Alt modifier") {
        public void changed(boolean val) {
          super.changed(val);
          Config.pickyalt = val;
          Utils.setprefb("pickyalt", val);
        }
      }).a = Config.pickyalt;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "NEW Item Icon Numbers") {
        public void changed(boolean val) {
          super.changed(val);
          Config.new_numbers_on_item_icons = val;
          Utils.setprefb("new_numbers_on_item_icons", val);
        }
      }).a = Config.new_numbers_on_item_icons;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "WeightWidget - added item count") {
        public void changed(boolean val) {
          super.changed(val);
          Config.weight_wdg_inv_items_nr = val;
          Utils.setprefb("weight_wdg_inv_items_nr", val);
          this.ui.gui.weightwdg.update(null);
        }
      }).a = Config.weight_wdg_inv_items_nr;
    y += 25;
    (new CheckBox(new Coord(0, y), tab, "Hide NEW Tooltip on inventories \"X\"-button") {
        public void changed(boolean val) {
          super.changed(val);
          Config.hide_tooltip_on_inv_x = val;
          Utils.setprefb("hide_tooltip_on_inv_x", val);
          if (!val && this.ui.gui.maininv != null && this.ui.gui.maininv.parent != null)
            ((Window)this.ui.gui.maininv.parent).cbtn.tooltip = null; 
        }
      }).a = Config.hide_tooltip_on_inv_x;
    makeRadarTab();
    this.body.getClass();
    tab = new Tabs.Tab(this.body, new Coord(480, 0), 60, "Hotkeys") {
        HotkeyList hkList = new HotkeyList(new Coord(0, 80), this);
      };
    hotkey_Key = new TextEntry(new Coord(30, 332), 30, tab, "") {
        protected void changed() {
          if (this.text.trim().length() > 0 && !this.text.trim().equals(" ")) {
            String t = this.text.toUpperCase().trim();
            if (t.length() > 1)
              t = t.substring(t.length() - 1, t.length()); 
            settext(t);
          } else {
            settext("");
          } 
          super.changed();
        }
        
        public void activate(String text) {
          setfocus(OptWnd2.hotkey_Com);
          super.activate(text);
        }
      };
    hotkey_Com = new TextEntry(new Coord(130, 332), 400, tab, "") {
        public void activate(String text) {
          String key = OptWnd2.hotkey_Key.text;
          String com = OptWnd2.hotkey_Com.text;
          if (HotkeyList.instance != null && 
            key != null && key.length() == 1 && 
            com != null && com.length() > 0) {
            HotkeyList.instance.add(key, com);
            OptWnd2.hotkey_Key.settext("");
            OptWnd2.hotkey_Com.settext("");
          } 
          super.activate(text);
        }
      };
    hotkey_Key.canactivate = true;
    hotkey_Com.canactivate = true;
    Button hkAdd = new Button(new Coord(535, 330), Integer.valueOf(45), tab, "Add") {
        public void click() {
          String key = OptWnd2.hotkey_Key.text;
          String com = OptWnd2.hotkey_Com.text;
          if (HotkeyList.instance != null && 
            key != null && key.length() == 1 && 
            com != null && com.length() > 0) {
            HotkeyList.instance.add(key, com);
            OptWnd2.hotkey_Key.settext("");
            OptWnd2.hotkey_Com.settext("");
          } 
        }
      };
    new Label(new Coord(0, 332), tab, "Key:");
    new Label(new Coord(70, 332), tab, "Command:");
    new Label(new Coord(10, 25), tab, "Enter commands to execute for configureable hotkeys.");
    new Label(new Coord(10, 37), tab, "\"Hotkey\" can be any letter, together with chosen CTRL/SHIFT/ALT-combo.");
    new Label(new Coord(10, 49), tab, "\"Command\" is a console command, but without the \":\" (colon).");
    new Label(new Coord(430, 25), tab, "WILL OVERRIDE any default");
    new Label(new Coord(430, 37), tab, "(menu-)shortcuts, hotkeys, and");
    new Label(new Coord(430, 49), tab, "even WASD if you chose to do so!");
    int xVal = 220;
    int xVal2 = 225;
    this.body.getClass();
    tab = new Tabs.Tab(this.body, new Coord(550, 0), 60, "Cheats") {
        FlowerList list;
        
        Button add;
        
        TextEntry value;
        
        public void wdgmsg(Widget sender, String msg, Object... args) {
          if ((sender == this.add || sender == this.value) && msg.equals("activate")) {
            this.list.add(this.value.text);
            this.value.settext("");
          } else {
            super.wdgmsg(sender, msg, args);
          } 
        }
      };
    new Label(new Coord(220, 30), tab, "Choose menu items to select automatically:");
    int i = 5;
    i += 25;
    (new CheckBox(new Coord(0, i), tab, "Auto sift") {
        public void changed(boolean val) {
          super.changed(val);
          Config.autosift = val;
          Utils.setprefb("autosift", val);
        }
      }).a = Config.autosift;
    i += 25;
    (new CheckBox(new Coord(0, i), tab, "Auto bucket") {
        public void changed(boolean val) {
          super.changed(val);
          Config.autobucket = val;
          Utils.setprefb("autobucket", val);
        }
      }).a = Config.autobucket;
    i += 25;
    (new CheckBox(new Coord(0, i), tab, "Show actor path") {
        public void changed(boolean val) {
          super.changed(val);
          Config.gobpath = val;
          Utils.setprefb("gobpath", val);
          OptWnd2.this.gob_path_color.enabled = val;
        }
      }).a = Config.gobpath;
    i += 25;
    this.gob_path_color = new CheckBox(new Coord(10, i), tab, "Use kin color") {
        public void changed(boolean val) {
          super.changed(val);
          Config.gobpath_color = val;
          Utils.setprefb("gobpath_color", val);
        }
      };
    this.gob_path_color.a = Config.gobpath_color;
    this.gob_path_color.enabled = Config.gobpath;
    i += 20;
    new Button(new Coord(10, i), Integer.valueOf(75), tab, "options") {
        public void click() {
          GobPathOptWnd.toggle();
        }
      };
    i += 25;
    (new CheckBox(new Coord(0, i), tab, "Auto drop bats") {
        public void changed(boolean val) {
          super.changed(val);
          Config.auto_drop_bats = val;
          Utils.setprefb("auto_drop_bats", val);
        }
      }).a = Config.auto_drop_bats;
    i += 25;
    (new CheckBox(new Coord(0, i), tab, "Auto logout") {
        public void changed(boolean val) {
          super.changed(val);
          Config.autolog = val;
          Utils.setprefb("autolog", val);
        }
      }).a = Config.autolog;
    i += 25;
    (new CheckBox(new Coord(0, i), tab, "Single item CTRL choose") {
        public void changed(boolean val) {
          super.changed(val);
          Config.singleItemCTRLChoose = val;
          Utils.setprefb("singleItemCTRLChoose", val);
        }
      }).a = Config.singleItemCTRLChoose;
    i += 25;
    (customShiftItemActCBox = new CheckBox(new Coord(0, i), tab, "Custom SHIFT-item-selection") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.toggleCustomItemAct(Boolean.valueOf(val));
        }
      }).a = Config.custom_shift_itemact;
    i += 25;
    (autoBackpackBucketCBox = new CheckBox(new Coord(0, i), tab, "Auto \"Container\" from inventory/backpack") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.toggleAutoBackpackBucket(Boolean.valueOf(val));
        }
      }).a = Config.auto_backpack_bucket;
    xVal = 190;
    xVal2 = 220;
    this.body.getClass();
    tab = new Tabs.Tab(this.body, new Coord(620, 0), 60, "Hacks") {
        HideGobsList list2;
        
        Button add2;
        
        TextEntry value2;
        
        public void wdgmsg(Widget sender, String msg, Object... args) {
          if ((sender == this.add2 || sender == this.value2) && msg.equals("activate")) {
            if (!this.value2.text.toLowerCase().contains("borka") && this.value2.text.length() > 2) {
              this.list2.add(this.value2.text);
              this.value2.settext("");
              if (Config.hideSomeGobs)
                OptWnd2.applyHideGobsList(); 
            } 
          } else {
            super.wdgmsg(sender, msg, args);
          } 
        }
      };
    new Label(new Coord(410, 30), tab, "Hide these objects:");
    (aCB2 = new CheckBox(new Coord(410, 335), tab, "Activate Hide-Objects") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.changeHideSomeGobs(Boolean.valueOf(val));
        }
      }).a = Config.hideSomeGobs;
    i = 5;
    i += 35;
    new Button(new Coord(10, i), Integer.valueOf(140), tab, "Buff/Alert Configuration") {
        public void click() {
          BuffAlertOptWnd.toggle();
        }
      };
    i += 35;
    (new CheckBox(new Coord(0, i), tab, "Auto-Doors") {
        public void changed(boolean val) {
          super.changed(val);
          Config.auto_door_on = val;
          Utils.setprefb("auto_door_on", val);
          GameUI.autoDoors = val;
        }
      }).a = Config.auto_door_on;
    i += 35;
    aCB = new CheckBox(new Coord(0, i), tab, "Activate Render Distance Limit") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.changeRenderDistance(Boolean.valueOf(val));
        }
      };
    aCB.a = Config.render_distance_bool_value;
    i += 35;
    new Button(new Coord(10, i), Integer.valueOf(160), tab, "Render Distance Configuration") {
        public void click() {
          RenderDistanceOptWnd.toggle();
        }
      };
    i += 35;
    new Button(new Coord(10, i), Integer.valueOf(160), tab, "FPS Display Configuration") {
        public void click() {
          FPSDisplayOptWnd.toggle();
        }
      };
    i += 35;
    (new CheckBox(new Coord(0, i), tab, "Chat-Online-Colour") {
        public void changed(boolean val) {
          super.changed(val);
          Config.chat_online_colour = val;
          Utils.setprefb("chat_online_colour", val);
          this.ui.gui.chat.chansel.rerender = true;
        }
      }).a = Config.chat_online_colour;
    (new CheckBox(new Coord(190, i), tab, "Auto-Sync Timers") {
        public void changed(boolean val) {
          super.changed(val);
          Config.auto_sync_timers = val;
          Utils.setprefb("auto_sync_timers", val);
        }
      }).a = Config.auto_sync_timers;
    i += 35;
    (new CheckBox(new Coord(0, i), tab, "Show chat channels in 2 columns instead one 1") {
        public void changed(boolean val) {
          super.changed(val);
          Config.two_chat_columns = val;
          Utils.setprefb("two_chat_columns", val);
          ChatUI.switchColumCount();
        }
      }).a = Config.two_chat_columns;
    i += 20;
    (new CheckBox(new Coord(0, i), tab, "Show Log channel for floating numbers") {
        public void changed(boolean val) {
          super.changed(val);
          Config.floating_text_to_console = val;
          Utils.setprefb("floating_text_to_console", val);
          ChatUI.toggleLogChannel();
        }
      }).a = Config.floating_text_to_console;
    i += 20;
    (new CheckBox(new Coord(0, i), tab, "Also add target name of floating numbers") {
        public void changed(boolean val) {
          super.changed(val);
          Config.floating_ttc_add_target_name = val;
          Utils.setprefb("floating_ttc_add_target_name", val);
        }
      }).a = Config.floating_ttc_add_target_name;
    i += 20;
    (new CheckBox(new Coord(0, i), tab, "Mute System chat channel") {
        public void changed(boolean val) {
          super.changed(val);
          Config.mute_system_chat = val;
          Utils.setprefb("mute_system_chat", val);
        }
      }).a = Config.mute_system_chat;
    i += 20;
    (new CheckBox(new Coord(0, i), tab, "Mute Log chat channel") {
        public void changed(boolean val) {
          super.changed(val);
          Config.mute_log_chat = val;
          Utils.setprefb("mute_log_chat", val);
        }
      }).a = Config.mute_log_chat;
    (new CheckBox(new Coord(230, i - 40), tab, "Newbie Protection") {
        public void changed(boolean val) {
          super.changed(val);
          Config.newbie_protection = val;
          Utils.setprefb("newbie_protection", val);
          Config.newbie_prot_hide_info = false;
          Utils.setprefb("newbie_prot_hide_info", false);
        }
      }).a = Config.newbie_protection;
    (invShiftCBox = new CheckBox(new Coord(200, i), tab, "Invert SHIFT on ItemAct") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.changeInvertShift(Boolean.valueOf(val));
        }
      }).a = Config.shift_invert_option_checkbox;
    i = 5;
    i += 35;
    new Label(new Coord(190, i), tab, "Movement types:");
    List<String> mlist = new ArrayList<>();
    mlist.add("wander");
    mlist.add("sprint");
    mlist.add("forage");
    mlist.add("sneak");
    mlist.add("run");
    movBoxList.clear();
    for (String str1 : mlist) {
      final String m2 = str1;
      i += 25;
      CheckBox box = new CheckBox(new Coord(190, i), tab, "Auto turn on \"" + m2 + "\" upon Login") {
          public void changed(boolean val) {
            super.changed(val);
            String mov = this.lbl.text;
            mov = mov.substring(mov.indexOf("\"") + 1, mov.lastIndexOf("\""));
            if (val) {
              Utils.setpref("auto_activate_movement_mode", mov);
              Config.auto_activate_movement_mode = mov;
              String[] as2 = { "blk", mov };
              this.ui.gui.wdgmsg("act", (Object[])as2);
              for (CheckBox b : OptWnd2.movBoxList) {
                if (this != b)
                  b.a = false; 
              } 
            } else if (mov.equals(Config.auto_activate_movement_mode)) {
              Utils.setpref("auto_activate_movement_mode", "no_move");
              Config.auto_activate_movement_mode = "no_move";
            } 
          }
        };
      box.a = str1.equals(Config.auto_activate_movement_mode);
      movBoxList.add(box);
    } 
    String last = Utils.getpref("optwndtab", "");
    for (Tabs.Tab t : this.body.tabs) {
      if (t.btn.text.text.equals(last))
        this.body.showtab(t); 
    } 
  }
  
  public static void applyHideGobsList() {
    OCache oc = UI.instance.sess.glob.oc;
    Collection<Gob> gobs = oc.getGobs();
    Set<Map.Entry<String, Boolean>> es = Config.HIDEGOBS.entrySet();
    for (Gob g : gobs) {
      ResDrawable rd = null;
      Composite cmp = null;
      String nm = "";
      try {
        rd = g.<ResDrawable>getattr(ResDrawable.class);
        if (rd != null)
          nm = ((Resource)rd.res.get()).name; 
      } catch (Loading loading) {}
      try {
        cmp = g.<Composite>getattr(Composite.class);
        if (cmp != null)
          nm = ((Resource)cmp.base.get()).name; 
      } catch (Loading loading) {}
      if (nm == null)
        continue; 
      for (Map.Entry<String, Boolean> e : es) {
        if (((Boolean)e.getValue()).booleanValue() && nm.contains(e.getKey()))
          oc.remove(g.id); 
      } 
    } 
  }
  
  public static void setRadarInfo(RadarConfig rcf, MarkerFactory mf) {
    rc = rcf;
    OptWnd2.mf = mf;
    if (instance == null)
      return; 
    instance.makeRadarTab();
  }
  
  private void makeRadarTab() {
    if (rc == null)
      return; 
    boolean viewingradartab = (this.body.curtab == this.radartab);
    if (this.radartab != null) {
      boolean success = this.body.tabs.remove(this.radartab);
      System.out.println("Removed the radartab step 1: " + success);
      this.radartab.unlink();
      this.radartab.btn.destroy();
      this.radartab.destroy();
    } 
    this.body.getClass();
    this.radartab = new Tabs.Tab(this.body, new Coord(400, 0), 70, "Radar config");
    int x = 0, y = 35;
    for (ConfigGroup cg : rc.getGroups()) {
      (new CheckBox(new Coord(x, y), this.radartab, cg.name) {
          public void changed(boolean val) {
            super.changed(val);
            cg.show = val;
            for (ConfigMarker cm : cg.markers)
              cm.show = val; 
            if (OptWnd2.mf != null)
              OptWnd2.mf.setConfig(OptWnd2.rc); 
            this.ui.sess.glob.oc.radar.refresh(OptWnd2.rc);
          }
        }).a = cg.show;
      y += 25;
      if (y > this.radartab.sz.y - 25) {
        y = 35;
        x += 200;
      } 
    } 
    if (viewingradartab)
      this.body.showtab(this.radartab); 
  }
  
  private static void checkVideoOpt(CheckBox check, GLSettings.BoolSetting setting) {
    checkVideoOpt(check, setting, (Object)null);
  }
  
  private static void checkVideoOpt(CheckBox check, GLSettings.BoolSetting setting, Object tooltip) {
    try {
      setting.validate(Boolean.valueOf(true));
      check.enabled = true;
      check.tooltip = tooltip;
    } catch (SettingException e) {
      check.enabled = false;
      check.tooltip = Text.render(e.getMessage());
    } 
  }
  
  protected static void setcamera(String camtype) {
    curcam = camtype;
    Utils.setpref("defcam", curcam);
    MapView mv = UI.instance.gui.map;
    if (mv != null)
      mv.setcam(curcam); 
    CamControlOptWnd.refreshHard();
  }
  
  private int getsfxvol() {
    return (int)(100.0D - Double.parseDouble(Utils.getpref("sfxvol", "1.0")) * 100.0D);
  }
  
  private void addinfo(String camtype, String title, String text, Tabs.Tab args) {
    this.caminfomap.put(camtype, new CamInfo(title, text, args));
    this.camname2type.put(title, camtype);
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.cbtn)
      super.wdgmsg(sender, msg, args); 
  }
  
  public static class Frame extends Widget {
    private final IBox box;
    
    private Color bgcoplor;
    
    public Frame(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
      this.box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
    }
    
    public Frame(Coord c, Coord sz, Color bg, Widget parent) {
      this(c, sz, parent);
      this.bgcoplor = bg;
    }
    
    public void draw(GOut og) {
      GOut g = og.reclip(Coord.z, this.sz);
      if (this.bgcoplor != null) {
        g.chcolor(this.bgcoplor);
        g.frect(this.box.btloff(), this.sz.sub(this.box.bisz()));
      } 
      g.chcolor(150, 200, 125, 255);
      this.box.draw(g, Coord.z, this.sz);
      super.draw(og);
    }
  }
  
  public static void toggle() {
    UI ui = UI.instance;
    if (instance == null) {
      instance = new OptWnd2(Coord.z, ui.gui);
    } else {
      ui.destroy(instance);
    } 
  }
  
  public static void refresh() {
    if (instance != null) {
      UI ui = UI.instance;
      ui.destroy(instance);
      instance = null;
      instance = new OptWnd2(Coord.z, ui.gui);
    } 
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  public static void close() {
    if (instance != null) {
      UI ui = UI.instance;
      ui.destroy(instance);
    } 
  }
  
  public static class OptUtil {
    public static boolean changeRenderDistance(Boolean input) {
      boolean val;
      if (input == null) {
        val = !Config.render_distance_bool_value;
      } else {
        val = input.booleanValue();
      } 
      Config.render_distance_bool_value = val;
      Utils.setprefb("render_distance_bool_value", val);
      OCache.renderDistance = val;
      if (OptWnd2.aCB != null)
        OptWnd2.aCB.a = val; 
      if (RenderDistanceOptWnd.aCB != null)
        RenderDistanceOptWnd.aCB.a = val; 
      if (!val)
        OCache.undoRenderDistance(); 
      Utils.msgOut("[Activate Render Distance Limit] set to: " + val + " - change this with CTRL+2 or under Options/Hacks/...");
      return false;
    }
    
    public static void toggleAutoBackpackBucket(Boolean val) {
      if (val == null)
        val = Boolean.valueOf(!Config.auto_backpack_bucket); 
      Config.auto_backpack_bucket = val.booleanValue();
      Utils.setprefb("auto_backpack_bucket", val.booleanValue());
      if (OptWnd2.autoBackpackBucketCBox != null)
        OptWnd2.autoBackpackBucketCBox.a = val.booleanValue(); 
      Utils.msgOut("[Auto-\"Container\"-from-inventory/backpack] is set to: " + val + " - toggle with CTRL+8");
    }
    
    public static void toggleCustomItemAct(Boolean val) {
      if (val == null)
        val = Boolean.valueOf(!Config.custom_shift_itemact); 
      Config.custom_shift_itemact = val.booleanValue();
      Utils.setprefb("custom_shift_itemact", val.booleanValue());
      if (OptWnd2.customShiftItemActCBox != null)
        OptWnd2.customShiftItemActCBox.a = val.booleanValue(); 
      Utils.msgOut("[Custom-SHIFT-ItemAct] is set to: " + val + " - toggle with CTRL+7");
    }
    
    public static void toggleHitBoxes(Boolean val) {
      if (val == null)
        val = Boolean.valueOf(!Config.hitbox_on); 
      Config.hitbox_on = val.booleanValue();
      Utils.setprefb("hitbox_on", val.booleanValue());
      if (OptWnd2.hitBoxCBox != null)
        OptWnd2.hitBoxCBox.a = val.booleanValue(); 
      Utils.msgOut("[Show-HitBoxes] is set to: " + val + " - toggle with CTRL+6");
    }
    
    public static void toggleContinuousSorting(Boolean val) {
      if (val == null)
        val = Boolean.valueOf(!Config.alwayssort); 
      Config.alwayssort = val.booleanValue();
      Utils.setprefb("alwayssort", val.booleanValue());
      if (OptWnd2.opt_cSort != null)
        OptWnd2.opt_cSort.a = val.booleanValue(); 
      Utils.msgOut("[Continuous Sorting] is set to: " + val);
    }
    
    public static void changeInvertShift(Boolean val) {
      if (val == null)
        val = Boolean.valueOf(Config.shift_invert_option_checkbox); 
      Config.shift_invert_option_checkbox = val.booleanValue();
      Utils.setprefb("shift_invert_option_checkbox", val.booleanValue());
      if (OptWnd2.invShiftCBox != null)
        OptWnd2.invShiftCBox.a = val.booleanValue(); 
      Utils.msgOut("[SHIFT-invert] is set to: " + val + " - toggle with CTRL+1");
    }
    
    public static boolean changeHideSomeGobs(Boolean val) {
      if (val == null)
        val = Boolean.valueOf(!Config.hideSomeGobs); 
      Config.hideSomeGobs = val.booleanValue();
      Utils.setprefb("hide_some_gobs", val.booleanValue());
      OCache.hideSomeGobs = val.booleanValue();
      if (Config.hideSomeGobs)
        OptWnd2.applyHideGobsList(); 
      if (OptWnd2.aCB2 != null)
        OptWnd2.aCB2.a = val.booleanValue(); 
      Utils.msgOut("[Hide Gobs] is set to: " + Config.hideSomeGobs + " - use CTRL+3 to toggle, or set under Options/Hacks/...");
      return val.booleanValue();
    }
    
    public static boolean changeDisplayFPS(Boolean val) {
      if (val == null)
        val = Boolean.valueOf(!Config.fps_display_show); 
      Config.fps_display_show = val.booleanValue();
      Utils.setprefb("fps_display_show", val.booleanValue());
      HavenPanel.fpsShow = val.booleanValue();
      if (FPSDisplayOptWnd.fpsCB != null)
        FPSDisplayOptWnd.fpsCB.a = val.booleanValue(); 
      Utils.msgOut("[FPS Display] is set to: " + Config.fps_display_show + " - use CTRL+4 to toggle, or set under Options/Hacks/FPS Display Configuration...");
      return val.booleanValue();
    }
    
    public static void toggleMuteAllAudio(Boolean val) {
      if (val != null) {
        Config.mute_all_audio = val.booleanValue();
      } else {
        Config.mute_all_audio = !Config.mute_all_audio;
      } 
      Utils.setprefb("mute_all_audio", Config.mute_all_audio);
      Utils.msgOut("All Audio Mute is set to: " + Config.mute_all_audio);
      if (Config.mute_all_audio) {
        if (Audio.volume > 0.0D) {
          Config.volume_audio_saved = Audio.volume;
          Utils.setpreff("volume_audio_saved", (float)Audio.volume);
        } 
        if (Music.volume > 0.0D) {
          Config.volume_music_saved = Music.volume;
          Utils.setpreff("volume_music_saved", (float)Music.volume);
        } 
        Audio.setvolume(0.0D);
        Music.setvolume(0.0D);
      } else {
        if (Config.volume_audio_saved != 0.0D)
          Audio.setvolume(Config.volume_audio_saved); 
        if (Config.volume_music_saved != 0.0D)
          Music.setvolume(Config.volume_music_saved); 
      } 
      if (OptWnd2.audio != null)
        OptWnd2.audio.val = (int)(Audio.volume * 1000.0D); 
      if (OptWnd2.music != null)
        OptWnd2.music.val = (int)(Music.volume * 1000.0D); 
    }
  }
}
