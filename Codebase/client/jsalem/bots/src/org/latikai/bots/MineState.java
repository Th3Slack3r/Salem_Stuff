package org.latikai.bots;

import haven.Coord;
import java.util.List;

public class MineState {
   public List<Coord> tiles = null;
   public Coord last_loc1 = Coord.z;
   public Coord last_loc2 = Coord.z;
   public Coord drop_location = Coord.z;
   public boolean started = false;
   public boolean continuing = false;  // True when continuing loop after drop
}
