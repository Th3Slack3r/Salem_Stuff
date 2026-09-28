package haven;

import java.awt.Color;
import javax.media.opengl.GL2;

public class DirLight extends Light {
  public float[] dir;
  
  public DirLight(FColor col, Coord3f dir) {
    super(col);
    this.dir = dir.norm().to4a(0.0F);
  }
  
  public DirLight(Color col, Coord3f dir) {
    super(col);
    this.dir = dir.norm().to4a(0.0F);
  }
  
  public DirLight(FColor amb, FColor dif, FColor spc, Coord3f dir) {
    super(amb, dif, spc);
    this.dir = dir.norm().to4a(0.0F);
  }
  
  public DirLight(Color amb, Color dif, Color spc, Coord3f dir) {
    super(amb, dif, spc);
    this.dir = dir.norm().to4a(0.0F);
  }
  
  public void enable(GOut g, int idx) {
    super.enable(g, idx);
    GL2 gl = g.gl;
    gl.glLightfv(16384 + idx, 4611, this.dir, 0);
  }
}
