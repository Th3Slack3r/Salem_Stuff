package haven;

import haven.glsl.Phong;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class Light implements Rendered {
  public float[] amb;
  
  public float[] dif;
  
  public float[] spc;
  
  private static final float[] defamb = new float[] { 0.0F, 0.0F, 0.0F, 1.0F };
  
  private static final float[] defdif = new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
  
  private static final float[] defspc = new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
  
  public Light() {
    this.amb = defamb;
    this.dif = defdif;
    this.spc = defspc;
  }
  
  public Light(FColor col) {
    this.amb = defamb;
    this.dif = this.spc = col.to4a();
  }
  
  public Light(Color col) {
    this.amb = defamb;
    this.dif = this.spc = Utils.c2fa(col);
  }
  
  public Light(FColor amb, FColor dif, FColor spc) {
    this.amb = amb.to4a();
    this.dif = dif.to4a();
    this.spc = spc.to4a();
  }
  
  public Light(Color amb, Color dif, Color spc) {
    this.amb = Utils.c2fa(amb);
    this.dif = Utils.c2fa(dif);
    this.spc = Utils.c2fa(spc);
  }
  
  public void enable(GOut g, int idx) {
    GL2 gl = g.gl;
    gl.glEnable(16384 + idx);
    gl.glLightfv(16384 + idx, 4608, this.amb, 0);
    gl.glLightfv(16384 + idx, 4609, this.dif, 0);
    gl.glLightfv(16384 + idx, 4610, this.spc, 0);
  }
  
  public void disable(GOut g, int idx) {
    GL2 gl = g.gl;
    gl.glLightfv(16384 + idx, 4608, defamb, 0);
    gl.glLightfv(16384 + idx, 4609, defdif, 0);
    gl.glLightfv(16384 + idx, 4610, defspc, 0);
    gl.glDisable(16384 + idx);
  }
  
  public static final GLState.Slot<LightList> lights = new GLState.Slot<>(GLState.Slot.Type.DRAW, LightList.class, new GLState.Slot[] { PView.cam });
  
  public static final GLState.Slot<Model> model = new GLState.Slot<>(GLState.Slot.Type.DRAW, Model.class, new GLState.Slot[] { PView.proj });
  
  public static final GLState.Slot<GLState> lighting = new GLState.Slot<>(GLState.Slot.Type.DRAW, GLState.class, new GLState.Slot[] { model, lights });
  
  public static class BaseLights extends GLState {
    private final ShaderMacro[] shaders;
    
    public BaseLights(ShaderMacro[] shaders) {
      this.shaders = shaders;
    }
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      if (g.st.prog == null) {
        gl.glEnable(2896);
      } else {
        reapply(g);
      } 
    }
    
    public void reapply(GOut g) {
      GL2 gl = g.gl;
      gl.glUniform1i(g.st.prog.uniform(Phong.nlights), ((Light.LightList)g.st.get((GLState.Slot)Light.lights)).nlights);
    }
    
    public void unapply(GOut g) {
      GL2 gl = g.gl;
      if (!g.st.usedprog)
        gl.glDisable(2896); 
    }
    
    public ShaderMacro[] shaders() {
      return this.shaders;
    }
    
    public boolean reqshaders() {
      return true;
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(Light.lighting, this);
    }
  }
  
  private static final ShaderMacro vlight = new ShaderMacro() {
      public void modify(ProgramContext prog) {
        new Phong(prog.vctx);
      }
    };
  
  private static final ShaderMacro plight = new ShaderMacro() {
      public void modify(ProgramContext prog) {
        new Phong(prog.fctx);
      }
    };
  
  public static final GLState vlights = new BaseLights(new ShaderMacro[] { vlight }) {
      public boolean reqshaders() {
        return false;
      }
    };
  
  public static final GLState plights = new BaseLights(new ShaderMacro[] { plight });
  
  public static final GLState.StandAlone celshade = new GLState.StandAlone(GLState.Slot.Type.DRAW, new GLState.Slot[] { lighting }) {
      public void apply(GOut g) {}
      
      public void unapply(GOut g) {}
      
      private final ShaderMacro[] shaders = new ShaderMacro[] { (ShaderMacro)new Phong.CelShade() };
      
      public ShaderMacro[] shaders() {
        return this.shaders;
      }
      
      public boolean reqshaders() {
        return true;
      }
    };
  
  @ResName("cel")
  public static class $cel implements Material.ResCons {
    public GLState cons(Resource res, Object... args) {
      return Light.celshade;
    }
  }
  
  public static final GLState deflight = new GLState() {
      public void apply(GOut g) {}
      
      public void unapply(GOut g) {}
      
      public void prep(GLState.Buffer buf) {
        if (buf.cfg.pref.flight.val.booleanValue()) {
          Light.plights.prep(buf);
        } else {
          Light.vlights.prep(buf);
        } 
        if (buf.cfg.pref.cel.val.booleanValue())
          Light.celshade.prep(buf); 
      }
    };
  
  @ResName("light")
  public static class $light implements Material.ResCons {
    public GLState cons(Resource res, Object... args) {
      String nm = (String)args[0];
      if (nm.equals("def"))
        return Light.deflight; 
      if (nm.equals("pv"))
        return Light.vlights; 
      if (nm.equals("pp"))
        return Light.plights; 
      if (nm.equals("n"))
        return null; 
      throw new Resource.LoadException("Unknown lighting type: " + nm, res);
    }
  }
  
  public static class LightList extends GLState {
    public final List<Light> ll = new ArrayList<>();
    
    public final List<Matrix4f> vl = new ArrayList<>();
    
    private final List<Light> en = new ArrayList<>();
    
    public int nlights = 0;
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      int nl = this.ll.size();
      if (g.gc.maxlights < nl)
        nl = g.gc.maxlights; 
      this.en.clear();
      for (int i = 0; i < nl; i++) {
        Matrix4f mv = this.vl.get(i);
        Light l = this.ll.get(i);
        g.st.matmode(5888);
        gl.glLoadMatrixf(mv.m, 0);
        this.en.add(l);
        l.enable(g, i);
        GOut.checkerr((GL)gl);
      } 
      this.nlights = nl;
    }
    
    public void unapply(GOut g) {
      for (int i = 0; i < this.en.size(); i++) {
        ((Light)this.en.get(i)).disable(g, i);
        GOut.checkerr((GL)g.gl);
      } 
      this.nlights = 0;
    }
    
    public int capply() {
      return 1000;
    }
    
    public int cunapply() {
      return 1000;
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(Light.lights, this);
    }
    
    private void add(Light l, Matrix4f loc) {
      this.ll.add(l);
      this.vl.add(loc);
      if (this.ll.size() != this.vl.size())
        throw new RuntimeException(); 
    }
    
    public int index(Light l) {
      return this.ll.indexOf(l);
    }
  }
  
  public static class Model extends GLState {
    public float[] amb;
    
    public int cc = 33273;
    
    private static final float[] defamb = new float[] { 0.2F, 0.2F, 0.2F, 1.0F };
    
    public Model(Color amb) {
      this.amb = Utils.c2fa(amb);
    }
    
    public Model() {
      this(Color.BLACK);
    }
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      gl.glLightModelfv(2899, this.amb, 0);
      gl.glLightModeli(33272, this.cc);
    }
    
    public void unapply(GOut g) {
      GL2 gl = g.gl;
      gl.glLightModelfv(2899, defamb, 0);
      gl.glLightModeli(33272, 33273);
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(Light.model, this);
    }
  }
  
  public void draw(GOut g) {}
  
  public boolean setup(RenderList rl) {
    LightList l = rl.state().<LightList>get(lights);
    if (l != null) {
      Camera cam = rl.state().<Camera>get(PView.cam);
      Location.Chain loc = rl.state().<Location.Chain>get(PView.loc);
      Matrix4f mv = cam.fin(Matrix4f.identity());
      if (loc != null)
        mv = mv.mul(loc.fin(Matrix4f.identity())); 
      l.add(this, mv);
    } 
    return false;
  }
  
  @LayerName("light")
  public static class Res extends Resource.Layer {
    public final int id;
    
    public final Color amb;
    
    public final Color dif;
    
    public final Color spc;
    
    public boolean hatt;
    
    public boolean hexp;
    
    public float ac;
    
    public float al;
    
    public float aq;
    
    public float exp;
    
    public Coord3f dir;
    
    private static Color cold(byte[] buf, int[] off) {
      double r = Utils.floatd(buf, off[0]);
      off[0] = off[0] + 5;
      double g = Utils.floatd(buf, off[0]);
      off[0] = off[0] + 5;
      double b = Utils.floatd(buf, off[0]);
      off[0] = off[0] + 5;
      double a = Utils.floatd(buf, off[0]);
      off[0] = off[0] + 5;
      return new Color((int)(r * 255.0D), (int)(g * 255.0D), (int)(b * 255.0D), (int)(a * 255.0D));
    }
    
    public Res(Resource res, byte[] buf) {
      super(res);
      int[] off = { 0 };
      this.id = Utils.int16d(buf, off[0]);
      off[0] = off[0] + 2;
      this.amb = cold(buf, off);
      this.dif = cold(buf, off);
      this.spc = cold(buf, off);
      while (off[0] < buf.length) {
        int t = buf[off[0]];
        off[0] = off[0] + 1;
        if (t == 1) {
          this.hatt = true;
          this.ac = (float)Utils.floatd(buf, off[0]);
          off[0] = off[0] + 5;
          this.al = (float)Utils.floatd(buf, off[0]);
          off[0] = off[0] + 5;
          this.aq = (float)Utils.floatd(buf, off[0]);
          off[0] = off[0] + 5;
          continue;
        } 
        if (t == 2) {
          float x = (float)Utils.floatd(buf, off[0]);
          off[0] = off[0] + 5;
          float y = (float)Utils.floatd(buf, off[0]);
          off[0] = off[0] + 5;
          float z = (float)Utils.floatd(buf, off[0]);
          off[0] = off[0] + 5;
          this.dir = new Coord3f(x, y, z);
          continue;
        } 
        if (t == 3) {
          this.hexp = true;
          this.exp = (float)Utils.floatd(buf, off[0]);
          off[0] = off[0] + 5;
          continue;
        } 
        throw new Resource.LoadException("Unknown light data: " + t, getres());
      } 
    }
    
    public Light make() {
      if (this.hatt) {
        PosLight ret;
        if (this.hexp) {
          ret = new SpotLight(this.amb, this.dif, this.spc, Coord3f.o, this.dir, this.exp);
        } else {
          ret = new PosLight(this.amb, this.dif, this.spc, Coord3f.o);
        } 
        ret.att(this.ac, this.al, this.aq);
        return ret;
      } 
      return new DirLight(this.amb, this.dif, this.spc, this.dir);
    }
    
    public void init() {}
  }
}
