package haven;

import javax.media.opengl.GL2;

public class WireMesh extends FastMesh {
  public WireMesh(VertexBuf vert, short[] ind) {
    super(vert, ind);
  }
  
  public WireMesh(FastMesh from, VertexBuf vert) {
    super(from, vert);
  }
  
  public void draw(GOut g) {
    g.apply();
    GL2 gl = g.gl;
    VertexBuf.VertexArray vbuf = null;
    for (int i = 0; i < this.vert.bufs.length; i++) {
      if (this.vert.bufs[i] instanceof VertexBuf.VertexArray)
        vbuf = (VertexBuf.VertexArray)this.vert.bufs[i]; 
    } 
    gl.glLineWidth(5.0F);
    gl.glBegin(1);
    int o0 = 0;
    for (int j = 0; j < this.num * 3; j++) {
      int idx = this.indb.get(j);
      int o = idx * 3;
      vertex(gl, o, vbuf);
      if (j % 3 == 0)
        o0 = o; 
      if (j % 3 == 2)
        vertex(gl, o0, vbuf); 
    } 
    gl.glEnd();
  }
  
  private void vertex(GL2 gl, int o, VertexBuf.VertexArray vbuf) {
    float minv = FlatnessTool.minheight;
    float delta = FlatnessTool.maxheight - minv;
    float v = vbuf.data.get(o + 2);
    v = (v - minv) / delta;
    if (v >= 1.0F)
      v = 0.999F; 
    if (v <= 0.0F)
      v = 0.001F; 
    gl.glNormal3f(0.0F, 0.0F, 1.0F);
    gl.glTexCoord2f(1.0F - v, 0.0F);
    gl.glVertex3f(vbuf.data.get(o), vbuf.data.get(o + 1), vbuf.data.get(o + 2));
  }
}
