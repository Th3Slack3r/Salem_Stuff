package haven;

import java.awt.event.KeyEvent;

public class RootWidget extends ConsoleHost {
  public static Resource defcurs = Resource.load("gfx/hud/curs/arw");
  
  Logout logout = null;
  
  Profile gprof;
  
  public RootWidget(UI ui, Coord sz) {
    super(ui, new Coord(0, 0), sz);
    setfocusctl(true);
    this.cursor = defcurs;
  }
  
  public boolean globtype(char key, KeyEvent ev) {
    int code = ev.getKeyCode();
    boolean ctrl = ev.isControlDown();
    boolean shift = ev.isShiftDown();
    boolean isgui = (this.ui != null && this.ui.gui != null);
    boolean alt = ev.isAltDown();
    if (!super.globtype(key, ev)) {
      if (key == '\000' && (!ctrl || !alt || !Utils.isCustomHotkey(ev)))
        return false; 
      if (Config.profile && key == '`') {
        new Profwnd(new Coord(100, 100), this, this.gprof, "Glob prof");
      } else if (Config.profile && key == '~') {
        GameUI gi = this.ui.gui;
        if (gi != null && gi.map != null)
          new Profwnd(new Coord(100, 100), this, gi.map.prof, "MV prof"); 
      } else if (key == ':') {
        entercmd();
      } else if (isgui && this.ui.rwidgets.containsKey(this.ui.gui) && Utils.isCustomHotkey(ev)) {
        for (HotkeyList.HotkeyJItem hkji : Config.HOTKEYLIST) {
          if (hkji.alt != alt)
            continue; 
          if (hkji.shift != shift)
            continue; 
          if (hkji.ctrl != ctrl)
            continue; 
          if (hkji.key.length() == 1 && ev.getKeyCode() == hkji.key.charAt(0))
            try {
              this.ui.cons.run(hkji.command);
            } catch (Exception ex) {
              System.out.println("Console not cooperating!");
            }  
        } 
      } else if (isgui && (code == 76 || code == 70) && ctrl) {
        FlatnessTool ft = FlatnessTool.instance(this.ui);
        if (ft != null)
          ft.toggle(); 
      } else if (isgui && code == 65 && ctrl && !shift) {
        OverviewTool ot = OverviewTool.instance(this.ui);
        if (ot != null)
          ot.toggle(); 
      } else if (isgui && code == 88 && ctrl && !shift) {
        CartographWindow.toggle();
      } else if (isgui && code == 68 && ctrl && !shift) {
        DarknessWnd.toggle();
      } else if (isgui && code == 78 && ctrl && shift) {
        Config.use_old_night_vision = !Config.use_old_night_vision;
        Utils.setprefb("use_old_night_vision", Config.use_old_night_vision);
        if (Config.use_old_night_vision) {
          Utils.msgOut("Bright-Mode: Old");
        } else {
          Utils.msgOut("Bright-Mode: New");
        } 
      } else if (isgui && code == 78 && ctrl && !shift) {
        if (Config.use_old_night_vision) {
          if (Config.old_night_vision_level != 0.0F) {
            Config.old_night_vision_level++;
            if (Config.old_night_vision_level >= 5.0F)
              Config.old_night_vision_level = 0.0F; 
          } else {
            Config.old_night_vision_level = 1.0F;
          } 
          Utils.setpreff("old_night_vision_level", Config.old_night_vision_level);
          if (Config.old_night_vision_level == 0.0F) {
            Utils.msgOut("Bright-Mode (Old) is: OFF");
          } else if (Config.old_night_vision_level == 1.0F) {
            Utils.msgOut("Bright-Mode (Old) is: 1 (pointing West)");
          } else if (Config.old_night_vision_level == 2.0F) {
            Utils.msgOut("Bright-Mode (Old) is: 2 (pointing South)");
          } else if (Config.old_night_vision_level == 3.0F) {
            Utils.msgOut("Bright-Mode (Old) is: 3 (pointing East)");
          } else {
            Utils.msgOut("Bright-Mode (Old) is: 4 (pointing North)");
          } 
          this.ui.sess.glob.brighten();
        } else {
          boolean mt = this.ui.gui.map.isTileInsideMine();
          if (mt) {
            if (Config.alwaysbright_in) {
              Config.brightang_in++;
              if (Config.brightang_in >= 7.0F) {
                Config.alwaysbright_in = false;
                Config.brightang_in = 0.0F;
              } 
            } else {
              Config.alwaysbright_in = true;
            } 
            Utils.msgOut("Bright-Mode (INSIDE Mines) is: " + (Config.alwaysbright_in ? ("ON - brigtness lvl: " + (Config.brightang_in + 1.0F)) : "OFF"));
            Utils.setprefb("alwaysbright_in", Config.alwaysbright_in);
            Utils.setpreff("brightang_in", Config.brightang_in);
          } else {
            if (Config.alwaysbright_out) {
              Config.brightang_out++;
              if (Config.brightang_out >= 7.0F) {
                Config.alwaysbright_out = false;
                Config.brightang_out = 0.0F;
              } 
            } else {
              Config.alwaysbright_out = true;
            } 
            Utils.msgOut("Bright-Mode (OUTSIDE Mines is: " + (Config.alwaysbright_out ? ("ON - brigtness lvl: " + (Config.brightang_out + 1.0F)) : "OFF"));
            Utils.setprefb("alwaysbright_out", Config.alwaysbright_out);
            Utils.setpreff("brightang_out", Config.brightang_out);
          } 
          this.ui.sess.glob.brighten();
        } 
      } else if (!isgui || code != 67 || !alt) {
        if (code == 82 && alt) {
          Config.toggleRadius();
        } else if (code == 67 && alt && isgui) {
          this.ui.gui.toggleCraftWnd();
        } else if (code == 70 && alt && isgui) {
          this.ui.gui.toggleFilterWnd();
        } else if (code == 82 && ctrl && isgui) {
          Window toolbelt_window = null;
          for (Widget w : this.ui.widgets.values()) {
            if (Window.class.isInstance(w)) {
              Window ww = (Window)w;
              if (ww.cap.text.toLowerCase().contains("belt") || ww.cap.text.toLowerCase().contains("sash") || ww.cap.text.toLowerCase().contains("pouch"))
                toolbelt_window = ww; 
            } 
          } 
          if (toolbelt_window == null) {
            if ((this.ui.gui.getEquipory()).slots[5] != null)
              (this.ui.gui.getEquipory()).slots[5].mousedown(Coord.z, 3); 
          } else {
            toolbelt_window.cbtn.click();
          } 
        } else if (code == 71 && ctrl && isgui) {
          Window toolbelt_backpack = null;
          for (Widget w : this.ui.widgets.values()) {
            if (Window.class.isInstance(w)) {
              Window ww = (Window)w;
              if (Utils.isBackPack(ww))
                toolbelt_backpack = ww; 
            } 
          } 
          if (toolbelt_backpack == null) {
            if ((this.ui.gui.getEquipory()).slots[14] != null)
              (this.ui.gui.getEquipory()).slots[14].mousedown(Coord.z, 3); 
          } else {
            toolbelt_backpack.cbtn.click();
          } 
        } else if (code == 90 && ctrl) {
          Config.center = !Config.center;
          this.ui.message(String.format("Tile centering is turned %s", new Object[] { Config.center ? "ON" : "OFF" }), GameUI.MsgType.INFO);
        } else if (code == 32) {
          SmartSpace.work();
        } else if (key != '\000') {
          wdgmsg("gk", new Object[] { Integer.valueOf(key) });
        } 
      } 
    } 
    return true;
  }
  
  public boolean keyup(KeyEvent ev) {
    if (ev.getKeyCode() == 154) {
      Screenshooter.take(this.ui.gui, Config.screenurl);
      return true;
    } 
    return super.keyup(ev);
  }
  
  public void draw(GOut g) {
    super.draw(g);
    drawcmd(g, new Coord(20, this.sz.y - 20));
  }
  
  public void error(String msg) {}
}
