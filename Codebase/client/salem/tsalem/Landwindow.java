import haven.BuddyWnd;
import haven.Button;
import haven.CheckBox;
import haven.Coord;
import haven.GOut;
import haven.Label;
import haven.MCache;
import haven.Tex;
import haven.TexI;
import haven.Text;
import haven.Utils;
import haven.Widget;
import haven.WidgetFactory;
import haven.Window;
import java.awt.Color;

public class Landwindow extends Window {
  Widget bn;
  
  Widget be;
  
  Widget bs;
  
  Widget bw;
  
  Widget buy;
  
  Widget reset;
  
  Widget dst;
  
  BuddyWnd.GroupSelector group;
  
  Label area;
  
  Label cost;
  
  Label lbl_upkeep;
  
  int auth;
  
  int acap;
  
  int adrain;
  
  boolean offline;
  
  Coord c1;
  
  Coord c2;
  
  Coord cc1;
  
  Coord cc2;
  
  MCache.Overlay ol;
  
  MCache map;
  
  int[] bflags = new int[8];
  
  PermBox[] perms = new PermBox[3];
  
  CheckBox homeck;
  
  private Tex rauth = null;
  
  private float upkeep = 0.0F;
  
  public static class Maker implements WidgetFactory {
    public Widget create(Coord var1, Widget var2, Object[] args) {
      Coord var4 = (Coord)args[0];
      Coord var5 = (Coord)args[1];
      boolean var6 = (((Integer)args[2]).intValue() != 0);
      return (Widget)new Landwindow(var1, var2, var4, var5, var6);
    }
  }
  
  private class PermBox extends CheckBox {
    int fl;
    
    PermBox(Coord var2, Widget var3, String var4, int var5) {
      super(var2, var3, var4);
      this.fl = var5;
    }
    
    public void changed(boolean var1) {
      int var2 = 0;
      for (PermBox perm : Landwindow.this.perms) {
        if (perm.a)
          var2 |= perm.fl; 
      } 
      Landwindow.this.wdgmsg("shared", new Object[] { Integer.valueOf(this.this$0.group.group), Integer.valueOf(var2) });
    }
  }
  
  private void fmtarea() {
    int area = (this.c2.x - this.c1.x + 1) * (this.c2.y - this.c1.y + 1);
    this.area.settext(String.format("Area: %d mÂ²", new Object[] { Integer.valueOf(area) }));
    this.upkeep = 4.0F + area / 300.0F;
    updupkeep();
  }
  
  private void updupkeep() {
    float days = this.auth / this.upkeep;
    this.lbl_upkeep.settext(String.format("Upkeep: %.2f/day, enough for %.1f days", new Object[] { Float.valueOf(this.upkeep), Float.valueOf(days) }));
  }
  
  private void updatecost() {
    int cost = (this.cc2.x - this.cc1.x + 1) * (this.cc2.y - this.cc1.y + 1) - (this.c2.x - this.c1.x + 1) * (this.c2.y - this.c1.y + 1);
    this.cost.settext(String.format("Cost: %d", new Object[] { Integer.valueOf(cost) }));
  }
  
  private void updflags() {
    int var1 = this.bflags[this.group.group];
    for (PermBox perm : this.perms)
      perm.a = ((var1 & perm.fl) != 0); 
  }
  
