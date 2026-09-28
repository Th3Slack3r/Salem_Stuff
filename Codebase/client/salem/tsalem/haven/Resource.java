package haven;

import dolda.jglob.Discoverable;
import dolda.xiphutil.VorbisStream;
import haven.plugins.Plugin;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.CertificateException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

public class Resource implements Comparable<Resource>, Prioritized, Serializable {
  private static final Map<String, Resource> cache;
  
  private static Loader loader;
  
  private static CacheSource prscache;
  
  public static ThreadGroup loadergroup = null;
  
  private static Map<String, LayerFactory<?>> ltypes = new TreeMap<>();
  
  static Set<Resource> loadwaited = new HashSet<>();
  
  public static Class<Image> imgc = Image.class;
  
  public static Class<Tile> tile = Tile.class;
  
  public static Class<Neg> negc = Neg.class;
  
  public static Class<Anim> animc = Anim.class;
  
  public static Class<Tileset> tileset = Tileset.class;
  
  public static Class<Pagina> pagina = Pagina.class;
  
  public static Class<AButton> action = AButton.class;
  
  public static Class<Audio> audio = Audio.class;
  
  public static Class<Tooltip> tooltip = Tooltip.class;
  
  private LoadException error;
  
  static {
    if (Config.softres) {
      cache = new CacheMap<>();
    } else {
      cache = new TreeMap<>();
    } 
    try {
      chainloader(new Loader(new FileSource(Utils.path(Config.userhome + "/custom_res"))));
      String dir = Config.resdir;
      if (dir == null)
        dir = System.getenv("SALEM_RESDIR"); 
      if (dir != null)
        chainloader(new Loader(new FileSource(Utils.path(dir)))); 
    } catch (Exception exception) {}
    if (!Config.nolocalres)
      chainloader(new Loader(new JarSource())); 
    for (Class<?> cl : (Iterable<Class<?>>)dolda.jglob.Loader.get(LayerName.class).classes()) {
      String nm = ((LayerName)cl.<LayerName>getAnnotation(LayerName.class)).value();
      if (LayerFactory.class.isAssignableFrom(cl)) {
        try {
          addltype(nm, cl.<LayerFactory>asSubclass(LayerFactory.class).newInstance());
        } catch (InstantiationException e) {
          throw new Error(e);
        } catch (IllegalAccessException e) {
          throw new Error(e);
        } 
        continue;
      } 
      if (Layer.class.isAssignableFrom(cl)) {
        addltype(nm, (Class)cl.asSubclass(Layer.class));
        continue;
      } 
      throw new Error("Illegal resource layer class: " + cl);
    } 
  }
  
  private Collection<Layer> layers = new LinkedList<>();
  
  public final String name;
  
  public int ver;
  
  public boolean loading;
  
  public ResSource source;
  
  private transient Indir<Resource> indir = null;
  
  int prio = 0;
  
  public static class Spec implements Indir<Resource> {
    public final String name;
    
    public final int ver;
    
    public Spec(String name, int ver) {
      this.name = name;
      this.ver = ver;
    }
    
    public Resource get(int prio) {
      return Resource.load(this.name, this.ver);
    }
    
    public Resource get() {
      return get(0);
    }
  }
  
  private Resource(String name, int ver) {
    this.name = name;
    this.ver = ver;
    this.error = null;
    this.loading = true;
  }
  
  public static void addcache(ResCache cache) {
    CacheSource src = new CacheSource(cache);
    prscache = src;
    chainloader(new Loader(src));
  }
  
  public static void addurl(URL url) {
    ResSource src = new HttpSource(url);
    final CacheSource mc = prscache;
    if (mc != null)
      src = new TeeSource(src) {
          public OutputStream fork(String name) throws IOException {
            return mc.cache.store("res/" + name);
          }
        }; 
    chainloader(new Loader(src));
  }
  
  public static void addplugin(Plugin plugin) {
    final Class<? extends Plugin> cl = (Class)plugin.getClass();
    chainloader(new Loader(new ResSource() {
            public InputStream get(String name) throws FileNotFoundException {
              InputStream s = cl.getResourceAsStream("/res/" + name + ".res");
              if (s == null)
                throw new FileNotFoundException("Could not find resource: " + name); 
              return s;
            }
            
            public String toString() {
              return "plugin resource source";
            }
          }));
  }
  
  private static void chainloader(Loader nl) {
    synchronized (Resource.class) {
      if (loader == null) {
        loader = nl;
      } else {
        Loader l;
        for (l = loader; l.next != null; l = l.next);
        l.chain(nl);
      } 
    } 
  }
  
  public static Resource load(String name, int ver, int prio) {
    Resource res;
    synchronized (cache) {
      res = cache.get(name);
      if (res != null)
        if (res.ver != -1 && ver != -1) {
          if (res.ver < ver) {
            res = null;
            cache.remove(name);
          } else if (res.ver > ver) {
            if (res == null || res.name == null || res.name.contains("gfx/terobjs/pinn"));
          } 
        } else if (ver == -1 && res.error != null) {
          res = null;
          cache.remove(name);
        }  
      if (res != null) {
        res.boostprio(prio);
        return res;
      } 
      res = new Resource(name, ver);
      res.prio = prio;
      cache.put(name, res);
    } 
    loader.load(res);
    return res;
  }
  
  public static int numloaded() {
    synchronized (cache) {
      return cache.size();
    } 
  }
  
  public static Collection<Resource> cached() {
    synchronized (cache) {
      return cache.values();
    } 
  }
  
  public static Resource load(String name, int ver) {
    return load(name, ver, 0);
  }
  
  public static int qdepth() {
    int ret = 0;
    for (Loader l = loader; l != null; l = l.next)
      ret += l.queue.size(); 
    return ret;
  }
  
  public static Resource load(String name) {
    return load(name, -1);
  }
  
