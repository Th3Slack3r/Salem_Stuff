package haven;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class AttrBonusWdg extends Widget {
   private static final Coord SZ = new Coord(175, 255);
   private static final String[] order = new String[]{
      "Blunt power",
      "Concussive power",
      "Impact power",
      "Feral power",
      "Piercing power",
      "Common combat power",
      "Blunt defence",
      "Concussive defence",
      "Impact defence",
      "Feral defence",
      "Piercing defence",
      "Common combat defence",
      "Feasting",
      "Mining",
      "Soil digging",
      "Weaving",
      "Woodworking",
      "Productivity",
      "Affluence",
      "Criminality",
      "Spellcraft"
   };
   private BufferedImage bonusImg;
   private static Coord bonusc = new Coord(5, 20);
   private boolean needUpdate;
   private WItem[] witems;
   private Scrollbar bar = new Scrollbar(new Coord(170, bonusc.y), SZ.y - bonusc.y, this, 0, 1);

   public AttrBonusWdg(Equipory equip, Coord c) {
      super(c, SZ, equip);
      this.bar.visible = false;
      this.visible = Utils.getprefb("artifice_bonuses", true);
      new Label(new Coord(5, 0), this, "Artifice bonuses:", new Text.Foundry(new Font("SansSerif", 0, 12)));
   }

   @Override
   public void draw(GOut g) {
      super.draw(g);
      if (this.needUpdate) {
         this.doUpdate();
      }

      if (this.bonusImg != null) {
         Coord c = bonusc;
         if (this.bar.visible) {
            c = bonusc.sub(0, this.bar.val);
         }

         g.image(this.bonusImg, c);
      }
   }

   @Override
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

      for (WItem wi : this.witems) {
         if (wi != null && wi.item != null) {
            try {
               for (ItemInfo ii : wi.item.info()) {
                  if (ii.getClass().getName().equals("ISlots")) {
                     try {
                        Object[] slots = (Object[])Reflect.getFieldValue(ii, "s");

                        for (Object slotted : slots) {
                           if (slotted != null) {
                              for (Object info : (ArrayList)Reflect.getFieldValue(slotted, "info")) {
                                 String[] attrs = (String[])Reflect.getFieldValue(info, "attrs");
                                 int[] vals = (int[])Reflect.getFieldValue(info, "vals");

                                 for (int i = 0; i < attrs.length; i++) {
                                    int val = vals[i];
                                    if (map.containsKey(attrs[i])) {
                                       val += map.get(attrs[i]);
                                    }

                                    map.put(attrs[i], val);
                                 }
                              }
                           }
                        }
                     } catch (Exception var20) {
                     }
                  }
               }
            } catch (Loading var21) {
               this.needUpdate = true;
            }
         }
      }

      int n = map.size();
      if (n > 0) {
         Resource res = Resource.load("ui/tt/dattr");
         ItemInfo.InfoFactory f = res.layer(Resource.CodeEntry.class).get(ItemInfo.InfoFactory.class);
         Object[] bonuses = new Object[2 * n + 1];
         bonuses[0] = null;
         int k = 0;

         for (String name : order) {
            if (map.containsKey(name)) {
               bonuses[1 + 2 * k] = name;
               bonuses[2 + 2 * k] = map.remove(name);
               k++;
            }
         }

         for (Entry<String, Integer> entry : map.entrySet()) {
            bonuses[1 + 2 * k] = entry.getKey();
            bonuses[2 + 2 * k] = entry.getValue();
            k++;
         }

         LinkedList<ItemInfo> list = new LinkedList<>();
         list.add(f.build(null, bonuses));
         this.bonusImg = ItemInfo.longtip(list);
      } else {
         this.bonusImg = null;
      }

      int delta = 0;
      if (this.bonusImg != null) {
         delta = this.bonusImg.getHeight() - SZ.y + bonusc.y;
      }

      this.bar.visible = delta > 0;
      this.bar.max = delta;
      this.bar.ch(0);
   }
}
