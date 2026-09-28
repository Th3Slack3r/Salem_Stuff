package haven;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

public class MeshAnim {
   public final MeshAnim.Frame[] frames;
   public final float len;

   public MeshAnim(MeshAnim.Frame[] frames, float len) {
      this.frames = frames;
      this.len = len;
   }

   public boolean animp(FastMesh mesh) {
      int min = -1;
      int max = -1;

      for (int i = 0; i < mesh.num * 3; i++) {
         int vi = mesh.indb.get(i);
         if (min < 0) {
            max = vi;
            min = vi;
         } else if (vi < min) {
            min = vi;
         } else if (vi > max) {
            max = vi;
         }
      }

      boolean[] used = new boolean[max + 1 - min];

      for (int ix = 0; ix < mesh.num * 3; ix++) {
         int vi = mesh.indb.get(ix);
         used[vi - min] = true;
      }

      for (MeshAnim.Frame f : this.frames) {
         for (int ix = 0; ix < f.idx.length; ix++) {
            int vi = f.idx[ix];
            if (vi >= min && vi <= max && used[f.idx[ix] - min]) {
               return true;
            }
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
         this.aupdate(0.0F);
      }

      public void aupdate(float time) {
         if (time > MeshAnim.this.len) {
            time = MeshAnim.this.len;
         }

         int l = 0;
         int r = MeshAnim.this.frames.length;

         while (true) {
            int c = l + (r - l >> 1);
            float ct = MeshAnim.this.frames[c].time;
            float nt = c < MeshAnim.this.frames.length - 1 ? MeshAnim.this.frames[c + 1].time : MeshAnim.this.len;
            if (ct > time) {
               r = c;
            } else {
               if (!(nt < time)) {
                  this.cf = MeshAnim.this.frames[c];
                  this.nf = MeshAnim.this.frames[(c + 1) % MeshAnim.this.frames.length];
                  if (nt == ct) {
                     this.a = 0.0F;
                  } else {
                     this.a = (time - ct) / (nt - ct);
                  }

                  this.seq++;
                  return;
               }

               l = c + 1;
            }
         }
      }

      public void tick(float dt) {
         this.time += dt;

         while (this.time > MeshAnim.this.len) {
            this.time = this.time - MeshAnim.this.len;
         }

         this.aupdate(this.time);
      }

      @Override
      public MorphedMesh.Morpher create(MorphedMesh.MorphedBuf vb) {
         return new MorphedMesh.Morpher() {
            int lseq = -1;

            @Override
            public boolean update() {
               if (this.lseq == Anim.this.seq) {
                  return false;
               } else {
                  this.lseq = Anim.this.seq;
                  return true;
               }
            }

            @Override
            public void morphp(FloatBuffer dst, FloatBuffer src) {
               if (dst != src) {
                  int l = dst.capacity();

                  for (int i = 0; i < l; i++) {
                     dst.put(i, src.get(i));
                  }
               }

               MeshAnim.Frame f = Anim.this.cf;
               float a = 1.0F - Anim.this.a;
               int i = 0;

               for (int po = 0; i < f.idx.length; po += 3) {
                  int vo = f.idx[i] * 3;
                  float x = dst.get(vo);
                  float y = dst.get(vo + 1);
                  float z = dst.get(vo + 2);
                  x += f.pos[po] * a;
                  y += f.pos[po + 1] * a;
                  z += f.pos[po + 2] * a;
                  dst.put(vo, x).put(vo + 1, y).put(vo + 2, z);
                  i++;
               }

               f = Anim.this.nf;
               a = Anim.this.a;
               i = 0;

               for (int po = 0; i < f.idx.length; po += 3) {
                  int vo = f.idx[i] * 3;
                  float x = dst.get(vo);
                  float y = dst.get(vo + 1);
                  float z = dst.get(vo + 2);
                  x += f.pos[po] * a;
                  y += f.pos[po + 1] * a;
                  z += f.pos[po + 2] * a;
                  dst.put(vo, x).put(vo + 1, y).put(vo + 2, z);
                  i++;
               }
            }

            @Override
            public void morphd(FloatBuffer dst, FloatBuffer src) {
               if (dst != src) {
                  int l = dst.capacity();

                  for (int i = 0; i < l; i++) {
                     dst.put(i, src.get(i));
                  }
               }

               MeshAnim.Frame f = Anim.this.cf;
               float a = 1.0F - Anim.this.a;
               int i = 0;

               for (int po = 0; i < f.idx.length; po += 3) {
                  int vo = f.idx[i] * 3;
                  float x = dst.get(vo);
                  float y = dst.get(vo + 1);
                  float z = dst.get(vo + 2);
                  x += f.nrm[po] * a;
                  y += f.nrm[po + 1] * a;
                  z += f.nrm[po + 2] * a;
                  dst.put(vo, x).put(vo + 1, y).put(vo + 2, z);
                  i++;
               }

               f = Anim.this.nf;
               a = Anim.this.a;
               i = 0;

               for (int po = 0; i < f.idx.length; po += 3) {
                  int vo = f.idx[i] * 3;
                  float x = dst.get(vo);
                  float y = dst.get(vo + 1);
                  float z = dst.get(vo + 2);
                  x += f.nrm[po] * a;
                  y += f.nrm[po + 1] * a;
                  z += f.nrm[po + 2] * a;
                  dst.put(vo, x).put(vo + 1, y).put(vo + 2, z);
                  i++;
               }
            }
         };
      }
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

   @Resource.LayerName("manim")
   public static class Res extends Resource.Layer {
      public final int id;
      public final MeshAnim a;

      public Res(Resource res, byte[] data) {
         res.getClass();
         super();
         Message buf = new Message(0, data);
         this.id = buf.int16();
         float len = buf.float32();
         List<MeshAnim.Frame> frames = new LinkedList<>();

         while (true) {
            int t = buf.uint8();
            if (t == 0) {
               this.a = new MeshAnim(frames.toArray(new MeshAnim.Frame[0]), len);
               return;
            }

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
      }

      @Override
      public void init() {
      }
   }
}
