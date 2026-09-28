package haven;

import haven.glsl.MiscLib;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.minimap.Radar;
import haven.resutil.GroundTile;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.media.opengl.GL2;
import org.ender.timer.Timer;

public class MapView extends PView implements DTarget, Console.Directory {
  public static final String DEFCAM = "sortho";
  
  private final R2DWdg r2dwdg;
  
  public long plgob = -1L;
  
  public Coord cc;
  
  private final Glob glob;
  
  private int view = 2;
  
  private final Collection<Delayed> delayed = new LinkedList<>();
  
  private final Collection<Delayed> delayed2 = new LinkedList<>();
  
  private final Collection<Rendered> extradraw = new LinkedList<>();
  
  public Camera camera;
  
  private Plob placing = null;
  
  private final int[] visol = new int[32];
  
  private Grabber grab;
  
  public static Integer distanceModX = null;
  
  public static Integer angleModX = null;
  
  public static Integer elevModX = null;
  
  public static Integer scrollModX = null;
  
  public static boolean invOne = false;
  
  public static boolean invAll = false;
  
  public static boolean startCountOnce = false;
  
  public static boolean stopCounting = false;
  
  public static boolean countIsRunning = false;
  
  public int dist = 0;
  
  public ArrayList<Integer> overlayCount = new ArrayList<>();
  
  public static boolean singleRad = false;
  
  private static boolean mineHelper = true;
  
  private static boolean mineHelperFastMode = false;
  
  public static boolean smallView = Config.mview_dist_small;
  
  public static boolean debugtest = false;
  
  public static long lastShiftRightClick = 0L;
  
  private static boolean lastClickWasItemAct = false;
  
  public static int ffThread = 0;
  
  public static boolean signalToStop = false;
  
  private static Gob ffTarget = null;
  
  String[] SHOVELS = new String[] { "Undertaker Shovel", "Steel Shovel", "Metal Shovel", "Wooden Shovel" };
  
  String[] SHOVELSRESNAME = new String[] { "invobjs/undertakershovel", "invobjs/steelshovel", "invobjs/shovel", "invobjs/wshovel" };
  
  LinkedHashMap<String[], List<String>> gobActionMap = (LinkedHashMap)new LinkedHashMap<>();
  
  public static boolean output = false;
  
  public static Coord in1 = null;
  
  public static Coord in2 = null;
  
  public static Coord out1 = null;
  
  public static Coord out2 = null;
  
  private boolean customItemActThreadActive;
  
  public boolean customItemActStop;
  
  private final ArrayList<String> biomeNames;
  
  public static boolean hidePlayer = false;
  
  private static int o1 = 0;
  
  private static int o2 = 0;
  
  private static int placeGrid = 0;
  
  private static long ts = 0L;
  
  private static Boolean tim = Boolean.valueOf(false);
  
  private boolean wasOn;
  
  private static GItem lastShovel = null;
  
  private static boolean thereWasNoShovel = false;
  
  public static double scaleForMarkers = 1.0D;
  
  public static final Map<String, Class<? extends Camera>> camtypes = new HashMap<>();
  
  private final Rendered map;
  
  public static final int WFOL = 18;
  
  public static abstract class Camera extends GLState.Abstract {
    protected Camera view = new Camera(Matrix4f.identity());
    
    protected Projection proj = new Projection(Matrix4f.identity());
    
    protected MapView mv;
    
    public static int angleModC = 0;
    
    public static int elevModC = 0;
    
    public static int distModC = 0;
    
    public static int scrollModC = 0;
    
    public static boolean usesElevMod = false;
    
    public static final String ANGLE_STR = "ANGLE_STR";
    
    public static final String ELEV_STR = "ELEV_STR";
    
    public static final String DIST_STR = "DIST_STR";
    
    public static final String SCROLL_STR = "SCROLL_STR";
    
    public static void getValues(String camName) {
      presetCamValues();
      int defAngleMod = (MapView.angleModX != null) ? MapView.angleModX.intValue() : Utils.getprefi("ANGLE_STR" + camName, 0);
      int defElevMod = (MapView.elevModX != null) ? MapView.elevModX.intValue() : Utils.getprefi("ELEV_STR" + camName, 10);
      int defDistMod = (MapView.distanceModX != null) ? MapView.distanceModX.intValue() : Utils.getprefi("DIST_STR" + camName, 500);
      int defScrollMod = (MapView.scrollModX != null) ? MapView.scrollModX.intValue() : Utils.getprefi("SCROLL_STR" + camName, 1);
      angleModC = defAngleMod;
      elevModC = defElevMod;
      distModC = defDistMod;
      scrollModC = defScrollMod;
      MapView.angleModX = null;
      MapView.elevModX = null;
      MapView.distanceModX = null;
      MapView.scrollModX = null;
    }
    
    private static void presetCamValues() {
      if (Utils.getprefb("preset_cam_setup_run_once", true)) {
        Utils.setprefb("preset_cam_setup_run_once", false);
        String camName = "farout";
        Utils.setprefi("ANGLE_STR" + camName, 40);
        Utils.setprefi("ELEV_STR" + camName, 30);
        Utils.setprefi("DIST_STR" + camName, 1750);
        Utils.setprefi("SCROLL_STR" + camName, 10);
        camName = "sortho";
        Utils.setprefi("DIST_STR" + camName, 300);
        camName = "ortho";
        Utils.setprefi("DIST_STR" + camName, 300);
      } 
    }
    
    public static void saveValues(String camName) {
      Utils.setprefi("ANGLE_STR" + camName, angleModC);
      Utils.setprefi("ELEV_STR" + camName, elevModC);
      Utils.setprefi("DIST_STR" + camName, distModC);
      Utils.setprefi("SCROLL_STR" + camName, scrollModC);
    }
    
    public Camera(MapView mv) {
      this.mv = mv;
      resized();
      String camName = Utils.getpref("defcam", "sortho");
      getValues(camName);
      usesElevMod = true;
    }
    
    public boolean click(Coord sc) {
      return false;
    }
    
    public void drag(Coord sc) {}
    
    public void release() {}
    
    public boolean wheel(Coord sc, int amount) {
      return false;
    }
    
    public void fixangle() {}
    
    public void resized() {
      float field = Config.camera_field_of_view;
      float aspect = this.mv.sz.y / this.mv.sz.x;
      this.proj.update(Projection.makefrustum(new Matrix4f(), -field, field, -aspect * field, aspect * field, 1.0F, 5000.0F));
    }
    
    public void prep(GLState.Buffer buf) {
      this.proj.prep(buf);
      this.view.prep(buf);
    }
    
    public abstract float angle();
    
    public abstract void tick(double param1Double);
  }
  
  private static class FollowCam extends Camera {
    private final float fr = 0.0F;
    
    private final float h = 10.0F;
    
    private float ca;
    
    private float cd;
    
    private Coord3f curc;
    
    private float elev;
    
    private float angl;
    
    private Coord dragorig;
    
    private float anglorig;
    
    private final double f0 = 0.2D;
    
    private final double f1 = 0.5D;
    
    private final double f2 = 0.9D;
    
    private final double fl;
    
    private final double fa;
    
    private final double fb;
    
    private static final float maxang = 1.4707963F;
    
    private static final float mindist = 10.0F;
    
    public FollowCam(MapView mv) {
      super(mv);
      this.fr = 0.0F;
      this.h = 10.0F;
      this.curc = null;
      this.elev = 1.5707964F * MapView.Camera.elevModC / 30.0F;
      this.angl = -1.5707964F * (10 + MapView.Camera.angleModC) / 10.0F;
      this.dragorig = null;
      this.f0 = 0.2D;
      this.f1 = 0.5D;
      this.f2 = 0.9D;
      this.fl = Math.sqrt(2.0D);
      getClass();
      getClass();
      getClass();
      getClass();
      this.fa = (this.fl * (0.5D - 0.2D) - 0.9D - 0.2D) / (this.fl - 2.0D);
      getClass();
      getClass();
      getClass();
      getClass();
      this.fb = (0.9D - 0.2D - 2.0D * (0.5D - 0.2D)) / (this.fl - 2.0D);
    }
    
    public void resized() {
      this.ca = this.mv.sz.y / this.mv.sz.x;
      this.cd = MapView.Camera.distModC * this.ca;
    }
    
    public boolean click(Coord c) {
      this.anglorig = this.angl;
      this.dragorig = c;
      return true;
    }
    
    public void drag(Coord c) {
      this.angl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
      this.angl %= 6.2831855F;
    }
    
    private float field(float elev) {
      double a = elev / 0.7853981633974483D;
      getClass();
      return (float)(0.2D + this.fa * a + this.fb * Math.sqrt(a));
    }
    
    private float dist(float elev) {
      float da = (float)Math.atan((this.ca * field(elev)));
      getClass();
      getClass();
      return (float)((this.cd - 10.0D / Math.tan(elev)) * Math.sin((elev - da)) / Math.sin(da) - 10.0D / Math.sin(elev));
    }
    
    public void tick(double dt) {
      Coord3f cc = this.mv.getcc();
      cc.y = -cc.y;
      if (this.curc == null)
        this.curc = cc; 
      float dx = cc.x - this.curc.x, dy = cc.y - this.curc.y;
      getClass();
      if (Math.sqrt((dx * dx + dy * dy)) > 0.0D) {
        Coord3f oc = this.curc;
        float pd = (float)Math.cos(this.elev) * dist(this.elev);
        Coord3f cambase = new Coord3f(this.curc.x + (float)Math.cos(this.angl) * pd, this.curc.y + (float)Math.sin(this.angl) * pd, 0.0F);
        float a = cc.xyangle(this.curc);
        getClass();
        float nx = cc.x + (float)Math.cos(a) * 0.0F;
        getClass();
        float ny = cc.y + (float)Math.sin(a) * 0.0F;
        this.curc = new Coord3f(nx, ny, cc.z);
        this.angl = this.curc.xyangle(cambase);
      } 
      float field = field(this.elev);
      getClass();
      this.view.update(PointedCam.compute(this.curc.add(0.0F, 0.0F, 10.0F), dist(this.elev), this.elev, this.angl));
      this.proj.update(Projection.makefrustum(new Matrix4f(), -field, field, -this.ca * field, this.ca * field, 1.0F, 5000.0F));
    }
    
    public float angle() {
      return this.angl;
    }
    
    public boolean wheel(Coord c, int amount) {
      float fe = this.elev;
      this.elev += amount * this.elev * 0.02F * MapView.Camera.scrollModC;
      if (this.elev > 1.4707963F)
        this.elev = 1.4707963F; 
      if (dist(this.elev) < 10.0F)
        this.elev = fe; 
      return true;
    }
  }
  
  private static class SmoothFollowCam extends Camera {
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
    
    private final double f0 = 0.2D;
    
    private final double f1 = 0.5D;
    
    private final double f2 = 0.9D;
    
    private final double fl;
    
    private final double fa;
    
    private final double fb;
    
    private static final float maxang = 1.4707963F;
    
    private static final float mindist = 50.0F;
    
    public SmoothFollowCam(MapView mv) {
      super(mv);
      this.f0 = 0.2D;
      this.f1 = 0.5D;
      this.f2 = 0.9D;
      this.fl = Math.sqrt(2.0D);
      getClass();
      getClass();
      getClass();
      getClass();
      this.fa = (this.fl * (0.5D - 0.2D) - 0.9D - 0.2D) / (this.fl - 2.0D);
      getClass();
      getClass();
      getClass();
      getClass();
      this.fb = (0.9D - 0.2D - 2.0D * (0.5D - 0.2D)) / (this.fl - 2.0D);
      this.elev = this.telev = 1.5707964F * MapView.Camera.elevModC / 30.0F;
      this.angl = this.tangl = -1.5707964F * (10 + MapView.Camera.angleModC) / 10.0F;
    }
    
    public void resized() {
      this.ca = this.mv.sz.y / this.mv.sz.x;
      this.cd = MapView.Camera.distModC * this.ca;
    }
    
    public boolean click(Coord c) {
      this.anglorig = this.tangl;
      this.dragorig = c;
      return true;
    }
    
    public void drag(Coord c) {
      this.tangl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
      this.tangl %= 6.2831855F;
    }
    
    private float field(float elev) {
      double a = elev / 0.7853981633974483D;
      getClass();
      return (float)(0.2D + this.fa * a + this.fb * Math.sqrt(a));
    }
    
    private float dist(float elev) {
      float da = (float)Math.atan((this.ca * field(elev)));
      getClass();
      getClass();
      return (float)((this.cd - 10.0D / Math.tan(elev)) * Math.sin((elev - da)) / Math.sin(da) - 10.0D / Math.sin(elev));
    }
    
    public void tick(double dt) {
      this.elev += (this.telev - this.elev) * (float)(1.0D - Math.pow(500.0D, -dt));
      if (Math.abs(this.telev - this.elev) < 1.0E-4D)
        this.elev = this.telev; 
      float dangl = this.tangl - this.angl;
      while (dangl > Math.PI)
        dangl -= 6.2831855F; 
      while (dangl < -3.141592653589793D)
        dangl += 6.2831855F; 
      this.angl += dangl * (float)(1.0D - Math.pow(500.0D, -dt));
      if (Math.abs(this.tangl - this.angl) < 1.0E-4D)
        this.angl = this.tangl; 
      Coord3f cc = this.mv.getcc();
      cc.y = -cc.y;
      if (this.curc == null)
        this.curc = cc; 
      float dx = cc.x - this.curc.x, dy = cc.y - this.curc.y;
      float dist = (float)Math.sqrt((dx * dx + dy * dy));
      if (dist > 250.0F) {
        this.curc = cc;
      } else {
        getClass();
        if (dist > 0.0F) {
          Coord3f oc = this.curc;
          float pd = (float)Math.cos(this.elev) * dist(this.elev);
          Coord3f cambase = new Coord3f(this.curc.x + (float)Math.cos(this.tangl) * pd, this.curc.y + (float)Math.sin(this.tangl) * pd, 0.0F);
          float a = cc.xyangle(this.curc);
          getClass();
          float nx = cc.x + (float)Math.cos(a) * 0.0F;
          getClass();
          float ny = cc.y + (float)Math.sin(a) * 0.0F;
          Coord3f tgtc = new Coord3f(nx, ny, cc.z);
          this.curc = this.curc.add(tgtc.sub(this.curc).mul((float)(1.0D - Math.pow(500.0D, -dt))));
          if (this.curc.dist(tgtc) < 0.01D)
            this.curc = tgtc; 
          this.tangl = this.curc.xyangle(cambase);
        } 
      } 
      float field = field(this.elev);
      getClass();
      this.view.update(PointedCam.compute(this.curc.add(0.0F, 0.0F, 10.0F), dist(this.elev), this.elev, this.angl));
      this.proj.update(Projection.makefrustum(new Matrix4f(), -field, field, -this.ca * field, this.ca * field, 1.0F, 5000.0F));
    }
    
    public float angle() {
      return this.angl;
    }
    
    public boolean wheel(Coord c, int amount) {
      float fe = this.telev;
      this.telev += amount * this.telev * 0.02F * MapView.Camera.scrollModC;
      if (this.telev > 1.4707963F)
        this.telev = 1.4707963F; 
      if (dist(this.telev) < 50.0F)
        this.telev = fe; 
      return true;
    }
    
    public String toString() {
      return String.format("%f %f %f", new Object[] { Float.valueOf(this.elev), Float.valueOf(dist(this.elev)), Float.valueOf(field(this.elev)) });
    }
  }
  
  private static class FreeCam extends Camera {
    private float dist;
    
    private float elev;
    
    private float angl;
    
    private Coord dragorig;
    
    private float elevorig;
    
    private float anglorig;
    
    public FreeCam(MapView mv) {
      super(mv);
      this.dist = MapView.Camera.distModC;
      this.elev = 1.5707964F * MapView.Camera.elevModC / 30.0F;
      this.angl = -1.5707964F * (10 + MapView.Camera.angleModC) / 10.0F;
      this.dragorig = null;
    }
    
    public void tick(double dt) {
      Coord3f cc = this.mv.getcc();
      cc.y = -cc.y;
      this.view.update(PointedCam.compute(cc.add(0.0F, 0.0F, 15.0F), this.dist, this.elev, this.angl));
    }
    
    public float angle() {
      return this.angl;
    }
    
    public boolean click(Coord c) {
      this.elevorig = this.elev;
      this.anglorig = this.angl;
      this.dragorig = c;
      return true;
    }
    
    public void drag(Coord c) {
      if (Config.invert_mouse_cam_y) {
        this.elev = this.elevorig + (c.y - this.dragorig.y) / 100.0F;
      } else {
        this.elev = this.elevorig - (c.y - this.dragorig.y) / 100.0F;
      } 
      if (this.elev < 0.0F)
        this.elev = 0.0F; 
      if (this.elev > 1.5707963267948966D)
        this.elev = 1.5707964F; 
      if (Config.invert_mouse_cam_x) {
        this.angl = this.anglorig - (c.x - this.dragorig.x) / 100.0F;
      } else {
        this.angl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
      } 
      this.angl %= 6.2831855F;
    }
    
    public boolean wheel(Coord c, int amount) {
      float d = this.dist + (amount * 20 * MapView.Camera.scrollModC);
      if (d < 5.0F)
        d = 5.0F; 
      this.dist = d;
      return true;
    }
  }
  
  private static class SFreeCam extends Camera {
    private float dist;
    
    private float tdist;
    
