package haven;

import java.awt.image.BufferedImage;

public class FoodInfo extends ItemInfo.Tip {
   public final int[] tempers;

   public FoodInfo(ItemInfo.Owner owner, int[] tempers) {
      super(owner);
      this.tempers = tempers;
   }

   @Override
   public BufferedImage longtip() {
      StringBuilder buf = new StringBuilder();
      buf.append("Heals: ");

      for (int i = 0; i < 4; i++) {
         if (i > 0) {
            buf.append(", ");
         }

         buf.append(String.format("$col[%s]{%s}", Tempers.tcolors[i], Utils.fpformat(this.tempers[i], 3, 1)));
      }

      return RichText.render(buf.toString(), 0).img;
   }
}
