package haven;

import haven.error.ErrorGui;
import haven.error.ErrorHandler;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainFrame extends Frame implements Runnable, Console.Directory {
   HavenPanel p;
   private final ThreadGroup g;
   public final Thread mt;
   DisplayMode fsmode = null;
   DisplayMode prefs = null;
   private Map<String, Console.Command> cmdmap = new TreeMap<>();

   DisplayMode findmode(int w, int h) {
      GraphicsDevice dev = this.getGraphicsConfiguration().getDevice();
      if (!dev.isFullScreenSupported()) {
         return null;
      } else {
         DisplayMode b = null;

         for (DisplayMode m : dev.getDisplayModes()) {
            int d = m.getBitDepth();
            if (m.getWidth() == w
               && m.getHeight() == h
               && (d == 24 || d == 32 || d == -1)
               && (b == null || d > b.getBitDepth() || d == b.getBitDepth() && m.getRefreshRate() > b.getRefreshRate())) {
               b = m;
            }
         }

         return b;
      }
   }

   public void setfs() {
      GraphicsDevice dev = this.getGraphicsConfiguration().getDevice();
      if (this.prefs == null) {
         this.prefs = dev.getDisplayMode();

         try {
            this.setVisible(false);
            this.dispose();
            this.setUndecorated(true);
            this.setVisible(true);
            dev.setFullScreenWindow(this);
            dev.setDisplayMode(this.fsmode);
            this.pack();
         } catch (Exception var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   public void setwnd() {
      GraphicsDevice dev = this.getGraphicsConfiguration().getDevice();
      if (this.prefs != null) {
         try {
            dev.setDisplayMode(this.prefs);
            dev.setFullScreenWindow(null);
            this.setVisible(false);
            this.dispose();
            this.setUndecorated(false);
            this.setVisible(true);
         } catch (Exception var3) {
            throw new RuntimeException(var3);
         }

         this.prefs = null;
      }
   }

   public boolean hasfs() {
      return this.prefs != null;
   }

   @Override
   public Map<String, Console.Command> findcmds() {
      return this.cmdmap;
   }

   private void seticon() {
      Image icon;
      try {
         InputStream data = MainFrame.class.getResourceAsStream("icon.gif");
         icon = ImageIO.read(data);
         data.close();
      } catch (IOException var3) {
         throw new Error(var3);
      }

      this.setIconImage(icon);
   }

   public MainFrame(Coord isz) {
      super("Salem");
      this.cmdmap.put("sz", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            if (args.length == 3) {
               int w = Integer.parseInt(args[1]);
               int h = Integer.parseInt(args[2]);
               MainFrame.this.p.setSize(w, h);
               MainFrame.this.pack();
               Utils.setprefc("wndsz", new Coord(w, h));
            } else if (args.length == 2) {
               if (args[1].equals("dyn")) {
                  MainFrame.this.setResizable(true);
                  Utils.setprefb("wndlock", false);
               } else if (args[1].equals("lock")) {
                  MainFrame.this.setResizable(false);
                  Utils.setprefb("wndlock", true);
               }
            }
         }
      });
      this.cmdmap.put("fsmode", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) throws Exception {
            if (args.length == 3) {
               DisplayMode mode = MainFrame.this.findmode(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
               if (mode == null) {
                  throw new Exception("No such mode is available");
               }

               MainFrame.this.fsmode = mode;
               Utils.setprefc("fsmode", new Coord(mode.getWidth(), mode.getHeight()));
            }
         }
      });
      this.cmdmap.put("fs", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            if (args.length >= 2) {
               Runnable r;
               if (Utils.atoi(args[1]) != 0) {
                  r = new Runnable() {
                     @Override
                     public void run() {
                        MainFrame.this.setfs();
                     }
                  };
               } else {
                  r = new Runnable() {
                     @Override
                     public void run() {
                        MainFrame.this.setwnd();
                     }
                  };
               }

               MainFrame.this.getToolkit().getSystemEventQueue();
               EventQueue.invokeLater(r);
            }
         }
      });
      Coord sz;
      if (isz == null) {
         sz = Utils.getprefc("wndsz", new Coord(800, 600));
         if (sz.x < 640) {
            sz.x = 640;
         }

         if (sz.y < 480) {
            sz.y = 480;
         }
      } else {
         sz = isz;
      }

      this.g = new ThreadGroup(HackThread.tg(), "Haven client");
      this.mt = new HackThread(this.g, this, "Haven main thread");
      this.p = new HavenPanel(sz.x, sz.y);
      if (this.fsmode == null) {
         Coord pfm = Utils.getprefc("fsmode", null);
         if (pfm != null) {
            this.fsmode = this.findmode(pfm.x, pfm.y);
         }
      }

      if (this.fsmode == null) {
         DisplayMode cm = this.getGraphicsConfiguration().getDevice().getDisplayMode();
         this.fsmode = this.findmode(cm.getWidth(), cm.getHeight());
      }

      if (this.fsmode == null) {
         this.fsmode = this.findmode(800, 600);
      }

      this.add(this.p);
      this.pack();
      this.setResizable(!Utils.getprefb("wndlock", false));
      this.p.requestFocus();
      this.seticon();
      this.setVisible(true);
      this.p.init();
      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            MainFrame.this.g.interrupt();
         }
      });
      if (isz == null && Utils.getprefb("wndmax", false)) {
         this.setExtendedState(this.getExtendedState() | 6);
      }
   }

   private void savewndstate() {
      if (this.prefs == null) {
         if (this.getExtendedState() == 0) {
            Dimension dim = this.p.getSize();
            Utils.setprefc("wndsz", new Coord(dim.width, dim.height));
         }

         Utils.setprefb("wndmax", (this.getExtendedState() & 6) != 0);
      }
   }

   @Override
   public void run() {
      if (Thread.currentThread() != this.mt) {
         throw new RuntimeException("MainFrame is being run from an invalid context");
      } else {
         Thread ui = new HackThread(this.p, "Haven UI thread");
         ui.start();

         try {
            Session sess = null;

            while (true) {
               UI.Runner fun;
               if (sess == null) {
                  Bootstrap bill = new Bootstrap(Config.defserv, Config.mainport);
                  if (Config.authuser != null && Config.authck != null) {
                     bill.setinitcookie(Config.authuser, Config.authck);
                     Config.authck = null;
                  }

                  fun = bill;
               } else {
                  fun = new RemoteUI(sess);
               }

               sess = fun.run(this.p.newui(sess));
            }
         } catch (InterruptedException var8) {
            this.savewndstate();
         } finally {
            ui.interrupt();
            this.dispose();
         }
      }
   }

   public static void setupres() {
      if (ResCache.global != null) {
         Resource.addcache(ResCache.global);
      }

      if (Config.resurl != null) {
         Resource.addurl(Config.resurl);
      }

      if (ResCache.global != null) {
         try {
            Resource.loadlist(ResCache.global.fetch("tmp/allused"), -10);
         } catch (IOException var2) {
         }
      }

      if (!Config.nopreload) {
         try {
            InputStream pls = Resource.class.getResourceAsStream("res-preload");
            if (pls != null) {
               Resource.loadlist(pls, -5);
            }

            pls = Resource.class.getResourceAsStream("res-bgload");
            if (pls != null) {
               Resource.loadlist(pls, -10);
            }
         } catch (IOException var1) {
            throw new Error(var1);
         }
      }
   }

   private static void netxsurgery() throws Exception {
      Class<?> nxc;
      try {
         nxc = Class.forName("net.sourceforge.jnlp.runtime.JNLPClassLoader");
      } catch (ClassNotFoundException var14) {
         try {
            nxc = Class.forName("netx.jnlp.runtime.JNLPClassLoader");
         } catch (ClassNotFoundException var13) {
            throw new Exception("No known NetX on classpath");
         }
      }

      ClassLoader cl = MainFrame.class.getClassLoader();
      if (!nxc.isInstance(cl)) {
         throw new Exception("Not running from a NetX classloader");
      } else {
         Field cblf;
         Field lf;
         try {
            cblf = nxc.getDeclaredField("codeBaseLoader");
            lf = nxc.getDeclaredField("loaders");
         } catch (NoSuchFieldException var12) {
            throw new Exception("JNLPClassLoader does not conform to its known structure");
         }

         cblf.setAccessible(true);
         lf.setAccessible(true);
         Set<Object> loaders = new HashSet<>();
         Stack<Object> open = new Stack<>();
         open.push(cl);

         while (!open.empty()) {
            Object cur = open.pop();
            if (!loaders.contains(cur)) {
               loaders.add(cur);

               Object curl;
               try {
                  curl = lf.get(cur);
               } catch (IllegalAccessException var11) {
                  throw new Exception("Reflection accessibility not available even though set");
               }

               for (int i = 0; i < Array.getLength(curl); i++) {
                  Object other = Array.get(curl, i);
                  if (nxc.isInstance(other)) {
                     open.push(other);
                  }
               }
            }
         }

         for (Object cur : loaders) {
            try {
               cblf.set(cur, null);
            } catch (IllegalAccessException var10) {
               throw new Exception("Reflection accessibility not available even though set");
            }
         }
      }
   }

   private static void javabughack() throws InterruptedException {
      try {
         SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
               PrintStream bitbucket = new PrintStream(new ByteArrayOutputStream());
               bitbucket.print(LoginScreen.textf);
               bitbucket.print(LoginScreen.textfs);
            }
         });
      } catch (InvocationTargetException var2) {
         throw new Error(var2);
      }

      IIORegistry.getDefaultInstance();

      try {
         netxsurgery();
      } catch (Exception var1) {
      }
   }

   private static void main2(String[] args) {
      Config.cmdline(args);

      try {
         javabughack();
      } catch (InterruptedException var14) {
         return;
      }

      setupres();
      MainFrame f = new MainFrame(null);
      if (Utils.getprefb("fullscreen", false)) {
         f.setfs();
      }

      f.mt.start();

      try {
         f.mt.join();
      } catch (InterruptedException var13) {
         f.g.interrupt();
         return;
      }

      dumplist(Resource.loadwaited, Config.loadwaited);
      dumplist(Resource.cached(), Config.allused);
      if (ResCache.global != null) {
         try {
            Collection<Resource> used = new LinkedList<>();

            for (Resource res : Resource.cached()) {
               if (res.prio >= 0) {
                  try {
                     res.checkerr();
                  } catch (Exception var15) {
                     continue;
                  }

                  used.add(res);
               }
            }

            Writer w = new OutputStreamWriter(ResCache.global.store("tmp/allused"), "UTF-8");

            try {
               Resource.dumplist(used, w);
            } finally {
               w.close();
            }
         } catch (IOException var16) {
         }
      }

      System.exit(0);
   }

   public static void main(final String[] args) {
      ThreadGroup g = new ThreadGroup("Haven main group");
      String ed;
      if (!(ed = Utils.getprop("haven.errorurl", "")).equals("")) {
         try {
            final ErrorHandler hg = new ErrorHandler(new URL(ed));
            hg.sethandler(new ErrorGui(null) {
               @Override
               public void errorsent() {
                  hg.interrupt();
               }
            });
            g = hg;
         } catch (MalformedURLException var4) {
         }
      }

      Thread main = new HackThread(g, new Runnable() {
         @Override
         public void run() {
            MainFrame.main2(args);
         }
      }, "Haven main thread");
      main.start();
   }

   private static void dumplist(Collection<Resource> list, String fn) {
      try {
         if (fn != null) {
            Writer w = new OutputStreamWriter(new FileOutputStream(fn), "UTF-8");

            try {
               Resource.dumplist(list, w);
            } finally {
               w.close();
            }
         }
      } catch (IOException var7) {
         throw new RuntimeException(var7);
      }
   }

   static {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var1) {
      }

      if ((WebBrowser.self = JnlpBrowser.create()) == null) {
         WebBrowser.self = DesktopBrowser.create();
      }
   }
}
