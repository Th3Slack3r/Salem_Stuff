package haven.resutil;

import haven.Coord;
import haven.Coord3f;
import haven.FastMesh;
import haven.GLState;
import haven.MCache;
import haven.MapMesh;
import haven.MeshBuf;
import haven.Resource;
import haven.Tex;
import java.lang.reflect.Array;
import java.util.Random;

public class RidgeTile extends GroundTile {
  public final int[] breaks;
  
  public final Resource[] walls;
  
  public final Resource[] lcorn;
  
  public final Resource[] rcorn;
  
  public final Resource[] strans;
  
  public final Resource[] c1trans;
  
  public final Resource[] c2trans;
  
  public RidgeTile(int id, Resource.Tileset set, int[] breaks, Resource[] walls, Resource[] lcorn, Resource[] rcorn, Resource[] strans, Resource[] c1trans, Resource[] c2trans) {
    super(id, set);
    this.breaks = breaks;
    this.walls = walls;
    this.lcorn = lcorn;
    this.rcorn = rcorn;
    this.strans = strans;
    this.c1trans = c1trans;
    this.c2trans = c2trans;
  }
  
  public boolean[] breaks(MapMesh m, Coord gc, int diff) {
    int z00 = m.map.getz(gc), z10 = m.map.getz(gc.add(1, 0)), z01 = m.map.getz(gc.add(0, 1)), z11 = m.map.getz(gc.add(1, 1));
    return new boolean[] { (Math.abs(z00 - z10) >= diff), (Math.abs(z10 - z11) >= diff), (Math.abs(z11 - z01) >= diff), (Math.abs(z01 - z00) >= diff) };
  }
  
  public boolean isend(MapMesh m, Coord gc, boolean[] b) {
    return ((b[0] ? 1 : 0) + (b[1] ? 1 : 0) + (b[2] ? 1 : 0) + (b[3] ? 1 : 0) == 1);
  }
  
  public boolean isstraight(MapMesh m, Coord gc, boolean[] b) {
    return ((b[0] && b[2] && !b[1] && !b[3]) || (b[1] && b[3] && !b[0] && !b[2]));
  }
  
  private static <T> T[] shift(T[] a, int n) {
    T[] r = (T[])Array.newInstance(a.getClass().getComponentType(), a.length);
    for (int i = 0; i < a.length; i++)
      r[(i + n) % a.length] = a[i]; 
    return r;
  }
  
  public void makewall(MapMesh m, Coord3f ul, Coord3f bl, Coord3f br, Coord3f ur, Resource wall, float w) {
    float hw = w / 2.0F;
    double tx = (br.x - bl.x), ty = (br.y - bl.y);
    double lf = 1.0D / Math.sqrt(tx * tx + ty * ty);
    float xbx = (float)(tx * lf);
    float xby = (float)(ty * lf);
    float lzof = (float)((br.z - bl.z) * lf);
    float lzsf = (float)((ur.z - br.z - ul.z + bl.z) * lf / 11.0D);
    float lzs = (float)((ul.z - bl.z) / 11.0D);
    float rzof = (float)((bl.z - br.z) * lf);
    float rzsf = (float)((ul.z - bl.z - ur.z + br.z) * lf / 11.0D);
    float rzs = (float)((ur.z - br.z) / 11.0D);
    float tys = (int)((ul.z - bl.z + 5.0F) / 11.0F);
    float tysf = (float)(((int)((ur.z - br.z + 5.0F) / 11.0F) - tys) * lf);
    float ybx = -xby, yby = xbx;
    for (FastMesh.MeshRes r : wall.layers(FastMesh.MeshRes.class)) {
      MeshBuf buf = MapMesh.Models.get(m, (GLState)r.mat.get());
      MeshBuf.Tex ta = (MeshBuf.Tex)buf.layer(MeshBuf.tex);
      MeshBuf.Vertex[] vs = buf.copy(r.m);
      for (MeshBuf.Vertex v : vs) {
        float x = v.pos.x, y = v.pos.y, z = v.pos.z;
        v.pos.x = x * xbx + y * ybx + bl.x;
        v.pos.y = x * xby + y * yby + bl.y;
        if (x < hw) {
          v.pos.z = lzof * x + (lzs + lzsf * x) * z + bl.z;
        } else {
          float X = w - x;
          v.pos.z = rzof * X + (rzs + rzsf * X) * z + br.z;
        } 
        float nx = v.nrm.x, ny = v.nrm.y;
        v.nrm.x = nx * xbx + ny * ybx;
        v.nrm.y = nx * xby + ny * yby;
        ((Coord3f)ta.get(v)).y = (tys + tysf * x) * ((Coord3f)ta.get(v)).y;
      } 
    } 
  }
  
