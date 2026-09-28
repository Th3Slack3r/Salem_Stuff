package haven;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.media.opengl.GL2;

public class Skeleton {
   public final Map<String, Skeleton.Bone> bones = new HashMap<>();
   public final Skeleton.Bone[] blist;
   public final Skeleton.Pose bindpose;

   public Skeleton(Collection<Skeleton.Bone> bones) {
      Set<Skeleton.Bone> bset = new HashSet<>(bones);
      this.blist = new Skeleton.Bone[bones.size()];
      int idx = 0;

      for (Skeleton.Bone b : bones) {
         this.bones.put(b.name, b);
      }

      while (!bset.isEmpty()) {
         boolean f = false;
         Iterator<Skeleton.Bone> i = bset.iterator();

         while (i.hasNext()) {
            Skeleton.Bone b = i.next();
            boolean has;
            if (b.parent == null) {
               has = true;
            } else {
               has = false;

               for (Skeleton.Bone p : this.blist) {
                  if (p == b.parent) {
                     has = true;
                     break;
                  }
               }
            }

            if (has) {
               this.blist[b.idx = idx++] = b;
               i.remove();
               f = true;
            }
         }

         if (!f) {
            throw new RuntimeException("Cyclical bone hierarchy");
         }
      }

      this.bindpose = this.mkbindpose();
   }

   private static float[] rotasq(float[] q, float[] axis, float angle) {
      float m = (float)Math.sin(angle / 2.0);
      q[0] = (float)Math.cos(angle / 2.0);
      q[1] = m * axis[0];
      q[2] = m * axis[1];
      q[3] = m * axis[2];
      return q;
   }

   private static float[] qqmul(float[] d, float[] a, float[] b) {
      float aw = a[0];
      float ax = a[1];
      float ay = a[2];
      float az = a[3];
      float bw = b[0];
      float bx = b[1];
      float by = b[2];
      float bz = b[3];
      d[0] = aw * bw - ax * bx - ay * by - az * bz;
      d[1] = aw * bx + ax * bw + ay * bz - az * by;
      d[2] = aw * by - ax * bz + ay * bw + az * bx;
      d[3] = aw * bz + ax * by - ay * bx + az * bw;
      return d;
   }

   private static float[] vqrot(float[] d, float[] v, float[] q) {
      float vx = v[0];
      float vy = v[1];
      float vz = v[2];
      float qw = q[0];
      float qx = q[1];
      float qy = q[2];
      float qz = q[3];
      d[0] = qw * qw * vx + 2.0F * qw * qy * vz - 2.0F * qw * qz * vy + qx * qx * vx + 2.0F * qx * qy * vy + 2.0F * qx * qz * vz - qz * qz * vx - qy * qy * vx;
      d[1] = 2.0F * qx * qy * vx + qy * qy * vy + 2.0F * qy * qz * vz + 2.0F * qw * qz * vx - qz * qz * vy + qw * qw * vy - 2.0F * qw * qx * vz - qx * qx * vy;
      d[2] = 2.0F * qx * qz * vx + 2.0F * qy * qz * vy + qz * qz * vz - 2.0F * qw * qy * vx - qy * qy * vz + 2.0F * qw * qx * vy - qx * qx * vz + qw * qw * vz;
      return d;
   }

   private static float[] vset(float[] d, float[] s) {
      d[0] = s[0];
      d[1] = s[1];
      d[2] = s[2];
      return d;
   }

   private static float[] qset(float[] d, float[] s) {
      d[0] = s[0];
      d[1] = s[1];
      d[2] = s[2];
      d[3] = s[3];
      return d;
   }

   private static float[] vinv(float[] d, float[] s) {
      d[0] = -s[0];
      d[1] = -s[1];
      d[2] = -s[2];
      return d;
   }

   private static float[] qinv(float[] d, float[] s) {
      d[0] = s[0];
      d[1] = -s[1];
      d[2] = -s[2];
      d[3] = -s[3];
      return d;
   }

   private static float[] vvadd(float[] d, float[] a, float[] b) {
      float ax = a[0];
      float ay = a[1];
      float az = a[2];
      float bx = b[0];
      float by = b[1];
      float bz = b[2];
      d[0] = ax + bx;
      d[1] = ay + by;
      d[2] = az + bz;
      return d;
   }

