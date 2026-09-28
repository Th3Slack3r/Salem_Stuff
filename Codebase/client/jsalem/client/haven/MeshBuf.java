package haven;

import haven.glsl.Attribute;
import java.awt.Color;
import java.lang.reflect.Constructor;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MeshBuf {
   public final Collection<MeshBuf.Vertex> v = new ArrayList<>();
   public final Collection<MeshBuf.Face> f = new ArrayList<>();
   private VertexBuf vbuf = null;
   private int nextid = 0;
   private MeshBuf.Layer<?>[] layers = new MeshBuf.Layer[0];
   private MeshBuf.LayerID<?>[] lids = new MeshBuf.LayerID[0];
   public static final MeshBuf.LayerID<MeshBuf.Tex> tex = new MeshBuf.CLayerID<>(MeshBuf.Tex.class);
   public static final MeshBuf.LayerID<MeshBuf.Col> col = new MeshBuf.CLayerID<>(MeshBuf.Col.class);
   private static final MeshBuf.LayerMapper defmapper = new MeshBuf.LayerMapper() {
      @Override
      public MeshBuf.Layer mapbuf(MeshBuf buf, VertexBuf.AttribArray src) {
         return src instanceof VertexBuf.TexelArray ? buf.layer(MeshBuf.tex) : null;
      }
   };

   public <L extends MeshBuf.Layer> L layer(MeshBuf.LayerID<L> id) {
      if (id == null) {
         throw new NullPointerException();
      } else {
         for (int i = 0; i < this.lids.length; i++) {
            if (this.lids[i] == id) {
               return (L)this.layers[i];
            }
         }

         L ret = (L)id.cons(this);
         this.lids[ret.idx] = id;
         return ret;
      }
   }

   public MeshBuf.Vertex[] copy(FastMesh src, MeshBuf.LayerMapper mapper) {
      int min = -1;
      int max = -1;

      for (int i = 0; i < src.num * 3; i++) {
         int idx = src.indb.get(i);
         if (min < 0 || idx < min) {
            min = idx;
         }

         if (idx > max) {
            max = idx;
         }
      }

      int nv = 0;
      VertexBuf.VertexArray posb = src.vert.buf(VertexBuf.VertexArray.class);
      VertexBuf.NormalArray nrmb = src.vert.buf(VertexBuf.NormalArray.class);
      MeshBuf.Vertex[] vmap = new MeshBuf.Vertex[max + 1 - min];

      for (int i = 0; i < src.num * 3; i++) {
         int idxx = src.indb.get(i);
         if (vmap[idxx - min] == null) {
            int o = idxx * posb.n;
            Coord3f pos = new Coord3f(posb.data.get(o), posb.data.get(o + 1), posb.data.get(o + 2));
            o = idxx * nrmb.n;
            Coord3f nrm = new Coord3f(nrmb.data.get(o), nrmb.data.get(o + 1), nrmb.data.get(o + 2));
            vmap[idxx - min] = new MeshBuf.Vertex(pos, nrm);
            nv++;
         }
      }

      for (VertexBuf.AttribArray data : src.vert.bufs) {
         MeshBuf.Layer l = mapper.mapbuf(this, data);
         if (l != null) {
            l.copy(src.vert, vmap, min);
         }
      }

      for (int ix = 0; ix < src.num; ix++) {
         int o = ix * 3;
         new MeshBuf.Face(vmap[src.indb.get(o) - min], vmap[src.indb.get(o + 1) - min], vmap[src.indb.get(o + 2) - min]);
      }

      MeshBuf.Vertex[] vl = new MeshBuf.Vertex[nv];
      int n = 0;

      for (int ix = 0; ix < vmap.length; ix++) {
         if (vmap[ix] != null) {
            vl[n++] = vmap[ix];
         }
      }

      return vl;
   }

   public MeshBuf.Vertex[] copy(FastMesh src) {
      return this.copy(src, defmapper);
   }

   private <T> VertexBuf.AttribArray mklayer(MeshBuf.Layer<T> l, Object[] abuf) {
      int i = 0;
      boolean f = false;

      for (MeshBuf.Vertex v : this.v) {
         if ((abuf[i++] = v.attrs[l.idx]) != null) {
            f = true;
         }
      }

      return !f ? null : l.build(Arrays.asList((T[])abuf));
   }

   private void mkvbuf() {
      if (this.v.isEmpty()) {
         throw new RuntimeException("Tried to build empty vertex buffer");
      } else {
         FloatBuffer pos = Utils.wfbuf(this.v.size() * 3);
         FloatBuffer nrm = Utils.wfbuf(this.v.size() * 3);
         int pi = 0;
         int ni = 0;
         short i = 0;

         for (MeshBuf.Vertex v : this.v) {
            pos.put(pi + 0, v.pos.x);
            pos.put(pi + 1, v.pos.y);
            pos.put(pi + 2, v.pos.z);
            nrm.put(pi + 0, v.nrm.x);
            nrm.put(pi + 1, v.nrm.y);
            nrm.put(pi + 2, v.nrm.z);
            pi += 3;
            ni += 3;
            v.idx = i++;
            if (i == 0) {
               throw new RuntimeException("Too many vertices in meshbuf");
            }
         }

         VertexBuf.AttribArray[] arrays = new VertexBuf.AttribArray[this.layers.length + 2];
         ni = 0;
         arrays[ni++] = new VertexBuf.VertexArray(pos);
         arrays[ni++] = new VertexBuf.NormalArray(nrm);
         Object[] abuf = new Object[this.v.size()];

         for (int ix = 0; ix < this.layers.length; ix++) {
            VertexBuf.AttribArray l = this.mklayer(this.layers[ix], abuf);
            if (l != null) {
               arrays[ni++] = l;
            }
         }

         this.vbuf = new VertexBuf(Utils.splice(arrays, 0, ni));
      }
   }

   public void clearfaces() {
      this.f.clear();
   }

   public FastMesh mkmesh(int i) {
      if (this.f.isEmpty()) {
         throw new RuntimeException("Tried to build empty mesh");
      } else {
         if (this.vbuf == null) {
            this.mkvbuf();
         }

         short[] idx = new short[this.f.size() * 3];
         int ii = 0;

         for (MeshBuf.Face f : this.f) {
            idx[ii + 0] = f.v1.idx;
            idx[ii + 1] = f.v2.idx;
            idx[ii + 2] = f.v3.idx;
            ii += 3;
         }

         return (FastMesh)(i == 18 ? new WireMesh(this.vbuf, idx) : new FastMesh(this.vbuf, idx));
      }
   }

   public FastMesh mkmesh() {
      return this.mkmesh(-1);
   }

   public boolean emptyp() {
      return this.f.isEmpty();
   }

   public abstract static class ALayerID<L> extends MeshBuf.LayerID<L> {
      public final Attribute attrib;

      public ALayerID(Attribute attrib) {
         this.attrib = attrib;
      }
   }

   public abstract class AttribLayer<T> extends MeshBuf.Layer<T> {
      public final Attribute attrib;

      public AttribLayer(Attribute attrib) {
         this.attrib = attrib;
      }
   }

   public static class CLayerID<L> extends MeshBuf.LayerID<L> {
      public final Class<L> cl;
      private final Constructor<L> cons;

      public CLayerID(Class<L> cl) {
         this.cl = cl;

         try {
            this.cons = cl.getConstructor(MeshBuf.class);
         } catch (NoSuchMethodException var3) {
            throw new RuntimeException(var3);
         }
      }

      @Override
      public L cons(MeshBuf buf) {
         return Utils.construct(this.cons, buf);
      }
   }

   public class Col extends MeshBuf.Layer<Color> {
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

   public abstract class Layer<T> {
      public final int idx = MeshBuf.this.nextid++;

      public Layer() {
         MeshBuf.this.layers = Utils.extend(MeshBuf.this.layers, MeshBuf.this.nextid);
         MeshBuf.this.lids = Utils.extend(MeshBuf.this.lids, MeshBuf.this.nextid);
         MeshBuf.this.layers[this.idx] = this;

         for (MeshBuf.Vertex o : MeshBuf.this.v) {
            o.attrs = Utils.extend(o.attrs, MeshBuf.this.nextid);
         }
      }

      public void set(MeshBuf.Vertex v, T data) {
         v.attrs[this.idx] = data;
      }

      public T get(MeshBuf.Vertex v) {
         return (T)v.attrs[this.idx];
      }

      public abstract VertexBuf.AttribArray build(Collection<T> var1);

      public void copy(VertexBuf src, MeshBuf.Vertex[] vmap, int off) {
      }
   }

   public abstract static class LayerID<L> {
      public abstract L cons(MeshBuf var1);
   }

   public interface LayerMapper {
      MeshBuf.Layer mapbuf(MeshBuf var1, VertexBuf.AttribArray var2);
   }

   public class Tex extends MeshBuf.Layer<Coord3f> {
      public VertexBuf.TexelArray build(Collection<Coord3f> in) {
         FloatBuffer data = Utils.wfbuf(in.size() * 2);

         for (Coord3f c : in) {
            data.put(c.x);
            data.put(c.y);
         }

         return new VertexBuf.TexelArray(data);
      }

      @Override
      public void copy(VertexBuf buf, MeshBuf.Vertex[] vmap, int off) {
         VertexBuf.TexelArray src = buf.buf(VertexBuf.TexelArray.class);
         if (src != null) {
            int i = 0;

            for (int o = off * 2; i < vmap.length; o += 2) {
               if (vmap[i] != null) {
                  this.set(vmap[i], new Coord3f(src.data.get(o), src.data.get(o + 1), 0.0F));
               }

               i++;
            }
         }
      }
   }

   public static class V1LayerID extends MeshBuf.ALayerID<MeshBuf.Vec1Layer> {
      public V1LayerID(Attribute attrib) {
         super(attrib);
      }

      public MeshBuf.Vec1Layer cons(MeshBuf buf) {
         return buf.new Vec1Layer(this.attrib);
      }
   }

   public static class V2LayerID extends MeshBuf.ALayerID<MeshBuf.Vec2Layer> {
      public V2LayerID(Attribute attrib) {
         super(attrib);
      }

      public MeshBuf.Vec2Layer cons(MeshBuf buf) {
         return buf.new Vec2Layer(this.attrib);
      }
   }

   public static class V3LayerID extends MeshBuf.ALayerID<MeshBuf.Vec3Layer> {
      public V3LayerID(Attribute attrib) {
         super(attrib);
      }

      public MeshBuf.Vec3Layer cons(MeshBuf buf) {
         return buf.new Vec3Layer(this.attrib);
      }
   }

   public static class V4LayerID extends MeshBuf.ALayerID<MeshBuf.Vec4Layer> {
      public V4LayerID(Attribute attrib) {
         super(attrib);
      }

      public MeshBuf.Vec4Layer cons(MeshBuf buf) {
         return buf.new Vec4Layer(this.attrib);
      }
   }

   public class Vec1Layer extends MeshBuf.AttribLayer<Float> {
      public Vec1Layer(Attribute attrib) {
         super(attrib);
      }

      public VertexBuf.Vec1Array build(Collection<Float> in) {
         FloatBuffer data = Utils.wfbuf(in.size());

         for (Float d : in) {
            data.put(d);
         }

         return new VertexBuf.Vec1Array(data, this.attrib);
      }
   }

   public class Vec2Layer extends MeshBuf.AttribLayer<Coord3f> {
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

   public class Vec3Layer extends MeshBuf.AttribLayer<Coord3f> {
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

   public class Vec4Layer extends MeshBuf.AttribLayer<float[]> {
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

      @Override
      public String toString() {
         return String.format("MeshBuf.Vertex(%s, %s)", this.pos, this.nrm);
      }
   }
}
