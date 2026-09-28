package haven.minimap;

import java.awt.Color;

public class MarkerTemplate {
  public final Color color;
  
  public final Marker.Shape shape;
  
  public final boolean visible;
  
  public final String tooltip;
  
  public final boolean showtooltip;
  
  public final int order;
  
  public final boolean showicon;
  
  public MarkerTemplate(Color color, boolean visible, String tooltip, boolean showtooltip, Marker.Shape shape, int order, boolean showicon) {
    this.color = color;
    this.visible = visible;
    this.tooltip = tooltip;
    this.showtooltip = showtooltip;
    this.shape = shape;
    this.order = order;
    this.showicon = showicon;
  }
}
