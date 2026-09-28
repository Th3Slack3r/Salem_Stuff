package haven;

public class Homing extends Moving {
   long tgt;
   Coord tc;
   int v;
   double dist;

   public Homing(Gob gob, long tgt, Coord tc, int v) {
      super(gob);
      this.tgt = tgt;
      this.tc = tc;
      this.v = v;
   }

   @Override
   public Coord3f getc() {
      Coord tc = this.tc;
      Gob tgt = this.gob.glob.oc.getgob(this.tgt);
      if (tgt != null) {
         tc = tgt.rc;
      }

      Coord d = tc.add(this.gob.rc.inv());
      double e = this.gob.rc.dist(tc);
      float rx = this.gob.rc.x;
      float ry = this.gob.rc.y;
      if (e > 1.0E-5) {
         rx += (float)(d.x / e * this.dist);
         ry += (float)(d.y / e * this.dist);
      }

      return new Coord3f(rx, ry, this.gob.glob.map.getcz(rx, ry));
   }

   @Override
   public double getv() {
      return this.v / 100.0 / 0.06;
   }

   @Override
   public void move(Coord c) {
      this.dist = 0.0;
   }

   @Override
   public void ctick(int dt) {
      double da = dt / 1000.0 / 0.06;
      this.dist = this.dist + da * 0.9 * (this.v / 100.0);
   }
}