  public static class Ridges extends MapMesh.Hooks {
    public final MapMesh m;
    
    private final Tile[] tiles;
    
    public Ridges(MapMesh m) {
      this.m = m;
      this.tiles = new Tile[m.sz.x * m.sz.y];
    }
    
    public class Tile {
      public TilePlane[] planes = new TilePlane[4];
      
      int n;
      
      public class TilePlane {
        public MapMesh.SPoint[] vrt;
        
        public float u;
        
        public float l;
        
        public float b;
        
        public float r;
        
        public TilePlane(MapMesh.SPoint[] vrt) {
          this.vrt = vrt;
          this.u = this.l = 0.0F;
          this.b = this.r = 1.0F;
          RidgeTile.Ridges.Tile.this.planes[RidgeTile.Ridges.Tile.this.n++] = this;
        }
      }
      
      public void layover(int z, Resource.Tile tile) {
        int w = (tile.tex().sz()).x, h = (tile.tex().sz()).y;
        for (int i = 0; i < this.n; i++) {
          RidgeTile.Ridges.this.m.getClass();
          MapMesh.Plane p = new MapMesh.Plane(RidgeTile.Ridges.this.m, (this.planes[i]).vrt, z, tile.tex(), (tile.t == 'g'));
          p.texrot(new Coord((int)(w * (this.planes[i]).l), (int)(h * (this.planes[i]).u)), new Coord((int)(w * (this.planes[i]).r), (int)(h * (this.planes[i]).b)), 0, false);
        } 
      }
    }
    
    public Tile get(Coord c) {
      return this.tiles[c.x + this.m.sz.x * c.y];
    }
    
    public void set(Coord c, Tile t) {
      this.tiles[c.x + this.m.sz.x * c.y] = t;
    }
    
    public void postcalcnrm(Random rnd) {}
  }
  
  public class Tile {
    public TilePlane[] planes = new TilePlane[4];
    
    int n;
    
    public class TilePlane {
      public MapMesh.SPoint[] vrt;
      
      public float u;
      
      public float l;
      
      public float b;
      
      public float r;
      
      public TilePlane(MapMesh.SPoint[] vrt) {
        this.vrt = vrt;
        this.u = this.l = 0.0F;
        this.b = this.r = 1.0F;
        RidgeTile.Ridges.Tile.this.planes[RidgeTile.Ridges.Tile.this.n++] = this;
      }
    }
    
    public void layover(int z, Resource.Tile tile) {
      int w = (tile.tex().sz()).x, h = (tile.tex().sz()).y;
      for (int i = 0; i < this.n; i++) {
        RidgeTile.Ridges.this.m.getClass();
        MapMesh.Plane p = new MapMesh.Plane(RidgeTile.Ridges.this.m, (this.planes[i]).vrt, z, tile.tex(), (tile.t == 'g'));
        p.texrot(new Coord((int)(w * (this.planes[i]).l), (int)(h * (this.planes[i]).u)), new Coord((int)(w * (this.planes[i]).r), (int)(h * (this.planes[i]).b)), 0, false);
      } 
    }
  }
  
  public class TilePlane {
    public MapMesh.SPoint[] vrt;
    
    public float u;
    
    public float l;
    
    public float b;
    
    public float r;
    
    public TilePlane(MapMesh.SPoint[] vrt) {
      this.vrt = vrt;
      this.u = this.l = 0.0F;
      this.b = this.r = 1.0F;
      RidgeTile.Ridges.Tile.this.planes[RidgeTile.Ridges.Tile.this.n++] = this;
    }
  }
  
