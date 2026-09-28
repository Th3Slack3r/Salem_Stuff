package haven;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import javax.media.opengl.GL2;

public abstract class TexL extends TexGL {
  protected Mipmapper mipmap = null;
  
  private Defer.Future<Prepared> decode = null;
  
  protected abstract BufferedImage fill();
  
  public TexL(Coord sz) {
    super(sz);
    if (sz.x != Tex.nextp2(sz.x) || sz.y != Tex.nextp2(sz.y))
      throw new RuntimeException("TexL does not support non-power-of-two textures"); 
  }
  
  public void mipmap(Mipmapper mipmap) {
    this.mipmap = mipmap;
    dispose();
  }
  
  private class Prepared {
    BufferedImage img = TexL.this.fill();
    
    byte[][] data;
    
    int ifmt;
    
    private Prepared() {
      if (!Utils.imgsz(this.img).equals(TexL.this.dim))
        throw new RuntimeException("Generated TexL image from " + TexL.this + " does not match declared size"); 
      this.ifmt = TexI.detectfmt(this.img);
      LinkedList<byte[]> data = (LinkedList)new LinkedList<>();
      if ((this.ifmt == 6407 || this.ifmt == 32992) && 
        TexL.this.mipmap != null && !(TexL.this.mipmap instanceof Mipmapper.Mipmapper3))
        this.ifmt = -1; 
      if (this.ifmt == 6407 || this.ifmt == 32992) {
        byte[] pixels = ((DataBufferByte)this.img.getRaster().getDataBuffer()).getData();
        data.add(pixels);
        if (TexL.this.mipmap != null) {
          Coord msz = TexL.this.dim;
          Mipmapper.Mipmapper3 alg = (Mipmapper.Mipmapper3)TexL.this.mipmap;
          while (msz.x > 1 || msz.y > 1) {
            pixels = alg.gen3(msz, pixels, this.ifmt);
            data.add(pixels);
            msz = Mipmapper.nextsz(msz);
          } 
        } 
      } else {
        byte[] pixels;
        if (this.ifmt == 6408 || this.ifmt == 32993) {
          pixels = ((DataBufferByte)this.img.getRaster().getDataBuffer()).getData();
        } else {
          pixels = TexI.convert(this.img, TexL.this.dim);
          this.ifmt = 6408;
        } 
        data.add(pixels);
        if (TexL.this.mipmap != null) {
          Coord msz = TexL.this.dim;
          while (msz.x > 1 || msz.y > 1) {
            pixels = TexL.this.mipmap.gen4(msz, pixels, this.ifmt);
            data.add(pixels);
            msz = Mipmapper.nextsz(msz);
          } 
        } 
      } 
      this.data = data.<byte[]>toArray(new byte[0][]);
    }
  }
  
  private Defer.Future<Prepared> prepare() {
    return Defer.later(new Defer.Callable<Prepared>() {
          public TexL.Prepared call() {
            TexL.Prepared ret = new TexL.Prepared();
            return ret;
          }
        });
  }
  
  protected void fill(GOut g) {
    if (this.decode == null)
      this.decode = prepare(); 
    Prepared prep = this.decode.get();
    this.decode = null;
    GL2 gl = g.gl;
    gl.glPixelStorei(3317, 1);
    Coord cdim = this.tdim;
    for (int i = 0; i < prep.data.length; i++) {
      gl.glTexImage2D(3553, i, 6408, cdim.x, cdim.y, 0, prep.ifmt, 5121, ByteBuffer.wrap(prep.data[i]));
      cdim = Mipmapper.nextsz(cdim);
    } 
  }
}
