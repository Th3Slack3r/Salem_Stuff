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
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.glsl.ValBlock;
import haven.glsl.VertexContext;
import java.awt.Color;
import java.util.Random;
import javax.media.opengl.GL2;

public class WaterTile extends Tiler {
   public final int depth;
   private static final Material.Colors bcol = new Material.Colors(new Color(128, 128, 128), new Color(255, 255, 255), new Color(0, 0, 0), new Color(0, 0, 0));
   public static final TexCube sky = new TexCube(Resource.loadimg("gfx/tiles/skycube"));
   static final TexI nrm = (TexI)Resource.loadtex("gfx/tiles/wn");
   public static final GLState surfmat = new GLState.Abstract() {
      final GLState s1 = new WaterTile.SimpleSurface();
      final GLState s2 = new WaterTile.BetterSurface();

      @Override
      public void prep(GLState.Buffer buf) {
         if (buf.cfg.pref.wsurf.val) {
            this.s2.prep(buf);
         } else {
            this.s1.prep(buf);
         }
      }
   };
   public static final MeshBuf.LayerID<MeshBuf.Vec1Layer> depthlayer = new MeshBuf.V1LayerID(WaterTile.BottomFog.depth);
   public static final WaterTile.BottomFog waterfog = new WaterTile.BottomFog();
   private static final GLState boff = new States.DepthOffset(4.0F, 4.0F);
   public static final GLState obfog = new GLState.StandAlone(GLState.Slot.Type.DRAW) {
      final AutoVarying fragd = new AutoVarying(Type.FLOAT) {
         @Override
         protected Expression root(VertexContext vctx) {
            return Cons.sub(Cons.pick((LValue)MiscLib.maploc.ref(), "z"), Cons.pick(vctx.mapv.depref(), "z"));
         }
      };
      final ShaderMacro[] shaders = new ShaderMacro[]{
         new ShaderMacro() {
            @Override
            public void modify(ProgramContext prog) {
               prog.fctx
                  .fragcol
                  .mod(
                     new Macro1<Expression>() {
                        public Expression expand(Expression in) {
                           return WaterTile.BottomFog.rgbmix
                              .call(in, WaterTile.BottomFog.mfogcolor, Cons.clamp(Cons.div(fragd.ref(), Cons.l(25.0)), Cons.l(0.0), Cons.l(1.0)));
                        }
                     },
                     1000
                  );
            }
         }
      };

      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public ShaderMacro[] shaders() {
         return this.shaders;
      }

      @Override
      public boolean reqshaders() {
         return true;
      }
   };
   public final Resource.Tileset bottom;
   public final GLState mat;

   public WaterTile(int id, Resource.Tileset set, int depth, Resource.Tileset bottom) {
      super(id);
      this.depth = depth;
      this.bottom = bottom;
      TexGL tex = (TexGL)((TexSI)bottom.ground.pick(0).tex()).parent;
      this.mat = new Material(Light.deflight, bcol, tex.draw(), waterfog, boff);
   }