  public void boostprio(int newprio) {
    if (this.prio < newprio)
      this.prio = newprio; 
  }
  
  public Resource loadwaitint() throws InterruptedException {
    synchronized (this) {
      boostprio(10);
      while (this.loading)
        wait(); 
    } 
    return this;
  }
  
  public String basename() {
    int p = this.name.lastIndexOf('/');
    if (p < 0)
      return this.name; 
    return this.name.substring(p + 1);
  }
  
  public Resource loadwait() {
    boolean i = false;
    synchronized (loadwaited) {
      loadwaited.add(this);
    } 
    synchronized (this) {
      boostprio(10);
      while (this.loading) {
        try {
          wait();
        } catch (InterruptedException e) {
          i = true;
        } 
      } 
    } 
    if (i)
      Thread.currentThread().interrupt(); 
    return this;
  }
  
  public static abstract class TeeSource implements ResSource, Serializable {
    public Resource.ResSource back;
    
    public TeeSource(Resource.ResSource back) {
      this.back = back;
    }
    
    public InputStream get(String name) throws IOException {
      StreamTee tee = new StreamTee(this.back.get(name));
      tee.setncwe();
      tee.attach(fork(name));
      return tee;
    }
    
    public abstract OutputStream fork(String param1String) throws IOException;
    
    public String toString() {
      return "forking source backed by " + this.back;
    }
  }
  
  public static class CacheSource implements ResSource, Serializable {
    public transient ResCache cache;
    
    public CacheSource(ResCache cache) {
      this.cache = cache;
    }
    
    public InputStream get(String name) throws IOException {
      return this.cache.fetch("res/" + name);
    }
    
    public String toString() {
      return "cache source backed by " + this.cache;
    }
  }
  
  public static class FileSource implements ResSource, Serializable {
    public static final Collection<String> wintraps = new HashSet<>(Arrays.asList(new String[] { 
            "con", "prn", "aux", "nul", "com0", "com1", "com2", "com3", "com4", "com5", 
            "com6", "com7", "com8", "com9", "lpt0", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", 
            "lpt6", "lpt7", "lpt8", "lpt9" }));
    
    public static final boolean windows = System.getProperty("os.name", "").startsWith("Windows");
    
    private static final boolean[] winsafe;
    
    public final Path base;
    
    static {
      boolean[] buf = new boolean[128];
      String safe = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_@";
      for (int i = 0; i < "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_@".length(); i++)
        buf["0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_@".charAt(i)] = true; 
      winsafe = buf;
    }
    
    public static boolean winsafechar(char c) {
      return (c >= winsafe.length || winsafe[c]);
    }
    
    public FileSource(Path base) {
      this.base = base;
    }
    
    private static String checkpart(String part, String whole) throws FileNotFoundException {
      if (windows && wintraps.contains(part))
        throw new FileNotFoundException(whole); 
      return part;
    }
    
    public InputStream get(String name) throws IOException {
      Path cur = this.base;
      String[] parts = name.split("/");
      for (int i = 0; i < parts.length - 1; i++)
        cur = cur.resolve(checkpart(parts[i], name)); 
      cur = cur.resolve(checkpart(parts[parts.length - 1], name) + ".res");
      try {
        return Files.newInputStream(cur, new java.nio.file.OpenOption[0]);
      } catch (NoSuchFileException e) {
        throw (FileNotFoundException)(new FileNotFoundException(name)).initCause(e);
      } 
    }
    
    public String toString() {
      return "filesystem res source (" + this.base + ")";
    }
  }
  
  public static class JarSource implements ResSource, Serializable {
    public InputStream get(String name) throws FileNotFoundException {
      InputStream s = Resource.class.getResourceAsStream("/res/" + name + ".res");
      if (s == null)
        throw new FileNotFoundException("Could not find resource locally: " + name); 
      return s;
    }
    
    public String toString() {
      return "local res source";
    }
  }
  
  public static class HttpSource implements ResSource, Serializable {
    private final transient SslHelper ssl = new SslHelper();
    
    public URL baseurl;
    
    public HttpSource(URL baseurl) {
      try {
        this.ssl.trust(Resource.class.getResourceAsStream("ressrv.crt"));
      } catch (CertificateException e) {
        throw new Error("Invalid built-in certificate", e);
      } catch (IOException e) {
        throw new Error(e);
      } 
      this.ssl.ignoreName();
      this.baseurl = baseurl;
    }
    
    private URL encodeurl(URL raw) throws IOException {
      try {
        return new URL((new URI(raw.getProtocol(), raw.getHost(), raw.getPath(), raw.getRef())).toASCIIString());
      } catch (URISyntaxException e) {
        throw new IOException(e);
      } 
    }
    
    public InputStream get(String name) throws IOException {
      URL resurl = encodeurl(new URL(this.baseurl, name + ".res"));
      int tries = 0;
      while (true) {
        try {
          URLConnection c;
          if (resurl.getProtocol().equals("https")) {
            c = this.ssl.connect(resurl);
          } else {
            c = resurl.openConnection();
          } 
          c.setUseCaches(false);
          c.addRequestProperty("User-Agent", "Haven/1.0");
          return c.getInputStream();
        } catch (ConnectException e) {
          if (++tries >= 5)
            throw new IOException("Connection failed five times", e); 
        } 
      } 
    }
    
    public String toString() {
      return "HTTP res source (" + this.baseurl + ")";
    }
  }
  
  static class Loader implements Runnable {
    private final Resource.ResSource src;
    
    private Loader next = null;
    
    private final Queue<Resource> queue = new PrioQueue<>();
    
    private transient Thread th = null;
    
    public Loader(Resource.ResSource src) {
      this.src = src;
    }
    
    public void chain(Loader next) {
      this.next = next;
    }
    
