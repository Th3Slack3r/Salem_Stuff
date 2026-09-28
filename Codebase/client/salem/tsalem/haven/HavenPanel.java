package haven;

import haven.error.ErrorHandler;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

public class HavenPanel extends GLCanvas implements Runnable, Console.Directory {
  UI ui;
  
  boolean inited = false;
  
  boolean rdr = false;
  
  int w;
  
  int h;
  
  long fd = 20L;
  
  public static long fps = 0L;
  
  double idle = 0.0D;
  
  Queue<InputEvent> events = new LinkedList<>();
  
  private String cursmode = "tex";
  
  private Resource lastcursor = null;
  
  public Coord mousepos = new Coord(0, 0);
  
  public Profile prof = new Profile(300);
  
  private Profile.Frame curf = null;
  
  public static final GLState.Slot<GLState> global = new GLState.Slot<>(GLState.Slot.Type.SYS, GLState.class, new GLState.Slot[0]);
  
  public static final GLState.Slot<GLState> proj2d = new GLState.Slot<>(GLState.Slot.Type.SYS, GLState.class, new GLState.Slot[] { global });
  
  private GLState gstate;
  
  private GLState rtstate;
  
  private GLState ostate;
  
  private GLState.Applier state = null;
  
  protected static GLConfig glconf = null;
  
  public static int fpsDisplayOffsetX = Config.fps_display_offset_x;
  
  public static int fpsDisplayOffsetY = Config.fps_display_offset_y;
  
  public static boolean fpsShow = Config.fps_display_show;
  
  public static boolean fpsOnlyFPS = Config.fps_display_only_fps;
  
  public static boolean hideUI = false;
  
  private final Map<String, Console.Command> cmdmap;
  
  private static GLCapabilities stdcaps() {
    GLProfile prof = GLProfile.getDefault();
    GLCapabilities cap = new GLCapabilities(prof);
    cap.setDoubleBuffered(true);
    cap.setAlphaBits(8);
    cap.setRedBits(8);
    cap.setGreenBits(8);
    cap.setBlueBits(8);
    cap.setSampleBuffers(true);
    cap.setNumSamples(4);
    cap.setDepthBits(24);
    return cap;
  }
  
  public HavenPanel(int w, int h, GLCapabilitiesChooser cc) {
    super((GLCapabilitiesImmutable)stdcaps(), cc, null, null);
    this.cmdmap = new TreeMap<>();
    this.cmdmap.put("hz", new Console.Command() {
          public void run(Console cons, String[] args) {
            HavenPanel.this.fd = (1000 / Integer.parseInt(args[1]));
          }
        });
    setSize(this.w = w, this.h = h);
    newui((Session)null);
    initgl();
    if (Toolkit.getDefaultToolkit().getMaximumCursorColors() >= 256)
      this.cursmode = "awt"; 
    setCursor(Toolkit.getDefaultToolkit().createCustomCursor(TexI.mkbuf(new Coord(1, 1)), new Point(), ""));
  }
  
  public HavenPanel(int w, int h) {
    this(w, h, (GLCapabilitiesChooser)null);
  }
  
