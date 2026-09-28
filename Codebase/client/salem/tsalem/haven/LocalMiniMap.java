package haven;

import haven.minimap.Marker;
import haven.minimap.Radar;
import haven.resutil.RidgeTile;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;

public class LocalMiniMap extends Window implements Console.Directory {
  private static final String OPT_SZ = "_sz";
  
  static Tex bg = Resource.loadtex("gfx/hud/bgtex");
  
  public static final Resource plx = Resource.load("gfx/hud/mmap/x");
  
  public final MapView mv;
  
  private Coord cc = null;
  
  public Coord cgrid = null;
  
  private Coord off = new Coord();
  
  boolean rsm = false;
  
  boolean dm = false;
  
  private static Coord gzsz = new Coord(15, 15);
  
  public int scale = 4;
  
  private static final Coord minsz = new Coord(125, 125);
  
  private static final double[] scales = new double[] { 0.5D, 0.66D, 0.8D, 0.9D, 1.0D, 1.25D, 1.5D, 1.75D, 2.0D };
  
  private Coord sp;
  
  private String session;
  
  private final Map<String, Console.Command> cmdmap = new TreeMap<>();
  
  private boolean radarenabled = true;
  
  private int height = 0;
  
  private Defer.Future<BufferedImage> heightmap;
  
  private Coord lastplg;
  
  private final Coord hmsz = MCache.cmaps.mul(3);
  
  private static final String OPT_LOCKED = "_locked";
  
  private static final BufferedImage ilockc = Resource.loadimg("gfx/hud/lockc");
  
  private static final BufferedImage ilockch = Resource.loadimg("gfx/hud/lockch");
  
  private static final BufferedImage ilocko = Resource.loadimg("gfx/hud/locko");
  
  private static final BufferedImage ilockoh = Resource.loadimg("gfx/hud/lockoh");
  
  private IButton lockbtn;
  
  boolean locked;
  
  private final Map<Coord, Defer.Future<MapTile>> cache = new LinkedHashMap<Coord, Defer.Future<MapTile>>(9, 0.75F, true) {
      private static final long serialVersionUID = 1L;
      
      protected boolean removeEldestEntry(Map.Entry<Coord, Defer.Future<LocalMiniMap.MapTile>> eldest) {
        if (size() > 75) {
          try {
            LocalMiniMap.MapTile t = ((Defer.Future<LocalMiniMap.MapTile>)eldest.getValue()).get();
            t.img.dispose();
          } catch (RuntimeException runtimeException) {}
          return true;
        } 
        return false;
      }
    };
  
  public static class MapTile {
    public final Tex img;
    
    public final Coord ul;
    
    public final Coord c;
    
    public MapTile(Tex img, Coord ul, Coord c) {
      this.img = img;
      this.ul = ul;
      this.c = c;
    }
  }
  
  private BufferedImage tileimg(int t, BufferedImage[] texes) throws Loading {
    BufferedImage img = texes[t];
    if (img == null) {
      Resource r = this.ui.sess.glob.map.tilesetr(t);
      if (r == null)
        return null; 
      Resource.Image ir = r.<Resource.Image>layer(Resource.imgc);
      if (ir == null)
        return null; 
      img = ir.img;
      texes[t] = img;
    } 
    return img;
  }
  
  public BufferedImage drawmap(Coord ul, Coord sz, boolean pretty) {
    BufferedImage[] texes = new BufferedImage[256];
    MCache m = UI.instance.sess.glob.map;
    BufferedImage buf = TexI.mkbuf(sz);
    Coord c = new Coord();
    for (c.y = 0; c.y < sz.y; c.y++) {
      for (c.x = 0; c.x < sz.x; c.x++) {
        int t;
        Coord c2 = ul.add(c);
        try {
          t = m.gettile(c2);
        } catch (LoadingMap e) {
          return null;
        } 
        try {
          BufferedImage tex = tileimg(t, texes);
          int rgb = 255;
          if (tex != null) {
            Coord tc = pretty ? c2 : c;
            rgb = tex.getRGB(Utils.floormod(tc.x, tex.getWidth()), Utils.floormod(tc.y, tex.getHeight()));
          } 
          buf.setRGB(c.x, c.y, rgb);
        } catch (Loading e) {
          return null;
        } 
        try {
          if (m.gettile(c2.add(-1, 0)) > t || m.gettile(c2.add(1, 0)) > t || m.gettile(c2.add(0, -1)) > t || m.gettile(c2.add(0, 1)) > t)
            buf.setRGB(c.x, c.y, Color.BLACK.getRGB()); 
        } catch (LoadingMap e) {}
      } 
    } 
    if (Config.localmm_ridges)
      drawRidges(ul, sz, m, buf, c); 
    return buf;
  }
  
