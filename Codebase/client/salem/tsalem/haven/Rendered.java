package haven;

import java.awt.Color;
import javax.media.opengl.GL2;

public interface Rendered extends Drawn {
  public static final GLState.Slot<Order> order = new GLState.Slot<>(GLState.Slot.Type.GEOM, Order.class, new GLState.Slot[] { HavenPanel.global });
  
  public static abstract class Order<T extends Rendered> extends GLState {
    public abstract int mainz();
    
    public abstract Rendered.RComparator<? super T> cmp();
    
    public void apply(GOut g) {}
    
    public void unapply(GOut g) {}
    
    public void prep(GLState.Buffer buf) {
      buf.put(Rendered.order, this);
    }
    
    public static class Default extends Order<Rendered> {
      private final int z;
      
      private final Rendered.RComparator<Rendered> cmp;
      
      public Default(int z) {
        this.cmp = new Rendered.RComparator<Rendered>() {
            public int compare(Rendered a, Rendered b, GLState.Buffer sa, GLState.Buffer sb) {
              return 0;
            }
          };
        this.z = z;
      }
      
      public int mainz() {
        return this.z;
      }
      
      public Rendered.RComparator<Rendered> cmp() {
        return this.cmp;
      }
    }
  }
  
  public static class Default extends Order<Rendered> {
    private final int z;
    
    private final Rendered.RComparator<Rendered> cmp;
    
    public Default(int z) {
      this.cmp = new Rendered.RComparator<Rendered>() {
          public int compare(Rendered a, Rendered b, GLState.Buffer sa, GLState.Buffer sb) {
            return 0;
          }
        };
      this.z = z;
    }
    
    public int mainz() {
      return this.z;
    }
    
    public Rendered.RComparator<Rendered> cmp() {
      return this.cmp;
    }
  }
  
  public static final Order deflt = new Order.Default(0);
  
  public static final Order first = new Order.Default(-2147483648);
  
  public static final Order last = new Order.Default(2147483647);
  
  public static final Order postfx = new Order.Default(5000);
  
  public static final Order postpfx = new Order.Default(5500);
  
  public static final Order eyesort = new Order.Default(10000) {
      private final Rendered.RComparator<Rendered> cmp = new Rendered.RComparator<Rendered>() {
          public int compare(Rendered a, Rendered b, GLState.Buffer sa, GLState.Buffer sb) {
            Camera ca = sa.<Camera>get(PView.cam);
            Location.Chain la = sa.<Location.Chain>get(PView.loc);
            Matrix4f mva = ca.fin(Matrix4f.id).mul(la.fin(Matrix4f.id));
            float da = (float)Math.sqrt((mva.m[12] * mva.m[12] + mva.m[13] * mva.m[13] + mva.m[14] * mva.m[14]));
            Camera cb = sb.<Camera>get(PView.cam);
            Location.Chain lb = sb.<Location.Chain>get(PView.loc);
            Matrix4f mvb = cb.fin(Matrix4f.id).mul(lb.fin(Matrix4f.id));
            float db = (float)Math.sqrt((mvb.m[12] * mvb.m[12] + mvb.m[13] * mvb.m[13] + mvb.m[14] * mvb.m[14]));
            if (da == db)
              return 0; 
            if (da < db)
              return 1; 
            return -1;
          }
        };
      
      public Rendered.RComparator<Rendered> cmp() {
        return this.cmp;
      }
    };
  
  public static final GLState.StandAlone skip = new GLState.StandAlone(GLState.Slot.Type.GEOM, new GLState.Slot[] { HavenPanel.global }) {
      public void apply(GOut g) {}
      
      public void unapply(GOut g) {}
    };
  
  boolean setup(RenderList paramRenderList);
  
  public static class Dot implements Rendered {
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
    
    public boolean setup(RenderList r) {
      return true;
    }
  }
  
  public static class Axes implements Rendered {
    public final float[] mid;
    
    public Axes(Color mid) {
      this.mid = Utils.c2fa(mid);
    }
    
    public Axes() {
      this(Color.BLACK);
    }
    
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
    
    public boolean setup(RenderList r) {
      r.state().put(States.color, null);
      return true;
    }
  }
  
  public static class Line implements Rendered {
    public final Coord3f end;
    
    public Line(Coord3f end) {
      this.end = end;
    }
    
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
    
    public boolean setup(RenderList r) {
      r.state().put(States.color, null);
      r.state().put(Light.lighting, null);
      return true;
    }
  }
  
  public static class Cube implements Rendered {
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
    
    public boolean setup(RenderList rls) {
      rls.state().put(States.color, null);
      return true;
    }
  }
  
  public static class TCube implements Rendered {
    public final Coord3f bn;
    
    public final Coord3f bp;
    
    public States.ColState sc = new States.ColState(new Color(255, 64, 64, 128)), ec = new States.ColState(new Color(255, 255, 255, 255));
    
    public TCube(Coord3f bn, Coord3f bp) {
      this.bn = bn;
      this.bp = bp;
    }
    
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
    
    public boolean setup(RenderList rls) {
      rls.state().put(States.color, null);
      rls.prepo(Rendered.eyesort);
      rls.prepo(States.presdepth);
      return true;
    }
  }
  
  public static class ScreenQuad implements Rendered {
    private static final Projection proj = new Projection(Matrix4f.id);
    
    private static final VertexBuf.VertexArray pos = new VertexBuf.VertexArray(Utils.bufcp(new float[] { 
            -1.0F, -1.0F, 0.0F, 1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F, -1.0F, 
            1.0F, 0.0F }));
    
    private static final VertexBuf.TexelArray tex = new VertexBuf.TexelArray(Utils.bufcp(new float[] { 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F }));
    
    public static final GLState state = new GLState.Abstract() {
        public void prep(GLState.Buffer buf) {
          Rendered.ScreenQuad.proj.prep(buf);
          States.ndepthtest.prep(buf);
          States.presdepth.prep(buf);
          buf.put(PView.cam, null);
          buf.put(PView.loc, null);
        }
      };
    
    public void draw(GOut g) {
      GL2 gl = g.gl;
      g.apply();
      pos.bind(g, false);
      tex.bind(g, false);
      gl.glDrawArrays(7, 0, 4);
      pos.unbind(g);
      tex.unbind(g);
    }
    
    public boolean setup(RenderList rls) {
      rls.prepo(state);
      return true;
    }
  }
  
  public static interface RComparator<T extends Rendered> {
    int compare(T param1T1, T param1T2, GLState.Buffer param1Buffer1, GLState.Buffer param1Buffer2);
  }
}
