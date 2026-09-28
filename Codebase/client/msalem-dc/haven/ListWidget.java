package haven;

public abstract class ListWidget<T> extends Widget {
   public final int itemh;
   public T sel;

   public ListWidget(Coord c, Coord sz, Widget parent, int itemh) {
      super(c, sz, parent);
      this.itemh = itemh;
   }

   protected abstract T listitem(int var1);

   protected abstract int listitems();

   protected abstract void drawitem(GOut var1, T var2);

   public void change(T item) {
      this.sel = item;
   }
}
