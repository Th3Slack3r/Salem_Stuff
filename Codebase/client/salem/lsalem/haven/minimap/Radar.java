package haven.minimap;

import haven.BuddyWnd;
import haven.GAttrib;
import haven.GLState;
import haven.Gob;
import haven.Indir;
import haven.KinInfo;
import haven.Material;
import haven.OptWnd2;
import haven.Resource;
import haven.Session;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Radar {
   private static Resource borkasound = Resource.load("sfx/hud/reset");
   private final MarkerFactory factory;
   private final Map<Long, Marker> markers = new HashMap<>();
   private final Map<Long, Radar.GobRes> undefined = new HashMap<>();
   private final Object markerLock = new Object();

   public Radar() {
      RadarConfig rc = new RadarConfig();
      this.factory = new MarkerFactory(rc);
      OptWnd2.setRadarInfo(rc, this.factory);
   }

   public void add(Gob g, Indir<Resource> res) {
      synchronized (this.markerLock) {
         if (!this.contains(g)) {
            boolean added = false;

            try {
               Resource r = res.get();
               if (r != null && r.name != null && r.name.length() != 0) {
                  this.add(r.name, g);
                  added = true;
               }
            } catch (Session.LoadingIndir var7) {
            } catch (Resource.Loading var8) {
            }

            if (!added) {
               this.undefined.put(g.id, new Radar.GobRes(g, res));
            }
         }
      }
   }

   public void update() {
      synchronized (this.markerLock) {
         this.checkUndefined();
      }
   }

   private void add(String name, Gob gob) {
      Marker m = this.factory.makeMarker(name, gob);
      if (m != null) {
         KinInfo ki = gob.getattr(KinInfo.class);
         if (ki != null) {
            m.override(ki.name, BuddyWnd.gc[ki.group]);
         }

         this.markers.put(gob.id, m);
         gob.setattr(new Radar.GobBlink(gob, m));
      }
   }

   private void checkUndefined() {
      if (this.undefined.size() != 0) {
         Radar.GobRes[] gs = this.undefined.values().toArray(new Radar.GobRes[this.undefined.size()]);

         for (Radar.GobRes gr : gs) {
            try {
               Resource r = gr.res.get();
               if (r != null && r.name != null && r.name.length() != 0) {
                  this.add(r.name, gr.gob);
                  this.undefined.remove(gr.gob.id);
               }
            } catch (Session.LoadingIndir var7) {
            } catch (Resource.Loading var8) {
            }
         }
      }
   }

   private boolean contains(Gob g) {
      return this.undefined.containsKey(g.id) || this.markers.containsKey(g.id);
   }

   public Marker[] getMarkers() {
      synchronized (this.markerLock) {
         this.checkUndefined();
         Marker[] collection = this.markers.values().toArray(new Marker[this.markers.size()]);
         Arrays.sort((Object[])collection);
         return collection;
      }
   }

   public Marker getMarker(Long gobid) {
      synchronized (this.markerLock) {
         this.checkUndefined();
         return this.markers.get(gobid);
      }
   }

   public Marker get(Gob gob) {
      return this.markers.get(gob.id);
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
         Marker[] ms = this.markers.values().toArray(new Marker[this.markers.size()]);
         this.markers.clear();

         for (Marker m : ms) {
            this.add(m.name, m.gob);
         }
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
         this.fx.amb = haven.Utils.c2fa(c);
         this.fx.dif = haven.Utils.c2fa(c);
         this.fx.emi = haven.Utils.c2fa(c);
      }

      @Override
      public void ctick(int dt) {
         int max = 2000;
         this.time = (this.time + dt) % max;
         float a = (float)this.time / max;
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
         return this.fx;
      }

      public boolean visible() {
         return this.marker.template.visible;
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
}
