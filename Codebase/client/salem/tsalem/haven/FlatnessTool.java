package haven;

import java.awt.event.KeyEvent;

class FlatnessTool extends Window implements MapView.Grabber {
  static final String title = "Area selection";
  
  static final String defaulttext = "Select area";
  
  public static float minheight = Float.MAX_VALUE;
  
  public static float maxheight = -minheight;
  
  private final Label text;
  
  private final Label area;
  
  private final MapView mv;
  
  Coord sc;
  
  Coord c1;
  
  Coord c2;
  
  MCache.Overlay ol;
  
  final MCache map;
  
  private Button btnToggle;
  
  private Button btnStatic;
  
  private boolean grabbed = false;
  
  private MapView.GrabXL grab;
  
  private static FlatnessTool instance;
  
  public static boolean staticMode = false;
  
  private static final String STATIC_OFF = "Static: OFF";
  
  private static final String STATIC_ON = "Static: ON";
  
  private static final String GRAB = "Grab";
  
  private static final String RELEASE = "Release";
  
  int c;
  
  Coord lastPlayerPos;
  
  boolean initNeeded;
  
  public FlatnessTool(MapView mv, Coord c, Widget parent) {
    super(c, new Coord(150, 50), parent, "Area selection");
    this.c = 0;
    this.lastPlayerPos = new Coord();
    this.initNeeded = false;
    this.map = this.ui.sess.glob.map;
    this.text = new Label(Coord.z, this, "Select area");
    this.area = new Label(new Coord(0, this.text.sz.y), this, "");
    this.mv = mv;
    mv.getClass();
    this.grab = new MapView.GrabXL(mv, this) {
        public boolean mmousewheel(Coord cc, int amount) {
          return false;
        }
      };
    this.mv.enol(new int[] { 18 });
    this.btnToggle = new Button(new Coord(0, (this.area.c.add(this.area.sz)).y), Integer.valueOf(75), this, "");
    this.btnStatic = new Button(new Coord(80, (this.area.c.add(this.area.sz)).y), Integer.valueOf(75), this, "Static: OFF");
    pack();
  }
  
  public static FlatnessTool instance(UI ui) {
    if (instance != null && instance.parent != ui.gui)
      instance.destroy(); 
    if (instance == null)
      instance = new FlatnessTool(ui.gui.map, new Coord(100, 100), ui.gui); 
    return instance;
  }
  
  public static void close() {
    if (instance != null) {
      instance.ui.destroy(instance);
      instance = null;
    } 
  }
  
  private void grab() {
    this.mv.grab(this.grab);
    this.btnToggle.change("Release");
  }
  
  private void release() {
    this.mv.release(this.grab);
    this.btnToggle.change("Grab");
  }
  
  private void staticOn() {
    this.initNeeded = true;
    this.grabbed = false;
    release();
    this.btnStatic.change("Static: ON");
    staticMode = true;
  }
  
  private void staticOff() {
    this.btnStatic.change("Static: OFF");
    staticMode = false;
  }
  
  public void toggle() {
    instance(UI.instance);
    if (this.ui.modshift) {
      flatnessStaticOnOff();
    } else {
      staticOff();
      this.grabbed = !this.grabbed;
      if (this.grabbed) {
        grab();
      } else {
        release();
      } 
    } 
  }
  
  private void checkflatness(Coord c1, Coord c2) {
    if (c1 == null || c2 == null)
      return; 
    this.c1 = c1;
    this.c2 = c2;
    c2 = c2.add(1, 1);
    minheight = Float.MAX_VALUE;
    maxheight = -minheight;
    float h = 0.0F;
    Coord sz = c2.sub(c1).abs();
    long n = sz.add(1, 1).mul();
    double mean = 0.0D;
    Coord c = new Coord();
    try {
      for (c.x = c1.x; c.x <= c2.x; c.x++) {
        for (c.y = c1.y; c.y <= c2.y; c.y++) {
          h = this.map.getcz(c.mul(MCache.tilesz));
          if (h < minheight)
            minheight = h; 
          if (h > maxheight)
            maxheight = h; 
          mean += h / n;
        } 
      } 
    } catch (LoadingMap e) {
      return;
    } 
    String text = "";
    if (minheight == maxheight) {
      text = text + "Area is flat.";
    } else {
      text = text + "Area isn't flat.";
    } 
    text = text + String.format(" Lowest: [%.0f], Highest: [%.0f], Mean: [%.2f].", new Object[] { Float.valueOf(minheight), Float.valueOf(maxheight), Double.valueOf(mean) });
    settext(text);
    setarea(sz);
    pack();
  }
  
