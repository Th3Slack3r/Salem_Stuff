package haven.resutil;

import haven.Coord;
import haven.Coord3f;
import haven.FastMesh;
import haven.MCache;
import haven.MapMesh;
import haven.MeshBuf;
import haven.Resource;
import haven.Tex;
import java.lang.reflect.Array;
import java.util.Random;

public class RidgeTile extends GroundTile {
   public final int[] breaks;
   public final Resource[] walls;
   public final Resource[] lcorn;
   public final Resource[] rcorn;
   public final Resource[] strans;
   public final Resource[] c1trans;
   public final Resource[] c2trans;
   private static final MapMesh.DataID<RidgeTile.Ridges> rid = MapMesh.makeid(RidgeTile.Ridges.class);
   private static final int[] cwx = new int[]{0, 1, 1, 0};
   private static final int[] cwy = new int[]{0, 0, 1, 1};
   private static final int[] ecwx = new int[]{0, 1, 0, -1};
   private static final int[] ecwy = new int[]{-1, 0, 1, 0};

   public RidgeTile(
      int id,
      Resource.Tileset set,
      int[] breaks,
      Resource[] walls,
      Resource[] lcorn,
      Resource[] rcorn,
      Resource[] strans,
      Resource[] c1trans,
      Resource[] c2trans
   ) {
      super(id, set);
      this.breaks = breaks;
      this.walls = walls;
      this.lcorn = lcorn;
      this.rcorn = rcorn;
      this.strans = strans;
      this.c1trans = c1trans;
      this.c2trans = c2trans;
   }

   public boolean[] breaks(MapMesh m, Coord gc, int diff) {
      int z00 = m.map.getz(gc);
      int z10 = m.map.getz(gc.add(1, 0));
      int z01 = m.map.getz(gc.add(0, 1));
      int z11 = m.map.getz(gc.add(1, 1));
      return new boolean[]{Math.abs(z00 - z10) >= diff, Math.abs(z10 - z11) >= diff, Math.abs(z11 - z01) >= diff, Math.abs(z01 - z00) >= diff};
   }

   public boolean isend(MapMesh m, Coord gc, boolean[] b) {
      return (b[0] ? 1 : 0) + (b[1] ? 1 : 0) + (b[2] ? 1 : 0) + (b[3] ? 1 : 0) == 1;
   }

   public boolean isstraight(MapMesh m, Coord gc, boolean[] b) {
      return b[0] && b[2] && !b[1] && !b[3] || b[1] && b[3] && !b[0] && !b[2];
   }

   private static <T> T[] shift(T[] a, int n) {
      T[] r = (T[])Array.newInstance(a.getClass().getComponentType(), a.length);

      for (int i = 0; i < a.length; i++) {
         r[(i + n) % a.length] = a[i];
      }

      return r;
   }

   public void makewall(MapMesh m, Coord3f ul, Coord3f bl, Coord3f br, Coord3f ur, Resource wall, float w) {
      float hw = w / 2.0F;
      double tx = br.x - bl.x;
      double ty = br.y - bl.y;
      double lf = 1.0 / Math.sqrt(tx * tx + ty * ty);
      float xbx = (float)(tx * lf);
      float xby = (float)(ty * lf);
      float lzof = (float)((br.z - bl.z) * lf);
      float lzsf = (float)((ur.z - br.z - ul.z + bl.z) * lf / 11.0);
      float lzs = (float)((ul.z - bl.z) / 11.0);
      float rzof = (float)((bl.z - br.z) * lf);
      float rzsf = (float)((ul.z - bl.z - ur.z + br.z) * lf / 11.0);
      float rzs = (float)((ur.z - br.z) / 11.0);
      float tys = (int)((ul.z - bl.z + 5.0F) / 11.0F);
      float tysf = (float)(((int)((ur.z - br.z + 5.0F) / 11.0F) - tys) * lf);
      float ybx = -xby;
      float yby = xbx;

      for (FastMesh.MeshRes r : wall.layers(FastMesh.MeshRes.class)) {
         MeshBuf buf = MapMesh.Models.get(m, r.mat.get());
         MeshBuf.Tex ta = buf.layer(MeshBuf.tex);
         MeshBuf.Vertex[] vs = buf.copy(r.m);

         for (MeshBuf.Vertex v : vs) {
            float x = v.pos.x;
            float y = v.pos.y;
            float z = v.pos.z;
            v.pos.x = x * xbx + y * ybx + bl.x;
            v.pos.y = x * xby + y * yby + bl.y;
            if (x < hw) {
               v.pos.z = lzof * x + (lzs + lzsf * x) * z + bl.z;
            } else {
               float X = w - x;
               v.pos.z = rzof * X + (rzs + rzsf * X) * z + br.z;
            }

            float nx = v.nrm.x;
            float ny = v.nrm.y;
            v.nrm.x = nx * xbx + ny * ybx;
            v.nrm.y = nx * xby + ny * yby;
            ta.get(v).y = (tys + tysf * x) * ta.get(v).y;
         }
      }
   }

