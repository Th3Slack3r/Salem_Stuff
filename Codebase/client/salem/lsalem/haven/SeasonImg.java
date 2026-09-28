package haven;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class SeasonImg extends Widget {
   private static final SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.ENGLISH);
   private static final long EPOCH;
   private static final Tex[] seasons = new Tex[]{
      Resource.loadtex("gfx/hud/coldsnap"), Resource.loadtex("gfx/hud/everbloom"), Resource.loadtex("gfx/hud/bloodmoon")
   };
   public static final IBox box = Window.swbox;
   public static final Color color = new Color(133, 92, 62);
   private final Coord isz;
   private final Coord ic;
   private long time = 0L;
   private Tex timeTex = null;

   public SeasonImg(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
      this.isz = sz.sub(box.bisz());
      this.ic = box.btloff();
      datef.setTimeZone(TimeZone.getTimeZone("GMT"));
   }

   @Override
   public void draw(GOut g) {
      Tex t = seasons[this.ui.sess.glob.season];
      g.image(t, this.ic, this.isz);
      g.chcolor(color);
      box.draw(g, Coord.z, this.sz);
      g.chcolor();
   }

   @Override
   public Object tooltip(Coord c, Widget prev) {
      long stime = this.ui.sess.glob.globtime();
      if ((int)(this.time / 1000L) != (int)(stime / 1000L) || this.timeTex == null) {
         this.time = stime;
         if (this.timeTex != null) {
            this.timeTex.dispose();
         }

         Date date = new Date(this.time + EPOCH);
         this.timeTex = Text.render(datef.format(date)).tex();
      }

      return this.timeTex;
   }

   static {
      Calendar c = new GregorianCalendar(1631, 0, 1);
      c.setTimeZone(TimeZone.getTimeZone("GMT"));
      EPOCH = c.getTimeInMillis();
   }
}