  private static void drawRidges(Coord ul, Coord sz, MCache m, BufferedImage buf, Coord c) {
    for (c.y = 1; c.y < sz.y - 1; c.y++) {
      for (c.x = 1; c.x < sz.x - 1; c.x++) {
        int t = m.gettile(ul.add(c));
        Tiler tl = m.tiler(t);
        if (tl instanceof RidgeTile && (
          (RidgeTile)tl).ridgep(m, ul.add(c)))
          for (int y = c.y; y <= c.y + 1; y++) {
            for (int x = c.x; x <= c.x + 1; x++) {
              int rgb = buf.getRGB(x, y);
              rgb = rgb & 0xFF000000 | (rgb & 0xFF0000) >> 17 << 16 | (rgb & 0xFF00) >> 9 << 8 | (rgb & 0xFF) >> 1 << 0;
              buf.setRGB(x, y, rgb);
            } 
          }  
      } 
    } 
  }
  
  private Defer.Future<BufferedImage> getheightmap(final Coord plg) {
    Defer.Future<BufferedImage> f = Defer.later(new Defer.Callable<BufferedImage>() {
          public BufferedImage call() {
            return LocalMiniMap.this.drawheightmap(plg);
          }
        });
    return f;
  }
  
  public BufferedImage drawheightmap(Coord plg) {
    MCache m = this.ui.sess.glob.map;
    Coord ul = plg.sub(1, 1).mul(MCache.cmaps);
    BufferedImage buf = TexI.mkbuf(this.hmsz);
    Coord c = new Coord();
    int MAX = Integer.MIN_VALUE;
    int MIN = Integer.MAX_VALUE;
    try {
      for (c.y = 0; c.y < this.hmsz.y; c.y++) {
        for (c.x = 0; c.x < this.hmsz.x; c.x++) {
          Coord c2 = ul.add(c);
          int t = m.getz(c2);
          if (t > MAX)
            MAX = t; 
          if (t < MIN)
            MIN = t; 
        } 
      } 
    } catch (LoadingMap e) {
      return null;
    } 
    int SIZE = MAX - MIN;
    for (c.y = 0; c.y < this.hmsz.y; c.y++) {
      for (c.x = 0; c.x < this.hmsz.x; c.x++) {
        Coord c2 = ul.add(c);
        int t2 = m.getz(c2);
        int t = Math.max(t2, MIN);
        t = Math.min(t, MAX);
        t -= MIN;
        if (SIZE > 0) {
          t = 255 * t / SIZE;
          t = t | t << 8 | t << 16 | this.height;
        } else {
          t = 0xFFFFFF | this.height;
        } 
        buf.setRGB(c.x, c.y, t);
        try {
          if (m.getz(c2.add(-1, 0)) > t2 + 11 || m.getz(c2.add(1, 0)) > t2 + 11 || m.getz(c2.add(0, -1)) > t2 + 11 || m.getz(c2.add(0, 1)) > t2 + 11)
            buf.setRGB(c.x, c.y, Color.RED.getRGB()); 
        } catch (LoadingMap e) {}
      } 
    } 
    return buf;
  }
  
  public LocalMiniMap(Coord c, Coord sz, Widget parent, MapView mv) {
    super(c, sz, parent, "mmap");
    this.cap = null;
    this.mv = mv;
    this.cmdmap.put("radar", new Console.Command() {
          public void run(Console console, String[] args) throws Exception {
            if (args.length == 2) {
              String arg = args[1];
              if (arg.equals("on")) {
                LocalMiniMap.this.radarenabled = true;
                return;
              } 
              if (arg.equals("off")) {
                LocalMiniMap.this.radarenabled = false;
                return;
              } 
              if (arg.equals("reload")) {
                LocalMiniMap.this.ui.sess.glob.oc.radar.reload();
                return;
              } 
            } 
            throw new Exception("No such setting");
          }
        });
    this.locked = getOptBool("_locked", false);
    this.lockbtn = new IButton(new Coord(-10, -43), this, this.locked ? ilockc : ilocko, this.locked ? ilocko : ilockc, this.locked ? ilockch : ilockoh) {
        public void click() {
          LocalMiniMap.this.locked = !LocalMiniMap.this.locked;
          if (LocalMiniMap.this.locked) {
            this.up = LocalMiniMap.ilockc;
            this.down = LocalMiniMap.ilocko;
            this.hover = LocalMiniMap.ilockch;
          } else {
            this.up = LocalMiniMap.ilocko;
            this.down = LocalMiniMap.ilockc;
            this.hover = LocalMiniMap.ilockoh;
          } 
          LocalMiniMap.this.storeOpt("_locked", LocalMiniMap.this.locked);
        }
      };
    this.lockbtn.recthit = true;
  }
  
