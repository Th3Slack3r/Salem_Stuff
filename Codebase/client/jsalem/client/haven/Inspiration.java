package haven;

import java.awt.image.BufferedImage;

public class Inspiration extends ItemInfo.Tip {
   public final int xc;
   public final String[] attrs;
   public final int[] exp;
   public final int[] o;

   public Inspiration(ItemInfo.Owner owner, int xc, String[] attrs, int[] exp) {
      super(owner);
      this.xc = xc;
      this.o = CharWnd.sortattrs(attrs);
      this.attrs = attrs;
      this.exp = exp;
   }

   @Override
   public BufferedImage longtip() {
      StringBuilder buf = new StringBuilder();
      buf.append("When studied:\n");

      for (int i = 0; i < this.attrs.length; i++) {
         buf.append(String.format("   %s: %d\n", CharWnd.attrnm.get(this.attrs[this.o[i]]), this.exp[this.o[i]]));
      }

      buf.append(String.format("   $b{$col[192,192,64]{Inspiration required: %d}}\n", this.xc));
      return RichText.render(buf.toString(), 0).img;
   }
}
