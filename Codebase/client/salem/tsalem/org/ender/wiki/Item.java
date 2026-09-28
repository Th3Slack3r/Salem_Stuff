package org.ender.wiki;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    builder.append(String.format("<item name=\"%s\" >", new Object[] { this.name.replaceAll("&", "&amp;") }));
    if (this.required != null)
      xml(builder, this.required, "required"); 
    if (this.locations != null)
      xml(builder, this.locations, "locations"); 
    if (this.tech != null)
      xml(builder, this.tech, "tech"); 
    if (this.reqby != null)
      xml(builder, this.reqby, "reqby"); 
    if (this.unlocks != null)
      xml(builder, this.unlocks, "unlocks"); 
    if (this.attreq != null)
      xml(builder, this.attreq, "attreq"); 
    if (this.attgive != null)
      xml(builder, this.attgive, "attgive"); 
    if (this.food != null)
      xml_food(builder); 
    if (this.content != null)
      xml_content(builder); 
    cloth_xml(builder);
    art_xml(builder);
    builder.append("\n</item>");
    return builder.toString();
  }
  
  private void art_xml(StringBuilder builder) {
    if (this.art_profs == null || this.art_profs.length == 0)
      return; 
    String tag = "artifact";
    builder.append(String.format("\n  <%s", new Object[] { "artifact" }));
    builder.append(String.format(" difficulty=\"%d to %d\"", new Object[] { Integer.valueOf(100 - this.art_pmin), Integer.valueOf(100 - this.art_pmax) }));
    builder.append(String.format(" profs=\"%s\"", new Object[] { join(", ", this.art_profs).replaceAll("&", "&amp;") }));
    String bonuses = "";
    boolean first = true;
    for (Map.Entry<String, Integer> entry : this.art_bonuses.entrySet()) {
      if (!first)
        bonuses = bonuses + ", "; 
      bonuses = bonuses + String.format("%s=%d", new Object[] { entry.getKey(), entry.getValue() });
      first = false;
    } 
    builder.append(String.format(" bonuses=\"%s\"", new Object[] { bonuses.replaceAll("&", "&amp;") }));
    builder.append(String.format(" />", new Object[0]));
  }
  
  private void cloth_xml(StringBuilder builder) {
    if (this.cloth_slots == 0)
      return; 
    String tag = "cloth";
    builder.append(String.format("\n  <%s", new Object[] { "cloth" }));
    builder.append(String.format(" slots=\"%d\"", new Object[] { Integer.valueOf(this.cloth_slots) }));
    builder.append(String.format(" />", new Object[0]));
  }
  
  private void xml_content(StringBuilder builder) {
    String tag = "content";
    builder.append(String.format("\n  <%s><![CDATA[%s]]></%s>", new Object[] { "content", this.content, "content" }));
  }
  
  private void xml_food(StringBuilder builder) {
    String tag = "food";
    builder.append(String.format("\n  <%s", new Object[] { "food" }));
    for (Map.Entry<String, Float[]> e : this.food.entrySet()) {
      Float[] vals = e.getValue();
      builder.append(String.format(" %s=\"%s %s %s %s\"", new Object[] { e.getKey(), vals[0], vals[1], vals[2], vals[3] }));
    } 
    Iterator<Map.Entry<String, Integer[]>> itr = this.food_reduce.entrySet().iterator();
    int i;
    for (i = 0; i < this.food_reduce.size(); i++) {
      Map.Entry<String, Integer[]> entry = itr.next();
      builder.append(String.format(" FoodReduce%d=\"%s %s %s\"", new Object[] { Integer.valueOf(i + 1), entry.getKey(), ((Integer[])entry.getValue())[0], ((Integer[])entry.getValue())[1] }));
    } 
    itr = this.food_restore.entrySet().iterator();
    for (i = 0; i < this.food_restore.size(); i++) {
      Map.Entry<String, Integer[]> entry = itr.next();
      builder.append(String.format(" FoodRestore%d=\"%s %s %s\"", new Object[] { Integer.valueOf(i + 1), entry.getKey(), ((Integer[])entry.getValue())[0], ((Integer[])entry.getValue())[1] }));
    } 
    builder.append(String.format(" full=\"%d\" uses=\"%d\"", new Object[] { Integer.valueOf(this.food_full), Integer.valueOf(this.food_uses) }));
    builder.append(String.format(" />", new Object[0]));
  }
  
  private void xml(StringBuilder builder, Map<String, Integer> map, String tag) {
    builder.append(String.format("\n  <%s", new Object[] { tag }));
    for (Map.Entry<String, Integer> e : map.entrySet()) {
      builder.append(String.format(" %s=\"%d\"", new Object[] { e.getKey(), e.getValue() }));
    } 
    builder.append(String.format(" />", new Object[0]));
  }
  
  private void xml(StringBuilder builder, Set<String> list, String tag) {
    for (String name : list) {
      builder.append(String.format("\n  <%s name=\"%s>\" />", new Object[] { tag, name.replaceAll("&", "&amp;") }));
    } 
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("Wiki Item '%s'", new Object[] { this.name }));
    if (this.locations != null)
      append(builder, this.locations, "Locations"); 
    if (this.required != null)
      append(builder, this.required, "Requires"); 
    if (this.reqby != null)
      append(builder, this.reqby, "Used by"); 
    if (this.tech != null)
      append(builder, this.tech, "Skills needed"); 
    if (this.unlocks != null)
      append(builder, this.unlocks, "Unlocks"); 
    if (this.attreq != null)
      append(builder, this.attreq, "Profs required"); 
    if (this.attgive != null)
      append(builder, this.attgive, "Profs gain"); 
    return builder.toString();
  }
  
  private void append(StringBuilder builder, Map<String, Integer> props, String msg) {
    builder.append(String.format("\n\t%s: ", new Object[] { msg }));
    String c = "";
    for (Map.Entry<String, Integer> e : props.entrySet()) {
      builder.append(String.format("%s'%s:%d'", new Object[] { c, e.getKey(), e.getValue() }));
      c = ", ";
    } 
    builder.append(';');
  }
  
  private void append(StringBuilder builder, Set<String> list, String msg) {
    builder.append(String.format("\n\t%s: ", new Object[] { msg }));
    String c = "";
    for (String name : list) {
      builder.append(String.format("%s'%s'", new Object[] { c, name }));
      c = ", ";
    } 
    builder.append(';');
  }
  
  public void setClothing(int slots) {
    this.cloth_slots = slots;
    if (slots == 0)
      return; 
  }
  
  public void setArtifact(String difficulty, String[] profs, Map<String, Integer> bonuses) {
    String[] ds = difficulty.split(" to ");
    try {
      this.art_pmin = 100 - Integer.parseInt(ds[0]);
      this.art_pmax = 100 - Integer.parseInt(ds[1]);
    } catch (Exception exception) {}
    this.art_profs = profs;
    this.art_bonuses = bonuses;
  }
  
  String join(String separator, String[] s) {
    int k = s.length;
    if (k == 0)
      return null; 
    StringBuilder out = new StringBuilder();
    out.append(s[0]);
    for (int x = 1; x < k; x++)
      out.append(separator).append(s[x]); 
    return out.toString();
  }
  
  public Object[] getArtBonuses() {
    if (this.art_bonuses == null)
      return new Object[] { Integer.valueOf(0) }; 
    Object[] ret = new Object[1 + this.art_bonuses.size() * 2];
    int i = 0;
    ret[i++] = Integer.valueOf(0);
    for (Map.Entry<String, Integer> entry : this.art_bonuses.entrySet()) {
      ret[i++] = entry.getKey();
      ret[i++] = entry.getValue();
    } 
    return ret;
  }
}
