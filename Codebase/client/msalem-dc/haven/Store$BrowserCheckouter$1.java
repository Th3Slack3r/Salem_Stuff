package haven;

class Store$BrowserCheckouter$1 extends Button {
   Store$BrowserCheckouter$1(Store.BrowserCheckouter this$1, Coord c, Integer w, Widget parent, String text, boolean var6) {
      super(c, w, parent, text);
      this.this$1 = this$1;
      this.val$reload = var6;
   }

   @Override
   public void click() {
      this.ui.destroy(this.this$1);
      if (this.val$reload) {
         this.this$1.this$0.new Loader();
      } else {
         this.this$1.this$0.new Browser(this.this$1.cat, this.this$1.cart);
      }
   }
}