   private static float[] qqslerp(float[] d, float[] a, float[] b, float t) {
      float aw = a[0];
      float ax = a[1];
      float ay = a[2];
      float az = a[3];
      float bw = b[0];
      float bx = b[1];
      float by = b[2];
      float bz = b[3];
      if (aw == bw && ax == bx && ay == by && az == bz) {
         return qset(d, a);
      } else {
         float cos = aw * bw + ax * bx + ay * by + az * bz;
         if (cos < 0.0F) {
            bw = -bw;
            bx = -bx;
            by = -by;
            bz = -bz;
            cos = -cos;
         }

         float d0;
         float d1;
         if (cos > 0.9999F) {
            d0 = 1.0F - t;
            d1 = t;
         } else {
            float da = (float)Math.acos(Utils.clip((double)cos, 0.0, 1.0));
            float nf = 1.0F / (float)Math.sin(da);
            d0 = (float)Math.sin((1.0F - t) * da) * nf;
            d1 = (float)Math.sin(t * da) * nf;
         }

         d[0] = d0 * aw + d1 * bw;
         d[1] = d0 * ax + d1 * bx;
         d[2] = d0 * ay + d1 * by;
         d[3] = d0 * az + d1 * bz;
         return d;
      }
   }

   public Skeleton.Pose mkbindpose() {
      Skeleton.Pose p = new Skeleton.Pose();

      for (int i = 0; i < this.blist.length; i++) {
         Skeleton.Bone b = this.blist[i];
         p.lpos[i][0] = b.ipos.x;
         p.lpos[i][1] = b.ipos.y;
         p.lpos[i][2] = b.ipos.z;
         rotasq(p.lrot[i], b.irax.to3a(), b.irang);
      }

      p.gbuild();
      return p;
   }

   public Skeleton.PoseMod nilmod() {
      return new Skeleton.PoseMod(Skeleton.ModOwner.nil) {
         @Override
         public boolean stat() {
            return true;
         }

         @Override
         public boolean done() {
            return false;
         }
      };
   }

   public static Skeleton.PoseMod combine(final Skeleton.PoseMod... mods) {
      Skeleton.PoseMod first = mods[0];
      return first.skel().super(first.owner) {
         final boolean stat;

         {
            boolean s = true;

            for (Skeleton.PoseMod m : mods) {
               s = s && m.stat();
            }

            this.stat = s;
         }

         @Override
         public void apply(Skeleton.Pose p) {
            for (Skeleton.PoseMod m : mods) {
               m.apply(p);
            }
         }

         @Override
         public boolean tick(float dt) {
            boolean ret = false;

            for (Skeleton.PoseMod m : mods) {
               if (m.tick(dt)) {
                  ret = true;
               }
            }

            return ret;
         }

         @Override
         public boolean stat() {
            return this.stat;
         }

         @Override
         public boolean done() {
            for (Skeleton.PoseMod m : mods) {
               if (m.done()) {
                  return true;
               }
            }

            return false;
         }
      };
   }

   public Skeleton.PoseMod mkposemod(Skeleton.ModOwner owner, Resource res, Message sdt) {
      Skeleton.ModFactory f = res.getcode(Skeleton.ModFactory.class, false);
      if (f == null) {
         f = Skeleton.ModFactory.def;
      }

      return f.create(this, owner, res, sdt);
   }

   public static class Bone {
      public String name;
      public Coord3f ipos;
      public Coord3f irax;
      public float irang;
      public Skeleton.Bone parent;
      public int idx;

      public Bone(String name, Coord3f ipos, Coord3f irax, float irang) {
         this.name = name;
         this.ipos = ipos;
         this.irax = irax;
         this.irang = irang;
      }
   }

   @Resource.LayerName("boneoff")
   public static class BoneOffset extends Resource.Layer implements Resource.IDLayer<String> {
      public final String nm;
      public final Skeleton.BoneOffset.Command[] prog;
      private static final Skeleton.BoneOffset.HatingJava[] opcodes = new Skeleton.BoneOffset.HatingJava[256];

      public BoneOffset(Resource res, byte[] buf) {
         res.super();
         int[] off = new int[]{0};
         this.nm = Utils.strd(buf, off);
         List<Skeleton.BoneOffset.Command> cbuf = new LinkedList<>();

         while (off[0] < buf.length) {
            cbuf.add(opcodes[buf[off[0]++]].make(buf, off));
         }

         this.prog = cbuf.toArray(new Skeleton.BoneOffset.Command[0]);
      }

      public String layerid() {
         return this.nm;
      }

      @Override
      public void init() {
      }

      public GLState forpose(Skeleton.Pose pose) {
         GLState[] ls = new GLState[this.prog.length];

         for (int i = 0; i < this.prog.length; i++) {
            ls[i] = this.prog[i].make(pose);
         }

         return GLState.compose(ls);
      }

