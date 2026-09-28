package haven;

import haven.glsl.Type;
import haven.glsl.Uniform;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import javax.media.opengl.GL;

public class MapView extends PView implements DTarget, Console.Directory {
   public static final String DEFCAM = "sortho";
   private final R2DWdg r2dwdg;
   public long plgob = -1L;
   public Coord cc;
   private final Glob glob;
   private int view = 2;
   private Collection<MapView.Delayed> delayed = new LinkedList<>();
   private Collection<MapView.Delayed> delayed2 = new LinkedList<>();
   private Collection<Rendered> extradraw = new LinkedList<>();
   public MapView.Camera camera;
   private MapView.Plob placing = null;
   private int[] visol = new int[32];
   private MapView.Grabber grab;
   public static final Map<String, Class<? extends MapView.Camera>> camtypes = new HashMap<>();
   private boolean rendering3d = true;
   private final Rendered map;
   public static final int WFOL = 18;
   public static final Tex wftex = Resource.loadtex("gfx/hud/flat");
   private final Rendered mapol;
   private final Rendered gobs;
   private Coord3f smapcc;
   private ShadowMap smap;
   private long lsmch;
   private DropSky.ResSky sky1;
   private DropSky.ResSky sky2;
   public Light amb;
   private Outlines outlines;
   public static final Uniform amblight = new Uniform.AutoApply(Type.INT) {
      @Override
      public void apply(GOut g, int loc) {
         int idx = -1;
         PView.RenderContext ctx = g.st.get(PView.ctx);
         if (ctx instanceof PView.WidgetContext) {
            Widget wdg = ((PView.WidgetContext)ctx).widget();
            if (wdg instanceof MapView) {
               idx = g.st.get(Light.lights).index(((MapView)wdg).amb);
            }
         }

         g.gl.glUniform1i(loc, idx);
      }
   };
   private final PView.RenderContext clickctx;
   private static final Text.Furnace polownertf = new PUtils.BlurFurn(new Text.Foundry("serif", 30).aa(true), 3, 1, Color.BLACK);
   private Text polownert;
   private long polchtm;
   private boolean camload;
   private Loading lastload;
   private int olflash;
   private long olftimer;
   private boolean camdrag;
   private boolean LMBdown;
   private boolean mousemoved;
   private Map<String, Console.Command> cmdmap;