    public void load(Resource res) {
      synchronized (this.queue) {
        this.queue.add(res);
        this.queue.notifyAll();
      } 
      synchronized (this) {
        if (this.th == null) {
          this.th = new HackThread(Resource.loadergroup, this, "Haven resource loader");
          this.th.setDaemon(true);
          this.th.start();
        } 
      } 
    }
    
    public void run() {
      try {
        while (true) {
          try {
            synchronized (this.queue) {
              while ((cur = this.queue.poll()) == null)
                this.queue.wait(); 
            } 
            synchronized (cur) {
              handle(cur);
            } 
            Resource cur = null;
          } catch (InterruptedException interruptedException) {
            synchronized (this) {
              this.th = null;
            } 
            break;
          } 
        } 
      } finally {
        synchronized (this) {
          this.th = null;
        } 
      } 
    }
    
    private void handle(Resource res) {
      InputStream in = null;
      try {
        res.source = this.src;
      } finally {
        try {
          if (in != null)
            in.close(); 
        } catch (IOException iOException) {}
      } 
    }
  }
  
  public static class LoadException extends RuntimeException {
    public Resource res;
    
    public Resource.ResSource src;
    
    public LoadException prev;
    
    public LoadException(String msg, Resource res) {
      super(msg);
      this.res = res;
    }
    
    public LoadException(String msg, Throwable cause, Resource res) {
      super(msg, cause);
      this.res = res;
    }
    
    public LoadException(Throwable cause, Resource res) {
      super("Load error in resource " + res.toString() + ", from " + res.source, cause);
      this.res = res;
    }
  }
  
  public static class Loading extends Loading {
    public final Resource res;
    
    public Loading(Resource res) {
      this.res = res;
    }
    
    public String toString() {
      return "#<Resource " + this.res.name + ">";
    }
    
    public boolean canwait() {
      return true;
    }
    
    public void waitfor() throws InterruptedException {
      this.res.loadwaitint();
    }
  }
  
  public static Coord cdec(byte[] buf, int off) {
    return new Coord(Utils.int16d(buf, off), Utils.int16d(buf, off + 2));
  }
  
  public abstract class Layer implements Serializable {
    public abstract void init();
    
    public Resource getres() {
      return Resource.this;
    }
  }
  
  public static class LayerConstructor<T extends Layer> implements LayerFactory<T> {
    public final Class<T> cl;
    
    private final Constructor<T> cons;
    
    public LayerConstructor(Class<T> cl) {
      this.cl = cl;
      try {
        this.cons = cl.getConstructor(new Class[] { Resource.class, byte[].class });
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("No proper constructor found for layer type " + cl.getName(), e);
      } 
    }
    
    public T cons(Resource res, byte[] buf) {
      try {
        return this.cons.newInstance(new Object[] { res, buf });
      } catch (InstantiationException e) {
        throw new Resource.LoadException(e, res);
      } catch (IllegalAccessException e) {
        throw new Resource.LoadException(e, res);
      } catch (InvocationTargetException e) {
        Throwable c = e.getCause();
        if (c instanceof RuntimeException)
          throw (RuntimeException)c; 
        throw new Resource.LoadException(e, res);
      } 
    }
  }
  
  public static void addltype(String name, LayerFactory<?> cons) {
    ltypes.put(name, cons);
  }
  
  public static <T extends Layer> void addltype(String name, Class<T> cl) {
    addltype(name, new LayerConstructor(cl));
  }
  
  @LayerName("image")
  public class Image extends Layer implements Comparable<Image>, IDLayer<Integer> {
    public transient BufferedImage img;
    
    private transient Tex tex;
    
    public final int z;
    
    public final int subz;
    
    public final boolean nooff;
    
    public final int id;
    
    private int gay = -1;
    
    public Coord sz;
    
    public Coord o;
    
    public Image(byte[] buf) {
      this.z = Utils.int16d(buf, 0);
      this.subz = Utils.int16d(buf, 2);
      this.nooff = ((buf[4] & 0x2) != 0);
      this.id = Utils.int16d(buf, 5);
      this.o = Resource.cdec(buf, 7);
      try {
        this.img = ImageIO.read(new ByteArrayInputStream(buf, 11, buf.length - 11));
      } catch (IOException e) {
        throw new Resource.LoadException(e, Resource.this);
      } 
      if (this.img == null)
        throw new Resource.LoadException("Invalid image data in " + Resource.this.name, Resource.this); 
      this.sz = Utils.imgsz(this.img);
    }
    
    public synchronized Tex tex() {
      if (this.tex != null)
        return this.tex; 
      this.tex = new TexI(this.img) {
          public String toString() {
            return "TexI(" + Resource.this.name + ", " + Resource.Image.this.id + ")";
          }
        };
      return this.tex;
    }
    
    private boolean detectgay() {
      for (int y = 0; y < this.sz.y; y++) {
        for (int x = 0; x < this.sz.x; x++) {
          if ((this.img.getRGB(x, y) & 0xFFFFFF) == 16711808)
            return true; 
        } 
      } 
      return false;
    }
    
    public boolean gayp() {
      if (this.gay == -1)
        this.gay = detectgay() ? 1 : 0; 
      return (this.gay == 1);
    }
    
    public int compareTo(Image other) {
      return this.z - other.z;
    }
    
    public Integer layerid() {
      return Integer.valueOf(this.id);
    }
    
    public void init() {}
  }
  
  @LayerName("tooltip")
  public class Tooltip extends Layer {
    public final String t;
    
    public Tooltip(byte[] buf) {
      try {
        this.t = new String(buf, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new Resource.LoadException(e, Resource.this);
      } 
    }
    
    public void init() {}
  }
  
  @LayerName("tile")
  public class Tile extends Layer {
    transient BufferedImage img;
    
    private transient Tex tex;
    
    public final int id;
    
    public final int w;
    
    public final char t;
    
