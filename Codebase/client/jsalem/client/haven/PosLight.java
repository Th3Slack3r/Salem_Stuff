package haven;

import java.awt.Color;
import javax.media.opengl.GL2;

public class PosLight extends Light {
   public float[] pos;
   public float ac = 1.0F;
   public float al = 0.0F;
   public float aq = 0.0F;

   public PosLight(FColor col, Coord3f pos) {
      super(col);
      this.pos = pos.to4a(1.0F);
   }

   public PosLight(Color col, Coord3f pos) {
      super(col);
      this.pos = pos.to4a(1.0F);
   }

   public PosLight(FColor amb, FColor dif, FColor spc, Coord3f pos) {
      super(amb, dif, spc);
      this.pos = pos.to4a(1.0F);
   }

   public PosLight(Color amb, Color dif, Color spc, Coord3f pos) {
      super(amb, dif, spc);
      this.pos = pos.to4a(1.0F);
   }

   public void move(Coord3f pos) {
      this.pos = pos.to4a(1.0F);
   }

   public void att(float c, float l, float q) {
      this.ac = c;
      this.al = l;
      this.aq = q;
   }

   @Override
   public void enable(GOut g, int idx) {
      super.enable(g, idx);
      GL2 gl = g.gl;
      gl.glLightfv(16384 + idx, 4611, this.pos, 0);
      gl.glLightf(16384 + idx, 4615, this.ac);
      gl.glLightf(16384 + idx, 4616, this.al);
      gl.glLightf(16384 + idx, 4617, this.aq);
   }

   @Override
   public void disable(GOut g, int idx) {
      GL2 gl = g.gl;
      gl.glLightf(16384 + idx, 4615, 1.0F);
      gl.glLightf(16384 + idx, 4616, 0.0F);
      gl.glLightf(16384 + idx, 4617, 0.0F);
      super.disable(g, idx);
   }
}
