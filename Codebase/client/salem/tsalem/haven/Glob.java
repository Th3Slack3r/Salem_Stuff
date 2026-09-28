package haven;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import org.ender.timer.Timer;

public class Glob {
  public static final int GMSG_TIME = 0;
  
  public static final int GMSG_ASTRO = 1;
  
  public static final int GMSG_LIGHT = 2;
  
  public static final int GMSG_SKY = 3;
  
  public static final float MAX_BRIGHT = 0.62F;
  
  public long time;
  
  public long epoch = System.currentTimeMillis();
  
  public int season;
  
  public OCache oc = new OCache(this);
  
  public MCache map;
  
  public Session sess;
  
  public Party party;
  
  public Set<Pagina> paginae = new HashSet<>();
  
  public int pagseq = 0;
  
  public Map<Resource, Pagina> pmap = new WeakHashMap<>();
  
  public Map<String, CAttr> cattr = new HashMap<>();
  
  public Map<Integer, Buff> buffs = new TreeMap<>();
  
  public Color lightamb = null, lightdif = null, lightspc = null;
  
  public Color olightamb = null, olightdif = null, olightspc = null;
  
  public Color tlightamb = null, tlightdif = null, tlightspc = null;
  
  public Color xlightamb = null;
  
  public Color xlightdif = null;
  
  public Color xlightspc = null;
  
  public double lightang = 0.0D, lightelev = 0.0D;
  
  public double olightang = 0.0D, olightelev = 0.0D;
  
  public double tlightang = 0.0D, tlightelev = 0.0D;
  
  public double xlightang = 0.0D;
  
  public double xlightelev = 0.0D;
  
  public long lchange = -1L;
  
  public Indir<Resource> sky1 = null;
  
  public Indir<Resource> sky2 = null;
  
  public double skyblend = 0.0D;
  
  public Color origamb = null;
  
  public long cattr_lastupdate = 0L;
  
  private long lastctick;
  
  private long lastrep;
  
  private long rgtime;
  
  private final int minute = 60;
  
  private final int hour;
  
  private final int day;
  
  private final int month;
  
  private final int year;
  
  public void purge() {
    this.map.purge();
    this.paginae.clear();
    this.pmap.clear();
    this.cattr.clear();
    this.buffs.clear();
  }
  
  public static class CAttr extends Observable {
    String nm;
    
    int base;
    
    int comp;
    
    public CAttr(String nm, int base, int comp) {
      this.nm = nm.intern();
      this.base = base;
      this.comp = comp;
    }
    
    public void update(int base, int comp) {
      if (base == this.base && comp == this.comp)
        return; 
      Integer old = Integer.valueOf(this.comp);
      this.base = base;
      this.comp = comp;
      setChanged();
      notifyObservers(old);
    }
    
    public String getName() {
      return this.nm;
    }
    
    public int getBase() {
      return this.base;
    }
    
    public int getComp() {
      return this.comp;
    }
  }
  
  public static class Pagina implements Serializable {
    private final Resource res;
    
    public State st;
    
    public int meter;
    
    public int dtime;
    
    public long gettime;
    
    public Image img;
    
    public int newp;
    
    public long fstart;
    
    public enum State {
      ENABLED,
      DISABLED {
        public Glob.Pagina.Image img(final Glob.Pagina pag) {
          return new Glob.Pagina.Image() {
              private Tex c = null;
              
              public Tex tex() {
                if (pag.res() == null)
                  return null; 
                if (this.c == null)
                  this.c = new TexI(PUtils.monochromize(((Resource.Image)pag.res().layer((Class)Resource.imgc)).img, Color.LIGHT_GRAY)); 
                return this.c;
              }
            };
        }
      };
      
      public Glob.Pagina.Image img(final Glob.Pagina pag) {
        return new Glob.Pagina.Image() {
            public Tex tex() {
              if (pag.res() == null)
                return null; 
              return ((Resource.Image)pag.res().<Resource.Image>layer(Resource.imgc)).tex();
            }
          };
      }
    }
    
