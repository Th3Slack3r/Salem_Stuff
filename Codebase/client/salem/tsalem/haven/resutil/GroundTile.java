package haven.resutil;

import haven.Coord;
import haven.MapMesh;
import haven.Resource;
import haven.Tiler;
import haven.Tiler.ResName;
import java.util.Random;

public class GroundTile extends Tiler {
  public final Resource.Tileset set;
  
  @ResName("gnd")
  public static class Fac implements Tiler.Factory {
    public Tiler create(int id, Resource.Tileset set) {
      return new GroundTile(id, set);
    }
  }
  
  public GroundTile(int id, Resource.Tileset set) {
    super(id);
    this.set = set;
  }
  
  public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
    Resource.Tile g = (Resource.Tile)this.set.ground.pick(rnd);
    m.getClass();
    new MapMesh.Plane(m, m.gnd(), lc, 0, g);
  }
  
  public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {
    if (m.map.gettile(gc) <= this.id)
      return; 
    if (this.set.btrans != null && bmask > 0)
      gt.layover(m, lc, gc, z, (Resource.Tile)this.set.btrans[bmask - 1].pick(rnd)); 
    if (this.set.ctrans != null && cmask > 0)
      gt.layover(m, lc, gc, z, (Resource.Tile)this.set.ctrans[cmask - 1].pick(rnd)); 
  }
}
