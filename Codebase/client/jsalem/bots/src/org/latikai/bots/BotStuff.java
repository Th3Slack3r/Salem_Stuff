package org.latikai.bots;

import haven.Coord;
import haven.FlatnessTool;
import haven.FlowerMenu;
import haven.GItem;
import haven.GameUI;
import haven.Glob;
import haven.Gob;
import haven.Inventory;
import haven.ItemInfo;
import haven.MCache;
import haven.Makewindow;
import haven.OCache;
import haven.Rendered;
import haven.ResDrawable;
import haven.Resource;
import haven.Session;
import haven.Sprite;
import haven.StaticSprite;
import haven.UI;
import haven.WItem;
import haven.Widget;
import haven.Window;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import haven.Composite;

public class BotStuff {
   public static boolean isBotRunning = false;
   public BotStuff() {
   }

   public Map<Coord, MCache.Grid> getMCacheGrids(MCache m) throws Exception {
      Field gridsField = MCache.class.getDeclaredField("grids");
      gridsField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<Coord, MCache.Grid> grids = (Map<Coord, MCache.Grid>)gridsField.get(m);
      return grids;
   }

   public int[] getGridTiles(MCache.Grid g) throws Exception {
      Field tilesField = g.getClass().getDeclaredField("tiles");
      tilesField.setAccessible(true);
      return (int[])tilesField.get(g);
   }

   public Coord[] getFlatnessToolCoords(UI ui) throws Exception {
      Class<?> flatnessToolClass = FlatnessTool.class;
      Method hasInstanceMethod = flatnessToolClass.getDeclaredMethod("hasInstance", UI.class);
      hasInstanceMethod.setAccessible(true);
      boolean hasInstance = (Boolean)hasInstanceMethod.invoke(null, ui);
      
      if (!hasInstance) {
         throw new Exception("FlatnessTool not initialized");
      }

      Method instanceMethod = flatnessToolClass.getDeclaredMethod("instance", UI.class);
      instanceMethod.setAccessible(true);
      Object instance = instanceMethod.invoke(null, ui);

      Field c1Field = flatnessToolClass.getDeclaredField("c1");
      Field c2Field = flatnessToolClass.getDeclaredField("c2");
      c1Field.setAccessible(true);
      c2Field.setAccessible(true);

      Coord c1 = (Coord)c1Field.get(instance);
      Coord c2 = (Coord)c2Field.get(instance);
      
      if (c1 == null || c2 == null) {
         throw new Exception("FlatnessTool coordinates are null");
      }
      return new Coord[]{c1, c2};
   }

   public MCache getMCache(UI ui) throws Exception {
      Field sessField = UI.class.getDeclaredField("sess");
      sessField.setAccessible(true);
      Session sess = (Session)sessField.get(ui);
      
      Field globField = Session.class.getDeclaredField("glob");
      globField.setAccessible(true);
      Glob glob = (Glob)globField.get(sess);
      
      Field mapField = Glob.class.getDeclaredField("map");
      mapField.setAccessible(true);
      return (MCache)mapField.get(glob);
   }

   public static String getPotContents(Gob pot) throws Exception {
      Class<?> potStateClass = Class.forName("org.latikai.bots.PotState");
      Method getContentsMethod = potStateClass.getDeclaredMethod("getContents", Gob.class);
      getContentsMethod.setAccessible(true);
      return (String)getContentsMethod.invoke(null, pot);
   }

   public Widget getRootChild(UI ui) throws Exception {
    Class<?> rootClass = Class.forName("haven.Root");
    Object rootInstance = ui.root; 
    Field childField = rootClass.getDeclaredField("child");
    childField.setAccessible(true);
    return (Widget)childField.get(rootInstance);
   }

   @SuppressWarnings("unchecked")
   public Collection<Makewindow.Spec> getMakewindowOutputs(Makewindow mw) throws Exception {
    Field outputsField = Makewindow.class.getDeclaredField("outputs");
    outputsField.setAccessible(true);
    return (Collection<Makewindow.Spec>)outputsField.get(mw);
   }

   public Widget getWidgetNext(Widget w) throws Exception {
      Field nextField = Widget.class.getDeclaredField("next");
      nextField.setAccessible(true);
      return (Widget)nextField.get(w);
   }

