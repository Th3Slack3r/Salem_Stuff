package haven;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class Gob implements Sprite.Owner, Skeleton.ModOwner, Rendered {
   public Coord rc;
   public Coord sc;
   public Coord3f sczu;
   public double a;
   public boolean virtual = false;
   int clprio = 0;
   public long id;
   public int frame;
   public int initdelay = (int)(Math.random() * 3000.0) + 3000;
   public final Glob glob;
   Map<Class<? extends GAttrib>, GAttrib> attr = new HashMap<>();
   public Collection<Gob.Overlay> ols = new LinkedList<>();
   public final GLState olmod = new GLState() {
      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public void prep(GLState.Buffer buf) {
         for (Gob.Overlay ol : Gob.this.ols) {
            if (ol.spr instanceof Gob.Overlay.SetupMod) {
               ((Gob.Overlay.SetupMod)ol.spr).setupgob(buf);
            }
         }
      }
   };
   public static final GLState.Slot<Gob.Save> savepos = new GLState.Slot<>(GLState.Slot.Type.SYS, Gob.Save.class, PView.loc);
   public final Gob.Save save = new Gob.Save();
   public final Gob.GobLocation loc = new Gob.GobLocation();

   public Gob(Glob glob, Coord c, long id, int frame) {
      this.glob = glob;
      this.rc = c;
      this.id = id;
      this.frame = frame;
   }

   public Gob(Glob glob, Coord c) {
      this(glob, c, -1L, 0);
   }

   public void ctick(int dt) {
      int dt2 = dt + this.initdelay;
      this.initdelay = 0;

      for (GAttrib a : this.attr.values()) {
         if (a instanceof Drawable) {
            a.ctick(dt2);
         } else {
            a.ctick(dt);
         }
      }

      Iterator<Gob.Overlay> i = this.ols.iterator();

      while (i.hasNext()) {
         Gob.Overlay ol = i.next();
         if (ol.spr == null) {
            try {
               ol.sdt.off = 0;
               ol.spr = Sprite.create(this, ol.res.get(), ol.sdt);
            } catch (Loading var6) {
            }
         } else {
            boolean done = ol.spr.tick(dt);
            if ((!ol.delign || ol.spr instanceof Gob.Overlay.CDel) && done) {
               i.remove();
            }
         }
      }

      if (this.virtual && this.ols.isEmpty()) {
         this.glob.oc.remove(this.id);
      }

      this.loc.tick();
   }

   public Gob.Overlay findol(int id) {
      for (Gob.Overlay ol : this.ols) {
         if (ol.id == id) {
            return ol;
         }
      }

      return null;
   }

   public void tick() {
      for (GAttrib a : this.attr.values()) {
         a.tick();
      }
   }

   public void dispose() {
      for (GAttrib a : this.attr.values()) {
         a.dispose();
      }
   }

   public void move(Coord c, double a) {
      Moving m = this.getattr(Moving.class);
      if (m != null) {
         m.move(c);
      }

      this.rc = c;
      this.a = a;
   }

   @Override
   public Coord3f getc() {
      Moving m = this.getattr(Moving.class);
      Coord3f ret = m != null ? m.getc() : this.getrc();
      DrawOffset df = this.getattr(DrawOffset.class);
      if (df != null) {
         ret = ret.add(df.off);
      }

      return ret;
   }

   public Coord3f getrc() {
      return new Coord3f(this.rc.x, this.rc.y, this.glob.map.getcz(this.rc));
   }

   private Class<? extends GAttrib> attrclass(Class<? extends GAttrib> cl) {
      while (true) {
         Class<?> p = cl.getSuperclass();
         if (p == GAttrib.class) {
            return cl;
         }

         cl = p.asSubclass(GAttrib.class);
      }
   }

   public void setattr(GAttrib a) {
      Class<? extends GAttrib> ac = this.attrclass((Class<? extends GAttrib>)a.getClass());
      this.attr.put(ac, a);
   }

   public <C extends GAttrib> C getattr(Class<C> c) {
      GAttrib attr = this.attr.get(this.attrclass(c));
      return !c.isInstance(attr) ? null : c.cast(attr);
   }

   public void delattr(Class<? extends GAttrib> c) {
      this.attr.remove(this.attrclass(c));
   }

   @Override
   public void draw(GOut g) {
   }

   @Override
   public boolean setup(RenderList rl) {
      for (Gob.Overlay ol : this.ols) {
         rl.add(ol, null);
      }

      for (Gob.Overlay ol : this.ols) {
         if (ol.spr instanceof Gob.Overlay.SetupMod) {
            ((Gob.Overlay.SetupMod)ol.spr).setupmain(rl);
         }
      }

      GobHealth hlt = this.getattr(GobHealth.class);
      if (hlt != null) {
         rl.prepc(hlt.getfx());
      }

      Drawable d = this.getattr(Drawable.class);
      if (d != null) {
         d.setup(rl);
      }

      Speaking sp = this.getattr(Speaking.class);
      if (sp != null) {
         rl.add(sp.fx, null);
      }

      KinInfo ki = this.getattr(KinInfo.class);
      if (ki != null) {
         rl.add(ki.fx, null);
      }

      return false;
   }

   @Override
   public Random mkrandoom() {
      return new Random(this.id);
   }

   @Override
   public Resource.Neg getneg() {
      Drawable d = this.getattr(Drawable.class);
      return d != null ? d.getneg() : null;
   }

   @Override
   public Glob glob() {
      return this.glob;
   }

   @Override
   public double getv() {
      Moving m = this.getattr(Moving.class);
      return m == null ? 0.0 : m.getv();
   }

   public interface ANotif<T extends GAttrib> {
      void ch(T var1);
   }

   public class GobLocation extends Location {
      private Coord3f c = null;
      private double a = 0.0;
      private Matrix4f update = null;

      public GobLocation() {
         super(Matrix4f.id);
      }

      public void tick() {
         try {
            Coord3f c = Gob.this.getc();
            c.y = -c.y;
            if (this.c == null || !c.equals(this.c) || this.a != Gob.this.a) {
               this.update(makexlate(new Matrix4f(), this.c = c).mul1(makerot(new Matrix4f(), Coord3f.zu, (float)(-(this.a = Gob.this.a)))));
            }
         } catch (Loading var2) {
         }
      }

      public Location freeze() {
         return new Location(this.fin(Matrix4f.id));
      }
   }

   public static class Overlay implements Rendered {
      public Indir<Resource> res;
      public Message sdt;
      public Sprite spr;
      public int id;
      public boolean delign = false;

      public Overlay(int id, Indir<Resource> res, Message sdt) {
         this.id = id;
         this.res = res;
         this.sdt = sdt;
         this.spr = null;
      }

      public Overlay(Sprite spr) {
         this.id = -1;
         this.res = null;
         this.sdt = null;
         this.spr = spr;
      }

      @Override
      public void draw(GOut g) {
      }

      @Override
      public boolean setup(RenderList rl) {
         if (this.spr != null) {
            rl.add(this.spr, null);
         }

         return false;
      }

      public interface CDel {
         void delete();
      }

      public interface CUpd {
         void update(Message var1);
      }

      public interface SetupMod {
         void setupgob(GLState.Buffer var1);

         void setupmain(RenderList var1);
      }
   }

   public class Save extends GLState {
      public Matrix4f cam = new Matrix4f();
      public Matrix4f wxf = new Matrix4f();
      public Matrix4f mv = new Matrix4f();
      public Projection proj = null;

      @Override
      public void apply(GOut g) {
         this.mv.load(this.cam.load(g.st.cam)).mul1(this.wxf.load(g.st.wxf));
         Projection proj = g.st.cur(PView.proj);
         Coord3f s = proj.toscreen(this.mv.mul4(Coord3f.o), g.sz);
         Gob.this.sc = new Coord(s);
         Gob.this.sczu = proj.toscreen(this.mv.mul4(Coord3f.zu), g.sz).sub(s);
         this.proj = proj;
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(Gob.savepos, this);
      }
   }
}