    public Tile(byte[] buf) {
      this.t = (char)Utils.ub(buf[0]);
      this.id = Utils.ub(buf[1]);
      this.w = Utils.uint16d(buf, 2);
      try {
        this.img = ImageIO.read(new ByteArrayInputStream(buf, 4, buf.length - 4));
      } catch (IOException e) {
        throw new Resource.LoadException(e, Resource.this);
      } 
      if (this.img == null)
        throw new Resource.LoadException("Invalid image data in " + Resource.this.name, Resource.this); 
    }
    
    public synchronized Tex tex() {
      if (this.tex == null)
        this.tex = new TexI(this.img); 
      return this.tex;
    }
    
    public void init() {}
  }
  
  @LayerName("neg")
  public class Neg extends Layer {
    public Coord cc;
    
    public Coord ac;
    
    public Coord bc;
    
    public Coord[][] ep;
    
    public Neg(byte[] buf) {
      this.cc = Resource.cdec(buf, 0);
      this.ac = Resource.cdec(buf, 4);
      this.bc = Resource.cdec(buf, 8);
      this.ep = new Coord[8][0];
      int en = buf[16];
      int off = 17;
      for (int i = 0; i < en; i++) {
        int epid = buf[off];
        int cn = Utils.uint16d(buf, off + 1);
        off += 3;
        this.ep[epid] = new Coord[cn];
        for (int o = 0; o < cn; o++) {
          this.ep[epid][o] = Resource.cdec(buf, off);
          off += 4;
        } 
      } 
    }
    
    public void init() {}
  }
  
  @LayerName("anim")
  public class Anim extends Layer {
    private final int[] ids;
    
    public int id;
    
    public int d;
    
    public Resource.Image[][] f;
    
    public Anim(byte[] buf) {
      this.id = Utils.int16d(buf, 0);
      this.d = Utils.uint16d(buf, 2);
      this.ids = new int[Utils.uint16d(buf, 4)];
      if (buf.length - 6 != this.ids.length * 2)
        throw new Resource.LoadException("Invalid anim descriptor in " + Resource.this.name, Resource.this); 
      for (int i = 0; i < this.ids.length; i++)
        this.ids[i] = Utils.int16d(buf, 6 + i * 2); 
    }
    
    public void init() {
      this.f = new Resource.Image[this.ids.length][];
      Resource.Image[] typeinfo = new Resource.Image[0];
      for (int i = 0; i < this.ids.length; i++) {
        LinkedList<Resource.Image> buf = new LinkedList<>();
        for (Resource.Image img : Resource.this.<Resource.Image>layers(Resource.Image.class, false)) {
          if (img.id == this.ids[i])
            buf.add(img); 
        } 
        this.f[i] = buf.<Resource.Image>toArray(typeinfo);
      } 
    }
  }
  
  @LayerName("tileset2")
  public class Tileset extends Layer {
    private String tn = "gnd";
    
    public Object[] ta = new Object[0];
    
    private transient Tiler.Factory tfac;
    
    public WeightList<Resource> flavobjs = new WeightList<>();
    
    public WeightList<Resource.Tile> ground;
    
    public WeightList<Resource.Tile>[] ctrans;
    
    public WeightList<Resource.Tile>[] btrans;
    
    public int flavprob;
    
    public Tileset(byte[] bbuf) {
      Message buf = new Message(0, bbuf);
      while (!buf.eom()) {
        int flnum, i, p = buf.uint8();
        switch (p) {
          case 0:
            this.tn = buf.string();
            this.ta = buf.list();
            continue;
          case 1:
            flnum = buf.uint16();
            this.flavprob = buf.uint16();
            for (i = 0; i < flnum; i++) {
              String fln = buf.string();
              int flv = buf.uint16();
              int flw = buf.uint8();
              try {
                this.flavobjs.add(Resource.load(fln, flv), flw);
              } catch (RuntimeException e) {
                throw new Resource.LoadException("Illegal resource dependency", e, Resource.this);
              } 
            } 
            continue;
        } 
        throw new Resource.LoadException("Invalid tileset part " + p + "  in " + Resource.this.name, Resource.this);
      } 
    }
    
    public Tiler.Factory tfac() {
      synchronized (this) {
        if (this.tfac == null) {
          Resource.CodeEntry ent = Resource.this.<Resource.CodeEntry>layer(Resource.CodeEntry.class);
          if (ent != null) {
            this.tfac = ent.<Tiler.Factory>get(Tiler.Factory.class);
          } else if ((this.tfac = Tiler.byname(this.tn)) == null) {
            throw new RuntimeException("Invalid tiler name in " + Resource.this.name + ": " + this.tn);
          } 
        } 
        return this.tfac;
      } 
    }
    
    private void packtiles(Collection<Resource.Tile> tiles, Coord tsz) {
      if (tiles.size() < 1)
        return; 
      int min = -1, minw = -1, minh = -1, mine = -1;
      final int nt = tiles.size();
      for (int i = 1; i <= nt; i++) {
        int w = Tex.nextp2(tsz.x * i);
        if (nt % i == 0) {
          h = nt / i;
        } else {
          h = nt / i + 1;
        } 
        int h = Tex.nextp2(tsz.y * h);
        int a = w * h;
        int e = (w < h) ? h : w;
        if (min == -1 || a < min || (a == min && e < mine)) {
          min = a;
          minw = w;
          minh = h;
          mine = e;
        } 
      } 
      final Resource.Tile[] order = new Resource.Tile[nt];
      final Coord[] place = new Coord[nt];
      Tex packbuf = new TexL(new Coord(minw, minh)) {
          protected BufferedImage fill() {
            BufferedImage buf = TexI.mkbuf(this.dim);
            Graphics g = buf.createGraphics();
            for (int i = 0; i < nt; i++)
              g.drawImage((order[i]).img, (place[i]).x, (place[i]).y, null); 
            g.dispose();
            return buf;
          }
          
          public String toString() {
            return "TileTex(" + Resource.this.name + ")";
          }
        };
      int x = 0, y = 0, n = 0;
      for (Resource.Tile t : tiles) {
        if (y >= minh)
          throw new Resource.LoadException("Could not pack tiles into calculated minimum texture", Resource.this); 
        order[n] = t;
        place[n] = new Coord(x, y);
        t.tex = new TexSI(packbuf, place[n], tsz);
        n++;
        if ((x += tsz.x) > minw - tsz.x) {
          x = 0;
          y += tsz.y;
        } 
      } 
    }
    
