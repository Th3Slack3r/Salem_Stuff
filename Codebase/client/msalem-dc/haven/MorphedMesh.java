package haven;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class MorphedMesh extends FastMesh implements ResPart {
   private static Map<MorphedMesh.Morpher.Factory, Collection<MorphedMesh.MorphedBuf>> bufs = new CacheMap<>(CacheMap.RefType.WEAK);

   private static MorphedMesh.MorphedBuf buf(VertexBuf buf, MorphedMesh.Morpher.Factory morph) {
      Collection<MorphedMesh.MorphedBuf> bl;
      synchronized (bufs) {
         bl = bufs.get(morph);
         if (bl == null) {
            bufs.put(morph, bl = new LinkedList<>());
         }
      }

      synchronized (bl) {
         for (MorphedMesh.MorphedBuf b : bl) {
            if (b.from == buf) {
               return b;
            }
         }

         MorphedMesh.MorphedBuf bx = new MorphedMesh.MorphedBuf(buf, morph);
         bl.add(bx);
         return bx;
      }
   }

   public MorphedMesh(FastMesh mesh, MorphedMesh.Morpher.Factory pose) {
      super(mesh, buf(mesh.vert, pose));
   }

   @Override
   public boolean setup(RenderList rl) {
      ((MorphedMesh.MorphedBuf)this.vert).update();
      return super.setup(rl);
   }

   @Override
   protected boolean compile() {
      return false;
   }

   @Override
   public int partid() {
      return this.from instanceof ResPart ? ((ResPart)this.from).partid() : -1;
   }

   @Override
   public String toString() {
      return "morphed(" + this.from + ")";
   }

   public static MorphedMesh.Morpher.Factory combine(final MorphedMesh.Morpher.Factory... parts) {
      return new MorphedMesh.Morpher.Factory() {
         @Override
         public MorphedMesh.Morpher create(MorphedMesh.MorphedBuf vb) {
            final MorphedMesh.Morpher[] mparts = new MorphedMesh.Morpher[parts.length];

            for (int i = 0; i < parts.length; i++) {
               mparts[i] = parts[i].create(vb);
            }

            return new MorphedMesh.Morpher() {
               @Override
               public boolean update() {
                  boolean ret = false;

                  for (MorphedMesh.Morpher p : mparts) {
                     if (p.update()) {
                        ret = true;
                     }
                  }

                  return ret;
               }

               @Override
               public void morphp(FloatBuffer dst, FloatBuffer src) {
                  for (MorphedMesh.Morpher p : mparts) {
                     p.morphp(dst, src);
                     src = dst;
                  }
               }

               @Override
               public void morphd(FloatBuffer dst, FloatBuffer src) {
                  for (MorphedMesh.Morpher p : mparts) {
                     p.morphd(dst, src);
                     src = dst;
                  }
               }
            };
         }
      };
   }

   public static class MorphedBuf extends VertexBuf {
      public final VertexBuf from;
      private final MorphedMesh.Morpher morph;

      private static VertexBuf.AttribArray[] ohBitterSweetJavaDays(VertexBuf from) {
         VertexBuf.AttribArray[] ret = new VertexBuf.AttribArray[from.bufs.length];

         for (int i = 0; i < from.bufs.length; i++) {
            if (from.bufs[i] instanceof VertexBuf.VertexArray) {
               ret[i] = ((VertexBuf.VertexArray)from.bufs[i]).dup();
               ret[i].vbomode(35048);
            } else if (from.bufs[i] instanceof VertexBuf.NormalArray) {
               ret[i] = ((VertexBuf.NormalArray)from.bufs[i]).dup();
               ret[i].vbomode(35048);
            } else if (from.bufs[i] instanceof PoseMorph.BoneArray) {
               ret[i] = ((PoseMorph.BoneArray)from.bufs[i]).dup();
            } else {
               ret[i] = from.bufs[i];
            }
         }

         return ret;
      }

      private MorphedBuf(VertexBuf buf, MorphedMesh.Morpher.Factory morph) {
         super(ohBitterSweetJavaDays(buf));
         this.from = buf;
         this.morph = morph.create(this);
      }

      public void update() {
         if (this.morph.update()) {
            VertexBuf.VertexArray apos = this.buf(VertexBuf.VertexArray.class);
            VertexBuf.NormalArray anrm = this.buf(VertexBuf.NormalArray.class);
            FloatBuffer opos = this.from.buf(VertexBuf.VertexArray.class).data;
            FloatBuffer onrm = this.from.buf(VertexBuf.NormalArray.class).data;
            FloatBuffer npos = apos.data;
            FloatBuffer nnrm = anrm.data;
            this.morph.morphp(npos, opos);
            this.morph.morphd(nnrm, onrm);
            apos.update();
            anrm.update();
         }
      }
   }

   public interface Morpher {
      boolean update();

      void morphp(FloatBuffer var1, FloatBuffer var2);

      void morphd(FloatBuffer var1, FloatBuffer var2);

      public interface Factory {
         MorphedMesh.Morpher create(MorphedMesh.MorphedBuf var1);
      }
   }
}