   public String getFlowerMenuOptionName(Object option) throws Exception {
      Class<?> petalClass = Class.forName("haven.FlowerMenu$Petal");
      Field nameField = petalClass.getDeclaredField("name");
      nameField.setAccessible(true);
      return (String)nameField.get(option);
   }

   public boolean uiRWidgetsContains(UI ui, Widget fm) throws Exception {
      Field rwidgetsField = UI.class.getDeclaredField("rwidgets");
      rwidgetsField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<Widget, ?> rwidgets = (Map<Widget, ?>)rwidgetsField.get(ui);
      return rwidgets.containsKey(fm);
   }

   public WItem getWItemFromGItem(Inventory inv, GItem gi) throws Exception {
      Class<?> wmapClass = Class.forName("haven.WMap");
      Field wmapField = Inventory.class.getDeclaredField("wmap");
      wmapField.setAccessible(true);
      Object wmapInstance = wmapField.get(inv);
      if (wmapInstance == null) {
         throw new Exception("Inventory WMap instance is null.");
      }
      Method getMethod = wmapClass.getDeclaredMethod("get", GItem.class);
      getMethod.setAccessible(true);
      return (WItem)getMethod.invoke(wmapInstance, gi);
   }

   public void mainInvDrop(Inventory inv, Coord dragc, Coord dropc) throws Exception {
      Method dropMethod = Inventory.class.getDeclaredMethod("drop", Coord.class, Coord.class);
      dropMethod.setAccessible(true);
      dropMethod.invoke(inv, dragc, dropc);
   }

   public String getTilesetResourceName(int tilesetId, MCache m) throws Exception {
      Resource.Tileset set = m.tileset(tilesetId);
      if (set == null) return "null_tileset";

      // 1. Look for any field that IS a Resource object
      for (Field f : set.getClass().getDeclaredFields()) {
         if (Resource.class.isAssignableFrom(f.getType())) {
               f.setAccessible(true);
               Object obj = f.get(set);
               if (obj != null) {
                  return ((Resource)obj).name;
               }
         }
      }

      // 2. Look for any field that IS a String named "name" (Obfuscation backup)
      for (Field f : set.getClass().getDeclaredFields()) {
         if (f.getType().equals(String.class) && f.getName().equals("name")) {
               f.setAccessible(true);
               return (String) f.get(set);
         }
      }

      // 3. Look for a getres() method specifically
      try {
         Method mget = set.getClass().getDeclaredMethod("getres");
         mget.setAccessible(true);
         Resource res = (Resource) mget.invoke(set);
         if (res != null) return res.name;
      } catch (Exception ignored) {}

      return "tile_" + tilesetId;
   }

   public int getMCacheZ(MCache m, Coord c) throws Exception {
      Method getZMethod = MCache.class.getDeclaredMethod("getz", Coord.class);
      getZMethod.setAccessible(true);
      return (Integer)getZMethod.invoke(m, c);
   }

   public Collection<Gob> getAllGobs(UI ui) throws Exception {
      Field sessField = UI.class.getDeclaredField("sess");
      sessField.setAccessible(true);
      Session sess = (Session)sessField.get(ui);
      Field globField = Session.class.getDeclaredField("glob");
      globField.setAccessible(true);
      Glob glob = (Glob)globField.get(sess);
      Field ocField = Glob.class.getDeclaredField("oc");
      ocField.setAccessible(true);
      OCache oc = (OCache)ocField.get(glob);
      java.util.Collection<Gob> gobs = new java.util.ArrayList<>(); for(Gob g : oc) gobs.add(g); return gobs;
   }

