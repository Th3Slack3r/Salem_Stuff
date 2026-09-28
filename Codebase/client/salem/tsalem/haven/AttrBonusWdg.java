package haven;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AttrBonusWdg extends Widget {
  private static final Coord SZ = new Coord(175, 255);
  
  private static final String[] order = new String[] { 
      "Blunt power", "Concussive power", "Impact power", "Feral power", "Piercing power", "Common combat power", "Blunt defence", "Concussive defence", "Impact defence", "Feral defence", 
      "Piercing defence", "Common combat defence", "Feasting", "Mining", "Soil digging", "Weaving", "Woodworking", "Productivity", "Affluence", "Criminality", 
      "Spellcraft", "Thermal" };
  
  private BufferedImage bonusImg;
  
  private static Coord bonusc = new Coord(5, 20);
  
  private boolean needUpdate;
  
  private WItem[] witems;
  
  private final Scrollbar bar;
  
  public AttrBonusWdg(Equipory equip, Coord c) {
    super(c, SZ, equip);
    this.bar = new Scrollbar(new Coord(170, bonusc.y), SZ.y - bonusc.y, this, 0, 1);
    this.bar.visible = false;
    this.visible = Utils.getprefb("artifice_bonuses", true);
    new Label(new Coord(5, 0), this, "Clothing bonuses:", new Text.Foundry(new Font("SansSerif", 0, 12)));
  }
  
  public void draw(GOut g) {
    super.draw(g);
    if (this.needUpdate)
      doUpdate(); 
    if (this.bonusImg != null) {
      Coord c = bonusc;
      if (this.bar.visible)
        c = bonusc.sub(0, this.bar.val); 
      g.image(this.bonusImg, c);
    } 
  }
  
  public boolean mousewheel(Coord c, int amount) {
    this.bar.ch(amount * 15);
    return true;
  }
  
  public void update(WItem[] witems) {
    this.witems = witems;
    this.needUpdate = true;
  }
  
  public void toggle() {
    this.visible = !this.visible;
    Utils.setprefb("artifice_bonuses", this.visible);
  }
  
  private void doUpdate() {
    Map<String, Integer> map = new HashMap<>();
    this.needUpdate = false;
    int thermal = 0;
    for (WItem wi : this.witems) {
      if (wi != null && wi.item != null)
        try {
          for (ItemInfo ii : wi.item.info()) {
            if (ii.getClass().getName().equals("ISlots")) {
              try {
                Object[] slots = (Object[])Reflect.getFieldValue(ii, "s");
                for (Object slotted : slots) {
                  if (slotted != null) {
                    ArrayList<Object> infos = (ArrayList<Object>)Reflect.getFieldValue(slotted, "info");
                    for (Object info : infos) {
                      String[] attrs = (String[])Reflect.getFieldValue(info, "attrs");
                      int[] vals = (int[])Reflect.getFieldValue(info, "vals");
                      for (int i = 0; i < attrs.length; i++) {
                        int val = vals[i];
                        if (map.containsKey(attrs[i]))
                          val += ((Integer)map.get(attrs[i])).intValue(); 
                        map.put(attrs[i], Integer.valueOf(val));
                      } 
                    } 
                  } 
                } 
              } catch (Exception exception) {}
              continue;
            } 
            if (ii.getClass().getName().contains("AdHoc"))
              try {
                ItemInfo.AdHoc ah = (ItemInfo.AdHoc)ii;
                if (ah.str.text.startsWith("Thermal"))
                  thermal += Integer.parseInt(ah.str.text.split(" ")[1]); 
              } catch (Exception exception) {} 
          } 
        } catch (Loading e) {
          this.needUpdate = true;
        }  
    } 
    int n = map.size();
    Object[] bonuses = new Object[2 * n + 3];
    bonuses[0] = null;
    if (n > 0) {
      int k = 0;
      for (String name : order) {
        if (map.containsKey(name)) {
          bonuses[1 + 2 * k] = name;
          bonuses[2 + 2 * k] = map.remove(name);
          k++;
        } 
      } 
      for (Map.Entry<String, Integer> entry : map.entrySet()) {
        bonuses[1 + 2 * k] = entry.getKey();
        bonuses[2 + 2 * k] = entry.getValue();
        k++;
      } 
    } 
    bonuses[2 * n + 1] = "Thermal";
    bonuses[2 * n + 2] = Integer.valueOf(thermal);
    try {
      Resource res = Resource.load("ui/tt/dattr");
      ItemInfo.InfoFactory f = ((Resource.CodeEntry)res.<Resource.CodeEntry>layer(Resource.CodeEntry.class)).<ItemInfo.InfoFactory>get(ItemInfo.InfoFactory.class);
      LinkedList<ItemInfo> list = new LinkedList<>();
      list.add(f.build(null, bonuses));
      this.bonusImg = ItemInfo.longtip(list);
    } catch (Exception ignored) {
      this.bonusImg = null;
    } 
    int delta = 0;
    if (this.bonusImg != null)
      delta = this.bonusImg.getHeight() - SZ.y + bonusc.y; 
    this.bar.visible = (delta > 0);
    this.bar.max = delta;
    this.bar.ch(0);
  }
}
