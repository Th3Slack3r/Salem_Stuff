package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.glu.GLU;

public class GOut {
   public final GL2 gl;
   public final GLConfig gc;
   public Coord ul;
   public Coord sz;
   public Coord tx;
   private States.ColState color = new States.ColState(Color.WHITE);
   public final GLContext ctx;
   private final GOut root;
   public final GLState.Applier st;
   private final GLState.Buffer def2d;

   protected GOut(GOut o) {
      this.gl = o.gl;
      this.gc = o.gc;
      this.ul = o.ul;
      this.sz = o.sz;
      this.tx = o.tx;
      this.color = o.color;
      this.ctx = o.ctx;
      this.root = o.root;
      this.st = o.st;
      this.def2d = o.def2d;
      this.st.set(this.def2d);
   }

   public GOut(GL2 gl, GLContext ctx, GLConfig cfg, GLState.Applier st, GLState.Buffer def2d, Coord sz) {
      this.gl = gl;
      this.gc = cfg;
      this.ul = this.tx = Coord.z;
      this.sz = sz;
      this.ctx = ctx;
      this.st = st;
      this.root = this;
      this.def2d = def2d;
   }

   public static GOut.GLException glexcfor(int code) {
      switch (code) {
         case 1280:
            return new GOut.GLInvalidEnumException();
         case 1281:
            return new GOut.GLInvalidValueException();
         case 1282:
            return new GOut.GLInvalidOperationException();
         case 1283:
         case 1284:
         default:
            return new GOut.GLException(code);
         case 1285:
            return new GOut.GLOutOfMemoryException();
      }
   }

   public static void checkerr(GL gl) {
      int err = gl.glGetError();
      if (err != 0) {
         throw glexcfor(err);
      }
   }

   private void checkerr() {
      checkerr(this.gl);
   }

   public GOut root() {
      return this.root;
   }

   public GLState.Buffer basicstate() {
      return this.def2d.copy();
   }

   public void image(BufferedImage img, Coord c) {
      if (img != null) {
         Tex tex = new TexI(img);
         this.image(tex, c);
         tex.dispose();
      }
   }

   public void image(Resource.Image img, Coord c) {
      if (img != null) {
         this.image(img.tex(), c.add(img.o));
      }
   }

   public void image(Tex tex, Coord c) {
      if (tex != null) {
         this.st.set(this.def2d);
         this.state(this.color);
         tex.crender(this, c.add(this.tx), this.ul, this.sz);
         this.checkerr();
      }
   }

   public void image(Indir<Tex> tex, Coord c) {
      this.image(tex.get(), c);
   }

   public void aimage(Tex tex, Coord c, double ax, double ay) {
      Coord sz = tex.sz();
      this.image(tex, c.add((int)(sz.x * -ax), (int)(sz.y * -ay)));
   }

   public void image(Tex tex, Coord c, Coord sz) {
      if (tex != null) {
         this.st.set(this.def2d);
         this.state(this.color);
         tex.crender(this, c.add(this.tx), this.ul, this.sz, sz);
         this.checkerr();
      }
   }

   public void image(Tex tex, Coord c, Coord ul, Coord sz) {
      if (tex != null) {
         this.st.set(this.def2d);
         this.state(this.color);
         ul = ul.add(this.tx);
         Coord br = ul.add(sz);
         if (ul.x < this.ul.x) {
            ul.x = this.ul.x;
         }

         if (ul.y < this.ul.y) {
            ul.y = this.ul.y;
         }

         if (br.x > this.ul.x + this.sz.x) {
            br.x = this.ul.x + this.sz.x;
         }

         if (br.y > this.ul.y + this.sz.y) {
            br.y = this.ul.y + this.sz.y;
         }

         tex.crender(this, c.add(this.tx), ul, br.sub(ul));
         this.checkerr();
      }
   }

   public void image(Tex tex, Coord c, GLState s) {
      this.st.set(this.def2d);
      if (s != null) {
         this.state(s);
      }

      tex.crender(this, c.add(this.tx), this.ul, this.sz);
      this.checkerr();
   }

