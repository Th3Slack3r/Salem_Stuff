package haven.resutil;

import haven.Coord;
import haven.Coord3f;
import haven.GLConfig;
import haven.GLState;
import haven.GOut;
import haven.Glob;
import haven.HavenPanel;
import haven.IDSet;
import haven.Light;
import haven.MCache;
import haven.MapMesh;
import haven.MapView;
import haven.Material;
import haven.MeshBuf;
import haven.PView;
import haven.Resource;
import haven.States;
import haven.Tex;
import haven.TexCube;
import haven.TexGL;
import haven.TexI;
import haven.TexSI;
import haven.Tiler;
import haven.Tiler.ResName;
import haven.glsl.Attribute;
import haven.glsl.AutoVarying;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.Function;
import haven.glsl.LValue;
import haven.glsl.Macro1;
import haven.glsl.MiscLib;
import haven.glsl.ProgramContext;
import haven.glsl.Return;
import haven.glsl.ShaderMacro;
import haven.glsl.Statement;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.ValBlock;
import haven.glsl.Variable;
import haven.glsl.VertexContext;
import java.awt.Color;
import java.util.Random;
import javax.media.opengl.GL2;

public class WaterTile extends Tiler {
  public final int depth;
  
  private static final Material.Colors bcol = new Material.Colors(new Color(128, 128, 128), new Color(255, 255, 255), new Color(0, 0, 0), new Color(0, 0, 0));
  
  public static class Bottom extends MapMesh.Surface {
    final MapMesh m;
    
    final boolean[] s;
    
    int[] ed;
    
    final MapMesh.Scan ss;
    
    public Bottom(MapMesh m) {
      super(m);
      this.m = m;
      Coord sz = m.sz;
      MCache map = m.map;
      MapMesh.Scan ds = new MapMesh.Scan(new Coord(-10, -10), sz.add(21, 21));
      this.ss = new MapMesh.Scan(new Coord(-9, -9), sz.add(19, 19));
      int[] d = new int[ds.l];
      this.s = new boolean[this.ss.l];
      this.ed = new int[this.ss.l];
      int j;
      for (j = ds.ul.y; j < ds.br.y; j++) {
        for (int x = ds.ul.y; x < ds.br.x; x++) {
          Tiler t = map.tiler(map.gettile(m.ul.add(x, j)));
          if (t instanceof WaterTile) {
            d[ds.o(x, j)] = ((WaterTile)t).depth;
          } else {
            d[ds.o(x, j)] = 0;
          } 
        } 
      } 
      for (j = this.ss.ul.y; j < this.ss.br.y; j++) {
        for (int x = this.ss.ul.x; x < this.ss.br.x; x++) {
          int td = d[ds.o(x, j)];
          if (d[ds.o(x - 1, j - 1)] < td)
            td = d[ds.o(x - 1, j - 1)]; 
          if (d[ds.o(x, j - 1)] < td)
            td = d[ds.o(x, j - 1)]; 
          if (d[ds.o(x - 1, j)] < td)
            td = d[ds.o(x - 1, j)]; 
          this.ed[this.ss.o(x, j)] = td;
          if (td == 0)
            this.s[this.ss.o(x, j)] = true; 
        } 
      } 
      for (int i = 0; i < 8; i++) {
        int[] sd = new int[this.ss.l];
        for (int k = this.ss.ul.y + 1; k < this.ss.br.y - 1; k++) {
          for (int x = this.ss.ul.x + 1; x < this.ss.br.x - 1; x++) {
            if (this.s[this.ss.o(x, k)]) {
              sd[this.ss.o(x, k)] = this.ed[this.ss.o(x, k)];
            } else {
              sd[this.ss.o(x, k)] = (this.ed[this.ss.o(x, k)] * 4 + this.ed[this.ss.o(x - 1, k)] + this.ed[this.ss.o(x + 1, k)] + this.ed[this.ss.o(x, k - 1)] + this.ed[this.ss.o(x, k + 1)]) / 8;
            } 
          } 
        } 
        this.ed = sd;
      } 
      for (int y = -1; y < sz.y + 2; y++) {
        for (int x = -1; x < sz.x + 2; x++)
          (spoint(new Coord(x, y))).pos.z -= this.ed[this.ss.o(x, y)]; 
      } 
    }
    
