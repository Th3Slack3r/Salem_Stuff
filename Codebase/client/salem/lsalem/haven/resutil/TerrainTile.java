package haven.resutil;

import haven.Coord;
import haven.Coord3f;
import haven.GLState;
import haven.IDSet;
import haven.MapMesh;
import haven.Material;
import haven.MeshBuf;
import haven.Resource;
import haven.SNoise3;
import haven.States;
import haven.Tex;
import haven.TexGL;
import haven.TexSI;
import haven.Tiler;
import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class TerrainTile extends Tiler {
   public final GLState base;
   public final SNoise3 noise;
   public final TerrainTile.Var[] var;
   public final Resource.Tileset transset;
   private static final int sr = 12;
   public final MapMesh.DataID<TerrainTile.Blend> blend = new MapMesh.DataID<TerrainTile.Blend>() {
      public TerrainTile.Blend make(MapMesh m) {
         return TerrainTile.this.new Blend(m);
      }
   };
   private static final Map<TexGL, AlphaTex> transtex = new WeakHashMap<>();
   private static final IDSet<GLState> transmats = new IDSet<>();

   public TerrainTile(int id, long nseed, GLState base, TerrainTile.Var[] var, Resource.Tileset transset) {
      super(id);
      this.noise = new SNoise3(nseed);
      this.base = GLState.compose(base, States.vertexcolor);

      for (TerrainTile.Var v : this.var = var) {
         v.mat = GLState.compose(v.mat, States.vertexcolor);
      }

      this.transset = transset;
   }

   @Override
   public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
      TerrainTile.Blend b = m.data(this.blend);

      for (int i = 0; i < this.var.length + 1; i++) {
         GLState mat = i == 0 ? this.base : this.var[i - 1].mat;
         if (b.en[i][b.es.o(lc)]) {
            new TerrainTile.Plane(
               m,
               m.gnd(),
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
   }

   private void laytrans(MapMesh m, Coord lc, int z, Resource.Tile t) {
      TerrainTile.Blend b = m.data(this.blend);

      for (int i = 0; i < this.var.length + 1; i++) {
         GLState mat = i == 0 ? this.base : this.var[i - 1].mat;
         Tex tt = t.tex();
         TexGL gt;
         if (tt instanceof TexGL) {
            gt = (TexGL)tt;
         } else {
            if (!(tt instanceof TexSI) || !(((TexSI)tt).parent instanceof TexGL)) {
               throw new RuntimeException("Cannot use texture for transitions: " + tt);
            }

            gt = (TexGL)((TexSI)tt).parent;
         }

         AlphaTex alpha;
         synchronized (transtex) {
            if ((alpha = transtex.get(gt)) == null) {
               transtex.put(gt, alpha = new AlphaTex(gt, 0.01F));
            }
         }

         mat = transmats.intern(GLState.compose(mat, alpha));
         if (b.en[i][b.es.o(lc)]) {
            new TerrainTile.TransPlane(
               m,
               m.gnd(),
               lc,
               z + i,
               mat,
               new int[]{
                  (int)(b.bv[i][b.vs.o(lc)] * 255.0F),
                  (int)(b.bv[i][b.vs.o(lc.add(0, 1))] * 255.0F),
                  (int)(b.bv[i][b.vs.o(lc.add(1, 1))] * 255.0F),
                  (int)(b.bv[i][b.vs.o(lc.add(1, 0))] * 255.0F)
               },
               tt
            );
         }
      }
   }

   @Override
   public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {
      if (this.transset != null) {
         if (m.map.gettile(gc) > this.id) {
            if (this.transset.btrans != null && bmask > 0) {
               this.laytrans(m, lc, z, this.transset.btrans[bmask - 1].pick(rnd));
            }

            if (this.transset.ctrans != null && cmask > 0) {
               this.laytrans(m, lc, z, this.transset.ctrans[cmask - 1].pick(rnd));
            }
         }
      }
   }

   public class Blend {
      final MapMesh m;
      final MapMesh.Scan vs;
      final MapMesh.Scan es;
      final float[][] bv;
      final boolean[][] en;

      private Blend(MapMesh m) {
         this.m = m;
         this.vs = new MapMesh.Scan(Coord.z.sub(12, 12), m.sz.add(25, 25));
         float[][] buf1 = new float[TerrainTile.this.var.length + 1][this.vs.l];
         float[][] lwc = new float[TerrainTile.this.var.length + 1][this.vs.l];

         for (int i = 0; i < TerrainTile.this.var.length + 1; i++) {
            for (int y = this.vs.ul.y; y < this.vs.br.y; y++) {
               for (int x = this.vs.ul.x; x < this.vs.br.x; x++) {
                  lwc[i][this.vs.o(x, y)] = (float)TerrainTile.this.noise.getr(0.5, 1.5, 32.0, x + m.ul.x, y + m.ul.y, i * 23);
               }
            }
         }

         this.setbase(buf1);

         for (int i = 0; i < 12; i++) {
            float[][] buf2 = new float[TerrainTile.this.var.length + 1][this.vs.l];

            for (int y = this.vs.ul.y; y < this.vs.br.y; y++) {
               for (int x = this.vs.ul.x; x < this.vs.br.x; x++) {
                  for (int o = 0; o < TerrainTile.this.var.length + 1; o++) {
                     float s = buf1[o][this.vs.o(x, y)] * 4.0F;
                     float w = 4.0F;
                     float lw = lwc[o][this.vs.o(x, y)];
                     if (lw < 0.0F) {
                        lw = lw * lw * lw;
                     } else {
                        lw *= lw;
                     }

                     if (x > this.vs.ul.x) {
                        s += buf1[o][this.vs.o(x - 1, y)] * lw;
                        w += lw;
                     }

                     if (y > this.vs.ul.y) {
                        s += buf1[o][this.vs.o(x, y - 1)] * lw;
                        w += lw;
                     }

                     if (x < this.vs.br.x - 1) {
                        s += buf1[o][this.vs.o(x + 1, y)] * lw;
                        w += lw;
                     }

                     if (y < this.vs.br.y - 1) {
                        s += buf1[o][this.vs.o(x, y + 1)] * lw;
                        w += lw;
                     }

                     buf2[o][this.vs.o(x, y)] = s / w;
                  }
               }
            }

            buf1 = buf2;
         }

         this.bv = buf1;

         for (int y = this.vs.ul.y; y < this.vs.br.y; y++) {
            for (int x = this.vs.ul.x; x < this.vs.br.x; x++) {
               for (int i = 0; i < TerrainTile.this.var.length + 1; i++) {
                  float v = this.bv[i][this.vs.o(x, y)];
                  v = v * 1.2F - 0.1F;
                  if (v < 0.0F) {
                     v = 0.0F;
                  } else if (v > 1.0F) {
                     v = 1.0F;
                  } else {
                     v = 0.25F + 0.75F * v;
                  }

                  this.bv[i][this.vs.o(x, y)] = v;
               }
            }
         }

         this.es = new MapMesh.Scan(Coord.z, m.sz);
         this.en = new boolean[TerrainTile.this.var.length + 1][this.es.l];

         for (int y = this.es.ul.y; y < this.es.br.y; y++) {
            for (int x = this.es.ul.x; x < this.es.br.x; x++) {
               boolean fall = false;

               for (int i = TerrainTile.this.var.length; i >= 0; i--) {
                  if (fall) {
                     this.en[i][this.es.o(x, y)] = false;
                  } else if (this.bv[i][this.vs.o(x, y)] < 0.001F
                     && this.bv[i][this.vs.o(x + 1, y)] < 0.001F
                     && this.bv[i][this.vs.o(x, y + 1)] < 0.001F
                     && this.bv[i][this.vs.o(x + 1, y + 1)] < 0.001F) {
                     this.en[i][this.es.o(x, y)] = false;
                  } else {
                     this.en[i][this.es.o(x, y)] = true;
                     if (this.bv[i][this.vs.o(x, y)] > 0.99F
                        && this.bv[i][this.vs.o(x + 1, y)] > 0.99F
                        && this.bv[i][this.vs.o(x, y + 1)] > 0.99F
                        && this.bv[i][this.vs.o(x + 1, y + 1)] > 0.99F) {
                        fall = true;
                     }
                  }
               }
            }
         }
      }

      private void setbase(float[][] bv) {
         for (int y = this.vs.ul.y; y < this.vs.br.y - 1; y++) {
            label41:
            for (int x = this.vs.ul.x; x < this.vs.br.x - 1; x++) {
               for (int i = TerrainTile.this.var.length - 1; i >= 0; i--) {
                  TerrainTile.Var v = TerrainTile.this.var[i];
                  double n = 0.0;

                  for (double s = 64.0; s >= 8.0; s /= 2.0) {
                     n += TerrainTile.this.noise.get(s, x + this.m.ul.x, y + this.m.ul.y, v.nz);
                  }

                  if (n / 2.0 >= v.thrl && n / 2.0 <= v.thrh) {
                     bv[i + 1][this.vs.o(x, y)] = 1.0F;
                     bv[i + 1][this.vs.o(x + 1, y)] = 1.0F;
                     bv[i + 1][this.vs.o(x, y + 1)] = 1.0F;
                     bv[i + 1][this.vs.o(x + 1, y + 1)] = 1.0F;
                     continue label41;
                  }
               }

               bv[0][this.vs.o(x, y)] = 1.0F;
               bv[0][this.vs.o(x + 1, y)] = 1.0F;
               bv[0][this.vs.o(x, y + 1)] = 1.0F;
               bv[0][this.vs.o(x + 1, y + 1)] = 1.0F;
            }
         }
      }
   }

   @Tiler.ResName("trn")
   public static class Factory implements Tiler.Factory {
      @Override
      public Tiler create(int id, Resource.Tileset set) {
         Resource res = set.getres();
         Resource.Tileset trans = null;
         Material base = null;
         Collection<TerrainTile.Var> var = new LinkedList<>();

         for (Object rdesc : set.ta) {
            Object[] desc = (Object[])rdesc;
            String p = (String)desc[0];
            if (p.equals("base")) {
               int mid = (Integer)desc[1];
               base = res.layer(Material.Res.class, mid).get();
            } else if (p.equals("var")) {
               int mid = (Integer)desc[1];
               double thrl;
               double thrh;
               if (desc[2] instanceof Object[]) {
                  thrl = ((Float)((Object[])desc[2])[0]).floatValue();
                  thrh = ((Float)((Object[])desc[2])[1]).floatValue();
               } else {
                  thrl = ((Float)desc[2]).floatValue();
                  thrh = Double.MAX_VALUE;
               }

               double nz = res.name.hashCode() * mid * 8129 % 10000;
               var.add(new TerrainTile.Var(res.layer(Material.Res.class, mid).get(), thrl, thrh, nz));
            } else if (p.equals("trans")) {
               Resource tres = Resource.load((String)desc[1], (Integer)desc[2]);
               trans = tres.layer(Resource.tileset);
            }
         }

         return new TerrainTile(id, res.name.hashCode(), base, var.toArray(new TerrainTile.Var[0]), trans);
      }
   }

   public class Plane extends MapMesh.Shape {
      public Coord lc;
      public MapMesh.SPoint[] vrt;
      public Coord3f[] tc;
      public int[] alpha;

      public Plane(MapMesh m, MapMesh.Surface surf, Coord sc, int z, GLState mat, int[] alpha) {
         m.getClass();
         super(z, mat);
         this.lc = new Coord(sc);
         this.vrt = surf.fortile(sc);
         float fac = 6.25F;
         this.tc = new Coord3f[]{
            new Coord3f((sc.x + 0) / fac, (sc.y + 0) / fac, 0.0F),
            new Coord3f((sc.x + 0) / fac, (sc.y + 1) / fac, 0.0F),
            new Coord3f((sc.x + 1) / fac, (sc.y + 1) / fac, 0.0F),
            new Coord3f((sc.x + 1) / fac, (sc.y + 0) / fac, 0.0F)
         };
         m.data(BumpMap.MapTangents.id);
         this.alpha = alpha;
      }

      public MeshBuf.Vertex mkvert(MeshBuf buf, int n) {
         MeshBuf.Vertex v = buf.new Vertex(this.vrt[n].pos, this.vrt[n].nrm);
         buf.layer(MeshBuf.tex).set(v, this.tc[n]);
         buf.layer(MeshBuf.col).set(v, new Color(255, 255, 255, this.alpha[n]));
         return v;
      }

      @Override
      public void build(MeshBuf buf) {
         MeshBuf.Vertex v1 = this.mkvert(buf, 0);
         MeshBuf.Vertex v2 = this.mkvert(buf, 1);
         MeshBuf.Vertex v3 = this.mkvert(buf, 2);
         MeshBuf.Vertex v4 = this.mkvert(buf, 3);
         this.m().data(BumpMap.MapTangents.id).set(buf, this.lc, v1, v2, v3, v4);
         MapMesh.splitquad(buf, v1, v2, v3, v4);
      }
   }

   public class TransPlane extends TerrainTile.Plane {
      public Coord3f[] cc;

      public TransPlane(MapMesh m, MapMesh.Surface surf, Coord sc, int z, GLState mat, int[] alpha, Tex tex) {
         super(m, surf, sc, z, mat, alpha);
         Coord s = tex.sz();
         this.cc = new Coord3f[]{
            new Coord3f(tex.tcx(0), tex.tcy(0), 0.0F),
            new Coord3f(tex.tcx(0), tex.tcy(s.y), 0.0F),
            new Coord3f(tex.tcx(s.x), tex.tcy(s.y), 0.0F),
            new Coord3f(tex.tcx(s.x), tex.tcy(0), 0.0F)
         };
      }

      @Override
      public MeshBuf.Vertex mkvert(MeshBuf buf, int n) {
         MeshBuf.Vertex v = super.mkvert(buf, n);
         buf.layer(AlphaTex.lclip).set(v, this.cc[n]);
         return v;
      }
   }

   public static class Var {
      public GLState mat;
      public double thrl;
      public double thrh;
      public double nz;

      public Var(GLState mat, double thrl, double thrh, double nz) {
         this.mat = mat;
         this.thrl = thrl;
         this.thrh = thrh;
         this.nz = nz;
      }
   }
}
