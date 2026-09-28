package haven;

public class AWidget extends Widget {
   public AWidget(Widget parent) {
      super(Coord.z, Coord.z, parent);
      this.hide();
   }
}
