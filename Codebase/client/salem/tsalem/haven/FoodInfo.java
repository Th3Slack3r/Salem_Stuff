package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FoodInfo extends ItemInfo.Tip {
  public final int[] tempers;
  
  public FoodInfo(ItemInfo.Owner owner, int[] tempers) {
    super(owner);
    this.tempers = tempers;
  }
  
  public BufferedImage longtip() {
    StringBuilder buf = new StringBuilder();
    buf.append("Heals: ");
    for (int i = 0; i < 4; i++) {
      if (i > 0)
        buf.append(", "); 
      buf.append(String.format("$col[%s]{%s}", new Object[] { Tempers.tcolors[i], Utils.fpformat(this.tempers[i], 3, 1) }));
    } 
    return (RichText.render(buf.toString(), 0, new Object[0])).img;
  }
  
  public static class Data implements ItemData.ITipData {
    public int[] tempers;
    
    public Data() {}
    
    public Data(FoodInfo info, double mult) {
      if (mult == 1.0D) {
        this.tempers = info.tempers;
      } else {
        this.tempers = fixMult(mult, info.tempers);
      } 
    }
    
    public static int[] fixMult(double mult, int[] from) {
      int[] res = new int[from.length];
      for (int i = 0; i < from.length; i++) {
        double a = from[i] / 100.0D * mult;
        res[i] = (int)(100L * Math.round(a));
      } 
      return res;
    }
    
    public ItemInfo.Tip create() {
      return new FoodInfo(null, this.tempers);
    }
    
    public static class DataAdapter extends TypeAdapter<Data> {
      public FoodInfo.Data read(JsonReader reader) throws IOException {
        FoodInfo.Data data = new FoodInfo.Data();
        List<Integer> vals = new LinkedList<>();
        reader.beginArray();
        while (reader.hasNext())
          vals.add(Integer.valueOf(reader.nextInt())); 
        reader.endArray();
        data.tempers = new int[vals.size()];
        for (int i = 0; i < data.tempers.length; i++)
          data.tempers[i] = ((Integer)vals.get(i)).intValue(); 
        return data;
      }
      
      public void write(JsonWriter writer, FoodInfo.Data data) throws IOException {
        writer.beginArray();
        int n = data.tempers.length;
        for (int i = 0; i < n; i++)
          writer.value(data.tempers[i]); 
        writer.endArray();
      }
    }
  }
}
