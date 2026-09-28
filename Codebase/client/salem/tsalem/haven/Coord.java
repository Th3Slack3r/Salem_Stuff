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
  
  public Coord(String str) {
    int x, y;
    if (str == null || str.charAt(0) != '(' || str.charAt(str.length() - 1) != ')') {
      x = y = 0;
    } else {
      str = str.substring(1, str.length() - 1);
      String[] val = str.split(", ");
      x = Integer.parseInt(val[0]);
      y = Integer.parseInt(val[1]);
    } 
    this.x = x;
    this.y = y;
  }
  
  public static Coord sc(double a, double r) {
    return new Coord((int)(Math.cos(a) * r), -((int)(Math.sin(a) * r)));
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof Coord))
      return false; 
    Coord c = (Coord)o;
    return (c.x == this.x && c.y == this.y);
  }
  
  public int compareTo(Coord c) {
    if (c.y != this.y)
      return c.y - this.y; 
    if (c.x != this.x)
      return c.x - this.x; 
    return 0;
  }
  
  public int hashCode() {
    return (this.y & 0xFFFF) << 16 | this.x & 0xFFFF;
  }
  
  public Coord add(int ax, int ay) {
    return new Coord(this.x + ax, this.y + ay);
  }
  
  public Coord add(Coord b) {
    return add(b.x, b.y);
  }
  
  public Coord sub(int ax, int ay) {
    return new Coord(this.x - ax, this.y - ay);
  }
  
  public Coord sub(Coord b) {
    return sub(b.x, b.y);
  }
  
  public Coord mul(int f) {
    return new Coord(this.x * f, this.y * f);
  }
  
  public Coord mul(double f) {
    return new Coord((int)(this.x * f), (int)(this.y * f));
  }
  
  public Coord mul(int i, int j) {
    return new Coord(this.x * i, this.y * j);
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
    return div(new Coord(d, d));
  }
  
  public Coord div(double f) {
    return new Coord((int)(this.x / f), (int)(this.y / f));
  }
  
  public Coord mod(Coord d) {
    return new Coord(Utils.floormod(this.x, d.x), Utils.floormod(this.y, d.y));
  }
  
  public Coord transpose() {
    return new Coord(this.y, this.x);
  }
  
  public boolean isect(Coord c, Coord s) {
    return (this.x >= c.x && this.y >= c.y && this.x < c.x + s.x && this.y < c.y + s.y);
  }
  
  public String toString() {
    return "(" + this.x + ", " + this.y + ")";
  }
  
  public double angle(Coord o) {
    Coord c = o.add(inv());
    if (c.x == 0) {
      if (c.y < 0)
        return -1.5707963267948966D; 
      return 1.5707963267948966D;
    } 
    if (c.x < 0) {
      if (c.y < 0)
        return -3.141592653589793D + Math.atan(c.y / c.x); 
      return Math.PI + Math.atan(c.y / c.x);
    } 
    return Math.atan(c.y / c.x);
  }
  
  public double dist(Coord o) {
    long dx = (o.x - this.x);
    long dy = (o.y - this.y);
    return Math.sqrt((dx * dx + dy * dy));
  }
  
  public int manhattan(Coord c) {
    return Math.abs(this.x - c.x) + Math.abs(this.y - c.y);
  }
  
  public int manhattan2(Coord c) {
    return Math.max(Math.abs(this.x - c.x), Math.abs(this.y - c.y));
  }
  
  public Coord clip(Coord ul, Coord sz) {
    Coord ret = this;
    if (ret.x < ul.x)
      ret = new Coord(ul.x, ret.y); 
    if (ret.y < ul.y)
      ret = new Coord(ret.x, ul.y); 
    if (ret.x > ul.x + sz.x)
      ret = new Coord(ul.x + sz.x, ret.y); 
    if (ret.y > ul.y + sz.y)
      ret = new Coord(ret.x, ul.y + sz.y); 
    return ret;
  }
  
  public Coord abs() {
    return new Coord(Math.abs(this.x), Math.abs(this.y));
  }
  
  public long mul() {
    return (this.x * this.y);
  }
  
  public Coord rotate(double angle) {
    return new Coord((int)(this.x * Math.cos(angle) - this.y * Math.sin(angle)), (int)(this.x * Math.sin(angle) + this.y * Math.cos(angle)));
  }
  
  public double distx(Coord c) {
    return Math.abs(c.x - this.x);
  }
  
  public double disty(Coord c) {
    return Math.abs(c.y - this.y);
  }
}
