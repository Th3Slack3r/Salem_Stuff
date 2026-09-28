package haven;

public class Location extends Transform {
   public static final Location onlyxl = new Location(Matrix4f.id) {
      private Matrix4f lp = null;
      private Matrix4f fin;

      @Override
      public Matrix4f fin(Matrix4f p) {
         if (p != this.lp) {
            this.fin = Matrix4f.identity();
            this.fin.m[12] = p.m[12];
            this.fin.m[13] = p.m[13];
            this.fin.m[14] = p.m[14];
         }

         return this.fin;
      }
   };

   public Location(Matrix4f xf) {
      super(xf);
   }

   @Override
   public void apply(GOut g) {
      throw new RuntimeException("Locations should not be applied directly.");
   }

   @Override
   public void unapply(GOut g) {
      throw new RuntimeException("Locations should not be applied directly.");
   }

   @Override
   public void prep(GLState.Buffer b) {
      Location.Chain p = b.get(PView.loc);
      b.put(PView.loc, new Location.Chain(this, p));
   }

   public static Location scale(Coord3f c) {
      return new Location(makescale(new Matrix4f(), c));
   }

   public static Location xlate(Coord3f c) {
      return new Location(makexlate(new Matrix4f(), c));
   }

   public static Location rot(Coord3f axis, float angle) {
      return new Location(makerot(new Matrix4f(), axis.norm(), angle));
   }

   public static class Chain extends GLState {
      public final Location loc;
      public final Location.Chain p;
      private Matrix4f bk;

      private Chain(Location loc, Location.Chain p) {
         this.loc = loc;
         this.p = p;
      }

      public Matrix4f fin(Matrix4f o) {
         return this.p == null ? this.loc.fin(o) : this.loc.fin(this.p.fin(o));
      }

      @Override
      public void apply(GOut g) {
         this.bk = g.st.wxf;
         g.st.wxf = this.fin(g.st.wxf);
      }

      @Override
      public void unapply(GOut g) {
         g.st.wxf = this.bk;
      }

      @Override
      public void prep(GLState.Buffer b) {
         throw new RuntimeException("Location chains should not be applied directly.");
      }

      @Override
      public String toString() {
         String ret = this.loc.toString();
         if (this.p != null) {
            ret = ret + " -> " + this.p;
         }

         return ret;
      }
   }
}