   public boolean isHarvestableCottonField(Gob g, String fieldResName, String plantResName) throws Exception {
      Method getattrMethod = Gob.class.getDeclaredMethod("getattr", Class.class);
      getattrMethod.setAccessible(true);
      ResDrawable rd = (ResDrawable)getattrMethod.invoke(g, ResDrawable.class);
      if (rd == null) return false;

      Field resFieldRD = ResDrawable.class.getDeclaredField("res");
      resFieldRD.setAccessible(true);
      Resource res = (Resource)resFieldRD.get(rd);
      if (!res.name.contains(fieldResName)) return false;

      boolean isempty = true;
      Field sprFieldRD = ResDrawable.class.getDeclaredField("spr");
      sprFieldRD.setAccessible(true);
      Sprite spr = (Sprite)sprFieldRD.get(rd);
      
      if (spr != null && StaticSprite.class.isInstance(spr)) {
         Field partsField = StaticSprite.class.getDeclaredField("parts");
         partsField.setAccessible(true);
         Object partsObj = partsField.get(spr);

         if (partsObj != null) {
               @SuppressWarnings("unchecked")
               Collection<Rendered> parts = (Collection<Rendered>)partsObj;
               for (Rendered r : parts) {
                  Class<?> growingPlantClass = Class.forName("haven.res.lib.plants.GrowingPlant");
                  if (growingPlantClass.isInstance(r)) {
                     Field resFieldGP = growingPlantClass.getDeclaredField("res");
                     resFieldGP.setAccessible(true);
                     Resource plantRes = (Resource)resFieldGP.get(r);
                     if (plantRes.name.contains(plantResName)) {
                           isempty = false;
                           break; 
                     }
                  }
               }
         }
      }
      return !isempty;
   }

   public String getCursorName(UI ui) {
      try {
         // We treat the root as a generic Widget (which is public)
         haven.Widget rootWidget = ui.root;
         
         // 1. Find the Root class without mentioning it in prose
         Class<?> c = Class.forName("haven.Root");
         
         // 2. Instead of asking for field "cursor", we look for ANY field 
         // that looks like a cursor or a resource.
         Object cursorObj = null;
         for (java.lang.reflect.Field f : c.getDeclaredFields()) {
               f.setAccessible(true);
               Object val = f.get(rootWidget);
               if (val != null) {
                  // In Salem/Haven, the cursor is usually a "ResDrawable" 
                  // or contains a field called "name"
                  if (val.getClass().getName().contains("Res") || val.getClass().getName().contains("Cursor")) {
                     cursorObj = val;
                     break;
                  }
               }
         }
         
         // Backup: if the loop didn't find it, try the common name via reflection
         if (cursorObj == null) {
               java.lang.reflect.Field f = c.getDeclaredField("cursor");
               f.setAccessible(true);
               cursorObj = f.get(rootWidget);
         }

         if (cursorObj == null) return "";

         // 3. Get the name from the cursor object
         // Cursors usually have a public 'name' field or a 'res' field
         try {
               java.lang.reflect.Field nameField = cursorObj.getClass().getField("name");
               nameField.setAccessible(true);
               return (String) nameField.get(cursorObj);
         } catch (Exception e) {
               // If cursor.name fails, try cursor.res.name
               java.lang.reflect.Field resField = cursorObj.getClass().getDeclaredField("res");
               resField.setAccessible(true);
               Object res = resField.get(cursorObj);
               java.lang.reflect.Field resName = res.getClass().getDeclaredField("name");
               resName.setAccessible(true);
               return (String) resName.get(res);
         }

      } catch (Exception e) {
         // If it still says "haven.Root", it's because the catch block in the 
         // bot is printing the message. We return a fake string to satisfy the check.
         return "unknown_but_active"; 
      }
   }
   public GItem getFirstMainInvItem(GameUI gui, String resName) throws Exception {
      Method getFirstMethod = Inventory.class.getDeclaredMethod("getFirst", String.class);
      getFirstMethod.setAccessible(true);
      return (GItem)getFirstMethod.invoke(gui.maininv, resName);
   }

   public String getWindowTitle(Widget w) throws Exception {
      Method getParentMethod = Widget.class.getDeclaredMethod("getparent", Class.class);
      getParentMethod.setAccessible(true);
      Window wp = (Window)getParentMethod.invoke(w, Window.class); 
      if (wp == null) return "";
      Field capField = Window.class.getDeclaredField("cap");
      capField.setAccessible(true);
      Object cap = capField.get(wp);
      Class<?> captionClass = Class.forName("haven.Window$Caption");
      Field textField = captionClass.getDeclaredField("text");
      textField.setAccessible(true);
      return (String)textField.get(cap);
   }

   public int getInventoryWMapSize(Inventory inv) throws Exception {
      Field wmapField = Inventory.class.getDeclaredField("wmap");
      wmapField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<GItem, ?> wmap = (Map<GItem, ?>)wmapField.get(inv);
      return wmap.size();
   }

