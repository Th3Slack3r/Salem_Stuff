package haven;

import java.awt.Color;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public abstract class PView extends Widget {
   private RenderList rls;
   public static final GLState.Slot<PView.RenderContext> ctx = new GLState.Slot<>(GLState.Slot.Type.SYS, PView.RenderContext.class);
   public static final GLState.Slot<PView.RenderState> wnd = new GLState.Slot<>(
      GLState.Slot.Type.SYS, PView.RenderState.class, HavenPanel.proj2d, GLFrameBuffer.slot
   );
   public static final GLState.Slot<Projection> proj = new GLState.Slot<>(GLState.Slot.Type.SYS, Projection.class, wnd);
   public static final GLState.Slot<Camera> cam = new GLState.Slot<>(GLState.Slot.Type.SYS, Camera.class, proj);
   public static final GLState.Slot<Location.Chain> loc = new GLState.Slot<>(GLState.Slot.Type.GEOM, Location.Chain.class, cam);
   public Profile prof = new Profile(300);
   protected Light.Model lm;
   private final PView.WidgetContext cstate = new PView.WidgetContext();
   private final PView.WidgetRenderState rstate = new PView.WidgetRenderState();
   private GLState pstate;
   private final Rendered scene = new Rendered() {
      @Override
      public void draw(GOut g) {
      }

      @Override
      public boolean setup(RenderList rl) {
         PView.this.setup(rl);
         return false;
      }
   };

   public PView(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
      this.pstate = this.makeproj();
      this.lm = new Light.Model();
      this.lm.cc = 33274;
   }

   protected GLState.Buffer basic(GOut g) {
      GLState.Buffer buf = g.basicstate();
      this.cstate.prep(buf);
      this.rstate.prep(buf);
      if (this.pstate != null) {
         this.pstate.prep(buf);
      }

      this.camera().prep(buf);
      if (this.ui.audio != null) {
         this.ui.audio.prep(buf);
      }

      return buf;
   }

   protected abstract GLState camera();

   protected abstract void setup(RenderList var1);

   protected Projection makeproj() {
      float field = 0.5F;
      float aspect = (float)this.sz.y / this.sz.x;
      return Projection.frustum(-field, field, -aspect * field, aspect * field, 1.0F, 5000.0F);
   }

   @Override
   public void resize(Coord sz) {
      super.resize(sz);
      this.pstate = this.makeproj();
   }

   protected Color clearcolor() {
      return Color.BLACK;
   }

   @Override
   public void draw(GOut g) {
      if (g.sz.x >= 1 && g.sz.y >= 1) {
         if (this.rls == null || this.rls.cfg != g.gc) {
            this.rls = new RenderList(g.gc);
         }

         Profile.Frame curf = null;
         if (Config.profile) {
            curf = this.prof.new Frame();
         }

         GLState.Buffer bk = g.st.copy();
         GLState.Buffer def = this.basic(g);
         if (g.gc.pref.fsaa.val) {
            States.fsaa.prep(def);
         }

         try {
            this.lm.prep(def);
            new Light.LightList().prep(def);
            this.rls.setup(this.scene, def);
            if (curf != null) {
               curf.tick("setup");
            }

            this.rls.fin();
            if (curf != null) {
               curf.tick("sort");
            }

            GOut rg;
            if (this.cstate.cur.fb != null) {
               GLState.Buffer gb = g.basicstate();
               HavenPanel.OrthoState.fixed(this.cstate.cur.fb.sz()).prep(gb);
               this.cstate.cur.fb.prep(gb);
               this.cstate.cur.fb.prep(def);
               rg = new GOut(g.gl, g.ctx, g.gc, g.st, gb, this.cstate.cur.fb.sz());
            } else {
               rg = g;
            }

            rg.st.set(def);
            Color cc = this.clearcolor();
            if (cc == null && this.cstate.cur.fb != null) {
               cc = new Color(0, 0, 0, 0);
            }

            rg.apply();
            GL gl = rg.gl;
            if (cc == null) {
               gl.glClear(256);
            } else {
               gl.glClearColor(cc.getRed() / 255.0F, cc.getGreen() / 255.0F, cc.getBlue() / 255.0F, cc.getAlpha() / 255.0F);
               gl.glClear(256 | 16384);
            }

            if (curf != null) {
               curf.tick("cls");
            }

            g.st.time = 0L;
            this.rls.render(rg);
            if (this.cstate.cur.fb != null) {
               this.cstate.cur.resolve(g);
            }

            if (curf != null) {
               curf.add("apply", g.st.time);
               curf.tick("render", g.st.time);
            }
         } finally {
            g.st.set(bk);
         }

         for (RenderList.Slot s : this.rls.slots()) {
            if (s.r instanceof PView.Render2D) {
               ((PView.Render2D)s.r).draw2d(g);
            }
         }

         if (curf != null) {
            curf.tick("2d");
         }

         if (curf != null) {
            curf.fin();
         }
      }
   }

   public abstract static class ConfContext extends PView.RenderContext implements GLState.GlobalState {
      public FBConfig cfg = new FBConfig(this, this.sz());
      public FBConfig cur = new FBConfig(this, this.sz());
      private final GLState.Global glob = new GLState.Global() {
         @Override
         public void postsetup(RenderList rl) {
            ConfContext.this.cfg.fin(ConfContext.this.cur);
            ConfContext.this.cur = ConfContext.this.cfg;
            ConfContext.this.cfg = new FBConfig(ConfContext.this, ConfContext.this.sz());
            if (ConfContext.this.cur.fb != null) {
               for (RenderList.Slot s : rl.slots()) {
                  if (s.os.get(PView.ctx) == ConfContext.this) {
                     ConfContext.this.cur.state.prep(s.os);
                  }
               }
            }
         }

         @Override
         public void prerender(RenderList rl, GOut g) {
         }

         @Override
         public void postrender(RenderList rl, GOut g) {
         }
      };

      protected abstract Coord sz();

      @Override
      public GLState.Global global(RenderList rl, GLState.Buffer ctx) {
         return this.glob;
      }
   }

   public abstract static class Draw2D implements PView.Render2D {
      @Override
      public void draw(GOut g) {
      }

      @Override
      public boolean setup(RenderList r) {
         return false;
      }
   }

   public interface Render2D extends Rendered {
      void draw2d(GOut var1);
   }

   public static class RenderContext extends GLState.Abstract {
      private Map<PView.RenderContext.DataID, Object> data = new CacheMap<>(CacheMap.RefType.WEAK);

      public <T> T data(PView.RenderContext.DataID<T> id) {
         T ret = (T)this.data.get(id);
         if (ret == null) {
            this.data.put(id, ret = id.make(this));
         }

         return ret;
      }

      @Override
      public void prep(GLState.Buffer b) {
         b.put(PView.ctx, this);
      }

      public Glob glob() {
         return null;
      }

      public interface DataID<T> {
         T make(PView.RenderContext var1);
      }
   }

   public abstract static class RenderState extends GLState {
      @Override
      public void apply(GOut g) {
         GL2 gl = g.gl;
         gl.glScissor(g.ul.x, g.root().sz.y - g.ul.y - g.sz.y, g.sz.x, g.sz.y);
         Coord ul = this.ul();
         Coord sz = this.sz();
         gl.glViewport(ul.x, g.root().sz.y - ul.y - sz.y, sz.x, sz.y);
         gl.glAlphaFunc(516, 0.5F);
         gl.glEnable(2929);
         gl.glEnable(2884);
         gl.glEnable(3089);
         gl.glDepthFunc(515);
         gl.glClearDepth(1.0);
      }

      @Override
      public void unapply(GOut g) {
         GL gl = g.gl;
         gl.glDisable(2929);
         gl.glDisable(2884);
         gl.glDisable(3089);
         gl.glViewport(g.root().ul.x, g.root().ul.y, g.root().sz.x, g.root().sz.y);
         gl.glScissor(g.root().ul.x, g.root().ul.y, g.root().sz.x, g.root().sz.y);
      }

      @Override
      public void prep(GLState.Buffer b) {
         b.put(PView.wnd, this);
      }

      public abstract Coord ul();

      public abstract Coord sz();
   }

   public class WidgetContext extends PView.ConfContext {
      @Override
      protected Coord sz() {
         return PView.this.sz;
      }

      @Override
      public Glob glob() {
         return PView.this.ui.sess.glob;
      }

      public PView widget() {
         return PView.this;
      }
   }

   private class WidgetRenderState extends PView.RenderState {
      private WidgetRenderState() {
      }

      @Override
      public Coord ul() {
         return PView.this.rootpos();
      }

      @Override
      public Coord sz() {
         return PView.this.sz;
      }
   }
}
