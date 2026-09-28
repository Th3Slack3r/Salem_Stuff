package haven;

import java.util.Collection;
import java.util.LinkedList;

public class Tabs {
   private Coord c;
   private Coord sz;
   private Widget parent;
   public Tabs.Tab curtab = null;
   public Collection<Tabs.Tab> tabs = new LinkedList<>();

   public Tabs(Coord c, Coord sz, Widget parent) {
      this.c = c;
      this.sz = sz;
      this.parent = parent;
   }

   public void showtab(Tabs.Tab tab) {
      Tabs.Tab old = this.curtab;
      if (old != null) {
         old.hide();
      }

      if ((this.curtab = tab) != null) {
         this.curtab.show();
      }

      this.changed(old, tab);
   }

   public void changed(Tabs.Tab from, Tabs.Tab to) {
   }

   public class Tab extends Widget {
      public Tabs.TabButton btn;

      public Tab() {
         super(Tabs.this.c, Tabs.this.sz, Tabs.this.parent);
         if (Tabs.this.curtab == null) {
            Tabs.this.curtab = this;
         } else {
            this.hide();
         }

         Tabs.this.tabs.add(this);
      }

      public Tab(Coord bc, int bw, String text) {
         this();
         this.btn = Tabs.this.new TabButton(bc, bw, text, this);
      }
   }

   public class TabButton extends Button {
      public final Tabs.Tab tab;

      private TabButton(Coord c, Integer w, String text, Tabs.Tab tab) {
         super(c, w, Tabs.this.parent, text);
         this.tab = tab;
      }

      @Override
      public void click() {
         Tabs.this.showtab(this.tab);
      }
   }
}
