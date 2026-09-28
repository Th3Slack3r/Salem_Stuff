package haven;

import haven.glsl.Attribute;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class VertexBuf {
  public static final GLState.Slot<Binding> bound = new GLState.Slot<>(GLState.Slot.Type.GEOM, Binding.class, new GLState.Slot[0]);
  
  public final AttribArray[] bufs;
  
  public final int num;
  
  public VertexBuf(AttribArray... bufs) {
    AttribArray[] na = new AttribArray[bufs.length];
    na[0] = bufs[0];
    int num = na[0].size();
    for (int i = 1; i < bufs.length; i++) {
      na[i] = bufs[i];
      if (na[i].size() != num)
        throw new RuntimeException("Buffer sizes do not match"); 
    } 
    this.bufs = na;
    this.num = num;
  }
  
  public <T extends AttribArray> T buf(Class<T> type) {
    for (AttribArray a : this.bufs) {
      if (type.isInstance(a))
        return type.cast(a); 
    } 
    return null;
  }
  
  public abstract class Binding extends GLState {
    public void prep(GLState.Buffer buf) {
      buf.put(VertexBuf.bound, this);
    }
  }
  
  public class MemBinding extends Binding {
    public void apply(GOut g) {
      for (int i = 0; i < VertexBuf.this.bufs.length; i++) {
        if (VertexBuf.this.bufs[i] instanceof VertexBuf.GLArray)
          ((VertexBuf.GLArray)VertexBuf.this.bufs[i]).bind(g, false); 
      } 
    }
    
    public void unapply(GOut g) {
      for (int i = 0; i < VertexBuf.this.bufs.length; i++) {
        if (VertexBuf.this.bufs[i] instanceof VertexBuf.GLArray)
          ((VertexBuf.GLArray)VertexBuf.this.bufs[i]).unbind(g); 
      } 
    }
  }
  
  public static abstract class AttribArray {
    public final int n;
    
    private GLBuffer bufobj;
    
    private int bufmode;
    
    private boolean update;
    
    public AttribArray(int n) {
      this.bufmode = 35044;
      this.update = false;
      this.n = n;
    }
    
    public abstract Buffer data();
    
    public abstract Buffer direct();
    
    public abstract int elsize();
    
    public int size() {
      Buffer b = data();
      b.rewind();
      return b.capacity() / this.n;
    }
    
    public void bindvbo(GOut g) {
      GL2 gl = g.gl;
      synchronized (this) {
        if (this.bufobj != null && this.bufobj.gl != gl)
          dispose(); 
        if (this.bufobj == null) {
          this.bufobj = new GLBuffer(gl);
          gl.glBindBuffer(34962, this.bufobj.id);
          Buffer data = data();
          data.rewind();
          gl.glBufferData(34962, (data.remaining() * elsize()), data, this.bufmode);
          GOut.checkerr((GL)gl);
          this.update = false;
        } else if (this.update) {
          gl.glBindBuffer(34962, this.bufobj.id);
          Buffer data = data();
          data.rewind();
          gl.glBufferData(34962, (data.remaining() * elsize()), data, this.bufmode);
          this.update = false;
        } else {
          gl.glBindBuffer(34962, this.bufobj.id);
        } 
      } 
    }
    
    public void vbomode(int mode) {
      this.bufmode = mode;
      dispose();
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
  
  public static interface GLArray {
    void bind(GOut param1GOut, boolean param1Boolean);
    
    void unbind(GOut param1GOut);
    
    Object progid(GOut param1GOut);
  }
  
  public static abstract class FloatArray extends AttribArray {
    public FloatBuffer data;
    
    public FloatArray(int n, FloatBuffer data) {
      super(n);
      data.rewind();
      if (data.capacity() % n != 0)
        throw new RuntimeException(String.format("float-array length %d does not match element count %d", new Object[] { Integer.valueOf(data.capacity()), Integer.valueOf(n) })); 
      this.data = data;
    }
    
    public FloatBuffer data() {
      return this.data;
    }
    
    public FloatBuffer direct() {
      if (!this.data.isDirect())
        this.data = Utils.bufcp(this.data); 
      return this.data;
    }
    
    public int elsize() {
      return 4;
    }
  }
  
  public static abstract class IntArray extends AttribArray {
    public IntBuffer data;
    
    public IntArray(int n, IntBuffer data) {
      super(n);
      data.rewind();
      if (data.capacity() % n != 0)
        throw new RuntimeException(String.format("int-array length %d does not match element count %d", new Object[] { Integer.valueOf(data.capacity()), Integer.valueOf(n) })); 
      this.data = data;
    }
    
    public IntBuffer data() {
      return this.data;
    }
    
    public IntBuffer direct() {
      if (!this.data.isDirect())
        this.data = Utils.bufcp(this.data); 
      return this.data;
    }
    
    public int elsize() {
      return 4;
    }
  }
  
  public static class VertexArray extends FloatArray implements GLArray {
    public VertexArray(FloatBuffer data) {
      super(3, data);
    }
    
    public VertexArray dup() {
      return new VertexArray(Utils.bufcp(this.data));
    }
    
    public void bind(GOut g, boolean asvbo) {
      GL2 gl = g.gl;
      if (asvbo) {
        bindvbo(g);
        gl.glVertexPointer(3, 5126, 0, 0L);
        gl.glBindBuffer(34962, 0);
      } else {
        this.data.rewind();
        gl.glVertexPointer(3, 5126, 0, direct());
      } 
      gl.glEnableClientState(32884);
    }
    
    public void unbind(GOut g) {
      g.gl.glDisableClientState(32884);
    }
    
    public Object progid(GOut g) {
      return null;
    }
    
    public void bind(GOut g) {
      bind(g, false);
    }
  }
  
  public static class NormalArray extends FloatArray implements GLArray {
    public NormalArray(FloatBuffer data) {
      super(3, data);
    }
    
    public NormalArray dup() {
      return new NormalArray(Utils.bufcp(this.data));
    }
    
    public void bind(GOut g, boolean asvbo) {
      GL2 gl = g.gl;
      if (asvbo) {
        bindvbo(g);
        gl.glNormalPointer(5126, 0, 0L);
        gl.glBindBuffer(34962, 0);
      } else {
        this.data.rewind();
        gl.glNormalPointer(5126, 0, direct());
      } 
      gl.glEnableClientState(32885);
    }
    
    public void unbind(GOut g) {
      g.gl.glDisableClientState(32885);
    }
    
    public Object progid(GOut g) {
      return null;
    }
    
    public void bind(GOut g) {
      bind(g, false);
    }
  }
  
  public static class ColorArray extends FloatArray implements GLArray {
    public ColorArray(FloatBuffer data) {
      super(4, data);
    }
    
    public ColorArray dup() {
      return new ColorArray(Utils.bufcp(this.data));
    }
    
    public void bind(GOut g, boolean asvbo) {
      GL2 gl = g.gl;
      if (asvbo) {
        bindvbo(g);
        gl.glColorPointer(4, 5126, 0, 0L);
        gl.glBindBuffer(34962, 0);
      } else {
        this.data.rewind();
        gl.glColorPointer(4, 5126, 0, direct());
      } 
      gl.glEnableClientState(32886);
    }
    
    public void unbind(GOut g) {
      g.gl.glDisableClientState(32886);
    }
    
    public Object progid(GOut g) {
      return null;
    }
  }
  
  public static class TexelArray extends FloatArray implements GLArray {
    public TexelArray(FloatBuffer data) {
      super(2, data);
    }
    
    public TexelArray dup() {
      return new TexelArray(Utils.bufcp(this.data));
    }
    
    public void bind(GOut g, boolean asvbo) {
      GL2 gl = g.gl;
      if (asvbo) {
        bindvbo(g);
        gl.glTexCoordPointer(2, 5126, 0, 0L);
        gl.glBindBuffer(34962, 0);
      } else {
        this.data.rewind();
        gl.glTexCoordPointer(2, 5126, 0, direct());
      } 
      gl.glEnableClientState(32888);
    }
    
    public void unbind(GOut g) {
      g.gl.glDisableClientState(32888);
    }
    
    public Object progid(GOut g) {
      return null;
    }
  }
  
  public static class NamedFloatArray extends FloatArray implements GLArray {
    public final Attribute attr;
    
    private int bound = -1;
    
    public NamedFloatArray(int n, FloatBuffer data, Attribute attr) {
      super(n, data);
      this.attr = attr;
    }
    
    public void bind(GOut g, boolean asvbo) {
      if (g.st.prog != null && (
        this.bound = g.st.prog.cattrib(this.attr)) != -1) {
        GL2 gl = g.gl;
        if (asvbo) {
          bindvbo(g);
          gl.glVertexAttribPointer(this.bound, this.n, 5126, false, 0, 0L);
          gl.glBindBuffer(34962, 0);
        } else {
          this.data.rewind();
          gl.glVertexAttribPointer(this.bound, this.n, 5126, false, 0, direct());
        } 
        gl.glEnableVertexAttribArray(this.bound);
      } 
    }
    
    public void unbind(GOut g) {
      if (this.bound != -1) {
        g.gl.glDisableVertexAttribArray(this.bound);
        this.bound = -1;
      } 
    }
    
    public Object progid(GOut g) {
      if (g.st.prog == null)
        return null; 
      return Integer.valueOf(g.st.prog.cattrib(this.attr));
    }
  }
  
  public static class Vec1Array extends NamedFloatArray implements GLArray {
    public Vec1Array(FloatBuffer data, Attribute attr) {
      super(1, data, attr);
    }
  }
  
  public static class Vec2Array extends NamedFloatArray implements GLArray {
    public Vec2Array(FloatBuffer data, Attribute attr) {
      super(2, data, attr);
    }
  }
  
  public static class Vec3Array extends NamedFloatArray implements GLArray {
    public Vec3Array(FloatBuffer data, Attribute attr) {
      super(3, data, attr);
    }
  }
  
  public static class Vec4Array extends NamedFloatArray implements GLArray {
    public Vec4Array(FloatBuffer data, Attribute attr) {
      super(4, data, attr);
    }
  }
  
  public void dispose() {
    for (AttribArray buf : this.bufs)
      buf.dispose(); 
  }
  
  @LayerName("vbuf")
  public static class VertexRes extends Resource.Layer {
    public final transient VertexBuf b;
    
    public VertexRes(Resource res, byte[] buf) {
      super(res);
      ArrayList<VertexBuf.AttribArray> bufs = new ArrayList<>();
      int fl = Utils.ub(buf[0]);
      int num = Utils.uint16d(buf, 1);
      int off = 3;
      while (off < buf.length) {
        int id = Utils.ub(buf[off++]);
        if (id == 0) {
          FloatBuffer data = Utils.wfbuf(num * 3);
          for (int i = 0; i < num * 3; i++)
            data.put((float)Utils.floatd(buf, off + i * 5)); 
          off += num * 5 * 3;
          bufs.add(new VertexBuf.VertexArray(data));
          continue;
        } 
        if (id == 1) {
          FloatBuffer data = Utils.wfbuf(num * 3);
          for (int i = 0; i < num * 3; i++)
            data.put((float)Utils.floatd(buf, off + i * 5)); 
          off += num * 5 * 3;
          bufs.add(new VertexBuf.NormalArray(data));
          continue;
        } 
        if (id == 2) {
          FloatBuffer data = Utils.wfbuf(num * 2);
          for (int i = 0; i < num * 2; i++)
            data.put((float)Utils.floatd(buf, off + i * 5)); 
          off += num * 5 * 2;
          bufs.add(new VertexBuf.TexelArray(data));
          continue;
        } 
        if (id == 3) {
          int mba = Utils.ub(buf[off++]);
          IntBuffer ba = Utils.wibuf(num * mba);
          for (int i = 0; i < num * mba; i++)
            ba.put(-1); 
          ba.rewind();
          FloatBuffer bw = Utils.wfbuf(num * mba);
          int[] na = new int[num];
          List<String> bones = new ArrayList<>();
          label63: while (true) {
            int[] ob = { off };
            String bone = Utils.strd(buf, ob);
            off = ob[0];
            if (bone.length() == 0)
              break; 
            int bidx = bones.size();
            bones.add(bone);
            while (true) {
              int run = Utils.uint16d(buf, off);
              off += 2;
              int st = Utils.uint16d(buf, off);
              off += 2;
              if (run == 0)
                continue label63; 
              for (int j = 0; j < run; j++) {
                float w = (float)Utils.floatd(buf, off);
                off += 5;
                int v = j + st;
                na[v] = na[v] + 1;
                int cna = na[v];
                if (cna < mba) {
                  bw.put(v * mba + cna, w);
                  ba.put(v * mba + cna, bidx);
                } 
              } 
            } 
          } 
          normweights(bw, ba, mba);
          bufs.add(new PoseMorph.BoneArray(mba, ba, bones.<String>toArray(new String[0])));
          bufs.add(new PoseMorph.WeightArray(mba, bw));
        } 
      } 
      this.b = new VertexBuf(bufs.<VertexBuf.AttribArray>toArray(new VertexBuf.AttribArray[0]));
    }
    
    private static void normweights(FloatBuffer bw, IntBuffer ba, int mba) {
      int i = 0;
      while (i < bw.capacity()) {
        float tw = 0.0F;
        int n = 0;
        int o;
        for (o = 0; o < mba && 
          ba.get(i + o) >= 0; o++) {
          tw += bw.get(i + o);
          n++;
        } 
        if (tw != 1.0F)
          for (o = 0; o < n; o++)
            bw.put(i + o, bw.get(i + o) / tw);  
        i += mba;
      } 
    }
    
    public void init() {}
  }
}