      static {
         opcodes[0] = new Skeleton.BoneOffset.HatingJava() {
            @Override
            public Skeleton.BoneOffset.Command make(byte[] buf, int[] off) {
               final float x = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               final float y = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               final float z = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               return new Skeleton.BoneOffset.Command() {
                  @Override
                  public GLState make(Skeleton.Pose pose) {
                     return Location.xlate(new Coord3f(x, y, z));
                  }
               };
            }
         };
         opcodes[1] = new Skeleton.BoneOffset.HatingJava() {
            @Override
            public Skeleton.BoneOffset.Command make(byte[] buf, int[] off) {
               final float ang = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               final float ax = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               final float ay = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               final float az = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               return new Skeleton.BoneOffset.Command() {
                  @Override
                  public GLState make(Skeleton.Pose pose) {
                     return Location.rot(new Coord3f(ax, ay, az), ang);
                  }
               };
            }
         };
         opcodes[2] = new Skeleton.BoneOffset.HatingJava() {
            @Override
            public Skeleton.BoneOffset.Command make(byte[] buf, int[] off) {
               final String bonenm = Utils.strd(buf, off);
               return new Skeleton.BoneOffset.Command() {
                  @Override
                  public GLState make(Skeleton.Pose pose) {
                     Skeleton.Bone bone = pose.skel().bones.get(bonenm);
                     return pose.bonetrans(bone.idx);
                  }
               };
            }
         };
         opcodes[3] = new Skeleton.BoneOffset.HatingJava() {
            @Override
            public Skeleton.BoneOffset.Command make(byte[] buf, int[] off) {
               float rx1 = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               float ry1 = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               float rz1 = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
               float l = (float)Math.sqrt(rx1 * rx1 + ry1 * ry1 + rz1 * rz1);
               final Coord3f ref = new Coord3f(rx1 / l, ry1 / l, rz1 / l);
               final String orignm = Utils.strd(buf, off);
               final String tgtnm = Utils.strd(buf, off);
               return new Skeleton.BoneOffset.Command() {
                  @Override
                  public GLState make(Skeleton.Pose pose) {
                     Skeleton.Bone orig = pose.skel().bones.get(orignm);
                     Skeleton.Bone tgt = pose.skel().bones.get(tgtnm);
                     return pose.new BoneAlign(ref, orig, tgt);
                  }
               };
            }
         };
      }

      public interface Command {
         GLState make(Skeleton.Pose var1);
      }

      public interface HatingJava {
         Skeleton.BoneOffset.Command make(byte[] var1, int[] var2);
      }
   }

   public static class FxTrack {
      public final Skeleton.FxTrack.Event[] events;

      public FxTrack(Skeleton.FxTrack.Event[] events) {
         this.events = events;
      }

      public abstract static class Event {
         public final float time;

         public Event(float time) {
            this.time = time;
         }

         public abstract void trigger(Gob var1);
      }

      public static class SpawnSprite extends Skeleton.FxTrack.Event {
         public final Indir<Resource> res;
         public final byte[] sdt;
         public final Location loc;

         public SpawnSprite(float time, Indir<Resource> res, byte[] sdt, Location loc) {
            super(time);
            this.res = res;
            this.sdt = sdt == null ? new byte[0] : sdt;
            this.loc = loc;
         }

         @Override
         public void trigger(Gob gob) {
            final Coord3f fc;
            try {
               fc = gob.getc();
            } catch (Loading var4) {
               return;
            }

            OCache var10003 = gob.glob.oc;
            Objects.requireNonNull(gob.glob.oc);
            Gob n = new OCache.Virtual(var10003, gob.rc, gob.a) {
               {
                  Objects.requireNonNull(x0);
               }

               @Override
               public Coord3f getc() {
                  return new Coord3f(fc);
               }

               @Override
               public boolean setup(RenderList rl) {
                  if (SpawnSprite.this.loc != null) {
                     rl.prepc(SpawnSprite.this.loc);
                  }

                  return super.setup(rl);
               }
            };
            n.ols.add(new Gob.Overlay(-1, this.res, new Message(0, this.sdt)));
         }
      }
   }

   @Resource.PublishedCode(
      name = "pose"
   )
   public interface ModFactory {
      Skeleton.ModFactory def = new Skeleton.ModFactory() {
         @Override
         public Skeleton.PoseMod create(Skeleton skel, Skeleton.ModOwner owner, Resource res, Message sdt) {
            int mask = Sprite.decnum(sdt);
            Collection<Skeleton.PoseMod> poses = new LinkedList<>();

            for (Skeleton.ResPose p : res.layers(Skeleton.ResPose.class)) {
               if (p.id < 0 || (mask & 1 << p.id) != 0) {
                  poses.add(p.forskel(owner, skel, p.defmode));
               }
            }

            if (poses.size() == 0) {
               return skel.nilmod();
            } else {
               return poses.size() == 1 ? Utils.el(poses) : Skeleton.combine(poses.toArray(new Skeleton.PoseMod[0]));
            }
         }
      };

