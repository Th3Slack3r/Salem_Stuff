package haven.minimap;

import haven.Config;
import haven.Gob;
import haven.GobIcon;
import haven.UI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MarkerFactory {
  private final Set<String> unknownNameCache;
  
  private final Map<String, MarkerTemplate> templateCache;
  
  private final Map<String, ConfigMarker> matches;
  
  private final List<ConfigMarker> patterns;
  
  public MarkerFactory() {
    this(new RadarConfig());
  }
  
  public MarkerFactory(RadarConfig rc) {
    this.unknownNameCache = new HashSet<>();
    this.templateCache = new HashMap<>();
    this.matches = new HashMap<>();
    this.patterns = new ArrayList<>();
    setConfig(rc);
  }
  
  public Marker makeMarker(String resname, Gob gob) {
    MarkerTemplate mt = findTemplate(resname);
    if (mt == null && Config.radar_icons && gob.getattr(GobIcon.class) != null) {
      mt = new MarkerTemplate(Color.WHITE, true, resname, true, Marker.Shape.CIRCLE, 0, true);
      this.templateCache.put(resname, mt);
    } 
    if (mt != null) {
      Marker m = new Marker(resname, gob, mt);
      try {
        if (gob.id == UI.instance.gui.map.plgob)
          m.setOrder(-100); 
      } catch (NullPointerException nullPointerException) {}
      if (m.name.contains("orka"))
        m.setOrder(100); 
      return m;
    } 
    return null;
  }
  
  private MarkerTemplate findTemplate(String resname) {
    if (this.unknownNameCache.contains(resname))
      return null; 
    if (this.templateCache.containsKey(resname))
      return this.templateCache.get(resname); 
    ConfigMarker marker = this.matches.get(resname);
    if (marker == null)
      for (ConfigMarker cm : this.patterns) {
        if (resname.matches(cm.match)) {
          marker = cm;
          break;
        } 
      }  
    if (marker != null) {
      MarkerTemplate template = createTemplate(resname, marker);
      this.templateCache.put(resname, template);
      return template;
    } 
    this.unknownNameCache.add(resname);
    return null;
  }
  
  private MarkerTemplate createTemplate(String resname, ConfigMarker cm) {
    return new MarkerTemplate(cm.color, cm.show, cm.hastext() ? cm.text : resname, cm.tooltip, cm.shape, cm.order, cm.showicon);
  }
  
  public void setConfig(RadarConfig config) {
    this.unknownNameCache.clear();
    this.templateCache.clear();
    this.matches.clear();
    this.patterns.clear();
    for (ConfigGroup group : config.getGroups()) {
      for (ConfigMarker marker : group.markers) {
        if (marker.ispattern) {
          this.patterns.add(marker);
          continue;
        } 
        this.matches.put(marker.match, marker);
      } 
    } 
  }
}