  private void initgl() {
    Thread caller = Thread.currentThread();
    final ErrorHandler h = ErrorHandler.find();
    addGLEventListener(new GLEventListener() {
          public void display(GLAutoDrawable d) {
            GL2 gl = d.getGL().getGL2();
            if (HavenPanel.this.inited && HavenPanel.this.rdr)
              HavenPanel.this.redraw(gl); 
            GLObject.disposeall(gl);
          }
          
          public void init(GLAutoDrawable d) {
            GL gl = d.getGL();
            HavenPanel.glconf = GLConfig.fromgl(gl, d.getContext(), HavenPanel.this.getChosenGLCapabilities());
            HavenPanel.glconf.pref = GLSettings.load(HavenPanel.glconf, true);
            HavenPanel.this.ui.cons.add(HavenPanel.glconf);
            if (h != null) {
              h.lsetprop("gl.vendor", gl.glGetString(7936));
              h.lsetprop("gl.version", gl.glGetString(7938));
              h.lsetprop("gl.renderer", gl.glGetString(7937));
              h.lsetprop("gl.exts", Arrays.asList(gl.glGetString(7939).split(" ")));
              h.lsetprop("gl.caps", d.getChosenGLCapabilities().toString());
              h.lsetprop("gl.conf", HavenPanel.glconf);
            } 
            Config.setglpref(HavenPanel.glconf.pref);
            HavenPanel.this.gstate = new GLState() {
                public void apply(GOut g) {
                  GL2 gl = g.gl;
                  gl.glColor3f(1.0F, 1.0F, 1.0F);
                  gl.glPointSize(4.0F);
                  gl.setSwapInterval(1);
                  gl.glEnable(3042);
                  gl.glBlendFunc(770, 771);
                  if (g.gc.glmajver >= 2)
                    gl.glBlendEquationSeparate(32774, 32776); 
                  if (g.gc.havefsaa())
                    g.gl.glDisable(32925); 
                  GOut.checkerr((GL)gl);
                }
                
                public void unapply(GOut g) {}
                
                public void prep(GLState.Buffer buf) {
                  buf.put(HavenPanel.global, this);
                }
              };
          }
          
          public void reshape(GLAutoDrawable d, int x, int y, final int w, final int h) {
            HavenPanel.this.ostate = HavenPanel.OrthoState.fixed(new Coord(w, h));
            HavenPanel.this.rtstate = new GLState() {
                public void apply(GOut g) {
                  GL2 gl = g.gl;
                  g.st.matmode(5889);
                  gl.glLoadIdentity();
                  gl.glOrtho(0.0D, w, 0.0D, h, -1.0D, 1.0D);
                }
                
                public void unapply(GOut g) {}
                
                public void prep(GLState.Buffer buf) {
                  buf.put(HavenPanel.proj2d, this);
                }
              };
            HavenPanel.this.w = w;
            HavenPanel.this.h = h;
          }
          
          public void displayChanged(GLAutoDrawable d, boolean cp1, boolean cp2) {}
          
          public void dispose(GLAutoDrawable d) {}
        });
  }
  
  public static abstract class OrthoState extends GLState {
    protected abstract Coord sz();
    
    public void apply(GOut g) {
      GL2 gl = g.gl;
      Coord sz = sz();
      g.st.matmode(5889);
      gl.glLoadIdentity();
      gl.glOrtho(0.0D, sz.x, sz.y, 0.0D, -1.0D, 1.0D);
    }
    
    public void unapply(GOut g) {}
    
    public void prep(GLState.Buffer buf) {
      buf.put(HavenPanel.proj2d, this);
    }
    
    public static OrthoState fixed(final Coord sz) {
      return new OrthoState() {
          protected Coord sz() {
            return sz;
          }
        };
    }
  }
  
  public void init() {
    setFocusTraversalKeysEnabled(false);
    newui((Session)null);
    addKeyListener(new KeyAdapter() {
          public void keyTyped(KeyEvent e) {
            synchronized (HavenPanel.this.events) {
              HavenPanel.this.events.add(e);
              HavenPanel.this.events.notifyAll();
            } 
          }
          
          public void keyPressed(KeyEvent e) {
            synchronized (HavenPanel.this.events) {
              HavenPanel.this.events.add(e);
              HavenPanel.this.events.notifyAll();
            } 
          }
          
          public void keyReleased(KeyEvent e) {
            synchronized (HavenPanel.this.events) {
              HavenPanel.this.events.add(e);
              HavenPanel.this.events.notifyAll();
            } 
          }
        });
    addMouseListener(new MouseAdapter() {
          public void mousePressed(MouseEvent e) {
            synchronized (HavenPanel.this.events) {
              HavenPanel.this.events.add(e);
              HavenPanel.this.events.notifyAll();
            } 
          }
          
          public void mouseReleased(MouseEvent e) {
            synchronized (HavenPanel.this.events) {
              HavenPanel.this.events.add(e);
              HavenPanel.this.events.notifyAll();
            } 
          }
        });
    addMouseMotionListener(new MouseMotionListener() {
          public void mouseDragged(MouseEvent e) {
            synchronized (HavenPanel.this.events) {
              HavenPanel.this.events.add(e);
            } 
          }
          
          public void mouseMoved(MouseEvent e) {
            synchronized (HavenPanel.this.events) {
              HavenPanel.this.events.add(e);
            } 
          }
        });
    addMouseWheelListener(new MouseWheelListener() {
          public void mouseWheelMoved(MouseWheelEvent e) {
            synchronized (HavenPanel.this.events) {
              HavenPanel.this.events.add(e);
              HavenPanel.this.events.notifyAll();
            } 
          }
        });
    this.inited = true;
  }
  
