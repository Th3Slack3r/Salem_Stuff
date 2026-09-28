package haven;

public class RenderDistanceOptWnd extends Window {
  private static Window instance;
  
  private TextEntry textEntry = null;
  
  private HSlider hSlider = null;
  
  public static CheckBox aCB = null;
  
  public static void toggle() {
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance == null) {
      instance = new RenderDistanceOptWnd(Coord.z, UI.instance.gui);
    } else {
      UI.instance.destroy(instance);
    } 
  }
  
  public RenderDistanceOptWnd(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Render Distance Configuration");
    this.justclose = true;
    int y = 0;
    aCB = new CheckBox(new Coord(0, 0), this, "Activate Render Distance Limitation") {
        public void changed(boolean val) {
          super.changed(val);
          OptWnd2.OptUtil.changeRenderDistance(Boolean.valueOf(val));
        }
      };
    aCB.a = Config.render_distance_bool_value;
    new Label(new Coord(0, 25), this, "Render Distance as number:");
    this.textEntry = new TextEntry(new Coord(200, 25), 50, this, "" + Config.render_distance_int_value) {
        protected void changed() {
          if (this.text.length() > 0) {
            int value = 0;
            try {
              value = Integer.parseInt(this.text);
            } catch (Exception exception) {}
            if (value < 10) {
              value = 10;
            } else if (value > 50) {
              value = 50;
            } 
            Config.render_distance_int_value = value;
            Utils.setprefi("render_distance_int_value", value);
            OCache.maxDist = value * 11;
            RenderDistanceOptWnd.this.hSlider.val = (value - 10) * 10;
          } 
        }
      };
    new Label(new Coord(0, 50), this, "Render Distance Slider:                                ");
    y += 20;
    this.hSlider = new HSlider(new Coord(0, 75), 400, this, 0, 400, (Config.render_distance_int_value - 10) * 10) {
        public void changed() {
          int value = this.val / 10 + 10;
          if (value < 10) {
            value = 10;
          } else if (value > 50) {
            value = 50;
          } 
          Config.render_distance_int_value = value;
          Utils.setprefi("render_distance_int_value", value);
          OCache.maxDist = value * 11;
          RenderDistanceOptWnd.this.textEntry.rsettext("" + value);
        }
      };
    (new CheckBox(new Coord(0, 100), this, "De-activate flavour objects") {
        public void changed(boolean val) {
          super.changed(val);
          Config.mcache_no_flav = val;
          Utils.setprefb("mcache_no_flav", val);
          MCache.noFlav = val;
        }
      }).a = Config.mcache_no_flav;
    (new CheckBox(new Coord(0, 125), this, "Smaller ground tiles area displayed") {
        public void changed(boolean val) {
          super.changed(val);
          Config.mview_dist_small = val;
          Utils.setprefb("mview_dist_small", val);
          MapView.smallView = val;
        }
      }).a = Config.mview_dist_small;
    pack();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
}
