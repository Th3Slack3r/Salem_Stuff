package haven;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

public class Profile {
   public Profile.Frame[] hist;
   private int i = 0;
   private static Color[] cols = new Color[16];

   public Profile(int hl) {
      this.hist = new Profile.Frame[hl];
   }

   public Profile.Frame last() {
      return this.i == 0 ? this.hist[this.hist.length - 1] : this.hist[this.i - 1];
   }

   public Tex draw(int h, long scale) {
      TexIM ret = new TexIM(new Coord(this.hist.length, h));
      Graphics g = ret.graphics();

      for (int i = 0; i < this.hist.length; i++) {
         Profile.Frame f = this.hist[i];
         if (f != null) {
            long a = 0L;

            for (int o = 0; o < f.prt.length; o++) {
               long c = a + f.prt[o];
               g.setColor(cols[o]);
               g.drawLine(i, (int)(h - a / scale), i, (int)(h - c / scale));
               a = c;
            }
         }
      }

      ret.update();
      return ret;
   }

   static {
      for (int i = 0; i < 16; i++) {
         int r = (i & 4) != 0 ? 1 : 0;
         int g = (i & 2) != 0 ? 1 : 0;
         int b = (i & 1) != 0 ? 1 : 0;
         if ((i & 8) != 0) {
            r *= 255;
            g *= 255;
            b *= 255;
         } else {
            r *= 128;
            g *= 128;
            b *= 128;
         }

         cols[i] = new Color(r, g, b);
      }
   }

   public class Frame {
      public String[] nm;
      public long total;
      public long[] prt;
      private List<Long> pw = new LinkedList<>();
      private List<String> nw = new LinkedList<>();
      private long then;
      private long last;

      public Frame() {
         this.start();
      }

      public void start() {
         this.last = this.then = System.nanoTime();
      }

      public void tick(String nm) {
         long now = System.nanoTime();
         this.pw.add(now - this.last);
         this.nw.add(nm);
         this.last = now;
      }

      public void add(String nm, long tm) {
         this.pw.add(tm);
         this.nw.add(nm);
      }

      public void tick(String nm, long subtm) {
         long now = System.nanoTime();
         this.pw.add(now - this.last - subtm);
         this.nw.add(nm);
         this.last = now;
      }

      public void fin() {
         this.total = System.nanoTime() - this.then;
         this.nm = new String[this.nw.size()];
         this.prt = new long[this.pw.size()];

         for (int i = 0; i < this.pw.size(); i++) {
            this.nm[i] = this.nw.get(i);
            this.prt[i] = this.pw.get(i);
         }

         Profile.this.hist[Profile.this.i] = this;
         if (++Profile.this.i >= Profile.this.hist.length) {
            Profile.this.i = 0;
         }

         this.pw = null;
         this.nw = null;
      }

      @Override
      public String toString() {
         StringBuilder buf = new StringBuilder();

         for (int i = 0; i < this.prt.length; i++) {
            if (i > 0) {
               buf.append(", ");
            }

            buf.append(this.nm[i] + ": " + this.prt[i]);
         }

         buf.append(", total: " + this.total);
         return buf.toString();
      }
   }
}