    public void init() {
      WeightList<Resource.Tile> ground = new WeightList<>();
      WeightList[] arrayOfWeightList1 = new WeightList[15];
      WeightList[] arrayOfWeightList2 = new WeightList[15];
      for (int i = 0; i < 15; i++) {
        arrayOfWeightList1[i] = new WeightList();
        arrayOfWeightList2[i] = new WeightList();
      } 
      int cn = 0, bn = 0;
      Collection<Resource.Tile> tiles = new LinkedList<>();
      Coord tsz = null;
      for (Resource.Tile t : Resource.this.<Resource.Tile>layers(Resource.Tile.class, false)) {
        if (t.t == 'g') {
          ground.add(t, t.w);
        } else if (t.t == 'b') {
          arrayOfWeightList2[t.id - 1].add(t, t.w);
          bn++;
        } else if (t.t == 'c') {
          arrayOfWeightList1[t.id - 1].add(t, t.w);
          cn++;
        } 
        tiles.add(t);
        if (tsz == null) {
          tsz = Utils.imgsz(t.img);
          continue;
        } 
        if (!Utils.imgsz(t.img).equals(tsz))
          throw new Resource.LoadException("Different tile sizes within set", Resource.this); 
      } 
      if (ground.size() > 0)
        this.ground = ground; 
      if (cn > 0)
        this.ctrans = (WeightList<Resource.Tile>[])arrayOfWeightList1; 
      if (bn > 0)
        this.btrans = (WeightList<Resource.Tile>[])arrayOfWeightList2; 
      packtiles(tiles, tsz);
    }
    
    private Tileset() {}
  }
  
  @LayerName("tileset")
  public static class OrigTileset implements LayerFactory<Tileset> {
    public Resource.Tileset cons(Resource res, byte[] buf) {
      res.getClass();
      Resource.Tileset ret = new Resource.Tileset();
      int[] off = new int[1];
      off[0] = 0;
      off[0] = off[0] + 1;
      int fl = Utils.ub(buf[off[0]]);
      int flnum = Utils.uint16d(buf, off[0]);
      off[0] = off[0] + 2;
      ret.flavprob = Utils.uint16d(buf, off[0]);
      off[0] = off[0] + 2;
      for (int i = 0; i < flnum; i++) {
        String fln = Utils.strd(buf, off);
        int flv = Utils.uint16d(buf, off[0]);
        off[0] = off[0] + 2;
        off[0] = off[0] + 1;
        int flw = Utils.ub(buf[off[0]]);
        try {
          ret.flavobjs.add(Resource.load(fln, flv), flw);
        } catch (RuntimeException e) {
          throw new Resource.LoadException("Illegal resource dependency", e, res);
        } 
      } 
      return ret;
    }
  }
  
  @LayerName("pagina")
  public class Pagina extends Layer {
    public final String text;
    
    public Pagina(byte[] buf) {
      try {
        this.text = new String(buf, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new Resource.LoadException(e, Resource.this);
      } 
    }
    
    public void init() {}
  }
  
  @LayerName("action")
  public class AButton extends Layer {
    public final String name;
    
    public final Resource parent;
    
    public final char hk;
    
    public final String[] ad;
    
    public AButton(byte[] buf) {
      int[] off = new int[1];
      off[0] = 0;
      String pr = Utils.strd(buf, off);
      int pver = Utils.uint16d(buf, off[0]);
      off[0] = off[0] + 2;
      if (pr.length() == 0) {
        this.parent = null;
      } else {
        try {
          this.parent = Resource.load(pr, pver);
        } catch (RuntimeException e) {
          throw new Resource.LoadException("Illegal resource dependency", e, Resource.this);
        } 
      } 
      this.name = Utils.strd(buf, off);
      Utils.strd(buf, off);
      this.hk = (char)Utils.uint16d(buf, off[0]);
      off[0] = off[0] + 2;
      this.ad = new String[Utils.uint16d(buf, off[0])];
      off[0] = off[0] + 2;
      for (int i = 0; i < this.ad.length; i++)
        this.ad[i] = Utils.strd(buf, off); 
    }
    
    public void init() {}
  }
  
  @LayerName("code")
  public class Code extends Layer {
    public final String name;
    
    public final transient byte[] data;
    
    public Code(byte[] buf) {
      int[] off = new int[1];
      off[0] = 0;
      this.name = Utils.strd(buf, off);
      this.data = new byte[buf.length - off[0]];
      System.arraycopy(buf, off[0], this.data, 0, this.data.length);
    }
    
    public void init() {}
  }
  
  public class ResClassLoader extends ClassLoader {
    public ResClassLoader(ClassLoader parent) {
      super(parent);
    }
    
    public Resource getres() {
      return Resource.this;
    }
    
    public String toString() {
      return "cl:" + Resource.this.toString();
    }
  }
  
  public static Resource classres(final Class<?> cl) {
    return AccessController.<Resource>doPrivileged(new PrivilegedAction<Resource>() {
          public Resource run() {
            ClassLoader l = cl.getClassLoader();
            if (l instanceof Resource.ResClassLoader)
              return ((Resource.ResClassLoader)l).getres(); 
            throw new RuntimeException("Cannot fetch resource of non-resloaded class " + cl);
          }
        });
  }
  
