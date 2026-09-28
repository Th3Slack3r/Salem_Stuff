package haven;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class MCache {
   public static final Coord tilesz = new Coord(11, 11);
   public static final Coord cmaps = new Coord(100, 100);
   public static final Coord cutsz = new Coord(25, 25);
   public static final Coord cutn = cmaps.div(cutsz);
   private final Resource.Spec[] nsets = new Resource.Spec[256];
   private final Reference<Resource>[] sets = new Reference[256];
   private final Reference<Resource.Tileset>[] csets = new Reference[256];
   private final Reference<Tiler>[] tiles = new Reference[256];
   Map<Coord, MCache.Request> req = new HashMap<>();
   public Map<Coord, MCache.Grid> grids = new HashMap<>();
   Session sess;
   Set<MCache.Overlay> ols = new HashSet<>();
   int olseq = 0;
   Random gen = new Random();
   Map<Integer, Defrag> fragbufs = new TreeMap<>();
   private MCache.Grid cached = null;

   public void purge() {
      this.req.clear();
      this.grids.clear();
      this.ols.clear();
   }

   public MCache(Session sess) {
      this.sess = sess;
   }

   public void ctick(int dt) {
      synchronized (this.grids) {
         for (MCache.Grid g : this.grids.values()) {
            g.tick(dt);
         }
      }
   }

   public void invalidate(Coord cc) {
      synchronized (this.req) {
         if (this.req.get(cc) == null) {
            this.req.put(cc, new MCache.Request());
         }
      }
   }

   public void invalblob(Message msg) {
      int type = msg.uint8();
      if (type == 0) {
         this.invalidate(msg.coord());
      } else if (type == 1) {
         Coord ul = msg.coord();
         Coord lr = msg.coord();
         this.trim(ul, lr);
      } else if (type == 2) {
         this.trimall();
      }
   }

   public MCache.Grid getgrid(Coord gc) {
      synchronized (this.grids) {
         if (this.cached == null || !this.cached.gc.equals(this.cached)) {
            this.cached = this.grids.get(gc);
            if (this.cached == null) {
               this.request(gc);
               throw new MCache.LoadingMap();
            }
         }

         return this.cached;
      }
   }

   public MCache.Grid getgridt(Coord tc) {
      return this.getgrid(tc.div(cmaps));
   }

   public int gettile(Coord tc) {
      MCache.Grid g = this.getgridt(tc);
      return g.gettile(tc.sub(g.ul));
   }

   public int getz(Coord tc) {
      MCache.Grid g = this.getgridt(tc);
      return g.getz(tc.sub(g.ul));
   }

   public float getcz(float px, float py) {
      float tw = tilesz.x;
      float th = tilesz.y;
      Coord ul = new Coord(Utils.floordiv(px, tw), Utils.floordiv(py, th));
      float sx = Utils.floormod(px, tw) / tw;
      float sy = Utils.floormod(py, th) / th;
      return (1.0F - sy) * ((1.0F - sx) * this.getz(ul) + sx * this.getz(ul.add(1, 0)))
         + sy * ((1.0F - sx) * this.getz(ul.add(0, 1)) + sx * this.getz(ul.add(1, 1)));
   }

   public float getcz(Coord pc) {
      return this.getcz(pc.x, pc.y);
   }

   public int getol(Coord tc) {
      MCache.Grid g = this.getgridt(tc);
      int ol = g.getol(tc.sub(g.ul));

      for (MCache.Overlay lol : this.ols) {
         if (tc.isect(lol.c1, lol.c2.add(lol.c1.inv()).add(new Coord(1, 1)))) {
            ol |= lol.mask;
         }
      }

      return ol;
   }

   public MapMesh getcut(Coord cc) {
      return this.getgrid(cc.div(cutn)).getcut(cc.mod(cutn));
   }

   public Collection<Gob> getfo(Coord cc) {
      return this.getgrid(cc.div(cutn)).getfo(cc.mod(cutn));
   }

   public Rendered getolcut(int ol, Coord cc) {
      return this.getgrid(cc.div(cutn)).getolcut(ol, cc.mod(cutn));
   }

   public void mapdata2(Message msg) {
      Coord c = msg.coord();
      synchronized (this.grids) {
         synchronized (this.req) {
            if (this.req.containsKey(c)) {
               MCache.Grid g = this.grids.get(c);
               if (g == null) {
                  this.grids.put(c, g = new MCache.Grid(c));
               }

               g.fill(msg);
               this.req.remove(c);
               this.olseq++;
            }
         }
      }
   }

   public void mapdata(Message msg) {
      long now = System.currentTimeMillis();
      int pktid = msg.int32();
      int off = msg.uint16();
      int len = msg.uint16();
      synchronized (this.fragbufs) {
         Defrag fragbuf;
         if ((fragbuf = this.fragbufs.get(pktid)) == null) {
            fragbuf = new Defrag(len);
            this.fragbufs.put(pktid, fragbuf);
         }

         fragbuf.add(msg.blob, 8, msg.blob.length - 8, off);
         fragbuf.last = now;
         if (fragbuf.done()) {
            this.mapdata2(fragbuf.msg());
            this.fragbufs.remove(pktid);
         }

         Iterator<Entry<Integer, Defrag>> i = this.fragbufs.entrySet().iterator();

         while (i.hasNext()) {
            Entry<Integer, Defrag> e = i.next();
            Defrag old = e.getValue();
            if (now - old.last > 10000L) {
               i.remove();
            }
         }
      }
   }

   public Resource tilesetr(int i) {
      synchronized (this.sets) {
         Resource res = this.sets[i] == null ? null : this.sets[i].get();
         if (res == null) {
            if (this.nsets[i] == null) {
               return null;
            }

            res = this.nsets[i].get();
            this.sets[i] = new SoftReference<>(res);
         }

         return res;
      }
   }

   public Resource.Tileset tileset(int i) {
      synchronized (this.csets) {
         Resource.Tileset cset = this.csets[i] == null ? null : this.csets[i].get();
         if (cset == null) {
            Resource res = this.tilesetr(i);
            if (res == null) {
               return null;
            }

            try {
               cset = res.layer(Resource.tileset);
            } catch (Loading var7) {
               throw new MCache.LoadingMap(var7);
            }

            this.csets[i] = new SoftReference<>(cset);
         }

         return cset;
      }
   }

   public Tiler tiler(int i) {
      synchronized (this.tiles) {
         Tiler tile = this.tiles[i] == null ? null : this.tiles[i].get();
         if (tile == null) {
            Resource.Tileset set = this.tileset(i);
            if (set == null) {
               return null;
            }

            tile = set.tfac().create(i, set);
            this.tiles[i] = new SoftReference<>(tile);
         }

         return tile;
      }
   }

   public void tilemap(Message msg) {
      while (!msg.eom()) {
         int id = msg.uint8();
         String resnm = msg.string();
         int resver = msg.uint16();
         this.nsets[id] = new Resource.Spec(resnm, resver);
      }
   }

   public void trimall() {
      synchronized (this.grids) {
         synchronized (this.req) {
            for (MCache.Grid g : this.grids.values()) {
               g.dispose();
            }

            this.grids.clear();
            this.req.clear();
         }
      }
   }

   public void trim(Coord ul, Coord lr) {
      synchronized (this.grids) {
         synchronized (this.req) {
            Iterator<Entry<Coord, MCache.Grid>> i = this.grids.entrySet().iterator();

            while (i.hasNext()) {
               Entry<Coord, MCache.Grid> e = i.next();
               Coord gc = e.getKey();
               MCache.Grid g = e.getValue();
               if (gc.x < ul.x || gc.y < ul.y || gc.x > lr.x || gc.y > lr.y) {
                  g.dispose();
                  i.remove();
               }
            }

            i = this.req.keySet().iterator();

            while (i.hasNext()) {
               Coord gc = (Coord)i.next();
               if (gc.x < ul.x || gc.y < ul.y || gc.x > lr.x || gc.y > lr.y) {
                  i.remove();
               }
            }
         }
      }
   }

   public void request(Coord gc) {
      synchronized (this.req) {
         if (!this.req.containsKey(gc)) {
            this.req.put(gc, new MCache.Request());
         }
      }
   }

   public void reqarea(Coord ul, Coord br) {
      ul = ul.div(cutsz);
      br = br.div(cutsz);
      Coord rc = new Coord();

      for (rc.y = ul.y; rc.y <= br.y; rc.y++) {
         for (rc.x = ul.x; rc.x <= br.x; rc.x++) {
            try {
               this.getcut(new Coord(rc));
            } catch (Loading var5) {
            }
         }
      }
   }

   public void sendreqs() {
      long now = System.currentTimeMillis();
      synchronized (this.req) {
         Iterator<Entry<Coord, MCache.Request>> i = this.req.entrySet().iterator();

         while (i.hasNext()) {
            Entry<Coord, MCache.Request> e = i.next();
            Coord c = e.getKey();
            MCache.Request r = e.getValue();
            if (now - r.lastreq > 1000L) {
               r.lastreq = now;
               if (++r.reqs >= 5) {
                  i.remove();
               } else {
                  Message msg = new Message(4);
                  msg.addcoord(c);
                  this.sess.sendmsg(msg);
               }
            }
         }
      }
   }

   public class Grid {
      public final int[] tiles = new int[MCache.cmaps.x * MCache.cmaps.y];
      public final int[] z = new int[MCache.cmaps.x * MCache.cmaps.y];
      public final int[] ol = new int[MCache.cmaps.x * MCache.cmaps.y];
      private final MCache.Grid.Cut[] cuts;
      int olseq = -1;
      private Collection<Gob>[] fo = null;
      public final Coord gc;
      public final Coord ul;
      public long id;
      String mnm;

      public Grid(Coord gc) {
         this.gc = gc;
         this.ul = gc.mul(MCache.cmaps);
         this.cuts = new MCache.Grid.Cut[MCache.cutn.x * MCache.cutn.y];

         for (int i = 0; i < this.cuts.length; i++) {
            this.cuts[i] = new MCache.Grid.Cut();
         }
      }

      public int gettile(Coord tc) {
         return this.tiles[tc.x + tc.y * MCache.cmaps.x];
      }

      public int getz(Coord tc) {
         return this.z[tc.x + tc.y * MCache.cmaps.x];
      }

      public int getol(Coord tc) {
         return this.ol[tc.x + tc.y * MCache.cmaps.x];
      }

      private void makeflavor() {
         Collection<Gob>[] fo = new Collection[MCache.cutn.x * MCache.cutn.y];

         for (int i = 0; i < fo.length; i++) {
            fo[i] = new LinkedList<>();
         }

         Coord c = new Coord(0, 0);
         Coord tc = this.gc.mul(MCache.cmaps);
         int i = 0;
         Random rnd = new Random(this.id);

         for (c.y = 0; c.y < MCache.cmaps.x; c.y++) {
            for (c.x = 0; c.x < MCache.cmaps.y; i++) {
               Resource.Tileset set = MCache.this.tileset(this.tiles[i]);
               if (set.flavobjs.size() > 0 && rnd.nextInt(set.flavprob) == 0) {
                  Resource r = set.flavobjs.pick(rnd);
                  double a = rnd.nextDouble() * 2.0 * Math.PI;
                  Gob g = new MCache.Grid.Flavobj(c.add(tc).mul(MCache.tilesz).add(MCache.tilesz.div(2)), a);
                  g.setattr(new ResDrawable(g, r));
                  Coord cc = c.div(MCache.cutsz);
                  fo[cc.x + cc.y * MCache.cutn.x].add(g);
               }

               c.x++;
            }
         }

         this.fo = fo;
      }

      public Collection<Gob> getfo(Coord cc) {
         if (this.fo == null) {
            this.makeflavor();
         }

         return this.fo[cc.x + cc.y * MCache.cutn.x];
      }

      private MCache.Grid.Cut geticut(Coord cc) {
         return this.cuts[cc.x + cc.y * MCache.cutn.x];
      }

      public MapMesh getcut(Coord cc) {
         MCache.Grid.Cut cut = this.geticut(cc);
         if (cut.dmesh != null && (cut.dmesh.done() || cut.mesh == null)) {
            MapMesh old = cut.mesh;
            cut.mesh = cut.dmesh.get();
            cut.dmesh = null;
            if (old != null) {
               old.dispose();
            }
         }

         return cut.mesh;
      }

      public Rendered getolcut(int ol, Coord cc) {
         int nseq = MCache.this.olseq;
         if (this.olseq != nseq) {
            for (int i = 0; i < MCache.cutn.x * MCache.cutn.y; i++) {
               if (this.cuts[i].ols != null) {
                  for (Rendered r : this.cuts[i].ols) {
                     if (r instanceof Disposable) {
                        ((Disposable)r).dispose();
                     }
                  }
               }

               this.cuts[i].ols = null;
            }

            this.olseq = nseq;
            FlatnessTool.recalcheight();
         }

         MCache.Grid.Cut cut = this.geticut(cc);
         if (cut.ols == null) {
            cut.ols = this.getcut(cc).makeols();
         }

         return cut.ols[ol];
      }

      private void buildcut(final Coord cc) {
         MCache.Grid.Cut cut = this.geticut(cc);
         int deftag = ++cut.deftag;
         cut.dmesh = Defer.later(new Defer.Callable<MapMesh>() {
            public MapMesh call() {
               Random rnd = new Random(Grid.this.id);
               rnd.setSeed(rnd.nextInt() ^ cc.x);
               rnd.setSeed(rnd.nextInt() ^ cc.y);
               return MapMesh.build(MCache.this, rnd, Grid.this.ul.add(cc.mul(MCache.cutsz)), MCache.cutsz);
            }
         });
      }

      public void ivneigh(Coord nc) {
         Coord cc = new Coord();

         for (cc.y = 0; cc.y < MCache.cutn.y; cc.y++) {
            for (cc.x = 0; cc.x < MCache.cutn.x; cc.x++) {
               if ((nc.x < 0 && cc.x == 0 || nc.x > 0 && cc.x == MCache.cutn.x - 1 || nc.x == 0)
                  && (nc.y < 0 && cc.y == 0 || nc.y > 0 && cc.y == MCache.cutn.y - 1 || nc.y == 0)) {
                  this.buildcut(new Coord(cc));
               }
            }
         }
      }

      public void tick(int dt) {
         if (this.fo != null) {
            Collection[] var2 = this.fo;
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; var4++) {
               for (Gob fo : var2[var4]) {
                  fo.ctick(dt);
               }
            }
         }
      }

      private void invalidate() {
         for (int y = 0; y < MCache.cutn.y; y++) {
            for (int x = 0; x < MCache.cutn.x; x++) {
               this.buildcut(new Coord(x, y));
            }
         }

         this.fo = null;

         for (Coord ic : new Coord[]{
            new Coord(-1, -1), new Coord(0, -1), new Coord(1, -1), new Coord(-1, 0), new Coord(1, 0), new Coord(-1, 1), new Coord(0, 1), new Coord(1, 1)
         }) {
            MCache.Grid ng = MCache.this.grids.get(this.gc.add(ic));
            if (ng != null) {
               ng.ivneigh(ic.inv());
            }
         }
      }

      public void dispose() {
         for (MCache.Grid.Cut cut : this.cuts) {
            if (cut.mesh != null) {
               cut.mesh.dispose();
            }

            if (cut.ols != null) {
               for (Rendered r : cut.ols) {
                  if (r instanceof Disposable) {
                     ((Disposable)r).dispose();
                  }
               }
            }
         }
      }

      public void fill(Message msg) {
         String mmname = msg.string().intern();
         if (mmname.equals("")) {
            this.mnm = null;
         } else {
            this.mnm = mmname;
         }

         int[] pfl = new int[256];

         while (true) {
            int pidx = msg.uint8();
            if (pidx == 255) {
               Message blob = msg.inflate();
               this.id = blob.int64();

               for (int i = 0; i < this.tiles.length; i++) {
                  this.tiles[i] = blob.uint8();
               }

               for (int i = 0; i < this.z.length; i++) {
                  this.z[i] = blob.int16();
               }

               for (int i = 0; i < this.ol.length; i++) {
                  this.ol[i] = 0;
               }

               while (true) {
                  int pidxx = blob.uint8();
                  if (pidxx == 255) {
                     this.invalidate();
                     return;
                  }

                  int fl = pfl[pidxx];
                  int type = blob.uint8();
                  Coord c1 = new Coord(blob.uint8(), blob.uint8());
                  Coord c2 = new Coord(blob.uint8(), blob.uint8());
                  int ol;
                  if (type == 0) {
                     if ((fl & 1) == 1) {
                        ol = 2;
                     } else {
                        ol = 1;
                     }
                  } else if (type == 1) {
                     if ((fl & 1) == 1) {
                        ol = 8;
                     } else {
                        ol = 4;
                     }
                  } else {
                     if (type != 2) {
                        throw new RuntimeException("Unknown plot type " + type);
                     }

                     ol = 16;
                  }

                  for (int y = c1.y; y <= c2.y; y++) {
                     for (int x = c1.x; x <= c2.x; x++) {
                        this.ol[x + y * MCache.cmaps.x] = this.ol[x + y * MCache.cmaps.x] | ol;
                     }
                  }
               }
            }

            pfl[pidx] = msg.uint8();
         }
      }

      private class Cut {
         MapMesh mesh;
         Defer.Future<MapMesh> dmesh;
         Rendered[] ols;
         int deftag;

         private Cut() {
         }
      }

      private class Flavobj extends Gob {
         private Flavobj(Coord c, double a) {
            super(MCache.this.sess.glob, c);
            this.a = a;
         }

         @Override
         public Random mkrandoom() {
            Random r = new Random(Grid.this.id);
            r.setSeed(r.nextInt() ^ this.rc.x);
            r.setSeed(r.nextInt() ^ this.rc.y);
            return r;
         }
      }
   }

   public static class LoadingMap extends Loading {
      public LoadingMap() {
      }

      public LoadingMap(Throwable cause) {
         super(cause);
      }
   }

   public class Overlay {
      private Coord c1;
      private Coord c2;
      private int mask;

      public Overlay(Coord c1, Coord c2, int mask) {
         this.c1 = c1;
         this.c2 = c2;
         this.mask = mask;
         MCache.this.ols.add(this);
         MCache.this.olseq++;
      }

      public void destroy() {
         MCache.this.ols.remove(this);
         MCache.this.olseq++;
      }

      public void update(Coord c1, Coord c2) {
         if (!c1.equals(this.c1) || !c2.equals(this.c2)) {
            MCache.this.olseq++;
            this.c1 = c1;
            this.c2 = c2;
         }
      }

      public void update() {
         MCache.this.olseq++;
      }
   }

   private static class Request {
      private long lastreq = 0L;
      private int reqs = 0;

      private Request() {
      }
   }
}