    private float elev;
    
    private float telev;
    
    private float angl;
    
    private float tangl;
    
    private Coord dragorig;
    
    private float elevorig;
    
    private float anglorig;
    
    private final float pi2 = 6.2831855F;
    
    private Coord3f cc;
    
    public SFreeCam(MapView mv) {
      super(mv);
      this.tdist = this.dist = MapView.Camera.distModC;
      this.telev = this.elev = 1.5707964F * MapView.Camera.elevModC / 30.0F;
      this.tangl = this.angl = -1.5707964F * (10 + MapView.Camera.angleModC) / 10.0F;
      this.dragorig = null;
      this.pi2 = 6.2831855F;
      this.cc = null;
    }
    
    public void tick(double dt) {
      this.angl += (this.tangl - this.angl) * (1.0F - (float)Math.pow(500.0D, -dt));
      getClass();
      while (this.angl > 6.2831855F) {
        getClass();
        this.angl -= 6.2831855F;
        getClass();
        this.tangl -= 6.2831855F;
        getClass();
        this.anglorig -= 6.2831855F;
      } 
      while (this.angl < 0.0F) {
        getClass();
        this.angl += 6.2831855F;
        getClass();
        this.tangl += 6.2831855F;
        getClass();
        this.anglorig += 6.2831855F;
      } 
      if (Math.abs(this.tangl - this.angl) < 1.0E-4D)
        this.angl = this.tangl; 
      this.elev += (this.telev - this.elev) * (1.0F - (float)Math.pow(500.0D, -dt));
      if (Math.abs(this.telev - this.elev) < 1.0E-4D)
        this.elev = this.telev; 
      this.dist += (this.tdist - this.dist) * (1.0F - (float)Math.pow(500.0D, -dt));
      if (Math.abs(this.tdist - this.dist) < 1.0E-4D)
        this.dist = this.tdist; 
      Coord3f mc = this.mv.getcc();
      mc.y = -mc.y;
      if (this.cc == null || Math.hypot((mc.x - this.cc.x), (mc.y - this.cc.y)) > 250.0D) {
        this.cc = mc;
      } else {
        this.cc = this.cc.add(mc.sub(this.cc).mul(1.0F - (float)Math.pow(500.0D, -dt)));
      } 
      this.view.update(PointedCam.compute(this.cc.add(0.0F, 0.0F, 15.0F), this.dist, this.elev, this.angl));
    }
    
    public float angle() {
      return this.angl;
    }
    
    public boolean click(Coord c) {
      this.elevorig = this.elev;
      this.anglorig = this.angl;
      this.dragorig = c;
      return true;
    }
    
    public void drag(Coord c) {
      if (Config.invert_mouse_cam_y) {
        this.telev = this.elevorig + (c.y - this.dragorig.y) / 100.0F;
      } else {
        this.telev = this.elevorig - (c.y - this.dragorig.y) / 100.0F;
      } 
      if (this.telev < 0.0F)
        this.telev = 0.0F; 
      if (this.telev > 1.5707963267948966D)
        this.telev = 1.5707964F; 
      if (Config.invert_mouse_cam_x) {
        this.tangl = this.anglorig - (c.x - this.dragorig.x) / 100.0F;
      } else {
        this.tangl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
      } 
    }
    
    public boolean wheel(Coord c, int amount) {
      float d = this.tdist + (amount * 10 * MapView.Camera.scrollModC);
      if (d < 5.0F)
        d = 5.0F; 
      this.tdist = d;
      return true;
    }
  }
  
  private static class FSFreeCam extends Camera {
    private float dist;
    
    private float tdist;
    
    private float elev;
    
    private float telev;
    
    private float angl;
    
    private float tangl;
    
    private Coord dragorig;
    
    private float elevorig;
    
    private float anglorig;
    
    private final float pi2 = 6.2831855F;
    
    private Coord3f cc;
    
    public FSFreeCam(MapView mv) {
      super(mv);
      this.tdist = this.dist = MapView.Camera.distModC;
      this.telev = this.elev = 1.5707964F * MapView.Camera.elevModC / 30.0F;
      this.tangl = this.angl = -1.5707964F * (10 + MapView.Camera.angleModC) / 10.0F;
      this.dragorig = null;
      this.pi2 = 6.2831855F;
      this.cc = null;
    }
    
    public void tick(double dt) {
      this.angl += (this.tangl - this.angl) * (1.0F - (float)Math.pow(500.0D, -dt));
      getClass();
      while (this.angl > 6.2831855F) {
        getClass();
        this.angl -= 6.2831855F;
        getClass();
        this.tangl -= 6.2831855F;
        getClass();
        this.anglorig -= 6.2831855F;
      } 
      while (this.angl < 0.0F) {
        getClass();
        this.angl += 6.2831855F;
        getClass();
        this.tangl += 6.2831855F;
        getClass();
        this.anglorig += 6.2831855F;
      } 
      if (Math.abs(this.tangl - this.angl) < 1.0E-4D)
        this.angl = this.tangl; 
      this.elev += (this.telev - this.elev) * (1.0F - (float)Math.pow(500.0D, -dt));
      if (Math.abs(this.telev - this.elev) < 1.0E-4D)
        this.elev = this.telev; 
      this.dist += (this.tdist - this.dist) * (1.0F - (float)Math.pow(500.0D, -dt));
      if (Math.abs(this.tdist - this.dist) < 1.0E-4D)
        this.dist = this.tdist; 
      Coord3f mc = this.mv.getcc();
      mc.y = -mc.y;
      if (this.cc == null || Math.hypot((mc.x - this.cc.x), (mc.y - this.cc.y)) > 250.0D) {
        this.cc = mc;
      } else {
        this.cc = this.cc.add(mc.sub(this.cc).mul(1.0F - (float)Math.pow(500.0D, -dt)));
      } 
      this.view.update(PointedCam.compute(this.cc.add(0.0F, 0.0F, 15.0F), this.dist, this.elev, this.angl));
    }
    
    public float angle() {
      return this.angl;
    }
    
    public boolean click(Coord c) {
      this.elevorig = this.elev;
      this.anglorig = this.angl;
      this.dragorig = c;
      return true;
    }
    
    public void drag(Coord c) {
      if (Config.invert_mouse_cam_y) {
        this.telev = this.elevorig + (c.y - this.dragorig.y) / 100.0F;
      } else {
        this.telev = this.elevorig - (c.y - this.dragorig.y) / 100.0F;
      } 
      if (this.telev < 0.0F)
        this.telev = 0.0F; 
      if (this.telev > 1.5707963267948966D)
        this.telev = 1.5707964F; 
      if (Config.invert_mouse_cam_x) {
        this.tangl = this.anglorig - (c.x - this.dragorig.x) / 100.0F;
      } else {
        this.tangl = this.anglorig + (c.x - this.dragorig.x) / 100.0F;
      } 
    }
    
    public boolean wheel(Coord c, int amount) {
      float d = this.tdist + (amount * 50);
      if (d < 5.0F)
        d = 5.0F; 
      this.tdist = d;
      return true;
    }
  }
  
  static {
    camtypes.put("sucky", SFreeCam.class);
    camtypes.put("farout", FSFreeCam.class);
  }
  
  private static class OrthoCam extends Camera {
    public static final float DEFANGLE = -1.5707964F;
    
    protected float dist;
    
    protected float elev;
    
    protected float angl;
    
    protected float field;
    
    private Coord dragorig;
    
    private float anglorig;
    
    protected Coord3f cc;
    
    public OrthoCam(MapView mv) {
      super(mv);
      this.dist = 500.0F;
      this.elev = 1.5707964F * MapView.Camera.elevModC / 30.0F;
      this.angl = -1.5707964F * (10 + MapView.Camera.angleModC) / 10.0F;
      this.field = (float)(100.0D * Math.sqrt(2.0D) * (MapView.Camera.distModC + 1)) / 250.0F;
      this.dragorig = null;
    }
    
    public void tick2(double dt) {
      Coord3f cc = this.mv.getcc();
      cc.y = -cc.y;
      this.cc = cc;
    }
    
    public void tick(double dt) {
      tick2(dt);
      float aspect = this.mv.sz.y / this.mv.sz.x;
      this.view.update(PointedCam.compute(this.cc.add(0.0F, 0.0F, 15.0F), this.dist, this.elev, this.angl));
      this.proj.update(Projection.makeortho(new Matrix4f(), -this.field, this.field, -this.field * aspect, this.field * aspect, 1.0F, 5000.0F));
    }
    
    public float angle() {
      return this.angl;
    }
    
    public boolean click(Coord c) {
      this.anglorig = this.angl;
      this.dragorig = c;
      return true;
    }
    
    public void fixangle() {
      this.angl = stepify(this.angl - -1.5707964F) + -1.5707964F;
    }
    
    protected float stepify(float a) {
      if (Config.isocam_steps) {
        a = (float)Math.round((4.0F * a) / Math.PI);
        a = (float)(a * Math.PI / 4.0D);
      } 
      return a;
    }
    
    public void drag(Coord c) {
      float delta = stepify((c.x - this.dragorig.x) / 100.0F);
      this.angl = this.anglorig + delta;
      this.angl %= 6.2831855F;
    }
    
    public boolean wheel(Coord c, int amount) {
      this.field += (amount * 10 * MapView.Camera.scrollModC);
      this.field = Math.max(Math.min(this.field, 4000.0F), 50.0F);
      return true;
    }
    
    public String toString() {
      return String.format("%f %f %f %f", new Object[] { Float.valueOf(this.dist), Double.valueOf(this.elev / Math.PI), Double.valueOf(this.angl / Math.PI), Float.valueOf(this.field) });
    }
  }
  
  private static class SOrthoCam extends OrthoCam {
    private Coord dragorig;
    
    private float anglorig;
    
    private float tangl;
    
    private float tfield;
    
    private final float pi2 = 6.2831855F;
    
    public SOrthoCam(MapView mv) {
      super(mv);
      this.dragorig = null;
      this.tangl = this.angl;
      this.tfield = this.field;
      this.pi2 = 6.2831855F;
    }
    
    public void tick2(double dt) {
      Coord3f mc = this.mv.getcc();
      mc.y = -mc.y;
      if (this.cc == null || Math.hypot((mc.x - this.cc.x), (mc.y - this.cc.y)) > 250.0D) {
        this.cc = mc;
      } else {
        this.cc = this.cc.add(mc.sub(this.cc).mul(1.0F - (float)Math.pow(500.0D, -dt)));
      } 
      this.angl += (this.tangl - this.angl) * (1.0F - (float)Math.pow(500.0D, -dt));
      getClass();
      while (this.angl > 6.2831855F) {
        getClass();
        this.angl -= 6.2831855F;
        getClass();
        this.tangl -= 6.2831855F;
        getClass();
        this.anglorig -= 6.2831855F;
      } 
      while (this.angl < 0.0F) {
        getClass();
        this.angl += 6.2831855F;
        getClass();
        this.tangl += 6.2831855F;
        getClass();
        this.anglorig += 6.2831855F;
      } 
      if (Math.abs(this.tangl - this.angl) < 1.0E-4D)
        this.angl = this.tangl %= 6.2831855F; 
      this.field += (this.tfield - this.field) * (1.0F - (float)Math.pow(500.0D, -dt));
      if (Math.abs(this.tfield - this.field) < 1.0E-4D)
        this.field = this.tfield; 
    }
    
    public boolean click(Coord c) {
      this.anglorig = this.angl;
      this.dragorig = c;
      return true;
    }
    
    public void fixangle() {
      this.tangl = stepify(this.tangl - -1.5707964F) + -1.5707964F;
    }
    
    public void drag(Coord c) {
      float delta = stepify((c.x - this.dragorig.x) / 100.0F);
      this.tangl = this.anglorig + delta;
      if (Config.isocam_steps) {
        float tangl_by1m = this.tangl * 1000000.0F;
        getClass();
        float oneStep_by1m = 6.2831855F / 8.0F * 1000000.0F;
        float mod_result = tangl_by1m % oneStep_by1m;
        if (mod_result > 10.0F && oneStep_by1m - mod_result > 10.0F) {
          getClass();
          this.tangl = Math.round(tangl_by1m / oneStep_by1m) * 6.2831855F / 8.0F;
        } 
      } 
    }
    
    public boolean wheel(Coord c, int amount) {
      this.tfield += (amount * 10 * MapView.Camera.scrollModC);
      this.tfield = Math.max(Math.min(this.tfield, 4000.0F), 20.0F);
      return true;
    }
  }
  
