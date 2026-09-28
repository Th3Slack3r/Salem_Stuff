package haven;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.imageio.ImageIO;

public class MiniMap extends Widget {
  static Map<String, Tex> grids = new WeakHashMap<>();
  
  static Set<String> loading = new HashSet<>();
  
  static Loader loader = new Loader();
  
  public static final Tex bg = Resource.loadtex("gfx/hud/mmap/ptex");
  
  public static final Tex nomap = Resource.loadtex("gfx/hud/mmap/nomap");
  
  public static final Resource plx = Resource.load("gfx/hud/mmap/x");
  
  MapView mv;
  
  static class Loader implements Runnable {
    Thread me = null;
    
    private InputStream getreal(String nm) throws IOException {
      URL url = new URL(Config.mapurl, nm + ".png");
      URLConnection c = url.openConnection();
      c.addRequestProperty("User-Agent", "Haven/1.0");
      InputStream s = c.getInputStream();
      return s;
    }
    
    private InputStream getcached(String nm) throws IOException {
      if (ResCache.global == null)
        throw new FileNotFoundException("No resource cache installed"); 
      return ResCache.global.fetch("mm/" + nm);
    }
    
    public void run() {
      try {
        while (true) {
          String grid;
          synchronized (MiniMap.grids) {
            grid = null;
            Iterator<String> iterator = MiniMap.loading.iterator();
            if (iterator.hasNext()) {
              String cg = iterator.next();
              grid = cg;
            } 
          } 
          if (grid == null)
            break; 
          try {
            InputStream in;
            BufferedImage img;
            try {
              in = getcached(grid);
            } catch (FileNotFoundException e) {
              in = getreal(grid);
            } 
            try {
              img = ImageIO.read(in);
            } finally {
              Utils.readtileof(in);
              in.close();
            } 
            Tex tex = new TexI(img);
            synchronized (MiniMap.grids) {
              MiniMap.grids.put(grid, tex);
              MiniMap.loading.remove(grid);
            } 
          } catch (IOException e) {
            synchronized (MiniMap.grids) {
              MiniMap.grids.put(grid, null);
              MiniMap.loading.remove(grid);
            } 
          } 
        } 
      } finally {
        synchronized (this) {
          this.me = null;
        } 
      } 
    }
    
    void start() {
      synchronized (this) {
        if (this.me == null) {
          this.me = new HackThread(this, "Minimap loader");
          this.me.setDaemon(true);
          this.me.start();
        } 
      } 
    }
    
    void req(String nm) {
      synchronized (MiniMap.grids) {
        if (MiniMap.loading.contains(nm))
          return; 
        MiniMap.loading.add(nm);
        start();
      } 
    }
  }
  
  public MiniMap(Coord c, Coord sz, Widget parent, MapView mv) {
    super(c, sz, parent);
    this.mv = mv;
  }
  
  public static Tex getgrid(final String nm) {
    return AccessController.<Tex>doPrivileged(new PrivilegedAction<Tex>() {
          public Tex run() {
            synchronized (MiniMap.grids) {
              if (MiniMap.grids.containsKey(nm))
                return MiniMap.grids.get(nm); 
              MiniMap.loader.req(nm);
              return null;
            } 
          }
        });
  }
  
  public void draw(GOut g) {
    Coord tc = this.mv.cc.div(MCache.tilesz);
    Coord ulg = tc.div(MCache.cmaps);
    while (ulg.x * MCache.cmaps.x - tc.x + this.sz.x / 2 > 0)
      ulg.x--; 
    while (ulg.y * MCache.cmaps.y - tc.y + this.sz.y / 2 > 0)
      ulg.y--; 
    boolean missing = false;
    g.image(bg, Coord.z);
    int y;
    label65: for (y = ulg.y; y * MCache.cmaps.y - tc.y + this.sz.y / 2 < this.sz.y; y++) {
      for (int x = ulg.x; x * MCache.cmaps.x - tc.x + this.sz.x / 2 < this.sz.x; x++) {
        MCache.Grid grid;
        Coord cg = new Coord(x, y);
        synchronized (this.ui.sess.glob.map.req) {
          synchronized (this.ui.sess.glob.map.grids) {
            grid = this.ui.sess.glob.map.grids.get(cg);
            if (grid == null)
              this.ui.sess.glob.map.request(cg); 
          } 
        } 
        if (grid != null) {
          if (grid.mnm == null) {
            missing = true;
            break label65;
          } 
          Tex tex = getgrid(grid.mnm);
          if (tex != null)
            g.image(tex, cg.mul(MCache.cmaps).add(tc.inv()).add(this.sz.div(2))); 
        } 
      } 
    } 
    if (missing) {
      g.image(nomap, Coord.z);
    } else if (!plx.loading) {
      synchronized (this.ui.sess.glob.party.memb) {
        for (Party.Member m : this.ui.sess.glob.party.memb.values()) {
          Coord ptc;
          try {
            ptc = m.getc();
          } catch (LoadingMap e) {
            ptc = null;
          } 
          if (ptc == null)
            continue; 
          Coord coord1 = ptc.div(MCache.tilesz).add(tc.inv()).add(this.sz.div(2));
          g.chcolor(m.col.getRed(), m.col.getGreen(), m.col.getBlue(), 128);
          g.image(((Resource.Image)plx.<Resource.Image>layer(Resource.imgc)).tex(), coord1.add(((Resource.Neg)plx.layer((Class)Resource.negc)).cc.inv()));
          g.chcolor();
        } 
      } 
    } 
    super.draw(g);
  }
}
