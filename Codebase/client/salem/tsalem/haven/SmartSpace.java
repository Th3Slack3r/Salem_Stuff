package haven;

import java.awt.Color;
import java.util.Collection;

public class SmartSpace {
  static long last_action = 0L;
  
  static final int action_gap = 200;
  
  static String last_patterns;
  
  static final int max_dist = 99;
  
  static String[] patterns;
  
  static Collection<Gob> gobs;
  
  static boolean do_exclude_gobs = false;
  
  static String[] excluded_gobs = new String[] { "horse", "canoe", "giantcricket", "stallion", "mare" };
  
  static String[] excluded_poses = new String[] { "canoesit", "ride", "rodeo" };
  
  static String[] space_patterns_exclude = new String[] { "gfx/terobjs/arch/", "footprints", "gfx/terobjs/bust", "groundpillow", "terobjs/splatter" };
  
  static String rusty_space_patterns = "terobjs/items;terobjs/herbs;kritter;terobjs;borka";
  
  static boolean canInteract(Gob gob) {
    if (!do_exclude_gobs)
      return true; 
    for (String excluded : excluded_gobs) {
      try {
        if (Utils.getGobName(gob).toUpperCase().contains(excluded.toUpperCase()))
          return false; 
      } catch (NullPointerException nullPointerException) {}
    } 
    return true;
  }
  
  static Gob getGob() {
    return getGob(false, null);
  }
  
  static Gob getGob(Coord mouseCoord) {
    return getGob(false, mouseCoord);
  }
  
  static Gob getGob(boolean lookForGrapes, Coord mouseCoord) {
    Gob closestGob = null;
    double min_dist = 0.0D;
    for (String pattern : patterns) {
      label40: for (Gob gob : gobs) {
        if (gob == null)
          continue; 
        if (!canInteract(gob))
          continue; 
        if (Utils.dist(gob, mouseCoord) > 99.0D)
          continue; 
        if (lookForGrapes && 
          !Utils.getGobName(gob).toLowerCase().contains("/grape"))
          continue; 
        for (String string : space_patterns_exclude) {
          if (Utils.getGobName(gob).toLowerCase().contains(string.toLowerCase()))
            continue label40; 
        } 
        if (Utils.getGobName(gob).toUpperCase().contains(pattern.toUpperCase()) && (
          closestGob == null || min_dist > Utils.dist(gob, mouseCoord))) {
          closestGob = gob;
          min_dist = Utils.dist(gob, mouseCoord);
        } 
      } 
    } 
    if (closestGob != null) {
      Gob target = closestGob;
      closestGob = null;
      if (Utils.getGobName(target).toLowerCase().contains("/pinn")) {
        Gob grapeGob = getGob(true, mouseCoord);
        if (grapeGob != null && 
          Utils.dist(target, mouseCoord) >= Utils.dist(grapeGob, mouseCoord) - 3.0D)
          return grapeGob; 
      } 
      return target;
    } 
    return null;
  }
  
  static void setupExclude() {
    String[] current_poses = Utils.getPoseList();
    try {
      for (String pose : current_poses) {
        for (String excluded : excluded_poses) {
          if (pose.toUpperCase().contains(excluded.toUpperCase())) {
            do_exclude_gobs = true;
            return;
          } 
        } 
      } 
    } catch (Exception e) {
      Utils.msgOut("[SmartSpace]: could not verify if using a mount, is \"remove all animations\" active?", Color.RED);
    } 
    do_exclude_gobs = false;
  }
  
  static void work() {
    work(null);
  }
  
  static void work(Coord mouseCoord) {
    Gob player = null;
    try {
      player = Utils.getPlayer();
    } catch (Exception e) {
      return;
    } 
    if (Utils.isCarrying()) {
      Gob gob1 = Utils.getCarryObject();
      if (gob1 != null) {
        Utils.msgLog(Utils.getGobName(gob1));
        doTheClick(gob1);
      } 
      return;
    } 
    if (System.currentTimeMillis() - last_action < 200L && last_action != 0L)
      return; 
    if (last_patterns == null || !last_patterns.equals(rusty_space_patterns))
      try {
        patterns = rusty_space_patterns.split(";");
        last_patterns = rusty_space_patterns;
      } catch (Exception e) {
        return;
      }  
    gobs = Utils.getAllGobs();
    gobs.remove(player);
    setupExclude();
    Gob gob = getGob(mouseCoord);
    if (gob != null) {
      Utils.msgLog(Utils.getGobName(gob));
      doTheClick(gob);
      last_action = System.currentTimeMillis();
    } 
  }
  
  private static void doTheClick(Gob gob) {
    if (UI.instance.modshift || UI.instance.gui.hand.isEmpty()) {
      UI.instance.gui.map.autoRecipeOnGobClick(gob, 3);
      Utils.gobClick(gob, 3);
    } else {
      Utils.gobItemActClick(gob);
    } 
  }
}
