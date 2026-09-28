package haven;

import dolda.jglob.Discoverable;
import dolda.jglob.Loader;
import java.awt.Color;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.media.opengl.GL2;

public class Material extends GLState {
  public final GLState[] states;
  
  public static final GLState nofacecull = new GLState.StandAlone(GLState.Slot.Type.GEOM, new GLState.Slot[] { PView.proj }) {
      public void apply(GOut g) {
        g.gl.glDisable(2884);
      }
      
      public void unapply(GOut g) {
        g.gl.glEnable(2884);
      }
    };
  
  @ResName("nofacecull")
  public static class $nofacecull implements ResCons {
    public GLState cons(Resource res, Object... args) {
      return Material.nofacecull;
    }
  }
  
  public static final float[] defamb = new float[] { 0.2F, 0.2F, 0.2F, 1.0F };
  
  public static final float[] defdif = new float[] { 0.8F, 0.8F, 0.8F, 1.0F };
  
  public static final float[] defspc = new float[] { 0.0F, 0.0F, 0.0F, 1.0F };
  
  public static final float[] defemi = new float[] { 0.0F, 0.0F, 0.0F, 1.0F };
  
  public static final GLState.Slot<Colors> colors = new GLState.Slot<>(GLState.Slot.Type.DRAW, Colors.class, new GLState.Slot[0]);
  
  @ResName("col")
  public static class Colors extends GLState {
    public float[] amb;
    
    public float[] dif;
    
    public float[] spc;
    
    public float[] emi;
    
    public float shine;
    
    public Colors() {
      this.amb = Material.defamb;
      this.dif = Material.defdif;
      this.spc = Material.defspc;
      this.emi = Material.defemi;
    }
    
    private Colors(float[] amb, float[] dif, float[] spc, float[] emi, float shine) {
      this.amb = amb;
      this.dif = dif;
      this.spc = spc;
      this.emi = emi;
      this.shine = shine;
    }
    
    private static float[] colmul(float[] c1, float[] c2) {
      return new float[] { c1[0] * c2[0], c1[1] * c2[1], c1[2] * c2[2], c1[3] * c2[3] };
    }
    
    private static float[] colblend(float[] in, float[] bl) {
      float f1 = bl[3], f2 = 1.0F - f1;
      return new float[] { in[0] * f2 + bl[0] * f1, in[1] * f2 + bl[1] * f1, in[2] * f2 + bl[2] * f1, in[3] };
    }
    
    public Colors(Color amb, Color dif, Color spc, Color emi, float shine) {
      this(Utils.c2fa(amb), Utils.c2fa(dif), Utils.c2fa(spc), Utils.c2fa(emi), shine);
    }
    
    public Colors(Color amb, Color dif, Color spc, Color emi) {
      this(amb, dif, spc, emi, 0.0F);
    }
    
    public Colors(Color col) {
      this(new Color((int)(col.getRed() * Material.defamb[0]), (int)(col.getGreen() * Material.defamb[1]), (int)(col.getBlue() * Material.defamb[2]), col.getAlpha()), new Color(
            (int)(col.getRed() * Material.defdif[0]), (int)(col.getGreen() * Material.defdif[1]), (int)(col.getBlue() * Material.defdif[2]), col.getAlpha()), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), 0.0F);
    }
    
