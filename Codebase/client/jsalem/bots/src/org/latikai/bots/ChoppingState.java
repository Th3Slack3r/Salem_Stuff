package org.latikai.bots;

import haven.Gob;
import java.util.List;

class ChoppingState {
   List<Gob> fields;
   int inventorycount = 0;

   ChoppingState(List<Gob> fields) {
      this.fields = fields;
   }
}
