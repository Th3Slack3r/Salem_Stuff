package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class GobbleEventInfo extends ItemInfo.Tip {
  public static final Color undebuff = new Color(192, 255, 192);
  
  public static final Color debuff = new Color(255, 192, 192);
  
  public int value;
  
  public Indir<Resource> res;
  
  public GobbleEventInfo(ItemInfo.Owner owner, int value, Indir<Resource> res) {
    super(owner);
    this.value = value;
    this.res = res;
  }
  
  public BufferedImage longtip() {
    int i = 16;
    BufferedImage head = getHead();
    BufferedImage icon = PUtils.convolvedown(((Resource.Image)((Resource)this.res.get()).layer((Class)Resource.imgc)).img, new Coord(16, 16), GobIcon.filter);
    String name = ((Resource.Tooltip)((Resource)this.res.get()).layer((Class)Resource.tooltip)).t;
    BufferedImage tail = (Text.render(name)).img;
    return ItemInfo.catimgsh(3, new BufferedImage[] { head, icon, tail });
  }
  
  private BufferedImage getHead() {
    String format = (this.value < 0) ? "%d%%" : "+%d%%";
    Color color = (this.value < 0) ? debuff : undebuff;
    return (Text.render(String.format(format, new Object[] { Integer.valueOf(this.value) }), color)).img;
  }
}
