package haven;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class OverviewTool extends Window {
   static final String title = "Abacus";
   private final Label text;
   private ArrayList<Label> ls = new ArrayList<>();
   private Map<String, Entry<Float, String>> uniques = new HashMap<>();
   private int sum;
   private static OverviewTool instance;
   private boolean invalidated = false;

   public OverviewTool(Coord c, Widget parent) {
      super(c, new Coord(300, 100), parent, "Abacus");
      this.text = new Label(Coord.z, this, "Creating overview. Please stand by...");
      this.toggle();
      this.pack();
   }

   public static OverviewTool instance(UI ui) {
      if (instance == null || instance.ui != ui) {
         instance = new OverviewTool(new Coord(100, 100), ui.gui);
      }

      return instance;
   }

   public static void close() {
      if (instance != null) {
         instance.ui.destroy(instance);
         instance = null;
      }
   }

   public void toggle() {
      if (this.visible = !this.visible) {
         this.update_text();
      }
   }

   private void update_uniques() {
      this.uniques = new HashMap<>();
      this.sum = this.ui.gui.maininv.wmap.size();

      for (GItem i : this.ui.gui.maininv.wmap.keySet()) {
         String name = null;
         String unit = null;
         float num = 1.0F;

         try {
            name = ItemInfo.getContent(i.info());
            if (name != null) {
               String[] parts = name.split(" ", 4);
               num = Float.parseFloat(parts[0]);
               unit = parts[1];
               name = parts[3];
            } else {
               name = i.name();
            }
         } catch (Loading var7) {
            continue;
         } catch (NumberFormatException var8) {
            name = i.name();
         }

         if (this.uniques.containsKey(name)) {
            this.uniques.put(name, new SimpleEntry<>(this.uniques.get(name).getKey() + num, unit));
         } else {
            this.uniques.put(name, new SimpleEntry<>(num, unit));
         }
      }
   }

   private void update_text() {
      if (this.visible) {
         this.update_uniques();
         String t = String.format("Carrying %.2f/%.2f kg (%d items)", this.ui.gui.weight / 1000.0, this.ui.sess.glob.cattr.get("carry").comp / 1000.0, this.sum);
         this.text.settext(t);
         int height = 25;
         Iterator<Label> itr = this.ls.iterator();

         while (itr.hasNext()) {
            Label l = itr.next();
            itr.remove();
            l.destroy();
         }

         this.ls = new ArrayList<>();
         this.ls.add(new Label(new Coord(0, height), this, "Overview of carried items:"));
         ArrayList<Entry<String, Entry<Float, String>>> object_counts = new ArrayList<>(this.uniques.entrySet());
         Collections.sort(object_counts, new Comparator<Entry<String, Entry<Float, String>>>() {
            public int compare(Entry<String, Entry<Float, String>> o1, Entry<String, Entry<Float, String>> o2) {
               String s1 = o1.getKey();
               String s2 = o2.getKey();
               if (s1 == null) {
                  s1 = "null";
               }

               if (s2 == null) {
                  s2 = "null";
               }

               return s1.compareTo(s2);
            }
         });

         for (Entry<String, Entry<Float, String>> e : object_counts) {
            height += 15;
            this.ls.add(new Label(new Coord(0, height), this, "   " + e.getKey() + ":"));
            String unit = e.getValue().getValue();
            if (unit != null) {
               this.ls.add(new Label(new Coord(150, height), this, " " + String.format("%.2f", e.getValue().getKey()) + " " + e.getValue().getValue()));
            } else {
               this.ls.add(new Label(new Coord(150, height), this, " " + e.getValue().getKey().intValue()));
            }
         }

         this.pack();
      }
   }

   public void force_update() {
      this.invalidated = true;
   }

   @Override
   public void tick(double dt) {
      if (this.invalidated) {
         this.invalidated = false;
         this.update_text();
      }
   }

   @Override
   public void destroy() {
      instance = null;
      super.destroy();
   }

   @Override
   public boolean type(char key, KeyEvent ev) {
      if (key != '\n' && key != 27) {
         return super.type(key, ev);
      } else {
         close();
         return true;
      }
   }

   @Override
   public void wdgmsg(Widget wdg, String msg, Object... args) {
      if (wdg == this.cbtn) {
         this.ui.destroy(this);
      } else {
         super.wdgmsg(wdg, msg, args);
      }
   }
}
