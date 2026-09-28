package haven;

import javax.media.opengl.GL2;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;

public class CartographWindow extends Window {
   public static CartographWindow instance = null;
   private CartographWindow.Marker selected_marker = null;
   private String selectedId = null;
   private TextEntry nameEntry = null;
   private static final RichText.Foundry foundry = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 12);
   private static CartographWindow.DrawnMap drawn;
   private CheckBox gridlines;
   private Button recenter;
   private Widget marker_info;
   private Label coordLabel;

   // Sync UI
   private TextEntry mapXEntry, mapYEntry;
   private Label mapXLabel, mapYLabel;
   private Button calibrateBtn, holdBtn, syncBtn;

   // Sync state
   private double offset_x = 0, offset_y = 0;
   private boolean tracking = false;
   private boolean held = false;
   private double heldMapX, heldMapY, heldGameX, heldGameY;
   private double lastSentX = Double.NaN, lastSentY = Double.NaN;
   private String playerMarkerId = null;

   // Timers
   private Timer syncTimer;
   private Timer markerTimer;

   // WitchWatchers markers (volatile: written on the Swing timer thread, read on the GL thread)
   private volatile List<WWMarker> wwMarkers = new ArrayList<>();

   // Endpoint
   private static final String ENDPOINT = "http://127.0.0.1:18321";
   private static final String CALIBRATION_FILE = System.getProperty("user.home") + "/Salem/cartograph_calibrations.json";

   // Map drag state
   boolean mmv = false;
   boolean rsm = false;
   private static Coord gzsz = new Coord(15, 15);
   private static Coord minsz = new Coord(560, 360);

   // WitchWatchers marker type colors
   private static final Map<String, Color> WW_COLORS = new LinkedHashMap<>();
   static {
      WW_COLORS.put("town", new Color(0x4caf50));
      WW_COLORS.put("player", new Color(0x2196f3));
      WW_COLORS.put("claim", new Color(0xff9800));
      WW_COLORS.put("interest", new Color(0xe91e63));
      WW_COLORS.put("abandon", new Color(0x9e9e9e));
      WW_COLORS.put("event", new Color(0x9c27b0));
   }

   public CartographWindow(Coord c, Widget parent) {
      super(c, new Coord(600, 450), parent, "TileTracker");
      this.justclose = true;
      drawn = new CartographWindow.DrawnMap();

      // Row 1: Grid lines checkbox + Marker info
      this.gridlines = new CheckBox(new Coord(15, this.sz.y - 155), this, "Grid lines") {
         @Override
         public void changed(boolean val) { CartographWindow.drawn.draw_grid = val; }
      };
      this.gridlines.a = false;

      // Row 2: Re-center
      this.recenter = new Button(new Coord(15, this.sz.y - 135), 100, this, "Re-center") {
         @Override
         public void click() { CartographWindow.drawn.off = Coord.z; }
      };

      // Row 3: Sync button (replaces Save map)
      this.syncBtn = new Button(new Coord(15, this.sz.y - 110), 100, this, "Sync") {
         @Override
         public void click() { doSyncToggle(); }
      };

      // Row 4: Single combined readout (grid | map/tile | offset | status)
      this.coordLabel = new Label(new Coord(130, this.sz.y - 98), this, "Grid: ---");

      // Row 5: Map X Y inputs + Calibrate + Hold
      this.mapXLabel = new Label(new Coord(15, this.sz.y - 78), this, "X:");
      this.mapXEntry = new TextEntry(new Coord(35, this.sz.y - 78), 70, this, "0");
      this.mapYLabel = new Label(new Coord(115, this.sz.y - 78), this, "Y:");
      this.mapYEntry = new TextEntry(new Coord(135, this.sz.y - 78), 70, this, "0");

      this.calibrateBtn = new Button(new Coord(215, this.sz.y - 81), 80, this, "Calibrate") {
         @Override
         public void click() { doCalibrate(); }
      };
      this.holdBtn = new Button(new Coord(305, this.sz.y - 81), 80, this, "Hold") {
         @Override
         public void click() { doHoldResume(); }
      };

      // Load saved calibration
      loadCalibration();

      // Timers
      syncTimer = new Timer(2000, e -> doSyncTick());
      syncTimer.setCoalesce(true);
      markerTimer = new Timer(4000, e -> doMarkerFetch());
      markerTimer.setCoalesce(true);

      this.setSelectedMarker(this.selected_marker);
   }

   // ========== SYNC LOGIC ==========

   private void doCalibrate() {
      try {
         double mapX = Double.parseDouble(mapXEntry.text);
         double mapY = Double.parseDouble(mapYEntry.text);
         if (UI.instance == null || UI.instance.gui == null || UI.instance.gui.map == null || UI.instance.gui.map.player() == null) {
            ui.message("[Cartograph] No player available.", GameUI.MsgType.ERROR);
            return;
         }
         Coord cc = UI.instance.gui.map.player().rc.div(MCache.tilesz);
         Coord off = new Coord();
         Coord tc = cc.add(off);
         Coord ncoord = tc.div(MCache.cmaps);
         offset_x = mapX - ncoord.x;
         offset_y = mapY - ncoord.y;
         saveCalibration();
         tracking = true;
         syncBtn.change("Stop");
         holdBtn.change("Hold");
         ui.message("[Cartograph] Calibrated. Offset: (" + (int)offset_x + ", " + (int)offset_y + ")", GameUI.MsgType.INFO);
         Coord n2 = UI.instance.gui.map.player().rc.div(MCache.tilesz).add(new Coord()).div(MCache.cmaps);
         createPlayerMarker(n2.x + offset_x, n2.y + offset_y);
         if (!syncTimer.isRunning()) syncTimer.start();
         if (!markerTimer.isRunning()) markerTimer.start();
         doMarkerFetch();
      } catch (NumberFormatException ex) {
         ui.message("[Cartograph] Invalid coordinates.", GameUI.MsgType.ERROR);
      }
   }

   private void doSyncToggle() {
      if (tracking) {
         tracking = false;
         syncTimer.stop();
         syncBtn.change("Sync");
         deletePlayerMarker();
         ui.message("[Cartograph] Sync stopped.", GameUI.MsgType.INFO);
      } else {
         if (offset_x == 0 && offset_y == 0) {
            ui.message("[Cartograph] Calibrate first!", GameUI.MsgType.ERROR);
            return;
         }
         tracking = true;
         syncBtn.change("Stop");
         holdBtn.change("Hold");
         Coord cc = UI.instance.gui.map.player().rc.div(MCache.tilesz);
         Coord off = new Coord();
         Coord tc = cc.add(off);
         Coord ncoord = tc.div(MCache.cmaps);
         createPlayerMarker(ncoord.x + offset_x, ncoord.y + offset_y);
         if (!syncTimer.isRunning()) syncTimer.start();
         if (!markerTimer.isRunning()) markerTimer.start();
         doMarkerFetch();
         ui.message("[Cartograph] Sync started.", GameUI.MsgType.INFO);
      }
   }

   private void doHoldResume() {
      if (held) {
         held = false;
         holdBtn.change("Hold");
         if (UI.instance != null && UI.instance.gui != null && UI.instance.gui.map != null && UI.instance.gui.map.player() != null) {
            Coord cc = UI.instance.gui.map.player().rc.div(MCache.tilesz);
            Coord off = new Coord();
            Coord tc = cc.add(off);
            Coord ncoord = tc.div(MCache.cmaps);
            offset_x = heldMapX - ncoord.x;
            offset_y = heldMapY - ncoord.y;
            saveCalibration();
            ui.message("[Cartograph] Resumed. New offset: (" + (int)offset_x + ", " + (int)offset_y + ")", GameUI.MsgType.INFO);
         }
         if (!syncTimer.isRunning()) syncTimer.start();
      } else {
         held = true;
         holdBtn.change("Resume");
         syncTimer.stop();
         if (UI.instance != null && UI.instance.gui != null && UI.instance.gui.map != null && UI.instance.gui.map.player() != null) {
            Coord cc = UI.instance.gui.map.player().rc.div(MCache.tilesz);
            Coord off = new Coord();
            Coord tc = cc.add(off);
            Coord ncoord = tc.div(MCache.cmaps);
            heldGameX = ncoord.x;
            heldGameY = ncoord.y;
            heldMapX = ncoord.x + offset_x;
            heldMapY = ncoord.y + offset_y;
         }
         ui.message("[Cartograph] Held. Position frozen.", GameUI.MsgType.INFO);
      }
   }

   private void doSyncTick() {
      if (!tracking || held) return;
      try {
         if (UI.instance == null || UI.instance.gui == null || UI.instance.gui.map == null || UI.instance.gui.map.player() == null) return;
         Coord cc = UI.instance.gui.map.player().rc.div(MCache.tilesz);
         Coord off = new Coord();
         Coord tc = cc.add(off);
         Coord ncoord = tc.div(MCache.cmaps);
         double cx = ncoord.x + offset_x;
         double cy = ncoord.y + offset_y;
         if (cx == lastSentX && cy == lastSentY) return;
         lastSentX = cx;
         lastSentY = cy;
         if (playerMarkerId != null) {
            postJSON(ENDPOINT + "/markers", "{\"action\":\"update\",\"id\":\"" + playerMarkerId + "\",\"x\":" + cx + ",\"y\":" + cy + "}");
         }
         postJSON(ENDPOINT + "/position", "{\"x\":" + cx + ",\"y\":" + cy + ",\"name\":\"Player\"}");
      } catch (Exception ex) {
         ui.message("[Cartograph] Sync failed — is the server running?", GameUI.MsgType.INFO);
         syncTimer.stop();
         tracking = false;
         syncBtn.change("Sync");
      }
   }

   // ========== PLAYER MARKER ==========

   private void createPlayerMarker(double x, double y) {
      try {
         String charName = "Player";
         if (UI.instance != null && UI.instance.gui != null) {
            try { charName = String.valueOf(UI.instance.gui.chrid); } catch (Exception e) {}
         }
         String title = charName + "(player)";
         String resp = postJSON(ENDPOINT + "/markers",
            "{\"action\":\"add\",\"x\":" + x + ",\"y\":" + y + ",\"title\":\"" + title + "\",\"type\":\"player\"}");
         if (resp != null && resp.contains("\"id\"")) {
            int idStart = resp.indexOf("\"id\":\"") + 6;
            int idEnd = resp.indexOf("\"", idStart);
            if (idStart > 5 && idEnd > idStart) {
               playerMarkerId = resp.substring(idStart, idEnd);
            }
         }
      } catch (Exception e) {}
   }

   private void deletePlayerMarker() {
      if (playerMarkerId != null) {
         try {
            postJSON(ENDPOINT + "/markers", "{\"action\":\"delete\",\"id\":\"" + playerMarkerId + "\"}");
         } catch (Exception e) {}
         playerMarkerId = null;
      }
   }

   // ========== MARKER FETCH ==========

    private void doMarkerFetch() {
       if (!tracking) return;
       try {
          if (UI.instance == null || UI.instance.gui == null || UI.instance.gui.map == null || UI.instance.gui.map.player() == null) return;
          Coord cc = UI.instance.gui.map.player().rc.div(MCache.tilesz);
          Coord off = new Coord();
          Coord tc = cc.add(off);
          Coord ncoord = tc.div(MCache.cmaps);
          int cx = (int)(ncoord.x + offset_x);
          int cy = (int)(ncoord.y + offset_y);
           // The 3x3 cells (cx-1, cx, cx+1) span [cx-1, cx+2). Markers sit on .0/.5
           // inside a cell, so an integer high bound drops the trailing .5 of the
           // last cell. Send the full span as decimals (server parses these as floats).
           String url = ENDPOINT + "/markers?x_min=" + (cx - 1.0)
              + "&x_max=" + (cx + 1.999)
              + "&y_min=" + (cy - 1.0)
              + "&y_max=" + (cy + 1.999);
          System.out.println("[Cartograph] FETCH URL: " + url);
          String resp = httpGet(url);
          System.out.println("[Cartograph] FETCH response: " + (resp == null ? "NULL" : resp.substring(0, Math.min(300, resp.length()))));
           if (resp != null) {
              List<WWMarker> fresh = new ArrayList<>();
              parseMarkers(resp, fresh);
              // Atomic swap: the GL thread never sees a half-filled list
              this.wwMarkers = fresh;
              System.out.println("[Cartograph] Parsed " + wwMarkers.size() + " markers total");
           } else {
             System.out.println("[Cartograph] FETCH FAILED - server not responding?");
          }
       } catch (Exception e) {
          System.out.println("[Cartograph] FETCH ERROR: " + e.getMessage());
       }
    }

   private static int skipWs(String s, int i) {
      while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
      return i;
   }

   /** Index just past the string literal whose opening quote is at i, or -1 if unterminated. */
   private static int endOfString(String s, int i) {
      i++;
      while (i < s.length()) {
         char ch = s.charAt(i);
         if (ch == '\\') { i += 2; continue; }
         if (ch == '"') return i + 1;
         i++;
      }
      return -1;
   }

   /** Index just past the object literal whose opening brace is at i (brace/quote aware), or -1. */
   private static int endOfObject(String s, int i) {
      int depth = 0;
      while (i < s.length()) {
         char ch = s.charAt(i);
         if (ch == '"') {
            int e = endOfString(s, i);
            if (e < 0) return -1;
            i = e;
            continue;
         }
         if (ch == '{') depth++;
         else if (ch == '}') {
            depth--;
            if (depth == 0) return i + 1;
         }
         i++;
      }
      return -1;
   }

   private static String unescape(String s) {
      if (s.indexOf('\\') < 0) return s;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
         char ch = s.charAt(i);
         if (ch == '\\' && i + 1 < s.length()) {
            char nx = s.charAt(++i);
            if (nx == 'n') sb.append('\n');
            else if (nx == 't') sb.append('\t');
            else if (nx == 'r') sb.append('\r');
            else if (nx == 'u' && i + 4 < s.length()) {
               try { sb.append((char)Integer.parseInt(s.substring(i + 1, i + 5), 16)); i += 4; }
               catch (Exception e) { sb.append(nx); }
            } else sb.append(nx);
         } else sb.append(ch);
      }
      return sb.toString();
   }

   private static void applyField(WWMarker m, String key, String val) {
      switch (key) {
         case "id": m.id = val; break;
         case "title": m.title = val; break;
         case "type": m.type = val; break;
         case "x": try { m.x = Double.parseDouble(val); } catch (Exception e) {} break;
         case "y": try { m.y = Double.parseDouble(val); } catch (Exception e) {} break;
      }
   }

   private static void parseFields(String obj, WWMarker m) {
      int i = 0;
      while (i < obj.length()) {
         if (obj.charAt(i) != '"') { i++; continue; }
         int ke = endOfString(obj, i);
         if (ke < 0) return;
         String key = obj.substring(i + 1, ke - 1);
         i = skipWs(obj, ke);
         if (i >= obj.length() || obj.charAt(i) != ':') continue;
         i = skipWs(obj, i + 1);
         if (i >= obj.length()) return;
         String val;
         if (obj.charAt(i) == '"') {
            int ve = endOfString(obj, i);
            if (ve < 0) return;
            val = unescape(obj.substring(i + 1, ve - 1));
            i = ve;
         } else {
            int vs = i;
            while (i < obj.length() && obj.charAt(i) != ',' && obj.charAt(i) != '}') i++;
            val = obj.substring(vs, i).trim();
         }
         applyField(m, key, val);
      }
   }

   private void parseMarkers(String json, List<WWMarker> out) {
      json = json.trim();
      if (!json.startsWith("[")) {
         System.out.println("[Cartograph] PARSE: not an array: " + json.substring(0, Math.min(100, json.length())));
         return;
      }
      int i = 0;
      int seq = 0;
      while (i < json.length()) {
         int objStart = json.indexOf('{', i);
         if (objStart < 0) break;
         int objEnd = endOfObject(json, objStart);
         if (objEnd < 0) break;
         WWMarker m = new WWMarker();
         parseFields(json.substring(objStart, objEnd), m);
         // Legacy app-created markers may have no id; give them a stable fallback
         // so Save/Delete still address them (the server persists real ids on read).
         if (m.id.isEmpty()) m.id = "legacy-" + seq + "-" + (int)(m.x * 100) + "-" + (int)(m.y * 100);
         m.color = WW_COLORS.getOrDefault(m.type, Color.WHITE);
         out.add(m);
         i = objEnd;
         seq++;
      }
   }

   // ========== MARKER CREATION ==========

   private void promptMarkerCreation(Coord gameTile) {
      // gameTile is already in micro-tiles (rc/11), divide by cmaps for precise grid coords
      double mx = (double) gameTile.x / MCache.cmaps.x + offset_x;
      double my = (double) gameTile.y / MCache.cmaps.y + offset_y;
      String json = "{\"action\":\"add\",\"x\":" + mx + ",\"y\":" + my + ",\"title\":\"Marker\",\"type\":\"interest\"}";
      System.out.println("[Cartograph] CREATE marker: " + json);
      String resp = postJSON(ENDPOINT + "/markers", json);
      System.out.println("[Cartograph] CREATE response: " + (resp == null ? "NULL" : resp));
         if (resp != null && resp.contains("\"id\"")) {
            int idStart = resp.indexOf("\"id\":\"") + 6;
            int idEnd = resp.indexOf("\"", idStart);
            String id = (idStart > 5 && idEnd > idStart) ? resp.substring(idStart, idEnd) : "";
            // No local copy: the server record is the only source of truth, otherwise a
            // client-side "Marker" ghost draws over the real name after an app rename.
            doMarkerFetch();
            selectById(id);
            ui.message("[Cartograph] Marker created.", GameUI.MsgType.INFO);
         } else if (resp == null) {
         ui.message("[Cartograph] Failed — is the server running?", GameUI.MsgType.INFO);
      }
   }

   // ========== HTTP HELPERS ==========

   private static String postJSON(String urlStr, String json) {
      try {
         URL url = new URL(urlStr);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Content-Type", "application/json");
         conn.setConnectTimeout(2000);
         conn.setReadTimeout(2000);
         conn.setDoOutput(true);
         OutputStream os = conn.getOutputStream();
         os.write(json.getBytes());
         os.flush();
         os.close();
         int code = conn.getResponseCode();
         if (code == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            return sb.toString();
         }
         conn.disconnect();
      } catch (Exception e) {}
      return null;
   }

   private static String httpGet(String urlStr) {
      try {
         URL url = new URL(urlStr);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.setConnectTimeout(2000);
         conn.setReadTimeout(2000);
         int code = conn.getResponseCode();
         if (code == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            return sb.toString();
         }
         conn.disconnect();
      } catch (Exception e) {}
      return null;
   }

   // ========== PERSISTENCE ==========

   private void saveCalibration() {
      try {
         File f = new File(CALIBRATION_FILE);
         f.getParentFile().mkdirs();
         PrintWriter pw = new PrintWriter(new FileWriter(f));
         pw.println("{\"offset_x\":" + offset_x + ",\"offset_y\":" + offset_y + "}");
         pw.close();
      } catch (Exception e) {}
   }

   private void loadCalibration() {
      try {
         File f = new File(CALIBRATION_FILE);
         if (f.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            br.close();
            if (line != null) {
               int xi = line.indexOf("\"offset_x\":");
               int yi = line.indexOf("\"offset_y\":");
               if (xi >= 0 && yi >= 0) {
                  offset_x = Double.parseDouble(line.substring(xi + 11, line.indexOf(',', xi)));
                  offset_y = Double.parseDouble(line.substring(yi + 11, line.indexOf('}', yi)));
               }
            }
         }
      } catch (Exception e) {}
   }

   // ========== UI HELPERS ==========

   // ========== EXISTING METHODS ==========

   // ========== SELECTION ==========

   /** A display object mirroring the current server record. */
   private CartographWindow.Marker newView(WWMarker m) {
      CartographWindow.Marker v = new CartographWindow.Marker(Coord.z, m.title);
      v.wwId = m.id;
      v.wwType = m.type;
      v.co = m.color;
      return v;
   }

   /** Select the marker with this id, populating the panel from live server data. */
   private void selectById(String id) {
      this.selectedId = id;
      for (WWMarker m : this.wwMarkers) {
         if (id != null && id.equals(m.id)) {
            this.setSelectedMarker(newView(m));
            return;
         }
      }
      this.setSelectedMarker(null);
   }

   /** Escape a value being embedded in a JSON string literal. */
   private static String jsonEsc(String s) {
      if (s == null) return "";
      return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
   }

   private void setSelectedMarker(CartographWindow.Marker selected) {
      this.selected_marker = selected;
      this.selectedId = (selected != null && selected.wwId != null && !selected.wwId.isEmpty())
         ? selected.wwId : null;
      this.nameEntry = null;
      if (this.marker_info != null) this.marker_info.destroy();
      this.marker_info = new Widget(new Coord(130, this.sz.y - 155), new Coord(270, 50), this) {
         @Override
         public void draw(GOut g) {
            g.chcolor(0, 0, 0, 128);
            g.frect(Coord.z, this.sz);
            g.chcolor();
            super.draw(g);
         }
      };
        new Label(new Coord(5, 5), this.marker_info, "Name:");
        if (this.selected_marker != null) {
           this.nameEntry = new TextEntry(new Coord(50, 5), 120, this.marker_info, this.selected_marker.name) {
              @Override
              public void activate(String text) { CartographWindow.this.selected_marker.changeName(text); }
           };
          // Type dropdown — cycles through types on click
          String currentType = this.selected_marker.wwType != null ? this.selected_marker.wwType : "interest";
          new Button(new Coord(180, 5), 80, this.marker_info, currentType) {
             @Override
             public void click() {
                String[] types = {"town", "player", "claim", "interest", "abandon", "event"};
                int idx = 0;
                for (int i = 0; i < types.length; i++) {
                   if (types[i].equals(CartographWindow.this.selected_marker.wwType)) { idx = i; break; }
                }
                idx = (idx + 1) % types.length;
                CartographWindow.this.selected_marker.wwType = types[idx];
                CartographWindow.this.selected_marker.co = WW_COLORS.getOrDefault(types[idx], Color.WHITE);
                this.change(types[idx]);
             }
          };
          // Row 2: Del + Save buttons
           new Button(new Coord(5, 25), 50, this.marker_info, "Del") {
              @Override
               public void click() {
                  String id = CartographWindow.this.selectedId;
                  if (id == null) return;
                  postJSON(ENDPOINT + "/markers", "{\"action\":\"delete\",\"id\":\"" + jsonEsc(id) + "\"}");
                  CartographWindow.this.setSelectedMarker(null);
                  CartographWindow.this.doMarkerFetch();
               }
           };
            new Button(new Coord(65, 25), 50, this.marker_info, "Save") {
               @Override
               public void click() {
                  if (CartographWindow.this.selected_marker == null) return;
                  String id = CartographWindow.this.selectedId;
                  if (id == null) {
                     ui.message("[Cartograph] Select a marker first.", GameUI.MsgType.ERROR);
                     return;
                  }
                  // Read the input box, not the model: TextEntry.activate() only fires
                  // on Enter, so selected_marker.name is stale until then.
                  String name = (CartographWindow.this.nameEntry != null)
                     ? CartographWindow.this.nameEntry.text
                     : CartographWindow.this.selected_marker.name;
                  String type = CartographWindow.this.selected_marker.wwType;
                  String json = "{\"action\":\"update\",\"id\":\"" + jsonEsc(id)
                     + "\",\"title\":\"" + jsonEsc(name)
                     + "\",\"type\":\"" + jsonEsc(type) + "\"}";
                  String resp = postJSON(ENDPOINT + "/markers", json);
                  if (resp != null) {
                     CartographWindow.this.selected_marker.changeName(name);
                     ui.message("[Cartograph] Marker saved.", GameUI.MsgType.INFO);
                  } else {
                     ui.message("[Cartograph] Save failed.", GameUI.MsgType.ERROR);
                  }
                  CartographWindow.this.doMarkerFetch();
               }
            };
       }
   }

   private String colorHex(Color co) {
      return "#" + Integer.toHexString(co.getRed()) + Integer.toHexString(co.getGreen()) + Integer.toHexString(co.getBlue());
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.cbtn) {
         if (this.justclose) this.destroy();
         else super.wdgmsg(sender, msg, args);
      }
   }

   public static void toggle() {
      UI ui = UI.instance;
      if (instance == null) {
         instance = new CartographWindow(ui.gui.sz.sub(600, 500).div(2), ui.gui);
      } else {
         ui.destroy(instance);
      }
   }

   @Override
   public void destroy() {
      if (syncTimer != null) syncTimer.stop();
      if (markerTimer != null) markerTimer.stop();
      deletePlayerMarker();
      instance = null;
      super.destroy();
   }

   public static void close() {
      if (instance != null) UI.instance.destroy(instance);
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      this.parent.setfocus(this);
      this.raise();
      if (button == 1) {
         this.ui.grabmouse(this);
         this.doff = c;
         if (c.isect(this.sz.sub(gzsz), gzsz)) { this.rsm = true; return true; }
      }
      return super.mousedown(c, button);
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      if (button == 1 && this.rsm) {
         this.ui.grabmouse(null);
         this.rsm = false;
         this.storeOpt("_sz", this.sz);
      }
      return super.mouseup(c, button);
   }

   @Override
   public void mousemove(Coord c) {
      if (this.rsm) {
         Coord d = c.sub(this.doff);
         Coord newsz = this.sz.add(d);
         newsz.x = Math.max(minsz.x, newsz.x);
         newsz.y = Math.max(minsz.y, newsz.y);
         this.doff = c;
         drawn.resize(newsz.sub(25, 170));
         this.sresize(newsz);
      } else {
         super.mousemove(c);
      }
   }

   public void updateCoords(Coord tile, Coord grid, Coord pixel) {
      String status = held ? "HELD" : (tracking ? "Syncing" : "Idle");
      String msg;
      if (tracking) {
         Coord cc = pixel.div(MCache.tilesz);
         Coord off = new Coord();
         Coord tc = cc.add(off);
         Coord ncoord = tc.div(MCache.cmaps);
         double cx = ncoord.x + offset_x;
         double cy = ncoord.y + offset_y;
         msg = String.format("Grid: %d, %d | Map: %.0f, %.0f | Offset: (%d, %d) | %s",
            ncoord.x, ncoord.y, cx, cy, (int)offset_x, (int)offset_y, status);
      } else {
         msg = String.format("Grid: %d, %d | Tile: %d, %d | Offset: (%d, %d) | %s",
            grid.x, grid.y, tile.x, tile.y, (int)offset_x, (int)offset_y, status);
      }
      if (this.coordLabel != null) this.coordLabel.settext(msg);
   }

   private void sresize(Coord sz) {
      IBox box;
      int th;
      if (this.cap == null) { box = wbox; th = 0; }
      else { box = topless; th = Window.th; }
      this.sz = sz;
      this.ctl = box.btloff().add(0, th);
      this.csz = sz.sub(box.bisz()).sub(0, th);
      this.atl = this.ctl.add(this.mrgn);
      this.asz = this.csz.sub(this.mrgn.mul(2));
      for (Widget ch = this.child; ch != null; ch = ch.next) ch.presize();
      // Reposition controls
      this.gridlines.c = new Coord(15, sz.y - 155);
      this.recenter.c = new Coord(15, sz.y - 135);
      this.syncBtn.c = new Coord(15, sz.y - 110);
      this.coordLabel.c = new Coord(130, sz.y - 98);
      this.mapXLabel.c = new Coord(15, sz.y - 78);
      this.mapXEntry.c = new Coord(35, sz.y - 78);
      this.mapYLabel.c = new Coord(115, sz.y - 78);
      this.mapYEntry.c = new Coord(135, sz.y - 78);
      this.calibrateBtn.c = new Coord(215, sz.y - 81);
      this.holdBtn.c = new Coord(305, sz.y - 81);
      this.marker_info.c = new Coord(130, sz.y - 155);
   }

   // ========== DRAWN MAP ==========

   private class DrawnMap extends Widget {
      Coord off = new Coord();
      boolean draw_grid = false;
      boolean save_image = false;
      private final Map<Coord, Defer.Future<LocalMiniMap.MapTile>> pcache = new LinkedHashMap<Coord, Defer.Future<LocalMiniMap.MapTile>>(9, 0.75F, true) {
         private static final long serialVersionUID = 2L;
      };

      private DrawnMap() {
         super(Coord.z, CartographWindow.this.sz.sub(25, 170), CartographWindow.this);
      }

      @Override
      public void draw(GOut og) {
         if (this.ui == null || this.ui.gui == null || this.ui.gui.map == null || this.ui.gui.map.player() == null) return;
         Coord cc = this.ui.gui.map.player().rc.div(MCache.tilesz);
         Coord plg = cc.div(MCache.cmaps);
         Coord tc = cc.add(this.off);
         Coord ulg = tc.div(MCache.cmaps);
         int dy = -tc.y + this.sz.y / 2;
         int dx = -tc.x + this.sz.x / 2;
         while (ulg.x * MCache.cmaps.x + dx > 0) ulg.x--;
         while (ulg.y * MCache.cmaps.y + dy > 0) ulg.y--;

         Coord grid = cc.div(MCache.cmaps);
         Coord pixel = this.ui.gui.map.player().rc;
         CartographWindow.this.updateCoords(cc, grid, pixel);

         final LocalMiniMap lmmap = this.ui.gui.mmap;
         Coord s = LocalMiniMap.bg.sz();
         for (int y = 0; y * s.y < this.sz.y; y++)
            for (int x = 0; x * s.x < this.sz.x; x++)
               og.image(LocalMiniMap.bg, new Coord(x * s.x, y * s.y));

         GOut g = og.reclipl(new Coord(), this.sz);
         g.gl.glPushMatrix();
         Coord cg = new Coord();
         synchronized (this.pcache) {
            for (cg.y = ulg.y; cg.y * MCache.cmaps.y + dy < this.sz.y; cg.y++) {
               for (cg.x = ulg.x; cg.x * MCache.cmaps.x + dx < this.sz.x; cg.x++) {
                  Defer.Future<LocalMiniMap.MapTile> f = this.pcache.get(cg);
                  final Coord tcg = new Coord(cg);
                  final Coord ul = cg.mul(MCache.cmaps);
                  int mdist = Math.abs(cg.x - plg.x) + Math.abs(cg.y - plg.y);
                  if (f == null && mdist <= 1) {
                     f = Defer.later(() -> {
                        BufferedImage img = lmmap.drawmap(ul, MCache.cmaps, true);
                        return img == null ? null : new LocalMiniMap.MapTile(new TexI(img), ul, tcg);
                     });
                     this.pcache.put(tcg, f);
                  }
                  if (f != null && f.done()) {
                     LocalMiniMap.MapTile mt = f.get();
                     if (mt == null) this.pcache.put(cg, null);
                     else g.image(mt.img, ul.add(tc.inv()).add(this.sz.div(2)));
                  }
               }
            }
         }

         // Grid lines + tile numbers
         if (this.draw_grid) {
            g.chcolor(255, 255, 255, 255);
            int startx = (g.sz.x / 2 - tc.x) % MCache.cmaps.x;
            startx = startx > 0 ? startx : startx + MCache.cmaps.x;
            int starty = (g.sz.y / 2 - tc.y) % MCache.cmaps.y;
            starty = starty > 0 ? starty : starty + MCache.cmaps.y;
            for (int x = startx; x < this.sz.x; x += MCache.cmaps.x)
               g.line(new Coord(x, 0), new Coord(x, this.sz.y), 1.0);
            for (int y = starty; y < this.sz.y; y += MCache.cmaps.y)
               g.line(new Coord(0, y), new Coord(this.sz.x, y), 1.0);
            // Tile numbers in center of each tile (map coords with offset)
            g.chcolor(255, 255, 255, 120);
            for (int x = startx; x < this.sz.x; x += MCache.cmaps.x) {
               for (int y = starty; y < this.sz.y; y += MCache.cmaps.y) {
                  int gridX = (tc.x + (x - this.sz.x / 2)) / MCache.cmaps.x;
                  int gridY = (tc.y + (y - this.sz.y / 2)) / MCache.cmaps.y;
                  int mapX = gridX + (int)offset_x;
                  int mapY = gridY + (int)offset_y;
                  String label = mapX + "," + mapY;
                  Tex textTex = Text.render(label).tex();
                  g.image(textTex, new Coord(x + 2, y + 2));
               }
            }
         }

         // Draw WitchWatchers markers (convert grid coords to micro-tiles, keep float precision)
         for (WWMarker m : CartographWindow.this.wwMarkers) {
            if ("player".equals(m.type)) continue; // player dot already shows position
            double markerMicroX = (m.x - offset_x) * MCache.cmaps.x;
            double markerMicroY = (m.y - offset_y) * MCache.cmaps.y;
            double sx = markerMicroX - tc.x + this.sz.x / 2.0;
            double sy = markerMicroY - tc.y + this.sz.y / 2.0;
            int ix = (int) sx;
            int iy = (int) sy;
            if (ix >= 0 && iy >= 0 && ix <= this.sz.x && iy <= this.sz.y) {
               Text mt = m.label();
               g.chcolor(24, 24, 16, 200);
               g.frect(new Coord(ix - 15, iy - 30), mt.sz().add(4, 4));
               g.chcolor(m.color);
               g.rect(new Coord(ix - 15, iy - 30), mt.sz().add(4, 4));
               g.line(new Coord(ix - 5, iy - 30 + mt.sz().y + 4), new Coord(ix, iy), 2.0);
               g.aimage(mt.tex(), new Coord(ix - 13, iy - 28), 0.0, 0.0);
            }
         }

         // Draw player dot at tile center (0.5 offset)
         double playerScreenX = (cc.x + 0.5 - tc.x) + this.sz.x / 2.0;
         double playerScreenY = (cc.y + 0.5 - tc.y) + this.sz.y / 2.0;
         g.chcolor(33, 150, 243, 220);
         g.frect(new Coord((int)playerScreenX - 6, (int)playerScreenY - 6), new Coord(12, 12));
         g.chcolor(255, 255, 255, 255);
         g.frect(new Coord((int)playerScreenX - 3, (int)playerScreenY - 3), new Coord(6, 6));

         g.gl.glPopMatrix();
         if (this.save_image) {
            String path = System.getProperty("user.home") + "/Salem/map/";
            new File(path).mkdirs();
            String filename = Utils.current_date() + ".png";
            try {
               BufferedImage bi = g.getimage();
               Screenshooter.png.write(new FileOutputStream(path + filename), bi, new Screenshooter.Shot(new TexI(bi), null));
            } catch (IOException e) {}
            this.save_image = false;
         }
      }

      @Override
      public boolean mousedown(Coord c, int button) {
         this.parent.setfocus(this);
         this.raise();
         if (button == 1 || button == 2) {
            // Left click always selects. Right click selects an existing marker, and
            // only creates a new one when the click is not on top of one.
            if (!selectMarkerAt(c) && button == 2) createMarkerAt(c);
         }
         if (button == 3) {
            CartographWindow.this.dm = true;
            this.ui.grabmouse(this);
            CartographWindow.this.doff = c;
            CartographWindow.this.mmv = false;
            return true;
         }
         return super.mousedown(c, button);
      }

      @Override
      public boolean mouseup(Coord c, int button) {
         if (button == 3) {
            if (!CartographWindow.this.mmv) {
               Coord gameTile = c.sub(this.sz.div(2)).add(this.off).add(this.ui.gui.map.player().rc.div(MCache.tilesz));
               if (CartographWindow.this.selected_marker != null) {
                  CartographWindow.this.selected_marker.loc = gameTile;
               } else {
                  CartographWindow.this.promptMarkerCreation(gameTile);
               }
            }
            CartographWindow.this.dm = false;
            this.ui.grabmouse(null);
            return true;
         }
         return super.mouseup(c, button);
      }

      @Override
      public void mousemove(Coord c) {
         CartographWindow.this.mmv = true;
         if (CartographWindow.this.dm) {
            Coord d = c.sub(CartographWindow.this.doff);
            this.off = this.off.sub(d);
            CartographWindow.this.doff = c;
         } else super.mousemove(c);
      }

      /** Hit-test the marker boxes the renderer actually drew. Returns true if one was selected. */
      private boolean selectMarkerAt(Coord c) {
         if (this.ui == null || this.ui.gui == null || this.ui.gui.map == null || this.ui.gui.map.player() == null) return false;
         CartographWindow.Marker selected = null;
         Coord cc2 = this.ui.gui.map.player().rc.div(MCache.tilesz);
         Coord tc2 = cc2.add(this.off);
         List<WWMarker> snap = CartographWindow.this.wwMarkers;
         for (WWMarker m : snap) {
            if ("player".equals(m.type)) continue; // hidden, so not clickable either
            double markerMicroX = (m.x - offset_x) * MCache.cmaps.x;
            double markerMicroY = (m.y - offset_y) * MCache.cmaps.y;
            double sx = markerMicroX - tc2.x + this.sz.x / 2.0;
            double sy = markerMicroY - tc2.y + this.sz.y / 2.0;
            int ix = (int) sx;
            int iy = (int) sy;
            Text mt = m.label();
            Coord box = new Coord(ix - 15, iy - 30);
            Coord bsz = mt.sz().add(4, 4);
            if (c.x >= box.x && c.y >= box.y && c.x <= box.x + bsz.x && c.y <= box.y + bsz.y) {
               selected = CartographWindow.this.newView(m);
               break;
            }
         }
         CartographWindow.this.setSelectedMarker(selected);
         return selected != null;
      }

      private void createMarkerAt(Coord c) {
         if (this.ui == null || this.ui.gui == null || this.ui.gui.map == null || this.ui.gui.map.player() == null) return;
         Coord gameTile = c.sub(this.sz.div(2)).add(this.off).add(this.ui.gui.map.player().rc.div(MCache.tilesz));
         CartographWindow.this.promptMarkerCreation(gameTile);
      }

      public void savePicture() { this.save_image = true; }
   }

   // ========== INNER CLASSES ==========

   private class Marker {
      Coord loc;
      String name;
      Text t;
      Color co;
      String wwId = "";
      String wwType = "interest";
      public Marker(Coord c, String s) { this.loc = c; this.name = s; this.co = Color.WHITE; this.t = foundry.render(s); }
      public void changeName(String s) { this.name = s; this.t = foundry.render(s); }
   }

   private static class WWMarker {
      String id = "", title = "", type = "interest";
      double x = 0, y = 0;
      Color color = Color.WHITE;
      private Text cached = null;
      private String cachedFor = null;

      /** Rendered label, cached: re-rendering every frame would leak a GL texture per frame. */
      Text label() {
         if (this.cached == null || !this.title.equals(this.cachedFor)) {
            this.cached = foundry.render(this.title);
            this.cachedFor = this.title;
         }
         return this.cached;
      }
   }
}
