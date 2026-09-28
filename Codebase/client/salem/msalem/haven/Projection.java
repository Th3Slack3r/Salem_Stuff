package haven;

import javax.media.opengl.GL2;

public class Projection extends Transform {
   private Matrix4f bk;

   public Projection(Matrix4f xf) {
      super(xf);
   }

   @Override
   public void apply(GOut g) {
      GL2 gl = g.gl;
      g.st.matmode(5889);
      gl.glPushMatrix();
      gl.glLoadMatrixf(this.fin(Matrix4f.id).m, 0);
   }

   @Override
   public void unapply(GOut g) {
      GL2 gl = g.gl;
      g.st.matmode(5889);
      gl.glPopMatrix();
   }

   @Override
   public void prep(GLState.Buffer b) {
      b.put(PView.proj, this);
   }

   public float[] toclip(Coord3f ec) {
      return this.fin(Matrix4f.id).mul4(ec.to4a(1.0F));
   }

   public Coord3f tonorm(Coord3f ec) {
      float[] o = this.toclip(ec);
      float d = 1.0F / o[3];
      return new Coord3f(o[0] * d, o[1] * d, o[2] * d);
   }

   public Coord3f toscreen(Coord3f ec, Coord sz) {
      Coord3f n = this.tonorm(ec);
      return new Coord3f((n.x + 1.0F) / 2.0F * sz.x, (-n.y + 1.0F) / 2.0F * sz.y, n.z);
   }

   public static Matrix4f makefrustum(Matrix4f d, float left, float right, float bottom, float top, float near, float far) {
      d.m[0] = 2.0F * near / (right - left);
      d.m[5] = 2.0F * near / (top - bottom);
      d.m[8] = (right + left) / (right - left);
      d.m[9] = (top + bottom) / (top - bottom);
      d.m[10] = -(far + near) / (far - near);
      d.m[11] = -1.0F;
      d.m[14] = -(2.0F * far * near) / (far - near);
      d.m[1] = d.m[2] = d.m[3] = d.m[4] = d.m[6] = d.m[7] = d.m[12] = d.m[13] = d.m[15] = 0.0F;
      return d;
   }

   public static Projection frustum(float left, float right, float bottom, float top, float near, float far) {
      return new Projection(makefrustum(new Matrix4f(), left, right, bottom, top, near, far));
   }

   public static Matrix4f makeortho(Matrix4f d, float left, float right, float bottom, float top, float near, float far) {
      d.m[0] = 2.0F / (right - left);
      d.m[5] = 2.0F / (top - bottom);
      d.m[10] = -2.0F / (far - near);
      d.m[12] = -(right + left) / (right - left);
      d.m[13] = -(top + bottom) / (top - bottom);
      d.m[14] = -(far + near) / (far - near);
      d.m[15] = 1.0F;
      d.m[1] = d.m[2] = d.m[3] = d.m[4] = d.m[6] = d.m[7] = d.m[8] = d.m[9] = d.m[11] = 0.0F;
      return d;
   }

   public static Projection ortho(float left, float right, float bottom, float top, float near, float far) {
      return new Projection(makeortho(new Matrix4f(), left, right, bottom, top, near, far));
   }

   public static class Modification extends Projection {
      public Projection bk;

      public Modification(Projection bk, Matrix4f mod) {
         super(mod);
         this.bk = bk;
      }

      @Override
      public Matrix4f fin(Matrix4f p) {
         return this.bk.fin(super.fin(p));
      }
   }
}
