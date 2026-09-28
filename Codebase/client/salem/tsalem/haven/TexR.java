package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

@LayerName("tex")
public class TexR extends Resource.Layer implements Resource.IDLayer<Integer> {
  private transient byte[] img;
  
  private transient byte[] mask;
  
  private final transient TexL tex;
  
  private final Coord off;
  
  private final Coord sz;
  
  public final int id;
  
  public TexR(Resource res, byte[] rbuf) {
    super(res);
    Message buf = new Message(0, rbuf);
    this.id = buf.int16();
    this.off = new Coord(buf.uint16(), buf.uint16());
    this.sz = new Coord(buf.uint16(), buf.uint16());
    this.tex = new Real();
    int minfilter = -1, magfilter = -1;
    while (!buf.eom()) {
      int ma, magf, minf, t = buf.uint8();
      switch (t) {
        case 0:
          this.img = buf.bytes(buf.int32());
          continue;
        case 1:
          ma = buf.uint8();
          (new Mipmapper[5])[0] = Mipmapper.avg;
          (new Mipmapper[5])[1] = Mipmapper.avg;
          (new Mipmapper[5])[2] = Mipmapper.rnd;
          (new Mipmapper[5])[3] = Mipmapper.cnt;
          (new Mipmapper[5])[4] = Mipmapper.dav;
          this.tex.mipmap((new Mipmapper[5])[ma]);
          continue;
        case 2:
          magf = buf.uint8();
          (new int[2])[0] = 9728;
          (new int[2])[1] = 9729;
          magfilter = (new int[2])[magf];
          continue;
        case 3:
          minf = buf.uint8();
          (new int[6])[0] = 9728;
          (new int[6])[1] = 9729;
          (new int[6])[2] = 9984;
          (new int[6])[3] = 9986;
          (new int[6])[4] = 9985;
          (new int[6])[5] = 9987;
          minfilter = (new int[6])[minf];
          continue;
        case 4:
          this.mask = buf.bytes(buf.int32());
          continue;
      } 
      throw new Resource.LoadException("Unknown texture data part " + t + " in " + res.name, getres());
    } 
    if (magfilter == -1)
      magfilter = 9729; 
    if (minfilter == -1)
      minfilter = (this.tex.mipmap == null) ? 9729 : 9987; 
    this.tex.magfilter(magfilter);
    this.tex.minfilter(minfilter);
  }
  
  private class Real extends TexL {
    private Real() {
      super(TexR.this.sz);
    }
    
    private BufferedImage rd(byte[] data) {
      try {
        return ImageIO.read(new ByteArrayInputStream(data));
      } catch (IOException e) {
        throw new RuntimeException("Invalid image data in " + (TexR.this.getres()).name, e);
      } 
    }
    
    protected BufferedImage fill() {
      if (TexR.this.mask == null)
        return rd(TexR.this.img); 
      BufferedImage col = rd(TexR.this.img);
      BufferedImage mask = rd(TexR.this.mask);
      Coord sz = Utils.imgsz(mask);
      BufferedImage ret = TexI.mkbuf(sz);
      Graphics g = ret.createGraphics();
      g.drawImage(col, 0, 0, sz.x, sz.y, null);
      Raster mr = mask.getRaster();
      if (mr.getNumBands() != 1)
        throw new RuntimeException("Invalid separated alpha data in " + (TexR.this.getres()).name); 
      WritableRaster rr = ret.getRaster();
      for (int y = 0; y < sz.y; y++) {
        for (int x = 0; x < sz.x; x++)
          rr.setSample(x, y, 3, mr.getSample(x, y, 0)); 
      } 
      g.dispose();
      return ret;
    }
    
    protected void fill(GOut g) {
      try {
        super.fill(g);
      } catch (Loading l) {
        throw RenderList.RLoad.wrap(l);
      } 
    }
    
    public String toString() {
      return "TexR(" + (TexR.this.getres()).name + ", " + TexR.this.id + ")";
    }
  }
  
  public TexGL tex() {
    return this.tex;
  }
  
  public Integer layerid() {
    return Integer.valueOf(this.id);
  }
  
  public void init() {}
}
