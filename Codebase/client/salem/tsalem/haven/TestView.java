package haven;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;

public class TestView extends PView {
  static final FastMesh[] tmesh;
  
  final PointedCam camera;
  
  static {
    Resource res = Resource.load("gfx/borka/body");
    res.loadwait();
    List<FastMesh> l = new ArrayList<>();
    for (FastMesh.MeshRes m : res.<FastMesh.MeshRes>layers(FastMesh.MeshRes.class))
      l.add(m.m); 
    tmesh = l.<FastMesh>toArray(new FastMesh[0]);
  }
  
  int sel = -1;
  
  public TestView(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
    this.camera = new PointedCam();
    this.camera.a = 4.712389F;
    this.camera.e = 1.5707964F;
    setcanfocus(true);
  }
  
  protected Camera camera() {
    return this.camera;
  }
  
  public static class Cube implements Rendered {
    public void draw(GOut g) {
      GL2 gl = g.gl;
      gl.glBegin(7);
      gl.glNormal3f(0.0F, 0.0F, 1.0F);
      gl.glColor3f(1.0F, 0.0F, 0.0F);
      gl.glVertex3f(-1.0F, 1.0F, 1.0F);
      gl.glVertex3f(-1.0F, -1.0F, 1.0F);
      gl.glVertex3f(1.0F, -1.0F, 1.0F);
      gl.glVertex3f(1.0F, 1.0F, 1.0F);
      gl.glNormal3f(1.0F, 0.0F, 0.0F);
      gl.glColor3f(0.0F, 1.0F, 0.0F);
      gl.glVertex3f(1.0F, 1.0F, 1.0F);
      gl.glVertex3f(1.0F, -1.0F, 1.0F);
      gl.glVertex3f(1.0F, -1.0F, -1.0F);
      gl.glVertex3f(1.0F, 1.0F, -1.0F);
      gl.glNormal3f(-1.0F, 0.0F, 0.0F);
      gl.glColor3f(0.0F, 0.0F, 1.0F);
      gl.glVertex3f(-1.0F, 1.0F, 1.0F);
      gl.glVertex3f(-1.0F, 1.0F, -1.0F);
      gl.glVertex3f(-1.0F, -1.0F, -1.0F);
      gl.glVertex3f(-1.0F, -1.0F, 1.0F);
      gl.glNormal3f(0.0F, 1.0F, 0.0F);
      gl.glColor3f(0.0F, 1.0F, 1.0F);
      gl.glVertex3f(-1.0F, 1.0F, 1.0F);
      gl.glVertex3f(1.0F, 1.0F, 1.0F);
      gl.glVertex3f(1.0F, 1.0F, -1.0F);
      gl.glVertex3f(-1.0F, 1.0F, -1.0F);
      gl.glNormal3f(0.0F, -1.0F, 0.0F);
      gl.glColor3f(1.0F, 0.0F, 1.0F);
      gl.glVertex3f(-1.0F, -1.0F, 1.0F);
      gl.glVertex3f(-1.0F, -1.0F, -1.0F);
      gl.glVertex3f(1.0F, -1.0F, -1.0F);
      gl.glVertex3f(1.0F, -1.0F, 1.0F);
      gl.glNormal3f(0.0F, 0.0F, -1.0F);
      gl.glColor3f(1.0F, 1.0F, 0.0F);
      gl.glVertex3f(-1.0F, 1.0F, -1.0F);
      gl.glVertex3f(1.0F, 1.0F, -1.0F);
      gl.glVertex3f(1.0F, -1.0F, -1.0F);
      gl.glVertex3f(-1.0F, -1.0F, -1.0F);
      gl.glEnd();
    }
    
    public boolean setup(RenderList rls) {
      rls.state().put(States.color, null);
      return true;
    }
  }
  
  protected void setup(RenderList rls) {
    int i = 0;
    for (FastMesh m : tmesh) {
      if (this.sel == -1 || i == this.sel)
        rls.add(m, Location.rot(new Coord3f(1.0F, 0.0F, 0.0F), 180.0F)); 
      i++;
    } 
    rls.add(new Cube(), Location.xlate(new Coord3f(-1.5F, 0.0F, 0.0F)));
    rls.add(new Cube(), Location.xlate(new Coord3f(1.5F, 0.0F, 0.0F)));
  }
  
  public void mousemove(Coord c) {
    if (c.x < 0 || c.x >= this.sz.x || c.y < 0 || c.y >= this.sz.y)
      return; 
    this.camera.e = 1.5707964F * c.y / this.sz.y;
    this.camera.a = 6.2831855F * c.x / this.sz.x;
  }
  
  public boolean mousewheel(Coord c, int amount) {
    float d = this.camera.dist + (amount * 5);
    if (d < 5.0F)
      d = 5.0F; 
    this.camera.dist = d;
    return true;
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (key == ' ') {
      this.sel = -1;
      return true;
    } 
    if (key >= '0' && key < 48 + tmesh.length) {
      this.sel = key - 48;
      return true;
    } 
    return false;
  }
}
