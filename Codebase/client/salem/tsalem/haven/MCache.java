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

public class MCache {
  public static final Coord tilesz = new Coord(11, 11);
  
  public static final Coord cmaps = new Coord(100, 100);
  
  public static final Coord cutsz = new Coord(25, 25);
  
  public static final Coord cutn = cmaps.div(cutsz);
  
  private final Resource.Spec[] nsets = new Resource.Spec[256];
  
  private final Reference<Resource>[] sets = (Reference<Resource>[])new Reference[256];
  
  private final Reference<Resource.Tileset>[] csets = (Reference<Resource.Tileset>[])new Reference[256];
  
  private final Reference<Tiler>[] tiles = (Reference<Tiler>[])new Reference[256];
  
  Map<Coord, Request> req = new HashMap<>();
  
  Map<Coord, Grid> grids = new HashMap<>();
  
  Session sess;
  
  Set<Overlay> ols = new HashSet<>();
  
  int olseq = 0;
  
  Random gen = new Random();
  
  Map<Integer, Defrag> fragbufs = new TreeMap<>();
  
  public static boolean noFlav = Config.mcache_no_flav;
  
  private Grid cached;
  
  public static class LoadingMap extends Loading {
    public LoadingMap() {}
    
    public LoadingMap(Throwable cause) {
      super(cause);
    }
  }
  
  public void purge() {
    this.req.clear();
    this.grids.clear();
    this.ols.clear();
  }
  
  private static class Request {
    private Request() {}
    
    private long lastreq = 0L;
    
    private int reqs = 0;
  }
  
  public class Overlay {
    private Coord c1;
    
    private Coord c2;
    
    private final int mask;
    
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
  
  public class Grid {
    public final int[] tiles = new int[MCache.cmaps.x * MCache.cmaps.y];
    
    public final int[] z = new int[MCache.cmaps.x * MCache.cmaps.y];
    
    public final int[] ol = new int[MCache.cmaps.x * MCache.cmaps.y];
    
    private final Cut[] cuts;
    
    int olseq = -1;
    
    public Collection<Gob>[] fo = null;
    
    public final Coord gc;
    
    public final Coord ul;
    
    public long id;
    
    String mnm;
    
    private class Cut {
      MapMesh mesh;
      
      Defer.Future<MapMesh> dmesh;
      
      Rendered[] ols;
      
      int deftag;
      
      private Cut() {}
    }
    
    private class Flavobj extends Gob {
      private Flavobj(Coord c, double a) {
        super(MCache.this.sess.glob, c);
        this.a = a;
      }
      
      public Random mkrandoom() {
        Random r = new Random(MCache.Grid.this.id);
        r.setSeed((r.nextInt() ^ this.rc.x));
        r.setSeed((r.nextInt() ^ this.rc.y));
        return r;
      }
    }
    
    public Grid(Coord gc) {
      this.gc = gc;
      this.ul = gc.mul(MCache.cmaps);
      this.cuts = new Cut[MCache.cutn.x * MCache.cutn.y];
      for (int i = 0; i < this.cuts.length; i++)
        this.cuts[i] = new Cut(); 
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
      if (MCache.noFlav)
        return; 
      Collection[] arrayOfCollection = new Collection[MCache.cutn.x * MCache.cutn.y];
      for (int i = 0; i < arrayOfCollection.length; i++)
        arrayOfCollection[i] = new LinkedList(); 
      Coord c = new Coord(0, 0);
      Coord tc = this.gc.mul(MCache.cmaps);
      int j = 0;
      Random rnd = new Random(this.id);
      for (c.y = 0; c.y < MCache.cmaps.x; c.y++) {
        for (c.x = 0; c.x < MCache.cmaps.y; c.x++, j++) {
          Resource.Tileset set = MCache.this.tileset(this.tiles[j]);
          if (set.flavobjs.size() > 0 && 
            rnd.nextInt(set.flavprob) == 0) {
            Resource r = set.flavobjs.pick(rnd);
            double a = rnd.nextDouble() * 2.0D * Math.PI;
            Gob g = new Flavobj(c.add(tc).mul(MCache.tilesz).add(MCache.tilesz.div(2)), a);
            g.setattr(new ResDrawable(g, r));
            Coord cc = c.div(MCache.cutsz);
            arrayOfCollection[cc.x + cc.y * MCache.cutn.x].add(g);
          } 
        } 
      } 
      this.fo = (Collection<Gob>[])arrayOfCollection;
    }
    
