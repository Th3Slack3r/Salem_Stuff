package haven;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ItemInfo {
  public final Owner owner;
  
  public ItemInfo(Owner owner) {
    this.owner = owner;
  }
  
  public static interface Owner {
    Glob glob();
    
    List<ItemInfo> info();
  }
  
  public static interface ResOwner extends Owner {
    Resource resource();
  }
  
  @PublishedCode(name = "tt")
  public static interface InfoFactory {
    ItemInfo build(ItemInfo.Owner param1Owner, Object... param1VarArgs);
  }
  
  public static abstract class Tip extends ItemInfo {
    public abstract BufferedImage longtip();
    
    public Tip(ItemInfo.Owner owner) {
      super(owner);
    }
  }
  
  public static class AdHoc extends Tip {
    public final Text str;
    
    public AdHoc(ItemInfo.Owner owner, String str) {
      super(owner);
      this.str = Text.render(str);
    }
    
    public BufferedImage longtip() {
      return this.str.img;
    }
  }
  
  public static class Name extends Tip {
    public final Text str;
    
    public Name(ItemInfo.Owner owner, Text str) {
      super(owner);
      this.str = str;
    }
    
    public Name(ItemInfo.Owner owner, String str) {
      this(owner, Text.render(str));
    }
    
    public BufferedImage longtip() {
      return this.str.img;
    }
  }
  
  public static class Contents extends Tip {
    public final List<ItemInfo> sub;
    
    private static final Text.Line ch = Text.render("Contents:");
    
    public Contents(ItemInfo.Owner owner, List<ItemInfo> sub) {
      super(owner);
      this.sub = sub;
    }
    
    public BufferedImage longtip() {
      BufferedImage stip = ItemInfo.longtip(this.sub);
      BufferedImage img = TexI.mkbuf(new Coord(stip.getWidth() + 10, stip.getHeight() + 15));
      Graphics g = img.getGraphics();
      g.drawImage(ch.img, 0, 0, null);
      g.drawImage(stip, 10, 15, null);
      g.dispose();
      return img;
    }
  }
  
  public static BufferedImage catimgs(int margin, BufferedImage... imgs) {
    int w = 0, h = -margin;
    for (BufferedImage img : imgs) {
      if (img != null) {
        if (img.getWidth() > w)
          w = img.getWidth(); 
        h += img.getHeight() + margin;
      } 
    } 
    BufferedImage ret = TexI.mkbuf(new Coord(w, h));
    Graphics g = ret.getGraphics();
    int y = 0;
    for (BufferedImage img : imgs) {
      if (img != null) {
        g.drawImage(img, 0, y, null);
        y += img.getHeight() + margin;
      } 
    } 
    g.dispose();
    return ret;
  }
  
  public static BufferedImage catimgsh(int margin, BufferedImage... imgs) {
    int w = -margin, h = 0;
    for (BufferedImage img : imgs) {
      if (img.getHeight() > h)
        h = img.getHeight(); 
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
      if (ii instanceof Tip) {
        Tip tip = (Tip)ii;
        try {
          buf.add(tip.longtip());
        } catch (IllegalArgumentException illegalArgumentException) {}
      } 
    } 
    if (buf.size() < 1)
      return null; 
    return catimgs(3, buf.<BufferedImage>toArray(new BufferedImage[buf.size()]));
  }
  
  public static <T> T find(Class<T> cl, List<ItemInfo> il) {
    for (ItemInfo inf : il) {
      if (cl.isInstance(inf))
        return cl.cast(inf); 
    } 
    return null;
  }
  
  public static List<ItemInfo> buildinfo(Owner owner, Object[] rawinfo) {
    List<ItemInfo> ret = new ArrayList<>();
    for (Object o : rawinfo) {
      if (o instanceof Object[]) {
        Object[] a = (Object[])o;
        Resource ttres = (owner.glob()).sess.getres(((Integer)a[0]).intValue()).get();
        InfoFactory f = ttres.<InfoFactory>getcode(InfoFactory.class, true);
        ItemInfo inf = f.build(owner, a);
        if (inf != null)
          ret.add(inf); 
      } else if (o instanceof String) {
        ret.add(new AdHoc(owner, (String)o));
      } else {
        throw new ClassCastException("Unexpected object type " + o.getClass() + " in item info array.");
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
        if (!f)
          buf.append(", "); 
        buf.append(dump(a));
        f = false;
      } 
      buf.append("]");
      return buf.toString();
    } 
    return arg.toString();
  }
  
  static final Pattern meter_patt = Pattern.compile("(\\d+)(?:[.,]\\d)?%");
  
  public static List<Integer> getMeters(List<ItemInfo> infos) {
    List<Integer> res = new ArrayList<>();
    for (ItemInfo info : infos) {
      if (info instanceof Contents) {
        Contents cnt = (Contents)info;
        res.addAll(getMeters(cnt.sub));
        continue;
      } 
      if (info instanceof AdHoc) {
        AdHoc ah = (AdHoc)info;
        try {
          Matcher m = meter_patt.matcher(ah.str.text);
          if (m.find())
            res.add(Integer.valueOf(Integer.parseInt(m.group(1)))); 
        } catch (Exception exception) {}
      } 
    } 
    return res;
  }
  
  public static double getGobbleMeter(List<ItemInfo> infos) {
    GobbleInfo gobble = find(GobbleInfo.class, infos);
    if (gobble != null && UI.instance != null && UI.instance.gui != null && UI.instance.gui.gobble != null) {
      if (UI.instance.gui.gobble instanceof Gobble)
        return ((Gobble)UI.instance.gui.gobble).foodeff(gobble); 
      if (UI.instance.gui.gobble instanceof OldGobble)
        return ((OldGobble)UI.instance.gui.gobble).foodeff(gobble); 
      return 0.0D;
    } 
    return 0.0D;
  }
  
  static final Pattern count_patt = Pattern.compile("(?:^|[\\s])([0-9]*\\.?[0-9]+\\s*%?)");
  
  public static String getCount(List<ItemInfo> infos) {
    String res = null;
    for (ItemInfo info : infos) {
      if (info instanceof Contents) {
        Contents cnt = (Contents)info;
        res = getCount(cnt.sub);
      } else if (info instanceof AdHoc) {
        AdHoc ah = (AdHoc)info;
        try {
          Matcher m = count_patt.matcher(ah.str.text);
          if (m.find() && !ah.str.text.contains("Difficulty"))
            res = m.group(1); 
        } catch (Exception exception) {}
      } else if (info instanceof Name) {
        Name name = (Name)info;
        try {
          Matcher m = count_patt.matcher(name.str.text);
          if (m.find())
            res = m.group(1); 
        } catch (Exception exception) {}
      } 
      if (res != null)
        return res; 
    } 
    return null;
  }
  
  public static String getCountNoThermal(List<ItemInfo> infos) {
    String res = null;
    for (ItemInfo info : infos) {
      if (info instanceof AdHoc) {
        AdHoc ah = (AdHoc)info;
        if (ah.str != null && ah.str.text != null && ah.str.text.contains("Thermal:"))
          continue; 
        if (ah.str != null && ah.str.text != null && ah.str.text.contains("Made by"))
          continue; 
      } 
      if (info instanceof Contents) {
        Contents cnt = (Contents)info;
        res = getCount(cnt.sub);
      } else if (info instanceof AdHoc) {
        AdHoc ah = (AdHoc)info;
        try {
          Matcher m = count_patt.matcher(ah.str.text);
          if (m.find() && !ah.str.text.contains("Difficulty"))
            res = m.group(1); 
        } catch (Exception exception) {}
      } else if (info instanceof Name) {
        Name name = (Name)info;
        try {
          Matcher m = count_patt.matcher(name.str.text);
          if (m.find())
            res = m.group(1); 
        } catch (Exception exception) {}
      } 
      if (res != null)
        return res; 
    } 
    return null;
  }
  
  public static String getContent(List<ItemInfo> infos) {
    String res = null;
    for (ItemInfo info : infos) {
      if (info instanceof Contents) {
        Contents cnt = (Contents)info;
        for (ItemInfo subInfo : cnt.sub) {
          if (subInfo instanceof Name) {
            Name name = (Name)subInfo;
            res = name.str.text;
          } 
          if (res != null)
            return res; 
        } 
      } 
    } 
    return null;
  }
  
  static final Pattern carats_patt = Pattern.compile("Carats: ([0-9]*\\.?[0-9]+)");
  
  public static Float getCarats(List<ItemInfo> infos) {
    float carats = 0.0F;
    for (ItemInfo info : infos) {
      if (info instanceof AdHoc) {
        AdHoc ah = (AdHoc)info;
        try {
          Matcher m = carats_patt.matcher(ah.str.text);
          if (m.find())
            carats = Float.parseFloat(m.group(1)); 
        } catch (Exception exception) {}
      } 
    } 
    return Float.valueOf(carats);
  }
}
