import haven.Coord;
import haven.GOut;
import haven.RichText;
import haven.Tex;
import haven.TexI;
import haven.Text;
import haven.Utils;
import haven.Widget;
import java.awt.Color;

class null extends Widget {
  null(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
    this.tooltip = RichText.render("Upkeep paid\n\n$i{(Hold silver and Right-Click the Claim Stone to add more.)}", 150, new Object[0]);
  }
  
  public void draw(GOut g) {
    int auth = Landwindow.this.auth;
    int acap = Landwindow.this.acap;
    if (acap > 0) {
      g.chcolor(0, 0, 0, 255);
      g.frect(Coord.z, this.sz);
      g.chcolor(128, 0, 0, 255);
      Coord var4 = this.sz.sub(2, 2);
      var4.x = auth * var4.x / acap;
      g.frect(new Coord(1, 1), var4);
      g.chcolor();
      if (Landwindow.access$000(Landwindow.this) == null) {
        Color color = Landwindow.this.offline ? Color.RED : Color.WHITE;
        Landwindow.access$002(Landwindow.this, (Tex)new TexI(Utils.outline2((Text.render(String.format("%s/%s", new Object[] { Integer.valueOf(auth), Integer.valueOf(acap) }), color)).img, Utils.contrast(color))));
      } 
      g.aimage(Landwindow.access$000(Landwindow.this), this.sz.div(2), 0.5D, 0.5D);
    } 
  }
}
