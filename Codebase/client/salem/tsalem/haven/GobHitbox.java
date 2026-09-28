package haven;

import java.awt.Color;
import javax.media.opengl.GL2;

public class GobHitbox extends Sprite {
  public static States.ColState fillclrstate = new States.ColState(new Color(255, 255, 255, 255));
  
  private static final States.ColState bbclrstate = new States.ColState(new Color(255, 255, 255, 255));
  
  private final Coordf a;
  
  private final Coordf b;
  
  private final Coordf c;
  
  private final Coordf d;
  
  private int mode;
  
  private States.ColState clrstate;
  
  public GobHitbox(Gob gob, Coord ac, Coord bc, boolean fill) {
    super(gob, null);
    if (fill) {
      this.mode = 7;
      this.clrstate = fillclrstate;
    } else {
      this.mode = 2;
      this.clrstate = bbclrstate;
    } 
    this.a = new Coordf(ac.x, ac.y);
    this.b = new Coordf(ac.x, bc.y);
    this.c = new Coordf(bc.x, bc.y);
    this.d = new Coordf(bc.x, ac.y);
  }
  
  public boolean setup(RenderList rl) {
    rl.prepo(this.clrstate);
    if (this.mode == 2)
      rl.prepo(States.xray); 
    return true;
  }
  
  public void draw(GOut g) {
    g.apply();
    GL2 gl = g.gl;
    if (this.mode == 2) {
      gl.glLineWidth(2.0F);
      gl.glBegin(this.mode);
      gl.glVertex3f(this.a.x, this.a.y, 1.0F);
      gl.glVertex3f(this.b.x, this.b.y, 1.0F);
      gl.glVertex3f(this.c.x, this.c.y, 1.0F);
      gl.glVertex3f(this.d.x, this.d.y, 1.0F);
    } else {
      gl.glBegin(this.mode);
      gl.glVertex3f(this.a.x, this.a.y, 11.0F);
      gl.glVertex3f(this.d.x, this.d.y, 11.0F);
      gl.glVertex3f(this.c.x, this.c.y, 11.0F);
      gl.glVertex3f(this.b.x, this.b.y, 11.0F);
    } 
    gl.glEnd();
    gl.glBegin(1);
    gl.glVertex3f(0.0F, 0.0F, 1.0F);
    gl.glVertex3f(this.a.x, this.a.y, 1.0F);
    gl.glEnd();
  }
  
  public static class BBox {
    public Coord a;
    
    public Coord b;
    
    public BBox(Coord a, Coord b) {
      this.a = a;
      this.b = b;
    }
  }
  
  private static final BBox bboxCalf = new BBox(new Coord(-9, -3), new Coord(9, 3));
  
  private static final BBox bboxLamb = new BBox(new Coord(-6, -2), new Coord(6, 2));
  
  private static final BBox bboxGoat = new BBox(new Coord(-6, -2), new Coord(6, 2));
  
  private static final BBox bboxPig = new BBox(new Coord(-6, -3), new Coord(6, 3));
  
  private static final BBox bboxCattle = new BBox(new Coord(-12, -4), new Coord(12, 4));
  
  private static final BBox bboxHorse = new BBox(new Coord(-8, -4), new Coord(8, 4));
  
  private static final BBox bboxSmelter = new BBox(new Coord(-34, -13), new Coord(13, 24));
  
  private static final BBox bboxForge = new BBox(new Coord(-5, -50), new Coord(5, 41));
  
  private static final BBox bboxWallseg = new BBox(new Coord(-5, -6), new Coord(6, 5));
  
  private static final BBox bboxHwall = new BBox(new Coord(-1, 0), new Coord(0, 11));
  
  public static BBox getBBox(Gob gob) {
    Resource res = null;
    try {
      res = gob.getres();
    } catch (Loading loading) {}
    if (res == null)
      return null; 
    String name = res.name;
    Resource.Neg neg = gob.getneg();
    if (name.endsWith("/oresmelter"))
      return bboxSmelter; 
    if (name.endsWith("/fineryforge"))
      return bboxForge; 
    return (neg == null) ? null : new BBox(neg.ac, neg.bc);
  }
}
