package org.ender.timer;

import haven.Audio;
import haven.Config;
import haven.Coord;
import haven.Label;
import haven.TimerPanel;
import haven.UI;
import haven.Widget;
import haven.Window;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Timer {
  protected static final int SERVER_RATIO = 3;
  
  public static long server;
  
  public static long local;
  
  private Long timerID;
  
  private String charName;
  
  private long start;
  
  private long duration;
  
  private String name;
  
  private transient long remaining;
  
  public transient Callback updater;
  
  public void setDuration(long duration) {
    this.duration = duration;
  }
  
  private InputStream FileInputStream(String string) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public Timer() {}
  
  public Timer(String name, long duration, String charName) {
    this.name = name;
    this.duration = duration;
    this.charName = charName;
  }
  
  public Timer(Timer t) {
    this.name = t.name;
    this.duration = t.duration;
    this.charName = t.charName;
    this.timerID = t.timerID;
  }
  
  public void setCharName(String cName) {
    this.charName = cName;
  }
  
  public String getCharName() {
    return this.charName;
  }
  
  public boolean isWorking() {
    return (this.start != 0L);
  }
  
  public void stop() {
    this.start = 0L;
    if (this.updater != null)
      this.updater.run(this); 
    TimerController.getInstance().save();
  }
  
  public void start() {
    this.start = server + 3L * (System.currentTimeMillis() - local);
    TimerController.getInstance().save();
  }
  
  public synchronized boolean update() {
    long now = System.currentTimeMillis();
    if (this.duration == 0L) {
      this.remaining = -local + now - this.start / 3L + server / 3L;
    } else {
      this.remaining = this.duration - now + local - (server - this.start) / 3L;
      if (this.remaining <= 0L) {
        String mN = TimerController.charName;
        String tN = this.charName;
        if (tN != null && tN != "" && !mN.equals(tN))
          return false; 
      } 
      if (this.remaining <= 0L) {
        String str;
        Window wnd = new Window(new Coord(250, 100), Coord.z, (Widget)UI.instance.root, this.name);
        if (this.remaining < -1500L) {
          str = String.format("%s elapsed since timer named \"%s\"  finished it's work", new Object[] { toString(), this.name });
        } else {
          str = String.format("Timer named \"%s\" just finished it's work at: %s", new Object[] { this.name, (new SimpleDateFormat("HH.mm.ss")).format(new Date()) });
        } 
        new Label(Coord.z, (Widget)wnd, str);
        wnd.justclose = true;
        wnd.pack();
        if (!TimerPanel.isSilenced()) {
          InputStream file = null;
          try {
            file = new FileInputStream(Config.userhome + "/timer.wav");
          } catch (FileNotFoundException ex) {
            file = Timer.class.getResourceAsStream("/timer.wav");
          } 
          Audio.play(file, 1.0D, 1.0D);
        } 
        return true;
      } 
    } 
    if (this.updater != null)
      this.updater.run(this); 
    return false;
  }
  
  public synchronized long getStart() {
    return this.start;
  }
  
  public synchronized void setStart(long start) {
    this.start = start;
  }
  
  public synchronized String getName() {
    return this.name;
  }
  
  public synchronized void setName(String name) {
    this.name = name;
  }
  
  public synchronized long getDuration() {
    return this.duration;
  }
  
  public synchronized long getFinishDate() {
    return this.duration + local - (server - this.start) / 3L;
  }
  
  public void destroy() {
    TimerController.getInstance().remove(this);
    this.updater = null;
  }
  
  public Long getTimerID() {
    return this.timerID;
  }
  
  public void setTimerID(Long timerID) {
    this.timerID = timerID;
  }
  
  public String toString() {
    long t = Math.abs(isWorking() ? this.remaining : this.duration) / 1000L;
    int h = (int)(t / 3600L);
    int m = (int)(t % 3600L / 60L);
    int s = (int)(t % 60L);
    if (h >= 24) {
      int d = h / 24;
      h %= 24;
      return String.format("%d:%02d:%02d:%02d", new Object[] { Integer.valueOf(d), Integer.valueOf(h), Integer.valueOf(m), Integer.valueOf(s) });
    } 
    return String.format("%d:%02d:%02d", new Object[] { Integer.valueOf(h), Integer.valueOf(m), Integer.valueOf(s) });
  }
  
  public static interface Callback {
    void run(Timer param1Timer);
  }
}
