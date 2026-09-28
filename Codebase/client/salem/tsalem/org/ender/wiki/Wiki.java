package org.ender.wiki;

import haven.Utils;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Wiki {
  private static final Pattern PAT_REDIRECT = Pattern.compile("#REDIRECT\\s*\\[\\[(.*)\\]\\]", 2);
  
  private static final Pattern PAT_CATS = Pattern.compile("\\{\\{([^\\|]*)(|.*?)}}", 40);
  
  private static final Pattern PAT_ARGS = Pattern.compile("\\s*\\|\\s*(.*?)\\s*=\\s*([^\\|]*)", 40);
  
  private static final String CONTENT_URL = "action=query&prop=revisions&titles=%s&rvprop=content&format=json";
  
  private static final String SEARCH_URL = "action=query&list=search&format=json&srprop=snippet&srsearch=%s";
  
  private static final String[] FOOD_ATTRS = new String[] { "Heals", "GluttonMin", "GluttonMax" };
  
  private static final String[] GLUTTON_MIN = new String[] { "Min Blood", "Min Phlegm", "Min Yellow Bile", "Min Black Bile" };
  
  private static final String[] GLUTTON_MAX = new String[] { "Max Blood", "Max Phlegm", "Max Yellow Bile", "Max Black Bile" };
  
  private static final Map<String, String> imap = new HashMap<>(15);
  
  public static final Map<String, String> buffmap = new HashMap<>(21);
  
  private static final LinkedBlockingQueue<Request> requests = new LinkedBlockingQueue<>();
  
  private static File folder;
  
  private static long update_date;
  
  private static final Map<String, Item> DB = new LinkedHashMap<String, Item>(9, 0.75F, true) {
      private static final long serialVersionUID = 1L;
      
      protected boolean removeEldestEntry(Map.Entry<String, Item> eldest) {
        return false;
      }
    };
  
  public static void init(File cfg, int workers) {
    URL u = Wiki.class.getResource("");
    String path = u.toString();
    path = path.substring(10, path.indexOf('!'));
    File f = new File(path);
    update_date = f.lastModified();
    imap.put("Arts & Crafts", "arts");
    imap.put("Cloak & Dagger", "cloak");
    imap.put("Faith & Wisdom", "faith");
    imap.put("Flora & Fauna", "wild");
    imap.put("Hammer & Nail", "nail");
    imap.put("Hunting & Hideworking", "hung");
    imap.put("Law & Lore", "law");
    imap.put("Mines & Mountains", "mine");
    imap.put("Herbs & Sprouts", "pots");
    imap.put("Sparks & Embers", "fire");
    imap.put("Stocks & Cultivars", "stock");
    imap.put("Sugar & Spice", "spice");
    imap.put("Thread & Needle", "thread");
    imap.put("Natural Philosophy", "natp");
    imap.put("Perennial Philosophy", "perp");
    imap.put("Uses", "uses");
    buffmap.put("Bread", "bread");
    buffmap.put("Vegetables and Greens", "vegfood");
    buffmap.put("Offal", "offal");
    buffmap.put("Foraged", "forage");
    buffmap.put("Poultry", "poultry");
    buffmap.put("Cabbages", "cabbage");
    buffmap.put("Candy", "candy");
    buffmap.put("Pies", "pies");
    buffmap.put("Maize", "corn");
    buffmap.put("Cereal", "cereal");
    buffmap.put("Meat", "meat");
    buffmap.put("Domesticated Meat", "meat");
    buffmap.put("Cookies and Crackers", "cookies");
    buffmap.put("Potato Food", "potatofood");
    buffmap.put("Berries", "berry");
    buffmap.put("Flowers and Herbs", "flowerfood");
    buffmap.put("Seafood", "seafood");
    buffmap.put("Fishes", "fish");
    buffmap.put("Game", "game");
    buffmap.put("Game Meat", "game");
    buffmap.put("Slugs Bugs and Kritters", "slugsnbugs");
    buffmap.put("Nuts and Seeds", "nut");
    buffmap.put("Crustacea and Shellfish", "shellfish");
    buffmap.put("Pumpkins and Gourds", "pumpkin");
    buffmap.put("Mushrooms", "shroom");
    folder = cfg;
    if (!folder.exists())
      folder.mkdirs(); 
    for (int i = 0; i < workers; i++) {
      Thread t = new Thread(new Runnable() {
            public void run() {
              while (true) {
                try {
                  while (true)
                    Wiki.load(Wiki.requests.take()); 
                  break;
                } catch (InterruptedException e) {
                  e.printStackTrace(System.out);
                } catch (Exception e) {
                  e.printStackTrace(System.out);
                } 
              } 
            }
          },  "Wiki loader " + i);
      t.setDaemon(true);
      t.start();
    } 
  }
  
  public static Item get(String name) {
    return get(name, null, Request.Type.ITEM);
  }
  
  public static Item get(String name, Request.Callback callback) {
    return get(name, callback, Request.Type.ITEM);
  }
  
  public static Item get(String name, Request.Callback callback, Request.Type type) {
    Item itm = null;
    synchronized (DB) {
      if (type == Request.Type.ITEM) {
        if (DB.containsKey(name)) {
          itm = DB.get(name);
          if (callback != null)
            callback.wiki_item_ready(itm); 
          return itm;
        } 
        itm = get_cache(name, true);
        DB.put(name, itm);
      } 
    } 
    request(new Request(name, callback, type));
    return itm;
  }
  
  public static void search(String name, Request.Callback callback) {
    get(name, callback, Request.Type.SEARCH);
  }
  
  private static void request(Request request) {
    try {
      requests.put(request);
    } catch (InterruptedException e) {
      e.printStackTrace(System.out);
    } 
  }
  
  private static void store(String label, Item item) {
    synchronized (DB) {
      DB.put(label, item);
    } 
  }
  
  private static void cache(Item item) {
    try {
      FileWriter fw = new FileWriter(new File(folder, item.name + ".xml"));
      fw.write(item.toXML());
      fw.close();
    } catch (IOException e) {
      e.printStackTrace(System.out);
    } 
  }
  
  private static void load(Request request) {
    Item item = (request.type == Request.Type.ITEM) ? get_cache(request.name, false) : null;
    if (item == null) {
      item = new Item();
      item.name = request.name;
      if (request.type == Request.Type.SEARCH) {
        item.content = do_search(request.name);
      } else {
        item.content = get_content(request.name);
        if (item.content != null) {
          String content = item.content;
          parse_content(item);
          item.content = content;
          item.content = parse_wiki(item);
          cache(item);
          store(request.name, item);
        } 
      } 
    } 
    if (request.callback != null)
      request.callback.wiki_item_ready(item); 
  }
  
  private static String do_search(String name) {
    String content = null;
    try {
      URI uri = new URI("http", null, "salemthegame.wiki", -1, "/api.php", String.format("action=query&list=search&format=json&srprop=snippet&srsearch=%s", new Object[] { name }), null);
      URL url = uri.toURL();
      String data = Utils.stream2str(url.openStream());
      JSONObject json = new JSONObject(data);
      JSONArray pages = json.getJSONObject("query").getJSONArray("search");
      if (pages == null || pages.length() == 0)
        return null; 
      if (pages.length() == 1) {
        Item item = new Item();
        item.name = pages.getJSONObject(0).getString("title");
        item.content = get_content(item.name);
        return parse_wiki(item);
      } 
      content = "";
      for (int i = 0; i < pages.length(); i++) {
        JSONObject page = pages.getJSONObject(i);
        String title = page.getString("title");
        String snip = page.getString("snippet");
        if (snip.length() > 0)
          snip = snip + "<BR/>"; 
        content = content + String.format("<B><a href=\"/index.php/%s\">%s</a></B><BR/>%s<BR/>", new Object[] { title, title, snip });
      } 
      return content;
    } catch (JSONException e) {
      System.err.println(String.format("Error while parsing '%s':\n%s\nContent:'%s'", new Object[] { name, e.getMessage(), content }));
    } catch (IOException e) {
      e.printStackTrace(System.out);
    } catch (URISyntaxException e) {
      e.printStackTrace(System.out);
    } 
    return null;
  }
  
  private static void parse_content(Item item) {
    Matcher m = PAT_CATS.matcher(item.content);
    while (m.find()) {
      if (m.groupCount() == 2) {
        String method = m.group(1).trim();
        String argsline = m.group(2);
        parse(item, method, getargs(argsline));
      } 
      item.content = m.replaceFirst("");
      m = PAT_CATS.matcher(item.content);
    } 
  }
  
  private static void parse(Item item, String method, Map<String, String> args) {
    if (method.equals("Inspirational")) {
      Map<String, Integer> attrs = new HashMap<>();
      for (Map.Entry<String, String> e : args.entrySet()) {
        try {
          String name = e.getKey();
          if (name.equals("inspiration"))
            continue; 
          attrs.put(imap.get(name), Integer.valueOf(Integer.parseInt(e.getValue())));
        } catch (NumberFormatException numberFormatException) {}
      } 
      item.attgive = attrs;
    } else if (method.contains("Food")) {
      item_parse_food(item, args);
    } else if (method.equals("Artifact")) {
      String difficulty = null;
      String[] profs = null;
      Map<String, Integer> bonuses = new HashMap<>();
      for (Map.Entry<String, String> entry : args.entrySet()) {
        String key = entry.getKey();
        if (key.equals("Proficiency Type")) {
          profs = ((String)entry.getValue()).split(", ");
          continue;
        } 
        if (key.equals("Difficulty")) {
          difficulty = entry.getValue();
          continue;
        } 
        try {
          bonuses.put(key, Integer.valueOf(Integer.parseInt(entry.getValue())));
        } catch (Exception exception) {}
      } 
      item.setArtifact(difficulty, profs, bonuses);
    } else if (method.equals("Clothing")) {
      item_parse_cloth(item, args);
    } 
  }
  
  private static void item_parse_food(Item item, Map<String, String> args) {
    Map<String, Float[]> food = (Map)new HashMap<>(3);
    String key = "Heals";
    String arg = args.get("Heals");
    if (arg == null)
      return; 
    String[] svals = arg.split(",");
    Float[] vals = new Float[4];
    int i = 0;
    for (String sval : svals) {
      float val = 0.0F;
      try {
        val = Float.parseFloat(sval);
      } catch (NumberFormatException numberFormatException) {}
      vals[i] = Float.valueOf(val);
      i++;
    } 
    food.put("Heals", vals);
    food.put("GluttonMax", parse_glutton(args, GLUTTON_MAX));
    food.put("GluttonMin", parse_glutton(args, GLUTTON_MIN));
    Map<String, Integer[]> food_reduce = (Map)new HashMap<>(3);
    Map<String, Integer[]> food_restore = (Map)new HashMap<>(3);
    for (i = 0; i < 3; i++) {
      String namerestore = args.get("FoodRestore" + (i + 1));
      String namereduce = args.get("FoodReduce" + (i + 1));
      if (namerestore != null && namerestore.length() > 0) {
        Integer[] restore = { Integer.valueOf(Integer.parseInt(args.get("%Restore" + (i + 1)))), Integer.valueOf(Integer.parseInt(args.get("%ChanceRestore" + (i + 1)))) };
        food_restore.put(namerestore, restore);
      } 
      if (namereduce != null && namereduce.length() > 0) {
        Integer[] reduce = { Integer.valueOf(Integer.parseInt(args.get("%Reduce" + (i + 1)))), Integer.valueOf(Integer.parseInt(args.get("%ChanceReduce" + (i + 1)))) };
        food_reduce.put(namereduce, reduce);
      } 
    } 
    item.food_restore = food_restore;
    item.food_reduce = food_reduce;
    try {
      String times = args.get("Gluttony Time");
      item.food_full = (times == null) ? 0 : parseTime(times).intValue();
    } catch (NumberFormatException numberFormatException) {}
    try {
      String usess = args.get("Uses");
      item.food_uses = (usess == null) ? 0 : Integer.parseInt(usess);
    } catch (NumberFormatException numberFormatException) {}
    item.food = food;
  }
  
  private static Integer parseTime(String time) {
    int mid = time.indexOf(':');
    if (mid < 0)
      return Integer.valueOf(Integer.parseInt(time)); 
    Integer hours = Integer.valueOf(Integer.parseInt(time.substring(0, mid)));
    Integer minutes = Integer.valueOf(Integer.parseInt(time.substring(mid + 1)));
    return Integer.valueOf(hours.intValue() * 60 + minutes.intValue());
  }
  
  private static Float[] parse_glutton(Map<String, String> args, String[] keys) {
    Float[] vals = new Float[4];
    for (int k = 0; k < 4; k++) {
      String key = keys[k];
      String arg = args.get(key);
      Float val = Float.valueOf(0.0F);
      if (arg != null) {
        try {
          val = Float.valueOf(Float.parseFloat(arg));
        } catch (NumberFormatException numberFormatException) {}
        vals[k] = val;
      } 
    } 
    return vals;
  }
  
  private static void item_parse_cloth(Item item, Map<String, String> args) {
    if (args == null)
      return; 
    int slots = 0;
    String sslots = args.containsKey("Artificer Slots") ? args.get("Artificer Slots") : args.get("slots");
    try {
      slots = Integer.parseInt(sslots);
    } catch (Exception exception) {}
    item.setClothing(slots);
  }
  
  private static Map<String, String> getargs(String argsline) {
    Map<String, String> args = new HashMap<>();
    Matcher m = PAT_ARGS.matcher(argsline);
    while (m.find()) {
      if (m.groupCount() == 2) {
        String name = m.group(1).trim();
        String val = m.group(2).trim();
        args.put(name, val);
      } 
    } 
    return args;
  }
  
  private static String get_content(String name) {
    String data = null;
    try {
      URI uri = new URI("http", null, "salemthegame.wiki", -1, "/api.php", null, null);
      URL link = uri.toURL();
      HttpURLConnection conn = (HttpURLConnection)link.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      data = String.format("action=query&prop=revisions&titles=%s&rvprop=content&format=json", new Object[] { URLEncoder.encode(name, "UTF-8") });
      DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
      wr.writeBytes(data);
      wr.flush();
      wr.close();
      data = Utils.stream2str(conn.getInputStream());
      JSONObject json = new JSONObject(data);
      json = json.getJSONObject("query").getJSONObject("pages");
      String pageid = JSONObject.getNames(json)[0];
      String content = json.getJSONObject(pageid).getJSONArray("revisions").getJSONObject(0).getString("*");
      String redirect = get_redirect(content);
      if (redirect != null)
        return get_content(redirect); 
      return content;
    } catch (JSONException jSONException) {
    
    } catch (IOException iOException) {
    
    } catch (URISyntaxException uRISyntaxException) {}
    return null;
  }
  
  private static String parse_wiki(Item item) {
    try {
      URI uri = new URI("http", null, "salemthegame.wiki", -1, "/api.php", null, null);
      URL link = uri.toURL();
      HttpURLConnection conn = (HttpURLConnection)link.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      String data = URLEncoder.encode("__NOTOC__\n" + item.content.trim(), "UTF-8");
      String title = URLEncoder.encode(item.name, "UTF-8");
      String req = String.format("action=parse&format=json&text=%s&title=%s", new Object[] { data, title });
      DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
      wr.writeBytes(req);
      wr.flush();
      wr.close();
      data = Utils.stream2str(conn.getInputStream());
      JSONObject json = new JSONObject(data);
      json = json.getJSONObject("parse").getJSONObject("text");
      String content = json.getString("*");
      return content;
    } catch (JSONException e) {
      e.printStackTrace(System.out);
    } catch (IOException e) {
      e.printStackTrace(System.out);
    } catch (URISyntaxException e) {
      e.printStackTrace(System.out);
    } 
    return null;
  }
  
  private static String get_redirect(String content) {
    Matcher m = PAT_REDIRECT.matcher(content);
    if (m.find() && m.groupCount() == 1)
      return m.group(1); 
    return null;
  }
  
  private static Item get_cache(String name, boolean fast) {
    File f = new File(folder, name + ".xml");
    if (!f.exists())
      return null; 
    if (!fast && has_update(name, f.lastModified()))
      return null; 
    return load_cache(f);
  }
  
  private static Item load_cache(File f) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(f);
      Item item = new Item();
      item.name = doc.getDocumentElement().getAttribute("name");
      item.required = parse_cache(doc, "required");
      item.locations = parse_cache(doc, "locations");
      item.reqby = parse_cache(doc, "reqby");
      item.tech = parse_cache(doc, "tech");
      item.unlocks = parse_cache(doc, "unlocks");
      item.attreq = parse_cache_map(doc, "attreq");
      item.attgive = parse_cache_map(doc, "attgive");
      parse_cache_food(doc, item);
      item.content = parse_cache_content(doc, "content");
      item_parse_cloth(item, parse_cache_str_map(doc, "cloth"));
      item_parse_artifact(item, doc);
      return item;
    } catch (MalformedURLException e) {
      e.printStackTrace(System.out);
    } catch (NullPointerException e) {
      e.printStackTrace(System.out);
    } catch (IOException e) {
      e.printStackTrace(System.out);
    } catch (SAXException e) {
      e.printStackTrace(System.out);
    } catch (ParserConfigurationException e) {
      e.printStackTrace(System.out);
    } 
    return null;
  }
  
  private static void item_parse_artifact(Item item, Document doc) {
    String tag = "artifact";
    NodeList list = doc.getElementsByTagName("artifact");
    if (list.getLength() > 0) {
      Node node = list.item(0);
      NamedNodeMap attributes = node.getAttributes();
      String difficulty = attributes.getNamedItem("difficulty").getNodeValue();
      String[] profs = attributes.getNamedItem("profs").getNodeValue().split(", ");
      String[] bonuses = attributes.getNamedItem("bonuses").getNodeValue().split(", ");
      Map<String, Integer> art_bonuses = new HashMap<>(bonuses.length);
      for (String bonus : bonuses) {
        String[] entry = bonus.split("=");
        try {
          art_bonuses.put(entry[0], Integer.valueOf(Integer.parseInt(entry[1])));
        } catch (Exception exception) {}
      } 
      item.setArtifact(difficulty, profs, art_bonuses);
    } 
  }
  
  private static String parse_cache_content(Document doc, String tag) {
    NodeList list = doc.getElementsByTagName(tag);
    if (list.getLength() > 0) {
      Node item = list.item(0);
      return item.getTextContent();
    } 
    return null;
  }
  
  private static void parse_cache_food(Document doc, Item item) {
    NodeList list = doc.getElementsByTagName("food");
    if (list.getLength() > 0) {
      Node node = list.item(0);
      Map<String, Float[]> food = (Map)new HashMap<>();
      NamedNodeMap attrs = node.getAttributes();
      for (String name : FOOD_ATTRS) {
        Node attr = attrs.getNamedItem(name);
        if (attr != null && attr.getNodeValue() != null) {
          String[] svals = attr.getNodeValue().split(" ");
          Float[] vals = new Float[4];
          for (int j = 0; j < 4; j++) {
            try {
              vals[j] = Float.valueOf(Float.parseFloat(svals[j]));
            } catch (NumberFormatException ex) {
              vals[j] = Float.valueOf(0.0F);
            } 
          } 
          food.put(name, vals);
        } 
      } 
      item.food = food;
      Map<String, Integer[]> food_reduce = (Map)new HashMap<>(3);
      Map<String, Integer[]> food_restore = (Map)new HashMap<>(3);
      for (int i = 0; i < 3; i++) {
        Node restnode = attrs.getNamedItem("FoodRestore" + (i + 1));
        if (restnode != null) {
          String[] vals = restnode.getNodeValue().split(" ");
          int count = vals.length;
          Integer[] values = { Integer.valueOf(Integer.parseInt(vals[count - 2])), Integer.valueOf(Integer.parseInt(vals[count - 1])) };
          String name = vals[0];
          for (int j = 1; j < count - 2; j++)
            name = name + " " + vals[j]; 
          if (vals.length > 0)
            food_restore.put(name, values); 
        } 
        Node redunode = attrs.getNamedItem("FoodReduce" + (i + 1));
        if (redunode != null) {
          String[] vals = redunode.getNodeValue().split(" ");
          int count = vals.length;
          Integer[] values = { Integer.valueOf(Integer.parseInt(vals[count - 2])), Integer.valueOf(Integer.parseInt(vals[count - 1])) };
          String name = vals[0];
          for (int j = 1; j < count - 2; j++)
            name = name + " " + vals[j]; 
          if (vals.length > 0)
            food_reduce.put(name, values); 
        } 
      } 
      item.food_restore = food_restore;
      item.food_reduce = food_reduce;
      try {
        Node timenode = attrs.getNamedItem("full");
        item.food_full = parseTime(timenode.getNodeValue()).intValue();
      } catch (NumberFormatException numberFormatException) {}
      try {
        Node usenode = attrs.getNamedItem("uses");
        item.food_uses = Integer.parseInt(usenode.getNodeValue());
      } catch (NumberFormatException numberFormatException) {}
    } 
  }
  
  private static Set<String> parse_cache(Document doc, String tag) {
    NodeList list = doc.getElementsByTagName(tag);
    if (list.getLength() > 0) {
      Set<String> items = new HashSet<>(list.getLength());
      for (int i = 0; i < list.getLength(); i++)
        items.add(list.item(i).getAttributes().getNamedItem("name").getNodeValue()); 
      return items;
    } 
    return null;
  }
  
  private static Map<String, String> parse_cache_str_map(Document doc, String tag) {
    NodeList list = doc.getElementsByTagName(tag);
    if (list.getLength() > 0) {
      Node item = list.item(0);
      Map<String, String> items = new HashMap<>();
      NamedNodeMap attrs = item.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attr = attrs.item(i);
        items.put(attr.getNodeName(), attr.getNodeValue());
      } 
      return items;
    } 
    return null;
  }
  
  private static Map<String, Integer> parse_cache_map(Document doc, String tag) throws NullPointerException {
    NodeList list = doc.getElementsByTagName(tag);
    if (list.getLength() > 0) {
      Node item = list.item(0);
      Map<String, Integer> items = new HashMap<>();
      NamedNodeMap attrs = item.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attr = attrs.item(i);
        String name = attr.getNodeName();
        Integer value = Integer.decode(attr.getNodeValue());
        if (name.equalsIgnoreCase("null"))
          throw new NullPointerException("WIKI: argument name is null!"); 
        items.put(name, value);
      } 
      return items;
    } 
    return null;
  }
  
  private static boolean has_update(String name, long date) {
    try {
      if (date < update_date)
        return true; 
      URI uri = new URI("http", "salemthegame.wiki", "/index.php/" + name, null);
      URL url = uri.toURL();
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("HEAD");
      conn.setIfModifiedSince(date);
      if (conn.getResponseCode() == 200)
        return true; 
    } catch (MalformedURLException e) {
      e.printStackTrace(System.out);
    } catch (IOException e) {
      e.printStackTrace(System.out);
    } catch (URISyntaxException e1) {
      e1.printStackTrace(System.out);
    } 
    return false;
  }
}
