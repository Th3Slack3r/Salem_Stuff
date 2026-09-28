package haven;

import java.awt.event.KeyEvent;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class OverviewTool extends Window {
  static final String title = "Abacus";
  
  private final Label text;
  
  private ArrayList<Label> ls = new ArrayList<>();
  
  private Map<String, Map.Entry<Float, String>> uniques = new HashMap<>();
  
  private int sum;
  
  private static OverviewTool instance;
  
  private boolean invalidated;
  
  public OverviewTool(Coord c, Widget parent) {
    super(c, new Coord(300, 100), parent, "Abacus");
    this.invalidated = false;
    this.text = new Label(Coord.z, this, "Creating overview. Please stand by...");
    toggle();
    pack();
    try {
      this.ui.gui.fitwdg(instance);
    } catch (Exception exception) {}
  }
  
  public static OverviewTool instance(UI ui) {
    if (instance != null && instance.ui != ui) {
      instance.destroy();
      instance = null;
    } 
    if (instance == null)
      instance = new OverviewTool(new Coord(100, 100), ui.gui); 
    return instance;
  }
  
  public static void close() {
    if (instance != null) {
      instance.ui.destroy(instance);
      instance = null;
    } 
  }
  
  public void toggle() {
    if (this.visible = !this.visible)
      update_text(); 
  }
  
  private void update_uniques() {
    this.uniques = new HashMap<>();
    this.sum = this.ui.gui.maininv.wmap.size();
    for (GItem i : this.ui.gui.maininv.wmap.keySet()) {
      String name = null, unit = null;
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
      } catch (Loading l) {
        continue;
      } catch (NumberFormatException n) {
        name = i.name();
      } 
      if (this.uniques.containsKey(name)) {
        this.uniques.put(name, new AbstractMap.SimpleEntry<>(Float.valueOf(((Float)((Map.Entry)this.uniques.get(name)).getKey()).floatValue() + num), unit));
        continue;
      } 
      this.uniques.put(name, new AbstractMap.SimpleEntry<>(Float.valueOf(num), unit));
    } 
  }
  
  private void update_text() {
    if (!this.visible)
      return; 
    update_uniques();
    String t = String.format("Carrying %.2f/%.2f kg (%d items)", new Object[] { Double.valueOf(this.ui.gui.weight / 1000.0D), Double.valueOf(((Glob.CAttr)this.ui.sess.glob.cattr.get("carry")).comp / 1000.0D), Integer.valueOf(this.sum) });
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
    ArrayList<Map.Entry<String, Map.Entry<Float, String>>> object_counts = new ArrayList<>(this.uniques.entrySet());
    Collections.sort(object_counts, new Comparator<Map.Entry<String, Map.Entry<Float, String>>>() {
          public int compare(Map.Entry<String, Map.Entry<Float, String>> o1, Map.Entry<String, Map.Entry<Float, String>> o2) {
            String s1 = o1.getKey();
            String s2 = o2.getKey();
            if (s1 == null)
              s1 = "null"; 
            if (s2 == null)
              s2 = "null"; 
            return s1.compareTo(s2);
          }
        });
    for (Map.Entry<String, Map.Entry<Float, String>> e : object_counts) {
      height += 15;
      this.ls.add(new Label(new Coord(0, height), this, "   " + shortify(e.getKey()) + ":"));
      String unit = (String)((Map.Entry)e.getValue()).getValue();
      if (unit != null) {
        this.ls.add(new Label(new Coord(150, height), this, " " + String.format("%.2f", new Object[] { ((Map.Entry)e.getValue()).getKey() }) + " " + (String)((Map.Entry)e.getValue()).getValue()));
        continue;
      } 
      this.ls.add(new Label(new Coord(150, height), this, " " + ((Float)((Map.Entry)e.getValue()).getKey()).intValue()));
    } 
    pack();
  }
  
  public static String shortify(String text) {
    int maxlength = 22;
    if (text == null || text.length() <= 22)
      return text; 
    return text.substring(0, 23) + "...";
  }
  
  public void force_update() {
    this.invalidated = true;
  }
  
  private static boolean t1 = false;
  
  private static int ct = 0;
  
  private static int ct2 = 1;
  
  public void tick(double dt) {
    if (!this.visible)
      return; 
    if (this.invalidated) {
      if (!t1) {
        t1 = true;
        this.invalidated = false;
        update_text();
      } else if (ct >= ct2) {
        if (ct2 <= 10)
          ct2++; 
        ct = 0;
        t1 = false;
      } else {
        ct++;
      } 
    } else if (t1) {
      t1 = false;
      ct = 0;
      ct2 = 1;
    } 
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (key == '\n' || key == '\033') {
      close();
      return true;
    } 
    return super.type(key, ev);
  }
  
  public void wdgmsg(Widget wdg, String msg, Object... args) {
    if (wdg == this.cbtn) {
      this.ui.destroy(this);
    } else {
      super.wdgmsg(wdg, msg, args);
    } 
  }
}
