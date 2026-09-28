package haven.res.ui.tt.slots;

import haven.CharWnd;
import haven.Coord;
import haven.ItemInfo;
import haven.Resource;
import haven.TexI;
import haven.Text;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ISlots extends ItemInfo.Tip {
  public static final Text ch = (Text)Text.render("Artificer's slots:");
  
  public static final Text broken = (Text)(new Text.Foundry(new Font("Serif", 2, 10), new Color(255, 0, 0))).render("Broken slot");
  
  public static final Text unused = (Text)(new Text.Foundry(new Font("Serif", 2, 10), new Color(224, 169, 0))).render("Unused slot");
  
  public static final Map<String, BufferedImage> picons;
  
  public final SItem[] s;
  
  public final int u;
  
  public final double pmin;
  
  public final double pmax;
  
  public final String[] attrs;
  
  public ISlots(ItemInfo.Owner var1, int var2, double var3, double var5, String[] var7, int var8) {
    super(var1);
    this.s = new SItem[var2];
    this.pmin = var3;
    this.pmax = var5;
    this.attrs = var7;
    this.u = var8;
  }
  
  public BufferedImage longtip() {
    BufferedImage var4, var1[] = new BufferedImage[this.s.length];
    int var2 = 0;
    for (int var3 = 0; var3 < this.s.length; var3++) {
      if (var3 < this.u) {
        if (this.s[var3] != null) {
          var1[var2++] = this.s[var3].tip();
        } else {
          var1[var2++] = broken.img;
        } 
      } else {
        var1[var2++] = unused.img;
      } 
    } 
    BufferedImage var8 = ItemInfo.catimgs(0, var1);
    if (this.attrs.length > 0) {
      BufferedImage[] var5 = new BufferedImage[this.attrs.length + 1];
      var5[0] = (Text.render(String.format("Difficulty: %d to %d", new Object[] { Integer.valueOf((int)Math.round(100.0D * (1.0D - this.pmin))), Integer.valueOf((int)Math.round(100.0D * (1.0D - this.pmax))) }))).img;
      int[] var6 = CharWnd.sortattrs(this.attrs);
      for (int var7 = 0; var7 < var6.length; var7++)
        var5[var7 + 1] = picons.get(this.attrs[var6[var7]]); 
      var4 = ItemInfo.catimgsh(2, var5);
    } else {
      var4 = (Text.render(String.format("Difficulty: %d", new Object[] { Integer.valueOf((int)Math.round(100.0D * (1.0D - this.pmin))) }))).img;
    } 
    BufferedImage var9 = TexI.mkbuf(new Coord(Math.max(Math.max((ch.sz()).x, var4.getWidth() + 10), var8.getWidth() + 10), var8.getHeight() + 30));
    Graphics var10 = var9.getGraphics();
    var10.drawImage(ch.img, 0, 0, (ImageObserver)null);
    var10.drawImage(var4, 10, 15, (ImageObserver)null);
    var10.drawImage(var8, 10, 30, (ImageObserver)null);
    var10.dispose();
    return var9;
  }
  
  static {
    HashMap<Object, Object> var0 = new HashMap<>();
    Iterator<String> var1 = CharWnd.attrnm.keySet().iterator();
    while (var1.hasNext()) {
      String var2 = var1.next();
      var0.put(var2, Resource.loadimg("gfx/hud/skills/" + var2));
    } 
    picons = (Map)var0;
  }
}
