package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.ender.timer.Timer;
import org.ender.timer.TimerController;

public class TimerPanel extends Window {
  private static TimerPanel instance;
  
  private final Button btnnew;
  
  private final Button btnreload;
  
  private final IButton lockbtn;
  
  private final IButton soundbtn;
  
  private static final BufferedImage ilockc = Resource.loadimg("gfx/hud/lockc");
  
  private static final BufferedImage ilockch = Resource.loadimg("gfx/hud/lockch");
  
  private static final BufferedImage ilocko = Resource.loadimg("gfx/hud/locko");
  
  private static final BufferedImage ilockoh = Resource.loadimg("gfx/hud/lockoh");
  
  private static final String OPT_LOCKED = "_locked";
  
  private static final BufferedImage isoundc = Resource.loadimg("gfx/hud/soundc");
  
  private static final BufferedImage isoundch = Resource.loadimg("gfx/hud/soundch");
  
  private static final BufferedImage isoundo = Resource.loadimg("gfx/hud/soundo");
  
  private static final BufferedImage isoundoh = Resource.loadimg("gfx/hud/soundoh");
  
  private static final String OPT_SOUNDED = "_sounded";
  
  static boolean locked;
  
  static boolean silenced;
  
  static {
    synchronized (Config.window_props) {
      try {
        silenced = Config.window_props.getProperty("Timers_sounded", null).equals("true");
      } catch (Exception e) {
        silenced = true;
      } 
    } 
  }
  
  public static TimerPanel getInstance() {
    if (instance != null && instance.parent != null && instance.parent != UI.instance.gui) {
      instance.destroy();
      instance = null;
    } 
    if (instance == null) {
      instance = new TimerPanel(UI.instance.gui);
      instance.visible = false;
    } 
    return instance;
  }
  
  public static void toggle() {
    getInstance();
    instance.visible = !instance.visible;
  }
  
  public static void reFresh() {
    TimerController.getInstance().load();
    boolean visibility = false;
    if (instance != null) {
      visibility = instance.visible;
      instance.destroy();
      instance = null;
      toggle();
      instance.visible = visibility;
    } 
  }
  
  private TimerPanel(Widget parent) {
    super(new Coord(250, 100), Coord.z, parent, "Timers");
    this.justclose = true;
    this.btnnew = new Button(Coord.z, Integer.valueOf(100), this, "Add timer");
    this.btnreload = new Button(Coord.z, Integer.valueOf(100), this, "Reload");
    this.lockbtn = new IButton(Coord.z, this, locked ? ilockc : ilocko, locked ? ilocko : ilockc, locked ? ilockch : ilockoh) {
        public void click() {
          TimerPanel.locked = !TimerPanel.locked;
          if (TimerPanel.locked) {
            this.up = TimerPanel.ilockc;
            this.down = TimerPanel.ilocko;
            this.hover = TimerPanel.ilockch;
          } else {
            this.up = TimerPanel.ilocko;
            this.down = TimerPanel.ilockc;
            this.hover = TimerPanel.ilockoh;
          } 
          TimerPanel.this.storeOpt("_locked", TimerPanel.locked);
        }
      };
    this.lockbtn.recthit = true;
    this.soundbtn = new IButton(Coord.z, this, silenced ? isoundc : isoundo, silenced ? isoundo : isoundc, silenced ? isoundch : isoundoh) {
        public void click() {
          TimerPanel.silenced = !TimerPanel.silenced;
          if (TimerPanel.silenced) {
            this.up = TimerPanel.isoundc;
            this.down = TimerPanel.isoundo;
            this.hover = TimerPanel.isoundch;
          } else {
            this.up = TimerPanel.isoundo;
            this.down = TimerPanel.isoundc;
            this.hover = TimerPanel.isoundoh;
          } 
          TimerPanel.this.storeOpt("_sounded", TimerPanel.silenced);
        }
      };
    this.soundbtn.recthit = true;
    synchronized ((TimerController.getInstance()).lock) {
      String mCN = TimerController.charName;
      for (Timer timer : (TimerController.getInstance()).timers) {
        String tCN = timer.getCharName();
        if (tCN != null && tCN != "" && !tCN.equals(mCN)) {
          timer.updater = null;
          continue;
        } 
        new TimerWdg(Coord.z, this, timer);
      } 
    } 
    pack();
  }
  