      Skeleton.PoseMod create(Skeleton var1, Skeleton.ModOwner var2, Resource var3, Message var4);
   }

   public interface ModOwner {
      Skeleton.ModOwner nil = new Skeleton.ModOwner() {
         @Override
         public double getv() {
            return 0.0;
         }

         @Override
         public Coord3f getc() {
            return Coord3f.o;
         }

         @Override
         public Glob glob() {
            throw new NullPointerException();
         }
      };

      double getv();

      Coord3f getc();

      Glob glob();
   }

   public class Pose {
      public float[][] lpos;
      public float[][] gpos;
      public float[][] lrot;
      public float[][] grot;
      private Skeleton.Pose from = null;
      public int seq = 0;
      public final Rendered debug = new Rendered() {
         @Override
         public void draw(GOut g) {
            GL2 gl = g.gl;
            g.st.put(Light.lighting, null);
            g.state(States.xray);
            g.apply();
            gl.glBegin(1);

            for (int i = 0; i < Skeleton.this.blist.length; i++) {
               if (Skeleton.this.blist[i].parent != null) {
                  int pi = Skeleton.this.blist[i].parent.idx;
                  gl.glColor3f(1.0F, 0.0F, 0.0F);
                  gl.glVertex3f(Pose.this.gpos[pi][0], Pose.this.gpos[pi][1], Pose.this.gpos[pi][2]);
                  gl.glColor3f(0.0F, 1.0F, 0.0F);
                  gl.glVertex3f(Pose.this.gpos[i][0], Pose.this.gpos[i][1], Pose.this.gpos[i][2]);
               }
            }

            gl.glEnd();
         }

         @Override
         public boolean setup(RenderList rl) {
            rl.prepo(States.xray);
            return true;
         }
      };

      private Pose() {
         int nb = Skeleton.this.blist.length;
         this.lpos = new float[nb][3];
         this.gpos = new float[nb][3];
         this.lrot = new float[nb][4];
         this.grot = new float[nb][4];
      }

      public Pose(Skeleton.Pose from) {
         this();
         this.from = from;
         this.reset();
         this.gbuild();
      }

      public Skeleton skel() {
         return Skeleton.this;
      }

      public void reset() {
         for (int i = 0; i < Skeleton.this.blist.length; i++) {
            Skeleton.vset(this.lpos[i], this.from.lpos[i]);
            Skeleton.qset(this.lrot[i], this.from.lrot[i]);
         }
      }

      public void gbuild() {
         int nb = Skeleton.this.blist.length;

         for (int i = 0; i < nb; i++) {
            Skeleton.Bone b = Skeleton.this.blist[i];
            if (b.parent == null) {
               this.gpos[i][0] = this.lpos[i][0];
               this.gpos[i][1] = this.lpos[i][1];
               this.gpos[i][2] = this.lpos[i][2];
               this.grot[i][0] = this.lrot[i][0];
               this.grot[i][1] = this.lrot[i][1];
               this.grot[i][2] = this.lrot[i][2];
               this.grot[i][3] = this.lrot[i][3];
            } else {
               int pi = b.parent.idx;
               Skeleton.qqmul(this.grot[i], this.grot[pi], this.lrot[i]);
               Skeleton.vqrot(this.gpos[i], this.lpos[i], this.grot[pi]);
               Skeleton.vvadd(this.gpos[i], this.gpos[i], this.gpos[pi]);
            }
         }

         this.seq++;
      }

      public void blend(Skeleton.Pose o, float d) {
         for (int i = 0; i < Skeleton.this.blist.length; i++) {
            Skeleton.qqslerp(this.lrot[i], this.lrot[i], o.lrot[i], d);
            this.lpos[i][0] = this.lpos[i][0] + (o.lpos[i][0] - this.lpos[i][0]) * d;
            this.lpos[i][1] = this.lpos[i][1] + (o.lpos[i][1] - this.lpos[i][1]) * d;
            this.lpos[i][2] = this.lpos[i][2] + (o.lpos[i][2] - this.lpos[i][2]) * d;
         }
      }

