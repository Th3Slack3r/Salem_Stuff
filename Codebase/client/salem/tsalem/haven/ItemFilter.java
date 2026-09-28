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
        if (match((ItemInfo.Name)item))
          return true; 
        continue;
      } 
      if (item instanceof FoodInfo) {
        if (match((FoodInfo)item))
          return true; 
        continue;
      } 
      if (item instanceof Inspiration) {
        if (match((Inspiration)item))
          return true; 
        continue;
      } 
      if (item instanceof ItemInfo.Contents) {
        if (match((ItemInfo.Contents)item))
          return true; 
        continue;
      } 
      if (item instanceof Alchemy) {
        if (match((Alchemy)item))
          return true; 
        continue;
      } 
      if (item instanceof GobbleInfo && 
        match((GobbleInfo)item))
        return true; 
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
    Compound result = new Compound();
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
          filter = new Alch(Alchemy.names[0], sign, value, opt);
        } else {
          filter = new Text(text, false);
        } 
      } else {
        tag = tag.toLowerCase();
        if (tag.equals("heal")) {
          filter = new Heal(text, sign, value, opt);
        } else if (tag.equals("gob")) {
          filter = new Gobble(text, sign, value, opt);
        } else if (tag.equals("txt")) {
          filter = new Text(text, true);
        } else if (tag.equals("xp")) {
          filter = new XP(text, sign, value, opt);
        } else if (tag.equals("has")) {
          filter = new Has(text, sign, value, opt);
        } else if (tag.equals("alch")) {
          filter = new Alch(text, sign, value, opt);
        } 
      } 
      if (filter != null)
        result.add(filter); 
    } 
    return result;
  }
  
  public static class Compound extends ItemFilter {
    List<ItemFilter> filters = new LinkedList<>();
    
    public boolean matches(List<ItemInfo> info) {
      if (this.filters.isEmpty())
        return false; 
      for (ItemFilter filter : this.filters) {
        if (!filter.matches(info))
          return false; 
      } 
      return true;
    }
    
    public void add(ItemFilter filter) {
      this.filters.add(filter);
    }
  }
  
  public static class Complex extends ItemFilter {
    protected final String text;
    
    protected final Sign sign;
    
    protected final Sign opts;
    
    protected float value;
    
    protected final boolean all;
    
    protected final boolean any;
    
    public Complex(String text, String sign, String value, String opts) {
      this.text = text.toLowerCase();
      this.sign = getSign(sign);
      this.opts = getSign(opts);
      float tmp = 0.0F;
      try {
        tmp = Float.parseFloat(value);
      } catch (Exception exception) {}
      this.value = tmp;
      this.all = (text.equals("*") || text.equals("all"));
      this.any = text.equals("any");
    }
    
    protected boolean test(double actual, double target) {
      switch (this.sign) {
        case GREATER:
          return (actual > target);
        case LESS:
          return (actual <= target);
        case EQUAL:
          return (actual == target);
        case GREQUAL:
          return (actual >= target);
      } 
      return (actual > 0.0D);
    }
    
    protected Sign getSign(String sign) {
      if (sign == null)
        return getaDefaultSign(); 
      if (sign.equals(">"))
        return Sign.GREATER; 
      if (sign.equals("<"))
        return Sign.LESS; 
      if (sign.equals("="))
        return Sign.EQUAL; 
      if (sign.equals("+"))
        return Sign.GREQUAL; 
      if (sign.equals("~"))
        return Sign.WAVE; 
      return getaDefaultSign();
    }
    
    protected Sign getaDefaultSign() {
      return Sign.DEFAULT;
    }
    
    public enum Sign {
      GREATER, LESS, EQUAL, GREQUAL, WAVE, DEFAULT;
    }
  }
  
  public static class Heal extends Complex {
    public Heal(String text, String sign, String value, String opts) {
      super(text, sign, value, opts);
      this.value = 1000.0F * this.value;
    }
    
    protected boolean match(FoodInfo item) {
      int[] tempers = item.tempers;
      if (this.all) {
        for (int k = 0; k < Tempers.anm.length; k++) {
          if (!test(tempers[k], this.value))
            return false; 
        } 
      } else {
        for (int k = 0; k < Tempers.anm.length; k++) {
          boolean enough = test(tempers[k], this.value);
          if ((this.any || Tempers.anm[k].equals(this.text)) && enough)
            return true; 
          if ((this.any || Tempers.rnm[k].toLowerCase().contains(this.text)) && enough)
            return true; 
        } 
      } 
      return false;
    }
  }
  
  public static class Gobble extends Complex {
    public Gobble(String text, String sign, String value, String opts) {
      super(text, sign, value, opts);
      this.value = 1000.0F * this.value;
    }
    
    protected boolean match(GobbleInfo item) {
      if (this.all) {
        for (int k = 0; k < Tempers.anm.length; k++) {
          if (!test(getBile(item, k), this.value))
            return false; 
        } 
      } else {
        for (int k = 0; k < Tempers.anm.length; k++) {
          boolean enough = test(getBile(item, k), this.value);
          if ((this.any || Tempers.anm[k].equals(this.text)) && enough)
            return true; 
          if ((this.any || Tempers.rnm[k].toLowerCase().contains(this.text)) && enough)
            return true; 
        } 
      } 
      return false;
    }
    
    private int getBile(GobbleInfo item, int k) {
      switch (this.opts) {
        case EQUAL:
          result = (item.h[k] + item.l[k]) / 2;
          return 100 * result / 100;
        case LESS:
          result = item.l[k];
          return 100 * result / 100;
      } 
      int result = item.h[k];
      return 100 * result / 100;
    }
  }
  
  public static class Has extends Complex {
    public Has(String text, String sign, String value, String opts) {
      super(text, sign, value, opts);
    }
    
    protected boolean match(ItemInfo.Contents item) {
      String name = name(item.sub).toLowerCase();
      float num = count(name);
      return (name.contains(this.text) && test(num, this.value));
    }
    
    protected ItemFilter.Complex.Sign getaDefaultSign() {
      return ItemFilter.Complex.Sign.GREQUAL;
    }
    
    private float count(String txt) {
      float n = 0.0F;
      if (txt != null)
        try {
          Matcher matcher = ItemFilter.float_p.matcher(txt);
          if (matcher.find())
            n = Float.parseFloat(matcher.group(1)); 
        } catch (Exception exception) {} 
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
  
  public static class XP extends Complex {
    public XP(String text, String sign, String value, String opts) {
      super(text, sign, value, opts);
    }
    
    protected boolean match(Inspiration item) {
      for (int k = 0; k < item.attrs.length; k++) {
        boolean enough = test(item.exp[k], this.value);
        if ((this.any || item.attrs[k].equals(this.text)) && enough)
          return true; 
        if ((this.any || ((String)CharWnd.attrnm.get(item.attrs[k])).toLowerCase().contains(this.text)) && enough)
          return true; 
      } 
      return false;
    }
  }
  
  public static class Alch extends Complex {
    public Alch(String text, String sign, String value, String opts) {
      super(text, sign, value, opts);
      this.value = (int)(100.0F * this.value);
    }
    
    protected boolean match(Alchemy item) {
      for (int k = 0; k < item.a.length; k++) {
        boolean enough = test((int)(10000.0D * item.a[k]), this.value);
        if ((this.any || Alchemy.names[k].toLowerCase().equals(this.text)) && enough)
          return true; 
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
    
    protected boolean match(ItemInfo.Name item) {
      return item.str.text.toLowerCase().contains(this.text);
    }
    
    protected boolean match(FoodInfo item) {
      if (!this.full)
        return false; 
      for (int k = 0; k < Tempers.anm.length; k++) {
        boolean notEmpty = (item.tempers[k] > 0);
        if (Tempers.anm[k].equals(this.text) && notEmpty)
          return true; 
        if (Tempers.rnm[k].toLowerCase().contains(this.text) && notEmpty)
          return true; 
      } 
      return false;
    }
    
    protected boolean match(GobbleInfo item) {
      if (!this.full)
        return false; 
      int k;
      for (k = 0; k < Tempers.anm.length; k++) {
        if (Tempers.anm[k].equals(this.text) && item.h[k] > 0)
          return true; 
      } 
      for (k = 0; k < Tempers.rnm.length; k++) {
        if (Tempers.rnm[k].toLowerCase().contains(this.text) && item.h[k] > 0)
          return true; 
      } 
      return false;
    }
    
    protected boolean match(Inspiration item) {
      if (!this.full)
        return false; 
      for (String attr : item.attrs) {
        if (attr.equals(this.text))
          return true; 
        if (((String)CharWnd.attrnm.get(attr)).toLowerCase().contains(this.text))
          return true; 
      } 
      return false;
    }
  }
}