    public Collection<Gob> getfo(Coord cc) {
      if (MCache.noFlav)
        return null; 
      if (this.fo == null)
        makeflavor(); 
      return this.fo[cc.x + cc.y * MCache.cutn.x];
    }
    
    private Cut geticut(Coord cc) {
      return this.cuts[cc.x + cc.y * MCache.cutn.x];
    }
    
    public MapMesh getcut(Coord cc) {
      Cut cut = geticut(cc);
      if (cut.dmesh != null && (
        cut.dmesh.done() || cut.mesh == null)) {
        MapMesh old = cut.mesh;
        cut.mesh = cut.dmesh.get();
        cut.dmesh = null;
        if (old != null)
          old.dispose(); 
      } 
      return cut.mesh;
    }
    
    public Rendered getolcut(int ol, Coord cc) {
      int nseq = MCache.this.olseq;
      if (this.olseq != nseq) {
        for (int i = 0; i < MCache.cutn.x * MCache.cutn.y; i++) {
          if ((this.cuts[i]).ols != null)
            for (Rendered r : (this.cuts[i]).ols) {
              if (r instanceof Disposable)
                ((Disposable)r).dispose(); 
            }  
          (this.cuts[i]).ols = null;
        } 
        this.olseq = nseq;
        FlatnessTool.recalcheight();
      } 
      Cut cut = geticut(cc);
      if (cut.ols == null)
        cut.ols = getcut(cc).makeols(); 
      return cut.ols[ol];
    }
    
    private void buildcut(final Coord cc) {
      Cut cut = geticut(cc);
      int deftag = ++cut.deftag;
      cut.dmesh = Defer.later(new Defer.Callable<MapMesh>() {
            public MapMesh call() {
              Random rnd = new Random(MCache.Grid.this.id);
              rnd.setSeed((rnd.nextInt() ^ cc.x));
              rnd.setSeed((rnd.nextInt() ^ cc.y));
              return MapMesh.build(MCache.this, rnd, MCache.Grid.this.ul.add(cc.mul(MCache.cutsz)), MCache.cutsz);
            }
          });
    }
    
    public void ivneigh(Coord nc) {
      Coord cc = new Coord();
      for (cc.y = 0; cc.y < MCache.cutn.y; cc.y++) {
        for (cc.x = 0; cc.x < MCache.cutn.x; cc.x++) {
          if (((nc.x < 0 && cc.x == 0) || (nc.x > 0 && cc.x == MCache.cutn.x - 1) || nc.x == 0) && ((nc.y < 0 && cc.y == 0) || (nc.y > 0 && cc.y == MCache.cutn.y - 1) || nc.y == 0))
            buildcut(new Coord(cc)); 
        } 
      } 
    }
    
    public void tick(int dt) {
      if (MCache.noFlav)
        return; 
      if (this.fo != null)
        for (Collection<Gob> fol : this.fo) {
          for (Gob fo : fol)
            fo.ctick(dt); 
        }  
    }
    
    private void invalidate() {
      for (int y = 0; y < MCache.cutn.y; y++) {
        for (int x = 0; x < MCache.cutn.x; x++)
          buildcut(new Coord(x, y)); 
      } 
      this.fo = null;
      for (Coord ic : new Coord[] { new Coord(-1, -1), new Coord(0, -1), new Coord(1, -1), new Coord(-1, 0), new Coord(1, 0), new Coord(-1, 1), new Coord(0, 1), new Coord(1, 1) }) {
        Grid ng = MCache.this.grids.get(this.gc.add(ic));
        if (ng != null)
          ng.ivneigh(ic.inv()); 
      } 
    }
    
