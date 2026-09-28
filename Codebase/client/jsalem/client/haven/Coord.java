package haven;

import java.awt.Dimension;
import java.io.Serializable;

public class Coord implements Comparable<Coord>, Serializable {
   public int x;
   public int y;
   public static Coord z = new Coord(0, 0);

   public Coord(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public Coord(Coord c) {
      this(c.x, c.y);
   }

   public Coord(Coord3f c) {
      this((int)c.x, (int)c.y);
   }

   public Coord() {
      this(0, 0);
   }

   public Coord(Dimension d) {
      this(d.width, d.height);
   }

   public static Coord sc(double a, double r) {
      return new Coord((int)(Math.cos(a) * r), -((int)(Math.sin(a) * r)));
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof Coord)) {
         return false;
      } else {
         Coord c = (Coord)o;
         return c.x == this.x && c.y == this.y;
      }
   }

   public int compareTo(Coord c) {
      if (c.y != this.y) {
         return c.y - this.y;
      } else {
         return c.x != this.x ? c.x - this.x : 0;
      }
   }

   @Override
   public int hashCode() {
      return (this.y & 65535) << 16 | this.x & 65535;
   }

   public Coord add(int ax, int ay) {
      return new Coord(this.x + ax, this.y + ay);
   }

   public Coord add(Coord b) {
      return this.add(b.x, b.y);
   }

   public Coord sub(int ax, int ay) {
      return new Coord(this.x - ax, this.y - ay);
   }

   public Coord sub(Coord b) {
      return this.sub(b.x, b.y);
   }

   public Coord mul(int f) {
      return new Coord(this.x * f, this.y * f);
   }

   public Coord mul(double f) {
      return new Coord((int)(this.x * f), (int)(this.y * f));
   }

   public Coord inv() {
      return new Coord(-this.x, -this.y);
   }

   public Coord mul(Coord f) {
      return new Coord(this.x * f.x, this.y * f.y);
   }

   public Coord div(Coord d) {
      return new Coord(Utils.floordiv(this.x, d.x), Utils.floordiv(this.y, d.y));
   }

   public Coord div(int d) {
      return this.div(new Coord(d, d));
   }

   public Coord mod(Coord d) {
      return new Coord(Utils.floormod(this.x, d.x), Utils.floormod(this.y, d.y));
   }

   public boolean isect(Coord c, Coord s) {
      return this.x >= c.x && this.y >= c.y && this.x < c.x + s.x && this.y < c.y + s.y;
   }

   @Override
   public String toString() {
      return "(" + this.x + ", " + this.y + ")";
   }

   public double angle(Coord o) {
      Coord c = o.add(this.inv());
      if (c.x == 0) {
         return c.y < 0 ? -Math.PI / 2 : Math.PI / 2;
      } else if (c.x < 0) {
         return c.y < 0 ? -Math.PI + Math.atan((double)c.y / c.x) : Math.PI + Math.atan((double)c.y / c.x);
      } else {
         return Math.atan((double)c.y / c.x);
      }
   }

   public double dist(Coord o) {
      long dx = o.x - this.x;
      long dy = o.y - this.y;
      return Math.sqrt(dx * dx + dy * dy);
   }

   public Coord clip(Coord ul, Coord sz) {
      Coord ret = this;
      if (this.x < ul.x) {
         ret = new Coord(ul.x, this.y);
      }

      if (ret.y < ul.y) {
         ret = new Coord(ret.x, ul.y);
      }

      if (ret.x > ul.x + sz.x) {
         ret = new Coord(ul.x + sz.x, ret.y);
      }

      if (ret.y > ul.y + sz.y) {
         ret = new Coord(ret.x, ul.y + sz.y);
      }

      return ret;
   }

   public Coord abs() {
      return new Coord(Math.abs(this.x), Math.abs(this.y));
   }

   public long mul() {
      return this.x * this.y;
   }
}
