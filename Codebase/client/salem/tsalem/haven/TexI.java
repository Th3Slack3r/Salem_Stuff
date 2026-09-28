package haven;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class TexI extends TexGL {
  public static ComponentColorModel glcm = new ComponentColorModel(ColorSpace.getInstance(1000), new int[] { 8, 8, 8, 8 }, true, false, 3, 0);
  
  public BufferedImage back;
  
  private int fmt = 6408;
  
  public Mipmapper mmalg = Mipmapper.avg;
  
  public TexI(BufferedImage img) {
    super(Utils.imgsz(img));
    this.back = img;
  }
  
  public TexI(Coord sz) {
    super(sz);
  }
  
  public static int detectfmt(BufferedImage img) {
    ColorModel cm = img.getColorModel();
    if (!(img.getSampleModel() instanceof PixelInterleavedSampleModel))
      return -1; 
    PixelInterleavedSampleModel sm = (PixelInterleavedSampleModel)img.getSampleModel();
    int[] cs = cm.getComponentSize();
    int[] off = sm.getBandOffsets();
    if (cm.getNumComponents() == 4 && off.length == 4) {
      if (cs[0] == 8 && cs[1] == 8 && cs[2] == 8 && cs[3] == 8 && cm.getTransferType() == 0 && cm.getTransparency() == 3) {
        if (off[0] == 0 && off[1] == 1 && off[2] == 2 && off[3] == 3)
          return 6408; 
        if (off[0] == 2 && off[1] == 1 && off[2] == 0 && off[3] == 3)
          return 32993; 
      } 
    } else if (cm.getNumComponents() == 3 && off.length == 3 && 
      cs[0] == 8 && cs[1] == 8 && cs[2] == 8 && cm.getTransferType() == 0 && cm.getTransparency() == 1) {
      if (off[0] == 0 && off[1] == 1 && off[2] == 2)
        return 6407; 
      if (off[0] == 2 && off[1] == 1 && off[2] == 0)
        return 32992; 
    } 
    return -1;
  }
  
  protected void fill(GOut g) {
    GL2 gL2 = g.gl;
    Coord sz = Utils.imgsz(this.back);
    int ifmt = detectfmt(this.back);
    if (ifmt == 6408 || ifmt == 32993) {
      byte[] pixels = ((DataBufferByte)this.back.getRaster().getDataBuffer()).getData();
      if (sz.equals(this.tdim)) {
        gL2.glTexImage2D(3553, 0, this.fmt, this.tdim.x, this.tdim.y, 0, ifmt, 5121, ByteBuffer.wrap(pixels));
        if (this.mipmap)
          genmipmap((GL)gL2, 1, this.tdim, pixels, ifmt); 
      } else {
        gL2.glTexImage2D(3553, 0, this.fmt, this.tdim.x, this.tdim.y, 0, ifmt, 5121, null);
        gL2.glTexSubImage2D(3553, 0, 0, 0, sz.x, sz.y, ifmt, 5121, ByteBuffer.wrap(pixels));
      } 
    } else if (ifmt == 6407 || ifmt == 32992) {
      gL2.glPixelStorei(3317, 1);
      byte[] pixels = ((DataBufferByte)this.back.getRaster().getDataBuffer()).getData();
      if (sz.equals(this.tdim)) {
        gL2.glTexImage2D(3553, 0, this.fmt, this.tdim.x, this.tdim.y, 0, ifmt, 5121, ByteBuffer.wrap(pixels));
        if (this.mipmap)
          genmipmap3((GL)gL2, 1, this.tdim, pixels, ifmt); 
      } else {
        gL2.glTexImage2D(3553, 0, this.fmt, this.tdim.x, this.tdim.y, 0, ifmt, 5121, null);
        gL2.glTexSubImage2D(3553, 0, 0, 0, sz.x, sz.y, ifmt, 5121, ByteBuffer.wrap(pixels));
      } 
    } else {
      byte[] pixels = convert(this.back, this.tdim);
      gL2.glTexImage2D(3553, 0, this.fmt, this.tdim.x, this.tdim.y, 0, 6408, 5121, ByteBuffer.wrap(pixels));
      if (this.mipmap)
        genmipmap((GL)gL2, 1, this.tdim, pixels, 6408); 
    } 
  }
  
  private void genmipmap(GL gl, int lev, Coord dim, byte[] data, int ifmt) {
    Coord ndim = Mipmapper.nextsz(dim);
    byte[] ndata = this.mmalg.gen4(dim, data, ifmt);
    gl.glTexImage2D(3553, lev, this.fmt, ndim.x, ndim.y, 0, ifmt, 5121, ByteBuffer.wrap(ndata));
    if (ndim.x > 1 || ndim.y > 1)
      genmipmap(gl, lev + 1, ndim, ndata, ifmt); 
  }
  
  private void genmipmap3(GL gl, int lev, Coord dim, byte[] data, int ifmt) {
    if (this.mmalg instanceof Mipmapper.Mipmapper3) {
      Coord ndim = Mipmapper.nextsz(dim);
      byte[] ndata = ((Mipmapper.Mipmapper3)this.mmalg).gen3(dim, data, ifmt);
      gl.glTexImage2D(3553, lev, this.fmt, ndim.x, ndim.y, 0, ifmt, 5121, ByteBuffer.wrap(ndata));
      if (ndim.x > 1 || ndim.y > 1)
        genmipmap3(gl, lev + 1, ndim, ndata, ifmt); 
    } else {
      genmipmap(gl, lev, dim, convert(this.back, dim), 6408);
    } 
  }
  
  public int getRGB(Coord c) {
    return this.back.getRGB(c.x, c.y);
  }
  
  public TexI mkmask() {
    TexI n = new TexI(this.back);
    n.fmt = 6406;
    return n;
  }
  
  public static BufferedImage mkbuf(Coord sz) {
    WritableRaster buf = Raster.createInterleavedRaster(0, sz.x, sz.y, 4, null);
    BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
    return tgt;
  }
  
  public static byte[] convert(BufferedImage img, Coord tsz, Coord ul, Coord sz) {
    WritableRaster buf = Raster.createInterleavedRaster(0, tsz.x, tsz.y, 4, null);
    BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
    Graphics g = tgt.createGraphics();
    g.drawImage(img, 0, 0, sz.x, sz.y, ul.x, ul.y, ul.x + sz.x, ul.y + sz.y, null);
    g.dispose();
    return ((DataBufferByte)buf.getDataBuffer()).getData();
  }
  
  public static byte[] convert(BufferedImage img, Coord tsz) {
    return convert(img, tsz, Coord.z, Utils.imgsz(img));
  }
}
