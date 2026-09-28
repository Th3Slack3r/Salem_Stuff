package haven;

public class HelpWnd extends Window {
  private final RichTextBox text;
  
  private Indir<Resource> showing = null;
  
  public Indir<Resource> res;
  
  public static final RichText.Foundry fnd = new RichText.Foundry(new Object[0]);
  
  static {
    fnd.aa = true;
  }
  
  public HelpWnd(Coord c, Widget parent, Indir<Resource> res) {
    super(c, new Coord(300, 430), parent, "Help");
    this.res = res;
    this.text = new RichTextBox(Coord.z, new Coord(300, 400), this, "", fnd);
    new Button(new Coord(100, 410), Integer.valueOf(100), this, "Dismiss") {
        public void click() {
          HelpWnd.this.wdgmsg("close", new Object[0]);
        }
      };
  }
  
  public void tick(double dt) {
    super.tick(dt);
    if (this.res != this.showing)
      try {
        this.text.settext(((Resource.Pagina)((Resource)this.res.get()).layer((Class)Resource.pagina)).text);
        this.showing = this.res;
      } catch (Loading loading) {} 
  }
}
