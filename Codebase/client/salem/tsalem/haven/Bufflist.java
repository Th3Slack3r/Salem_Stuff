package haven;

import java.util.Iterator;

public class Bufflist extends Widget {
  static final Tex frame = Resource.loadtex("gfx/hud/buffs/frame");
  
  static final Tex cframe = Resource.loadtex("gfx/hud/buffs/cframe");
  
  static final Tex ameter = Resource.loadtex("gfx/hud/buffs/cbar");
  
  static final Coord imgoff = new Coord(6, 6);
  
  static final Coord ameteroff = new Coord(4, 52);
  
  static final Coord cmeteroff = new Coord(10, -3);
  
  static final Coord cmeteroffA = new Coord(20, 20);
  
  static final Coord cmeterul = new Coord(-20, -20);
  
  static final Coord cmeterbr = new Coord(20, 20);
  
  static final int margin = 2;
  
  static final int num = 15;
  
  public Bufflist(Coord c, Widget parent) {
    super(c, new Coord(15 * (frame.sz()).x + 28, (cframe.sz()).y), parent);
  }
  
  public void draw(GOut g) {
    int i = 0;
    int w = (frame.sz()).x + 2;
    long now = System.currentTimeMillis();
    synchronized (this.ui.sess.glob.buffs) {
      Iterator<Buff> i$ = this.ui.sess.glob.buffs.values().iterator();
      while (i$.hasNext()) {
        Buff b = i$.next();
        if (b.major) {
          Coord bc = new Coord(i * w, 0);
          if (b.ameter >= 0) {
            g.image(cframe, bc);
            g.image(ameter, bc.add(ameteroff), bc.add(ameteroff), new Coord(b.ameter * (ameter.sz()).x / 100, (ameter.sz()).y));
          } else {
            g.image(frame, bc);
          } 
          try {
            Tex e = ((Resource.Image)((Resource)b.res.get()).<Resource.Image>layer(Resource.imgc)).tex();
            g.image(e, bc.add(imgoff));
            if (b.nmeter >= 0) {
              Tex alarm_icon = b.nmeter();
              g.image(alarm_icon, bc.add(imgoff).add(e.sz()).add(alarm_icon.sz().inv()).add(-1, -1));
            } 
            if (b.cmeter >= 0) {
              double m = b.cmeter / 100.0D;
              if (b.cticks >= 0) {
                double lineTop = b.cticks * 0.06D;
                double pt = (now - b.gettime) / 1000.0D;
                m *= (lineTop - pt) / lineTop;
              } 
              m = Utils.clip(m, 0.0D, 1.0D);
              if (Config.buff_alert_activate && b.alarmRedOverlay) {
                g.chcolor(255, 0, 0, 128);
                g.frect(bc, frame.sz());
              } 
              if (Config.buff_alert_show_line_duration) {
                g.chcolor(0, 255, 64, 255);
                Coord arg20 = bc.add(lineLeft, 0);
                Coord lineBottom = bc.add(lineLeft, (int)(lineHeight * m));
                g.line(arg20, lineBottom, 4.0D);
              } 
              if (Config.buff_alert_show_pie_chart_duration) {
                g.chcolor(255, 255, 255, 128);
                g.prect(bc.add(imgoff).add(cmeteroffA), cmeterul, cmeterbr, 6.283185307179586D * m);
              } 
              g.chcolor();
            } 
            if (!b.alarm || b.skipThis()) {
              Tex alarm_icon = b.getAlarmIcon();
              g.image(alarm_icon, bc.add(imgoff).add(new Coord(0, 22)));
            } else if (Config.buff_alert_activate && b.cmeter >= 0 && b.cmeter <= Config.buff_alert_activation_value) {
              if (Config.buff_alert_sound_activate && now > lastAlarm + alarmSoundGap) {
                lastAlarm = now;
                Audio.play(alarmSound);
              } 
              if (now > b.lastBlink + alarmBlinkGap) {
                b.alarmRedOverlay = !b.alarmRedOverlay;
                b.lastBlink = now;
              } 
            } 
            if (Config.buff_alert_show_percent)
              if (b.cmeter >= 100) {
                g.image(b.getCmeterTex(), bc.add(imgoff).add(cmeteroff).add(new Coord(-13, 0)));
              } else if (b.cmeter >= 10) {
                g.image(b.getCmeterTex(), bc.add(imgoff).add(cmeteroff).add(new Coord(-8, 0)));
              } else {
                g.image(b.getCmeterTex(), bc.add(imgoff).add(cmeteroff).add(new Coord(-3, 0)));
              }  
            if (Config.buff_alert_activate && b.cmeter > Config.buff_alert_activation_value && b.alarmRedOverlay)
              b.alarmRedOverlay = !b.alarmRedOverlay; 
          } catch (Loading loading) {}
          i++;
          if (i >= 15)
            break; 
        } 
      } 
    } 
  }
  
  private Buff getBuffUnderCursor(Coord wdgMouseCoord) {
    int i = 0;
    int w = (frame.sz()).x + 2;
    synchronized (this.ui.sess.glob.buffs) {
      Iterator<Buff> i$ = this.ui.sess.glob.buffs.values().iterator();
      while (i$.hasNext()) {
        Buff b = i$.next();
        if (b.major) {
          Coord bc = new Coord(i * w, 0);
          if (wdgMouseCoord.isect(bc, frame.sz()))
            return b; 
          i++;
          if (i >= 15)
            break; 
        } 
      } 
      return null;
    } 
  }
  
  public Object tooltip(Coord wdgMouseCoord, Widget prev) {
    long now = System.currentTimeMillis();
    if (prev != this)
      this.hoverstart = now; 
    Buff b = getBuffUnderCursor(wdgMouseCoord);
    if (b != null) {
      String tt = b.tooltip();
      if (this.tipped != tt)
        this.shorttip = this.longtip = null; 
      this.tipped = tt;
      try {
        if (now - this.hoverstart < 1000L) {
          if (this.shorttip == null)
            this.shorttip = Text.render(tt).tex(); 
          return this.shorttip;
        } 
        if (this.longtip == null) {
          String e = RichText.Parser.quote(tt);
          Resource.Pagina pag = ((Resource)b.res.get()).<Resource.Pagina>layer(Resource.pagina);
          if (pag != null)
            e = e + "\n\n" + pag.text; 
          this.longtip = RichText.render(e, 200, new Object[0]).tex();
        } 
        return this.longtip;
      } catch (Loading arg8) {
        return "...";
      } 
    } 
    return null;
  }
  
  public boolean mousedown(Coord c, int button) {
    Buff b = getBuffUnderCursor(c);
    if (b != null) {
      if (b.cmeter == -1)
        return true; 
      b.alarmRedOverlay = false;
      if (button == 1) {
        b.alarm = !b.alarm;
      } else if (button == 3) {
        b.togglePermMute();
      } 
    } 
    return true;
  }
  
  public static void reloadAlarmSound(String s) {
    alarmSound = Resource.load(s);
  }
  
  static final int lineLeft = (frame.sz()).x - 5;
  
  static final int lineHeight = (frame.sz()).y;
  
  static Resource alarmSound = Resource.load(Config.buff_alert_sound_resource);
  
  static long lastAlarm = 0L;
  
  static long alarmSoundGap = 10000L;
  
  static long alarmBlinkGap = 1000L;
  
  private long hoverstart;
  
  private Tex shorttip;
  
  private Tex longtip;
  
  private String tipped;
}
