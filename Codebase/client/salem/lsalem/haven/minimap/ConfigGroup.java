package haven.minimap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ConfigGroup {
   public Color color;
   public String name;
   public boolean show;
   public List<ConfigMarker> markers = new ArrayList<>();
   public Marker.Shape shape;
   public int order;
   public boolean showicon;
}
