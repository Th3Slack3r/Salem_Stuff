package haven;

import java.awt.Color;
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
   public boolean sendttupdate = false;
   public boolean matched = false;
   private long filtered = 0L;
   public boolean drop = false;
   private double dropTimer = 0.0;

   public int compareTo(GItem that) {
      Alchemy thisalch = ItemInfo.find(Alchemy.class, this.info());
      Alchemy thatalch = ItemInfo.find(Alchemy.class, that.info());
      if (thisalch == thatalch) {
         return this.rawinfo.hashCode() - that.rawinfo.hashCode();
      } else if (thisalch == null) {
         return -1;
      } else if (thatalch == null) {
         return 1;
      } else if (thisalch.a[0] == thatalch.a[0]) {
         return 0;
      } else {
         return thisalch.a[0] - thatalch.a[0] < 0.0 ? -1 : 1;
      }
   }

   public static void setFilter(ItemFilter filter) {
      GItem.filter = filter;
      lastFilter = System.currentTimeMillis();
   }

   public GItem(Widget parent, Indir<Resource> res) {
      this(Coord.z, parent, res);
   }

   public GItem(Coord c, Widget parent, Indir<Resource> res) {
      super(parent);
      this.c = c;
      this.res = res;
   }

   @Override
   public Glob glob() {
      return this.ui.sess.glob;
   }

   @Override
   public List<ItemInfo> info() {
      if (this.info == null) {
         this.info = ItemInfo.buildinfo(this, this.rawinfo);
         ItemInfo.Name nm = ItemInfo.find(ItemInfo.Name.class, this.info);
         if (nm != null && this.meter > 0) {
            String newtext = nm.str.text + "   (" + this.meter + "% done)";
            ItemInfo.Name newnm = new ItemInfo.Name(nm.owner, newtext);
            int nameidx = this.info.indexOf(nm);
            this.info.set(nameidx, newnm);
         }
      }

      return this.info;
   }

   @Override
   public Resource resource() {
      return this.res.get();
   }

   public String resname() {
      Resource res = this.resource();
      return res != null ? res.name : "";
   }

   public String name() {
      if (this.info != null) {
         ItemInfo.Name name = ItemInfo.find(ItemInfo.Name.class, this.info);
         return name != null ? name.str.text : null;
      } else {
         return null;
      }
   }

   public void testMatch() {
      if (this.filtered < lastFilter) {
         this.matched = filter != null && filter.matches(this.info());
         this.filtered = lastFilter;
      }
   }

   public boolean isSame(GItem that) {
      boolean same = true;
      if (!this.resname().equals(that.resname())) {
         return false;
      } else {
         List<ItemInfo> thisinfo = this.info();
         List<ItemInfo> thatinfo = that.info();

         for (ItemInfo this_ii : thisinfo) {
            if (ItemInfo.AdHoc.class.isInstance(this_ii)) {
               ItemInfo.AdHoc this_adhoc = (ItemInfo.AdHoc)this_ii;
               boolean got_it = false;

               for (ItemInfo that_ii : thatinfo) {
                  if (ItemInfo.AdHoc.class.isInstance(that_ii)) {
                     ItemInfo.AdHoc that_adhoc = (ItemInfo.AdHoc)that_ii;
                     if (this_adhoc.str.text.equals(that_adhoc.str.text)) {
                        got_it = true;
                        break;
                     }
                  }
               }

               if (!got_it) {
                  return false;
               }
            }
         }

         for (ItemInfo that_iix : thatinfo) {
            if (ItemInfo.AdHoc.class.isInstance(that_iix)) {
               ItemInfo.AdHoc that_adhoc = (ItemInfo.AdHoc)that_iix;
               boolean got_it = false;

               for (ItemInfo this_iix : thisinfo) {
                  if (ItemInfo.AdHoc.class.isInstance(this_iix)) {
                     ItemInfo.AdHoc this_adhoc = (ItemInfo.AdHoc)this_iix;
                     if (this_adhoc.str.text.equals(that_adhoc.str.text)) {
                        got_it = true;
                        break;
                     }
                  }
               }

               if (!got_it) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   @Override
   public void tick(double dt) {
      super.tick(dt);
      if (this.drop) {
         this.dropTimer += dt;
         if (this.dropTimer > 0.1) {
            this.dropTimer = 0.0;
            this.wdgmsg("take", new Object[]{Coord.z});
            this.ui.message("Dropping bat!", GameUI.MsgType.BAD);
         }
      }
   }

   @Override
   public void uimsg(String name, Object... args) {
      if (name == "num") {
         int oldnum = this.num;
         this.num = (Integer)args[0];
      } else if (name == "chres") {
         this.res = this.ui.sess.getres((Integer)args[0]);
      } else if (name == "tt") {
         this.info = null;
         this.rawinfo = args;
         this.filtered = 0L;
         if (this.sendttupdate) {
            this.wdgmsg("ttupdate", new Object[0]);
         }

         if (this.parent == this.ui.gui.maininv) {
            this.ui.gui.maininv.resort();
            OverviewTool.instance(this.ui).force_update();
         }

         if (Config.autobucket
            && this.parent == this.ui.gui
            && this.rawinfo.length == 1
            && ((Object[])this.rawinfo[0]).length > 1
            && String.class.isInstance(((Object[])this.rawinfo[0])[1])) {
            String newname = (String)((Object[])this.rawinfo[0])[1];
            if (newname.equals("Bucket")) {
               int tile = this.ui.sess.glob.map.gettile(this.ui.gui.map.player().rc.div(11.0));
               Resource tilesetr = this.ui.sess.glob.map.tilesetr(tile);
               if (tilesetr.name.contains("water")) {
                  this.ui.gui.map.wdgmsg("itemact", new Object[]{this.ui.gui.map.player().sc, this.ui.gui.map.player().rc, 0});
               }
            }
         }
      } else if (name == "meter") {
         this.meter = (Integer)args[0];
      }
   }

   @Widget.RName("item")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         int res = (Integer)args[0];
         return new GItem(c, parent, parent.ui.sess.getres(res));
      }
   }

   public class Amount extends ItemInfo implements GItem.NumberInfo {
      private final int num;

      public Amount(int num) {
         super(GItem.this);
         this.num = num;
      }

      @Override
      public int itemnum() {
         return this.num;
      }
   }

   public interface ColorInfo {
      Color olcol();
   }

   public interface NumberInfo {
      int itemnum();
   }
}