   @Override
   public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
      Resource.Tile g = this.bottom.ground.pick(rnd);
      new WaterTile.BottomPlane(m, m.data(WaterTile.Bottom.id), lc, 0, this.mat, g.tex());
      m.new Plane(m.gnd(), lc, 257, surfmat);
   }

   @Override
   public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {
      if (m.map.gettile(gc) > this.id) {
         if (this.bottom.btrans != null && bmask > 0) {
            Resource.Tile t = this.bottom.btrans[bmask - 1].pick(rnd);
            if (gt instanceof WaterTile) {
               new WaterTile.BottomPlane(m, m.data(WaterTile.Bottom.id), lc, z, this.mat, t.tex());
            } else {
               gt.layover(m, lc, gc, z, t);
            }
         }

         if (this.bottom.ctrans != null && cmask > 0) {
            Resource.Tile t = this.bottom.ctrans[cmask - 1].pick(rnd);
            if (gt instanceof WaterTile) {
               new WaterTile.BottomPlane(m, m.data(WaterTile.Bottom.id), lc, z, this.mat, t.tex());
            } else {
               gt.layover(m, lc, gc, z, t);
            }
         }
      }
   }

   public WaterTile(int id, Resource.Tileset set, int depth) {
      this(id, set, depth, set);
   }

   @Override
   public GLState drawstate(Glob glob, GLConfig cfg, Coord3f c) {
      return cfg.pref.wsurf.val ? obfog : null;
   }

   static {
      nrm.mipmap();
      nrm.magfilter(9729);
   }

   public static class BetterSurface extends WaterTile.SimpleSurface {
      private final Uniform ssky = new Uniform(Type.SAMPLERCUBE);
      private final Uniform snrm = new Uniform(Type.SAMPLER2D);
      private final Uniform icam = new Uniform(Type.MAT3);
      private ShaderMacro[] shaders = new ShaderMacro[]{
         new ShaderMacro() {
            final AutoVarying skyc = new AutoVarying(Type.VEC3) {
               @Override
               protected Expression root(VertexContext vctx) {
                  return Cons.mul(BetterSurface.this.icam.ref(), Cons.reflect(MiscLib.vertedir(vctx).depref(), vctx.eyen.depref()));
               }
            };

            @Override
            public void modify(final ProgramContext prog) {
               MiscLib.fragedir(prog.fctx);
               ValBlock var10003 = prog.fctx.uniform;
               prog.fctx.uniform.getClass();
               final ValBlock.Value nmod = new ValBlock.Value(var10003, Type.VEC3) {
                  {
                     x0.getClass();
                  }

                  @Override
                  public Expression root() {
                     return Cons.mul(
                        Cons.sub(
                           Cons.mix(
                              Cons.add(
                                 Cons.pick(
                                    Cons.texture2D(
                                       BetterSurface.this.snrm.ref(),
                                       Cons.add(
                                          Cons.mul(Cons.pick((LValue)MiscLib.fragmapv.ref(), "st"), Cons.vec2(Cons.l(0.01), Cons.l(0.012))),
                                          Cons.mul(MiscLib.time.ref(), Cons.vec2(Cons.l(0.025), Cons.l(0.035)))
                                       )
                                    ),
                                    "rgb"
                                 ),
                                 Cons.pick(
                                    Cons.texture2D(
                                       BetterSurface.this.snrm.ref(),
                                       Cons.add(
                                          Cons.mul(Cons.pick((LValue)MiscLib.fragmapv.ref(), "st"), Cons.vec2(Cons.l(0.019), Cons.l(0.018))),
                                          Cons.mul(MiscLib.time.ref(), Cons.vec2(Cons.l(-0.035), Cons.l(-0.025)))
                                       )
                                    ),
                                    "rgb"
                                 )
                              ),
                              Cons.add(
                                 Cons.pick(
                                    Cons.texture2D(
                                       BetterSurface.this.snrm.ref(),
                                       Cons.add(
                                          Cons.mul(Cons.pick((LValue)MiscLib.fragmapv.ref(), "st"), Cons.vec2(Cons.l(0.01), Cons.l(0.012))),
                                          Cons.add(Cons.mul(MiscLib.time.ref(), Cons.vec2(Cons.l(0.025), Cons.l(0.035))), Cons.vec2(Cons.l(0.5), Cons.l(0.5)))
                                       )
                                    ),
                                    "rgb"
                                 ),
                                 Cons.pick(
                                    Cons.texture2D(
                                       BetterSurface.this.snrm.ref(),
                                       Cons.add(
                                          Cons.mul(Cons.pick((LValue)MiscLib.fragmapv.ref(), "st"), Cons.vec2(Cons.l(0.019), Cons.l(0.018))),
                                          Cons.add(Cons.mul(MiscLib.time.ref(), Cons.vec2(Cons.l(-0.035), Cons.l(-0.025))), Cons.vec2(Cons.l(0.5), Cons.l(0.5)))
                                       )
                                    ),
                                    "rgb"
                                 )
                              ),
                              Cons.abs(Cons.sub(Cons.mod(MiscLib.time.ref(), Cons.l(2.0)), Cons.l(1.0)))
                           ),
                           Cons.l(1.0)
                        ),
                        Cons.vec3(Cons.l(0.0625), Cons.l(0.0625), Cons.l(1.0))
                     );
                  }
               };
               nmod.force();
               MiscLib.frageyen(prog.fctx)
                  .mod(
                     new Macro1<Expression>() {
                        public Expression expand(Expression in) {
                           Expression m = nmod.ref();
                           return Cons.add(
                              Cons.mul(Cons.pick(m, "x"), Cons.vec3(Cons.l(1.0), Cons.l(0.0), Cons.l(0.0))),
                              Cons.mul(Cons.pick(m, "y"), Cons.vec3(Cons.l(0.0), Cons.l(1.0), Cons.l(0.0))),
                              Cons.mul(Cons.pick(m, "z"), in)
                           );
                        }
                     },
                     -10
                  );
               prog.fctx
                  .fragcol
                  .mod(
                     new Macro1<Expression>() {
                        public Expression expand(Expression in) {
                           return Cons.mul(
                              in,
                              Cons.textureCube(
                                 BetterSurface.this.ssky.ref(),
                                 Cons.neg(
                                    Cons.mul(
                                       BetterSurface.this.icam.ref(), Cons.reflect(MiscLib.fragedir(prog.fctx).depref(), MiscLib.frageyen(prog.fctx).depref())
                                    )
                                 )
                              ),
                              Cons.l(0.4)
                           );
                        }
                     },
                     0
                  );
            }
         }
      };

      private BetterSurface() {
      }

      @Override
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
         this.reapply(g);
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

      @Override
      public ShaderMacro[] shaders() {
         return this.shaders;
      }

      @Override
      public boolean reqshaders() {
         return true;
      }

      @Override
      public void apply(GOut g) {
         if (g.st.prog == null) {
            super.apply(g);
         } else {
            this.papply(g);
         }
      }

      @Override
      public void unapply(GOut g) {
         if (!g.st.usedprog) {
            super.unapply(g);
         } else {
            this.punapply(g);
         }
      }
   }

   public static class Bottom extends MapMesh.Surface {
      final MapMesh m;
      final boolean[] s;
      int[] ed;
      final MapMesh.Scan ss;
      public static final MapMesh.DataID<WaterTile.Bottom> id = MapMesh.makeid(WaterTile.Bottom.class);

      public Bottom(MapMesh m) {
         m.getClass();
         super();
         this.m = m;
         Coord sz = m.sz;
         MCache map = m.map;
         MapMesh.Scan ds = new MapMesh.Scan(new Coord(-10, -10), sz.add(21, 21));
         this.ss = new MapMesh.Scan(new Coord(-9, -9), sz.add(19, 19));
         int[] d = new int[ds.l];
         this.s = new boolean[this.ss.l];
         this.ed = new int[this.ss.l];

         for (int y = ds.ul.y; y < ds.br.y; y++) {
            for (int x = ds.ul.y; x < ds.br.x; x++) {
               Tiler t = map.tiler(map.gettile(m.ul.add(x, y)));
               if (t instanceof WaterTile) {
                  d[ds.o(x, y)] = ((WaterTile)t).depth;
               } else {
                  d[ds.o(x, y)] = 0;
               }
            }
         }

         for (int y = this.ss.ul.y; y < this.ss.br.y; y++) {
            for (int xx = this.ss.ul.x; xx < this.ss.br.x; xx++) {
               int td = d[ds.o(xx, y)];
               if (d[ds.o(xx - 1, y - 1)] < td) {
                  td = d[ds.o(xx - 1, y - 1)];
               }

               if (d[ds.o(xx, y - 1)] < td) {
                  td = d[ds.o(xx, y - 1)];
               }

               if (d[ds.o(xx - 1, y)] < td) {
                  td = d[ds.o(xx - 1, y)];
               }

               this.ed[this.ss.o(xx, y)] = td;
               if (td == 0) {
                  this.s[this.ss.o(xx, y)] = true;
               }
            }
         }

         for (int i = 0; i < 8; i++) {
            int[] sd = new int[this.ss.l];

            for (int y = this.ss.ul.y + 1; y < this.ss.br.y - 1; y++) {
               for (int xx = this.ss.ul.x + 1; xx < this.ss.br.x - 1; xx++) {
                  if (this.s[this.ss.o(xx, y)]) {
                     sd[this.ss.o(xx, y)] = this.ed[this.ss.o(xx, y)];
                  } else {
                     sd[this.ss.o(xx, y)] = (
                           this.ed[this.ss.o(xx, y)] * 4
                              + this.ed[this.ss.o(xx - 1, y)]
                              + this.ed[this.ss.o(xx + 1, y)]
                              + this.ed[this.ss.o(xx, y - 1)]
                              + this.ed[this.ss.o(xx, y + 1)]
                        )
                        / 8;
                  }
               }
            }

            this.ed = sd;
         }

         for (int y = -1; y < sz.y + 2; y++) {
            for (int xxx = -1; xxx < sz.x + 2; xxx++) {
               Coord3f var10000 = this.spoint(new Coord(xxx, y)).pos;
               var10000.z = var10000.z - this.ed[this.ss.o(xxx, y)];
            }
         }
      }

      public int d(int x, int y) {
         return this.ed[this.ss.o(x, y)];
      }

      @Override
      public void calcnrm() {
         super.calcnrm();
         Coord c = new Coord();

         for (c.y = 0; c.y <= this.m.sz.y; c.y++) {
            for (c.x = 0; c.x <= this.m.sz.x; c.x++) {
               if (this.s[this.ss.o(c)]) {
                  this.spoint(c).nrm = this.m.gnd().spoint(c).nrm;
               }
            }
         }
      }
   }

   public static class BottomFog extends GLState.StandAlone {
      public static final double maxdepth = 25.0;
      public static final Color fogcolor = new Color(13, 38, 25);
      public static final Expression mfogcolor = Cons.mul(
         Cons.col3(fogcolor), Cons.pick((LValue)Cons.fref((LValue)Cons.idx(ProgramContext.gl_LightSource.ref(), MapView.amblight.ref()), "diffuse"), "rgb")
      );
      public static Function rgbmix = new Function.Def(Type.VEC4) {
         {
            Expression a = this.param(Function.PDir.IN, Type.VEC4).ref();
            Expression b = this.param(Function.PDir.IN, Type.VEC3).ref();
            Expression m = this.param(Function.PDir.IN, Type.FLOAT).ref();
            this.code.add(new Return(Cons.vec4(Cons.mix(Cons.pick(a, "rgb"), b, m), Cons.pick(a, "a"))));
         }
      };
      public static final Attribute depth = new Attribute(Type.FLOAT);
      public static final AutoVarying fragd = new AutoVarying(Type.FLOAT) {
         @Override
         protected Expression root(VertexContext vctx) {
            return WaterTile.BottomFog.depth.ref();
         }
      };
      private final ShaderMacro[] shaders = new ShaderMacro[]{
         new ShaderMacro() {
            @Override
            public void modify(ProgramContext prog) {
               prog.fctx
                  .fragcol
                  .mod(
                     new Macro1<Expression>() {
                        public Expression expand(Expression in) {
                           return WaterTile.BottomFog.rgbmix
                              .call(in, WaterTile.BottomFog.mfogcolor, Cons.min(Cons.div(WaterTile.BottomFog.fragd.ref(), Cons.l(25.0)), Cons.l(1.0)));
                        }
                     },
                     1000
                  );
            }
         }
      };

      private BottomFog() {
         super(GLState.Slot.Type.DRAW);
      }

      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public ShaderMacro[] shaders() {
         return this.shaders;
      }

      @Override
      public boolean reqshaders() {
         return true;
      }

      @Override
      public void prep(GLState.Buffer buf) {
         if (buf.cfg.pref.wsurf.val) {
            super.prep(buf);
         }
      }
   }

   public static class BottomPlane extends MapMesh.Plane {
      WaterTile.Bottom srf;
      Coord lc;

      public BottomPlane(MapMesh m, WaterTile.Bottom srf, Coord lc, int z, GLState mat, Tex tex) {
         m.getClass();
         super(srf.fortile(lc), z, mat, tex);
         this.srf = srf;
         this.lc = new Coord(lc);
      }

      @Override
      public void build(MeshBuf buf) {
         MeshBuf.Tex ta = buf.layer(MeshBuf.tex);
         MeshBuf.Vec1Layer da = buf.layer(WaterTile.depthlayer);
         MeshBuf.Vertex v1 = buf.new Vertex(this.vrt[0].pos, this.vrt[0].nrm);
         MeshBuf.Vertex v2 = buf.new Vertex(this.vrt[1].pos, this.vrt[1].nrm);
         MeshBuf.Vertex v3 = buf.new Vertex(this.vrt[2].pos, this.vrt[2].nrm);
         MeshBuf.Vertex v4 = buf.new Vertex(this.vrt[3].pos, this.vrt[3].nrm);
         ta.set(v1, new Coord3f(this.tex.tcx(this.texx[0]), this.tex.tcy(this.texy[0]), 0.0F));
         ta.set(v2, new Coord3f(this.tex.tcx(this.texx[1]), this.tex.tcy(this.texy[1]), 0.0F));
         ta.set(v3, new Coord3f(this.tex.tcx(this.texx[2]), this.tex.tcy(this.texy[2]), 0.0F));
         ta.set(v4, new Coord3f(this.tex.tcx(this.texx[3]), this.tex.tcy(this.texy[3]), 0.0F));
         da.set(v1, (float)this.srf.d(this.lc.x, this.lc.y));
         da.set(v2, (float)this.srf.d(this.lc.x, this.lc.y + 1));
         da.set(v3, (float)this.srf.d(this.lc.x + 1, this.lc.y + 1));
         da.set(v4, (float)this.srf.d(this.lc.x + 1, this.lc.y));
         MapMesh.splitquad(buf, v1, v2, v3, v4);
      }
   }

   @Tiler.ResName("water")
   public static class Fac implements Tiler.Factory {
      @Override
      public Tiler create(int id, Resource.Tileset set) {
         int a = 0;
         int depth = (Integer)set.ta[a++];
         Resource.Tileset ground = set;
         TerrainTile terrain = null;

         while (a < set.ta.length) {
            Object[] desc = (Object[])set.ta[a++];
            String p = (String)desc[0];
            if (p.equals("gnd")) {
               Resource gres = Resource.load((String)desc[1], (Integer)desc[2]);
               ground = gres.layer(Resource.tileset);
            } else if (p.equals("trn")) {
               Resource tres = Resource.load((String)desc[1], (Integer)desc[2]);
               Resource.Tileset tset = tres.layer(Resource.tileset);
               terrain = (TerrainTile)tset.tfac().create(-1, tset);
            }
         }

         return (Tiler)(terrain == null ? new WaterTile(id, set, depth, ground) : new WaterTile.TWaterTile(id, set, depth, terrain));
      }
   }

   public static class SimpleSurface extends GLState.StandAlone {
      private static States.DepthOffset soff = new States.DepthOffset(2.0F, 2.0F);
      GLState.TexUnit tsky;
      GLState.TexUnit tnrm;

      private SimpleSurface() {
         super(GLState.Slot.Type.DRAW, PView.cam, HavenPanel.global);
      }

      @Override
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

      @Override
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

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(States.color, null);
         buf.put(Light.lighting, null);
         soff.prep(buf);
         super.prep(buf);
      }
   }

   public static class TWaterTile extends WaterTile {
      private static final IDSet<GLState> bmats = new IDSet<>();
      public final TerrainTile bottom;

      public TWaterTile(int id, Resource.Tileset set, int depth, TerrainTile bottom) {
         super(id, set, depth);
         this.bottom = bottom;
      }

      @Override
      public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
         TerrainTile.Blend b = m.data(this.bottom.blend);

         for (int i = 0; i < this.bottom.var.length + 1; i++) {
            GLState mat = i == 0 ? this.bottom.base : this.bottom.var[i - 1].mat;
            mat = bmats.intern(GLState.compose(mat, waterfog, WaterTile.boff));
            if (b.en[i][b.es.o(lc)]) {
               new WaterTile.TWaterTile.BottomPlane(
                  m,
                  m.data(WaterTile.Bottom.id),
                  lc,
                  i,
                  mat,
                  new int[]{
                     (int)(b.bv[i][b.vs.o(lc)] * 255.0F),
                     (int)(b.bv[i][b.vs.o(lc.add(0, 1))] * 255.0F),
                     (int)(b.bv[i][b.vs.o(lc.add(1, 1))] * 255.0F),
                     (int)(b.bv[i][b.vs.o(lc.add(1, 0))] * 255.0F)
                  }
               );
            }
         }

         m.new Plane(m.gnd(), lc, 257, surfmat);
      }

      @Override
      public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {
      }

      public class BottomPlane extends TerrainTile.Plane {
         float[] depth;

         public BottomPlane(MapMesh m, WaterTile.Bottom srf, Coord lc, int z, GLState mat, int[] alpha) {
            TerrainTile var10001 = TWaterTile.this.bottom;
            TWaterTile.this.bottom.getClass();
            super(m, srf, lc, z, mat, alpha);
            this.depth = new float[]{srf.d(lc.x, lc.y), srf.d(lc.x, lc.y + 1), srf.d(lc.x + 1, lc.y + 1), srf.d(lc.x + 1, lc.y)};
         }

         @Override
         public MeshBuf.Vertex mkvert(MeshBuf buf, int n) {
            MeshBuf.Vertex v = super.mkvert(buf, n);
            buf.layer(WaterTile.depthlayer).set(v, this.depth[n]);
            return v;
         }
      }
   }
}