   public void vertex(Coord c) {
      this.gl.glVertex2i(c.x + this.tx.x, c.y + this.tx.y);
   }

   public void vertex(float x, float y) {
      this.gl.glVertex2f(x + this.tx.x, y + this.tx.y);
   }

   public void apply() {
      this.st.apply(this);
   }

   public void state(GLState st) {
      this.st.prep(st);
   }

   public void state2d() {
      this.st.set(this.def2d);
   }

   public void line(Coord c1, Coord c2, double w) {
      this.st.set(this.def2d);
      this.state(this.color);
      this.apply();
      this.gl.glLineWidth((float)w);
      this.gl.glBegin(1);
      this.vertex(c1);
      this.vertex(c2);
      this.gl.glEnd();
      this.checkerr();
   }

   public void text(String text, Coord c) {
      this.atext(text, c, 0.0, 0.0);
   }

   public void atext(String text, Coord c, double ax, double ay) {
      Text t = Text.render(text);
      Tex T = t.tex();
      Coord sz = t.sz();
      this.image(T, c.add((int)(sz.x * -ax), (int)(sz.y * -ay)));
      T.dispose();
      this.checkerr();
   }

   public void poly(Coord... c) {
      this.st.set(this.def2d);
      this.state(this.color);
      this.apply();
      this.gl.glBegin(9);

      for (Coord vc : c) {
         this.vertex(vc);
      }

      this.gl.glEnd();
      this.checkerr();
   }

   public void poly2(Object... c) {
      this.st.set(this.def2d);
      this.st.put(States.color, States.vertexcolor);
      this.apply();
      this.gl.glBegin(9);

      for (int i = 0; i < c.length; i += 2) {
         Coord vc = (Coord)c[i];
         Color col = (Color)c[i + 1];
         this.gl.glColor4f(col.getRed() / 255.0F, col.getGreen() / 255.0F, col.getBlue() / 255.0F, col.getAlpha() / 255.0F);
         this.vertex(vc);
      }

      this.gl.glEnd();
      this.checkerr();
   }

   public void frect(Coord ul, Coord sz) {
      ul = this.tx.add(ul);
      Coord br = ul.add(sz);
      if (ul.x < this.ul.x) {
         ul.x = this.ul.x;
      }

      if (ul.y < this.ul.y) {
         ul.y = this.ul.y;
      }

      if (br.x > this.ul.x + this.sz.x) {
         br.x = this.ul.x + this.sz.x;
      }

      if (br.y > this.ul.y + this.sz.y) {
         br.y = this.ul.y + this.sz.y;
      }

      if (ul.x < br.x && ul.y < br.y) {
         this.st.set(this.def2d);
         this.state(this.color);
         this.apply();
         this.gl.glBegin(7);
         this.gl.glVertex2i(ul.x, ul.y);
         this.gl.glVertex2i(br.x, ul.y);
         this.gl.glVertex2i(br.x, br.y);
         this.gl.glVertex2i(ul.x, br.y);
         this.gl.glEnd();
         this.checkerr();
      }
   }

   public void frect(Coord c1, Coord c2, Coord c3, Coord c4) {
      this.st.set(this.def2d);
      this.state(this.color);
      this.apply();
      this.gl.glBegin(7);
      this.vertex(c1);
      this.vertex(c2);
      this.vertex(c3);
      this.vertex(c4);
      this.gl.glEnd();
      this.checkerr();
   }

