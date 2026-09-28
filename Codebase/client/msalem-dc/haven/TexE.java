package haven;

import javax.media.opengl.GL;

public class TexE extends TexGL {
   public final int ifmt;
   public final int dfmt;
   public final int dtype;
   public boolean invert;

   public TexE(Coord sz, int ifmt, int dfmt, int dtype, boolean invert) {
      super(sz, sz);
      this.ifmt = ifmt;
      this.dfmt = dfmt;
      this.dtype = dtype;
      this.invert = invert;
   }

   public TexE(Coord sz, int ifmt, int dfmt, int dtype) {
      this(sz, ifmt, dfmt, dtype, true);
   }

   public TexE(Coord sz) {
      this(sz, 6408, 6408, 5121);
   }

   @Override
   protected void fill(GOut g) {
      GL gl = g.gl;
      gl.glTexImage2D(3553, 0, this.ifmt, this.tdim.x, this.tdim.y, 0, this.dfmt, this.dtype, null);
   }

   @Override
   public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
      if (this.invert) {
         super.render(g, c, new Coord(ul.x, br.y), new Coord(br.x, ul.y), sz);
      } else {
         super.render(g, c, ul, br, sz);
      }
   }
}