    public int d(int x, int y) {
      return this.ed[this.ss.o(x, y)];
    }
    
    public void calcnrm() {
      super.calcnrm();
      Coord c = new Coord();
      for (c.y = 0; c.y <= this.m.sz.y; c.y++) {
        for (c.x = 0; c.x <= this.m.sz.x; c.x++) {
          if (this.s[this.ss.o(c)])
            (spoint(c)).nrm = (this.m.gnd().spoint(c)).nrm; 
        } 
      } 
    }
    
    public static final MapMesh.DataID<Bottom> id = MapMesh.makeid(Bottom.class);
  }
  
  public static final TexCube sky = new TexCube(Resource.loadimg("gfx/tiles/skycube"));
  
  static final TexI nrm = (TexI)Resource.loadtex("gfx/tiles/wn");
  
  static {
    nrm.mipmap();
    nrm.magfilter(9729);
  }
  
  public static class SimpleSurface extends GLState.StandAlone {
    private static States.DepthOffset soff = new States.DepthOffset(2.0F, 2.0F);
    
    GLState.TexUnit tsky;
    
    GLState.TexUnit tnrm;
    
    private SimpleSurface() {
      super(GLState.Slot.Type.DRAW, new GLState.Slot[] { PView.cam, HavenPanel.global });
    }
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      (this.tsky = g.st.texalloc()).act();
      gl.glTexGeni(8192, 9472, 34066);
      gl.glTexGeni(8193, 9472, 34066);
      gl.glTexGeni(8194, 9472, 34066);
      gl.glEnable(3168);
      gl.glEnable(3169);
      gl.glEnable(3170);
      gl.glTexEnvi(8960, 8704, 8448);
      gl.glEnable(34067);
      gl.glBindTexture(34067, WaterTile.sky.glid(g));
      gl.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
      g.st.matmode(5890);
      gl.glPushMatrix();
      g.st.cam.transpose().trim3(1.0F).loadgl(gl);
    }
    
