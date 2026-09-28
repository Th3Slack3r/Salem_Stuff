package haven.res.ui.tt.slots;

import haven.Coord;
import haven.Glob;
import haven.Indir;
import haven.ItemInfo;
import haven.PUtils;
import haven.Resource;
import haven.TexI;
import haven.Text;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class SItem implements ItemInfo.ResOwner {
  public final Indir<Resource> res;
  
  public final List<ItemInfo> info;
  
  final ISlots this$0;
  
  public SItem(ISlots this$0, Indir<Resource> res, Object[] array) {
    this.this$0 = this$0;
    this.res = res;
    this.info = ItemInfo.buildinfo((ItemInfo.Owner)this, array);
  }
  
  public Glob glob() {
    return this.this$0.owner.glob();
  }
  
  public List<ItemInfo> info() {
    return this.info;
  }
  
  public Resource resource() {
    return (Resource)this.res.get();
  }
  
  public BufferedImage tip() {
    BufferedImage convolvedown = PUtils.convolvedown(((Resource.Image)((Resource)this.res.get()).layer(Resource.imgc)).img, new Coord(16, 16), (PUtils.Convolution)new PUtils.Hanning(1.0D));
    BufferedImage img = (Text.render(((Resource.Tooltip)((Resource)this.res.get()).layer(Resource.tooltip)).t)).img;
    BufferedImage longtip = ItemInfo.longtip(this.info);
    Coord coord = new Coord(19 + img.getWidth(), 16);
    if (longtip != null)
      coord = new Coord(Math.max(coord.x, 10 + longtip.getWidth()), coord.y + longtip.getHeight()); 
    BufferedImage mkbuf = TexI.mkbuf(coord);
    Graphics graphics = mkbuf.getGraphics();
    graphics.drawImage(convolvedown, 0, 0, null);
    graphics.drawImage(img, 19, (16 - img.getHeight()) / 2, null);
    if (longtip != null)
      graphics.drawImage(longtip, 10, 16, null); 
    graphics.dispose();
    return mkbuf;
  }
}
