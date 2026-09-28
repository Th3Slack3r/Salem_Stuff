package haven.minimap;

import java.awt.Color;

public class ConfigMarker {
  public Color color;
  
  public String match;
  
  public boolean ispattern;
  
  public boolean show;
  
  public String text;
  
  public boolean tooltip;
  
  public Marker.Shape shape;
  
  public int order;
  
  public boolean showicon;
  
  public boolean hastext() {
    return (this.text != null && this.text.length() != 0);
  }
}
