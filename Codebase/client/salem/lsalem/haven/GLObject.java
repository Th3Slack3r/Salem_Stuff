package haven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public abstract class GLObject {
   private static final Map<GL, Collection<GLObject>> disposed = new HashMap<>();
   private boolean del;
   public final GL2 gl;

   public GLObject(GL2 gl) {
      this.gl = gl;
   }

   public void dispose() {
      Collection<GLObject> can;
      synchronized (disposed) {
         if (this.del) {
            return;
         }

         this.del = true;
         can = disposed.get(this.gl);
         if (can == null) {
            can = new LinkedList<>();
            disposed.put(this.gl, can);
         }
      }

      synchronized (can) {
         can.add(this);
      }
   }

   @Override
   protected void finalize() {
      this.dispose();
   }

   protected abstract void delete();

   public static void disposeall(GL2 gl) {
      Collection<GLObject> can;
      synchronized (disposed) {
         can = disposed.get(gl);
         if (can == null) {
            return;
         }
      }

      Collection<GLObject> copy;
      synchronized (can) {
         copy = new ArrayList<>(can);
         can.clear();
      }

      for (GLObject obj : copy) {
         obj.delete();
      }

      GOut.checkerr(gl);
   }
}
