package haven;

public abstract class Tex {
   protected Coord dim;
   public static final Tex empty = new Tex(Coord.z) {
      @Override
      public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
      }

      @Override
      public float tcx(int x) {
         return 0.0F;
      }

      @Override
      public float tcy(int y) {
         return 0.0F;
      }

      @Override
      public GLState draw() {
         return null;
      }

      @Override
      public GLState clip() {
         return null;
      }
   };

   public Tex(Coord sz) {
      this.dim = sz;
   }

   public Coord sz() {
      return this.dim;
   }

   public static int nextp2(int in) {
      int h = Integer.highestOneBit(in);
      return h == in ? h : h * 2;
   }

   public abstract void render(GOut var1, Coord var2, Coord var3, Coord var4, Coord var5);

   public abstract float tcx(int var1);

   public abstract float tcy(int var1);

   public abstract GLState draw();

   public abstract GLState clip();

   public void render(GOut g, Coord c) {
      this.render(g, c, Coord.z, this.dim, this.dim);
   }

   public void crender(GOut g, Coord c, Coord ul, Coord sz, Coord tsz) {
      if (tsz.x != 0 && tsz.y != 0) {
         if (c.x < ul.x + sz.x && c.y < ul.y + sz.y && c.x + tsz.x > ul.x && c.y + tsz.y > ul.y) {
            Coord t = new Coord(c);
            Coord uld = new Coord(0, 0);
            Coord brd = new Coord(this.dim);
            Coord szd = new Coord(tsz);
            if (c.x < ul.x) {
               int pd = ul.x - c.x;
               t.x = ul.x;
               uld.x = pd * this.dim.x / tsz.x;
               szd.x -= pd;
            }

            if (c.y < ul.y) {
               int pd = ul.y - c.y;
               t.y = ul.y;
               uld.y = pd * this.dim.y / tsz.y;
               szd.y -= pd;
            }

            if (c.x + tsz.x > ul.x + sz.x) {
               int pd = c.x + tsz.x - (ul.x + sz.x);
               szd.x -= pd;
               brd.x = brd.x - pd * this.dim.x / tsz.x;
            }

            if (c.y + tsz.y > ul.y + sz.y) {
               int pd = c.y + tsz.y - (ul.y + sz.y);
               szd.y -= pd;
               brd.y = brd.y - pd * this.dim.y / tsz.y;
            }

            this.render(g, t, uld, brd, szd);
         }
      }
   }

   public void crender(GOut g, Coord c, Coord ul, Coord sz) {
      this.crender(g, c, ul, sz, this.dim);
   }

   public void dispose() {
   }
}
