package haven;

import haven.res.lib.HomeTrackerFX;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.ender.timer.TimerController;

public class GameUI extends ConsoleHost implements Console.Directory {
  public final String chrid;
  
  private static final int[] fkeys = new int[] { 
      112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 
      122, 123 };
  
  private static final int[] nkeys = new int[] { 
      49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 
      45, 61 };
  
  public final long plid;
  
  public final EquipProxyWdg equipProxy;
  
  public MenuGrid menu;
  
  public CraftWnd craftwnd;
  
  public Tempers tm;
  
  public Widget gobble;
  
  public MapView map;
  
  public LocalMiniMap mmap;
  
  public Fightview fv;
  
  public static final Text.Foundry errfoundry = new Text.Foundry(new Font("SansSerif", 1, 14), new Color(192, 0, 0));
  
  private Text lasterr;
  
  private long errtime;
  
  public InvWindow invwnd;
  
  private Window equwnd;
  
  private Window makewnd;
  
  public Inventory maininv;
  
  public WeightWdg weightwdg;
  
  public MainMenu mainmenu;
  
  public BuddyWnd buddies;
  
  public CharWnd chrwdg;
  
  public Polity polity;
  
  public HelpWnd help;
  
  public OptWnd opts;
  
  public Store storewnd;
  
  public Collection<GItem> hand = new LinkedList<>();
  
  protected WItem vhand;
  
  public ChatUI chat;
  
  public FlatnessTool flat;
  
  public FilterWnd filter = new FilterWnd(this);
  
  public ChatUI.Channel syslog;
  
  public ChatUI.Channel xlog = null;
  
  private HomeTrackerFX.HTrackWdg hrtptr;
  
  public int prog = -1;
  
  private boolean afk = false;
  
  public Indir<Resource>[] belt = (Indir<Resource>[])new Indir[144];
  
  public Indir<Resource> lblk;
  
  public Indir<Resource> dblk;
  
  public String polowner;
  
  public static boolean autoDoors = Config.auto_door_on;
  
  public static boolean debugDoors = false;
  
  public static boolean autoCricket = false;
  
  private List<Class<? extends Widget>> filterout = new ArrayList<>();
  
  public int weight;
  
  public static boolean craftP = false;
  
  public static long craftT = 0L;
  
  public static int craftA = 0;
  
  private static int count = -1;
  
  public static ArrayList<ItemAndValue> iListO;
  
  private Widget attrview;
  
  public static class ItemAndValue {
    public WItem wItem;
    
    public String content;
    
    public ItemAndValue(WItem w, String c) {
      this.wItem = w;
      this.content = c;
    }
    
    public static void prepIList() {
      ArrayList<ItemAndValue> newList = new ArrayList<>();
      List<WItem> items = UI.instance.gui.maininv.getSameName("", Boolean.valueOf(true));
      for (WItem w : items) {
        if (w == null || !w.visible)
          continue; 
        List<ItemInfo> info = w.item.info();
        if (info != null) {
          GItem.ColorInfo cinf = ItemInfo.<GItem.ColorInfo>find(GItem.ColorInfo.class, info);
          if (cinf != null && cinf.olcol().getGreen() == 255) {
            String wC = "";
            try {
              wC = ItemInfo.getContent(info);
            } catch (Exception exception) {}
            newList.add(new ItemAndValue(w, wC));
          } 
        } 
      } 
      if (GameUI.iListO == null) {
        GameUI.iListO = newList;
        return;
      } 
      boolean changed = false;
      for (ItemAndValue iVNew : newList) {
        boolean contains = false;
        for (ItemAndValue iVOld : GameUI.iListO) {
          if (iVOld.wItem == iVNew.wItem && (iVNew.content == null || iVNew.content.equals(iVOld.content)))
            contains = true; 
        } 
        if (!contains)
          changed = true; 
      } 
      if (changed) {
        GameUI.craftA++;
        GameUI.iListO = newList;
      } 
    }
  }
  
  public abstract class Belt extends Widget {
    public Belt(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
    }
    
