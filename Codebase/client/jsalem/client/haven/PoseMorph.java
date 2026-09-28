package haven;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class PoseMorph implements MorphedMesh.Morpher.Factory {
   public final Skeleton.Pose pose;
   private float[][] offs;
   private int seq = -1;

   public PoseMorph(Skeleton.Pose pose) {
      this.pose = pose;
      this.offs = new float[pose.skel().blist.length][16];
   }

   public static boolean boned(FastMesh mesh) {
      PoseMorph.BoneArray ba = mesh.vert.buf(PoseMorph.BoneArray.class);
      if (ba == null) {
         return false;
      } else {
         for (int i = 0; i < mesh.num * 3; i++) {
            if (ba.data.get(mesh.indb.get(i) * ba.n) != -1) {
               return true;
            }
         }

         return false;
      }
   }

   public static String boneidp(FastMesh mesh) {
      PoseMorph.BoneArray ba = mesh.vert.buf(PoseMorph.BoneArray.class);
      if (ba == null) {
         return null;
      } else {
         int retb = -1;

         for (int i = 0; i < mesh.num * 3; i++) {
            int vi = mesh.indb.get(i) * ba.n;
            int curb = ba.data.get(vi);
            if (curb == -1) {
               return null;
            }

            if (retb == -1) {
               retb = curb;
            } else if (retb != curb) {
               return null;
            }

            if (ba.n != 1 && ba.data.get(vi + 1) != -1) {
               return null;
            }
         }

         return ba.names[retb];
      }
   }

   private void update() {
      if (this.seq != this.pose.seq) {
         this.seq = this.pose.seq;

         for (int i = 0; i < this.offs.length; i++) {
            this.pose.boneoff(i, this.offs[i]);
         }
      }
   }

   @Override
   public MorphedMesh.Morpher create(final MorphedMesh.MorphedBuf vb) {
      PoseMorph.BoneArray ob = vb.from.buf(PoseMorph.BoneArray.class);
      PoseMorph.BoneArray nb = vb.buf(PoseMorph.BoneArray.class);
      int[] xl = new int[nb.names.length];

      for (int i = 0; i < xl.length; i++) {
         Skeleton.Bone b = this.pose.skel().bones.get(nb.names[i]);
         if (b == null) {
            throw new RuntimeException("Bone \"" + nb.names[i] + "\" in vertex-buf reference does not exist in skeleton " + this.pose.skel());
         }

         xl[i] = b.idx;
      }

      for (int i = 0; i < ob.data.capacity(); i++) {
         if (ob.data.get(i) == -1) {
            nb.data.put(i, -1);
         } else {
            nb.data.put(i, xl[ob.data.get(i)]);
         }
      }

      return new MorphedMesh.Morpher() {
         private int pseq = -1;

         @Override
         public boolean update() {
            if (this.pseq == PoseMorph.this.pose.seq) {
               return false;
            } else {
               PoseMorph.this.update();
               this.pseq = PoseMorph.this.pose.seq;
               return true;
            }
         }

         @Override
         public void morphp(FloatBuffer dst, FloatBuffer src) {
            PoseMorph.BoneArray ba = vb.buf(PoseMorph.BoneArray.class);
            int apv = ba.n;
            IntBuffer bl = ba.data;
            FloatBuffer wl = vb.buf(PoseMorph.WeightArray.class).data;
            int vo = 0;
            int ao = 0;

            for (int ix = 0; ix < vb.num; ix++) {
               float opx = src.get(vo);
               float opy = src.get(vo + 1);
               float opz = src.get(vo + 2);
               float npx = 0.0F;
               float npy = 0.0F;
               float npz = 0.0F;
               float rw = 1.0F;

               for (int o = 0; o < apv; o++) {
                  int bi = bl.get(ao + o);
                  if (bi < 0) {
                     break;
                  }

                  float bw = wl.get(ao + o);
                  float[] xf = PoseMorph.this.offs[bi];
                  npx += (xf[0] * opx + xf[4] * opy + xf[8] * opz + xf[12]) * bw;
                  npy += (xf[1] * opx + xf[5] * opy + xf[9] * opz + xf[13]) * bw;
                  npz += (xf[2] * opx + xf[6] * opy + xf[10] * opz + xf[14]) * bw;
                  rw -= bw;
               }

               npx += opx * rw;
               npy += opy * rw;
               npz += opz * rw;
               dst.put(vo, npx);
               dst.put(vo + 1, npy);
               dst.put(vo + 2, npz);
               vo += 3;
               ao += apv;
            }
         }

         @Override
         public void morphd(FloatBuffer dst, FloatBuffer src) {
            PoseMorph.BoneArray ba = vb.buf(PoseMorph.BoneArray.class);
            int apv = ba.n;
            IntBuffer bl = ba.data;
            FloatBuffer wl = vb.buf(PoseMorph.WeightArray.class).data;
            int vo = 0;
            int ao = 0;

            for (int ix = 0; ix < vb.num; ix++) {
               float onx = src.get(vo);
               float ony = src.get(vo + 1);
               float onz = src.get(vo + 2);
               float nnx = 0.0F;
               float nny = 0.0F;
               float nnz = 0.0F;
               float rw = 1.0F;

               for (int o = 0; o < apv; o++) {
                  int bi = bl.get(ao + o);
                  if (bi < 0) {
                     break;
                  }

                  float bw = wl.get(ao + o);
                  float[] xf = PoseMorph.this.offs[bi];
                  nnx += (xf[0] * onx + xf[4] * ony + xf[8] * onz) * bw;
                  nny += (xf[1] * onx + xf[5] * ony + xf[9] * onz) * bw;
                  nnz += (xf[2] * onx + xf[6] * ony + xf[10] * onz) * bw;
                  rw -= bw;
               }

               nnx += onx * rw;
               nny += ony * rw;
               nnz += onz * rw;
               dst.put(vo, nnx);
               dst.put(vo + 1, nny);
               dst.put(vo + 2, nnz);
               vo += 3;
               ao += apv;
            }
         }
      };
   }

   public static class BoneArray extends VertexBuf.IntArray {
      public final String[] names;

      public BoneArray(int apv, IntBuffer data, String[] names) {
         super(apv, data);
         this.names = names;
      }

      public PoseMorph.BoneArray dup() {
         return new PoseMorph.BoneArray(this.n, Utils.bufcp(this.data), Utils.splice(this.names, 0));
      }
   }

   public static class WeightArray extends VertexBuf.FloatArray {
      public WeightArray(int apv, FloatBuffer data) {
         super(apv, data);
      }
   }
}