    public void unapply(GOut g) {
      GL2 gl = g.gl;
      this.tsky.act();
      g.st.matmode(5890);
      gl.glPopMatrix();
      gl.glDisable(34067);
      gl.glDisable(3168);
      gl.glDisable(3169);
      gl.glDisable(3170);
      gl.glColor3f(1.0F, 1.0F, 1.0F);
      this.tsky.free();
      this.tsky = null;
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(States.color, null);
      buf.put(Light.lighting, null);
      soff.prep(buf);
      super.prep(buf);
    }
  }
  
  public static class BetterSurface extends SimpleSurface {
    private final Uniform ssky = new Uniform(Type.SAMPLERCUBE);
    
    private final Uniform snrm = new Uniform(Type.SAMPLER2D);
    
    private final Uniform icam = new Uniform(Type.MAT3);
    
    private final ShaderMacro[] shaders;
    
    private BetterSurface() {
      this.shaders = new ShaderMacro[] { new ShaderMacro() {
            final AutoVarying skyc = new AutoVarying(Type.VEC3) {
                protected Expression root(VertexContext vctx) {
                  return (Expression)Cons.mul(new Expression[] { (Expression)WaterTile.BetterSurface.access$100(this.this$1.this$0).ref(), Cons.reflect(MiscLib.vertedir(vctx).depref(), vctx.eyen.depref()) });
                }
              };
            
            public void modify(final ProgramContext prog) {
              MiscLib.fragedir(prog.fctx);
              prog.fctx.uniform.getClass();
              final ValBlock.Value nmod = new ValBlock.Value(prog.fctx.uniform, Type.VEC3) {
                  public Expression root() {
                    return 
                      (Expression)Cons.mul(new Expression[] { (Expression)Cons.sub(Cons.mix(
                              (Expression)Cons.add(new Expression[] { (Expression)Cons.pick(Cons.texture2D((Expression)WaterTile.BetterSurface.access$200(this.this$1.this$0).ref(), (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick((LValue)MiscLib.fragmapv.ref(), "st"), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(0.01D), (Expression)Cons.l(0.012D) }) }), (Expression)Cons.mul(new Expression[] { (Expression)MiscLib.time.ref(), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(0.025D), (Expression)Cons.l(0.035D) }) }) })), "rgb"), 
                                  (Expression)Cons.pick(Cons.texture2D((Expression)WaterTile.BetterSurface.access$200(this.this$1.this$0).ref(), (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick((LValue)MiscLib.fragmapv.ref(), "st"), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(0.019D), (Expression)Cons.l(0.018D) }) }), (Expression)Cons.mul(new Expression[] { (Expression)MiscLib.time.ref(), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(-0.035D), (Expression)Cons.l(-0.025D) }) }) })), "rgb") }), (Expression)Cons.add(new Expression[] { (Expression)Cons.pick(Cons.texture2D((Expression)WaterTile.BetterSurface.access$200(this.this$1.this$0).ref(), 
                                      (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick((LValue)MiscLib.fragmapv.ref(), "st"), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(0.01D), (Expression)Cons.l(0.012D) }) }), (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)MiscLib.time.ref(), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(0.025D), (Expression)Cons.l(0.035D) }) }), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(0.5D), (Expression)Cons.l(0.5D) }) }) })), "rgb"), (Expression)Cons.pick(Cons.texture2D((Expression)WaterTile.BetterSurface.access$200(this.this$1.this$0).ref(), 
                                      (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick((LValue)MiscLib.fragmapv.ref(), "st"), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(0.019D), (Expression)Cons.l(0.018D) }) }), (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)MiscLib.time.ref(), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(-0.035D), (Expression)Cons.l(-0.025D) }) }), (Expression)Cons.vec2(new Expression[] { (Expression)Cons.l(0.5D), (Expression)Cons.l(0.5D) }) }) })), "rgb") }), Cons.abs((Expression)Cons.sub(Cons.mod((Expression)MiscLib.time.ref(), (Expression)Cons.l(2.0D)), (Expression)Cons.l(1.0D)))), (Expression)Cons.l(1.0D)), (Expression)Cons.vec3(new Expression[] { (Expression)Cons.l(0.0625D), (Expression)Cons.l(0.0625D), (Expression)Cons.l(1.0D) }) });
                  }
                };
              nmod.force();
              MiscLib.frageyen(prog.fctx).mod(new Macro1<Expression>() {
                    public Expression expand(Expression in) {
                      Expression m = nmod.ref();
                      return (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick(m, "x"), (Expression)Cons.vec3(new Expression[] { (Expression)Cons.l(1.0D), (Expression)Cons.l(0.0D), (Expression)Cons.l(0.0D) }) }), (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick(m, "y"), (Expression)Cons.vec3(new Expression[] { (Expression)Cons.l(0.0D), (Expression)Cons.l(1.0D), (Expression)Cons.l(0.0D) }) }), (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick(m, "z"), in }) });
                    }
                  }-10);
              prog.fctx.fragcol.mod(new Macro1<Expression>() {
                    public Expression expand(Expression in) {
                      return (Expression)Cons.mul(new Expression[] { in, Cons.textureCube((Expression)WaterTile.BetterSurface.access$300(this.this$1.this$0).ref(), (Expression)Cons.neg((Expression)Cons.mul(new Expression[] { (Expression)WaterTile.BetterSurface.access$100(this.this$1.this$0).ref(), Cons.reflect(MiscLib.fragedir(this.val$prog.fctx).depref(), MiscLib.frageyen(this.val$prog.fctx).depref()) }))), (Expression)Cons.l(0.4D) });
                    }
                  }0);
            }
          } };
    }
    
    public void reapply(GOut g) {
      GL2 gl = g.gl;
      gl.glUniform1i(g.st.prog.uniform(this.ssky), this.tsky.id);
      gl.glUniform1i(g.st.prog.uniform(this.snrm), this.tnrm.id);
      gl.glUniformMatrix3fv(g.st.prog.uniform(this.icam), 1, false, g.st.cam.transpose().trim3(), 0);
    }
    
    private void papply(GOut g) {
      GL2 gl = g.gl;
      gl.glBlendFunc(1, 1);
      (this.tsky = g.st.texalloc()).act();
      gl.glBindTexture(34067, WaterTile.sky.glid(g));
      (this.tnrm = g.st.texalloc()).act();
      gl.glBindTexture(3553, WaterTile.nrm.glid(g));
      reapply(g);
    }
    
    private void punapply(GOut g) {
      GL2 gl = g.gl;
      this.tsky.act();
      gl.glBindTexture(34067, 0);
      this.tnrm.act();
      gl.glBindTexture(3553, 0);
      this.tsky.free();
      this.tsky = null;
      this.tnrm.free();
      this.tnrm = null;
      gl.glBlendFunc(770, 771);
    }
    
    public ShaderMacro[] shaders() {
      return this.shaders;
    }
    
    public boolean reqshaders() {
      return true;
    }
    
    public void apply(GOut g) {
      if (g.st.prog == null) {
        super.apply(g);
      } else {
        papply(g);
      } 
    }
    
    public void unapply(GOut g) {
      if (!g.st.usedprog) {
        super.unapply(g);
      } else {
        punapply(g);
      } 
    }
  }
  
  public static final GLState surfmat = (GLState)new GLState.Abstract() {
      final GLState s1 = (GLState)new WaterTile.SimpleSurface(), s2 = (GLState)new WaterTile.BetterSurface();
      
      public void prep(GLState.Buffer buf) {
        if (((Boolean)buf.cfg.pref.wsurf.val).booleanValue()) {
          this.s2.prep(buf);
        } else {
          this.s1.prep(buf);
        } 
      }
    };
  
  public static final MeshBuf.LayerID<MeshBuf.Vec1Layer> depthlayer = (MeshBuf.LayerID<MeshBuf.Vec1Layer>)new MeshBuf.V1LayerID(BottomFog.depth);
  
  public static class BottomFog extends GLState.StandAlone {
    public static final double maxdepth = 25.0D;
    
    public static final Color fogcolor = new Color(13, 38, 25);
    
    public static final Expression mfogcolor = (Expression)Cons.mul(new Expression[] { Cons.col3(fogcolor), (Expression)Cons.pick((LValue)Cons.fref((LValue)Cons.idx((Expression)ProgramContext.gl_LightSource.ref(), (Expression)MapView.amblight.ref()), "diffuse"), "rgb") });
    
    public static Function rgbmix = (Function)new Function.Def(Type.VEC4) {
      
      };
    
    public static final Attribute depth = new Attribute(Type.FLOAT);
    
    public static final AutoVarying fragd = new AutoVarying(Type.FLOAT) {
        protected Expression root(VertexContext vctx) {
          return (Expression)WaterTile.BottomFog.depth.ref();
        }
      };
    
    private final ShaderMacro[] shaders = new ShaderMacro[] { new ShaderMacro() {
          public void modify(ProgramContext prog) {
            prog.fctx.fragcol.mod(new Macro1<Expression>() {
                  public Expression expand(Expression in) {
                    return WaterTile.BottomFog.rgbmix.call(new Expression[] { in, WaterTile.BottomFog.mfogcolor, Cons.min(new Expression[] { (Expression)Cons.div((Expression)WaterTile.BottomFog.fragd.ref(), (Expression)Cons.l(25.0D)), (Expression)Cons.l(1.0D) }) });
                  }
                }1000);
          }
        } };
    
    private BottomFog() {
      super(GLState.Slot.Type.DRAW, new GLState.Slot[0]);
    }
    
    public void apply(GOut g) {}
    
    public void unapply(GOut g) {}
    
    public ShaderMacro[] shaders() {
      return this.shaders;
    }
    
    public boolean reqshaders() {
      return true;
    }
    
    public void prep(GLState.Buffer buf) {
      if (((Boolean)buf.cfg.pref.wsurf.val).booleanValue())
        super.prep(buf); 
    }
  }
  
  public static final BottomFog waterfog = new BottomFog();
  
  private static final GLState boff = (GLState)new States.DepthOffset(4.0F, 4.0F);
  
  public static final GLState obfog = (GLState)new GLState.StandAlone(GLState.Slot.Type.DRAW, new GLState.Slot[0]) {
      final AutoVarying fragd = new AutoVarying(Type.FLOAT) {
          protected Expression root(VertexContext vctx) {
            return (Expression)Cons.sub((Expression)Cons.pick((LValue)MiscLib.maploc.ref(), "z"), (Expression)Cons.pick(vctx.mapv.depref(), "z"));
          }
        };
      
      final ShaderMacro[] shaders = new ShaderMacro[] { new ShaderMacro() {
            public void modify(ProgramContext prog) {
              prog.fctx.fragcol.mod(new Macro1<Expression>() {
                    public Expression expand(Expression in) {
                      return WaterTile.BottomFog.rgbmix.call(new Expression[] { in, WaterTile.BottomFog.mfogcolor, Cons.clamp((Expression)Cons.div((Expression)this.this$1.this$0.fragd.ref(), (Expression)Cons.l(25.0D)), (Expression)Cons.l(0.0D), (Expression)Cons.l(1.0D)) });
                    }
                  }1000);
            }
          } };
      
      public void apply(GOut g) {}
      
      public void unapply(GOut g) {}
      
      public ShaderMacro[] shaders() {
        return this.shaders;
      }
      
      public boolean reqshaders() {
        return true;
      }
    };
  
  public final Resource.Tileset bottom;
  
  public final GLState mat;
  
  @ResName("water")
  public static class Fac implements Tiler.Factory {
    public Tiler create(int id, Resource.Tileset set) {
      int a = 0;
      int depth = ((Integer)set.ta[a++]).intValue();
      Resource.Tileset ground = set;
      TerrainTile terrain = null;
      while (a < set.ta.length) {
        Object[] desc = (Object[])set.ta[a++];
        String p = (String)desc[0];
        if (p.equals("gnd")) {
          Resource gres = Resource.load((String)desc[1], ((Integer)desc[2]).intValue());
          ground = (Resource.Tileset)gres.layer(Resource.tileset);
          continue;
        } 
        if (p.equals("trn")) {
          Resource tres = Resource.load((String)desc[1], ((Integer)desc[2]).intValue());
          Resource.Tileset tset = (Resource.Tileset)tres.layer(Resource.tileset);
          terrain = (TerrainTile)tset.tfac().create(-1, tset);
        } 
      } 
      if (terrain == null)
        return new WaterTile(id, set, depth, ground); 
      return new WaterTile.TWaterTile(id, set, depth, terrain);
    }
  }
  
  public WaterTile(int id, Resource.Tileset set, int depth, Resource.Tileset bottom) {
    super(id);
    this.depth = depth;
    this.bottom = bottom;
    TexGL tex = (TexGL)((TexSI)((Resource.Tile)bottom.ground.pick(0)).tex()).parent;
    this.mat = (GLState)new Material(new GLState[] { Light.deflight, (GLState)bcol, tex.draw(), (GLState)waterfog, boff });
  }
  
  public static class BottomPlane extends MapMesh.Plane {
    WaterTile.Bottom srf;
    
    Coord lc;
    
    public BottomPlane(MapMesh m, WaterTile.Bottom srf, Coord lc, int z, GLState mat, Tex tex) {
      super(m, srf.fortile(lc), z, mat, tex);
      this.srf = srf;
      this.lc = new Coord(lc);
    }
    
    public void build(MeshBuf buf) {
      MeshBuf.Tex ta = (MeshBuf.Tex)buf.layer(MeshBuf.tex);
      MeshBuf.Vec1Layer da = (MeshBuf.Vec1Layer)buf.layer(WaterTile.depthlayer);
      buf.getClass();
      MeshBuf.Vertex v1 = new MeshBuf.Vertex(buf, (this.vrt[0]).pos, (this.vrt[0]).nrm);
      buf.getClass();
      MeshBuf.Vertex v2 = new MeshBuf.Vertex(buf, (this.vrt[1]).pos, (this.vrt[1]).nrm);
      buf.getClass();
      MeshBuf.Vertex v3 = new MeshBuf.Vertex(buf, (this.vrt[2]).pos, (this.vrt[2]).nrm);
      buf.getClass();
      MeshBuf.Vertex v4 = new MeshBuf.Vertex(buf, (this.vrt[3]).pos, (this.vrt[3]).nrm);
      ta.set(v1, new Coord3f(this.tex.tcx(this.texx[0]), this.tex.tcy(this.texy[0]), 0.0F));
      ta.set(v2, new Coord3f(this.tex.tcx(this.texx[1]), this.tex.tcy(this.texy[1]), 0.0F));
      ta.set(v3, new Coord3f(this.tex.tcx(this.texx[2]), this.tex.tcy(this.texy[2]), 0.0F));
      ta.set(v4, new Coord3f(this.tex.tcx(this.texx[3]), this.tex.tcy(this.texy[3]), 0.0F));
      da.set(v1, Float.valueOf(this.srf.d(this.lc.x, this.lc.y)));
      da.set(v2, Float.valueOf(this.srf.d(this.lc.x, this.lc.y + 1)));
      da.set(v3, Float.valueOf(this.srf.d(this.lc.x + 1, this.lc.y + 1)));
      da.set(v4, Float.valueOf(this.srf.d(this.lc.x + 1, this.lc.y)));
      MapMesh.splitquad(buf, v1, v2, v3, v4);
    }
  }
  
  public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
    Resource.Tile g = (Resource.Tile)this.bottom.ground.pick(rnd);
    new BottomPlane(m, (Bottom)m.data(Bottom.id), lc, 0, this.mat, g.tex());
    m.getClass();
    new MapMesh.Plane(m, m.gnd(), lc, 257, surfmat);
  }
  
  public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {
    if (m.map.gettile(gc) <= this.id)
      return; 
    if (this.bottom.btrans != null && bmask > 0) {
      Resource.Tile t = (Resource.Tile)this.bottom.btrans[bmask - 1].pick(rnd);
      if (gt instanceof WaterTile) {
        new BottomPlane(m, (Bottom)m.data(Bottom.id), lc, z, this.mat, t.tex());
      } else {
        gt.layover(m, lc, gc, z, t);
      } 
    } 
    if (this.bottom.ctrans != null && cmask > 0) {
      Resource.Tile t = (Resource.Tile)this.bottom.ctrans[cmask - 1].pick(rnd);
      if (gt instanceof WaterTile) {
        new BottomPlane(m, (Bottom)m.data(Bottom.id), lc, z, this.mat, t.tex());
      } else {
        gt.layover(m, lc, gc, z, t);
      } 
    } 
  }
  
  public WaterTile(int id, Resource.Tileset set, int depth) {
    this(id, set, depth, set);
  }
  
  public static class TWaterTile extends WaterTile {
    private static final IDSet<GLState> bmats = new IDSet();
    
    public final TerrainTile bottom;
    
    public TWaterTile(int id, Resource.Tileset set, int depth, TerrainTile bottom) {
      super(id, set, depth);
      this.bottom = bottom;
    }
    
    public class BottomPlane extends TerrainTile.Plane {
      float[] depth;
      
      public BottomPlane(MapMesh m, WaterTile.Bottom srf, Coord lc, int z, GLState mat, int[] alpha) {
        super(m, srf, lc, z, mat, alpha);
        this.depth = new float[] { srf.d(lc.x, lc.y), srf.d(lc.x, lc.y + 1), srf.d(lc.x + 1, lc.y + 1), srf.d(lc.x + 1, lc.y) };
      }
      
      public MeshBuf.Vertex mkvert(MeshBuf buf, int n) {
        MeshBuf.Vertex v = super.mkvert(buf, n);
        ((MeshBuf.Vec1Layer)buf.layer(WaterTile.depthlayer)).set(v, Float.valueOf(this.depth[n]));
        return v;
      }
    }
    
    public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
      TerrainTile.Blend b = (TerrainTile.Blend)m.data(this.bottom.blend);
      for (int i = 0; i < this.bottom.var.length + 1; i++) {
        GLState mat = (i == 0) ? this.bottom.base : (this.bottom.var[i - 1]).mat;
        mat = (GLState)bmats.intern(GLState.compose(new GLState[] { mat, (GLState)WaterTile.waterfog, WaterTile.access$600() }));
        if (b.en[i][b.es.o(lc)])
          new BottomPlane(m, (WaterTile.Bottom)m.data(WaterTile.Bottom.id), lc, i, mat, new int[] { (int)(b.bv[i][b.vs.o(lc)] * 255.0F), (int)(b.bv[i][b.vs.o(lc.add(0, 1))] * 255.0F), (int)(b.bv[i][b.vs.o(lc.add(1, 1))] * 255.0F), (int)(b.bv[i][b.vs.o(lc.add(1, 0))] * 255.0F) }); 
      } 
      m.getClass();
      new MapMesh.Plane(m, m.gnd(), lc, 257, WaterTile.surfmat);
    }
    
    public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {}
  }
  
  public GLState drawstate(Glob glob, GLConfig cfg, Coord3f c) {
    if (((Boolean)cfg.pref.wsurf.val).booleanValue())
      return obfog; 
    return null;
  }
}
