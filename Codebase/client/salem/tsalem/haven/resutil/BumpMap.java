package haven.resutil;

import haven.Coord;
import haven.Coord3f;
import haven.GLState;
import haven.GOut;
import haven.MapMesh;
import haven.Material;
import haven.Material.ResName;
import haven.MeshBuf;
import haven.Resource;
import haven.TexGL;
import haven.TexR;
import haven.glsl.Attribute;
import haven.glsl.AutoVarying;
import haven.glsl.Cons;
import haven.glsl.Expression;
import haven.glsl.Macro1;
import haven.glsl.MiscLib;
import haven.glsl.ProgramContext;
import haven.glsl.ShaderMacro;
import haven.glsl.Tex2D;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.ValBlock;
import haven.glsl.VertexContext;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.media.opengl.GL2;

public class BumpMap extends GLState {
  public static final GLState.Slot<BumpMap> slot = new GLState.Slot(GLState.Slot.Type.DRAW, BumpMap.class, new GLState.Slot[0]);
  
  public static final Attribute tan = new Attribute(Type.VEC3);
  
  public static final Attribute bit = new Attribute(Type.VEC3);
  
  private static final Uniform ctex = new Uniform(Type.SAMPLER2D);
  
  public final TexGL tex;
  
  private GLState.TexUnit sampler;
  
  public BumpMap(TexGL tex) {
    this.tex = tex;
  }
  