   public void ftexrect(Coord ul, Coord sz, GLState s, float tl, float tt, float tr, float tb) {
      ul = this.tx.add(ul);
      Coord br = ul.add(sz);
      Coord ult = new Coord(0, 0);
      Coord brt = new Coord(sz);
      if (ul.x < this.ul.x) {
         ult.x = ult.x + (this.ul.x - ul.x);
         ul.x = this.ul.x;
      }

      if (ul.y < this.ul.y) {
         ult.y = ult.y + (this.ul.y - ul.y);
         ul.y = this.ul.y;
      }

      if (br.x > this.ul.x + this.sz.x) {
         brt.x = brt.x - (br.x - (this.ul.x + this.sz.x));
         br.x = this.ul.x + this.sz.x;
      }

      if (br.y > this.ul.y + this.sz.y) {
         brt.y = brt.y - (br.y - (this.ul.y + this.sz.y));
         br.y = this.ul.y + this.sz.y;
      }

      if (ul.x < br.x && ul.y < br.y) {
         this.st.set(this.def2d);
         this.state(s);
         this.apply();
         float l = tl + (tr - tl) * ult.x / sz.x;
         float t = tt + (tb - tt) * ult.y / sz.y;
         float r = tl + (tr - tl) * brt.x / sz.x;
         float b = tt + (tb - tt) * brt.y / sz.y;
         this.gl.glBegin(7);
         this.gl.glTexCoord2f(l, b);
         this.gl.glVertex2i(ul.x, ul.y);
         this.gl.glTexCoord2f(r, b);
         this.gl.glVertex2i(br.x, ul.y);
         this.gl.glTexCoord2f(r, t);
         this.gl.glVertex2i(br.x, br.y);
         this.gl.glTexCoord2f(l, t);
         this.gl.glVertex2i(ul.x, br.y);
         this.gl.glEnd();
         this.checkerr();
      }
   }

