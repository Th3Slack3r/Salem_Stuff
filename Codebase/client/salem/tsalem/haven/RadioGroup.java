package haven;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

public class RadioGroup {
  private final Widget parent;
  
  private final ArrayList<RadioButton> btns;
  
  private final HashMap<String, RadioButton> map;
  
  private final HashMap<RadioButton, String> rmap;
  
  private RadioButton checked;
  
  public RadioGroup(Widget parent) {
    this.parent = parent;
    this.btns = new ArrayList<>();
    this.map = new HashMap<>();
    this.rmap = new HashMap<>();
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
    
    public boolean mousedown(Coord c, int button) {
      if (this.a || button != 1)
        return false; 
      RadioGroup.this.check(this);
      return true;
    }
    
    public void changed(boolean val) {
      this.a = val;
      if (!this.skip_super)
        super.changed(val); 
      this.lbl = CheckBox.lblf.render(this.lbl.text, this.a ? Color.YELLOW : Color.WHITE);
    }
  }
  
  public RadioButton add(String lbl, Coord c) {
    return add(lbl, c, false);
  }
  
  public RadioButton add(String lbl, Coord c, boolean skip) {
    RadioButton rb = new RadioButton(c, this.parent, lbl, skip);
    this.btns.add(rb);
    this.map.put(lbl, rb);
    this.rmap.put(rb, lbl);
    if (this.checked == null)
      this.checked = rb; 
    return rb;
  }
  
  public void check(int index) {
    if (index >= 0 && index < this.btns.size())
      check(this.btns.get(index)); 
  }
  
  public void check(String lbl) {
    if (this.map.containsKey(lbl))
      check(this.map.get(lbl)); 
  }
  
  public void check(RadioButton rb) {
    if (this.checked != null)
      this.checked.changed(false); 
    this.checked = rb;
    this.checked.changed(true);
    changed(this.btns.indexOf(this.checked), this.rmap.get(this.checked));
  }
  
  public void hide() {
    for (RadioButton rb : this.btns)
      rb.hide(); 
  }
  
  public void show() {
    for (RadioButton rb : this.btns)
      rb.show(); 
  }
  
  public void changed(int btn, String lbl) {}
}
