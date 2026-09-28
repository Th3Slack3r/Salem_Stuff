package haven;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

public class GItem extends AWidget implements ItemInfo.ResOwner {
   public Indir<Resource> res;
   public int meter = 0;
   public int num = -1;
   private Object[] rawinfo;
   private List<ItemInfo> info = Collections.emptyList();

   public GItem(Widget parent, Indir<Resource> res) {
      super(parent);
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
      }

      return this.info;
   }

   @Override
   public Resource resource() {
      return this.res.get();
   }

   @Override
   public void uimsg(String name, Object... args) {
      if (name == "num") {
         this.num = (Integer)args[0];
      } else if (name == "chres") {
         this.res = this.ui.sess.getres((Integer)args[0]);
      } else if (name == "tt") {
         this.info = null;
         this.rawinfo = args;
      } else if (name == "meter") {
         this.meter = (Integer)args[0];
      }
   }

   @Widget.RName("item")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         int res = (Integer)args[0];
         return new GItem(parent, parent.ui.sess.getres(res));
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
