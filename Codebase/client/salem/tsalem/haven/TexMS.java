package haven;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public abstract class TexMS {
  protected TexGL.TexOb t = null;
  
  public final int w;
  
  public final int h;
  
  public final int s;
  
  public TexMS(int w, int h, int s) {
    this.w = w;
    this.h = h;
    this.s = s;
  }
  
  protected abstract void fill(GOut paramGOut);
  
  private void create(GOut g) {
    GL2 gl = g.gl;
    this.t = new TexGL.TexOb(gl);
    gl.glBindTexture(37120, this.t.id);
    fill(g);
    GOut.checkerr((GL)gl);
  }
  
  public int glid(GOut g) {
    synchronized (this) {
      if (this.t != null && this.t.gl != g.gl)
        dispose(); 
      if (this.t == null)
        create(g); 
      return this.t.id;
    } 
  }
  
  public void dispose() {
    synchronized (this) {
      if (this.t != null) {
        this.t.dispose();
        this.t = null;
      } 
    } 
  }
}
