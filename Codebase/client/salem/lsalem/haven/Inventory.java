package haven;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
   private Comparator<WItem> sorter = null;
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
            if (result == 0) {
               result = Inventory.cmp_desc.compare(o1, o2);
            }

            return result;
         } catch (Loading var4) {
            return 0;
         }
      }
   };
   private static final Comparator<WItem> cmp_gobble = new Comparator<WItem>() {
      public int compare(WItem o1, WItem o2) {
         try {
            GobbleInfo g1 = ItemInfo.find(GobbleInfo.class, o1.item.info());
            GobbleInfo g2 = ItemInfo.find(GobbleInfo.class, o2.item.info());
            if (g1 == null && g2 == null) {
               return Inventory.cmp_name.compare(o1, o2);
            } else if (g1 == null) {
               return 1;
            } else if (g2 == null) {
               return -1;
            } else {
               int v1 = g1.mainTemper();
               int v2 = g2.mainTemper();
               return v1 == v2 ? Inventory.cmp_name.compare(o1, o2) : v2 - v1;
            }
         } catch (Loading var7) {
            return 0;
         }
      }
   };
   Coord isz;
   Coord isz_client;
   public Map<GItem, WItem> wmap = new HashMap<>();
   public int newseq = 0;
   BiMap<Coord, Coord> dictionaryClientServer;
   boolean isTranslated = false;

   @Override
   public void draw(GOut g) {
      invsq(g, Coord.z, this.isz_client);

      for (Coord cc = new Coord(0, 0); cc.y < this.isz_client.y; cc.y++) {
         for (cc.x = 0; cc.x < this.isz_client.x; cc.x++) {
            invrefl(g, sqoff(cc), isqsz);
         }
      }

      super.draw(g);
   }

   public Inventory(Coord c, Coord sz, Widget parent) {
      super(c, invsz(sz), parent);
      this.isz = sz;
      this.isz_client = sz;
      if (!sz.equals(new Coord(1, 1)) && Window.class.isInstance(parent)) {
         this.dictionaryClientServer = HashBiMap.create();
         IButton sbtn = new IButton(Coord.z, parent, Window.obtni[0], Window.obtni[1], Window.obtni[2]) {
            {
               this.tooltip = Text.render("Sort the items in this inventory by name.");
            }

            @Override
            public void click() {
               if (this.ui != null) {
                  Inventory.this.sorter = Inventory.cmp_name;
                  Inventory.this.sortItemsLocally(Inventory.cmp_name);
               }
            }
         };
         sbtn.visible = true;
         ((Window)parent).addtwdg(sbtn);
         IButton sgbtn = new IButton(Coord.z, parent, Window.gbtni[0], Window.gbtni[1], Window.gbtni[2]) {
            {
               this.tooltip = Text.render("Sort the items in this inventory by gobble values.");
            }

            @Override
            public void click() {
               if (this.ui != null) {
                  Inventory.this.sorter = Inventory.cmp_gobble;
                  Inventory.this.sortItemsLocally(Inventory.cmp_gobble);
               }
            }
         };
         sgbtn.visible = true;
         ((Window)parent).addtwdg(sgbtn);
         IButton nsbtn = new IButton(Coord.z, parent, Window.lbtni[0], Window.lbtni[1], Window.lbtni[2]) {
            {
               this.tooltip = Text.render("Undo client-side sorting.");
            }

            @Override
            public void click() {
               if (this.ui != null) {
                  Inventory.this.sorter = null;
                  Inventory.this.removeDictionary();
               }
            }
         };
         nsbtn.visible = true;
         ((Window)parent).addtwdg(nsbtn);
      }
   }

   public void sortItemsLocally(Comparator<WItem> comp) {
      this.isTranslated = true;
      int width = this.isz.x;
      int height = this.isz.y;
      if (this.equals(this.ui.gui.maininv)) {
         int nr_items = this.wmap.size();
         float aspect_ratio = 2.0F;
         width = Math.max(4, (int)Math.ceil(Math.sqrt(aspect_ratio * nr_items)));
         height = Math.max(4, (int)Math.ceil(nr_items / width));
      }

      List<WItem> array = new ArrayList<>(this.wmap.values());
      Collections.sort(array, comp);
      int index = 0;
      BiMap<Coord, Coord> newdictionary = HashBiMap.create();

      try {
         for (WItem w : array) {
            Coord newclientloc = new Coord(index % width, index / width);
            Coord serverloc = w.server_c;
            newdictionary.put(newclientloc, serverloc);
            w.c = sqoff(newclientloc);
            index++;
         }

         this.dictionaryClientServer = newdictionary;
      } catch (IllegalArgumentException var11) {
      }

      this.updateClientSideSize();
   }

   public Coord translateCoordinatesClientServer(Coord client) {
      if (!this.isTranslated) {
         return client;
      } else {
         Coord server = client;
         if (this.dictionaryClientServer.containsKey(client)) {
            server = this.dictionaryClientServer.get(client);
         } else if (this.dictionaryClientServer.containsValue(client)) {
            int width = this.isz.x;
            int height = this.isz.y;
            int index = 0;

            Coord newloc;
            do {
               newloc = new Coord(index % (width - 1), index / (width - 1));
               index++;
            } while (this.dictionaryClientServer.containsValue(newloc));

            server = newloc;
            this.dictionaryClientServer.put(client, newloc);
         }

         return server;
      }
   }

   public Coord translateCoordinatesServerClient(Coord server) {
      if (!this.isTranslated) {
         return server;
      } else {
         BiMap<Coord, Coord> dictionaryServerClient = this.dictionaryClientServer.inverse();
         Coord client;
         if (dictionaryServerClient.containsKey(server)) {
            client = dictionaryServerClient.get(server);
         } else {
            int width = this.isz_client.x;
            int height = this.isz_client.y;
            int index = 0;

            Coord newloc;
            do {
               newloc = new Coord(index % (width - 1), index / (width - 1));
               index++;
            } while (this.dictionaryClientServer.containsKey(newloc));

            boolean expanded = false;
            if (newloc.y >= height - 1 && 2 * height >= width) {
               newloc = new Coord(width, 0);
               expanded = true;
            }

            client = newloc;
            this.dictionaryClientServer.put(newloc, server);
            if (expanded) {
               this.updateClientSideSize();
            }
         }

         return client;
      }
   }

   public void removeDictionary() {
      this.isTranslated = false;
      this.dictionaryClientServer = HashBiMap.create();

      for (WItem w : this.wmap.values()) {
         w.c = sqoff(w.server_c);
      }

      this.updateClientSideSize();
   }

   public Coord updateClientSideSize() {
      if (!this.equals(this.ui.gui.maininv)) {
         return this.isz_client = this.isz;
      } else {
         int maxx = 2;
         int maxy = 2;

         for (WItem w : this.wmap.values()) {
            Coord wc = sqroff(w.c);
            maxx = Math.max(wc.x, maxx);
            maxy = Math.max(wc.y, maxy);
         }

         this.isz_client = new Coord(maxx + 2, maxy + 2);
         this.resize(invsz(this.isz_client));
         return this.isz_client;
      }
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

      for (rc.y = ul.y; rc.y < c.y + sz.y; rc.y = rc.y + refl.sz().y) {
         for (rc.x = ul.x; rc.x < c.x + sz.x; rc.x = rc.x + refl.sz().x) {
            g.image(refl, rc, c, sz);
         }
      }
   }

   public static void invsq(GOut g, Coord c, Coord sz) {
      for (Coord cc = new Coord(0, 0); cc.y < sz.y; cc.y++) {
         for (cc.x = 0; cc.x < sz.x; cc.x++) {
            g.image(bsq, c.add(cc.mul(sqsz)).add(ctl.sz()));
         }
      }

      for (int x = 0; x < sz.x; x++) {
         g.image(obt, c.add(ctl.sz().x + sqsz.x * x, 0));
         g.image(obb, c.add(ctl.sz().x + sqsz.x * x, obt.sz().y + sqsz.y * sz.y - 4));
      }

      for (int y = 0; y < sz.y; y++) {
         g.image(obl, c.add(0, ctl.sz().y + sqsz.y * y));
         g.image(obr, c.add(obl.sz().x + sqsz.x * sz.x - 4, ctl.sz().y + sqsz.y * y));
      }

      g.image(ctl, c);
      g.image(ctr, c.add(ctl.sz().x + sqsz.x * sz.x - 4, 0));
      g.image(cbl, c.add(0, ctl.sz().y + sqsz.y * sz.y - 4));
      g.image(cbr, c.add(cbl.sz().x + sqsz.x * sz.x - 4, ctr.sz().y + sqsz.y * sz.y - 4));
   }

   public static void invsq(GOut g, Coord c) {
      g.image(sqlite, c);
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      if (this.ui.modshift) {
         this.wdgmsg("xfer", new Object[]{amount});
      }

      return true;
   }

   public void resort() {
      if (this.sorter != null) {
         if (Config.alwayssort) {
            this.sortItemsLocally(this.sorter);
         } else {
            this.updateClientSideSize();
         }
      }
   }

   @Override
   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      Coord server_c = (Coord)pargs[0];
      Coord c = this.translateCoordinatesServerClient(server_c);
      Widget ret = gettype(type).create(c, this, cargs);
      if (ret instanceof GItem) {
         GItem i = (GItem)ret;
         this.wmap.put(i, new WItem(sqoff(c), this, i, server_c));
         this.newseq++;
         if (this.isTranslated) {
            this.resort();
         }

         if (this == this.ui.gui.maininv) {
            OverviewTool.instance(this.ui).force_update();
         } else {
            try {
               String title = (this.parent instanceof Window && ((Window)this.parent).cap != null) ? ((Window)this.parent).cap.text : "?";
               String resname = i.res.get().name;
               System.out.println("[Container] " + title + " + " + resname);
            } catch (Exception e) {
               System.out.println("[Container] item added (name not loaded yet)");
            }
         }
      }

      return ret;
   }

   @Override
   public void cdestroy(Widget w) {
      super.cdestroy(w);
      if (w instanceof GItem) {
         GItem i = (GItem)w;
         WItem wi = this.wmap.remove(i);
         Coord wc = sqroff(wi.c.add(isqsz.div(2)));
         if (this.isTranslated) {
            this.dictionaryClientServer.remove(sqroff(wi.c.add(isqsz.div(2))));
            this.resort();
         }

         if (this == this.ui.gui.maininv) {
            OverviewTool.instance(this.ui).force_update();
         }

         this.ui.destroy(wi);
      }
   }

   @Override
   public boolean drop(Coord cc, Coord ul) {
      Coord clientcoords = sqroff(ul.add(isqsz.div(2)));
      Coord servercoords = this.translateCoordinatesClientServer(clientcoords);
      this.wdgmsg("drop", new Object[]{servercoords});
      return true;
   }

   @Override
   public boolean iteminteract(Coord cc, Coord ul) {
      return false;
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg.equals("sz")) {
         this.isz = (Coord)args[0];
         if (this.isTranslated) {
            this.resize(invsz(this.updateClientSideSize()));
         } else {
            this.isz_client = this.isz;
            this.resize(invsz(this.isz));
         }
      }
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (msg.equals("transfer-same")) {
         this.process(this.getSame((GItem)args[0], (Boolean)args[1]), "transfer");
      } else if (msg.equals("drop-same")) {
         this.process(this.getSame((GItem)args[0], (Boolean)args[1]), "drop");
      } else {
         super.wdgmsg(sender, msg, args);
      }
   }

   public void process(List<WItem> items, String action) {
      for (WItem item : items) {
         item.item.wdgmsg(action, new Object[]{Coord.z});
      }
   }

   public List<WItem> getSameName(String name, Boolean ascending) {
      List<WItem> items = new ArrayList<>();

      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible && wdg instanceof WItem && ((WItem)wdg).item.resname().endsWith(name)) {
            items.add((WItem)wdg);
         }
      }

      Collections.sort(items, ascending ? cmp_asc : cmp_desc);
      return items;
   }

   public List<WItem> getSame(GItem item, Boolean ascending) {
      String name = item.resname();
      List<WItem> items = new ArrayList<>();

      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible && wdg instanceof WItem) {
            boolean same;
            if (Config.pickyalt) {
               same = item.isSame(((WItem)wdg).item);
            } else {
               String thatname = ((WItem)wdg).item.resname();
               same = thatname.equals(name);
            }

            if (same) {
               items.add((WItem)wdg);
            }
         }
      }

      Collections.sort(items, ascending ? cmp_asc : cmp_desc);
      return items;
   }

   public GItem getFirst(String name) {
      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible && wdg instanceof WItem && ((WItem)wdg).item.resname().contains(name)) {
            return ((WItem)wdg).item;
         }
      }

      return null;
   }

   private List<GItem> getAll() {
      List<GItem> items = new ArrayList<>();

      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible && wdg instanceof WItem) {
            items.add(((WItem)wdg).item);
         }
      }

      return items;
   }

   public boolean isEmpty() {
      return this.wmap.isEmpty();
   }

   public boolean isFull() {
      return this.wmap.size() == this.isz.x * this.isz.y;
   }

   public int freeSpace() {
      return this.isz.x * this.isz.y - this.wmap.size();
   }

   public int countOccurences(String item_name) {
      int number = 0;

      for (GItem gi : this.wmap.keySet()) {
         try {
            if (gi.res.get().name.endsWith(item_name)) {
               number++;
            }
         } catch (Resource.Loading var6) {
         }
      }

      return number;
   }

   public int countOccurencesLazy(String item_name) {
      int number = 0;

      for (GItem gi : this.wmap.keySet()) {
         try {
            if (gi.res.get().name.contains(item_name)) {
               number++;
            }
         } catch (Resource.Loading var6) {
         }
      }

      return number;
   }

   public int getWaterCarried() {
      int amount = 0;

      for (GItem gi : this.wmap.keySet()) {
         if (gi.res.get().name.endsWith("bucket-water")) {
            for (ItemInfo ii : gi.info()) {
               if (ii.getClass().equals(ItemInfo.Contents.class)) {
                  String text = ((ItemInfo.Name)((ItemInfo.Contents)ii).sub.get(0)).str.text;
                  amount = (int)(amount + 100.0 * Double.parseDouble(text.substring(0, text.indexOf(108))));
               }
            }

            System.out.println("");
         }
      }

      return amount;
   }

   public int countSeeds(String restriction, int per_field) {
      int amount = 0;

      for (GItem gi : this.wmap.keySet()) {
         String name = gi.res.get().name;
         if ((name.startsWith("gfx/invobjs/seeds") || name.startsWith("gfx/invobjs/maize")) && name.contains(restriction)) {
            for (ItemInfo ii : gi.info()) {
               if (ii.getClass().equals(GItem.Amount.class)) {
                  amount += ((GItem.Amount)ii).itemnum() / per_field;
               }
            }
         }
      }

      return amount;
   }

   @Widget.RName("inv")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Inventory(c, (Coord)args[0], parent);
      }
   }
}
