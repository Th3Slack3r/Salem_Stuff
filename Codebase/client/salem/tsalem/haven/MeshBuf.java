package haven;

import haven.glsl.Attribute;
import java.awt.Color;
import java.lang.reflect.Constructor;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MeshBuf {
  public final Collection<Vertex> v = new ArrayList<>();
  
  public final Collection<Face> f = new ArrayList<>();
  
  private VertexBuf vbuf = null;
  
  private int nextid = 0;
  
  private Layer<?>[] layers = (Layer<?>[])new Layer[0];
  
  private LayerID<?>[] lids = (LayerID<?>[])new LayerID[0];
  
  public abstract class Layer<T> {
    public final int idx;
    
    public Layer() {
      this.idx = MeshBuf.this.nextid++;
      MeshBuf.this.layers = (Layer<?>[])Utils.<Layer>extend((Layer[])MeshBuf.this.layers, MeshBuf.this.nextid);
      MeshBuf.this.lids = (MeshBuf.LayerID<?>[])Utils.<MeshBuf.LayerID>extend((MeshBuf.LayerID[])MeshBuf.this.lids, MeshBuf.this.nextid);
      MeshBuf.this.layers[this.idx] = this;
      for (MeshBuf.Vertex o : MeshBuf.this.v)
        o.attrs = Utils.extend(o.attrs, MeshBuf.this.nextid); 
    }
    
    public void set(MeshBuf.Vertex v, T data) {
      v.attrs[this.idx] = data;
    }
    
    public T get(MeshBuf.Vertex v) {
      return (T)v.attrs[this.idx];
    }
    
    public abstract VertexBuf.AttribArray build(Collection<T> param1Collection);
    
    public void copy(VertexBuf src, MeshBuf.Vertex[] vmap, int off) {}
  }
  
  public static abstract class LayerID<L> {
    public abstract L cons(MeshBuf param1MeshBuf);
  }
  
  public static class CLayerID<L> extends LayerID<L> {
    public final Class<L> cl;
    
    private final Constructor<L> cons;
    
    public CLayerID(Class<L> cl) {
      this.cl = cl;
      try {
        this.cons = cl.getConstructor(new Class[] { MeshBuf.class });
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } 
    }
    
    public L cons(MeshBuf buf) {
      return Utils.construct(this.cons, new Object[] { buf });
    }
  }
  
  public class Tex extends Layer<Coord3f> {
    public VertexBuf.TexelArray build(Collection<Coord3f> in) {
      FloatBuffer data = Utils.wfbuf(in.size() * 2);
      for (Coord3f c : in) {
        data.put(c.x);
        data.put(c.y);
      } 
      return new VertexBuf.TexelArray(data);
    }
    
    public void copy(VertexBuf buf, MeshBuf.Vertex[] vmap, int off) {
      VertexBuf.TexelArray src = buf.<VertexBuf.TexelArray>buf(VertexBuf.TexelArray.class);
      if (src == null)
        return; 
      for (int i = 0, o = off * 2; i < vmap.length; i++, o += 2) {
        if (vmap[i] != null)
          set(vmap[i], new Coord3f(src.data.get(o), src.data.get(o + 1), 0.0F)); 
      } 
    }
  }
  
  public static final LayerID<Tex> tex = new CLayerID<>(Tex.class);
  
  public class Col extends Layer<Color> {
    public VertexBuf.ColorArray build(Collection<Color> in) {
      FloatBuffer data = Utils.wfbuf(in.size() * 4);
      for (Color c : in) {
        data.put(c.getRed() / 255.0F);
        data.put(c.getGreen() / 255.0F);
        data.put(c.getBlue() / 255.0F);
        data.put(c.getAlpha() / 255.0F);
      } 
      return new VertexBuf.ColorArray(data);
    }
  }
  
  public static final LayerID<Col> col = new CLayerID<>(Col.class);
  
  public abstract class AttribLayer<T> extends Layer<T> {
    public final Attribute attrib;
    
    public AttribLayer(Attribute attrib) {
      this.attrib = attrib;
    }
  }
  
  public class Vec1Layer extends AttribLayer<Float> {
    public Vec1Layer(Attribute attrib) {
      super(attrib);
    }
    
    public VertexBuf.Vec1Array build(Collection<Float> in) {
      FloatBuffer data = Utils.wfbuf(in.size());
      for (Float d : in)
        data.put(d.floatValue()); 
      return new VertexBuf.Vec1Array(data, this.attrib);
    }
  }
  
  public class Vec2Layer extends AttribLayer<Coord3f> {
    public Vec2Layer(Attribute attrib) {
      super(attrib);
    }
    
    public VertexBuf.Vec2Array build(Collection<Coord3f> in) {
      FloatBuffer data = Utils.wfbuf(in.size() * 2);
      for (Coord3f d : in) {
        data.put(d.x);
        data.put(d.y);
      } 
      return new VertexBuf.Vec2Array(data, this.attrib);
    }
  }
  
  public class Vec3Layer extends AttribLayer<Coord3f> {
    public Vec3Layer(Attribute attrib) {
      super(attrib);
    }
    
    public VertexBuf.Vec3Array build(Collection<Coord3f> in) {
      FloatBuffer data = Utils.wfbuf(in.size() * 3);
      for (Coord3f d : in) {
        data.put(d.x);
        data.put(d.y);
        data.put(d.z);
      } 
      return new VertexBuf.Vec3Array(data, this.attrib);
    }
  }
  
  public class Vec4Layer extends AttribLayer<float[]> {
    public Vec4Layer(Attribute attrib) {
      super(attrib);
    }
    
    public VertexBuf.Vec4Array build(Collection<float[]> in) {
      FloatBuffer data = Utils.wfbuf(in.size() * 4);
      for (float[] d : in) {
        data.put(d[0]);
        data.put(d[1]);
        data.put(d[2]);
        data.put(d[3]);
      } 
      return new VertexBuf.Vec4Array(data, this.attrib);
    }
  }
  
  public static abstract class ALayerID<L> extends LayerID<L> {
    public final Attribute attrib;
    
    public ALayerID(Attribute attrib) {
      this.attrib = attrib;
    }
  }
  
  public static class V1LayerID extends ALayerID<Vec1Layer> {
    public V1LayerID(Attribute attrib) {
      super(attrib);
    }
    
    public MeshBuf.Vec1Layer cons(MeshBuf buf) {
      buf.getClass();
      return new MeshBuf.Vec1Layer(this.attrib);
    }
  }
  
  public static class V2LayerID extends ALayerID<Vec2Layer> {
    public V2LayerID(Attribute attrib) {
      super(attrib);
    }
    
    public MeshBuf.Vec2Layer cons(MeshBuf buf) {
      buf.getClass();
      return new MeshBuf.Vec2Layer(this.attrib);
    }
  }
  
  public static class V3LayerID extends ALayerID<Vec3Layer> {
    public V3LayerID(Attribute attrib) {
      super(attrib);
    }
    
    public MeshBuf.Vec3Layer cons(MeshBuf buf) {
      buf.getClass();
      return new MeshBuf.Vec3Layer(this.attrib);
    }
  }
  
  public static class V4LayerID extends ALayerID<Vec4Layer> {
    public V4LayerID(Attribute attrib) {
      super(attrib);
    }
    
    public MeshBuf.Vec4Layer cons(MeshBuf buf) {
      buf.getClass();
      return new MeshBuf.Vec4Layer(this.attrib);
    }
  }
  
  public <L extends Layer> L layer(LayerID<L> id) {
    if (id == null)
      throw new NullPointerException(); 
    for (int i = 0; i < this.lids.length; i++) {
      if (this.lids[i] == id)
        return (L)this.layers[i]; 
    } 
    Layer layer = (Layer)id.cons(this);
    this.lids[layer.idx] = id;
    return (L)layer;
  }
  
  public class Vertex {
    public Coord3f pos;
    
    public Coord3f nrm;
    
    private Object[] attrs = new Object[MeshBuf.this.layers.length];
    
    private short idx;
    
    public Vertex(Coord3f pos, Coord3f nrm) {
      this.pos = pos;
      this.nrm = nrm;
      MeshBuf.this.v.add(this);
    }
    
    public String toString() {
      return String.format("MeshBuf.Vertex(%s, %s)", new Object[] { this.pos, this.nrm });
    }
  }
  
  public class Face {
    public final MeshBuf.Vertex v1;
    
    public final MeshBuf.Vertex v2;
    
    public final MeshBuf.Vertex v3;
    
    public Face(MeshBuf.Vertex v1, MeshBuf.Vertex v2, MeshBuf.Vertex v3) {
      this.v1 = v1;
      this.v2 = v2;
      this.v3 = v3;
      MeshBuf.this.f.add(this);
    }
  }
  
  public Vertex[] copy(FastMesh src, LayerMapper mapper) {
    int min = -1, max = -1;
    for (int i = 0; i < src.num * 3; i++) {
      int idx = src.indb.get(i);
      if (min < 0 || idx < min)
        min = idx; 
      if (idx > max)
        max = idx; 
    } 
    int nv = 0;
    VertexBuf.VertexArray posb = src.vert.<VertexBuf.VertexArray>buf(VertexBuf.VertexArray.class);
    VertexBuf.NormalArray nrmb = src.vert.<VertexBuf.NormalArray>buf(VertexBuf.NormalArray.class);
    Vertex[] vmap = new Vertex[max + 1 - min];
    int j;
    for (j = 0; j < src.num * 3; j++) {
      int idx = src.indb.get(j);
      if (vmap[idx - min] == null) {
        int o = idx * posb.n;
        Coord3f pos = new Coord3f(posb.data.get(o), posb.data.get(o + 1), posb.data.get(o + 2));
        o = idx * nrmb.n;
        Coord3f nrm = new Coord3f(nrmb.data.get(o), nrmb.data.get(o + 1), nrmb.data.get(o + 2));
        vmap[idx - min] = new Vertex(pos, nrm);
        nv++;
      } 
    } 
    for (VertexBuf.AttribArray data : src.vert.bufs) {
      Layer l = mapper.mapbuf(this, data);
      if (l != null)
        l.copy(src.vert, vmap, min); 
    } 
    for (j = 0; j < src.num; j++) {
      int o = j * 3;
      new Face(vmap[src.indb.get(o) - min], vmap[src.indb.get(o + 1) - min], vmap[src.indb.get(o + 2) - min]);
    } 
    Vertex[] vl = new Vertex[nv];
    int n = 0;
    for (int k = 0; k < vmap.length; k++) {
      if (vmap[k] != null)
        vl[n++] = vmap[k]; 
    } 
    return vl;
  }
  
  private static final LayerMapper defmapper = new LayerMapper() {
      public MeshBuf.Layer mapbuf(MeshBuf buf, VertexBuf.AttribArray src) {
        if (src instanceof VertexBuf.TexelArray)
          return buf.layer((MeshBuf.LayerID)MeshBuf.tex); 
        return null;
      }
    };
  
  public Vertex[] copy(FastMesh src) {
    return copy(src, defmapper);
  }
  
  private <T> VertexBuf.AttribArray mklayer(Layer<T> l, Object[] abuf) {
    int i = 0;
    boolean f = false;
    for (Vertex v : this.v) {
      abuf[i++] = v.attrs[l.idx];
      if (v.attrs[l.idx] != null)
        f = true; 
    } 
    if (!f)
      return null; 
    return l.build(Arrays.asList((T[])abuf));
  }
  
  private void mkvbuf() {
    if (this.v.isEmpty())
      throw new RuntimeException("Tried to build empty vertex buffer"); 
    FloatBuffer pos = Utils.wfbuf(this.v.size() * 3);
    FloatBuffer nrm = Utils.wfbuf(this.v.size() * 3);
    int pi = 0, ni = 0;
    short i = 0;
    for (Vertex v : this.v) {
      pos.put(pi + 0, v.pos.x);
      pos.put(pi + 1, v.pos.y);
      pos.put(pi + 2, v.pos.z);
      nrm.put(pi + 0, v.nrm.x);
      nrm.put(pi + 1, v.nrm.y);
      nrm.put(pi + 2, v.nrm.z);
      pi += 3;
      ni += 3;
      i = (short)(i + 1);
      v.idx = i;
      if (i == 0)
        throw new RuntimeException("Too many vertices in meshbuf"); 
    } 
    VertexBuf.AttribArray[] arrays = new VertexBuf.AttribArray[this.layers.length + 2];
    int li = 0;
    arrays[li++] = new VertexBuf.VertexArray(pos);
    arrays[li++] = new VertexBuf.NormalArray(nrm);
    Object[] abuf = new Object[this.v.size()];
    for (int j = 0; j < this.layers.length; j++) {
      VertexBuf.AttribArray l = mklayer(this.layers[j], abuf);
      if (l != null)
        arrays[li++] = l; 
    } 
    this.vbuf = new VertexBuf(Utils.<VertexBuf.AttribArray>splice(arrays, 0, li));
  }
  
  public void clearfaces() {
    this.f.clear();
  }
  
  public FastMesh mkmesh(int i) {
    if (this.f.isEmpty())
      throw new RuntimeException("Tried to build empty mesh"); 
    if (this.vbuf == null)
      mkvbuf(); 
    short[] idx = new short[this.f.size() * 3];
    int ii = 0;
    for (Face f : this.f) {
      idx[ii + 0] = f.v1.idx;
      idx[ii + 1] = f.v2.idx;
      idx[ii + 2] = f.v3.idx;
      ii += 3;
    } 
    if (i == 18)
      return new WireMesh(this.vbuf, idx); 
    return new FastMesh(this.vbuf, idx);
  }
  
  public FastMesh mkmesh() {
    return mkmesh(-1);
  }
  
  public boolean emptyp() {
    return this.f.isEmpty();
  }
  
  public static interface LayerMapper {
    MeshBuf.Layer mapbuf(MeshBuf param1MeshBuf, VertexBuf.AttribArray param1AttribArray);
  }
}
