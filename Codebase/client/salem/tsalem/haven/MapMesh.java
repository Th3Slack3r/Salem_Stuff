package haven;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapMesh implements Rendered, Disposable {
  public final Coord ul;
  
  public final Coord sz;
  
  public final MCache map;
  
  private Map<Tex, GLState[]> texmap = (Map)new HashMap<>();
  
  private final Map<DataID, Object> data = new HashMap<>();
  
  private final List<Rendered> extras = new ArrayList<>();
  
  private List<Layer> layers;
  
  private FastMesh[] flats;
  
  private final List<Disposable> dparts = new ArrayList<>();
  
  public static <T> DataID<T> makeid(Class<T> cl) {
    try {
      final Constructor<T> cons = cl.getConstructor(new Class[] { MapMesh.class });
      return new DataID<T>() {
          public T make(MapMesh m) {
            return Utils.construct(cons, new Object[] { m });
          }
        };
    } catch (NoSuchMethodException noSuchMethodException) {
      try {
        final Constructor<T> cons = cl.getConstructor(new Class[0]);
        return new DataID<T>() {
            public T make(MapMesh m) {
              return Utils.construct(cons, new Object[0]);
            }
          };
      } catch (NoSuchMethodException noSuchMethodException1) {
        throw new Error("No proper data-ID constructor found");
      } 
    } 
  }
  
  public static class Hooks {
    public void calcnrm() {}
    
    public void postcalcnrm(Random rnd) {}
    
    public boolean clean() {
      return false;
    }
  }
  
  public <T> T data(DataID<T> id) {
    T ret = (T)this.data.get(id);
    if (ret == null)
      this.data.put(id, ret = id.make(this)); 
    return ret;
  }
  
  private static final Material.Colors gcol = new Material.Colors(new Color(128, 128, 128), new Color(255, 255, 255), new Color(0, 0, 0), new Color(0, 0, 0));
  
  public GLState stfor(Tex tex, boolean clip) {
    TexGL gt;
    if (tex instanceof TexGL) {
      gt = (TexGL)tex;
    } else if (tex instanceof TexSI && ((TexSI)tex).parent instanceof TexGL) {
      gt = (TexGL)((TexSI)tex).parent;
    } else {
      throw new RuntimeException("Cannot use texture for map rendering: " + tex);
    } 
    GLState[] ret = this.texmap.get(gt);
    if (ret == null)
      this.texmap.put(gt, ret = new GLState[] { new Material(new GLState[] { Light.deflight, gcol, gt.draw(), gt.clip() }), new Material(new GLState[] { Light.deflight, gcol, gt.draw() }) }); 
    return ret[clip ? 0 : 1];
  }
  
  public static class Scan {
    public final Coord ul;
    
    public final Coord sz;
    
    public final Coord br;
    
    public final int l;
    
    public Scan(Coord ul, Coord sz) {
      this.ul = ul;
      this.sz = sz;
      this.br = sz.add(ul);
      this.l = sz.x * sz.y;
    }
    
    public int o(int x, int y) {
      return x - this.ul.x + (y - this.ul.y) * this.sz.x;
    }
    
    public int o(Coord in) {
      return o(in.x, in.y);
    }
  }
  
  public static class SPoint {
    public Coord3f pos;
    
    public Coord3f nrm = Coord3f.zu;
    
    public SPoint(Coord3f pos) {
      this.pos = pos;
    }
  }
  
  public class Surface extends Hooks {
    public final MapMesh.SPoint[] surf = new MapMesh.SPoint[(MapMesh.this.sz.x + 3) * (MapMesh.this.sz.y + 3)];
    
    public Surface() {
      Coord c = new Coord();
      int i = 0;
      for (c.y = -1; c.y <= MapMesh.this.sz.y + 1; c.y++) {
        for (c.x = -1; c.x <= MapMesh.this.sz.x + 1; c.x++)
          this.surf[i++] = new MapMesh.SPoint(new Coord3f((c.x * MCache.tilesz.x), (c.y * -MCache.tilesz.y), MapMesh.this.map.getz(MapMesh.this.ul.add(c)))); 
      } 
    }
    
    public int idx(Coord lc) {
      return lc.x + 1 + (lc.y + 1) * (MapMesh.this.sz.x + 3);
    }
    
    public MapMesh.SPoint spoint(Coord lc) {
      return this.surf[idx(lc)];
    }
    
    public void calcnrm() {
      Coord c = new Coord();
      int i = idx(Coord.z);
      int r = MapMesh.this.sz.x + 3;
      for (c.y = 0; c.y <= MapMesh.this.sz.y; c.y++) {
        for (c.x = 0; c.x <= MapMesh.this.sz.x; c.x++) {
          MapMesh.SPoint p = this.surf[i];
          Coord3f s = (this.surf[i + r]).pos.sub(p.pos);
          Coord3f w = (this.surf[i - 1]).pos.sub(p.pos);
          Coord3f n = (this.surf[i - r]).pos.sub(p.pos);
          Coord3f e = (this.surf[i + 1]).pos.sub(p.pos);
          Coord3f nrm = n.cmul(w).add(e.cmul(n)).add(s.cmul(e)).add(w.cmul(s)).norm();
          p.nrm = nrm;
          i++;
        } 
        i += 2;
      } 
    }
    
    public MapMesh.SPoint[] fortile(Coord sc) {
      return new MapMesh.SPoint[] { spoint(sc), spoint(sc.add(0, 1)), spoint(sc.add(1, 1)), spoint(sc.add(1, 0)) };
    }
  }
  
  public static void splitquad(MeshBuf buf, MeshBuf.Vertex v1, MeshBuf.Vertex v2, MeshBuf.Vertex v3, MeshBuf.Vertex v4) {
    if (Math.abs(v1.pos.z - v3.pos.z) > Math.abs(v2.pos.z - v4.pos.z)) {
      buf.getClass();
      new MeshBuf.Face(buf, v1, v2, v3);
      buf.getClass();
      new MeshBuf.Face(buf, v1, v3, v4);
    } else {
      buf.getClass();
      new MeshBuf.Face(buf, v1, v2, v4);
      buf.getClass();
      new MeshBuf.Face(buf, v2, v3, v4);
    } 
  }
  
  public abstract class Shape {
    public Shape(int z, GLState st) {
      reg(z, st);
    }
    
    public abstract void build(MeshBuf param1MeshBuf);
    
    private void reg(int z, GLState st) {
      for (MapMesh.Layer layer : MapMesh.this.layers) {
        if (layer.st == st && layer.z == z) {
          layer.pl.add(this);
          return;
        } 
      } 
      MapMesh.Layer l = new MapMesh.Layer();
      l.st = st;
      l.z = z;
      l.pl.add(this);
      MapMesh.this.layers.add(l);
    }
    
    public MapMesh m() {
      return MapMesh.this;
    }
  }
  
  private static SPoint[] fortile(Surface surf, Coord sc) {
    return surf.fortile(sc);
  }
  
  public class Plane extends Shape {
    public MapMesh.SPoint[] vrt;
    
    public int[] texx;
    
    public int[] texy;
    
    public Tex tex = null;
    
    public Plane(MapMesh.SPoint[] vrt, int z, GLState st) {
      super(z, st);
      this.vrt = vrt;
    }
    
    public Plane(MapMesh.Surface surf, Coord sc, int z, GLState st) {
      this(MapMesh.fortile(surf, sc), z, st);
    }
    
    public Plane(MapMesh.SPoint[] vrt, int z, GLState st, Tex tex) {
      this(vrt, z, st);
      this.tex = tex;
      texrot(null, null, 0, false);
    }
    
    public Plane(MapMesh.SPoint[] vrt, int z, Tex tex, boolean clip) {
      this(vrt, z, MapMesh.this.stfor(tex, clip), tex);
    }
    
    public Plane(MapMesh.Surface surf, Coord sc, int z, Tex tex, boolean clip) {
      this(MapMesh.fortile(surf, sc), z, tex, clip);
    }
    
    public Plane(MapMesh.Surface surf, Coord sc, int z, Tex tex) {
      this(surf, sc, z, tex, true);
    }
    
    public Plane(MapMesh.Surface surf, Coord sc, int z, Resource.Tile tile) {
      this(surf, sc, z, tile.tex(), (tile.t != 'g'));
    }
    
    public void texrot(Coord ul, Coord br, int rot, boolean flipx) {
      int[] x;
      int[] y;
      if (ul == null)
        ul = Coord.z; 
      if (br == null)
        br = this.tex.sz(); 
      if (!flipx) {
        x = new int[] { ul.x, ul.x, br.x, br.x };
        y = new int[] { ul.y, br.y, br.y, ul.y };
      } else {
        x = new int[] { br.x, br.x, ul.x, ul.x };
        y = new int[] { ul.y, br.y, br.y, ul.y };
      } 
      if (this.texx == null) {
        this.texx = new int[4];
        this.texy = new int[4];
      } 
      for (int i = 0; i < 4; i++) {
        this.texx[i] = x[(i + rot) % 4];
        this.texy[i] = y[(i + rot) % 4];
      } 
    }
    
    public void build(MeshBuf buf) {
      MeshBuf.Tex btex = buf.<MeshBuf.Tex>layer(MeshBuf.tex);
      buf.getClass();
      MeshBuf.Vertex v1 = new MeshBuf.Vertex(buf, (this.vrt[0]).pos, (this.vrt[0]).nrm);
      buf.getClass();
      MeshBuf.Vertex v2 = new MeshBuf.Vertex(buf, (this.vrt[1]).pos, (this.vrt[1]).nrm);
      buf.getClass();
      MeshBuf.Vertex v3 = new MeshBuf.Vertex(buf, (this.vrt[2]).pos, (this.vrt[2]).nrm);
      buf.getClass();
      MeshBuf.Vertex v4 = new MeshBuf.Vertex(buf, (this.vrt[3]).pos, (this.vrt[3]).nrm);
      Tex tex = this.tex;
      if (tex != null) {
        int r = (tex.sz()).x, b = (tex.sz()).y;
        btex.set(v1, new Coord3f(tex.tcx(this.texx[0]), tex.tcy(this.texy[0]), 0.0F));
        btex.set(v2, new Coord3f(tex.tcx(this.texx[1]), tex.tcy(this.texy[1]), 0.0F));
        btex.set(v3, new Coord3f(tex.tcx(this.texx[2]), tex.tcy(this.texy[2]), 0.0F));
        btex.set(v4, new Coord3f(tex.tcx(this.texx[3]), tex.tcy(this.texy[3]), 0.0F));
      } 
      MapMesh.splitquad(buf, v1, v2, v3, v4);
    }
  }
  
  private static final Rendered.Order mmorder = new Rendered.Order<Layer>() {
      public int mainz() {
        return 1000;
      }
      
      private final Rendered.RComparator<MapMesh.Layer> cmp = new Rendered.RComparator<MapMesh.Layer>() {
          public int compare(MapMesh.Layer a, MapMesh.Layer b, GLState.Buffer sa, GLState.Buffer sb) {
            return a.z - b.z;
          }
        };
      
      public Rendered.RComparator<MapMesh.Layer> cmp() {
        return this.cmp;
      }
    };
  
  private static class Layer implements Rendered {
    GLState st;
    
    int z;
    
    FastMesh mesh;
    
    Collection<MapMesh.Shape> pl = new LinkedList<>();
    
    public void draw(GOut g) {
      this.mesh.draw(g);
    }
    
    public boolean setup(RenderList rl) {
      rl.prepo(this.st);
      rl.prepo(MapMesh.mmorder);
      return true;
    }
    
    private Layer() {}
  }
  
  private MapMesh(MCache map, Coord ul, Coord sz) {
    this.map = map;
    this.ul = ul;
    this.sz = sz;
  }
  
  private static void dotrans(MapMesh m, Random rnd, Coord lc, Coord gc) {
    Tiler ground = m.map.tiler(m.map.gettile(gc));
    int[][] tr = new int[3][3];
    int max = -1;
    for (int y = -1; y <= 1; y++) {
      for (int x = -1; x <= 1; x++) {
        if (x != 0 || y != 0) {
          int tn = m.map.gettile(gc.add(x, y));
          tr[x + 1][y + 1] = tn;
          if (tn > max)
            max = tn; 
        } 
      } 
    } 
    int[] bx = { 0, 1, 2, 1 };
    int[] by = { 1, 0, 1, 2 };
    int[] cx = { 0, 2, 2, 0 };
    int[] cy = { 0, 0, 2, 2 };
    for (int i = max; i >= 0; i--) {
      int bm = 0, cm = 0;
      int o;
      for (o = 0; o < 4; o++) {
        if (tr[bx[o]][by[o]] == i)
          bm |= 1 << o; 
      } 
      for (o = 0; o < 4; o++) {
        if ((bm & (1 << o | 1 << (o + 1) % 4)) == 0)
          if (tr[cx[o]][cy[o]] == i)
            cm |= 1 << o;  
      } 
      if (bm != 0 || cm != 0) {
        Tiler t = m.map.tiler(i);
        if (t != null)
          t.trans(m, rnd, ground, lc, gc, 255 - i, bm, cm); 
      } 
    } 
  }
  
  public class Ground extends Surface {
    public boolean clean() {
      return true;
    }
  }
  
  private static DataID<Ground> gndid = makeid(Ground.class);
  
  public Surface gnd() {
    return data((DataID)gndid);
  }
  
  public static class Models extends Hooks {
    private final MapMesh m;
    
    private final Map<GLState, MeshBuf> models = new HashMap<>();
    
    public Models(MapMesh m) {
      this.m = m;
    }
    
    private static MapMesh.DataID<Models> msid = MapMesh.makeid(Models.class);
    
    public static MeshBuf get(MapMesh m, GLState st) {
      Models ms = m.<Models>data(msid);
      MeshBuf ret = ms.models.get(st);
      if (ret == null)
        ms.models.put(st, ret = new MeshBuf()); 
      return ret;
    }
    
    public void postcalcnrm(Random rnd) {
      for (Map.Entry<GLState, MeshBuf> mod : this.models.entrySet()) {
        FastMesh mesh = ((MeshBuf)mod.getValue()).mkmesh(-1);
        this.m.extras.add(((GLState)mod.getKey()).apply(mesh));
        this.m.dparts.add(mesh);
      } 
    }
  }
  
  public static MapMesh build(MCache mc, Random rnd, Coord ul, Coord sz) {
    MapMesh m = new MapMesh(mc, ul, sz);
    Coord c = new Coord();
    m.layers = new ArrayList<>();
    for (c.y = 0; c.y < sz.y; c.y++) {
      for (c.x = 0; c.x < sz.x; c.x++) {
        Coord gc = c.add(ul);
        long ns = rnd.nextLong();
        mc.tiler(mc.gettile(gc)).lay(m, rnd, c, gc);
        dotrans(m, rnd, c, gc);
        rnd.setSeed(ns);
      } 
    } 
    for (Object obj : m.data.values()) {
      if (obj instanceof Hooks)
        ((Hooks)obj).calcnrm(); 
    } 
    for (Object obj : m.data.values()) {
      if (obj instanceof Hooks)
        ((Hooks)obj).postcalcnrm(rnd); 
    } 
    for (Layer l : m.layers) {
      MeshBuf buf = new MeshBuf();
      if (l.pl.isEmpty())
        throw new RuntimeException("Map layer without planes?!"); 
      for (Shape p : l.pl)
        p.build(buf); 
      l.mesh = buf.mkmesh(-1);
      m.dparts.add(l.mesh);
    } 
    Collections.sort(m.layers, new Comparator<Layer>() {
          public int compare(MapMesh.Layer a, MapMesh.Layer b) {
            return a.z - b.z;
          }
        });
    m.consflat();
    m.clean();
    return m;
  }
  
  private static States.DepthOffset gmoff = new States.DepthOffset(-1.0F, -1.0F);
  
  public static class GroundMod implements Rendered, Disposable {
    private static final Rendered.Order gmorder = new Rendered.Order.Default(1001);
    
    public final Material mat;
    
    public final Coord cc;
    
    public final FastMesh mesh;
    
    public GroundMod(MCache map, MapMesh.DataID<? extends MapMesh.Surface> surf, Tex tex, Coord cc, Coord ul, Coord br) {
      this.mat = new Material(tex);
      this.cc = cc;
      if (tex instanceof TexGL) {
        TexGL gt = (TexGL)tex;
        if (gt.wrapmode != 33069) {
          gt.wrapmode = 33069;
          gt.dispose();
        } 
      } 
      if (surf == null)
        surf = (MapMesh.DataID)MapMesh.gndid; 
      MeshBuf buf = new MeshBuf();
      MeshBuf.Tex ta = buf.<MeshBuf.Tex>layer(MeshBuf.tex);
      Coord ult = ul.div(MCache.tilesz);
      Coord brt = br.sub(1, 1).div(MCache.tilesz).add(1, 1);
      Coord t = new Coord();
      float cz = map.getcz(cc);
      MeshBuf.Vertex[][] vm = new MeshBuf.Vertex[brt.x - ult.x + 1][brt.y - ult.y + 1];
      for (t.y = ult.y; t.y <= brt.y; t.y++) {
        for (t.x = ult.x; t.x <= brt.x; t.x++) {
          MapMesh cut = map.getcut(t.div(MCache.cutsz));
          MapMesh.SPoint p = ((MapMesh.Surface)cut.<MapMesh.Surface>data((MapMesh.DataID)surf)).spoint(t.mod(MCache.cutsz));
          Coord3f texc = new Coord3f((t.x * MCache.tilesz.x - ul.x) / (br.x - ul.x), (t.y * MCache.tilesz.y - ul.y) / (br.y - ul.y), 0.0F);
          Coord3f pos = p.pos.add((cut.ul.x * MCache.tilesz.x - cc.x), -(cut.ul.y * MCache.tilesz.y - cc.y), -cz);
          buf.getClass();
          MeshBuf.Vertex v = vm[t.x - ult.x][t.y - ult.y] = new MeshBuf.Vertex(buf, pos, p.nrm);
          ta.set(v, texc);
        } 
      } 
      for (t.y = 0; t.y < brt.y - ult.y; t.y++) {
        for (t.x = 0; t.x < brt.x - ult.x; t.x++)
          MapMesh.splitquad(buf, vm[t.x][t.y], vm[t.x][t.y + 1], vm[t.x + 1][t.y + 1], vm[t.x + 1][t.y]); 
      } 
      this.mesh = buf.mkmesh(-1);
    }
    
    @Deprecated
    public GroundMod(MCache map, Class<? extends MapMesh.Surface> surf, Tex tex, Coord cc, Coord ul, Coord br) {
      this(map, (MapMesh.DataID<? extends MapMesh.Surface>)null, tex, cc, ul, br);
      if (surf != null)
        throw new RuntimeException(); 
    }
    
    public void dispose() {
      this.mesh.dispose();
    }
    
    public void draw(GOut g) {}
    
    public boolean setup(RenderList rl) {
      rl.prepc(gmorder);
      rl.prepc(this.mat);
      rl.prepc(MapMesh.gmoff);
      rl.add(this.mesh, null);
      return false;
    }
  }
  
  public static final Rendered.Order olorder = new Rendered.Order.Default(1002);
  
  public Rendered[] makeols() {
    Surface surf = new Surface();
    surf.calcnrm();
    MeshBuf buf = new MeshBuf();
    MeshBuf.Vertex[][] v = new MeshBuf.Vertex[this.sz.x + 1][this.sz.y + 1];
    Coord t = new Coord();
    for (t.y = 0; t.y <= this.sz.y; t.y++) {
      for (t.x = 0; t.x <= this.sz.x; t.x++) {
        SPoint p = surf.spoint(t);
        buf.getClass();
        v[t.x][t.y] = new MeshBuf.Vertex(buf, p.pos, p.nrm);
      } 
    } 
    int[][] ol = new int[this.sz.x][this.sz.y];
    for (t.y = 0; t.y < this.sz.y; t.y++) {
      for (t.x = 0; t.x < this.sz.x; t.x++)
        ol[t.x][t.y] = this.map.getol(this.ul.add(t)); 
    } 
    Rendered[] ret = new Rendered[32];
    for (int i = 0; i < 32; i++) {
      boolean h = false;
      buf.clearfaces();
      for (t.y = 0; t.y < this.sz.y; t.y++) {
        for (t.x = 0; t.x < this.sz.x; t.x++) {
          if ((ol[t.x][t.y] & 1 << i) != 0) {
            h = true;
            splitquad(buf, v[t.x][t.y], v[t.x][t.y + 1], v[t.x + 1][t.y + 1], v[t.x + 1][t.y]);
          } 
        } 
      } 
      if (h) {
        final FastMesh mesh = buf.mkmesh(i);
        ret[i] = new OL();
      } 
    } 
    class OL implements Rendered, Disposable {
      public void draw(GOut g) {
        mesh.draw(g);
      }
      
      public void dispose() {
        mesh.dispose();
      }
      
      public boolean setup(RenderList rl) {
        rl.prepo(MapMesh.olorder);
        return true;
      }
    };
    return ret;
  }
  
  private void clean() {
    this.texmap = null;
    for (Layer l : this.layers)
      l.pl = null; 
    int on = this.data.size();
    for (Iterator<Map.Entry<DataID, Object>> i = this.data.entrySet().iterator(); i.hasNext(); ) {
      Object d = ((Map.Entry)i.next()).getValue();
      if (!(d instanceof Hooks) || !((Hooks)d).clean())
        i.remove(); 
    } 
  }
  
  public void draw(GOut g) {}
  
  private void consflat() {
    Surface g = gnd();
    FloatBuffer pos = FloatBuffer.wrap(new float[this.sz.x * this.sz.y * 12]);
    FloatBuffer col1 = FloatBuffer.wrap(new float[this.sz.x * this.sz.y * 16]);
    FloatBuffer col2 = FloatBuffer.wrap(new float[this.sz.x * this.sz.y * 16]);
    ShortBuffer ind = ShortBuffer.wrap(new short[this.sz.x * this.sz.y * 6]);
    short i = 0;
    Coord c = new Coord();
    for (c.y = 0; c.y < this.sz.y; c.y++) {
      for (c.x = 0; c.x < this.sz.x; c.x++) {
        SPoint p = g.spoint(c);
        float z0 = p.pos.z;
        pos.put(p.pos.x).put(p.pos.y).put(z0);
        col1.put((c.x + 1) / 256.0F).put((c.y + 1) / 256.0F).put(0.0F).put(1.0F);
        col2.put(0.0F).put(0.0F).put(0.0F).put(1.0F);
        p = g.spoint(c.add(0, 1));
        float z1 = p.pos.z;
        pos.put(p.pos.x).put(p.pos.y).put(z1);
        col1.put((c.x + 1) / 256.0F).put((c.y + 1) / 256.0F).put(0.0F).put(1.0F);
        col2.put(0.0F).put(1.0F).put(0.0F).put(1.0F);
        p = g.spoint(c.add(1, 1));
        float z2 = p.pos.z;
        pos.put(p.pos.x).put(p.pos.y).put(z2);
        col1.put((c.x + 1) / 256.0F).put((c.y + 1) / 256.0F).put(0.0F).put(1.0F);
        col2.put(1.0F).put(1.0F).put(0.0F).put(1.0F);
        p = g.spoint(c.add(1, 0));
        float z3 = p.pos.z;
        pos.put(p.pos.x).put(p.pos.y).put(z3);
        col1.put((c.x + 1) / 256.0F).put((c.y + 1) / 256.0F).put(0.0F).put(1.0F);
        col2.put(1.0F).put(0.0F).put(0.0F).put(1.0F);
        if (Math.abs(z0 - z2) > Math.abs(z1 - z3)) {
          ind.put(i).put((short)(i + 1)).put((short)(i + 2));
          ind.put(i).put((short)(i + 2)).put((short)(i + 3));
        } else {
          ind.put(i).put((short)(i + 1)).put((short)(i + 3));
          ind.put((short)(i + 1)).put((short)(i + 2)).put((short)(i + 3));
        } 
        i = (short)(i + 4);
      } 
    } 
    VertexBuf.VertexArray posa = new VertexBuf.VertexArray(pos);
    VertexBuf.ColorArray cola1 = new VertexBuf.ColorArray(col1);
    VertexBuf.ColorArray cola2 = new VertexBuf.ColorArray(col2);
    this.flats = new FastMesh[] { new FastMesh(new VertexBuf(new VertexBuf.AttribArray[] { posa }, ), ind), new FastMesh(new VertexBuf(new VertexBuf.AttribArray[] { posa, cola1 }, ), ind), new FastMesh(new VertexBuf(new VertexBuf.AttribArray[] { posa, cola2 }, ), ind) };
  }
  
  public void drawflat(GOut g, int mode) {
    g.apply();
    this.flats[mode].draw(g);
  }
  
  public void dispose() {
    for (Disposable p : this.dparts)
      p.dispose(); 
  }
  
  public boolean setup(RenderList rl) {
    for (Layer l : this.layers)
      rl.add(l, null); 
    for (Rendered e : this.extras)
      rl.add(e, null); 
    return true;
  }
  
  public static interface DataID<T> {
    T make(MapMesh param1MapMesh);
  }
}
