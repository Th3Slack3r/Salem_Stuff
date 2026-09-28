package org.ender.wiki;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Item {
   public String name;
   public Set<String> required;
   public Set<String> locations;
   public Set<String> tech;
   public Set<String> reqby;
   public Set<String> unlocks;
   public Map<String, Integer> attreq;
   public Map<String, Integer> attgive;
   public String content;
   public Map<String, Integer[]> food_reduce;
   public Map<String, Integer[]> food_restore;
   public Map<String, Float[]> food;
   public int food_full = 0;
   public int food_uses = 1;
   public int cloth_slots = 0;
   public int cloth_pmin = 0;
   public int cloth_pmax = 0;
   public String[] cloth_profs;
   public int art_pmin;
   public int art_pmax;
   public String[] art_profs;
   public Map<String, Integer> art_bonuses;

   public String toXML() {
      StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
      builder.append(String.format("<item name=\"%s\" >", this.name.replaceAll("&", "&amp;")));
      if (this.required != null) {
         this.xml(builder, this.required, "required");
      }

      if (this.locations != null) {
         this.xml(builder, this.locations, "locations");
      }

      if (this.tech != null) {
         this.xml(builder, this.tech, "tech");
      }

      if (this.reqby != null) {
         this.xml(builder, this.reqby, "reqby");
      }

      if (this.unlocks != null) {
         this.xml(builder, this.unlocks, "unlocks");
      }

      if (this.attreq != null) {
         this.xml(builder, this.attreq, "attreq");
      }

      if (this.attgive != null) {
         this.xml(builder, this.attgive, "attgive");
      }

      if (this.food != null) {
         this.xml_food(builder);
      }

      if (this.content != null) {
         this.xml_content(builder);
      }

      this.cloth_xml(builder);
      this.art_xml(builder);
      builder.append("\n</item>");
      return builder.toString();
   }

   private void art_xml(StringBuilder builder) {
      if (this.art_profs != null && this.art_profs.length != 0) {
         String tag = "artifact";
         builder.append(String.format("\n  <%s", tag));
         builder.append(String.format(" difficulty=\"%d to %d\"", 100 - this.art_pmin, 100 - this.art_pmax));
         builder.append(String.format(" profs=\"%s\"", this.join(", ", this.art_profs).replaceAll("&", "&amp;")));
         String bonuses = "";
         boolean first = true;

         for (Entry<String, Integer> entry : this.art_bonuses.entrySet()) {
            if (!first) {
               bonuses = bonuses + ", ";
            }

            bonuses = bonuses + String.format("%s=%d", entry.getKey(), entry.getValue());
            first = false;
         }

         builder.append(String.format(" bonuses=\"%s\"", bonuses.replaceAll("&", "&amp;")));
         builder.append(String.format(" />"));
      }
   }

   private void cloth_xml(StringBuilder builder) {
      if (this.cloth_slots != 0) {
         String tag = "cloth";
         builder.append(String.format("\n  <%s", tag));
         builder.append(String.format(" slots=\"%d\"", this.cloth_slots));
         builder.append(String.format(" />"));
      }
   }

   private void xml_content(StringBuilder builder) {
      String tag = "content";
      builder.append(String.format("\n  <%s><![CDATA[%s]]></%s>", tag, this.content, tag));
   }

   private void xml_food(StringBuilder builder) {
      String tag = "food";
      builder.append(String.format("\n  <%s", tag));

      for (Entry<String, Float[]> e : this.food.entrySet()) {
         Float[] vals = e.getValue();
         builder.append(String.format(" %s=\"%s %s %s %s\"", e.getKey(), vals[0], vals[1], vals[2], vals[3]));
      }

      Iterator<Entry<String, Integer[]>> itr = this.food_reduce.entrySet().iterator();

      for (int i = 0; i < this.food_reduce.size(); i++) {
         Entry<String, Integer[]> entry = itr.next();
         builder.append(String.format(" FoodReduce%d=\"%s %s %s\"", i + 1, entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
      }

      itr = this.food_restore.entrySet().iterator();

      for (int i = 0; i < this.food_restore.size(); i++) {
         Entry<String, Integer[]> entry = itr.next();
         builder.append(String.format(" FoodRestore%d=\"%s %s %s\"", i + 1, entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
      }

      builder.append(String.format(" full=\"%d\" uses=\"%d\"", this.food_full, this.food_uses));
      builder.append(String.format(" />"));
   }

   private void xml(StringBuilder builder, Map<String, Integer> map, String tag) {
      builder.append(String.format("\n  <%s", tag));

      for (Entry<String, Integer> e : map.entrySet()) {
         builder.append(String.format(" %s=\"%d\"", e.getKey(), e.getValue()));
      }

      builder.append(String.format(" />"));
   }

   private void xml(StringBuilder builder, Set<String> list, String tag) {
      for (String name : list) {
         builder.append(String.format("\n  <%s name=\"%s>\" />", tag, name.replaceAll("&", "&amp;")));
      }
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(String.format("Wiki Item '%s'", this.name));
      if (this.locations != null) {
         this.append(builder, this.locations, "Locations");
      }

      if (this.required != null) {
         this.append(builder, this.required, "Requires");
      }

      if (this.reqby != null) {
         this.append(builder, this.reqby, "Used by");
      }

      if (this.tech != null) {
         this.append(builder, this.tech, "Skills needed");
      }

      if (this.unlocks != null) {
         this.append(builder, this.unlocks, "Unlocks");
      }

      if (this.attreq != null) {
         this.append(builder, this.attreq, "Profs required");
      }

      if (this.attgive != null) {
         this.append(builder, this.attgive, "Profs gain");
      }

      return builder.toString();
   }

   private void append(StringBuilder builder, Map<String, Integer> props, String msg) {
      builder.append(String.format("\n\t%s: ", msg));
      String c = "";

      for (Entry<String, Integer> e : props.entrySet()) {
         builder.append(String.format("%s'%s:%d'", c, e.getKey(), e.getValue()));
         c = ", ";
      }

      builder.append(';');
   }

   private void append(StringBuilder builder, Set<String> list, String msg) {
      builder.append(String.format("\n\t%s: ", msg));
      String c = "";

      for (String name : list) {
         builder.append(String.format("%s'%s'", c, name));
         c = ", ";
      }

      builder.append(';');
   }

   public void setClothing(int slots) {
      this.cloth_slots = slots;
      if (slots != 0) {
         ;
      }
   }

   public void setArtifact(String difficulty, String[] profs, Map<String, Integer> bonuses) {
      String[] ds = difficulty.split(" to ");

      try {
         this.art_pmin = 100 - Integer.parseInt(ds[0]);
         this.art_pmax = 100 - Integer.parseInt(ds[1]);
      } catch (Exception var6) {
      }

      this.art_profs = profs;
      this.art_bonuses = bonuses;
   }

   String join(String separator, String[] s) {
      int k = s.length;
      if (k == 0) {
         return null;
      } else {
         StringBuilder out = new StringBuilder();
         out.append(s[0]);

         for (int x = 1; x < k; x++) {
            out.append(separator).append(s[x]);
         }

         return out.toString();
      }
   }

   public Object[] getArtBonuses() {
      if (this.art_bonuses == null) {
         return new Object[]{0};
      } else {
         Object[] ret = new Object[1 + this.art_bonuses.size() * 2];
         int i = 0;
         ret[i++] = 0;

         for (Entry<String, Integer> entry : this.art_bonuses.entrySet()) {
            ret[i++] = entry.getKey();
            ret[i++] = entry.getValue();
         }

         return ret;
      }
   }
}
