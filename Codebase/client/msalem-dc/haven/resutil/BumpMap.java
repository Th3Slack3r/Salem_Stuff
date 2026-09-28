package haven.resutil;

import haven.Coord;
import haven.Coord3f;
import haven.GLState;
import haven.GOut;
import haven.MapMesh;
import haven.Material;
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
import java.util.Objects;
import java.util.Random;
import javax.media.opengl.GL2;

public class BumpMap extends GLState {
   public static final GLState.Slot<BumpMap> slot = new GLState.Slot<>(GLState.Slot.Type.DRAW, BumpMap.class);
   public static final Attribute tan = new Attribute(Type.VEC3);
   public static final Attribute bit = new Attribute(Type.VEC3);
   private static final Uniform ctex = new Uniform(Type.SAMPLER2D);
   public final TexGL tex;
   private GLState.TexUnit sampler;
   private static final ShaderMacro[] shaders = new ShaderMacro[]{new ShaderMacro() {
      final AutoVarying tanc = new AutoVarying(Type.VEC3) {
         @Override
         protected Expression root(VertexContext vctx) {
            return Cons.mul(VertexContext.gl_NormalMatrix.ref(), BumpMap.tan.ref());
         }
      };
      final AutoVarying bitc = new AutoVarying(Type.VEC3) {
         @Override
         protected Expression root(VertexContext vctx) {
            return Cons.mul(VertexContext.gl_NormalMatrix.ref(), BumpMap.bit.ref());
         }
      };

      @Override
      public void modify(ProgramContext prog) {
         ValBlock var10003 = prog.fctx.uniform;
         Objects.requireNonNull(prog.fctx.uniform);
         final ValBlock.Value nmod = new ValBlock.Value(var10003, Type.VEC3) {
            {
               Objects.requireNonNull(x0);
            }

            @Override
            public Expression root() {
               return Cons.mul(Cons.sub(Cons.pick(Cons.texture2D(BumpMap.ctex.ref(), Tex2D.texcoord.ref()), "rgb"), Cons.l(0.5)), Cons.l(2.0));
            }
         };
         nmod.force();
         MiscLib.frageyen(prog.fctx).mod(new Macro1<Expression>() {
            public Expression expand(Expression in) {
               Expression m = nmod.ref();
               return Cons.add(Cons.mul(Cons.pick(m, "s"), tanc.ref()), Cons.mul(Cons.pick(m, "t"), bitc.ref()), Cons.mul(Cons.pick(m, "p"), in));
            }
         }, -100);
      }
   }};
   public static final MeshBuf.LayerID<MeshBuf.Vec3Layer> ltan = new MeshBuf.V3LayerID(tan);
   public static final MeshBuf.LayerID<MeshBuf.Vec3Layer> lbit = new MeshBuf.V3LayerID(bit);

   public BumpMap(TexGL tex) {
      this.tex = tex;
   }

   @Override
   public ShaderMacro[] shaders() {
      return shaders;
   }

   @Override
   public boolean reqshaders() {
      return true;
   }

   @Override
   public void reapply(GOut g) {
      g.gl.glUniform1i(g.st.prog.uniform(ctex), this.sampler.id);
   }

   @Override
   public void apply(GOut g) {
      this.sampler = TexGL.lbind(g, this.tex);
      this.reapply(g);
   }

   @Override
   public void unapply(GOut g) {
      GL2 gl = g.gl;
      this.sampler.act();
      gl.glBindTexture(3553, 0);
      this.sampler.free();
      this.sampler = null;
   }

   @Override
   public void prep(GLState.Buffer buf) {
      buf.put(slot, this);
   }

   @Material.ResName("bump")
   public static class $bump implements Material.ResCons2 {
      @Override
      public void cons(final Resource res, List<GLState> states, List<Material.Res.Resolver> left, Object... args) {
         int a = 0;
         final Resource tres;
         final int tid;
         if (args[a] instanceof String) {
            tres = Resource.load((String)args[a], (Integer)args[a + 1]);
            tid = (Integer)args[a + 2];
            a += 3;
         } else {
            tres = res;
            tid = (Integer)args[a];
            a++;
         }

         left.add(new Material.Res.Resolver() {
            @Override
            public void resolve(Collection<GLState> buf) {
               TexR rt = tres.layer(TexR.class, tid);
               if (rt == null) {
                  throw new RuntimeException(String.format("Specified texture %d for %s not found in %s", tid, res, tres));
               } else {
                  buf.add(new BumpMap(rt.tex()));
               }
            }
         });
      }
   }

   public static class MapTangents extends MapMesh.Hooks {
      public final MapMesh m;
      public final MapMesh.Scan s;
      public final Coord3f[] tan;
      public final Coord3f[] bit;
      public static final MapMesh.DataID<BumpMap.MapTangents> id = MapMesh.makeid(BumpMap.MapTangents.class);

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

      @Override
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
         MeshBuf.Vec3Layer btan = buf.layer(BumpMap.ltan);
         MeshBuf.Vec3Layer bbit = buf.layer(BumpMap.lbit);
         btan.set(v1, this.tan[this.s.o(lc)]);
         bbit.set(v1, this.bit[this.s.o(lc)]);
         btan.set(v2, this.tan[this.s.o(lc.add(0, 1))]);
         bbit.set(v2, this.bit[this.s.o(lc.add(0, 1))]);
         btan.set(v3, this.tan[this.s.o(lc.add(1, 1))]);
         bbit.set(v3, this.bit[this.s.o(lc.add(1, 1))]);
         btan.set(v4, this.tan[this.s.o(lc.add(1, 0))]);
         bbit.set(v4, this.bit[this.s.o(lc.add(1, 0))]);
      }
   }
}
