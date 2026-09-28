package haven;

import haven.glsl.GLColorVary;
import haven.glsl.ShaderMacro;
import java.awt.Color;
import javax.media.opengl.GL2;

public abstract class States extends GLState {
  public static final GLState.Slot<ColState> color = new GLState.Slot<>(GLState.Slot.Type.DRAW, ColState.class, new GLState.Slot[] { HavenPanel.global });
  
  public static class ColState extends GLState {
    private static final ShaderMacro[] shaders = new ShaderMacro[] { (ShaderMacro)new GLColorVary() };
    
    public final Color c;
    
    public final float[] ca;
    
    public ColState(Color c) {
      this.c = c;
      this.ca = Utils.c2fa(c);
    }
    
    public ColState(int r, int g, int b, int a) {
      this(Utils.clipcol(r, g, b, a));
    }
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      gl.glColor4fv(this.ca, 0);
    }
    
    public int capply() {
      return 1;
    }
    
    public void unapply(GOut g) {
      GL2 gl = g.gl;
      gl.glColor3f(1.0F, 1.0F, 1.0F);
    }
    
    public int capplyfrom(GLState o) {
      if (o instanceof ColState)
        return 1; 
      return -1;
    }
    
    public void applyfrom(GOut g, GLState o) {
      apply(g);
    }
    
    public ShaderMacro[] shaders() {
      return shaders;
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(States.color, this);
    }
    
    public boolean equals(Object o) {
      return (o instanceof ColState && ((ColState)o).c == this.c);
    }
    
    public String toString() {
      return "ColState(" + this.c + ")";
    }
  }
  
  public static final ColState vertexcolor = new ColState(0, 0, 0, 0) {
      public void apply(GOut g) {}
      
      public boolean equals(Object o) {
        return (o == this);
      }
      
      public String toString() {
        return "ColState(vertex)";
      }
    };
  
  public static final GLState.StandAlone ndepthtest = new GLState.StandAlone(GLState.Slot.Type.GEOM, new GLState.Slot[] { PView.proj }) {
      public void apply(GOut g) {
        g.gl.glDisable(2929);
      }
      
      public void unapply(GOut g) {
        g.gl.glEnable(2929);
      }
    };
  
  public static final GLState xray = GLState.compose(new GLState[] { ndepthtest, Rendered.last });
  
  public static final GLState.StandAlone fsaa = new GLState.StandAlone(GLState.Slot.Type.SYS, new GLState.Slot[] { PView.proj }) {
      public void apply(GOut g) {
        g.gl.glEnable(32925);
      }
      
      public void unapply(GOut g) {
        g.gl.glDisable(32925);
      }
    };
  
  public static final GLState.Slot<Coverage> coverage = new GLState.Slot<>(GLState.Slot.Type.DRAW, Coverage.class, new GLState.Slot[] { PView.proj });
  
  public static class Coverage extends GLState {
    public final float cov;
    
    public final boolean inv;
    
    public Coverage(float cov, boolean inv) {
      this.cov = cov;
      this.inv = inv;
    }
    
    public void apply(GOut g) {
      GL2 gL2 = g.gl;
      gL2.glEnable(32928);
      gL2.glSampleCoverage(this.cov, this.inv);
    }
    
    public void unapply(GOut g) {
      GL2 gL2 = g.gl;
      gL2.glSampleCoverage(1.0F, false);
      gL2.glDisable(32928);
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(States.coverage, this);
    }
  }
  
  public static final GLState.StandAlone presdepth = new GLState.StandAlone(GLState.Slot.Type.GEOM, new GLState.Slot[] { PView.proj }) {
      public void apply(GOut g) {
        g.gl.glDepthMask(false);
      }
      
      public void unapply(GOut g) {
        g.gl.glDepthMask(true);
      }
    };
  
  public static final GLState.Slot<Fog> fog = new GLState.Slot<>(GLState.Slot.Type.DRAW, Fog.class, new GLState.Slot[] { PView.proj });
  
  public static class Fog extends GLState {
    public final Color c;
    
    public final float[] ca;
    
    public final float s;
    
    public final float e;
    
    public Fog(Color c, float s, float e) {
      this.c = c;
      this.ca = Utils.c2fa(c);
      this.s = s;
      this.e = e;
    }
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      gl.glFogi(2917, 9729);
      gl.glFogf(2915, this.s);
      gl.glFogf(2916, this.e);
      gl.glFogfv(2918, this.ca, 0);
      gl.glEnable(2912);
    }
    
    public void unapply(GOut g) {
      GL2 gl = g.gl;
      gl.glDisable(2912);
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(States.fog, this);
    }
  }
  
  public static final GLState.Slot<DepthOffset> depthoffset = new GLState.Slot<>(GLState.Slot.Type.GEOM, DepthOffset.class, new GLState.Slot[] { PView.proj });
  
  public static class DepthOffset extends GLState {
    public final int mode;
    
    public final float factor;
    
    public final float units;
    
    public DepthOffset(int mode, float factor, float units) {
      this.mode = mode;
      this.factor = factor;
      this.units = units;
    }
    
    public DepthOffset(float factor, float units) {
      this(32823, factor, units);
    }
    
    public void apply(GOut g) {
      GL2 gL2 = g.gl;
      gL2.glPolygonOffset(this.factor, this.units);
      gL2.glEnable(this.mode);
    }
    
    public void unapply(GOut g) {
      GL2 gL2 = g.gl;
      gL2.glDisable(this.mode);
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(States.depthoffset, this);
    }
  }
  
  public static final GLState.StandAlone nullprog = new GLState.StandAlone(GLState.Slot.Type.DRAW, new GLState.Slot[] { PView.proj }) {
      private final ShaderMacro[] sh = new ShaderMacro[0];
      
      public void apply(GOut g) {}
      
      public void unapply(GOut g) {}
      
      public ShaderMacro[] shaders() {
        return this.sh;
      }
      
      public boolean reqshaders() {
        return true;
      }
    };
  
  public static final GLState.Slot<GLState> adhoc = new GLState.Slot<>(GLState.Slot.Type.DRAW, GLState.class, new GLState.Slot[] { PView.wnd });
  
  public static class AdHoc extends GLState {
    private final ShaderMacro[] sh;
    
    public AdHoc(ShaderMacro[] sh) {
      this.sh = sh;
    }
    
    public AdHoc(ShaderMacro sh) {
      this(new ShaderMacro[] { sh });
    }
    
    public void apply(GOut g) {}
    
    public void unapply(GOut g) {}
    
    public ShaderMacro[] shaders() {
      return this.sh;
    }
    
    public boolean reqshaders() {
      return (this.sh != null);
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(States.adhoc, this);
    }
  }
  
  public static final GLState.StandAlone normalize = new GLState.StandAlone(GLState.Slot.Type.GEOM, new GLState.Slot[] { PView.proj }) {
      public void apply(GOut g) {
        g.gl.glEnable(2977);
      }
      
      public void unapply(GOut g) {
        g.gl.glDisable(2977);
      }
    };
}
