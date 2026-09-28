package haven;

import haven.glsl.Attribute;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.media.opengl.GL2;

public class VertexBuf {
   public static final GLState.Slot<VertexBuf.Binding> bound = new GLState.Slot<>(GLState.Slot.Type.GEOM, VertexBuf.Binding.class);
   public final VertexBuf.AttribArray[] bufs;
   public final int num;

   public VertexBuf(VertexBuf.AttribArray... bufs) {
      VertexBuf.AttribArray[] na = new VertexBuf.AttribArray[bufs.length];
      na[0] = bufs[0];
      int num = na[0].size();

      for (int i = 1; i < bufs.length; i++) {
         na[i] = bufs[i];
         if (na[i].size() != num) {
            throw new RuntimeException("Buffer sizes do not match");
         }
      }

      this.bufs = na;
      this.num = num;
   }

   public <T extends VertexBuf.AttribArray> T buf(Class<T> type) {
      for (VertexBuf.AttribArray a : this.bufs) {
         if (type.isInstance(a)) {
            return type.cast(a);
         }
      }

      return null;
   }

   public void dispose() {
      for (VertexBuf.AttribArray buf : this.bufs) {
         buf.dispose();
      }
   }

   public abstract static class AttribArray {
      public final int n;
      private GLBuffer bufobj;
      private int bufmode = 35044;
      private boolean update = false;

      public AttribArray(int n) {
         this.n = n;
      }

      public abstract Buffer data();

      public abstract Buffer direct();

      public abstract int elsize();

      public int size() {
         Buffer b = this.data();
         b.rewind();
         return b.capacity() / this.n;
      }

      public void bindvbo(GOut g) {
         GL2 gl = g.gl;
         synchronized (this) {
            if (this.bufobj != null && this.bufobj.gl != gl) {
               this.dispose();
            }

            if (this.bufobj == null) {
               this.bufobj = new GLBuffer(gl);
               gl.glBindBuffer(34962, this.bufobj.id);
               Buffer data = this.data();
               data.rewind();
               gl.glBufferData(34962, data.remaining() * this.elsize(), data, this.bufmode);
               GOut.checkerr(gl);
               this.update = false;
            } else if (this.update) {
               gl.glBindBuffer(34962, this.bufobj.id);
               Buffer data = this.data();
               data.rewind();
               gl.glBufferData(34962, data.remaining() * this.elsize(), data, this.bufmode);
               this.update = false;
            } else {
               gl.glBindBuffer(34962, this.bufobj.id);
            }
         }
      }

      public void vbomode(int mode) {
         this.bufmode = mode;
         this.dispose();
      }

      public void dispose() {
         synchronized (this) {
            if (this.bufobj != null) {
               this.bufobj.dispose();
               this.bufobj = null;
            }
         }
      }

      public void update() {
         this.update = true;
      }
   }