  UI newui(Session sess) {
    if (this.ui != null)
      this.ui.destroy(); 
    this.ui = new UI(new Coord(this.w, this.h), sess);
    this.ui.root.gprof = this.prof;
    if (getParent() instanceof Console.Directory)
      this.ui.cons.add((Console.Directory)getParent()); 
    this.ui.cons.add(this);
    if (glconf != null)
      this.ui.cons.add(glconf); 
    return this.ui;
  }
  
  private static Cursor makeawtcurs(BufferedImage img, Coord hs) {
    Dimension cd = Toolkit.getDefaultToolkit().getBestCursorSize(img.getWidth(), img.getHeight());
    BufferedImage buf = TexI.mkbuf(new Coord((int)cd.getWidth(), (int)cd.getHeight()));
    Graphics g = buf.getGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
    return Toolkit.getDefaultToolkit().createCustomCursor(buf, new Point(hs.x, hs.y), "");
  }
  
  void redraw(GL2 gl) {
    Object tooltip;
    if (this.state == null || this.state.gl != gl)
      this.state = new GLState.Applier(gl, glconf); 
    GLState.Buffer ibuf = new GLState.Buffer(glconf);
    this.gstate.prep(ibuf);
    this.ostate.prep(ibuf);
    GOut g = new GOut(gl, getContext(), glconf, this.state, ibuf, new Coord(this.w, this.h));
    UI ui = this.ui;
    this.state.set(ibuf);
    g.state(this.rtstate);
    TexRT.renderall(g);
    if (this.curf != null)
      this.curf.tick("texrt"); 
    g.state(this.ostate);
    g.apply();
    gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
    gl.glClear(16384);
    if (this.curf != null)
      this.curf.tick("cls"); 
    synchronized (ui) {
      ui.draw(g);
    } 
    if (this.curf != null)
      this.curf.tick("draw"); 
    if (fpsShow) {
      int yOff = fpsDisplayOffsetY * this.h / 100;
      int xOff = fpsDisplayOffsetX * this.w / 100;
      int y = this.h - yOff - 20;
      int x = 10 + xOff;
      if (y < 100) {
        y = 100;
      } else if (y > this.h - 20) {
        y = this.h - 20;
      } 
      if (x < 10) {
        x = 10;
      } else if (x > this.w - 200) {
        x = this.w - 200;
      } 
      FastText.aprintf(g, new Coord(x, y), 0.0D, 1.0D, "FPS: %d (%d%% idle)", new Object[] { Long.valueOf(fps), Integer.valueOf((int)(this.idle * 100.0D)) });
      if (!fpsOnlyFPS) {
        Runtime rt = Runtime.getRuntime();
        long free = rt.freeMemory(), total = rt.totalMemory();
        y -= 15;
        FastText.aprintf(g, new Coord(x, y), 0.0D, 1.0D, "Mem: %,011d / %,011d / %,011d", new Object[] { Long.valueOf(total - free), Long.valueOf(total), Long.valueOf(rt.maxMemory()) });
        y -= 15;
        FastText.aprintf(g, new Coord(x, y), 0.0D, 1.0D, "Tex-current: %d", new Object[] { Integer.valueOf(TexGL.num()) });
        y -= 15;
        FastText.aprintf(g, new Coord(x, y), 0.0D, 1.0D, "RT-current: %d", new Object[] { Integer.valueOf(((Collection)TexRT.current.get(gl)).size()) });
        y -= 15;
        FastText.aprintf(g, new Coord(x, y), 0.0D, 1.0D, "GL progs: %d", new Object[] { Integer.valueOf(g.st.numprogs()) });
        if (Resource.qdepth() > 0) {
          y -= 15;
          FastText.aprintf(g, new Coord(x, y), 0.0D, 1.0D, "RQ depth: %d (%d)", new Object[] { Integer.valueOf(Resource.qdepth()), Integer.valueOf(Resource.numloaded()) });
        } 
      } 
    } 
    try {
      synchronized (ui) {
        tooltip = ui.root.tooltip(this.mousepos, ui.root);
      } 
    } catch (Loading e) {
      tooltip = "...";
    } 
    Tex tt = null;
    if (tooltip != null)
      if (tooltip instanceof Text) {
        tt = ((Text)tooltip).tex();
      } else if (tooltip instanceof Tex) {
        tt = (Tex)tooltip;
      } else if (tooltip instanceof Indir) {
        Indir<?> t = (Indir)tooltip;
        Object o = t.get();
        if (o instanceof Tex)
          tt = (Tex)o; 
      } else if (tooltip instanceof String && ((String)tooltip).length() > 0) {
        tt = Text.render((String)tooltip).tex();
      }  
    if (tt != null) {
      Coord sz = tt.sz();
      Coord pos = this.mousepos.add(sz.inv());
      if (pos.x < 5)
        pos.x = 5; 
      if (pos.y < 5)
        pos.y = 5; 
      g.chcolor(35, 35, 35, 192);
      g.frect(pos.add(-2, -2), sz.add(4, 4));
      g.chcolor(244, 247, 21, 192);
      g.rect(pos.add(-3, -3), sz.add(6, 6));
      g.chcolor();
      g.image(tt, pos);
    } 
    synchronized (ui) {
      ui.lastdraw(g);
    } 
    ui.lasttip = tooltip;
    Resource curs = ui.root.getcurs(this.mousepos);
    if (!curs.loading)
      if (this.cursmode == "awt") {
        if (curs != this.lastcursor)
          try {
            setCursor(makeawtcurs(((Resource.Image)curs.layer((Class)Resource.imgc)).img, ((Resource.Neg)curs.layer((Class)Resource.negc)).cc));
            this.lastcursor = curs;
          } catch (Exception e) {
            this.cursmode = "tex";
          }  
      } else if (this.cursmode == "tex") {
        Coord dc = this.mousepos.add(((Resource.Neg)curs.layer((Class)Resource.negc)).cc.inv());
        g.image(curs.<Resource.Image>layer(Resource.imgc), dc);
      }  
    this.state.clean();
    if (glconf.pref.dirty) {
      glconf.pref.save();
      glconf.pref.dirty = false;
    } 
  }
  
