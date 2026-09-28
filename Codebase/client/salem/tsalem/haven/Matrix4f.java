package haven;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class Matrix4f {
  public final float[] m;
  
  public static final Matrix4f id = identity();
  
  public Matrix4f() {
    this.m = new float[16];
  }
  
  public Matrix4f(Matrix4f b) {
    this();
    System.arraycopy(b.m, 0, this.m, 0, 16);
  }
  
  public Matrix4f(float e00, float e01, float e02, float e03, float e10, float e11, float e12, float e13, float e20, float e21, float e22, float e23, float e30, float e31, float e32, float e33) {
    this();
    this.m[0] = e00;
    this.m[4] = e01;
    this.m[8] = e02;
    this.m[12] = e03;
    this.m[1] = e10;
    this.m[5] = e11;
    this.m[9] = e12;
    this.m[13] = e13;
    this.m[2] = e20;
    this.m[6] = e21;
    this.m[10] = e22;
    this.m[14] = e23;
    this.m[3] = e30;
    this.m[7] = e31;
    this.m[11] = e32;
    this.m[15] = e33;
  }
  
  public Matrix4f(float[] m) {
    this.m = m;
  }
  
  public static Matrix4f identity() {
    return new Matrix4f(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
  }
  
  public Matrix4f load(Matrix4f o) {
    for (int i = 0; i < 16; i++)
      this.m[i] = o.m[i]; 
    return this;
  }
  
  public float get(int x, int y) {
    return this.m[y + x * 4];
  }
  
  public void set(int x, int y, float v) {
    this.m[y + x * 4] = v;
  }
  
  public Matrix4f add(Matrix4f b) {
    Matrix4f n = new Matrix4f();
    for (int i = 0; i < 16; i++)
      n.m[i] = this.m[i] + b.m[i]; 
    return n;
  }
  
  public Coord3f mul4(Coord3f b) {
    float x = this.m[0] * b.x + this.m[4] * b.y + this.m[8] * b.z + this.m[12];
    float y = this.m[1] * b.x + this.m[5] * b.y + this.m[9] * b.z + this.m[13];
    float z = this.m[2] * b.x + this.m[6] * b.y + this.m[10] * b.z + this.m[14];
    return new Coord3f(x, y, z);
  }
  
  public float[] mul4(float[] b) {
    float x = this.m[0] * b[0] + this.m[4] * b[1] + this.m[8] * b[2] + this.m[12] * b[3];
    float y = this.m[1] * b[0] + this.m[5] * b[1] + this.m[9] * b[2] + this.m[13] * b[3];
    float z = this.m[2] * b[0] + this.m[6] * b[1] + this.m[10] * b[2] + this.m[14] * b[3];
    float w = this.m[3] * b[0] + this.m[7] * b[1] + this.m[11] * b[2] + this.m[15] * b[3];
    return new float[] { x, y, z, w };
  }
  
  public Matrix4f mul(Matrix4f o) {
    Matrix4f n = new Matrix4f();
    int i = 0;
    for (int x = 0; x < 16; x += 4) {
      for (int y = 0; y < 4; y++)
        n.m[i++] = this.m[y] * o.m[x] + this.m[y + 4] * o.m[x + 1] + this.m[y + 8] * o.m[x + 2] + this.m[y + 12] * o.m[x + 3]; 
    } 
    return n;
  }
  
  public Matrix4f mul1(Matrix4f o) {
    int i = 0;
    float[] n = new float[16];
    for (int x = 0; x < 16; x += 4) {
      for (int y = 0; y < 4; y++)
        n[i++] = this.m[y] * o.m[x] + this.m[y + 4] * o.m[x + 1] + this.m[y + 8] * o.m[x + 2] + this.m[y + 12] * o.m[x + 3]; 
    } 
    for (i = 0; i < 16; i++)
      this.m[i] = n[i]; 
    return this;
  }
  
  public Matrix4f transpose() {
    Matrix4f n = new Matrix4f();
    for (int y = 0; y < 4; y++) {
      for (int x = 0; x < 4; x++)
        n.set(x, y, get(y, x)); 
    } 
    return n;
  }
  
  public float[] trim3() {
    return new float[] { this.m[0], this.m[1], this.m[2], this.m[4], this.m[5], this.m[6], this.m[8], this.m[9], this.m[10] };
  }
  
  public Matrix4f trim3(float e33) {
    Matrix4f n = new Matrix4f(this);
    n.m[14] = 0.0F;
    n.m[13] = 0.0F;
    n.m[12] = 0.0F;
    n.m[11] = 0.0F;
    n.m[7] = 0.0F;
    n.m[3] = 0.0F;
    n.m[15] = e33;
    return n;
  }
  
  public Matrix4f invert() {
    float[] r = new float[16];
    r[0] = this.m[5] * this.m[10] * this.m[15] - this.m[5] * this.m[11] * this.m[14] - this.m[9] * this.m[6] * this.m[15] + this.m[9] * this.m[7] * this.m[14] + this.m[13] * this.m[6] * this.m[11] - this.m[13] * this.m[7] * this.m[10];
    r[4] = -this.m[4] * this.m[10] * this.m[15] + this.m[4] * this.m[11] * this.m[14] + this.m[8] * this.m[6] * this.m[15] - this.m[8] * this.m[7] * this.m[14] - this.m[12] * this.m[6] * this.m[11] + this.m[12] * this.m[7] * this.m[10];
    r[8] = this.m[4] * this.m[9] * this.m[15] - this.m[4] * this.m[11] * this.m[13] - this.m[8] * this.m[5] * this.m[15] + this.m[8] * this.m[7] * this.m[13] + this.m[12] * this.m[5] * this.m[11] - this.m[12] * this.m[7] * this.m[9];
    r[12] = -this.m[4] * this.m[9] * this.m[14] + this.m[4] * this.m[10] * this.m[13] + this.m[8] * this.m[5] * this.m[14] - this.m[8] * this.m[6] * this.m[13] - this.m[12] * this.m[5] * this.m[10] + this.m[12] * this.m[6] * this.m[9];
    r[1] = -this.m[1] * this.m[10] * this.m[15] + this.m[1] * this.m[11] * this.m[14] + this.m[9] * this.m[2] * this.m[15] - this.m[9] * this.m[3] * this.m[14] - this.m[13] * this.m[2] * this.m[11] + this.m[13] * this.m[3] * this.m[10];
    r[5] = this.m[0] * this.m[10] * this.m[15] - this.m[0] * this.m[11] * this.m[14] - this.m[8] * this.m[2] * this.m[15] + this.m[8] * this.m[3] * this.m[14] + this.m[12] * this.m[2] * this.m[11] - this.m[12] * this.m[3] * this.m[10];
    r[9] = -this.m[0] * this.m[9] * this.m[15] + this.m[0] * this.m[11] * this.m[13] + this.m[8] * this.m[1] * this.m[15] - this.m[8] * this.m[3] * this.m[13] - this.m[12] * this.m[1] * this.m[11] + this.m[12] * this.m[3] * this.m[9];
    r[13] = this.m[0] * this.m[9] * this.m[14] - this.m[0] * this.m[10] * this.m[13] - this.m[8] * this.m[1] * this.m[14] + this.m[8] * this.m[2] * this.m[13] + this.m[12] * this.m[1] * this.m[10] - this.m[12] * this.m[2] * this.m[9];
    r[2] = this.m[1] * this.m[6] * this.m[15] - this.m[1] * this.m[7] * this.m[14] - this.m[5] * this.m[2] * this.m[15] + this.m[5] * this.m[3] * this.m[14] + this.m[13] * this.m[2] * this.m[7] - this.m[13] * this.m[3] * this.m[6];
    r[6] = -this.m[0] * this.m[6] * this.m[15] + this.m[0] * this.m[7] * this.m[14] + this.m[4] * this.m[2] * this.m[15] - this.m[4] * this.m[3] * this.m[14] - this.m[12] * this.m[2] * this.m[7] + this.m[12] * this.m[3] * this.m[6];
    r[10] = this.m[0] * this.m[5] * this.m[15] - this.m[0] * this.m[7] * this.m[13] - this.m[4] * this.m[1] * this.m[15] + this.m[4] * this.m[3] * this.m[13] + this.m[12] * this.m[1] * this.m[7] - this.m[12] * this.m[3] * this.m[5];
    r[14] = -this.m[0] * this.m[5] * this.m[14] + this.m[0] * this.m[6] * this.m[13] + this.m[4] * this.m[1] * this.m[14] - this.m[4] * this.m[2] * this.m[13] - this.m[12] * this.m[1] * this.m[6] + this.m[12] * this.m[2] * this.m[5];
    r[3] = -this.m[1] * this.m[6] * this.m[11] + this.m[1] * this.m[7] * this.m[10] + this.m[5] * this.m[2] * this.m[11] - this.m[5] * this.m[3] * this.m[10] - this.m[9] * this.m[2] * this.m[7] + this.m[9] * this.m[3] * this.m[6];
    r[7] = this.m[0] * this.m[6] * this.m[11] - this.m[0] * this.m[7] * this.m[10] - this.m[4] * this.m[2] * this.m[11] + this.m[4] * this.m[3] * this.m[10] + this.m[8] * this.m[2] * this.m[7] - this.m[8] * this.m[3] * this.m[6];
    r[11] = -this.m[0] * this.m[5] * this.m[11] + this.m[0] * this.m[7] * this.m[9] + this.m[4] * this.m[1] * this.m[11] - this.m[4] * this.m[3] * this.m[9] - this.m[8] * this.m[1] * this.m[7] + this.m[8] * this.m[3] * this.m[5];
    r[15] = this.m[0] * this.m[5] * this.m[10] - this.m[0] * this.m[6] * this.m[9] - this.m[4] * this.m[1] * this.m[10] + this.m[4] * this.m[2] * this.m[9] + this.m[8] * this.m[1] * this.m[6] - this.m[8] * this.m[2] * this.m[5];
    float det = this.m[0] * r[0] + this.m[1] * r[4] + this.m[2] * r[8] + this.m[3] * r[12];
    if (det == 0.0F)
      return null; 
    det = 1.0F / det;
    for (int i = 0; i < 16; i++)
      r[i] = r[i] * det; 
    return new Matrix4f(r);
  }
  
  public void getgl(GL gl, int matrix) {
    gl.glGetFloatv(matrix, this.m, 0);
  }
  
  public void loadgl(GL2 gl) {
    gl.glLoadMatrixf(this.m, 0);
  }
  
  public static Matrix4f fromgl(GL gl, int matrix) {
    Matrix4f m = new Matrix4f();
    m.getgl(gl, matrix);
    return m;
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append('[');
    for (int y = 0; y < 4; y++) {
      if (y > 0)
        buf.append(", "); 
      buf.append('[');
      for (int x = 0; x < 4; x++) {
        if (x > 0)
          buf.append(", "); 
        buf.append(Float.toString(get(x, y)));
      } 
      buf.append(']');
    } 
    buf.append(']');
    return buf.toString();
  }
  
  public String toString2() {
    StringBuilder buf = new StringBuilder();
    for (int y = 0; y < 4; y++) {
      buf.append('[');
      for (int x = 0; x < 4; x++) {
        if (x > 0)
          buf.append(", "); 
        buf.append(Float.toString(get(x, y)));
      } 
      buf.append("]\n");
    } 
    return buf.toString();
  }
}
