package haven;

import java.awt.Color;
import javax.media.opengl.GL2;

public class SpotLight extends PosLight {
   public float[] dir;
   public float exp;
   public float cut;
   private static final float[] defdir = new float[]{0.0F, 0.0F, -1.0F};

   public SpotLight(FColor col, Coord3f pos, Coord3f dir, float exp) {
      super(col, pos);
      this.dir = dir.to3a();
      this.exp = exp;
      this.cut = 90.0F;
   }

   public SpotLight(Color col, Coord3f pos, Coord3f dir, float exp) {
      super(col, pos);
      this.dir = dir.to3a();
      this.exp = exp;
      this.cut = 90.0F;
   }

   public SpotLight(FColor amb, FColor dif, FColor spc, Coord3f pos, Coord3f dir, float exp) {
      super(amb, dif, spc, pos);
      this.dir = dir.norm().to3a();
      this.exp = exp;
      this.cut = 90.0F;
   }

   public SpotLight(Color amb, Color dif, Color spc, Coord3f pos, Coord3f dir, float exp) {
      super(amb, dif, spc, pos);
      this.dir = dir.norm().to3a();
      this.exp = exp;
      this.cut = 90.0F;
   }

   @Override
   public void enable(GOut g, int idx) {
      super.enable(g, idx);
      GL2 gl = g.gl;
      gl.glLightfv(16384 + idx, 4612, this.dir, 0);
      gl.glLightf(16384 + idx, 4613, this.exp);
      gl.glLightf(16384 + idx, 4614, this.cut);
   }

   @Override
   public void disable(GOut g, int idx) {
      GL2 gl = g.gl;
      gl.glLightfv(16384 + idx, 4612, defdir, 0);
      gl.glLightf(16384 + idx, 4613, 0.0F);
      gl.glLightf(16384 + idx, 4614, 180.0F);
      super.disable(g, idx);
   }
}
