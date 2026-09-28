package haven;

public class TexSI extends Tex {
   public final Tex parent;
   private final Coord ul;

   public TexSI(Tex parent, Coord ul, Coord sz) {
      super(sz);
      this.parent = parent;
      this.ul = ul;
   }

   @Override
   public float tcx(int x) {
      return this.parent.tcx(x + this.ul.x);
   }

   @Override
   public float tcy(int y) {
      return this.parent.tcy(y + this.ul.y);
   }

   @Override
   public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
      this.parent.render(g, c, this.ul.add(ul), this.ul.add(br), sz);
   }

   @Override
   public GLState draw() {
      return this.parent.draw();
   }

   @Override
   public GLState clip() {
      return this.parent.clip();
   }
}
