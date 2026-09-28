package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariantsInfo extends ItemInfo.Tip {
  public Map<String, float[]> variants;
  
  public VariantsInfo(ItemInfo.Owner owner, Map<String, float[]> variants) {
    super(owner);
    this.variants = variants;
  }
  
  public BufferedImage longtip() {
    BufferedImage img = null;
    int k = 0;
    int n = (this.variants == null) ? 0 : this.variants.size();
    if (n > 0) {
      BufferedImage[] names = new BufferedImage[n];
      BufferedImage[] vals = new BufferedImage[n];
      int namew = 0, valuew = 0, totalh = 0;
      for (Map.Entry<String, float[]> variant : this.variants.entrySet()) {
        String resn = variant.getKey();
        float[] mults = variant.getValue();
        Resource.Tooltip tt = Resource.load(resn).<Resource.Tooltip>layer(Resource.tooltip);
        names[k] = (RichText.render(((tt != null) ? tt.t : resn) + ":", 0, new Object[0])).img;
        String buf = "";
        for (int j = 0; j < 4; j++) {
          if (j > 0)
            buf = buf + ", "; 
          buf = buf + String.format("$col[%s]{x%.2f}", new Object[] { Tempers.tcolors[j], Float.valueOf(mults[j]) });
        } 
        vals[k] = (RichText.render(buf, 0, new Object[0])).img;
        namew = Math.max(namew, names[k].getWidth());
        valuew = Math.max(valuew, vals[k].getWidth());
        totalh += names[k].getHeight();
        k++;
      } 
      img = TexI.mkbuf(new Coord(namew + 5 + valuew, totalh));
      Graphics g = img.getGraphics();
      int ch = 0;
      for (int i = 0; i < names.length; i++) {
        g.drawImage(names[i], 0, ch, null);
        g.drawImage(vals[i], namew + 5, ch, null);
        ch += names[i].getHeight();
      } 
    } 
    return img;
  }
  
  public static class Data implements ItemData.ITipData {
    private Map<String, float[]> vars;
    
    public ItemInfo.Tip create() {
      return new VariantsInfo(null, this.vars);
    }
    
    public static class DataAdapter extends TypeAdapter<Data> {
      public void write(JsonWriter writer, VariantsInfo.Data data) throws IOException {}
      
      public VariantsInfo.Data read(JsonReader reader) throws IOException {
        VariantsInfo.Data data = new VariantsInfo.Data();
        Map<String, float[]> vars = (Map)new LinkedHashMap<>();
        data.vars = vars;
        reader.beginObject();
        while (reader.hasNext()) {
          String name = reader.nextName();
          float[] mults = new float[4];
          int k = 0;
          reader.beginArray();
          while (reader.hasNext())
            mults[k++] = (float)reader.nextDouble(); 
          reader.endArray();
          vars.put(name, mults);
        } 
        reader.endObject();
        return data;
      }
    }
  }
}