  public Coord p2c(Coord pc) {
    return pc.div(MCache.tilesz).sub(this.cc).add(this.sz.div(2));
  }
  
  public Coord c2p(Coord c) {
    return c.sub(this.sz.div(2)).add(this.cc).mul(MCache.tilesz).add(MCache.tilesz.div(2));
  }
  
  public void drawicons(GOut g) {
    OCache oc = this.ui.sess.glob.oc;
    synchronized (oc) {
      for (Gob gob : oc) {
        try {
          GobIcon icon = gob.<GobIcon>getattr(GobIcon.class);
          if (icon != null) {
            Coord gc = p2c(gob.rc);
            Tex tex = icon.tex();
            tex.dim = tex.dim.mul(2);
            g.image(tex, gc.sub(tex.sz().div(2)));
          } 
        } catch (Loading loading) {}
      } 
    } 
  }
  
  public Gob findicongob(Coord c) {
    OCache oc = this.ui.sess.glob.oc;
    synchronized (oc) {
      for (Gob gob : oc) {
        try {
          GobIcon icon = gob.<GobIcon>getattr(GobIcon.class);
          if (icon != null) {
            Coord gc = p2c(gob.rc);
            Coord sz = icon.tex().sz();
            if (c.isect(gc.sub(sz.div(2)), sz))
              return gob; 
          } 
        } catch (Loading loading) {}
      } 
    } 
    return null;
  }
  
  protected void loadOpts() {
    super.loadOpts();
    this.sz = getOptCoord("_sz", this.sz);
  }
  
  public void toggleHeight() {
    if (this.height == 0) {
      this.height = 16777216;
    } else if (this.height == 16777216) {
      this.height = -16777216;
    } else if (this.height == -16777216) {
      this.height = -1258291200;
    } else {
      this.height = 0;
    } 
    clearheightmap();
  }
  
  private void clearheightmap() {
    if (this.heightmap != null && this.heightmap.done() && this.heightmap.get() != null)
      ((BufferedImage)this.heightmap.get()).flush(); 
    this.heightmap = null;
  }
  
  public void tick(double dt) {
    Gob pl = this.ui.sess.glob.oc.getgob(this.mv.plgob);
    if (pl == null) {
      this.cc = null;
      return;
    } 
    this.cc = pl.rc.div(MCache.tilesz);
  }
  
