package haven;

import dolda.jglob.Discoverable;
import dolda.xiphutil.VorbisStream;
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
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

public class Resource implements Comparable<Resource>, Prioritized, Serializable {
   private static final Map<String, Resource> cache;
   private static Resource.Loader loader;
   private static Resource.CacheSource prscache;
   public static ThreadGroup loadergroup = null;
   private static Map<String, Resource.LayerFactory<?>> ltypes = new TreeMap<>();
   static Set<Resource> loadwaited = new HashSet<>();
   public static Class<Resource.Image> imgc = Resource.Image.class;
   public static Class<Resource.Tile> tile = Resource.Tile.class;
   public static Class<Resource.Neg> negc = Resource.Neg.class;
   public static Class<Resource.Anim> animc = Resource.Anim.class;
   public static Class<Resource.Tileset> tileset = Resource.Tileset.class;
   public static Class<Resource.Pagina> pagina = Resource.Pagina.class;
   public static Class<Resource.AButton> action = Resource.AButton.class;
   public static Class<Resource.Audio> audio = Resource.Audio.class;
   public static Class<Resource.Tooltip> tooltip = Resource.Tooltip.class;
   private Resource.LoadException error;
   private Collection<Resource.Layer> layers = new LinkedList<>();
   public final String name;
   public int ver;
   public boolean loading;
   public Resource.ResSource source;
   private transient Indir<Resource> indir = null;
   int prio = 0;

   private Resource(String name, int ver) {
      this.name = name;
      this.ver = ver;
      this.error = null;
      this.loading = true;
   }

   public static void addcache(ResCache cache) {
      Resource.CacheSource src = new Resource.CacheSource(cache);
      prscache = src;
      chainloader(new Resource.Loader(src));
   }

   public static void addurl(URL url) {
      Resource.ResSource src = new Resource.HttpSource(url);
      final Resource.CacheSource mc = prscache;
      if (mc != null) {
         src = new Resource.TeeSource(src) {
            @Override
            public OutputStream fork(String name) throws IOException {
               return mc.cache.store("res/" + name);
            }
         };
      }

      chainloader(new Resource.Loader(src));
   }

   private static void chainloader(Resource.Loader nl) {
      synchronized (Resource.class) {
         if (loader == null) {
            loader = nl;
         } else {
            Resource.Loader l = loader;

            while (l.next != null) {
               l = l.next;
            }

            l.chain(nl);
         }
      }
   }

   public static Resource load(String name, int ver, int prio) {
      Resource res;
      synchronized (cache) {
         res = cache.get(name);
         if (res != null) {
            if (res.ver != -1 && ver != -1) {
               if (res.ver < ver) {
                  res = null;
                  cache.remove(name);
               } else if (res.ver > ver) {
                  throw new Resource.LoadException(
                     String.format("Weird version number on %s (%d > %d), loaded from %s", res.name, res.ver, ver, res.source), res
                  );
               }
            } else if (ver == -1 && res.error != null) {
               res = null;
               cache.remove(name);
            }
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

      for (Resource.Loader l = loader; l != null; l = l.next) {
         ret += l.queue.size();
      }

      return ret;
   }

   public static Resource load(String name) {
      return load(name, -1);
   }

   public void boostprio(int newprio) {
      if (this.prio < newprio) {
         this.prio = newprio;
      }
   }

   public Resource loadwaitint() throws InterruptedException {
      synchronized (this) {
         this.boostprio(10);

         while (this.loading) {
            this.wait();
         }

         return this;
      }
   }

   public String basename() {
      int p = this.name.lastIndexOf(47);
      return p < 0 ? this.name : this.name.substring(p + 1);
   }

   public Resource loadwait() {
      boolean i = false;
      synchronized (loadwaited) {
         loadwaited.add(this);
      }

      synchronized (this) {
         this.boostprio(10);

         while (this.loading) {
            try {
               this.wait();
            } catch (InterruptedException var5) {
               i = true;
            }
         }
      }

      if (i) {
         Thread.currentThread().interrupt();
      }

      return this;
   }

   public static Coord cdec(byte[] buf, int off) {
      return new Coord(Utils.int16d(buf, off), Utils.int16d(buf, off + 2));
   }

   public static void addltype(String name, Resource.LayerFactory<?> cons) {
      ltypes.put(name, cons);
   }

   public static <T extends Resource.Layer> void addltype(String name, Class<T> cl) {
      addltype(name, new Resource.LayerConstructor<>(cl));
   }

   public static Resource classres(final Class<?> cl) {
      return AccessController.doPrivileged(new PrivilegedAction<Resource>() {
         public Resource run() {
            ClassLoader l = cl.getClassLoader();
            if (l instanceof Resource.ResClassLoader) {
               return ((Resource.ResClassLoader)l).getres();
            } else {
               throw new RuntimeException("Cannot fetch resource of non-resloaded class " + cl);
            }
         }
      });
   }

   public <T> T getcode(Class<T> cl, boolean fail) {
      Resource.CodeEntry e = this.layer(Resource.CodeEntry.class);
      if (e == null) {
         if (fail) {
            throw new RuntimeException("Tried to fetch non-present res-loaded class " + cl.getName() + " from " + this.name);
         } else {
            return null;
         }
      } else {
         return e.get(cl, fail);
      }
   }

   private void readall(InputStream in, byte[] buf) throws IOException {
      int off = 0;

      while (off < buf.length) {
         int ret = in.read(buf, off, buf.length - off);
         if (ret < 0) {
            throw new Resource.LoadException("Incomplete resource at " + this.name, this);
         }

         off += ret;
      }
   }

   public <L extends Resource.Layer> Collection<L> layers(final Class<L> cl, boolean th) {
      if (this.loading && th) {
         throw new Resource.Loading(this);
      } else {
         this.checkerr();
         return new AbstractCollection<L>() {
            @Override
            public int size() {
               int s = 0;

               for (L l : this) {
                  s++;
               }

               return s;
            }

            @Override
            public Iterator<L> iterator() {
               return new Iterator<L>() {
                  Iterator<Resource.Layer> i = Resource.this.layers.iterator();
                  L c = (L)this.n();

                  private L n() {
                     while (this.i.hasNext()) {
                        Resource.Layer l = this.i.next();
                        if (cl.isInstance(l)) {
                           return cl.cast(l);
                        }
                     }

                     return null;
                  }

                  @Override
                  public boolean hasNext() {
                     return this.c != null;
                  }

                  public L next() {
                     L ret = this.c;
                     if (ret == null) {
                        throw new NoSuchElementException();
                     } else {
                        this.c = (L)this.n();
                        return ret;
                     }
                  }

                  @Override
                  public void remove() {
                     throw new UnsupportedOperationException();
                  }
               };
            }
         };
      }
   }

   public <L extends Resource.Layer> Collection<L> layers(Class<L> cl) {
      return this.layers(cl, true);
   }

   public <L extends Resource.Layer> L layer(Class<L> cl, boolean th) {
      if (this.loading && th) {
         throw new Resource.Loading(this);
      } else {
         this.checkerr();

         for (Resource.Layer l : this.layers) {
            if (cl.isInstance(l)) {
               return cl.cast(l);
            }
         }

         return null;
      }
   }

   public <L extends Resource.Layer> L layer(Class<L> cl) {
      return this.layer(cl, true);
   }

   public <I, L extends Resource.IDLayer<I>> L layer(Class<L> cl, I id) {
      if (this.loading) {
         throw new Resource.Loading(this);
      } else {
         this.checkerr();

         for (Resource.Layer l : this.layers) {
            if (cl.isInstance(l)) {
               L ll = (L)cl.cast(l);
               if (ll.layerid().equals(id)) {
                  return ll;
               }
            }
         }

         return null;
      }
   }

   public int compareTo(Resource other) {
      this.checkerr();
      int nc = this.name.compareTo(other.name);
      if (nc != 0) {
         return nc;
      } else if (this.ver != other.ver) {
         return this.ver - other.ver;
      } else if (other != this) {
         throw new RuntimeException("Resource identity crisis!");
      } else {
         return 0;
      }
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof Resource)) {
         return false;
      } else {
         Resource o = (Resource)other;
         return o.name.equals(this.name) && o.ver == this.ver;
      }
   }

