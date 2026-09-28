package haven;

import haven.minimap.Radar;
import haven.res.lib.tree.TreeSprite;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Gob implements Sprite.Owner, Skeleton.ModOwner, Rendered {
  private static final Color COLOR_fruit_tree = new Color(205, 205, 255, 200);
  
  private static final Color COLOR_brazier = new Color(255, 105, 180, 200);
  
  private static final float[] C2FA_brazier = Utils.c2fa(COLOR_brazier);
  
  public Coord rc;
  
  public Coord sc;
  
  public Coord3f sczu;
  
  public double a;
  
  public boolean virtual = false;
  
  int clprio = 0;
  
  public long id;
  
  public int frame;
  
  public int initdelay = (int)(Math.random() * 3000.0D) + 3000;
  
  public final Glob glob;
  
  Map<Class<? extends GAttrib>, GAttrib> attr = new HashMap<>();
  
  public Collection<Overlay> ols = new LinkedList<>();
  
  private GobPath path = null;
  
  public boolean hidden;
  
  public boolean hiddenByInv;
  
  public boolean wasHidden = false;
  
  private static List<Long> timeList = new LinkedList<>();
  
  int skip1 = 0;
  
  int smokecap = 3000;
  
  boolean smokeCapReached = false;
  
  Boolean isFurnace = null;
  
  long firstTime = 0L;
  
  long lt = 0L;
  
  boolean didRingOnce = false;
  
  int ringing = 0;
  
  public AtomicBoolean hasIcon;
  
  public final GLState olmod;
  
  public static class Overlay implements Rendered {
    public Indir<Resource> res;
    
    public Message sdt;
    
    public Sprite spr;
    
    public int id;
    
    public boolean delign = false;
    
    public Boolean isSmoke = null;
    
    public Overlay(int id, Indir<Resource> res, Message sdt) {
      this.id = id;
      this.res = res;
      this.sdt = sdt;
      this.spr = null;
    }
    
    public Overlay(Sprite spr) {
      this.id = -1;
      this.res = null;
      this.sdt = null;
      this.spr = spr;
    }
    
    public void draw(GOut g) {}
    
    public boolean setup(RenderList rl) {
      if (this.spr != null)
        rl.add(this.spr, null); 
      return false;
    }
    
    public static interface SetupMod {
      void setupgob(GLState.Buffer param2Buffer);
      
      void setupmain(RenderList param2RenderList);
    }
    
    public static interface CUpd {
      void update(Message param2Message);
    }
    
    public static interface CDel {
      void delete();
    }
  }
  
  public Gob(Glob glob, Coord c) {
    this(glob, c, -1L, 0);
  }
  
  public void ctick(int dt) {
    if (this.hidden || this.hiddenByInv)
      return; 
    int dt2 = dt + this.initdelay;
    this.initdelay = 0;
    for (GAttrib a : this.attr.values()) {
      if (a instanceof Drawable) {
        a.ctick(dt2);
        continue;
      } 
      a.ctick(dt);
    } 
    for (Iterator<Overlay> i = this.ols.iterator(); i.hasNext(); ) {
      Overlay ol = i.next();
      if (ol != null && ol.isSmoke == null)
        try {
          if (this.isFurnace == null)
            if ((getres()).name.toLowerCase().contains("cementationfurnace")) {
              this.isFurnace = Boolean.valueOf(true);
            } else {
              this.isFurnace = Boolean.valueOf(false);
            }  
          if (this.isFurnace != null && this.isFurnace.booleanValue() && ol != null && ol.res != null && ol.res.get() != null && ((Resource)ol.res.get()).name != null && ((Resource)ol.res.get()).name.toLowerCase().contains("ismoke"))
            ol.isSmoke = Boolean.valueOf(true); 
          if (ol.isSmoke == null)
            ol.isSmoke = Boolean.valueOf(false); 
        } catch (Exception e) {
          try {
            ol.isSmoke = null;
          } catch (Exception exception) {}
        }  
      if (ol.spr == null) {
        try {
          ol.sdt.off = 0;
          ol.spr = Sprite.create(this, ol.res.get(), ol.sdt);
        } catch (Loading e) {
          if (e.getMessage() != null && e.getMessage().contains("Too many sounds playing at once."))
            synchronized (timeList) {
              int counter = 0;
              Long now = Long.valueOf(System.currentTimeMillis());
              Iterator<Long> i_timeList = timeList.iterator();
              while (i_timeList.hasNext()) {
                Long occurance = i_timeList.next();
                if (now.longValue() - occurance.longValue() > 200L) {
                  i_timeList.remove();
                  continue;
                } 
                counter++;
              } 
              timeList.add(now);
              if (counter > 4)
                i.remove(); 
            }  
        } catch (Exception exception) {}
        continue;
      } 
      if (ol != null && ol.isSmoke != null && ol.isSmoke.booleanValue()) {
        try {
          Sprite spr = ol.spr;
          Field field = spr.getClass().getDeclaredField("bollar");
          field.setAccessible(true);
          ArrayList<Object> bollar = (ArrayList<Object>)field.get(spr);
          ArrayList<Object> returnArray = new ArrayList();
          if (bollar.size() > this.smokecap) {
            this.smokeCapReached = true;
            returnArray.addAll(bollar.subList(bollar.size() - this.smokecap, bollar.size() - 1));
            field.set(spr, returnArray);
          } 
        } catch (Exception exception) {}
        long now = System.currentTimeMillis();
        if (!this.smokeCapReached) {
          if (this.firstTime == 0L)
            this.firstTime = now + 1500L; 
          if (this.firstTime < now)
            this.smokeCapReached = true; 
        } 
        int fakeDT = 7 * dt * (int)HavenPanel.fps / 50;
        if (this.smokeCapReached && this.skip1 < 10) {
          this.skip1++;
          continue;
        } 
        this.skip1 = 0;
        boolean bool = ol.spr.tick(fakeDT);
        if ((!ol.delign || ol.spr instanceof Overlay.CDel) && bool)
          i.remove(); 
        continue;
      } 
      boolean done = ol.spr.tick(dt);
      if ((!ol.delign || ol.spr instanceof Overlay.CDel) && done)
        i.remove(); 
    } 
    if (this.virtual && this.ols.isEmpty())
      this.glob.oc.remove(this.id); 
    this.loc.tick();
  }
  
  public Overlay findol(int id) {
    for (Overlay ol : this.ols) {
      if (ol.id == id)
        return ol; 
    } 
    return null;
  }
  
  public void tick() {
    if (this.hidden || this.hiddenByInv)
      return; 
    for (GAttrib a : this.attr.values())
      a.tick(); 
  }
  
  public void dispose() {
    for (GAttrib a : this.attr.values())
      a.dispose(); 
  }
  
  public void move(Coord c, double a) {
    Moving m = getattr(Moving.class);
    if (m != null)
      m.move(c); 
    this.rc = c;
    this.a = a;
  }
  
  public Coord3f getc() {
    Moving m = getattr(Moving.class);
    Coord3f ret = (m != null) ? m.getc() : getrc();
    DrawOffset df = getattr(DrawOffset.class);
    if (df != null)
      ret = ret.add(df.off); 
    return ret;
  }
  
  public Coord3f getrc() {
    return new Coord3f(this.rc.x, this.rc.y, this.glob.map.getcz(this.rc));
  }
  
  private Class<? extends GAttrib> attrclass(Class<? extends GAttrib> cl) {
    while (true) {
      Class<?> p = cl.getSuperclass();
      if (p == GAttrib.class)
        return cl; 
      cl = p.asSubclass(GAttrib.class);
    } 
  }
  
  public Gob(Glob glob, Coord c, long id, int frame) {
    this.hasIcon = new AtomicBoolean();
    this.olmod = new GLState() {
        public void apply(GOut g) {}
        
        public void unapply(GOut g) {}
        
        public void prep(GLState.Buffer buf) {
          for (Gob.Overlay ol : Gob.this.ols) {
            if (ol.spr instanceof Gob.Overlay.SetupMod)
              ((Gob.Overlay.SetupMod)ol.spr).setupgob(buf); 
          } 
        }
      };
    this.save = new Save();
    this.loc = new GobLocation();
    this.crashOut = 0;
    this.glob = glob;
    this.rc = c;
    this.id = id;
    this.frame = frame;
  }
  
  public void setattr(GAttrib a) {
    Class<? extends GAttrib> ac = attrclass((Class)a.getClass());
    if (Config.gobpath && ac == Moving.class) {
      if (this.path == null) {
        this.path = new GobPath(this);
        this.ols.add(new Overlay(this.path));
      } 
      this.path.move((Moving)a);
    } 
    if (ac != null && GobIcon.class == ac)
      this.hasIcon.set(true); 
    this.attr.put(ac, a);
  }
  
  public <C extends GAttrib> C getattr(Class<C> c) {
    GAttrib attr = this.attr.get(attrclass(c));
    if (!c.isInstance(attr))
      return null; 
    return c.cast(attr);
  }
  
  public void delattr(Class<? extends GAttrib> c) {
    Class<? extends GAttrib> aClass = attrclass(c);
    this.attr.remove(aClass);
    if (aClass == Moving.class && this.path != null)
      this.path.stop(); 
  }
  
  public void draw(GOut g) {}
  
  public boolean setup(RenderList rl) {
    if (this.hidden || this.hiddenByInv)
      return false; 
    ResDrawable rd = getattr(ResDrawable.class);
    for (Overlay ol : this.ols)
      rl.add(ol, null); 
    for (Overlay ol : this.ols) {
      if (ol.spr instanceof Overlay.SetupMod)
        ((Overlay.SetupMod)ol.spr).setupmain(rl); 
    } 
    GobHealth hlt = getattr(GobHealth.class);
    if (hlt != null)
      rl.prepc(hlt.getfx()); 
    if (Config.blink) {
      Radar.GobBlink blnk = getattr(Radar.GobBlink.class);
      if (blnk != null && blnk.visible())
        rl.prepc(blnk.getfx()); 
    } 
    if (Config.raidermodebraziers) {
      boolean brazier = false;
      if (rd != null && rd.res != null)
        brazier = ((Resource)rd.res.get()).name.contains("brazier"); 
      if (brazier && hlt != null && hlt.asfloat() > 0.5D) {
        Material.Colors fx = new Material.Colors();
        fx.amb = C2FA_brazier;
        fx.dif = C2FA_brazier;
        fx.emi = C2FA_brazier;
        rl.prepc(fx);
      } 
    } 
    if (Config.highlight_claimed_leantos)
      if (rd != null && rd.res != null && ((Resource)rd.res.get()).name.contains("leanto"))
        if (rd.spr != null && ((StaticSprite)rd.spr).parts.length > 1) {
          Material.Colors fx = new Material.Colors();
          Color c = Config.thornbushColour;
          fx.amb = Utils.c2fa(c);
          fx.dif = Utils.c2fa(c);
          fx.emi = Utils.c2fa(c);
          rl.prepc(fx);
        }   
    if (Config.farmermodetrees) {
      boolean thornbush = false;
      if (rd != null && rd.res != null)
        thornbush = ((Resource)rd.res.get()).name.contains("thornbush"); 
      if (thornbush && rd.spr != null && ((StaticSprite)rd.spr).parts.length > 1) {
        Material.Colors fx = new Material.Colors();
        Color c = Config.thornbushColour;
        fx.amb = Utils.c2fa(c);
        fx.dif = Utils.c2fa(c);
        fx.emi = Utils.c2fa(c);
        rl.prepc(fx);
        ringOnce();
      } 
      boolean fruittree = false;
      if (rd != null && rd.res != null)
        fruittree = (((Resource)rd.res.get()).name.contains("apple") || ((Resource)rd.res.get()).name.contains("cherry") || ((Resource)rd.res.get()).name.contains("mulberry") || ((Resource)rd.res.get()).name.contains("pear") || ((Resource)rd.res.get()).name.contains("peach") || ((Resource)rd.res.get()).name.contains("persimmon") || ((Resource)rd.res.get()).name.contains("plum") || ((Resource)rd.res.get()).name.contains("snozberry")); 
      if (fruittree)
        if (rd.spr != null && ((StaticSprite)rd.spr).parts.length > 2 && !rd.sdt.toString().equals("Message(0): 03 00 00 00 ")) {
          Material.Colors fx = new Material.Colors();
          Color c = COLOR_fruit_tree;
          fx.amb = Utils.c2fa(c);
          fx.dif = Utils.c2fa(c);
          fx.emi = Utils.c2fa(c);
          rl.prepc(fx);
        } else if (rd.spr != null && ((StaticSprite)rd.spr).parts.length > 2 && rd.sdt.toString().equals("Message(0): 03 00 00 00 ")) {
          ((StaticSprite)rd.spr).prepc_location = TreeSprite.mkscale(0.5F);
        } else if (rd.spr != null) {
          ((StaticSprite)rd.spr).prepc_location = TreeSprite.mkscale(0.2F);
        }  
    } 
    if (Config.hitbox_on) {
      GobHitbox.BBox bbox = GobHitbox.getBBox(this);
      if (bbox != null)
        rl.add(new Overlay(new GobHitbox(this, bbox.a, bbox.b, false)), null); 
    } 
    GobHighlight highlight = getattr(GobHighlight.class);
    if (highlight != null)
      if (highlight.duration > 0) {
        rl.prepc(highlight.getfx());
      } else {
        delattr((Class)GobHighlight.class);
      }  
    Drawable d = getattr(Drawable.class);
    if (d != null)
      d.setup(rl); 
    Speaking sp = getattr(Speaking.class);
    if (sp != null)
      rl.add(sp.fx, null); 
    KinInfo ki = getattr(KinInfo.class);
    if (ki != null)
      rl.add(ki.fx, null); 
    return false;
  }
  
  public Random mkrandoom() {
    return new Random(this.id);
  }
  
  public Resource getres() {
    Drawable d = getattr(Drawable.class);
    if (d != null && !d.toString().startsWith("haven.res.lib.globfx.GlobEffector"))
      return d.getres(); 
    return null;
  }
  
  public Resource.Neg getneg() {
    Drawable d = getattr(Drawable.class);
    if (d != null)
      return d.getneg(); 
    return null;
  }
  
  public Glob glob() {
    return this.glob;
  }
  
  public double getv() {
    Moving m = getattr(Moving.class);
    if (m == null)
      return 0.0D; 
    return m.getv();
  }
  
  public static final GLState.Slot<Save> savepos = new GLState.Slot<>(GLState.Slot.Type.SYS, Save.class, new GLState.Slot[] { PView.loc });
  
  public final Save save;
  
  public final GobLocation loc;
  
  int crashOut;
  
  public class Save extends GLState {
    public Matrix4f cam = new Matrix4f();
    
    public Matrix4f wxf = new Matrix4f();
    
    public Matrix4f mv = new Matrix4f();
    
    public Projection proj = null;
    
    public void apply(GOut g) {
      this.mv.load(this.cam.load(g.st.cam)).mul1(this.wxf.load(g.st.wxf));
      Projection proj = g.st.<Projection>cur(PView.proj);
      Coord3f s = proj.toscreen(this.mv.mul4(Coord3f.o), g.sz);
      Gob.this.sc = new Coord(s);
      Gob.this.sczu = proj.toscreen(this.mv.mul4(Coord3f.zu), g.sz).sub(s);
      this.proj = proj;
    }
    
    public void unapply(GOut g) {}
    
    public void prep(GLState.Buffer buf) {
      buf.put(Gob.savepos, this);
    }
  }
  
  public class GobLocation extends Location {
    public Coord3f c = null;
    
    private double a = 0.0D;
    
    private final Matrix4f update = null;
    
    public GobLocation() {
      super(Matrix4f.id);
    }
    
    public void tick() {
      try {
        Coord3f c = Gob.this.getc();
        c.y = -c.y;
        if (this.c == null || !c.equals(this.c) || this.a != Gob.this.a)
          update(Transform.makexlate(new Matrix4f(), this.c = c).mul1(Transform.makerot(new Matrix4f(), Coord3f.zu, (float)-(this.a = Gob.this.a)))); 
      } catch (Loading loading) {}
    }
    
    public Location freeze() {
      return new Location(fin(Matrix4f.id));
    }
  }
  
  private void ringOnce() {
    if (Config.ring_on_thornbush)
      try {
        if (!this.didRingOnce && this.crashOut < 100) {
          if (this.ringing == 0) {
            Utils.msgOut("There is a blooming thornbush nearby!");
            Audio.play(Resource.load("/res/sfx/instrument/harp/A"));
          } 
          if (this.ringing == 50)
            Audio.play(Resource.load("/res/sfx/instrument/harp/Asharp")); 
          if (this.ringing == 100) {
            Audio.play(Resource.load("/res/sfx/instrument/harp/B"));
            this.didRingOnce = true;
          } 
          this.ringing++;
        } 
      } catch (Exception e) {
        this.crashOut++;
      }  
  }
  
  public static interface ANotif<T extends GAttrib> {
    void ch(T param1T);
  }
  
  public static interface CDel {
    void delete();
  }
  
  public static interface SetupMod {
    void setupgob(GLState.Buffer param1Buffer);
    
    void setupmain(RenderList param1RenderList);
  }
}
