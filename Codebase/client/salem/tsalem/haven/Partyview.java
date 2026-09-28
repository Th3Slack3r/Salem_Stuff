package haven;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Partyview extends Widget {
  static final BufferedImage[] pleave = new BufferedImage[] { Resource.loadimg("gfx/hud/pleave"), Resource.loadimg("gfx/hud/pleave"), Resource.loadimg("gfx/hud/pleave") };
  
  long ign;
  
  Party party = this.ui.sess.glob.party;
  
  Map<Long, Party.Member> om = null;
  
  Party.Member ol = null;
  
  Map<Party.Member, FramedAva> avs = new HashMap<>();
  
  IButton leave = null;
  
  @RName("pv")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Partyview(c, parent, ((Integer)args[0]).intValue());
    }
  }
  
  Partyview(Coord c, Widget parent, long ign) {
    super(c, new Coord(84, 140), parent);
    this.ign = ign;
  }
  
  public void tick(double dt) {
    if (this.party.memb != this.om) {
      int i = 0;
      Collection<Party.Member> old = new HashSet<>(this.avs.keySet());
      for (Party.Member m : (this.om = this.party.memb).values()) {
        if (m.gobid == this.ign)
          continue; 
        FramedAva w = this.avs.get(m);
        if (w == null) {
          w = new FramedAva(Coord.z, new Coord(36, 36), this, m.gobid, "avacam") {
              private Tex tooltip = null;
              
              public Object tooltip(Coord c, Widget prev) {
                Gob gob = m.getgob();
                if (gob == null)
                  return this.tooltip; 
                KinInfo ki = gob.<KinInfo>getattr(KinInfo.class);
                if (ki == null)
                  return null; 
                return this.tooltip = ki.rendered();
              }
            };
          this.avs.put(m, w);
          continue;
        } 
        old.remove(m);
      } 
      for (Party.Member m : old) {
        this.ui.destroy(this.avs.get(m));
        this.avs.remove(m);
      } 
      List<Map.Entry<Party.Member, FramedAva>> wl = new ArrayList<>(this.avs.entrySet());
      Collections.sort(wl, new Comparator<Map.Entry<Party.Member, FramedAva>>() {
            public int compare(Map.Entry<Party.Member, FramedAva> a, Map.Entry<Party.Member, FramedAva> b) {
              long aid = ((Party.Member)a.getKey()).gobid, bid = ((Party.Member)b.getKey()).gobid;
              if (aid < bid)
                return -1; 
              if (bid > aid)
                return 1; 
              return 0;
            }
          });
      for (Map.Entry<Party.Member, FramedAva> e : wl) {
        ((FramedAva)e.getValue()).c = new Coord(i % 2 * 38, i / 2 * 38);
        i++;
      } 
      if (this.avs.size() > 0) {
        if (this.leave == null) {
          this.leave = new IButton(Coord.z, this, pleave[0], pleave[1], pleave[2]);
          this.leave.tooltip = Text.render("Leave party");
        } 
        this.leave.c = new Coord(i % 2 * 38, i / 2 * 38);
      } 
      if (this.avs.size() == 0 && this.leave != null) {
        this.ui.destroy(this.leave);
        this.leave = null;
      } 
    } 
    for (Map.Entry<Party.Member, FramedAva> e : this.avs.entrySet())
      ((FramedAva)e.getValue()).color = ((Party.Member)e.getKey()).col; 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.leave) {
      wdgmsg("leave", new Object[0]);
      return;
    } 
    for (Party.Member m : this.avs.keySet()) {
      if (sender == this.avs.get(m)) {
        wdgmsg("click", new Object[] { Integer.valueOf((int)m.gobid), args[0] });
        return;
      } 
    } 
    super.wdgmsg(sender, msg, args);
  }
  
  public void draw(GOut g) {
    super.draw(g);
  }
}
