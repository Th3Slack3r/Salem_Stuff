package org.latikai.bots;

import haven.Gob;
import java.util.List;

class HarvestState {
   List<Gob> fields;
   int inventorycount;

   public HarvestState(List<Gob> fields) {
      this.fields = fields;
   }
}