  public Landwindow(Coord c, Widget parent, Coord c1, Coord c2, final boolean homestead) {
    super(c, new Coord(0, 0), parent, "Stake");
    this.cc1 = this.c1 = c1;
    this.cc2 = this.c2 = c2;
    this.map = this.ui.sess.glob.map;
    this.ui.gui.map.enol(new int[] { 0, 1, 16 });
    this.map.getClass();
    this.ol = new MCache.Overlay(this.map, this.cc1, this.cc2, 65536);
    this.area = new Label(new Coord(0, 0), (Widget)this, "");
    int y = 15;
    this.lbl_upkeep = new Label(new Coord(0, y), (Widget)this, "");
    y += 15;
    new Widget(new Coord(0, y), new Coord(220, 20), (Widget)this) {
        public void draw(GOut g) {
          int auth = Landwindow.this.auth;
          int acap = Landwindow.this.acap;
          if (acap > 0) {
            g.chcolor(0, 0, 0, 255);
            g.frect(Coord.z, this.sz);
            g.chcolor(128, 0, 0, 255);
            Coord var4 = this.sz.sub(2, 2);
            var4.x = auth * var4.x / acap;
            g.frect(new Coord(1, 1), var4);
            g.chcolor();
            if (Landwindow.this.rauth == null) {
              Color color = Landwindow.this.offline ? Color.RED : Color.WHITE;
              Landwindow.this.rauth = (Tex)new TexI(Utils.outline2((Text.render(String.format("%s/%s", new Object[] { Integer.valueOf(auth), Integer.valueOf(acap) }), color)).img, Utils.contrast(color)));
            } 
            g.aimage(Landwindow.this.rauth, this.sz.div(2), 0.5D, 0.5D);
          } 
        }
      };
    y += 25;
    this.cost = new Label(new Coord(0, y), (Widget)this, "Cost: 0");
    y += 25;
    fmtarea();
    this.bn = (Widget)new Button(new Coord(70, y), Integer.valueOf(80), (Widget)this, "Extend North");
    this.be = (Widget)new Button(new Coord(140, y + 25), Integer.valueOf(80), (Widget)this, "Extend East");
    this.bs = (Widget)new Button(new Coord(70, y + 50), Integer.valueOf(80), (Widget)this, "Extend South");
    this.bw = (Widget)new Button(new Coord(0, y + 25), Integer.valueOf(80), (Widget)this, "Extend West");
    y += 80;
    this.buy = (Widget)new Button(new Coord(0, y), Integer.valueOf(60), (Widget)this, "Buy");
    this.reset = (Widget)new Button(new Coord(80, y), Integer.valueOf(60), (Widget)this, "Reset");
    this.dst = (Widget)new Button(new Coord(160, y), Integer.valueOf(60), (Widget)this, "Declaim");
    y += 25;
    new Label(new Coord(0, y), (Widget)this, "Assign permissions to memorized people:");
    y += 15;
    this.group = new BuddyWnd.GroupSelector(new Coord(0, y), (Widget)this, 0) {
        protected void changed(int group) {
          super.changed(group);
          Landwindow.this.updflags();
        }
      };
    y += 20;
    this.perms[0] = new PermBox(new Coord(10, y), (Widget)this, "Trespassing", 1);
    y += 20;
    this.perms[1] = new PermBox(new Coord(10, y), (Widget)this, "Theft", 2);
    y += 20;
    this.perms[2] = new PermBox(new Coord(10, y), (Widget)this, "Vandalism", 4);
    y += 20;
    y += 10;
    this.homeck = new CheckBox(new Coord(0, y), (Widget)this, "Use as homestead") {
        public boolean mousedown(Coord c, int button) {
          if (!this.a) {
            Landwindow.this.wdgmsg("mkhome", new Object[0]);
            set(true);
          } 
          return true;
        }
        
        public void changed(boolean val) {}
      };
    pack();
  }
  
  public void destroy() {
    this.ui.gui.map.disol(new int[] { 0, 1, 16 });
    this.ol.destroy();
    super.destroy();
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg.equals("upd")) {
      Coord var3 = (Coord)args[0];
      Coord var4 = (Coord)args[1];
      this.c1 = var3;
      this.c2 = var4;
      fmtarea();
      updatecost();
    } else if (msg.equals("shared")) {
      int var5 = ((Integer)args[0]).intValue();
      int var6 = ((Integer)args[1]).intValue();
      this.bflags[var5] = var6;
      if (var5 == this.group.group)
        updflags(); 
    } else if (msg.equals("auth")) {
      this.auth = ((Integer)args[0]).intValue();
      this.acap = ((Integer)args[1]).intValue();
      this.adrain = ((Integer)args[2]).intValue();
      this.offline = (((Integer)args[3]).intValue() != 0);
      this.rauth = null;
      updupkeep();
    } 
  }
  
  public void wdgmsg(Widget sender, String m, Object... args) {
    if (sender == this.bn) {
      this.cc1 = this.cc1.add(0, -1);
      this.ol.update(this.cc1, this.cc2);
      updatecost();
    } else if (sender == this.be) {
      this.cc2 = this.cc2.add(1, 0);
      this.ol.update(this.cc1, this.cc2);
      updatecost();
    } else if (sender == this.bs) {
      this.cc2 = this.cc2.add(0, 1);
      this.ol.update(this.cc1, this.cc2);
      updatecost();
    } else if (sender == this.bw) {
      this.cc1 = this.cc1.add(-1, 0);
      this.ol.update(this.cc1, this.cc2);
      updatecost();
    } else if (sender == this.buy) {
      wdgmsg("take", new Object[] { this.cc1, this.cc2 });
    } else if (sender == this.reset) {
      this.ol.update(this.cc1 = this.c1, this.cc2 = this.c2);
      updatecost();
    } else if (sender == this.dst) {
      wdgmsg("declaim", new Object[0]);
    } else {
      super.wdgmsg(sender, m, args);
    } 
  }
}