   public void ftexrect(Coord ul, Coord sz, GLState s) {
      this.ftexrect(ul, sz, s, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   public void fellipse(Coord c, Coord r, int a1, int a2) {
      this.st.set(this.def2d);
      this.state(this.color);
      this.apply();
      this.gl.glBegin(6);
      this.vertex(c);

      for (int i = a1; i <= a2; i += 5) {
         double a = i * Math.PI * 2.0 / 360.0;
         this.vertex(c.add((int)(Math.cos(a) * r.x), -((int)(Math.sin(a) * r.y))));
      }

      this.gl.glEnd();
      this.checkerr();
   }

   public void fellipse(Coord c, Coord r) {
      this.fellipse(c, r, 0, 360);
   }

   public void rect(Coord ul, Coord sz) {
      this.st.set(this.def2d);
      this.state(this.color);
      this.apply();
      this.gl.glLineWidth(1.0F);
      this.gl.glBegin(2);
      this.vertex(ul.x + 0.5F, ul.y + 0.5F);
      this.vertex(ul.x + sz.x - 0.5F, ul.y + 0.5F);
      this.vertex(ul.x + sz.x - 0.5F, ul.y + sz.y - 0.5F);
      this.vertex(ul.x + 0.5F, ul.y + sz.y - 0.5F);
      this.gl.glEnd();
      this.checkerr();
   }

   public void prect(Coord c, Coord ul, Coord br, double a) {
      this.st.set(this.def2d);
      this.state(this.color);
      this.apply();
      this.gl.glEnable(2881);
      this.gl.glBegin(6);
      this.vertex(c);
      this.vertex(c.add(0, ul.y));
      double p2 = Math.PI / 2;
      float tc = (float)(Math.tan(a) * -ul.y);
      if (!(a > p2) && !(tc > br.x)) {
         this.vertex(c.x + tc, c.y + ul.y);
      } else {
         this.vertex(c.x + br.x, c.y + ul.y);
         tc = (float)(Math.tan(a - (Math.PI / 2)) * br.x);
         if (!(a > p2 * 2.0) && !(tc > br.y)) {
            this.vertex(c.x + br.x, c.y + tc);
         } else {
            this.vertex(c.x + br.x, c.y + br.y);
            tc = (float)(-Math.tan(a - Math.PI) * br.y);
            if (!(a > p2 * 3.0) && !(tc < ul.x)) {
               this.vertex(c.x + tc, c.y + br.y);
            } else {
               this.vertex(c.x + ul.x, c.y + br.y);
               tc = (float)(-Math.tan(a - (Math.PI * 3.0 / 2.0)) * -ul.x);
               if (!(a > p2 * 4.0) && !(tc < ul.y)) {
                  this.vertex(c.x + ul.x, c.y + tc);
               } else {
                  this.vertex(c.x + ul.x, c.y + ul.y);
                  tc = (float)(Math.tan(a) * -ul.y);
                  this.vertex(c.x + tc, c.y + ul.y);
               }
            }
         }
      }

      this.gl.glEnd();
      this.gl.glDisable(2881);
      this.checkerr();
   }

   public void chcolor(Color c) {
      if (!c.equals(this.color.c)) {
         this.color = new States.ColState(c);
      }
   }

   public void chcolor(int r, int g, int b, int a) {
      this.chcolor(Utils.clipcol(r, g, b, a));
   }

   public void chcolor() {
      this.chcolor(Color.WHITE);
   }

   Color getcolor() {
      return this.color.c;
   }

   public GOut reclip(Coord ul, Coord sz) {
      GOut g = new GOut(this);
      g.tx = this.tx.add(ul);
      g.ul = new Coord(g.tx);
      Coord gbr = g.ul.add(sz);
      Coord tbr = this.ul.add(this.sz);
      if (g.ul.x < this.ul.x) {
         g.ul.x = this.ul.x;
      }

      if (g.ul.y < this.ul.y) {
         g.ul.y = this.ul.y;
      }

      if (gbr.x > tbr.x) {
         gbr.x = tbr.x;
      }

      if (gbr.y > tbr.y) {
         gbr.y = tbr.y;
      }

      g.sz = gbr.sub(g.ul);
      return g;
   }

   public GOut reclipl(Coord ul, Coord sz) {
      GOut g = new GOut(this);
      g.tx = this.tx.add(ul);
      g.ul = new Coord(g.tx);
      g.sz = sz;
      return g;
   }

   public Color getpixel(Coord c) {
      byte[] buf = new byte[4];
      this.gl.glReadPixels(c.x + this.tx.x, this.root.sz.y - c.y - this.tx.y, 1, 1, 6408, 5121, ByteBuffer.wrap(buf));
      this.checkerr();
      return new Color(buf[0] & 255, buf[1] & 255, buf[2] & 255);
   }

   public BufferedImage getimage(Coord ul, Coord sz) {
      byte[] buf = new byte[sz.x * sz.y * 4];
      this.gl.glReadPixels(ul.x + this.tx.x, this.root.sz.y - ul.y - sz.y - this.tx.y, sz.x, sz.y, 6408, 5121, ByteBuffer.wrap(buf));
      this.checkerr();

      for (int y = 0; y < sz.y / 2; y++) {
         int to = y * sz.x * 4;
         int bo = (sz.y - y - 1) * sz.x * 4;

         for (int o = 0; o < sz.x * 4; bo++) {
            byte t = buf[to];
            buf[to] = buf[bo];
            buf[bo] = t;
            o++;
            to++;
         }
      }

      WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(buf, buf.length), sz.x, sz.y, 4 * sz.x, 4, new int[]{0, 1, 2, 3}, null);
      return new BufferedImage(TexI.glcm, raster, false, null);
   }

   public BufferedImage getimage() {
      return this.getimage(Coord.z, this.sz);
   }

   public static class GLException extends RuntimeException {
      public int code;
      public String str;
      private static GLU glu = new GLU();

      public GLException(int code) {
         super("GL Error: " + code + " (" + glu.gluErrorString(code) + ")");
         this.code = code;
         this.str = glu.gluErrorString(code);
      }
   }

   public static class GLInvalidEnumException extends GOut.GLException {
      public GLInvalidEnumException() {
         super(1280);
      }
   }

   public static class GLInvalidOperationException extends GOut.GLException {
      public GLInvalidOperationException() {
         super(1282);
      }
   }

   public static class GLInvalidValueException extends GOut.GLException {
      public GLInvalidValueException() {
         super(1281);
      }
   }

   public static class GLOutOfMemoryException extends GOut.GLException {
      public GLOutOfMemoryException() {
         super(1285);
      }
   }
}
