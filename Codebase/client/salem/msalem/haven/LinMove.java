package haven;

public class LinMove extends Moving {
   public Coord s;
   public Coord t;
   public int c;
   public double a;

   public LinMove(Gob gob, Coord s, Coord t, int c) {
      super(gob);
      this.s = s;
      this.t = t;
      this.c = c;
      this.a = 0.0;
   }

   @Override
   public Coord3f getc() {
      float cx = (this.t.x - this.s.x) * (float)this.a;
      float cy = (this.t.y - this.s.y) * (float)this.a;
      cx += this.s.x;
      cy += this.s.y;
      return new Coord3f(cx, cy, this.gob.glob.map.getcz(cx, cy));
   }

   @Override
   public double getv() {
      return this.c == 0 ? 0.0 : this.s.dist(this.t) / (this.c * 0.06);
   }

   @Override
   public void ctick(int dt) {
      double da = dt / 1000.0 / (this.c * 0.06);
      this.a += da * 0.9;
      if (this.a > 1.0) {
         this.a = 1.0;
      }
   }

   public void setl(int l) {
      double a = (double)l / this.c;
      if (a > this.a) {
         this.a = a;
      }
   }
}
