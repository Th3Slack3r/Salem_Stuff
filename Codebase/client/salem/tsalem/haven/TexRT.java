package haven;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public abstract class TexRT extends TexGL {
  static Map<GL, Collection<TexRT>> current = new WeakHashMap<>();
  
  private GL incurrent = null;
  
  public Profile prof = new Profile(300);
  
  private Profile.Frame curf;
  
  public TexRT(Coord sz) {
    super(sz);
  }
  
  protected abstract boolean subrend(GOut paramGOut);
  
  private void rerender(GL gl) {
    if (this.incurrent != gl) {
      Collection<TexRT> tc;
      synchronized (current) {
        tc = current.get(gl);
        if (tc == null) {
          tc = new HashSet<>();
          current.put(gl, tc);
        } 
      } 
      synchronized (tc) {
        tc.add(this);
      } 
      this.incurrent = gl;
    } 
  }
  
  public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
    super.render(g, c, ul, br, sz);
    rerender((GL)g.gl);
  }
  
  protected byte[] initdata() {
    return new byte[this.tdim.x * this.tdim.y * 4];
  }
  
  protected void fill(GOut g) {
    rerender((GL)g.gl);
    byte[] idat = initdata();
    g.gl.glTexImage2D(3553, 0, 6408, this.tdim.x, this.tdim.y, 0, 6408, 5121, (idat == null) ? null : ByteBuffer.wrap(idat));
    GOut.checkerr((GL)g.gl);
  }
  
  private void subrend2(GOut g) {
    if (this.t == null)
      return; 
    GL2 gL2 = g.gl;
    if (Config.profile) {
      this.prof.getClass();
      this.curf = new Profile.Frame(this.prof);
    } 
    if (!subrend(g))
      return; 
    if (this.curf != null)
      this.curf.tick("render"); 
    g.st.prep(draw());
    g.apply();
    gL2.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, this.dim.x, this.dim.y);
    GOut.checkerr((GL)gL2);
    if (this.curf != null) {
      this.curf.tick("copy");
      this.curf.fin();
      this.curf = null;
    } 
  }
  
  public static void renderall(GOut g) {
    Collection<TexRT> tc;
    GL2 gL2 = g.gl;
    synchronized (current) {
      tc = current.get(gL2);
      current.put(gL2, new HashSet<>());
    } 
    if (tc != null)
      synchronized (tc) {
        for (TexRT t : tc) {
          t.incurrent = null;
          t.subrend2(g);
        } 
      }  
  }
}
