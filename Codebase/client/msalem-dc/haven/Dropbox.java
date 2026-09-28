package haven;

import java.awt.Color;

public abstract class Dropbox<T> extends ListWidget<T> {
   public static final Tex drop = Resource.loadtex("gfx/hud/drop");
   public final int listh;
   private final Coord dropc;
   private Dropbox<T>.Droplist dl;

   public Dropbox(Coord c, Widget parent, int w, int listh, int itemh) {
      super(c, new Coord(w, itemh).add(Window.fbox.bisz()), parent, itemh);
      this.listh = listh;
      this.dropc = new Coord(this.sz.x - Window.fbox.bl.sz().x - drop.sz().x, Window.fbox.bt.sz().y);
   }

   @Override
   public void draw(GOut g) {
      g.chcolor(Color.BLACK);
      g.frect(Coord.z, this.sz);
      g.chcolor();
      Window.fbox.draw(g, Coord.z, this.sz);
      Coord off = Window.fbox.btloff();
      if (this.sel != null) {
         this.drawitem(g.reclip(off, new Coord(this.sz.x - drop.sz().x, this.itemh).sub(Window.fbox.bisz())), this.sel);
      }

      g.image(drop, this.dropc);
      super.draw(g);
   }

   @Override
   public boolean mousedown(Coord c, int btn) {
      if (super.mousedown(c, btn)) {
         return true;
      } else if (this.dl == null && btn == 1) {
         this.dl = new Dropbox.Droplist();
         return true;
      } else {
         return true;
      }
   }

   private class Droplist extends Listbox<T> {
      private Droplist() {
         super(
            Dropbox.this.rootpos().add(0, Dropbox.this.sz.y),
            Dropbox.this.ui.root,
            Dropbox.this.sz.x - Window.fbox.bisz().x,
            Math.min(Dropbox.this.listh, Dropbox.this.listitems()),
            Dropbox.this.itemh
         );
         this.ui.grabmouse(this);
         this.sel = Dropbox.this.sel;
      }

      @Override
      protected T listitem(int i) {
         return Dropbox.this.listitem(i);
      }

      @Override
      protected int listitems() {
         return Dropbox.this.listitems();
      }

      @Override
      protected void drawitem(GOut g, T item) {
         Dropbox.this.drawitem(g, item);
      }

      @Override
      public boolean mousedown(Coord c, int btn) {
         if (!c.isect(Coord.z, this.sz)) {
            this.reqdestroy();
            return true;
         } else {
            return super.mousedown(c, btn);
         }
      }

      @Override
      public void destroy() {
         this.ui.grabmouse(null);
         super.destroy();
         Dropbox.this.dl = null;
      }

      @Override
      public void change(T item) {
         Dropbox.this.change(item);
         this.reqdestroy();
      }
   }
}