      public Location bonetrans(final int bone) {
         return new Location(Matrix4f.identity()) {
            private int cseq = -1;

            @Override
            public Matrix4f fin(Matrix4f p) {
               if (this.cseq != Pose.this.seq) {
                  Matrix4f xf = Transform.makexlate(new Matrix4f(), new Coord3f(Pose.this.gpos[bone][0], Pose.this.gpos[bone][1], Pose.this.gpos[bone][2]));
                  if (Pose.this.grot[bone][0] < 0.9999) {
                     float ang = (float)(Math.acos(Pose.this.grot[bone][0]) * 2.0);
                     xf = xf.mul1(
                        Transform.makerot(new Matrix4f(), new Coord3f(Pose.this.grot[bone][1], Pose.this.grot[bone][2], Pose.this.grot[bone][3]).norm(), ang)
                     );
                  }

                  this.update(xf);
                  this.cseq = Pose.this.seq;
               }

               return super.fin(p);
            }
         };
      }

      public Location bonetrans2(final int bone) {
         return new Location(Matrix4f.identity()) {
            private int cseq = -1;
            private float[] pos = new float[3];
            private float[] rot = new float[4];

            @Override
            public Matrix4f fin(Matrix4f p) {
               if (this.cseq != Pose.this.seq) {
                  this.rot = Skeleton.qqmul(this.rot, Pose.this.grot[bone], Skeleton.qinv(this.rot, Skeleton.this.bindpose.grot[bone]));
                  this.pos = Skeleton.vvadd(
                     this.pos, Pose.this.gpos[bone], Skeleton.vqrot(this.pos, Skeleton.vinv(this.pos, Skeleton.this.bindpose.gpos[bone]), this.rot)
                  );
                  Matrix4f xf = Transform.makexlate(new Matrix4f(), new Coord3f(this.pos[0], this.pos[1], this.pos[2]));
                  if (this.rot[0] < 0.999999) {
                     float ang = (float)(Math.acos(this.rot[0]) * 2.0);
                     xf = xf.mul1(Transform.makerot(new Matrix4f(), new Coord3f(this.rot[1], this.rot[2], this.rot[3]).norm(), ang));
                  }

                  this.update(xf);
                  this.cseq = Pose.this.seq;
               }

               return super.fin(p);
            }
         };
      }

      public void boneoff(int bone, float[] offtrans) {
         float[] rot = new float[4];
         float[] xlate = new float[3];
         rot = Skeleton.qqmul(rot, this.grot[bone], Skeleton.qinv(rot, Skeleton.this.bindpose.grot[bone]));
         xlate = Skeleton.vvadd(xlate, this.gpos[bone], Skeleton.vqrot(xlate, Skeleton.vinv(xlate, Skeleton.this.bindpose.gpos[bone]), rot));
         offtrans[3] = 0.0F;
         offtrans[7] = 0.0F;
         offtrans[11] = 0.0F;
         offtrans[15] = 1.0F;
         offtrans[12] = xlate[0];
         offtrans[13] = xlate[1];
         offtrans[14] = xlate[2];
         float w = -rot[0];
         float x = rot[1];
         float y = rot[2];
         float z = rot[3];
         float xw = x * w * 2.0F;
         float xx = x * x * 2.0F;
         float xy = x * y * 2.0F;
         float xz = x * z * 2.0F;
         float yw = y * w * 2.0F;
         float yy = y * y * 2.0F;
         float yz = y * z * 2.0F;
         float zw = z * w * 2.0F;
         float zz = z * z * 2.0F;
         offtrans[0] = 1.0F - (yy + zz);
         offtrans[5] = 1.0F - (xx + zz);
         offtrans[10] = 1.0F - (xx + yy);
         offtrans[1] = xy - zw;
         offtrans[2] = xz + yw;
         offtrans[4] = xy + zw;
         offtrans[6] = yz - xw;
         offtrans[8] = xz - yw;
         offtrans[9] = yz + xw;
      }

      public class BoneAlign extends Location {
         private final Coord3f ref;
         private final int orig;
         private final int tgt;
         private int cseq = -1;

         public BoneAlign(Coord3f ref, Skeleton.Bone orig, Skeleton.Bone tgt) {
            super(Matrix4f.identity());
            this.ref = ref;
            this.orig = orig.idx;
            this.tgt = tgt.idx;
         }

         @Override
         public Matrix4f fin(Matrix4f p) {
            if (this.cseq != Pose.this.seq) {
               Coord3f cur = new Coord3f(
                     Pose.this.gpos[this.tgt][0] - Pose.this.gpos[this.orig][0],
                     Pose.this.gpos[this.tgt][1] - Pose.this.gpos[this.orig][1],
                     Pose.this.gpos[this.tgt][2] - Pose.this.gpos[this.orig][2]
                  )
                  .norm();
               Coord3f axis = cur.cmul(this.ref).norm();
               float ang = (float)Math.acos(cur.dmul(this.ref));
               this.update(
                  Transform.makexlate(new Matrix4f(), new Coord3f(Pose.this.gpos[this.orig][0], Pose.this.gpos[this.orig][1], Pose.this.gpos[this.orig][2]))
                     .mul1(Transform.makerot(new Matrix4f(), axis, -ang))
               );
               this.cseq = Pose.this.seq;
            }

            return super.fin(p);
         }
      }
   }

