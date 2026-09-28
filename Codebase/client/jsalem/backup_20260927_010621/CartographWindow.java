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
   private final ArrayList<CartographWindow.Marker> markers = new ArrayList<>();
   private CartographWindow.Marker selected_marker = null;
   private static final RichText.Foundry foundry = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 12);
   private static CartographWindow.DrawnMap drawn;
   private CheckBox gridlines;
   private Button recenter;
   private Button save;
   private Widget marker_info;
   private Label coordLabel;

   // Sync UI
   private TextEntry mapXEntry, mapYEntry;
   private Button calibrateBtn, holdBtn, syncBtn;
   private Label statusLabel;

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

   // WitchWatchers markers
   private List<WWMarker> wwMarkers = new ArrayList<>();

   // Endpoint
   private static final String ENDPOINT = "http://127.0.0.1:18321";
   private static final String CALIBRATION_FILE = System.getProperty("user.home") + "/Salem/cartograph_calibrations.json";

   // Map drag state
   boolean mmv = false;
   boolean rsm = false;
   private static Coord gzsz = new Coord(15, 15);
   private static Coord minsz = new Coord(500, 360);

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
      super(c, new Coord(600, 500), parent, "Cartograph");
      this.justclose = true;
      drawn = new CartographWindow.DrawnMap();

      // Grid lines checkbox
      this.gridlines = new CheckBox(new Coord(15, this.sz.y - 205), this, "Grid lines") {
         @Override
         public void changed(boolean val) { CartographWindow.drawn.draw_grid = val; }
      };
      this.gridlines.a = false;

      // Re-center button
      this.recenter = new Button(new Coord(15, this.sz.y - 185), 100, this, "Re-center") {
         @Override
         public void click() { CartographWindow.drawn.off = Coord.z; }
      };

      // Save button
      this.save = new Button(new Coord(15, this.sz.y - 160), 100, this, "Save map") {
         @Override
         public void click() { CartographWindow.drawn.savePicture(); }
      };

      // Coord readout
      this.coordLabel = new Label(new Coord(15, this.sz.y - 140), this, "Coords: ---");

      // Sync UI
      new Label(new Coord(15, this.sz.y - 120), this, "Map X:");
      this.mapXEntry = new TextEntry(new Coord(50, this.sz.y - 120), 80, this, "0");
      new Label(new Coord(140, this.sz.y - 120), this, "Y:");
      this.mapYEntry = new TextEntry(new Coord(160, this.sz.y - 120), 80, this, "0");

      this.calibrateBtn = new Button(new Coord(250, this.sz.y - 123), 80, this, "Calibrate") {
         @Override
         public void click() { doCalibrate(); }
      };

      this.statusLabel = new Label(new Coord(15, this.sz.y - 100), this, "Offset: (0, 0) | Status: Idle");

      this.holdBtn = new Button(new Coord(15, this.sz.y - 80), 80, this, "Hold") {
         @Override
         public void click() { doHoldResume(); }
      };

      this.syncBtn = new Button(new Coord(105, this.sz.y - 80), 80, this, "Sync") {
         @Override
         public void click() { doSyncToggle(); }
      };

      // Load saved calibration
      loadCalibration();

      // Timers
      syncTimer = new Timer(2000, e -> doSyncTick());
      syncTimer.setCoalesce(true);
      markerTimer = new Timer(10000, e -> doMarkerFetch());
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
         // Exact plugin code
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
         updateStatusLabel();
         ui.message("[Cartograph] Calibrated. Offset: (" + (int)offset_x + ", " + (int)offset_y + ")", GameUI.MsgType.INFO);

         // Create player marker on server
         createPlayerMarker(ncoord.x + offset_x, ncoord.y + offset_y);

         // Start sync and marker timers
         if (!syncTimer.isRunning()) syncTimer.start();
         if (!markerTimer.isRunning()) markerTimer.start();
         doMarkerFetch();
      } catch (NumberFormatException ex) {
         ui.message("[Cartograph] Invalid coordinates.", GameUI.MsgType.ERROR);
      }
   }

   private void doSyncToggle() {
      if (tracking) {
         // Stop sync
         tracking = false;
         syncTimer.stop();
         syncBtn.change("Sync");
         // Delete player marker
         deletePlayerMarker();
         updateStatusLabel();
         ui.message("[Cartograph] Sync stopped.", GameUI.MsgType.INFO);
      } else {
         // Start sync
         if (offset_x == 0 && offset_y == 0) {
            ui.message("[Cartograph] Calibrate first!", GameUI.MsgType.ERROR);
            return;
         }
         tracking = true;
         syncBtn.change("Stop");
         holdBtn.change("Hold");
         updateStatusLabel();
         // Create player marker
         Coord cc = UI.instance.gui.map.player().rc.div(MCache.tilesz);
         Coord off = new Coord();
         Coord tc = cc.add(off);
         Coord ncoord = tc.div(MCache.cmaps);
         createPlayerMarker(ncoord.x + offset_x, ncoord.y + offset_y);
         // Start timers
         if (!syncTimer.isRunning()) syncTimer.start();
         if (!markerTimer.isRunning()) markerTimer.start();
         doMarkerFetch();
         ui.message("[Cartograph] Sync started.", GameUI.MsgType.INFO);
      }
   }

   private void doHoldResume() {
      if (held) {
         // Resume
          held = false;
          holdBtn.change("Hold");
          // Recalculate offset from held map coords
          if (UI.instance != null && UI.instance.gui != null && UI.instance.gui.map != null && UI.instance.gui.map.player() != null) {
             Coord cc = UI.instance.gui.map.player().rc.div(MCache.tilesz);
             Coord off = new Coord();
             Coord tc = cc.add(off);
             Coord ncoord = tc.div(MCache.cmaps);
             offset_x = heldMapX - ncoord.x;
             offset_y = heldMapY - ncoord.y;
            saveCalibration();
            updateStatusLabel();
            ui.message("[Cartograph] Resumed. New offset: (" + (int)offset_x + ", " + (int)offset_y + ")", GameUI.MsgType.INFO);
         }
         if (!syncTimer.isRunning()) syncTimer.start();
      } else {
         // Hold
          held = true;
          holdBtn.change("Resume");
          syncTimer.stop();
          // Store current corrected coords
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
         updateStatusLabel();
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
         // Only send if tile changed
         if (cx == lastSentX && cy == lastSentY) return;
         lastSentX = cx;
         lastSentY = cy;
         // Update player marker on server
         if (playerMarkerId != null) {
            postJSON(ENDPOINT + "/markers", "{\"action\":\"update\",\"id\":\"" + playerMarkerId + "\",\"x\":" + cx + ",\"y\":" + cy + "}");
         }
         // Send position with center flag
         postJSON(ENDPOINT + "/position", "{\"x\":" + cx + ",\"y\":" + cy + ",\"name\":\"Player\",\"center\":true}");
      } catch (Exception ex) {
         ui.message("[Cartograph] Sync failed — is the server running?", GameUI.MsgType.INFO);
         syncTimer.stop();
         tracking = false;
         syncBtn.change("Sync");
         updateStatusLabel();
      }
   }

   // ========== PLAYER MARKER ==========

   private void createPlayerMarker(double x, double y) {
      try {
         String charName = "Player";
         if (UI.instance != null && UI.instance.gui != null) {
            try { charName = UI.instance.gui.chrid + ""; } catch (Exception e) {}
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
         String resp = httpGet(ENDPOINT + "/markers?x_min=" + (ncoord.x - 1) + "&x_max=" + (ncoord.x + 1) + "&y_min=" + (ncoord.y - 1) + "&y_max=" + (ncoord.y + 1));
         if (resp != null) {
            wwMarkers.clear();
            parseMarkers(resp);
         }
      } catch (Exception e) {}
   }

   private void parseMarkers(String json) {
      // Simple JSON parser for marker array
      json = json.trim();
      if (!json.startsWith("[")) return;
      json = json.substring(1, json.length() - 1);
      int i = 0;
      while (i < json.length()) {
         int objStart = json.indexOf('{', i);
         if (objStart < 0) break;
         int objEnd = json.indexOf('}', objStart);
         if (objEnd < 0) break;
         String obj = json.substring(objStart + 1, objEnd);
         WWMarker m = new WWMarker();
         // Parse fields
         for (String field : obj.split(",")) {
            String[] kv = field.split(":");
            if (kv.length != 2) continue;
            String key = kv[0].trim().replace("\"", "");
            String val = kv[1].trim().replace("\"", "");
            switch (key) {
               case "id": m.id = val; break;
               case "title": m.title = val; break;
               case "type": m.type = val; break;
               case "x": try { m.x = Double.parseDouble(val); } catch (Exception e) {} break;
               case "y": try { m.y = Double.parseDouble(val); } catch (Exception e) {} break;
            }
         }
         m.color = WW_COLORS.getOrDefault(m.type, Color.WHITE);
         wwMarkers.add(m);
         i = objEnd + 1;
      }
   }

   // ========== MARKER CREATION (right-click) ==========

   private void promptMarkerCreation(Coord gameTile) {
      // Simple name input — type defaults to "interest"
      String title = "Marker";
      double mx = gameTile.x + offset_x;
      double my = gameTile.y + offset_y;
      String resp = postJSON(ENDPOINT + "/markers",
         "{\"action\":\"add\",\"x\":" + mx + ",\"y\":" + my + ",\"title\":\"" + title + "\",\"type\":\"interest\"}");
      if (resp != null && resp.contains("\"ok\"")) {
         ui.message("[Cartograph] Marker created at (" + (int)mx + ", " + (int)my + ")", GameUI.MsgType.INFO);
         doMarkerFetch();
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
               // Simple parse
               int xi = line.indexOf("\"offset_x\":");
               int yi = line.indexOf("\"offset_y\":");
               if (xi >= 0 && yi >= 0) {
                  offset_x = Double.parseDouble(line.substring(xi + 11, line.indexOf(',', xi)));
                  offset_y = Double.parseDouble(line.substring(yi + 11, line.indexOf('}', yi)));
                  updateStatusLabel();
               }
            }
         }
      } catch (Exception e) {}
   }

   // ========== UI HELPERS ==========

   private void updateStatusLabel() {
      String status;
      if (held) status = "Held";
      else if (tracking) status = "Tracking";
      else status = "Idle";
      statusLabel.settext("Offset: (" + (int)offset_x + ", " + (int)offset_y + ") | " + status);
   }

   // ========== EXISTING METHODS ==========

   private void setSelectedMarker(CartographWindow.Marker selected) {
      this.selected_marker = selected;
      if (this.marker_info != null) this.marker_info.destroy();
      this.marker_info = new Widget(new Coord(130, this.sz.y - 205), new Coord(350, 85), this) {
         @Override
         public void draw(GOut g) {
            g.chcolor(0, 0, 0, 128);
            g.frect(Coord.z, this.sz);
            g.chcolor();
            super.draw(g);
         }
      };
      new Label(new Coord(10, 10), this.marker_info, "Marker info:");
      if (this.selected_marker != null) {
         new Label(new Coord(30, 30), this.marker_info, "Name:");
         new TextEntry(new Coord(80, 30), 120, this.marker_info, this.selected_marker.name) {
            @Override
            public void activate(String text) { CartographWindow.this.selected_marker.changeName(text); }
         };
         new Label(new Coord(30, 60), this.marker_info, "Color:");
         new TextEntry(new Coord(80, 60), 120, this.marker_info, this.colorHex(this.selected_marker.co)) {
            @Override
            public void activate(String text) {
               try { CartographWindow.this.selected_marker.co = Color.decode(text); } catch (Exception e) {}
            }
         };
         new Button(new Coord(250, 45), 50, this.marker_info, "Del") {
            @Override
            public void click() {
               CartographWindow.this.markers.remove(CartographWindow.this.selected_marker);
               CartographWindow.this.setSelectedMarker(null);
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
      // Stop timers
      if (syncTimer != null) syncTimer.stop();
      if (markerTimer != null) markerTimer.stop();
      // Delete player marker on close
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
         drawn.resize(newsz.sub(25, 200));
         this.sresize(newsz);
      } else {
         super.mousemove(c);
      }
   }

   public void updateCoords(Coord tile, Coord grid, Coord pixel) {
      String msg;
      if (tracking) {
         Coord cc = pixel.div(MCache.tilesz);
         Coord off = new Coord();
         Coord tc = cc.add(off);
         Coord ncoord = tc.div(MCache.cmaps);
         double cx = ncoord.x + offset_x;
         double cy = ncoord.y + offset_y;
         msg = String.format("Grid: %d, %d | Map: %.0f, %.0f | %s", ncoord.x, ncoord.y, cx, cy, held ? "HELD" : "Syncing");
      } else {
         msg = String.format("Tile: %d, %d | Grid: %d, %d | Pixel: %d, %d", tile.x, tile.y, grid.x, grid.y, pixel.x, pixel.y);
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
      this.gridlines.c = new Coord(15, sz.y - 205);
      this.recenter.c = new Coord(15, sz.y - 185);
      this.save.c = new Coord(15, sz.y - 160);
      this.coordLabel.c = new Coord(15, sz.y - 140);
      this.mapXEntry.c = new Coord(50, sz.y - 120);
      this.mapYEntry.c = new Coord(160, sz.y - 120);
      this.calibrateBtn.c = new Coord(250, sz.y - 123);
      this.statusLabel.c = new Coord(15, sz.y - 100);
      this.holdBtn.c = new Coord(15, sz.y - 80);
      this.syncBtn.c = new Coord(105, sz.y - 80);
      this.marker_info.c = new Coord(130, sz.y - 205);
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
         super(Coord.z, CartographWindow.this.sz.sub(25, 200), CartographWindow.this);
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

         // Grid lines
         if (this.draw_grid) {
            g.chcolor(255, 255, 255, 255);
            int startx = (g.sz.x / 2 - tc.x) % MCache.cmaps.x;
            startx = startx > 0 ? startx : startx + MCache.cmaps.x;
            for (int x = startx; x < this.sz.x; x += MCache.cmaps.x)
               g.line(new Coord(x, 0), new Coord(x, this.sz.y), 1.0);
            int starty = (g.sz.y / 2 - tc.y) % MCache.cmaps.y;
            starty = starty > 0 ? starty : starty + MCache.cmaps.y;
            for (int y = starty; y < this.sz.y; y += MCache.cmaps.y)
               g.line(new Coord(0, y), new Coord(this.sz.x, y), 1.0);
         }

         // Draw game markers (local)
         for (CartographWindow.Marker m : CartographWindow.this.markers) {
            Coord onscreen = m.loc.sub(this.off).sub(cc).add(this.sz.mul(0.5));
            if (onscreen.x >= 0 && onscreen.y >= 0 && onscreen.x <= this.sz.x && onscreen.y <= this.sz.y) {
               g.chcolor(24, 24, 16, 200);
               g.frect(onscreen.add(-15, -30), m.t.sz().add(4, 4));
               g.chcolor(m.co);
               g.rect(onscreen.add(-15, -30), m.t.sz().add(4, 4));
               g.line(onscreen.add(-5, -30 + m.t.sz().y + 4), onscreen, 2.0);
               g.aimage(m.t.tex(), onscreen.add(-13, -28), 0.0, 0.0);
            }
         }

         // Draw WitchWatchers markers
         for (WWMarker m : CartographWindow.this.wwMarkers) {
            double mx = m.x - offset_x;
            double my = m.y - offset_y;
            Coord onscreen = new Coord((int)(mx - tc.x + this.sz.x / 2), (int)(my - tc.y + this.sz.y / 2));
            if (onscreen.x >= 0 && onscreen.y >= 0 && onscreen.x <= this.sz.x && onscreen.y <= this.sz.y) {
               Text mt = foundry.render(m.title);
               g.chcolor(24, 24, 16, 200);
               g.frect(onscreen.add(-15, -30), mt.sz().add(4, 4));
               g.chcolor(m.color);
               g.rect(onscreen.add(-15, -30), mt.sz().add(4, 4));
               g.line(onscreen.add(-5, -30 + mt.sz().y + 4), onscreen, 2.0);
               g.aimage(mt.tex(), onscreen.add(-13, -28), 0.0, 0.0);
            }
         }

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
         if (button == 2) {
            CartographWindow.Marker selected = null;
            for (CartographWindow.Marker m : CartographWindow.this.markers) {
               Coord onscreen = m.loc.sub(this.off).add(this.sz.mul(0.5)).sub(this.ui.gui.map.player().rc.div(MCache.tilesz));
               if (onscreen.x >= 15 && onscreen.y >= 30 && onscreen.x <= this.sz.x - m.t.sz().x + 11 && onscreen.y <= this.sz.y) {
                  Coord c1 = onscreen.add(-15, -30);
                  Coord c2 = c1.add(m.t.sz().add(4, 4));
                  if (c.x > c1.x && c.y > c1.y && c.y < c2.y && c.x < c2.x) selected = m;
               }
            }
            CartographWindow.this.setSelectedMarker(selected);
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
                  // Create marker on WitchWatchers
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

      public void savePicture() { this.save_image = true; }
   }

   // ========== INNER CLASSES ==========

   private class Marker {
      Coord loc;
      String name;
      Text t;
      Color co;
      public Marker(Coord c, String s) { this.loc = c; this.name = s; this.co = Color.WHITE; this.t = foundry.render(s); }
      public void changeName(String s) { this.name = s; this.t = foundry.render(s); }
   }

   private class WWMarker {
      String id = "", title = "", type = "interest";
      double x = 0, y = 0;
      Color color = Color.WHITE;
   }
}
