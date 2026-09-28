package haven;

public class FilterWnd extends GameUI.Hidewnd {
   TextEntry input;

   FilterWnd(Widget parent) {
      super(new Coord(120, 200), Coord.z, parent, "Filter");
      this.cbtn.visible = false;
      this.cap = null;
      this.input = new TextEntry(Coord.z, 200, this, "") {
         @Override
         public void changed() {
            FilterWnd.this.chectInput();
         }
      };
      this.pack();
      this.hide();
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
         this.setFilter(this.input.text);
      } else {
         this.setFilter(null);
      }
   }

   @Override
   public void hide() {
      super.hide();
      this.setFilter(null);
   }

   @Override
   public void show() {
      super.show();
      this.setfocus(this.input);
      this.chectInput();
      this.raise();
   }
}