    public void dispose() {
      for (Cut cut : this.cuts) {
        if (cut.mesh != null)
          cut.mesh.dispose(); 
        if (cut.ols != null)
          for (Rendered r : cut.ols) {
            if (r instanceof Disposable)
              ((Disposable)r).dispose(); 
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
        if (pidx == 255)
          break; 
        pfl[pidx] = msg.uint8();
      } 
      Message blob = msg.inflate();
      this.id = blob.int64();
      int i;
      for (i = 0; i < this.tiles.length; i++)
        this.tiles[i] = blob.uint8(); 
      for (i = 0; i < this.z.length; i++)
        this.z[i] = blob.int16(); 
      for (i = 0; i < this.ol.length; i++)
        this.ol[i] = 0; 
      while (true) {
        int ol, pidx = blob.uint8();
        if (pidx == 255)
          break; 
        int fl = pfl[pidx];
        int type = blob.uint8();
        Coord c1 = new Coord(blob.uint8(), blob.uint8());
        Coord c2 = new Coord(blob.uint8(), blob.uint8());
        if (type == 0) {
          if ((fl & 0x1) == 1) {
            ol = 2;
          } else {
            ol = 1;
          } 
        } else if (type == 1) {
          if ((fl & 0x1) == 1) {
            ol = 8;
          } else {
            ol = 4;
          } 
        } else if (type == 2) {
          ol = 16;
        } else {
          throw new RuntimeException("Unknown plot type " + type);
        } 
        for (int y = c1.y; y <= c2.y; y++) {
          for (int x = c1.x; x <= c2.x; x++)
            this.ol[x + y * MCache.cmaps.x] = this.ol[x + y * MCache.cmaps.x] | ol; 
        } 
      } 
      invalidate();
    }
  }
  
  public void ctick(int dt) {
    synchronized (this.grids) {
      for (Grid g : this.grids.values())
        g.tick(dt); 
    } 
  }
  
  public void invalidate(Coord cc) {
    synchronized (this.req) {
      if (this.req.get(cc) == null)
        this.req.put(cc, new Request()); 
    } 
  }
  
  public void invalblob(Message msg) {
    int type = msg.uint8();
    if (type == 0) {
      invalidate(msg.coord());
    } else if (type == 1) {
      Coord ul = msg.coord();
      Coord lr = msg.coord();
      trim(ul, lr);
    } else if (type == 2) {
      trimall();
    } 
  }
  
  public MCache(Session sess) {
    this.cached = null;
    this.sess = sess;
  }
  
  public Grid getgrid(Coord gc) {
    synchronized (this.grids) {
      if (this.cached == null || !this.cached.gc.equals(this.cached)) {
        this.cached = this.grids.get(gc);
        if (this.cached == null) {
          request(gc);
          throw new LoadingMap();
        } 
      } 
      return this.cached;
    } 
  }
  
  public Grid getgridt(Coord tc) {
    return getgrid(tc.div(cmaps));
  }
  
  public int gettile(Coord tc) {
    Grid g = getgridt(tc);
    return g.gettile(tc.sub(g.ul));
  }
  
  public int getz(Coord tc) {
    Grid g = getgridt(tc);
    return g.getz(tc.sub(g.ul));
  }
  
  public float getcz(float px, float py) {
    float tw = tilesz.x, th = tilesz.y;
    Coord ul = new Coord(Utils.floordiv(px, tw), Utils.floordiv(py, th));
    float sx = Utils.floormod(px, tw) / tw;
    float sy = Utils.floormod(py, th) / th;
    return (1.0F - sy) * ((1.0F - sx) * getz(ul) + sx * getz(ul.add(1, 0))) + sy * ((1.0F - sx) * getz(ul.add(0, 1)) + sx * getz(ul.add(1, 1)));
  }
  
  public float getcz(Coord pc) {
    return getcz(pc.x, pc.y);
  }
  
  public int getol(Coord tc) {
    Grid g = getgridt(tc);
    int ol = g.getol(tc.sub(g.ul));
    for (Overlay lol : this.ols) {
      if (tc.isect(lol.c1, lol.c2.add(lol.c1.inv()).add(new Coord(1, 1))))
        ol |= lol.mask; 
    } 
    return ol;
  }
  
  public MapMesh getcut(Coord cc) {
    return getgrid(cc.div(cutn)).getcut(cc.mod(cutn));
  }
  
  public Collection<Gob> getfo(Coord cc) {
    return getgrid(cc.div(cutn)).getfo(cc.mod(cutn));
  }
  
  public Rendered getolcut(int ol, Coord cc) {
    return getgrid(cc.div(cutn)).getolcut(ol, cc.mod(cutn));
  }
  