  @RName("mapview")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      Coord sz = (Coord)args[0];
      Coord mc = (Coord)args[1];
      int pgob = -1;
      if (args.length > 2)
        pgob = ((Integer)args[2]).intValue(); 
      return new MapView(c, sz, parent, mc, pgob);
    }
  }
  
  public MapView(Coord c, Coord sz, Widget parent, Coord cc, long plgob) {
    super(c, sz, parent);
    this.gobActionMap.put(new String[] { "gfx/terobjs/tailorcleaner", "", "" }, Arrays.asList(new String[] { "cotton" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/cottoncleaner", "", "" }, Arrays.asList(new String[] { "cotton" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/loom", "", "" }, Arrays.asList(new String[] { "cottonbundle", "cottoncloth" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/butterchurn", "", "" }, Arrays.asList(new String[] { "butter" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/grindan", "", "" }, Arrays.asList(new String[] { "flour" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/windmillgrindertop", "", "" }, Arrays.asList(new String[] { "flour" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/coppergrinder", "gfx/invobjs/bone", "1" }, Arrays.asList(new String[] { "bonemeal" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/coppergrinder", "", "" }, Arrays.asList(new String[] { "flour" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/grindstone", "gfx/invobjs/bone", "1" }, Arrays.asList(new String[] { "bonemeal" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/grindstone", "", "" }, Arrays.asList(new String[] { "flour" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/meatgrinder", "", "" }, Arrays.asList(new String[] { "sausagelinks" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/cuttingwheel", "", "" }, Arrays.asList(new String[] { "cabochoncut", "pearcut", "squarecut", "starburstcut" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/crucible", "gfx/invobjs/silicatecompound", "5" }, Arrays.asList(new String[] { "glasspane" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/crucible", "gfx/invobjs/goldenegg", "25" }, Arrays.asList(new String[] { "goldbar" }));
    this.gobActionMap.put(new String[] { "gfx/terobjs/crucible", "", "" }, Arrays.asList(new String[] { "goldenegg" }));
    this.customItemActThreadActive = false;
    this.customItemActStop = false;
    this.biomeNames = new ArrayList<>();
    this.wasOn = false;
    this.biomeNames.add("gfx/tiles/grass");
    this.biomeNames.add("gfx/tiles/brush");
    this.biomeNames.add("gfx/tiles/crag");
    this.biomeNames.add("gfx/tiles/leaf");
    this.biomeNames.add("gfx/tiles/oldblack");
    this.biomeNames.add("gfx/tiles/shrub");
    this.biomeNames.add("gfx/tiles/fen");
    this.biomeNames.add("gfx/tiles/greenwood");
    this.biomeNames.add("gfx/tiles/savanna");
    this.biomeNames.add("gfx/tiles/beach");
    this.biomeNames.add("gfx/tiles/rbeach");
    this.biomeNames.add("gfx/tiles/snow");
    this.biomeNames.add("gfx/tiles/dirt");
    this.biomeNames.add("gfx/tiles/apath");
    camtypes.put("follow", FollowCam.class);
    camtypes.put("sfollow", SmoothFollowCam.class);
    camtypes.put("free", FreeCam.class);
    camtypes.put("ortho", OrthoCam.class);
    camtypes.put("sortho", SOrthoCam.class);
    this.map = new Rendered() {
        public void draw(GOut g) {}
        
        public boolean setup(RenderList rl) {
          Coord cc = MapView.this.cc.div(MCache.tilesz).div(MCache.cutsz);
          Coord o = new Coord();
          if (MapView.smallView) {
            MapView.this.view = 1;
          } else {
            MapView.this.view = 2;
          } 
          for (o.y = -MapView.this.view; o.y <= MapView.this.view; o.y++) {
            for (o.x = -MapView.this.view; o.x <= MapView.this.view; o.x++) {
              Coord pc = cc.add(o).mul(MCache.cutsz).mul(MCache.tilesz);
              MapMesh cut = MapView.this.glob.map.getcut(cc.add(o));
              rl.add(cut, Location.xlate(new Coord3f(pc.x, -pc.y, 0.0F)));
            } 
          } 
          return false;
        }
      };
    this.mapol = new Rendered() {
        private final GLState[] mats;
        
        public void draw(GOut g) {}
        
        public boolean setup(RenderList rl) {
          Coord cc = MapView.this.cc.div(MCache.tilesz).div(MCache.cutsz);
          Coord o = new Coord();
          for (o.y = -MapView.this.view; o.y <= MapView.this.view; o.y++) {
            for (o.x = -MapView.this.view; o.x <= MapView.this.view; o.x++) {
              Coord pc = cc.add(o).mul(MCache.cutsz).mul(MCache.tilesz);
              for (int i = 0; i < MapView.this.visol.length; i++) {
                if (this.mats[i] != null)
                  if (MapView.this.visol[i] > 0) {
                    Rendered olcut = MapView.this.glob.map.getolcut(i, cc.add(o));
                    if (olcut != null)
                      rl.add(olcut, GLState.compose(new GLState[] { Location.xlate(new Coord3f(pc.x, -pc.y, 0.0F)), this.mats[i] })); 
                  }  
              } 
            } 
          } 
          return false;
        }
      };
    this.gobs = new Rendered() {
        public void draw(GOut g) {}
        
        public boolean setup(RenderList rl) {
          synchronized (MapView.this.glob.oc) {
            for (Gob gob : MapView.this.glob.oc) {
              if (!MapView.hidePlayer || gob != MapView.this.player())
                MapView.this.addgob(rl, gob); 
            } 
          } 
          return false;
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
    this.cmdmap.put("help", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            MapView.this.ui.message("\n\n\n", GameUI.MsgType.INFO);
            MapView.this.ui.message("This is a list of console commands, [] are required arguments, () are optional", GameUI.MsgType.INFO);
            MapView.this.ui.message("\n\n     Camera control:", GameUI.MsgType.INFO);
            MapView.this.ui.message(":cam [cam name, like sortho, best, sucky] (distance) (angle) (elevation) (scroll-speed-multiplier)", GameUI.MsgType.INFO);
            MapView.this.ui.message("enter angle and elevation in degrees (ï¿½)", GameUI.MsgType.INFO);
            MapView.this.ui.message("if you want to skip a value, put an \"x\" there instead", GameUI.MsgType.INFO);
            MapView.this.ui.message("example= :cam sucky 200 270 x 10", GameUI.MsgType.INFO);
            MapView.this.ui.message("so this example would give you the smooth freestyle cam (sucky),", GameUI.MsgType.INFO);
            MapView.this.ui.message("with distance = 200, angle (left-right) = 270ï¿½, elevation (up-down) is x so default, and a scroll speed multiplier of 10", GameUI.MsgType.INFO);
            MapView.this.ui.message("\n\n     Debug:", GameUI.MsgType.INFO);
            MapView.this.ui.message(":debug toggles debug mode on/of for reading out the commands of what you do with clicks and so on", GameUI.MsgType.INFO);
            MapView.this.ui.message("\n\n     Auto-Unkin-Red:", GameUI.MsgType.INFO);
            MapView.this.ui.message(":autounkinred toggles auto-unkinning red (unkin, not forget) characters in your kin list", GameUI.MsgType.INFO);
            MapView.this.ui.message("\n\n     Invisibility:", GameUI.MsgType.INFO);
            MapView.this.ui.message(":inv [one/all]", GameUI.MsgType.INFO);
            MapView.this.ui.message(":inv (one/all/omit for off)", GameUI.MsgType.INFO);
            MapView.this.ui.message("CTRL-click turns one object invisible, or all of the same kind", GameUI.MsgType.INFO);
            MapView.this.ui.message("\n\n     Counting:", GameUI.MsgType.INFO);
            MapView.this.ui.message(":count (name) (radius)", GameUI.MsgType.INFO);
            MapView.this.ui.message("name, use 1 letter like x to skip when using other options, you can CTRL-click then to select object to count", GameUI.MsgType.INFO);
            MapView.this.ui.message("radius in which to count stuff around the player, use 0 for infinite to skip when using overlay count but no radius", GameUI.MsgType.INFO);
            MapView.this.ui.message(":count x 0  ...will prompt you to CTRL-click an object to define the target using 0 = infinite range (all visible)", GameUI.MsgType.INFO);
            MapView.this.ui.message("Pro-Tip: if you enter :count without any parameters, it is also ctrl-click-select and max range.", GameUI.MsgType.INFO);
            MapView.this.ui.message("\n\n     Single Radius:", GameUI.MsgType.INFO);
            MapView.this.ui.message(":rad", GameUI.MsgType.INFO);
            MapView.this.ui.message("toggles the single radius function on/off", GameUI.MsgType.INFO);
            MapView.this.ui.message("if on, CTRL-click on anything with a radius will turn on/off only this objects radius", GameUI.MsgType.INFO);
            MapView.this.ui.message("\n\n     AutoDoor:", GameUI.MsgType.INFO);
            MapView.this.ui.message(":autodoor", GameUI.MsgType.INFO);
            MapView.this.ui.message("toggles the AutoDoor function on/off", GameUI.MsgType.INFO);
            MapView.this.ui.message("\n\n     MiningHelper:", GameUI.MsgType.INFO);
            MapView.this.ui.message(":mining", GameUI.MsgType.INFO);
            MapView.this.ui.message("toggles the MiningHelper function: FastMode/off/on", GameUI.MsgType.INFO);
            MapView.this.ui.message(":minging off", GameUI.MsgType.INFO);
            MapView.this.ui.message("deactivates it completely", GameUI.MsgType.INFO);
          }
        });
    this.cmdmap.put("cam", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args.length >= 2 && args[1].length() > 0 && 
              MapView.camtypes.containsKey(args[1])) {
              MapView.this.setMods(args);
              OptWnd2.setcamera(args[1]);
              if (OptWnd2.instance != null) {
                OptWnd2.toggle();
                OptWnd2.toggle();
              } 
            } 
          }
        });
    this.cmdmap.put("whyload", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Loading l = MapView.this.lastload;
            if (l == null)
              throw new Exception("Not loading"); 
            l.printStackTrace(cons.out);
          }
        });
    this.cmdmap.put("debug", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            RemoteUI.debug = !(RemoteUI.debug == true);
            MapView.this.ui.message("debug: " + RemoteUI.debug, GameUI.MsgType.INFO);
          }
        });
    this.cmdmap.put("debugtest", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            MapView.debugtest = !MapView.debugtest;
            MapView.this.ui.message("debugtest: " + MapView.debugtest, GameUI.MsgType.INFO);
          }
        });
    this.cmdmap.put("inv", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args.length == 2 && args[1].equals("all")) {
              MapView.invAll = !(MapView.invAll == true);
              MapView.this.ui.message("invisibility all: " + MapView.invAll, GameUI.MsgType.INFO);
              MapView.invOne = false;
            } else if (args.length == 2 && args[1].equals("one")) {
              MapView.invOne = !(MapView.invOne == true);
              MapView.this.ui.message("invisibility one: " + MapView.invOne, GameUI.MsgType.INFO);
              MapView.invAll = false;
            } else {
              MapView.this.ui.message("invisibility: off ", GameUI.MsgType.INFO);
              MapView.invAll = false;
              MapView.invOne = false;
            } 
          }
        });
    this.cmdmap.put("count", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            try {
              if (MapView.countIsRunning) {
                MapView.this.ui.message("stopping old counter...", GameUI.MsgType.INFO);
                MapView.stopCounting = true;
              } else {
                while (MapView.stopCounting)
                  MapView.sleep(50); 
                String name = "";
                int distL = 0;
                MapView.this.overlayCount.clear();
                if (args.length > 1)
                  try {
                    name = args[1];
                  } catch (Exception exception) {} 
                if (args.length > 2)
                  try {
                    distL = Integer.parseInt(args[2]);
                    MapView.this.dist = distL;
                  } catch (Exception exception) {} 
                if (name.length() < 2) {
                  MapView.this.ui.message("start counting with a click...", GameUI.MsgType.INFO);
                  MapView.startCountOnce = true;
                } else {
                  MapView.this.countingGobs(name, MapView.this.dist, MapView.this.overlayCount);
                } 
              } 
            } catch (Exception e) {
              MapView.this.ui.message("error7: " + e.getMessage() + e.toString(), GameUI.MsgType.INFO);
            } 
          }
        });
    this.cmdmap.put("rad", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            MapView.singleRad = !MapView.singleRad;
            MapView.this.ui.message("sRad: " + MapView.singleRad, GameUI.MsgType.INFO);
          }
        });
    this.cmdmap.put("charname", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            MapView.this.ui.message("test: " + MainFrame.instance.getTitle(), GameUI.MsgType.INFO);
            MapView.this.ui.message("test: " + MainFrame.instance.getTitle().split(" - ")[0], GameUI.MsgType.INFO);
          }
        });
    this.cmdmap.put("autodoor", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args.length > 1) {
              if (args[1].toLowerCase().contains("debug")) {
                GameUI.debugDoors = !GameUI.debugDoors;
                MapView.this.ui.message("AutoDoors Debug: " + GameUI.debugDoors, GameUI.MsgType.INFO);
              } 
            } else {
              GameUI.autoDoors = !GameUI.autoDoors;
              MapView.this.ui.message("AutoDoors: " + GameUI.autoDoors, GameUI.MsgType.INFO);
            } 
          }
        });
    this.cmdmap.put("mining", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args.length > 1) {
              if (args[1].toLowerCase().contains("off")) {
                MapView.mineHelper = false;
                MapView.mineHelperFastMode = false;
              } 
            } else if (MapView.mineHelper) {
              if (!MapView.mineHelperFastMode) {
                MapView.mineHelperFastMode = true;
              } else {
                MapView.mineHelper = false;
                MapView.mineHelperFastMode = false;
              } 
            } else {
              MapView.mineHelper = true;
              MapView.mineHelperFastMode = false;
            } 
            MapView.this.ui.message("MiningHelper: " + MapView.mineHelper, GameUI.MsgType.INFO);
            MapView.this.ui.message("MH Fast Mode: " + MapView.mineHelperFastMode, GameUI.MsgType.INFO);
          }
        });
    this.cmdmap.put("autounkinred", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Config.auto_unkin_red = !Config.auto_unkin_red;
            Utils.setprefb("auto_unkin_red", Config.auto_unkin_red);
            if (Config.auto_unkin_red)
              BuddyWnd.unkinAllRedOnce(); 
            MapView.this.ui.message("Auto Unkin Red: " + Config.auto_unkin_red, GameUI.MsgType.INFO);
          }
        });
    this.cmdmap.put("testc", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            try {
              MapView.output = true;
            } catch (Exception e) {
              Utils.errOut(e);
            } 
          }
        });
    this.cmdmap.put("tile", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            try {
              String tileName = MapView.this.getNameOfTileIfGroundTile(MapView.this.getTile((MapView.this.ui.gui.map.player()).rc.div(11)));
              if (tileName == null) {
                int tile = UI.instance.sess.glob.map.gettile((UI.instance.gui.map.player()).rc.div(11.0D));
                Resource tilesetr = UI.instance.sess.glob.map.tilesetr(tile);
                if (!Config.autobucket || tilesetr.name.contains("water"));
                tileName = tilesetr.name;
              } 
              Utils.msgOut("Tile Name: " + tileName);
            } catch (Exception e) {
              Utils.errOut(e);
            } 
          }
        });
    this.cmdmap.put("x", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Set<Glob.Pagina> paginae = MapView.this.ui.sess.glob.paginae;
            MenuGrid menu = MapView.this.ui.gui.menu;
            Glob.Pagina cur = MapView.this.ui.gui.menu.cur;
            ArrayList<String> strings = new ArrayList<>();
            for (Glob.Pagina pagina : paginae)
              strings.add((pagina.res()).name); 
            try {
              if (args != null) {
                if (args.length == 3 && args[1] != null && args[1].toLowerCase().contains("show")) {
                  if (args[2] != null) {
                    ArrayList<String> printList = new ArrayList<>();
                    for (Glob.Pagina p : paginae) {
                      if ((p.res()).name.toLowerCase().contains(args[2].toLowerCase()))
                        printList.add((p.res()).name); 
                    } 
                    Collections.sort(printList);
                    for (String s : printList)
                      Utils.msgOut(s); 
                  } 
                  return;
                } 
                if (args.length == 2 && args[1] != null) {
                  ArrayList<String> targets = new ArrayList<>();
                  for (String s : strings) {
                    if (s.toLowerCase().contains(args[1].toLowerCase()))
                      targets.add(s); 
                  } 
                  targets.sort(new Comparator<String>() {
                        public int compare(String o1, String o2) {
                          if (o1 == null)
                            return 1; 
                          if (o2 == null)
                            return -1; 
                          if (o1.length() < o2.length())
                            return -1; 
                          if (o1.length() > o2.length())
                            return 1; 
                          return 0;
                        }
                      });
                  for (Glob.Pagina p : paginae) {
                    if ((p.res()).name.toLowerCase().equals(targets.get(0))) {
                      MapView.this.ui.gui.menu.use(p);
                      return;
                    } 
                  } 
                  for (Glob.Pagina p : paginae) {
                    if ((p.res()).name.toLowerCase().contains(args[1].toLowerCase())) {
                      MapView.this.ui.gui.menu.use(p);
                      return;
                    } 
                  } 
                } 
              } 
            } catch (Exception e) {
              System.out.println();
              e.printStackTrace();
            } 
            String currentMenu = "";
            try {
              currentMenu = (MapView.this.ui.gui.menu.cur.res()).name;
              Utils.msgLog(currentMenu);
            } catch (Exception exception) {}
            if ("paginae/act/add".equals(currentMenu)) {
              MapView.this.ui.gui.menu.use(MenuGrid.next);
              return;
            } 
            for (Glob.Pagina p : paginae) {
              if ((p.res()).name.toLowerCase().contains("paginae/act/add")) {
                MapView.this.ui.gui.menu.use(p);
                break;
              } 
            } 
          }
        });
    this.cmdmap.put("h", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args.length > 1) {
              if (args[1] != null && "help".equals(args[1])) {
                Utils.msgOut("to hide all the UI, or reset to show everything, simply use :h");
                Utils.msgOut("to hide parts, use :h parameter1 parameter2 and so on");
                Utils.msgOut("Valid Parameters are:");
                Utils.msgOut("quickbars");
                Utils.msgOut("gamemenu");
                Utils.msgOut("mainmenu");
                Utils.msgOut("buff");
                Utils.msgOut("equip");
                Utils.msgOut("humors");
                Utils.msgOut("cravings");
                Utils.msgOut("fork");
                Utils.msgOut("minimap");
                Utils.msgOut("movement");
                Utils.msgOut("chatui");
                Utils.msgOut("weight");
                Utils.msgOut("fight");
                Utils.msgOut("party");
                Utils.msgOut("season");
              } else {
                for (int i = 1; i < args.length; i++) {
                  if ("quickbars".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.ToolBeltWdg"); 
                  if ("gamemenu".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.MenuGrid"); 
                  if ("mainmenu".contains(args[i].toLowerCase())) {
                    GameUI.classNames.add("haven.GameUI$6");
                    GameUI.classNames.add("haven.GameUI$7");
                    GameUI.classNames.add("haven.GameUI$MainMenu");
                  } 
                  if ("buff".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.Bufflist"); 
                  if ("equip".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.EquipProxyWdg"); 
                  if ("humors".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.Tempers"); 
                  if ("cravings".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.Tempers$2"); 
                  if ("fork".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.Tempers$1"); 
                  if ("minimap".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.LocalMiniMap"); 
                  if ("movement".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.GameUI$5"); 
                  if ("chatui".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.ChatUI"); 
                  if ("weight".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.WeightWdg"); 
                  if ("fight".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.Fightview"); 
                  if ("party".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.Partyview"); 
                  if ("season".contains(args[i].toLowerCase()))
                    GameUI.classNames.add("haven.SeasonImg"); 
                } 
              } 
            } else if (GameUI.classNames.size() > 0) {
              GameUI.classNames.clear();
              HavenPanel.hideUI = false;
            } else {
              HavenPanel.hideUI = !HavenPanel.hideUI;
            } 
          }
        });
    this.cmdmap.put("p", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            MapView.hidePlayer = !MapView.hidePlayer;
          }
        });
    this.cmdmap.put("ll", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args != null && args.length == 2)
              try {
                int i = Integer.parseInt(args[1]);
                if (i >= 0 && i < 9999)
                  HavenPanel.glconf.maxlights = i; 
              } catch (Exception exception) {} 
          }
        });
    this.cmdmap.put("offset", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            int arg1 = 0;
            int arg2 = 0;
            if (args != null && args.length >= 3)
              try {
                int i = Integer.parseInt(args[2]);
                arg1 = i % 11;
              } catch (Exception exception) {} 
            if (args != null && args.length >= 2)
              try {
                int i = Integer.parseInt(args[1]);
                arg2 = i % 11;
              } catch (Exception exception) {} 
            Utils.msgOut("OffSet for placing ghost-objects set to: " + arg1 + " : " + arg2);
            MapView.o1 = arg1;
            MapView.o2 = arg2;
          }
        });
    this.cmdmap.put("placegrid", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args != null && args.length == 2)
              try {
                int i = Integer.parseInt(args[1]) % 11;
                MapView.placeGrid = i;
              } catch (Exception exception) {} 
            Utils.msgOut("Grid granularity for placing ghost-objects set to:" + MapView.placeGrid);
          }
        });
    this.cmdmap.put("ccp", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Config.cache_project = !Config.cache_project;
            Utils.setprefb("cache_project", Config.cache_project);
            Utils.msgOut("Cache set to:" + Config.cache_project);
          }
        });
    this.cmdmap.put("space", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            SmartSpace.work();
          }
        });
    this.cmdmap.put("test", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            ArrayList<Boolean> bA = new ArrayList<>();
            int c = 1;
            long nanS = System.nanoTime();
            for (int i = 0; i < 1; i++)
              bA.add(Boolean.valueOf(MainFrame.instance.isActive())); 
            Utils.msgLog("test: " + ((System.nanoTime() - nanS) / 1000000L) + "   " + (System.nanoTime() - nanS));
            MainFrame.instance.isActive();
          }
        });
    this.cmdmap.put("loadtest", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            try {
              String resName = args[1];
              int verNum = Integer.parseInt(args[2]);
              try {
                Resource.load(resName, verNum, -5);
                MapView.sleep(100);
                Utils.msgLog("loaded: " + verNum);
              } catch (Exception exception) {}
            } catch (Exception exception) {}
          }
        });
    this.cmdmap.put("animaloutput", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            boolean wrongInput = false;
            if (args != null && args.length == 2) {
              try {
                int i = Integer.parseInt(args[1]);
                Config.domestic_animals_stats_offset = i;
                Utils.setprefi("domestic_animals_stats_offset", i);
              } catch (Exception exception) {}
            } else if (args != null && args.length == 7) {
              try {
                for (int i = 1; i < args.length; ) {
                  if (Integer.parseInt(args[i]) == 1 || Integer.parseInt(args[i]) == 0) {
                    i++;
                    continue;
                  } 
                  wrongInput = true;
                } 
                if (!wrongInput) {
                  boolean statValue = false;
                  statValue = (Integer.parseInt(args[1]) == 1);
                  Config.animal_stat_tranquility = statValue;
                  Utils.setprefb("animal_stat_tranquility", statValue);
                  statValue = (Integer.parseInt(args[2]) == 1);
                  Config.animal_stat_immunity = statValue;
                  Utils.setprefb("animal_stat_immunity", statValue);
                  statValue = (Integer.parseInt(args[3]) == 1);
                  Config.animal_stat_metabolism = statValue;
                  Utils.setprefb("animal_stat_metabolism", statValue);
                  statValue = (Integer.parseInt(args[4]) == 1);
                  Config.animal_stat_size = statValue;
                  Utils.setprefb("animal_stat_size", statValue);
                  statValue = (Integer.parseInt(args[5]) == 1);
                  Config.animal_stat_productivity = statValue;
                  Utils.setprefb("animal_stat_productivity", statValue);
                  statValue = (Integer.parseInt(args[6]) == 1);
                  Config.animal_stat_longevity = statValue;
                  Utils.setprefb("animal_stat_longevity", statValue);
                } else {
                  Utils.msgOut("you need to putin 1 or 0 for each stat to turn it on/off");
                  Utils.msgOut("an example for a valid input is :animaloutput 1 1 1 0 0 0");
                } 
              } catch (Exception exception) {}
            } else {
              Config.domestic_animal_stats_to_log_chat = !Config.domestic_animal_stats_to_log_chat;
              Utils.setprefb("domestic_animal_stats_to_log_chat", Config.domestic_animal_stats_to_log_chat);
            } 
            Utils.msgLog("Domestic Animal Stats Output is " + (Config.domestic_animal_stats_to_log_chat ? "ON" : "OFF") + " ...offset value is: " + Config.domestic_animals_stats_offset);
            Utils.msgLog("Tranquility" + (Config.animal_stat_tranquility ? "ON" : "OFF"));
            Utils.msgLog("Immunity" + (Config.animal_stat_immunity ? "ON" : "OFF"));
            Utils.msgLog("Metabolism" + (Config.animal_stat_metabolism ? "ON" : "OFF"));
            Utils.msgLog("Size" + (Config.animal_stat_size ? "ON" : "OFF"));
            Utils.msgLog("Productiivty" + (Config.animal_stat_productivity ? "ON" : "OFF"));
            Utils.msgLog("Longevity" + (Config.animal_stat_longevity ? "ON" : "OFF"));
          }
        });
    this.cmdmap.put("testcravings", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Tempers.testCravings();
          }
        });
    this.cmdmap.put("ttoff", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Config.tt_off = !Config.tt_off;
            Utils.setprefb("tt_off", Config.tt_off);
            Utils.msgLog("ToolTips are: " + (Config.tt_off ? "Off" : "On"));
          }
        });
    this.cmdmap.put("seasonchangemessage", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Config.season_change_message_off = !Config.season_change_message_off;
            Utils.setprefb("season_change_message_off", Config.season_change_message_off);
            Utils.msgLog("Season Change Message is: " + (Config.tt_off ? "Off" : "On"));
          }
        });
    this.cmdmap.put("flatgridsize", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args != null && args.length == 2)
              try {
                int i = Integer.parseInt(args[1]);
                if (i > 80)
                  i = 80; 
                if (i < 1)
                  i = 1; 
                Config.static_flat_grid_size = i;
                Utils.setprefi("static_flat_grid_size", i);
                Utils.msgLog("static flat grid size set to: " + Config.static_flat_grid_size);
              } catch (Exception e) {
                Utils.msgLog("this was not a whole number");
              }  
          }
        });
    this.cmdmap.put("npcrgb", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            boolean successRGB = false;
            if (args != null && args.length == 4)
              try {
                int i1 = Integer.parseInt(args[1]);
                int i2 = Integer.parseInt(args[2]);
                int i3 = Integer.parseInt(args[3]);
                if (i1 >= 0 && i1 < 256 && i2 >= 0 && i2 < 256 && i3 >= 0 && i3 < 256) {
                  Config.npc_colour1 = i1;
                  Config.npc_colour2 = i2;
                  Config.npc_colour3 = i3;
                  Utils.setprefi("npc_colour1", i1);
                  Utils.setprefi("npc_colour2", i2);
                  Utils.setprefi("npc_colour3", i3);
                  Utils.msgOut("colour of npcs and mannequins set to: " + i1 + " " + i2 + " " + i3);
                  Radar.npcColour = new Color(Config.npc_colour1, Config.npc_colour2, Config.npc_colour3);
                  successRGB = true;
                  MapView.this.ui.sess.glob.oc.radar.reload();
                } 
              } catch (Exception exception) {} 
            if (args != null && args.length == 1) {
              Config.npc_colour1 = 102;
              Config.npc_colour2 = 0;
              Config.npc_colour3 = 102;
              Utils.setprefi("npc_colour1", Config.npc_colour1);
              Utils.setprefi("npc_colour2", Config.npc_colour2);
              Utils.setprefi("npc_colour3", Config.npc_colour3);
              Utils.msgOut("colour of npcs and mannequins set to default: " + Config.npc_colour1 + " " + Config.npc_colour2 + " " + Config.npc_colour3);
              Radar.npcColour = new Color(Config.npc_colour1, Config.npc_colour2, Config.npc_colour3);
              successRGB = true;
              MapView.this.ui.sess.glob.oc.radar.reload();
            } 
            if (!successRGB) {
              Utils.msgOut("could not set colour, use :npcrgb followed by 3 numbers 0 to 255 separated by 1 space");
              Utils.msgOut("or use :npcrgb without any numbers to reset to default colour");
            } 
          }
        });
    this.cmdmap.put("servertime", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            long rtime = 0L;
            long stime = MapView.this.ui.sess.glob.globtime();
            Date date = new Date(stime + SeasonImg.EPOCH);
            Utils.msgLog("server time: " + Timer.server + " globTime: " + (MapView.this.ui.sess.glob.globtime() + SeasonImg.EPOCH));
            Utils.msgLog("server time: " + date + "  inv: " + Utils.getInvMaxSize());
          }
        });
    this.cmdmap.put("scalemarkers", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args != null && args.length == 2)
              try {
                int i = Integer.parseInt(args[1]);
                MapView.scaleForMarkers = (100 / i);
              } catch (Exception e) {
                Utils.msgLog("this was not a whole number");
              }  
            Utils.msgLog("server time: ");
          }
        });
    this.cmdmap.put("belt", new Console.Command() {
          public void run(Console cons, String[] args) {
            try {
              final Object[] ad = new Object[4];
              ad[0] = Integer.valueOf(Integer.parseInt(args[1]));
              ad[1] = Integer.valueOf(1);
              ad[2] = Integer.valueOf(0);
              ad[3] = new Coord();
              Coord mc = MapView.this.ui.mc;
              MapView.this.ui.gui.map.getClass();
              MapView.this.ui.gui.map.delay(new MapView.Hittest(MapView.this.ui.gui.map, mc) {
                    protected void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
                      ad[3] = mc;
                      MapView.this.ui.gui.wdgmsg("belt", ad);
                    }
                  });
            } catch (Exception e) {
              Utils.msgLog("belt needs to be followed by 1 number");
            } 
          }
        });
    this.cmdmap.put("tbalert", new Console.Command() {
          public void run(Console cons, String[] args) {
            try {
              Config.ring_on_thornbush = !Config.ring_on_thornbush;
              Utils.setprefb("ring_on_thornbush", Config.ring_on_thornbush);
              Utils.msgOut("Thornbush alert is now set to: " + (Config.ring_on_thornbush ? "ON" : "OFF"));
            } catch (Exception e) {
              Utils.msgLog("error on command line: " + e.getMessage());
            } 
          }
        });
    this.cmdmap.put("tbcolour", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args != null && args.length == 4)
              try {
                int i1 = Integer.parseInt(args[1]);
                int i2 = Integer.parseInt(args[2]);
                int i3 = Integer.parseInt(args[3]);
                if (i1 >= 0 && i1 < 256 && i2 >= 0 && i2 < 256 && i3 >= 0 && i3 < 256) {
                  int tbcolour = i3 + (i2 << 8) + (i1 << 16);
                  Config.thornbushColour = new Color(tbcolour);
                  Utils.setprefi("thornbush_colour", tbcolour);
                  Utils.msgOut("colour of blooming thornbushes set to: " + i1 + " " + i2 + " " + i3);
                } 
              } catch (Exception e) {
                Utils.msgOut("This did not work out, try to provide 3 whole numbers between 0 and 255 as colour parameters");
              }  
          }
        });
    this.cmdmap.put("soc", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Config.smart_space_on_click = !Config.smart_space_on_click;
            Utils.setprefb("smart_space_on_click", Config.smart_space_on_click);
            Utils.msgOut("SmartSpace on Click is set to: " + (Config.smart_space_on_click ? "ON" : "OFF"));
          }
        });
    this.cmdmap.put("roc", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Config.auto_recipe_on_gob_click = !Config.auto_recipe_on_gob_click;
            Utils.setprefb("auto_recipe_on_gob_click", Config.auto_recipe_on_gob_click);
            Utils.msgOut("auto Recipe on Click is set to: " + (Config.auto_recipe_on_gob_click ? "ON" : "OFF"));
          }
        });
    this.cmdmap.put("hcl", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            Config.highlight_claimed_leantos = !Config.highlight_claimed_leantos;
            Utils.setprefb("highlight_claimed_leantos", Config.highlight_claimed_leantos);
            Utils.msgOut("highlight claimed leantos is set to: " + (Config.highlight_claimed_leantos ? "ON" : "OFF"));
          }
        });
    setcam(Utils.getpref("defcam", "sortho"));
    this.glob = this.ui.sess.glob;
    this.cc = cc;
    this.plgob = plgob;
    Radar.plGob = plgob;
    setcanfocus(true);
    this.r2dwdg = new R2DWdg(this);
  }
  
  public void enol(int... overlays) {
    for (int ol : overlays)
      this.visol[ol] = this.visol[ol] + 1; 
  }
  
  public void disol(int... overlays) {
    for (int ol : overlays)
      this.visol[ol] = this.visol[ol] - 1; 
  }
  
  public boolean visol(int ol) {
    return (this.visol[ol] > 0);
  }
  
  public static final Tex wftex = Resource.loadtex("gfx/hud/flat");
  
  private final Rendered mapol;
  
  private final Rendered gobs;
  
  private Coord3f smapcc;
  
  private ShadowMap smap;
  
  private long lsmch;
  
  private final DropSky.ResSky sky1;
  
  private final DropSky.ResSky sky2;
  
  public Light amb;
  
  private final Outlines outlines;
  
  void addgob(RenderList rl, Gob gob) {
    GLState xf;
    try {
      xf = Following.xf(gob);
    } catch (Exception e) {
      xf = null;
    } 
    GLState extra = null;
    if (xf == null) {
      xf = gob.loc;
      try {
        Coord3f c = gob.getc();
        Tiler tile = this.glob.map.tiler(this.glob.map.gettile((new Coord(c)).div(MCache.tilesz)));
        extra = tile.drawstate(this.glob, rl.cfg, c);
      } catch (Loading e) {
        extra = null;
      } 
    } 
    if (extra != null) {
      rl.add(gob, GLState.compose(new GLState[] { extra, xf, gob.olmod, gob.save }));
    } else {
      rl.add(gob, GLState.compose(new GLState[] { xf, gob.olmod, gob.save }));
    } 
  }
  
  public GLState camera() {
    return this.camera;
  }
  
  protected Projection makeproj() {
    return null;
  }
  
  private void updsmap(RenderList rl, DirLight light) {
    if (rl.cfg.pref.lshadow.val.booleanValue()) {
      if (this.smap == null)
        this.smap = new ShadowMap(new Coord(2048, 2048), 750.0F, 5000.0F, 1.0F); 
      this.smap.light = light;
      Coord3f dir = new Coord3f(-light.dir[0], -light.dir[1], -light.dir[2]);
      Coord3f cc = getcc();
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
      if (this.smap != null)
        this.smap.dispose(); 
      this.smap = null;
      this.smapcc = null;
    } 
  }
  
  public void setup(RenderList rl) {
    if (Config.use_old_night_vision) {
      setup_old(rl);
    } else {
      setup_new(rl);
    } 
  }
  
  public void setup_new(RenderList rl) {
    Gob pl = player();
    if (pl != null)
      this.cc = new Coord(pl.getc()); 
    synchronized (this.glob) {
      if (this.glob.lightamb != null) {
        boolean mt = isTileInsideMine();
        boolean on = ((Config.alwaysbright_in && mt) || (Config.alwaysbright_out && !mt));
        if (on && ts > System.currentTimeMillis()) {
          this.ui.sess.glob.brighten();
          this.wasOn = true;
        } 
        if (this.wasOn && !on) {
          this.wasOn = false;
          this.ui.sess.glob.blob(null);
          this.ui.sess.glob.ticklight(10000);
          this.ui.sess.glob.brighten();
        } 
        if (on) {
          this.glob.lightang = 2.356194490192345D;
          this.glob.lightelev = 0.9773843811168246D;
          DirLight light = new DirLight(this.glob.lightamb, this.glob.lightdif, this.glob.lightspc, Coord3f.o.sadd((float)this.glob.lightelev, (float)this.glob.lightang, 1.0F));
          rl.add(light, null);
          this.glob.lightang = 5.497787143782138D;
          DirLight light3 = new DirLight(this.glob.lightamb, this.glob.lightdif, this.glob.lightspc, Coord3f.o.sadd((float)this.glob.lightelev, (float)this.glob.lightang, 1.0F));
          rl.add(light3, null);
          updsmap(rl, light);
          this.amb = light;
        } else {
          DirLight light = new DirLight(this.glob.lightamb, this.glob.lightdif, this.glob.lightspc, Coord3f.o.sadd((float)this.glob.lightelev, (float)this.glob.lightang, 1.0F));
          rl.add(light, null);
          updsmap(rl, light);
          this.amb = light;
        } 
      } else {
        this.amb = null;
      } 
    } 
    if (rl.cfg.pref.outline.val.booleanValue())
      rl.add(this.outlines, null); 
    rl.add(this.map, null);
    rl.add(this.mapol, null);
    rl.add(this.gobs, null);
    if (this.placing != null)
      addgob(rl, this.placing); 
    synchronized (this.extradraw) {
      for (Rendered extra : this.extradraw)
        rl.add(extra, null); 
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
  
  public void setup_old(RenderList rl) {
    Gob pl = player();
    if (pl != null)
      this.cc = new Coord(pl.getc()); 
    synchronized (this.glob) {
      if (this.glob.lightamb != null) {
        DirLight light = new DirLight(this.glob.lightamb, this.glob.lightdif, this.glob.lightspc, Coord3f.o.sadd((float)this.glob.lightelev, (float)this.glob.lightang, 1.0F));
        rl.add(light, null);
        updsmap(rl, light);
        this.amb = light;
      } else {
        this.amb = null;
      } 
    } 
    if (rl.cfg.pref.outline.val.booleanValue())
      rl.add(this.outlines, null); 
    rl.add(this.map, null);
    rl.add(this.mapol, null);
    rl.add(this.gobs, null);
    if (this.placing != null)
      addgob(rl, this.placing); 
    synchronized (this.extradraw) {
      for (Rendered extra : this.extradraw)
        rl.add(extra, null); 
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
  
  public static final Uniform amblight = (Uniform)new Uniform.AutoApply(Type.INT, new GLState.Slot[0]) {
      public void apply(GOut g, int loc) {
        int idx = -1;
        PView.RenderContext ctx = g.st.<PView.RenderContext>get(PView.ctx);
        if (ctx instanceof PView.WidgetContext) {
          Widget wdg = ((PView.WidgetContext)ctx).widget();
          if (wdg instanceof MapView)
            idx = ((Light.LightList)g.st.<Light.LightList>get(Light.lights)).index(((MapView)wdg).amb); 
        } 
        g.gl.glUniform1i(loc, idx);
      }
    };
  
  private final PView.RenderContext clickctx;
  
  public void drawadd(Rendered extra) {
    synchronized (this.extradraw) {
      this.extradraw.add(extra);
    } 
  }
  
  public Gob player() {
    return this.glob.oc.getgob(this.plgob);
  }
  
  public Coord3f getcc() {
    Gob pl = player();
    if (pl != null)
      return pl.getc(); 
    return new Coord3f(this.cc.x, this.cc.y, this.glob.map.getcz(this.cc));
  }
  
  private GLState.Buffer clickbasic(GOut g) {
    GLState.Buffer ret = basic(g);
    this.clickctx.prep(ret);
    return ret;
  }
  
  private static abstract class Clicklist<T> extends RenderList {
    private final Map<Color, T> rmap = new HashMap<>();
    
    private int i = 1;
    
    private final GLState.Buffer plain;
    
    private final GLState.Buffer bk;
    
    private Clicklist(GLState.Buffer plain) {
      super(plain.cfg);
      this.plain = plain;
      this.bk = new GLState.Buffer(plain.cfg);
    }
    
    protected Color newcol(T t) {
      int cr = (this.i & 0xF) << 4 | (this.i & 0xF000) >> 12, cg = (this.i & 0xF0) << 0 | (this.i & 0xF0000) >> 16, cb = (this.i & 0xF00) >> 4 | (this.i & 0xF00000) >> 20;
      Color col = new Color(cr, cg, cb);
      this.i++;
      this.rmap.put(col, t);
      return col;
    }
    
    protected void render(GOut g, Rendered r) {
      try {
        if (r instanceof FRendered)
          ((FRendered)r).drawflat(g); 
      } catch (RLoad l) {
        if (this.ignload)
          return; 
        throw l;
      } 
    }
    
    public T get(GOut g, Coord c) {
      return this.rmap.get(g.getpixel(c));
    }
    
    protected void setup(RenderList.Slot s, Rendered r) {
      T t = map(r);
      super.setup(s, r);
      s.os.copy(this.bk);
      this.plain.copy(s.os);
      this.bk.copy(s.os, GLState.Slot.Type.GEOM);
      if (t != null) {
        Color col = newcol(t);
        (new States.ColState(col)).prep(s.os);
      } 
    }
    
    protected abstract T map(Rendered param1Rendered);
  }
  
  private static class Maplist extends Clicklist<MapMesh> {
    private int mode = 0;
    
    private MapMesh limit = null;
    
    private Maplist(GLState.Buffer plain) {
      super(plain);
    }
    
    protected MapMesh map(Rendered r) {
      if (r instanceof MapMesh)
        return (MapMesh)r; 
      return null;
    }
    
    protected void render(GOut g, Rendered r) {
      if (r instanceof MapMesh) {
        MapMesh m = (MapMesh)r;
        if (this.mode != 0)
          g.state(States.vertexcolor); 
        if (this.limit == null || this.limit == m)
          m.drawflat(g, this.mode); 
      } 
    }
  }
  
  private Coord checkmapclick(GOut g, Coord c) {
    Maplist rl = new Maplist(clickbasic(g));
    rl.setup(this.map, clickbasic(g));
    rl.fin();
    rl.render(g);
    MapMesh hit = rl.get(g, c);
    if (hit == null)
      return null; 
    rl.limit = hit;
    rl.mode = 1;
    rl.render(g);
    Color hitcol = g.getpixel(c);
    Coord tile = new Coord(hitcol.getRed() - 1, hitcol.getGreen() - 1);
    if (!tile.isect(Coord.z, rl.limit.sz))
      return null; 
    rl.mode = 2;
    rl.render(g);
    Color color1 = g.getpixel(c);
    if (color1.getBlue() != 0)
      return null; 
    Coord pixel = new Coord(color1.getRed() * MCache.tilesz.x / 255, color1.getGreen() * MCache.tilesz.y / 255);
    return rl.limit.ul.add(tile).mul(MCache.tilesz).add(pixel);
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
  
  private ClickInfo checkgobclick(GOut g, Coord c) {
    Clicklist<ClickInfo> rl = new Clicklist<ClickInfo>(clickbasic(g)) {
        Gob curgob;
        
        Gob.Overlay curol;
        
        MapView.ClickInfo curinfo;
        
        public MapView.ClickInfo map(Rendered r) {
          return this.curinfo;
        }
        
        public void add(Rendered r, GLState t) {
          Gob prevg = this.curgob;
          Gob.Overlay prevo = this.curol;
          if (r instanceof Gob) {
            this.curgob = (Gob)r;
          } else if (r instanceof Gob.Overlay) {
            this.curol = (Gob.Overlay)r;
          } 
          if (this.curgob == null || !(r instanceof FRendered)) {
            this.curinfo = null;
          } else {
            this.curinfo = new MapView.ClickInfo(this.curgob, this.curol, r);
          } 
          super.add(r, t);
          this.curgob = prevg;
          this.curol = prevo;
        }
      };
    rl.setup(this.gobs, clickbasic(g));
    rl.fin();
    rl.render(g);
    return rl.get(g, c);
  }
  
  public void delay(Delayed d) {
    synchronized (this.delayed) {
      this.delayed.add(d);
    } 
  }
  
  public void delay2(Delayed d) {
    synchronized (this.delayed2) {
      this.delayed2.add(d);
    } 
  }
  
  protected void undelay(Collection<Delayed> list, GOut g) {
    synchronized (list) {
      for (Delayed d : list)
        d.run(g); 
      list.clear();
    } 
  }
  
  private static final Text.Furnace polownertf = new PUtils.BlurFurn((new Text.Foundry("serif", 30)).aa(true), 3, 1, Color.BLACK);
  
  private Text polownert;
  
  private long polchtm;
  
  private boolean camload;
  
  private Loading lastload;
  
  private int olflash;
  
  private long olftimer;
  
  private boolean camdrag;
  
  private boolean LMBdown;
  
  private boolean mousemoved;
  
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
        a = (int)(255L * (2000L - poldt - 4000L) / 2000L);
      } 
      g.chcolor(255, 255, 255, a);
      g.aimage(this.polownert.tex(), this.sz.div(new Coord(2, 4)), 0.5D, 0.5D);
      g.chcolor();
    } 
  }
  
  private void drawarrow(GOut g, double a) {
    Coord ac, hsz = this.sz.div(2);
    double ca = -Coord.z.angle(hsz);
    if (a > ca && a < -ca) {
      ac = new Coord(this.sz.x, hsz.y - (int)(Math.tan(a) * hsz.x));
    } else if (a > -ca && a < Math.PI + ca) {
      ac = new Coord(hsz.x - (int)(Math.tan(a - 1.5707963267948966D) * hsz.y), 0);
    } else if (a > -3.141592653589793D - ca && a < ca) {
      ac = new Coord(hsz.x + (int)(Math.tan(a + 1.5707963267948966D) * hsz.y), this.sz.y);
    } else {
      ac = new Coord(0, hsz.y + (int)(Math.tan(a) * hsz.x));
    } 
    if (!Config.big_group_arrow) {
      Coord bc = ac.add(Coord.sc(a, -10.0D));
      g.line(bc, bc.add(Coord.sc(a, -40.0D)), 2.0D);
      g.line(bc, bc.add(Coord.sc(a + 0.7853981633974483D, -10.0D)), 2.0D);
      g.line(bc, bc.add(Coord.sc(a - 0.7853981633974483D, -10.0D)), 2.0D);
    } else {
      Coord bc = ac.add(Coord.sc(a, -100.0D));
      g.line(bc, bc.add(Coord.sc(a, -120.0D)), 10.0D);
      g.line(bc, bc.add(Coord.sc(a + 0.7853981633974483D, -50.0D)), 6.0D);
      g.line(bc, bc.add(Coord.sc(a - 0.7853981633974483D, -50.0D)), 6.0D);
    } 
  }
  
  public double screenangle(Coord mc, boolean clip) {
    Coord3f cc;
    try {
      cc = getcc();
    } catch (Loading e) {
      return Double.NaN;
    } 
    Coord3f mloc = new Coord3f(mc.x, -mc.y, cc.z);
    float[] sloc = this.camera.proj.toclip(this.camera.view.fin(Matrix4f.id).mul4(mloc));
    if (clip) {
      float w = sloc[3];
      if (sloc[0] > -w && sloc[0] < w && sloc[1] > -w && sloc[1] < w)
        return Double.NaN; 
    } 
    float a = this.sz.y / this.sz.x;
    return Math.atan2((sloc[1] * a), sloc[0]);
  }
  
  private void partydraw(GOut g) {
    for (Party.Member m : this.ui.sess.glob.party.memb.values()) {
      if (m.gobid == this.plgob)
        continue; 
      Coord mc = m.getc();
      if (mc == null)
        continue; 
      double a = screenangle(mc, true);
      if (a == Double.NaN)
        continue; 
      g.chcolor(m.col);
      drawarrow(g, a);
    } 
    g.chcolor();
  }
  
  public void draw(GOut g) {
    this.glob.map.sendreqs();
    if (this.olftimer != 0L && this.olftimer < System.currentTimeMillis())
      unflashol(); 
    try {
      if (output) {
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10; i++)
          Coord coord = MiscLib.ssz(g); 
        Utils.msgOut("t: " + (System.currentTimeMillis() - t1));
        Utils.msgOut("");
        long t2 = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
          Coord coord1 = MiscLib.ssz(g);
          Coord coord2 = checkmapclick(g, new Coord(1, 1));
          Coord coord3 = checkmapclick(g, new Coord(coord1.x - 1, 1));
          Coord coord4 = checkmapclick(g, new Coord(coord1.x - 1, coord1.y - 1));
          Coord coord5 = checkmapclick(g, new Coord(0, coord1.y));
        } 
        Utils.msgOut("t: " + (System.currentTimeMillis() - t2));
        Utils.msgOut("");
        Coord ssz = MiscLib.ssz(g);
        Coord ccA = checkmapclick(g, new Coord(1, 1));
        Coord ccB = checkmapclick(g, new Coord(ssz.x - 1, 1));
        Coord ccC = checkmapclick(g, new Coord(ssz.x - 1, ssz.y - 1));
        Coord ccD = checkmapclick(g, new Coord(0, ssz.y));
        Utils.msgOut("testc: " + ssz + " | " + ccA + " | " + ccB + " | " + ccC + " | " + ccD);
        Utils.msgOut("pc: " + (player()).rc + " " + (player()).sc);
        Utils.msgOut("M: " + MainFrame.instance.p.w + "x" + MainFrame.instance.p.h);
        output = false;
      } 
      if (this.camload)
        throw new MCache.LoadingMap(); 
      undelay(this.delayed, g);
      super.draw(g);
      Matrix4f dgpcam = new Matrix4f(), dgpwxf = new Matrix4f();
      dgpcam.load(g.st.cam);
      dgpwxf.load(g.st.wxf);
      undelay(this.delayed2, g);
      poldraw(g);
      partydraw(g);
      if (Config.showgobpath)
        drawGobPath(g, dgpcam, dgpwxf); 
    } catch (Loading e) {
      this.lastload = e;
      String text = e.getMessage();
      if (text == null)
        text = "Loading..."; 
      if (!Config.noloading) {
        g.chcolor(Color.BLACK);
        g.frect(Coord.z, this.sz);
      } 
      g.chcolor(Color.WHITE);
      g.atext(text, this.sz.div(2), 0.5D, 0.5D);
    } 
  }
  
  private void drawGobPath(GOut g, Matrix4f dgpcam, Matrix4f dgpwxf) {
    g.chcolor(Color.GREEN);
    Matrix4f cam = new Matrix4f(), wxf = new Matrix4f(), mv = new Matrix4f();
    Matrix4f wxfaccent = new Matrix4f();
    mv.load(cam.load(dgpcam)).mul1(wxf.load(dgpwxf));
    wxfaccent = wxf.trim3(1.0F).transpose();
    float field = Config.camera_field_of_view;
    float aspect = g.sz.y / g.sz.x;
    Projection proj = Projection.frustum(-field, field, -aspect * field, aspect * field, 1.0F, 5000.0F);
    synchronized (this.glob.oc) {
      for (Gob gob : this.glob.oc) {
        if (gob.sc == null)
          continue; 
        Moving m = gob.<Moving>getattr(Moving.class);
        if (m != null) {
          Coord3f reposstart = Coord3f.o, reposend = Coord3f.o;
          reposstart = gob.getc();
          if (m instanceof LinMove) {
            LinMove gobpath = (LinMove)m;
            float targetheight = 0.0F;
            try {
              targetheight = this.glob.map.getcz(gobpath.t);
            } catch (LoadingMap e) {
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
          } else if (m instanceof Following) {
            Following gobpath = (Following)m;
            long targetid = gobpath.tgt;
            Gob target = this.glob.oc.getgob(targetid);
            if (target != null) {
              reposend = target.getc();
            } else {
              reposend = gobpath.getc();
            } 
          } else {
            continue;
          } 
          reposstart.x -= wxf.get(3, 0);
          reposstart.y *= -1.0F;
          reposstart.y -= wxf.get(3, 1);
          reposstart.z -= wxf.get(3, 2);
          reposstart = wxfaccent.mul4(reposstart);
          Coord3f sstart = proj.toscreen(mv.mul4(reposstart), g.sz);
          Coord scstart = new Coord(sstart);
          reposend.x -= wxf.get(3, 0);
          reposend.y *= -1.0F;
          reposend.y -= wxf.get(3, 1);
          reposend.z -= wxf.get(3, 2);
          reposend = wxfaccent.mul4(reposend);
          Coord3f send = proj.toscreen(mv.mul4(reposend), g.sz);
          Coord scend = new Coord(send);
          g.line(scstart, scend, 2.0D);
        } 
      } 
    } 
    g.chcolor();
  }
  
  public void tick(double dt) {
    this.camload = false;
    try {
      this.camera.tick(dt);
    } catch (Loading e) {
      this.camload = true;
    } 
    if (this.placing != null)
      this.placing.ctick((int)(dt * 1000.0D)); 
  }
  
  public void resize(Coord sz) {
    super.resize(sz);
    this.r2dwdg.resize(sz);
    this.camera.resized();
  }
  
  protected void render2d(GOut g) {}
  
  private class Plob extends Gob {
    public void move(Coord c, double a) {
      super.move(c, a);
    }
    
    Coord lastmc = null;
    
    boolean freerot = false;
    
    private Plob(Indir<Resource> res, Message sdt) {
      super(MapView.this.glob, Coord.z);
      setattr(new ResDrawable(this, res, sdt));
      if (MapView.this.ui.mc.isect(MapView.this.rootpos(), MapView.this.sz))
        MapView.this.delay(new Adjust(MapView.this.ui.mc.sub(MapView.this.rootpos()), false)); 
    }
    
    private class Adjust extends MapView.Maptest {
      boolean adjust;
      
      Adjust(Coord c, boolean ta) {
        super(c);
        this.adjust = ta;
      }
      
      public void hit(Coord pc, Coord mc) {
        MapView.Plob.this.rc = mc;
        if (this.adjust) {
          MapView.Plob.this.rc = MapView.Plob.this.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2)).add(new Coord(MapView.o1, MapView.o2));
        } else if (MapView.placeGrid > 1 && MapView.placeGrid < 12) {
          int x = MapView.Plob.this.rc.x - (MapView.Plob.this.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2))).x;
          int y = MapView.Plob.this.rc.y - (MapView.Plob.this.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2))).y;
          int off1 = 0;
          int off2 = 0;
          if (MapView.o1 == 0 && MapView.o2 == 0) {
            off1 = -(Math.abs(MapView.placeGrid) - 1);
            off2 = -(Math.abs(MapView.placeGrid) - 1);
          } else {
            off1 = MapView.o1;
            off2 = MapView.o2;
          } 
          x = x - x % MapView.placeGrid + off1;
          y = y - y % MapView.placeGrid + off2;
          MapView.Plob.this.rc = MapView.Plob.this.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2)).add(new Coord(x, y));
        } 
        Gob pl = MapView.this.player();
        if (pl != null && !MapView.Plob.this.freerot)
          MapView.Plob.this.a = MapView.Plob.this.rc.angle(pl.rc); 
        MapView.Plob.this.lastmc = pc;
      }
    }
  }
  
  private class Adjust extends Maptest {
    boolean adjust;
    
    Adjust(Coord c, boolean ta) {
      super(c);
      this.adjust = ta;
    }
    
    public void hit(Coord pc, Coord mc) {
      this.this$1.rc = mc;
      if (this.adjust) {
        this.this$1.rc = this.this$1.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2)).add(new Coord(MapView.o1, MapView.o2));
      } else if (MapView.placeGrid > 1 && MapView.placeGrid < 12) {
        int x = this.this$1.rc.x - (this.this$1.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2))).x;
        int y = this.this$1.rc.y - (this.this$1.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2))).y;
        int off1 = 0;
        int off2 = 0;
        if (MapView.o1 == 0 && MapView.o2 == 0) {
          off1 = -(Math.abs(MapView.placeGrid) - 1);
          off2 = -(Math.abs(MapView.placeGrid) - 1);
        } else {
          off1 = MapView.o1;
          off2 = MapView.o2;
        } 
        x = x - x % MapView.placeGrid + off1;
        y = y - y % MapView.placeGrid + off2;
        this.this$1.rc = this.this$1.rc.div(MCache.tilesz).mul(MCache.tilesz).add(MCache.tilesz.div(2)).add(new Coord(x, y));
      } 
      Gob pl = MapView.this.player();
      if (pl != null && !this.this$1.freerot)
        this.this$1.a = this.this$1.rc.angle(pl.rc); 
      this.this$1.lastmc = pc;
    }
  }
  
  private void unflashol() {
    for (int i = 0; i < this.visol.length; i++) {
      if ((this.olflash & 1 << i) != 0)
        this.visol[i] = this.visol[i] - 1; 
    } 
    this.olflash = 0;
    this.olftimer = 0L;
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "place") {
      Message sdt;
      int a = 0;
      Indir<Resource> res = this.ui.sess.getres(((Integer)args[a++]).intValue());
      if (args.length > a && args[a] instanceof byte[]) {
        sdt = new Message(0, (byte[])args[a++]);
      } else {
        sdt = Message.nil;
      } 
      this.placing = new Plob(res, sdt);
      while (a < args.length) {
        Message odt;
        Indir<Resource> ores = this.ui.sess.getres(((Integer)args[a++]).intValue());
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
      unflashol();
      this.olflash = ((Integer)args[0]).intValue();
      for (int i = 0; i < this.visol.length; i++) {
        if ((this.olflash & 1 << i) != 0)
          this.visol[i] = this.visol[i] + 1; 
      } 
      this.olftimer = System.currentTimeMillis() + ((Integer)args[1]).intValue();
    } else {
      super.uimsg(msg, args);
    } 
  }
  
  public abstract class Maptest implements Delayed {
    public final Coord pc;
    
    public Maptest(Coord c) {
      this.pc = c;
    }
    
    public void run(GOut g) {
      Coord mc;
      GLState.Buffer bk = g.st.copy();
      try {
        GL2 gL2 = g.gl;
        g.st.set(MapView.this.clickbasic(g));
        g.apply();
        gL2.glClear(16640);
        mc = MapView.this.checkmapclick(g, this.pc);
      } finally {
        g.st.set(bk);
      } 
      if (mc != null) {
        hit(this.pc, mc);
      } else {
        nohit(this.pc);
      } 
    }
    
    protected abstract void hit(Coord param1Coord1, Coord param1Coord2);
    
    protected void nohit(Coord pc) {}
  }
  
  public abstract class Hittest implements Delayed {
    private final Coord clickc;
    
    public Hittest(Coord c) {
      this.clickc = c;
    }
    
    public void run(GOut g) {
      Coord mapcl;
      MapView.ClickInfo gobcl;
      GLState.Buffer bk = g.st.copy();
      try {
        GL2 gL2 = g.gl;
        g.st.set(MapView.this.clickbasic(g));
        g.apply();
        gL2.glClear(16640);
        mapcl = MapView.this.checkmapclick(g, this.clickc);
        g.st.set(bk);
        g.st.set(MapView.this.clickbasic(g));
        g.apply();
        gL2.glClear(16384);
        gobcl = MapView.this.checkgobclick(g, this.clickc);
      } finally {
        g.st.set(bk);
      } 
      if (mapcl != null) {
        if (gobcl == null) {
          hit(this.clickc, mapcl, null);
        } else {
          hit(this.clickc, mapcl, gobcl);
        } 
      } else {
        nohit(this.clickc);
      } 
    }
    
    protected abstract void hit(Coord param1Coord1, Coord param1Coord2, MapView.ClickInfo param1ClickInfo);
    
    protected void nohit(Coord pc) {}
  }
  
  private static int getid(Rendered tgt) {
    if (tgt instanceof ResPart)
      return ((ResPart)tgt).partid(); 
    return -1;
  }
  
  private class Click extends Hittest {
    int clickb;
    
    private Click(Coord c, int b) {
      super(c);
      this.clickb = b;
    }
    
    protected void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
      if (inf == null && this.clickb == 3 && !Utils.isCarrying() && Config.smart_space_on_click && !MapView.this.ui.modmeta && !MapView.this.ui.modctrl && !MapView.this.ui.modshift) {
        SmartSpace.work(mc);
        return;
      } 
      MapView.this.autoRecipeOnGobClick(inf, this.clickb);
      int modflags = MapView.this.ui.modflags();
      if (this.clickb == 1 && inf != null && MapView.this.ui.root.cursor.name.contains("kreuz"))
        try {
          String rName = (inf.gob.getres()).name;
          if (rName.toLowerCase().contains("stump")) {
            MapView.this.equipShovelAndClick(pc, mc, this.clickb, modflags, inf, false);
            return;
          } 
        } catch (Exception exception) {} 
      if (this.clickb == 3 && inf != null && !MapView.this.ui.modmeta && !MapView.this.ui.modctrl && !MapView.this.ui.modshift && checkBuildingWithDoorsAndClick(inf))
        return; 
      if (MapView.this.ui.modmeta && !MapView.this.ui.gui.hand.isEmpty()) {
        MapView.this.ui.modmeta = false;
        modflags = MapView.this.ui.modflags();
        MapView.this.ui.modmeta = true;
      } 
      if (MapView.debugtest)
        MapView.this.ui.message("test MV " + modflags, GameUI.MsgType.INFO); 
      if (Config.center)
        mc = mc.div(11).mul(11).add(5, 5); 
      if (inf == null) {
        if (modflags == 0 && MapView.mineHelper && MapView.this.miningHelper() == true)
          if (this.clickb == 3) {
            if (MapView.mineHelperFastMode) {
              MapView.this.ui.wdgmsg(MapView.this.ui.gui.map, "click", new Object[] { (this.this$0.ui.gui.map.player()).sc, (this.this$0.ui.gui.map.player()).rc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf((int)(this.this$0.ui.gui.map.player()).id), (this.this$0.ui.gui.map.player()).rc, Integer.valueOf(0), Integer.valueOf(-1) });
              MapView.this.ui.wdgmsg(MapView.this.ui.gui.map, "click", new Object[] { (this.this$0.ui.gui.map.player()).sc, (this.this$0.ui.gui.map.player()).rc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf((int)(this.this$0.ui.gui.map.player()).id), (this.this$0.ui.gui.map.player()).rc, Integer.valueOf(0), Integer.valueOf(-1) });
              MapView.this.ui.gui.wdgmsg("act", new Object[] { "mine" });
              MapView.this.waitAndGetMiningCursor();
              return;
            } 
            MapView.this.wdgmsg("click", new Object[] { pc, mc, Integer.valueOf(this.clickb), Integer.valueOf(modflags) });
            MapView.this.getMiningCursorBack();
          } else if (this.clickb == 1 && !MapView.this.checkForMineAbleTile(pc, mc)) {
            MapView.this.wdgmsg("click", new Object[] { pc, mc, Integer.valueOf(3), Integer.valueOf(modflags) });
            MapView.this.getMiningCursorBack();
          }  
        MapView.this.wdgmsg("click", new Object[] { pc, mc, Integer.valueOf(this.clickb), Integer.valueOf(modflags) });
      } else {
        if (!MapView.this.ui.modmeta && (MapView.lastShiftRightClick + 5000L > System.currentTimeMillis() || MapView.ffThread > 0) && this.clickb == 3)
          if (MapView.lastClickWasItemAct) {
            delayClickAndCheckForHandContent(modflags, pc, mc, inf);
            return;
          }  
        if (MapView.this.ui.modmeta && MapView.this.ui.gui.hand.isEmpty()) {
          ChatUI.Channel channel = MapView.this.ui.gui.chat.sel;
          if (channel != null && channel instanceof ChatUI.EntryChannel)
            ((ChatUI.EntryChannel)channel).send(String.format("$hl[%d]", new Object[] { Long.valueOf(inf.gob.id) })); 
        } 
        if (inf.ol == null) {
          MapView.this.wdgmsg("click", new Object[] { pc, mc, Integer.valueOf(this.clickb), Integer.valueOf(modflags), Integer.valueOf(0), Integer.valueOf((int)inf.gob.id), inf.gob.rc, Integer.valueOf(0), Integer.valueOf(MapView.access$1900(inf.r)) });
        } else {
          MapView.this.wdgmsg("click", new Object[] { pc, mc, Integer.valueOf(this.clickb), Integer.valueOf(modflags), Integer.valueOf(1), Integer.valueOf((int)inf.gob.id), inf.gob.rc, Integer.valueOf(inf.ol.id), Integer.valueOf(MapView.access$1900(inf.r)) });
        } 
        if (MapView.this.ui.modctrl)
          if (MapView.startCountOnce) {
            MapView.startCountOnce = false;
            MapView.this.countingGobs(MapView.this.getGobName(inf.gob), MapView.this.dist, MapView.this.overlayCount);
          }  
        if (MapView.invOne && MapView.this.ui.modctrl) {
          try {
            if (inf.gob != MapView.this.ui.gui.map.player())
              makeInvisible(inf); 
          } catch (Exception e) {
            MapView.this.ui.message("error5: " + e.getMessage(), GameUI.MsgType.INFO);
          } 
        } else if (MapView.invAll && MapView.this.ui.modctrl) {
          try {
            String name = MapView.this.getGobName(inf.gob);
            Collection<Gob> gobs = MapView.this.ui.sess.glob.oc.getGobs();
            for (Gob gob : gobs) {
              if (gob == MapView.this.ui.gui.map.player())
                continue; 
              try {
                String gobName = MapView.this.getGobName(gob);
                if (gobName.equals(name))
                  gob.hiddenByInv = true; 
              } catch (Exception exception) {}
            } 
          } catch (Exception e) {
            MapView.this.ui.message("error6: " + e.getMessage(), GameUI.MsgType.INFO);
          } 
        } 
        if (MapView.singleRad && MapView.this.ui.modctrl) {
          Gob gob = inf.gob;
          ResDrawable rd = null;
          Composite cmp = null;
          try {
            rd = gob.<ResDrawable>getattr(ResDrawable.class);
            if (rd != null)
              rd.manualRadius = !rd.manualRadius; 
          } catch (Loading loading) {}
          try {
            cmp = gob.<Composite>getattr(Composite.class);
            if (cmp != null)
              cmp.manualRadius = !cmp.manualRadius; 
          } catch (Loading loading) {}
        } 
      } 
      MapView.lastClickWasItemAct = false;
    }
    
    private boolean checkBuildingWithDoorsAndClick(MapView.ClickInfo inf) {
      String[] listOfBuildingsWithDoors = { "hovel", "house", "barn", "igloo", "church", "windmill", "crypt", "mineentrance", "stonetower" };
      String rName = (inf.gob.getres()).name;
      for (String name : listOfBuildingsWithDoors) {
        if (rName.toLowerCase().contains(name)) {
          MapView.this.ui.wdgmsg(MapView.this.ui.gui.map, "click", new Object[] { inf.gob.sc, inf.gob.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)inf.gob.id), inf.gob.rc, Integer.valueOf(0), Integer.valueOf(16) });
          return true;
        } 
      } 
      return false;
    }
    
    public void makeInvisible(MapView.ClickInfo inf) {
      inf.gob.hiddenByInv = true;
    }
    
    private void delayClickAndCheckForHandContent(final int modflags, final Coord pc, final Coord mc, final MapView.ClickInfo inf) {
      (new Thread(new Runnable() {
            public void run() {
              int counter = 0;
              while (MapView.this.ui.gui.hand.isEmpty() && counter <= 10) {
                MapView.sleep(10);
                counter++;
              } 
              if (!MapView.this.ui.gui.hand.isEmpty())
                try {
                  MapView.this.hitSubMethod(pc, mc, inf);
                  return;
                } catch (Exception exception) {} 
              MapView.lastClickWasItemAct = false;
              if (inf.ol == null) {
                MapView.this.wdgmsg("click", new Object[] { this.val$pc, this.val$mc, Integer.valueOf(this.this$1.clickb), Integer.valueOf(this.val$modflags), Integer.valueOf(0), Integer.valueOf((int)this.val$inf.gob.id), this.val$inf.gob.rc, Integer.valueOf(0), Integer.valueOf(MapView.access$1900(this.val$inf.r)) });
              } else {
                MapView.this.wdgmsg("click", new Object[] { this.val$pc, this.val$mc, Integer.valueOf(this.this$1.clickb), Integer.valueOf(this.val$modflags), Integer.valueOf(1), Integer.valueOf((int)this.val$inf.gob.id), this.val$inf.gob.rc, Integer.valueOf(this.val$inf.ol.id), Integer.valueOf(MapView.access$1900(this.val$inf.r)) });
              } 
              if (MapView.this.ui.modctrl)
                (new Thread(new Runnable() {
                      public void run() {
                        MapView.this.startFuelFillerThread(pc, mc, inf);
                      }
                    }"assistantFuelFiller")).start(); 
            }
          }"NoEmptyClick")).start();
    }
  }
  
  public void grab(Grabber grab) {
    this.grab = grab;
  }
  
  public void release(Grabber grab) {
    if (this.grab == grab)
      this.grab = null; 
  }
  
  public boolean mousedown(Coord c, int button) {
    this.parent.setfocus(this);
    if (button == 1) {
      this.LMBdown = true;
      this.mousemoved = false;
    } 
    if ((!Config.laptopcontrols && button == 2) || (Config.laptopcontrols && button == 3 && this.LMBdown)) {
      if (this.camera.click(c)) {
        this.ui.grabmouse(this);
        this.camdrag = true;
      } 
    } else if (this.placing != null) {
      if (this.placing.lastmc != null)
        wdgmsg("place", new Object[] { this.placing.rc, Integer.valueOf((int)(this.placing.a * 180.0D / Math.PI)), Integer.valueOf(button), Integer.valueOf(this.ui.modflags()) }); 
    } else if ((this.grab == null || !this.grab.mmousedown(c, button)) && (!Config.laptopcontrols || !this.LMBdown)) {
      delay(new Click(c, button));
    } 
    return true;
  }
  
  public void mousemove(Coord c) {
    if (this.grab != null)
      this.grab.mmousemove(c); 
    if (this.camdrag) {
      this.camera.drag(c);
      this.mousemoved = true;
    } else if (this.placing != null && (this.placing.lastmc == null || !this.placing.lastmc.equals(c))) {
      this.placing.getClass();
      delay(new Plob.Adjust(c, !this.ui.modctrl));
    } 
  }
  
  public boolean mouseup(Coord c, int button) {
    if ((!Config.laptopcontrols && button == 2) || (Config.laptopcontrols && button == 3 && this.camdrag)) {
      if (this.camdrag) {
        this.camera.release();
        this.ui.grabmouse(null);
        this.camdrag = false;
      } 
    } else if (this.grab != null) {
      this.grab.mmouseup(c, button);
    } else if (Config.laptopcontrols && this.LMBdown && button == 1 && !this.mousemoved && this.placing == null) {
      delay(new Click(c, button));
    } 
    if (button == 1)
      this.LMBdown = false; 
    return true;
  }
  
  public boolean mousewheel(Coord c, int amount) {
    if (this.grab != null && this.grab.mmousewheel(c, amount))
      return true; 
    if (this.ui.modshift) {
      if (this.placing != null) {
        this.placing.freerot = true;
        if (this.ui.modctrl || (Config.laptopcontrols && this.ui.modmeta)) {
          this.placing.a += amount * Math.PI / 16.0D;
        } else {
          this.placing.a = 0.7853981633974483D * Math.round((this.placing.a + amount * Math.PI / 4.0D) / 0.7853981633974483D);
        } 
      } 
      return true;
    } 
    return this.camera.wheel(c, amount);
  }
  
  public boolean drop(Coord cc, Coord ul) {
    delay(new Hittest(cc) {
          public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
            MapView.this.wdgmsg("drop", new Object[] { pc, mc, Integer.valueOf(this.this$0.ui.modflags()) });
          }
        });
    return true;
  }
  
  public boolean iteminteract(Coord cc, Coord ul) {
    delay(new Hittest(cc) {
          public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
            MapView.this.hitSubMethod(pc, mc, inf);
          }
        });
    return true;
  }
  
  public static boolean interruptedPreviousThread = false;
  
  private final Map<String, Console.Command> cmdmap;
  
  private void hitSubMethod(Coord pc, Coord mc, ClickInfo inf) {
    if (inf == null) {
      wdgmsg("itemact", new Object[] { pc, mc, Integer.valueOf(this.ui.modflags()) });
    } else if (this.ui.modctrl && !this.ui.gui.hand.isEmpty()) {
      if (signalToStop)
        try {
          if (inf.gob == ffTarget)
            return; 
        } catch (Exception exception) {} 
      ffTarget = inf.gob;
      startFuelFillerThread(pc, mc, inf);
    } else if (Config.shift_invert_option_checkbox) {
      int sm = this.ui.modshift ? 0 : 1;
      customItemAct(pc, mc, sm, (int)inf.gob.id, inf.gob.rc, getid(inf.r));
    } else {
      customItemAct(pc, mc, this.ui.modflags(), (int)inf.gob.id, inf.gob.rc, getid(inf.r));
    } 
  }
  
  private void startFuelFillerThread(final Coord pc, final Coord mc, final ClickInfo inf) {
    try {
      (new Thread(new Runnable() {
            public void run() {
              if (MapView.ffThread > 0) {
                MapView.interruptedPreviousThread = true;
                MapView.signalToStop = true;
              } 
              int counter = 0;
              while (MapView.ffThread > 0) {
                if (counter > 500) {
                  MapView.ffThread = 0;
                  return;
                } 
                MapView.sleep(10);
                counter++;
              } 
              int handID = MapView.this.getHandID();
              MapView.this.customItemAct(pc, mc, 1, (int)inf.gob.id, inf.gob.rc, MapView.getid(inf.r));
              if (MapView.interruptedPreviousThread)
                MapView.sleep(150); 
              Gob cricket = null;
              if (UI.instance.gui.map.player().getattr(Moving.class) != null && UI.instance.gui.map.player().getattr((Class)Moving.class) instanceof Following)
                try {
                  cricket = MapView.this.getClosestGob("cricket", 10.0D);
                } catch (Exception exception) {} 
              MapView.signalToStop = false;
              MapView.ffThread++;
              counter = 0;
              boolean waiting = true;
              while (!MapView.signalToStop && waiting) {
                if (counter > 300) {
                  MapView.ffThread--;
                  MapView.interruptedPreviousThread = false;
                  return;
                } 
                if (handID != MapView.this.getHandID())
                  waiting = false; 
                if (UI.instance.gui.map.player().getattr(Moving.class) != null)
                  break; 
                MapView.sleep(10);
                counter++;
              } 
              while (!MapView.signalToStop && waiting) {
                if (counter > 500) {
                  MapView.ffThread--;
                  MapView.interruptedPreviousThread = false;
                  return;
                } 
                if (handID != MapView.this.getHandID())
                  waiting = false; 
                if (UI.instance.gui.map.player().getattr(Moving.class) == null)
                  break; 
                MapView.sleep(10);
                counter++;
              } 
              counter = 0;
              if (MapView.interruptedPreviousThread) {
                do {
                
                } while (counter < 300 && ((cricket != null) ? (cricket.getattr(Moving.class) == null) : (UI.instance.gui.map.player().getattr(Moving.class) == null)));
                MapView.sleep(10);
                counter++;
              } 
              MapView.interruptedPreviousThread = false;
              MapView.this.startFuelFiller(inf, pc, mc);
            }
          }"FuelFillerIntegrated")).start();
    } catch (Exception e) {
      ffThread--;
      interruptedPreviousThread = false;
    } 
  }
  
  public boolean globtype(char c, KeyEvent ev) {
    if (Config.laptopcontrols)
      if (c == '+') {
        mousewheel(this.cc, -1);
      } else if (c == '-') {
        mousewheel(this.cc, 1);
      }  
    return false;
  }
  
  public void setcam(String cam) {
    try {
      Class<? extends Camera> camtype = camtypes.get(cam);
      if (camtype == null)
        camtype = camtypes.values().iterator().next(); 
      Constructor<? extends Camera> constructor = camtype.getConstructor(new Class[] { MapView.class });
      this.camera = Utils.<Camera>construct((Constructor)constructor, new Object[] { this });
    } catch (NoSuchMethodException e) {
      e.printStackTrace(System.out);
    } catch (SecurityException e) {
      e.printStackTrace(System.out);
    } catch (IllegalArgumentException e) {
      e.printStackTrace(System.out);
    } 
  }
  
  public class GrabXL implements Grabber {
    private final MapView.Grabber bk;
    
    public boolean mv = false;
    
    public GrabXL(MapView.Grabber bk) {
      this.bk = bk;
    }
    
    public boolean mmousedown(Coord cc, final int button) {
      MapView.this.delay(new MapView.Hittest(cc) {
            public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
              MapView.GrabXL.this.bk.mmousedown(mc, button);
            }
          });
      return true;
    }
    
    public boolean mmouseup(Coord cc, final int button) {
      MapView.this.delay(new MapView.Hittest(cc) {
            public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
              MapView.GrabXL.this.bk.mmouseup(mc, button);
            }
          });
      return true;
    }
    
    public boolean mmousewheel(Coord cc, final int amount) {
      MapView.this.delay(new MapView.Hittest(cc) {
            public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
              MapView.GrabXL.this.bk.mmousewheel(mc, amount);
            }
          });
      return true;
    }
    
    public void mmousemove(Coord cc) {
      if (this.mv)
        MapView.this.delay(new MapView.Hittest(cc) {
              public void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
                MapView.GrabXL.this.bk.mmousemove(mc);
              }
            }); 
    }
  }
  
  private void countingGobs(final String gobName, final int dist, final ArrayList<Integer> oLCount) {
    if (countIsRunning)
      return; 
    countIsRunning = true;
    (new Thread(new Runnable() {
          public void run() {
            MapView.this.ui.message("start counting of: " + gobName + " in range of: " + dist, GameUI.MsgType.INFO);
            boolean noDist = false;
            boolean noOLCount = false;
            ArrayList<Long> iDs = new ArrayList<>();
            int counter = 0;
            int counterOld = 0;
            int iteration = 0;
            if (dist == 0)
              noDist = true; 
            if (oLCount.size() == 0)
              noOLCount = true; 
            while (!MapView.stopCounting) {
              String name = gobName;
              Collection<Gob> gobs = MapView.this.ui.sess.glob.oc.getGobs();
              for (Gob gob : gobs) {
                try {
                  if (iDs.contains(Long.valueOf(gob.id)))
                    continue; 
                  if (!noDist) {
                    Coord pCoord = (MapView.this.ui.gui.map.player()).rc;
                    Coord gCoord = gob.rc;
                    if (gCoord.dist(pCoord) > dist)
                      continue; 
                  } 
                  iDs.add(Long.valueOf(gob.id));
                  if (!noOLCount) {
                    ResDrawable rd = gob.<ResDrawable>getattr(ResDrawable.class);
                    if (rd != null) {
                      StaticSprite sSpr = null;
                      Object tempObj = null;
                      try {
                        Field field = ResDrawable.class.getDeclaredField("spr");
                        field.setAccessible(true);
                        tempObj = field.get(rd);
                        if (tempObj instanceof StaticSprite)
                          sSpr = (StaticSprite)tempObj; 
                      } catch (Exception e) {
                        UI.instance.message("error1: " + e.getMessage(), GameUI.MsgType.INFO);
                      } 
                      if (sSpr != null && 
                        !oLCount.contains(Integer.valueOf(sSpr.parts.length)))
                        continue; 
                    } 
                  } 
                  String gobName = MapView.this.getGobName(gob);
                  if (gobName.toLowerCase().contains(name.toLowerCase()))
                    counter++; 
                } catch (Exception exception) {}
              } 
              if (iteration % 5 == 0 && (counterOld == 0 || counterOld != counter)) {
                counterOld = counter;
                MapView.this.ui.message("counting: [" + gobName + "] in range of: " + dist + " # " + counter, GameUI.MsgType.INFO);
              } 
              iteration++;
              MapView.sleep(500);
            } 
            MapView.stopCounting = false;
            MapView.countIsRunning = false;
            MapView.this.ui.message("[Stopped] counting: [" + gobName + "] in range of: " + dist + " # " + counter, GameUI.MsgType.INFO);
          }
        }"GobCounter")).start();
  }
  
  private void setMods(String[] args) {
    try {
      if (args.length > 2 && args[2].length() > 0 && !args[2].toLowerCase().equals("x"))
        distanceModX = Integer.valueOf(args[2]); 
      if (args.length > 3 && args[3].length() > 0 && !args[3].toLowerCase().equals("x"))
        angleModX = Integer.valueOf(Integer.valueOf(args[3]).intValue() / 9); 
      if (args.length > 4 && args[4].length() > 0 && !args[4].toLowerCase().equals("x"))
        elevModX = Integer.valueOf(Integer.valueOf(args[4]).intValue() / 3); 
      if (args.length > 5 && args[5].length() > 0 && !args[5].toLowerCase().equals("x"))
        scrollModX = Integer.valueOf(args[5]); 
    } catch (Exception e) {
      this.ui.message("err: " + e.getMessage(), GameUI.MsgType.INFO);
    } 
  }
  
  public Map<String, Console.Command> findcmds() {
    return this.cmdmap;
  }
  
  private String getGobName(Gob gob) {
    try {
      String name = null;
      ResDrawable rd = null;
      Composite cmp = null;
      try {
        rd = gob.<ResDrawable>getattr(ResDrawable.class);
        if (rd != null)
          name = ((Resource)rd.res.get()).name; 
      } catch (Loading loading) {}
      try {
        cmp = gob.<Composite>getattr(Composite.class);
        if (cmp != null)
          name = ((Resource)cmp.base.get()).name; 
      } catch (Loading loading) {}
      if (name != null)
        return name; 
    } catch (Exception exception) {}
    return null;
  }
  
  private boolean miningHelper() {
    try {
      Resource curs = this.ui.root.cursor;
      if (curs.name.contains("gfx/hud/curs/mine"))
        return true; 
    } catch (Exception exception) {}
    return false;
  }
  
  public boolean checkForMineAbleTile(Coord pc, Coord mc) {
    Coord coord = mc;
    String tileName = getNameOfTileIfGroundTile(getTile(coord.div(11)));
    if (tileName == null)
      return true; 
    return false;
  }
  
  public boolean isTileInsideMine() {
    long now = System.currentTimeMillis();
    if (ts > now)
      return tim.booleanValue(); 
    tim = Boolean.valueOf(isTileInsideMine2());
    ts = now + 100L;
    return tim.booleanValue();
  }
  
  public boolean isTileInsideMine2() {
    final Glob globTemp = this.ui.sess.glob;
    try {
      Coord mc = (this.ui.gui.map.player()).rc;
      String tileName = getNameOfTileIfGroundTile(getTile(mc.div(11)));
      if (tileName != null && tileName.contains("gfx/tiles/mountain")) {
        float h = this.ui.sess.glob.map.getcz(mc);
        if (h == 0.0F)
          return true; 
      } 
      return false;
    } catch (Exception e) {
      (new Thread(new Runnable() {
            public void run() {
              int x = 0;
              String tileName = null;
              while (x < 100) {
                x++;
                MapView.sleep(100);
                try {
                  Coord mc = (MapView.this.ui.gui.map.player()).rc;
                  tileName = MapView.this.ui.gui.map.getNameOfTileIfGroundTile(MapView.this.ui.gui.map.getTile(mc.div(11)));
                  if (tileName != null) {
                    globTemp.brighten();
                    break;
                  } 
                } catch (Exception exception) {}
              } 
            }
          },  "brightModeHelper")).start();
      return false;
    } 
  }
  
  private void getMiningCursorBack() {
    try {
      (new Thread(new Runnable() {
            public void run() {
              try {
                Gob player = MapView.this.ui.gui.map.player();
                Moving moving = player.<Moving>getattr(Moving.class);
                int counter = 0;
                while (moving == null && counter <= 25) {
                  MapView.sleep(20);
                  moving = player.<Moving>getattr(Moving.class);
                  counter++;
                } 
                if (moving != null)
                  MapView.this.ui.gui.wdgmsg("act", new Object[] { "mine" }); 
              } catch (Exception exception) {}
            }
          }"MCHelp")).start();
    } catch (Exception exception) {}
  }
  
  public void waitAndGetMiningCursor() {
    try {
      (new Thread(new Runnable() {
            public void run() {
              MapView.sleep(100);
              int counter = 0;
              while (!MapView.mineHelper && counter <= 20) {
                MapView.sleep(50);
                counter++;
              } 
              MapView.this.ui.gui.wdgmsg("act", new Object[] { "mine" });
            }
          }"MCHelp")).start();
    } catch (Exception exception) {}
  }
  
  public Gob getClosestGob(String gobName, double maxDist) {
    List<Gob> closeGobs = null;
    Gob closest_gob = null;
    List<String> names = new ArrayList<>();
    Collection<Gob> gobs = Utils.getGobsWithinX(maxDist);
    double distance = 0.0D;
    Iterator<Gob> gobs_iterator = gobs.iterator();
    Gob current_gob = null;
    while (gobs_iterator.hasNext()) {
      current_gob = gobs_iterator.next();
      Coord gob_location = current_gob.rc;
      String nm = "";
      try {
        ResDrawable rd = current_gob.<ResDrawable>getattr(ResDrawable.class);
        Composite cmp = current_gob.<Composite>getattr(Composite.class);
        if (rd != null)
          nm = ((Resource)rd.res.get()).name; 
        if (cmp != null)
          nm = ((Resource)cmp.base.get()).name; 
      } catch (Loading loading) {}
      if (nm.toLowerCase().contains(gobName.toLowerCase()))
        closest_gob = current_gob; 
    } 
    return closest_gob;
  }
  
  public Collection<Gob> getGobsNotPlayerWithinX(double maxDist) {
    Collection<Gob> gobs = this.ui.sess.glob.oc.getGobs();
    Collection<Gob> returnGobs = new ArrayList<>();
    Coord player_location = (this.ui.gui.map.player()).rc;
    Gob player = this.ui.gui.map.player();
    for (Gob gob : gobs) {
      if (gob != player && gob.rc.dist(player_location) <= maxDist)
        returnGobs.add(gob); 
    } 
    return returnGobs;
  }
  
  public Collection<Gob> getGobsNotPlayerWithinX(double maxDist, ArrayList<String> excludeList) {
    Collection<Gob> gobs = this.ui.sess.glob.oc.getGobs();
    Collection<Gob> returnGobs = new ArrayList<>();
    Coord player_location = (this.ui.gui.map.player()).rc;
    Gob player = this.ui.gui.map.player();
    label16: for (Gob gob : gobs) {
      for (String string : excludeList) {
        String gobName = gob.getres().basename().toLowerCase();
        if (gobName.contains(string))
          continue label16; 
      } 
      if (gob != player && gob.rc.dist(player_location) <= maxDist)
        returnGobs.add(gob); 
    } 
    return returnGobs;
  }
  
  private static void sleep(int timeInMiliS) {
    try {
      Thread.sleep(timeInMiliS);
    } catch (InterruptedException interruptedException) {}
  }
  
  public Tiler getTile(Coord c) {
    MCache mcache = this.ui.sess.glob.map;
    Tiler tiler = mcache.tiler(mcache.gettile(c));
    return tiler;
  }
  
  public String getNameOfTileIfGroundTile(Tiler tiler) {
    String result = null;
    if (tiler instanceof GroundTile) {
      GroundTile gt = (GroundTile)tiler;
      String resName = (gt.set.getres()).name;
      result = resName;
    } 
    return result;
  }
  
  private boolean checkNames(String nm) {
    if (nm.contains("terobjs/brazier") || nm.contains("terobjs/torchpost") || nm.contains("terobjs/babybrazier") || nm.endsWith("stove") || nm.contains("terobjs/oven") || nm.contains("terobjs/fireplace") || nm.contains("terobjs/meatsmoker") || nm.contains("terobjs/fineryforge") || nm
      .contains("terobjs/oresmelter") || nm.contains("terobjs/cementationfurnace") || nm.contains("terobjs/kiln") || nm.contains("terobjs/haystack") || nm.contains("terobjs/field") || nm.contains("terobjs/compost") || nm.contains("terobjs/turkeycoop") || nm.contains("terobjs/barrel") || nm
      .contains("terobjs/bigbarrel") || nm.contains("terobjs/farmerbin") || nm.contains("terobjs/ttub") || nm.contains("terobjs/windmillgrinderbottom") || nm.contains("gfx/terobjs/foodtrough") || nm.contains("gfx/terobjs/watertower"))
      return true; 
    return false;
  }
  
  private void startFuelFiller(ClickInfo inf, Coord pc, Coord mc) {
    Gob closest_gob = null;
    Gob current_gob = inf.gob;
    ResDrawable rd = null;
    String nm = "";
    try {
      rd = current_gob.<ResDrawable>getattr(ResDrawable.class);
      if (rd != null)
        nm = ((Resource)rd.res.get()).name; 
    } catch (Loading loading) {
    
    } catch (Exception e) {
      Utils.msgOut("error in FuelFiller");
    } 
    if (checkNames(nm))
      closest_gob = current_gob; 
    int counter = 0;
    int oldID = 0;
    int handID = 0;
    int limit = 2;
    if (closest_gob != null)
      while (!signalToStop) {
        handID = getHandID();
        if (handID == oldID) {
          counter++;
          if (counter > 2)
            signalToStop = true; 
        } else {
          counter = 0;
          oldID = handID;
        } 
        if (!signalToStop) {
          customItemAct(pc, mc, 1, (int)inf.gob.id, inf.gob.rc, getid(inf.r));
          sleep(150);
        } 
        if (this.ui.gui.hand.isEmpty()) {
          for (int i = 0; i < 15; i++) {
            sleep(10);
            if (!this.ui.gui.hand.isEmpty())
              break; 
          } 
          if (this.ui.gui.hand.isEmpty())
            signalToStop = true; 
        } 
      }  
    ffThread--;
    signalToStop = false;
  }
  
  private int getHandID() {
    try {
      return ((GItem)UI.instance.gui.hand.iterator().next()).wdgid();
    } catch (Exception exception) {
      return 0;
    } 
  }
  
  public void customItemAct(Coord pc, Coord mc, int modflags, int gobID, Coord rc, int tgt) {
    lastClickWasItemAct = true;
    lastShiftRightClick = System.currentTimeMillis();
    boolean noCustIAct = false;
    if (this.ui.gui.vhand != null)
      try {
        if (this.ui.gui.vhand.item.name().toLowerCase().contains("wing-a-rang"))
          noCustIAct = true; 
      } catch (Exception exception) {} 
    if (!Config.custom_shift_itemact || noCustIAct) {
      wdgmsg("itemact", new Object[] { pc, mc, Integer.valueOf(modflags), Integer.valueOf(gobID), rc, Integer.valueOf(tgt) });
    } else {
      try {
        if ((modflags & 0x1) == 1) {
          int counter = 0;
          while (this.ui.gui.hand.isEmpty() && counter < 200) {
            counter++;
            sleep(10);
          } 
          if (this.ui.gui.hand.isEmpty())
            return; 
          WItem vhand = this.ui.gui.vhand;
          final List<WItem> items = this.ui.gui.maininv.getSameName(vhand.item.resname(), Boolean.valueOf(this.ui.modmeta));
          modflags--;
          wdgmsg("itemact", new Object[] { pc, mc, Integer.valueOf(modflags), Integer.valueOf(gobID), rc, Integer.valueOf(tgt) });
          modflags++;
          if (!this.customItemActThreadActive) {
            this.customItemActThreadActive = true;
            this.customItemActStop = false;
            try {
              (new Thread(new Runnable() {
                    public void run() {
                      try {
                        int counter = 0;
                        while (!MapView.this.ui.gui.hand.isEmpty() && counter < 500 && !MapView.this.customItemActStop) {
                          counter++;
                          MapView.sleep(10);
                        } 
                        WItem item = items.get(0);
                        if (item != null && !MapView.this.customItemActStop)
                          item.item.wdgmsg("take", new Object[] { Coord.z }); 
                        MapView.this.customItemActThreadActive = false;
                        MapView.this.customItemActStop = false;
                      } catch (Exception exception) {
                      
                      } finally {
                        MapView.this.customItemActThreadActive = false;
                        MapView.this.customItemActStop = false;
                      } 
                    }
                  }"CustomShiftItemAct")).start();
            } catch (Exception e) {
              this.customItemActThreadActive = false;
              this.customItemActStop = false;
            } 
          } 
        } else {
          wdgmsg("itemact", new Object[] { pc, mc, Integer.valueOf(modflags), Integer.valueOf(gobID), rc, Integer.valueOf(tgt) });
        } 
      } catch (Exception exception) {}
    } 
  }
  
  void equipShovelAndClick(final Coord pc, final Coord mc, final int clickb, final int modflags, final ClickInfo inf, final boolean secondCall) {
    (new Thread(new Runnable() {
          public void run() {
            try {
              UI ui = UI.instance;
              GItem gItem = null;
              if ((ui.gui.getEquipory()).slots[7] != null)
                gItem = ((ui.gui.getEquipory()).slots[7]).item; 
              if (gItem != null && gItem == MapView.lastShovel) {
                MapView.this.wdgmsg("click", new Object[] { this.val$pc, this.val$mc, Integer.valueOf(this.val$clickb), Integer.valueOf(this.val$modflags), Integer.valueOf(0), Integer.valueOf((int)this.val$inf.gob.id), this.val$inf.gob.rc, Integer.valueOf(0), Integer.valueOf(MapView.access$1900(this.val$inf.r)) });
                return;
              } 
              if (gItem == null && MapView.thereWasNoShovel) {
                MapView.this.wdgmsg("click", new Object[] { this.val$pc, this.val$mc, Integer.valueOf(this.val$clickb), Integer.valueOf(this.val$modflags), Integer.valueOf(0), Integer.valueOf((int)this.val$inf.gob.id), this.val$inf.gob.rc, Integer.valueOf(0), Integer.valueOf(MapView.access$1900(this.val$inf.r)) });
                return;
              } 
              MapView.thereWasNoShovel = false;
              boolean transferShovelFromInv = false;
              boolean takeShovelFromToolbelt = false;
              boolean takeShovelFromBackPack = false;
              boolean openedToolbelt = false;
              boolean alreadyTopShovelEquipped = false;
              boolean useTileTrick = false;
              WItem targetShovel = null;
              WItem equippedItem = (ui.gui.getEquipory()).slots[7];
              if (equippedItem == null) {
                List<WItem> items = null;
                items = ui.gui.maininv.getSameName(MapView.this.SHOVELSRESNAME[0], Boolean.valueOf(true));
                if (items != null && items.size() > 0) {
                  ((WItem)items.get(0)).item.wdgmsg("transfer", new Object[] { Coord.z });
                  alreadyTopShovelEquipped = true;
                } 
                items = ui.gui.maininv.getSameName(MapView.this.SHOVELSRESNAME[1], Boolean.valueOf(true));
                if (items != null && items.size() > 0) {
                  ((WItem)items.get(0)).item.wdgmsg("transfer", new Object[] { Coord.z });
                  int count = 0;
                  while (count < 200 && 
                    (ui.gui.getEquipory()).slots[7] == null) {
                    MapView.sleep(10);
                    count++;
                  } 
                  alreadyTopShovelEquipped = true;
                } 
              } else {
                String shovelName = equippedItem.item.name();
                if (MapView.this.SHOVELS[0].equals(shovelName) || MapView.this.SHOVELS[1].equals(shovelName))
                  alreadyTopShovelEquipped = true; 
                if (MapView.this.SHOVELS[2].equals(shovelName))
                  targetShovel = equippedItem; 
              } 
              if (!alreadyTopShovelEquipped) {
                ShovelAssist sA = new ShovelAssist();
                for (String shovel : MapView.this.SHOVELSRESNAME) {
                  List<WItem> items = ui.gui.maininv.getSameName(shovel, Boolean.valueOf(true));
                  if (items != null && items.size() > 0) {
                    if (ShovelAssist.access$3500(sA, targetShovel, items.get(0))) {
                      targetShovel = items.get(0);
                      transferShovelFromInv = true;
                    } 
                    break;
                  } 
                } 
                ShovelAssist.access$3600(sA);
                if (sA.invTargetBelt == null) {
                  WItem wItemBelt = (ui.gui.getEquipory()).slots[5];
                  if (wItemBelt != null && (wItemBelt.item
                    .name().toLowerCase().contains("master's belt") || wItemBelt.item
                    .name().toLowerCase().contains("toolbelt"))) {
                    wItemBelt.item.wdgmsg("iact", new Object[] { Coord.z });
                    openedToolbelt = true;
                    int counter = 0;
                    while (counter < 300 && 
                      sA.invTargetBelt == null) {
                      MapView.sleep(10);
                      ShovelAssist.access$3600(sA);
                      counter++;
                    } 
                  } 
                } 
                if (sA.invTargetBelt != null)
                  for (String shovel : MapView.this.SHOVELSRESNAME) {
                    List<WItem> items = sA.invTargetBelt.getSameName(shovel, Boolean.valueOf(true));
                    if (items != null && items.size() > 0) {
                      if (ShovelAssist.access$3500(sA, targetShovel, items.get(0))) {
                        targetShovel = items.get(0);
                        transferShovelFromInv = false;
                        takeShovelFromToolbelt = true;
                      } 
                      break;
                    } 
                  }  
                ShovelAssist.access$3700(sA);
                if (sA.invTargetBackPack != null)
                  for (String shovel : MapView.this.SHOVELSRESNAME) {
                    List<WItem> items = sA.invTargetBackPack.getSameName(shovel, Boolean.valueOf(true));
                    if (items != null && items.size() > 0) {
                      if (ShovelAssist.access$3500(sA, targetShovel, items.get(0))) {
                        targetShovel = items.get(0);
                        transferShovelFromInv = false;
                        takeShovelFromToolbelt = false;
                        takeShovelFromBackPack = true;
                      } 
                      break;
                    } 
                  }  
                boolean done = false;
                if (targetShovel != null && targetShovel != equippedItem) {
                  if (takeShovelFromToolbelt) {
                    String tileName = null;
                    try {
                      tileName = MapView.this.getNameOfTileIfGroundTile(MapView.this.getTile((MapView.this.ui.gui.map.player()).rc.div(11)));
                    } catch (Exception exception) {}
                    if (tileName != null && tileName.length() > 0)
                      for (String biome : MapView.this.biomeNames) {
                        if (tileName.toLowerCase().contains(biome)) {
                          useTileTrick = true;
                          break;
                        } 
                      }  
                    if (useTileTrick && !secondCall) {
                      MapView.this.ui.gui.wdgmsg("act", new Object[] { "dig" });
                      int count = 0;
                      while (!ui.root.cursor.name.equals("gfx/hud/curs/dig") && count < 200) {
                        MapView.sleep(10);
                        count++;
                      } 
                      MapView map = UI.instance.gui.map;
                      Gob player = map.player();
                      if (ShovelAssist.access$3800(sA)) {
                        count = 0;
                        while (equippedItem == (ui.gui.getEquipory()).slots[7] && count < 200) {
                          MapView.sleep(10);
                          count++;
                        } 
                        MapView.this.ui.gui.wdgmsg("act", new Object[] { "destroy" });
                        count = 0;
                        while (!ui.root.cursor.name.contains("kreuz") && count < 200) {
                          MapView.sleep(10);
                          count++;
                        } 
                        done = true;
                      } else {
                        MapView.this.ui.gui.wdgmsg("act", new Object[] { "destroy" });
                        count = 0;
                        while (!ui.root.cursor.name.contains("kreuz") && count < 200) {
                          MapView.sleep(10);
                          count++;
                        } 
                      } 
                    } 
                  } 
                  if (!done) {
                    int count = 0;
                    if (equippedItem != null) {
                      equippedItem.item.wdgmsg("transfer", new Object[] { Coord.z });
                      while (count < 200 && 
                        (ui.gui.getEquipory()).slots[7] != null) {
                        MapView.sleep(10);
                        count++;
                      } 
                    } 
                    if (transferShovelFromInv) {
                      targetShovel.item.wdgmsg("transfer", new Object[] { Coord.z });
                      count = 0;
                      while (count < 200 && 
                        (ui.gui.getEquipory()).slots[7] == null) {
                        MapView.sleep(10);
                        count++;
                      } 
                    } else if (takeShovelFromToolbelt || takeShovelFromBackPack) {
                      WItem vhand = ui.gui.vhand;
                      targetShovel.item.wdgmsg("take", new Object[] { Coord.z });
                      count = 0;
                      while (count < 200 && 
                        vhand == ui.gui.vhand) {
                        MapView.sleep(10);
                        count++;
                      } 
                      vhand = ui.gui.vhand;
                      EquipProxyWdg.drop(7);
                      count = 0;
                      while (count < 200 && 
                        vhand == ui.gui.vhand) {
                        MapView.sleep(10);
                        count++;
                      } 
                    } 
                    if (equippedItem != null && sA.invTargetBelt != null)
                      sA.invTargetBelt.wdgmsg("xfer", new Object[] { Integer.valueOf(1) }); 
                  } 
                } 
                if (openedToolbelt && sA.invTargetBelt != null)
                  ((Window)sA.invTargetBelt.getparent((Class)Window.class)).cbtn.click(); 
              } 
              MapView.this.wdgmsg("click", new Object[] { this.val$pc, this.val$mc, Integer.valueOf(this.val$clickb), Integer.valueOf(this.val$modflags), Integer.valueOf(0), Integer.valueOf((int)this.val$inf.gob.id), this.val$inf.gob.rc, Integer.valueOf(0), Integer.valueOf(MapView.access$1900(this.val$inf.r)) });
            } catch (Exception exception) {
            
            } finally {
              if (MapView.this.ui.root.cursor.name.equals("gfx/hud/curs/dig")) {
                MapView.this.ui.gui.wdgmsg("act", new Object[] { "destroy" });
                int count = 0;
                while (!MapView.this.ui.root.cursor.name.contains("kreuz") && count < 200) {
                  MapView.sleep(10);
                  count++;
                } 
                if (!secondCall)
                  MapView.this.equipShovelAndClick(pc, mc, clickb, modflags, inf, true); 
              } 
            } 
            class ShovelAssist {
              Widget widget = null;
              
              Inventory invTargetBelt = null;
              
              Inventory invTargetBackPack = null;
              
              Window win = null;
              
              private void findToolBelt() {
                for (Widget w : UI.instance.widgets.values()) {
                  if (Inventory.class.isInstance(w)) {
                    this.win = w.<Window>getparent(Window.class);
                    if (this.win != null && this.win.cap.text.trim().toLowerCase().contains("toolbelt")) {
                      this.widget = w;
                      this.invTargetBelt = (Inventory)w;
                    } 
                  } 
                } 
              }
              
              private void findBackPack() {
                for (Widget w : UI.instance.widgets.values()) {
                  if (Inventory.class.isInstance(w)) {
                    this.win = w.<Window>getparent(Window.class);
                    if (this.win != null && this.win.cap.text.trim().toLowerCase().endsWith("pack")) {
                      this.widget = w;
                      this.invTargetBackPack = (Inventory)w;
                    } 
                  } 
                } 
              }
              
              private boolean isOtherShovelBetter(WItem wItemShovelA, WItem wItemShovelB) {
                if (wItemShovelB == null)
                  return false; 
                if (wItemShovelA == null)
                  return true; 
                String shovelA = wItemShovelA.item.name();
                String shovelB = wItemShovelB.item.name();
                for (String shovelName : MapView.this.SHOVELS) {
                  if (shovelName.equals(shovelB) && !shovelName.equals(shovelA))
                    return true; 
                } 
                return false;
              }
              
              private boolean doDigClick() {
                MapView map = UI.instance.gui.map;
                Gob player = map.player();
                Coord playerC = player.rc;
                Coord targetC = null;
                int a = (player.rc.div(11).mul(11)).x;
                int b = (player.rc.div(11).mul(11)).y;
                int m = 0;
                a = player.rc.x - a;
                b = player.rc.y - b;
                Coord cleanN = player.rc.div(11).mul(11);
                Coord centerN = cleanN.add(5, 5);
                ArrayList<Coord> cornerList = new ArrayList<>();
                ArrayList<Coord> centerList = new ArrayList<>();
                ArrayList<Coord> cList = new ArrayList<>();
                for (int i = -2; i <= 2; i++) {
                  for (int k = -2; k <= 2; k++) {
                    Coord corner = player.rc.div(11).mul(11).add(11 * i, 11 * k);
                    Coord center = player.rc.div(11).mul(11).add(11 * i, 11 * k).add(5, 5);
                    String tileName = null;
                    try {
                      tileName = MapView.this.getNameOfTileIfGroundTile(MapView.this.getTile(corner.div(11)));
                    } catch (Exception exception) {}
                    if (tileName != null && tileName.length() > 0)
                      for (String biome : MapView.this.biomeNames) {
                        if (tileName.toLowerCase().contains(biome)) {
                          cList.add(corner);
                          cornerList.add(corner);
                          cList.add(center);
                          centerList.add(center);
                          break;
                        } 
                      }  
                  } 
                } 
                ArrayList<String> list = new ArrayList<>();
                list.add("footprint");
                list.add("field");
                list.add("log");
                list.add("leanto");
                list.add("fern");
                list.add("boundry");
                list.add("claim");
                Collection<Gob> gobs = MapView.this.getGobsNotPlayerWithinX(22.0D, list);
                for (Coord c : cList) {
                  double distPL = c.dist(playerC);
                  boolean passedGobCheck = true;
                  for (Gob g : gobs) {
                    double distGL = c.dist(g.rc);
                    double distPG = playerC.dist(g.rc);
                    if (distGL > distPG && distPL < distGL)
                      continue; 
                    passedGobCheck = false;
                  } 
                  if (passedGobCheck && (targetC == null || targetC.dist(playerC) > distPL))
                    targetC = c; 
                } 
                if (targetC != null) {
                  if (cornerList.contains(targetC)) {
                    m = 0;
                    map.wdgmsg(map, "click", new Object[] { player.sc, targetC, Integer.valueOf(1), Integer.valueOf(m) });
                  } else if (centerList.contains(targetC)) {
                    m = 2;
                    map.wdgmsg(map, "click", new Object[] { player.sc, targetC, Integer.valueOf(1), Integer.valueOf(m) });
                  } else {
                    return false;
                  } 
                  return true;
                } 
                return false;
              }
            };
            GItem currentTool = null;
            if ((MapView.this.ui.gui.getEquipory()).slots[7] != null)
              currentTool = ((MapView.this.ui.gui.getEquipory()).slots[7]).item; 
            MapView.lastShovel = currentTool;
            if (currentTool == null)
              MapView.thereWasNoShovel = true; 
          }
        }"EquipShovelThread")).start();
  }
  
  public void autoRecipeOnGobClick(ClickInfo inf, int clickb) {
    if (inf != null)
      autoRecipeOnGobClick(inf.gob, clickb); 
  }
  
  public void autoRecipeOnGobClick(Gob gob, int clickb) {
    if (!Config.auto_recipe_on_gob_click)
      return; 
    if (gob != null && clickb == 3 && !Utils.isCarrying()) {
      String rName = null;
      try {
        rName = (gob.getres()).name;
      } catch (Exception exception) {}
      if (rName != null && rName.length() > 0)
        for (Map.Entry<String[], List<String>> en : this.gobActionMap.entrySet()) {
          if (rName.toLowerCase().contains(((String[])en.getKey())[0])) {
            if (((String[])en.getKey())[1] != null && ((String[])en.getKey())[1] != "" && (
              (String[])en.getKey())[2] != null && ((String[])en.getKey())[2] != "") {
              List<WItem> items = this.ui.gui.maininv.getSameName(((String[])en.getKey())[1], Boolean.valueOf(true));
              if (items == null || items.size() < Integer.parseInt(((String[])en.getKey())[2]))
                continue; 
            } 
            (new Thread(new Runnable() {
                  public void run() {
                    for (String recipeName : en.getValue()) {
                      MapView.this.ui.gui.wdgmsg("act", new Object[] { "craft", recipeName });
                      try {
                        Thread.sleep(100L);
                      } catch (InterruptedException interruptedException) {}
                    } 
                  }
                })).start();
            break;
          } 
        }  
    } 
  }
  
  public static interface Grabber {
    boolean mmousedown(Coord param1Coord, int param1Int);
    
    boolean mmouseup(Coord param1Coord, int param1Int);
    
    boolean mmousewheel(Coord param1Coord, int param1Int);
    
    void mmousemove(Coord param1Coord);
  }
  
  public static interface Delayed {
    void run(GOut param1GOut);
  }
}
