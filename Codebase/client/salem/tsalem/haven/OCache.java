package haven;

import haven.minimap.Marker;
import haven.minimap.Radar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class OCache implements Iterable<Gob> {
  private final Collection<Collection<Gob>> local = new LinkedList<>();
  
  private final Map<Long, Gob> objs = new TreeMap<>();
  
  private final Map<Long, Gob> shadowObjs = new TreeMap<>();
  
  private final Map<Long, Integer> deleted = new TreeMap<>();
  
  private final Glob glob;
  
  public final Radar radar = new Radar();
  
  public static int maxDist = Config.render_distance_int_value * 11;
  
  public static boolean renderDistance = Config.render_distance_bool_value;
  
  public static boolean hideSomeGobs = Config.hideSomeGobs;
  
  private boolean s1;
  
  private boolean s2;
  
  Coord cp;
  
  Coord co;
  
  int od;
  
  private long nextvirt;
  
  public Collection<Gob> getGobs() {
    try {
      return new ArrayList<>(this.objs.values());
    } catch (Exception e) {
      try {
        Thread.sleep(50L);
      } catch (InterruptedException interruptedException) {}
      return getGobs();
    } 
  }
  
  public synchronized void remove(long id, int frame) {
    if (this.objs.containsKey(Long.valueOf(id)) && (
      !this.deleted.containsKey(Long.valueOf(id)) || ((Integer)this.deleted.get(Long.valueOf(id))).intValue() < frame)) {
      Gob old = this.objs.remove(Long.valueOf(id));
      this.deleted.put(Long.valueOf(id), Integer.valueOf(frame));
      old.dispose();
      this.radar.remove(Long.valueOf(id));
    } 
  }
  
  public synchronized void remS(long id) {}
  
  public synchronized void revG() {}
  
  public synchronized void remove(long id) {
    this.objs.remove(Long.valueOf(id));
  }
  
  public synchronized void tick() {
    for (Gob g : this.objs.values())
      g.tick(); 
  }
  
  public OCache(Glob glob) {
    this.s1 = false;
    this.s2 = false;
    this.cp = null;
    this.co = new Coord(0, 0);
    this.od = 0;
    this.nextvirt = -1L;
    this.glob = glob;
  }
  
  public void ctick(int dt) {
    synchronized (this) {
      ArrayList<Gob> copy = new ArrayList<>();
      for (Gob g : this)
        copy.add(g); 
      if (renderDistance)
        if (this.s1) {
          this.s1 = false;
          if (this.s2) {
            this.s2 = false;
            try {
              this.cp = (UI.instance.gui.map.player()).rc;
            } catch (Exception exception) {}
            if (this.cp != null)
              for (Gob g : copy) {
                if (g.rc.dist(this.cp) > maxDist) {
                  g.hidden = true;
                  if (!g.wasHidden)
                    g.wasHidden = true; 
                  continue;
                } 
                g.hidden = false;
              }  
            this.cp = null;
          } else {
            this.s2 = true;
          } 
        } else {
          this.s1 = true;
        }  
      for (Gob g : copy)
        g.ctick(dt); 
    } 
  }
  
  public static void undoRenderDistance() {
    synchronized (UI.instance.sess.glob.oc) {
      ArrayList<Gob> copy = new ArrayList<>();
      for (Gob g : UI.instance.sess.glob.oc)
        copy.add(g); 
      for (Gob g : copy)
        g.hidden = false; 
    } 
  }
  
  public Iterator<Gob> iterator() {
    Collection<Iterator<Gob>> is = new LinkedList<>();
    for (Collection<Gob> gc : this.local)
      is.add(gc.iterator()); 
    return new I2<>((Iterator<Gob>[])new Iterator[] { this.objs.values().iterator(), new I2<>(is) });
  }
  
  public synchronized void ladd(Collection<Gob> gob) {
    this.local.add(gob);
  }
  
  public synchronized void lrem(Collection<Gob> gob) {
    this.local.remove(gob);
  }
  
  public synchronized Gob getgob(long id) {
    return this.objs.get(Long.valueOf(id));
  }
  
  public synchronized Gob getgob(long id, int frame) {
    if (!this.objs.containsKey(Long.valueOf(id))) {
      boolean r = false;
      if (this.deleted.containsKey(Long.valueOf(id)))
        if (((Integer)this.deleted.get(Long.valueOf(id))).intValue() < frame) {
          this.deleted.remove(Long.valueOf(id));
        } else {
          r = true;
        }  
      if (r)
        return null; 
      Gob g = new Gob(this.glob, Coord.z, id, frame);
      this.objs.put(Long.valueOf(id), g);
      return g;
    } 
    Gob ret = this.objs.get(Long.valueOf(id));
    if (ret.frame >= frame)
      return null; 
    return ret;
  }
  
  public class Virtual extends Gob {
    public Virtual(Coord c, double a) {
      super(OCache.this.glob, c, OCache.this.nextvirt--, 0);
      this.a = a;
      this.virtual = true;
      synchronized (OCache.this) {
        OCache.this.objs.put(Long.valueOf(this.id), this);
      } 
    }
  }
  
  public synchronized void move(Gob g, Coord c, double a) {
    g.move(c, a);
  }
  
  public synchronized void cres(Gob g, Indir<Resource> res, Message sdt) {
    if (hideSomeGobs)
      try {
        String nm = ((Resource)res.get()).name;
        Set<Map.Entry<String, Boolean>> entrySet = Config.HIDEGOBS.entrySet();
        for (Map.Entry<String, Boolean> entry : entrySet) {
          if (((Boolean)entry.getValue()).booleanValue() && nm.contains(entry.getKey())) {
            Gob old = this.objs.remove(Long.valueOf(g.id));
            old.dispose();
            return;
          } 
        } 
      } catch (Exception e) {
        final Gob g2 = g;
        final Indir<Resource> res2 = res;
        final Message sdt2 = sdt;
        (new Thread(new Runnable() {
              public void run() {
                int counter = 0;
                while (counter <= 100) {
                  OCache.this.sleep(100);
                  try {
                    String nm = ((Resource)res2.get()).name;
                    if (nm != null)
                      break; 
                  } catch (Exception exception) {}
                  counter++;
                } 
                OCache.this.cres(g2, res2, sdt2);
              }
            }"SlowBoatLoad")).start();
        return;
      }  
    ResDrawable d = (ResDrawable)g.<Drawable>getattr(Drawable.class);
    if (d != null && d.res == res && !d.sdt.equals(sdt) && d.spr != null && d.spr instanceof Gob.Overlay.CUpd) {
      ((Gob.Overlay.CUpd)d.spr).update(sdt);
      d.sdt = sdt;
    } else if (d == null || d.res != res || !d.sdt.equals(sdt)) {
      g.setattr(new ResDrawable(g, res, sdt));
      this.radar.add(g, res);
    } 
  }
  
  public synchronized void linbeg(Gob g, Coord s, Coord t, int c) {
    LinMove lm = new LinMove(g, s, t, c);
    g.setattr(lm);
  }
  
  public synchronized void linstep(Gob g, int l) {
    Moving m = g.<Moving>getattr(Moving.class);
    if (m == null || !(m instanceof LinMove))
      return; 
    LinMove lm = (LinMove)m;
    if (l < 0 || l >= lm.c) {
      g.delattr((Class)Moving.class);
    } else {
      lm.setl(l);
    } 
  }
  
  public synchronized void speak(Gob g, float zo, String text) {
    if (text.length() < 1) {
      g.delattr((Class)Speaking.class);
    } else {
      Speaking m = g.<Speaking>getattr(Speaking.class);
      if (m == null) {
        g.setattr(new Speaking(g, zo, text));
      } else {
        m.zo = zo;
        m.update(text);
      } 
    } 
  }
  
  public synchronized void composite(Gob g, Indir<Resource> base) {
    Composite cmp = (Composite)g.<Drawable>getattr(Drawable.class);
    if (cmp == null || !cmp.base.equals(base)) {
      cmp = new Composite(g, base);
      g.setattr(cmp);
      this.radar.add(g, base);
    } 
  }
  
  public synchronized void cmppose(Gob g, int pseq, List<ResData> poses, List<ResData> tposes, boolean interp, float ttime) {
    Composite cmp = (Composite)g.<Drawable>getattr(Drawable.class);
    if (cmp.pseq != pseq) {
      cmp.pseq = pseq;
      if (poses != null)
        cmp.chposes(poses, interp); 
      if (tposes != null)
        cmp.tposes(tposes, WrapMode.ONCE, ttime); 
    } 
  }
  
  public synchronized void cmpmod(Gob g, List<Composited.MD> mod) {
    Composite cmp = (Composite)g.<Drawable>getattr(Drawable.class);
    cmp.chmod(mod);
  }
  
  public synchronized void cmpequ(Gob g, List<Composited.ED> equ) {
    Composite cmp = (Composite)g.<Drawable>getattr(Drawable.class);
    cmp.chequ(equ);
  }
  
  public synchronized void avatar(Gob g, List<Indir<Resource>> layers) {
    Avatar ava = g.<Avatar>getattr(Avatar.class);
    if (ava == null) {
      ava = new Avatar(g);
      g.setattr(ava);
    } 
    ava.setlayers(layers);
  }
  
  public synchronized void zoff(Gob g, float off) {
    if (off == 0.0F) {
      g.delattr((Class)DrawOffset.class);
    } else {
      DrawOffset dro = g.<DrawOffset>getattr(DrawOffset.class);
      if (dro == null) {
        dro = new DrawOffset(g, new Coord3f(0.0F, 0.0F, off));
        g.setattr(dro);
      } else {
        dro.off = new Coord3f(0.0F, 0.0F, off);
      } 
    } 
  }
  
  public synchronized void lumin(Gob g, Coord off, int sz, int str) {
    g.setattr(new Lumin(g, off, sz, str));
  }
  
  public synchronized void follow(Gob g, long oid, Indir<Resource> xfres, String xfname) {
    if (oid == 4294967295L) {
      g.delattr((Class)Following.class);
    } else {
      Following flw = g.<Following>getattr(Following.class);
      if (flw == null) {
        flw = new Following(g, oid, xfres, xfname);
        g.setattr(flw);
      } else {
        synchronized (flw) {
          flw.tgt = oid;
          flw.xfres = xfres;
          flw.xfname = xfname;
          flw.lxfb = null;
          flw.xf = null;
        } 
      } 
    } 
  }
  
  public synchronized void homostop(Gob g) {
    g.delattr((Class)Homing.class);
  }
  
  public synchronized void homing(Gob g, long oid, Coord tc, int v) {
    g.setattr(new Homing(g, oid, tc, v));
  }
  
  public synchronized void homocoord(Gob g, Coord tc, int v) {
    Homing homo = g.<Homing>getattr(Homing.class);
    if (homo != null) {
      homo.tc = tc;
      homo.v = v;
    } 
  }
  
  public synchronized void overlay(Gob g, int olid, boolean prs, Indir<Resource> resid, Message sdt) {
    Gob.Overlay ol = g.findol(olid);
    if (resid != null) {
      if (ol == null) {
        g.ols.add(ol = new Gob.Overlay(olid, resid, sdt));
      } else if (!ol.sdt.equals(sdt)) {
        if (ol.spr instanceof Gob.Overlay.CUpd) {
          ((Gob.Overlay.CUpd)ol.spr).update(sdt);
          ol.sdt = sdt;
        } else {
          g.ols.remove(ol);
          g.ols.add(ol = new Gob.Overlay(olid, resid, sdt));
        } 
      } 
      ol.delign = prs;
    } else if (ol != null && ol.spr instanceof Gob.Overlay.CDel) {
      ((Gob.Overlay.CDel)ol.spr).delete();
    } else {
      g.ols.remove(ol);
    } 
  }
  
  public synchronized void health(Gob g, int hp) {
    g.setattr(new GobHealth(g, hp));
  }
  
  public synchronized void buddy(Gob g, String name, int group, int type) {
    if (name == null) {
      g.delattr((Class)KinInfo.class);
    } else {
      KinInfo b = g.<KinInfo>getattr(KinInfo.class);
      if (b == null) {
        g.setattr(new KinInfo(g, name, group, type));
      } else {
        b.update(name, group, type);
      } 
      Marker m = this.radar.getMarker(Long.valueOf(g.id));
      if (m != null)
        m.override(name, BuddyWnd.gc[group]); 
    } 
  }
  
  public synchronized void icon(Gob g, Indir<Resource> res) {
    if (g == null)
      return; 
    if (res == null) {
      g.delattr((Class)GobIcon.class);
    } else {
      g.setattr(new GobIcon(g, res));
    } 
  }
  
  private void sleep(int timeInMiliS) {
    try {
      Thread.sleep(timeInMiliS);
    } catch (InterruptedException interruptedException) {}
  }
}
