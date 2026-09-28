package haven.minimap;

import haven.BuddyWnd;
import haven.Config;
import haven.GAttrib;
import haven.GLState;
import haven.Gob;
import haven.Indir;
import haven.KinInfo;
import haven.Material;
import haven.OptWnd2;
import haven.ResDrawable;
import haven.Resource;
import haven.Session;
import haven.StaticSprite;
import haven.Utils;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Radar {
  private static Resource borkasound = Resource.load("sfx/hud/reset");
  
  private final MarkerFactory factory;
  
  private final Map<Long, Marker> markers = new HashMap<>();
  
  private final Map<Long, GobRes> undefined = new HashMap<>();
  
  private final Object markerLock = new Object();
  
  public void add(Gob g, Indir<Resource> res) {
    synchronized (this.markerLock) {
      if (contains(g))
        return; 
      boolean added = false;
      try {
        Resource r = (Resource)res.get();
        if (r != null && r.name != null && r.name.length() != 0) {
          add(r.name, g);
          added = true;
        } 
      } catch (haven.Session.LoadingIndir loadingIndir) {
      
      } catch (haven.Resource.Loading loading) {}
      if (!added)
        this.undefined.put(Long.valueOf(g.id), new GobRes(g, res)); 
    } 
  }
  
  public void update() {
    synchronized (this.markerLock) {
      checkUndefined();
    } 
  }
  
  public static Color npcColour = new Color(Config.npc_colour1, Config.npc_colour2, Config.npc_colour3);
  
  public static long plGob = -1L;
  
  Color emptyLeantoColour;
  
  public Radar() {
    this.emptyLeantoColour = null;
    RadarConfig rc = new RadarConfig();
    this.factory = new MarkerFactory(rc);
    OptWnd2.setRadarInfo(rc, this.factory);
  }
  
  private void add(String name, Gob gob) {
    Marker m = this.factory.makeMarker(name, gob);
    if (m != null) {
      KinInfo ki = (KinInfo)gob.getattr(KinInfo.class);
      if (ki != null) {
        m.override(ki.name, BuddyWnd.gc[ki.group]);
      } else if (!gob.hasIcon.get() && gob.id != plGob) {
        if (name != null && name.toLowerCase().contains("borka")) {
          m.override("NPC", npcColour);
          m.order = 99;
        } 
      } 
      if (name != null && name.toLowerCase().contains("leanto")) {
        if (this.emptyLeantoColour == null)
          this.emptyLeantoColour = new Color(m.template.color.darker().darker().getRGB()); 
        ResDrawable rd = (ResDrawable)gob.getattr(ResDrawable.class);
        if (rd.spr != null && ((StaticSprite)rd.spr).parts.length <= 1) {
          m.override("Leano", this.emptyLeantoColour);
        } else {
          m.order = 98;
        } 
      } 
      this.markers.put(Long.valueOf(gob.id), m);
      gob.setattr(new GobBlink(gob, m));
    } 
  }
  
  private void checkUndefined() {
    if (this.undefined.size() == 0)
      return; 
    GobRes[] gs = (GobRes[])this.undefined.values().toArray((Object[])new GobRes[this.undefined.size()]);
    for (GobRes gr : gs) {
      try {
        Resource r = (Resource)gr.res.get();
        if (r != null && r.name != null && r.name.length() != 0) {
          add(r.name, gr.gob);
          this.undefined.remove(Long.valueOf(gr.gob.id));
        } 
      } catch (haven.Session.LoadingIndir loadingIndir) {
      
      } catch (haven.Resource.Loading loading) {}
    } 
  }
  
  private boolean contains(Gob g) {
    return (this.undefined.containsKey(Long.valueOf(g.id)) || this.markers.containsKey(Long.valueOf(g.id)));
  }
  
  public Marker[] getMarkers() {
    synchronized (this.markerLock) {
      checkUndefined();
      Marker[] collection = (Marker[])this.markers.values().toArray((Object[])new Marker[this.markers.size()]);
      Arrays.sort((Object[])collection);
      return collection;
    } 
  }
  
  public Marker getMarker(Long gobid) {
    synchronized (this.markerLock) {
      checkUndefined();
      return this.markers.get(gobid);
    } 
  }
  
  public Marker get(Gob gob) {
    return this.markers.get(Long.valueOf(gob.id));
  }
  
  public void remove(Long gobid) {
    synchronized (this.markerLock) {
      this.markers.remove(gobid);
      this.undefined.remove(gobid);
    } 
  }
  
  public void reload() {
    synchronized (this.markerLock) {
      this.undefined.clear();
      RadarConfig rc = new RadarConfig();
      OptWnd2.setRadarInfo(rc, this.factory);
      this.factory.setConfig(rc);
      Marker[] ms = (Marker[])this.markers.values().toArray((Object[])new Marker[this.markers.size()]);
      this.markers.clear();
      for (Marker m : ms)
        add(m.name, m.gob); 
    } 
  }
  
  public void refresh(RadarConfig rc) {
    synchronized (this.markerLock) {
      this.undefined.clear();
      OptWnd2.setRadarInfo(rc, this.factory);
      this.factory.setConfig(rc);
      Marker[] ms = (Marker[])this.markers.values().toArray((Object[])new Marker[this.markers.size()]);
      this.markers.clear();
      for (Marker m : ms)
        add(m.name, m.gob); 
    } 
  }
  
  private static class GobRes {
    public final Gob gob;
    
    public final Indir<Resource> res;
    
    public GobRes(Gob gob, Indir<Resource> res) {
      this.gob = gob;
      this.res = res;
    }
  }
  
  public static class GobBlink extends GAttrib {
    private final Marker marker;
    
    Material.Colors fx;
    
    int time = 0;
    
    public GobBlink(Gob gob, Marker marker) {
      super(gob);
      this.marker = marker;
      Color c = new Color(255, 100, 100, 100);
      this.fx = new Material.Colors();
      this.fx.amb = Utils.c2fa(c);
      this.fx.dif = Utils.c2fa(c);
      this.fx.emi = Utils.c2fa(c);
    }
    
    public void ctick(int dt) {
      int max = 2000;
      this.time = (this.time + dt) % 2000;
      float a = this.time / 2000.0F;
      if (a > 0.6F) {
        a = 0.0F;
      } else if (a > 0.3F) {
        a = 2.0F - a / 0.3F;
      } else {
        a /= 0.3F;
      } 
      this.fx.amb[3] = a;
      this.fx.dif[3] = a;
      this.fx.emi[3] = a;
    }
    
    public GLState getfx() {
      return (GLState)this.fx;
    }
    
    public boolean visible() {
      return this.marker.template.visible;
    }
  }
}
