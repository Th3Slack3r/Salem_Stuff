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
import java.util.Map.Entry;

public class Partyview extends Widget {
   static final BufferedImage[] pleave = new BufferedImage[]{
      Resource.loadimg("gfx/hud/pleave"), Resource.loadimg("gfx/hud/pleave"), Resource.loadimg("gfx/hud/pleave")
   };
   long ign;
   Party party;
   Map<Long, Party.Member> om;
   Party.Member ol;
   Map<Party.Member, FramedAva> avs;
   IButton leave;

   Partyview(Coord c, Widget parent, long ign) {
      super(c, new Coord(84, 140), parent);
      this.party = this.ui.sess.glob.party;
      this.om = null;
      this.ol = null;
      this.avs = new HashMap<>();
      this.leave = null;
      this.ign = ign;
   }

   @Override
   public void tick(double dt) {
      if (this.party.memb != this.om) {
         int i = 0;
         Collection<Party.Member> old = new HashSet<>(this.avs.keySet());

         for (final Party.Member m : (this.om = this.party.memb).values()) {
            if (m.gobid != this.ign) {
               FramedAva w = this.avs.get(m);
               if (w == null) {
                  w = new FramedAva(Coord.z, new Coord(36, 36), this, m.gobid, "avacam") {
                     private Tex tooltip = null;

                     @Override
                     public Object tooltip(Coord c, Widget prev) {
                        Gob gob = m.getgob();
                        if (gob == null) {
                           return this.tooltip;
                        } else {
                           KinInfo ki = gob.getattr(KinInfo.class);
                           return ki == null ? null : (this.tooltip = ki.rendered());
                        }
                     }
                  };
                  this.avs.put(m, w);
               } else {
                  old.remove(m);
               }
            }
         }

         for (Party.Member mx : old) {
            this.ui.destroy(this.avs.get(mx));
            this.avs.remove(mx);
         }

         List<Entry<Party.Member, FramedAva>> wl = new ArrayList<>(this.avs.entrySet());
         Collections.sort(wl, new Comparator<Entry<Party.Member, FramedAva>>() {
            public int compare(Entry<Party.Member, FramedAva> a, Entry<Party.Member, FramedAva> b) {
               long aid = a.getKey().gobid;
               long bid = b.getKey().gobid;
               if (aid < bid) {
                  return -1;
               } else {
                  return bid > aid ? 1 : 0;
               }
            }
         });

         for (Entry<Party.Member, FramedAva> e : wl) {
            e.getValue().c = new Coord(i % 2 * 38, i / 2 * 38);
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

      for (Entry<Party.Member, FramedAva> e : this.avs.entrySet()) {
         e.getValue().color = e.getKey().col;
      }
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.leave) {
         this.wdgmsg("leave", new Object[0]);
      } else {
         for (Party.Member m : this.avs.keySet()) {
            if (sender == this.avs.get(m)) {
               this.wdgmsg("click", new Object[]{(int)m.gobid, args[0]});
               return;
            }
         }

         super.wdgmsg(sender, msg, args);
      }
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
   }

   @Widget.RName("pv")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Partyview(c, parent, ((Integer)args[0]).intValue());
      }
   }
}
