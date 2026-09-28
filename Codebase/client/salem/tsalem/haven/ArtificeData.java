package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ArtificeData implements ItemData.ITipData {
  public int pmax;
  
  public int pmin;
  
  public String[] profs;
  
  public String[] attrs;
  
  public int[] vals;
  
  public ArtificeData() {}
  
  public ArtificeData(ItemInfo info) {
    this.pmin = (int)(100.0D * Reflect.getFieldValueDouble(info, "pmin"));
    this.pmax = (int)(100.0D * Reflect.getFieldValueDouble(info, "pmax"));
    this.profs = (String[])Reflect.getFieldValue(info, "attrs");
    List<ItemInfo> sub = (List<ItemInfo>)Reflect.getFieldValue(info, "sub");
    info = sub.get(0);
    this.attrs = (String[])Reflect.getFieldValue(info, "attrs");
    this.vals = (int[])Reflect.getFieldValue(info, "vals");
  }
  
  public ItemInfo.Tip create() {
    Resource res = Resource.load("ui/tt/slot");
    if (res == null)
      return null; 
    ItemInfo.InfoFactory f = ((Resource.CodeEntry)res.<Resource.CodeEntry>layer(Resource.CodeEntry.class)).<ItemInfo.InfoFactory>get(ItemInfo.InfoFactory.class);
    Session sess = UI.instance.sess;
    int rid = sess.getresid("ui/tt/dattr");
    if (rid == 0)
      return null; 
    Object[] bonuses = new Object[1 + this.attrs.length + this.vals.length];
    bonuses[0] = Integer.valueOf(rid);
    for (int k = 0; k < this.attrs.length; k++) {
      bonuses[1 + 2 * k] = this.attrs[k];
      bonuses[2 + 2 * k] = Integer.valueOf(this.vals[k]);
    } 
    Object[] args = new Object[4 + this.profs.length];
    int i = 0;
    args[i++] = Integer.valueOf(0);
    args[i++] = Integer.valueOf(this.pmin);
    args[i++] = Integer.valueOf(this.pmax);
    for (String prof : this.profs)
      args[i++] = prof; 
    (new Object[1])[0] = bonuses;
    args[i] = new Object[1];
    return (ItemInfo.Tip)f.build(sess, args);
  }
  
  public static class DataAdapter extends TypeAdapter<ArtificeData> {
    public void write(JsonWriter writer, ArtificeData data) throws IOException {
      writer.beginObject();
      writer.name("pmin").value(data.pmin);
      writer.name("pmax").value(data.pmax);
      writer.name("profs");
      writer.beginArray();
      for (String prof : data.profs)
        writer.value(prof); 
      writer.endArray();
      writer.name("bonuses");
      writer.beginObject();
      int n = data.attrs.length;
      for (int i = 0; i < n; i++)
        writer.name(data.attrs[i]).value(data.vals[i]); 
      writer.endObject();
      writer.endObject();
    }
    
    public ArtificeData read(JsonReader reader) throws IOException {
      ArtificeData data = new ArtificeData();
      reader.beginObject();
      while (reader.hasNext()) {
        String name = reader.nextName();
        if (name.equals("pmin")) {
          data.pmin = reader.nextInt();
          continue;
        } 
        if (name.equals("pmax")) {
          data.pmax = reader.nextInt();
          continue;
        } 
        if (name.equals("profs")) {
          data.profs = parseArray(reader);
          continue;
        } 
        if (name.equals("bonuses"))
          parseObject(reader, data); 
      } 
      reader.endObject();
      return data;
    }
    
    private void parseObject(JsonReader reader, ArtificeData data) throws IOException {
      List<String> names = new LinkedList<>();
      List<Integer> vals = new LinkedList<>();
      reader.beginObject();
      while (reader.hasNext()) {
        names.add(reader.nextName());
        vals.add(Integer.valueOf(reader.nextInt()));
      } 
      reader.endObject();
      data.attrs = names.<String>toArray(new String[names.size()]);
      data.vals = new int[vals.size()];
      for (int i = 0; i < data.vals.length; i++)
        data.vals[i] = ((Integer)vals.get(i)).intValue(); 
    }
    
    private String[] parseArray(JsonReader reader) throws IOException {
      List<String> values = new LinkedList<>();
      reader.beginArray();
      while (reader.hasNext())
        values.add(reader.nextString()); 
      reader.endArray();
      return values.<String>toArray(new String[values.size()]);
    }
  }
}
