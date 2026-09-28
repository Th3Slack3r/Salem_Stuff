package org.ender.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.Config;
import haven.MainFrame;
import haven.TimerPanel;
import haven.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TimerController extends Thread {
  public static ArrayList<Timer> timerSList = new ArrayList<>();
  
  private static TimerController instance;
  
  private static File config;
  
  public List<Timer> timers;
  
  public final Object lock = new Object();
  
  public static String charName;
  
  public static long lastUpdateTime = 0L;
  
  public static TimerController getInstance() {
    return instance;
  }
  
  private final ArrayList<Timer> ignoreList = new ArrayList<>();
  
  public TimerController() {
    super("Timer Thread");
    charName = MainFrame.cName;
    load();
    setDaemon(true);
    start();
  }
  
  public static void init(String server) {
    config = Config.getFile(String.format("timer_%s.cfg", new Object[] { server }));
    instance = new TimerController();
  }
  
  public void run() {
    while (true) {
      synchronized (this.lock) {
        if (Config.auto_sync_timers && Utils.getprefl("last_update_timestamp", 0L) != lastUpdateTime) {
          lastUpdateTime = Utils.getprefl("last_update_timestamp", 0L);
          load();
          TimerPanel.reFresh();
        } 
        for (Timer timer : this.timers) {
          if (!this.ignoreList.contains(timer) && timer.isWorking() && timer.update())
            timer.stop(); 
        } 
      } 
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException interruptedException) {}
    } 
  }
  
  public void add(Timer timer) {
    synchronized (this.lock) {
      boolean update = (Utils.getprefl("last_update_timestamp", 0L) != lastUpdateTime);
      if (update)
        load(); 
      this.timers.add(timer);
      timerSList.add(timer);
      setIDsForAll();
      save();
      if (update)
        TimerPanel.reFresh(); 
    } 
  }
  
  public void remove(Timer timer) {
    synchronized (this.lock) {
      this.timers.remove(timer);
    } 
  }
  
  public void load() {
    try {
      for (Timer timer : timerSList)
        timer.destroy(); 
      Gson gson = (new GsonBuilder()).create();
      InputStream is = new FileInputStream(config);
      this.timers = (List<Timer>)gson.fromJson(Utils.stream2str(is), (new TypeToken<List<Timer>>() {
          
          }).getType());
      timerSList.addAll(this.timers);
      if (!allHaveIDs()) {
        setIDsForAll();
        save();
      } 
      lastUpdateTime = Utils.getprefl("last_update_timestamp", 0L);
      String tN = "";
      for (Timer timer : this.timers) {
        tN = timer.getCharName();
        if (tN != null && tN != "" && !charName.equals(tN)) {
          timer.updater = null;
          this.ignoreList.add(timer);
        } 
      } 
    } catch (Exception exception) {}
    if (this.timers == null)
      this.timers = new LinkedList<>(); 
  }
  
  public void save() {
    Gson gson = (new GsonBuilder()).create();
    String data = gson.toJson(this.timers);
    boolean exists = config.exists();
    if (!exists)
      try {
        (new File(config.getParent())).mkdirs();
        exists = config.createNewFile();
      } catch (IOException iOException) {} 
    if (exists && config.canWrite()) {
      PrintWriter out = null;
      try {
        out = new PrintWriter(config);
        out.print(data);
      } catch (FileNotFoundException fileNotFoundException) {
      
      } finally {
        if (out != null)
          out.close(); 
      } 
    } 
    long tempTime = System.currentTimeMillis();
    Utils.setprefl("last_update_timestamp", tempTime);
    lastUpdateTime = tempTime;
  }
  
  private boolean allHaveIDs() {
    boolean allHaveIDs = true;
    for (Timer timer : this.timers) {
      if (timer.getTimerID() == null || timer.getTimerID().longValue() == 0L) {
        allHaveIDs = false;
        break;
      } 
    } 
    return allHaveIDs;
  }
  
  private void setIDsForAll() {
    ArrayList<Long> iDs = new ArrayList<>();
    for (Timer timer : this.timers) {
      if (timer.getTimerID() != null && timer.getTimerID().longValue() != 0L)
        iDs.add(timer.getTimerID()); 
    } 
    for (Timer timer : this.timers) {
      if (timer.getTimerID() == null || timer.getTimerID().longValue() == 0L) {
        Long val = Long.valueOf(0L);
        for (int i = 0; i < this.timers.size() + 100; i++) {
          val = getRNG();
          if (!iDs.contains(val)) {
            timer.setTimerID(val);
            iDs.add(val);
            break;
          } 
        } 
      } 
    } 
  }
  
  public Timer getTimerByID(Long iD) {
    for (Timer timer : this.timers) {
      if (timer.getTimerID().equals(iD))
        return timer; 
    } 
    return null;
  }
  
  public static Long getRNG() {
    return Long.valueOf((new Random()).nextLong());
  }
}