    public Colors(Resource res, Object... args) {
      this((Color)args[0], (Color)args[1], (Color)args[2], (Color)args[3], ((Float)args[4]).floatValue());
    }
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      gl.glMaterialfv(1032, 4608, this.amb, 0);
      gl.glMaterialfv(1032, 4609, this.dif, 0);
      gl.glMaterialfv(1032, 4610, this.spc, 0);
      gl.glMaterialfv(1032, 5632, this.emi, 0);
      gl.glMaterialf(1032, 5633, this.shine);
    }
    
    public void unapply(GOut g) {
      GL2 gl = g.gl;
      gl.glMaterialfv(1032, 4608, Material.defamb, 0);
      gl.glMaterialfv(1032, 4609, Material.defdif, 0);
      gl.glMaterialfv(1032, 4610, Material.defspc, 0);
      gl.glMaterialfv(1032, 5632, Material.defemi, 0);
      gl.glMaterialf(1032, 5633, 0.0F);
    }
    
    public int capplyfrom(GLState from) {
      if (from instanceof Colors)
        return 5; 
      return -1;
    }
    
    public void applyfrom(GOut g, GLState from) {
      if (from instanceof Colors)
        apply(g); 
    }
    
    public void prep(GLState.Buffer buf) {
      Colors p = buf.<Colors>get(Material.colors);
      if (p != null) {
        buf.put(Material.colors, p.combine(this));
      } else {
        buf.put(Material.colors, this);
      } 
    }
    
    public Colors combine(Colors other) {
      return new Colors(colblend(other.amb, this.amb), colblend(other.dif, this.dif), colblend(other.spc, this.spc), colblend(other.emi, this.emi), other.shine);
    }
    
    public String toString() {
      return String.format("(%.1f, %.1f, %.1f), (%.1f, %.1f, %.1f), (%.1f, %.1f, %.1f @ %.1f)", new Object[] { Float.valueOf(this.amb[0]), Float.valueOf(this.amb[1]), Float.valueOf(this.amb[2]), Float.valueOf(this.dif[0]), Float.valueOf(this.dif[1]), Float.valueOf(this.dif[2]), Float.valueOf(this.spc[0]), Float.valueOf(this.spc[1]), Float.valueOf(this.spc[2]), Float.valueOf(this.shine) });
    }
  }
  
  @ResName("vcol")
  public static class $vcol implements ResCons {
    public GLState cons(Resource res, Object... args) {
      return new States.ColState((Color)args[0]);
    }
  }
  
  @ResName("order")
  public static class $order implements ResCons {
    public GLState cons(Resource res, Object... args) {
      String nm = (String)args[0];
      if (nm.equals("first"))
        return Rendered.first; 
      if (nm.equals("last"))
        return Rendered.last; 
      if (nm.equals("pfx"))
        return Rendered.postpfx; 
      if (nm.equals("eye"))
        return Rendered.eyesort; 
      throw new Resource.LoadException("Unknown draw order: " + nm, res);
    }
  }
  
  public void apply(GOut g) {}
  
  public void unapply(GOut g) {}
  
  public Material(GLState... states) {
    this.states = states;
  }
  
  public Material() {
    this(new GLState[] { Light.deflight, new Colors() });
  }
  
  public Material(Color amb, Color dif, Color spc, Color emi, float shine) {
    this(new GLState[] { Light.deflight, new Colors(amb, dif, spc, emi, shine) });
  }
  
  public Material(Color col) {
    this(new GLState[] { Light.deflight, new Colors(col) });
  }
  
  public Material(Tex tex) {
    this(new GLState[] { Light.deflight, new Colors(), tex.draw(), tex.clip() });
  }
  
  public Material(Tex tex, boolean bright) {
    this(new GLState[] { null, new Colors(defamb, defdif, defspc, bright ? new float[4] : defemi, 0.0F, null), tex.draw(), tex.clip() });
  }
  
  public String toString() {
    return Arrays.<GLState>asList(this.states).toString();
  }
  
  public void prep(GLState.Buffer buf) {
    for (GLState st : this.states)
      st.prep(buf); 
  }
  
  public static class Res extends Resource.Layer implements Resource.IDLayer<Integer> {
    public final int id;
    
    private transient List<GLState> states = new LinkedList<>();
    
    private transient List<Resolver> left = new LinkedList<>();
    
    private transient Material m;
    
    private boolean mipmap = false, linear = false;
    
    public Res(Resource res, int id) {
      super(res);
      this.id = id;
    }
    
    public Material get() {
      synchronized (this) {
        if (this.m == null) {
          for (Iterator<Resolver> i = this.left.iterator(); i.hasNext(); ) {
            Resolver r = i.next();
            r.resolve(this.states);
            i.remove();
          } 
          this.m = new Material(this.states.<GLState>toArray(new GLState[0])) {
              public String toString() {
                return super.toString() + "@" + (Material.Res.this.getres()).name;
              }
            };
        } 
        return this.m;
      } 
    }
    
    public static interface Resolver {
      void resolve(Collection<GLState> param2Collection);
    }
    
    public void init() {
      for (Resource.Image img : getres().<Resource.Image>layers(Resource.imgc, false)) {
        TexGL tex = (TexGL)img.tex();
        if (this.mipmap)
          tex.mipmap(); 
        if (this.linear)
          tex.magfilter(9729); 
      } 
    }
    
    public Integer layerid() {
      return Integer.valueOf(this.id);
    }
  }
  
  @LayerName("mat")
  public static class OldMat implements Resource.LayerFactory<Res> {
    private static Color col(byte[] buf, int[] off) {
      double r = Utils.floatd(buf, off[0]);
      off[0] = off[0] + 5;
      double g = Utils.floatd(buf, off[0]);
      off[0] = off[0] + 5;
      double b = Utils.floatd(buf, off[0]);
      off[0] = off[0] + 5;
      double a = Utils.floatd(buf, off[0]);
      off[0] = off[0] + 5;
      return new Color((float)r, (float)g, (float)b, (float)a);
    }
    
    public Material.Res cons(final Resource res, byte[] buf) {
      int id = Utils.uint16d(buf, 0);
      Material.Res ret = new Material.Res(res, id);
      int[] off = { 2 };
      GLState light = Light.deflight;
      while (off[0] < buf.length) {
        String thing = Utils.strd(buf, off).intern();
        if (thing == "col") {
          Color amb = col(buf, off);
          Color dif = col(buf, off);
          Color spc = col(buf, off);
          double shine = Utils.floatd(buf, off[0]);
          off[0] = off[0] + 5;
          Color emi = col(buf, off);
          ret.states.add(new Material.Colors(amb, dif, spc, emi, (float)shine));
          continue;
        } 
        if (thing == "linear") {
          ret.linear = true;
          continue;
        } 
        if (thing == "mipmap") {
          ret.mipmap = true;
          continue;
        } 
        if (thing == "nofacecull") {
          ret.states.add(Material.nofacecull);
          continue;
        } 
        if (thing == "tex") {
          final int tid = Utils.uint16d(buf, off[0]);
          off[0] = off[0] + 2;
          ret.left.add(new Material.Res.Resolver() {
                public void resolve(Collection<GLState> buf) {
                  for (Resource.Image img : res.<Resource.Image>layers(Resource.imgc)) {
                    if (img.id == tid) {
                      buf.add(img.tex().draw());
                      buf.add(img.tex().clip());
                      return;
                    } 
                  } 
                  throw new RuntimeException(String.format("Specified texture %d not found in %s", new Object[] { Integer.valueOf(this.val$tid), this.val$res }));
                }
              });
          continue;
        } 
        if (thing == "texlink") {
          final String nm = Utils.strd(buf, off);
          final int ver = Utils.uint16d(buf, off[0]);
          off[0] = off[0] + 2;
          final int tid = Utils.uint16d(buf, off[0]);
          off[0] = off[0] + 2;
          ret.left.add(new Material.Res.Resolver() {
                public void resolve(Collection<GLState> buf) {
                  Resource tres = Resource.load(nm, ver);
                  for (Resource.Image img : tres.<Resource.Image>layers(Resource.imgc)) {
                    if (img.id == tid) {
                      buf.add(img.tex().draw());
                      buf.add(img.tex().clip());
                      return;
                    } 
                  } 
                  throw new RuntimeException(String.format("Specified texture %d for %s not found in %s", new Object[] { Integer.valueOf(this.val$tid), this.val$res, tres }));
                }
              });
          continue;
        } 
        if (thing == "light") {
          String l = Utils.strd(buf, off);
          if (l.equals("pv")) {
            light = Light.vlights;
            continue;
          } 
          if (l.equals("pp")) {
            light = Light.plights;
            continue;
          } 
          if (l.equals("n")) {
            light = null;
            continue;
          } 
          throw new Resource.LoadException("Unknown lighting type: " + thing, res);
        } 
        throw new Resource.LoadException("Unknown material part: " + thing, res);
      } 
      if (light != null)
        ret.states.add(light); 
      return ret;
    }
  }
  
  private static final Map<String, ResCons2> rnames = new TreeMap<>();
  
  static {
    for (Class<?> cl : (Iterable<Class<?>>)Loader.get(ResName.class).classes()) {
      String nm = ((ResName)cl.<ResName>getAnnotation(ResName.class)).value();
      if (ResCons.class.isAssignableFrom(cl)) {
        final ResCons scons;
        try {
          scons = cl.<ResCons>asSubclass(ResCons.class).newInstance();
        } catch (InstantiationException e) {
          throw new Error(e);
        } catch (IllegalAccessException e) {
          throw new Error(e);
        } 
        rnames.put(nm, new ResCons2() {
              public void cons(Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
                GLState ret = scons.cons(res, args);
                if (ret != null)
                  states.add(ret); 
              }
            });
        continue;
      } 
      if (ResCons2.class.isAssignableFrom(cl)) {
        try {
          rnames.put(nm, cl.<ResCons2>asSubclass(ResCons2.class).newInstance());
        } catch (InstantiationException e) {
          final ResCons scons;
          throw new Error(scons);
        } catch (IllegalAccessException e) {
          throw new Error(e);
        } 
        continue;
      } 
      if (GLState.class.isAssignableFrom(cl)) {
        final Constructor<? extends GLState> cons;
        try {
          cons = cl.<GLState>asSubclass(GLState.class).getConstructor(new Class[] { Resource.class, Object[].class });
        } catch (NoSuchMethodException e) {
          throw new Error("No proper constructor for res-consable GL state " + cl.getName(), e);
        } 
        rnames.put(nm, new ResCons2() {
              public void cons(Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
                states.add(Utils.construct(cons, new Object[] { res, args }));
              }
            });
        continue;
      } 
      throw new Error("Illegal material constructor class: " + cl);
    } 
  }
  
  @LayerName("mat2")
  public static class NewMat implements Resource.LayerFactory<Res> {
    public Material.Res cons(Resource res, byte[] bbuf) {
      Message buf = new Message(0, bbuf);
      int id = buf.uint16();
      Material.Res ret = new Material.Res(res, id);
      while (!buf.eom()) {
        String nm = buf.string();
        Object[] args = buf.list();
        if (nm.equals("linear")) {
          ret.linear = true;
          continue;
        } 
        if (nm.equals("mipmap")) {
          ret.mipmap = true;
          continue;
        } 
        Material.ResCons2 cons = (Material.ResCons2)Material.rnames.get(nm);
        if (cons == null)
          throw new Resource.LoadException("Unknown material part name: " + nm, res); 
        cons.cons(res, ret.states, ret.left, args);
      } 
      return ret;
    }
  }
  
  public static interface ResCons2 {
    void cons(Resource param1Resource, List<GLState> param1List, List<Material.Res.Resolver> param1List1, Object... param1VarArgs);
  }
  
  public static interface ResCons {
    GLState cons(Resource param1Resource, Object... param1VarArgs);
  }
  
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Discoverable
  public static @interface ResName {
    String value();
  }
}
