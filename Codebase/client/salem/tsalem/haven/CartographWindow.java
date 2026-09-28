package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CartographWindow extends Window {
  public static CartographWindow instance = null;
  
  private final ArrayList<Marker> markers = new ArrayList<>();
  
  private Marker selected_marker = null;
  
  private class Marker {
    Coord loc;
    
    String name;
    
    Text t;
    
    Color co;
    
    public Marker(Coord c, String s) {
      this.loc = c;
      this.name = s;
      this.co = Color.WHITE;
      this.t = CartographWindow.foundry.render(s);
    }
    
    public void changeName(String s) {
      this.name = s;
      this.t = CartographWindow.foundry.render(s);
    }
  }
  
  private static final RichText.Foundry foundry = new RichText.Foundry(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(12) });
  
  private static DrawnMap drawn;
  
  private final CheckBox gridlines;
  
  private final Button recenter;
  
  private final Button save;
  
  private OptWnd2.Frame marker_info;
  
  boolean mmv = false;
  
  boolean rsm = false;
  
  private static Coord gzsz = new Coord(15, 15);
  
  private static Coord minsz = new Coord(500, 360);
  
  private class DrawnMap extends Widget {
    Coord off = new Coord();
    
    boolean draw_grid = false;
    
    boolean save_image = false;
    
    private final Map<Coord, Defer.Future<LocalMiniMap.MapTile>> pcache = new LinkedHashMap<Coord, Defer.Future<LocalMiniMap.MapTile>>(9, 0.75F, true) {
        private static final long serialVersionUID = 2L;
      };
    
    public void draw(GOut og) {
      if (this.ui == null || this.ui.gui == null || this.ui.gui.map == null || this.ui.gui.map.player() == null)
        return; 
      Coord cc = (this.ui.gui.map.player()).rc.div(MCache.tilesz);
      Coord plg = cc.div(MCache.cmaps);
      Coord tc = cc.add(this.off);
      Coord ulg = tc.div(MCache.cmaps);
      int dy = -tc.y + this.sz.y / 2;
      int dx = -tc.x + this.sz.x / 2;
      while (ulg.x * MCache.cmaps.x + dx > 0)
        ulg.x--; 
      while (ulg.y * MCache.cmaps.y + dy > 0)
        ulg.y--; 
      final LocalMiniMap lmmap = this.ui.gui.mmap;
      Coord s = LocalMiniMap.bg.sz();
      for (int y = 0; y * s.y < this.sz.y; y++) {
        for (int x = 0; x * s.x < this.sz.x; x++)
          og.image(LocalMiniMap.bg, new Coord(x * s.x, y * s.y)); 
      } 
      GOut g = og.reclipl(new Coord(), this.sz);
      g.gl.glPushMatrix();
      Coord cg = new Coord();
      synchronized (this.pcache) {
        for (cg.y = ulg.y; cg.y * MCache.cmaps.y + dy < this.sz.y; cg.y++) {
          for (cg.x = ulg.x; cg.x * MCache.cmaps.x + dx < this.sz.x; cg.x++) {
            Defer.Future<LocalMiniMap.MapTile> f = this.pcache.get(cg);
            final Coord tcg = new Coord(cg);
            final Coord ul = cg.mul(MCache.cmaps);
            if (f == null && cg.manhattan2(plg) <= 1) {
              f = Defer.later(new Defer.Callable<LocalMiniMap.MapTile>() {
                    public LocalMiniMap.MapTile call() {
                      BufferedImage img = lmmap.drawmap(ul, MCache.cmaps, true);
                      if (img == null)
                        return null; 
                      return new LocalMiniMap.MapTile(new TexI(img), ul, tcg);
                    }
                  });
              this.pcache.put(tcg, f);
            } 
            if (f != null && f.done()) {
              LocalMiniMap.MapTile mt = f.get();
              if (mt == null) {
                this.pcache.put(cg, null);
              } else {
                Tex img = mt.img;
                g.image(img, ul.add(tc.inv()).add(this.sz.div(2)));
              } 
            } 
          } 
        } 
      } 
      if (this.draw_grid) {
        g.chcolor(255, 255, 255, 255);
        int startx = (g.sz.x / 2 - tc.x) % MCache.cmaps.x;
        startx = (startx > 0) ? startx : (startx + MCache.cmaps.x);
        int x;
        for (x = startx; x < this.sz.x; x += MCache.cmaps.x)
          g.line(new Coord(x, 0), new Coord(x, this.sz.y), 1.0D); 
        int starty = (g.sz.y / 2 - tc.y) % MCache.cmaps.y;
        starty = (starty > 0) ? starty : (starty + MCache.cmaps.y);
        int i;
        for (i = starty; i < this.sz.y; i += MCache.cmaps.y)
          g.line(new Coord(0, i), new Coord(this.sz.x, i), 1.0D); 
      } 
      for (CartographWindow.Marker m : CartographWindow.this.markers) {
        Coord onscreen = m.loc.sub(this.off).sub(cc).add(this.sz.mul(0.5D));
        if (onscreen.x < 15 || onscreen.y < 30 || onscreen.x > this.sz.x - (m.t.sz()).x + 11 || onscreen.y > this.sz.y)
          continue; 
        g.chcolor(24, 24, 16, 200);
        g.frect(onscreen.add(-15, -30), m.t.sz().add(4, 4));
        g.chcolor(m.co);
        g.rect(onscreen.add(-15, -30), m.t.sz().add(4, 4));
        g.line(onscreen.add(-5, -30 + (m.t.sz()).y + 4), onscreen, 2.0D);
        g.aimage(m.t.tex(), onscreen.add(-13, -28), 0.0D, 0.0D);
      } 
      g.gl.glPopMatrix();
      if (this.save_image) {
        String path = String.format("%s/map/", new Object[] { Config.userhome });
        String filename = String.format("%s.png", new Object[] { Utils.current_date() });
        try {
          BufferedImage bi = g.getimage();
          Screenshooter.png.write(new FileOutputStream(path + filename), bi, new Screenshooter.Shot(new TexI(bi), null));
        } catch (IOException iOException) {}
        this.save_image = false;
      } 
    }
    
    public boolean mousedown(Coord c, int button) {
      this.parent.setfocus(this);
      raise();
      if (button == 2) {
        CartographWindow.Marker selected = null;
        for (CartographWindow.Marker m : CartographWindow.this.markers) {
          Coord onscreen = m.loc.sub(this.off).add(this.sz.mul(0.5D)).sub((this.ui.gui.map.player()).rc.div(MCache.tilesz));
          if (onscreen.x < 15 || onscreen.y < 30 || onscreen.x > this.sz.x - (m.t.sz()).x + 11 || onscreen.y > this.sz.y)
            continue; 
          Coord c1 = onscreen.add(-15, -30);
          Coord c2 = c1.add(m.t.sz().add(4, 4));
          if (c.x > c1.x && c.y > c1.y && c.y < c2.y && c.x < c2.x)
            selected = m; 
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
    
    public boolean mouseup(Coord c, int button) {
      if (button == 3) {
        if (!CartographWindow.this.mmv)
          if (CartographWindow.this.selected_marker != null) {
            CartographWindow.this.selected_marker.loc = c.sub(this.sz.div(2)).add(this.off).add((this.ui.gui.map.player()).rc.div(MCache.tilesz));
          } else {
            CartographWindow.Marker newm = new CartographWindow.Marker(c.sub(this.sz.div(2)).add(this.off).add((this.ui.gui.map.player()).rc.div(MCache.tilesz)), "Marker");
            CartographWindow.this.markers.add(newm);
            CartographWindow.this.setSelectedMarker(newm);
          }  
        CartographWindow.this.dm = false;
        this.ui.grabmouse(null);
        return true;
      } 
      return super.mouseup(c, button);
    }
    
    public void mousemove(Coord c) {
      CartographWindow.this.mmv = true;
      if (CartographWindow.this.dm) {
        Coord d = c.sub(CartographWindow.this.doff);
        this.off = this.off.sub(d);
        CartographWindow.this.doff = c;
        return;
      } 
      super.mousemove(c);
    }
    
    public void savePicture() {
      this.save_image = true;
    }
    
    private DrawnMap() {
      super(Coord.z, CartographWindow.this.sz.sub(25, 150), CartographWindow.this);
    }
  }
  
  public CartographWindow(Coord c, Widget parent) {
    super(c, new Coord(500, 360), parent, "Cartograph");
    this.justclose = true;
    drawn = new DrawnMap();
    this.gridlines = new CheckBox(new Coord(15, this.sz.y - 145), this, "Display grid lines") {
        public void changed(boolean val) {
          CartographWindow.drawn.draw_grid = val;
        }
      };
    this.gridlines.a = false;
    this.recenter = new Button(new Coord(15, this.sz.y - 120), Integer.valueOf(100), this, "Re-center") {
        public void click() {
          CartographWindow.drawn.off = Coord.z;
        }
      };
    this.save = new Button(new Coord(15, this.sz.y - 80), Integer.valueOf(100), this, "Save map to file") {
        public void click() {
          CartographWindow.drawn.savePicture();
        }
      };
    setSelectedMarker(this.selected_marker);
  }
  
  private void setSelectedMarker(Marker selected) {
    this.selected_marker = selected;
    if (this.marker_info != null)
      this.marker_info.destroy(); 
    this.marker_info = new OptWnd2.Frame(new Coord(130, this.sz.y - 145), new Coord(350, 85), this) {
        public void draw(GOut og) {
          super.draw(og);
        }
      };
    new Label(new Coord(10, 10), this.marker_info, "Marker info:");
    if (this.selected_marker != null) {
      new Label(new Coord(30, 30), this.marker_info, "Marker name:");
      new TextEntry(new Coord(110, 30), 100, this.marker_info, this.selected_marker.name) {
          public void activate(String text) {
            CartographWindow.this.selected_marker.changeName(text);
          }
        };
      new Label(new Coord(30, 60), this.marker_info, "Marker color:");
      new TextEntry(new Coord(110, 60), 100, this.marker_info, colorHex(this.selected_marker.co)) {
          public void activate(String text) {
            Color c = null;
            try {
              c = Color.decode(text);
            } catch (NumberFormatException numberFormatException) {}
            if (c != null)
              CartographWindow.this.selected_marker.co = c; 
          }
        };
      new Button(new Coord(250, 45), Integer.valueOf(50), this.marker_info, "Delete") {
          public void click() {
            CartographWindow.this.markers.remove(CartographWindow.this.selected_marker);
            CartographWindow.this.setSelectedMarker((CartographWindow.Marker)null);
          }
        };
    } 
  }
  
  private String colorHex(Color co) {
    String s = "#" + Integer.toHexString(co.getRed()) + Integer.toHexString(co.getGreen()) + Integer.toHexString(co.getBlue());
    return s;
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.cbtn)
      super.wdgmsg(sender, msg, args); 
  }
  
  public static void toggle() {
    UI ui = UI.instance;
    if (instance == null) {
      instance = new CartographWindow(ui.gui.sz.sub(500, 360).div(2), ui.gui);
    } else {
      ui.destroy(instance);
    } 
  }
  
  public void destroy() {
    instance = null;
    super.destroy();
  }
  
  public static void close() {
    if (instance != null) {
      UI ui = UI.instance;
      ui.destroy(instance);
    } 
  }
  
  public boolean mousedown(Coord c, int button) {
    this.parent.setfocus(this);
    raise();
    if (button == 1) {
      this.ui.grabmouse(this);
      this.doff = c;
      if (c.isect(this.sz.sub(gzsz), gzsz)) {
        this.rsm = true;
        return true;
      } 
    } 
    return super.mousedown(c, button);
  }
  
  public boolean mouseup(Coord c, int button) {
    if (button == 1 && this.rsm) {
      this.ui.grabmouse(null);
      this.rsm = false;
      storeOpt("_sz", this.sz);
    } 
    return super.mouseup(c, button);
  }
  
  public void mousemove(Coord c) {
    if (this.rsm) {
      Coord d = c.sub(this.doff);
      Coord newsz = this.sz.add(d);
      newsz.x = Math.max(minsz.x, newsz.x);
      newsz.y = Math.max(minsz.y, newsz.y);
      this.doff = c;
      drawn.resize(newsz.sub(25, 150));
      sresize(newsz);
    } else {
      super.mousemove(c);
    } 
  }
  
  private void sresize(Coord sz) {
    IBox box;
    int th;
    if (this.cap == null) {
      box = Window.wbox;
      th = 0;
    } else {
      box = Window.topless;
      th = Window.th;
    } 
    this.sz = sz;
    this.ctl = box.btloff().add(0, th);
    this.csz = sz.sub(box.bisz()).sub(0, th);
    this.atl = this.ctl.add(this.mrgn);
    this.asz = this.csz.sub(this.mrgn.mul(2));
    this.ac = new Coord();
    placetwdgs();
    for (Widget ch = this.child; ch != null; ch = ch.next)
      ch.presize(); 
    this.gridlines.c = new Coord(15, sz.y - 145);
    this.recenter.c = new Coord(15, sz.y - 120);
    this.save.c = new Coord(15, sz.y - 80);
    this.marker_info.c = new Coord(130, sz.y - 145);
  }
}
