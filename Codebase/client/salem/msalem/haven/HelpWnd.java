package haven;

public class HelpWnd extends Window {
   public static final RichText.Foundry fnd = new RichText.Foundry();
   public Indir<Resource> res;
   private Indir<Resource> showing = null;
   private final RichTextBox text;

   public HelpWnd(Coord c, Widget parent, Indir<Resource> res) {
      super(c, new Coord(300, 430), parent, "Help");
      this.res = res;
      this.text = new RichTextBox(Coord.z, new Coord(300, 400), this, "", fnd);
      new Button(new Coord(100, 410), 100, this, "Dismiss") {
         @Override
         public void click() {
            HelpWnd.this.wdgmsg("close", new Object[0]);
         }
      };
   }

   @Override
   public void tick(double dt) {
      super.tick(dt);
      if (this.res != this.showing) {
         try {
            this.text.settext(this.res.get().layer(Resource.pagina).text);
            this.showing = this.res;
         } catch (Loading var4) {
         }
      }
   }

   static {
      fnd.aa = true;
   }
}
