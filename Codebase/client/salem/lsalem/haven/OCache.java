package haven;

import haven.minimap.Marker;
import haven.minimap.Radar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OCache implements Iterable<Gob> {
   private Collection<Collection<Gob>> local = new LinkedList<>();
   private Map<Long, Gob> objs = new TreeMap<>();
   private Map<Long, Integer> deleted = new TreeMap<>();
   private Glob glob;
   public final Radar radar = new Radar();
   private long nextvirt = -1L;

   public OCache(Glob glob) {
      this.glob = glob;
   }

   public Collection<Gob> getGobs() {
      return this.objs.values();
   }

   public synchronized void remove(long id, int frame) {
      if (this.objs.containsKey(id) && (!this.deleted.containsKey(id) || this.deleted.get(id) < frame)) {
         Gob old = this.objs.remove(id);
         this.deleted.put(id, frame);
         old.dispose();
         this.radar.remove(id);
      }
   }

   public synchronized void remove(long id) {
      this.objs.remove(id);
   }

   public synchronized void tick() {
      for (Gob g : this.objs.values()) {
         g.tick();
      }
   }

   public void ctick(int dt) {
      synchronized (this) {
         ArrayList<Gob> copy = new ArrayList<>();

         for (Gob g : this) {
            copy.add(g);
         }

         for (Gob g : copy) {
            g.ctick(dt);
         }
      }
   }

   @Override
   public Iterator<Gob> iterator() {
      Collection<Iterator<Gob>> is = new LinkedList<>();

      for (Collection<Gob> gc : this.local) {
         is.add(gc.iterator());
      }

      return new I2<>(this.objs.values().iterator(), new I2<>(is));
   }

   public synchronized void ladd(Collection<Gob> gob) {
      this.local.add(gob);
   }

   public synchronized void lrem(Collection<Gob> gob) {
      this.local.remove(gob);
   }

   public synchronized Gob getgob(long id) {
      return this.objs.get(id);
   }

   public synchronized Gob getgob(long id, int frame) {
      if (!this.objs.containsKey(id)) {
         boolean r = false;
         if (this.deleted.containsKey(id)) {
            if (this.deleted.get(id) < frame) {
               this.deleted.remove(id);
            } else {
               r = true;
            }
         }

         if (r) {
            return null;
         } else {
            Gob g = new Gob(this.glob, Coord.z, id, frame);
            this.objs.put(id, g);
            return g;
         }
      } else {
         Gob ret = this.objs.get(id);
         return ret.frame >= frame ? null : ret;
      }
   }

   public synchronized void move(Gob g, Coord c, double a) {
      g.move(c, a);
   }

   public synchronized void cres(Gob g, Indir<Resource> res, Message sdt) {
      ResDrawable d = (ResDrawable)g.getattr(Drawable.class);
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
      Moving m = g.getattr(Moving.class);
      if (m != null && m instanceof LinMove) {
         LinMove lm = (LinMove)m;
         if (l >= 0 && l < lm.c) {
            lm.setl(l);
         } else {
            g.delattr(Moving.class);
         }
      }
   }

   public synchronized void speak(Gob g, float zo, String text) {
      if (text.length() < 1) {
         g.delattr(Speaking.class);
      } else {
         Speaking m = g.getattr(Speaking.class);
         if (m == null) {
            g.setattr(new Speaking(g, zo, text));
         } else {
            m.zo = zo;
            m.update(text);
         }
      }
   }

   public synchronized void composite(Gob g, Indir<Resource> base) {
      Composite cmp = (Composite)g.getattr(Drawable.class);
      if (cmp == null || !cmp.base.equals(base)) {
         cmp = new Composite(g, base);
         g.setattr(cmp);
         this.radar.add(g, base);
      }
   }

   public synchronized void cmppose(Gob g, int pseq, List<ResData> poses, List<ResData> tposes, boolean interp, float ttime) {
      Composite cmp = (Composite)g.getattr(Drawable.class);
      if (cmp.pseq != pseq) {
         cmp.pseq = pseq;
         if (poses != null) {
            cmp.chposes(poses, interp);
         }

         if (tposes != null) {
            cmp.tposes(tposes, WrapMode.ONCE, ttime);
         }
      }
   }

   public synchronized void cmpmod(Gob g, List<Composited.MD> mod) {
      Composite cmp = (Composite)g.getattr(Drawable.class);
      cmp.chmod(mod);
   }

   public synchronized void cmpequ(Gob g, List<Composited.ED> equ) {
      Composite cmp = (Composite)g.getattr(Drawable.class);
      cmp.chequ(equ);
   }

   public synchronized void avatar(Gob g, List<Indir<Resource>> layers) {
      Avatar ava = g.getattr(Avatar.class);
      if (ava == null) {
         ava = new Avatar(g);
         g.setattr(ava);
      }

      ava.setlayers(layers);
   }

   public synchronized void drawoff(Gob g, Coord off) {
      if (off.x == 0 && off.y == 0) {
         g.delattr(DrawOffset.class);
      } else {
         DrawOffset dro = g.getattr(DrawOffset.class);
         if (dro == null) {
            dro = new DrawOffset(g, off);
            g.setattr(dro);
         } else {
            dro.off = off;
         }
      }
   }

   public synchronized void lumin(Gob g, Coord off, int sz, int str) {
      g.setattr(new Lumin(g, off, sz, str));
   }

   public synchronized void follow(Gob g, long oid, Indir<Resource> xfres, String xfname) {
      if (oid == 4294967295L) {
         g.delattr(Following.class);
      } else {
         Following flw = g.getattr(Following.class);
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
      g.delattr(Homing.class);
   }

   public synchronized void homing(Gob g, long oid, Coord tc, int v) {
      g.setattr(new Homing(g, oid, tc, v));
   }

   public synchronized void homocoord(Gob g, Coord tc, int v) {
      Homing homo = g.getattr(Homing.class);
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
         g.delattr(KinInfo.class);
      } else {
         KinInfo b = g.getattr(KinInfo.class);
         if (b == null) {
            g.setattr(new KinInfo(g, name, group, type));
         } else {
            b.update(name, group, type);
         }

         Marker m = this.radar.getMarker(g.id);
         if (m != null) {
            m.override(name, BuddyWnd.gc[group]);
         }
      }
   }

   public synchronized void icon(Gob g, Indir<Resource> res) {
      if (g != null) {
         if (res == null) {
            g.delattr(GobIcon.class);
         } else {
            g.setattr(new GobIcon(g, res));
         }
      }
   }

   public class Virtual extends Gob {
      public Virtual(Coord c, double a) {
         super(OCache.this.glob, c, OCache.this.nextvirt--, 0);
         this.a = a;
         this.virtual = true;
         synchronized (OCache.this) {
            OCache.this.objs.put(this.id, this);
         }
      }
   }
}