  private static final ShaderMacro[] shaders = new ShaderMacro[] { new ShaderMacro() {
        final AutoVarying tanc = new AutoVarying(Type.VEC3) {
            protected Expression root(VertexContext vctx) {
              return (Expression)Cons.mul(new Expression[] { (Expression)VertexContext.gl_NormalMatrix.ref(), (Expression)BumpMap.tan.ref() });
            }
          };
        
        final AutoVarying bitc = new AutoVarying(Type.VEC3) {
            protected Expression root(VertexContext vctx) {
              return (Expression)Cons.mul(new Expression[] { (Expression)VertexContext.gl_NormalMatrix.ref(), (Expression)BumpMap.bit.ref() });
            }
          };
        
        public void modify(ProgramContext prog) {
          prog.fctx.uniform.getClass();
          final ValBlock.Value nmod = new ValBlock.Value(prog.fctx.uniform, Type.VEC3) {
              public Expression root() {
                return (Expression)Cons.mul(new Expression[] { (Expression)Cons.sub((Expression)Cons.pick(Cons.texture2D((Expression)BumpMap.access$000().ref(), (Expression)Tex2D.texcoord.ref()), "rgb"), (Expression)Cons.l(0.5D)), (Expression)Cons.l(2.0D) });
              }
            };
          nmod.force();
          MiscLib.frageyen(prog.fctx).mod(new Macro1<Expression>() {
                public Expression expand(Expression in) {
                  Expression m = nmod.ref();
                  return (Expression)Cons.add(new Expression[] { (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick(m, "s"), (Expression)this.this$0.tanc.ref() }), (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick(m, "t"), (Expression)this.this$0.bitc.ref() }), (Expression)Cons.mul(new Expression[] { (Expression)Cons.pick(m, "p"), in }) });
                }
              }-100);
        }
      } };
  
  public ShaderMacro[] shaders() {
    return shaders;
  }
  
  public boolean reqshaders() {
    return true;
  }
  
  public void reapply(GOut g) {
    g.gl.glUniform1i(g.st.prog.uniform(ctex), this.sampler.id);
  }
  
  public void apply(GOut g) {
    this.sampler = TexGL.lbind(g, this.tex);
    reapply(g);
  }
  
  public void unapply(GOut g) {
    GL2 gl = g.gl;
    this.sampler.act();
    gl.glBindTexture(3553, 0);
    this.sampler.free();
    this.sampler = null;
  }
  
  public void prep(GLState.Buffer buf) {
    buf.put(slot, this);
  }
  
  public static class MapTangents extends MapMesh.Hooks {
    public final MapMesh m;
    
    public final MapMesh.Scan s;
    
    public final Coord3f[] tan;
    
    public final Coord3f[] bit;
    
    public MapTangents(MapMesh m) {
      this.m = m;
      this.s = new MapMesh.Scan(Coord.z, m.sz.add(1, 1));
      this.tan = new Coord3f[this.s.l];
      this.bit = new Coord3f[this.s.l];
      for (int i = 0; i < this.s.l; i++) {
        this.tan[i] = new Coord3f(0.0F, 0.0F, 0.0F);
        this.bit[i] = new Coord3f(0.0F, 0.0F, 0.0F);
      } 
    }
    
    public void postcalcnrm(Random rnd) {
      MapMesh.Surface gnd = this.m.gnd();
      for (int y = this.s.ul.y; y < this.s.br.y; y++) {
        for (int x = this.s.ul.x; x < this.s.br.x; x++) {
          MapMesh.SPoint sp = gnd.spoint(new Coord(x, y));
          Coord3f ct = Coord3f.yu.cmul(sp.nrm).norm();
          Coord3f cb = sp.nrm.cmul(Coord3f.xu).norm();
          Coord3f mt = this.tan[this.s.o(x, y)];
          mt.x = ct.x;
          mt.y = ct.y;
          mt.z = ct.z;
          Coord3f mb = this.bit[this.s.o(x, y)];
          mb.x = cb.x;
          mb.y = cb.y;
          mb.z = cb.z;
        } 
      } 
    }
    
    public void set(MeshBuf buf, Coord lc, MeshBuf.Vertex v1, MeshBuf.Vertex v2, MeshBuf.Vertex v3, MeshBuf.Vertex v4) {
      MeshBuf.Vec3Layer btan = (MeshBuf.Vec3Layer)buf.layer(BumpMap.ltan);
      MeshBuf.Vec3Layer bbit = (MeshBuf.Vec3Layer)buf.layer(BumpMap.lbit);
      btan.set(v1, this.tan[this.s.o(lc)]);
      bbit.set(v1, this.bit[this.s.o(lc)]);
      btan.set(v2, this.tan[this.s.o(lc.add(0, 1))]);
      bbit.set(v2, this.bit[this.s.o(lc.add(0, 1))]);
      btan.set(v3, this.tan[this.s.o(lc.add(1, 1))]);
      bbit.set(v3, this.bit[this.s.o(lc.add(1, 1))]);
      btan.set(v4, this.tan[this.s.o(lc.add(1, 0))]);
      bbit.set(v4, this.bit[this.s.o(lc.add(1, 0))]);
    }
    
    public static final MapMesh.DataID<MapTangents> id = MapMesh.makeid(MapTangents.class);
  }
  
  public static final MeshBuf.LayerID<MeshBuf.Vec3Layer> ltan = (MeshBuf.LayerID<MeshBuf.Vec3Layer>)new MeshBuf.V3LayerID(tan);
  
  public static final MeshBuf.LayerID<MeshBuf.Vec3Layer> lbit = (MeshBuf.LayerID<MeshBuf.Vec3Layer>)new MeshBuf.V3LayerID(bit);
  
  @ResName("bump")
  public static class $bump implements Material.ResCons2 {
    public void cons(final Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
      final Resource tres;
      final int tid, a = 0;
      if (args[a] instanceof String) {
        tres = Resource.load((String)args[a], ((Integer)args[a + 1]).intValue());
        tid = ((Integer)args[a + 2]).intValue();
        a += 3;
      } else {
        tres = res;
        tid = ((Integer)args[a]).intValue();
        a++;
      } 
      left.add(new Material.Res.Resolver() {
            public void resolve(Collection<GLState> buf) {
              TexR rt = (TexR)tres.layer(TexR.class, Integer.valueOf(tid));
              if (rt == null)
                throw new RuntimeException(String.format("Specified texture %d for %s not found in %s", new Object[] { Integer.valueOf(this.val$tid), this.val$res, this.val$tres })); 
              buf.add(new BumpMap(rt.tex()));
            }
          });
    }
  }
}