   private void load(InputStream in) throws IOException {
      String sig = "Haven Resource 1";
      byte[] buf = new byte[sig.length()];
      this.readall(in, buf);
      if (!sig.equals(new String(buf))) {
         throw new Resource.LoadException("Invalid res signature", this);
      } else {
         buf = new byte[2];
         this.readall(in, buf);
         int ver = Utils.uint16d(buf, 0);
         List<Resource.Layer> layers = new LinkedList<>();
         if (this.ver == -1) {
            this.ver = ver;
         } else if (ver != this.ver) {
            throw new Resource.LoadException("Wrong res version (" + ver + " != " + this.ver + ")", this);
         }

         label46:
         while (true) {
            StringBuilder tbuf = new StringBuilder();

            int ib;
            while ((ib = in.read()) != -1) {
               byte bb = (byte)ib;
               if (bb == 0) {
                  buf = new byte[4];
                  this.readall(in, buf);
                  bb = (byte)Utils.int32d(buf, 0);
                  buf = new byte[bb];
                  this.readall(in, buf);
                  Resource.LayerFactory<?> lc = ltypes.get(tbuf.toString());
                  if (lc != null) {
                     layers.add(lc.cons(this, buf));
                  }
                  continue label46;
               }

               tbuf.append((char)bb);
            }

            if (tbuf.length() != 0) {
               throw new Resource.LoadException("Incomplete resource at " + this.name, this);
            }

            this.layers = layers;

            for (Resource.Layer l : layers) {
               l.init();
            }

            return;
         }
      }
   }

   public Indir<Resource> indir() {
      if (this.indir != null) {
         return this.indir;
      } else {
         this.indir = new Indir<Resource>() {
            public Resource res = Resource.this;

            public Resource get() {
               if (Resource.this.loading) {
                  throw new Resource.Loading(Resource.this);
               } else {
                  return Resource.this;
               }
            }

            public void set(Resource r) {
               throw new RuntimeException();
            }

             public int compareTo(Indir<Resource> x) {
                try {
                   Resource rx = x.get();
                   if (rx == null) return -1;
                   return Resource.this.compareTo(rx);
                } catch (Loading e) {
                   return -1;
                }
             }
         };
         return this.indir;
      }
   }

   public void checkerr() {
      if (!this.loading && this.error != null) {
         throw new RuntimeException("Delayed error in resource " + this.name + " (v" + this.ver + "), from " + this.source, this.error);
      }
   }

   @Override
   public int priority() {
      return this.prio;
   }

   public static BufferedImage loadimg(String name) {
      Resource res = load(name);
      res.loadwait();
      return res.layer(imgc).img;
   }

   public static Tex loadtex(String name) {
      Resource res = load(name);
      res.loadwait();
      return res.layer(imgc).tex();
   }

   @Override
   public String toString() {
      return this.name + "(v" + this.ver + ")";
   }