   public abstract class PoseMod {
      public final Skeleton.ModOwner owner;
      public float[][] lpos;
      public float[][] lrot;

      public PoseMod(Skeleton.ModOwner owner) {
         this.owner = owner;
         int nb = Skeleton.this.blist.length;
         this.lpos = new float[nb][3];
         this.lrot = new float[nb][4];

         for (int i = 0; i < nb; i++) {
            this.lrot[i][0] = 1.0F;
         }
      }

      @Deprecated
      public PoseMod() {
         this(Skeleton.ModOwner.nil);
      }

      public Skeleton skel() {
         return Skeleton.this;
      }

      public void reset() {
         for (int i = 0; i < Skeleton.this.blist.length; i++) {
            this.lpos[i][0] = 0.0F;
            this.lpos[i][1] = 0.0F;
            this.lpos[i][2] = 0.0F;
            this.lrot[i][0] = 1.0F;
            this.lrot[i][1] = 0.0F;
            this.lrot[i][2] = 0.0F;
            this.lrot[i][3] = 0.0F;
         }
      }

      public void rot(int bone, float ang, float ax, float ay, float az) {
         float[] x = new float[]{ax, ay, az};
         Skeleton.qqmul(this.lrot[bone], this.lrot[bone], Skeleton.rotasq(new float[4], x, ang));
      }

      public void apply(Skeleton.Pose p) {
         for (int i = 0; i < Skeleton.this.blist.length; i++) {
            Skeleton.vvadd(p.lpos[i], p.lpos[i], this.lpos[i]);
            Skeleton.qqmul(p.lrot[i], p.lrot[i], this.lrot[i]);
         }
      }

      public boolean tick(float dt) {
         return false;
      }

      public abstract boolean stat();

      public abstract boolean done();
   }

   @Resource.LayerName("skel")
   public static class Res extends Resource.Layer {
      public final Skeleton s;

      public Res(Resource res, byte[] buf) {
         Objects.requireNonNull(res);
         super();
         Map<String, Skeleton.Bone> bones = new HashMap<>();
         Map<Skeleton.Bone, String> pm = new HashMap<>();
         int[] off = new int[]{0};

         while (off[0] < buf.length) {
            String bnm = Utils.strd(buf, off);
            Coord3f pos = new Coord3f((float)Utils.floatd(buf, off[0]), (float)Utils.floatd(buf, off[0] + 5), (float)Utils.floatd(buf, off[0] + 10));
            off[0] += 15;
            Coord3f rax = new Coord3f((float)Utils.floatd(buf, off[0]), (float)Utils.floatd(buf, off[0] + 5), (float)Utils.floatd(buf, off[0] + 10)).norm();
            off[0] += 15;
            float rang = (float)Utils.floatd(buf, off[0]);
            off[0] += 5;
            String bp = Utils.strd(buf, off);
            Skeleton.Bone b = new Skeleton.Bone(bnm, pos, rax, rang);
            if (bones.put(bnm, b) != null) {
               throw new RuntimeException("Duplicate bone name: " + b.name);
            }

            pm.put(b, bp);
         }

         for (Skeleton.Bone b : bones.values()) {
            String bp = pm.get(b);
            if (bp.length() == 0) {
               b.parent = null;
            } else if ((b.parent = bones.get(bp)) == null) {
               throw new Resource.LoadException("Parent bone " + bp + " not found for " + b.name, this.getres());
            }
         }

         this.s = new Skeleton(bones.values()) {
            @Override
            public String toString() {
               return "Skeleton(" + Res.this.getres().name + ")";
            }
         };
      }

      @Override
      public void init() {
      }
   }

   @Resource.LayerName("skan")
   public static class ResPose extends Resource.Layer implements Resource.IDLayer<Integer> {
      public final int id;
      public final float len;
      public final Skeleton.Track[] tracks;
      public final Skeleton.FxTrack[] effects;
      public final double nspeed;
      public final WrapMode defmode;

      private static Skeleton.Track.Frame[] parseframes(byte[] buf, int[] off) {
         Skeleton.Track.Frame[] frames = new Skeleton.Track.Frame[Utils.uint16d(buf, off[0])];
         off[0] += 2;

         for (int i = 0; i < frames.length; i++) {
            float tm = (float)Utils.floatd(buf, off[0]);
            off[0] += 5;
            float[] trans = new float[3];

            for (int o = 0; o < 3; o++) {
               trans[o] = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
            }

            float rang = (float)Utils.floatd(buf, off[0]);
            off[0] += 5;
            float[] rax = new float[3];

            for (int o = 0; o < 3; o++) {
               rax[o] = (float)Utils.floatd(buf, off[0]);
               off[0] += 5;
            }

            frames[i] = new Skeleton.Track.Frame(tm, trans, Skeleton.rotasq(new float[4], rax, rang));
         }

         return frames;
      }

