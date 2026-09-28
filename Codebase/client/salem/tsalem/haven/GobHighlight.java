package haven;

import java.awt.Color;

public class GobHighlight extends GAttrib {
  Material.Colors fx;
  
  int time = 0;
  
  int duration = 5000;
  
  public GobHighlight(Gob gob) {
    super(gob);
    Color c = new Color(64, 255, 64, 100);
    this.fx = new Material.Colors();
    this.fx.amb = Utils.c2fa(c);
    this.fx.dif = Utils.c2fa(c);
    this.fx.emi = Utils.c2fa(c);
  }
  
  public void ctick(int dt) {
    this.time += dt;
    float a = (float)(0.49000000953674316D * (1.0D + Math.sin((this.time / 100.0F))));
    this.fx.amb[3] = a;
    this.fx.dif[3] = a;
    this.fx.emi[3] = a;
    this.duration -= dt;
  }
  
  public GLState getfx() {
    return this.fx;
  }
}
