package org.latikai.bots;

import haven.Coord;

class LimeState {
   Coord lime_location = Coord.z;
   boolean starteddigging = false;
   boolean startedmoving = false;
   boolean startedchipping = false;

   LimeState(Coord loc) {
      this.lime_location = loc;
   }
}