  public <T> T getcode(Class<T> cl, boolean fail) {
    CodeEntry e = layer(CodeEntry.class);
    if (e == null) {
      if (fail)
        throw new RuntimeException("Tried to fetch non-present res-loaded class " + cl.getName() + " from " + this.name); 
      return null;
    } 
    return e.get(cl, fail);
  }
  
  public static class LibClassLoader extends ClassLoader {
    private final ClassLoader[] classpath;
    
    public LibClassLoader(ClassLoader parent, Collection<ClassLoader> classpath) {
      super(parent);
      this.classpath = classpath.<ClassLoader>toArray(new ClassLoader[0]);
    }
    
    public Class<?> findClass(String name) throws ClassNotFoundException {
      for (ClassLoader lib : this.classpath) {
        try {
          return lib.loadClass(name);
        } catch (ClassNotFoundException classNotFoundException) {}
      } 
      throw new ClassNotFoundException("Could not find " + name + " in any of " + Arrays.asList(this.classpath).toString());
    }
  }
  
  @LayerName("codeentry")
  public class CodeEntry extends Layer {
    private String clnm;
    
    private final Map<String, Resource.Code> clmap = new TreeMap<>();
    
    private final Map<String, String> pe = new TreeMap<>();
    
    private final Collection<Resource> classpath = new LinkedList<>();
    
    private transient ClassLoader loader;
    
    private transient Map<String, Class<?>> lpe = null;
    
    private transient Map<Class<?>, Object> ipe = new HashMap<>();
    
    public CodeEntry(byte[] buf) {
      int[] off = new int[1];
      off[0] = 0;
      label24: while (off[0] < buf.length) {
        off[0] = off[0] + 1;
        int t = buf[off[0]];
        if (t == 1)
          while (true) {
            String en = Utils.strd(buf, off);
            String cn = Utils.strd(buf, off);
            if (en.length() == 0)
              continue label24; 
            this.pe.put(en, cn);
          }  
        if (t == 2)
          while (true) {
            String ln = Utils.strd(buf, off);
            if (ln.length() == 0)
              continue label24; 
            int ver = Utils.uint16d(buf, off[0]);
            off[0] = off[0] + 2;
            this.classpath.add(Resource.load(ln, ver));
          }  
        throw new Resource.LoadException("Unknown codeentry data type: " + t, Resource.this);
      } 
    }
    
    public void init() {
      for (Resource.Code c : Resource.this.<Resource.Code>layers(Resource.Code.class, false))
        this.clmap.put(c.name, c); 
    }
    
    public ClassLoader loader(final boolean wait) {
      synchronized (this) {
        if (this.loader == null)
          this.loader = AccessController.<ClassLoader>doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                  ClassLoader parent = Resource.class.getClassLoader();
                  if (Resource.CodeEntry.this.classpath.size() > 0) {
                    Collection<ClassLoader> loaders = new LinkedList<>();
                    for (Resource res : Resource.CodeEntry.this.classpath) {
                      if (wait)
                        res.loadwait(); 
                      loaders.add(((Resource.CodeEntry)res.<Resource.CodeEntry>layer(Resource.CodeEntry.class)).loader(wait));
                    } 
                    parent = new Resource.LibClassLoader(parent, loaders);
                  } 
                  return new Resource.ResClassLoader(parent) {
                      public Class<?> findClass(String name) throws ClassNotFoundException {
                        Resource.Code c = (Resource.Code)Resource.CodeEntry.this.clmap.get(name);
                        if (c == null)
                          throw new ClassNotFoundException("Could not find class " + name + " in resource (" + Resource.this + ")"); 
                        return defineClass(name, c.data, 0, c.data.length);
                      }
                    };
                }
              }); 
      } 
      return this.loader;
    }
    
    private void load() {
      synchronized (CodeEntry.class) {
        if (this.lpe != null)
          return; 
        ClassLoader loader = loader(false);
        this.lpe = new TreeMap<>();
        try {
          for (Map.Entry<String, String> e : this.pe.entrySet()) {
            String name = e.getKey();
            String clnm = e.getValue();
            Class<?> cl = loader.loadClass(clnm);
            this.lpe.put(name, cl);
          } 
        } catch (ClassNotFoundException e) {
          throw new Resource.LoadException(e, Resource.this);
        } 
      } 
    }
    
    public <T> Class<? extends T> getcl(Class<T> cl, boolean fail) {
      Class<?> acl;
      load();
      Resource.PublishedCode entry = cl.<Resource.PublishedCode>getAnnotation(Resource.PublishedCode.class);
      if (entry == null)
        throw new RuntimeException("Tried to fetch non-published res-loaded class " + cl.getName() + " from " + Resource.this.name); 
      synchronized (this.lpe) {
        if ((acl = this.lpe.get(entry.name())) == null) {
          if (fail)
            throw new RuntimeException("Tried to fetch non-present res-loaded class " + cl.getName() + " from " + Resource.this.name); 
          return null;
        } 
      } 
      return acl.asSubclass(cl);
    }
    
    public <T> Class<? extends T> getcl(Class<T> cl) {
      return getcl(cl, true);
    }
    
    public <T> T get(Class<T> cl, boolean fail) {
      Class<?> acl;
      load();
      Resource.PublishedCode entry = cl.<Resource.PublishedCode>getAnnotation(Resource.PublishedCode.class);
      if (entry == null)
        throw new RuntimeException("Tried to fetch non-published res-loaded class " + cl.getName() + " from " + Resource.this.name); 
      synchronized (this.lpe) {
        if ((acl = this.lpe.get(entry.name())) == null) {
          if (fail)
            throw new RuntimeException("Tried to fetch non-present res-loaded class " + cl.getName() + " from " + Resource.this.name); 
          return null;
        } 
      } 
      try {
        synchronized (this.ipe) {
          T inst;
          Object rinst, pinst;
          if ((pinst = this.ipe.get(acl)) != null)
            return cl.cast(pinst); 
          if (entry.instancer() != Resource.PublishedCode.Instancer.class) {
            rinst = ((Resource.PublishedCode.Instancer)entry.instancer().newInstance()).make(acl);
          } else {
            rinst = acl.newInstance();
          } 
          try {
            inst = cl.cast(rinst);
          } catch (ClassCastException e) {
            throw new ClassCastException("Published class in " + Resource.this.name + " is not of type " + cl);
          } 
          this.ipe.put(acl, inst);
          return inst;
        } 
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } 
    }
    