    public Pagina(Resource res) {
      this.res = res;
      state(State.ENABLED);
    }
    
    public Resource res() {
      return this.res;
    }
    
    public Resource.AButton act() {
      if ((res()).loading)
        return null; 
      return res().<Resource.AButton>layer(Resource.action);
    }
    
    public void state(State st) {
      this.st = st;
      this.img = st.img(this);
    }
    
    public static interface Image {
      Tex tex();
    }
  }
  
  public enum State {
    ENABLED,
    DISABLED {
      public Glob.Pagina.Image img(final Glob.Pagina pag) {
        return new Glob.Pagina.Image() {
            private Tex c = null;
            
            public Tex tex() {
              if (pag.res() == null)
                return null; 
              if (this.c == null)
                this.c = new TexI(PUtils.monochromize(((Resource.Image)pag.res().layer((Class)Resource.imgc)).img, Color.LIGHT_GRAY)); 
              return this.c;
            }
          };
      }
    };
    
    public Glob.Pagina.Image img(final Glob.Pagina pag) {
      return new Glob.Pagina.Image() {
          public Tex tex() {
            if (pag.res() == null)
              return null; 
            return ((Resource.Image)pag.res().<Resource.Image>layer(Resource.imgc)).tex();
          }
        };
    }
  }
  
  private static Color colstep(Color o, Color t, double a) {
    int or = o.getRed(), og = o.getGreen(), ob = o.getBlue(), oa = o.getAlpha();
    int tr = t.getRed(), tg = t.getGreen(), tb = t.getBlue(), ta = t.getAlpha();
    return new Color(or + (int)((tr - or) * a), og + (int)((tg - og) * a), ob + (int)((tb - ob) * a), oa + (int)((ta - oa) * a));
  }
  
  public void ticklight(int dt) {
    if (this.lchange >= 0L) {
      this.lchange += dt;
      if (this.lchange > 2000L) {
        this.lchange = -1L;
        this.origamb = this.tlightamb;
        this.lightdif = this.tlightdif;
        this.lightspc = this.tlightspc;
        this.lightang = this.tlightang;
        this.lightelev = this.tlightelev;
      } else {
        double a = this.lchange / 2000.0D;
        this.origamb = colstep(this.olightamb, this.tlightamb, a);
        this.lightdif = colstep(this.olightdif, this.tlightdif, a);
        this.lightspc = colstep(this.olightspc, this.tlightspc, a);
        this.lightang = this.olightang + a * Utils.cangle(this.tlightang - this.olightang);
        this.lightelev = this.olightelev + a * Utils.cangle(this.tlightelev - this.olightelev);
      } 
      brighten();
    } 
  }
  
  public Glob(Session sess) {
    this.lastctick = 0L;
    this.lastrep = 0L;
    this.rgtime = 0L;
    this.minute = 60;
    getClass();
    this.hour = 60 * 60;
    this.day = this.hour * 24;
    this.month = this.day * 30;
    this.year = this.month * 12;
    this.seasonNames = new String[] { "Coldsnap", "Everbloom", "Blood Moon" };
    this.sess = sess;
    this.map = new MCache(sess);
    this.party = new Party(this);
  }
  
  public void ctick() {
    long now = System.currentTimeMillis();
    if (this.lastctick == 0L) {
      dt = 0;
    } else {
      dt = (int)(now - this.lastctick);
    } 
    int dt = Math.max(dt, 0);
    synchronized (this) {
      ticklight(dt);
    } 
    this.oc.ctick(dt);
    this.map.ctick(dt);
    this.lastctick = now;
  }
  
  private static double defix(int i) {
    return i / 1.0E9D;
  }
  
