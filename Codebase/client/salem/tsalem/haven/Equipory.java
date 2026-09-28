package haven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Equipory extends Widget implements DTarget {
  public static final Box[] boxen = new Box[] { 
      new Box(new Coord(250, 0), Resource.loadtex("gfx/hud/inv/head"), 0), new Box(new Coord(50, 70), Resource.loadtex("gfx/hud/inv/face"), 0), new Box(new Coord(250, 70), Resource.loadtex("gfx/hud/inv/shirt"), 0), new Box(new Coord(300, 70), 
        Resource.loadtex("gfx/hud/inv/torsoa"), 0), new Box(new Coord(50, 0), Resource.loadtex("gfx/hud/inv/keys"), 0), new Box(new Coord(50, 210), Resource.loadtex("gfx/hud/inv/belt"), 0), new Box(new Coord(25, 140), Resource.loadtex("gfx/hud/inv/lhande"), 0), new Box(new Coord(275, 140), 
        Resource.loadtex("gfx/hud/inv/rhande"), 0), null, new Box(new Coord(0, 0), Resource.loadtex("gfx/hud/inv/wallet"), 0), 
      new Box(new Coord(0, 210), Resource.loadtex("gfx/hud/inv/coat"), 0), new Box(new Coord(300, 0), Resource.loadtex("gfx/hud/inv/cape"), 0), new Box(new Coord(300, 210), 
        Resource.loadtex("gfx/hud/inv/pants"), 0), new Box(new Coord(100, 0), null, 0), new Box(new Coord(0, 70), Resource.loadtex("gfx/hud/inv/back"), 0), new Box(new Coord(250, 210), Resource.loadtex("gfx/hud/inv/feet"), 0), new Box(new Coord(250, 0), 
        
        Resource.loadtex("gfx/hud/inv/costumehead"), 1), new Box(new Coord(50, 70), Resource.loadtex("gfx/hud/inv/costumeface"), 1), new Box(new Coord(250, 70), Resource.loadtex("gfx/hud/inv/costumeshirt"), 1), new Box(new Coord(300, 70), 
        Resource.loadtex("gfx/hud/inv/costumetorsoa"), 1), 
      new Box(new Coord(0, 210), Resource.loadtex("gfx/hud/inv/costumecoat"), 1), new Box(new Coord(300, 0), Resource.loadtex("gfx/hud/inv/costumecape"), 1), new Box(new Coord(300, 210), 
        Resource.loadtex("gfx/hud/inv/costumepants"), 1), new Box(new Coord(250, 210), Resource.loadtex("gfx/hud/inv/costumefeet"), 1) };
  
  public static final Coord isz = isz();
  
  public final Widget[] tabs = new Widget[2];
  
  public final WItem[] slots = new WItem[boxen.length];
  
  private final Map<GItem, WItem[]> wmap = (Map)new HashMap<>();
  
  private final AttrBonusWdg bonuses;
  
  private final IButton showbonus;
  
  private final IButton hidebonus;
  
  private EquipOpts opts;
  
  private final List<GItem> checkForDrop = new LinkedList<>();
  
  private static Coord isz() {
    Coord isz = new Coord();
    for (Box box : boxen) {
      if (box != null) {
        if (box.c.x + (Inventory.sqlite.sz()).x > isz.x)
          box.c.x += (Inventory.sqlite.sz()).x; 
        if (box.c.y + (Inventory.sqlite.sz()).y > isz.y)
          box.c.y += (Inventory.sqlite.sz()).y; 
      } 
    } 
    return isz;
  }
  
  public static class Box {
    public final Coord c;
    
    public final Tex bg;
    
    public final int tab;
    
    public Box(Coord c, Tex bg, int tab) {
      this.c = c;
      this.bg = bg;
      this.tab = tab;
    }
  }
  
  @RName("epry")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      long gobid;
      if (args.length < 1) {
        gobid = ((GameUI)parent.getparent((Class)GameUI.class)).plid;
      } else {
        gobid = ((Integer)args[0]).intValue();
      } 
      return new Equipory(c, parent, gobid);
    }
  }
  
  private class Boxen extends Widget implements DTarget {
    final int tab;
    
    private Boxen(Coord c, Widget parent, int tab) {
      super(c, Equipory.isz, parent);
      this.tab = tab;
    }
    
    public void draw(GOut g) {
      for (int i = 0; i < Equipory.boxen.length; i++) {
        Equipory.Box box = Equipory.boxen[i];
        if (box != null && box.tab == this.tab) {
          g.image(Inventory.sqlite, box.c);
          if (Equipory.this.slots[i] == null && box.bg != null)
            g.image(box.bg, box.c.add(Inventory.sqlo)); 
        } 
      } 
    }
    
    public boolean drop(Coord cc, Coord ul) {
      ul = ul.add(Inventory.sqlite.sz().div(2));
      for (int i = 0; i < Equipory.boxen.length; i++) {
        Equipory.Box box = Equipory.boxen[i];
        if (box != null && box.tab == this.tab)
          if (ul.isect(box.c, Inventory.sqlite.sz())) {
            Equipory.this.wdgmsg("drop", new Object[] { Integer.valueOf(i) });
            return true;
          }  
      } 
      Equipory.this.wdgmsg("drop", new Object[] { Integer.valueOf(-1) });
      return true;
    }
    
    public boolean iteminteract(Coord cc, Coord ul) {
      return false;
    }
  }
  
  public Equipory(Coord c, Widget parent, long gobid) {
    super(c, isz, parent);
    this.bonuses = new AttrBonusWdg(this, new Coord(isz.x, 0));
    Avaview ava = new Avaview(Coord.z, isz, this, gobid, "equcam") {
        public boolean mousedown(Coord c, int button) {
          return false;
        }
        
        protected Color clearcolor() {
          return null;
        }
      };
    int bx = 0;
    String s1 = "Equipment";
    String s2 = "Costume";
    for (int i = 0; i < this.tabs.length; i++) {
      String s3;
      this.tabs[i] = new Widget(Coord.z, this.sz, this);
      this.tabs[i].show((i == 0));
      new Boxen(Coord.z, this.tabs[i], i);
      final int t = i;
      if (t > 0) {
        s3 = "Costume";
      } else {
        s3 = "Equipment";
      } 
      Widget btn = new Button(new Coord(bx, isz.y + 5), Integer.valueOf(60), this, s3) {
          public void click() {
            for (int i = 0; i < Equipory.this.tabs.length; i++)
              Equipory.this.tabs[i].show((i == t)); 
          }
        };
      if (t > 0) {
        btn.tooltip = Text.render("Costume");
      } else {
        btn.tooltip = Text.render("Equipment");
      } 
      bx = btn.c.x + btn.sz.x + 228;
    } 
    this.opts = new EquipOpts(new Coord(200, 100), this.ui.gui);
    this.opts.hide();
    Window p = (Window)parent;
    this.showbonus = new IButton(Coord.z, p, Window.rbtni[0], Window.rbtni[1], Window.rbtni[2]) {
        public void click() {
          Equipory.this.toggleBonuses();
        }
      };
    this.showbonus.visible = !this.bonuses.visible;
    p.addtwdg(this.showbonus);
    this.hidebonus = new IButton(Coord.z, p, Window.lbtni[0], Window.lbtni[1], Window.lbtni[2]) {
        public void click() {
          Equipory.this.toggleBonuses();
        }
      };
    this.hidebonus.visible = this.bonuses.visible;
    p.addtwdg(this.hidebonus);
    p.addtwdg(new IButton(Coord.z, p, Window.obtni[0], Window.obtni[1], Window.obtni[2]) {
          public void click() {
            Equipory.this.toggleOptions();
          }
        });
    pack();
    parent.pack();
  }
  
  private void toggleBonuses() {
    this.bonuses.toggle();
    this.showbonus.visible = !this.bonuses.visible;
    this.hidebonus.visible = this.bonuses.visible;
    pack();
    this.parent.pack();
  }
  
  private void toggleOptions() {
    if (this.opts != null)
      this.opts.toggle(); 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender instanceof GItem && this.wmap.containsKey(sender) && msg.equals("ttupdate")) {
      this.bonuses.update(this.slots);
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public void tick(double dt) {
    super.tick(dt);
    try {
      if (!this.checkForDrop.isEmpty()) {
        GItem g = this.checkForDrop.get(0);
        if (g.resname().equals("gfx/invobjs/bat"))
          g.drop = true; 
        this.checkForDrop.remove(0);
      } 
    } catch (Loading loading) {}
  }
  
  public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    Widget ret = Widget.gettype(type).create(Coord.z, this, cargs);
    if (ret instanceof GItem) {
      GItem g = (GItem)ret;
      g.sendttupdate = true;
      WItem[] v = new WItem[pargs.length];
      for (int i = 0; i < pargs.length; i++) {
        int ep = ((Integer)pargs[i]).intValue();
        Box box = boxen[ep];
        v[i] = new WItem(box.c.add(Inventory.sqlo), this.tabs[box.tab], g);
        this.slots[ep] = new WItem(box.c.add(Inventory.sqlo), this.tabs[box.tab], g);
      } 
      this.wmap.put(g, v);
      if (Config.auto_drop_bats)
        this.checkForDrop.add(g); 
    } 
    return ret;
  }
  
  public void cdestroy(Widget w) {
    super.cdestroy(w);
    if (w instanceof GItem) {
      GItem i = (GItem)w;
      for (WItem v : (WItem[])this.wmap.remove(i)) {
        this.ui.destroy(v);
        for (int s = 0; s < this.slots.length; s++) {
          if (this.slots[s] == v)
            this.slots[s] = null; 
        } 
      } 
      this.bonuses.update(this.slots);
    } 
  }
  
  public boolean drop(Coord cc, Coord ul) {
    ul = ul.add(Inventory.sqlite.sz().div(2));
    for (int i = 0; i < boxen.length; i++) {
      if (boxen[i] != null)
        if (ul.isect((boxen[i]).c, Inventory.sqlite.sz())) {
          if (this.slots[i] != null) {
            final int ix = i;
            (new Thread(new Runnable() {
                  public void run() {
                    EquipProxyWdg.switchItem(ix);
                  }
                },  "SwitchItem")).start();
          } else {
            wdgmsg("drop", new Object[] { Integer.valueOf(i) });
          } 
          return true;
        }  
    } 
    wdgmsg("drop", new Object[] { Integer.valueOf(-1) });
    return true;
  }
  
  public boolean iteminteract(Coord cc, Coord ul) {
    return false;
  }
}
