package org.latikai.bots;

import haven.Coord;
import haven.FastMesh;
import haven.Gob;
import haven.Rendered;
import haven.StaticSprite;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class PotState extends Bot {
   List<Gob> pots = new ArrayList<>();
   List<String> contents = new ArrayList<>();
   int index = 0;
   int direction = 1;

   boolean hasNext() {
      return this.index + this.direction >= 0 && this.index + this.direction < this.pots.size();
   }

   Gob next() {
      this.index = this.index + this.direction;
      return this.current();
   }

   Gob current() {
      return this.pots.get(this.index);
   }

   String currentContent() {
      return this.contents.get(this.index);
   }

   void reverse() {
      this.direction *= -1;
   }

   static boolean needsPicking(Gob g) {
      for (Gob.Overlay ol : g.ols) {
         try {
            Class<?> cl = ol.spr.getClass();
            Field espr = cl.getDeclaredField("espr");
            espr.setAccessible(true);
            StaticSprite ss = (StaticSprite)espr.get(ol.spr);

            for (Rendered r : ss.parts) {
               Field resf = r.getClass().getDeclaredField("r");
               FastMesh.ResourceMesh rr = (FastMesh.ResourceMesh)resf.get(r);
               if (rr.res.name.contains("herb") || rr.res.name.contains("pumpkin")) {
                  return true;
               }
            }
         } catch (Exception var12) {
         }
      }

      return false;
   }

   static String getContents(Gob g) {
      try {
         for (Gob.Overlay ol : g.ols) {
            Class<?> cl = ol.spr.getClass();
            Field espr = cl.getDeclaredField("espr");
            espr.setAccessible(true);
            StaticSprite ss = (StaticSprite)espr.get(ol.spr);
            Rendered[] var6 = ss.parts;
            int var7 = var6.length;
            byte var8 = 0;
            if (var8 < var7) {
               Rendered r = var6[var8];
               Field resf = r.getClass().getDeclaredField("r");
               FastMesh.ResourceMesh rr = (FastMesh.ResourceMesh)resf.get(r);
               Coord goloc = g.rc;
               return rr.res.name.substring(rr.res.name.lastIndexOf(47) + 1);
            }
         }
      } catch (Exception var14) {
      }

      return null;
   }
}
