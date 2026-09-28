package haven.resutil;

import haven.Coord;
import haven.MapMesh;
import haven.Resource;
import haven.Tiler;
import java.util.Random;

public class GroundTile extends Tiler {
   public final Resource.Tileset set;

   public GroundTile(int id, Resource.Tileset set) {
      super(id);
      this.set = set;
   }

   @Override
   public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
      Resource.Tile g = this.set.ground.pick(rnd);
      m.new Plane(m.gnd(), lc, 0, g);
   }

   @Override
   public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {
      if (m.map.gettile(gc) > this.id) {
         if (this.set.btrans != null && bmask > 0) {
            gt.layover(m, lc, gc, z, this.set.btrans[bmask - 1].pick(rnd));
         }

         if (this.set.ctrans != null && cmask > 0) {
            gt.layover(m, lc, gc, z, this.set.ctrans[cmask - 1].pick(rnd));
         }
      }
   }

   @Tiler.ResName("gnd")
   public static class Fac implements Tiler.Factory {
      @Override
      public Tiler create(int id, Resource.Tileset set) {
         return new GroundTile(id, set);
      }
   }
}