  void dispatch() {
    synchronized (this.events) {
      InputEvent e = null;
      while ((e = this.events.poll()) != null) {
        if (e instanceof MouseEvent) {
          MouseEvent me = (MouseEvent)e;
          if (me.getID() == 501) {
            if (MapView.ffThread > 0) {
              MapView.signalToStop = true;
              MapView.interruptedPreviousThread = true;
            } 
            FlowerMenu.interruptReceived = true;
            if (me.getButton() == 1 && this.ui != null && this.ui.gui != null && this.ui.gui.map != null)
              this.ui.gui.map.customItemActStop = true; 
            UI.tCount = 0;
            this.ui.mousedown(me, new Coord(me.getX(), me.getY()), me.getButton());
          } else if (me.getID() == 502) {
            this.ui.mouseup(me, new Coord(me.getX(), me.getY()), me.getButton());
          } else if (me.getID() == 503 || me.getID() == 506) {
            this.mousepos = new Coord(me.getX(), me.getY());
            this.ui.mousemove(me, this.mousepos);
          } else if (me instanceof MouseWheelEvent) {
            this.ui.mousewheel(me, new Coord(me.getX(), me.getY()), ((MouseWheelEvent)me).getWheelRotation());
          } 
        } else if (e instanceof KeyEvent) {
          KeyEvent ke = (KeyEvent)e;
          if (ke.getID() == 401) {
            this.ui.keydown(ke);
          } else if (ke.getID() == 402) {
            this.ui.keyup(ke);
          } else if (ke.getID() == 400) {
            this.ui.type(ke);
          } 
        } 
        this.ui.lastevent = System.currentTimeMillis();
      } 
    } 
  }
  
