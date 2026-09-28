package haven;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WItemComparator implements Comparator<WItem> {
   static final Pattern count_patt = Pattern.compile("([0-9]*\\.?[0-9]+)");
   public static final Comparator<WItem> cmp_stats_asc = new WItemComparator();
   public static final Comparator<WItem> cmp_stats_desc = new WItemComparator() {
      @Override
      public int compare(WItem o1, WItem o2) {
         return super.compare(o2, o1);
      }
   };
   public static final Comparator<WItem> sort = new Comparator<WItem>() {
      public int compare(WItem o1, WItem o2) {
         String n1 = o1.item.name();
         String n2 = o2.item.name();

         try {
            if (n1 != null && n2 != null) {
               int k = n1.compareTo(n2);
               if (k == 0) {
                  n1 = o1.item.resname();
                  n2 = o2.item.resname();
                  if (n1 != null && n2 != null) {
                     k = n1.compareTo(n2);
                     return k == 0 ? WItemComparator.cmp_stats_desc.compare(o1, o2) : k;
                  } else {
                     throw new Loading();
                  }
               } else {
                  return k;
               }
            } else {
               throw new Loading();
            }
         } catch (Resource.Loading var6) {
            throw new Loading();
         }
      }
   };

   public int compare(WItem o1, WItem o2) {
      int carats = this.carats(o1, o2);
      if (carats == 0) {
         int alchemy = this.alchemy(o1, o2);
         return alchemy == 0 ? this.number(o1, o2) : alchemy;
      } else {
         return carats;
      }
   }

   protected int alchemy(WItem o1, WItem o2) {
      Alchemy a = o1.alch.get();
      double q1 = a == null ? 0.0 : a.purity();
      a = o2.alch.get();
      double q2 = a == null ? 0.0 : a.purity();
      if (q1 == q2) {
         return 0;
      } else {
         return q1 > q2 ? 1 : -1;
      }
   }

   protected int carats(WItem o1, WItem o2) {
      Float c1 = o1 != null && o1.carats != null ? o1.carats.get() : null;
      Float c2 = o2 != null && o2.carats != null ? o2.carats.get() : null;
      if (c1 == null) {
         c1 = 0.0F;
      }

      if (c2 == null) {
         c2 = 0.0F;
      }

      if (c1 > c2) {
         return 1;
      } else {
         return c2 > c1 ? -1 : 0;
      }
   }

   protected int number(WItem o1, WItem o2) {
      float n1 = this.getCount(o1);
      float n2 = this.getCount(o2);
      if (n1 > n2) {
         return 1;
      } else {
         return n2 > n1 ? -1 : 0;
      }
   }

   protected float getCount(WItem wItem) {
      float num = wItem.item.num;

      try {
         if (num < 0.0F) {
            GItem.NumberInfo ninf = ItemInfo.find(GItem.NumberInfo.class, wItem.item.info());
            if (ninf != null) {
               num = ninf.itemnum();
            } else {
               String snum = ItemInfo.getCount(wItem.item.info());
               if (snum != null) {
                  Matcher m = count_patt.matcher(snum);
                  if (m.find()) {
                     num = Float.parseFloat(m.group(1));
                  }
               }
            }
         }
      } catch (Exception var6) {
      }

      return num;
   }
}
