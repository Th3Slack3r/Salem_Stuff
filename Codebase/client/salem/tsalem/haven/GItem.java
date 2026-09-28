package haven;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GItem extends AWidget implements ItemInfo.ResOwner, Comparable<GItem> {
  public static volatile long infoUpdated;
  
  static ItemFilter filter = null;
  
  private static long lastFilter = 0L;
  
  public Indir<Resource> res;
  
  public int meter = 0;
  
  public int num = -1;
  
  private Object[] rawinfo;
  
  private List<ItemInfo> info = Collections.emptyList();
  
  public boolean marked = false;
  
  public static String handItemContainerContent = null;
  
  public static int handItemID = 0;
  
  private static ArrayList<String> nameList = new ArrayList<>();
  
  private static ArrayList<String> drinkingVessels = new ArrayList<>();
  
  private static ArrayList<String> listOfKeyWords = new ArrayList<>();
  
  static {
    nameList.add("gfx/invobjs/pale");
    nameList.add("gfx/invobjs/bucket");
    nameList.add("gfx/invobjs/copperpot");
    nameList.add("gfx/invobjs/claypot");
    drinkingVessels.add("flask");
    drinkingVessels.add("barkcup");
    drinkingVessels.add("goathorn");
    drinkingVessels.add("glassvial");
    listOfKeyWords.add("seeds");
    listOfKeyWords.add("of");
  }
  
  public int compareTo(GItem that) {
    Alchemy thisalch = ItemInfo.<Alchemy>find(Alchemy.class, info());
    Alchemy thatalch = ItemInfo.<Alchemy>find(Alchemy.class, that.info());
    if (thisalch == thatalch)
      return this.rawinfo.hashCode() - that.rawinfo.hashCode(); 
    if (thisalch == null)
      return -1; 
    if (thatalch == null)
      return 1; 
    if (thisalch.a[0] == thatalch.a[0])
      return 0; 
    return (thisalch.a[0] - thatalch.a[0] < 0.0D) ? -1 : 1;
  }
  
  public boolean sendttupdate = false;
  
  public boolean matched = false;
  
  private long filtered = 0L;
  
  public boolean drop = false;
  
  private double dropTimer = 0.0D;
  
  public static void setFilter(ItemFilter filter) {
    GItem.filter = filter;
    lastFilter = System.currentTimeMillis();
  }
  
  @RName("item")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      int res = ((Integer)args[0]).intValue();
      return new GItem(c, parent, parent.ui.sess.getres(res));
    }
  }
  
  public class Amount extends ItemInfo implements NumberInfo {
    private final int num;
    
    public Amount(int num) {
      super(GItem.this);
      this.num = num;
    }
    
    public int itemnum() {
      return this.num;
    }
  }
  
  public GItem(Widget parent, Indir<Resource> res) {
    this(Coord.z, parent, res);
  }
  
  public GItem(Coord c, Widget parent, Indir<Resource> res) {
    super(parent);
    this.c = c;
    this.res = res;
  }
  
  public Glob glob() {
    return this.ui.sess.glob;
  }
  
  public List<ItemInfo> info() {
    if (this.info == null) {
      this.info = ItemInfo.buildinfo(this, this.rawinfo);
      ItemInfo.Name nm = ItemInfo.<ItemInfo.Name>find(ItemInfo.Name.class, this.info);
      if (nm != null && 
        this.meter > 0) {
        String newtext = nm.str.text + "   (" + this.meter + "% done)";
        ItemInfo.Name newnm = new ItemInfo.Name(nm.owner, newtext);
        int nameidx = this.info.indexOf(nm);
        this.info.set(nameidx, newnm);
      } 
    } 
    return this.info;
  }
  
  public Resource resource() {
    return this.res.get();
  }
  
  public String resname() {
    Resource res = null;
    try {
      res = resource();
    } catch (Loading loading) {}
    if (res != null)
      return res.name; 
    return "";
  }
  
  public String name() {
    if (this.info != null) {
      ItemInfo.Name name = ItemInfo.<ItemInfo.Name>find(ItemInfo.Name.class, this.info);
      return (name != null) ? name.str.text : null;
    } 
    return null;
  }
  
  public void testMatch() {
    if (this.filtered < lastFilter) {
      this.matched = (filter != null && filter.matches(info()));
      this.filtered = lastFilter;
    } 
  }
  
  public boolean isSame(GItem that) {
    return isSame(that, Boolean.valueOf(false));
  }
  
  public boolean isSame(GItem that, Boolean onlySamePercentage) {
    if (!resname().equals(that.resname()))
      return false; 
    List<ItemInfo> thisinfo = info();
    List<ItemInfo> thatinfo = that.info();
    for (ItemInfo this_ii : thisinfo) {
      if (ItemInfo.AdHoc.class.isInstance(this_ii)) {
        ItemInfo.AdHoc this_adhoc = (ItemInfo.AdHoc)this_ii;
        boolean got_it = false;
        for (ItemInfo that_ii : thatinfo) {
          if (ItemInfo.AdHoc.class.isInstance(that_ii)) {
            ItemInfo.AdHoc that_adhoc = (ItemInfo.AdHoc)that_ii;
            if (this_adhoc.str.text.equals(that_adhoc.str.text) || this_adhoc.str.text
              .toLowerCase().contains("carats") || this_adhoc.str.text
              .toLowerCase().contains("weight")) {
              got_it = true;
              break;
            } 
          } 
        } 
        if (!got_it)
          return false; 
      } 
    } 
    for (ItemInfo that_ii : thatinfo) {
      if (ItemInfo.AdHoc.class.isInstance(that_ii)) {
        ItemInfo.AdHoc that_adhoc = (ItemInfo.AdHoc)that_ii;
        boolean got_it = false;
        for (ItemInfo this_ii : thisinfo) {
          if (ItemInfo.AdHoc.class.isInstance(this_ii)) {
            ItemInfo.AdHoc this_adhoc = (ItemInfo.AdHoc)this_ii;
            if (this_adhoc.str.text.equals(that_adhoc.str.text) || this_adhoc.str.text
              .toLowerCase().contains("carats") || this_adhoc.str.text
              .toLowerCase().contains("weight")) {
              got_it = true;
              break;
            } 
          } 
        } 
        if (!got_it)
          return false; 
      } 
    } 
    if (onlySamePercentage.booleanValue() && compareTo(that) != 0)
      return false; 
    return true;
  }
  
  public void tick(double dt) {
    super.tick(dt);
    if (this.drop) {
      this.dropTimer += dt;
      if (this.dropTimer > 0.1D) {
        this.dropTimer = 0.0D;
        wdgmsg("take", new Object[] { Coord.z });
        this.ui.message("Dropping bat!", GameUI.MsgType.BAD);
      } 
    } 
  }
  
  public void uimsg(String name, Object... args) {
    if (name == "num") {
      int oldnum = this.num;
      this.num = ((Integer)args[0]).intValue();
    } else if (name == "chres") {
      this.res = this.ui.sess.getres(((Integer)args[0]).intValue());
    } else if (name == "tt") {
      this.info = null;
      this.rawinfo = args;
      this.filtered = 0L;
      if (this.sendttupdate)
        wdgmsg("ttupdate", new Object[0]); 
      if (this.parent == this.ui.gui.maininv) {
        this.ui.gui.maininv.resort();
        OverviewTool.instance(this.ui).force_update();
      } else if (this.parent instanceof Inventory) {
        ((Inventory)this.parent).resort();
      } 
      if ((Config.autobucket || Config.auto_backpack_bucket) && this.parent == this.ui.gui) {
        boolean autoBucketClick = false;
        if (nameList.contains(resname()))
          try {
            int tile = this.ui.sess.glob.map.gettile((this.ui.gui.map.player()).rc.div(11.0D));
            Resource tilesetr = this.ui.sess.glob.map.tilesetr(tile);
            if (Config.autobucket && tilesetr.name.contains("water")) {
              this.ui.gui.map.wdgmsg("itemact", new Object[] { (this.ui.gui.map.player()).sc, (this.ui.gui.map.player()).rc, Integer.valueOf(0) });
              autoBucketClick = true;
            } 
          } catch (Exception e) {
            this.ui.message("[AutoBucket] error: " + e.toString(), GameUI.MsgType.INFO);
          }  
        final GItem thisGItem = this;
        final boolean autoBucketClick2 = autoBucketClick;
        (new Thread(new Runnable() {
              public void run() {
                boolean invObjTarget = false;
                if (ItemDrag.lastClickTargetType != null && ItemDrag.lastClickTargetType.equals(ItemDrag.TargetType.InventoryObject))
                  invObjTarget = true; 
                boolean isFlaskOrCup = GItem.this.isDrinkingVessel(thisGItem);
                if (!autoBucketClick2 && Config.auto_backpack_bucket && !invObjTarget && !isFlaskOrCup && 
                  GItem.handItemID == thisGItem.wdgid() && 
                  GItem.handItemContainerContent != null) {
                  List<ItemInfo> info2 = null;
                  int count = 0;
                  while (info2 == null && count < 50) {
                    count++;
                    try {
                      info2 = thisGItem.info();
                    } catch (Exception e) {
                      try {
                        Thread.sleep(10L);
                      } catch (Exception exception) {}
                    } 
                  } 
                  if (ItemInfo.getContent(info2) == null) {
                    Window win = null;
                    WItem targetItem = null;
                    Inventory invTarget = null;
                    double maxFill = 0.0D;
                    for (Widget w : UI.instance.widgets.values()) {
                      if (w instanceof Inventory) {
                        win = w.<Window>getparent(Window.class);
                        if (win != null && Utils.isBackPack(win))
                          invTarget = (Inventory)w; 
                      } 
                    } 
                    if (invTarget != null) {
                      ArrayList<WItem> itemsFromBackpack = thisGItem.getWItemsFromInv(invTarget);
                      for (WItem wItem : itemsFromBackpack) {
                        double fill = thisGItem.getItemFill(wItem.item);
                        if (maxFill < fill) {
                          maxFill = fill;
                          targetItem = wItem;
                        } 
                      } 
                    } 
                    List<WItem> itemsFromMainInv = thisGItem.ui.gui.maininv.getSameName("", Boolean.valueOf(true));
                    for (WItem wItem : itemsFromMainInv) {
                      double fill = thisGItem.getItemFill(wItem.item);
                      if (maxFill < fill) {
                        maxFill = fill;
                        invTarget = thisGItem.ui.gui.maininv;
                        targetItem = wItem;
                      } 
                    } 
                    if (targetItem != null)
                      thisGItem.ui.wdgmsg(invTarget, "drop", new Object[] { targetItem.server_c }); 
                  } 
                } 
                GItem.handItemID = thisGItem.wdgid();
                GItem.handItemContainerContent = thisGItem.getContentType(thisGItem);
              }
            }"AutoBucketFromInvOrBPack")).start();
      } 
    } else if (name == "meter") {
      this.meter = ((Integer)args[0]).intValue();
    } 
  }
  
  private ArrayList<WItem> getWItemsFromInv(Inventory inv) {
    ArrayList<WItem> items = new ArrayList<>();
    WItem current = null;
    for (Widget w = inv.lchild; w != null; w = w.prev) {
      if (w.visible && w instanceof WItem) {
        current = (WItem)w;
        items.add(current);
      } 
    } 
    return items;
  }
  
  private ArrayList<GItem> getGItemsFromInv(Inventory inv) {
    ArrayList<GItem> items = new ArrayList<>();
    GItem current = null;
    for (Widget w = inv.lchild; w != null; w = w.prev) {
      if (w.visible && w instanceof WItem) {
        current = ((WItem)w).item;
        items.add(current);
      } 
    } 
    return items;
  }
  
  private double getItemFill(GItem gItem) {
    double fill = 0.0D;
    String contentType = getContentType(gItem);
    if (contentType != null && contentType.equals(handItemContainerContent)) {
      if (isDrinkingVessel(gItem))
        return fill; 
      fill = Double.parseDouble(ItemInfo.getCount(gItem.info()));
    } 
    return fill;
  }
  
  private boolean isDrinkingVessel(GItem gItem) {
    String name = gItem.resname();
    for (String drinkingVessel : drinkingVessels) {
      if (name != null && name.contains(drinkingVessel))
        return true; 
    } 
    return false;
  }
  
  private String getContentType(GItem gItem) {
    String contentType = null;
    List<ItemInfo> info2 = null;
    try {
      info2 = gItem.info();
    } catch (Exception exception) {}
    if (this.info != null && info2 != null) {
      String content = ItemInfo.getContent(info2);
      for (String string : listOfKeyWords) {
        if (content != null && content.contains(string)) {
          contentType = content.substring(content.indexOf(string));
          break;
        } 
      } 
    } 
    return contentType;
  }
  
  private String getHandContent() {
    int counter = 0;
    while (counter < 50) {
      counter++;
      try {
        Thread.sleep(10L);
        return ItemInfo.getContent(((GItem)this.ui.gui.hand.iterator().next()).info());
      } catch (Exception exception) {}
    } 
    return null;
  }
  
  public static interface NumberInfo {
    int itemnum();
  }
  
  public static interface ColorInfo {
    Color olcol();
  }
}