      private Skeleton.FxTrack parsefx(byte[] buf, int[] off) {
         Skeleton.FxTrack.Event[] events = new Skeleton.FxTrack.Event[Utils.uint16d(buf, off[0])];
         off[0] += 2;
         int i = 0;

         while (i < events.length) {
            float tm = (float)Utils.floatd(buf, off[0]);
            off[0] += 5;
            int t = Utils.ub(buf[off[0]++]);
            switch (t) {
               case 0:
                  String resnm = Utils.strd(buf, off);
                  int resver = Utils.uint16d(buf, off[0]);
                  off[0] += 2;
                  byte[] sdt = new byte[Utils.ub(buf[off[0]++])];
                  System.arraycopy(buf, off[0], sdt, 0, sdt.length);
                  off[0] += sdt.length;
                  Indir<Resource> res = Resource.load(resnm, resver).indir();
                  events[i] = new Skeleton.FxTrack.SpawnSprite(tm, res, sdt, null);
                  i++;
                  break;
               default:
                  throw new Resource.LoadException("Illegal control event: " + t, this.getres());
            }
         }

         return new Skeleton.FxTrack(events);
      }

      public ResPose(Resource res, byte[] buf) {
         Objects.requireNonNull(res);
         super();
         this.id = Utils.int16d(buf, 0);
         int fl = buf[2];
         int mode = buf[3];
         if (mode == 0) {
            this.defmode = WrapMode.ONCE;
         } else if (mode == 1) {
            this.defmode = WrapMode.LOOP;
         } else if (mode == 2) {
            this.defmode = WrapMode.PONG;
         } else {
            if (mode != 3) {
               throw new Resource.LoadException("Illegal animation mode: " + mode, this.getres());
            }

            this.defmode = WrapMode.PONGLOOP;
         }

         this.len = (float)Utils.floatd(buf, 4);
         int[] off = new int[]{9};
         if ((fl & 1) != 0) {
            this.nspeed = Utils.floatd(buf, off[0]);
            off[0] += 5;
         } else {
            this.nspeed = -1.0;
         }

         Collection<Skeleton.Track> tracks = new LinkedList<>();
         Collection<Skeleton.FxTrack> fx = new LinkedList<>();

         while (off[0] < buf.length) {
            String bnm = Utils.strd(buf, off);
            if (bnm.equals("{ctl}")) {
               fx.add(this.parsefx(buf, off));
            } else {
               tracks.add(new Skeleton.Track(bnm, parseframes(buf, off)));
            }
         }

         this.tracks = tracks.toArray(new Skeleton.Track[0]);
         this.effects = fx.toArray(new Skeleton.FxTrack[0]);
      }

      public Skeleton.TrackMod forskel(Skeleton.ModOwner owner, Skeleton skel, WrapMode mode) {
         Skeleton.Track[] remap = new Skeleton.Track[skel.blist.length];

         for (Skeleton.Track t : this.tracks) {
            Skeleton.Bone b = skel.bones.get(t.bone);
            if (b == null) {
               throw new RuntimeException("Bone \"" + t.bone + "\" in animation reference does not exist in skeleton " + skel);
            }

            remap[b.idx] = t;
         }

         Skeleton.TrackMod ret = skel.new TrackMod(owner, remap, this.effects, this.len, mode);
         if (this.nspeed > 0.0) {
            ret.speedmod = true;
            ret.nspeed = this.nspeed;
         }

         return ret;
      }

      @Deprecated
      public Skeleton.TrackMod forskel(Skeleton skel, WrapMode mode) {
         return this.forskel(Skeleton.ModOwner.nil, skel, mode);
      }

      @Deprecated
      public Skeleton.TrackMod forgob(Skeleton skel, WrapMode mode, Gob gob) {
         return this.forskel(gob, skel, mode);
      }

      public Integer layerid() {
         return this.id;
      }

      @Override
      public void init() {
      }
   }

   public static class Track {
      public final String bone;
      public final Skeleton.Track.Frame[] frames;

      public Track(String bone, Skeleton.Track.Frame[] frames) {
         this.bone = bone;
         this.frames = frames;
      }

      public static class Frame {
         public final float time;
         public final float[] trans;
         public final float[] rot;

