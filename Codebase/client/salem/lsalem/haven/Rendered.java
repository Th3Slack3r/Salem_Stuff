package haven;

import java.awt.Color;
import javax.media.opengl.GL2;

public interface Rendered extends Drawn {
   GLState.Slot<Rendered.Order> order = new GLState.Slot<>(GLState.Slot.Type.GEOM, Rendered.Order.class, HavenPanel.global);
   Rendered.Order deflt = new Rendered.Order.Default(0);
   Rendered.Order first = new Rendered.Order.Default(Integer.MIN_VALUE);
   Rendered.Order last = new Rendered.Order.Default(Integer.MAX_VALUE);
   Rendered.Order postfx = new Rendered.Order.Default(5000);
   Rendered.Order postpfx = new Rendered.Order.Default(5500);
   Rendered.Order eyesort = new Rendered.Order.Default(10000) {
      private final Rendered.RComparator<Rendered> cmp = new Rendered.RComparator<Rendered>() {
         @Override
         public int compare(Rendered a, Rendered b, GLState.Buffer sa, GLState.Buffer sb) {
            Camera ca = sa.get(PView.cam);
            Location.Chain la = sa.get(PView.loc);
            Matrix4f mva = ca.fin(Matrix4f.id).mul(la.fin(Matrix4f.id));
            float da = (float)Math.sqrt(mva.m[12] * mva.m[12] + mva.m[13] * mva.m[13] + mva.m[14] * mva.m[14]);
            Camera cb = sb.get(PView.cam);
            Location.Chain lb = sb.get(PView.loc);
            Matrix4f mvb = cb.fin(Matrix4f.id).mul(lb.fin(Matrix4f.id));
            float db = (float)Math.sqrt(mvb.m[12] * mvb.m[12] + mvb.m[13] * mvb.m[13] + mvb.m[14] * mvb.m[14]);
            return da < db ? 1 : -1;
         }
      };

      @Override
      public Rendered.RComparator<Rendered> cmp() {
         return this.cmp;
      }
   };
   GLState.StandAlone skip = new GLState.StandAlone(GLState.Slot.Type.GEOM, HavenPanel.global) {
      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }
   };

   boolean setup(RenderList var1);

   public static class Axes implements Rendered {
      public final float[] mid;

      public Axes(Color mid) {
         this.mid = Utils.c2fa(mid);
      }

      public Axes() {
         this(Color.BLACK);
      }

      @Override
      public void draw(GOut g) {
         GL2 gl = g.gl;
         g.st.put(Light.lighting, null);
         g.state(States.xray);
         g.apply();
         gl.glBegin(1);
         gl.glColor4fv(this.mid, 0);
         gl.glVertex3f(0.0F, 0.0F, 0.0F);
         gl.glColor3f(1.0F, 0.0F, 0.0F);
         gl.glVertex3f(1.0F, 0.0F, 0.0F);
         gl.glColor4fv(this.mid, 0);
         gl.glVertex3f(0.0F, 0.0F, 0.0F);
         gl.glColor3f(0.0F, 1.0F, 0.0F);
         gl.glVertex3f(0.0F, 1.0F, 0.0F);
         gl.glColor4fv(this.mid, 0);
         gl.glVertex3f(0.0F, 0.0F, 0.0F);
         gl.glColor3f(0.0F, 0.0F, 1.0F);
         gl.glVertex3f(0.0F, 0.0F, 1.0F);
         gl.glEnd();
      }

      @Override
      public boolean setup(RenderList r) {
         r.state().put(States.color, null);
         return true;
      }
   }

   public static class Cube implements Rendered {
      @Override
      public void draw(GOut g) {
         GL2 gl = g.gl;
         g.apply();
         gl.glEnable(2903);
         gl.glBegin(7);
         gl.glNormal3f(0.0F, 0.0F, 1.0F);
         gl.glColor3f(0.0F, 0.0F, 1.0F);
         gl.glVertex3f(-1.0F, 1.0F, 1.0F);
         gl.glVertex3f(-1.0F, -1.0F, 1.0F);
         gl.glVertex3f(1.0F, -1.0F, 1.0F);
         gl.glVertex3f(1.0F, 1.0F, 1.0F);
         gl.glNormal3f(1.0F, 0.0F, 0.0F);
         gl.glColor3f(1.0F, 0.0F, 0.0F);
         gl.glVertex3f(1.0F, 1.0F, 1.0F);
         gl.glVertex3f(1.0F, -1.0F, 1.0F);
         gl.glVertex3f(1.0F, -1.0F, -1.0F);
         gl.glVertex3f(1.0F, 1.0F, -1.0F);
         gl.glNormal3f(-1.0F, 0.0F, 0.0F);
         gl.glColor3f(0.0F, 1.0F, 1.0F);
         gl.glVertex3f(-1.0F, 1.0F, 1.0F);
         gl.glVertex3f(-1.0F, 1.0F, -1.0F);
         gl.glVertex3f(-1.0F, -1.0F, -1.0F);
         gl.glVertex3f(-1.0F, -1.0F, 1.0F);
         gl.glNormal3f(0.0F, 1.0F, 0.0F);
         gl.glColor3f(0.0F, 1.0F, 0.0F);
         gl.glVertex3f(-1.0F, 1.0F, 1.0F);
         gl.glVertex3f(1.0F, 1.0F, 1.0F);
         gl.glVertex3f(1.0F, 1.0F, -1.0F);
         gl.glVertex3f(-1.0F, 1.0F, -1.0F);
         gl.glNormal3f(0.0F, -1.0F, 0.0F);
         gl.glColor3f(1.0F, 0.0F, 1.0F);
         gl.glVertex3f(-1.0F, -1.0F, 1.0F);
         gl.glVertex3f(-1.0F, -1.0F, -1.0F);
         gl.glVertex3f(1.0F, -1.0F, -1.0F);
         gl.glVertex3f(1.0F, -1.0F, 1.0F);
         gl.glNormal3f(0.0F, 0.0F, -1.0F);
         gl.glColor3f(1.0F, 1.0F, 0.0F);
         gl.glVertex3f(-1.0F, 1.0F, -1.0F);
         gl.glVertex3f(1.0F, 1.0F, -1.0F);
         gl.glVertex3f(1.0F, -1.0F, -1.0F);
         gl.glVertex3f(-1.0F, -1.0F, -1.0F);
         gl.glEnd();
         gl.glColor3f(1.0F, 1.0F, 1.0F);
         gl.glDisable(2903);
      }

      @Override
      public boolean setup(RenderList rls) {
         rls.state().put(States.color, null);
         return true;
      }
   }

   public static class Dot implements Rendered {
      @Override
      public void draw(GOut g) {
         GL2 gl = g.gl;
         g.st.put(Light.lighting, null);
         g.state(States.xray);
         g.apply();
         gl.glBegin(0);
         gl.glColor3f(1.0F, 0.0F, 0.0F);
         gl.glVertex3f(0.0F, 0.0F, 0.0F);
         gl.glEnd();
      }

      @Override
      public boolean setup(RenderList r) {
         return true;
      }
   }

   public static class Line implements Rendered {
      public final Coord3f end;

      public Line(Coord3f end) {
         this.end = end;
      }

      @Override
      public void draw(GOut g) {
         GL2 gl = g.gl;
         g.apply();
         gl.glBegin(1);
         gl.glColor3f(1.0F, 0.0F, 0.0F);
         gl.glVertex3f(0.0F, 0.0F, 0.0F);
         gl.glColor3f(0.0F, 1.0F, 0.0F);
         gl.glVertex3f(this.end.x, this.end.y, this.end.z);
         gl.glEnd();
      }

      @Override
      public boolean setup(RenderList r) {
         r.state().put(States.color, null);
         r.state().put(Light.lighting, null);
         return true;
      }
   }

   public abstract static class Order<T extends Rendered> extends GLState {
      public abstract int mainz();

      public abstract Rendered.RComparator<? super T> cmp();

      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(Rendered.order, this);
      }

      public static class Default extends Rendered.Order<Rendered> {
         private final int z;
         private Rendered.RComparator<Rendered> cmp = new Rendered.RComparator<Rendered>() {
            @Override
            public int compare(Rendered a, Rendered b, GLState.Buffer sa, GLState.Buffer sb) {
               return 0;
            }
         };

         public Default(int z) {
            this.z = z;
         }

         @Override
         public int mainz() {
            return this.z;
         }

         @Override
         public Rendered.RComparator<Rendered> cmp() {
            return this.cmp;
         }
      }
   }

   public interface RComparator<T extends Rendered> {
      int compare(T var1, T var2, GLState.Buffer var3, GLState.Buffer var4);
   }

   public static class ScreenQuad implements Rendered {
      private static final Projection proj = new Projection(Matrix4f.id);
      private static final VertexBuf.VertexArray pos = new VertexBuf.VertexArray(
         Utils.bufcp(new float[]{-1.0F, -1.0F, 0.0F, 1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F, -1.0F, 1.0F, 0.0F})
      );
      private static final VertexBuf.TexelArray tex = new VertexBuf.TexelArray(Utils.bufcp(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}));
      public static final GLState state = new GLState.Abstract() {
         @Override
         public void prep(GLState.Buffer buf) {
            Rendered.ScreenQuad.proj.prep(buf);
            States.ndepthtest.prep(buf);
            States.presdepth.prep(buf);
            buf.put(PView.cam, null);
            buf.put(PView.loc, null);
         }
      };

      @Override
      public void draw(GOut g) {
         GL2 gl = g.gl;
         g.apply();
         pos.bind(g, false);
         tex.bind(g, false);
         gl.glDrawArrays(7, 0, 4);
         pos.unbind(g);
         tex.unbind(g);
      }

      @Override
      public boolean setup(RenderList rls) {
         rls.prepo(state);
         return true;
      }
   }

   public static class TCube implements Rendered {
      public final Coord3f bn;
      public final Coord3f bp;
      public States.ColState sc = new States.ColState(new Color(255, 64, 64, 128));
      public States.ColState ec = new States.ColState(new Color(255, 255, 255, 255));

      public TCube(Coord3f bn, Coord3f bp) {
         this.bn = bn;
         this.bp = bp;
      }

      @Override
      public void draw(GOut g) {
         GL2 gl = g.gl;
         g.state(Light.deflight);
         g.state(this.sc);
         g.apply();
         gl.glEnable(2903);
         gl.glBegin(7);
         gl.glNormal3f(0.0F, 0.0F, 1.0F);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bp.z);
         gl.glNormal3f(1.0F, 0.0F, 0.0F);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bn.z);
         gl.glNormal3f(-1.0F, 0.0F, 0.0F);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bp.z);
         gl.glNormal3f(0.0F, 1.0F, 0.0F);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bn.z);
         gl.glNormal3f(0.0F, -1.0F, 0.0F);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bp.z);
         gl.glNormal3f(0.0F, 0.0F, -1.0F);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bn.z);
         gl.glEnd();
         gl.glDisable(2903);
         g.st.put(Light.lighting, null);
         g.state(this.ec);
         g.apply();
         gl.glLineWidth(1.2F);
         gl.glBegin(3);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bp.z);
         gl.glEnd();
         gl.glBegin(3);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bn.z);
         gl.glEnd();
         gl.glBegin(1);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bp.z);
         gl.glEnd();
         gl.glPointSize(5.0F);
         gl.glBegin(0);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bn.y, this.bp.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bp.x, this.bp.y, this.bp.z);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bn.z);
         gl.glVertex3f(this.bn.x, this.bp.y, this.bp.z);
         gl.glEnd();
      }

      @Override
      public boolean setup(RenderList rls) {
         rls.state().put(States.color, null);
         rls.prepo(eyesort);
         rls.prepo(States.presdepth);
         return true;
      }
   }
}