  public void uglyjoglhack() throws InterruptedException {
    try {
      this.rdr = true;
      display();
    } catch (RuntimeException e) {
      if (e.getCause() instanceof InterruptedException)
        throw (InterruptedException)e.getCause(); 
      throw e;
    } finally {
      this.rdr = false;
    } 
  }
  
  public void run() {
    int c = 0;
    boolean iA = false;
    try {
      int frames = 0, waited = 0;
      long fthen = System.nanoTime();
      Coord pcO = Coord.z;
      Coord pcN = pcO;
      int mT = 1;
      boolean x1 = false;
      boolean x2 = false;
      while (true) {
        UI ui = this.ui;
        long then = System.nanoTime();
        if (Config.profile) {
          this.prof.getClass();
          this.curf = new Profile.Frame(this.prof);
        } 
        synchronized (ui) {
          if (ui.sess != null)
            ui.sess.glob.ctick(); 
          dispatch();
          ui.tick();
          if (ui.root.sz.x != this.w || ui.root.sz.y != this.h)
            ui.root.resize(new Coord(this.w, this.h)); 
        } 
        if (this.curf != null)
          this.curf.tick("dsp"); 
        if (MainFrame.instance.getExtendedState() != 1)
          uglyjoglhack(); 
        ui.audio.cycle();
        if (this.curf != null)
          this.curf.tick("aux"); 
        frames++;
        long now = System.nanoTime();
        iA = MainFrame.instance.isActive();
        if (!AudioSprite.wasInactive && !iA)
          AudioSprite.wasInactive = true; 
        this.fd = (Config.slowmin && !iA) ? mT : (1000 / Config.custom_fps_target);
        this.fd *= 1000000L;
        if (now - then < this.fd) {
          synchronized (this.events) {
            long ttw = this.fd - now - then;
            long ttwl = ttw / 1000000L;
            int ttwn = (int)(ttw - ttwl * 1000000L);
            this.events.wait(ttwl, ttwn);
          } 
          waited = (int)(waited + System.nanoTime() - now);
        } 
        if (this.curf != null)
          this.curf.tick("wait"); 
        if (now - fthen > 1000000000L) {
          AudioSprite.checkForOtherInstancesMusic();
          fps = frames;
          this.idle = waited / (now - fthen);
          frames = 0;
          waited = 0;
          fthen = now;
          if (!iA) {
            try {
              pcN = (ui.gui.map.player()).rc;
            } catch (Exception exception) {}
            if (pcO.equals(pcN)) {
              if (c >= 3) {
                mT = 200;
              } else {
                c++;
              } 
            } else {
              c = 0;
              mT = 100;
            } 
            pcO = pcN;
          } else {
            c = 0;
            mT = 100;
          } 
        } 
        if (this.curf != null)
          this.curf.fin(); 
        if (Thread.interrupted())
          throw new InterruptedException(); 
      } 
    } catch (InterruptedException interruptedException) {
    
    } finally {
      this.ui.destroy();
    } 
  }
  
  public GraphicsConfiguration getconf() {
    return getGraphicsConfiguration();
  }
  
  public Map<String, Console.Command> findcmds() {
    return this.cmdmap;
  }
}
