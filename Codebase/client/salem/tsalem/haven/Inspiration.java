package haven;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Inspiration extends ItemInfo.Tip {
  public final int xc;
  
  public final int base;
  
  public final float multi;
  
  public final String[] attrs;
  
  public final int[] exp;
  
  public final int[] o;
  
  public Inspiration(ItemInfo.Owner owner, int xc, String[] attrs, int[] exp) {
    super(owner);
    this.o = CharWnd.sortattrs(attrs);
    this.attrs = attrs;
    this.exp = exp;
    this.base = total();
    this.xc = (xc >= 0) ? xc : this.base;
    int k = Math.round(100.0F * xc / this.base);
    this.multi = k / 100.0F;
  }
  
  public Inspiration(ItemInfo.Owner o, String[] attrs, int[] exp) {
    this(o, -1, attrs, exp);
  }
  
  public int total() {
    int ret = 0;
    int n = this.attrs.length;
    for (int i = 0; i < n; i++) {
      if (!this.attrs[i].equals("uses"))
        ret += this.exp[i]; 
    } 
    return ret;
  }
  
  public BufferedImage longtip() {
    StringBuilder buf = new StringBuilder();
    Color[] cs = UI.instance.gui.chrwdg.attrcols(this.attrs);
    buf.append("When studied:");
    int uses = -1;
    for (int i = 0; i < this.attrs.length; i++) {
      int k = this.o[i];
      String type = this.attrs[k];
      if (type.equals("uses")) {
        uses = this.exp[k];
      } else {
        String attr = CharWnd.attrnm.get(type);
        if (attr != null) {
          Color c = cs[k];
          buf.append(String.format("\n$col[%d,%d,%d]{%s: %d}", new Object[] { Integer.valueOf(c.getRed()), Integer.valueOf(c.getGreen()), Integer.valueOf(c.getBlue()), attr, Integer.valueOf(this.exp[k]) }));
        } 
      } 
    } 
    buf.append(String.format("   $b{$col[192,192,64]{inspiration required: %d}}\n", new Object[] { Integer.valueOf((this.xc == 0) ? total() : this.xc) }));
    if (uses > 0)
      buf.append(String.format("$b{$col[192,192,64]{Uses: %d}}\n", new Object[] { Integer.valueOf(uses) })); 
    return (RichText.stdf.render(buf.toString(), 0, new Object[0])).img;
  }
  
  public static class Data implements ItemData.ITipData {
    String[] attrs;
    
    int[] exp;
    
    public Data() {}
    
    public Data(Inspiration info) {
      this.attrs = info.attrs;
      this.exp = info.exp;
    }
    
    public ItemInfo.Tip create() {
      return new Inspiration(null, 0, this.attrs, this.exp);
    }
    
    public static class DataAdapter extends TypeAdapter<Data> {
      public void write(JsonWriter writer, Inspiration.Data data) throws IOException {
        writer.beginObject();
        int n = data.attrs.length;
        for (int i = 0; i < n; i++)
          writer.name(data.attrs[i]).value(data.exp[i]); 
        writer.endObject();
      }
      
      public Inspiration.Data read(JsonReader reader) throws IOException {
        List<String> names = new LinkedList<>();
        List<Integer> vals = new LinkedList<>();
        reader.beginObject();
        while (reader.hasNext()) {
          names.add(reader.nextName());
          vals.add(Integer.valueOf(reader.nextInt()));
        } 
        reader.endObject();
        Inspiration.Data data = new Inspiration.Data();
        data.attrs = names.<String>toArray(new String[names.size()]);
        data.exp = new int[vals.size()];
        for (int i = 0; i < data.exp.length; i++)
          data.exp[i] = ((Integer)vals.get(i)).intValue(); 
        return data;
      }
    }
  }
}
