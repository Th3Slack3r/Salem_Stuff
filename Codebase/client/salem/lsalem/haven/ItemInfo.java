package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ItemInfo {
   public final ItemInfo.Owner owner;
   static final Pattern meter_patt = Pattern.compile("(\\d+)(?:[.,]\\d)?%");
   static final Pattern count_patt = Pattern.compile("(?:^|[\\s])([0-9]*\\.?[0-9]+\\s*%?)");
   static final Pattern carats_patt = Pattern.compile("Carats: ([0-9]*\\.?[0-9]+)");

   public ItemInfo(ItemInfo.Owner owner) {
      this.owner = owner;
   }

   public static BufferedImage catimgs(int margin, BufferedImage... imgs) {
      int w = 0;
      int h = -margin;

      for (BufferedImage img : imgs) {
         if (img != null) {
            if (img.getWidth() > w) {
               w = img.getWidth();
            }

            h += img.getHeight() + margin;
         }
      }

      BufferedImage ret = TexI.mkbuf(new Coord(w, h));
      Graphics g = ret.getGraphics();
      int y = 0;

      for (BufferedImage imgx : imgs) {
         if (imgx != null) {
            g.drawImage(imgx, 0, y, null);
            y += imgx.getHeight() + margin;
         }
      }

      g.dispose();
      return ret;
   }

   public static BufferedImage catimgsh(int margin, BufferedImage... imgs) {
      int w = -margin;
      int h = 0;

      for (BufferedImage img : imgs) {
         if (img.getHeight() > h) {
            h = img.getHeight();
         }

         w += img.getWidth() + margin;
      }

      BufferedImage ret = TexI.mkbuf(new Coord(w, h));
      Graphics g = ret.getGraphics();
      int x = 0;

      for (BufferedImage img : imgs) {
         g.drawImage(img, x, (h - img.getHeight()) / 2, null);
         x += img.getWidth() + margin;
      }

      g.dispose();
      return ret;
   }

   public static BufferedImage longtip(List<ItemInfo> info) {
      List<BufferedImage> buf = new ArrayList<>();

      for (ItemInfo ii : info) {
         if (ii instanceof ItemInfo.Tip) {
            ItemInfo.Tip tip = (ItemInfo.Tip)ii;

            try {
               buf.add(tip.longtip());
            } catch (IllegalArgumentException var6) {
            }
         }
      }

      return buf.size() < 1 ? null : catimgs(3, buf.toArray(new BufferedImage[buf.size()]));
   }

   public static <T> T find(Class<T> cl, List<ItemInfo> il) {
      for (ItemInfo inf : il) {
         if (cl.isInstance(inf)) {
            return cl.cast(inf);
         }
      }

      return null;
   }

   public static List<ItemInfo> buildinfo(ItemInfo.Owner owner, Object[] rawinfo) {
      List<ItemInfo> ret = new ArrayList<>();

      for (Object o : rawinfo) {
         if (o instanceof Object[]) {
            Object[] a = (Object[])o;
            Resource ttres = owner.glob().sess.getres((Integer)a[0]).get();
            ItemInfo.InfoFactory f = ttres.getcode(ItemInfo.InfoFactory.class, true);
            ItemInfo inf = f.build(owner, a);
            if (inf != null) {
               ret.add(inf);
            }
         } else {
            if (!(o instanceof String)) {
               throw new ClassCastException("Unexpected object type " + o.getClass() + " in item info array.");
            }

            ret.add(new ItemInfo.AdHoc(owner, (String)o));
         }
      }

      return ret;
   }

   private static String dump(Object arg) {
      if (arg instanceof Object[]) {
         StringBuilder buf = new StringBuilder();
         buf.append("[");
         boolean f = true;

         for (Object a : (Object[])arg) {
            if (!f) {
               buf.append(", ");
            }

            buf.append(dump(a));
            f = false;
         }

         buf.append("]");
         return buf.toString();
      } else {
         return arg.toString();
      }
   }

   public static List<Integer> getMeters(List<ItemInfo> infos) {
      List<Integer> res = new ArrayList<>();

      for (ItemInfo info : infos) {
         if (info instanceof ItemInfo.Contents) {
            ItemInfo.Contents cnt = (ItemInfo.Contents)info;
            res.addAll(getMeters(cnt.sub));
         } else if (info instanceof ItemInfo.AdHoc) {
            ItemInfo.AdHoc ah = (ItemInfo.AdHoc)info;

            try {
               Matcher m = meter_patt.matcher(ah.str.text);
               if (m.find()) {
                  res.add(Integer.parseInt(m.group(1)));
               }
            } catch (Exception var6) {
            }
         }
      }

      return res;
   }

   public static double getGobbleMeter(List<ItemInfo> infos) {
      GobbleInfo gobble = find(GobbleInfo.class, infos);
      return gobble != null && UI.instance != null && UI.instance.gui != null && UI.instance.gui.gobble != null ? UI.instance.gui.gobble.foodeff(gobble) : 0.0;
   }

   public static String getCount(List<ItemInfo> infos) {
      String res = null;

      for (ItemInfo info : infos) {
         if (info instanceof ItemInfo.Contents) {
            ItemInfo.Contents cnt = (ItemInfo.Contents)info;
            res = getCount(cnt.sub);
         } else if (info instanceof ItemInfo.AdHoc) {
            ItemInfo.AdHoc ah = (ItemInfo.AdHoc)info;

            try {
               Matcher m = count_patt.matcher(ah.str.text);
               if (m.find() && !ah.str.text.contains("Difficulty")) {
                  res = m.group(1);
               }
            } catch (Exception var7) {
            }
         } else if (info instanceof ItemInfo.Name) {
            ItemInfo.Name name = (ItemInfo.Name)info;

            try {
               Matcher m = count_patt.matcher(name.str.text);
               if (m.find()) {
                  res = m.group(1);
               }
            } catch (Exception var6) {
            }
         }

         if (res != null) {
            return res;
         }
      }

      return null;
   }

   public static String getContent(List<ItemInfo> infos) {
      String res = null;

      for (ItemInfo info : infos) {
         if (info instanceof ItemInfo.Contents) {
            ItemInfo.Contents cnt = (ItemInfo.Contents)info;

            for (ItemInfo subInfo : cnt.sub) {
               if (subInfo instanceof ItemInfo.Name) {
                  ItemInfo.Name name = (ItemInfo.Name)subInfo;
                  res = name.str.text;
               }

               if (res != null) {
                  return res;
               }
            }
         }
      }

      return null;
   }

   public static Float getCarats(List<ItemInfo> infos) {
      float carats = 0.0F;

      for (ItemInfo info : infos) {
         if (info instanceof ItemInfo.AdHoc) {
            ItemInfo.AdHoc ah = (ItemInfo.AdHoc)info;

            try {
               Matcher m = carats_patt.matcher(ah.str.text);
               if (m.find()) {
                  carats = Float.parseFloat(m.group(1));
               }
            } catch (Exception var6) {
            }
         }
      }

      return carats;
   }

   public static class AdHoc extends ItemInfo.Tip {
      public final Text str;

      public AdHoc(ItemInfo.Owner owner, String str) {
         super(owner);
         this.str = Text.render(str);
      }

      @Override
      public BufferedImage longtip() {
         return this.str.img;
      }
   }

   public static class Contents extends ItemInfo.Tip {
      public final List<ItemInfo> sub;
      private static final Text.Line ch = Text.render("Contents:");

      public Contents(ItemInfo.Owner owner, List<ItemInfo> sub) {
         super(owner);
         this.sub = sub;
      }

      @Override
      public BufferedImage longtip() {
         BufferedImage stip = longtip(this.sub);
         BufferedImage img = TexI.mkbuf(new Coord(stip.getWidth() + 10, stip.getHeight() + 15));
         Graphics g = img.getGraphics();
         g.drawImage(ch.img, 0, 0, null);
         g.drawImage(stip, 10, 15, null);
         g.dispose();
         return img;
      }
   }

   @Resource.PublishedCode(
      name = "tt"
   )
   public interface InfoFactory {
      ItemInfo build(ItemInfo.Owner var1, Object... var2);
   }

   public static class Name extends ItemInfo.Tip {
      public final Text str;

      public Name(ItemInfo.Owner owner, Text str) {
         super(owner);
         this.str = str;
      }

      public Name(ItemInfo.Owner owner, String str) {
         this(owner, Text.render(str));
      }

      @Override
      public BufferedImage longtip() {
         return this.str.img;
      }
   }

   public interface Owner {
      Glob glob();

      List<ItemInfo> info();
   }

   public interface ResOwner extends ItemInfo.Owner {
      Resource resource();
   }

   public abstract static class Tip extends ItemInfo {
      public abstract BufferedImage longtip();

      public Tip(ItemInfo.Owner owner) {
         super(owner);
      }
   }
}