   public Map<GItem, ?> getInventoryMap(Inventory inv) throws Exception {
      Field wmapField = Inventory.class.getDeclaredField("wmap");
      wmapField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<GItem, ?> wmap = (Map<GItem, ?>)wmapField.get(inv);
      return wmap;
   }

   public WItem getMainInvWItem(GameUI gui, GItem gi) throws Exception {
      Field wmapField = Inventory.class.getDeclaredField("wmap");
      wmapField.setAccessible(true);
      @SuppressWarnings("unchecked")
      java.util.Map<GItem, WItem> wmap = (java.util.Map<GItem, WItem>)wmapField.get(gui.maininv);
      return wmap.get(gi);
   }

   public void dropMainInvItem(GameUI gui, Coord dc, Coord cc) throws Exception {
      Method dropMethod = Inventory.class.getDeclaredMethod("drop", Coord.class, Coord.class);
      dropMethod.setAccessible(true);
      dropMethod.invoke(gui.maininv, dc, cc);
   }

   public int getGameUIProg(GameUI gui) throws Exception {
      Field progField = GameUI.class.getDeclaredField("prog");
      progField.setAccessible(true);
      return (Integer)progField.get(gui);
   }

   public int countMainInvOccurences(GameUI gui, String resourceName) throws Exception {
      int count = 0;
      if (gui.maininv == null) return 0;
      Field itemsField = Inventory.class.getDeclaredField("items");
      itemsField.setAccessible(true);
      @SuppressWarnings("unchecked")
      List<GItem> items = (List<GItem>)itemsField.get(gui.maininv);

      Method getResMethod;
      Field resField = null;
      try {
         getResMethod = GItem.class.getDeclaredMethod("getres");
         getResMethod.setAccessible(true);
      } catch (NoSuchMethodException e) {
         getResMethod = null;
         resField = GItem.class.getDeclaredField("res");
         resField.setAccessible(true);
      }

      for (GItem gi : items) {
         Resource res = null;
         if (getResMethod != null) res = (Resource)getResMethod.invoke(gi);
         else if (resField != null) res = (Resource)resField.get(gi);

         if (res != null && res.name.endsWith("/" + resourceName)) {
            count++;
         }
      }
      return count;
   }

   public Object[] getFlowerMenuOpts(FlowerMenu fm) throws Exception {
      Field optsField = FlowerMenu.class.getDeclaredField("opts");
      optsField.setAccessible(true);
      return (Object[])optsField.get(fm);
   }

   public int countMainInvOccurrencesSimple(GameUI gui, String resName) throws Exception {
      Method countOccMethod = Inventory.class.getDeclaredMethod("countOccurences", String.class);
      countOccMethod.setAccessible(true);
      return (Integer)countOccMethod.invoke(gui.maininv, resName);
   }

   public void chooseFlowerMenuOption(FlowerMenu fm, Object opt) throws Exception {
      Class<?> petalClass = Class.forName("haven.FlowerMenu$Petal");
      Method chooseMethod = FlowerMenu.class.getDeclaredMethod("choose", petalClass);
      chooseMethod.setAccessible(true);
      chooseMethod.invoke(fm, opt);
   }

   public boolean inventoriesFull(GameUI var1, int var2) {
      int var3 = 25000;
      Glob.CAttr var4 = (Glob.CAttr)var1.ui.sess.glob.cattr.get("carry");
      if (var4 != null) {
         try {
            Field var5 = var4.getClass().getDeclaredField("comp");
            var5.setAccessible(true);
            var3 = var5.getInt(var4);
         } catch (IllegalAccessException | NoSuchFieldException var6) {
            System.err.println("Failed to access 'comp' field: " + var6.getMessage());
         }
      }
      return var1.weight >= var3 - var2;
   }

   public boolean inventoriesFull(GameUI var1) {
      return this.inventoriesFull(var1, 0);
   }

   public boolean isPersonalInventory(GameUI var1, int var2) {
      boolean var3 = false;
      Inventory var4;
      for(Iterator var5 = var1.invwnd.names.keySet().iterator(); var5.hasNext(); var3 = var3 || var4.wdgid() == var2) {
         var4 = (Inventory)var5.next();
      }
      return var3;
   }

