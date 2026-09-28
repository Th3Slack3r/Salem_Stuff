package haven;

import haven.error.ErrorGui;
import haven.error.ErrorHandler;
import haven.error.ErrorLogFormatter;
import haven.error.ErrorStatus;
import haven.error.LoggingOutputStream;
import java.awt.Component;
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
import java.io.OutputStream;
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
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainFrame extends Frame implements Runnable, Console.Directory {
  public static final String TITLE = String.format("Salem (modded by Taipion (base on Latikais (based on Enders)) v%s)", new Object[] { Config.version });
  
  public static MainFrame instance;
  
  HavenPanel p;
  
  private final ThreadGroup g;
  
  public final Thread mt;
  
  DisplayMode fsmode;
  
  DisplayMode prefs;
  
  public static String cName = "";
  
  private final Map<String, Console.Command> cmdmap;
  
  static {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception exception) {}
    if ((WebBrowser.self = JnlpBrowser.create()) == null)
      WebBrowser.self = DesktopBrowser.create(); 
  }
  
  DisplayMode findmode(int w, int h) {
    GraphicsDevice dev = getGraphicsConfiguration().getDevice();
    if (!dev.isFullScreenSupported())
      return null; 
    DisplayMode b = null;
    for (DisplayMode m : dev.getDisplayModes()) {
      int d = m.getBitDepth();
      if (m.getWidth() == w && m.getHeight() == h && (d == 24 || d == 32 || d == -1) && (b == null || d > b.getBitDepth() || (d == b.getBitDepth() && m.getRefreshRate() > b.getRefreshRate())))
        b = m; 
    } 
    return b;
  }
  
  public void setfs() {
    GraphicsDevice dev = getGraphicsConfiguration().getDevice();
    if (this.prefs != null)
      return; 
    this.prefs = dev.getDisplayMode();
    try {
      setVisible(false);
      dispose();
      setUndecorated(true);
      setVisible(true);
      dev.setFullScreenWindow(this);
      System.out.println("Fullscreen support: " + dev.isFullScreenSupported());
      System.out.println("Display mode change support: " + dev.isDisplayChangeSupported());
      DisplayMode[] t = dev.getDisplayModes();
      dev.setDisplayMode(this.fsmode);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public void setwnd() {
    GraphicsDevice dev = getGraphicsConfiguration().getDevice();
    if (this.prefs == null)
      return; 
    try {
      dev.setDisplayMode(this.prefs);
      dev.setFullScreenWindow(null);
      setVisible(false);
      dispose();
      setUndecorated(false);
      setVisible(true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
    this.prefs = null;
  }
  
  public boolean hasfs() {
    return (this.prefs != null);
  }
  
  public Map<String, Console.Command> findcmds() {
    return this.cmdmap;
  }
  
  private void seticon() {
    Image icon;
    try {
      InputStream data = MainFrame.class.getResourceAsStream("icon.gif");
      icon = ImageIO.read(data);
      data.close();
    } catch (IOException e) {
      throw new Error(e);
    } 
    setIconImage(icon);
  }
  
  public MainFrame(Coord isz) {
    super(TITLE);
    Coord sz;
    this.fsmode = null;
    this.prefs = null;
    this.cmdmap = new TreeMap<>();
    this.cmdmap.put("sz", new Console.Command() {
          public void run(Console cons, String[] args) {
            if (args.length == 3) {
              int w = Integer.parseInt(args[1]), h = Integer.parseInt(args[2]);
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
          public void run(Console cons, String[] args) throws Exception {
            if (args.length == 3) {
              DisplayMode mode = MainFrame.this.findmode(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
              if (mode == null)
                throw new Exception("No such mode is available"); 
              MainFrame.this.fsmode = mode;
              Utils.setprefc("fsmode", new Coord(mode.getWidth(), mode.getHeight()));
            } 
          }
        });
    this.cmdmap.put("fs", new Console.Command() {
          public void run(Console cons, String[] args) {
            if (args.length >= 2) {
              Runnable r;
              if (Utils.atoi(args[1]) != 0) {
                r = new Runnable() {
                    public void run() {
                      MainFrame.this.setfs();
                    }
                  };
              } else {
                r = new Runnable() {
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
    instance = this;
    if (isz == null) {
      sz = Utils.getprefc("wndsz", new Coord(800, 600));
      if (sz.x < 640)
        sz.x = 640; 
      if (sz.y < 480)
        sz.y = 480; 
    } else {
      sz = isz;
    } 
    this.g = new ThreadGroup(HackThread.tg(), "Haven client");
    this.mt = new HackThread(this.g, this, "Haven main thread");
    this.p = new HavenPanel(sz.x, sz.y);
    if (this.fsmode == null) {
      Coord pfm = Utils.getprefc("fsmode", null);
      if (pfm != null)
        this.fsmode = findmode(pfm.x, pfm.y); 
    } 
    if (this.fsmode == null) {
      DisplayMode cm = getGraphicsConfiguration().getDevice().getDisplayMode();
      this.fsmode = findmode(cm.getWidth(), cm.getHeight());
    } 
    if (this.fsmode == null)
      this.fsmode = findmode(800, 600); 
    add((Component)this.p);
    pack();
    setResizable(!Utils.getprefb("wndlock", false));
    this.p.requestFocus();
    seticon();
    setVisible(true);
    this.p.init();
    addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            MainFrame.this.g.interrupt();
          }
        });
    if (isz == null && Utils.getprefb("wndmax", false))
      setExtendedState(getExtendedState() | 0x6); 
  }
  
  public void setTitle(String charname) {
    String str = TITLE;
    if (charname != null) {
      str = charname + " - " + str;
      cName = charname;
    } 
    super.setTitle(str);
  }
  
  private void savewndstate() {
    if (this.prefs == null) {
      if (getExtendedState() == 0) {
        Dimension dim = this.p.getSize();
        Utils.setprefc("wndsz", new Coord(dim.width, dim.height));
      } 
      Utils.setprefb("wndmax", ((getExtendedState() & 0x6) != 0));
    } 
  }
  
  public void run() {
    if (Thread.currentThread() != this.mt)
      throw new RuntimeException("MainFrame is being run from an invalid context"); 
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
    } catch (InterruptedException interruptedException) {
      savewndstate();
    } finally {
      ui.interrupt();
      dispose();
    } 
  }
  
  public static void setupres() {
    if (ResCache.global != null)
      Resource.addcache(ResCache.global); 
    if (Config.resurl != null)
      Resource.addurl(Config.resurl); 
    if (ResCache.global != null)
      try {
        Resource.loadlist(ResCache.global.fetch("tmp/allused"), -10);
      } catch (IOException iOException) {} 
    if (!Config.nopreload)
      try {
        InputStream pls = Resource.class.getResourceAsStream("res-preload");
        if (pls != null)
          Resource.loadlist(pls, -5); 
        pls = Resource.class.getResourceAsStream("res-bgload");
        if (pls != null)
          Resource.loadlist(pls, -10); 
      } catch (IOException e) {
        throw new Error(e);
      }  
  }
  
  private static void netxsurgery() throws Exception {
    Class<?> nxc;
    Field cblf, lf;
    try {
      nxc = Class.forName("net.sourceforge.jnlp.runtime.JNLPClassLoader");
    } catch (ClassNotFoundException e1) {
      try {
        nxc = Class.forName("netx.jnlp.runtime.JNLPClassLoader");
      } catch (ClassNotFoundException e2) {
        throw new Exception("No known NetX on classpath");
      } 
    } 
    ClassLoader cl = MainFrame.class.getClassLoader();
    if (!nxc.isInstance(cl))
      throw new Exception("Not running from a NetX classloader"); 
    try {
      cblf = nxc.getDeclaredField("codeBaseLoader");
      lf = nxc.getDeclaredField("loaders");
    } catch (NoSuchFieldException e) {
      throw new Exception("JNLPClassLoader does not conform to its known structure");
    } 
    cblf.setAccessible(true);
    lf.setAccessible(true);
    Set<Object> loaders = new HashSet();
    Stack<Object> open = new Stack();
    open.push(cl);
    while (!open.empty()) {
      Object curl, cur = open.pop();
      if (loaders.contains(cur))
        continue; 
      loaders.add(cur);
      try {
        curl = lf.get(cur);
      } catch (IllegalAccessException e) {
        throw new Exception("Reflection accessibility not available even though set");
      } 
      for (int i = 0; i < Array.getLength(curl); i++) {
        Object other = Array.get(curl, i);
        if (nxc.isInstance(other))
          open.push(other); 
      } 
    } 
    for (Object cur : loaders) {
      try {
        cblf.set(cur, null);
      } catch (IllegalAccessException e) {
        throw new Exception("Reflection accessibility not available even though set");
      } 
    } 
  }
  
  private static void javabughack() throws InterruptedException {
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              PrintStream bitbucket = new PrintStream(new ByteArrayOutputStream());
              bitbucket.print(LoginScreen.textf);
              bitbucket.print(LoginScreen.textfs);
            }
          });
    } catch (InvocationTargetException e) {
      throw new Error(e);
    } 
    IIORegistry.getDefaultInstance();
    try {
      netxsurgery();
    } catch (Exception exception) {}
  }
  
  private static void main2(String[] args) {
    Config.cmdline(args);
    try {
      javabughack();
    } catch (InterruptedException e) {
      return;
    } 
    setupres();
    MainFrame f = new MainFrame(null);
    if (Utils.getprefb("fullscreen", false))
      f.setfs(); 
    f.mt.start();
    try {
      f.mt.join();
    } catch (InterruptedException e) {
      f.g.interrupt();
      return;
    } 
    dumplist(Resource.loadwaited, Config.loadwaited);
    dumplist(Resource.cached(), Config.allused);
    if (ResCache.global != null)
      try {
        Collection<Resource> used = new LinkedList<>();
        for (Resource res : Resource.cached()) {
          if (res.prio >= 0) {
            try {
              res.checkerr();
            } catch (Exception e) {
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
      } catch (IOException iOException) {} 
    System.exit(0);
  }
  
  public static void main(final String[] args) {
    ErrorHandler errorHandler;
    System.getProperties().setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    ThreadGroup g = new ThreadGroup("Haven main group");
    String ed;
    if (!(ed = Utils.getprop("haven.errorurl", "")).equals("")) {
      try {
        final ErrorHandler hg = new ErrorHandler(new URL(ed));
        hg.sethandler((ErrorStatus)new ErrorGui(null) {
              public void errorsent() {
                hg.interrupt();
              }
            });
        errorHandler = hg;
      } catch (MalformedURLException malformedURLException) {}
    } else {
      final ErrorHandler hg = new ErrorHandler();
      hg.sethandler((ErrorStatus)new ErrorGui(null) {
            public void errorsent() {
              hg.interrupt();
            }
          });
      errorHandler = hg;
    } 
    initErrorLogs();
    Thread main = new HackThread((ThreadGroup)errorHandler, new Runnable() {
          public void run() {
            MainFrame.main2(args);
          }
        },  "Haven main thread");
    main.start();
  }
  
  private static void initErrorLogs() {
    LogManager logManager = LogManager.getLogManager();
    logManager.reset();
    try {
      Handler fileHandler = new FileHandler("%h/Salem/error_%g.log", 10000, 3, true);
      fileHandler.setFormatter((Formatter)new ErrorLogFormatter());
      Logger.getLogger("").addHandler(fileHandler);
      LoggingOutputStream loggingOutputStream = new LoggingOutputStream(Logger.getLogger("stderr"), Level.SEVERE);
      System.setErr(new PrintStream((OutputStream)loggingOutputStream, true));
    } catch (SecurityException e) {
      e.printStackTrace(System.out);
    } catch (IOException e) {
      e.printStackTrace(System.out);
    } 
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
}