   public static void loadlist(InputStream list, int prio) throws IOException {
      BufferedReader in = new BufferedReader(new InputStreamReader(list, "us-ascii"));

      String ln;
      while ((ln = in.readLine()) != null) {
         int pos = ln.indexOf(58);
         if (pos >= 0) {
            String nm = ln.substring(0, pos);

            int ver;
            try {
               ver = Integer.parseInt(ln.substring(pos + 1));
            } catch (NumberFormatException var9) {
               continue;
            }

            try {
               load(nm, ver, prio);
            } catch (RuntimeException var8) {
            }
         }
      }

      in.close();
   }

   public static void dumplist(Collection<Resource> list, Writer dest) {
      PrintWriter out = new PrintWriter(dest);
      List<Resource> sorted = new ArrayList<>(list);
      Collections.sort(sorted);

      for (Resource res : sorted) {
         if (!res.loading) {
            out.println(res.name + ":" + res.ver);
         }
      }
   }

   public static void updateloadlist(File file) throws Exception {
      BufferedReader r = new BufferedReader(new FileReader(file));
      Map<String, Integer> orig = new HashMap<>();

      String ln;
      while ((ln = r.readLine()) != null) {
         int pos = ln.indexOf(58);
         if (pos < 0) {
            System.err.println("Weird line: " + ln);
         } else {
            String nm = ln.substring(0, pos);
            int ver = Integer.parseInt(ln.substring(pos + 1));
            orig.put(nm, ver);
         }
      }

      r.close();

      for (String nm : orig.keySet()) {
         load(nm);
      }

      while (true) {
         int d = qdepth();
         if (d == 0) {
            System.out.println();
            Collection<Resource> cur = new LinkedList<>();

            for (Entry<String, Integer> e : orig.entrySet()) {
               String nm = e.getKey();
               int ver = e.getValue();
               Resource res = load(nm);
               res.loadwait();
               res.checkerr();
               if (res.ver != ver) {
                  System.out.println(nm + ": " + ver + " -> " + res.ver);
               }

               cur.add(res);
            }

            Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            try {
               dumplist(cur, w);
            } finally {
               w.close();
            }

            return;
         }

         System.out.print("\u001b[1GLoading... " + d + "\u001b[K");
         Thread.sleep(500L);
      }
   }

   public static void main(String[] args) throws Exception {
      String cmd = args[0].intern();
      if (cmd == "update") {
         updateloadlist(new File(args[1]));
      }
   }

   static {
      if (Config.softres) {
         cache = new CacheMap<>();
      } else {
         cache = new TreeMap<>();
      }

      if (!Config.nolocalres) {
         loader = new Resource.Loader(new Resource.JarSource());
      }

      try {
         String dir = Config.resdir;
         if (dir == null) {
            dir = System.getenv("SALEM_RESDIR");
         }

         if (dir != null) {
            chainloader(new Resource.Loader(new Resource.FileSource(Utils.path(dir))));
         }
      } catch (Exception var6) {
      }

      for (Class<?> cl : dolda.jglob.Loader.get(Resource.LayerName.class).classes()) {
         String nm = cl.getAnnotation(Resource.LayerName.class).value();
         if (Resource.LayerFactory.class.isAssignableFrom(cl)) {
            try {
               addltype(nm, (Resource.LayerFactory<?>)cl.asSubclass(Resource.LayerFactory.class).newInstance());
            } catch (InstantiationException var4) {
               throw new Error(var4);
            } catch (IllegalAccessException var5) {
               throw new Error(var5);
            }
         } else {
            if (!Resource.Layer.class.isAssignableFrom(cl)) {
               throw new Error("Illegal resource layer class: " + cl);
            }

            addltype(nm, cl.asSubclass(Resource.Layer.class));
         }
      }
   }

   @Resource.LayerName("action")
   public class AButton extends Resource.Layer {
      public final String name;
      public final Resource parent;
      public final char hk;
      public final String[] ad;

      public AButton(byte[] buf) {
         int[] off = new int[]{0};
         String pr = Utils.strd(buf, off);
         int pver = Utils.uint16d(buf, off[0]);
         off[0] += 2;
         if (pr.length() == 0) {
            this.parent = null;
         } else {
            try {
               this.parent = Resource.load(pr, pver);
            } catch (RuntimeException var7) {
               throw new Resource.LoadException("Illegal resource dependency", var7, Resource.this);
            }
         }

         this.name = Utils.strd(buf, off);
         Utils.strd(buf, off);
         this.hk = (char)Utils.uint16d(buf, off[0]);
         off[0] += 2;
         this.ad = new String[Utils.uint16d(buf, off[0])];
         off[0] += 2;

         for (int i = 0; i < this.ad.length; i++) {
            this.ad[i] = Utils.strd(buf, off);
         }
      }

      @Override
      public void init() {
      }
   }

   @Resource.LayerName("anim")
   public class Anim extends Resource.Layer {
      private int[] ids;
      public int id;
      public int d;
      public Resource.Image[][] f;

      public Anim(byte[] buf) {
         this.id = Utils.int16d(buf, 0);
         this.d = Utils.uint16d(buf, 2);
         this.ids = new int[Utils.uint16d(buf, 4)];
         if (buf.length - 6 != this.ids.length * 2) {
            throw new Resource.LoadException("Invalid anim descriptor in " + Resource.this.name, Resource.this);
         } else {
            for (int i = 0; i < this.ids.length; i++) {
               this.ids[i] = Utils.int16d(buf, 6 + i * 2);
            }
         }
      }

