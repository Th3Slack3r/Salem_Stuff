package haven;

import java.awt.Color;
import java.util.Date;
import org.ender.timer.Timer;
import org.ender.timer.TimerController;

public class TimerWdg extends Widget {
  static Tex bg = Resource.loadtex("gfx/hud/bosq");
  
  private Timer timer;
  
  public Label time;
  
  public Label name;
  
  protected Button start;
  
  protected Button stop;
  
  protected Button delete;
  
  public TimerWdg(Coord c, Widget parent, Timer timer) {
    super(c, bg.sz(), parent);
    this.timer = timer;
    timer.updater = new Timer.Callback() {
        public void run(Timer timer) {
          synchronized (TimerWdg.this.time) {
            TimerWdg.this.time.settext(timer.toString());
            TimerWdg.this.updbtns();
          } 
        }
      };
    this.name = new Label(new Coord(5, 5), this, timer.getName());
    this.time = new Label(new Coord(5, 25), this, timer.toString());
    this.start = new Button(new Coord(125, 4), Integer.valueOf(50), this, "start");
    this.stop = new Button(new Coord(125, 4), Integer.valueOf(50), this, "stop");
    this.delete = new Button(new Coord(125, 27), Integer.valueOf(50), this, "delete");
    String tCN = timer.getCharName();
    String mCN = TimerController.charName;
    if (mCN.equals(tCN))
      this.name.setcolor(Color.CYAN); 
    if (timer.getDuration() < 1L) {
      this.start.change(Color.CYAN);
      this.stop.change(Color.CYAN);
    } 
    updbtns();
  }
  
  public Object tooltip(Coord c, Widget prev) {
    if (this.timer.isWorking()) {
      if (this.tooltip == null)
        this.tooltip = Text.render((new Date(this.timer.getFinishDate())).toString()).tex(); 
      return this.tooltip;
    } 
    this.tooltip = null;
    return null;
  }
  
  private void updbtns() {
    this.start.visible = !this.timer.isWorking();
    this.stop.visible = this.timer.isWorking();
    if (this.timer.isWorking()) {
      this.time.setcolor(Color.GREEN);
    } else {
      this.time.setcolor(Color.WHITE);
    } 
  }
  
  public void destroy() {
    unlink();
    Window wnd = getparent(Window.class);
    if (wnd != null)
      wnd.pack(); 
    this.timer.updater = null;
    this.timer = null;
    super.destroy();
  }
  
  public void draw(GOut g) {
    g.image(bg, Coord.z);
    super.draw(g);
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.start || sender == this.stop || sender == this.delete) {
      boolean update = (Utils.getprefl("last_update_timestamp", 0L) != TimerController.lastUpdateTime);
      Timer tempT = this.timer;
      if (update) {
        Long iD = this.timer.getTimerID();
        TimerController.getInstance().load();
        tempT = TimerController.getInstance().getTimerByID(iD);
      } 
      if (sender == this.start) {
        tempT.start();
        if (update) {
          TimerPanel.reFresh();
        } else {
          updbtns();
        } 
      } else if (sender == this.stop) {
        tempT.stop();
        if (update) {
          TimerPanel.reFresh();
        } else {
          updbtns();
        } 
      } else if (sender == this.delete && 
        !TimerPanel.isDeletionLocked()) {
        if (tempT != null) {
          tempT.destroy();
          TimerController.getInstance().save();
          if (update) {
            TimerPanel.reFresh();
          } else {
            this.ui.destroy(this);
          } 
        } 
      } 
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void hideTimer(Timer timer) {
    if (!TimerPanel.isDeletionLocked()) {
      timer.destroy();
      this.ui.destroy(this);
    } 
  }
}
