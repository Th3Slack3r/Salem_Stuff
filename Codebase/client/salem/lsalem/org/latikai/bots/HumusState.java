package org.latikai.bots;

import haven.Gob;
import java.util.List;

class HumusState {
   List<Gob> fields;
   int inventory_count;

   public HumusState(List<Gob> fields) {
      this.fields = fields;
   }
}
