package haven;

public class FPSDisplayOptWnd extends Window {
  private static Window instance;
  
  private HSlider hSlider = null;
  
  public static CheckBox fpsCB = null;
  
  public static void toggle() {
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance == null) {
      instance = new FPSDisplayOptWnd(Coord.z, UI.instance.gui);
    } else {
      UI.instance.destroy(instance);
    } 
  }
  
  public FPSDisplayOptWnd(Coord c, Widget parent) {
    super(c, Coord.z, parent, "FPS Display Configuration");
    this.justclose = true;
    (new CheckBox(new Coord(150, 0), this, "Hide additional Info") {
        public void changed(boolean val) {
          super.changed(val);
          Config.fps_display_only_fps = val;
          Utils.setprefb("fps_display_only_fps", val);
          HavenPanel.fpsOnlyFPS = val;
        }
      }).a = Config.fps_display_only_fps;
    (fpsCB = new CheckBox(new Coord(0, 0), this, "Show FPS Display") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.changeDisplayFPS(Boolean.valueOf(val));
        }
      }).a = Config.fps_display_show;
    new Label(new Coord(0, 25), this, "Y value:");
    this.hSlider = new HSlider(new Coord(0, 50), 400, this, 0, 400, Config.fps_display_offset_y * 4) {
        public void changed() {
          int value = this.val / 4;
          if (value < 0) {
            value = 0;
          } else if (value > 100) {
            value = 100;
          } 
          Config.fps_display_offset_y = value;
          Utils.setprefi("fps_display_offset_y", value);
          HavenPanel.fpsDisplayOffsetY = value;
        }
      };
    new Label(new Coord(0, 75), this, "X value:");
    this.hSlider = new HSlider(new Coord(0, 100), 400, this, 0, 400, Config.fps_display_offset_x * 4) {
        public void changed() {
          int value = this.val / 4;
          if (value < 0) {
            value = 0;
          } else if (value > 100) {
            value = 100;
          } 
          Config.fps_display_offset_x = value;
          Utils.setprefi("fps_display_offset_x", value);
          HavenPanel.fpsDisplayOffsetX = value;
        }
      };
    pack();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
}
