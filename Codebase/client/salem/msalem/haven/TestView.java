package haven;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;

public class TestView extends PView {
   static final FastMesh[] tmesh;
   final PointedCam camera;
   int sel = -1;

   public TestView(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
      this.camera = new PointedCam();
      this.camera.a = (float) (Math.PI * 3.0 / 2.0);
      this.camera.e = (float) (Math.PI / 2);
      this.setcanfocus(true);
   }

   protected Camera camera() {
      return this.camera;
   }

   @Override
   protected void setup(RenderList rls) {
      int i = 0;

      for (FastMesh m : tmesh) {
         if (this.sel == -1 || i == this.sel) {
            rls.add(m, Location.rot(new Coord3f(1.0F, 0.0F, 0.0F), 180.0F));
         }

         i++;
      }

      rls.add(new TestView.Cube(), Location.xlate(new Coord3f(-1.5F, 0.0F, 0.0F)));
      rls.add(new TestView.Cube(), Location.xlate(new Coord3f(1.5F, 0.0F, 0.0F)));
   }

   @Override
   public void mousemove(Coord c) {
      if (c.x >= 0 && c.x < this.sz.x && c.y >= 0 && c.y < this.sz.y) {
         this.camera.e = (float) (Math.PI / 2) * ((float)c.y / this.sz.y);
         this.camera.a = (float) (Math.PI * 2) * ((float)c.x / this.sz.x);
      }
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      float d = this.camera.dist + amount * 5;
      if (d < 5.0F) {
         d = 5.0F;
      }

      this.camera.dist = d;
      return true;
   }

   @Override
   public boolean type(char key, KeyEvent ev) {
      if (key == ' ') {
         this.sel = -1;
         return true;
      } else if (key >= '0' && key < 48 + tmesh.length) {
         this.sel = key - '0';
         return true;
      } else {
         return false;
      }
   }

   static {
      Resource res = Resource.load("gfx/borka/body");
      res.loadwait();
      List<FastMesh> l = new ArrayList<>();

      for (FastMesh.MeshRes m : res.layers(FastMesh.MeshRes.class)) {
         l.add(m.m);
      }

      tmesh = l.toArray(new FastMesh[0]);
   }

   public static class Cube implements Rendered {
      @Override
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

      @Override
      public boolean setup(RenderList rls) {
         rls.state().put(States.color, null);
         return true;
      }
   }
}
