package haven;

public class DrawOffset extends GAttrib {
   public Coord3f off;

   public DrawOffset(Gob gob, Coord3f off) {
      super(gob);
      this.off = off;
   }
}
