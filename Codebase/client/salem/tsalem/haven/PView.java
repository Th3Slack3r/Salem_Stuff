package haven;

import java.awt.Color;
import java.util.Map;
import javax.media.opengl.GL2;

public abstract class PView extends Widget {
  public RenderList rls;
  
  public static final GLState.Slot<RenderContext> ctx = new GLState.Slot<>(GLState.Slot.Type.SYS, RenderContext.class, new GLState.Slot[0]);
  
  public static final GLState.Slot<RenderState> wnd = new GLState.Slot<>(GLState.Slot.Type.SYS, RenderState.class, new GLState.Slot[] { HavenPanel.proj2d, GLFrameBuffer.slot });
  
  public static final GLState.Slot<Projection> proj = new GLState.Slot<>(GLState.Slot.Type.SYS, Projection.class, new GLState.Slot[] { wnd });
  
  public static final GLState.Slot<Camera> cam = new GLState.Slot<>(GLState.Slot.Type.SYS, Camera.class, new GLState.Slot[] { proj });
  
  public static final GLState.Slot<Location.Chain> loc = new GLState.Slot<>(GLState.Slot.Type.GEOM, Location.Chain.class, new GLState.Slot[] { cam });
  
  public Profile prof = new Profile(300);
  
  protected Light.Model lm;
  
  private final WidgetContext cstate = new WidgetContext();
  
  private final WidgetRenderState rstate = new WidgetRenderState();
  
  private GLState pstate;
  
  private final Rendered scene;
  
  public static class RenderContext extends GLState.Abstract {
    private final Map<DataID, Object> data = new CacheMap<>(CacheMap.RefType.WEAK);
    
    public <T> T data(DataID<T> id) {
      T ret = (T)this.data.get(id);
      if (ret == null)
        this.data.put(id, ret = id.make(this)); 
      return ret;
    }
    
    public void prep(GLState.Buffer b) {
      b.put(PView.ctx, this);
    }
    
    public Glob glob() {
      return null;
    }
    
    public static interface DataID<T> {
      T make(PView.RenderContext param2RenderContext);
    }
  }
  
  public static abstract class ConfContext extends RenderContext implements GLState.GlobalState {
    public FBConfig cfg = new FBConfig(this, sz());
    
    public FBConfig cur = new FBConfig(this, sz());
    
    protected abstract Coord sz();
    
    public GLState.Global global(RenderList rl, GLState.Buffer ctx) {
      return this.glob;
    }
    
    private final GLState.Global glob = new GLState.Global() {
        public void postsetup(RenderList rl) {
          PView.ConfContext.this.cfg.fin(PView.ConfContext.this.cur);
          PView.ConfContext.this.cur = PView.ConfContext.this.cfg;
          PView.ConfContext.this.cfg = new FBConfig(PView.ConfContext.this, PView.ConfContext.this.sz());
          if (PView.ConfContext.this.cur.fb != null)
            for (RenderList.Slot s : rl.slots()) {
              if (s.os.get(PView.ctx) == PView.ConfContext.this)
                PView.ConfContext.this.cur.state.prep(s.os); 
            }  
        }
        
        public void prerender(RenderList rl, GOut g) {}
        
        public void postrender(RenderList rl, GOut g) {}
      };
  }
  
  public class WidgetContext extends ConfContext {
    protected Coord sz() {
      return PView.this.sz;
    }
    
    public Glob glob() {
      return PView.this.ui.sess.glob;
    }
    
    public PView widget() {
      return PView.this;
    }
  }
  
  public static abstract class RenderState extends GLState {
    public void apply(GOut g) {
      GL2 gl = g.gl;
      gl.glScissor(g.ul.x, (g.root()).sz.y - g.ul.y - g.sz.y, g.sz.x, g.sz.y);
      Coord ul = ul();
      Coord sz = sz();
      gl.glViewport(ul.x, (g.root()).sz.y - ul.y - sz.y, sz.x, sz.y);
      gl.glAlphaFunc(516, 0.5F);
      gl.glEnable(2929);
      gl.glEnable(2884);
      gl.glEnable(3089);
      gl.glDepthFunc(515);
      gl.glClearDepth(1.0D);
    }
    
