package haven;

public class LogChatFilterOptWnd extends Window {
  private static Window instance;
  
  public static CheckBox aCB = null;
  
  public static void toggle() {
    if (instance != null && instance.parent != UI.instance.gui)
      instance.destroy(); 
    if (instance == null) {
      instance = new LogChatFilterOptWnd(Coord.z, UI.instance.gui);
    } else {
      UI.instance.destroy(instance);
    } 
  }
  
  public LogChatFilterOptWnd(Coord c, Widget parent) {
    super(c, Coord.z, parent, "Log channel filter options");
    this.justclose = true;
    int y = 0;
    y += 5;
    new Label(new Coord(0, y), this, "Check to filter out things from the Log channel:");
    y += 20;
    aCB = new CheckBox(new Coord(0, y), this, "Filter Fertilizers") {
        public void changed(boolean val) {
          super.changed(val);
          Config.ft_filter_fertilizers = val;
          Utils.setprefb("ft_filter_fertilizers", val);
          this.tooltip = Text.render("If checked, fertilizers will be filtered out and not shown on the Log channel.");
        }
      };
    aCB.a = Config.ft_filter_fertilizers;
    pack();
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
}
