package haven;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventory extends Widget implements DTarget {
  private static final Tex obt = Resource.loadtex("gfx/hud/inv/obt");
  
  private static final Tex obr = Resource.loadtex("gfx/hud/inv/obr");
  
  private static final Tex obb = Resource.loadtex("gfx/hud/inv/obb");
  
  private static final Tex obl = Resource.loadtex("gfx/hud/inv/obl");
  
  private static final Tex ctl = Resource.loadtex("gfx/hud/inv/octl");
  
  private static final Tex ctr = Resource.loadtex("gfx/hud/inv/octr");
  
  private static final Tex cbr = Resource.loadtex("gfx/hud/inv/ocbr");
  
  private static final Tex cbl = Resource.loadtex("gfx/hud/inv/ocbl");
  
  private static final Tex bsq = Resource.loadtex("gfx/hud/inv/sq");
  
  public static final Coord sqsz = bsq.sz();
  
  public static final Coord isqsz = new Coord(40, 40);
  
  public static final Tex sqlite = Resource.loadtex("gfx/hud/inv/sq1");
  
  public static final Coord sqlo = new Coord(4, 4);
  
  public static final Tex refl = Resource.loadtex("gfx/hud/invref");
  
  private static final ArrayList<String> sortInventoryNames = new ArrayList<>();
  
  private static long lastAltScroll = 0L;
  
  boolean nameCheckDone;
  
  public static int TRANSFER_LIMIT = 100;
  
  private static final BufferedImage ilockc = Resource.loadimg("gfx/hud/lockc");
  
  private static final BufferedImage ilockch = Resource.loadimg("gfx/hud/lockch");
  
  private static final BufferedImage ilocko = Resource.loadimg("gfx/hud/locko");
  
  private static final BufferedImage ilockoh = Resource.loadimg("gfx/hud/lockoh");
  
  private IButton lockbtn;
  
  public static boolean backpackLocked;
  
  private boolean isBackpack;
  
  private Comparator<WItem> sorter;
  
  private static final String AMBERGRIS = "Ambergris";
  
  private static final String WEIGHT = "Weight";
  
  private boolean removeTooltipOnce;
  
  private static final Comparator<WItem> cmp_asc = new WItemComparator();
  
  private static final Comparator<WItem> cmp_desc = new Comparator<WItem>() {
      public int compare(WItem o1, WItem o2) {
        return Inventory.cmp_asc.compare(o2, o1);
      }
    };
  
  private static final Comparator<WItem> cmp_name = new Comparator<WItem>() {
      public int compare(WItem o1, WItem o2) {
        try {
          int result = o1.item.resname().compareTo(o2.item.resname());
          if (result == 0)
            result = Inventory.cmp_desc.compare(o1, o2); 
          return result;
        } catch (Loading l) {
          return 0;
        } 
      }
    };
  
  private static final Comparator<WItem> cmp_inspi = new Comparator<WItem>() {
      public int compare(WItem o1, WItem o2) {
        try {
          Inspiration i1 = ItemInfo.<Inspiration>find(Inspiration.class, o1.item.info());
          Inspiration i2 = ItemInfo.<Inspiration>find(Inspiration.class, o2.item.info());
          if (i1 == null && i2 == null)
            return 0; 
          if (i2 == null || i1.base > i2.base)
            return -1; 
          if (i1 == null || i1.base < i2.base)
            return 1; 
          return 0;
        } catch (Loading l) {
          return 0;
        } 
      }
    };
  
  private static final Comparator<WItem> cmp_gobble = new Comparator<WItem>() {
      public int compare(WItem o1, WItem o2) {
        try {
          GobbleInfo g1 = ItemInfo.<GobbleInfo>find(GobbleInfo.class, o1.item.info());
          GobbleInfo g2 = ItemInfo.<GobbleInfo>find(GobbleInfo.class, o2.item.info());
          if (g1 == null && g2 == null) {
            if ("Ambergris".equals(o1.item.name()) && "Ambergris".equals(o2.item.name()))
              return Inventory.cmp_inspi.compare(o1, o2); 
            ItemInfo.AdHoc a1 = ItemInfo.<ItemInfo.AdHoc>find(ItemInfo.AdHoc.class, o1.item.info());
            ItemInfo.AdHoc a2 = ItemInfo.<ItemInfo.AdHoc>find(ItemInfo.AdHoc.class, o2.item.info());
            if (a1 != null && a2 != null)
              try {
                if (a1.str.text.contains("Weight") && a2.str.text.contains("Weight")) {
                  double d1 = Double.parseDouble(a1.str.text.split(" ")[1]);
                  double d2 = Double.parseDouble(a2.str.text.split(" ")[1]);
                  if (d1 > d2)
                    return -1; 
                  if (d1 < d2)
                    return 1; 
                } 
              } catch (Exception exception) {} 
            String str1 = ItemInfo.getContent(o1.item.info());
            String str2 = ItemInfo.getContent(o2.item.info());
            if (str1 != null && str2 != null)
              try {
                double d1 = Double.parseDouble(str1.split(" ")[0]);
                double d2 = Double.parseDouble(str2.split(" ")[0]);
                if (d1 > d2)
                  return -1; 
                if (d1 < d2)
                  return 1; 
                return 0;
              } catch (Exception exception) {} 
            return Inventory.cmp_name.compare(o1, o2);
          } 
          if (g1 == null)
            return 1; 
          if (g2 == null)
            return -1; 
          int c1 = g1.mainColour();
          int c2 = g2.mainColour();
          if (c1 > c2)
            return 1; 
          if (c1 < c2)
            return -1; 
          int v1 = g1.mainTemper();
          int v2 = g2.mainTemper();
          if (v1 == v2)
            return Inventory.cmp_name.compare(o1, o2); 
          if (v2 > v1)
            return 1; 
          return -1;
        } catch (Loading l) {
          return 0;
        } 
      }
    };
  
  Coord isz;
  
  Coord isz_client;
  
  Map<GItem, WItem> wmap;
  
  public int newseq;
  
  BiMap<Coord, Coord> dictionaryClientServer;
  
  boolean isTranslated;
  
  @RName("inv")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Inventory(c, (Coord)args[0], parent);
    }
  }
  
  public void draw(GOut g) {
    invsq(g, Coord.z, this.isz_client);
    for (Coord cc = new Coord(0, 0); cc.y < this.isz_client.y; cc.y++) {
      for (cc.x = 0; cc.x < this.isz_client.x; cc.x++)
        invrefl(g, sqoff(cc), isqsz); 
    } 
    if (this.isBackpack)
      this.lockbtn.draw(g.reclipl(xlate(this.lockbtn.c, true), this.lockbtn.sz)); 
    try {
      if (this.removeTooltipOnce && this == this.ui.gui.maininv) {
        this.removeTooltipOnce = false;
        ((Window)this.parent).cbtn.tooltip = null;
      } 
    } catch (Exception exception) {}
    super.draw(g);
  }
  
  public Inventory(Coord c, Coord sz, Widget parent) {
    super(c, invsz(sz), parent);
    sortInventoryNames.add("backpack");
    sortInventoryNames.add("barkpack");
    sortInventoryNames.add("large urn");
    sortInventoryNames.add("chest");
    sortInventoryNames.add("shed");
    sortInventoryNames.add("cupboard");
    sortInventoryNames.add("basket");
    sortInventoryNames.add("dresser");
    sortInventoryNames.add("box");
    sortInventoryNames.add("potion rack");
    sortInventoryNames.add("toolbox");
    sortInventoryNames.add("piggy bank");
    sortInventoryNames.add(" pack");
    sortInventoryNames.add("big autumn");
    this.nameCheckDone = false;
    this.isBackpack = false;
    this.sorter = null;
    this.removeTooltipOnce = true;
    this.wmap = new HashMap<>();
    this.newseq = 0;
    this.isTranslated = false;
    this.isz = sz;
    this.isz_client = sz;
    Widget window_parent = parent;
    while (!Window.class.isInstance(window_parent) && !RootWidget.class.isInstance(window_parent))
      window_parent = window_parent.parent; 
    ((Window)window_parent).hasInventory = true;
    if (this.ui.gui.maininv != this)
      ((Window)window_parent).inventory = this; 
    if (!Config.hide_tooltip_on_inv_x && this != this.ui.gui.maininv)
      ((Window)window_parent).cbtn.tooltip = Text.render("NEW: Hold SHIFT and click to \"Take ALL and Close\"."); 
    if (sz.equals(new Coord(1, 1)) || !Window.class.isInstance(window_parent))
      return; 
    if (sz.x == 1)
      ((Window)window_parent).isOneColumOnly = true; 
    this.dictionaryClientServer = (BiMap<Coord, Coord>)HashBiMap.create();
    IButton sbtn = new IButton(Coord.z, window_parent, Window.obtni[0], Window.obtni[1], Window.obtni[2]) {
        public void click() {
          if (this.ui != null) {
            Inventory.this.sorter = Inventory.cmp_name;
            Inventory.this.sortItemsLocally(Inventory.cmp_name);
          } 
        }
        
        public boolean mousedown(Coord c, int button) {
          if (button != 1 && button != 3)
            return false; 
          if (!checkhit(c))
            return false; 
          this.a = true;
          this.ui.grabmouse(this);
          render();
          return true;
        }
        
        public boolean mouseup(Coord c, int button) {
          if (this.a && button == 1) {
            this.a = false;
            this.ui.grabmouse(null);
            if (checkhit(c))
              click(); 
            render();
            return true;
          } 
          if (this.a && button == 3) {
            this.a = false;
            this.ui.grabmouse(null);
            if (checkhit(c))
              OptWnd2.OptUtil.toggleContinuousSorting(null); 
            render();
            return true;
          } 
          return false;
        }
      };
    sbtn.visible = true;
    ((Window)window_parent).addtwdg(sbtn);
    IButton sgbtn = new IButton(Coord.z, window_parent, Window.gbtni[0], Window.gbtni[1], Window.gbtni[2]) {
        public void click() {
          if (this.ui != null) {
            Inventory.this.sorter = Inventory.cmp_gobble;
            Inventory.this.sortItemsLocally(Inventory.cmp_gobble);
          } 
        }
      };
    sgbtn.visible = true;
    ((Window)window_parent).addtwdg(sgbtn);
    IButton nsbtn = new IButton(Coord.z, window_parent, Window.lbtni[0], Window.lbtni[1], Window.lbtni[2]) {
        public void click() {
          if (this.ui != null) {
            Inventory.this.sorter = null;
            Inventory.this.removeDictionary();
          } 
        }
      };
    nsbtn.visible = true;
    ((Window)window_parent).addtwdg(nsbtn);
    Window win = getparent(Window.class);
    if (win != null && 
      Utils.isBackPack(win)) {
      this.isBackpack = true;
      win.isBackpack = true;
      backpackLocked = Config.backpack_locked;
      this.lockbtn = new IButton(Coord.z, window_parent, backpackLocked ? ilockc : ilocko, backpackLocked ? ilocko : ilockc, backpackLocked ? ilockch : ilockoh) {
          public void click() {
            Inventory.backpackLocked = !Inventory.backpackLocked;
            if (Inventory.backpackLocked) {
              this.up = Inventory.ilockc;
              this.down = Inventory.ilocko;
              this.hover = Inventory.ilockch;
            } else {
              this.up = Inventory.ilocko;
              this.down = Inventory.ilockc;
              this.hover = Inventory.ilockoh;
            } 
            Utils.setprefb("backpack_locked", Inventory.backpackLocked);
          }
        };
      this.lockbtn.recthit = true;
      this.lockbtn.visible = true;
      ((Window)window_parent).addtwdg(this.lockbtn);
    } 
  }
  
  public void sortItemsLocally(Comparator<WItem> comp) {
    this.isTranslated = true;
    List<WItem> array = new ArrayList<>(this.wmap.values());
    try {
      Collections.sort(array, comp);
    } catch (IllegalArgumentException e) {
      return;
    } 
    int width = this.isz.x;
    int height = this.isz.y;
    if (equals(this.ui.gui.maininv)) {
      int nr_items = this.wmap.size();
      height = 4;
      width = 4;
      while (nr_items > height * width) {
        if (width == height * 2 || width == 32) {
          height++;
          continue;
        } 
        width++;
      } 
    } 
    int index = 0;
    HashBiMap hashBiMap = HashBiMap.create();
    try {
      for (WItem w : array) {
        Coord newclientloc = new Coord(index % width, index / width);
        Coord serverloc = w.server_c;
        hashBiMap.put(newclientloc, serverloc);
        w.c = sqoff(newclientloc);
        index++;
      } 
      this.dictionaryClientServer = (BiMap<Coord, Coord>)hashBiMap;
    } catch (IllegalArgumentException illegalArgumentException) {}
    updateClientSideSize();
  }
  
  public Coord translateCoordinatesClientServer(Coord client) {
    if (!this.isTranslated)
      return client; 
    Coord server = client;
    if (this.dictionaryClientServer.containsKey(client)) {
      server = (Coord)this.dictionaryClientServer.get(client);
    } else if (this.dictionaryClientServer.containsValue(client)) {
      int width = this.isz.x;
      int index = 0;
      while (true) {
        Coord newloc = new Coord(index % (width - 1), index / (width - 1));
        index++;
        if (!this.dictionaryClientServer.containsValue(newloc)) {
          server = newloc;
          this.dictionaryClientServer.put(client, server);
          break;
        } 
      } 
    } 
    return server;
  }
  
  Coord getEmptyLocalSpot(BiMap<Coord, Coord> dictionary, int width) {
    int index = 0;
    while (true) {
      Coord newloc = new Coord(index % (width - 1), index / (width - 1));
      index++;
      if (!dictionary.containsKey(newloc))
        return newloc; 
    } 
  }
  
  public Coord translateCoordinatesServerClient(Coord server) {
    if (!this.isTranslated)
      return server; 
    Coord client = server;
    BiMap<Coord, Coord> dictionaryServerClient = this.dictionaryClientServer.inverse();
    if (dictionaryServerClient.containsKey(server)) {
      client = (Coord)dictionaryServerClient.get(server);
    } else {
      int width = this.isz_client.x;
      int height = this.isz_client.y;
      Coord newloc = getEmptyLocalSpot(this.dictionaryClientServer, width);
      boolean expanded = false;
      if (newloc.y >= height && 2 * height >= width) {
        newloc = new Coord(0, height);
        expanded = true;
      } 
      client = newloc;
      this.dictionaryClientServer.put(client, server);
      if (expanded)
        updateClientSideSize(); 
    } 
    return client;
  }
  
  public void removeDictionary() {
    this.isTranslated = false;
    this.dictionaryClientServer = (BiMap<Coord, Coord>)HashBiMap.create();
    for (WItem w : this.wmap.values())
      w.c = sqoff(w.server_c); 
    updateClientSideSize();
  }
  
  public Coord updateClientSideSize() {
    if (equals(this.ui.gui.maininv)) {
      int maxx = 2;
      int maxy = 2;
      for (WItem w : this.wmap.values()) {
        Coord wc = sqroff(w.c);
        maxx = Math.max(wc.x, maxx);
        maxy = Math.max(wc.y, maxy);
      } 
      this.isz_client = new Coord(Math.min(maxx, 30) + 2, Math.min(maxy, 30) + 2);
      resize(invsz(this.isz_client));
      return this.isz_client;
    } 
    return this.isz_client = this.isz;
  }
  
  public static Coord sqoff(Coord c) {
    return c.mul(sqsz).add(ctl.sz());
  }
  
  public static Coord sqroff(Coord c) {
    return c.sub(ctl.sz()).div(sqsz);
  }
  
  public static Coord invsz(Coord sz) {
    return sz.mul(sqsz).add(ctl.sz()).add(cbr.sz()).sub(4, 4);
  }
  
  public static void invrefl(GOut g, Coord c, Coord sz) {
    Coord ul = g.ul.sub(g.ul.div(2)).mod(refl.sz()).inv();
    Coord rc = new Coord();
    for (rc.y = ul.y; rc.y < c.y + sz.y; rc.y += (refl.sz()).y) {
      for (rc.x = ul.x; rc.x < c.x + sz.x; rc.x += (refl.sz()).x)
        g.image(refl, rc, c, sz); 
    } 
  }
  
  public static void invsq(GOut g, Coord c, Coord sz) {
    for (Coord cc = new Coord(0, 0); cc.y < sz.y; cc.y++) {
      for (cc.x = 0; cc.x < sz.x; cc.x++)
        g.image(bsq, c.add(cc.mul(sqsz)).add(ctl.sz())); 
    } 
    for (int x = 0; x < sz.x; x++) {
      g.image(obt, c.add((ctl.sz()).x + sqsz.x * x, 0));
      g.image(obb, c.add((ctl.sz()).x + sqsz.x * x, (obt.sz()).y + sqsz.y * sz.y - 4));
    } 
    for (int y = 0; y < sz.y; y++) {
      g.image(obl, c.add(0, (ctl.sz()).y + sqsz.y * y));
      g.image(obr, c.add((obl.sz()).x + sqsz.x * sz.x - 4, (ctl.sz()).y + sqsz.y * y));
    } 
    g.image(ctl, c);
    g.image(ctr, c.add((ctl.sz()).x + sqsz.x * sz.x - 4, 0));
    g.image(cbl, c.add(0, (ctl.sz()).y + sqsz.y * sz.y - 4));
    g.image(cbr, c.add((cbl.sz()).x + sqsz.x * sz.x - 4, (ctr.sz()).y + sqsz.y * sz.y - 4));
  }
  
  public static void invsq(GOut g, Coord c) {
    g.image(sqlite, c);
  }
  
  private static HashMap<WItem, Long> tempMap = new HashMap<>();
  
  private static void clearOldItems() {
    long s = System.currentTimeMillis() - 300L;
    ArrayList<WItem> toRemove = new ArrayList<>();
    for (Map.Entry<WItem, Long> e : tempMap.entrySet()) {
      if (((Long)e.getValue()).longValue() < s)
        toRemove.add(e.getKey()); 
    } 
    for (WItem wItem : toRemove)
      tempMap.remove(wItem); 
  }
  
  private static void addItemToMap(WItem w) {
    long s = System.currentTimeMillis();
    tempMap.put(w, Long.valueOf(s));
  }
  
  public boolean mousewheel(Coord c, int amount) {
    if (this.ui.modshift) {
      wdgmsg("xfer", new Object[] { Integer.valueOf(amount) });
    } else if (this.ui.modctrl && this != this.ui.gui.maininv) {
      try {
        if (amount < 0) {
          List<WItem> mainInvItems = this.ui.gui.maininv.getSameName("", Boolean.valueOf(true));
          int mainInvItemSize = mainInvItems.size();
          if (mainInvItemSize > 1023)
            return true; 
          List<WItem> items = getSameName("", Boolean.valueOf(true));
          Collections.sort(items, new Comparator<WItem>() {
                public int compare(WItem o2, WItem o1) {
                  if (o1.server_c.y > o2.server_c.y)
                    return 1; 
                  if (o1.server_c.y < o2.server_c.y)
                    return -1; 
                  if (o1.server_c.x > o2.server_c.x)
                    return 1; 
                  if (o1.server_c.x < o2.server_c.x)
                    return -1; 
                  return 0;
                }
              });
          clearOldItems();
          for (WItem wItem : items) {
            if (tempMap.containsKey(wItem))
              continue; 
            if (amount < 0 && mainInvItemSize < Utils.getInvMaxSize()) {
              wItem.item.wdgmsg("transfer", new Object[] { Coord.z });
              addItemToMap(wItem);
              amount++;
              mainInvItemSize++;
            } 
          } 
        } else {
          if (this == this.ui.gui.maininv)
            return true; 
          HashMap<String, Integer> iMap = new HashMap<>();
          List<WItem> items = this.ui.gui.maininv.getSameName("", Boolean.valueOf(true));
          int itemSize = items.size();
          if (itemSize <= 0)
            return true; 
          clearOldItems();
          for (WItem wItem : items) {
            if (tempMap.containsKey(wItem))
              continue; 
            Integer number = Integer.valueOf(1);
            String name = wItem.item.resname();
            if (iMap.containsKey(name)) {
              Integer oldNumber = iMap.get(name);
              number = Integer.valueOf(number.intValue() + oldNumber.intValue());
            } 
            iMap.put(name, number);
          } 
          while (amount > 0) {
            String targetName = "";
            int lowestNumber = 1025;
            for (Map.Entry<String, Integer> e : iMap.entrySet()) {
              if (((Integer)e.getValue()).intValue() < lowestNumber && ((Integer)e.getValue()).intValue() > 0) {
                lowestNumber = ((Integer)e.getValue()).intValue();
                targetName = e.getKey();
              } 
            } 
            List<WItem> targets = this.ui.gui.maininv.getSameName(targetName, Boolean.valueOf(true));
            if (targets.size() > 0) {
              for (WItem wItem : targets) {
                if (tempMap.containsKey(wItem))
                  continue; 
                addItemToMap(wItem);
                wItem.item.wdgmsg("transfer", new Object[] { Coord.z });
                lowestNumber--;
                iMap.put(targetName, Integer.valueOf(lowestNumber));
                itemSize++;
                amount--;
                if (amount <= 0)
                  break; 
              } 
              continue;
            } 
            return true;
          } 
        } 
      } catch (Exception exception) {}
    } 
    return true;
  }
  
  public void resort() {
    if (this.sorter == null && !Config.alwayssort)
      return; 
    if (!this.nameCheckDone && this.sorter == null && Config.alwayssort) {
      this.nameCheckDone = true;
      if (Config.sort_only_main_inv) {
        if (this == this.ui.gui.maininv)
          this.sorter = cmp_name; 
      } else {
        Window win = getparent(Window.class);
        if (win != null)
          for (String invName : sortInventoryNames) {
            if (win.cap.text.trim().toLowerCase().contains(invName)) {
              this.sorter = cmp_name;
              break;
            } 
          }  
      } 
    } 
    if (Config.alwayssort) {
      if (this.sorter == null)
        return; 
      sortItemsLocally(this.sorter);
    } else {
      updateClientSideSize();
    } 
  }
  
  public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    Coord server_c = (Coord)pargs[0];
    Coord c = translateCoordinatesServerClient(server_c);
    Widget ret = Widget.gettype(type).create(c, this, cargs);
    if (ret instanceof GItem) {
      GItem i = (GItem)ret;
      this.wmap.put(i, new WItem(sqoff(c), this, i, server_c));
      this.newseq++;
      if (this.isTranslated)
        resort(); 
      if (this == this.ui.gui.maininv)
        OverviewTool.instance(this.ui).force_update(); 
    } 
    return ret;
  }
  
  public void cdestroy(Widget w) {
    super.cdestroy(w);
    if (w instanceof GItem) {
      GItem i = (GItem)w;
      WItem wi = this.wmap.remove(i);
      if (this.isTranslated) {
        this.dictionaryClientServer.remove(sqroff(wi.c.add(isqsz.div(2))));
        resort();
      } 
      if (this == this.ui.gui.maininv) {
        OverviewTool.instance(this.ui).force_update();
        if (GameUI.craftP);
      } 
      this.ui.destroy(wi);
    } 
  }
  
  public boolean drop(Coord cc, Coord ul) {
    Coord clientcoords = sqroff(ul.add(isqsz.div(2)));
    Coord servercoords = translateCoordinatesClientServer(clientcoords);
    wdgmsg("drop", new Object[] { servercoords });
    return true;
  }
  
  public boolean iteminteract(Coord cc, Coord ul) {
    return false;
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg.equals("sz")) {
      this.isz = (Coord)args[0];
      if (this.isTranslated) {
        updateClientSideSize();
      } else {
        this.isz_client = this.isz;
        resize(invsz(this.isz));
      } 
    } 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (msg.equals("transfer-same-no-limit")) {
      process(getSame((GItem)args[0], (Boolean)args[1]), "transfer");
    } else if (msg.equals("transfer-same")) {
      if (Config.limit_transfer_amount) {
        process(getSame((GItem)args[0], (Boolean)args[1]), "transfer", TRANSFER_LIMIT);
      } else {
        process(getSame((GItem)args[0], (Boolean)args[1]), "transfer");
      } 
    } else if (msg.equals("drop-same")) {
      ((GItem)args[0]).wdgmsg("drop", new Object[] { this.c, Integer.valueOf(-1) });
    } else if (msg.equals("transfer-same-percentage")) {
      if (Config.limit_transfer_amount) {
        process(getSame((GItem)args[0], (Boolean)args[1], Boolean.valueOf(true)), "transfer", TRANSFER_LIMIT);
      } else {
        process(getSame((GItem)args[0], (Boolean)args[1], Boolean.valueOf(true)), "transfer");
      } 
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  private void process(List<WItem> items, String action) {
    for (WItem item : items) {
      item.item.wdgmsg(action, new Object[] { Coord.z });
    } 
  }
  
  private void process(List<WItem> items, String action, int limitation) {
    int count = 0;
    for (WItem item : items) {
      if (++count > limitation)
        break; 
      item.item.wdgmsg(action, new Object[] { Coord.z });
    } 
  }
  
  public List<WItem> getSameName(String name, Boolean ascending) {
    List<WItem> items = new ArrayList<>();
    for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg.visible && wdg instanceof WItem && 
        ((WItem)wdg).item.resname().contains(name))
        items.add((WItem)wdg); 
    } 
    Collections.sort(items, ascending.booleanValue() ? cmp_asc : cmp_desc);
    return items;
  }
  
  private List<WItem> getSame(GItem item, Boolean ascending) {
    return getSame(item, ascending, Boolean.valueOf(false));
  }
  
  private List<WItem> getSame(GItem item, Boolean ascending, Boolean onlySamePercentage) {
    String name = item.resname();
    List<WItem> items = new ArrayList<>();
    for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg.visible && wdg instanceof WItem) {
        boolean same;
        if (Config.pickyalt) {
          same = item.isSame(((WItem)wdg).item, onlySamePercentage);
        } else if (onlySamePercentage.booleanValue()) {
          same = item.isSame(((WItem)wdg).item, onlySamePercentage);
        } else {
          String thatname = ((WItem)wdg).item.resname();
          same = thatname.equals(name);
        } 
        if (same)
          items.add((WItem)wdg); 
      } 
    } 
    Collections.sort(items, ascending.booleanValue() ? cmp_asc : cmp_desc);
    return items;
  }
}