         public Frame(float time, float[] trans, float[] rot) {
            this.time = time;
            this.trans = trans;
            this.rot = rot;
         }
      }
   }

   public class TrackMod extends Skeleton.PoseMod {
      public final Skeleton.Track[] tracks;
      public final Skeleton.FxTrack[] effects;
      public final float len;
      public final WrapMode mode;
      private final boolean stat;
      private boolean done;
      public float time = 0.0F;
      private boolean speedmod = false;
      private double nspeed = 0.0;
      private boolean back = false;

      public TrackMod(Skeleton.ModOwner owner, Skeleton.Track[] tracks, Skeleton.FxTrack[] effects, float len, WrapMode mode) {
         super(owner);
         this.tracks = tracks;
         this.effects = effects;
         this.len = len;
         this.mode = mode;

         for (Skeleton.Track t : tracks) {
            if (t != null && t.frames.length > 1) {
               this.stat = false;
               this.aupdate(0.0F);
               return;
            }
         }

         this.stat = this.done = true;
         this.aupdate(0.0F);
      }

      @Deprecated
      public TrackMod(Skeleton.Track[] tracks, float len, WrapMode mode) {
         this(Skeleton.ModOwner.nil, tracks, new Skeleton.FxTrack[0], len, mode);
      }

      public void aupdate(float time) {
         if (time > this.len) {
            time = this.len;
         }

         this.reset();

         for (int i = 0; i < this.tracks.length; i++) {
            Skeleton.Track t = this.tracks[i];
            if (t != null && t.frames.length != 0) {
               if (t.frames.length == 1) {
                  Skeleton.qset(this.lrot[i], t.frames[0].rot);
                  Skeleton.vset(this.lpos[i], t.frames[0].trans);
               } else {
                  int l = 0;
                  int r = t.frames.length;

                  while (true) {
                     int c = l + (r - l >> 1);
                     float ct = t.frames[c].time;
                     float nt = c < t.frames.length - 1 ? t.frames[c + 1].time : this.len;
                     if (ct > time) {
                        r = c;
                     } else {
                        if (!(nt < time)) {
                           Skeleton.Track.Frame cf = t.frames[c];
                           Skeleton.Track.Frame nf = t.frames[(c + 1) % t.frames.length];
                           float d;
                           if (nt == ct) {
                              d = 0.0F;
                           } else {
                              d = (time - ct) / (nt - ct);
                           }

                           Skeleton.qqslerp(this.lrot[i], cf.rot, nf.rot, d);
                           this.lpos[i][0] = cf.trans[0] + (nf.trans[0] - cf.trans[0]) * d;
                           this.lpos[i][1] = cf.trans[1] + (nf.trans[1] - cf.trans[1]) * d;
                           this.lpos[i][2] = cf.trans[2] + (nf.trans[2] - cf.trans[2]) * d;
                           break;
                        }

                        l = c + 1;
                     }
                  }
               }
            }
         }
      }

      private void playfx(float ot, float nt) {
         if (this.owner instanceof Gob) {
            Gob gob = (Gob)this.owner;
            if (ot > nt) {
               this.playfx(Math.min(ot, this.len), this.len);
               this.playfx(0.0F, Math.max(0.0F, nt));
            } else {
               for (Skeleton.FxTrack t : this.effects) {
                  for (Skeleton.FxTrack.Event ev : t.events) {
                     if (ev.time >= ot && ev.time < nt) {
                        ev.trigger(gob);
                     }
                  }
               }
            }
         }
      }

      @Override
      public boolean tick(float dt) {
         if (this.speedmod) {
            dt = (float)(dt * (this.owner.getv() / this.nspeed));
         }

         float nt = this.time + (this.back ? -dt : dt);
         switch (this.mode) {
            case LOOP:
               nt %= this.len;
               break;
            case ONCE:
               if (nt > this.len) {
                  nt = this.len;
                  this.done = true;
               }
               break;
            case PONG:
               if (!this.back && nt > this.len) {
                  nt = this.len;
                  this.back = true;
               } else if (this.back && nt < 0.0F) {
                  nt = 0.0F;
                  this.done = true;
               }
               break;
            case PONGLOOP:
               if (!this.back && nt > this.len) {
                  nt = this.len;
                  this.back = true;
               } else if (this.back && nt < 0.0F) {
                  nt = 0.0F;
                  this.back = false;
               }
         }

         float ot = this.time;
         this.time = nt;
         if (!this.stat) {
            this.aupdate(this.time);
            if (!this.back) {
               this.playfx(ot, nt);
            } else {
               this.playfx(nt, ot);
            }

            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean stat() {
         return this.stat;
      }

      @Override
      public boolean done() {
         return this.done;
      }
   }
}