   public abstract class Binding extends GLState {
      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(VertexBuf.bound, this);
      }
   }

   public static class ColorArray extends VertexBuf.FloatArray implements VertexBuf.GLArray {
      public ColorArray(FloatBuffer data) {
         super(4, data);
      }

      public VertexBuf.ColorArray dup() {
         return new VertexBuf.ColorArray(Utils.bufcp(this.data));
      }

      @Override
      public void bind(GOut g, boolean asvbo) {
         GL2 gl = g.gl;
         if (asvbo) {
            this.bindvbo(g);
            gl.glColorPointer(4, 5126, 0, 0L);
            gl.glBindBuffer(34962, 0);
         } else {
            ((Buffer)this.data).rewind();
            gl.glColorPointer(4, 5126, 0, this.direct());
         }

         gl.glEnableClientState(32886);
      }

      @Override
      public void unbind(GOut g) {
         g.gl.glDisableClientState(32886);
      }

      @Override
      public Object progid(GOut g) {
         return null;
      }
   }

   public abstract static class FloatArray extends VertexBuf.AttribArray {
      public FloatBuffer data;

      public FloatArray(int n, FloatBuffer data) {
         super(n);
         ((Buffer)data).rewind();
         if (data.capacity() % n != 0) {
            throw new RuntimeException(String.format("float-array length %d does not match element count %d", data.capacity(), n));
         } else {
            this.data = data;
         }
      }

      public FloatBuffer data() {
         return this.data;
      }

      public FloatBuffer direct() {
         if (!this.data.isDirect()) {
            this.data = Utils.bufcp(this.data);
         }

         return this.data;
      }

      @Override
      public int elsize() {
         return 4;
      }
   }

   public interface GLArray {
      void bind(GOut var1, boolean var2);

      void unbind(GOut var1);

      Object progid(GOut var1);
   }

   public abstract static class IntArray extends VertexBuf.AttribArray {
      public IntBuffer data;

      public IntArray(int n, IntBuffer data) {
         super(n);
         ((Buffer)data).rewind();
         if (data.capacity() % n != 0) {
            throw new RuntimeException(String.format("int-array length %d does not match element count %d", data.capacity(), n));
         } else {
            this.data = data;
         }
      }

      public IntBuffer data() {
         return this.data;
      }

      public IntBuffer direct() {
         if (!this.data.isDirect()) {
            this.data = Utils.bufcp(this.data);
         }

         return this.data;
      }

      @Override
      public int elsize() {
         return 4;
      }
   }

   public class MemBinding extends VertexBuf.Binding {
      @Override
      public void apply(GOut g) {
         for (int i = 0; i < VertexBuf.this.bufs.length; i++) {
            if (VertexBuf.this.bufs[i] instanceof VertexBuf.GLArray) {
               ((VertexBuf.GLArray)VertexBuf.this.bufs[i]).bind(g, false);
            }
         }
      }

      @Override
      public void unapply(GOut g) {
         for (int i = 0; i < VertexBuf.this.bufs.length; i++) {
            if (VertexBuf.this.bufs[i] instanceof VertexBuf.GLArray) {
               ((VertexBuf.GLArray)VertexBuf.this.bufs[i]).unbind(g);
            }
         }
      }
   }

   public static class NamedFloatArray extends VertexBuf.FloatArray implements VertexBuf.GLArray {
      public final Attribute attr;
      private int bound = -1;

      public NamedFloatArray(int n, FloatBuffer data, Attribute attr) {
         super(n, data);
         this.attr = attr;
      }

      @Override
      public void bind(GOut g, boolean asvbo) {
         if (g.st.prog != null && (this.bound = g.st.prog.cattrib(this.attr)) != -1) {
            GL2 gl = g.gl;
            if (asvbo) {
               this.bindvbo(g);
               gl.glVertexAttribPointer(this.bound, this.n, 5126, false, 0, 0L);
               gl.glBindBuffer(34962, 0);
            } else {
               ((Buffer)this.data).rewind();
               gl.glVertexAttribPointer(this.bound, this.n, 5126, false, 0, this.direct());
            }

            gl.glEnableVertexAttribArray(this.bound);
         }
      }

      @Override
      public void unbind(GOut g) {
         if (this.bound != -1) {
            g.gl.glDisableVertexAttribArray(this.bound);
            this.bound = -1;
         }
      }

      @Override
      public Object progid(GOut g) {
         return g.st.prog == null ? null : g.st.prog.cattrib(this.attr);
      }
   }

   public static class NormalArray extends VertexBuf.FloatArray implements VertexBuf.GLArray {
      public NormalArray(FloatBuffer data) {
         super(3, data);
      }

      public VertexBuf.NormalArray dup() {
         return new VertexBuf.NormalArray(Utils.bufcp(this.data));
      }

      @Override
      public void bind(GOut g, boolean asvbo) {
         GL2 gl = g.gl;
         if (asvbo) {
            this.bindvbo(g);
            gl.glNormalPointer(5126, 0, 0L);
            gl.glBindBuffer(34962, 0);
         } else {
            ((Buffer)this.data).rewind();
            gl.glNormalPointer(5126, 0, this.direct());
         }

         gl.glEnableClientState(32885);
      }

      @Override
      public void unbind(GOut g) {
         g.gl.glDisableClientState(32885);
      }

      @Override
      public Object progid(GOut g) {
         return null;
      }

      public void bind(GOut g) {
         this.bind(g, false);
      }
   }

   public static class TexelArray extends VertexBuf.FloatArray implements VertexBuf.GLArray {
      public TexelArray(FloatBuffer data) {
         super(2, data);
      }

      public VertexBuf.TexelArray dup() {
         return new VertexBuf.TexelArray(Utils.bufcp(this.data));
      }

      @Override
      public void bind(GOut g, boolean asvbo) {
         GL2 gl = g.gl;
         if (asvbo) {
            this.bindvbo(g);
            gl.glTexCoordPointer(2, 5126, 0, 0L);
            gl.glBindBuffer(34962, 0);
         } else {
            ((Buffer)this.data).rewind();
            gl.glTexCoordPointer(2, 5126, 0, this.direct());
         }

         gl.glEnableClientState(32888);
      }

      @Override
      public void unbind(GOut g) {
         g.gl.glDisableClientState(32888);
      }

      @Override
      public Object progid(GOut g) {
         return null;
      }
   }

   public static class Vec1Array extends VertexBuf.NamedFloatArray implements VertexBuf.GLArray {
      public Vec1Array(FloatBuffer data, Attribute attr) {
         super(1, data, attr);
      }
   }

   public static class Vec2Array extends VertexBuf.NamedFloatArray implements VertexBuf.GLArray {
      public Vec2Array(FloatBuffer data, Attribute attr) {
         super(2, data, attr);
      }
   }

   public static class Vec3Array extends VertexBuf.NamedFloatArray implements VertexBuf.GLArray {
      public Vec3Array(FloatBuffer data, Attribute attr) {
         super(3, data, attr);
      }
   }

   public static class Vec4Array extends VertexBuf.NamedFloatArray implements VertexBuf.GLArray {
      public Vec4Array(FloatBuffer data, Attribute attr) {
         super(4, data, attr);
      }
   }

   public static class VertexArray extends VertexBuf.FloatArray implements VertexBuf.GLArray {
      public VertexArray(FloatBuffer data) {
         super(3, data);
      }

      public VertexBuf.VertexArray dup() {
         return new VertexBuf.VertexArray(Utils.bufcp(this.data));
      }

      @Override
      public void bind(GOut g, boolean asvbo) {
         GL2 gl = g.gl;
         if (asvbo) {
            this.bindvbo(g);
            gl.glVertexPointer(3, 5126, 0, 0L);
            gl.glBindBuffer(34962, 0);
         } else {
            ((Buffer)this.data).rewind();
            gl.glVertexPointer(3, 5126, 0, this.direct());
         }

         gl.glEnableClientState(32884);
      }

      @Override
      public void unbind(GOut g) {
         g.gl.glDisableClientState(32884);
      }

      @Override
      public Object progid(GOut g) {
         return null;
      }

      public void bind(GOut g) {
         this.bind(g, false);
      }
   }

   @Resource.LayerName("vbuf")
   public static class VertexRes extends Resource.Layer {
      public final transient VertexBuf b;

      public VertexRes(Resource res, byte[] buf) {
         Objects.requireNonNull(res);
         super();
         ArrayList<VertexBuf.AttribArray> bufs = new ArrayList<>();
         int fl = Utils.ub(buf[0]);
         int num = Utils.uint16d(buf, 1);
         int off = 3;

         while (off < buf.length) {
            int id = Utils.ub(buf[off++]);
            if (id == 0) {
               FloatBuffer data = Utils.wfbuf(num * 3);

               for (int i = 0; i < num * 3; i++) {
                  data.put((float)Utils.floatd(buf, off + i * 5));
               }

               off += num * 5 * 3;
               bufs.add(new VertexBuf.VertexArray(data));
            } else if (id == 1) {
               FloatBuffer data = Utils.wfbuf(num * 3);

               for (int i = 0; i < num * 3; i++) {
                  data.put((float)Utils.floatd(buf, off + i * 5));
               }

               off += num * 5 * 3;
               bufs.add(new VertexBuf.NormalArray(data));
            } else if (id == 2) {
               FloatBuffer data = Utils.wfbuf(num * 2);

               for (int i = 0; i < num * 2; i++) {
                  data.put((float)Utils.floatd(buf, off + i * 5));
               }

               off += num * 5 * 2;
               bufs.add(new VertexBuf.TexelArray(data));
            } else if (id == 3) {
               int mba = Utils.ub(buf[off++]);
               IntBuffer ba = Utils.wibuf(num * mba);

               for (int i = 0; i < num * mba; i++) {
                  ba.put(-1);
               }

               ((Buffer)ba).rewind();
               FloatBuffer bw = Utils.wfbuf(num * mba);
               int[] na = new int[num];
               List<String> bones = new ArrayList<>();

               while (true) {
                  int[] ob = new int[]{off};
                  String bone = Utils.strd(buf, ob);
                  off = ob[0];
                  if (bone.length() == 0) {
                     normweights(bw, ba, mba);
                     bufs.add(new PoseMorph.BoneArray(mba, ba, bones.toArray(new String[0])));
                     bufs.add(new PoseMorph.WeightArray(mba, bw));
                     break;
                  }

                  int bidx = bones.size();
                  bones.add(bone);

                  while (true) {
                     int run = Utils.uint16d(buf, off);
                     off += 2;
                     int st = Utils.uint16d(buf, off);
                     off += 2;
                     if (run == 0) {
                        break;
                     }

                     for (int i = 0; i < run; i++) {
                        float w = (float)Utils.floatd(buf, off);
                        off += 5;
                        int v = i + st;
                        int cna = (int)(na[v]++);
                        if (!(cna >= mba)) {
                           bw.put(v * mba + cna, w);
                           ba.put(v * mba + cna, bidx);
                        }
                     }
                  }
               }
            }
         }

         this.b = new VertexBuf(bufs.toArray(new VertexBuf.AttribArray[0]));
      }

      private static void normweights(FloatBuffer bw, IntBuffer ba, int mba) {
         int i = 0;

         while (i < bw.capacity()) {
            float tw = 0.0F;
            int n = 0;

            for (int o = 0; o < mba && ba.get(i + o) >= 0; o++) {
               tw += bw.get(i + o);
               n++;
            }

            if (tw != 1.0F) {
               for (int o = 0; o < n; o++) {
                  bw.put(i + o, bw.get(i + o) / tw);
               }
            }

            i += mba;
         }
      }

      @Override
      public void init() {
      }
   }
}
