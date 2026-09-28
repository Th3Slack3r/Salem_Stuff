package haven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class EquipOpts extends GameUI.Hidewnd {
   private static final Map<Integer, String> slotNames;
   private static final List<Integer> slotOrder;
   private Map<CheckBox, Integer> checkSlots = new HashBMap<>();
   private List<Integer> selected;

   public EquipOpts(Coord c, Widget parent) {
      super(c, Coord.z, parent, "Proxy CFG");
      int k = 0;
      this.read();

      for (int slot : slotOrder) {
         CheckBox checkBox = new CheckBox(new Coord(0, 20 * k++), this, slotNames.get(slot)) {
            @Override
            public void changed(boolean val) {
               EquipOpts.this.setSlotState(this, val);
            }
         };
         checkBox.a = this.selected.contains(slot);
         this.checkSlots.put(checkBox, slot);
      }

      this.pack();
      this.update();
   }

   @Override
   public Coord contentsz() {
      Coord sz = super.contentsz();
      sz.x = Math.max(sz.x, 100);
      return sz;
   }

   public void toggle() {
      this.show(!this.visible);
      if (this.visible) {
         this.raise();
      }
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      super.wdgmsg(sender, msg, args);
   }

   private void read() {
      this.selected = new LinkedList<>();
      String[] slots = Utils.getpref("equip_proxy_slots", "6;7;9;14;5;4;13").split(";");

      for (String slot : slots) {
         try {
            this.selected.add(Integer.parseInt(slot));
         } catch (NumberFormatException var7) {
         }
      }
   }

   private void setSlotState(CheckBox check, boolean val) {
      int slot = this.checkSlots.get(check);
      int k = this.selected.indexOf(slot);
      if (!val && k >= 0) {
         this.selected.remove(k);
      } else if (val && k < 0) {
         this.selected.add(slot);
      }

      this.store();
      this.update();
   }

   private void store() {
      String buf = "";
      int n = this.selected.size();

      for (int i = 0; i < n; i++) {
         buf = buf + this.selected.get(i);
         if (i < n - 1) {
            buf = buf + ";";
         }
      }

      Utils.setpref("equip_proxy_slots", buf);
   }

   private void update() {
      int[] slots = new int[this.selected.size()];
      int k = 0;

      for (int slot : slotOrder) {
         if (this.selected.contains(slot)) {
            slots[k++] = slot;
         }
      }

      this.ui.gui.equipProxy.setSlots(slots);
   }

   static {
      final List<Integer> ao = new ArrayList<>();
      Map<Integer, String> an = new HashMap<Integer, String>() {
         public String put(Integer k, String v) {
            ao.add(k);
            return super.put(k, v);
         }
      };
      an.put(0, "Head");
      an.put(13, "Neck");
      an.put(6, "Left hand");
      an.put(7, "Right hand");
      an.put(9, "Purse");
      an.put(14, "Back");
      an.put(5, "Belt");
      an.put(4, "Keys");
      slotNames = Collections.unmodifiableMap(an);
      slotOrder = Collections.unmodifiableList(ao);
   }
}
