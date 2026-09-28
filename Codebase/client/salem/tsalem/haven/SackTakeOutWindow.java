package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SackTakeOutWindow extends Widget {
  static Tex bg = Resource.loadtex("gfx/hud/bosq");
  
  private int rem = 0, bi = 0;
  
  static Text.Foundry lf = new Text.Foundry(new Font("SansSerif", 0, 18), Color.WHITE);
  
  private final Indir<Resource> res;
  
  private Tex label;
  
  private final Value value;
  
  private final Button take;
  
  static {
    lf.aa = true;
  }
  
  @RName("spbox")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new SackTakeOutWindow(c, parent, parent.ui.sess.getres(((Integer)args[0]).intValue()), ((Integer)args[1]).intValue(), ((Integer)args[2]).intValue());
    }
  }
  
  private void setlabel(int rem, int bi) {
    this.rem = rem;
    this.bi = bi;
    if (this.label != null)
      this.label.dispose(); 
    this.label = lf.renderf("%d/%d", new Object[] { Integer.valueOf(rem), Integer.valueOf(bi) }).tex();
  }
  
  public SackTakeOutWindow(Coord c, Widget parent, Indir<Resource> res, int rem, int bi) {
    super(c, bg.sz(), parent);
    this.res = res;
    this.value = new Value(new Coord(125, 27), 35, this, "");
    this.take = new Button(new Coord(165, 27), Integer.valueOf(35), this, "Take");
    this.value.canactivate = true;
    this.take.canactivate = true;
  }
  
  public void draw(GOut g) {
    super.draw(g);
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender != this.value && sender != this.take)
      super.wdgmsg(sender, msg, args); 
  }
  
  public static class Value extends TextEntry {
    public static final Set<Integer> ALLOWED_KEYS = new HashSet<>(Arrays.asList(new Integer[] { 
            Integer.valueOf(48), Integer.valueOf(49), Integer.valueOf(50), Integer.valueOf(51), Integer.valueOf(52), Integer.valueOf(53), Integer.valueOf(54), Integer.valueOf(55), Integer.valueOf(56), Integer.valueOf(57), 
            Integer.valueOf(96), Integer.valueOf(97), 
            Integer.valueOf(98), Integer.valueOf(99), Integer.valueOf(100), Integer.valueOf(101), Integer.valueOf(102), Integer.valueOf(103), Integer.valueOf(104), Integer.valueOf(105), 
            Integer.valueOf(37), Integer.valueOf(39), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(127) }));
    
    public Value(Coord c, int w, Widget parent, String deftext) {
      super(c, w, parent, deftext);
    }
    
    public boolean type(char c, KeyEvent ev) {
      if (ALLOWED_KEYS.contains(Integer.valueOf(ev.getKeyCode())))
        return super.type(c, ev); 
      this.ui.root.globtype(c, ev);
      return false;
    }
  }
}
