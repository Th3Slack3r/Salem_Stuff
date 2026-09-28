package haven;

import java.awt.Color;

public class Speaking extends GAttrib {
  float zo;
  
  Text text;
  
  static IBox sb = null;
  
  Tex svans;
  
  static final int sx = 3;
  
  final PView.Draw2D fx;
  
  public Speaking(Gob gob, float zo, String text) {
    super(gob);
    this.fx = new PView.Draw2D() {
        public void draw2d(GOut g) {
          if (Speaking.this.gob.sc != null)
            Speaking.this.draw(g, Speaking.this.gob.sc.add(new Coord(Speaking.this.gob.sczu.mul(Speaking.this.zo)))); 
        }
      };
    if (sb == null)
      sb = new IBox("gfx/hud/emote", "tl", "tr", "bl", "br", "el", "er", "et", "eb"); 
    this.svans = Resource.loadtex("gfx/hud/emote/svans");
    this.zo = zo;
    this.text = Text.render(text, Color.BLACK);
  }
  
  public void update(String text) {
    this.text = Text.render(text, Color.BLACK);
  }
  
  public void draw(GOut g, Coord c) {
    Coord sz = this.text.sz();
    if (sz.x < 10)
      sz.x = 10; 
    Coord tl = c.add((new Coord(3, (sb.bsz()).y + sz.y + (this.svans.sz()).y - 1)).inv());
    Coord ftl = tl.add(sb.tloff());
    g.chcolor(Color.WHITE);
    g.frect(ftl, sz);
    sb.draw(g, tl, sz.add(sb.bsz()));
    g.chcolor(Color.BLACK);
    g.image(this.text.tex(), ftl);
    g.chcolor(Color.WHITE);
    g.image(this.svans, c.add(0, -(this.svans.sz()).y));
  }
}
