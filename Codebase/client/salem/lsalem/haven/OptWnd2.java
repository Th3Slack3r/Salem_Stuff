package haven;

import haven.minimap.ConfigGroup;
import haven.minimap.ConfigMarker;
import haven.minimap.MarkerFactory;
import haven.minimap.RadarConfig;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptWnd2 extends Window {
   public static final RichText.Foundry foundry = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
   public static OptWnd2 instance = null;
   private final CheckBox gob_path_color;
   private Tabs body;
   private Tabs.Tab radartab;
   private static RadarConfig rc = null;
   private static MarkerFactory mf = null;
   private String curcam;
   private Map<String, OptWnd2.CamInfo> caminfomap = new HashMap<>();
   private Map<String, String> camname2type = new HashMap<>();
   private Comparator<String> camcomp = new Comparator<String>() {
      public int compare(String a, String b) {
         if (a.startsWith("The ")) {
            a = a.substring(4);
         }

         if (b.startsWith("The ")) {
            b = b.substring(4);
         }

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

   public OptWnd2(Coord c, Widget parent) {
      super(c, new Coord(500, 360), parent, "Options");
      this.justclose = true;
      this.body = new Tabs(Coord.z, new Coord(500, 360), this) {
         @Override
         public void changed(Tabs.Tab from, Tabs.Tab to) {
            Utils.setpref("optwndtab", to.btn.text.text);
            from.btn.c.y = 0;
            to.btn.c.y = -2;
         }
      };
      Widget tab = this.body.new Tab(new Coord(0, 0), 60, "General");
      new Button(new Coord(0, 30), 125, tab, "Quit") {
         @Override
         public void click() {
            HackThread.tg().interrupt();
         }
      };
      new Button(new Coord(135, 30), 125, tab, "Switch character") {
         @Override
         public void click() {
            this.ui.gui.act("lo", "cs");
         }
      };
      new Button(new Coord(0, 60), 125, tab, "Log out") {
         @Override
         public void click() {
            this.ui.gui.act("lo");
         }
      };
      final Widget editbox = new OptWnd2.Frame(new Coord(310, 30), new Coord(90, 100), tab);
      new Label(new Coord(20, 10), editbox, "Edit mode:");
      RadioGroup editmode = new RadioGroup(editbox) {
         @Override
         public void changed(int btn, String lbl) {
            Utils.setpref("editmode", lbl.toLowerCase());
         }
      };
      editmode.add("Emacs", new Coord(10, 25));
      editmode.add("PC", new Coord(10, 50));
      if (Utils.getpref("editmode", "pc").equals("emacs")) {
         editmode.check("Emacs");
      } else {
         editmode.check("PC");
      }

      int y = 100;
      this.opt_show_tempers = new CheckBox(new Coord(0, y), tab, "Always show humor numbers") {
         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.show_tempers = val;
            Utils.setprefb("show_tempers", val);
         }
      };
      this.opt_show_tempers.a = Config.show_tempers;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Store minimap") {
         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.store_map = val;
            Utils.setprefb("store_map", val);
            if (val) {
               this.ui.gui.mmap.cgrid = null;
            }
         }
      }).a = Config.store_map;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Study protection") {
         {
            this.tooltip = Text.render("Leave only 'Study' option in right-click menus, if they have one.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.flower_study = val;
            Utils.setprefb("flower_study", val);
         }
      }).a = Config.flower_study;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Show aether as multiplier") {
         {
            this.tooltip = Text.render("Makes aether be displayed as the effective multiplier rather than the percentage.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.pure_mult = val;
            Utils.setprefb("pure_mult", val);
         }
      }).a = Config.pure_mult;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Radar icons") {
         {
            this.tooltip = Text.render("Objects detected by radar will be shown by icons, if available");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.radar_icons = val;
            Utils.setprefb("radar_icons", val);
         }
      }).a = Config.radar_icons;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Blink radar objects") {
         {
            this.tooltip = Text.render("Objects detected by radar will blink");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.blink = val;
            Utils.setprefb("blink", val);
         }
      }).a = Config.blink;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Take screenshots silently") {
         {
            this.tooltip = Text.render("Screenshots will be taken without showing screenshot dialog");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.ss_silent = val;
            Utils.setprefb("ss_slent", val);
         }
      }).a = Config.ss_silent;
      (new CheckBox(new Coord(200, y), tab, "Compress screenshots") {
         {
            this.tooltip = Text.render("Compressed screenshots use .JPEG, non-compressed .PNG");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.ss_compress = val;
            Utils.setprefb("ss_compress", val);
         }
      }).a = Config.ss_compress;
      (new CheckBox(new Coord(200, y + 25), tab, "Include UI on screenshots") {
         {
            this.tooltip = Text.render("Sets default value of include UI on screenshot dialog");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.ss_ui = val;
            Utils.setprefb("ss_ui", val);
         }
      }).a = Config.ss_ui;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Show weight widget") {
         {
            this.tooltip = Text.render("Shows small floating widget with current carrying weight");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.weight_wdg = val;
            Utils.setprefb("weight_wdg", val);
         }
      }).a = Config.weight_wdg;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Arrow home pointer") {
         {
            this.tooltip = Text.render("Makes home pointer display as green arrow over character head");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.hptr = val;
            Utils.setprefb("hptr", val);
            this.ui.gui.mainmenu.pv = Config.hpointv && !val;
         }
      }).a = Config.hptr;
      y += 25;
      (new CheckBox(new Coord(0, y), tab, "Crafting menu resets") {
         {
            this.tooltip = Text.render("Makes the crafting menu reset after selecting a recipe.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.menugrid_resets = val;
            Utils.setprefb("menugrid_resets", val);
         }
      }).a = Config.menugrid_resets;
      int var45 = 125;
      var45 += 25;
      (new CheckBox(new Coord(200, var45), tab, "Show item contents as icons") {
         {
            this.tooltip = Text.render("draws small icons of content of seed and flour bags");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.show_contents_icons = val;
            Utils.setprefb("show_contents_icons", val);
         }
      }).a = Config.show_contents_icons;
      var45 += 25;
      (new CheckBox(new Coord(200, var45), tab, "Auto open craft window") {
         {
            this.tooltip = Text.render("Makes craft window open if you click on any crafting item in menugrid or toolbelt.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.autoopen_craftwnd = val;
            Utils.setprefb("autoopen_craftwnd", val);
         }
      }).a = Config.autoopen_craftwnd;
      var45 += 25;
      (new CheckBox(new Coord(200, var45), tab, "Show gobble meters") {
         {
            this.tooltip = Text.render("During gobbling displays meters that show food efficiency.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.gobble_meters = val;
            Utils.setprefb("gobble_meters", val);
         }
      }).a = Config.gobble_meters;
      var45 += 25;
      (new CheckBox(new Coord(200, var45), tab, "Translate") {
         {
            this.tooltip = Text.render("Translate texts using trans.txt.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.translate = val;
            Utils.setprefb("translate", val);
         }
      }).a = Config.translate;
      this.curcam = Utils.getpref("defcam", "sortho");
      tab = this.body.new Tab(new Coord(70, 0), 60, "Camera");
      new Label(new Coord(10, 30), tab, "Camera type:");
      editbox = new RichTextBox(new Coord(180, 25), new Coord(210, 180), tab, "", foundry);
      editbox.bg = new Color(0, 0, 0, 64);
      this.addinfo(
         "ortho",
         "Isometric Cam",
         "Isometric camera centered on character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.",
         null
      );
      this.addinfo(
         "sortho",
         "Smooth Isometric Cam",
         "Isometric camera centered on character with smoothed movement. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.",
         null
      );
      this.addinfo(
         "follow",
         "Follow Cam",
         "The camera follows the character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.",
         null
      );
      this.addinfo(
         "sfollow",
         "Smooth Follow Cam",
         "The camera smoothly follows the character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.",
         null
      );
      this.addinfo(
         "free",
         "Freestyle Cam",
         "You can move around freely within the larger area around character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.",
         null
      );
      this.addinfo(
         "best",
         "Smooth Freestyle Cam",
         "You can move around freely within the larger area around character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.",
         null
      );
      final Tabs cambox = new Tabs(new Coord(100, 60), new Coord(300, 200), tab);
      RadioGroup cameras = new RadioGroup(tab) {
         @Override
         public void changed(int btn, String lbl) {
            if (OptWnd2.this.camname2type.containsKey(lbl)) {
               lbl = OptWnd2.this.camname2type.get(lbl);
            }

            if (!lbl.equals(OptWnd2.this.curcam)) {
               OptWnd2.this.setcamera(lbl);
            }

            OptWnd2.CamInfo inf = OptWnd2.this.caminfomap.get(lbl);
            if (inf == null) {
               cambox.showtab(null);
               editbox.settext("");
            } else {
               cambox.showtab(inf.args);
               editbox.settext(String.format("$size[12]{%s}\n\n$col[200,175,150,255]{%s}", inf.name, inf.desc));
            }
         }
      };
      List<String> clist = new ArrayList<>();

      for (String camtype : MapView.camtypes.keySet()) {
         clist.add(this.caminfomap.containsKey(camtype) ? this.caminfomap.get(camtype).name : camtype);
      }

      Collections.sort(clist, this.camcomp);
      int yx = 25;

      for (String camname : clist) {
         yx += 25;
         cameras.add(camname, new Coord(10, yx));
      }

      cameras.check(this.caminfomap.containsKey(this.curcam) ? this.caminfomap.get(this.curcam).name : this.curcam);
      yx += 40;
      (new CheckBox(new Coord(5, yx), tab, "Rotate isometric cams by steps") {
         {
            this.tooltip = Text.render("Makes isometric cameras rotate in 90 degree steps.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.isocam_steps = val;
            Utils.setprefb("isocam_steps", val);
            if (this.ui.gui != null && this.ui.gui.map != null && this.ui.gui.map.camera != null) {
               this.ui.gui.map.camera.fixangle();
            }
         }
      }).a = Config.isocam_steps;
      int var56 = 200;
      var56 += 25;
      this.opt_aa = new CheckBox(new Coord(180, var56), tab, "Antialiasing") {
         @Override
         public void set(boolean val) {
            try {
               Config.glcfg.fsaa.set(val);
            } catch (GLSettings.SettingException var3) {
               val = false;
               this.<GameUI>getparent(GameUI.class).error(var3.getMessage());
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
      var56 += 25;
      this.opt_qw = new CheckBox(new Coord(180, var56), tab, "Quality water") {
         @Override
         public void set(boolean val) {
            try {
               Config.glcfg.wsurf.set(val);
            } catch (GLSettings.SettingException var3) {
               val = false;
               this.<GameUI>getparent(GameUI.class).error(var3.getMessage());
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
      var56 += 25;
      this.opt_sb = new CheckBox(new Coord(180, var56), tab, "Skybox") {
         {
            this.tooltip = Text.render("Display the skybox.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.skybox = val;
            Utils.setprefb("skybox", val);
         }
      };
      this.opt_sb.a = Config.skybox;
      int var60 = 200;
      int x = 290;
      var60 += 25;
      this.opt_flight = new CheckBox(new Coord(x, var60), tab, "Per-pixel lighting") {
         @Override
         public void set(boolean val) {
            try {
               Config.glcfg.flight.set(val);
               if (!val) {
                  Config.glcfg.flight.set(false);
                  Config.glcfg.cel.set(false);
                  Config.shadows = OptWnd2.this.opt_shadow.a = false;
                  Config.cellshade = OptWnd2.this.opt_cel.a = false;
               }
            } catch (GLSettings.SettingException var3) {
               val = false;
               this.<GameUI>getparent(GameUI.class).error(var3.getMessage());
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
      var60 += 25;
      this.opt_shadow = new CheckBox(new Coord(x, var60), tab, "Shadows") {
         @Override
         public void set(boolean val) {
            try {
               Config.glcfg.lshadow.set(val);
            } catch (GLSettings.SettingException var3) {
               val = false;
               this.<GameUI>getparent(GameUI.class).error(var3.getMessage());
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
      var60 += 25;
      this.opt_cel = new CheckBox(new Coord(x, var60), tab, "Cel-shading") {
         @Override
         public void set(boolean val) {
            try {
               Config.glcfg.cel.set(val);
            } catch (GLSettings.SettingException var3) {
               val = false;
               this.<GameUI>getparent(GameUI.class).error(var3.getMessage());
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
      var60 = tab.sz.y - 50;
      new Label(new Coord(10, var60), tab, "Camera FOV:");
      new HSlider(new Coord(85, var60 + 5), 200, tab, 0, 1000, (int)(Config.camera_field_of_view * 1000.0F)) {
         @Override
         public void changed() {
            Config.camera_field_of_view = this.val / 1000.0F;
            Utils.setpreff("camera_field_of_view", this.val / 1000.0F);
            if (this.ui != null && this.ui.gui != null && this.ui.gui.map.camera != null) {
               this.ui.gui.map.camera.resized();
            }
         }
      };
      var60 = tab.sz.y - 20;
      new Label(new Coord(10, var60), tab, "Brightness:");
      new HSlider(new Coord(85, var60 + 5), 200, tab, 0, 1000, (int)(Config.brighten * 1000.0F)) {
         @Override
         public void changed() {
            Config.setBrighten(this.val / 1000.0F);
            this.ui.sess.glob.brighten();
         }
      };
      tab = this.body.new Tab(new Coord(140, 0), 60, "Audio");
      int yxx = 30;
      new Label(new Coord(0, yxx), tab, "Audio volume");
      int var18 = yxx + 20;
      new HSlider(new Coord(0, var18), 200, tab, 0, 1000, (int)(Audio.volume * 1000.0)) {
         @Override
         public void changed() {
            Audio.setvolume(this.val / 1000.0);
         }
      };
      int var19 = var18 + 30;
      new Label(new Coord(0, var19), tab, "Music volume");
      int var20 = var19 + 20;
      new HSlider(new Coord(0, var20), 200, tab, 0, 1000, (int)(Music.volume * 1000.0)) {
         @Override
         public void changed() {
            Music.setvolume(this.val / 1000.0);
         }
      };
      tab = this.body.new Tab(new Coord(210, 0), 60, "Latikai");
      (new CheckBox(new Coord(0, 35), tab, "Enable 1x1 field fix") {
         {
            this.tooltip = Text.render("Only show a single instance of a crop, at the center of its field.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.fieldfix = val;
            Utils.setprefb("fieldfix", val);
         }
      }).a = Config.fieldfix;
      final Label cropscale = new Label(new Coord(0, 60), tab, "Crop scaling: x" + Config.fieldproducescale);
      new HSlider(new Coord(15, 80), 200, tab, 0, 9, Config.fieldproducescale - 1) {
         @Override
         public void changed() {
            Config.setFieldproducescale(this.val + 1);
            cropscale.settext("Crop scaling: x" + Config.fieldproducescale);
         }
      };
      (new CheckBox(new Coord(0, 100), tab, "Show gob paths") {
         {
            this.tooltip = Text.render("Show paths of moving entities of the world.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.showgobpath = val;
            Utils.setprefb("showgobpath", val);
         }
      }).a = Config.showgobpath;
      (new CheckBox(new Coord(0, 120), tab, "Always show purity percentage/multiplier") {
         {
            this.tooltip = Text.render("Always shows the purity on inventory items (as a percentage or as a multiplier, depending on the setting).");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.alwaysshowpurity = val;
            Utils.setprefb("alwaysshowpurity", val);
         }
      }).a = Config.alwaysshowpurity;
      (new CheckBox(new Coord(0, 140), tab, "Laptop mode for the mouse") {
         {
            this.tooltip = Text.render("Switches the mode for the mouse and world interactions");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.laptopcontrols = val;
            Utils.setprefb("laptopcontrols", val);
         }
      }).a = Config.laptopcontrols;
      new Label(new Coord(10, 160), tab, "Laptop controls: Move the camera by pressing LMB, then dragging RMB.");
      new Label(new Coord(10, 170), tab, "Zoom in with + and out with -, and rotate objects like that while pressing shift.");
      new Label(new Coord(10, 180), tab, "Rotate in precise mode by pressing shift-alt rather than shift-ctrl.");
      (new CheckBox(new Coord(0, 200), tab, "Raider mode trees") {
         {
            this.tooltip = Text.render("All trees are rendered as tiny versions of themselves. Re-load your area after changing.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.raidermodetrees = val;
            Utils.setprefb("raidermodetrees", val);
         }
      }).a = Config.raidermodetrees;
      (new CheckBox(new Coord(150, 200), tab, "Raider mode braziers") {
         {
            this.tooltip = Text.render("Braziers are rendered in hot pink.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.raidermodebraziers = val;
            Utils.setprefb("raidermodebraziers", val);
         }
      }).a = Config.raidermodebraziers;
      (new CheckBox(new Coord(300, 200), tab, "Farmer mode trees/bushes") {
         {
            this.tooltip = Text.render("Fruit-bearing trees and flowered thornbushes are made clear.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.farmermodetrees = val;
            Utils.setprefb("farmermodetrees", val);
         }
      }).a = Config.farmermodetrees;
      (new CheckBox(new Coord(150, 220), tab, "Show ridges on the minimap.") {
         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.localmm_ridges = val;
            Utils.setprefb("localmm_ridges", val);
         }
      }).a = Config.localmm_ridges;
      (new CheckBox(new Coord(0, 220), tab, "Alternate prospecting") {
         {
            this.tooltip = Text.render("Shows the rough direction and the pie slice to search in, rather than the erratic arrow. Give it some time!");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.altprosp = val;
            Utils.setprefb("altprosp", val);
         }
      }).a = Config.altprosp;
      (new CheckBox(new Coord(0, 240), tab, "Enable tracking on log-in") {
         {
            this.tooltip = Text.render("Enable tracking as soon as a character logs in.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.alwaystrack = val;
            Utils.setprefb("alwaystrack", val);
         }
      }).a = Config.alwaystrack;
      (new CheckBox(new Coord(0, 260), tab, "Lower framerate on unfocused instances") {
         {
            this.tooltip = Text.render("Lowers the target framerate for unfocused windows from 50 to 10.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.slowmin = val;
            Utils.setprefb("slowmin", val);
         }
      }).a = Config.slowmin;
      (new CheckBox(new Coord(0, 280), tab, "Always face the primary target") {
         {
            this.tooltip = Text.render("Always face the target at the top of the aggro list. WARNING: will not work when not rendering, e.g. when minimized.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.watchguard = val;
            Utils.setprefb("watchguard", val);
         }
      }).a = Config.watchguard;
      (new CheckBox(new Coord(0, 300), tab, "Fast flower menus") {
         {
            this.tooltip = Text.render("Get rid of the delays when opening flower menus.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.fast_menu = val;
            Utils.setprefb("fast_flowers", val);
         }
      }).a = Config.fast_menu;
      (new CheckBox(new Coord(0, 320), tab, "Mute the violin player") {
         {
            this.tooltip = Text.render("The violin player will lose his strings. Please remember that he has a family of his own to support!");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.mute_violin = val;
            Utils.setprefb("mute_violin", val);
         }
      }).a = Config.mute_violin;
      (new CheckBox(new Coord(0, 340), tab, "Log all chat messages to file") {
            {
               this.tooltip = Text.render(
                  "Chat messages will be available in \"C:\\Users\\<account>\\Salem\\logs\\<character>\\<channel>\\\" or similar.\nChanges only apply to new channels."
               );
            }

            @Override
            public void changed(boolean val) {
               super.changed(val);
               Config.chatlogs = val;
               Utils.setprefb("chatlogs", val);
            }
         })
         .a = Config.chatlogs;
      RadioGroup fontsizes = new RadioGroup(tab) {
         @Override
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
      new Label(new Coord(280, 40), tab, "Chat font size:");
      fontsizes.add("Base size 12", new Coord(300, 60));
      fontsizes.add("Base size 14", new Coord(300, 85));
      fontsizes.add("Base size 16", new Coord(300, 110));
      fontsizes.add("Base size 20", new Coord(300, 135));
      x = (int)Utils.getpreff("chatfontsize", 12.0F);
      fontsizes.check("Base size " + x);
      (new CheckBox(new Coord(300, 240), tab, "Remove all animations") {
         {
            this.tooltip = Text.render("Removes all animations of more than a single frame, should ease processing times.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.remove_animations = val;
            Utils.setprefb("remove_animations", val);
         }
      }).a = Config.remove_animations;
      (new CheckBox(new Coord(300, 260), tab, "Hide the minimap") {
         {
            this.tooltip = Text.render("The minimap will not be rendered.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.hide_minimap = val;
            Utils.setprefb("hide_minimap", val);
            this.ui.gui.updateRenderFilter();
         }
      }).a = Config.hide_minimap;
      (new CheckBox(new Coord(300, 280), tab, "Hide the humours") {
         {
            this.tooltip = Text.render("The humours will not be rendered.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.hide_tempers = val;
            Utils.setprefb("hide_tempers", val);
            this.ui.gui.updateRenderFilter();
         }
      }).a = Config.hide_tempers;
      (new CheckBox(new Coord(300, 300), tab, "Enable continuous sorting.") {
         {
            this.tooltip = Text.render("Toggle between on-demand sorting and continuous sorting.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.alwayssort = val;
            Utils.setprefb("alwayssort", val);
         }
      }).a = Config.alwayssort;
      (new CheckBox(new Coord(300, 320), tab, "Picky Alt modifier.") {
         {
            this.tooltip = Text.render("The alt modifier will now only take items which contain the same ingredients, as well as having the same name.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.pickyalt = val;
            Utils.setprefb("pickyalt", val);
         }
      }).a = Config.pickyalt;
      (new CheckBox(new Coord(300, 340), tab, "Combat radii for human characters.") {
         {
            this.tooltip = Text.render("Any body will now show area of effect for roundhouse kick, cleave, and stomp.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.borka_radii = val;
            Utils.setprefb("borka_radii", val);
         }
      }).a = Config.borka_radii;
      this.makeRadarTab();
      tab = this.body.new Tab(new Coord(360, 0), 60, "Hotkeys");
      new Label(new Coord(10, 25), tab, "Enter commands to execute for configureable hotkeys.");
      new Label(new Coord(10, 35), tab, "Use your hotkeys through shift+ctrl+<key>.");
      new Label(new Coord(10, 45), tab, "Only single-character capitalized hotkeys will work.");
      new Label(new Coord(10, 55), tab, "Hotkeys in use by the UI itself are fixed and immutable.");
      int yxxx = 85;

      for (final int i = 0; i < 6; i++) {
         final String hname = String.format("hotkey%d", i + 1);
         final String hcommand = String.format("command%d", i + 1);
         String lt = String.format("Hotkey %d:", i + 1);
         new Label(new Coord(10, yxxx), tab, lt);
         new TextEntry(new Coord(60, yxxx), 30, tab, Config.hnames[i]) {
            @Override
            public void changed() {
               if (super.text.length() > 0) {
                  Utils.setpref(hname, super.text.substring(0, 1));
                  Config.hnames[i] = super.text;
               }
            }
         };
         String var71 = String.format("Command %d:", i + 1);
         new Label(new Coord(105, yxxx), tab, var71);
         new TextEntry(new Coord(170, yxxx), 150, tab, Config.hcommands[i]) {
            @Override
            public void changed() {
               if (super.text.length() > 0) {
                  Utils.setpref(hcommand, super.text);
                  Config.hcommands[i] = super.text;
               }
            }
         };
         yxxx += 25;
      }

      Tabs var10003 = this.body;
      this.body.getClass();
      tab = new Tabs.Tab(var10003, new Coord(430, 0), 60, "Cheats") {
         FlowerList list;
         Button add;
         TextEntry value;

         {
            x0.getClass();
            this.list = new FlowerList(new Coord(200, 55), this);
            this.add = new Button(new Coord(355, 308), 45, this, "Add");
            this.value = new TextEntry(new Coord(200, 310), 150, this, "");
            this.value.canactivate = true;
         }

         @Override
         public void wdgmsg(Widget sender, String msg, Object... args) {
            if ((sender == this.add || sender == this.value) && msg.equals("activate")) {
               this.list.add(this.value.text);
               this.value.settext("");
            } else {
               super.wdgmsg(sender, msg, args);
            }
         }
      };
      new Label(new Coord(200, 30), tab, "Choose menu items to select automatically:");
      int yxxxx = 5;
      int var24 = yxxxx + 25;
      (new CheckBox(new Coord(0, var24), tab, "Auto sift") {
         {
            this.tooltip = Text.render("Clicks on ground with sift cursor will be repeated until non-sift click received.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.autosift = val;
            Utils.setprefb("autosift", val);
         }
      }).a = Config.autosift;
      int var25 = var24 + 25;
      (new CheckBox(new Coord(0, var25), tab, "Auto bucket") {
         {
            this.tooltip = Text.render("Right-clicks on ground when you put an empty bucket on the cursor and are standing over water.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.autobucket = val;
            Utils.setprefb("autobucket", val);
         }
      }).a = Config.autobucket;
      int var26 = var25 + 25;
      (new CheckBox(new Coord(0, var26), tab, "Show actor path") {
         {
            this.tooltip = Text.render("Will draw line to position where actor is moving.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.gobpath = val;
            Utils.setprefb("gobpath", val);
            OptWnd2.this.gob_path_color.enabled = val;
         }
      }).a = Config.gobpath;
      int var27 = var26 + 25;
      this.gob_path_color = new CheckBox(new Coord(10, var27), tab, "Use kin color") {
         {
            this.tooltip = Text.render("Will draw actor path using color from kin list.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.gobpath_color = val;
            Utils.setprefb("gobpath_color", val);
         }
      };
      this.gob_path_color.a = Config.gobpath_color;
      this.gob_path_color.enabled = Config.gobpath;
      int var28 = var27 + 25;
      new Button(new Coord(10, var28), 75, tab, "options") {
         @Override
         public void click() {
            GobPathOptWnd.toggle();
         }
      };
      int var29 = var28 + 35;
      (new CheckBox(new Coord(0, var29), tab, "Auto drop bats") {
         {
            this.tooltip = Text.render("Will automatically drop bats that sit on your neck.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.auto_drop_bats = val;
            Utils.setprefb("auto_drop_bats", val);
         }
      }).a = Config.auto_drop_bats;
      int var30 = var29 + 35;
      (new CheckBox(new Coord(0, var30), tab, "Auto logout") {
         {
            this.tooltip = Text.render("Will automatically log you out if you do not interact with the client for 10 minutes.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.autolog = val;
            Utils.setprefb("autolog", val);
         }
      }).a = Config.autolog;
      int var31 = var30 + 35;
      (new CheckBox(new Coord(0, var31), tab, "Single item CTRL choose") {
         {
            this.tooltip = Text.render("If checked, will automatically select single item menus if CTRL is pressed when menu is opened.");
         }

         @Override
         public void changed(boolean val) {
            super.changed(val);
            Config.singleItemCTRLChoose = val;
            Utils.setprefb("singleItemCTRLChoose", val);
         }
      }).a = Config.singleItemCTRLChoose;
      String last = Utils.getpref("optwndtab", "");

      for (Tabs.Tab t : this.body.tabs) {
         if (t.btn.text.text.equals(last)) {
            this.body.showtab(t);
         }
      }
   }

   public static void setRadarInfo(RadarConfig rcf, MarkerFactory mf) {
      rc = rcf;
      OptWnd2.mf = mf;
      if (instance != null) {
         instance.makeRadarTab();
      }
   }

   private void makeRadarTab() {
      if (rc != null) {
         boolean viewingradartab = this.body.curtab == this.radartab;
         if (this.radartab != null) {
            boolean success = this.body.tabs.remove(this.radartab);
            System.out.println("Removed the radartab step 1: " + success);
            this.radartab.unlink();
            this.radartab.btn.destroy();
            this.radartab.destroy();
         }

         this.radartab = this.body.new Tab(new Coord(280, 0), 70, "Radar config");
         int x = 0;
         int y = 35;

         for (final ConfigGroup cg : rc.getGroups()) {
            (new CheckBox(new Coord(x, y), this.radartab, cg.name) {
               @Override
               public void changed(boolean val) {
                  super.changed(val);
                  cg.show = val;

                  for (ConfigMarker cm : cg.markers) {
                     cm.show = val;
                  }

                  if (OptWnd2.mf != null) {
                     OptWnd2.mf.setConfig(OptWnd2.rc);
                  }
               }
            }).a = cg.show;
            y += 25;
            if (y > this.radartab.sz.y) {
               y = 35;
               x += 100;
            }
         }

         if (viewingradartab) {
            this.body.showtab(this.radartab);
         }
      }
   }

   private static void checkVideoOpt(CheckBox check, GLSettings.BoolSetting setting) {
      checkVideoOpt(check, setting, null);
   }

   private static void checkVideoOpt(CheckBox check, GLSettings.BoolSetting setting, Object tooltip) {
      try {
         setting.validate(true);
         check.enabled = true;
         check.tooltip = tooltip;
      } catch (GLSettings.SettingException var4) {
         check.enabled = false;
         check.tooltip = Text.render(var4.getMessage());
      }
   }

   private void setcamera(String camtype) {
      this.curcam = camtype;
      Utils.setpref("defcam", this.curcam);
      MapView mv = this.ui.gui.map;
      if (mv != null) {
         mv.setcam(this.curcam);
      }
   }

   private int getsfxvol() {
      return (int)(100.0 - Double.parseDouble(Utils.getpref("sfxvol", "1.0")) * 100.0);
   }

   private void addinfo(String camtype, String title, String text, Tabs.Tab args) {
      this.caminfomap.put(camtype, new OptWnd2.CamInfo(title, text, args));
      this.camname2type.put(title, camtype);
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.cbtn) {
         super.wdgmsg(sender, msg, args);
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

   @Override
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

   public static class Frame extends Widget {
      private IBox box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
      private Color bgcoplor;

      public Frame(Coord c, Coord sz, Widget parent) {
         super(c, sz, parent);
      }

      public Frame(Coord c, Coord sz, Color bg, Widget parent) {
         this(c, sz, parent);
         this.bgcoplor = bg;
      }

      @Override
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
}