  public void draw(GOut og) {
    if (this.cc == null)
      return; 
    Coord plg = this.cc.div(MCache.cmaps);
    checkSession(plg);
    if (!plg.equals(this.lastplg)) {
      this.lastplg = plg;
      clearheightmap();
    } 
    if (this.height != 0 && this.heightmap == null)
      this.heightmap = getheightmap(plg); 
    double scale = getScale();
    Coord hsz = this.sz.div(scale);
    Coord tc = this.cc.add(this.off.div(scale));
    Coord ulg = tc.div(MCache.cmaps);
    int dy = -tc.y + hsz.y / 2;
    int dx = -tc.x + hsz.x / 2;
    while (ulg.x * MCache.cmaps.x + dx > 0)
      ulg.x--; 
    while (ulg.y * MCache.cmaps.y + dy > 0)
      ulg.y--; 
    Coord s = bg.sz();
    for (int y = 0; y * s.y < this.sz.y; y++) {
      for (int x = 0; x * s.x < this.sz.x; x++)
        og.image(bg, new Coord(x * s.x, y * s.y)); 
    } 
    GOut g = og.reclipl(og.ul.mul((1.0D - scale) / scale), hsz);
    g.gl.glPushMatrix();
    g.gl.glScaled(scale, scale, scale);
    Coord cg = new Coord();
    synchronized (this.cache) {
      for (cg.y = ulg.y; cg.y * MCache.cmaps.y + dy < hsz.y; cg.y++) {
        for (cg.x = ulg.x; cg.x * MCache.cmaps.x + dx < hsz.x; cg.x++) {
          Defer.Future<MapTile> f = this.cache.get(cg);
          final Coord tcg = new Coord(cg);
          final Coord ul = cg.mul(MCache.cmaps);
          if (f == null && cg.manhattan2(plg) <= 1) {
            f = Defer.later(new Defer.Callable<MapTile>() {
                  public LocalMiniMap.MapTile call() {
                    BufferedImage img = LocalMiniMap.this.drawmap(ul, MCache.cmaps, true);
                    if (img == null)
                      return null; 
                    LocalMiniMap.MapTile mapTile = new LocalMiniMap.MapTile(new TexI(img), ul, tcg);
                    if (Config.store_map) {
                      img = LocalMiniMap.this.drawmap(ul, MCache.cmaps, false);
                      LocalMiniMap.this.store(img, tcg);
                    } 
                    return mapTile;
                  }
                });
            this.cache.put(tcg, f);
          } 
          if (f != null && f.done()) {
            MapTile mt = f.get();
            if (mt == null) {
              this.cache.put(cg, null);
            } else {
              Tex img = mt.img;
              g.image(img, ul.add(tc.inv()).add(hsz.div(2)));
            } 
          } 
        } 
      } 
    } 
    Coord c0 = hsz.div(2).sub(tc);
    if (this.height != 0 && this.heightmap != null && this.heightmap.done()) {
      BufferedImage img = this.heightmap.get();
      if (img != null) {
        g.image(img, c0.add(plg.sub(1, 1).mul(MCache.cmaps)));
      } else {
        clearheightmap();
      } 
    } 
    drawmarkers(g, c0);
    synchronized (this.ui.sess.glob.party.memb) {
      try {
        Tex tx = ((Resource.Image)plx.<Resource.Image>layer(Resource.imgc)).tex();
        Coord negc = ((Resource.Neg)plx.layer((Class)Resource.negc)).cc;
        for (Party.Member memb : this.ui.sess.glob.party.memb.values()) {
          Coord ptc = memb.getc();
          if (ptc == null)
            continue; 
          ptc = c0.add(ptc.div(MCache.tilesz));
          g.chcolor(memb.col);
          g.image(tx, ptc.sub(negc));
          g.chcolor();
        } 
      } catch (Loading loading) {}
    } 
    g.gl.glPopMatrix();
    Window.swbox.draw(og, Coord.z, this.sz);
    this.lockbtn.draw(og.reclipl(xlate(this.lockbtn.c, true), this.lockbtn.sz));
  }
  
  private String mapfolder() {
    return String.format("%s/map/%s/", new Object[] { Config.userhome, Config.server });
  }
  
  private String mapfile(String file) {
    return String.format("%s%s", new Object[] { mapfolder(), file });
  }
  
  private String mapsessfile(String file) {
    return String.format("%s%s/%s", new Object[] { mapfolder(), this.session, file });
  }
  
  private String mapsessfolder() {
    return mapsessfile("");
  }
  
  private void store(BufferedImage img, Coord cg) {
    if (!Config.store_map || img == null)
      return; 
    Coord c = cg.sub(this.sp);
    String fileName = mapsessfile(String.format("tile_%d_%d.png", new Object[] { Integer.valueOf(c.x), Integer.valueOf(c.y) }));
    File outputfile = new File(fileName);
    try {
      ImageIO.write(img, "png", outputfile);
    } catch (IOException iOException) {}
  }
  
  private void checkSession(Coord plg) {
    if (this.cgrid == null || plg.manhattan(this.cgrid) > 5) {
      this.sp = plg;
      synchronized (this.cache) {
        for (Defer.Future<MapTile> v : this.cache.values()) {
          if (v != null && v.done()) {
            MapTile tile = v.get();
            if (tile != null && tile.img != null)
              tile.img.dispose(); 
          } 
        } 
        this.cache.clear();
      } 
      this.session = Utils.current_date();
      if (Config.store_map) {
        (new File(mapsessfolder())).mkdirs();
        try {
          Writer currentSessionFile = new FileWriter(mapfile("currentsession.js"));
          currentSessionFile.write("var currentSession = '" + this.session + "';\n");
          currentSessionFile.close();
        } catch (IOException iOException) {}
      } 
    } 
    this.cgrid = plg;
  }
  
