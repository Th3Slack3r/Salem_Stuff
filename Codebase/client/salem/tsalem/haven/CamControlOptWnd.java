package haven;

public class CamControlOptWnd extends Window {
  private static Window instance;
  
  private HSlider hSlider = null;
  
  public static void toggle() {
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance == null) {
      instance = new CamControlOptWnd(Coord.z, UI.instance.gui);
    } else {
      UI.instance.destroy(instance);
    } 
  }
  
  public static void refreshHard() {
    if (instance != null && instance.parent != UI.instance.gui) {
      instance.destroy();
      instance = null;
      return;
    } 
    if (instance != null) {
      instance.destroy();
      instance = null;
      instance = new CamControlOptWnd(Coord.z, UI.instance.gui);
    } 
  }
  
  public CamControlOptWnd(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Cam Control Options");
    this.justclose = true;
    if (OptWnd2.curcam == null || OptWnd2.curcam.length() <= 0)
      OptWnd2.curcam = Utils.getpref("defcam", "sortho"); 
    int angle = MapView.Camera.angleModC;
    int elev = MapView.Camera.elevModC;
    int dist = MapView.Camera.distModC;
    int scroll = MapView.Camera.scrollModC;
    int y = 0;
    new Label(new Coord(0, y), this, "Here you can define the default values for all cams.");
    y += 25;
    new Label(new Coord(0, y), this, "Current cam is: " + OptWnd2.curcam);
    y += 25;
    new Label(new Coord(0, y), this, "Angle (Left-Right):");
    y += 25;
    this.hSlider = new HSlider(new Coord(0, y), 400, this, 0, 40, angle) {
        public void changed() {
          MapView.Camera.angleModC = this.val;
          CamControlOptWnd.saveMe("Angle", "ï¿½", this.val, 9);
        }
      };
    if (MapView.Camera.usesElevMod) {
      y += 25;
      new Label(new Coord(0, y), this, "Elevation (Up-Down):");
      y += 25;
      this.hSlider = new HSlider(new Coord(0, y), 400, this, 0, 30, elev) {
          public void changed() {
            int value = this.val;
            MapView.Camera.elevModC = value;
            CamControlOptWnd.saveMe("Elevation", "ï¿½", this.val, 3);
          }
        };
    } 
    y += 25;
    new Label(new Coord(0, y), this, "Distance");
    y += 25;
    this.hSlider = new HSlider(new Coord(0, y), 400, this, 0, 400, dist / 10) {
        public void changed() {
          int value = this.val * 10;
          MapView.Camera.distModC = value;
          CamControlOptWnd.saveMe("Distance", "", value, 1);
        }
      };
    y += 25;
    new Label(new Coord(0, y), this, "Scroll Speed");
    y += 25;
    this.hSlider = new HSlider(new Coord(0, y), 400, this, 0, 40, scroll - 1) {
        public void changed() {
          int value = this.val + 1;
          MapView.Camera.scrollModC = value;
          CamControlOptWnd.saveMe("Scroll Speed", " x", value, 1);
        }
      };
    pack();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  private static void saveMe(String variableName, String unit, int val, int multiplier) {
    MapView.Camera.saveValues(OptWnd2.curcam);
    UI.instance.message(variableName + ": " + (val * multiplier) + unit, GameUI.MsgType.INFO);
    MapView mv = UI.instance.gui.map;
    if (mv != null)
      mv.setcam(OptWnd2.curcam); 
  }
}
