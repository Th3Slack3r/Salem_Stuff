package haven;

import java.awt.Color;

public class Buff {
  public static final Text.Foundry nfnd = new Text.Foundry("SansSerif", 10);
  
  public static final Text.Foundry alarmTF = new Text.Foundry("SansSerif", 14);
  
  public static final Text.Foundry alarmTF2 = new Text.Foundry("SansSerif", 18);
  
  int id;
  
  Indir<Resource> res;
  
  String tt = null;
  
  int ameter = -1;
  
  int nmeter = -1;
  
  int cmeter = -1;
  
  int cticks = -1;
  
  long gettime;
  
  Tex ntext = null;
  
  boolean major = false;
  
  boolean alarm = true;
  
  Tex alarmIconS = null;
  
  Tex alarmIconP = null;
  
  boolean alarmRedOverlay = false;
  
  long lastBlink = 0L;
  
  private int prev_cmeter = 0;
  
  Tex cmeter_value_tex;
  
  Boolean skipAlertPerm = null;
  
  public Buff(int id, Indir<Resource> res) {
    this.id = id;
    this.res = res;
  }
  
  Tex nmeter() {
    if (this.ntext == null)
      this.ntext = new TexI(Utils.outline2((nfnd.render(Integer.toString(this.nmeter), Color.WHITE)).img, Color.BLACK)); 
    return this.ntext;
  }
  
  Tex getAlarmIcon() {
    if (this.skipAlertPerm != null && this.skipAlertPerm.booleanValue()) {
      if (this.alarmIconP == null)
        this.alarmIconP = new TexI(Utils.outline2((alarmTF.render("P", Color.CYAN)).img, Color.BLACK)); 
      return this.alarmIconP;
    } 
    if (this.alarmIconS == null)
      this.alarmIconS = new TexI(Utils.outline2((alarmTF.render("S", Color.RED)).img, Color.BLACK)); 
    return this.alarmIconS;
  }
  
  Tex getCmeterTex() {
    if (this.prev_cmeter == this.cmeter)
      return this.cmeter_value_tex; 
    this.prev_cmeter = this.cmeter;
    refreshCMeterTex();
    return this.cmeter_value_tex;
  }
  
  public void refreshCMeterTex() {
    if (Config.buff_alert_bigger_percentage) {
      this.cmeter_value_tex = new TexI(Utils.outline2((alarmTF2.render((this.cmeter >= 0) ? (this.cmeter + "%") : "", Color.BLACK)).img, Color.WHITE));
    } else {
      this.cmeter_value_tex = new TexI(Utils.outline2((alarmTF.render((this.cmeter >= 0) ? (this.cmeter + "%") : "", Color.WHITE)).img, Color.BLACK));
    } 
  }
  
  public String tooltip() {
    if (this.cmeter == -1)
      return (this.tt != null) ? this.tt : ((Resource.Tooltip)((Resource)this.res.get()).layer((Class)Resource.tooltip)).t; 
    return (this.tt != null) ? this.tt : (((Resource.Tooltip)((Resource)this.res.get()).layer((Class)Resource.tooltip)).t + "\nDuration: " + this.cmeter + " %");
  }
  
  public boolean skipThis() {
    if (this.skipAlertPerm == null) {
      try {
        String t = ((Resource.Tooltip)((Resource)this.res.get()).layer((Class)Resource.tooltip)).t;
        if (Config.BUFFNOALERT.containsKey(t) && ((Boolean)Config.BUFFNOALERT.get(t)).booleanValue()) {
          this.skipAlertPerm = Boolean.valueOf(true);
        } else {
          this.skipAlertPerm = Boolean.valueOf(false);
        } 
      } catch (Exception e) {
        return false;
      } 
    } else if (!this.alarm || this.skipAlertPerm.booleanValue()) {
      return true;
    } 
    return false;
  }
  
  public void addOrChange(String name, boolean hideThis) {
    if (name != null && !name.isEmpty()) {
      synchronized (Config.BUFFNOALERT) {
        Config.BUFFNOALERT.put(name, Boolean.valueOf(hideThis));
      } 
      Config.saveBuffNoAlert();
    } 
  }
  
  public void togglePermMute() {
    String t = ((Resource.Tooltip)((Resource)this.res.get()).layer((Class)Resource.tooltip)).t;
    if (this.skipAlertPerm == null)
      this.skipAlertPerm = Boolean.valueOf(false); 
    this.skipAlertPerm = Boolean.valueOf(!this.skipAlertPerm.booleanValue());
    if (!this.skipAlertPerm.booleanValue())
      this.alarm = true; 
    addOrChange(t, this.skipAlertPerm.booleanValue());
  }
}