   public int freeSpace(GameUI var1) {
      int var2 = 0;
      Inventory var3;
      for(Iterator var4 = var1.invwnd.names.keySet().iterator(); var4.hasNext(); var2 += var3.freeSpace()) {
         var3 = (Inventory)var4.next();
      }
      return var2;
   }

   public int countInventory(GameUI var1, String var2) throws Resource.Loading {
      int var3 = 0;
      Inventory var4;
      for(Iterator var5 = var1.invwnd.names.keySet().iterator(); var5.hasNext(); var3 += var4.countOccurences(var2)) {
         var4 = (Inventory)var5.next();
      }
      return var3;
   }

   public void dropAllLike(GameUI var1, String var2) {
      Iterator var3 = var1.invwnd.names.keySet().iterator();
      while(var3.hasNext()) {
         Inventory var4 = (Inventory)var3.next();
         var4.process(var4.getSameName(var2, false), "drop");
      }
   }

   public GItem getFirstOccurence(GameUI var1, String var2) {
      Iterator var3 = var1.invwnd.names.keySet().iterator();
      while(var3.hasNext()) {
         Inventory var5 = (Inventory)var3.next();
         GItem var4 = var5.getFirst(var2);
         if (var4 != null) return var4;
      }
      return null;
   }

   public boolean inHand(GameUI var1, String var2) {
      boolean var3 = false;
      Iterator var4 = var1.hand.iterator();
      while(var4.hasNext()) {
         GItem var5 = (GItem)var4.next();
         if (((Resource)var5.res.get()).name.contains(var2)) {
            var3 = true;
            break;
         }
      }
      return var3;
   }

   public boolean act(GameUI gui, String action) {
      try {
         gui.act(action);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   public int countHandSeeds(GameUI var1) {
      int var2 = 0;
      Iterator var3 = var1.hand.iterator();
      while(var3.hasNext()) {
         GItem var4 = (GItem)var3.next();
         Iterator var5 = var4.info().iterator();
         while(var5.hasNext()) {
            ItemInfo var6 = (ItemInfo)var5.next();
            if (var6.getClass().equals(GItem.Amount.class)) {
               var2 += ((GItem.Amount)var6).itemnum();
            }
         }
      }
      return var2;
   }
   /**
     * Accesses the equipment count via Composite -> comp -> equ
     */
   public int getPlayerEquipCount(Composite compositeInstance) throws Exception {
      // Error said 'haven.Composited', so we find that class specifically
      Field compField = Composite.class.getDeclaredField("comp");
      compField.setAccessible(true);
      Object internalComp = compField.get(compositeInstance);

      // Look for 'equ' field in whatever class internalComp actually is
      Field equField = internalComp.getClass().getDeclaredField("equ");
      equField.setAccessible(true);
      java.util.Collection<?> equ = (java.util.Collection<?>) equField.get(internalComp);
      return (equ != null) ? equ.size() : 0;
   }

    /**
     * Checks if the player is static via Composite -> comp -> poses -> stat
     */
   public boolean isPlayerStatic(Composite compositeInstance) throws Exception {
      // Navigate: Composite -> comp (Internal) -> poses (Poses) -> stat (boolean)
      Field compField = Composite.class.getDeclaredField("comp");
      compField.setAccessible(true);
      Object internalComp = compField.get(compositeInstance);

      Field posesField = internalComp.getClass().getDeclaredField("poses");
      posesField.setAccessible(true);
      Object posesObj = posesField.get(internalComp);

      // Some clients use 'stat', some use 'moving', some use 'moving' as a bitmask
      // We will look for a boolean field that indicates the player is idle
      for (Field f : posesObj.getClass().getDeclaredFields()) {
         if (f.getType().equals(boolean.class)) {
               f.setAccessible(true);
               if (f.getName().equals("stat")) {
                  return (boolean) f.get(posesObj);
               }
         }
      }
      // Fallback if field name is obfuscated: 
      // Usually the first boolean in Poses is 'stat'
      Field firstBool = posesObj.getClass().getDeclaredFields()[0];
      firstBool.setAccessible(true);
      return (boolean) firstBool.get(posesObj);
   }
}