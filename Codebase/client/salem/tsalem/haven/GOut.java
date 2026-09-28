package haven;

import java.awt.Color;
import java.awt.Point;
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
  
  public static class GLInvalidEnumException extends GLException {
    public GLInvalidEnumException() {
      super(1280);
    }
  }
  
  public static class GLInvalidValueException extends GLException {
    public GLInvalidValueException() {
      super(1281);
    }
  }
  
  public static class GLInvalidOperationException extends GLException {
    public GLInvalidOperationException() {
      super(1282);
    }
  }
  
  public static class GLOutOfMemoryException extends GLException {
    public GLOutOfMemoryException() {
      super(1285);
    }
  }
  
  public static GLException glexcfor(int code) {
    switch (code) {
      case 1280:
        return new GLInvalidEnumException();
      case 1281:
        return new GLInvalidValueException();
      case 1282:
        return new GLInvalidOperationException();
      case 1285:
        return new GLOutOfMemoryException();
    } 
    return new GLException(code);
  }
  
  public static void checkerr(GL gl) {
    int err = gl.glGetError();
    if (err != 0)
      throw glexcfor(err); 
  }
  
  private void checkerr() {
    checkerr((GL)this.gl);
  }
  
  public GOut root() {
    return this.root;
  }
  
  public GLState.Buffer basicstate() {
    return this.def2d.copy();
  }
  
  public void image(BufferedImage img, Coord c) {
    if (img == null)
      return; 
    Tex tex = new TexI(img);
    image(tex, c);
    tex.dispose();
  }
  
  public void image(Resource.Image img, Coord c) {
    if (img == null)
      return; 
    image(img.tex(), c.add(img.o));
  }
  
  public void image(Tex tex, Coord c) {
    if (tex == null)
      return; 
    this.st.set(this.def2d);
    state(this.color);
    tex.crender(this, c.add(this.tx), this.ul, this.sz);
    checkerr();
  }
  
  public void image(Indir<Tex> tex, Coord c) {
    image(tex.get(), c);
  }
  
  public void aimage(Tex tex, Coord c, double ax, double ay) {
    Coord sz = tex.sz();
    image(tex, c.add((int)(sz.x * -ax), (int)(sz.y * -ay)));
  }
  
  public void image(Tex tex, Coord c, Coord sz) {
    if (tex == null)
      return; 
    this.st.set(this.def2d);
    state(this.color);
    tex.crender(this, c.add(this.tx), this.ul, this.sz, sz);
    checkerr();
  }
  
  public void image(Tex tex, Coord c, Coord ul, Coord sz) {
    if (tex == null)
      return; 
    this.st.set(this.def2d);
    state(this.color);
    ul = ul.add(this.tx);
    Coord br = ul.add(sz);
    if (ul.x < this.ul.x)
      ul.x = this.ul.x; 
    if (ul.y < this.ul.y)
      ul.y = this.ul.y; 
    if (br.x > this.ul.x + this.sz.x)
      this.ul.x += this.sz.x; 
    if (br.y > this.ul.y + this.sz.y)
      this.ul.y += this.sz.y; 
    tex.crender(this, c.add(this.tx), ul, br.sub(ul));
    checkerr();
  }
  
  public void image(Tex tex, Coord c, GLState s) {
    this.st.set(this.def2d);
    if (s != null)
      state(s); 
    tex.crender(this, c.add(this.tx), this.ul, this.sz);
    checkerr();
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
    state(this.color);
    apply();
    this.gl.glLineWidth((float)w);
    this.gl.glBegin(1);
    vertex(c1);
    vertex(c2);
    this.gl.glEnd();
    checkerr();
  }
  
  public void text(String text, Coord c) {
    atext(text, c, 0.0D, 0.0D);
  }
  
  public Coord atext(String text, Coord c, double ax, double ay) {
    Text t = Text.render(text);
    Tex T = t.tex();
    Coord sz = t.sz();
    image(T, c.add((int)(sz.x * -ax), (int)(sz.y * -ay)));
    T.dispose();
    checkerr();
    return sz;
  }
  
  public void poly(Coord... c) {
    this.st.set(this.def2d);
    state(this.color);
    apply();
    this.gl.glBegin(9);
    for (Coord vc : c)
      vertex(vc); 
    this.gl.glEnd();
    checkerr();
  }
  
  public void poly2(Object... c) {
    this.st.set(this.def2d);
    this.st.put(States.color, States.vertexcolor);
    apply();
    this.gl.glBegin(9);
    for (int i = 0; i < c.length; i += 2) {
      Coord vc = (Coord)c[i];
      Color col = (Color)c[i + 1];
      this.gl.glColor4f(col.getRed() / 255.0F, col.getGreen() / 255.0F, col.getBlue() / 255.0F, col.getAlpha() / 255.0F);
      vertex(vc);
    } 
    this.gl.glEnd();
    checkerr();
  }
  
  public void frect(Coord ul, Coord sz) {
    ul = this.tx.add(ul);
    Coord br = ul.add(sz);
    if (ul.x < this.ul.x)
      ul.x = this.ul.x; 
    if (ul.y < this.ul.y)
      ul.y = this.ul.y; 
    if (br.x > this.ul.x + this.sz.x)
      this.ul.x += this.sz.x; 
    if (br.y > this.ul.y + this.sz.y)
      this.ul.y += this.sz.y; 
    if (ul.x >= br.x || ul.y >= br.y)
      return; 
    this.st.set(this.def2d);
    state(this.color);
    apply();
    this.gl.glBegin(7);
    this.gl.glVertex2i(ul.x, ul.y);
    this.gl.glVertex2i(br.x, ul.y);
    this.gl.glVertex2i(br.x, br.y);
    this.gl.glVertex2i(ul.x, br.y);
    this.gl.glEnd();
    checkerr();
  }
  
  public void frect(Coord c1, Coord c2, Coord c3, Coord c4) {
    this.st.set(this.def2d);
    state(this.color);
    apply();
    this.gl.glBegin(7);
    vertex(c1);
    vertex(c2);
    vertex(c3);
    vertex(c4);
    this.gl.glEnd();
    checkerr();
  }
  
  public void ftexrect(Coord ul, Coord sz, GLState s, float tl, float tt, float tr, float tb) {
    ul = this.tx.add(ul);
    Coord br = ul.add(sz);
    Coord ult = new Coord(0, 0);
    Coord brt = new Coord(sz);
    if (ul.x < this.ul.x) {
      ult.x += this.ul.x - ul.x;
      ul.x = this.ul.x;
    } 
    if (ul.y < this.ul.y) {
      ult.y += this.ul.y - ul.y;
      ul.y = this.ul.y;
    } 
    if (br.x > this.ul.x + this.sz.x) {
      brt.x -= br.x - this.ul.x + this.sz.x;
      this.ul.x += this.sz.x;
    } 
    if (br.y > this.ul.y + this.sz.y) {
      brt.y -= br.y - this.ul.y + this.sz.y;
      this.ul.y += this.sz.y;
    } 
    if (ul.x >= br.x || ul.y >= br.y)
      return; 
    this.st.set(this.def2d);
    state(s);
    apply();
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
    checkerr();
  }
  
  public void ftexrect(Coord ul, Coord sz, GLState s) {
    ftexrect(ul, sz, s, 0.0F, 0.0F, 1.0F, 1.0F);
  }
  
  public void fellipse(Coord c, Coord r, int a1, int a2) {
    this.st.set(this.def2d);
    state(this.color);
    apply();
    this.gl.glBegin(6);
    vertex(c);
    for (int i = a1; i <= a2; i += 5) {
      double a = i * Math.PI * 2.0D / 360.0D;
      vertex(c.add((int)(Math.cos(a) * r.x), -((int)(Math.sin(a) * r.y))));
    } 
    this.gl.glEnd();
    checkerr();
  }
  
  public void fellipse(Coord c, Coord r) {
    fellipse(c, r, 0, 360);
  }
  
  public void rect(Coord ul, Coord sz) {
    this.st.set(this.def2d);
    state(this.color);
    apply();
    this.gl.glLineWidth(1.0F);
    this.gl.glBegin(2);
    vertex(ul.x + 0.5F, ul.y + 0.5F);
    vertex((ul.x + sz.x) - 0.5F, ul.y + 0.5F);
    vertex((ul.x + sz.x) - 0.5F, (ul.y + sz.y) - 0.5F);
    vertex(ul.x + 0.5F, (ul.y + sz.y) - 0.5F);
    this.gl.glEnd();
    checkerr();
  }
  
  public void prect(Coord c, Coord ul, Coord br, double a) {
    this.st.set(this.def2d);
    state(this.color);
    apply();
    this.gl.glEnable(2881);
    this.gl.glBegin(6);
    vertex(c);
    vertex(c.add(0, ul.y));
    double p2 = 1.5707963267948966D;
    float tc = (float)(Math.tan(a) * -ul.y);
    if (a > 1.5707963267948966D || tc > br.x) {
      vertex((c.x + br.x), (c.y + ul.y));
    } else {
      vertex(c.x + tc, (c.y + ul.y));
      this.gl.glEnd();
      this.gl.glDisable(2881);
      checkerr();
    } 
    tc = (float)(Math.tan(a - 1.5707963267948966D) * br.x);
    if (a > Math.PI || tc > br.y) {
      vertex((c.x + br.x), (c.y + br.y));
    } else {
      vertex((c.x + br.x), c.y + tc);
      this.gl.glEnd();
      this.gl.glDisable(2881);
      checkerr();
    } 
    tc = (float)(-Math.tan(a - Math.PI) * br.y);
    if (a > 4.71238898038469D || tc < ul.x) {
      vertex((c.x + ul.x), (c.y + br.y));
    } else {
      vertex(c.x + tc, (c.y + br.y));
      this.gl.glEnd();
      this.gl.glDisable(2881);
      checkerr();
    } 
    tc = (float)(-Math.tan(a - 4.71238898038469D) * -ul.x);
    if (a > 6.283185307179586D || tc < ul.y) {
      vertex((c.x + ul.x), (c.y + ul.y));
    } else {
      vertex((c.x + ul.x), c.y + tc);
      this.gl.glEnd();
      this.gl.glDisable(2881);
      checkerr();
    } 
    tc = (float)(Math.tan(a) * -ul.y);
    vertex(c.x + tc, (c.y + ul.y));
    this.gl.glEnd();
    this.gl.glDisable(2881);
    checkerr();
  }
  
  public void chcolor(Color c) {
    if (c.equals(this.color.c))
      return; 
    this.color = new States.ColState(c);
  }
  
  public void chcolor(int r, int g, int b, int a) {
    chcolor(Utils.clipcol(r, g, b, a));
  }
  
  public void chcolor() {
    chcolor(Color.WHITE);
  }
  
  Color getcolor() {
    return this.color.c;
  }
  
  public GOut reclip(Coord ul, Coord sz) {
    GOut g = new GOut(this);
    g.tx = this.tx.add(ul);
    g.ul = new Coord(g.tx);
    Coord gbr = g.ul.add(sz), tbr = this.ul.add(this.sz);
    if (g.ul.x < this.ul.x)
      g.ul.x = this.ul.x; 
    if (g.ul.y < this.ul.y)
      g.ul.y = this.ul.y; 
    if (gbr.x > tbr.x)
      gbr.x = tbr.x; 
    if (gbr.y > tbr.y)
      gbr.y = tbr.y; 
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
    checkerr();
    return new Color(buf[0] & 0xFF, buf[1] & 0xFF, buf[2] & 0xFF);
  }
  
  public BufferedImage getimage(Coord ul, Coord sz) {
    byte[] buf = new byte[sz.x * sz.y * 4];
    this.gl.glReadPixels(ul.x + this.tx.x, this.root.sz.y - ul.y - sz.y - this.tx.y, sz.x, sz.y, 6408, 5121, ByteBuffer.wrap(buf));
    checkerr();
    for (int y = 0; y < sz.y / 2; y++) {
      int to = y * sz.x * 4, bo = (sz.y - y - 1) * sz.x * 4;
      for (int o = 0; o < sz.x * 4; o++, to++, bo++) {
        byte t = buf[to];
        buf[to] = buf[bo];
        buf[bo] = t;
      } 
    } 
    WritableRaster raster = Raster.createInterleavedRaster(new DataBufferByte(buf, buf.length), sz.x, sz.y, 4 * sz.x, 4, new int[] { 0, 1, 2, 3 }, (Point)null);
    return new BufferedImage(TexI.glcm, raster, false, null);
  }
  
  public BufferedImage getimage() {
    return getimage(Coord.z, this.sz);
  }
}