  public long globtime() {
    long now = System.currentTimeMillis();
    long raw = (now - this.epoch) * 3L + this.time * 1000L;
    if (this.lastrep == 0L) {
      this.rgtime = raw;
    } else {
      long gd = (now - this.lastrep) * 3L;
      this.rgtime += gd;
      if (Math.abs(this.rgtime + gd - raw) > 1000L)
        this.rgtime += (long)((raw - this.rgtime) * (1.0D - Math.pow(10.0D, -(now - this.lastrep) / 1000.0D))); 
    } 
    this.lastrep = now;
    return this.rgtime;
  }
  
  public static boolean brightNow = false;
  
  private final String[] seasonNames;
  
  private static String ordinal(int i) {
    String[] sufixes = { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
    switch (i % 100) {
      case 11:
      case 12:
      case 13:
        return i + "th";
    } 
    return i + sufixes[i % 10];
  }
  
  private void setServerTime(int st) {
    this.time = st;
  }
  
  public void blob(Message msg) {
    if (msg == null) {
      synchronized (this) {
        this.tlightamb = this.xlightamb;
        this.tlightdif = this.xlightdif;
        this.tlightspc = this.xlightspc;
        this.tlightang = this.xlightang;
        this.tlightelev = this.xlightelev;
        this.olightamb = this.origamb;
        this.olightdif = this.lightdif;
        this.olightspc = this.lightspc;
        this.olightang = this.lightang;
        this.olightelev = this.lightelev;
        this.lchange = 0L;
        brighten();
      } 
      return;
    } 
    boolean inc = (msg.uint8() != 0);
    while (!msg.eom()) {
      int id1, id2, t = msg.uint8();
      switch (t) {
        case 0:
          setServerTime(msg.int32());
          this.season = msg.uint8();
          this.epoch = System.currentTimeMillis();
          if (!inc)
            this.lastrep = 0L; 
          Timer.server = 1000L * this.time;
          Timer.local = System.currentTimeMillis();
          if (Config.last_season_code < 0) {
            Config.last_season_code = this.season;
            Utils.setprefi("last_season_code", this.season);
          } 
          if (Config.last_season_code != this.season) {
            final String lastSeasonString;
            Glob finalThis = this;
            if (Config.last_season_code != -1) {
              lastSeasonString = this.seasonNames[Config.last_season_code];
            } else {
              lastSeasonString = "unknown";
            } 
            final String newSeasonString = this.seasonNames[this.season];
            Config.last_season_code = finalThis.season;
            Utils.setprefi("last_season_code", finalThis.season);
            (new Thread(new Runnable() {
                  public void run() {
                    while (!UI.ui_init_done)
                      Utils.sleep(1000); 
                    Utils.sleep(3000);
                    Utils.msgLog("Old season was: " + lastSeasonString + " new is: " + newSeasonString);
                    if (!Config.season_change_message_off) {
                      Window wnd = new Window(new Coord(250, 100), Coord.z, UI.instance.root, "Season changed!");
                      String str = "The new season is: " + newSeasonString;
                      new Label(Coord.z, wnd, str);
                      wnd.justclose = true;
                      wnd.pack();
                    } 
                  }
                })).start();
          } 
          continue;
        case 2:
          synchronized (this) {
            this.tlightamb = msg.color();
            this.tlightdif = msg.color();
            this.tlightspc = msg.color();
            this.tlightang = msg.int32() / 1000000.0D * Math.PI * 2.0D;
            this.tlightelev = msg.int32() / 1000000.0D * Math.PI * 2.0D;
            this.xlightamb = this.tlightamb;
            this.xlightdif = this.tlightdif;
            this.xlightspc = this.tlightspc;
            this.xlightang = this.tlightang;
            this.xlightelev = this.tlightelev;
            if (inc) {
              this.olightamb = this.origamb;
              this.olightdif = this.lightdif;
              this.olightspc = this.lightspc;
              this.olightang = this.lightang;
              this.olightelev = this.lightelev;
              this.lchange = 0L;
            } else {
              this.origamb = this.tlightamb;
              this.lightdif = this.tlightdif;
              this.lightspc = this.tlightspc;
              this.lightang = this.tlightang;
              this.lightelev = this.tlightelev;
              this.lchange = -1L;
            } 
            brighten();
          } 
          continue;
        case 3:
          id1 = msg.uint16();
          if (id1 == 65535) {
            synchronized (this) {
              this.sky1 = this.sky2 = null;
              this.skyblend = 0.0D;
            } 
            continue;
          } 
          id2 = msg.uint16();
          if (id2 == 65535) {
            synchronized (this) {
              this.sky1 = this.sess.getres(id1);
              this.sky2 = null;
              this.skyblend = 0.0D;
            } 
            continue;
          } 
          synchronized (this) {
            this.sky1 = this.sess.getres(id1);
            this.sky2 = this.sess.getres(id2);
            this.skyblend = msg.int32() / 1000000.0D;
          } 
          continue;
      } 
      throw new RuntimeException("Unknown globlob type: " + t);
    } 
  }
  
  public synchronized void brighten() {
    if (Config.use_old_night_vision) {
      brighten_old();
    } else {
      brighten_new();
    } 
  }
  
  public synchronized void brighten_new() {
    float[] hsb;
    Boolean mt = null;
    if (Config.alwaysbright_in || Config.alwaysbright_out)
      try {
        mt = Boolean.valueOf(UI.instance.gui.map.isTileInsideMine());
      } catch (Exception exception) {} 
    if (mt == null || ((!Config.alwaysbright_in || !mt.booleanValue()) && (!Config.alwaysbright_out || mt.booleanValue()))) {
      hsb = Color.RGBtoHSB(this.origamb.getRed(), this.origamb.getGreen(), this.origamb.getBlue(), null);
      float b = hsb[2];
      if (b < 0.62F)
        hsb[2] = b + Config.brighten * (0.62F - b); 
    } else if (mt.booleanValue()) {
      int i = (int)Config.brightang_in + 1;
      float[] hsb2 = Color.RGBtoHSB(255 * (i + 4) / 12, 255 * (i + 4) / 12, 208 * (i + 4) / 12, null);
      this.lightdif = Color.getHSBColor(hsb2[0], hsb2[1], hsb2[2]);
      hsb2 = Color.RGBtoHSB(255, 255, 255, null);
      this.lightspc = Color.getHSBColor(hsb2[0], hsb2[1], hsb2[2]);
      hsb = Color.RGBtoHSB(96 * i / 8, 96 * i / 8, 160 * i / 8, null);
    } else {
      int i = (int)Config.brightang_out + 1;
      float[] hsb2 = Color.RGBtoHSB(255 * (i + 4) / 12, 255 * (i + 4) / 12, 208 * (i + 4) / 12, null);
      this.lightdif = Color.getHSBColor(hsb2[0], hsb2[1], hsb2[2]);
      hsb2 = Color.RGBtoHSB(255, 255, 255, null);
      this.lightspc = Color.getHSBColor(hsb2[0], hsb2[1], hsb2[2]);
      hsb = Color.RGBtoHSB(96 * i / 8, 96 * i / 8, 160 * i / 8, null);
    } 
    this.lightamb = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    int alpha = this.lightamb.getAlpha();
    DarknessWnd.update();
  }
  
  public synchronized void brighten_old() {
    float[] hsb;
    if (Config.old_night_vision_level == 0.0F) {
      hsb = Color.RGBtoHSB(this.origamb.getRed(), this.origamb.getGreen(), this.origamb.getBlue(), null);
      float b = hsb[2];
      if (b < 0.62F)
        hsb[2] = b + Config.brighten * (0.62F - b); 
    } else {
      this.lightang = (Config.old_night_vision_level - 1.0F) * Math.PI / 2.0D;
      this.lightelev = 0.9773843811168246D;
      float[] hsb2 = Color.RGBtoHSB(255, 255, 208, null);
      this.lightdif = Color.getHSBColor(hsb2[0], hsb2[1], hsb2[2]);
      hsb2 = Color.RGBtoHSB(255, 255, 255, null);
      this.lightspc = Color.getHSBColor(hsb2[0], hsb2[1], hsb2[2]);
      hsb = Color.RGBtoHSB(96, 96, 160, null);
    } 
    this.lightamb = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    DarknessWnd.update();
  }
  
  public Pagina paginafor(Resource res) {
    if (res == null)
      return null; 
    synchronized (this.pmap) {
      Pagina p = this.pmap.get(res);
      if (p == null)
        this.pmap.put(res, p = new Pagina(res)); 
      return p;
    } 
  }
  
  public void paginae(Message msg) {
    synchronized (this.paginae) {
      while (!msg.eom()) {
        int act = msg.uint8();
        if (act == 43) {
          String nm = msg.string();
          int ver = msg.uint16();
          final Pagina pag = paginafor(Resource.load(nm, ver));
          this.paginae.add(pag);
          pag.state(Pagina.State.ENABLED);
          pag.meter = 0;
          int t;
          while ((t = msg.uint8()) != 0) {
            if (t == 33) {
              pag.state(Pagina.State.DISABLED);
              continue;
            } 
            if (t == 42) {
              pag.meter = msg.int32();
              pag.gettime = System.currentTimeMillis();
              pag.dtime = msg.int32();
              continue;
            } 
            if (t == 94) {
              pag.newp = 1;
              Utils.defer(new Runnable() {
                    public void run() {
                      pag.res().loadwait();
                      String name = ((Resource.AButton)pag.res().layer((Class)Resource.action)).name;
                      UI.instance.message(String.format("You gain access to '%s'!", new Object[] { name }), GameUI.MsgType.INFO);
                    }
                  });
            } 
          } 
          continue;
        } 
        if (act == 45) {
          String nm = msg.string();
          int ver = msg.uint16();
          this.paginae.remove(paginafor(Resource.load(nm, ver)));
        } 
      } 
      this.pagseq++;
    } 
  }
  
  public void cattr(Message msg) {
    synchronized (this.cattr) {
      while (!msg.eom()) {
        String nm = msg.string();
        int base = msg.int32();
        int comp = msg.int32();
        CAttr a = this.cattr.get(nm);
        if (a == null) {
          a = new CAttr(nm, base, comp);
          this.cattr.put(nm, a);
        } else {
          a.update(base, comp);
        } 
        if (nm.equals("carry")) {
          GameUI gui = UI.instance.gui;
          if (gui != null)
            gui.uimsg("weight", new Object[] { Integer.valueOf(gui.weight) }); 
        } 
      } 
    } 
    this.cattr_lastupdate = System.currentTimeMillis();
  }
  
  public void buffmsg(Message msg) {
    String name = msg.string().intern();
    synchronized (this.buffs) {
      if (name == "clear") {
        this.buffs.clear();
      } else if (name == "set") {
        int id = msg.int32();
        Indir<Resource> res = this.sess.getres(msg.uint16());
        String tt = msg.string();
        int ameter = msg.int32();
        int nmeter = msg.int32();
        int cmeter = msg.int32();
        int cticks = msg.int32();
        boolean major = (msg.uint8() != 0);
        Buff buff;
        if ((buff = this.buffs.get(Integer.valueOf(id))) == null) {
          buff = new Buff(id, res);
        } else {
          buff.res = res;
        } 
        if (tt.equals("")) {
          buff.tt = null;
        } else {
          buff.tt = tt;
        } 
        buff.ameter = ameter;
        buff.nmeter = nmeter;
        buff.ntext = null;
        buff.cmeter = cmeter;
        buff.cticks = cticks;
        buff.major = major;
        buff.gettime = System.currentTimeMillis();
        this.buffs.put(Integer.valueOf(id), buff);
      } else if (name == "rm") {
        int id = msg.int32();
        this.buffs.remove(Integer.valueOf(id));
      } 
    } 
  }
}