   public MapView(Coord c, Coord sz, Widget parent, Coord cc, long plgob) {
      super(c, sz, parent);
      camtypes.put("follow", MapView.FollowCam.class);
      camtypes.put("sfollow", MapView.SmoothFollowCam.class);
      camtypes.put("free", MapView.FreeCam.class);
      camtypes.put("ortho", MapView.OrthoCam.class);
      camtypes.put("sortho", MapView.SOrthoCam.class);
      this.map = new Rendered() {
         @Override
         public void draw(GOut g) {
         }

         @Override
         public boolean setup(RenderList rl) {
            Coord ccx = MapView.this.cc.div(MCache.tilesz).div(MCache.cutsz);
            Coord o = new Coord();

            for (o.y = -MapView.this.view; o.y <= MapView.this.view; o.y++) {
               for (o.x = -MapView.this.view; o.x <= MapView.this.view; o.x++) {
                  Coord pc = ccx.add(o).mul(MCache.cutsz).mul(MCache.tilesz);
                  MapMesh cut = MapView.this.glob.map.getcut(ccx.add(o));
                  rl.add(cut, Location.xlate(new Coord3f(pc.x, -pc.y, 0.0F)));

                  Collection<Gob> fol;
                  try {
                     fol = MapView.this.glob.map.getfo(ccx.add(o));
                  } catch (Loading var9) {
                     fol = Collections.emptyList();
                  }

                  for (Gob fo : fol) {
                     MapView.this.addgob(rl, fo);
                  }
               }
            }

            return false;
         }
      };
      this.mapol = new Rendered() {
         private final GLState[] mats = new GLState[32];

         {
            this.mats[0] = new Material(new Color(255, 0, 128, 32));
            this.mats[1] = new Material(new Color(0, 0, 255, 32));
            this.mats[2] = new Material(new Color(255, 0, 0, 32));
            this.mats[3] = new Material(new Color(128, 0, 255, 32));
            this.mats[4] = new Material(new Color(255, 0, 0, 96));
            this.mats[16] = new Material(new Color(0, 255, 0, 32));
            this.mats[17] = new Material(new Color(255, 255, 0, 32));
            this.mats[18] = new Material(MapView.wftex, true);
            this.mats[18] = new Material(MapView.wftex);
         }

         @Override
         public void draw(GOut g) {
         }

         @Override
         public boolean setup(RenderList rl) {
            Coord cc = MapView.this.cc.div(MCache.tilesz).div(MCache.cutsz);
            Coord o = new Coord();

            for (o.y = -MapView.this.view; o.y <= MapView.this.view; o.y++) {
               for (o.x = -MapView.this.view; o.x <= MapView.this.view; o.x++) {
                  Coord pc = cc.add(o).mul(MCache.cutsz).mul(MCache.tilesz);

                  for (int i = 0; i < MapView.this.visol.length; i++) {
                     if (this.mats[i] != null && MapView.this.visol[i] > 0) {
                        Rendered olcut = MapView.this.glob.map.getolcut(i, cc.add(o));
                        if (olcut != null) {
                           rl.add(olcut, GLState.compose(Location.xlate(new Coord3f(pc.x, -pc.y, 0.0F)), this.mats[i]));
                        }
                     }
                  }
               }
            }

            return false;
         }
      };
      this.gobs = new Rendered() {
         @Override
         public void draw(GOut g) {
         }

         @Override
         public boolean setup(RenderList rl) {
            synchronized (MapView.this.glob.oc) {
               for (Gob gob : MapView.this.glob.oc) {
                  MapView.this.addgob(rl, gob);
               }

               return false;
            }
         }
      };
      this.smapcc = null;
      this.smap = null;
      this.lsmch = 0L;
      this.sky1 = new DropSky.ResSky(null);
      this.sky2 = new DropSky.ResSky(null);
      this.amb = null;
      this.outlines = new Outlines(false);
      this.clickctx = new PView.RenderContext();
      this.polownert = null;
      this.polchtm = 0L;
      this.camload = false;
      this.lastload = null;
      this.camdrag = false;
      this.LMBdown = false;
      this.mousemoved = false;
      this.cmdmap = new TreeMap<>();
      this.cmdmap.put("cam", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) throws Exception {
            MapView.this.setcam(args[1]);
            Class<? extends MapView.Camera> ccx = MapView.camtypes.get(args[1]);
            if (ccx == null) {
               throw new Exception("no such camera type: " + args[1]);
            } else {
               Constructor<? extends MapView.Camera> ccc = ccx.getConstructor(MapView.class);
               MapView.this.camera = Utils.construct(ccc, MapView.this);
            }
         }
      });
      this.cmdmap.put("whyload", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) throws Exception {
            Loading l = MapView.this.lastload;
            if (l == null) {
               throw new Exception("Not loading");
            } else {
               l.printStackTrace(cons.out);
            }
         }
      });
      this.setcam(Utils.getpref("defcam", "sortho"));
      this.glob = this.ui.sess.glob;
      this.cc = cc;
      this.plgob = plgob;
      this.setcanfocus(true);
      this.r2dwdg = new R2DWdg(this);
   }

   public void enol(int... overlays) {
      for (int ol : overlays) {
         this.visol[ol]++;
      }
   }

   public void disol(int... overlays) {
      for (int ol : overlays) {
         this.visol[ol]--;
      }
   }

   public boolean visol(int ol) {
      return this.visol[ol] > 0;
   }

   void addgob(RenderList rl, Gob gob) {
      GLState xf;
      try {
         xf = Following.xf(gob);
      } catch (Loading var8) {
         xf = null;
      }

      GLState extra = null;
      if (xf == null) {
         xf = gob.loc;

         try {
            Coord3f c = gob.getc();
            Tiler tile = this.glob.map.tiler(this.glob.map.gettile(new Coord(c).div(MCache.tilesz)));
            extra = tile.drawstate(this.glob, rl.cfg, c);
         } catch (Loading var7) {
            extra = null;
         }
      }

      if (extra != null) {
         rl.add(gob, GLState.compose(extra, xf, gob.olmod, gob.save));
      } else {
         rl.add(gob, GLState.compose(xf, gob.olmod, gob.save));
      }
   }

   @Override
   public GLState camera() {
      return this.camera;
   }

   @Override
   protected Projection makeproj() {
      return null;
   }

   private void updsmap(RenderList rl, DirLight light) {
      if (rl.cfg.pref.lshadow.val) {
         if (this.smap == null) {
            this.smap = new ShadowMap(new Coord(2048, 2048), 750.0F, 5000.0F, 1.0F);
         }

         this.smap.light = light;
         Coord3f dir = new Coord3f(-light.dir[0], -light.dir[1], -light.dir[2]);
         Coord3f cc = this.getcc();
         cc.y = -cc.y;
         boolean ch = false;
         long now = System.currentTimeMillis();
         if (this.smapcc == null || this.smapcc.dist(cc) > 50.0F) {
            this.smapcc = cc;
            ch = true;
         } else if (now - this.lsmch > 100L) {
            ch = true;
         }

         if (ch) {
            this.smap.setpos(this.smapcc.add(dir.neg().mul(1000.0F)), dir);
            this.lsmch = now;
         }

         rl.prepc(this.smap);
      } else {
         if (this.smap != null) {
            this.smap.dispose();
         }

         this.smap = null;
         this.smapcc = null;
      }
   }

   @Override
   public void setup(RenderList rl) {
      if (this.rendering3d) {
         Gob pl = this.player();
         if (pl != null) {
            this.cc = new Coord(pl.getc());
         }

         synchronized (this.glob) {
            if (this.glob.lightamb != null) {
               DirLight light = new DirLight(
                  this.glob.lightamb, this.glob.lightdif, this.glob.lightspc, Coord3f.o.sadd((float)this.glob.lightelev, (float)this.glob.lightang, 1.0F)
               );
               rl.add(light, null);
               this.updsmap(rl, light);
               this.amb = light;
            } else {
               this.amb = null;
            }
         }

         if (rl.cfg.pref.outline.val) {
            rl.add(this.outlines, null);
         }

         rl.add(this.map, null);
         rl.add(this.mapol, null);
         rl.add(this.gobs, null);
         if (this.placing != null) {
            this.addgob(rl, this.placing);
         }

         synchronized (this.extradraw) {
            for (Rendered extra : this.extradraw) {
               rl.add(extra, null);
            }

            this.extradraw.clear();
         }

         if (Config.skybox && this.glob.sky1 != null) {
            this.sky1.update(this.glob.sky1);
            rl.add(this.sky1, Rendered.last);
            if (this.glob.sky2 != null) {
               this.sky2.update(this.glob.sky2);
               this.sky2.alpha = this.glob.skyblend;
               rl.add(this.sky2, Rendered.last);
            }
         }
      }
   }

   public void drawadd(Rendered extra) {
      synchronized (this.extradraw) {
         this.extradraw.add(extra);
      }
   }

   public Gob player() {
      return this.glob.oc.getgob(this.plgob);
   }

   public Coord3f getcc() {
      Gob pl = this.player();
      return pl != null ? pl.getc() : new Coord3f(this.cc.x, this.cc.y, this.glob.map.getcz(this.cc));
   }

   private GLState.Buffer clickbasic(GOut g) {
      GLState.Buffer ret = this.basic(g);
      this.clickctx.prep(ret);
      return ret;
   }

   private Coord checkmapclick(GOut g, Coord c) {
      MapView.Maplist rl = new MapView.Maplist(this.clickbasic(g));
      rl.setup(this.map, this.clickbasic(g));
      rl.fin();
      rl.render(g);
      MapMesh hit = rl.get(g, c);
      if (hit == null) {
         return null;
      } else {
         rl.limit = hit;
         rl.mode = 1;
         rl.render(g);
         Color hitcol = g.getpixel(c);
         Coord tile = new Coord(hitcol.getRed() - 1, hitcol.getGreen() - 1);
         if (!tile.isect(Coord.z, rl.limit.sz)) {
            return null;
         } else {
            rl.mode = 2;
            rl.render(g);
            Color hitcolx = g.getpixel(c);
            if (hitcolx.getBlue() != 0) {
               return null;
            } else {
               Coord pixel = new Coord(hitcolx.getRed() * MCache.tilesz.x / 255, hitcolx.getGreen() * MCache.tilesz.y / 255);
               return rl.limit.ul.add(tile).mul(MCache.tilesz).add(pixel);
            }
         }
      }
   }

   private MapView.ClickInfo checkgobclick(GOut g, Coord c) {
      MapView.Clicklist<MapView.ClickInfo> rl = new MapView.Clicklist<MapView.ClickInfo>(this.clickbasic(g)) {
         Gob curgob;
         Gob.Overlay curol;
         MapView.ClickInfo curinfo;

         public MapView.ClickInfo map(Rendered r) {
            return this.curinfo;
         }

         @Override
         public void add(Rendered r, GLState t) {
            Gob prevg = this.curgob;
            Gob.Overlay prevo = this.curol;
            if (r instanceof Gob) {
               this.curgob = (Gob)r;
            } else if (r instanceof Gob.Overlay) {
               this.curol = (Gob.Overlay)r;
            }

            if (this.curgob != null && r instanceof FRendered) {
               this.curinfo = new MapView.ClickInfo(this.curgob, this.curol, r);
            } else {
               this.curinfo = null;
            }

            super.add(r, t);
            this.curgob = prevg;
            this.curol = prevo;
         }
      };
      rl.setup(this.gobs, this.clickbasic(g));
      rl.fin();
      rl.render(g);
      return rl.get(g, c);
   }

   public void delay(MapView.Delayed d) {
      if (this.rendering3d) {
         synchronized (this.delayed) {
            this.delayed.add(d);
         }
      }
   }

   public void delay2(MapView.Delayed d) {
      if (this.rendering3d) {
         synchronized (this.delayed2) {
            this.delayed2.add(d);
         }
      }
   }

   protected void undelay(Collection<MapView.Delayed> list, GOut g, boolean clear) {
      synchronized (list) {
         for (MapView.Delayed d : list) {
            d.run(g);
         }

         if (clear) {
            list.clear();
         }
      }
   }

   public void setpoltext(String text) {
      this.polownert = polownertf.render(text);
      this.polchtm = System.currentTimeMillis();
   }

   private void poldraw(GOut g) {
      long now = System.currentTimeMillis();
      long poldt = now - this.polchtm;
      if (this.polownert != null && poldt < 6000L) {
         int a;
         if (poldt < 1000L) {
            a = (int)(255L * poldt / 1000L);
         } else if (poldt < 4000L) {
            a = 255;
         } else {
            a = (int)(255L * (2000L - (poldt - 4000L)) / 2000L);
         }

         g.chcolor(255, 255, 255, a);
         g.aimage(this.polownert.tex(), this.sz.div(2), 0.5, 0.5);
         g.chcolor();
      }
   }

   private void drawarrow(GOut g, double a) {
      Coord hsz = this.sz.div(2);
      double ca = -Coord.z.angle(hsz);
      Coord ac;
      if (a > ca && a < -ca) {
         ac = new Coord(this.sz.x, hsz.y - (int)(Math.tan(a) * hsz.x));
      } else if (a > -ca && a < Math.PI + ca) {
         ac = new Coord(hsz.x - (int)(Math.tan(a - (Math.PI / 2)) * hsz.y), 0);
      } else if (a > -Math.PI - ca && a < ca) {
         ac = new Coord(hsz.x + (int)(Math.tan(a + (Math.PI / 2)) * hsz.y), this.sz.y);
      } else {
         ac = new Coord(0, hsz.y + (int)(Math.tan(a) * hsz.x));
      }

      Coord bc = ac.add(Coord.sc(a, -10.0));
      g.line(bc, bc.add(Coord.sc(a, -40.0)), 2.0);
      g.line(bc, bc.add(Coord.sc(a + (Math.PI / 4), -10.0)), 2.0);
      g.line(bc, bc.add(Coord.sc(a - (Math.PI / 4), -10.0)), 2.0);
   }

   public double screenangle(Coord mc, boolean clip) {
      Coord3f cc;
      try {
         cc = this.getcc();
      } catch (Loading var7) {
         return Double.NaN;
      }

      Coord3f mloc = new Coord3f(mc.x, -mc.y, cc.z);
      float[] sloc = this.camera.proj.toclip(this.camera.view.fin(Matrix4f.id).mul4(mloc));
      if (clip) {
         float w = sloc[3];
         if (sloc[0] > -w && sloc[0] < w && sloc[1] > -w && sloc[1] < w) {
            return Double.NaN;
         }
      }

      float a = (float)this.sz.y / this.sz.x;
      return Math.atan2(sloc[1] * a, sloc[0]);
   }

   private void partydraw(GOut g) {
      for (Party.Member m : this.ui.sess.glob.party.memb.values()) {
         if (m.gobid != this.plgob) {
            Coord mc = m.getc();
            if (mc != null) {
               double a = this.screenangle(mc, true);
               if (a != Double.NaN) {
                  g.chcolor(m.col);
                  this.drawarrow(g, a);
               }
            }
         }
      }

      g.chcolor();
   }

   @Override
   public void draw(GOut g) {
      this.glob.map.sendreqs();
      if (this.olftimer != 0L && this.olftimer < System.currentTimeMillis()) {
         this.unflashol();
      }

      try {
         if (this.camload) {
            throw new MCache.LoadingMap();
         }

         this.undelay(this.delayed, g, true);
         super.draw(g);
         Matrix4f dgpcam = new Matrix4f();
         Matrix4f dgpwxf = new Matrix4f();
         dgpcam.load(g.st.cam);
         dgpwxf.load(g.st.wxf);
         this.undelay(this.delayed2, g, true);
         this.poldraw(g);
         this.partydraw(g);
         if (Config.showgobpath) {
            this.drawGobPath(g, dgpcam, dgpwxf);
         }

         if (this.player() != null) {
            Moving m = this.player().getattr(Moving.class);
            if (m != null && m instanceof LinMove) {
               LinMove gobpath = (LinMove)m;
               LocatorTool.addPath(gobpath);
            }
         }

         if (Config.watchguard) {
            this.orientToEnemy();
         }

         this.glob
            .map
            .reqarea(this.cc.div(MCache.tilesz).sub(MCache.cutsz.mul(this.view + 1)), this.cc.div(MCache.tilesz).add(MCache.cutsz.mul(this.view + 1)));
      } catch (Loading var6) {
         this.lastload = var6;
         String text = "Loading...";
         g.chcolor(Color.BLACK);
         g.frect(Coord.z, this.sz);
         g.chcolor(Color.WHITE);
         g.atext(text, this.sz.div(2), 0.5, 0.5);
      }
   }

   public void toggle_rendering() {
      this.rendering3d = !this.rendering3d;
      this.ui.fps_goal = this.rendering3d ? 50 : 5;
      if (!this.rendering3d) {
         this.delayed.clear();
         this.delayed2.clear();
         this.extradraw.clear();
      }
   }

   private void drawGobPath(GOut g, Matrix4f dgpcam, Matrix4f dgpwxf) {
      g.chcolor(Color.GREEN);
      Matrix4f cam = new Matrix4f();
      Matrix4f wxf = new Matrix4f();
      Matrix4f mv = new Matrix4f();
      new Matrix4f();
      mv.load(cam.load(dgpcam)).mul1(wxf.load(dgpwxf));
      Matrix4f wxfaccent = wxf.trim3(1.0F).transpose();
      float field = Config.camera_field_of_view;
      float aspect = (float)g.sz.y / g.sz.x;
      Projection proj = Projection.frustum(-field, field, -aspect * field, aspect * field, 1.0F, 5000.0F);
      synchronized (this.glob.oc) {
         for (Gob gob : this.glob.oc) {
            if (gob.sc != null) {
               Moving m = gob.getattr(Moving.class);
               if (m != null) {
                  Coord3f reposstart = Coord3f.o;
                  Coord3f reposend = Coord3f.o;
                  reposstart = gob.getc();
                  if (m instanceof LinMove) {
                     LinMove gobpath = (LinMove)m;
                     float targetheight = 0.0F;

                     try {
                        targetheight = this.glob.map.getcz(gobpath.t);
                     } catch (MCache.LoadingMap var22) {
                        targetheight = reposstart.z;
                     }

                     reposend = new Coord3f(gobpath.t.x, gobpath.t.y, targetheight);
                  } else if (m instanceof Homing) {
                     Homing gobpath = (Homing)m;
                     long targetid = gobpath.tgt;
                     Gob target = this.glob.oc.getgob(targetid);
                     if (target != null) {
                        reposend = target.getc();
                     } else {
                        reposend = new Coord3f(gobpath.tc.x, gobpath.tc.y, reposstart.z);
                     }
                  } else {
                     if (!(m instanceof Following)) {
                        continue;
                     }

                     Following gobpath = (Following)m;
                     long targetid = gobpath.tgt;
                     Gob target = this.glob.oc.getgob(targetid);
                     if (target != null) {
                        reposend = target.getc();
                     } else {
                        reposend = gobpath.getc();
                     }
                  }

                  reposstart.x = reposstart.x - wxf.get(3, 0);
                  reposstart.y *= -1.0F;
                  reposstart.y = reposstart.y - wxf.get(3, 1);
                  reposstart.z = reposstart.z - wxf.get(3, 2);
                  reposstart = wxfaccent.mul4(reposstart);
                  Coord3f sstart = proj.toscreen(mv.mul4(reposstart), g.sz);
                  Coord scstart = new Coord(sstart);
                  reposend.x = reposend.x - wxf.get(3, 0);
                  reposend.y *= -1.0F;
                  reposend.y = reposend.y - wxf.get(3, 1);
                  reposend.z = reposend.z - wxf.get(3, 2);
                  reposend = wxfaccent.mul4(reposend);
                  Coord3f send = proj.toscreen(mv.mul4(reposend), g.sz);
                  Coord scend = new Coord(send);
                  g.line(scstart, scend, 2.0);
               }
            }
         }
      }

      g.chcolor();
   }

   private void orientToEnemy() {
      if (this.ui.gui.fv != null && !this.ui.gui.fv.lsrel.isEmpty()) {
         Moving m = this.player().getattr(Moving.class);
         if (m != null) {
            return;
         }

         Fightview.Relation r = this.ui.gui.fv.lsrel.getFirst();
         Gob enemy = this.glob.oc.getgob(r.gobid);
         if (enemy != null && !enemy.virtual) {
            double angle = this.player().rc.angle(enemy.rc);
            double diff = Math.abs(this.player().a - angle);
            if (diff > Math.PI) {
               diff = (Math.PI * 2) - diff;
            }

            if (Math.abs(diff) > Math.PI / 6) {
               double cosa = Math.cos(angle);
               double sina = Math.sin(angle);
               this.wdgmsg(this, "click", new Object[]{this.player().sc, this.player().rc.add((int)Math.round(cosa), (int)Math.round(sina)), 1, 0});
            }
         }
      }
   }

   @Override
   public void tick(double dt) {
      this.camload = false;

      try {
         this.camera.tick(dt);
      } catch (Loading var4) {
         this.camload = true;
      }

      if (this.placing != null) {
         this.placing.ctick((int)(dt * 1000.0));
      }
   }

   @Override
   public void resize(Coord sz) {
      super.resize(sz);
      this.r2dwdg.resize(sz);
      this.camera.resized();
   }

   @Override
   protected void render2d(GOut g) {
   }

   private void unflashol() {
      for (int i = 0; i < this.visol.length; i++) {
         if ((this.olflash & 1 << i) != 0) {
            this.visol[i]--;
         }
      }

      this.olflash = 0;
      this.olftimer = 0L;
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "place") {
         int a = 0;
         Indir<Resource> res = this.ui.sess.getres((Integer)args[a++]);
         Message sdt;
         if (args.length > a && args[a] instanceof byte[]) {
            sdt = new Message(0, (byte[])args[a++]);
         } else {
            sdt = Message.nil;
         }

         this.placing = new MapView.Plob(res, sdt);

         while (a < args.length) {
            Indir<Resource> ores = this.ui.sess.getres((Integer)args[a++]);
            Message odt;
            if (args.length > a && args[a] instanceof byte[]) {
               odt = new Message(0, (byte[])args[a++]);
            } else {
               odt = Message.nil;
            }

            this.placing.ols.add(new Gob.Overlay(-1, ores, odt));
         }
      } else if (msg == "unplace") {
         this.placing = null;
      } else if (msg == "move") {
         this.cc = (Coord)args[0];
      } else if (msg == "flashol") {
         this.unflashol();
         this.olflash = (Integer)args[0];

         for (int i = 0; i < this.visol.length; i++) {
            if ((this.olflash & 1 << i) != 0) {
               this.visol[i]++;
            }
         }

         this.olftimer = System.currentTimeMillis() + ((Integer)args[1]).intValue();
      } else {
         super.uimsg(msg, args);
      }
   }

   private static int getid(Rendered tgt) {
      return tgt instanceof ResPart ? ((ResPart)tgt).partid() : -1;
   }

   public void grab(MapView.Grabber grab) {
      this.grab = grab;
   }

   public void release(MapView.Grabber grab) {
      if (this.grab == grab) {
         this.grab = null;
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      this.parent.setfocus(this);
      if (button == 1) {
         this.LMBdown = true;
         this.mousemoved = false;
      }

      if ((Config.laptopcontrols || button != 2) && (!Config.laptopcontrols || button != 3 || !this.LMBdown)) {
         if (this.placing != null) {
            if (this.placing.lastmc != null) {
               this.wdgmsg("place", new Object[]{this.placing.rc, (int)(this.placing.a * 180.0 / Math.PI), button, this.ui.modflags()});
            }
         } else if ((this.grab == null || !this.grab.mmousedown(c, button)) && (!Config.laptopcontrols || !this.LMBdown)) {
            this.delay(new MapView.Click(c, button));
         }
      } else if (this.camera.click(c)) {
         this.ui.grabmouse(this);
         this.camdrag = true;
      }

      return true;
   }

   @Override
   public void mousemove(Coord c) {
      if (this.grab != null) {
         this.grab.mmousemove(c);
      }

      if (this.camdrag) {
         this.camera.drag(c);
         this.mousemoved = true;
      } else if (this.placing != null && (this.placing.lastmc == null || !this.placing.lastmc.equals(c))) {
         this.delay(this.placing.new Adjust(c, !this.ui.modctrl));
      }
   }

   @Override
   public boolean mouseup(Coord c, int button) {
      if (!Config.laptopcontrols && button == 2 || Config.laptopcontrols && button == 3 && this.camdrag) {
         if (this.camdrag) {
            this.camera.release();
            this.ui.grabmouse(null);
            this.camdrag = false;
         }
      } else if (this.grab != null) {
         this.grab.mmouseup(c, button);
      } else if (Config.laptopcontrols && this.LMBdown && button == 1 && !this.mousemoved && this.placing == null) {
         this.delay(new MapView.Click(c, button));
      }

      if (button == 1) {
         this.LMBdown = false;
      }

      return true;
   }

   @Override
   public boolean mousewheel(Coord c, int amount) {
      if (this.grab != null && this.grab.mmousewheel(c, amount)) {
         return true;
      } else if (!this.ui.modshift) {
         return this.camera.wheel(c, amount);
      } else {
         if (this.placing != null) {
            this.placing.freerot = true;
            if (!this.ui.modctrl && (!Config.laptopcontrols || !this.ui.modmeta)) {
               this.placing.a = (Math.PI / 4) * Math.round((this.placing.a + amount * Math.PI / 4.0) / (Math.PI / 4));
            } else {
               this.placing.a += amount * Math.PI / 16.0;
            }
         }

         return true;
      }
   }

   @Override
   public boolean drop(Coord cc, Coord ul) {
      this.delay(new MapView.Hittest(cc) {
         @Override
         public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
            MapView.this.wdgmsg("drop", new Object[]{pc, mc, MapView.this.ui.modflags()});
         }
      });
      return true;
   }

   @Override
   public boolean iteminteract(Coord cc, Coord ul) {
      this.delay(new MapView.Hittest(cc) {
         @Override
         public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
            if (inf == null) {
               MapView.this.wdgmsg("itemact", new Object[]{pc, mc, MapView.this.ui.modflags()});
            } else {
               MapView.this.wdgmsg("itemact", new Object[]{pc, mc, MapView.this.ui.modflags(), (int)inf.gob.id, inf.gob.rc, MapView.getid(inf.r)});
            }
         }
      });
      return true;
   }

   @Override
   public boolean globtype(char c, KeyEvent ev) {
      if (Config.laptopcontrols) {
         if (c == '+') {
            this.mousewheel(this.cc, -1);
         } else if (c == '-') {
            this.mousewheel(this.cc, 1);
         }
      }

      return false;
   }

   public void setcam(String cam) {
      try {
         Class<? extends MapView.Camera> camtype = camtypes.get(cam);
         if (camtype == null) {
            camtype = camtypes.values().iterator().next();
         }

         Constructor<? extends MapView.Camera> constructor = camtype.getConstructor(MapView.class);
         this.camera = Utils.construct(constructor, this);
      } catch (NoSuchMethodException var4) {
         var4.printStackTrace(System.out);
      } catch (SecurityException var5) {
         var5.printStackTrace(System.out);
      } catch (IllegalArgumentException var6) {
         var6.printStackTrace(System.out);
      }
   }

   @Override
   public Map<String, Console.Command> findcmds() {
      return this.cmdmap;
   }

   static {
      camtypes.put("best", MapView.FreeCam.class);
      camtypes.put("sucky", MapView.SFreeCam.class);
   }

   @Widget.RName("mapview")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         Coord sz = (Coord)args[0];
         Coord mc = (Coord)args[1];
         int pgob = -1;
         if (args.length > 2) {
            pgob = (Integer)args[2];
         }

         return new MapView(c, sz, parent, mc, pgob);
      }
   }

   public abstract static class Camera extends GLState.Abstract {
      protected haven.Camera view = new haven.Camera(Matrix4f.identity());
      protected Projection proj = new Projection(Matrix4f.identity());
      protected MapView mv;

      public Camera(MapView mv) {
         this.mv = mv;
         this.resized();
      }

      public boolean click(Coord sc) {
         return false;
      }

      public void drag(Coord sc) {
      }

      public void release() {
      }

      public boolean wheel(Coord sc, int amount) {
         return false;
      }

      public void fixangle() {
      }

      public void resized() {
         float field = Config.camera_field_of_view;
         float aspect = (float)this.mv.sz.y / this.mv.sz.x;
         this.proj.update(Projection.makefrustum(new Matrix4f(), -field, field, -aspect * field, aspect * field, 1.0F, 5000.0F));
      }

      @Override
      public void prep(GLState.Buffer buf) {
         this.proj.prep(buf);
         this.view.prep(buf);
      }

      public abstract float angle();

      public abstract void tick(double var1);
   }

   private class Click extends MapView.Hittest {
      int clickb;

      private Click(Coord c, int b) {
         super(c);
         this.clickb = b;
      }

      @Override
      protected void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
         int modflags = MapView.this.ui.modflags();
         if (inf == null) {
            if (Config.center) {
               mc = mc.div(11).mul(11).add(5, 5);
            }

            MapView.this.wdgmsg("click", new Object[]{pc, mc, this.clickb, modflags});
         } else {
            if (MapView.this.ui.modmeta) {
               ChatUI.Channel channel = MapView.this.ui.gui.chat.sel;
               if (channel != null && channel instanceof ChatUI.EntryChannel) {
                  ((ChatUI.EntryChannel)channel).send(String.format("$hl[%d]", inf.gob.id));
               }
            }

            if (inf.ol == null) {
               MapView.this.wdgmsg("click", new Object[]{pc, mc, this.clickb, modflags, 0, (int)inf.gob.id, inf.gob.rc, 0, MapView.getid(inf.r)});
            } else {
               MapView.this.wdgmsg("click", new Object[]{pc, mc, this.clickb, modflags, 1, (int)inf.gob.id, inf.gob.rc, inf.ol.id, MapView.getid(inf.r)});
            }
         }
      }
   }

   public static class ClickInfo {
      Gob gob;
      Gob.Overlay ol;
      Rendered r;

      ClickInfo(Gob gob, Gob.Overlay ol, Rendered r) {
         this.gob = gob;
         this.ol = ol;
         this.r = r;
      }
   }

   private abstract static class Clicklist<T> extends RenderList {
      private Map<Color, T> rmap = new HashMap<>();
      private int i = 1;
      private GLState.Buffer plain;
      private GLState.Buffer bk;

      protected abstract T map(Rendered var1);

      private Clicklist(GLState.Buffer plain) {
         super(plain.cfg);
         this.plain = plain;
         this.bk = new GLState.Buffer(plain.cfg);
      }

      protected Color newcol(T t) {
         int cr = (this.i & 15) << 4 | (this.i & 61440) >> 12;
         int cg = (this.i & 240) << 0 | (this.i & 983040) >> 16;
         int cb = (this.i & 3840) >> 4 | (this.i & 15728640) >> 20;
         Color col = new Color(cr, cg, cb);
         this.i++;
         this.rmap.put(col, t);
         return col;
      }

      @Override
      protected void render(GOut g, Rendered r) {
         try {
            if (r instanceof FRendered) {
               ((FRendered)r).drawflat(g);
            }
         } catch (RenderList.RLoad var4) {
            if (!this.ignload) {
               throw var4;
            }
         }
      }

      public T get(GOut g, Coord c) {
         return this.rmap.get(g.getpixel(c));
      }

      @Override
      protected void setup(RenderList.Slot s, Rendered r) {
         T t = this.map(r);
         super.setup(s, r);
         s.os.copy(this.bk);
         this.plain.copy(s.os);
         this.bk.copy(s.os, GLState.Slot.Type.GEOM);
         if (t != null) {
            Color col = this.newcol(t);
            new States.ColState(col).prep(s.os);
         }
      }
   }

   public interface Delayed {
      void run(GOut var1);
   }

   private static class FollowCam extends MapView.Camera {
      private final float fr = 0.0F;
      private final float h = 10.0F;
      private float ca;
      private float cd;
      private Coord3f curc = null;
      private float elev = (float) (Math.PI / 6);
      private float angl = 0.0F;
      private Coord dragorig = null;
      private float anglorig;
      private double f0 = 0.2;
      private double f1 = 0.5;
      private double f2 = 0.9;
      private double fl = Math.sqrt(2.0);
      private double fa = (this.fl * (this.f1 - this.f0) - (this.f2 - this.f0)) / (this.fl - 2.0);
      private double fb = (this.f2 - this.f0 - 2.0 * (this.f1 - this.f0)) / (this.fl - 2.0);
      private static final float maxang = 1.4707963F;
      private static final float mindist = 10.0F;

      public FollowCam(MapView mv) {
         super(mv);
      }

      @Override
      public void resized() {
         this.ca = (float)this.mv.sz.y / this.mv.sz.x;
         this.cd = 400.0F * this.ca;
      }

      @Override
      public boolean click(Coord c) {
         this.anglorig = this.angl;
         this.dragorig = c;
         return true;
      }

      @Override
      public void drag(Coord c) {
         this.angl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
         this.angl %= (float) (Math.PI * 2);
      }

      private float field(float elev) {
         double a = elev / (Math.PI / 4);
         return (float)(this.f0 + this.fa * a + this.fb * Math.sqrt(a));
      }

      private float dist(float elev) {
         float da = (float)Math.atan(this.ca * this.field(elev));
         return (float)((this.cd - 10.0 / Math.tan(elev)) * Math.sin(elev - da) / Math.sin(da) - 10.0 / Math.sin(elev));
      }

      @Override
      public void tick(double dt) {
         Coord3f cc = this.mv.getcc();
         cc.y = -cc.y;
         if (this.curc == null) {
            this.curc = cc;
         }

         float dx = cc.x - this.curc.x;
         float dy = cc.y - this.curc.y;
         if (Math.sqrt(dx * dx + dy * dy) > 0.0) {
            Coord3f oc = this.curc;
            float pd = (float)Math.cos(this.elev) * this.dist(this.elev);
            Coord3f cambase = new Coord3f(this.curc.x + (float)Math.cos(this.angl) * pd, this.curc.y + (float)Math.sin(this.angl) * pd, 0.0F);
            float a = cc.xyangle(this.curc);
            float nx = cc.x + (float)Math.cos(a) * 0.0F;
            float ny = cc.y + (float)Math.sin(a) * 0.0F;
            this.curc = new Coord3f(nx, ny, cc.z);
            this.angl = this.curc.xyangle(cambase);
         }

         float field = this.field(this.elev);
         this.view.update(PointedCam.compute(this.curc.add(0.0F, 0.0F, 10.0F), this.dist(this.elev), this.elev, this.angl));
         this.proj.update(Projection.makefrustum(new Matrix4f(), -field, field, -this.ca * field, this.ca * field, 1.0F, 5000.0F));
      }

      @Override
      public float angle() {
         return this.angl;
      }

      @Override
      public boolean wheel(Coord c, int amount) {
         float fe = this.elev;
         this.elev = this.elev + amount * this.elev * 0.02F;
         if (this.elev > 1.4707963F) {
            this.elev = 1.4707963F;
         }

         if (this.dist(this.elev) < 10.0F) {
            this.elev = fe;
         }

         return true;
      }
   }

   private static class FreeCam extends MapView.Camera {
      private float dist = 500.0F;
      private float elev = (float) (Math.PI / 4);
      private float angl = 0.0F;
      private Coord dragorig = null;
      private float elevorig;
      private float anglorig;

      public FreeCam(MapView mv) {
         super(mv);
      }

      @Override
      public void tick(double dt) {
         Coord3f cc = this.mv.getcc();
         cc.y = -cc.y;
         this.view.update(PointedCam.compute(cc.add(0.0F, 0.0F, 15.0F), this.dist, this.elev, this.angl));
      }

      @Override
      public float angle() {
         return this.angl;
      }

      @Override
      public boolean click(Coord c) {
         this.elevorig = this.elev;
         this.anglorig = this.angl;
         this.dragorig = c;
         return true;
      }

      @Override
      public void drag(Coord c) {
         this.elev = this.elevorig - (c.y - this.dragorig.y) / 100.0F;
         if (this.elev < 0.0F) {
            this.elev = 0.0F;
         }

         if (this.elev > Math.PI / 2) {
            this.elev = (float) (Math.PI / 2);
         }

         this.angl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
         this.angl %= (float) (Math.PI * 2);
      }

      @Override
      public boolean wheel(Coord c, int amount) {
         float d = this.dist + amount * 10;
         if (d < 5.0F) {
            d = 5.0F;
         }

         this.dist = d;
         return true;
      }
   }

   public class GrabXL implements MapView.Grabber {
      private final MapView.Grabber bk;
      public boolean mv = false;

      public GrabXL(MapView.Grabber bk) {
         this.bk = bk;
      }

      @Override
      public boolean mmousedown(Coord cc, final int button) {
         MapView.this.delay(new MapView.Hittest(cc) {
            @Override
            public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
               GrabXL.this.bk.mmousedown(mc, button);
            }
         });
         return true;
      }

      @Override
      public boolean mmouseup(Coord cc, final int button) {
         MapView.this.delay(new MapView.Hittest(cc) {
            @Override
            public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
               GrabXL.this.bk.mmouseup(mc, button);
            }
         });
         return true;
      }

      @Override
      public boolean mmousewheel(Coord cc, final int amount) {
         MapView.this.delay(new MapView.Hittest(cc) {
            @Override
            public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
               GrabXL.this.bk.mmousewheel(mc, amount);
            }
         });
         return true;
      }

      @Override
      public void mmousemove(Coord cc) {
         if (this.mv) {
            MapView.this.delay(new MapView.Hittest(cc) {
               @Override
               public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
                  GrabXL.this.bk.mmousemove(mc);
               }
            });
         }
      }
   }

   public interface Grabber {
      boolean mmousedown(Coord var1, int var2);

      boolean mmouseup(Coord var1, int var2);

      boolean mmousewheel(Coord var1, int var2);

      void mmousemove(Coord var1);
   }

   public abstract class Hittest implements MapView.Delayed {
      private final Coord clickc;

      public Hittest(Coord c) {
         this.clickc = c;
      }

      @Override
      public void run(GOut g) {
         GLState.Buffer bk = g.st.copy();

         Coord mapcl;
         MapView.ClickInfo gobcl;
         try {
            GL gl = g.gl;
            g.st.set(MapView.this.clickbasic(g));
            g.apply();
            gl.glClear(16640);
            mapcl = MapView.this.checkmapclick(g, this.clickc);
            g.st.set(bk);
            g.st.set(MapView.this.clickbasic(g));
            g.apply();
            gl.glClear(16384);
            gobcl = MapView.this.checkgobclick(g, this.clickc);
         } finally {
            g.st.set(bk);
         }

         if (mapcl != null) {
            if (gobcl == null) {
               this.hit(this.clickc, mapcl, null);
            } else {
               this.hit(this.clickc, mapcl, gobcl);
            }
         } else {
            this.nohit(this.clickc);
         }
      }

      protected abstract void hit(Coord var1, Coord var2, MapView.ClickInfo var3);

      protected void nohit(Coord pc) {
      }
   }

   private static class Maplist extends MapView.Clicklist<MapMesh> {
      private int mode = 0;
      private MapMesh limit = null;

      private Maplist(GLState.Buffer plain) {
         super(plain);
      }

      protected MapMesh map(Rendered r) {
         return r instanceof MapMesh ? (MapMesh)r : null;
      }

      @Override
      protected void render(GOut g, Rendered r) {
         if (r instanceof MapMesh) {
            MapMesh m = (MapMesh)r;
            if (this.mode != 0) {
               g.state(States.vertexcolor);
            }

            if (this.limit == null || this.limit == m) {
               m.drawflat(g, this.mode);
            }
         }
      }
   }

   public abstract class Maptest implements MapView.Delayed {
      private final Coord pc;

      public Maptest(Coord c) {
         this.pc = c;
      }

      @Override
      public void run(GOut g) {
         GLState.Buffer bk = g.st.copy();

         Coord mc;
         try {
            GL gl = g.gl;
            g.st.set(MapView.this.clickbasic(g));
            g.apply();
            gl.glClear(16640);
            mc = MapView.this.checkmapclick(g, this.pc);
         } finally {
            g.st.set(bk);
         }

         if (mc != null) {
            this.hit(this.pc, mc);
         } else {
            this.nohit(this.pc);
         }
      }

      protected abstract void hit(Coord var1, Coord var2);

      protected void nohit(Coord pc) {
      }
   }

   private static class OrthoCam extends MapView.Camera {
      public static final float DEFANGLE = (float) (-Math.PI / 4);
      protected float dist = 500.0F;
      protected float elev = (float) (Math.PI / 6);
      protected float angl = (float) (-Math.PI / 4);
      protected float field = (float)(100.0 * Math.sqrt(2.0));
      private Coord dragorig = null;
      private float anglorig;
      protected Coord3f cc;

      public OrthoCam(MapView mv) {
         super(mv);
      }

      public void tick2(double dt) {
         Coord3f cc = this.mv.getcc();
         cc.y = -cc.y;
         this.cc = cc;
      }

      @Override
      public void tick(double dt) {
         this.tick2(dt);
         float aspect = (float)this.mv.sz.y / this.mv.sz.x;
         this.view.update(PointedCam.compute(this.cc.add(0.0F, 0.0F, 15.0F), this.dist, this.elev, this.angl));
         this.proj.update(Projection.makeortho(new Matrix4f(), -this.field, this.field, -this.field * aspect, this.field * aspect, 1.0F, 5000.0F));
      }

      @Override
      public float angle() {
         return this.angl;
      }

      @Override
      public boolean click(Coord c) {
         this.anglorig = this.angl;
         this.dragorig = c;
         return true;
      }

      @Override
      public void fixangle() {
         this.angl = this.stepify(this.angl - (float) (-Math.PI / 4)) + (float) (-Math.PI / 4);
      }

      protected float stepify(float a) {
         if (Config.isocam_steps) {
            a = (float)Math.round(2.0F * a / Math.PI);
            a = (float)(a * Math.PI / 2.0);
         }

         return a;
      }

      @Override
      public void drag(Coord c) {
         float delta = this.stepify((c.x - this.dragorig.x) / 100.0F);
         this.angl = this.anglorig + delta;
         this.angl %= (float) (Math.PI * 2);
      }

      @Override
      public boolean wheel(Coord c, int amount) {
         this.field += amount * 10;
         this.field = Math.max(Math.min(this.field, 500.0F), 50.0F);
         return true;
      }

      @Override
      public String toString() {
         return String.format("%f %f %f %f", this.dist, this.elev / Math.PI, this.angl / Math.PI, this.field);
      }
   }

   private class Plob extends Gob {
      Coord lastmc = null;
      boolean freerot = false;

      private Plob(Indir<Resource> res, Message sdt) {
         super(MapView.this.glob, Coord.z);
         this.setattr(new ResDrawable(this, res, sdt));
         if (MapView.this.ui.mc.isect(MapView.this.rootpos(), MapView.this.sz)) {
            MapView.this.delay(new MapView.Plob.Adjust(MapView.this.ui.mc.sub(MapView.this.rootpos()), false));
         }
      }

      private class Adjust extends MapView.Maptest {
         boolean adjust;

         Adjust(Coord c, boolean ta) {
            super(c);
            this.adjust = ta;
         }

         @Override
         public void hit(Coord pc, Coord mc) {
            Plob.this.rc = mc;
            if (this.adjust) {
               Plob.this.rc = Plob.this.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2));
            }

            Gob pl = MapView.this.player();
            if (pl != null && !Plob.this.freerot) {
               Plob.this.a = Plob.this.rc.angle(pl.rc);
            }

            Plob.this.lastmc = pc;
         }
      }
   }

   private static class SFreeCam extends MapView.Camera {
      private float dist = 50.0F;
      private float tdist = this.dist;
      private float elev = (float) (Math.PI / 4);
      private float telev = this.elev;
      private float angl = 0.0F;
      private float tangl = this.angl;
      private Coord dragorig = null;
      private float elevorig;
      private float anglorig;
      private final float pi2 = (float) (Math.PI * 2);
      private Coord3f cc = null;

      public SFreeCam(MapView mv) {
         super(mv);
      }

      @Override
      public void tick(double dt) {
         for (this.angl = this.angl + (this.tangl - this.angl) * (1.0F - (float)Math.pow(500.0, -dt));
            this.angl > (float) (Math.PI * 2);
            this.anglorig -= (float) (Math.PI * 2)
         ) {
            this.angl -= (float) (Math.PI * 2);
            this.tangl -= (float) (Math.PI * 2);
         }

         while (this.angl < 0.0F) {
            this.angl += (float) (Math.PI * 2);
            this.tangl += (float) (Math.PI * 2);
            this.anglorig += (float) (Math.PI * 2);
         }

         if (Math.abs(this.tangl - this.angl) < 1.0E-4) {
            this.angl = this.tangl;
         }

         this.elev = this.elev + (this.telev - this.elev) * (1.0F - (float)Math.pow(500.0, -dt));
         if (Math.abs(this.telev - this.elev) < 1.0E-4) {
            this.elev = this.telev;
         }

         this.dist = this.dist + (this.tdist - this.dist) * (1.0F - (float)Math.pow(500.0, -dt));
         if (Math.abs(this.tdist - this.dist) < 1.0E-4) {
            this.dist = this.tdist;
         }

         Coord3f mc = this.mv.getcc();
         mc.y = -mc.y;
         if (this.cc != null && !(Math.hypot(mc.x - this.cc.x, mc.y - this.cc.y) > 250.0)) {
            this.cc = this.cc.add(mc.sub(this.cc).mul(1.0F - (float)Math.pow(500.0, -dt)));
         } else {
            this.cc = mc;
         }

         this.view.update(PointedCam.compute(this.cc.add(0.0F, 0.0F, 15.0F), this.dist, this.elev, this.angl));
      }

      @Override
      public float angle() {
         return this.angl;
      }

      @Override
      public boolean click(Coord c) {
         this.elevorig = this.elev;
         this.anglorig = this.angl;
         this.dragorig = c;
         return true;
      }

      @Override
      public void drag(Coord c) {
         this.telev = this.elevorig - (c.y - this.dragorig.y) / 100.0F;
         if (this.telev < 0.0F) {
            this.telev = 0.0F;
         }

         if (this.telev > Math.PI / 2) {
            this.telev = (float) (Math.PI / 2);
         }

         this.tangl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
      }

      @Override
      public boolean wheel(Coord c, int amount) {
         float d = this.tdist + amount * 5;
         if (d < 5.0F) {
            d = 5.0F;
         }

         this.tdist = d;
         return true;
      }
   }

   private static class SOrthoCam extends MapView.OrthoCam {
      private Coord dragorig = null;
      private float anglorig;
      private float tangl = this.angl;
      private float tfield = this.field;
      private final float pi2 = (float) (Math.PI * 2);

      public SOrthoCam(MapView mv) {
         super(mv);
      }

      @Override
      public void tick2(double dt) {
         Coord3f mc = this.mv.getcc();
         mc.y = -mc.y;
         if (this.cc != null && !(Math.hypot(mc.x - this.cc.x, mc.y - this.cc.y) > 250.0)) {
            this.cc = this.cc.add(mc.sub(this.cc).mul(1.0F - (float)Math.pow(500.0, -dt)));
         } else {
            this.cc = mc;
         }

         for (this.angl = this.angl + (this.tangl - this.angl) * (1.0F - (float)Math.pow(500.0, -dt));
            this.angl > (float) (Math.PI * 2);
            this.anglorig -= (float) (Math.PI * 2)
         ) {
            this.angl -= (float) (Math.PI * 2);
            this.tangl -= (float) (Math.PI * 2);
         }

         while (this.angl < 0.0F) {
            this.angl += (float) (Math.PI * 2);
            this.tangl += (float) (Math.PI * 2);
            this.anglorig += (float) (Math.PI * 2);
         }

         if (Math.abs(this.tangl - this.angl) < 1.0E-4) {
            this.angl = this.tangl %= (float) (Math.PI * 2);
         }

         this.field = this.field + (this.tfield - this.field) * (1.0F - (float)Math.pow(500.0, -dt));
         if (Math.abs(this.tfield - this.field) < 1.0E-4) {
            this.field = this.tfield;
         }
      }

      @Override
      public boolean click(Coord c) {
         this.anglorig = this.angl;
         this.dragorig = c;
         return true;
      }

      @Override
      public void fixangle() {
         this.tangl = this.stepify(this.tangl - (float) (-Math.PI / 4)) + (float) (-Math.PI / 4);
      }

      @Override
      public void drag(Coord c) {
         float delta = this.stepify((c.x - this.dragorig.x) / 100.0F);
         this.tangl = this.anglorig + delta;
      }

      @Override
      public boolean wheel(Coord c, int amount) {
         this.tfield += amount * 10;
         this.tfield = Math.max(Math.min(this.tfield, 400.0F), 50.0F);
         return true;
      }
   }

   private static class SmoothFollowCam extends MapView.Camera {
      private final float fr = 0.0F;
      private final float h = 10.0F;
      private float ca;
      private float cd;
      private float da;
      private Coord3f curc = null;
      private float elev;
      private float telev;
      private float angl;
      private float tangl;
      private Coord dragorig = null;
      private float anglorig;
      private double f0 = 0.2;
      private double f1 = 0.5;
      private double f2 = 0.9;
      private double fl = Math.sqrt(2.0);
      private double fa = (this.fl * (this.f1 - this.f0) - (this.f2 - this.f0)) / (this.fl - 2.0);
      private double fb = (this.f2 - this.f0 - 2.0 * (this.f1 - this.f0)) / (this.fl - 2.0);
      private static final float maxang = 1.4707963F;
      private static final float mindist = 50.0F;

      public SmoothFollowCam(MapView mv) {
         super(mv);
         this.elev = this.telev = (float) (Math.PI / 6);
         this.angl = this.tangl = 0.0F;
      }

      @Override
      public void resized() {
         this.ca = (float)this.mv.sz.y / this.mv.sz.x;
         this.cd = 400.0F * this.ca;
      }

      @Override
      public boolean click(Coord c) {
         this.anglorig = this.tangl;
         this.dragorig = c;
         return true;
      }

      @Override
      public void drag(Coord c) {
         this.tangl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
         this.tangl %= (float) (Math.PI * 2);
      }

      private float field(float elev) {
         double a = elev / (Math.PI / 4);
         return (float)(this.f0 + this.fa * a + this.fb * Math.sqrt(a));
      }

      private float dist(float elev) {
         float da = (float)Math.atan(this.ca * this.field(elev));
         return (float)((this.cd - 10.0 / Math.tan(elev)) * Math.sin(elev - da) / Math.sin(da) - 10.0 / Math.sin(elev));
      }

      @Override
      public void tick(double dt) {
         this.elev = this.elev + (this.telev - this.elev) * (float)(1.0 - Math.pow(500.0, -dt));
         if (Math.abs(this.telev - this.elev) < 1.0E-4) {
            this.elev = this.telev;
         }

         float dangl = this.tangl - this.angl;

         while (dangl > Math.PI) {
            dangl -= (float) (Math.PI * 2);
         }

         while (dangl < -Math.PI) {
            dangl += (float) (Math.PI * 2);
         }

         this.angl = this.angl + dangl * (float)(1.0 - Math.pow(500.0, -dt));
         if (Math.abs(this.tangl - this.angl) < 1.0E-4) {
            this.angl = this.tangl;
         }

         Coord3f cc = this.mv.getcc();
         cc.y = -cc.y;
         if (this.curc == null) {
            this.curc = cc;
         }

         float dx = cc.x - this.curc.x;
         float dy = cc.y - this.curc.y;
         float dist = (float)Math.sqrt(dx * dx + dy * dy);
         if (dist > 250.0F) {
            this.curc = cc;
         } else if (dist > 0.0F) {
            Coord3f oc = this.curc;
            float pd = (float)Math.cos(this.elev) * this.dist(this.elev);
            Coord3f cambase = new Coord3f(this.curc.x + (float)Math.cos(this.tangl) * pd, this.curc.y + (float)Math.sin(this.tangl) * pd, 0.0F);
            float a = cc.xyangle(this.curc);
            float nx = cc.x + (float)Math.cos(a) * 0.0F;
            float ny = cc.y + (float)Math.sin(a) * 0.0F;
            Coord3f tgtc = new Coord3f(nx, ny, cc.z);
            this.curc = this.curc.add(tgtc.sub(this.curc).mul((float)(1.0 - Math.pow(500.0, -dt))));
            if (this.curc.dist(tgtc) < 0.01) {
               this.curc = tgtc;
            }

            this.tangl = this.curc.xyangle(cambase);
         }

         float field = this.field(this.elev);
         this.view.update(PointedCam.compute(this.curc.add(0.0F, 0.0F, 10.0F), this.dist(this.elev), this.elev, this.angl));
         this.proj.update(Projection.makefrustum(new Matrix4f(), -field, field, -this.ca * field, this.ca * field, 1.0F, 5000.0F));
      }

      @Override
      public float angle() {
         return this.angl;
      }

      @Override
      public boolean wheel(Coord c, int amount) {
         float fe = this.telev;
         this.telev = this.telev + amount * this.telev * 0.02F;
         if (this.telev > 1.4707963F) {
            this.telev = 1.4707963F;
         }

         if (this.dist(this.telev) < 50.0F) {
            this.telev = fe;
         }

         return true;
      }

      @Override
      public String toString() {
         return String.format("%f %f %f", this.elev, this.dist(this.elev), this.field(this.elev));
      }
   }
}
