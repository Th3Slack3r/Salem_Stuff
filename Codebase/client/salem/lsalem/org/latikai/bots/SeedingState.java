package org.latikai.bots;

import haven.GItem;
import haven.Gob;
import haven.ItemInfo;
import haven.UI;
import java.util.List;

class SeedingState {
   List<Gob> fields;
   int handcount;

   SeedingState(List<Gob> fields) {
      this.fields = fields;
   }

   static int countHandSeeds(UI ui) {
      int count = 0;

      for (GItem gii : ui.gui.hand) {
         for (ItemInfo ii : gii.info()) {
            if (ii.getClass().equals(GItem.Amount.class)) {
               count += ((GItem.Amount)ii).itemnum();
            }
         }
      }

      return count;
   }
}
