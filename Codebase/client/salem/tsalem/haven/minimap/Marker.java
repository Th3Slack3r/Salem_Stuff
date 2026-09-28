package haven.minimap;

import haven.Config;
import haven.Coord;
import haven.Coord3f;
import haven.GOut;
import haven.Gob;
import haven.GobIcon;
import haven.Loading;
import haven.MCache;
import haven.Tex;
import java.awt.Color;

public class Marker implements Comparable {
  public final String name;
  
  public final Gob gob;
  
  public final MarkerTemplate template;
  
  public int order;
  
  public int compareTo(Object that) {
    return this.order - ((Marker)that).order;
  }
  
  private boolean override = false;
  
  private String override_name;
  
  private Color override_color;
  
  public enum Shape {
    CIRCLE(10),
    TRIANGLE(13),
    TRIANGLED(13),
    DIAMOND(13),
    SQUARE(9),
    PENTAGON(13);
    
    public int sz;
    
    public Tex tex;
    
    Shape(int sz) {
      this.sz = sz;
    }
    
    public static Shape get(String val) {
      if (val.equals("circle"))
        return CIRCLE; 
      if (val.equals("up"))
        return TRIANGLE; 
      if (val.equals("down"))
        return TRIANGLED; 
      if (val.equals("diamond"))
        return DIAMOND; 
      if (val.equals("square"))
        return SQUARE; 
      if (val.equals("pentagon"))
        return PENTAGON; 
      return CIRCLE;
    }
  }
  
  public Marker(String name, Gob gob, MarkerTemplate template) {
    this.name = name;
    this.gob = gob;
    this.template = template;
    this.order = template.order;
    if (template.shape.tex == null)
      template.shape.tex = Utils.generateMarkerTex(template.shape); 
  }
  
  public void setOrder(int order) {
    this.order = order;
  }
  
  public boolean hit(Coord c) {
    Coord3f ptc3f = this.gob.getc();
    if (ptc3f == null)
      return false; 
    Coord p = new Coord((int)ptc3f.x, (int)ptc3f.y);
    int radius = 4;
    return ((c.x - p.x) * (c.x - p.x) + (c.y - p.y) * (c.y - p.y) < 16 * MCache.tilesz.x * MCache.tilesz.y);
  }
  
  public void override(String name, Color c) {
    this.override = true;
    this.override_name = name;
    this.override_color = c;
  }
  
  public String getTooltip() {
    if (this.override)
      return this.override_name; 
    return this.template.tooltip;
  }
  
  public void draw(GOut g, Coord c) {
    draw(g, c, 1.0D);
  }
  
  public void draw(GOut g, Coord c, double scale) {
    Coord3f ptc3f = this.gob.getc();
    if (ptc3f == null)
      return; 
    Coord ptc = new Coord((int)ptc3f.x, (int)ptc3f.y);
    ptc = ptc.div(MCache.tilesz).add(c);
    if (Config.radar_icons)
      try {
        GobIcon icon = (GobIcon)this.gob.getattr(GobIcon.class);
        if (icon != null && this.template.showicon) {
          Tex tex1 = icon.tex();
          g.image(tex1, ptc.sub(tex1.sz().div(2)), tex1.sz().div(scale));
          return;
        } 
      } catch (Loading loading) {} 
    g.chcolor(this.template.color);
    if (this.override)
      g.chcolor(this.override_color); 
    Tex tex = this.template.shape.tex;
    g.image(tex, ptc.sub(tex.sz().div(2)), tex.sz().div(scale));
    g.chcolor();
  }
}
