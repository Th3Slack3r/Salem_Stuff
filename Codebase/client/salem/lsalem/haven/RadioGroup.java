package haven;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

public class RadioGroup {
   private Widget parent;
   private ArrayList<RadioGroup.RadioButton> btns;
   private HashMap<String, RadioGroup.RadioButton> map;
   private HashMap<RadioGroup.RadioButton, String> rmap;
   private RadioGroup.RadioButton checked;

   public RadioGroup(Widget parent) {
      this.parent = parent;
      this.btns = new ArrayList<>();
      this.map = new HashMap<>();
      this.rmap = new HashMap<>();
   }

   public RadioGroup.RadioButton add(String lbl, Coord c) {
      return this.add(lbl, c, false);
   }

   public RadioGroup.RadioButton add(String lbl, Coord c, boolean skip) {
      RadioGroup.RadioButton rb = new RadioGroup.RadioButton(c, this.parent, lbl, skip);
      this.btns.add(rb);
      this.map.put(lbl, rb);
      this.rmap.put(rb, lbl);
      if (this.checked == null) {
         this.checked = rb;
      }

      return rb;
   }

   public void check(int index) {
      if (index >= 0 && index < this.btns.size()) {
         this.check(this.btns.get(index));
      }
   }

   public void check(String lbl) {
      if (this.map.containsKey(lbl)) {
         this.check(this.map.get(lbl));
      }
   }

   public void check(RadioGroup.RadioButton rb) {
      if (this.checked != null) {
         this.checked.changed(false);
      }

      this.checked = rb;
      this.checked.changed(true);
      this.changed(this.btns.indexOf(this.checked), this.rmap.get(this.checked));
   }

   public void hide() {
      for (RadioGroup.RadioButton rb : this.btns) {
         rb.hide();
      }
   }

   public void show() {
      for (RadioGroup.RadioButton rb : this.btns) {
         rb.show();
      }
   }

   public void changed(int btn, String lbl) {
   }

   public class RadioButton extends CheckBox {
      private boolean skip_super = false;

      RadioButton(Coord c, Widget parent, String lbl) {
         super(c, parent, lbl);
      }

      RadioButton(Coord c, Widget parent, String lbl, boolean skip) {
         super(c, parent, lbl);
         this.skip_super = skip;
      }

      @Override
      public boolean mousedown(Coord c, int button) {
         if (!this.a && button == 1) {
            RadioGroup.this.check(this);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void changed(boolean val) {
         this.a = val;
         if (!this.skip_super) {
            super.changed(val);
         }

         this.lbl = CheckBox.lblf.render(this.lbl.text, this.a ? Color.YELLOW : Color.WHITE);
      }
   }
}