  private static final MapMesh.DataID<Ridges> rid = MapMesh.makeid(Ridges.class);
  
  private static final int[] cwx = new int[] { 0, 1, 1, 0 }, cwy = new int[] { 0, 0, 1, 1 }, ecwx = new int[] { 0, 1, 0, -1 }, ecwy = new int[] { -1, 0, 1, 0 };
  
  public void remapquad(Ridges.Tile.TilePlane p, int q) {
    p.u = cwy[q] * 0.5F;
    p.l = cwx[q] * 0.5F;
    p.b = p.u + 0.5F;
    p.r = p.l + 0.5F;
  }
  
  public void remaphalf(Ridges.Tile.TilePlane p, int fq) {
    int l = Math.min(cwx[fq], cwx[(fq + 1) % 4]), r = Math.max(cwx[fq], cwx[(fq + 1) % 4]) + 1;
    int t = Math.min(cwy[fq], cwy[(fq + 1) % 4]), b = Math.max(cwy[fq], cwy[(fq + 1) % 4]) + 1;
    p.u = t * 0.5F;
    p.l = l * 0.5F;
    p.b = b * 0.5F;
    p.r = r * 0.5F;
  }
  
  private void layend(MapMesh m, Random rnd, Coord lc, Coord gc, int dir) {
    Ridges.Tile.TilePlane left, right;
    MapMesh.SPoint[] uh;
    MapMesh.Surface g = m.gnd();
    MapMesh.SPoint bl = g.spoint(lc.add(cwx[dir], cwy[dir])), br = g.spoint(lc.add(cwx[(dir + 1) % 4], cwy[(dir + 1) % 4])), fr = g.spoint(lc.add(cwx[(dir + 2) % 4], cwy[(dir + 2) % 4]));
    MapMesh.SPoint fl = g.spoint(lc.add(cwx[(dir + 3) % 4], cwy[(dir + 3) % 4]));
    boolean cw = (bl.pos.z > br.pos.z);
    MapMesh.SPoint bu = new MapMesh.SPoint(bl.pos.add(br.pos).mul(0.5F));
    MapMesh.SPoint bb = new MapMesh.SPoint(bl.pos.add(br.pos).mul(0.5F));
    MapMesh.SPoint fm = new MapMesh.SPoint(fl.pos.add(fr.pos).mul(0.5F));
    Ridges r = (Ridges)m.data(rid);
    r.getClass();
    Ridges.Tile tile = new Ridges.Tile();
    if (cw) {
      bu.pos.z = bl.pos.z;
      bb.pos.z = br.pos.z;
      tile.getClass();
      left = new Ridges.Tile.TilePlane(uh = shift(new MapMesh.SPoint[] { fl, fm, bu, bl }, 5 - dir));
      tile.getClass();
      right = new Ridges.Tile.TilePlane(shift(new MapMesh.SPoint[] { fm, fr, br, bb }, 5 - dir));
    } else {
      bu.pos.z = br.pos.z;
      bb.pos.z = bl.pos.z;
      tile.getClass();
      left = new Ridges.Tile.TilePlane(shift(new MapMesh.SPoint[] { fl, fm, bb, bl }, 5 - dir));
      tile.getClass();
      right = new Ridges.Tile.TilePlane(uh = shift(new MapMesh.SPoint[] { fm, fr, br, bu }, 5 - dir));
    } 
    remaphalf(left, (dir + 3) % 4);
    remaphalf(right, (dir + 1) % 4);
    r.set(lc, tile);
    tile.layover(0, (Resource.Tile)this.set.ground.pick(rnd));
    m.getClass();
    (new MapMesh.Plane(m, uh, 256, ((Resource.Image)this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc)).tex(), false)).texrot(null, null, 1 + dir + (cw ? 2 : 0), false);
    if (cw) {
      makewall(m, fm.pos, fm.pos, bb.pos, bu.pos, this.walls[rnd.nextInt(this.walls.length)], 11.0F);
    } else {
      makewall(m, bu.pos, bb.pos, fm.pos, fm.pos, this.walls[rnd.nextInt(this.walls.length)], 11.0F);
    } 
  }
  
  public void layend(MapMesh m, Random rnd, Coord lc, Coord gc, boolean[] b) {
    for (int dir = 0; dir < 4; dir++) {
      if (b[dir]) {
        layend(m, rnd, lc, gc, dir);
        return;
      } 
    } 
  }
  
  public void layridge(MapMesh m, Random rnd, Coord lc, Coord gc, boolean[] b) {
    int z00 = m.map.getz(gc), z10 = m.map.getz(gc.add(1, 0)), z01 = m.map.getz(gc.add(0, 1)), z11 = m.map.getz(gc.add(1, 1));
    int dir = b[0] ? ((z00 > z10) ? 0 : 2) : ((z00 > z01) ? 1 : 3);
    boolean tb1 = m.map.tiler(m.map.gettile(gc.add(ecwx[dir], ecwy[dir]))) instanceof RidgeTile;
    boolean tb2 = m.map.tiler(m.map.gettile(gc.add(ecwx[(dir + 2) % 4], ecwy[(dir + 2) % 4]))) instanceof RidgeTile;
    if (tb1 || tb2) {
      if (!tb1) {
        layend(m, rnd, lc, gc, (dir + 2) % 4);
        return;
      } 
      if (!tb2)
        layend(m, rnd, lc, gc, dir); 
    } 
    MapMesh.Surface g = m.gnd();
    MapMesh.SPoint ur = g.spoint(lc.add(cwx[dir], cwy[dir])), br = g.spoint(lc.add(cwx[(dir + 1) % 4], cwy[(dir + 1) % 4])), bl = g.spoint(lc.add(cwx[(dir + 2) % 4], cwy[(dir + 2) % 4]));
    MapMesh.SPoint ul = g.spoint(lc.add(cwx[(dir + 3) % 4], cwy[(dir + 3) % 4]));
    MapMesh.SPoint mlu = new MapMesh.SPoint(ul.pos.add(bl.pos).mul(0.5F)), mlb = new MapMesh.SPoint(ul.pos.add(bl.pos).mul(0.5F)), mru = new MapMesh.SPoint(ur.pos.add(br.pos).mul(0.5F)), mrb = new MapMesh.SPoint(ur.pos.add(br.pos).mul(0.5F));
    mlu.pos.z = ul.pos.z;
    mru.pos.z = ur.pos.z;
    mlb.pos.z = bl.pos.z;
    mrb.pos.z = br.pos.z;
    Ridges r = (Ridges)m.data(rid);
    r.getClass();
    Ridges.Tile tile = new Ridges.Tile();
    tile.getClass();
    Ridges.Tile.TilePlane upper = new Ridges.Tile.TilePlane(shift(new MapMesh.SPoint[] { ul, mlu, mru, ur }, 5 - dir));
    tile.getClass();
    Ridges.Tile.TilePlane lower = new Ridges.Tile.TilePlane(shift(new MapMesh.SPoint[] { mlb, bl, br, mrb }, 5 - dir));
    remaphalf(upper, (dir + 3) % 4);
    remaphalf(lower, (dir + 1) % 4);
    r.set(lc, tile);
    tile.layover(0, (Resource.Tile)this.set.ground.pick(rnd));
    m.getClass();
    (new MapMesh.Plane(m, upper.vrt, 256, ((Resource.Image)this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc)).tex(), false)).texrot(null, null, 3 + dir, false);
    makewall(m, mlu.pos, mlb.pos, mrb.pos, mru.pos, this.walls[rnd.nextInt(this.walls.length)], 11.0F);
  }
  
  public void mkcornwall(MapMesh m, Random rnd, Coord3f ul, Coord3f bl, Coord3f br, Coord3f ur, boolean cw) {
    if (cw) {
      makewall(m, ul, bl, br, ur, this.lcorn[rnd.nextInt(this.lcorn.length)], 5.5F);
    } else {
      makewall(m, ul, bl, br, ur, this.rcorn[rnd.nextInt(this.rcorn.length)], 5.5F);
    } 
  }
  
  public void laycomplex(MapMesh m, Random rnd, Coord lc, Coord gc, boolean[] b) {
    MapMesh.Surface g = m.gnd();
    MapMesh.SPoint[] crn = { g.spoint(lc), g.spoint(lc.add(1, 0)), g.spoint(lc.add(1, 1)), g.spoint(lc.add(0, 1)) };
    int s;
    for (s = 0;; s++) {
      if (b[s]) {
        s = (s + 1) % 4;
        break;
      } 
    } 
    MapMesh.SPoint[] ct = new MapMesh.SPoint[4];
    MapMesh.SPoint[] h1 = new MapMesh.SPoint[4];
    MapMesh.SPoint[] h2 = new MapMesh.SPoint[4];
    for (int i = s, n = 0; n < 4; i = (i + 1) % 4, n++) {
      if (!b[(i + 3) % 4]) {
        h1[i] = h2[(i + 3) % 4];
        (h1[i]).pos.z = ((h1[i]).pos.z + (crn[i]).pos.z) * 0.5F;
      } else {
        h1[i] = new MapMesh.SPoint((crn[(i + 3) % 4]).pos.add((crn[i]).pos).mul(0.5F));
        (h1[i]).pos.z = (crn[i]).pos.z;
      } 
      h2[i] = new MapMesh.SPoint((crn[(i + 1) % 4]).pos.add((crn[i]).pos).mul(0.5F));
      (h2[i]).pos.z = (crn[i]).pos.z;
    } 
    MapMesh.SPoint cc = null;
    int j, k;
    for (j = s, k = 0; k < 4; j = (j + 1) % 4, k++) {
      if (cc == null) {
        cc = new MapMesh.SPoint((crn[0]).pos.add((crn[1]).pos).add((crn[2]).pos).add((crn[3]).pos).mul(0.25F));
        if (b[j]) {
          cc.pos.z = (crn[j]).pos.z;
        } else {
          cc.pos.z = ((h1[j]).pos.z + (h2[(j + 1) % 4]).pos.z) * 0.5F;
        } 
      } 
      ct[j] = cc;
      if (b[j])
        cc = null; 
    } 
    for (j = s, k = 0; k < 4; j = (j + 1) % 4, k++) {
      if (b[j] && !(m.map.tiler(m.map.gettile(gc.add(ecwx[j], ecwy[j]))) instanceof RidgeTile)) {
        (h2[j]).pos.z = ((h2[j]).pos.z + (h1[(j + 1) % 4]).pos.z) * 0.5F;
        h1[(j + 1) % 4] = h2[j];
      } 
    } 
    Ridges r = (Ridges)m.data(rid);
    r.getClass();
    Ridges.Tile tile = new Ridges.Tile();
    boolean cont = false;
    for (int i1 = s, i2 = 0; i2 < 4; i1 = (i1 + 1) % 4, i2++) {
      if (cont) {
        cont = false;
      } else if (!b[i1] && b[(i1 + 1) % 4] && b[(i1 + 3) % 4]) {
        tile.getClass();
        Ridges.Tile.TilePlane pl = new Ridges.Tile.TilePlane(shift(new MapMesh.SPoint[] { crn[i1], h1[i1], h2[(i1 + 1) % 4], crn[(i1 + 1) % 4] }, 4 - i1));
        remaphalf(pl, i1);
        cont = true;
        MapMesh.SPoint pc = ct[(i1 + 3) % 4], sPoint1 = ct[i1];
        if (pc.pos.z > sPoint1.pos.z) {
          mkcornwall(m, rnd, pc.pos, sPoint1.pos, (h1[i1]).pos, (h2[(i1 + 3) % 4]).pos, true);
        } else {
          mkcornwall(m, rnd, (h1[i1]).pos, (h2[(i1 + 3) % 4]).pos, pc.pos, sPoint1.pos, false);
          m.getClass();
          (new MapMesh.Plane(m, pl.vrt, 256, ((Resource.Image)this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc)).tex(), false)).texrot(null, null, i1, false);
        } 
      } else {
        tile.getClass();
        Ridges.Tile.TilePlane pl = new Ridges.Tile.TilePlane(shift(new MapMesh.SPoint[] { crn[i1], h1[i1], ct[i1], h2[i1] }, 4 - i1));
        remapquad(pl, i1);
        boolean[] ub = new boolean[4], db = new boolean[4], tb = new boolean[4];
        for (int o = 0; o < 4; o++) {
          int u = (i1 + o) % 4;
          tb[o] = b[u];
          ub[o] = (b[u] && (h2[u]).pos.z < (h1[(u + 1) % 4]).pos.z);
          db[o] = (b[u] && (h2[u]).pos.z > (h1[(u + 1) % 4]).pos.z);
        } 
        if (ub[3] && db[0]) {
          m.getClass();
          (new MapMesh.Plane(m, pl.vrt, 256, ((Resource.Image)this.c1trans[rnd.nextInt(this.c1trans.length)].layer(Resource.imgc)).tex(), false)).texrot(null, null, i1, false);
        } else if (!tb[0] && !tb[3] && db[1] && ub[2]) {
          m.getClass();
          (new MapMesh.Plane(m, pl.vrt, 256, ((Resource.Image)this.c2trans[rnd.nextInt(this.c2trans.length)].layer(Resource.imgc)).tex(), false)).texrot(null, null, i1, false);
        } else if (ub[3] && !db[0]) {
          Tex t = ((Resource.Image)this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc)).tex();
          m.getClass();
          (new MapMesh.Plane(m, pl.vrt, 256, t, false)).texrot(Coord.z, new Coord((t.sz()).x / 2, (t.sz()).y), i1, false);
        } else if (!ub[3] && db[0]) {
          Tex t = ((Resource.Image)this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc)).tex();
          m.getClass();
          (new MapMesh.Plane(m, pl.vrt, 256, t, false)).texrot(Coord.z, new Coord((t.sz()).x / 2, (t.sz()).y), i1 + 3, false);
        } 
        if (b[(i1 + 3) % 4]) {
          MapMesh.SPoint pc = ct[(i1 + 3) % 4], sPoint1 = ct[i1];
          if (pc.pos.z > sPoint1.pos.z) {
            mkcornwall(m, rnd, pc.pos, sPoint1.pos, (h1[i1]).pos, (h2[(i1 + 3) % 4]).pos, true);
          } else {
            mkcornwall(m, rnd, (h1[i1]).pos, (h2[(i1 + 3) % 4]).pos, pc.pos, sPoint1.pos, false);
          } 
        } 
      } 
    } 
    r.set(lc, tile);
    tile.layover(0, (Resource.Tile)this.set.ground.pick(rnd));
  }
  
  public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
    boolean[] b = breaks(m, gc, this.breaks[0]);
    if (b[0] || b[1] || b[2] || b[3]) {
      if (isend(m, gc, b)) {
        layend(m, rnd, lc, gc, b);
      } else if (isstraight(m, gc, b)) {
        layridge(m, rnd, lc, gc, b);
      } else {
        laycomplex(m, rnd, lc, gc, b);
      } 
    } else {
      super.lay(m, rnd, lc, gc);
    } 
  }
  
  public void layover(MapMesh m, Coord lc, Coord gc, int z, Resource.Tile t) {
    boolean[] b = breaks(m, gc, this.breaks[0]);
    if (b[0] || b[1] || b[2] || b[3]) {
      Ridges.Tile tile = ((Ridges)m.data(rid)).get(lc);
      if (tile == null)
        throw new NullPointerException("Ridged tile has not been properly initialized"); 
      tile.layover(z, t);
    } else {
      super.layover(m, lc, gc, z, t);
    } 
  }
  
  public boolean ridgep(MCache map, Coord tc) {
    int z00 = map.getz(tc), z10 = map.getz(tc.add(1, 0)), z01 = map.getz(tc.add(0, 1)), z11 = map.getz(tc.add(1, 1));
    int diff = this.breaks[0];
    return (Math.abs(z00 - z10) >= diff || Math.abs(z10 - z11) >= diff || Math.abs(z11 - z01) >= diff || Math.abs(z01 - z00) >= diff);
  }
}
