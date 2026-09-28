package haven;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ItemFilter {
   private static final Pattern q = Pattern.compile("(?:(\\w+):)?([\\w\\*]+)(?:([<>=+~])(\\d+(?:\\.\\d+)?)?([<>=+~])?)?");
   private static final Pattern float_p = Pattern.compile("(\\d+(?:\\.\\d+)?)");

   public boolean matches(List<ItemInfo> info) {
      for (ItemInfo item : info) {
         if (item instanceof ItemInfo.Name) {
            if (this.match((ItemInfo.Name)item)) {
               return true;
            }
         } else if (item instanceof FoodInfo) {
            if (this.match((FoodInfo)item)) {
               return true;
            }
         } else if (item instanceof Inspiration) {
            if (this.match((Inspiration)item)) {
               return true;
            }
         } else if (item instanceof ItemInfo.Contents) {
            if (this.match((ItemInfo.Contents)item)) {
               return true;
            }
         } else if (item instanceof Alchemy) {
            if (this.match((Alchemy)item)) {
               return true;
            }
         } else if (item instanceof GobbleInfo && this.match((GobbleInfo)item)) {
            return true;
         }
      }

      return false;
   }

   protected boolean match(ItemInfo.Contents item) {
      return false;
   }

   protected boolean match(Alchemy item) {
      return false;
   }

   protected boolean match(Inspiration item) {
      return false;
   }

   protected boolean match(GobbleInfo item) {
      return false;
   }

   protected boolean match(FoodInfo item) {
      return false;
   }

   protected boolean match(ItemInfo.Name item) {
      return false;
   }

   public static ItemFilter create(String query) {
      ItemFilter.Compound result = new ItemFilter.Compound();
      Matcher m = q.matcher(query);

      while (m.find()) {
         String tag = m.group(1);
         String text = m.group(2).toLowerCase();
         String sign = m.group(3);
         String value = m.group(4);
         String opt = m.group(5);
         ItemFilter filter = null;
         if (tag == null) {
            if (sign != null && text.equals("q")) {
               filter = new ItemFilter.Alch(Alchemy.names[0], sign, value, opt);
            } else {
               filter = new ItemFilter.Text(text, false);
            }
         } else {
            tag = tag.toLowerCase();
            if (tag.equals("heal")) {
               filter = new ItemFilter.Heal(text, sign, value, opt);
            } else if (tag.equals("gob")) {
               filter = new ItemFilter.Gobble(text, sign, value, opt);
            } else if (tag.equals("txt")) {
               filter = new ItemFilter.Text(text, true);
            } else if (tag.equals("xp")) {
               filter = new ItemFilter.XP(text, sign, value, opt);
            } else if (tag.equals("has")) {
               filter = new ItemFilter.Has(text, sign, value, opt);
            } else if (tag.equals("alch")) {
               filter = new ItemFilter.Alch(text, sign, value, opt);
            }
         }

         if (filter != null) {
            result.add(filter);
         }
      }

      return result;
   }

   public static class Alch extends ItemFilter.Complex {
      public Alch(String text, String sign, String value, String opts) {
         super(text, sign, value, opts);
         this.value = (int)(100.0F * this.value);
      }

      @Override
      protected boolean match(Alchemy item) {
         for (int k = 0; k < item.a.length; k++) {
            boolean enough = this.test((int)(10000.0 * item.a[k]), this.value);
            if ((this.any || Alchemy.names[k].toLowerCase().equals(this.text)) && enough) {
               return true;
            }
         }

         return false;
      }
   }

   public static class Complex extends ItemFilter {
      protected final String text;
      protected final ItemFilter.Complex.Sign sign;
      protected final ItemFilter.Complex.Sign opts;
      protected float value;
      protected final boolean all;
      protected final boolean any;

      public Complex(String text, String sign, String value, String opts) {
         this.text = text.toLowerCase();
         this.sign = this.getSign(sign);
         this.opts = this.getSign(opts);
         float tmp = 0.0F;

         try {
            tmp = Float.parseFloat(value);
         } catch (Exception var7) {
         }

         this.value = tmp;
         this.all = text.equals("*") || text.equals("all");
         this.any = text.equals("any");
      }

      protected boolean test(double actual, double target) {
         switch (this.sign) {
            case GREATER:
               return actual > target;
            case LESS:
               return actual <= target;
            case EQUAL:
               return actual == target;
            case GREQUAL:
               return actual >= target;
            default:
               return actual > 0.0;
         }
      }

      protected ItemFilter.Complex.Sign getSign(String sign) {
         if (sign == null) {
            return this.getaDefaultSign();
         } else if (sign.equals(">")) {
            return ItemFilter.Complex.Sign.GREATER;
         } else if (sign.equals("<")) {
            return ItemFilter.Complex.Sign.LESS;
         } else if (sign.equals("=")) {
            return ItemFilter.Complex.Sign.EQUAL;
         } else if (sign.equals("+")) {
            return ItemFilter.Complex.Sign.GREQUAL;
         } else {
            return sign.equals("~") ? ItemFilter.Complex.Sign.WAVE : this.getaDefaultSign();
         }
      }

      protected ItemFilter.Complex.Sign getaDefaultSign() {
         return ItemFilter.Complex.Sign.DEFAULT;
      }

      public static enum Sign {
         GREATER,
         LESS,
         EQUAL,
         GREQUAL,
         WAVE,
         DEFAULT;
      }
   }

   public static class Compound extends ItemFilter {
      List<ItemFilter> filters = new LinkedList<>();

      @Override
      public boolean matches(List<ItemInfo> info) {
         if (this.filters.isEmpty()) {
            return false;
         } else {
            for (ItemFilter filter : this.filters) {
               if (!filter.matches(info)) {
                  return false;
               }
            }

            return true;
         }
      }

      public void add(ItemFilter filter) {
         this.filters.add(filter);
      }
   }

   public static class Gobble extends ItemFilter.Complex {
      public Gobble(String text, String sign, String value, String opts) {
         super(text, sign, value, opts);
         this.value = 1000.0F * this.value;
      }

      @Override
      protected boolean match(GobbleInfo item) {
         if (this.all) {
            for (int k = 0; k < Tempers.anm.length; k++) {
               if (!this.test(this.getBile(item, k), this.value)) {
                  return false;
               }
            }
         } else {
            for (int kx = 0; kx < Tempers.anm.length; kx++) {
               boolean enough = this.test(this.getBile(item, kx), this.value);
               if ((this.any || Tempers.anm[kx].equals(this.text)) && enough) {
                  return true;
               }

               if ((this.any || Tempers.rnm[kx].toLowerCase().contains(this.text)) && enough) {
                  return true;
               }
            }
         }

         return false;
      }

      private int getBile(GobbleInfo item, int k) {
         int result;
         switch (this.opts) {
            case GREATER:
            default:
               result = item.h[k];
               break;
            case LESS:
               result = item.l[k];
               break;
            case EQUAL:
               result = (item.h[k] + item.l[k]) / 2;
         }

         return 100 * (result / 100);
      }
   }

   public static class Has extends ItemFilter.Complex {
      public Has(String text, String sign, String value, String opts) {
         super(text, sign, value, opts);
      }

      @Override
      protected boolean match(ItemInfo.Contents item) {
         String name = this.name(item.sub).toLowerCase();
         float num = this.count(name);
         return name.contains(this.text) && this.test(num, this.value);
      }

      @Override
      protected ItemFilter.Complex.Sign getaDefaultSign() {
         return ItemFilter.Complex.Sign.GREQUAL;
      }

      private float count(String txt) {
         float n = 0.0F;
         if (txt != null) {
            try {
               Matcher matcher = ItemFilter.float_p.matcher(txt);
               if (matcher.find()) {
                  n = Float.parseFloat(matcher.group(1));
               }
            } catch (Exception var4) {
            }
         }

         return n;
      }

      private String name(List<ItemInfo> sub) {
         String txt = null;

         for (ItemInfo subInfo : sub) {
            if (subInfo instanceof ItemInfo.Name) {
               ItemInfo.Name name = (ItemInfo.Name)subInfo;
               txt = name.str.text;
            }
         }

         return txt;
      }
   }

   public static class Heal extends ItemFilter.Complex {
      public Heal(String text, String sign, String value, String opts) {
         super(text, sign, value, opts);
         this.value = 1000.0F * this.value;
      }

      @Override
      protected boolean match(FoodInfo item) {
         int[] tempers = item.tempers;
         if (this.all) {
            for (int k = 0; k < Tempers.anm.length; k++) {
               if (!this.test(tempers[k], this.value)) {
                  return false;
               }
            }
         } else {
            for (int kx = 0; kx < Tempers.anm.length; kx++) {
               boolean enough = this.test(tempers[kx], this.value);
               if ((this.any || Tempers.anm[kx].equals(this.text)) && enough) {
                  return true;
               }

               if ((this.any || Tempers.rnm[kx].toLowerCase().contains(this.text)) && enough) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public static class Text extends ItemFilter {
      private String text;
      private final boolean full;

      public Text(String text, boolean full) {
         this.full = full;
         this.text = text.toLowerCase();
      }

      public void update(String text) {
         this.text = text.toLowerCase();
      }

      @Override
      protected boolean match(ItemInfo.Name item) {
         return item.str.text.toLowerCase().contains(this.text);
      }

      @Override
      protected boolean match(FoodInfo item) {
         if (!this.full) {
            return false;
         } else {
            for (int k = 0; k < Tempers.anm.length; k++) {
               boolean notEmpty = item.tempers[k] > 0;
               if (Tempers.anm[k].equals(this.text) && notEmpty) {
                  return true;
               }

               if (Tempers.rnm[k].toLowerCase().contains(this.text) && notEmpty) {
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      protected boolean match(GobbleInfo item) {
         if (!this.full) {
            return false;
         } else {
            for (int k = 0; k < Tempers.anm.length; k++) {
               if (Tempers.anm[k].equals(this.text) && item.h[k] > 0) {
                  return true;
               }
            }

            for (int kx = 0; kx < Tempers.rnm.length; kx++) {
               if (Tempers.rnm[kx].toLowerCase().contains(this.text) && item.h[kx] > 0) {
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      protected boolean match(Inspiration item) {
         if (!this.full) {
            return false;
         } else {
            for (String attr : item.attrs) {
               if (attr.equals(this.text)) {
                  return true;
               }

               if (CharWnd.attrnm.get(attr).toLowerCase().contains(this.text)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public static class XP extends ItemFilter.Complex {
      public XP(String text, String sign, String value, String opts) {
         super(text, sign, value, opts);
      }

      @Override
      protected boolean match(Inspiration item) {
         for (int k = 0; k < item.attrs.length; k++) {
            boolean enough = this.test(item.exp[k], this.value);
            if ((this.any || item.attrs[k].equals(this.text)) && enough) {
               return true;
            }

            if ((this.any || CharWnd.attrnm.get(item.attrs[k]).toLowerCase().contains(this.text)) && enough) {
               return true;
            }
         }

         return false;
      }
   }
}
