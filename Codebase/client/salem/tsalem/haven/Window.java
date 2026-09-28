package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Window extends Widget implements DTarget {
  protected static final Tex tleft = Resource.loadtex("gfx/hud/wnd/tleft");
  
  protected static final Tex tmain = Resource.loadtex("gfx/hud/wnd/tmain");
  
  protected static final Tex tright = Resource.loadtex("gfx/hud/wnd/tright");
  
  public static final BufferedImage[] cbtni = new BufferedImage[] { Resource.loadimg("gfx/hud/wnd/cbtn"), Resource.loadimg("gfx/hud/wnd/cbtnd"), Resource.loadimg("gfx/hud/wnd/cbtnh") };
  
  public static final BufferedImage[] lbtni = new BufferedImage[] { Resource.loadimg("gfx/hud/wnd/lbtn"), Resource.loadimg("gfx/hud/wnd/lbtnd"), Resource.loadimg("gfx/hud/wnd/lbtnh") };
  
  public static final BufferedImage[] rbtni = new BufferedImage[] { Resource.loadimg("gfx/hud/wnd/rbtn"), Resource.loadimg("gfx/hud/wnd/rbtnd"), Resource.loadimg("gfx/hud/wnd/rbtnh") };
  
  public static final BufferedImage[] obtni = new BufferedImage[] { Resource.loadimg("gfx/hud/wnd/obtn"), Resource.loadimg("gfx/hud/wnd/obtnd"), Resource.loadimg("gfx/hud/wnd/obtnh") };
  
  public static final BufferedImage[] gbtni = new BufferedImage[] { Resource.loadimg("gfx/hud/wnd/gbtn"), Resource.loadimg("gfx/hud/wnd/gbtnd"), Resource.loadimg("gfx/hud/wnd/gbtnh") };
  
  public static final Color cc = new Color(248, 230, 190);
  
  public static final Text.Furnace cf = new Text.Imager((new Text.Foundry(new Font("Serif", 1, 15), cc)).aa(true)) {
      protected BufferedImage proc(Text text) {
        return PUtils.rasterimg(PUtils.blurmask2(text.img.getRaster(), 1, 1, Color.BLACK));
      }
    };
  
  public static final IBox fbox = new IBox("gfx/hud", "ftl", "ftr", "fbl", "fbr", "fl", "fr", "ft", "fb");
  
  public static final IBox tbox = new IBox("gfx/hud", "ttl", "ttr", "tbl", "tbr", "tl", "tr", "tt", "tb");
  
  public static final IBox swbox = new IBox("gfx/hud", "stl", "str", "sbl", "sbr", "sl", "sr", "st", "sb");
  
  public static final IBox wbox = new IBox("gfx/hud/wnd", "tl", "tr", "bl", "br", "vl", "vr", "ht", "hb");
  
  protected static final IBox topless = new IBox(Tex.empty, Tex.empty, wbox.cbl, wbox.cbr, wbox.bl, wbox.br, Tex.empty, wbox.bb);
  
  protected static final int th = (tleft.sz()).y;
  
  protected static final int tdh = th - (tmain.sz()).y;
  
  protected static final int tc = tdh + 18;
  
  private static final Coord capc = new Coord(20, th - 3);
  
  public Coord mrgn = new Coord(10, 10);
  
  public Text cap;
  
  public boolean dt = false;
  
  public boolean dm = false;
  
  public Coord ctl;
  
  public Coord csz;
  
  public Coord atl;
  
  public Coord asz;
  
  public Coord ac;
  
  public Coord doff;
  
  protected final IButton cbtn;
  
  private final Collection<Widget> twdgs = new LinkedList<>();
  
  private static final String OPT_POS = "_pos";
  
  public Coord tlo;
  
  public Coord rbo;
  
  public boolean justclose = false;
  
  protected final String name;
  
  public boolean isBackpack = false;
  
  public boolean hasInventory = false;
  
  public boolean isOneColumOnly = false;
  
  public Inventory inventory = null;
  
  public int temperVMeterSum;
  
  public int temperValueSum;
  
  @RName("wnd")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      if (args.length < 2)
        return new Window(c, (Coord)args[0], parent, null); 
      return new Window(c, (Coord)args[0], parent, (String)args[1]);
    }
  }
  
  public Window(Coord c, Coord sz, Widget parent, String cap) {
    super(c, new Coord(0, 0), parent);
    if (cap != null) {
      this.cap = cf.render(cap);
      this.name = cap;
    } else {
      this.cap = null;
      this.name = null;
    } 
    resize(sz);
    setfocustab(true);
    parent.setfocus(this);
    this.cbtn = new IButton(Coord.z, this, cbtni[0], cbtni[1], cbtni[2]);
    this.cbtn.recthit = true;
    addtwdg(this.cbtn);
    loadOpts();
  }
  
  public Coord contentsz() {
    Coord max = new Coord(0, 0);
    for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
      if (!this.twdgs.contains(wdg))
        if (wdg.visible) {
          Coord br = wdg.c.add(wdg.sz);
          if (br.x > max.x)
            max.x = br.x; 
          if (br.y > max.y)
            max.y = br.y; 
        }  
    } 
    return max.sub(1, 1);
  }
  
  protected void placetwdgs() {
    if (this.hasInventory) {
      if (!this.isOneColumOnly) {
        int x = this.sz.x - 5;
        boolean first = true;
        int offsetY = 0;
        int offsetX = 5;
        int count = 1;
        for (Widget ch : this.twdgs) {
          if (ch.visible) {
            if (count == 5) {
              ch.c = xlate(new Coord(5, tc - ch.sz.y / 2 + offsetY), false);
            } else {
              ch.c = xlate(new Coord(x -= ch.sz.x + offsetX, tc - ch.sz.y / 2 + offsetY), false);
            } 
            if (first) {
              first = false;
              x -= 10;
              offsetX = 9;
              offsetY = 19;
            } 
            count++;
          } 
        } 
      } else {
        int x = this.sz.x - 5;
        boolean first = true;
        int offsetY = 0;
        int offsetX = 5;
        for (Widget ch : this.twdgs) {
          if (ch.visible) {
            ch.c = xlate(new Coord(x -= ch.sz.x + 5, tc - ch.sz.y / 2 + offsetY), false);
            if (first) {
              first = false;
              x = this.sz.x - 5;
              offsetY = 19;
            } 
          } 
        } 
      } 
    } else {
      int x = this.sz.x - 5;
      for (Widget ch : this.twdgs) {
        if (ch.visible)
          ch.c = xlate(new Coord(x -= ch.sz.x + 5, tc - ch.sz.y / 2), false); 
      } 
    } 
  }
  
  public void addtwdg(Widget wdg) {
    this.twdgs.add(wdg);
    placetwdgs();
  }
  
  public void resize(Coord sz) {
    IBox box;
    int th;
    if (this.cap == null) {
      box = wbox;
      th = 0;
    } else {
      box = topless;
      th = Window.th;
    } 
    sz = sz.add(box.bisz()).add(0, th).add(this.mrgn.mul(2));
    this.sz = sz;
    this.ctl = box.btloff().add(0, th);
    this.csz = sz.sub(box.bisz()).sub(0, th);
    this.atl = this.ctl.add(this.mrgn);
    this.asz = this.csz.sub(this.mrgn.mul(2));
    this.ac = new Coord();
    placetwdgs();
    for (Widget ch = this.child; ch != null; ch = ch.next)
      ch.presize(); 
  }
  
  public Coord xlate(Coord c, boolean in) {
    if (in)
      return c.add(this.atl); 
    return c.sub(this.atl);
  }
  
  public void cdraw(GOut g) {}
  
  public void draw(GOut g) {
    g.chcolor(0, 0, 0, 160);
    if (this.ctl == null || this.csz == null)
      return; 
    g.frect(this.ctl, this.csz);
    g.chcolor();
    cdraw(g.reclip(xlate(Coord.z, true), this.asz));
    if (this.cap != null) {
      topless.draw(g, new Coord(0, th), this.sz.sub(0, th));
      g.image(tleft, Coord.z);
      Coord tmul = new Coord((tleft.sz()).x, tdh);
      Coord tmbr = new Coord(this.sz.x - (tright.sz()).x, th);
      int x;
      for (x = tmul.x; x < tmbr.x; x += (tmain.sz()).x)
        g.image(tmain, new Coord(x, tdh), tmul, tmbr); 
      g.image(tright, new Coord(this.sz.x - (tright.sz()).x, tdh));
      g.image(this.cap.tex(), capc.sub(0, (this.cap.sz()).y));
    } else {
      wbox.draw(g, Coord.z, this.sz);
    } 
    super.draw(g);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "pack") {
      pack();
    } else if (msg == "dt") {
      this.dt = (((Integer)args[0]).intValue() != 0);
    } else {
      super.uimsg(msg, args);
    } 
  }
  
  public boolean mousedown(Coord c, int button) {
    if (c.y < tdh && this.cap != null)
      return false; 
    this.parent.setfocus(this);
    raise();
    if (super.mousedown(c, button))
      return true; 
    if (button == 1 && 
      !this.ui.modshift && !this.ui.modctrl && (
      !this.isBackpack || !Inventory.backpackLocked)) {
      this.ui.grabmouse(this);
      this.dm = true;
      this.doff = c;
    } 
    return true;
  }
  
  public boolean old_mousedown(Coord c, int button) {
    if (c.y < tdh && this.cap != null)
      return false; 
    this.parent.setfocus(this);
    raise();
    if (super.mousedown(c, button))
      return true; 
    if (button == 1) {
      this.ui.grabmouse(this);
      this.dm = true;
      this.doff = c;
    } 
    return true;
  }
  
  public boolean mouseup(Coord c, int button) {
    if (this.dm) {
      canceldm();
      storeOpt("_pos", this.c);
    } else {
      super.mouseup(c, button);
    } 
    return true;
  }
  
  public void canceldm() {
    if (this.dm)
      this.ui.grabmouse(null); 
    this.dm = false;
  }
  
  public void mousemove(Coord c) {
    if (this.dm) {
      this.c = this.c.add(c.add(this.doff.inv()));
    } else {
      super.mousemove(c);
    } 
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.cbtn) {
      try {
        if (this.ui.modshift && 
          this.hasInventory && 
          this.inventory != null && this.ui.gui.maininv != this.inventory) {
          List<WItem> items = this.inventory.getSameName("", Boolean.valueOf(true));
          for (WItem w : items) {
            w.item.wdgmsg("transfer", new Object[] { Coord.z });
          } 
        } 
      } catch (Exception exception) {}
      if (this.justclose) {
        this.ui.destroy(this);
      } else {
        wdgmsg("close", new Object[0]);
      } 
    } else {
      super.wdgmsg(sender, msg, args);
    } 
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (super.type(key, ev))
      return true; 
    if (key == '\033') {
      if (this.justclose) {
        this.ui.destroy(this);
      } else {
        wdgmsg("close", new Object[0]);
      } 
      return true;
    } 
    return false;
  }
  
  public boolean drop(Coord cc, Coord ul) {
    if (this.dt) {
      wdgmsg("drop", new Object[] { cc });
      return true;
    } 
    return false;
  }
  
  public boolean iteminteract(Coord cc, Coord ul) {
    return false;
  }
  
  public Object tooltip(Coord c, Widget prev) {
    Object ret = super.tooltip(c, prev);
    if (ret != null)
      return ret; 
    return "";
  }
  
  public void storeOpt(String opt, String value) {
    if (this.name == null)
      return; 
    Config.setWindowOpt(this.name + opt, value);
  }
  
  public void storeOpt(String opt, Coord value) {
    storeOpt(opt, value.toString());
  }
  
  public void storeOpt(String opt, boolean value) {
    if (this.name == null)
      return; 
    Config.setWindowOpt(this.name + opt, Boolean.valueOf(value));
  }
  
  public Coord getOptCoord(String opt, Coord def) {
    synchronized (Config.window_props) {
      return new Coord(Config.window_props.getProperty(this.name + opt, def.toString()));
    } 
  }
  
  public boolean getOptBool(String opt, boolean def) {
    synchronized (Config.window_props) {
      return Config.window_props.getProperty(this.name + opt, null).equals("true");
    } 
  }
  
  protected void loadOpts() {
    if (this.name == null)
      return; 
    this.c = getOptCoord("_pos", this.c);
  }
}
