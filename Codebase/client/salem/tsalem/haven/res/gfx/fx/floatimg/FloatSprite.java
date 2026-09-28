package haven.res.gfx.fx.floatimg;

import haven.Coord;
import haven.GOut;
import haven.Gob;
import haven.PView;
import haven.RenderList;
import haven.Resource;
import haven.Sprite;
import haven.Tex;
import haven.Utils;

public class FloatSprite extends Sprite implements PView.Render2D {
  public final int ms;
  
  final Tex tex;
  
  final int sy;
  
  double a = 0.0D;
  
  public int cury() {
    return this.sy - (int)(10.0D * this.a);
  }
  
  public FloatSprite(Sprite.Owner owner, Resource resource, Tex tex, int n) {
    super(owner, resource);
    this.tex = tex;
    this.ms = n;
    this.sy = place((Gob)owner, (tex.sz()).y);
  }
  
  private static int place(Gob gob, int n) {
    int n2 = 0;
    label19: while (true) {
      for (Gob.Overlay overlay : gob.ols) {
        if (!(overlay.spr instanceof FloatSprite))
          continue; 
        FloatSprite floatSprite = (FloatSprite)overlay.spr;
        int n3 = floatSprite.cury();
        int n4 = (floatSprite.tex.sz()).y;
        if (n3 < n2 || n3 >= n2 + n) {
          if (n2 < n3 || n2 >= n3 + n4)
            continue; 
          continue label19;
        } 
        n2 = n3 - n;
      } 
      break;
    } 
    return n2;
  }
  
  public void draw2d(GOut gOut) {
    Coord coord = ((Gob)this.owner).sc;
    if (coord == null)
      return; 
    int n = (this.a < 0.75D) ? 255 : (int)Utils.clip(255.0D * (1.0D - this.a) / 0.25D, 0.0D, 255.0D);
    gOut.chcolor(255, 255, 255, n);
    Coord coord2 = this.tex.sz().inv();
    coord2.x /= 2;
    coord2.y += cury();
    coord2.y -= 15;
    gOut.image(this.tex, coord.add(coord2));
    gOut.chcolor();
  }
  
  public boolean setup(RenderList renderList) {
    return false;
  }
  
  public boolean tick(int n) {
    this.a += n / this.ms;
    return (this.a >= 1.0D);
  }
}
