package haven;

public class FrameRateOptWnd extends Window {
  private static Window instance;
  
  private TextEntry textEntry = null;
  
  private HSlider hSlider = null;
  
  public static CheckBox aCB = null;
  
  public static void toggle() {
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance == null) {
      instance = new FrameRateOptWnd(Coord.z, UI.instance.gui);
    } else {
      UI.instance.destroy(instance);
    } 
  }
  
  public FrameRateOptWnd(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Frame Rate Configuration");
    this.justclose = true;
    new Label(new Coord(0, 25), this, "Target Frame Rate:");
    this.textEntry = new TextEntry(new Coord(200, 25), 50, this, "" + Config.custom_fps_target) {
        protected void changed() {
          if (this.text.length() > 0) {
            int value = 0;
            try {
              value = Integer.parseInt(this.text);
            } catch (Exception exception) {}
            if (value < 5) {
              value = 5;
            } else if (value > 300) {
              value = 300;
            } 
            Config.custom_fps_target = value;
            Utils.setprefi("custom_fps_target", value);
            FrameRateOptWnd.this.hSlider.val = value;
          } 
        }
      };
    new Label(new Coord(0, 50), this, "Target FPS Slider:                                ");
    this.hSlider = new HSlider(new Coord(0, 75), 300, this, 1, 300, Config.custom_fps_target) {
        public void changed() {
          int value = this.val;
          if (value < 1) {
            value = 1;
          } else if (value > 300) {
            value = 300;
          } 
          Config.custom_fps_target = value;
          Utils.setprefi("custom_fps_target", value);
          FrameRateOptWnd.this.textEntry.rsettext("" + value);
        }
      };
    new Button(new Coord(0, 100), Integer.valueOf(140), this, "Set to Default") {
        public void click() {
          Config.custom_fps_target = 50;
          Utils.setprefi("custom_fps_target", 50);
          FrameRateOptWnd.this.textEntry.rsettext("50");
          FrameRateOptWnd.this.hSlider.val = 50;
        }
      };
    pack();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
}
