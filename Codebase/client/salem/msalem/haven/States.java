package haven;

import haven.glsl.GLColorVary;
import haven.glsl.ShaderMacro;
import java.awt.Color;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public abstract class States extends GLState {
   public static final GLState.Slot<States.ColState> color = new GLState.Slot<>(GLState.Slot.Type.DRAW, States.ColState.class, HavenPanel.global);
   public static final States.ColState vertexcolor = new States.ColState(0, 0, 0, 0) {
      @Override
      public void apply(GOut g) {
      }

      @Override
      public boolean equals(Object o) {
         return o == this;
      }

      @Override
      public String toString() {
         return "ColState(vertex)";
      }
   };
   public static final GLState.StandAlone ndepthtest = new GLState.StandAlone(GLState.Slot.Type.GEOM, PView.proj) {
      @Override
      public void apply(GOut g) {
         g.gl.glDisable(2929);
      }

      @Override
      public void unapply(GOut g) {
         g.gl.glEnable(2929);
      }
   };
   public static final GLState xray = compose(new GLState[]{ndepthtest, Rendered.last});
   public static final GLState.StandAlone fsaa = new GLState.StandAlone(GLState.Slot.Type.SYS, PView.proj) {
      @Override
      public void apply(GOut g) {
         g.gl.glEnable(32925);
      }

      @Override
      public void unapply(GOut g) {
         g.gl.glDisable(32925);
      }
   };
   public static final GLState.Slot<States.Coverage> coverage = new GLState.Slot<>(GLState.Slot.Type.DRAW, States.Coverage.class, PView.proj);
   public static final GLState.StandAlone presdepth = new GLState.StandAlone(GLState.Slot.Type.GEOM, PView.proj) {
      @Override
      public void apply(GOut g) {
         g.gl.glDepthMask(false);
      }

      @Override
      public void unapply(GOut g) {
         g.gl.glDepthMask(true);
      }
   };
   public static final GLState.Slot<States.Fog> fog = new GLState.Slot<>(GLState.Slot.Type.DRAW, States.Fog.class, PView.proj);
   public static final GLState.Slot<States.DepthOffset> depthoffset = new GLState.Slot<>(GLState.Slot.Type.GEOM, States.DepthOffset.class, PView.proj);
   public static final GLState.StandAlone nullprog = new GLState.StandAlone(GLState.Slot.Type.DRAW, PView.proj) {
      private final ShaderMacro[] sh = new ShaderMacro[0];

      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public ShaderMacro[] shaders() {
         return this.sh;
      }

      @Override
      public boolean reqshaders() {
         return true;
      }
   };
   public static final GLState.Slot<GLState> adhoc = new GLState.Slot<>(GLState.Slot.Type.DRAW, GLState.class, PView.wnd);
   public static final GLState.StandAlone normalize = new GLState.StandAlone(GLState.Slot.Type.GEOM, PView.proj) {
      @Override
      public void apply(GOut g) {
         g.gl.glEnable(2977);
      }

      @Override
      public void unapply(GOut g) {
         g.gl.glDisable(2977);
      }
   };

   private States() {
   }

   public static class AdHoc extends GLState {
      private final ShaderMacro[] sh;

      public AdHoc(ShaderMacro[] sh) {
         this.sh = sh;
      }

      public AdHoc(ShaderMacro sh) {
         this(new ShaderMacro[]{sh});
      }

      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public ShaderMacro[] shaders() {
         return this.sh;
      }

      @Override
      public boolean reqshaders() {
         return this.sh != null;
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(States.adhoc, this);
      }
   }

   public static class ColState extends GLState {
      private static final ShaderMacro[] shaders = new ShaderMacro[]{new GLColorVary()};
      public final Color c;
      public final float[] ca;

      public ColState(Color c) {
         this.c = c;
         this.ca = Utils.c2fa(c);
      }

      public ColState(int r, int g, int b, int a) {
         this(Utils.clipcol(r, g, b, a));
      }

      @Override
      public void apply(GOut g) {
         GL2 gl = g.gl;
         gl.glColor4fv(this.ca, 0);
      }

      @Override
      public int capply() {
         return 1;
      }

      @Override
      public void unapply(GOut g) {
         GL2 gl = g.gl;
         gl.glColor3f(1.0F, 1.0F, 1.0F);
      }

      @Override
      public int capplyfrom(GLState o) {
         return o instanceof States.ColState ? 1 : -1;
      }

      @Override
      public void applyfrom(GOut g, GLState o) {
         this.apply(g);
      }

      @Override
      public ShaderMacro[] shaders() {
         return shaders;
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(States.color, this);
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof States.ColState && ((States.ColState)o).c == this.c;
      }

      @Override
      public String toString() {
         return "ColState(" + this.c + ")";
      }
   }

   public static class Coverage extends GLState {
      public final float cov;
      public final boolean inv;

      public Coverage(float cov, boolean inv) {
         this.cov = cov;
         this.inv = inv;
      }

      @Override
      public void apply(GOut g) {
         GL gl = g.gl;
         gl.glEnable(32928);
         gl.glSampleCoverage(this.cov, this.inv);
      }

      @Override
      public void unapply(GOut g) {
         GL gl = g.gl;
         gl.glSampleCoverage(1.0F, false);
         gl.glDisable(32928);
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(States.coverage, this);
      }
   }

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

      @Override
      public void apply(GOut g) {
         GL gl = g.gl;
         gl.glPolygonOffset(this.factor, this.units);
         gl.glEnable(this.mode);
      }

      @Override
      public void unapply(GOut g) {
         GL gl = g.gl;
         gl.glDisable(this.mode);
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(States.depthoffset, this);
      }
   }

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

      @Override
      public void apply(GOut g) {
         GL2 gl = g.gl;
         gl.glFogi(2917, 9729);
         gl.glFogf(2915, this.s);
         gl.glFogf(2916, this.e);
         gl.glFogfv(2918, this.ca, 0);
         gl.glEnable(2912);
      }

      @Override
      public void unapply(GOut g) {
         GL2 gl = g.gl;
         gl.glDisable(2912);
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(States.fog, this);
      }
   }
}
