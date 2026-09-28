package haven;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

public class SkelSprite extends Sprite implements Gob.Overlay.CUpd {
   private static final GLState rigid = new Material.Colors(Color.GREEN);
   private static final GLState morphed = new Material.Colors(Color.RED);
   private static final GLState unboned = new Material.Colors(Color.YELLOW);
   public static boolean bonedb = false;
   public static final float defipol = 0.0F;
   private final Skeleton skel;
   public final Skeleton.Pose pose;
   private final PoseMorph morph;
   private Skeleton.Pose oldpose;
   private float ipold;
   private float ipol;
   private Skeleton.PoseMod[] mods = new Skeleton.PoseMod[0];
   private boolean stat = true;
   private Rendered[] parts;
   public static final Sprite.Factory fact = new Sprite.Factory() {
      @Override
      public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
         return res.layer(Skeleton.Res.class) == null ? null : new SkelSprite(owner, res, sdt);
      }
   };

   private SkelSprite(Sprite.Owner owner, Resource res, Message sdt) {
      super(owner, res);
      this.skel = res.layer(Skeleton.Res.class).s;
      this.pose = this.skel.new Pose(this.skel.bindpose);
      this.morph = new PoseMorph(this.pose);
      int fl = sdt.eom() ? -65536 : decnum(sdt);
      this.chparts(fl);
      this.chposes(fl, 0.0F);
   }

   private void chparts(int mask) {
      Collection<Rendered> rl = new LinkedList<>();

      for (FastMesh.MeshRes mr : this.res.layers(FastMesh.MeshRes.class)) {
         if (mr.mat != null && (mr.id < 0 || (1 << mr.id & mask) != 0)) {
            Rendered r;
            if (PoseMorph.boned(mr.m)) {
               String bnm = PoseMorph.boneidp(mr.m);
               if (bnm == null) {
                  r = mr.mat.get().apply(new MorphedMesh(mr.m, this.morph));
                  if (bonedb) {
                     r = morphed.apply(r);
                  }
               } else {
                  r = this.pose.bonetrans2(this.skel.bones.get(bnm).idx).apply(mr.mat.get().apply(mr.m));
                  if (bonedb) {
                     r = rigid.apply(r);
                  }
               }
            } else {
               r = mr.mat.get().apply(mr.m);
               if (bonedb) {
                  r = unboned.apply(r);
               }
            }

            rl.add(r);
         }
      }

      this.parts = rl.toArray(new Rendered[0]);
   }

   private void rebuild() {
      this.pose.reset();

      for (Skeleton.PoseMod m : this.mods) {
         m.apply(this.pose);
      }

      if (this.ipold > 0.0F) {
         float f = this.ipold * this.ipold * (3.0F - 2.0F * this.ipold);
         this.pose.blend(this.oldpose, f);
      }

      this.pose.gbuild();
   }

   private void chposes(int mask, float ipol) {
      if ((this.ipol = ipol) > 0.0F) {
         this.oldpose = this.skel.new Pose(this.pose);
         this.ipold = 1.0F;
      }

      Collection<Skeleton.PoseMod> poses = new LinkedList<>();
      this.stat = true;

      for (Skeleton.ResPose p : this.res.layers(Skeleton.ResPose.class)) {
         if (p.id < 0 || (mask & 1 << p.id) != 0) {
            Skeleton.ModOwner mo = this.owner instanceof Skeleton.ModOwner ? (Skeleton.ModOwner)this.owner : Skeleton.ModOwner.nil;
            Skeleton.PoseMod mod = p.forskel(mo, this.skel, p.defmode);
            if (!mod.stat()) {
               this.stat = false;
            }

            poses.add(mod);
         }
      }

      this.mods = poses.toArray(new Skeleton.PoseMod[0]);
      this.rebuild();
   }

   @Override
   public void update(Message sdt) {
      int fl = sdt.eom() ? -65536 : decnum(sdt);
      this.chparts(fl);
      this.chposes(fl, 0.0F);
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
      if (!this.stat || this.ipold > 0.0F) {
         float dt = idt / 1000.0F;

         for (Skeleton.PoseMod m : this.mods) {
            m.tick(dt);
         }

         if (this.ipold > 0.0F && (this.ipold = this.ipold - dt / this.ipol) < 0.0F) {
            this.ipold = 0.0F;
            this.oldpose = null;
         }

         this.rebuild();
      }

      return false;
   }

   static {
      Console.setscmd("bonedb", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            SkelSprite.bonedb = Utils.parsebool(args[1], false);
         }
      });
   }
}