    public void keyact(final int slot) {
      if (GameUI.this.map != null) {
        Coord mvc = GameUI.this.map.rootxlate(this.ui.mc);
        if (mvc.isect(Coord.z, GameUI.this.map.sz)) {
          GameUI.this.map.getClass();
          GameUI.this.map.delay(new MapView.Hittest(GameUI.this.map, mvc) {
                protected void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
                  if (inf == null) {
                    GameUI.this.wdgmsg("belt", new Object[] { Integer.valueOf(this.val$slot), Integer.valueOf(1), Integer.valueOf(this.this$1.ui.modflags()), mc });
                  } else {
                    GameUI.this.wdgmsg("belt", new Object[] { Integer.valueOf(this.val$slot), Integer.valueOf(1), Integer.valueOf(this.this$1.ui.modflags()), mc, Integer.valueOf((int)inf.gob.id), inf.gob.rc });
                  } 
                }
                
                protected void nohit(Coord pc) {
                  GameUI.this.wdgmsg("belt", new Object[] { Integer.valueOf(this.val$slot), Integer.valueOf(1), Integer.valueOf(this.this$1.ui.modflags()) });
                }
              });
        } 
      } 
    }
  }
  
  @RName("gameui")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      String chrid = (String)args[0];
      int plid = ((Integer)args[1]).intValue();
      return new GameUI(parent, chrid, plid);
    }
  }
  
  public GameUI(Widget parent, String chrid, long plid) {
    super(Coord.z, parent.sz, parent);
    this.progt = null;
    this.dwalking = false;
    this.dwalkang = new Coord();
    this.dkeys = new boolean[] { false, false, false, false };
    this.amIMoving = null;
    this.lastTimeMsg = 0L;
    new ToolBeltWdg(this, "F-Belt", 0, fkeys);
    new ToolBeltWdg(this, "NumericBelt", 6, nkeys);
    this.cmdmap = new TreeMap<>();
    this.cmdmap.put("afk", new Console.Command() {
          public void run(Console cons, String[] args) {
            GameUI.this.afk = true;
            GameUI.this.wdgmsg("afk", new Object[0]);
          }
        });
    this.cmdmap.put("act", new Console.Command() {
          public void run(Console cons, String[] args) {
            Object[] ad = new Object[args.length - 1];
            System.arraycopy(args, 1, ad, 0, ad.length);
            GameUI.this.wdgmsg("act", ad);
          }
        });
    this.cmdmap.put("tool", new Console.Command() {
          public void run(Console cons, String[] args) {
            Widget.gettype(args[1]).create(new Coord(200, 200), GameUI.this, new Object[0]);
          }
        });
    this.cmdmap.put("homestead", new Console.Command() {
          public void run(Console cons, String[] args) {
            GameUI.this.homesteadWalk();
          }
        });
    this.cmdmap.put("flatness", new Console.Command() {
          public void run(Console cons, String[] args) {
            FlatnessTool.instance(GameUI.this.ui);
          }
        });
    this.ui.gui = this;
    this.chrid = chrid;
    this.plid = plid;
    setcanfocus(true);
    setfocusctl(true);
    this.menu = new MenuGrid(Coord.z, this);
    new SeasonImg(new Coord(2, 2), Avaview.dasz, this);
    new Bufflist(new Coord(80, 60), this);
    this.equipProxy = new EquipProxyWdg(new Coord(80, 2), new int[] { 6, 7, 9, 14, 5, 4 }, this);
    this.tm = new Tempers(Coord.z, this);
    this.chat = new ChatUI(Coord.z, 0, this);
    this.syslog = new ChatUI.Log(this.chat, "System");
    if (Config.floating_text_to_console)
      this.xlog = new ChatUI.Log(this.chat, "Log"); 
    this.ui.cons.out = new PrintWriter(new Writer() {
          StringBuilder buf = new StringBuilder();
          
          public void write(char[] src, int off, int len) {
            this.buf.append(src, off, len);
            int p;
            while ((p = this.buf.indexOf("\n")) >= 0) {
              GameUI.this.syslog.append(this.buf.substring(0, p), Color.WHITE);
              this.buf.delete(0, p + 1);
            } 
          }
          
          public void close() {}
          
          public void flush() {}
        });
    this.opts = new OptWnd(this.sz.sub(200, 200).div(2), this);
    this.opts.hide();
    TimerController.init(Config.server);
    makemenu();
    resize(this.sz);
    updateRenderFilter();
  }
  
  public static class MenuButton extends IButton {
    private final int gkey;
    
    private long flash;
    
    private Tex glowmask;
    
    MenuButton(Coord c, Widget parent, String base, int gkey, String tooltip) {
      super(c, parent, Resource.loadimg("gfx/hud/" + base + "up"), Resource.loadimg("gfx/hud/" + base + "down"));
      this.tooltip = Text.render(tooltip);
      this.gkey = (char)gkey;
    }
    
    public void click() {}
    
    protected void toggle() {
      BufferedImage sel = this.up;
      BufferedImage img = this.down;
      this.hover = this.up = img;
      this.down = sel;
    }
    
    public boolean globtype(char key, KeyEvent ev) {
      if (this.gkey != -1 && key == this.gkey) {
        click();
        return true;
      } 
      return super.globtype(key, ev);
    }
    
    public void draw(GOut g) {
      super.draw(g);
      if (this.flash > 0L) {
        if (this.glowmask == null)
          this.glowmask = new TexI(PUtils.glowmask(PUtils.glowmask(this.up.getRaster()), 10, new Color(192, 255, 64))); 
        g = g.reclipl(new Coord(-10, -10), g.sz.add(20, 20));
        double ph = (System.currentTimeMillis() - this.flash) / 1000.0D;
        g.chcolor(255, 255, 255, (int)(128.0D * (Math.cos(ph * Math.PI * 2.0D) * -0.5D + 0.5D)));
        g.image(this.glowmask, Coord.z);
        g.chcolor();
      } 
    }
    
    public void flash(boolean f) {
      if (f) {
        if (this.flash == 0L)
          this.flash = System.currentTimeMillis(); 
      } else {
        this.flash = 0L;
      } 
    }
  }
  
  public static class MenuButtonT extends MenuButton {
    MenuButtonT(Coord c, Widget parent, String base, int gkey, String tooltip) {
      super(c, parent, base, gkey, tooltip);
      this.hover = this.down;
    }
    
    protected void toggle() {
      BufferedImage img = this.up;
      this.up = this.hover;
      this.hover = img;
      this.down = img;
    }
  }
  
  static class Hidewnd extends Window {
    Hidewnd(Coord c, Coord sz, Widget parent, String cap) {
      super(c, sz, parent, cap);
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this && msg.equals("close")) {
        hide();
        return;
      } 
      super.wdgmsg(sender, msg, args);
    }
  }
  
  public static class InvWindow extends Hidewnd {
    public final Map<Inventory, String> names = new HashMap<>();
    
    private Label[] labels = new Label[0];
    
    private final GameUI wui;
    
    private final Label wlbl;
    
    @RName("invwnd")
    public static class $_ implements Widget.Factory {
      public Widget create(Coord c, Widget parent, Object[] args) {
        String cap = (String)args[0];
        return new GameUI.InvWindow(c, new Coord(100, 100), parent, cap, null);
      }
    }
    
    public InvWindow(Coord c, Coord sz, Widget parent, String cap, GameUI wui) {
      super(c, sz, parent, cap);
      if ((this.wui = wui) != null) {
        this.wlbl = new Label(Coord.z, this, "");
        updweight();
      } else {
        this.wlbl = null;
      } 
    }
    
    private void updweight() {
      int weight = this.wui.weight;
      int nr = 0;
      if (this.wui.maininv != null)
        nr = this.wui.maininv.wmap.size(); 
      int cap = 25000;
      Glob.CAttr ca = this.ui.sess.glob.cattr.get("carry");
      if (ca != null)
        cap = ca.comp; 
      this.wlbl.settext(String.format("Carrying %.2f/%.2f kg (%d in inventory)", new Object[] { Double.valueOf(weight / 1000.0D), Double.valueOf(cap / 1000.0D), Integer.valueOf(nr) }));
      this.wlbl.setcolor((weight > cap) ? Color.RED : Color.WHITE);
    }
    
    private void repack() {
      for (Label lbl : this.labels) {
        if (lbl != null)
          lbl.destroy(); 
      } 
      int mw = 0;
      for (Inventory inv : this.names.keySet())
        mw = Math.max(mw, inv.sz.x); 
      List<String> cn = new ArrayList<>();
      for (String nm : this.names.values()) {
        if (!cn.contains(nm))
          cn.add(nm); 
      } 
      Collections.sort(cn);
      Label[] nl = new Label[cn.size()];
      int n = 0, y = 0;
      for (String nm : cn) {
        if (!nm.equals("")) {
          nl[n] = new Label(new Coord(0, y), this, nm);
          y = (nl[n]).c.y + (nl[n]).sz.y + 5;
        } 
        int x = 0;
        int mh = 0;
        for (Map.Entry<Inventory, String> e : this.names.entrySet()) {
          if (((String)e.getValue()).equals(nm)) {
            Inventory inv = e.getKey();
            if (x > 0 && x + inv.sz.x > mw) {
              x = 0;
              y += mh + 5;
              mh = 0;
            } 
            inv.c = new Coord(x, y);
            mh = Math.max(mh, inv.sz.y);
            x += inv.sz.x + 5;
          } 
        } 
        y += mh + 5;
        n++;
      } 
      if (this.wlbl != null)
        this.wlbl.c = new Coord(0, y); 
      this.labels = nl;
      pack();
    }
    
    public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      String nm;
      if (pargs.length > 0) {
        nm = (String)pargs[0];
      } else {
        nm = "";
      } 
      Inventory inv = (Inventory)Widget.gettype(type).create(Coord.z, this, cargs);
      this.names.put(inv, nm);
      repack();
      return inv;
    }
    
    public void cdestroy(Widget w) {
      if (w instanceof Inventory && this.names.containsKey(w)) {
        Inventory inv = (Inventory)w;
        this.names.remove(inv);
        repack();
      } 
    }
    
    public void cresize(Widget w) {
      if (w instanceof Inventory && this.names.containsKey(w))
        repack(); 
    }
  }
  
  private void updhand() {
    if ((this.hand.isEmpty() && this.vhand != null) || (this.vhand != null && !this.hand.contains(this.vhand.item))) {
      this.ui.destroy(this.vhand);
      this.vhand = null;
      this.ui.tIDrag = null;
      GItem.handItemContainerContent = null;
    } 
    if (!this.hand.isEmpty() && this.vhand == null) {
      GItem fi = this.hand.iterator().next();
      this.vhand = new ItemDrag(new Coord(15, 15), this, fi);
      this.ui.tIDrag = (ItemDrag)this.vhand;
    } 
  }
  
  public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    String place = ((String)pargs[0]).intern();
    if (place == "mapview") {
      Coord cc = (Coord)cargs[0];
      this.map = new MapView(Coord.z, this.sz, this, cc, this.plid);
      this.map.lower();
      if (this.mmap != null)
        this.ui.destroy(this.mmap); 
      if (Config.pclaimv)
        this.map.enol(new int[] { 0, 1 }); 
      if (Config.tclaimv)
        this.map.enol(new int[] { 2, 3 }); 
      if (Config.wclaimv)
        this.map.enol(new int[] { 4 }); 
      updateRenderFilter();
      this.mmap = new LocalMiniMap(new Coord(this.sz.x - 250, 15), new Coord(146, 146), this, this.map);
      return this.map;
    } 
    if (place == "fight") {
      this.fv = (Fightview)Widget.gettype(type).create(new Coord(this.sz.x - Fightview.width, 0), this, cargs);
      return this.fv;
    } 
    if (place == "inv") {
      String nm = (pargs.length > 1) ? (String)pargs[1] : null;
      if (this.invwnd == null) {
        this.invwnd = new InvWindow(new Coord(100, 100), Coord.z, this, "Inventory", this);
        this.invwnd.hide();
      } 
      if (nm == null) {
        Inventory inv = (Inventory)this.invwnd.makechild(type, new Object[0], cargs);
        this.maininv = inv;
        this.weightwdg = new WeightWdg(new Coord(10, 100), this);
        return inv;
      } 
      return this.invwnd.makechild(type, new Object[] { nm }, cargs);
    } 
    if (place == "equ") {
      this.equwnd = new Hidewnd(new Coord(400, 10), Coord.z, this, "Equipment");
      Widget equ = Widget.gettype(type).create(Coord.z, this.equwnd, cargs);
      this.equwnd.pack();
      this.equwnd.hide();
      return equ;
    } 
    if (place == "hand") {
      GItem g = (GItem)Widget.gettype(type).create((Coord)pargs[1], this, cargs);
      this.hand.add(g);
      updhand();
      return g;
    } 
    if (place == "craft") {
      final Widget[] mk = { null };
      showCraftWnd();
      if (this.craftwnd != null) {
        mk[0] = Widget.gettype(type).create(new Coord(215, 250), this.craftwnd, cargs);
        this.craftwnd.setMakewindow(mk[0]);
        return mk[0];
      } 
      this.makewnd = new Window(new Coord(350, 100), Coord.z, this, "Crafting") {
          public void wdgmsg(Widget sender, String msg, Object... args) {
            if (sender == this && msg.equals("close")) {
              mk[0].wdgmsg("close", new Object[0]);
              return;
            } 
            super.wdgmsg(sender, msg, args);
          }
          
          public void cdestroy(Widget w) {
            if (w == mk[0]) {
              this.ui.destroy(this);
              GameUI.this.makewnd = null;
            } 
          }
        };
      mk[0] = Widget.gettype(type).create(Coord.z, this.makewnd, cargs);
      this.makewnd.pack();
      return mk[0];
    } 
    if (place == "buddy") {
      this.buddies = (BuddyWnd)Widget.gettype(type).create(new Coord(187, 50), this, cargs);
      this.buddies.hide();
      return this.buddies;
    } 
    if (place == "pol") {
      this.polity = (Polity)Widget.gettype(type).create(new Coord(500, 50), this, cargs);
      this.polity.hide();
      return this.polity;
    } 
    if (place == "chr") {
      this.chrwdg = (CharWnd)Widget.gettype(type).create(new Coord(100, 50), this, cargs);
      this.chrwdg.hide();
      fixattrview(this.chrwdg);
      return this.chrwdg;
    } 
    if (place == "chat")
      return this.chat.makechild(type, new Object[0], cargs); 
    if (place == "party")
      return Widget.gettype(type).create(new Coord(2, 80), this, cargs); 
    if (place == "misc") {
      if (type.contains("ui/hrtptr")) {
        if (this.hrtptr != null) {
          this.hrtptr.dispose();
          this.hrtptr = null;
        } 
        this.hrtptr = new HomeTrackerFX.HTrackWdg(this, Widget.gettype(type).create((Coord)pargs[1], this, cargs));
        return (Widget)this.hrtptr;
      } 
      return Widget.gettype(type).create((Coord)pargs[1], this, cargs);
    } 
    throw new UI.UIException("Illegal gameui child", type, pargs);
  }
  
  public Equipory getEquipory() {
    if (this.equwnd != null)
      for (Widget wdg = this.equwnd.child; wdg != null; wdg = wdg.next) {
        if (wdg instanceof Equipory)
          return (Equipory)wdg; 
      }  
    return null;
  }
  
  public void cdestroy(Widget w) {
    if (w instanceof GItem && this.hand.contains(w)) {
      this.hand.remove(w);
      updhand();
    } else if (w == this.polity) {
      this.polity = null;
    } else if (w == this.chrwdg) {
      this.chrwdg = null;
      this.attrview.destroy();
    } 
  }
  
  public void destroy() {
    super.destroy();
    OptWnd2.close();
    TimerPanel.close();
    DarknessWnd.close();
    FlatnessTool.close();
    OverviewTool.close();
    HotkeyListWindow.close();
    WikiBrowser.close();
    if (this.menu != null)
      this.menu.destroy(); 
    if (this.tm != null)
      this.tm.destroy(); 
    if (this.gobble != null)
      this.gobble.destroy(); 
    if (this.map != null)
      this.map.destroy(); 
    if (this.mmap != null)
      this.mmap.destroy(); 
    if (this.fv != null)
      this.fv.destroy(); 
    if (this.invwnd != null)
      this.invwnd.destroy(); 
    if (this.equwnd != null)
      this.equwnd.destroy(); 
    if (this.makewnd != null)
      this.makewnd.destroy(); 
    if (this.maininv != null)
      this.maininv.destroy(); 
    if (this.mainmenu != null)
      this.mainmenu.destroy(); 
    if (this.buddies != null)
      this.buddies.destroy(); 
    if (this.chrwdg != null)
      this.chrwdg.destroy(); 
    if (this.polity != null)
      this.polity.destroy(); 
    if (this.help != null)
      this.help.destroy(); 
    if (this.chat != null)
      this.chat.destroy(); 
    if (this.syslog != null)
      this.syslog.destroy(); 
    if (this.hrtptr != null)
      this.hrtptr.destroy(); 
  }
  
  private void fixattrview(final CharWnd cw) {
    final IBox box = new IBox(Window.fbox.ctl, Tex.empty, Window.fbox.cbl, Tex.empty, Window.fbox.bl, Tex.empty, Window.fbox.bt, Window.fbox.bb);
    CharWnd.Attr a = (CharWnd.Attr)cw.attrwdgs.child;
    final Coord moff = new Coord(20, 0);
    this.attrview = new Widget(Coord.z, (new Coord(a.expsz.x, cw.attrwdgs.sz.y)).add(moff).add(10, Window.cbtni[0].getHeight() + 10).add(box.bisz()), this) {
        boolean act = false;
        
        Label la;
        
        int cmod = 0;
        
        public void draw(GOut g) {
          if (this.cmod != cw.tmexp) {
            this.cmod = cw.tmexp;
            this.la.settext(String.format("Insp: %d", new Object[] { Integer.valueOf(this.cmod) }));
          } 
          if (GameUI.this.fv != null && !GameUI.this.fv.lsrel.isEmpty())
            return; 
          g.chcolor(0, 0, 0, 128);
          g.frect(box.btloff(), this.sz.sub(box.bisz()));
          g.chcolor();
          super.draw(g);
          box.draw(g, Coord.z, this.sz);
        }
        
        public void presize() {
          this.c = new Coord(GameUI.this.sz.x - this.sz.x, (GameUI.this.menu.c.y - this.sz.y) / 2);
        }
        
        public boolean show(boolean show) {
          return super.show((show && this.act));
        }
        
        private void act(boolean act) {
          Utils.setprefb("attrview", this.act = act);
          show(act);
        }
      };
  }
  
  private void togglecw() {
    if (this.chrwdg != null) {
      if (this.chrwdg.show(!this.chrwdg.visible)) {
        this.chrwdg.raise();
        fitwdg(this.chrwdg);
        setfocus(this.chrwdg);
      } 
      this.attrview.show(!this.chrwdg.visible);
    } 
  }
  
  static Text.Furnace progf = new PUtils.BlurFurn((new Text.Foundry(new Font("serif", 1, 24))).aa(true), 2, 1, new Color(0, 16, 16));
  
  Text progt;
  
  public void updateRenderFilter() {
    this.filterout = new ArrayList<>();
    if (Config.hide_minimap)
      this.filterout.add(LocalMiniMap.class); 
    if (Config.hide_tempers) {
      this.filterout.add(Tempers.class);
      this.tm.hide();
    } else {
      this.tm.show();
    } 
  }
  
  public static ArrayList<String> classNames = new ArrayList<>();
  
  private boolean dwalking;
  
  private Coord dwalkang;
  
  private long dwalkhys;
  
  private float dwalkbase;
  
  private final boolean[] dkeys;
  
  public Moving amIMoving;
  
  public long lastTimeMsg;
  
  private void drawFiltered(GOut g) {
    for (Widget wdg = this.child; wdg != null; wdg = next) {
      Widget next = wdg.next;
      if (wdg.isTempHidden) {
        wdg.isTempHidden = false;
        wdg.visible = true;
      } 
      if (wdg.visible)
        if (this.filterout.contains(wdg.getClass())) {
          wdg.isTempHidden = true;
          wdg.visible = false;
        } else if (HavenPanel.hideUI && !(wdg instanceof MapView)) {
          wdg.isTempHidden = true;
          wdg.visible = false;
        } else {
          Iterator<String> iterator = classNames.iterator();
          while (true) {
            if (iterator.hasNext()) {
              String string = iterator.next();
              if (!(wdg instanceof MapView))
                if (wdg != null && wdg.getClass().getName().toLowerCase().endsWith(string.toLowerCase())) {
                  wdg.isTempHidden = true;
                  wdg.visible = false;
                  break;
                }  
              continue;
            } 
            Coord cc = xlate(wdg.c, true);
            GOut g2 = g.reclip(cc, wdg.sz);
            wdg.draw(g2);
            break;
          } 
        }  
    } 
  }
  
  public void draw(GOut g) {
    drawFiltered(g);
    if (this.prog >= 0) {
      String progs = String.format("%d%%", new Object[] { Integer.valueOf(this.prog) });
      if (this.progt == null || !progs.equals(this.progt.text))
        this.progt = progf.render(progs); 
      g.aimage(this.progt.tex(), new Coord(this.sz.x / 2, this.sz.y * 4 / 10), 0.5D, 0.5D);
    } 
    int by = this.sz.y;
    if (Config.chat_expanded)
      by = Math.min(by, this.chat.c.y); 
    int bx = this.mainmenu.sz.x + 10;
    if (this.cmdline != null) {
      by -= 20;
      drawcmd(g, new Coord(bx, by));
    } else if (this.lasterr != null) {
      if (System.currentTimeMillis() - this.errtime > 3000L) {
        this.lasterr = null;
      } else {
        g.chcolor(0, 0, 0, 192);
        g.frect(new Coord(bx - 2, by - 22), this.lasterr.sz().add(4, 4));
        g.chcolor();
        by -= 20;
        g.image(this.lasterr.tex(), new Coord(bx, by));
      } 
    } 
    if (!Config.chat_expanded)
      this.chat.drawsmall(g, new Coord(bx, by), 50); 
  }
  
  public void tick(double dt) {
    super.tick(dt);
    dwalkupd();
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "err") {
      String err = (String)args[0];
      error(err);
    } else if (msg == "prog") {
      int newProg = -1;
      if (args.length > 0) {
        newProg = ((Integer)args[0]).intValue();
      } else {
        newProg = -1;
      } 
      if (craftP && newProg < this.prog)
        craftA++; 
      this.prog = newProg;
    } else if (msg == "setbelt") {
      int slot = ((Integer)args[0]).intValue();
      if (args.length < 2) {
        this.belt[slot] = null;
      } else {
        this.belt[slot] = this.ui.sess.getres(((Integer)args[1]).intValue());
      } 
    } else if (msg == "ins") {
      this.tm.updinsanity(((Integer)args[0]).intValue());
    } else if (msg == "stm") {
      int[] n = new int[4];
      for (int i = 0; i < 4; i++)
        n[i] = ((Integer)args[i]).intValue(); 
      this.tm.upds(n);
    } else if (msg == "htm") {
      int[] n = new int[4];
      for (int i = 0; i < 4; i++)
        n[i] = ((Integer)args[i]).intValue(); 
      this.tm.updh(n);
    } else if (msg == "gavail") {
      this.tm.gavail = (((Integer)args[0]).intValue() != 0);
    } else if (msg == "cravail") {
      if (args[0] == null) {
        this.tm.cravail((Indir<Resource>)null);
      } else {
        this.tm.cravail(this.ui.sess.getres(((Integer)args[0]).intValue()));
      } 
    } else if (msg == "gobble") {
      boolean g = (((Integer)args[0]).intValue() != 0);
      if (g && this.gobble == null) {
        boolean old = (args.length < 2 || ((Integer)args[1]).intValue() == 0);
        this.tm.hide();
        this.gobble = old ? new OldGobble(Coord.z, this) : new Gobble(Coord.z, this);
        resize(this.sz);
      } else if (!g && this.gobble != null) {
        this.ui.destroy(this.gobble);
        this.gobble = null;
        this.tm.show();
      } 
    } else if (Gobble.msgs.contains(msg)) {
      this.gobble.uimsg(msg, args);
    } else if (msg == "polowner") {
      String o = (String)args[0];
      boolean n = (((Integer)args[1]).intValue() != 0);
      if (o.length() == 0) {
        o = null;
      } else {
        o = o.intern();
      } 
      if (o != this.polowner) {
        if (this.map != null)
          if (o == null) {
            if (this.polowner != null)
              this.map.setpoltext("Leaving " + this.polowner); 
          } else {
            this.map.setpoltext("Entering " + o);
          }  
        this.polowner = o;
      } 
    } else if (msg == "dblk") {
      int id = ((Integer)args[0]).intValue();
      this.dblk = (id < 0) ? null : this.ui.sess.getres(id);
    } else if (msg == "lblk") {
      int id = ((Integer)args[0]).intValue();
      this.lblk = (id < 0) ? null : this.ui.sess.getres(id);
    } else if (msg == "showhelp") {
      Indir<Resource> res = this.ui.sess.getres(((Integer)args[0]).intValue());
      if (this.help == null) {
        this.help = new HelpWnd(this.sz.div(2).sub(150, 200), this, res);
      } else {
        this.help.res = res;
      } 
    } else if (msg == "weight") {
      this.weight = ((Integer)args[0]).intValue();
      if (this.invwnd != null)
        this.invwnd.updweight(); 
      if (this.weightwdg != null) {
        this.weightwdg.update(Integer.valueOf(this.weight));
        OverviewTool.instance(this.ui).force_update();
      } 
    } else {
      super.uimsg(msg, args);
    } 
  }
  
  public void wdgmsg(String msg, Object... args) {
    super.wdgmsg(msg, args);
    if (msg.equals("belt"))
      checkBelt(args); 
  }
  
  private void checkBelt(Object... args) {
    try {
      int index = ((Integer)args[0]).intValue();
      Indir<Resource> indir = this.belt[index];
      if (indir != null)
        try {
          Resource res = indir.get();
          if (this.menu.isCrafting(res))
            showCraftWnd(); 
          if (this.craftwnd != null)
            this.craftwnd.select(res, false); 
        } catch (Loading loading) {} 
    } catch (Exception exception) {}
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.menu) {
      wdgmsg(msg, args);
      return;
    } 
    if (sender == this.buddies && msg == "close") {
      this.buddies.hide();
    } else if (sender == this.polity && msg == "close") {
      this.polity.hide();
    } else if (sender == this.chrwdg && msg == "close") {
      this.chrwdg.hide();
    } else {
      if (sender == this.help && msg == "close") {
        this.ui.destroy(this.help);
        this.help = null;
        return;
      } 
      if (sender == this.storewnd && msg == "close") {
        this.ui.destroy(this.storewnd);
        this.storewnd = null;
        return;
      } 
    } 
    super.wdgmsg(sender, msg, args);
  }
  
  void fitwdg(Widget wdg) {
    wdg.render_c = new Coord(wdg.c);
    if (wdg.render_c.x < 0)
      wdg.render_c.x = 0; 
    if (wdg.render_c.y < 0)
      wdg.render_c.y = 0; 
    if (wdg.render_c.x + wdg.sz.x > this.sz.x)
      this.sz.x -= wdg.sz.x; 
    if (wdg.render_c.y + wdg.sz.y > this.sz.y)
      this.sz.y -= wdg.sz.y; 
    wdg.c = wdg.render_c;
  }
  
  public static boolean linMoveActive = false;
  
  private void dwalkupd() {
    Coord a = new Coord();
    if (this.dkeys[0])
      a = a.add(1, 0); 
    if (this.dkeys[1])
      a = a.add(0, 1); 
    if (this.dkeys[2])
      a = a.add(-1, 0); 
    if (this.dkeys[3])
      a = a.add(0, -1); 
    long now = System.currentTimeMillis();
    if (a.x != 0 || a.y != 0) {
      this.amIMoving = null;
      try {
        this.amIMoving = this.ui.gui.map.player().<Moving>getattr(Moving.class);
      } catch (Exception exception) {}
      if (this.amIMoving != null) {
        if (autoCricket && this.amIMoving instanceof Following && this.lastTimeMsg < now) {
          float da = this.dwalkbase + (float)a.angle(Coord.z);
          wdgmsg("dwalk", new Object[] { Integer.valueOf((int)(da / 6.283185307179586D * 1000.0D)) });
          this.dwalkang = a;
          this.lastTimeMsg = now + 900L;
        } 
      } else if (autoDoors && linMoveActive && this.lastTimeMsg < now + 400L) {
        linMoveActive = false;
        try {
          getThatDoorOpen(a);
        } catch (Exception e) {
          UI.instance.message("error3: " + e.getMessage() + e.toString(), MsgType.INFO);
        } 
      } 
    } 
    if (!a.equals(this.dwalkang) && now > this.dwalkhys) {
      if (a.x == 0 && a.y == 0) {
        linMoveActive = false;
        wdgmsg("dwalk", new Object[0]);
      } else {
        float da = this.dwalkbase + (float)a.angle(Coord.z);
        wdgmsg("dwalk", new Object[] { Integer.valueOf((int)(da / 6.283185307179586D * 1000.0D)) });
        this.lastTimeMsg = now + 900L;
        linMoveActive = true;
      } 
      this.dwalkang = a;
    } 
  }
  
  private void getThatDoorOpen(Coord a) {
    int wasd = 0;
    if (a.x == 1 && a.y == 0) {
      wasd = 0;
    } else if (a.x == 1 && a.y == 1) {
      wasd = 125;
    } else if (a.x == 0 && a.y == 1) {
      wasd = 250;
    } else if (a.x == -1 && a.y == 1) {
      wasd = 375;
    } else if (a.x == -1 && a.y == 0) {
      wasd = 500;
    } else if (a.x == -1 && a.y == -1) {
      wasd = 625;
    } else if (a.x == 0 && a.y == -1) {
      wasd = 750;
    } else if (a.x == 1 && a.y == -1) {
      wasd = 875;
    } 
    int angle = ((int)(this.dwalkbase / 6.283185307179586D * 1000.0D) + 1750) % 1000;
    int wAngle = (angle + wasd) % 1000;
    if (debugDoors)
      UI.instance.message("wAngle: " + wAngle + " wasd: " + wasd, MsgType.INFO); 
    String[] gates = { "gate", "cp" };
    Collection<Gob> gobsWithinX = getGobsWithinX(33.0D);
    Collection<Gob> gobs = getGobsWithNames(gobsWithinX, gates);
    Coord pC = (this.ui.gui.map.player()).rc;
    ArrayList<Gob> doors = new ArrayList<>();
    ArrayList<Gob> corners = new ArrayList<>();
    for (Gob gob : gobs) {
      ResDrawable rd = null;
      String nm = "";
      try {
        rd = gob.<ResDrawable>getattr(ResDrawable.class);
        if (rd != null)
          nm = ((Resource)rd.res.get()).name; 
      } catch (Loading loading) {}
      if (nm.toLowerCase().contains("gate")) {
        doors.add(gob);
        continue;
      } 
      if (nm.toLowerCase().endsWith("cp") && !nm.toLowerCase().contains("grape"))
        corners.add(gob); 
    } 
    boolean doorFound = false;
    for (Gob gob : doors) {
      if (debugDoors) {
        UI.instance.message("W: 500-100, E: 0-500, S: 250-750, N: 750-250", MsgType.INFO);
        UI.instance.message("gX: " + gob.rc.x + "gY: " + gob.rc.y, MsgType.INFO);
        UI.instance.message("pX: " + pC.x + "pY: " + pC.y, MsgType.INFO);
      } 
      if (debugDoors)
        UI.instance.message("check1", MsgType.INFO); 
      int cornersNr = 0;
      int dist = 0;
      for (Gob gobC : corners) {
        if (gob.rc.x == gobC.rc.x && gob.rc.dist(gobC.rc) <= 18.0D) {
          cornersNr++;
          if (debugDoors)
            UI.instance.message("x corners ++", MsgType.INFO); 
        } 
      } 
      if (cornersNr == 2 && ((gob.rc.x < pC.x && wAngle >= 505 && wAngle <= 995) || (gob.rc.x > pC.x && wAngle >= 5 && wAngle <= 495))) {
        dist = Math.abs(Math.abs(pC.x) - Math.abs(gob.rc.x));
        if (debugDoors)
          UI.instance.message("dist x: " + dist, MsgType.INFO); 
      } 
      cornersNr = 0;
      for (Gob gobC : corners) {
        if (gob.rc.y == gobC.rc.y && gob.rc.dist(gobC.rc) <= 18.0D) {
          cornersNr++;
          if (debugDoors)
            UI.instance.message("y corners ++", MsgType.INFO); 
        } 
      } 
      if (cornersNr == 2)
        if ((gob.rc.y < pC.y && ((wAngle >= 755 && wAngle <= 1000) || (wAngle >= 0 && wAngle <= 245))) || (gob.rc.y > pC.y && wAngle >= 255 && wAngle <= 745)) {
          dist = Math.abs(Math.abs(pC.y) - Math.abs(gob.rc.y));
          if (debugDoors)
            UI.instance.message("dist y: " + dist, MsgType.INFO); 
        }  
      if (dist >= 5 && dist <= 15) {
        doorFound = true;
        this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { gob.sc, gob.rc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf((int)gob.id), gob.rc, Integer.valueOf(0), Integer.valueOf(-1) });
        final Coord aT = a;
        (new Thread(new Runnable() {
              public void run() {
                int exitCounter = 0;
                while (exitCounter < 100 && GameUI.this.ui.gui.map.player().getattr(Moving.class) == null) {
                  try {
                    Thread.sleep(10L);
                  } catch (InterruptedException interruptedException) {}
                  exitCounter++;
                } 
                exitCounter = 0;
                while (exitCounter < 100 && GameUI.this.ui.gui.map.player().getattr(Moving.class) != null) {
                  try {
                    Thread.sleep(10L);
                  } catch (InterruptedException interruptedException) {}
                  exitCounter++;
                } 
                float da = GameUI.this.dwalkbase + (float)aT.angle(Coord.z);
                GameUI.this.wdgmsg("dwalk", new Object[] { Integer.valueOf((int)(da / 6.283185307179586D * 1000.0D)) });
                GameUI.this.lastTimeMsg = System.currentTimeMillis() + 900L;
                GameUI.linMoveActive = true;
              }
            }"MoveMe")).start();
        break;
      } 
    } 
    if (!doorFound) {
      Collection<Gob> gobsX = getGobsWithinX(120.0D);
      String[] names = { 
          "house", "hovel", "barn", "igloo", "windmill", "crypt", "mineentrance", "downhole", "mineexit", "door", 
          "staircase", "church", "stonetower" };
      Collection<Gob> gobsN = getGobsWithNames(gobsX, names);
      Gob targetGob = null;
      String targetName = "";
      for (Gob g : gobsN) {
        if ((g.rc.y >= pC.y || ((wAngle < 755 || wAngle > 1000) && (wAngle < 0 || wAngle > 245))) && (g.rc.y <= pC.y || wAngle < 255 || wAngle > 745) && (g.rc.x >= pC.x || wAngle < 505 || wAngle > 995) && (g.rc.x <= pC.x || wAngle < 5 || wAngle > 495))
          continue; 
        targetGob = g;
        float dist = 100000.0F;
        ResDrawable rd = null;
        String nm = "";
        try {
          dist = this.ui.gui.map.player().getrc().dist(g.getrc());
          rd = g.<ResDrawable>getattr(ResDrawable.class);
          if (rd != null)
            nm = ((Resource)rd.res.get()).name.toLowerCase(); 
        } catch (Exception e) {
          UI.instance.message("error4: " + e.getMessage(), MsgType.INFO);
        } 
        targetName = nm;
        if (nm.contains("door")) {
          if (dist < 22.0F) {
            this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(-1) });
            break;
          } 
          continue;
        } 
        if (dist < 40.0F && nm.contains("hovel")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
        if (dist < 70.0F && nm.contains("house")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
        if (dist < 120.0F && nm.contains("barn")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
        if (dist < 50.0F && nm.contains("igloo")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
        if (dist < 70.0F && nm.contains("church")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
        if (dist < 40.0F && nm.contains("windmill")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
        if (dist < 50.0F && nm.contains("crypt")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
        if (dist < 30.0F && nm.contains("mineentrance")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
        if (dist < 20.0F && nm.contains("downhole")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(-1) });
          break;
        } 
        if (dist < 30.0F && nm.contains("mineexit")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(-1) });
          break;
        } 
        if (dist < 33.0F && nm.contains("staircase")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(-1) });
          break;
        } 
        if (dist < 40.0F && nm.contains("stonetower")) {
          this.ui.wdgmsg(this.ui.gui.map, "click", new Object[] { g.sc, g.sc, Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf((int)g.id), g.rc, Integer.valueOf(0), Integer.valueOf(16) });
          break;
        } 
      } 
      if (targetGob != null && debugDoors)
        tOut(targetName + " " + targetGob.getrc().dist(this.ui.gui.map.player().getrc())); 
    } 
  }
  
  private void tOut(String s) {
    UI.instance.message(s, MsgType.INFO);
  }
  
  private Collection<Gob> getGobsWithNames(Collection<Gob> gobs, String[] names) {
    Collection<Gob> returnGobs = new ArrayList<>();
    for (Gob gob : gobs) {
      ResDrawable rd = null;
      String nm = "";
      try {
        rd = gob.<ResDrawable>getattr(ResDrawable.class);
        if (rd != null)
          nm = ((Resource)rd.res.get()).name; 
      } catch (Loading loading) {}
      for (String name : names) {
        if (nm.toLowerCase().contains(name.toLowerCase())) {
          returnGobs.add(gob);
          break;
        } 
      } 
    } 
    return returnGobs;
  }
  
  public Collection<Gob> getGobsWithinX(double maxDist) {
    Collection<Gob> gobs = this.ui.sess.glob.oc.getGobs();
    Collection<Gob> returnGobs = new ArrayList<>();
    Coord player_location = (this.ui.gui.map.player()).rc;
    for (Gob gob : gobs) {
      if (gob.rc.dist(player_location) <= maxDist)
        returnGobs.add(gob); 
    } 
    return returnGobs;
  }
  
  private int dwalkkey(int key) {
    if (key == 87 || key == 38 || key == 224)
      return 0; 
    if (key == 68 || key == 39 || key == 227)
      return 1; 
    if (key == 83 || key == 40 || key == 225)
      return 2; 
    if (key == 65 || key == 37 || key == 226)
      return 3; 
    throw new Error();
  }
  
  private void dwalkdown(KeyEvent ev) {
    if (!this.dwalking) {
      if (MapView.ffThread > 0) {
        MapView.signalToStop = true;
        MapView.interruptedPreviousThread = true;
      } 
      FlowerMenu.interruptReceived = true;
      this.dwalking = true;
      this.dwalkbase = -this.map.camera.angle();
      this.ui.grabkeys(this);
    } 
    int k = dwalkkey(ev.getKeyCode());
    this.dkeys[k] = true;
    this.dwalkhys = ev.getWhen();
  }
  
  private void dwalkup(KeyEvent ev) {
    int k = dwalkkey(ev.getKeyCode());
    this.dkeys[k] = false;
    this.dwalkhys = ev.getWhen() + 100L;
    if (!this.dkeys[0] && !this.dkeys[1] && !this.dkeys[2] && !this.dkeys[3]) {
      this.dwalking = false;
      this.ui.grabkeys(null);
    } 
  }
  
  private static final Tex menubg = Resource.loadtex("gfx/hud/menubg");
  
  private static final Tex menubgfull = Resource.loadtex("gfx/hud/menubgfull");
  
  public class MainMenu extends Widget {
    public final GameUI.MenuButton invb;
    
    public final GameUI.MenuButton equb;
    
    public final GameUI.MenuButton chrb;
    
    public final GameUI.MenuButton budb;
    
    public final GameUI.MenuButton polb;
    
    public final GameUI.MenuButton optb;
    
    public final GameUI.MenuButton clab;
    
    public final GameUI.MenuButton towb;
    
    public final GameUI.MenuButton warb;
    
    public final GameUI.MenuButton ptrb;
    
    public final GameUI.MenuButton lndb;
    
    public final GameUI.MenuButton chatb;
    
    public final GameUI.MenuButton hwab;
    
    public boolean hpv = true;
    
    public boolean pv = (this.hpv && !Config.hptr);
    
    boolean full = Config.mainmenu_full;
    
    public GameUI.MenuButton[] tohide = new GameUI.MenuButton[] { this.invb = new GameUI.MenuButton(new Coord(4, 8), this, "inv", 9, "Inventory (Tab)") {
          int seq = 0;
          
          public void click() {
            if (GameUI.this.invwnd != null && GameUI.this.invwnd.show(!GameUI.this.invwnd.visible)) {
              GameUI.this.invwnd.raise();
              GameUI.this.fitwdg(GameUI.this.invwnd);
            } 
          }
          
          public void tick(double dt) {
            if (GameUI.this.maininv != null)
              if (GameUI.this.invwnd.visible) {
                this.seq = GameUI.this.maininv.newseq;
                flash(false);
              } else if (GameUI.this.maininv.newseq != this.seq) {
                flash(true);
              }  
          }
        }, this.equb = new GameUI.MenuButton(new Coord(62, 8), this, "equ", 5, "Equipment (Ctrl+E)") {
          public void click() {
            if (GameUI.this.equwnd != null && GameUI.this.equwnd.show(!GameUI.this.equwnd.visible)) {
              GameUI.this.equwnd.raise();
              GameUI.this.fitwdg(GameUI.this.equwnd);
            } 
          }
        }, this.chrb = new GameUI.MenuButton(new Coord(120, 8), this, "chr", 20, "Studying (Ctrl+T)") {
          public void click() {
            GameUI.this.togglecw();
          }
          
          public void tick(double dt) {
            if (GameUI.this.chrwdg != null && GameUI.this.chrwdg.skavail) {
              flash(true);
            } else {
              flash(false);
            } 
          }
        }, this.budb = new GameUI.MenuButton(new Coord(4, 66), this, "bud", 2, "Buddy List (Ctrl+B)") {
          public void click() {
            if (GameUI.this.buddies != null && GameUI.this.buddies.show(!GameUI.this.buddies.visible)) {
              GameUI.this.buddies.raise();
              GameUI.this.fitwdg(GameUI.this.buddies);
              setfocus(GameUI.this.buddies);
            } 
          }
        }, this.polb = new GameUI.MenuButton(new Coord(62, 66), this, "pol", 16, "Town (Ctrl+P)") {
          final Tex gray = Resource.loadtex("gfx/hud/polgray");
          
          public void draw(GOut g) {
            if (GameUI.this.polity == null) {
              g.image(this.gray, Coord.z);
            } else {
              super.draw(g);
            } 
          }
          
          public void click() {
            if (GameUI.this.polity != null && GameUI.this.polity.show(!GameUI.this.polity.visible)) {
              GameUI.this.polity.raise();
              GameUI.this.fitwdg(GameUI.this.polity);
              setfocus(GameUI.this.polity);
            } 
          }
        }, this.optb = new GameUI.MenuButton(new Coord(120, 66), this, "opt", 15, "Options (Ctrl+O)") {
          public void click() {
            OptWnd2.toggle();
          }
        } };
    
    public IButton cash;
    
    public IButton manual;
    
    public MainMenu(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
      int y = sz.y - 21;
      int x = 6;
      this.clab = new GameUI.MenuButtonT(new Coord(x, y), this, "cla", -1, "Display personal claims") {
          public void click() {
            if (!GameUI.this.map.visol(0)) {
              GameUI.this.map.enol(new int[] { 0, 1 });
            } else {
              GameUI.this.map.disol(new int[] { 0, 1 });
            } 
            toggle();
            Config.pclaimv = !Config.pclaimv;
            Utils.setprefb("pclaimv", Config.pclaimv);
          }
        };
      if (Config.pclaimv)
        this.clab.toggle(); 
      this.clab.render();
      x += 18;
      this.towb = new GameUI.MenuButtonT(new Coord(x, y), this, "tow", -1, "Display town claims") {
          public void click() {
            if (!GameUI.this.map.visol(2)) {
              GameUI.this.map.enol(new int[] { 2, 3 });
            } else {
              GameUI.this.map.disol(new int[] { 2, 3 });
            } 
            toggle();
            Config.tclaimv = !Config.tclaimv;
            Utils.setprefb("tclaimv", Config.tclaimv);
          }
        };
      if (Config.tclaimv)
        this.towb.toggle(); 
      this.towb.render();
      x += 18;
      this.warb = new GameUI.MenuButtonT(new Coord(x, y), this, "war", -1, "Display waste claims") {
          public void click() {
            if (!GameUI.this.map.visol(4)) {
              GameUI.this.map.enol(new int[] { 4 });
            } else {
              GameUI.this.map.disol(new int[] { 4 });
            } 
            toggle();
            Config.wclaimv = !Config.wclaimv;
            Utils.setprefb("wclaimv", Config.wclaimv);
          }
        };
      if (Config.wclaimv)
        this.warb.toggle(); 
      this.warb.render();
      x += 18;
      this.ptrb = new GameUI.MenuButton(new Coord(x, y), this, "ptr", -1, "Display homestead pointer") {
          public void click() {
            Config.hpointv = !Config.hpointv;
            Utils.setprefb("hpointv", Config.hpointv);
            GameUI.MainMenu.this.pv = (Config.hpointv && !Config.hptr);
          }
        };
      this.pv = (Config.hpointv && !Config.hptr);
      x += 18;
      this.hwab = new GameUI.MenuButton(new Coord(x, y), this, "hwa", -1, "Walk to your homestead") {
          public void click() {
            GameUI.this.homesteadWalk();
          }
        };
      x += 12;
      new GameUI.MenuButton(new Coord(x, y), this, "height", -1, "Display heightmap") {
          public void click() {
            GameUI.this.mmap.toggleHeight();
            toggle();
          }
          
          protected void toggle() {
            BufferedImage img = this.up;
            this.up = this.hover;
            this.hover = this.down;
            this.down = img;
          }
        };
      x += 18;
      this.lndb = new GameUI.MenuButton(new Coord(x, y), this, "lnd", 12, "Display Landscape Tool") {
          public void click() {
            FlatnessTool ft = FlatnessTool.instance(this.ui);
            if (ft != null)
              ft.toggle(); 
          }
        };
      x += 18;
      this.chatb = new GameUI.MenuButton(new Coord(x, y), this, "chat", 3, "Chat (Ctrl+C)") {
          public void click() {
            GameUI.this.chat.toggle();
          }
        };
      new GameUI.MenuButton(new Coord(this.sz.x - 22, y), this, "gear", -1, "Menu") {
          public void click() {
            GameUI.this.mainmenu.toggle();
            toggle();
          }
          
          protected void toggle() {
            BufferedImage img = this.up;
            this.up = this.down;
            this.down = img;
          }
        };
    }
    
    public void draw(GOut g) {
      g.image(Config.mainmenu_full ? GameUI.menubgfull : GameUI.menubg, Coord.z);
      super.draw(g);
    }
    
    public void toggle() {
      Utils.setprefb("mainmenu_full", Config.mainmenu_full = !Config.mainmenu_full);
      apply_visibility();
    }
    
    public void apply_visibility() {
      for (Widget w : this.tohide)
        w.visible = Config.mainmenu_full; 
      if (this.cash != null)
        this.cash.presize(); 
      if (this.manual != null)
        this.manual.presize(); 
    }
  }
  
  public void showCraftWnd() {
    showCraftWnd(false);
  }
  
  public void showCraftWnd(boolean force) {
    if (this.craftwnd == null && (force || Config.autoopen_craftwnd))
      new CraftWnd(Coord.z, this); 
  }
  
  public void toggleCraftWnd() {
    if (this.craftwnd == null) {
      showCraftWnd(true);
    } else {
      this.craftwnd.wdgmsg(this.craftwnd, "close", new Object[0]);
    } 
  }
  
  public void toggleFilterWnd() {
    this.filter.show(!this.filter.visible);
  }
  
  private void makemenu() {
    this.mainmenu = new MainMenu(new Coord(0, this.sz.y - (menubg.sz()).y), menubg.sz(), this);
    (new Widget(Coord.z, Inventory.isqsz.add(Window.swbox.bisz()), this) {
        private final Tex none = Resource.loadtex("gfx/hud/blknone");
        
        private Tex mono;
        
        private Indir<Resource> monores;
        
        public void draw(GOut g) {
          try {
            if (GameUI.this.lblk != null) {
              g.image(((Resource.Image)((Resource)GameUI.this.lblk.get()).<Resource.Image>layer(Resource.imgc)).tex(), Window.swbox.btloff());
            } else if (GameUI.this.dblk != null) {
              if (this.monores != GameUI.this.dblk) {
                if (this.mono != null)
                  this.mono.dispose(); 
                this.mono = new TexI(PUtils.monochromize(((Resource.Image)((Resource)GameUI.this.dblk.get()).layer((Class)Resource.imgc)).img, new Color(128, 128, 128)));
                this.monores = GameUI.this.dblk;
              } 
              g.image(this.mono, Window.swbox.btloff());
            } else {
              g.image(this.none, Window.swbox.btloff());
            } 
          } catch (Loading loading) {}
          g.chcolor(133, 92, 62, 255);
          Window.swbox.draw(g, Coord.z, this.sz);
          g.chcolor();
        }
        
        public void presize() {
          this.c = GameUI.this.menu.c.add(GameUI.this.menu.sz.x, 0).sub(this.sz);
        }
        
        public boolean globtype(char key, KeyEvent ev) {
          if (key == '\023') {
            GameUI.this.act(new String[] { "blk" });
            return true;
          } 
          return super.globtype(key, ev);
        }
        
        public boolean mousedown(Coord c, int btn) {
          GameUI.this.act(new String[] { "blk" });
          return true;
        }
      }).presize();
    if (Config.manualurl != null && WebBrowser.self != null) {
      IButton manual = new IButton(new Coord(0, 0), this, Resource.loadimg("gfx/hud/manu"), Resource.loadimg("gfx/hud/mand"), Resource.loadimg("gfx/hud/manh")) {
          public void click() {
            URL base = Config.manualurl;
            try {
              WebBrowser.self.show(base);
            } catch (BrowserException e) {
              GameUI.this.error("Could not launch web browser.");
            } 
          }
          
          public void presize() {
            this.c = GameUI.this.mainmenu.c.sub(0, this.sz.y).add(0, 0);
          }
          
          public Object tooltip(Coord c, Widget prev) {
            if (checkhit(c))
              return super.tooltip(c, prev); 
            return null;
          }
        };
      manual.presize();
      this.mainmenu.manual = manual;
    } 
    if (Config.storebase != null) {
      (new IButton(Coord.z, this, Resource.loadimg("gfx/hud/cashu"), Resource.loadimg("gfx/hud/cashd"), Resource.loadimg("gfx/hud/cashh")) {
          public void click() {
            if (GameUI.this.storewnd == null) {
              GameUI.this.storewnd = new Store(Coord.z, GameUI.this, Config.storebase);
              GameUI.this.storewnd.c = GameUI.this.storewnd.parent.sz.sub(GameUI.this.storewnd.sz).div(2);
            } else {
              this.ui.destroy(GameUI.this.storewnd);
              GameUI.this.storewnd = null;
            } 
          }
          
          public void presize() {
            this.c = GameUI.this.mainmenu.c.sub(0, this.sz.y).add(90, 0);
          }
          
          public Object tooltip(Coord c, Widget prev) {
            if (checkhit(c))
              return super.tooltip(c, prev); 
            return null;
          }
        }).presize();
    } else if (Config.storebase != null && WebBrowser.self != null) {
      IButton cash = new IButton(Coord.z, this, Resource.loadimg("gfx/hud/cashu"), Resource.loadimg("gfx/hud/cashd"), Resource.loadimg("gfx/hud/cashh")) {
          private String encode(String in) {
            byte[] enc;
            StringBuilder buf = new StringBuilder();
            try {
              enc = in.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
              throw new Error(e);
            } 
            for (byte c : enc) {
              if ((c >= 97 && c <= 122) || (c >= 65 && c <= 90) || (c >= 48 && c <= 57) || c == 46) {
                buf.append((char)c);
              } else {
                buf.append("%" + Utils.num2hex((c & 0xF0) >> 4) + Utils.num2hex(c & 0xF));
              } 
            } 
            return buf.toString();
          }
          
          public void click() {
            URL base = Config.storeurl;
            try {
              WebBrowser.self.show(new URL(base.getProtocol(), base.getHost(), base.getPort(), base.getFile() + "?userid=" + encode(this.ui.sess.username)));
            } catch (MalformedURLException e) {
              throw new RuntimeException(e);
            } catch (BrowserException e) {
              GameUI.this.error("Could not launch web browser.");
            } 
          }
          
          public void presize() {
            this.c = new Coord(90, GameUI.this.mainmenu.c.y - this.sz.y + (Config.mainmenu_full ? 0 : 119));
          }
          
          public Object tooltip(Coord c, Widget prev) {
            if (checkhit(c))
              return super.tooltip(c, prev); 
            return null;
          }
        };
      cash.presize();
      this.mainmenu.cash = cash;
    } 
    if (this.mainmenu.manual != null || this.mainmenu.cash != null)
      this.mainmenu.apply_visibility(); 
  }
  
  public boolean globtype(char key, KeyEvent ev) {
    int keyCode = ev.getKeyCode();
    if (key == ':') {
      entercmd();
      return true;
    } 
    if (Config.screenurl != null && keyCode == 83 && (ev.getModifiersEx() & 0x300) != 0) {
      Screenshooter.take(this, Config.screenurl);
      return true;
    } 
    if (!ev.isControlDown() && !Utils.isCustomHotkey(ev) && (keyCode == 87 || keyCode == 65 || keyCode == 83 || keyCode == 68 || keyCode == 38 || keyCode == 224 || keyCode == 40 || keyCode == 225 || keyCode == 39 || keyCode == 227 || keyCode == 37 || keyCode == 226)) {
      dwalkdown(ev);
      return true;
    } 
    return super.globtype(key, ev);
  }
  
  public boolean keydown(KeyEvent ev) {
    int key = ev.getKeyCode();
    if (key == 38 || key == 224)
      key = 87; 
    if (key == 40 || key == 225)
      key = 83; 
    if (key == 39 || key == 227)
      key = 68; 
    if (key == 37 || key == 226)
      key = 65; 
    if (this.dwalking && !ev.isControlDown() && (key == 87 || key == 65 || key == 83 || key == 68)) {
      dwalkdown(ev);
      return true;
    } 
    return super.keydown(ev);
  }
  
  public boolean keyup(KeyEvent ev) {
    int key = ev.getKeyCode();
    if (key == 38 || key == 224)
      key = 87; 
    if (key == 40 || key == 225)
      key = 83; 
    if (key == 39 || key == 227)
      key = 68; 
    if (key == 37 || key == 226)
      key = 65; 
    if (this.dwalking && !ev.isControlDown() && (key == 87 || key == 65 || key == 83 || key == 68)) {
      dwalkup(ev);
      return true;
    } 
    return super.keyup(ev);
  }
  
  public boolean mousedown(Coord c, int button) {
    return super.mousedown(c, button);
  }
  
  public void resize(Coord sz) {
    this.sz = sz;
    this.menu.c = sz.sub(this.menu.sz);
    this.tm.c = new Coord((sz.x - this.tm.sz.x) / 2, 0);
    this.chat.move(new Coord(this.mainmenu.sz.x, sz.y));
    this.chat.resize(sz.x - this.chat.c.x - this.menu.sz.x);
    if (this.gobble != null)
      this.gobble.c = new Coord((sz.x - this.gobble.sz.x) / 2, 0); 
    if (this.map != null)
      this.map.resize(sz); 
    if (this.fv != null)
      this.fv.c = new Coord(sz.x - Fightview.width, 0); 
    this.mainmenu.c = new Coord(0, sz.y - this.mainmenu.sz.y);
    for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg.visible)
        fitwdg(wdg); 
    } 
    super.resize(sz);
  }
  
  public void presize() {
    resize(this.parent.sz);
  }
  
  public void error(String msg) {
    message(msg, MsgType.ERROR);
  }
  
  private static final Resource errsfx = Resource.load("sfx/error");
  
  private final Map<String, Console.Command> cmdmap;
  
  public void message(String msg, MsgType type) {
    message(msg, getMsgColor(type));
  }
  
  public void message(String msg, Color msgColor) {
    this.errtime = System.currentTimeMillis();
    this.lasterr = errfoundry.render(msg, msgColor);
    this.syslog.append(msg, msgColor);
    if (msg.toLowerCase().contains("unequip"))
      EquipProxyWdg.stopSwitchingItems = true; 
    if (msg.toLowerCase().contains("criminal acts are now turned on")) {
      FlowerMenu.crimesOn = true;
    } else if (msg.toLowerCase().contains("criminal acts are now turned off")) {
      FlowerMenu.crimesOn = false;
    } 
    if (!Config.mute_system_chat)
      Audio.play(errsfx); 
  }
  
  public void messageToLog(String msg, Color msgColor) {
    this.xlog.append(msg, msgColor);
    if (!Config.mute_log_chat)
      Audio.play(errsfx); 
  }
  
  public static Color getMsgColor(MsgType type) {
    switch (type) {
      case INFO:
        return Color.CYAN;
      case GOOD:
        return Color.GREEN;
      case BAD:
        return Color.RED;
      case ERROR:
        return Color.RED;
    } 
    return Color.WHITE;
  }
  
  public enum MsgType {
    INFO, GOOD, BAD, ERROR;
  }
  
  public void act(String... args) {
    wdgmsg("act", (Object[])args);
  }
  
  public void act(int mods, Coord mc, Gob gob, String... args) {
    int n = args.length;
    Object[] al = new Object[n];
    System.arraycopy(args, 0, al, 0, n);
    if (mc != null) {
      al = Utils.extend(al, al.length + 2);
      al[n++] = Integer.valueOf(mods);
      al[n++] = mc;
      if (gob != null) {
        al = Utils.extend(al, al.length + 2);
        al[n++] = Integer.valueOf((int)gob.id);
        al[n++] = gob.rc;
      } 
    } 
    wdgmsg("act", al);
  }
  
  public class FKeyBelt extends Belt implements DTarget, DropTarget {
    public final int[] beltkeys = new int[] { 
        112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 
        122, 123 };
    
    public int curbelt = 0;
    
    public FKeyBelt(Coord c, Widget parent) {
      super(c, Inventory.invsz(new Coord(12, 1)), parent);
    }
    
    private Coord beltc(int i) {
      return Inventory.sqoff(new Coord(i, 0));
    }
    
    private int beltslot(Coord c) {
      for (int i = 0; i < 12; i++) {
        if (c.isect(beltc(i), Inventory.isqsz))
          return i + this.curbelt * 12; 
      } 
      return -1;
    }
    
    public void draw(GOut g) {
      Inventory.invsq(g, Coord.z, new Coord(12, 1));
      for (int i = 0; i < 12; i++) {
        int slot = i + this.curbelt * 12;
        Coord c = beltc(i);
        try {
          Indir<Resource> ir = GameUI.this.belt[slot];
          if (ir != null)
            g.image(((Resource.Image)((Resource)ir.get()).<Resource.Image>layer(Resource.imgc)).tex(), c); 
        } catch (Loading loading) {}
        g.chcolor(156, 180, 158, 255);
        FastText.aprintf(g, c.add(Inventory.isqsz), 1.0D, 1.0D, "F%d", new Object[] { Integer.valueOf(i + 1) });
        g.chcolor();
      } 
    }
    
    public boolean mousedown(Coord c, int button) {
      int slot = beltslot(c);
      if (slot != -1) {
        if (button == 1)
          GameUI.this.wdgmsg("belt", new Object[] { Integer.valueOf(slot), Integer.valueOf(1), Integer.valueOf(this.ui.modflags()) }); 
        if (button == 3)
          GameUI.this.wdgmsg("setbelt", new Object[] { Integer.valueOf(slot), Integer.valueOf(1) }); 
        return true;
      } 
      return false;
    }
    
    public boolean globtype(char key, KeyEvent ev) {
      if (key != '\000')
        return false; 
      boolean M = ((ev.getModifiersEx() & 0x300) != 0);
      for (int i = 0; i < this.beltkeys.length; i++) {
        if (ev.getKeyCode() == this.beltkeys[i]) {
          if (M) {
            this.curbelt = i;
            return true;
          } 
          keyact(i + this.curbelt * 12);
          return true;
        } 
      } 
      return false;
    }
    
    public boolean drop(Coord c, Coord ul) {
      int slot = beltslot(c);
      if (slot != -1) {
        GameUI.this.wdgmsg("setbelt", new Object[] { Integer.valueOf(slot), Integer.valueOf(0) });
        return true;
      } 
      return false;
    }
    
    public boolean iteminteract(Coord c, Coord ul) {
      return false;
    }
    
    public boolean dropthing(Coord c, Object thing) {
      int slot = beltslot(c);
      if (slot != -1 && thing instanceof Resource) {
        Resource res = (Resource)thing;
        if (res.layer(Resource.action) != null) {
          GameUI.this.wdgmsg("setbelt", new Object[] { Integer.valueOf(slot), res.name });
          return true;
        } 
      } 
      return false;
    }
  }
  
  public class NKeyBelt extends Belt implements DTarget, DropTarget {
    public int curbelt = 0;
    
    public NKeyBelt(Coord c, Widget parent) {
      super(c, Inventory.invsz(new Coord(10, 1)), parent);
    }
    
    private Coord beltc(int i) {
      return Inventory.sqoff(new Coord(i, 0));
    }
    
    private int beltslot(Coord c) {
      for (int i = 0; i < 10; i++) {
        if (c.isect(beltc(i), Inventory.isqsz))
          return i + this.curbelt * 12; 
      } 
      return -1;
    }
    
    public void draw(GOut g) {
      Inventory.invsq(g, Coord.z, new Coord(10, 1));
      for (int i = 0; i < 10; i++) {
        int slot = i + this.curbelt * 12;
        Coord c = beltc(i);
        try {
          Indir<Resource> ir = GameUI.this.belt[slot];
          if (ir != null)
            g.image(((Resource.Image)((Resource)ir.get()).<Resource.Image>layer(Resource.imgc)).tex(), c); 
        } catch (Loading loading) {}
        g.chcolor(156, 180, 158, 255);
        FastText.aprintf(g, c.add(Inventory.isqsz), 1.0D, 1.0D, "%d", new Object[] { Integer.valueOf((i + 1) % 10) });
        g.chcolor();
      } 
    }
    
    public boolean mousedown(Coord c, int button) {
      int slot = beltslot(c);
      if (slot != -1) {
        if (button == 1)
          GameUI.this.wdgmsg("belt", new Object[] { Integer.valueOf(slot), Integer.valueOf(1), Integer.valueOf(this.ui.modflags()) }); 
        if (button == 3)
          GameUI.this.wdgmsg("setbelt", new Object[] { Integer.valueOf(slot), Integer.valueOf(1) }); 
        return true;
      } 
      return false;
    }
    
    public boolean globtype(char key, KeyEvent ev) {
      if (key != '\000')
        return false; 
      int c = ev.getKeyChar();
      if (c < 48 || c > 57)
        return false; 
      int i = Utils.floormod(c - 48 - 1, 10);
      boolean M = ((ev.getModifiersEx() & 0x300) != 0);
      if (M) {
        this.curbelt = i;
      } else {
        keyact(i + this.curbelt * 12);
      } 
      return true;
    }
    
    public boolean drop(Coord c, Coord ul) {
      int slot = beltslot(c);
      if (slot != -1) {
        GameUI.this.wdgmsg("setbelt", new Object[] { Integer.valueOf(slot), Integer.valueOf(0) });
        return true;
      } 
      return false;
    }
    
    public boolean iteminteract(Coord c, Coord ul) {
      return false;
    }
    
    public boolean dropthing(Coord c, Object thing) {
      int slot = beltslot(c);
      if (slot != -1 && thing instanceof Resource) {
        Resource res = (Resource)thing;
        if (res.layer(Resource.action) != null) {
          GameUI.this.wdgmsg("setbelt", new Object[] { Integer.valueOf(slot), res.name });
          return true;
        } 
      } 
      return false;
    }
  }
  
  public Map<String, Console.Command> findcmds() {
    return this.cmdmap;
  }
  
  private void homesteadWalk() {
    if (this.map.player() != null)
      for (Gob.Overlay ol : (this.map.player()).ols) {
        if (ol.spr.getClass().equals(HomeTrackerFX.class)) {
          HomeTrackerFX htfx = (HomeTrackerFX)ol.spr;
          this.ui.wdgmsg(this.map, "click", new Object[] { (this.map.player()).sc, htfx.c, Integer.valueOf(1), Integer.valueOf(0) });
        } 
      }  
  }
}