   public void remapquad(RidgeTile.Ridges.Tile.TilePlane p, int q) {
      p.u = cwy[q] * 0.5F;
      p.l = cwx[q] * 0.5F;
      p.b = p.u + 0.5F;
      p.r = p.l + 0.5F;
   }

   public void remaphalf(RidgeTile.Ridges.Tile.TilePlane p, int fq) {
      int l = Math.min(cwx[fq], cwx[(fq + 1) % 4]);
      int r = Math.max(cwx[fq], cwx[(fq + 1) % 4]) + 1;
      int t = Math.min(cwy[fq], cwy[(fq + 1) % 4]);
      int b = Math.max(cwy[fq], cwy[(fq + 1) % 4]) + 1;
      p.u = t * 0.5F;
      p.l = l * 0.5F;
      p.b = b * 0.5F;
      p.r = r * 0.5F;
   }

   private void layend(MapMesh m, Random rnd, Coord lc, Coord gc, int dir) {
      MapMesh.Surface g = m.gnd();
      MapMesh.SPoint bl = g.spoint(lc.add(cwx[dir], cwy[dir]));
      MapMesh.SPoint br = g.spoint(lc.add(cwx[(dir + 1) % 4], cwy[(dir + 1) % 4]));
      MapMesh.SPoint fr = g.spoint(lc.add(cwx[(dir + 2) % 4], cwy[(dir + 2) % 4]));
      MapMesh.SPoint fl = g.spoint(lc.add(cwx[(dir + 3) % 4], cwy[(dir + 3) % 4]));
      boolean cw = bl.pos.z > br.pos.z;
      MapMesh.SPoint bu = new MapMesh.SPoint(bl.pos.add(br.pos).mul(0.5F));
      MapMesh.SPoint bb = new MapMesh.SPoint(bl.pos.add(br.pos).mul(0.5F));
      MapMesh.SPoint fm = new MapMesh.SPoint(fl.pos.add(fr.pos).mul(0.5F));
      RidgeTile.Ridges r = m.data(rid);
      RidgeTile.Ridges.Tile tile = r.new Tile();
      RidgeTile.Ridges.Tile.TilePlane left;
      RidgeTile.Ridges.Tile.TilePlane right;
      MapMesh.SPoint[] uh;
      if (cw) {
         bu.pos.z = bl.pos.z;
         bb.pos.z = br.pos.z;
         left = tile.new TilePlane(uh = shift(new MapMesh.SPoint[]{fl, fm, bu, bl}, 5 - dir));
         right = tile.new TilePlane(shift(new MapMesh.SPoint[]{fm, fr, br, bb}, 5 - dir));
      } else {
         bu.pos.z = br.pos.z;
         bb.pos.z = bl.pos.z;
         left = tile.new TilePlane(shift(new MapMesh.SPoint[]{fl, fm, bb, bl}, 5 - dir));
         right = tile.new TilePlane(uh = shift(new MapMesh.SPoint[]{fm, fr, br, bu}, 5 - dir));
      }

      this.remaphalf(left, (dir + 3) % 4);
      this.remaphalf(right, (dir + 1) % 4);
      r.set(lc, tile);
      tile.layover(0, this.set.ground.pick(rnd));
      m.new Plane(uh, 256, this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc).tex(), false).texrot(null, null, 1 + dir + (cw ? 2 : 0), false);
      if (cw) {
         this.makewall(m, fm.pos, fm.pos, bb.pos, bu.pos, this.walls[rnd.nextInt(this.walls.length)], 11.0F);
      } else {
         this.makewall(m, bu.pos, bb.pos, fm.pos, fm.pos, this.walls[rnd.nextInt(this.walls.length)], 11.0F);
      }
   }

   public void layend(MapMesh m, Random rnd, Coord lc, Coord gc, boolean[] b) {
      for (int dir = 0; dir < 4; dir++) {
         if (b[dir]) {
            this.layend(m, rnd, lc, gc, dir);
            return;
         }
      }
   }

   public void layridge(MapMesh m, Random rnd, Coord lc, Coord gc, boolean[] b) {
      int z00 = m.map.getz(gc);
      int z10 = m.map.getz(gc.add(1, 0));
      int z01 = m.map.getz(gc.add(0, 1));
      int z11 = m.map.getz(gc.add(1, 1));
      int dir = b[0] ? (z00 > z10 ? 0 : 2) : (z00 > z01 ? 1 : 3);
      boolean tb1 = m.map.tiler(m.map.gettile(gc.add(ecwx[dir], ecwy[dir]))) instanceof RidgeTile;
      boolean tb2 = m.map.tiler(m.map.gettile(gc.add(ecwx[(dir + 2) % 4], ecwy[(dir + 2) % 4]))) instanceof RidgeTile;
      if (tb1 || tb2) {
         if (!tb1) {
            this.layend(m, rnd, lc, gc, (dir + 2) % 4);
            return;
         }

         if (!tb2) {
            this.layend(m, rnd, lc, gc, dir);
         }
      }

      MapMesh.Surface g = m.gnd();
      MapMesh.SPoint ur = g.spoint(lc.add(cwx[dir], cwy[dir]));
      MapMesh.SPoint br = g.spoint(lc.add(cwx[(dir + 1) % 4], cwy[(dir + 1) % 4]));
      MapMesh.SPoint bl = g.spoint(lc.add(cwx[(dir + 2) % 4], cwy[(dir + 2) % 4]));
      MapMesh.SPoint ul = g.spoint(lc.add(cwx[(dir + 3) % 4], cwy[(dir + 3) % 4]));
      MapMesh.SPoint mlu = new MapMesh.SPoint(ul.pos.add(bl.pos).mul(0.5F));
      MapMesh.SPoint mlb = new MapMesh.SPoint(ul.pos.add(bl.pos).mul(0.5F));
      MapMesh.SPoint mru = new MapMesh.SPoint(ur.pos.add(br.pos).mul(0.5F));
      MapMesh.SPoint mrb = new MapMesh.SPoint(ur.pos.add(br.pos).mul(0.5F));
      mlu.pos.z = ul.pos.z;
      mru.pos.z = ur.pos.z;
      mlb.pos.z = bl.pos.z;
      mrb.pos.z = br.pos.z;
      RidgeTile.Ridges r = m.data(rid);
      RidgeTile.Ridges.Tile tile = r.new Tile();
      RidgeTile.Ridges.Tile.TilePlane upper = tile.new TilePlane(shift(new MapMesh.SPoint[]{ul, mlu, mru, ur}, 5 - dir));
      RidgeTile.Ridges.Tile.TilePlane lower = tile.new TilePlane(shift(new MapMesh.SPoint[]{mlb, bl, br, mrb}, 5 - dir));
      this.remaphalf(upper, (dir + 3) % 4);
      this.remaphalf(lower, (dir + 1) % 4);
      r.set(lc, tile);
      tile.layover(0, this.set.ground.pick(rnd));
      m.new Plane(upper.vrt, 256, this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc).tex(), false).texrot(null, null, 3 + dir, false);
      this.makewall(m, mlu.pos, mlb.pos, mrb.pos, mru.pos, this.walls[rnd.nextInt(this.walls.length)], 11.0F);
   }

   public void mkcornwall(MapMesh m, Random rnd, Coord3f ul, Coord3f bl, Coord3f br, Coord3f ur, boolean cw) {
      if (cw) {
         this.makewall(m, ul, bl, br, ur, this.lcorn[rnd.nextInt(this.lcorn.length)], 5.5F);
      } else {
         this.makewall(m, ul, bl, br, ur, this.rcorn[rnd.nextInt(this.rcorn.length)], 5.5F);
      }
   }

   public void laycomplex(MapMesh m, Random rnd, Coord lc, Coord gc, boolean[] b) {
      MapMesh.Surface g = m.gnd();
      MapMesh.SPoint[] crn = new MapMesh.SPoint[]{g.spoint(lc), g.spoint(lc.add(1, 0)), g.spoint(lc.add(1, 1)), g.spoint(lc.add(0, 1))};
      int s = 0;

      while (!b[s]) {
         s++;
      }

      s = (s + 1) % 4;
      MapMesh.SPoint[] ct = new MapMesh.SPoint[4];
      MapMesh.SPoint[] h1 = new MapMesh.SPoint[4];
      MapMesh.SPoint[] h2 = new MapMesh.SPoint[4];
      int r = s;

      for (int tile = 0; tile < 4; tile++) {
         if (!b[(r + 3) % 4]) {
            h1[r] = h2[(r + 3) % 4];
            h1[r].pos.z = (h1[r].pos.z + crn[r].pos.z) * 0.5F;
         } else {
            h1[r] = new MapMesh.SPoint(crn[(r + 3) % 4].pos.add(crn[r].pos).mul(0.5F));
            h1[r].pos.z = crn[r].pos.z;
         }

         h2[r] = new MapMesh.SPoint(crn[(r + 1) % 4].pos.add(crn[r].pos).mul(0.5F));
         h2[r].pos.z = crn[r].pos.z;
         r = (r + 1) % 4;
      }

      MapMesh.SPoint cc = null;
      int i = s;

      for (int n = 0; n < 4; n++) {
         if (cc == null) {
            cc = new MapMesh.SPoint(crn[0].pos.add(crn[1].pos).add(crn[2].pos).add(crn[3].pos).mul(0.25F));
            if (b[i]) {
               cc.pos.z = crn[i].pos.z;
            } else {
               cc.pos.z = (h1[i].pos.z + h2[(i + 1) % 4].pos.z) * 0.5F;
            }
         }

         ct[i] = cc;
         if (b[i]) {
            cc = null;
         }

         i = (i + 1) % 4;
      }

      i = s;

      for (int n = 0; n < 4; n++) {
         if (b[i] && !(m.map.tiler(m.map.gettile(gc.add(ecwx[i], ecwy[i]))) instanceof RidgeTile)) {
            h2[i].pos.z = (h2[i].pos.z + h1[(i + 1) % 4].pos.z) * 0.5F;
            h1[(i + 1) % 4] = h2[i];
         }

         i = (i + 1) % 4;
      }

      RidgeTile.Ridges rx = m.data(rid);
      RidgeTile.Ridges.Tile tile = rx.new Tile();
      boolean cont = false;
      int ix = s;

      for (int n = 0; n < 4; n++) {
         if (cont) {
            cont = false;
         } else if (!b[ix] && b[(ix + 1) % 4] && b[(ix + 3) % 4]) {
            RidgeTile.Ridges.Tile.TilePlane pl = tile.new TilePlane(shift(new MapMesh.SPoint[]{crn[ix], h1[ix], h2[(ix + 1) % 4], crn[(ix + 1) % 4]}, 4 - ix));
            this.remaphalf(pl, ix);
            cont = true;
            MapMesh.SPoint pc = ct[(ix + 3) % 4];
            MapMesh.SPoint ccx = ct[ix];
            if (pc.pos.z > ccx.pos.z) {
               this.mkcornwall(m, rnd, pc.pos, ccx.pos, h1[ix].pos, h2[(ix + 3) % 4].pos, true);
            } else {
               this.mkcornwall(m, rnd, h1[ix].pos, h2[(ix + 3) % 4].pos, pc.pos, ccx.pos, false);
               m.new Plane(pl.vrt, 256, this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc).tex(), false).texrot(null, null, ix, false);
            }
         } else {
            RidgeTile.Ridges.Tile.TilePlane pl = tile.new TilePlane(shift(new MapMesh.SPoint[]{crn[ix], h1[ix], ct[ix], h2[ix]}, 4 - ix));
            this.remapquad(pl, ix);
            boolean[] ub = new boolean[4];
            boolean[] db = new boolean[4];
            boolean[] tb = new boolean[4];

            for (int o = 0; o < 4; o++) {
               int u = (ix + o) % 4;
               tb[o] = b[u];
               ub[o] = b[u] && h2[u].pos.z < h1[(u + 1) % 4].pos.z;
               db[o] = b[u] && h2[u].pos.z > h1[(u + 1) % 4].pos.z;
            }

            if (ub[3] && db[0]) {
               m.new Plane(pl.vrt, 256, this.c1trans[rnd.nextInt(this.c1trans.length)].layer(Resource.imgc).tex(), false).texrot(null, null, ix, false);
            } else if (!tb[0] && !tb[3] && db[1] && ub[2]) {
               m.new Plane(pl.vrt, 256, this.c2trans[rnd.nextInt(this.c2trans.length)].layer(Resource.imgc).tex(), false).texrot(null, null, ix, false);
            } else if (ub[3] && !db[0]) {
               Tex t = this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc).tex();
               m.new Plane(pl.vrt, 256, t, false).texrot(Coord.z, new Coord(t.sz().x / 2, t.sz().y), ix, false);
            } else if (!ub[3] && db[0]) {
               Tex t = this.strans[rnd.nextInt(this.strans.length)].layer(Resource.imgc).tex();
               m.new Plane(pl.vrt, 256, t, false).texrot(Coord.z, new Coord(t.sz().x / 2, t.sz().y), ix + 3, false);
            }

            if (b[(ix + 3) % 4]) {
               MapMesh.SPoint pc = ct[(ix + 3) % 4];
               MapMesh.SPoint ccx = ct[ix];
               if (pc.pos.z > ccx.pos.z) {
                  this.mkcornwall(m, rnd, pc.pos, ccx.pos, h1[ix].pos, h2[(ix + 3) % 4].pos, true);
               } else {
                  this.mkcornwall(m, rnd, h1[ix].pos, h2[(ix + 3) % 4].pos, pc.pos, ccx.pos, false);
               }
            }
         }

         ix = (ix + 1) % 4;
      }

      rx.set(lc, tile);
      tile.layover(0, this.set.ground.pick(rnd));
   }

   @Override
   public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
      boolean[] b = this.breaks(m, gc, this.breaks[0]);
      if (!b[0] && !b[1] && !b[2] && !b[3]) {
         super.lay(m, rnd, lc, gc);
      } else if (this.isend(m, gc, b)) {
         this.layend(m, rnd, lc, gc, b);
      } else if (this.isstraight(m, gc, b)) {
         this.layridge(m, rnd, lc, gc, b);
      } else {
         this.laycomplex(m, rnd, lc, gc, b);
      }
   }

   @Override
   public void layover(MapMesh m, Coord lc, Coord gc, int z, Resource.Tile t) {
      boolean[] b = this.breaks(m, gc, this.breaks[0]);
      if (!b[0] && !b[1] && !b[2] && !b[3]) {
         super.layover(m, lc, gc, z, t);
      } else {
         RidgeTile.Ridges.Tile tile = m.data(rid).get(lc);
         if (tile == null) {
            throw new NullPointerException("Ridged tile has not been properly initialized");
         }

         tile.layover(z, t);
      }
   }

   public boolean ridgep(MCache map, Coord tc) {
      int z00 = map.getz(tc);
      int z10 = map.getz(tc.add(1, 0));
      int z01 = map.getz(tc.add(0, 1));
      int z11 = map.getz(tc.add(1, 1));
      int diff = this.breaks[0];
      return Math.abs(z00 - z10) >= diff || Math.abs(z10 - z11) >= diff || Math.abs(z11 - z01) >= diff || Math.abs(z01 - z00) >= diff;
   }

   public static class Ridges extends MapMesh.Hooks {
      public final MapMesh m;
      private final RidgeTile.Ridges.Tile[] tiles;

      public Ridges(MapMesh m) {
         this.m = m;
         this.tiles = new RidgeTile.Ridges.Tile[m.sz.x * m.sz.y];
      }

      public RidgeTile.Ridges.Tile get(Coord c) {
         return this.tiles[c.x + this.m.sz.x * c.y];
      }

      public void set(Coord c, RidgeTile.Ridges.Tile t) {
         this.tiles[c.x + this.m.sz.x * c.y] = t;
      }

      @Override
      public void postcalcnrm(Random rnd) {
      }

      public class Tile {
         public RidgeTile.Ridges.Tile.TilePlane[] planes = new RidgeTile.Ridges.Tile.TilePlane[4];
         int n;

         public void layover(int z, Resource.Tile tile) {
            int w = tile.tex().sz().x;
            int h = tile.tex().sz().y;

            for (int i = 0; i < this.n; i++) {
               MapMesh.Plane p = Ridges.this.m.new Plane(this.planes[i].vrt, z, tile.tex(), tile.t == 'g');
               p.texrot(
                  new Coord((int)(w * this.planes[i].l), (int)(h * this.planes[i].u)),
                  new Coord((int)(w * this.planes[i].r), (int)(h * this.planes[i].b)),
                  0,
                  false
               );
            }
         }

         public class TilePlane {
            public MapMesh.SPoint[] vrt;
            public float u;
            public float l;
            public float b;
            public float r;

            public TilePlane(MapMesh.SPoint[] vrt) {
               this.vrt = vrt;
               this.u = this.l = 0.0F;
               this.b = this.r = 1.0F;
               Tile.this.planes[Tile.this.n++] = this;
            }
         }
      }
   }
}
