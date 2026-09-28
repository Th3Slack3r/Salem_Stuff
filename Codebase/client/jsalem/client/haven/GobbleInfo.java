package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class GobbleInfo extends ItemInfo.Tip {
   public final int[] l;
   public final int[] h;
   public final int[] types;
   public final int ft;
   public final List<GobbleInfo.Event> evs;
   private static final Text.Line head = Text.render("When gobbled:");

   public GobbleInfo(ItemInfo.Owner owner, int[] l, int[] h, int[] types, int ft, List<GobbleInfo.Event> evs) {
      super(owner);
      this.l = l;
      this.h = h;
      this.types = types;
      this.ft = ft;

      for (GobbleInfo.Event ev : this.evs = evs) {
         ev.rinf = ItemInfo.longtip(ev.info);
         if (ev.p < 1.0) {
            ev.rp = RichText.render(String.format("$i{(%d%% chance)}", (int)Math.round(ev.p * 100.0)), 0).img;
         }
      }
   }

   @Override
   public BufferedImage longtip() {
      StringBuilder buf = new StringBuilder();
      buf.append("Points: ");

      for (int i = 0; i < 4; i++) {
         if (i > 0) {
            buf.append(", ");
         }

         buf.append(String.format("$col[%s]{%s-%s}", Tempers.tcolors[i], Utils.fpformat(this.l[i], 3, 1), Utils.fpformat(this.h[i], 3, 1)));
      }

      buf.append('\n');
      int min = (this.ft + 30) / 60;
      buf.append(String.format("Full and Fed Up for %02d:%02d\n", min / 60, min % 60));
      BufferedImage gi = RichText.render(buf.toString(), 0).img;
      Coord sz = PUtils.imgsz(gi);

      for (GobbleInfo.Event ev : this.evs) {
         int w = ev.rinf.getWidth();
         if (ev.rp != null) {
            w += 5 + ev.rp.getWidth();
         }

         sz.x = Math.max(sz.x, w);
         sz.y = sz.y + ev.rinf.getHeight();
      }

      BufferedImage img = TexI.mkbuf(sz.add(10, head.sz().y + 2));
      Graphics g = img.getGraphics();
      int y = 0;
      g.drawImage(head.img, 0, y, null);
      y += head.sz().y + 2;
      g.drawImage(gi, 10, y, null);
      y += gi.getHeight();

      for (GobbleInfo.Event ev : this.evs) {
         g.drawImage(ev.rinf, 10, y, null);
         if (ev.rp != null) {
            g.drawImage(ev.rp, 10 + ev.rinf.getWidth() + 5, y, null);
         }

         y += ev.rinf.getHeight();
      }

      g.dispose();
      return img;
   }

   public static class Event {
      public final List<ItemInfo> info;
      public final double p;
      private BufferedImage rinf;
      private BufferedImage rp;

      public Event(List<ItemInfo> info, double p) {
         this.info = info;
         this.p = p;
      }
   }
}
