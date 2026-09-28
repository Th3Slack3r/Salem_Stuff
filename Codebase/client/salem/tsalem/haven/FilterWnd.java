package haven;

public class FilterWnd extends GameUI.Hidewnd {
  TextEntry input;
  
  FilterWnd(Widget parent) {
    super(new Coord(120, 200), Coord.z, parent, "Filter");
    this.cbtn.visible = false;
    this.cap = null;
    this.input = new TextEntry(Coord.z, 200, this, "") {
        protected void changed() {
          FilterWnd.this.chectInput();
        }
      };
    pack();
    hide();
  }
  
  private void setFilter(String text) {
    if (text == null) {
      GItem.setFilter(null);
    } else {
      GItem.setFilter(ItemFilter.create(text));
    } 
  }
  
  private void chectInput() {
    if (this.input.text.length() >= 2) {
      setFilter(this.input.text);
    } else {
      setFilter((String)null);
    } 
  }
  
  public void hide() {
    super.hide();
    setFilter((String)null);
  }
  
  public void show() {
    super.show();
    setfocus(this.input);
    chectInput();
    raise();
  }
}