      @Override
      public void init() {
         this.f = new Resource.Image[this.ids.length][];
         Resource.Image[] typeinfo = new Resource.Image[0];

         for (int i = 0; i < this.ids.length; i++) {
            LinkedList<Resource.Image> buf = new LinkedList<>();

            for (Resource.Image img : Resource.this.layers(Resource.Image.class, false)) {
               if (img.id == this.ids[i]) {
                  buf.add(img);
               }
            }

            this.f[i] = buf.toArray(typeinfo);
         }
      }
   }

   @Resource.LayerName("audio")
   public class Audio extends Resource.Layer implements Resource.IDLayer<String> {
      public transient byte[] coded;
      public final String id;
      public double bvol = 1.0;

      public Audio(byte[] coded, String id) {
         this.coded = coded;
         this.id = id.intern();
      }

      public Audio(byte[] buf) {
         this(buf, "cl");
      }

      @Override
      public void init() {
      }

      public InputStream pcmstream() {
         try {
            return new VorbisStream(new ByteArrayInputStream(this.coded)).pcmstream();
         } catch (IOException var2) {
            throw new RuntimeException(var2);
         }
      }

      public String layerid() {
         return this.id;
      }
   }

   @Resource.LayerName("audio2")
   public static class Audio2 implements Resource.LayerFactory<Resource.Audio> {
      public Resource.Audio cons(Resource res, byte[] buf) {
         int[] off = new int[]{0};
         int ver = buf[off[0]++];
         if (ver != 1 && ver != 2) {
            throw new Resource.LoadException("Unknown audio layer version: " + ver, res);
         } else {
            String id = Utils.strd(buf, off);
            double bvol = 1.0;
            if (ver == 2) {
               bvol = Utils.uint16d(buf, off[0]) / 1000.0;
               off[0] += 2;
            }

            byte[] data = new byte[buf.length - off[0]];
            System.arraycopy(buf, off[0], data, 0, buf.length - off[0]);
            Resource.Audio ret = res.new Audio(data, id);
            ret.bvol = bvol;
            return ret;
         }
      }
   }

   public static class CacheSource implements Resource.ResSource, Serializable {
      public transient ResCache cache;

      public CacheSource(ResCache cache) {
         this.cache = cache;
      }

      @Override
      public InputStream get(String name) throws IOException {
         return this.cache.fetch("res/" + name);
      }

      @Override
      public String toString() {
         return "cache source backed by " + this.cache;
      }
   }

   @Resource.LayerName("code")
   public class Code extends Resource.Layer {
      public final String name;
      public final transient byte[] data;

      public Code(byte[] buf) {
         int[] off = new int[]{0};
         this.name = Utils.strd(buf, off);
         this.data = new byte[buf.length - off[0]];
         System.arraycopy(buf, off[0], this.data, 0, this.data.length);
      }

      @Override
      public void init() {
      }
   }

   @Resource.LayerName("codeentry")
   public class CodeEntry extends Resource.Layer {
      private String clnm;
      private Map<String, Resource.Code> clmap = new TreeMap<>();
      private Map<String, String> pe = new TreeMap<>();
      private Collection<Resource> classpath = new LinkedList<>();
      private transient ClassLoader loader;
      private transient Map<String, Class<?>> lpe = null;
      private transient Map<Class<?>, Object> ipe = new HashMap<>();

      public CodeEntry(byte[] buf) {
         int[] off = new int[]{0};

         while (off[0] < buf.length) {
            int t = buf[off[0]++];
            if (t == 1) {
               while (true) {
                  String en = Utils.strd(buf, off);
                  String cn = Utils.strd(buf, off);
                  if (en.length() == 0) {
                     break;
                  }

                  this.pe.put(en, cn);
               }
            } else {
               if (t != 2) {
                  throw new Resource.LoadException("Unknown codeentry data type: " + t, Resource.this);
               }

               while (true) {
                  String ln = Utils.strd(buf, off);
                  if (ln.length() == 0) {
                     break;
                  }

                  int ver = Utils.uint16d(buf, off[0]);
                  off[0] += 2;
                  this.classpath.add(Resource.load(ln, ver));
               }
            }
         }
      }

      @Override
      public void init() {
         for (Resource.Code c : Resource.this.layers(Resource.Code.class, false)) {
            this.clmap.put(c.name, c);
         }
      }

      public ClassLoader loader(final boolean wait) {
         synchronized (this) {
            if (this.loader == null) {
               this.loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                  public ClassLoader run() {
                     ClassLoader parent = Resource.class.getClassLoader();
                     if (CodeEntry.this.classpath.size() > 0) {
                        Collection<ClassLoader> loaders = new LinkedList<>();

                        for (Resource res : CodeEntry.this.classpath) {
                           if (wait) {
                              res.loadwait();
                           }

                           loaders.add(res.layer(Resource.CodeEntry.class).loader(wait));
                        }

                        parent = new Resource.LibClassLoader(parent, loaders);
                     }

                     return new Resource.ResClassLoader(parent) {
                        @Override
                        public Class<?> findClass(String name) throws ClassNotFoundException {
                           Resource.Code c = CodeEntry.this.clmap.get(name);
                           if (c == null) {
                              throw new ClassNotFoundException("Could not find class " + name + " in resource (" + Resource.this + ")");
                           } else {
                              return this.defineClass(name, c.data, 0, c.data.length);
                           }
                        }
                     };
                  }
               });
            }
         }