    public <T> T get(Class<T> cl) {
      return get(cl, true);
    }
  }
  
  @LayerName("audio")
  public class Audio extends Layer implements IDLayer<String> {
    public transient byte[] coded;
    
    public final String id;
    
    public double bvol = 1.0D;
    
    public Audio(byte[] coded, String id) {
      this.coded = coded;
      this.id = id.intern();
    }
    
    public Audio(byte[] buf) {
      this(buf, "cl");
    }
    
    public void init() {}
    
    public InputStream pcmstream() {
      try {
        return (new VorbisStream(new ByteArrayInputStream(this.coded))).pcmstream();
      } catch (IOException e) {
        throw new RuntimeException(e);
      } 
    }
    
    public String layerid() {
      return this.id;
    }
  }
  
  @LayerName("audio2")
  public static class Audio2 implements LayerFactory<Audio> {
    public Resource.Audio cons(Resource res, byte[] buf) {
      int[] off = { 0 };
      off[0] = off[0] + 1;
      int ver = buf[off[0]];
      if (ver == 1 || ver == 2) {
        String id = Utils.strd(buf, off);
        double bvol = 1.0D;
        if (ver == 2) {
          bvol = Utils.uint16d(buf, off[0]) / 1000.0D;
          off[0] = off[0] + 2;
        } 
        byte[] data = new byte[buf.length - off[0]];
        System.arraycopy(buf, off[0], data, 0, buf.length - off[0]);
        res.getClass();
        Resource.Audio ret = new Resource.Audio(data, id);
        ret.bvol = bvol;
        return ret;
      } 
      throw new Resource.LoadException("Unknown audio layer version: " + ver, res);
    }
  }
  
  @LayerName("midi")
  public class Music extends Layer {
    transient Sequence seq;
    
    public Music(byte[] buf) {
      try {
        this.seq = MidiSystem.getSequence(new ByteArrayInputStream(buf));
      } catch (InvalidMidiDataException e) {
        throw new Resource.LoadException("Invalid MIDI data", Resource.this);
      } catch (IOException e) {
        throw new Resource.LoadException(e, Resource.this);
      } 
    }
    
    public void init() {}
  }
  
  @LayerName("font")
  public class Font extends Layer {
    public final transient java.awt.Font font;
    
    public Font(byte[] buf) {
      int[] off = { 0 };
      off[0] = off[0] + 1;
      int ver = buf[off[0]];
      if (ver == 1) {
        off[0] = off[0] + 1;
        int type = buf[off[0]];
        if (type == 0) {
          try {
            this.font = java.awt.Font.createFont(0, new ByteArrayInputStream(buf, off[0], buf.length - off[0]));
          } catch (Exception e) {
            throw new RuntimeException(e);
          } 
        } else {
          throw new Resource.LoadException("Unknown font type: " + type, Resource.this);
        } 
      } else {
        throw new Resource.LoadException("Unknown font layer version: " + ver, Resource.this);
      } 
    }
    
    public void init() {}
  }
  
  private void readall(InputStream in, byte[] buf) throws IOException {
    int off = 0;
    while (off < buf.length) {
      int ret = in.read(buf, off, buf.length - off);
      if (ret < 0)
        throw new LoadException("Incomplete resource at " + this.name, this); 
      off += ret;
    } 
  }
  
  public <L extends Layer> Collection<L> layers(final Class<L> cl, boolean th) {
    if (this.loading && th)
      throw new Loading(this); 
    checkerr();
    return new AbstractCollection<L>() {
        public int size() {
          int s = 0;
          for (Resource.Layer layer : this)
            s++; 
          return s;
        }
        
        public Iterator<L> iterator() {
          return new Iterator() {
              Iterator<Resource.Layer> i = Resource.this.layers.iterator();
              
              L c = n();
              
              private L n() {
                while (this.i.hasNext()) {
                  Resource.Layer l = this.i.next();
                  if (cl.isInstance(l))
                    return (L)cl.cast(l); 
                } 
                return null;
              }
              
              public boolean hasNext() {
                return (this.c != null);
              }
              
              public L next() {
                L ret = this.c;
                if (ret == null)
                  throw new NoSuchElementException(); 
                this.c = n();
                return ret;
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
      };
  }
  
  public <L extends Layer> Collection<L> layers(Class<L> cl) {
    return layers(cl, true);
  }
  
  public <L extends Layer> L layer(Class<L> cl, boolean th) {
    if (this.loading && th)
      throw new Loading(this); 
    checkerr();
    for (Layer l : this.layers) {
      if (cl.isInstance(l))
        return cl.cast(l); 
    } 
    return null;
  }
  
  public <L extends Layer> L layer(Class<L> cl) {
    return layer(cl, true);
  }
  
  public <I, L extends IDLayer<I>> L layer(Class<L> cl, I id) {
    if (this.loading)
      throw new Loading(this); 
    checkerr();
    for (Layer l : this.layers) {
      if (cl.isInstance(l)) {
        IDLayer<T> iDLayer = (IDLayer)cl.cast(l);
        if (iDLayer.layerid().equals(id))
          return (L)iDLayer; 
      } 
    } 
    return null;
  }
  
  public int compareTo(Resource other) {
    checkerr();
    int nc = this.name.compareTo(other.name);
    if (nc != 0)
      return nc; 
    if (this.ver != other.ver)
      return this.ver - other.ver; 
    if (other != this)
      throw new RuntimeException("Resource identity crisis!"); 
    return 0;
  }
  
  public boolean equals(Object other) {
    if (!(other instanceof Resource))
      return false; 
    Resource o = (Resource)other;
    return (o.name.equals(this.name) && o.ver == this.ver);
  }
  
  private void load(InputStream in) throws IOException {
    String sig = "Haven Resource 1";
    byte[] buf = new byte["Haven Resource 1".length()];
    readall(in, buf);
    if (!"Haven Resource 1".equals(new String(buf)))
      throw new LoadException("Invalid res signature", this); 
    buf = new byte[2];
    readall(in, buf);
    int ver = Utils.uint16d(buf, 0);
    List<Layer> layers = new LinkedList<>();
    if (this.ver == -1) {
      this.ver = ver;
    } else if (ver != this.ver) {
      throw new LoadException("Wrong res version (" + ver + " != " + this.ver + ")", this);
    } 
    while (true) {
      StringBuilder tbuf = new StringBuilder();
      while (true) {
        int len, ib;
        if ((ib = in.read()) == -1) {
          if (tbuf.length() == 0)
            break; 
          throw new LoadException("Incomplete resource at " + this.name, this);
        } 
        byte bb = (byte)ib;
        if (bb == 0) {
          buf = new byte[4];
          readall(in, buf);
          len = Utils.int32d(buf, 0);
          buf = new byte[len];
          readall(in, buf);
          LayerFactory<?> lc = ltypes.get(tbuf.toString());
          if (lc == null)
            continue; 
          layers.add((Layer)lc.cons(this, buf));
          continue;
        } 
        tbuf.append((char)len);
      } 
      break;
    } 
    this.layers = layers;
    for (Layer l : layers)
      l.init(); 
  }
  
  public Indir<Resource> indir() {
    if (this.indir != null)
      return this.indir; 
    this.indir = new Indir<Resource>() {
        public Resource res = Resource.this;
        
        public Resource get() {
          if (Resource.this.loading)
            throw new Resource.Loading(Resource.this); 
          return Resource.this;
        }
        
        public void set(Resource r) {
          throw new RuntimeException();
        }
        
        public int compareTo(Indir<Resource> x) {
          return Resource.this.compareTo(((null)getClass().cast(x)).res);
        }
      };
    return this.indir;
  }
  
  public void checkerr() {}
  
  public int priority() {
    return this.prio;
  }
  
  public static BufferedImage loadimg(String name) {
    Resource res = load(name);
    res.loadwait();
    return ((Image)res.layer((Class)imgc)).img;
  }
  
  public static Tex loadtex(String name) {
    Resource res = load(name);
    res.loadwait();
    return ((Image)res.<Image>layer(imgc)).tex();
  }
  
  public String toString() {
    return this.name + "(v" + this.ver + ")";
  }
  
  public static void loadlist(InputStream list, int prio) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(list, "us-ascii"));
    String ln;
    while ((ln = in.readLine()) != null) {
      int ver, pos = ln.indexOf(':');
      if (pos < 0)
        continue; 
      String nm = ln.substring(0, pos);
      try {
        ver = Integer.parseInt(ln.substring(pos + 1));
      } catch (NumberFormatException e) {
        continue;
      } 
      try {
        load(nm, ver, prio);
      } catch (RuntimeException runtimeException) {}
    } 
    in.close();
  }
  
  public static void dumplist(Collection<Resource> list, Writer dest) {
    PrintWriter out = new PrintWriter(dest);
    List<Resource> sorted = new ArrayList<>(list);
    Collections.sort(sorted);
    for (Resource res : sorted) {
      if (res.loading)
        continue; 
      out.println(res.name + ":" + res.ver);
    } 
  }
  
  public static void updateloadlist(File file) throws Exception {
    BufferedReader r = new BufferedReader(new FileReader(file));
    Map<String, Integer> orig = new HashMap<>();
    String ln;
    while ((ln = r.readLine()) != null) {
      int pos = ln.indexOf(':');
      if (pos < 0) {
        System.err.println("Weird line: " + ln);
        continue;
      } 
      String nm = ln.substring(0, pos);
      int ver = Integer.parseInt(ln.substring(pos + 1));
      orig.put(nm, Integer.valueOf(ver));
    } 
    r.close();
    for (String nm : orig.keySet())
      load(nm); 
    while (true) {
      int d = qdepth();
      if (d == 0)
        break; 
      System.out.print("\033[1GLoading... " + d + "\033[K");
      Thread.sleep(500L);
    } 
    System.out.println();
    Collection<Resource> cur = new LinkedList<>();
    for (Map.Entry<String, Integer> e : orig.entrySet()) {
      String nm = e.getKey();
      int ver = ((Integer)e.getValue()).intValue();
      Resource res = load(nm);
      res.loadwait();
      res.checkerr();
      if (res.ver != ver)
        System.out.println(nm + ": " + ver + " -> " + res.ver); 
      cur.add(res);
    } 
    Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
    try {
      dumplist(cur, w);
    } finally {
      w.close();
    } 
  }
  
  public static void main(String[] args) throws Exception {
    String cmd = args[0].intern();
    if (cmd == "update")
      updateloadlist(new File(args[1])); 
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE})
  public static @interface PublishedCode {
    String name();
    
    Class<? extends Instancer> instancer() default Instancer.class;
    
    public static interface Instancer {
      Object make(Class<?> param2Class) throws InstantiationException, IllegalAccessException;
    }
  }
  
  public static interface IDLayer<T> {
    T layerid();
  }
  
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Discoverable
  public static @interface LayerName {
    String value();
  }
  
  public static interface LayerFactory<T extends Layer> {
    T cons(Resource param1Resource, byte[] param1ArrayOfbyte);
  }
  
  public static interface ResSource {
    InputStream get(String param1String) throws IOException;
  }
}
