import haven.Coord;
import haven.Widget;
import haven.WidgetFactory;

public class Maker implements WidgetFactory {
  public Widget create(Coord var1, Widget var2, Object[] args) {
    Coord var4 = (Coord)args[0];
    Coord var5 = (Coord)args[1];
    boolean var6 = (((Integer)args[2]).intValue() != 0);
    return (Widget)new Landwindow(var1, var2, var4, var5, var6);
  }
}
