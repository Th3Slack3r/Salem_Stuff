package haven;

import java.util.Date;
import org.ender.timer.Timer;
import org.ender.timer.TimerController;

public class TimerWdg extends Widget {
   static Tex bg = Resource.loadtex("gfx/hud/bosq");
   private Timer timer;
   public Label time;
   public Label name;
   private Button start;
   private Button stop;
   private Button delete;

   public TimerWdg(Coord c, Widget parent, Timer timer) {
      super(c, bg.sz(), parent);
      this.timer = timer;
      timer.updater = new Timer.Callback() {
         @Override
         public void run(Timer timer) {
            synchronized (TimerWdg.this.time) {
               TimerWdg.this.time.settext(timer.toString());
               TimerWdg.this.updbtns();
            }
         }
      };
      this.name = new Label(new Coord(5, 5), this, timer.getName());
      this.time = new Label(new Coord(5, 25), this, timer.toString());
      this.start = new Button(new Coord(125, 4), 50, this, "start");
      this.stop = new Button(new Coord(125, 4), 50, this, "stop");
      this.delete = new Button(new Coord(125, 27), 50, this, "delete");
      this.updbtns();
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      if (this.timer.isWorking()) {
         if (this.tooltip == null) {
            this.tooltip = Text.render(new Date(this.timer.getFinishDate()).toString()).tex();
         }

         return this.tooltip;
      } else {
         this.tooltip = null;
         return null;
      }
   }

   private void updbtns() {
      this.start.visible = !this.timer.isWorking();
      this.stop.visible = this.timer.isWorking();
   }

   @Override
   public void destroy() {
      this.unlink();
      Window wnd = this.getparent(Window.class);
      if (wnd != null) {
         wnd.pack();
      }

      this.timer.updater = null;
      this.timer = null;
      super.destroy();
   }

   @Override
   public void draw(GOut g) {
      g.image(bg, Coord.z);
      super.draw(g);
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.start) {
         this.timer.start();
         this.updbtns();
      } else if (sender == this.stop) {
         this.timer.stop();
         this.updbtns();
      } else if (sender == this.delete) {
         if (!TimerPanel.isDeletionLocked()) {
            this.timer.destroy();
            TimerController.getInstance().save();
            this.ui.destroy(this);
         }
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }
}