         return this.loader;
      }

      private void load() {
         synchronized (Resource.CodeEntry.class) {
            if (this.lpe == null) {
               ClassLoader loader = this.loader(false);
               this.lpe = new TreeMap<>();

               try {
                  for (Entry<String, String> e : this.pe.entrySet()) {
                     String name = e.getKey();
                     String clnm = e.getValue();
                     Class<?> cl = loader.loadClass(clnm);
                     this.lpe.put(name, cl);
                  }
               } catch (ClassNotFoundException var9) {
                  throw new Resource.LoadException(var9, Resource.this);
               }
            }
         }
      }

      public <T> Class<? extends T> getcl(Class<T> cl, boolean fail) {
         this.load();
         Resource.PublishedCode entry = cl.getAnnotation(Resource.PublishedCode.class);
         if (entry == null) {
            throw new RuntimeException("Tried to fetch non-published res-loaded class " + cl.getName() + " from " + Resource.this.name);
         } else {
            Class<?> acl;
            synchronized (this.lpe) {
               if ((acl = this.lpe.get(entry.name())) == null) {
                  if (fail) {
                     throw new RuntimeException("Tried to fetch non-present res-loaded class " + cl.getName() + " from " + Resource.this.name);
                  }

                  return null;
               }
            }

            return acl.asSubclass(cl);
         }
      }

      public <T> Class<? extends T> getcl(Class<T> cl) {
         return this.getcl(cl, true);
      }

      public <T> T get(Class<T> cl, boolean fail) {
         this.load();
         Resource.PublishedCode entry = cl.getAnnotation(Resource.PublishedCode.class);
         if (entry == null) {
            throw new RuntimeException("Tried to fetch non-published res-loaded class " + cl.getName() + " from " + Resource.this.name);
         } else {
            Class<?> acl;
            synchronized (this.lpe) {
               if ((acl = this.lpe.get(entry.name())) == null) {
                  if (fail) {
                     throw new RuntimeException("Tried to fetch non-present res-loaded class " + cl.getName() + " from " + Resource.this.name);
                  }

                  return null;
               }
            }

            try {
               synchronized (this.ipe) {
                  Object pinst;
                  if ((pinst = this.ipe.get(acl)) != null) {
                     return cl.cast(pinst);
                  } else {
                     Object rinst;
                     if (entry.instancer() != Resource.PublishedCode.Instancer.class) {
                        rinst = entry.instancer().newInstance().make(acl);
                     } else {
                        rinst = acl.newInstance();
                     }

                     T inst;
                     try {
                        inst = cl.cast(rinst);
                     } catch (ClassCastException var11) {
                        throw new ClassCastException("Published class in " + Resource.this.name + " is not of type " + cl);
                     }

                     this.ipe.put(acl, inst);
                     return inst;
                  }
               }
            } catch (InstantiationException var13) {
               throw new RuntimeException(var13);
            } catch (IllegalAccessException var14) {
               throw new RuntimeException(var14);
            }
         }
      }

      public <T> T get(Class<T> cl) {
         return this.get(cl, true);
      }
   }

   public static class FileSource implements Resource.ResSource, Serializable {
      public static final Collection<String> wintraps = new HashSet<>(
         Arrays.asList(
            "con",
            "prn",
            "aux",
            "nul",
            "com0",
            "com1",
            "com2",
            "com3",
            "com4",
            "com5",
            "com6",
            "com7",
            "com8",
            "com9",
            "lpt0",
            "lpt1",
            "lpt2",
            "lpt3",
            "lpt4",
            "lpt5",
            "lpt6",
            "lpt7",
            "lpt8",
            "lpt9"
         )
      );
      public static final boolean windows = System.getProperty("os.name", "").startsWith("Windows");
      private static final boolean[] winsafe;
      public final Path base;

      public static boolean winsafechar(char c) {
         return c >= winsafe.length || winsafe[c];
      }

      public FileSource(Path base) {
         this.base = base;
      }

      private static String checkpart(String part, String whole) throws FileNotFoundException {
         if (windows && wintraps.contains(part)) {
            throw new FileNotFoundException(whole);
         } else {
            return part;
         }
      }

      @Override
      public InputStream get(String name) throws IOException {
         Path cur = this.base;
         String[] parts = name.split("/");

         for (int i = 0; i < parts.length - 1; i++) {
            cur = cur.resolve(checkpart(parts[i], name));
         }

         cur = cur.resolve(checkpart(parts[parts.length - 1], name) + ".res");

         try {
            return Files.newInputStream(cur);
         } catch (NoSuchFileException var5) {
            throw (FileNotFoundException)new FileNotFoundException(name).initCause(var5);
         }
      }

      @Override
      public String toString() {
         return "filesystem res source (" + this.base + ")";
      }

      static {
         boolean[] buf = new boolean[128];
         String safe = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_@";

         for (int i = 0; i < safe.length(); i++) {
            buf[safe.charAt(i)] = true;
         }

         winsafe = buf;
      }
   }

   @Resource.LayerName("font")
   public class Font extends Resource.Layer {
      public final transient java.awt.Font font;

      public Font(byte[] buf) {
         int[] off = new int[]{0};
         int ver = buf[off[0]++];
         if (ver == 1) {
            int type = buf[off[0]++];
            if (type == 0) {
               try {
                  this.font = java.awt.Font.createFont(0, new ByteArrayInputStream(buf, off[0], buf.length - off[0]));
               } catch (Exception var7) {
                  throw new RuntimeException(var7);
               }
            } else {
               throw new Resource.LoadException("Unknown font type: " + type, Resource.this);
            }
         } else {
            throw new Resource.LoadException("Unknown font layer version: " + ver, Resource.this);
         }
      }

      @Override
      public void init() {
      }
   }

   public static class HttpSource implements Resource.ResSource, Serializable {
      private final transient SslHelper ssl = new SslHelper();
      public URL baseurl;

      public HttpSource(URL baseurl) {
         try {
            this.ssl.trust(Resource.class.getResourceAsStream("ressrv.crt"));
         } catch (CertificateException var3) {
            throw new Error("Invalid built-in certificate", var3);
         } catch (IOException var4) {
            throw new Error(var4);
         }

         this.ssl.ignoreName();
         this.baseurl = baseurl;
      }

      private URL encodeurl(URL raw) throws IOException {
         try {
            return new URL(new URI(raw.getProtocol(), raw.getHost(), raw.getPath(), raw.getRef()).toASCIIString());
         } catch (URISyntaxException var3) {
            throw new IOException(var3);
         }
      }

      @Override
      public InputStream get(String name) throws IOException {
         URL resurl = this.encodeurl(new URL(this.baseurl, name + ".res"));
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
            } catch (ConnectException var6) {
               if (++tries >= 5) {
                  throw new IOException("Connection failed five times", var6);
               }
            }
         }
      }

      @Override
      public String toString() {
         return "HTTP res source (" + this.baseurl + ")";
      }
   }

   public interface IDLayer<T> {
      T layerid();
   }

   @Resource.LayerName("image")
   public class Image extends Resource.Layer implements Comparable<Resource.Image>, Resource.IDLayer<Integer> {
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
         this.nooff = (buf[4] & 2) != 0;
         this.id = Utils.int16d(buf, 5);
         this.o = Resource.cdec(buf, 7);

         try {
            this.img = ImageIO.read(new ByteArrayInputStream(buf, 11, buf.length - 11));
         } catch (IOException var4) {
            throw new Resource.LoadException(var4, Resource.this);
         }

         if (this.img == null) {
            throw new Resource.LoadException("Invalid image data in " + Resource.this.name, Resource.this);
         } else {
            this.sz = Utils.imgsz(this.img);
         }
      }

      public synchronized Tex tex() {
         if (this.tex != null) {
            return this.tex;
         } else {
            this.tex = new TexI(this.img) {
               @Override
               public String toString() {
                  return "TexI(" + Resource.this.name + ", " + Image.this.id + ")";
               }
            };
            return this.tex;
         }
      }

      private boolean detectgay() {
         for (int y = 0; y < this.sz.y; y++) {
            for (int x = 0; x < this.sz.x; x++) {
               if ((this.img.getRGB(x, y) & 16777215) == 16711808) {
                  return true;
               }
            }
         }

         return false;
      }

      public boolean gayp() {
         if (this.gay == -1) {
            this.gay = this.detectgay() ? 1 : 0;
         }

         return this.gay == 1;
      }

      public int compareTo(Resource.Image other) {
         return this.z - other.z;
      }

      public Integer layerid() {
         return this.id;
      }

      @Override
      public void init() {
      }
   }

   public static class JarSource implements Resource.ResSource, Serializable {
      @Override
      public InputStream get(String name) throws FileNotFoundException {
         InputStream s = Resource.class.getResourceAsStream("/res/" + name + ".res");
         if (s == null) {
            throw new FileNotFoundException("Could not find resource locally: " + name);
         } else {
            return s;
         }
      }

      @Override
      public String toString() {
         return "local res source";
      }
   }

   public abstract class Layer implements Serializable {
      public abstract void init();

      public Resource getres() {
         return Resource.this;
      }
   }

   public static class LayerConstructor<T extends Resource.Layer> implements Resource.LayerFactory<T> {
      public final Class<T> cl;
      private final Constructor<T> cons;

      public LayerConstructor(Class<T> cl) {
         this.cl = cl;

         try {
            this.cons = cl.getConstructor(Resource.class, byte[].class);
         } catch (NoSuchMethodException var3) {
            throw new RuntimeException("No proper constructor found for layer type " + cl.getName(), var3);
         }
      }

      @Override
      public T cons(Resource res, byte[] buf) {
         try {
            return this.cons.newInstance(res, buf);
         } catch (InstantiationException var5) {
            throw new Resource.LoadException(var5, res);
         } catch (IllegalAccessException var6) {
            throw new Resource.LoadException(var6, res);
         } catch (InvocationTargetException var7) {
            Throwable c = var7.getCause();
            if (c instanceof RuntimeException) {
               throw (RuntimeException)c;
            } else {
               throw new Resource.LoadException(var7, res);
            }
         }
      }
   }

   public interface LayerFactory<T extends Resource.Layer> {
      T cons(Resource var1, byte[] var2);
   }

   @Target({ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   @Discoverable
   public @interface LayerName {
      String value();
   }

   public static class LibClassLoader extends ClassLoader {
      private final ClassLoader[] classpath;

      public LibClassLoader(ClassLoader parent, Collection<ClassLoader> classpath) {
         super(parent);
         this.classpath = classpath.toArray(new ClassLoader[0]);
      }

      @Override
      public Class<?> findClass(String name) throws ClassNotFoundException {
         for (ClassLoader lib : this.classpath) {
            try {
               return lib.loadClass(name);
            } catch (ClassNotFoundException var7) {
            }
         }

         throw new ClassNotFoundException("Could not find " + name + " in any of " + Arrays.asList(this.classpath).toString());
      }
   }

   public static class LoadException extends RuntimeException {
      public Resource res;
      public Resource.ResSource src;
      public Resource.LoadException prev;

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

   private static class Loader implements Runnable {
      private Resource.ResSource src;
      private Resource.Loader next = null;
      private Queue<Resource> queue = new PrioQueue<>();
      private transient Thread th = null;

      public Loader(Resource.ResSource src) {
         this.src = src;
      }

      public void chain(Resource.Loader next) {
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

      @Override
      public void run() {
         try {
            while (true) {
               Resource cur;
               synchronized (this.queue) {
                  while ((cur = this.queue.poll()) == null) {
                     this.queue.wait();
                  }
               }

               synchronized (cur) {
                  this.handle(cur);
               }

               cur = null;
            }
         } catch (InterruptedException var18) {
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

            try {
               try {
                  in = this.src.get(res.name);
                  res.load(in);
                  res.error = null;
                  res.loading = false;
                  res.notifyAll();
                  return;
               } catch (IOException var14) {
                  throw new Resource.LoadException(var14, res);
               }
            } catch (RuntimeException var15) {
               Resource.LoadException error;
               if (var15 instanceof Resource.LoadException) {
                  error = (Resource.LoadException)var15;
               } else {
                  error = new Resource.LoadException(var15, res);
               }

               error.src = this.src;
               error.prev = res.error;
               res.error = error;
               if (this.next == null) {
                  res.loading = false;
                  res.notifyAll();
               } else {
                  this.next.load(res);
               }
            }
         } finally {
            try {
               if (in != null) {
                  in.close();
               }
            } catch (IOException var13) {
            }
         }
      }
   }

   public static class Loading extends haven.Loading {
      public final Resource res;

      public Loading(Resource res) {
         this.res = res;
      }

      @Override
      public String toString() {
         return "#<Resource " + this.res.name + ">";
      }

      @Override
      public boolean canwait() {
         return true;
      }

      @Override
      public void waitfor() throws InterruptedException {
         this.res.loadwaitint();
      }
   }

   @Resource.LayerName("midi")
   public class Music extends Resource.Layer {
      transient Sequence seq;

      public Music(byte[] buf) {
         try {
            this.seq = MidiSystem.getSequence(new ByteArrayInputStream(buf));
         } catch (InvalidMidiDataException var4) {
            throw new Resource.LoadException("Invalid MIDI data", Resource.this);
         } catch (IOException var5) {
            throw new Resource.LoadException(var5, Resource.this);
         }
      }

      @Override
      public void init() {
      }
   }

   @Resource.LayerName("neg")
   public class Neg extends Resource.Layer {
      public Coord cc;
      public Coord[][] ep;

      public Neg(byte[] buf) {
         this.cc = Resource.cdec(buf, 0);
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

      @Override
      public void init() {
      }
   }

   @Resource.LayerName("tileset")
   public static class OrigTileset implements Resource.LayerFactory<Resource.Tileset> {
      public Resource.Tileset cons(Resource res, byte[] buf) {
         Resource.Tileset ret = res.new Tileset();
         int[] off = new int[]{0};
         int fl = Utils.ub(buf[off[0]++]);
         int flnum = Utils.uint16d(buf, off[0]);
         off[0] += 2;
         ret.flavprob = Utils.uint16d(buf, off[0]);
         off[0] += 2;

         for (int i = 0; i < flnum; i++) {
            String fln = Utils.strd(buf, off);
            int flv = Utils.uint16d(buf, off[0]);
            off[0] += 2;
            int flw = Utils.ub(buf[off[0]++]);

            try {
               ret.flavobjs.add(Resource.load(fln, flv), flw);
            } catch (RuntimeException var12) {
               throw new Resource.LoadException("Illegal resource dependency", var12, res);
            }
         }

         return ret;
      }
   }

   @Resource.LayerName("pagina")
   public class Pagina extends Resource.Layer {
      public final String text;

      public Pagina(byte[] buf) {
         try {
            this.text = new String(buf, "UTF-8");
         } catch (UnsupportedEncodingException var4) {
            throw new Resource.LoadException(var4, Resource.this);
         }
      }

      @Override
      public void init() {
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.TYPE})
   public @interface PublishedCode {
      String name();

      Class<? extends Resource.PublishedCode.Instancer> instancer() default Resource.PublishedCode.Instancer.class;

      public interface Instancer {
         Object make(Class<?> var1) throws InstantiationException, IllegalAccessException;
      }
   }

   public class ResClassLoader extends ClassLoader {
      public ResClassLoader(ClassLoader parent) {
         super(parent);
      }

      public Resource getres() {
         return Resource.this;
      }

      @Override
      public String toString() {
         return "cl:" + Resource.this.toString();
      }
   }

   public interface ResSource {
      InputStream get(String var1) throws IOException;
   }

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
         return this.get(0);
      }
   }

   public abstract static class TeeSource implements Resource.ResSource, Serializable {
      public Resource.ResSource back;

      public TeeSource(Resource.ResSource back) {
         this.back = back;
      }

      @Override
      public InputStream get(String name) throws IOException {
         StreamTee tee = new StreamTee(this.back.get(name));
         tee.setncwe();
         tee.attach(this.fork(name));
         return tee;
      }

      public abstract OutputStream fork(String var1) throws IOException;

      @Override
      public String toString() {
         return "forking source backed by " + this.back;
      }
   }

   @Resource.LayerName("tile")
   public class Tile extends Resource.Layer {
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
         } catch (IOException var4) {
            throw new Resource.LoadException(var4, Resource.this);
         }

         if (this.img == null) {
            throw new Resource.LoadException("Invalid image data in " + Resource.this.name, Resource.this);
         }
      }

      public synchronized Tex tex() {
         if (this.tex == null) {
            this.tex = new TexI(this.img);
         }

         return this.tex;
      }

      @Override
      public void init() {
      }
   }

   @Resource.LayerName("tileset2")
   public class Tileset extends Resource.Layer {
      private String tn = "gnd";
      public Object[] ta = new Object[0];
      private transient Tiler.Factory tfac;
      public WeightList<Resource> flavobjs = new WeightList<>();
      public WeightList<Resource.Tile> ground;
      public WeightList<Resource.Tile>[] ctrans;
      public WeightList<Resource.Tile>[] btrans;
      public int flavprob;

      private Tileset() {
      }

      public Tileset(byte[] bbuf) {
         Message buf = new Message(0, bbuf);

         while (!buf.eom()) {
            int p = buf.uint8();
            switch (p) {
               case 0:
                  this.tn = buf.string();
                  this.ta = buf.list();
                  break;
               case 1:
                  int flnum = buf.uint16();
                  this.flavprob = buf.uint16();

                  for (int i = 0; i < flnum; i++) {
                     String fln = buf.string();
                     int flv = buf.uint16();
                     int flw = buf.uint8();

                     try {
                        this.flavobjs.add(Resource.load(fln, flv), flw);
                     } catch (RuntimeException var11) {
                        throw new Resource.LoadException("Illegal resource dependency", var11, Resource.this);
                     }
                  }
                  break;
               default:
                  throw new Resource.LoadException("Invalid tileset part " + p + "  in " + Resource.this.name, Resource.this);
            }
         }
      }

      public Tiler.Factory tfac() {
         synchronized (this) {
            if (this.tfac == null) {
               Resource.CodeEntry ent = Resource.this.layer(Resource.CodeEntry.class);
               if (ent != null) {
                  this.tfac = ent.get(Tiler.Factory.class);
               } else if ((this.tfac = Tiler.byname(this.tn)) == null) {
                  throw new RuntimeException("Invalid tiler name in " + Resource.this.name + ": " + this.tn);
               }
            }

            return this.tfac;
         }
      }

      private void packtiles(Collection<Resource.Tile> tiles, Coord tsz) {
         if (tiles.size() >= 1) {
            int min = -1;
            int minw = -1;
            int minh = -1;
            int mine = -1;
            final int nt = tiles.size();

            for (int i = 1; i <= nt; i++) {
               int w = Tex.nextp2(tsz.x * i);
               int h;
               if (nt % i == 0) {
                  h = nt / i;
               } else {
                  h = nt / i + 1;
               }

               h = Tex.nextp2(tsz.y * h);
               int a = w * h;
               int e = w < h ? h : w;
               if (min == -1 || a < min || a == min && e < mine) {
                  min = a;
                  minw = w;
                  minh = h;
                  mine = e;
               }
            }

            final Resource.Tile[] order = new Resource.Tile[nt];
            final Coord[] place = new Coord[nt];
            Tex packbuf = new TexL(new Coord(minw, minh)) {
               {
                  this.mipmap(Mipmapper.avg);
                  this.minfilter(9986);
                  this.centroid = true;
               }

               @Override
               protected BufferedImage fill() {
                  BufferedImage buf = TexI.mkbuf(this.dim);
                  Graphics g = buf.createGraphics();

                  for (int i = 0; i < nt; i++) {
                     g.drawImage(order[i].img, place[i].x, place[i].y, null);
                  }

                  g.dispose();
                  return buf;
               }

               @Override
               public String toString() {
                  return "TileTex(" + Resource.this.name + ")";
               }
            };
            int x = 0;
            int y = 0;
            int n = 0;

            for (Resource.Tile t : tiles) {
               if (y >= minh) {
                  throw new Resource.LoadException("Could not pack tiles into calculated minimum texture", Resource.this);
               }

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
      }

      @Override
      public void init() {
         WeightList<Resource.Tile> ground = new WeightList<>();
         WeightList<Resource.Tile>[] ctrans = new WeightList[15];
         WeightList<Resource.Tile>[] btrans = new WeightList[15];

         for (int i = 0; i < 15; i++) {
            ctrans[i] = new WeightList<>();
            btrans[i] = new WeightList<>();
         }

         int cn = 0;
         int bn = 0;
         Collection<Resource.Tile> tiles = new LinkedList<>();
         Coord tsz = null;

         for (Resource.Tile t : Resource.this.layers(Resource.Tile.class, false)) {
            if (t.t == 'g') {
               ground.add(t, t.w);
            } else if (t.t == 'b') {
               btrans[t.id - 1].add(t, t.w);
               bn++;
            } else if (t.t == 'c') {
               ctrans[t.id - 1].add(t, t.w);
               cn++;
            }

            tiles.add(t);
            if (tsz == null) {
               tsz = Utils.imgsz(t.img);
            } else if (!Utils.imgsz(t.img).equals(tsz)) {
               throw new Resource.LoadException("Different tile sizes within set", Resource.this);
            }
         }

         if (ground.size() > 0) {
            this.ground = ground;
         }

         if (cn > 0) {
            this.ctrans = ctrans;
         }

         if (bn > 0) {
            this.btrans = btrans;
         }

         this.packtiles(tiles, tsz);
      }
   }

   @Resource.LayerName("tooltip")
   public class Tooltip extends Resource.Layer {
      public final String t;

      public Tooltip(byte[] buf) {
         try {
            this.t = new String(buf, "UTF-8");
         } catch (UnsupportedEncodingException var4) {
            throw new Resource.LoadException(var4, Resource.this);
         }
      }

      @Override
      public void init() {
      }
   }
}
