package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemData {
  private static Gson gson;
  
  private static Map<String, ItemData> item_data = new LinkedHashMap<String, ItemData>(9, 0.75F, true) {
      private static final long serialVersionUID = 1L;
      
      protected boolean removeEldestEntry(Map.Entry<String, ItemData> eldest) {
        return (size() > 75);
      }
    };
  
  ArrayList<Data> data = new ArrayList<>();
  
  public VariantsInfo.Data variants;
  
  private class Data implements Comparable<Data> {
    public String name;
    
    public List<String> adhoc = new ArrayList<>();
    
    public double purity;
    
    public FoodInfo.Data food;
    
    public Inspiration.Data inspiration;
    
    public GobbleInfo.Data gobble;
    
    public ArtificeData artifice;
    
    public int uses;
    
    public Data(GItem item) {
      this(item.info());
    }
    
    public Data(List<ItemInfo> info) {
      init(info);
    }
    
    public void init(List<ItemInfo> info) {
      double multiplier = ItemData.getMultiplier(info);
      this.uses = ItemData.getUses(info);
      for (ItemInfo ii : info) {
        String className = ii.getClass().getCanonicalName();
        if (ii instanceof ItemInfo.Name) {
          this.name = ((ItemInfo.Name)ii).str.text;
          continue;
        } 
        if (ii instanceof Alchemy) {
          this.purity = ((Alchemy)ii).purity();
          continue;
        } 
        if (ii instanceof ItemInfo.AdHoc) {
          this.adhoc.add(((ItemInfo.AdHoc)ii).str.text);
          continue;
        } 
        if (ii instanceof FoodInfo) {
          this.food = new FoodInfo.Data((FoodInfo)ii, multiplier);
          continue;
        } 
        if (ii instanceof Inspiration) {
          this.inspiration = new Inspiration.Data((Inspiration)ii);
          continue;
        } 
        if (ii instanceof GobbleInfo) {
          this.gobble = new GobbleInfo.Data((GobbleInfo)ii, multiplier);
          continue;
        } 
        if (className.equals("Slotted"))
          this.artifice = new ArtificeData(ii); 
      } 
    }
    
    public BufferedImage longtip() {
      String tt = String.format("%s\npurity:%.2f", new Object[] { this.name, Double.valueOf(this.purity) });
      for (String hoc : this.adhoc)
        tt = tt + "\n" + hoc; 
      BufferedImage img = (MenuGrid.ttfnd.render(tt, 300, new Object[0])).img;
      ItemData.ITipData[] data = { this.food, this.gobble, this.inspiration, this.artifice };
      for (ItemData.ITipData tip : data) {
        if (tip != null)
          img = ItemInfo.catimgs(3, new BufferedImage[] { img, tip.create().longtip() }); 
      } 
      if (this.uses > 0)
        img = ItemInfo.catimgs(3, new BufferedImage[] { img, (RichText.stdf.render(String.format("$b{$col[192,192,64]{Uses: %d}}\n", new Object[] { Integer.valueOf(this.uses) }))).img }); 
      return img;
    }
    
    public int compareTo(Data arg1) {
      int ret = this.name.compareTo(arg1.name);
      if (ret != 0)
        return ret; 
      ret = this.adhoc.size() - arg1.adhoc.size();
      if (ret != 0)
        return ret; 
      for (int i = 0; i < this.adhoc.size(); i++) {
        ret = ((String)this.adhoc.get(i)).compareTo(arg1.adhoc.get(i));
        if (ret != 0)
          return ret; 
      } 
      return ret;
    }
  }
  
  public void update(GItem item) {
    Data n = new Data(item);
    for (Data d : this.data) {
      if (0 == d.compareTo(n)) {
        this.data.remove(d);
        break;
      } 
    } 
    this.data.add(n);
    Collections.sort(this.data);
  }
  
  public Tex longtip(Resource res, int idx) {
    Resource.AButton ad = res.<Resource.AButton>layer(Resource.action);
    Resource.Pagina pg = res.<Resource.Pagina>layer(Resource.pagina);
    String tt = String.format("%s (%d of %d)\n\n", new Object[] { ad.name, Integer.valueOf(idx), Integer.valueOf(this.data.size()) });
    if (pg != null)
      tt = tt + pg.text; 
    BufferedImage img = (MenuGrid.ttfnd.render(tt, 300, new Object[0])).img;
    if (idx > 0 && idx <= this.data.size()) {
      Data d = this.data.get(idx - 1);
      img = ItemInfo.catimgs(3, new BufferedImage[] { img, d.longtip() });
    } else {
      for (Data d : this.data) {
        img = ItemInfo.catimgs(3, new BufferedImage[] { img, d.longtip() });
      } 
    } 
    return new TexI(img);
  }
  
  public static void actualize(GItem item, Glob.Pagina pagina) {
    String name = item.name();
    if (name == null)
      return; 
    name = (pagina.res()).name;
    ItemData data = item_data.get(name);
    if (data == null)
      data = new ItemData(); 
    data.update(item);
    item_data.put(name, data);
    store(name, data);
  }
  
  static int getUses(List<ItemInfo> info) {
    GItem.NumberInfo ninf = ItemInfo.<GItem.NumberInfo>find(GItem.NumberInfo.class, info);
    if (ninf != null)
      return ninf.itemnum(); 
    return -1;
  }
  
  private static double getMultiplier(List<ItemInfo> info) {
    Alchemy alch = ItemInfo.<Alchemy>find(Alchemy.class, info);
    if (alch != null)
      return 1.0D + alch.purity(); 
    return 1.0D;
  }
  
  private static void store(String name, ItemData data) {
    File file = Config.getFile(getFilename(name));
    boolean exists = file.exists();
    if (!exists)
      try {
        (new File(file.getParent())).mkdirs();
        exists = file.createNewFile();
      } catch (IOException iOException) {} 
    if (exists && file.canWrite()) {
      PrintWriter out = null;
      try {
        out = new PrintWriter(file);
        out.print(getGson().toJson(data));
      } catch (FileNotFoundException fileNotFoundException) {
      
      } finally {
        if (out != null)
          out.close(); 
      } 
    } 
  }
  
  public static ItemData get(String name) {
    if (item_data.containsKey(name))
      return item_data.get(name); 
    return load(name);
  }
  
  private static ItemData load(String name) {
    ItemData data = null;
    String filename = getFilename(name);
    InputStream inputStream = null;
    File file = Config.getFile(filename);
    if (file.exists() && file.canRead()) {
      try {
        inputStream = new FileInputStream(file);
      } catch (FileNotFoundException fileNotFoundException) {}
    } else {
      inputStream = ItemData.class.getResourceAsStream(filename);
    } 
    if (inputStream != null) {
      data = parseStream(inputStream);
      item_data.put(name, data);
    } 
    return data;
  }
  
  private static String getFilename(String name) {
    return "/item_data/" + name + ".json";
  }
  
  private static ItemData parseStream(InputStream inputStream) {
    ItemData data = null;
    try {
      String json = Utils.stream2str(inputStream);
      data = (ItemData)getGson().fromJson(json, ItemData.class);
    } catch (JsonSyntaxException jsonSyntaxException) {
      try {
        inputStream.close();
      } catch (IOException iOException) {}
    } finally {
      try {
        inputStream.close();
      } catch (IOException iOException) {}
    } 
    return data;
  }
  
  private static Gson getGson() {
    if (gson == null) {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(Inspiration.Data.class, (new Inspiration.Data.DataAdapter()).nullSafe());
      builder.registerTypeAdapter(FoodInfo.Data.class, (new FoodInfo.Data.DataAdapter()).nullSafe());
      builder.registerTypeAdapter(GobbleInfo.Data.class, (new GobbleInfo.Data.DataAdapter()).nullSafe());
      builder.registerTypeAdapter(ArtificeData.class, (new ArtificeData.DataAdapter()).nullSafe());
      builder.registerTypeAdapter(VariantsInfo.Data.class, (new VariantsInfo.Data.DataAdapter()).nullSafe());
      builder.setPrettyPrinting();
      gson = builder.create();
    } 
    return gson;
  }
  
  public static interface ITipData {
    ItemInfo.Tip create();
  }
}
