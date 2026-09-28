package haven;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

public class MeshAnim {
  public final Frame[] frames;
  
  public final float len;
  
  public MeshAnim(Frame[] frames, float len) {
    this.frames = frames;
    this.len = len;
  }
  
  public static class Frame {
    public final float time;
    
    public final int[] idx;
    
    public final float[] pos;
    
    public final float[] nrm;
    
    public Frame(float time, int[] idx, float[] pos, float[] nrm) {
      this.time = time;
      this.idx = idx;
      this.pos = pos;
      this.nrm = nrm;
    }
  }
  
  public boolean animp(FastMesh mesh) {
    int min = -1, max = -1;
    for (int i = 0; i < mesh.num * 3; i++) {
      int vi = mesh.indb.get(i);
      if (min < 0) {
        min = max = vi;
      } else if (vi < min) {
        min = vi;
      } else if (vi > max) {
        max = vi;
      } 
    } 
    boolean[] used = new boolean[max + 1 - min];
    for (int j = 0; j < mesh.num * 3; j++) {
      int vi = mesh.indb.get(j);
      used[vi - min] = true;
    } 
    for (Frame f : this.frames) {
      for (int k = 0; k < f.idx.length; k++) {
        int vi = f.idx[k];
        if (vi >= min && vi <= max)
          if (used[f.idx[k] - min])
            return true;  
      } 
    } 
    return false;
  }
  
  public class Anim implements MorphedMesh.Morpher.Factory {
    public float time = 0.0F;
    
    private MeshAnim.Frame cf;
    
    private MeshAnim.Frame nf;
    
    private float a;
    
    private int seq = 0;
    
    public Anim() {
      aupdate(0.0F);
    }
    
    public void aupdate(float time) {
      float ct, nt;
      int c;
      if (time > MeshAnim.this.len)
        time = MeshAnim.this.len; 
      int l = 0, r = MeshAnim.this.frames.length;
      while (true) {
        c = l + (r - l >> 1);
        ct = (MeshAnim.this.frames[c]).time;
        nt = (c < MeshAnim.this.frames.length - 1) ? (MeshAnim.this.frames[c + 1]).time : MeshAnim.this.len;
        if (ct > time) {
          r = c;
          continue;
        } 
        if (nt < time) {
          l = c + 1;
          continue;
        } 
        break;
      } 
      this.cf = MeshAnim.this.frames[c];
      this.nf = MeshAnim.this.frames[(c + 1) % MeshAnim.this.frames.length];
      if (nt == ct) {
        this.a = 0.0F;
      } else {
        this.a = (time - ct) / (nt - ct);
      } 
      this.seq++;
    }
    
    public void tick(float dt) {
      this.time += dt;
      while (this.time > MeshAnim.this.len)
        this.time -= MeshAnim.this.len; 
      aupdate(this.time);
    }
    
    public MorphedMesh.Morpher create(MorphedMesh.MorphedBuf vb) {
      return new MorphedMesh.Morpher() {
          int lseq = -1;
          
          public boolean update() {
            if (this.lseq == MeshAnim.Anim.this.seq)
              return false; 
            this.lseq = MeshAnim.Anim.this.seq;
            return true;
          }
          
          public void morphp(FloatBuffer dst, FloatBuffer src) {
            if (dst != src) {
              int l = dst.capacity();
              for (int j = 0; j < l; j++)
                dst.put(j, src.get(j)); 
            } 
            MeshAnim.Frame f = MeshAnim.Anim.this.cf;
            float a = 1.0F - MeshAnim.Anim.this.a;
            int i, po;
            for (i = 0, po = 0; i < f.idx.length; i++, po += 3) {
              int vo = f.idx[i] * 3;
              float x = dst.get(vo), y = dst.get(vo + 1), z = dst.get(vo + 2);
              x += f.pos[po] * a;
              y += f.pos[po + 1] * a;
              z += f.pos[po + 2] * a;
              dst.put(vo, x).put(vo + 1, y).put(vo + 2, z);
            } 
            f = MeshAnim.Anim.this.nf;
            a = MeshAnim.Anim.this.a;
            for (i = 0, po = 0; i < f.idx.length; i++, po += 3) {
              int vo = f.idx[i] * 3;
              float x = dst.get(vo), y = dst.get(vo + 1), z = dst.get(vo + 2);
              x += f.pos[po] * a;
              y += f.pos[po + 1] * a;
              z += f.pos[po + 2] * a;
              dst.put(vo, x).put(vo + 1, y).put(vo + 2, z);
            } 
          }
          
          public void morphd(FloatBuffer dst, FloatBuffer src) {
            if (dst != src) {
              int l = dst.capacity();
              for (int j = 0; j < l; j++)
                dst.put(j, src.get(j)); 
            } 
            MeshAnim.Frame f = MeshAnim.Anim.this.cf;
            float a = 1.0F - MeshAnim.Anim.this.a;
            int i, po;
            for (i = 0, po = 0; i < f.idx.length; i++, po += 3) {
              int vo = f.idx[i] * 3;
              float x = dst.get(vo), y = dst.get(vo + 1), z = dst.get(vo + 2);
              x += f.nrm[po] * a;
              y += f.nrm[po + 1] * a;
              z += f.nrm[po + 2] * a;
              dst.put(vo, x).put(vo + 1, y).put(vo + 2, z);
            } 
            f = MeshAnim.Anim.this.nf;
            a = MeshAnim.Anim.this.a;
            for (i = 0, po = 0; i < f.idx.length; i++, po += 3) {
              int vo = f.idx[i] * 3;
              float x = dst.get(vo), y = dst.get(vo + 1), z = dst.get(vo + 2);
              x += f.nrm[po] * a;
              y += f.nrm[po + 1] * a;
              z += f.nrm[po + 2] * a;
              dst.put(vo, x).put(vo + 1, y).put(vo + 2, z);
            } 
          }
        };
    }
  }
  
  @LayerName("manim")
  public static class Res extends Resource.Layer {
    public final int id;
    
    public final MeshAnim a;
    
    public Res(Resource res, byte[] data) {
      super(res);
      Message buf = new Message(0, data);
      this.id = buf.int16();
      float len = buf.float32();
      List<MeshAnim.Frame> frames = new LinkedList<>();
      while (true) {
        int t = buf.uint8();
        if (t == 0)
          break; 
        float tm = buf.float32();
        int n = buf.uint16();
        int[] idx = new int[n];
        float[] pos = new float[n * 3];
        float[] nrm = new float[n * 3];
        int i = 0;
        while (i < n) {
          int st = buf.uint16();
          int run = buf.uint16();
          for (int o = 0; o < run; o++) {
            idx[i] = st + o;
            pos[i * 3 + 0] = buf.float32();
            pos[i * 3 + 1] = buf.float32();
            pos[i * 3 + 2] = buf.float32();
            nrm[i * 3 + 0] = buf.float32();
            nrm[i * 3 + 1] = buf.float32();
            nrm[i * 3 + 2] = buf.float32();
            i++;
          } 
        } 
        frames.add(new MeshAnim.Frame(tm, idx, pos, nrm));
      } 
      this.a = new MeshAnim(frames.<MeshAnim.Frame>toArray(new MeshAnim.Frame[0]), len);
    }
    
    public void init() {}
  }
}
