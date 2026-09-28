package haven;

import java.awt.Color;

public class GobHealth extends GAttrib {
  int hp;
  
  Material.Colors fx;
  
  public GobHealth(Gob g, int hp) {
    super(g);
    this.hp = hp;
    this.fx = new Material.Colors(new Color(255, 0, 0, 128 - hp * 128 / 4));
  }
  
  public GLState getfx() {
    if (this.hp >= 4)
      return GLState.nullstate; 
    return this.fx;
  }
  
  public double asfloat() {
    return this.hp / 4.0D;
  }
}