  public double getScale() {
    return scales[this.scale];
  }
  
  public void setScale(int scale) {
    this.scale = Math.max(0, Math.min(scale, scales.length - 1));
  }
  
  public boolean mousedown(Coord c, int button) {
    this.parent.setfocus(this);
    raise();
    Marker m = getmarkerat(c);
    Coord mc = uitomap(c);
    if (button == 3) {
      if (m != null) {
        this.mv.wdgmsg("click", new Object[] { this.c.add(c), mc, Integer.valueOf(button), Integer.valueOf(this.ui.modflags()), Integer.valueOf(0), Integer.valueOf((int)m.gob.id), m.gob.rc, Integer.valueOf(0), Integer.valueOf(-1) });
        return true;
      } 
      this.dm = true;
      this.ui.grabmouse(this);
      this.doff = c;
      return true;
    } 
    if (button == 1) {
      if (m != null || !this.ui.modctrl) {
        if (m != null && m.gob != null)
          m.gob.setattr(new GobHighlight(m.gob)); 
        if (!this.ui.modctrl)
          this.mv.wdgmsg("click", new Object[] { Coord.z, mc, Integer.valueOf(button), Integer.valueOf(0) }); 
        return true;
      } 
      this.ui.grabmouse(this);
      this.doff = c;
      if (c.isect(this.sz.sub(gzsz), gzsz)) {
        this.rsm = true;
        return true;
      } 
    } 
    return old_mousedown(c, button);
  }
  
  public boolean mouseup(Coord c, int button) {
    if (button == 2) {
      this.off.x = this.off.y = 0;
      return true;
    } 
    if (button == 3) {
      this.dm = false;
      this.ui.grabmouse(null);
      return true;
    } 
    if (this.rsm) {
      this.ui.grabmouse(null);
      this.rsm = false;
      storeOpt("_sz", this.sz);
    } else {
      super.mouseup(c, button);
    } 
    return true;
  }
  
  public void mousemove(Coord c) {
    if (this.dm) {
      Coord d = c.sub(this.doff);
      this.off = this.off.sub(d);
      this.doff = c;
      return;
    } 
    if (!this.locked && this.rsm) {
      Coord d = c.sub(this.doff);
      this.sz = this.sz.add(d);
      this.sz.x = Math.max(minsz.x, this.sz.x);
      this.sz.y = Math.max(minsz.y, this.sz.y);
      this.doff = c;
    } else if (!this.locked) {
      super.mousemove(c);
    } 
  }
  
  public boolean mousewheel(Coord c, int amount) {
    if (amount > 0) {
      setScale(this.scale - 1);
    } else {
      setScale(this.scale + 1);
    } 
    return true;
  }
  
  private void drawmarkers(GOut g, Coord tc) {
    if (!this.radarenabled)
      return; 
    double scale = MapView.scaleForMarkers;
    Radar radar = this.ui.sess.glob.oc.radar;
    try {
      for (Marker m : radar.getMarkers()) {
        if (m.template.visible)
          m.draw(g, tc, scale); 
      } 
    } catch (LoadingMap loadingMap) {}
  }
  
  private Coord uitomap(Coord c) {
    return c.sub(this.sz.div(2)).add(this.off).div(getScale()).mul(MCache.tilesz).add(this.mv.cc);
  }
  
  private Marker getmarkerat(Coord c) {
    if (this.radarenabled) {
      Radar radar = this.ui.sess.glob.oc.radar;
      try {
        Coord mc = uitomap(c);
        for (Marker m : radar.getMarkers()) {
          if (m.template.visible && m.hit(mc))
            return m; 
        } 
      } catch (LoadingMap loadingMap) {}
    } 
    return null;
  }
  
  public Object tooltip(Coord c, boolean again) {
    Marker m = getmarkerat(c);
    if (m != null)
      return m.getTooltip(); 
    return null;
  }
  
  public Map<String, Console.Command> findcmds() {
    return this.cmdmap;
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (key == '\033')
      return false; 
    return super.type(key, ev);
  }
  
  public void wdgmsg(String msg, Object... args) {}
}
