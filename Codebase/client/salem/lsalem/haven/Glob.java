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
   public Set<Glob.Pagina> paginae = new HashSet<>();
   public int pagseq = 0;
   public Map<Resource, Glob.Pagina> pmap = new WeakHashMap<>();
   public Map<String, Glob.CAttr> cattr = new HashMap<>();
   public Map<Integer, Buff> buffs = new TreeMap<>();
   public Color lightamb = null;
   public Color lightdif = null;
   public Color lightspc = null;
   public Color olightamb = null;
   public Color olightdif = null;
   public Color olightspc = null;
   public Color tlightamb = null;
   public Color tlightdif = null;
   public Color tlightspc = null;
   public double lightang = 0.0;
   public double lightelev = 0.0;
   public double olightang = 0.0;
   public double olightelev = 0.0;
   public double tlightang = 0.0;
   public double tlightelev = 0.0;
   public long lchange = -1L;
   public Indir<Resource> sky1 = null;
   public Indir<Resource> sky2 = null;
   public double skyblend = 0.0;
   public Color origamb = null;
   public long cattr_lastupdate = 0L;
   private long lastctick = 0L;
   private long lastrep = 0L;
   private long rgtime = 0L;
   private final int minute = 60;
   private final int hour = 3600;
   private final int day = 86400;
   private final int month = 2592000;
   private final int year = 31104000;

   public Glob(Session sess) {
      this.sess = sess;
      this.map = new MCache(sess);
      this.party = new Party(this);
   }

   public void purge() {
      this.map.purge();
      this.paginae.clear();
      this.pmap.clear();
      this.cattr.clear();
      this.buffs.clear();
   }

   private static Color colstep(Color o, Color t, double a) {
      int or = o.getRed();
      int og = o.getGreen();
      int ob = o.getBlue();
      int oa = o.getAlpha();
      int tr = t.getRed();
      int tg = t.getGreen();
      int tb = t.getBlue();
      int ta = t.getAlpha();
      return new Color(or + (int)((tr - or) * a), og + (int)((tg - og) * a), ob + (int)((tb - ob) * a), oa + (int)((ta - oa) * a));
   }

   private void ticklight(int dt) {
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
            double a = this.lchange / 2000.0;
            this.origamb = colstep(this.olightamb, this.tlightamb, a);
            this.lightdif = colstep(this.olightdif, this.tlightdif, a);
            this.lightspc = colstep(this.olightspc, this.tlightspc, a);
            this.lightang = this.olightang + a * Utils.cangle(this.tlightang - this.olightang);
            this.lightelev = this.olightelev + a * Utils.cangle(this.tlightelev - this.olightelev);
         }

         this.brighten();
      }
   }

   public void ctick() {
      long now = System.currentTimeMillis();
      int dt;
      if (this.lastctick == 0L) {
         dt = 0;
      } else {
         dt = (int)(now - this.lastctick);
      }

      dt = Math.max(dt, 0);
      synchronized (this) {
         this.ticklight(dt);
      }

      this.oc.ctick(dt);
      this.map.ctick(dt);
      this.lastctick = now;
   }

   private static double defix(int i) {
      return i / 1.0E9;
   }

   public long globtime() {
      long now = System.currentTimeMillis();
      long raw = (now - this.epoch) * 3L + this.time * 1000L;
      if (this.lastrep == 0L) {
         this.rgtime = raw;
      } else {
         long gd = (now - this.lastrep) * 3L;
         this.rgtime += gd;
         if (Math.abs(this.rgtime + gd - raw) > 1000L) {
            this.rgtime = this.rgtime + (long)((raw - this.rgtime) * (1.0 - Math.pow(10.0, -(now - this.lastrep) / 1000.0)));
         }
      }

      this.lastrep = now;
      return this.rgtime;
   }

   private static String ordinal(int i) {
      String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
      switch (i % 100) {
         case 11:
         case 12:
         case 13:
            return i + "th";
         default:
            return i + sufixes[i % 10];
      }
   }

   private void setServerTime(int st) {
      this.time = st;
   }

   public void blob(Message msg) {
      boolean inc = msg.uint8() != 0;

      while (!msg.eom()) {
         int t = msg.uint8();
         switch (t) {
            case 0:
               this.setServerTime(msg.int32());
               this.season = msg.uint8();
               this.epoch = System.currentTimeMillis();
               if (!inc) {
                  this.lastrep = 0L;
               }

               Timer.server = 1000L * this.time;
               Timer.local = System.currentTimeMillis();
               break;
            case 1:
            default:
               throw new RuntimeException("Unknown globlob type: " + t);
            case 2:
               synchronized (this) {
                  this.tlightamb = msg.color();
                  this.tlightdif = msg.color();
                  this.tlightspc = msg.color();
                  this.tlightang = msg.int32() / 1000000.0 * Math.PI * 2.0;
                  this.tlightelev = msg.int32() / 1000000.0 * Math.PI * 2.0;
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

                  this.brighten();
                  break;
               }
            case 3:
               int id1 = msg.uint16();
               if (id1 == 65535) {
                  synchronized (this) {
                     this.sky1 = this.sky2 = null;
                     this.skyblend = 0.0;
                  }
               } else {
                  int id2 = msg.uint16();
                  if (id2 == 65535) {
                     synchronized (this) {
                        this.sky1 = this.sess.getres(id1);
                        this.sky2 = null;
                        this.skyblend = 0.0;
                     }
                  } else {
                     synchronized (this) {
                        this.sky1 = this.sess.getres(id1);
                        this.sky2 = this.sess.getres(id2);
                        this.skyblend = msg.int32() / 1000000.0;
                     }
                  }
               }
         }
      }
   }

   public synchronized void brighten() {
      float[] hsb;
      if (!Config.alwaysbright) {
         hsb = Color.RGBtoHSB(this.origamb.getRed(), this.origamb.getGreen(), this.origamb.getBlue(), null);
         float b = hsb[2];
         if (b < 0.62F) {
            hsb[2] = b + Config.brighten * (0.62F - b);
         }
      } else {
         this.lightang = Config.brightang * Math.PI / 2.0;
         this.lightelev = 0.9773843811168246;
         float[] hsb2 = Color.RGBtoHSB(255, 255, 208, null);
         this.lightdif = Color.getHSBColor(hsb2[0], hsb2[1], hsb2[2]);
         hsb2 = Color.RGBtoHSB(255, 255, 255, null);
         this.lightspc = Color.getHSBColor(hsb2[0], hsb2[1], hsb2[2]);
         hsb = Color.RGBtoHSB(96, 96, 160, null);
      }

      this.lightamb = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
      DarknessWnd.update();
   }

   public Glob.Pagina paginafor(Resource res) {
      if (res == null) {
         return null;
      } else {
         synchronized (this.pmap) {
            Glob.Pagina p = this.pmap.get(res);
            if (p == null) {
               this.pmap.put(res, p = new Glob.Pagina(res));
            }

            return p;
         }
      }
   }

   public void paginae(Message msg) {
      synchronized (this.paginae) {
         while (!msg.eom()) {
            int act = msg.uint8();
            if (act == 43) {
               String nm = msg.string();
               int ver = msg.uint16();
               final Glob.Pagina pag = this.paginafor(Resource.load(nm, ver));
               this.paginae.add(pag);
               pag.state(Glob.Pagina.State.ENABLED);
               pag.meter = 0;

               int t;
               while ((t = msg.uint8()) != 0) {
                  if (t == 33) {
                     pag.state(Glob.Pagina.State.DISABLED);
                  } else if (t == 42) {
                     pag.meter = msg.int32();
                     pag.gettime = System.currentTimeMillis();
                     pag.dtime = msg.int32();
                  } else if (t == 94) {
                     pag.newp = 1;
                     Utils.defer(new Runnable() {
                        @Override
                        public void run() {
                           pag.res().loadwait();
                           String name = pag.res().layer(Resource.action).name;
                           UI.instance.message(String.format("You gain access to '%s'!", name), GameUI.MsgType.INFO);
                        }
                     });
                  }
               }
            } else if (act == 45) {
               String nm = msg.string();
               int ver = msg.uint16();
               this.paginae.remove(this.paginafor(Resource.load(nm, ver)));
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
            Glob.CAttr a = this.cattr.get(nm);
            if (a == null) {
               a = new Glob.CAttr(nm, base, comp);
               this.cattr.put(nm, a);
            } else {
               a.update(base, comp);
            }

            if (nm.equals("carry")) {
               GameUI gui = UI.instance.gui;
               if (gui != null) {
                  gui.uimsg("weight", gui.weight);
               }
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
            boolean major = msg.uint8() != 0;
            Buff buff;
            if ((buff = this.buffs.get(id)) == null) {
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
            this.buffs.put(id, buff);
         } else if (name == "rm") {
            int idx = msg.int32();
            this.buffs.remove(idx);
         }
      }
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
         if (base != this.base || comp != this.comp) {
            Integer old = this.comp;
            this.base = base;
            this.comp = comp;
            this.setChanged();
            this.notifyObservers(old);
         }
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
      public Glob.Pagina.State st;
      public int meter;
      public int dtime;
      public long gettime;
      public Glob.Pagina.Image img;
      public int newp;
      public long fstart;

      public Pagina(Resource res) {
         this.res = res;
         this.state(Glob.Pagina.State.ENABLED);
      }

      public Resource res() {
         return this.res;
      }

      public Resource.AButton act() {
         return this.res().loading ? null : this.res().layer(Resource.action);
      }

      public void state(Glob.Pagina.State st) {
         this.st = st;
         this.img = st.img(this);
      }

      public interface Image {
         Tex tex();
      }

      public static enum State {
         ENABLED,
         DISABLED {
            @Override
            public Glob.Pagina.Image img(final Glob.Pagina pag) {
               return new Glob.Pagina.Image() {
                  private Tex c = null;

                  @Override
                  public Tex tex() {
                     if (pag.res() == null) {
                        return null;
                     } else {
                        if (this.c == null) {
                           this.c = new TexI(PUtils.monochromize(pag.res().layer(Resource.imgc).img, Color.LIGHT_GRAY));
                        }

                        return this.c;
                     }
                  }
               };
            }
         };

         private State() {
         }

         public Glob.Pagina.Image img(final Glob.Pagina pag) {
            return new Glob.Pagina.Image() {
               @Override
               public Tex tex() {
                  return pag.res() == null ? null : pag.res().layer(Resource.imgc).tex();
               }
            };
         }
      }
   }
}
