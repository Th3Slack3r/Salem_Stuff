package haven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Composited implements Rendered {
   public final Skeleton skel;
   public final Skeleton.Pose pose;
   private final PoseMorph morph;
   private Collection<Composited.Model> mod = new LinkedList<>();
   public Collection<Composited.Equ> equ = new LinkedList<>();
   public Composited.Poses poses = new Composited.Poses();
   public List<Composited.MD> nmod = null;
   public List<Composited.MD> cmod = new LinkedList<>();
   public List<Composited.ED> nequ = null;
   public List<Composited.ED> cequ = new LinkedList<>();
   private static final Rendered.Order modorder = new Rendered.Order<Composited.Model.Layer>() {
      private final Rendered.RComparator<Composited.Model.Layer> cmp = new Rendered.RComparator<Composited.Model.Layer>() {
         public int compare(Composited.Model.Layer a, Composited.Model.Layer b, GLState.Buffer sa, GLState.Buffer sb) {
            return a.z1 != b.z1 ? a.z1 - b.z1 : a.z2 - b.z2;
         }
      };

      @Override
      public int mainz() {
         return 1;
      }

      @Override
      public Rendered.RComparator<Composited.Model.Layer> cmp() {
         return this.cmp;
      }
   };

   public Composited(Skeleton skel) {
      this.skel = skel;
      this.pose = skel.new Pose(skel.bindpose);
      this.morph = new PoseMorph(this.pose);
   }

   private void nmod(boolean nocatch) {
      Iterator<Composited.MD> i = this.nmod.iterator();

      while (i.hasNext()) {
         Composited.MD md = i.next();

         try {
            if (md.real == null) {
               FastMesh.MeshRes mr = md.mod.get().layer(FastMesh.MeshRes.class);
               md.real = new Composited.Model(mr.m);
               if (md.mod.get().name.equals("gfx/borka/male") || md.mod.get().name.equals("gfx/borka/female")) {
                  md.real.z = -1;
               }

               this.mod.add(md.real);
            }

            Iterator<Indir<Resource>> o = md.tex.iterator();

            while (o.hasNext()) {
               Indir<Resource> res = o.next();

               for (Material.Res mr : res.get().layers(Material.Res.class)) {
                  md.real.addlay(mr.get());
               }

               o.remove();
            }

            i.remove();
         } catch (Loading var8) {
            if (nocatch) {
               throw var8;
            }
         }
      }

      if (this.nmod.isEmpty()) {
         this.nmod = null;
      }
   }

   private void nequ(boolean nocatch) {
      Iterator<Composited.ED> i = this.nequ.iterator();

      while (i.hasNext()) {
         Composited.ED ed = i.next();

         try {
            if (ed.t == 0) {
               this.equ.add(new Composited.SpriteEqu(ed));
            } else if (ed.t == 1) {
               this.equ.add(new Composited.LightEqu(ed));
            }

            i.remove();
         } catch (Loading var5) {
            if (nocatch) {
               throw var5;
            }
         }
      }

      if (this.nequ.isEmpty()) {
         this.nequ = null;
      }
   }

   public void changes(boolean nocatch) {
      if (this.nmod != null) {
         this.nmod(nocatch);
      }

      if (this.nequ != null) {
         this.nequ(nocatch);
      }
   }

   public void changes() {
      this.changes(false);
   }

   @Override
   public boolean setup(RenderList rl) {
      this.changes();

      for (Composited.Model mod : this.mod) {
         rl.add(mod, null);
      }

      for (Composited.Equ equ : this.equ) {
         rl.add(equ, equ.et);
      }

      return false;
   }

   @Override
   public void draw(GOut g) {
   }

   public void tick(int dt) {
      if (this.poses != null) {
         this.poses.tick(dt / 1000.0F);
      }

      for (Composited.Equ equ : this.equ) {
         equ.tick(dt);
      }
   }

   @Deprecated
   public void tick(int dt, double v) {
      this.tick(dt);
   }

   public void chmod(List<Composited.MD> mod) {
      if (!mod.equals(this.cmod)) {
         this.mod = new LinkedList<>();
         this.nmod = new LinkedList<>();

         for (Composited.MD md : mod) {
            this.nmod.add(md.clone());
         }

         this.cmod = new ArrayList<>(mod);
      }
   }

   public void chequ(List<Composited.ED> equ) {
      if (!equ.equals(this.cequ)) {
         this.equ = new LinkedList<>();
         this.nequ = new LinkedList<>();

         for (Composited.ED ed : equ) {
            this.nequ.add(ed.clone());
         }

         this.cequ = new ArrayList<>(equ);
      }
   }

   public static class ED implements Cloneable {
      public int t;
      public String at;
      public Indir<Resource> res;
      public Coord3f off;

      public ED(int t, String at, Indir<Resource> res, Coord3f off) {
         this.t = t;
         this.at = at;
         this.res = res;
         this.off = off;
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof Composited.ED)) {
            return false;
         } else {
            Composited.ED e = (Composited.ED)o;
            return this.t == e.t && this.at.equals(e.at) && this.res.equals(e.res);
         }
      }

      public Composited.ED clone() {
         try {
            return (Composited.ED)super.clone();
         } catch (CloneNotSupportedException var2) {
            throw new RuntimeException(var2);
         }
      }
   }

   public abstract class Equ implements Rendered {
      private final GLState et;

      private Equ(Composited.ED ed) {
         Skeleton.BoneOffset bo = ed.res.get().layer(Skeleton.BoneOffset.class, ed.at);
         GLState bt;
         if (bo != null) {
            bt = bo.forpose(Composited.this.pose);
         } else {
            Skeleton.Bone bone = Composited.this.skel.bones.get(ed.at);
            bt = Composited.this.pose.bonetrans(bone.idx);
         }

         if (ed.off.x == 0.0F && ed.off.y == 0.0F && ed.off.z == 0.0F) {
            this.et = bt;
         } else {
            this.et = GLState.compose(bt, Location.xlate(ed.off));
         }
      }

      public void tick(int dt) {
      }
   }

   private class LightEqu extends Composited.Equ {
      private final Light l;

      private LightEqu(Composited.ED ed) {
         super(ed);
         this.l = ed.res.get().layer(Light.Res.class).make();
      }

      @Override
      public void draw(GOut g) {
      }

      @Override
      public boolean setup(RenderList rl) {
         rl.add(this.l, null);
         return false;
      }
   }

   public static class MD implements Cloneable {
      public Indir<Resource> mod;
      public List<Indir<Resource>> tex;
      private Composited.Model real;

      public MD(Indir<Resource> mod, List<Indir<Resource>> tex) {
         this.mod = mod;
         this.tex = tex;
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof Composited.MD)) {
            return false;
         } else {
            Composited.MD m = (Composited.MD)o;
            return this.mod.equals(m.mod) && this.tex.equals(m.tex);
         }
      }

      public Composited.MD clone() {
         try {
            Composited.MD ret = (Composited.MD)super.clone();
            ret.tex = new LinkedList<>(this.tex);
            return ret;
         } catch (CloneNotSupportedException var2) {
            throw new RuntimeException(var2);
         }
      }

      @Override
      public String toString() {
         return this.mod + "+" + this.tex;
      }
   }

   private class Model implements Rendered {
      private final MorphedMesh m;
      int z = 0;
      int lz = 0;
      private final List<Composited.Model.Layer> lay = new ArrayList<>();

      private Model(FastMesh m) {
         this.m = new MorphedMesh(m, Composited.this.morph);
      }

      private void addlay(Material mat) {
         this.lay.add(new Composited.Model.Layer(mat, this.z, this.lz++));
      }

      @Override
      public void draw(GOut g) {
      }

      @Override
      public boolean setup(RenderList r) {
         this.m.setup(r);

         for (Composited.Model.Layer lay : this.lay) {
            r.add(lay, null);
         }

         return false;
      }

      private class Layer implements FRendered {
         private final Material mat;
         private final int z1;
         private final int z2;

         private Layer(Material mat, int z1, int z2) {
            this.mat = mat;
            this.z1 = z1;
            this.z2 = z2;
         }

         @Override
         public void draw(GOut g) {
            Model.this.m.draw(g);
         }

         @Override
         public void drawflat(GOut g) {
            if (this.z2 == 0) {
               Model.this.m.drawflat(g);
            }
         }

         @Override
         public boolean setup(RenderList r) {
            r.prepo(Composited.modorder);
            r.prepo(this.mat);
            return true;
         }
      }
   }

   public class Poses {
      private final Skeleton.PoseMod[] mods;
      Skeleton.Pose old;
      float ipold = 0.0F;
      float ipol = 0.0F;
      public float limit = -1.0F;
      public boolean stat;
      public boolean ldone;

      public Poses() {
         this.mods = new Skeleton.PoseMod[0];
      }

      public Poses(List<? extends Skeleton.PoseMod> mods) {
         this.mods = mods.toArray(new Skeleton.PoseMod[0]);
         this.stat = true;

         for (Skeleton.PoseMod mod : this.mods) {
            if (!mod.stat()) {
               this.stat = false;
               break;
            }
         }
      }

      private void rebuild() {
         Composited.this.pose.reset();

         for (Skeleton.PoseMod m : this.mods) {
            m.apply(Composited.this.pose);
         }

         if (this.ipold > 0.0F) {
            Composited.this.pose.blend(this.old, this.ipold);
         }

         Composited.this.pose.gbuild();
      }

      public void set(float ipol) {
         if ((this.ipol = ipol) > 0.0F) {
            this.old = Composited.this.skel.new Pose(Composited.this.pose);
            this.ipold = 1.0F;
         }

         Composited.this.poses = this;
         this.rebuild();
      }

      public void tick(float dt) {
         boolean build = false;
         if (this.limit >= 0.0F && (this.limit -= dt) < 0.0F) {
            this.ldone = true;
         }

         boolean done = this.ldone;

         for (Skeleton.PoseMod m : this.mods) {
            m.tick(dt);
            if (!m.done()) {
               done = false;
            }
         }

         if (!this.stat) {
            build = true;
         }

         if (this.ipold > 0.0F) {
            if ((this.ipold = this.ipold - dt / this.ipol) < 0.0F) {
               this.ipold = 0.0F;
               this.old = null;
            }

            build = true;
         }

         if (build) {
            this.rebuild();
         }

         if (done) {
            this.done();
         }
      }

      @Deprecated
      public void tick(float dt, double v) {
         this.tick(dt);
      }

      protected void done() {
      }
   }

   private class SpriteEqu extends Composited.Equ {
      private final Sprite spr;

      private SpriteEqu(Composited.ED ed) {
         super(ed);
         this.spr = Sprite.create(null, ed.res.get(), new Message(0));
      }

      @Override
      public void draw(GOut g) {
      }

      @Override
      public boolean setup(RenderList rl) {
         rl.add(this.spr, null);
         return false;
      }

      @Override
      public void tick(int dt) {
         this.spr.tick(dt);
      }
   }
}