    public void unapply(GOut g) {
      GL2 gL2 = g.gl;
      gL2.glDisable(2929);
      gL2.glDisable(2884);
      gL2.glDisable(3089);
      gL2.glViewport((g.root()).ul.x, (g.root()).ul.y, (g.root()).sz.x, (g.root()).sz.y);
      gL2.glScissor((g.root()).ul.x, (g.root()).ul.y, (g.root()).sz.x, (g.root()).sz.y);
    }
    
    public void prep(GLState.Buffer b) {
      b.put(PView.wnd, this);
    }
    
    public abstract Coord ul();
    
    public abstract Coord sz();
  }
  
  private class WidgetRenderState extends RenderState {
    private WidgetRenderState() {}
    
    public Coord ul() {
      return PView.this.rootpos();
    }
    
    public Coord sz() {
      return PView.this.sz;
    }
  }
  
  public PView(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
    this.scene = new Rendered() {
        public void draw(GOut g) {}
        
        public boolean setup(RenderList rl) {
          PView.this.setup(rl);
          return false;
        }
      };
    this.pstate = makeproj();
    this.lm = new Light.Model();
    this.lm.cc = 33274;
  }
  
  protected GLState.Buffer basic(GOut g) {
    GLState.Buffer buf = g.basicstate();
    this.cstate.prep(buf);
    this.rstate.prep(buf);
    if (this.pstate != null)
      this.pstate.prep(buf); 
    camera().prep(buf);
    if (this.ui.audio != null)
      this.ui.audio.prep(buf); 
    return buf;
  }
  
  protected abstract GLState camera();
  
  protected abstract void setup(RenderList paramRenderList);
  
  protected Projection makeproj() {
    float field = 0.5F;
    float aspect = this.sz.y / this.sz.x;
    return Projection.frustum(-0.5F, 0.5F, -aspect * 0.5F, aspect * 0.5F, 1.0F, 5000.0F);
  }
  
  public void resize(Coord sz) {
    super.resize(sz);
    this.pstate = makeproj();
  }
  
  protected Color clearcolor() {
    return Color.BLACK;
  }
  
  public void draw(GOut g) {
    if (g.sz.x < 1 || g.sz.y < 1)
      return; 
    if (this.rls == null || this.rls.cfg != g.gc)
      this.rls = new RenderList(g.gc); 
    Profile.Frame curf = null;
    if (Config.profile) {
      this.prof.getClass();
      curf = new Profile.Frame(this.prof);
    } 
    GLState.Buffer bk = g.st.copy();
    GLState.Buffer def = basic(g);
    if (g.gc.pref.fsaa.val.booleanValue())
      States.fsaa.prep(def); 
    try {
      GOut rg;
      this.lm.prep(def);
      (new Light.LightList()).prep(def);
      this.rls.setup(this.scene, def);
      if (curf != null)
        curf.tick("setup"); 
      this.rls.fin();
      if (curf != null)
        curf.tick("sort"); 
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
      Color cc = clearcolor();
      if (cc == null && this.cstate.cur.fb != null)
        cc = new Color(0, 0, 0, 0); 
      rg.apply();
      GL2 gL2 = rg.gl;
      if (cc == null) {
        gL2.glClear(256);
      } else {
        gL2.glClearColor(cc.getRed() / 255.0F, cc.getGreen() / 255.0F, cc.getBlue() / 255.0F, cc.getAlpha() / 255.0F);
        gL2.glClear(16640);
      } 
      if (curf != null)
        curf.tick("cls"); 
      g.st.time = 0L;
      this.rls.render(rg);
      if (this.cstate.cur.fb != null)
        this.cstate.cur.resolve(g); 
      if (curf != null) {
        curf.add("apply", g.st.time);
        curf.tick("render", g.st.time);
      } 
    } finally {
      g.st.set(bk);
    } 
    render2d(g);
    g.st.set(def);
    g.apply();
    g.st.set(bk);
    if (curf != null)
      curf.tick("2d"); 
    if (curf != null)
      curf.fin(); 
  }
  
  protected void render2d(GOut g) {
    for (RenderList.Slot s : this.rls.slots()) {
      if (s.r instanceof Render2D)
        ((Render2D)s.r).draw2d(g); 
    } 
  }
  
  public static abstract class Draw2D implements Render2D {
    public void draw(GOut g) {}
    
    public boolean setup(RenderList r) {
      return false;
    }
  }
  
  public static interface Render2D extends Rendered {
    void draw2d(GOut param1GOut);
  }
}
