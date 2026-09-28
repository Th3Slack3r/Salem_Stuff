package haven;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class AnimSprite extends Sprite {
   private Rendered[] parts;
   private MeshAnim.Anim[] anims;
   public static final Sprite.Factory fact = new Sprite.Factory() {
      @Override
      public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
         return res.layer(MeshAnim.Res.class) == null ? null : new AnimSprite(owner, res, sdt);
      }
   };

   private AnimSprite(Sprite.Owner owner, Resource res, Message sdt) {
      super(owner, res);
      int mask = sdt.eom() ? -65536 : decnum(sdt);
      Collection<MeshAnim> anims = new LinkedList<>();

      for (MeshAnim.Res ar : res.layers(MeshAnim.Res.class)) {
         if (ar.id < 0 || (1 << ar.id & mask) != 0) {
            anims.add(ar.a);
         }
      }

      this.anims = new MeshAnim.Anim[anims.size()];
      Iterator<MeshAnim> it = anims.iterator();

      for (int i = 0; it.hasNext(); i++) {
         this.anims[i] = (MeshAnim)it.next().new Anim();
      }

      MorphedMesh.Morpher.Factory morph = MorphedMesh.combine(this.anims);
      Collection<Rendered> rl = new LinkedList<>();

      for (FastMesh.MeshRes mr : res.layers(FastMesh.MeshRes.class)) {
         if (mr.mat != null && (mr.id < 0 || (1 << mr.id & mask) != 0)) {
            boolean stat = true;

            for (MeshAnim anim : anims) {
               if (anim.animp(mr.m)) {
                  stat = false;
                  break;
               }
            }

            if (stat) {
               rl.add(mr.mat.get().apply(mr.m));
            } else {
               rl.add(mr.mat.get().apply(new MorphedMesh(mr.m, morph)));
            }
         }
      }

      this.parts = rl.toArray(new Rendered[0]);
   }

   @Override
   public boolean setup(RenderList rl) {
      for (Rendered p : this.parts) {
         rl.add(p, null);
      }

      return false;
   }

   @Override
   public boolean tick(int idt) {
      float dt = idt / 1000.0F;

      for (MeshAnim.Anim anim : this.anims) {
         anim.tick(dt);
      }

      return false;
   }
}
