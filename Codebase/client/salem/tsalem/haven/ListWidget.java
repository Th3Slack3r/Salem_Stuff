package haven;

public abstract class ListWidget<T> extends Widget {
  public final int itemh;
  
  public T sel;
  
  public ListWidget(Coord c, Coord sz, Widget parent, int itemh) {
    super(c, sz, parent);
    this.itemh = itemh;
  }
  
  protected abstract T listitem(int paramInt);
  
  protected abstract int listitems();
  
  protected abstract void drawitem(GOut paramGOut, T paramT);
  
  public void change(T item) {
    this.sel = item;
  }
}
