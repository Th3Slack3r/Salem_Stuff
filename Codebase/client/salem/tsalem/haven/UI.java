package haven;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class UI {
  public Map<Integer, Widget> widgets = new TreeMap<>();
  
  public Map<Widget, Integer> rwidgets = new HashMap<>();
  
  public Coord mc = Coord.z;
  
  public Coord lcc = Coord.z;
  
  public Console cons = new WidgetConsole();
  
  private final Collection<AfterDraw> afterdraws = new LinkedList<>();
  
  private final Collection<AfterDraw> afterttdraws = new LinkedList<>();
  
  public final ActAudio audio = new ActAudio();
  
  private long lastactivity = 0L;
  
  public ItemDrag tIDrag = null;
  
  public MapView tIDragMove = null;
  
  private static long tShift = 0L;
  
  public static int tCount = 0;
  
  private static boolean hShift = false;
  
  public static boolean ui_init_done = false;
  
  private boolean toggleToMinRenderStuff = false;
  
  long lastevent = this.lasttick = System.currentTimeMillis();
  
  public static UI instance;
  
  public GameUI gui;
  
  public RootWidget root;
  
  private Widget keygrab;
  
  private Widget mousegrab;
  
  Receiver rcvr;
  
  public Session sess;
  
  public boolean modshift;
  
  public boolean modctrl;
  
  public boolean modmeta;
  
  public boolean modsuper;
  
  public Object lasttip;
  
  long lasttick;
  
  public Widget mouseon;
  
  public MenuGrid mnu;
  
  private boolean clickForItemDrag;
  
  private int kcode;
  
  public static interface Receiver {
    void rcvmsg(int param1Int, String param1String, Object... param1VarArgs);
  }
  
  public static interface Runner {
    Session run(UI param1UI) throws InterruptedException;
  }
  
  public static interface AfterDraw {
    void draw(GOut param1GOut);
  }
  
  private class WidgetConsole extends Console {
    private WidgetConsole() {
      setcmd("q", new Console.Command() {
            public void run(Console cons, String[] args) {
              HackThread.tg().interrupt();
            }
          });
      setcmd("lo", new Console.Command() {
            public void run(Console cons, String[] args) {
              UI.this.sess.close();
            }
          });
    }
    
    private void findcmds(Map<String, Console.Command> map, Widget wdg) {
      if (wdg instanceof Console.Directory) {
        Map<String, Console.Command> cmds = ((Console.Directory)wdg).findcmds();
        synchronized (cmds) {
          map.putAll(cmds);
        } 
      } 
      for (Widget ch = wdg.child; ch != null; ch = ch.next)
        findcmds(map, ch); 
    }
    
    public Map<String, Console.Command> findcmds() {
      Map<String, Console.Command> ret = super.findcmds();
      findcmds(ret, UI.this.root);
      return ret;
    }
  }
  
  public static class UIException extends RuntimeException {
    public String mname;
    
    public Object[] args;
    
    public UIException(String message, String mname, Object... args) {
      super(message);
      this.mname = mname;
      this.args = args;
    }
  }
  
  public void setreceiver(Receiver rcvr) {
    this.rcvr = rcvr;
  }
  
  public void bind(Widget w, int id) {
    this.widgets.put(Integer.valueOf(id), w);
    this.rwidgets.put(w, Integer.valueOf(id));
  }
  
  public void drawafter(AfterDraw ad) {
    synchronized (this.afterdraws) {
      this.afterdraws.add(ad);
    } 
  }
  
  public void drawaftertt(AfterDraw ad) {
    synchronized (this.afterttdraws) {
      this.afterttdraws.add(ad);
    } 
  }
  
  public void lastdraw(GOut g) {
    synchronized (this.afterttdraws) {
      for (AfterDraw ad : this.afterttdraws)
        ad.draw(g); 
      this.afterttdraws.clear();
    } 
  }
  
  public void tick() {
    long now = System.currentTimeMillis();
    this.root.tick((now - this.lasttick) / 1000.0D);
    this.lasttick = now;
  }
  
  public void draw(GOut g) {
    this.root.draw(g);
    synchronized (this.afterdraws) {
      for (AfterDraw ad : this.afterdraws)
        ad.draw(g); 
      this.afterdraws.clear();
    } 
  }
  
  public void newwidget(int id, String type, int parent, Object[] pargs, Object... cargs) throws InterruptedException {
    synchronized (this) {
      Widget pwdg = this.widgets.get(Integer.valueOf(parent));
      if (pwdg == null)
        throw new UIException("Null parent widget " + parent + " for " + id, type, cargs); 
      Widget wdg = pwdg.makechild(type.intern(), pargs, cargs);
      bind(wdg, id);
      if (type.equals("gameui")) {
        if (Config.alwaystrack) {
          String[] as = { "tracking" };
          wdgmsg(wdg, "act", (Object[])as);
        } 
        String aamm = Config.auto_activate_movement_mode;
        if (aamm != null && aamm != "" && aamm.length() > 0) {
          String[] as2 = { "blk", aamm };
          wdgmsg(wdg, "act", (Object[])as2);
        } 
        FlowerMenu.crimesOn = false;
        ui_init_done = true;
      } 
    } 
  }
  
  public void grabmouse(Widget wdg) {
    if (this.clickForItemDrag && this.tIDragMove != null && wdg instanceof ItemDrag) {
      this.tIDrag = (ItemDrag)wdg;
      this.mousegrab = this.tIDragMove;
    } else {
      this.mousegrab = wdg;
    } 
  }
  
  public void grabkeys(Widget wdg) {
    this.keygrab = wdg;
  }
  
  private void removeid(Widget wdg) {
    if (this.rwidgets.containsKey(wdg)) {
      int id = ((Integer)this.rwidgets.get(wdg)).intValue();
      this.widgets.remove(Integer.valueOf(id));
      this.rwidgets.remove(wdg);
    } 
    for (Widget child = wdg.child; child != null; child = child.next)
      removeid(child); 
  }
  
  public void destroy(Widget wdg) {
    if (this.mousegrab != null && this.mousegrab.hasparent(wdg))
      this.mousegrab = null; 
    if (this.keygrab != null && this.keygrab.hasparent(wdg))
      this.keygrab = null; 
    removeid(wdg);
    wdg.reqdestroy();
  }
  
  public void destroy(int id) {
    synchronized (this) {
      if (this.widgets.containsKey(Integer.valueOf(id))) {
        Widget wdg = this.widgets.get(Integer.valueOf(id));
        destroy(wdg);
        if (wdg == this.gui) {
          this.sess.glob.purge();
          this.gui = null;
          this.cons.clearout();
          this.mnu = null;
        } 
      } 
    } 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    int id;
    synchronized (this) {
      if (!this.rwidgets.containsKey(sender))
        return; 
      id = ((Integer)this.rwidgets.get(sender)).intValue();
    } 
    if (this.rcvr != null)
      this.rcvr.rcvmsg(id, msg, args); 
  }
  
  public void uimsg(int id, String msg, Object... args) {
    synchronized (this) {
      Widget wdg = this.widgets.get(Integer.valueOf(id));
      if (wdg != null) {
        wdg.uimsg(msg.intern(), args);
      } else {
        throw new UIException("Uimsg to non-existent widget " + id, msg, args);
      } 
    } 
  }
  
  private void setmods(InputEvent ev) {
    int mod = ev.getModifiersEx();
    Debug.kf1 = this.modshift = ((mod & 0x40) != 0);
    Debug.kf2 = this.modctrl = ((mod & 0x80) != 0);
    Debug.kf3 = this.modmeta = ((mod & 0x300) != 0);
  }
  
  public UI(Coord sz, Session sess) {
    this.kcode = 0;
    if (instance != null)
      instance.destroy(); 
    instance = this;
    this.root = new RootWidget(this, sz);
    this.widgets.put(Integer.valueOf(0), this.root);
    this.rwidgets.put(this.root, Integer.valueOf(0));
    this.sess = sess;
  }
  
  public void type(KeyEvent ev) {
    be_active();
    setmods(ev);
    ev.setKeyCode(this.kcode);
    if (this.keygrab == null) {
      if (!this.root.type(ev.getKeyChar(), ev))
        this.root.globtype(ev.getKeyChar(), ev); 
    } else {
      this.keygrab.type(ev.getKeyChar(), ev);
    } 
  }
  
  public void keydown(KeyEvent ev) {
    setmods(ev);
    if (checkKeysForShortcuts(ev))
      return; 
    this.kcode = ev.getKeyCode();
    if (this.keygrab == null) {
      if (!this.root.keydown(ev))
        this.root.globtype(false, ev); 
    } else {
      this.keygrab.keydown(ev);
    } 
  }
  
  private boolean checkKeysForShortcuts(KeyEvent ev) {
    int kC = ev.getKeyCode();
    if (this.modctrl) {
      if (kC == 57) {
        OptWnd2.OptUtil.toggleMuteAllAudio(null);
        return true;
      } 
      if (kC == 56) {
        OptWnd2.OptUtil.toggleAutoBackpackBucket(null);
        return true;
      } 
      if (kC == 55) {
        OptWnd2.OptUtil.toggleCustomItemAct(null);
        return true;
      } 
      if (kC == 54) {
        OptWnd2.OptUtil.toggleHitBoxes(null);
        return true;
      } 
      if (kC == 53) {
        if (!this.toggleToMinRenderStuff) {
          OCache.renderDistance = true;
          OCache.maxDist = 110;
          MapView.smallView = true;
          MCache.noFlav = true;
        } else {
          boolean val = Utils.getprefb("render_distance_bool_value", false);
          Config.render_distance_bool_value = val;
          OCache.renderDistance = val;
          int dist = Utils.getprefi("render_distance_int_value", 30);
          Config.render_distance_int_value = dist;
          OCache.maxDist = dist * 11;
          if (!val)
            OCache.undoRenderDistance(); 
          boolean noFlav = Utils.getprefb("mcache_no_flav", val);
          Config.mcache_no_flav = noFlav;
          MCache.noFlav = noFlav;
          boolean smallView = Utils.getprefb("mview_dist_small", val);
          Config.mview_dist_small = smallView;
          MapView.smallView = smallView;
        } 
        this.toggleToMinRenderStuff = !this.toggleToMinRenderStuff;
        Utils.msgOut("[Quick-Min-Render-Distance] is set to: " + this.toggleToMinRenderStuff + " - toggle with CTRL+5");
        return true;
      } 
      if (kC == 52) {
        OptWnd2.OptUtil.changeDisplayFPS(null);
        return true;
      } 
      if (kC == 51) {
        OptWnd2.OptUtil.changeHideSomeGobs(null);
        return true;
      } 
      if (kC == 50) {
        OptWnd2.OptUtil.changeRenderDistance(null);
        return true;
      } 
      if (kC == 49) {
        toggleShift();
        return true;
      } 
    } 
    return false;
  }
  
  private static void toggleShift() {
    OptWnd2.OptUtil.changeInvertShift(Boolean.valueOf(!Config.shift_invert_option_checkbox));
  }
  
  public void keyup(KeyEvent ev) {
    int kC = ev.getKeyCode();
    if (kC == 16)
      hShift = false; 
    setmods(ev);
    this.kcode = 0;
    if (this.keygrab == null) {
      this.root.keyup(ev);
    } else {
      this.keygrab.keyup(ev);
    } 
  }
  
  private Coord wdgxlate(Coord c, Widget wdg) {
    return c.add(wdg.c.inv()).add(wdg.parent.rootpos().inv());
  }
  
  public boolean dropthing(Widget w, Coord c, Object thing) {
    if (w instanceof DropTarget && (
      (DropTarget)w).dropthing(c, thing))
      return true; 
    for (Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
      Coord cc = w.xlate(wdg.c, true);
      if (c.isect(cc, wdg.sz) && 
        dropthing(wdg, c.add(cc.inv()), thing))
        return true; 
    } 
    return false;
  }
  
  public long timesinceactive() {
    return System.currentTimeMillis() - this.lastactivity;
  }
  
  public void be_active() {
    this.lastactivity = System.currentTimeMillis();
  }
  
  public void mousedown(MouseEvent ev, Coord c, int button) {
    be_active();
    setmods(ev);
    this.lcc = this.mc = c;
    if (button == 2 && this.mousegrab instanceof ItemDrag) {
      this.clickForItemDrag = true;
      this.tIDrag = (ItemDrag)this.mousegrab;
    } 
    if (button == 3 && this.clickForItemDrag && this.tIDrag != null && this.mousegrab instanceof MapView) {
      this.tIDragMove = (MapView)this.mousegrab;
      this.mousegrab = this.tIDrag;
      this.mousegrab.mousedown(wdgxlate(c, this.mousegrab), button);
      this.mousegrab = this.tIDragMove;
    } else if (this.mousegrab == null || (this.clickForItemDrag && this.tIDrag != null)) {
      this.root.mousedown(c, button);
    } else {
      this.mousegrab.mousedown(wdgxlate(c, this.mousegrab), button);
    } 
  }
  
  public void mouseup(MouseEvent ev, Coord c, int button) {
    setmods(ev);
    this.mc = c;
    if (this.mousegrab == null || (button == 2 && this.clickForItemDrag)) {
      this.root.mouseup(c, button);
      if (button == 2 && this.clickForItemDrag) {
        if (this.tIDrag != null)
          this.mousegrab = this.tIDrag; 
        this.clickForItemDrag = false;
      } 
    } else if (button == 3 && this.clickForItemDrag && this.tIDrag != null && this.mousegrab instanceof MapView) {
      this.tIDragMove = (MapView)this.mousegrab;
      this.mousegrab = this.tIDrag;
      this.mousegrab.mouseup(wdgxlate(c, this.mousegrab), button);
      this.mousegrab = this.tIDragMove;
    } else {
      this.mousegrab.mouseup(wdgxlate(c, this.mousegrab), button);
    } 
  }
  
  public void mousemove(MouseEvent ev, Coord c) {
    this.mc = c;
    if (this.mousegrab == null) {
      this.root.mousemove(c);
    } else {
      this.mousegrab.mousemove(wdgxlate(c, this.mousegrab));
    } 
  }
  
  public void mousewheel(MouseEvent ev, Coord c, int amount) {
    setmods(ev);
    this.lcc = this.mc = c;
    if (this.mousegrab == null || this.mousegrab instanceof ItemDrag) {
      this.root.mousewheel(c, amount);
    } else {
      this.mousegrab.mousewheel(wdgxlate(c, this.mousegrab), amount);
    } 
  }
  
  public int modflags() {
    return (this.modshift ? 1 : 0) | (this.modctrl ? 2 : 0) | (this.modmeta ? 4 : 0) | (this.modsuper ? 8 : 0);
  }
  
  public void message(String str, GameUI.MsgType type) {
    if (this.cons != null && this.gui != null)
      this.gui.message(str, type); 
  }
  
  public void message(String str, Color msgColor) {
    if (this.cons != null && this.gui != null)
      this.gui.message(str, msgColor); 
  }
  
  public static boolean isCursor(String name) {
    return (instance != null && instance.root != null && instance.root.cursor.name.equals(name));
  }
  
  public void destroy() {
    this.audio.clear();
  }
  
  public static class Cursor {
    public static final String SIFTING = "gfx/hud/curs/sft";
    
    public static final String GOBBLE = "gfx/hud/curs/eat";
    
    public static final String FISH = "gfx/hud/curs/fish";
    
    public static final String MINE = "gfx/hud/curs/mine";
    
    public static final String DIG = "gfx/hud/curs/dig";
  }
}
