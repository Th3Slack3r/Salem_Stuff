package haven;

public class EquipProxyWdg extends Widget implements DTarget {
   private Coord slotsz;
   private int[] slots;

   public EquipProxyWdg(Coord c, int[] slots, Widget parent) {
      super(c, Coord.z, parent);
      this.setSlots(slots);
   }

   public void setSlots(int[] slots) {
      this.slots = slots;
      this.slotsz = new Coord(slots.length, 1);
      this.sz = Inventory.invsz(this.slotsz);
   }

   private int slot(Coord c) {
      int slot = Inventory.sqroff(c).x;
      if (slot < 0) {
         slot = 0;
      }

      if (slot >= this.slots.length) {
         slot = this.slots.length - 1;
      }

      return this.slots[slot];
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      Equipory e = this.ui.gui.getEquipory();
      if (e != null) {
         WItem w = e.slots[this.slot(c)];
         if (w != null) {
            w.mousedown(Coord.z, button);
            return true;
         }
      }

      return false;
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      Equipory e = this.ui.gui.getEquipory();
      if (e != null) {
         int k = 0;
         Inventory.invsq(g, Coord.z, this.slotsz);
         Coord c0 = new Coord(0, 0);

         for (int slot : this.slots) {
            c0.x = k;
            WItem w = e.slots[slot];
            if (w != null) {
               w.draw(g.reclipl(Inventory.sqoff(c0), g.sz));
            } else {
               Tex ebg = Equipory.ebgs[slot];
               if (ebg != null) {
                  g.image(ebg, Inventory.sqoff(c0));
               }
            }

            k++;
         }
      }
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      Equipory e = this.ui.gui.getEquipory();
      if (e != null) {
         WItem w = e.slots[this.slot(c)];
         if (w != null) {
            return w.tooltip(c, (Widget)(prev == this ? w : prev));
         }
      }

      return super.tooltip(c, prev);
   }

   @Override
   public boolean drop(Coord cc, Coord ul) {
      Equipory e = this.ui.gui.getEquipory();
      if (e != null) {
         e.wdgmsg("drop", new Object[]{this.slot(cc)});
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean iteminteract(Coord cc, Coord ul) {
      Equipory e = this.ui.gui.getEquipory();
      if (e != null) {
         WItem w = e.slots[this.slot(cc)];
         if (w != null) {
            return w.iteminteract(cc, ul);
         }
      }

      return false;
   }
}