  public void mapdata2(Message msg) {
    Coord c = msg.coord();
    synchronized (this.grids) {
      synchronized (this.req) {
        if (this.req.containsKey(c)) {
          Grid g = this.grids.get(c);
          if (g == null)
            this.grids.put(c, g = new Grid(c)); 
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
      if ((fragbuf = this.fragbufs.get(Integer.valueOf(pktid))) == null) {
        fragbuf = new Defrag(len);
        this.fragbufs.put(Integer.valueOf(pktid), fragbuf);
      } 
      fragbuf.add(msg.blob, 8, msg.blob.length - 8, off);
      fragbuf.last = now;
      if (fragbuf.done()) {
        mapdata2(fragbuf.msg());
        this.fragbufs.remove(Integer.valueOf(pktid));
      } 
      for (Iterator<Map.Entry<Integer, Defrag>> i = this.fragbufs.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry<Integer, Defrag> e = i.next();
        Defrag old = e.getValue();
        if (now - old.last > 10000L)
          i.remove(); 
      } 
    } 
  }
  
  public Resource tilesetr(int i) {
    synchronized (this.sets) {
      Resource res = (this.sets[i] == null) ? null : this.sets[i].get();
      if (res == null) {
        if (this.nsets[i] == null)
          return null; 
        res = this.nsets[i].get();
        this.sets[i] = new SoftReference<>(res);
      } 
      return res;
    } 
  }
  
  public Resource.Tileset tileset(int i) {
    synchronized (this.csets) {
      Resource.Tileset cset = (this.csets[i] == null) ? null : this.csets[i].get();
      if (cset == null) {
        Resource res = tilesetr(i);
        if (res == null)
          return null; 
        try {
          cset = res.<Resource.Tileset>layer(Resource.tileset);
        } catch (Loading e) {
          throw new LoadingMap(e);
        } 
        this.csets[i] = new SoftReference<>(cset);
      } 
      return cset;
    } 
  }
  
  public Tiler tiler(int i) {
    synchronized (this.tiles) {
      Tiler tile = (this.tiles[i] == null) ? null : this.tiles[i].get();
      if (tile == null) {
        Resource.Tileset set = tileset(i);
        if (set == null)
          return null; 
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
        for (Grid g : this.grids.values())
          g.dispose(); 
        this.grids.clear();
        this.req.clear();
      } 
    } 
  }
  
  public void trim(Coord ul, Coord lr) {
    synchronized (this.grids) {
      synchronized (this.req) {
        for (Iterator<Map.Entry<Coord, Grid>> iterator = this.grids.entrySet().iterator(); iterator.hasNext(); ) {
          Map.Entry<Coord, Grid> e = iterator.next();
          Coord gc = e.getKey();
          Grid g = e.getValue();
          if (gc.x < ul.x || gc.y < ul.y || gc.x > lr.x || gc.y > lr.y) {
            g.dispose();
            iterator.remove();
          } 
        } 
        for (Iterator<Coord> i = this.req.keySet().iterator(); i.hasNext(); ) {
          Coord gc = i.next();
          if (gc.x < ul.x || gc.y < ul.y || gc.x > lr.x || gc.y > lr.y)
            i.remove(); 
        } 
      } 
    } 
  }
  
  public void request(Coord gc) {
    synchronized (this.req) {
      if (!this.req.containsKey(gc))
        this.req.put(gc, new Request()); 
    } 
  }
  
  public void reqarea(Coord ul, Coord br) {
    ul = ul.div(cutsz);
    br = br.div(cutsz);
    Coord rc = new Coord();
    for (rc.y = ul.y; rc.y <= br.y; rc.y++) {
      for (rc.x = ul.x; rc.x <= br.x; rc.x++) {
        try {
          getcut(new Coord(rc));
        } catch (Loading loading) {}
      } 
    } 
  }
  
  public void sendreqs() {
    long now = System.currentTimeMillis();
    synchronized (this.req) {
      for (Iterator<Map.Entry<Coord, Request>> i = this.req.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry<Coord, Request> e = i.next();
        Coord c = e.getKey();
        Request r = e.getValue();
        if (now - r.lastreq > 1000L) {
          r.lastreq = now;
          if (++r.reqs >= 5) {
            i.remove();
            continue;
          } 
          Message msg = new Message(4);
          msg.addcoord(c);
          this.sess.sendmsg(msg);
        } 
      } 
    } 
  }
}
