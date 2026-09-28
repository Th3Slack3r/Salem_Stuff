package org.latikai.bots;

import haven.Gob;
import java.util.ArrayList;
import java.util.List;

class PPotState extends Bot {
   List<Gob> pots = new ArrayList<>();
   int index = 0;
   int direction = 1;

   boolean hasNext() {
      return this.index + this.direction >= 0 && this.index + this.direction < this.pots.size();
   }

   Gob next() {
      this.index = this.index + this.direction;
      return this.current();
   }

   Gob current() {
      return this.pots.get(this.index);
   }

   void reverse() {
      this.direction *= -1;
   }
}
