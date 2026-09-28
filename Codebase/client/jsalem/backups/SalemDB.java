package org.latikai.bots;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SalemDB {
   private Connection conn;
   private static SalemDB instance;
   private static final String DB_NAME = "items.db";
   private static final String CACHE_TABLE = "CraftingRecipes";
   private static final String PLAYER_TABLE = "Players";

   private SalemDB() {
      try {
         Class.forName("org.sqlite.JDBC");
         String path = findDbPath();
         if (path == null) return;
         conn = DriverManager.getConnection("jdbc:sqlite:" + path);
         ensureCacheTable();
         System.out.println("[SalemDB] Connected to " + path);
      } catch (Exception e) {
         System.out.println("[SalemDB] Error: " + e.getMessage());
      }
   }

   public static synchronized SalemDB get() {
      if (instance == null) instance = new SalemDB();
      return instance;
   }

   private String findDbPath() {
      String[] paths = {
         "items.db",
         "../items.db",
         System.getProperty("user.dir") + "/items.db",
         System.getProperty("user.dir") + "/../items.db"
      };
      for (String p : paths) {
         File f = new File(p);
         if (f.exists()) return f.getAbsolutePath();
      }
      return null;
   }

   private void ensureCacheTable() throws Exception {
      try (Statement s = conn.createStatement()) {
         s.execute("CREATE TABLE IF NOT EXISTS " + CACHE_TABLE + " (Item TEXT PRIMARY KEY, Materials TEXT, CachedAt DATETIME DEFAULT CURRENT_TIMESTAMP)");
         s.execute("CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE + " (ID INTEGER PRIMARY KEY, FirstSeen DATETIME DEFAULT CURRENT_TIMESTAMP, LastSeen DATETIME DEFAULT CURRENT_TIMESTAMP, MessageCount INTEGER DEFAULT 0)");
      }
   }

   private static final java.util.Map<String, String> STAT_COLUMNS = new java.util.HashMap<>();
   static {
      STAT_COLUMNS.put("affluence", "Affluence"); STAT_COLUMNS.put("alloying", "Alloying");
      STAT_COLUMNS.put("blunt", "Blunt_Power"); STAT_COLUMNS.put("piercing", "Piercing_Power"); STAT_COLUMNS.put("impact", "Impact_Power");
      STAT_COLUMNS.put("blunt defence", "Blunt_Defence"); STAT_COLUMNS.put("piercing defence", "Piercing_Defence"); STAT_COLUMNS.put("impact defence", "Impact_Defence");
      STAT_COLUMNS.put("feral", "Feral_Defence"); STAT_COLUMNS.put("combat", "Common_Combat_Power"); STAT_COLUMNS.put("combat defence", "Common_Combat_Defence");
      STAT_COLUMNS.put("criminality", "Criminality"); STAT_COLUMNS.put("feasting", "Feasting");
      STAT_COLUMNS.put("mine", "Mining"); STAT_COLUMNS.put("mining", "Mining"); STAT_COLUMNS.put("mines", "Mining"); STAT_COLUMNS.put("mountain", "Mines_Mountains"); STAT_COLUMNS.put("mountains", "Mines_Mountains");
      STAT_COLUMNS.put("pockets", "Pockets"); STAT_COLUMNS.put("productivity", "Productivity"); STAT_COLUMNS.put("rummaging", "Rummaging");
      STAT_COLUMNS.put("digging", "Soil_Digging"); STAT_COLUMNS.put("spellpower", "Spellpower"); STAT_COLUMNS.put("weaving", "Weaving");
      STAT_COLUMNS.put("woodworking", "Woodworking"); STAT_COLUMNS.put("slots", "Slots");
      STAT_COLUMNS.put("cut", "cut"); STAT_COLUMNS.put("gem", "gem"); STAT_COLUMNS.put("arts", "Arts_Crafts");
      STAT_COLUMNS.put("cloak", "Cloak_Dagger"); STAT_COLUMNS.put("dagger", "Cloak_Dagger"); STAT_COLUMNS.put("faith", "Faith_Wisdom");
      STAT_COLUMNS.put("wisdom", "Faith_Wisdom"); STAT_COLUMNS.put("flora", "Flora_Fauna"); STAT_COLUMNS.put("fauna", "Flora_Fauna");
      STAT_COLUMNS.put("hammer", "Hammer_Nail"); STAT_COLUMNS.put("nail", "Hammer_Nail");
      STAT_COLUMNS.put("hunt", "Hunting_Hideworking"); STAT_COLUMNS.put("hide", "Hunting_Hideworking"); STAT_COLUMNS.put("hunting", "Hunting_Hideworking");
      STAT_COLUMNS.put("law", "Law_Lore"); STAT_COLUMNS.put("lore", "Law_Lore");
      STAT_COLUMNS.put("herb", "Herbs_Sprouts"); STAT_COLUMNS.put("herbs", "Herbs_Sprouts"); STAT_COLUMNS.put("sprouts", "Herbs_Sprouts");
      STAT_COLUMNS.put("spark", "Sparks_Embers"); STAT_COLUMNS.put("sparks", "Sparks_Embers"); STAT_COLUMNS.put("embers", "Sparks_Embers");
      STAT_COLUMNS.put("stock", "Stocks_Cultivars"); STAT_COLUMNS.put("stocks", "Stocks_Cultivars"); STAT_COLUMNS.put("cultivar", "Stocks_Cultivars"); STAT_COLUMNS.put("cultivars", "Stocks_Cultivars");
      STAT_COLUMNS.put("sugar", "Sugar_Spice"); STAT_COLUMNS.put("spice", "Sugar_Spice");
      STAT_COLUMNS.put("thread", "Thread_Needle"); STAT_COLUMNS.put("needle", "Thread_Needle");
   }

   private static final java.util.Set<String> STOP_WORDS = new java.util.HashSet<>(java.util.Arrays.asList(
      "a","an","the","is","it","of","to","for","in","on","at","by","with","from","and","or",
      "what","whats","how","who","where","when","why","which","this","that","these","those",
      "tell","me","about","does","do","did","can","could","would","should","will","has","have",
      "get","got","make","made","know","find","need","use","used","say","said","like",
      "are","was","were","been","being","some","any","all","each","every","both","few",
      "more","most","other","into","over","such","only","own","same","so","than","too","very",
      "just","also","not","no","now","then","there","here","please","thanks","thx","hey","hi",
      "hello","yes","yeah","yep","nope","no","nah","ok","okay","sure","right","well",
      "look","looking","search","searching","stats","info","information","name","called"
   ));

   public String searchItem(String text) {
      if (conn == null || text == null) return null;
      String lower = text.toLowerCase().replaceAll("[^a-z0-9'\\s]", " ").trim();
      String[] words = lower.split("\\s+");

       // Stat-based queries take priority (e.g. "what adds feasting")
      for (String w : words) {
         String col = STAT_COLUMNS.get(w);
         if (col != null && !col.equals("cut") && !col.equals("gem")) {
            String result = searchByStat(col);
            if (result != null) return result;
         }
      }

      // First try the full text as a direct lookup
      String result = lookupItem(lower);
      if (result != null) return result;
      // Try progressively shorter suffixes
      for (int end = words.length; end >= 1; end--) {
         for (int start = 0; start + end <= words.length; start++) {
            StringBuilder phrase = new StringBuilder();
            for (int i = start; i < start + end; i++) {
               if (!STOP_WORDS.contains(words[i])) {
                  if (phrase.length() > 0) phrase.append(" ");
                  phrase.append(words[i]);
               }
            }
            if (phrase.length() > 0) {
               result = lookupItem(phrase.toString());
               if (result != null) return result;
            }
         }
      }
      // Single-word fallback only if the original query had exactly 1 meaningful word
      int meaningful = 0;
      for (String w : words) { if (!STOP_WORDS.contains(w) && w.length() > 2) meaningful++; }
      if (meaningful <= 1) {
         for (String w : words) {
            if (!STOP_WORDS.contains(w) && w.length() > 2) {
               result = lookupItem(w);
               if (result != null) return result;
            }
         }
      }
      return null;
   }

   public String lookupItem(String name) {
      if (conn == null || name == null || name.trim().isEmpty()) return null;
      String clean = name.trim().toLowerCase().replaceAll("[^a-z0-9'\\s]", " ").trim();
      String[] words = clean.split("\\s+");
      if (words.length == 0 || (words.length == 1 && words[0].length() < 3)) return null;

      // Build word-level clauses: each word must appear as a whole word or at start/end
      StringBuilder wordClause = new StringBuilder();
      for (String w : words) {
         if (w.length() < 2) continue;
         if (wordClause.length() > 0) wordClause.append(" AND ");
         wordClause.append("(LOWER(Item) LIKE '% ")
            .append(w).append(" %' OR LOWER(Item) LIKE '")
            .append(w).append(" %' OR LOWER(Item) LIKE '% ")
            .append(w).append("' OR LOWER(Item) = '")
            .append(w).append("')");
      }
      if (wordClause.length() == 0) return null;

      try {
         String[][] tables = {
            {"Artifact", "Artifacts", "Item, Blunt_Power, Piercing_Power, Impact_Power, Blunt_Defence, Piercing_Defence, Impact_Defence, Feral_Defence, Common_Combat_Power, Common_Combat_Defence, Criminality, Feasting, Mining, Pockets, Productivity, Rummaging, Soil_Digging, Spellpower, Weaving, Woodworking, Affluence, Alloying, Slots"},
            {"Clothing", "Clothes", "Item, Equipment_Slot, Artificer_Slots, Thermal_Value, Weight"},
            {"Inspiration", "Inspirationals", "Item, Arts_Crafts, Cloak_Dagger, Faith_Wisdom, Flora_Fauna, Hammer_Nail, Hunting_Hideworking, Law_Lore, Mines_Mountains, Herbs_Sprouts, Sparks_Embers, Stocks_Cultivars, Sugar_Spice, Thread_Needle, Natural_Philosophy, Perennial_Philosophy, Inspiration"}
         };

         // Check Creatures table too
         try (PreparedStatement ps = conn.prepareStatement("SELECT Name FROM Creatures WHERE " + wordClause + " LIMIT 1")) {
            try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) return "Creature - " + rs.getString("Name");
            }
         } catch (Exception e) {}

         for (String[] tbl : tables) {
            String sql = "SELECT '" + tbl[0] + "' as src, " + tbl[2] + " FROM " + tbl[1] + " WHERE " + wordClause + " LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
               try (ResultSet rs = ps.executeQuery()) {
                  if (rs.next()) {
                     StringBuilder sb = new StringBuilder();
                     sb.append(rs.getString("Item")).append(": ");
                     int cols = rs.getMetaData().getColumnCount();
                     for (int i = 3; i <= cols; i++) {
                        String col = rs.getMetaData().getColumnLabel(i);
                        Object val = rs.getObject(i);
                        if (val != null && !col.equals("src")) {
                           String display = col.replace("_", " ");
                           if (val instanceof Number && ((Number)val).intValue() != 0) {
                              sb.append(display).append(" +").append(val).append(", ");
                           }
                        }
                     }
                     String result = sb.toString().replaceAll(", $", "");
                     if (result.contains(":")) return result;
                  }
               }
            }
         }
         return null;
      } catch (Exception e) {
         System.out.println("[SalemDB] Query error: " + e.getMessage());
         return null;
      }
   }

   public String lookupCachedRecipe(String itemName) {
      if (conn == null) return null;
      try (PreparedStatement ps = conn.prepareStatement("SELECT Materials FROM " + CACHE_TABLE + " WHERE LOWER(Item) = ?")) {
         ps.setString(1, itemName.toLowerCase());
         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("Materials");
         }
      } catch (Exception e) {
         System.out.println("[SalemDB] Cache read error: " + e.getMessage());
      }
      return null;
   }

   public void cacheRecipe(String itemName, String materials) {
      if (conn == null) return;
      try (PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO " + CACHE_TABLE + " (Item, Materials) VALUES (?, ?)")) {
         ps.setString(1, itemName.toLowerCase());
         ps.setString(2, materials);
         ps.executeUpdate();
         System.out.println("[SalemDB] Cached recipe: " + itemName);
      } catch (Exception e) {
         System.out.println("[SalemDB] Cache write error: " + e.getMessage());
      }
   }

   private static final java.util.Set<String> INSPIRATION_COLS = new java.util.HashSet<>(java.util.Arrays.asList(
      "Arts_Crafts","Cloak_Dagger","Faith_Wisdom","Flora_Fauna","Hammer_Nail","Hunting_Hideworking",
      "Law_Lore","Mines_Mountains","Herbs_Sprouts","Sparks_Embers","Stocks_Cultivars","Sugar_Spice",
      "Thread_Needle","Natural_Philosophy","Perennial_Philosophy"
   ));

   private String searchByStat(String col) {
      if (conn == null) return null;
      String table = INSPIRATION_COLS.contains(col) ? "Inspirationals" : "Artifacts";
      try (Statement s = conn.createStatement()) {
         try (ResultSet rs = s.executeQuery("SELECT Item, " + col + " FROM " + table + " WHERE " + col + " IS NOT NULL AND CAST(" + col + " AS INTEGER) > 0 ORDER BY CAST(" + col + " AS INTEGER) DESC LIMIT 8")) {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
               String item = rs.getString("Item");
               try {
                  int val = Integer.parseInt(rs.getString(col).replaceAll(",", ""));
                  sb.append(item).append(" +").append(val).append(", ");
               } catch (Exception e) {}
            }
            String result = sb.toString().replaceAll(", $", "");
            if (result.contains("+")) return "Items with " + col.replace("_", " ") + ": " + result;
         }
      } catch (Exception e) {
         System.out.println("[SalemDB] Stat search error: " + e.getMessage());
      }
      return null;
   }

   public void trackPlayer(int id) {
      if (conn == null) return;
      try {
         try (PreparedStatement ps = conn.prepareStatement("INSERT INTO " + PLAYER_TABLE + " (ID, MessageCount) VALUES (?, 1)")) {
            ps.setInt(1, id);
            ps.executeUpdate();
         }
      } catch (java.sql.SQLException e) {
         if (e.getMessage().contains("UNIQUE") || e.getMessage().contains("PRIMARY KEY")) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE " + PLAYER_TABLE + " SET LastSeen = CURRENT_TIMESTAMP, MessageCount = MessageCount + 1 WHERE ID = ?")) {
               ps.setInt(1, id);
               ps.executeUpdate();
            } catch (Exception e2) {}
         }
      } catch (Exception e) {}
   }

   public void close() {
      try { if (conn != null) conn.close(); } catch (Exception e) {}
   }
}