  private void setarea(Coord sz) {
    this.area.settext(String.format("Size: (%d×%d) = %dm²", new Object[] { Integer.valueOf(sz.x), Integer.valueOf(sz.y), Long.valueOf(sz.mul()) }));
  }
  
  public void destroy() {
    if (this.ol != null)
      this.ol.destroy(); 
    this.mv.disol(new int[] { 18 });
    this.mv.release(this.grab);
    instance = null;
    super.destroy();
  }
  
  public boolean mmousedown(Coord mc, int button) {
    Coord c = mc.div(MCache.tilesz);
    if (this.ol != null)
      this.ol.destroy(); 
    this.map.getClass();
    this.ol = new MCache.Overlay(this.map, c, c, 262144);
    this.sc = c;
    this.grab.mv = true;
    this.ui.grabmouse(this.mv);
    checkflatness(c, c);
    return true;
  }
  
  public boolean mmouseup(Coord mc, int button) {
    this.grab.mv = false;
    this.ui.grabmouse(null);
    return true;
  }
  
  public void mmousemove(Coord mc) {
    if (!this.grab.mv)
      return; 
    Coord c = mc.div(MCache.tilesz);
    Coord c1 = new Coord(0, 0);
    Coord c2 = new Coord(0, 0);
    if (c.x < this.sc.x) {
      c1.x = c.x;
      c2.x = this.sc.x;
    } else {
      c1.x = this.sc.x;
      c2.x = c.x;
    } 
    if (c.y < this.sc.y) {
      c1.y = c.y;
      c2.y = this.sc.y;
    } else {
      c1.y = this.sc.y;
      c2.y = c.y;
    } 
    this.ol.update(c1, c2);
    checkflatness(c1, c2);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "reset") {
      this.ol.destroy();
      this.ol = null;
      this.c1 = this.c2 = null;
    } 
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (key == '\n' || key == '\033') {
      close();
      return true;
    } 
    return super.type(key, ev);
  }
  
  public void wdgmsg(Widget wdg, String msg, Object... args) {
    if (wdg == this.cbtn) {
      this.ui.destroy(this);
    } else if (wdg == this.btnToggle) {
      toggle();
    } else if (wdg == this.btnStatic) {
      if (staticMode) {
        staticOff();
      } else {
        staticOn();
      } 
    } else {
      super.wdgmsg(wdg, msg, args);
    } 
  }
  
  private final void settext(String text) {
    this.text.settext(text);
  }
  
  public static void recalcheight() {
    if (instance != null)
      instance.checkflatness(instance.c1, instance.c2); 
  }
  
  public boolean mmousewheel(Coord mc, int amount) {
    return false;
  }
  
  public void flatnessStaticOnOff() {
    if (staticMode) {
      staticOff();
      close();
    } else {
      staticOn();
    } 
  }
  
  public void tick(double dt) {
    if (staticMode) {
      try {
        if (this.c > 5) {
          this.c = 0;
          int size = Config.static_flat_grid_size;
          Coord newPlayerPos = (this.ui.gui.map.player()).rc.div(11.0D);
          if (this.initNeeded || this.ol == null) {
            if (this.ol != null)
              this.ol.destroy(); 
            this.map.getClass();
            this.ol = new MCache.Overlay(this.map, newPlayerPos, newPlayerPos, 262144);
          } 
          if (newPlayerPos.dist(this.lastPlayerPos) > 0.0D || this.initNeeded) {
            this.initNeeded = false;
            this.lastPlayerPos = newPlayerPos;
            Coord range = new Coord(size, size);
            this.ol.update(newPlayerPos.sub(range), newPlayerPos.add(range));
            checkflatness(newPlayerPos.sub(range), newPlayerPos.add(range));
          } 
        } else {
          this.c++;
        } 
      } catch (Exception exception) {}
    } else {
      this.initNeeded = true;
    } 
    super.tick(dt);
  }
}
