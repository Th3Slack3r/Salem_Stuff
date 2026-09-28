package haven;

import javax.media.opengl.GL3bc;

public class TexMSE extends TexMS {
   public final int ifmt;
   public final int dfmt;
   public final int dtype;
   public final boolean fixed;

   public TexMSE(Coord sz, int samples, int ifmt, int dfmt, int dtype, boolean fixed) {
      super(sz.x, sz.y, samples);
      this.ifmt = ifmt;
      this.dfmt = dfmt;
      this.dtype = dtype;
      this.fixed = fixed;
   }

   public TexMSE(Coord sz, int samples, int ifmt, int dfmt, int dtype) {
      this(sz, samples, ifmt, dfmt, dtype, false);
   }

   @Override
   protected void fill(GOut g) {
      GL3bc gl = g.gl.getGL3bc();
      gl.glTexImage2DMultisample(37120, this.s, this.ifmt, this.w, this.h, this.fixed);
   }
}