  public static boolean isDeletionLocked() {
    return locked;
  }
  
  public static boolean isSilenced() {
    return silenced;
  }
  
  protected void loadOpts() {
    super.loadOpts();
    synchronized (Config.window_props) {
      locked = getOptBool("_locked", false);
    } 
  }
  
  public void pack() {
    int i = 0, h = 0;
    synchronized ((TimerController.getInstance()).lock) {
      n = (TimerController.getInstance()).timers.size();
      for (Timer timer : (TimerController.getInstance()).timers) {
        if (timer.getCharName() != null && !timer.getCharName().equals(TimerController.charName))
          n--; 
      } 
    } 
    int n = (int)Math.ceil(Math.sqrt(n / 3.0D));
    for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
      if (wdg instanceof TimerWdg) {
        wdg.c = new Coord(i % n * wdg.sz.x, i / n * wdg.sz.y);
        h = wdg.c.y + wdg.sz.y;
        i++;
      } 
    } 
    this.btnnew.c = new Coord(0, h + 4);
    this.btnreload.c = new Coord(100, h + 4);
    this.lockbtn.c = new Coord(204, h + 6);
    this.soundbtn.c = new Coord(224, h + 6);
    super.pack();
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.btnnew) {
      new TimerAddWdg(this.c, this.ui.root, this);
    } else if (sender == this.btnreload) {
      reFresh();
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  class TimerAddWdg extends Window {
    private final TextEntry name;
    
    private final TextEntry hours;
    
    private final TextEntry minutes;
    
    private final TextEntry seconds;
    
    private final Button btnadd;
    
    private final Button btnaddForCharOnly;
    
    private TimerPanel panel;
    
    public TimerAddWdg(Coord c, Widget parent, TimerPanel panel) {
      super(c, Coord.z, parent, "Add timer");
      this.justclose = true;
      this.panel = panel;
      this.name = new TextEntry(Coord.z, new Coord(150, 18), this, "timer");
      new Label(new Coord(0, 25), this, "hours");
      new Label(new Coord(50, 25), this, "min");
      new Label(new Coord(100, 25), this, "sec");
      this.hours = new TextEntry(new Coord(0, 40), new Coord(45, 18), this, "0");
      this.minutes = new TextEntry(new Coord(50, 40), new Coord(45, 18), this, "00");
      this.seconds = new TextEntry(new Coord(100, 40), new Coord(45, 18), this, "00");
      this.btnadd = new Button(new Coord(0, 60), Integer.valueOf(100), this, "Add");
      this.btnaddForCharOnly = new Button(new Coord(100, 60), Integer.valueOf(150), this, "Add for Char Only");
      pack();
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.btnadd || sender == this.btnaddForCharOnly) {
        try {
          long time = 0L;
          time += Integer.parseInt(this.seconds.text);
          time += (Integer.parseInt(this.minutes.text) * 60);
          time += (Integer.parseInt(this.hours.text) * 3600);
          Timer timer = new Timer();
          timer.setDuration(1000L * time);
          timer.setName(this.name.text);
          if (sender == this.btnaddForCharOnly)
            timer.setCharName(TimerController.charName); 
          TimerController.getInstance().add(timer);
          TimerWdg tW = new TimerWdg(Coord.z, this.panel, timer);
          if (sender == this.btnaddForCharOnly)
            tW.name.setcolor(Color.cyan); 
          this.panel.pack();
          this.ui.destroy(this);
        } catch (Exception e) {
          System.out.println(e.getMessage());
          e.printStackTrace(System.out);
        } 
      } else if (sender == this.cbtn) {
        this.ui.destroy(this);
      } else {
        super.wdgmsg(sender, msg, args);
      } 
    }
    
    public void destroy() {
      this.panel = null;
      super.destroy();
    }
  }
  
  public static void close() {
    if (instance != null)
      instance.destroy(); 
  }
}
