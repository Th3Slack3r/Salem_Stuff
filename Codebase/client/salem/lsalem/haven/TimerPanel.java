package haven;

import java.awt.image.BufferedImage;
import org.ender.timer.Timer;
import org.ender.timer.TimerController;

public class TimerPanel extends Window {
   private static TimerPanel instance;
   private Button btnnew;
   private IButton lockbtn;
   private IButton soundbtn;
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

   public static TimerPanel getInstance() {
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

   private TimerPanel(Widget parent) {
      super(new Coord(250, 100), Coord.z, parent, "Timers");
      this.justclose = true;
      this.btnnew = new Button(Coord.z, 100, this, "Add timer");
      this.lockbtn = new IButton(Coord.z, this, locked ? ilockc : ilocko, locked ? ilocko : ilockc, locked ? ilockch : ilockoh) {
         {
            this.tooltip = Text.render("Whether to protect timers from accidental deletion.");
         }

         @Override
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
         {
            this.tooltip = Text.render("Whether to play sound on timer finish: timer.wav is played, can be overwritten in ~/Salem");
         }

         @Override
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
      synchronized (TimerController.getInstance().lock) {
         for (Timer timer : TimerController.getInstance().timers) {
            new TimerWdg(Coord.z, this, timer);
         }
      }

      this.pack();
   }

   public static boolean isDeletionLocked() {
      return locked;
   }

   public static boolean isSilenced() {
      return silenced;
   }

   @Override
   protected void loadOpts() {
      super.loadOpts();
      synchronized (Config.window_props) {
         locked = this.getOptBool("_locked", false);
      }
   }

   @Override
   public void pack() {
      int i = 0;
      int h = 0;
      int n;
      synchronized (TimerController.getInstance().lock) {
         n = TimerController.getInstance().timers.size();
      }

      n = (int)Math.ceil(Math.sqrt(n / 3.0));

      for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
         if (wdg instanceof TimerWdg) {
            wdg.c = new Coord(i % n * wdg.sz.x, i / n * wdg.sz.y);
            h = wdg.c.y + wdg.sz.y;
            i++;
         }
      }

      this.btnnew.c = new Coord(0, h + 4);
      this.lockbtn.c = new Coord(this.btnnew.sz.x + 4, h + 6);
      this.soundbtn.c = new Coord(this.btnnew.sz.x + 24, h + 6);
      super.pack();
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.btnnew) {
         new TimerPanel.TimerAddWdg(this.c, this.ui.root, this);
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   @Override
   public void destroy() {
      instance = null;
      super.destroy();
   }

   public static void close() {
      if (instance != null) {
         instance.destroy();
      }
   }

   static {
      synchronized (Config.window_props) {
         try {
            silenced = Config.window_props.getProperty("Timers_sounded", null).equals("true");
         } catch (Exception var3) {
            silenced = true;
         }
      }
   }

   class TimerAddWdg extends Window {
      private TextEntry name;
      private TextEntry hours;
      private TextEntry minutes;
      private TextEntry seconds;
      private Button btnadd;
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
         this.btnadd = new Button(new Coord(0, 60), 100, this, "Add");
         this.pack();
      }

      @Override
      public void wdgmsg(Widget sender, String msg, Object... args) {
         if (sender == this.btnadd) {
            try {
               long time = 0L;
               time += Integer.parseInt(this.seconds.text);
               time += Integer.parseInt(this.minutes.text) * 60;
               time += Integer.parseInt(this.hours.text) * 3600;
               Timer timer = new Timer();
               timer.setDuration(1000L * time);
               timer.setName(this.name.text);
               TimerController.getInstance().add(timer);
               new TimerWdg(Coord.z, this.panel, timer);
               this.panel.pack();
               this.ui.destroy(this);
            } catch (Exception var7) {
               System.out.println(var7.getMessage());
               var7.printStackTrace(System.out);
            }
         } else if (sender == this.cbtn) {
            TimerPanel.toggle();
         } else {
            super.wdgmsg(sender, msg, args);
         }
      }

      @Override
      public void destroy() {
         this.panel = null;
         super.destroy();
      }
   }
}
