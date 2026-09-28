package org.latikai.bots;

import haven.Gob;
import java.util.List;

class DrossingState {
   List<Gob> fields;
   int inventorycount = 0;

   DrossingState(List<Gob> fields) {
      this.fields = fields;
   }
}
