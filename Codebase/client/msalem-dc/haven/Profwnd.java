package haven;

import java.util.Arrays;

public class Profwnd extends Window {
   public final Profile prof;
   public long mt = 50000000L;
   private static final int h = 80;

   public Profwnd(Coord c, Widget parent, Profile prof, String title) {
      super(c, new Coord(prof.hist.length, 80), parent, title);
      this.prof = prof;
   }

   @Override
   public void cdraw(GOut g) {
      long[] ttl = new long[this.prof.hist.length];

      for (int i = 0; i < this.prof.hist.length; i++) {
         if (this.prof.hist[i] != null) {
            ttl[i] = this.prof.hist[i].total;
         }
      }

      Arrays.sort(ttl);
      int ti = ttl.length;

      for (int ix = 0; ix < ttl.length; ix++) {
         if (ttl[ix] != 0L) {
            ti = ttl.length - (ttl.length - ix) / 10;
            break;
         }
      }

      if (ti < ttl.length) {
         this.mt = ttl[ti];
      } else {
         this.mt = 50000000L;
      }

      g.image(this.prof.draw(80, this.mt / 80L), Coord.z);
   }

   public String tooltip(Coord c, boolean again) {
      c = this.xlate(c, false);
      if (c.x >= 0 && c.x < this.prof.hist.length && c.y >= 0 && c.y < 80) {
         int x = c.x;
         int y = c.y;
         long t = (80 - y) * (this.mt / 80L);
         Profile.Frame f = this.prof.hist[x];
         if (f != null) {
            for (int i = 0; i < f.prt.length; i++) {
               if ((t -= f.prt[i]) < 0L) {
                  return String.format("%.2f ms, %s: %.2f ms", f.total / 1000000.0, f.nm[i], f.prt[i] / 1000000.0);
               }
            }
         }
      }

      return "";
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (msg.equals("close")) {
         this.ui.destroy(this);
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }
}
