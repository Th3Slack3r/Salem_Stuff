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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
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
   long fps = 0L;
   double idle = 0.0;
   Queue<InputEvent> events = new LinkedList<>();
   private String cursmode = "tex";
   private Resource lastcursor = null;
   public Coord mousepos = new Coord(0, 0);
   public Profile prof = new Profile(300);
   private Profile.Frame curf = null;
   public static final GLState.Slot<GLState> global = new GLState.Slot<>(GLState.Slot.Type.SYS, GLState.class);
   public static final GLState.Slot<GLState> proj2d = new GLState.Slot<>(GLState.Slot.Type.SYS, GLState.class, global);
   private GLState gstate;
   private GLState rtstate;
   private GLState ostate;
   private GLState.Applier state = null;
   private GLConfig glconf = null;
   private Map<String, Console.Command> cmdmap = new TreeMap<>();

   private static GLCapabilities stdcaps() {
      GLProfile prof = GLProfile.get("GL4bc");
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
      super(stdcaps(), cc, null, null);
      this.cmdmap.put("hz", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            HavenPanel.this.fd = 1000 / Integer.parseInt(args[1]);
         }
      });
      this.setSize(this.w = w, this.h = h);
      this.newui(null);
      this.initgl();
      if (Toolkit.getDefaultToolkit().getMaximumCursorColors() >= 256) {
         this.cursmode = "awt";
      }

      this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(TexI.mkbuf(new Coord(1, 1)), new Point(), ""));
   }

   public HavenPanel(int w, int h) {
      this(w, h, null);
   }

   private void initgl() {
      Thread caller = Thread.currentThread();
      final ErrorHandler h = ErrorHandler.find();
      this.addGLEventListener(new GLEventListener() {
         public void display(GLAutoDrawable d) {
            GL2 gl = d.getGL().getGL2();
            if (HavenPanel.this.inited && HavenPanel.this.rdr) {
               HavenPanel.this.redraw(gl);
            }

            GLObject.disposeall(gl);
         }

         public void init(GLAutoDrawable d) {
            GL gl = d.getGL();
            HavenPanel.this.glconf = GLConfig.fromgl(gl, d.getContext(), HavenPanel.this.getChosenGLCapabilities());
            HavenPanel.this.glconf.pref = GLSettings.load(HavenPanel.this.glconf, true);
            HavenPanel.this.ui.cons.add(HavenPanel.this.glconf);
            if (h != null) {
               h.lsetprop("gl.vendor", gl.glGetString(7936));
               h.lsetprop("gl.version", gl.glGetString(7938));
               h.lsetprop("gl.renderer", gl.glGetString(7937));
               h.lsetprop("gl.exts", Arrays.asList(gl.glGetString(7939).split(" ")));
               h.lsetprop("gl.caps", d.getChosenGLCapabilities().toString());
               h.lsetprop("gl.conf", HavenPanel.this.glconf);
            }

            Config.setglpref(HavenPanel.this.glconf.pref);
            HavenPanel.this.gstate = new GLState() {
               @Override
               public void apply(GOut g) {
                  GL2 glx = g.gl;
                  glx.glColor3f(1.0F, 1.0F, 1.0F);
                  glx.glPointSize(4.0F);
                  glx.setSwapInterval(1);
                  glx.glEnable(3042);
                  glx.glBlendFunc(770, 771);
                  if (g.gc.glmajver >= 2) {
                     glx.glBlendEquationSeparate(32774, 32776);
                  }

                  if (g.gc.havefsaa()) {
                     g.gl.glDisable(32925);
                  }

                  GOut.checkerr(glx);
               }

               @Override
               public void unapply(GOut g) {
               }

               @Override
               public void prep(GLState.Buffer buf) {
                  buf.put(HavenPanel.global, this);
               }
            };
         }

         public void reshape(GLAutoDrawable d, int x, int y, final int w, final int hx) {
            HavenPanel.this.ostate = HavenPanel.OrthoState.fixed(new Coord(w, h));
            HavenPanel.this.rtstate = new GLState() {
               @Override
               public void apply(GOut g) {
                  GL2 gl = g.gl;
                  g.st.matmode(5889);
                  gl.glLoadIdentity();
                  gl.glOrtho(0.0, w, 0.0, h, -1.0, 1.0);
               }

               @Override
               public void unapply(GOut g) {
               }

               @Override
               public void prep(GLState.Buffer buf) {
                  buf.put(HavenPanel.proj2d, this);
               }
            };
            HavenPanel.this.w = w;
            HavenPanel.this.h = h;
         }

         public void displayChanged(GLAutoDrawable d, boolean cp1, boolean cp2) {
         }

         public void dispose(GLAutoDrawable d) {
         }
      });
   }

   public void init() {
      this.setFocusTraversalKeysEnabled(false);
      this.newui(null);
      this.addKeyListener(new KeyAdapter() {
         @Override
         public void keyTyped(KeyEvent e) {
            synchronized (HavenPanel.this.events) {
               HavenPanel.this.events.add(e);
               HavenPanel.this.events.notifyAll();
            }
         }

         @Override
         public void keyPressed(KeyEvent e) {
            synchronized (HavenPanel.this.events) {
               HavenPanel.this.events.add(e);
               HavenPanel.this.events.notifyAll();
            }
         }

         @Override
         public void keyReleased(KeyEvent e) {
            synchronized (HavenPanel.this.events) {
               HavenPanel.this.events.add(e);
               HavenPanel.this.events.notifyAll();
            }
         }
      });
      this.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            synchronized (HavenPanel.this.events) {
               HavenPanel.this.events.add(e);
               HavenPanel.this.events.notifyAll();
            }
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            synchronized (HavenPanel.this.events) {
               HavenPanel.this.events.add(e);
               HavenPanel.this.events.notifyAll();
            }
         }
      });
      this.addMouseMotionListener(new MouseMotionListener() {
         @Override
         public void mouseDragged(MouseEvent e) {
            synchronized (HavenPanel.this.events) {
               HavenPanel.this.events.add(e);
            }
         }

         @Override
         public void mouseMoved(MouseEvent e) {
            synchronized (HavenPanel.this.events) {
               HavenPanel.this.events.add(e);
            }
         }
      });
      this.addMouseWheelListener(new MouseWheelListener() {
         @Override
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
      if (this.ui != null) {
         this.ui.destroy();
      }

      this.ui = new UI(new Coord(this.w, this.h), sess);
      this.ui.root.gprof = this.prof;
      if (this.getParent() instanceof Console.Directory) {
         this.ui.cons.add((Console.Directory)this.getParent());
      }

      this.ui.cons.add(this);
      if (this.glconf != null) {
         this.ui.cons.add(this.glconf);
      }

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
      if (this.state == null || this.state.gl != gl) {
         this.state = new GLState.Applier(gl, this.glconf);
      }

      GLState.Buffer ibuf = new GLState.Buffer(this.glconf);
      this.gstate.prep(ibuf);
      this.ostate.prep(ibuf);
      GOut g = new GOut(gl, this.getContext(), this.glconf, this.state, ibuf, new Coord(this.w, this.h));
      UI ui = this.ui;
      this.state.set(ibuf);
      g.state(this.rtstate);
      TexRT.renderall(g);
      if (this.curf != null) {
         this.curf.tick("texrt");
      }

      g.state(this.ostate);
      g.apply();
      gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
      gl.glClear(16384);
      if (this.curf != null) {
         this.curf.tick("cls");
      }

      synchronized (ui) {
         ui.draw(g);
      }

      if (this.curf != null) {
         this.curf.tick("draw");
      }

      if (Config.dbtext) {
         int y = this.h - 20;
         y -= 15;
         FastText.aprintf(g, new Coord(10, y), 0.0, 1.0, "FPS: %d (%d%% idle)", this.fps, (int)(this.idle * 100.0));
         Runtime rt = Runtime.getRuntime();
         long free = rt.freeMemory();
         long total = rt.totalMemory();
         y -= 15;
         FastText.aprintf(g, new Coord(10, y), 0.0, 1.0, "Mem: %,011d/%,011d/%,011d/%,011d", free, total - free, total, rt.maxMemory());
         y -= 15;
         FastText.aprintf(g, new Coord(10, y), 0.0, 1.0, "Tex-current: %d", TexGL.num());
         y -= 15;
         FastText.aprintf(g, new Coord(10, y), 0.0, 1.0, "RT-current: %d", TexRT.current.get(gl).size());
         y -= 15;
         FastText.aprintf(g, new Coord(10, y), 0.0, 1.0, "GL progs: %d", g.st.numprogs());
         GameUI gi = ui.gui;
         if (gi != null && gi.map != null) {
            try {
               y -= 15;
               FastText.aprintf(g, new Coord(10, y), 0.0, 1.0, "MV pos: %s (%s)", gi.map.getcc(), gi.map.camera);
            } catch (Loading var19) {
            }
         }

         if (Resource.qdepth() > 0) {
            y -= 15;
            FastText.aprintf(g, new Coord(10, y), 0.0, 1.0, "RQ depth: %d (%d)", Resource.qdepth(), Resource.numloaded());
         }
      }

      Object tooltip;
      try {
         synchronized (ui) {
            tooltip = ui.root.tooltip(this.mousepos, ui.root);
         }
      } catch (Loading var18) {
         tooltip = "...";
      }

      Tex tt = null;
      if (tooltip != null) {
         if (tooltip instanceof Text) {
            tt = ((Text)tooltip).tex();
         } else if (tooltip instanceof Tex) {
            tt = (Tex)tooltip;
         } else if (tooltip instanceof Indir) {
            Indir<?> t = (Indir<?>)tooltip;
            Object o = t.get();
            if (o instanceof Tex) {
               tt = (Tex)o;
            }
         } else if (tooltip instanceof String && ((String)tooltip).length() > 0) {
            tt = Text.render((String)tooltip).tex();
         }
      }

      if (tt != null) {
         Coord sz = tt.sz();
         Coord pos = this.mousepos.add(sz.inv());
         if (pos.x < 5) {
            pos.x = 5;
         }

         if (pos.y < 5) {
            pos.y = 5;
         }

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
      if (!curs.loading) {
         if (this.cursmode == "awt") {
            if (curs != this.lastcursor) {
               try {
                  this.setCursor(makeawtcurs(curs.layer(Resource.imgc).img, curs.layer(Resource.negc).cc));
                  this.lastcursor = curs;
               } catch (Exception var15) {
                  this.cursmode = "tex";
               }
            }
         } else if (this.cursmode == "tex") {
            Coord dc = this.mousepos.add(curs.layer(Resource.negc).cc.inv());
            g.image(curs.layer(Resource.imgc), dc);
         }
      }

      this.state.clean();
      if (this.glconf.pref.dirty) {
         this.glconf.pref.save();
         this.glconf.pref.dirty = false;
      }
   }

   void dispatch() {
      synchronized (this.events) {
         InputEvent e;
         for (e = null; (e = this.events.poll()) != null; this.ui.lastevent = System.currentTimeMillis()) {
            if (e instanceof MouseEvent) {
               MouseEvent me = (MouseEvent)e;
               if (me.getID() == 501) {
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
         }
      }
   }

   public void uglyjoglhack() throws InterruptedException {
      try {
         this.rdr = true;
         this.display();
      } catch (RuntimeException var5) {
         if (var5.getCause() instanceof InterruptedException) {
            throw (InterruptedException)var5.getCause();
         }

         throw var5;
      } finally {
         this.rdr = false;
      }
   }

   @Override
   public void run() {
      try {
         int frames = 0;
         int waited = 0;
         long fthen = System.currentTimeMillis();

         do {
            Debug.cycle();
            UI ui = this.ui;
            long then = System.currentTimeMillis();
            if (Config.profile) {
               this.curf = this.prof.new Frame();
            }

            synchronized (ui) {
               if (ui.sess != null) {
                  ui.sess.glob.ctick();
               }

               this.dispatch();
               ui.tick();
               if (ui.root.sz.x != this.w || ui.root.sz.y != this.h) {
                  ui.root.resize(new Coord(this.w, this.h));
               }
            }

            if (this.curf != null) {
               this.curf.tick("dsp");
            }

            if (MainFrame.instance.getExtendedState() != 1) {
               this.uglyjoglhack();
            }

            ui.audio.cycle();
            if (this.curf != null) {
               this.curf.tick("aux");
            }

            frames++;
            long now = System.currentTimeMillis();
            this.fd = Config.slowmin && !MainFrame.instance.isActive() ? 100L : ui.get_fps_wait_time();
            if (now - then < this.fd) {
               synchronized (this.events) {
                  this.events.wait(this.fd - (now - then));
               }

               waited = (int)(waited + (System.currentTimeMillis() - now));
            }

            if (this.curf != null) {
               this.curf.tick("wait");
            }

            if (now - fthen > 1000L) {
               this.fps = frames;
               this.idle = (double)waited / (now - fthen);
               frames = 0;
               waited = 0;
               fthen = now;
            }

            if (this.curf != null) {
               this.curf.fin();
            }
         } while (!Thread.interrupted());

         throw new InterruptedException();
      } catch (InterruptedException var20) {
      } finally {
         this.ui.destroy();
      }
   }

   public GraphicsConfiguration getconf() {
      return this.getGraphicsConfiguration();
   }

   @Override
   public Map<String, Console.Command> findcmds() {
      return this.cmdmap;
   }

   public abstract static class OrthoState extends GLState {
      protected abstract Coord sz();

      @Override
      public void apply(GOut g) {
         GL2 gl = g.gl;
         Coord sz = this.sz();
         g.st.matmode(5889);
         gl.glLoadIdentity();
         gl.glOrtho(0.0, sz.x, sz.y, 0.0, -1.0, 1.0);
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(HavenPanel.proj2d, this);
      }

      public static HavenPanel.OrthoState fixed(final Coord sz) {
         return new HavenPanel.OrthoState() {
            @Override
            protected Coord sz() {
               return sz;
            }
         };
      }
   }
}
