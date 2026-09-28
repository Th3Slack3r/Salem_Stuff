package haven;

public class Scrollport extends Widget {
  public final Scrollbar bar;
  
  public final Scrollcont cont;
  
  @RName("scr")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Scrollport(c, (Coord)args[0], parent);
    }
  }
  
  public Scrollport(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
    this.bar = new Scrollbar(new Coord(sz.x, 0), sz.y, this, 0, 0) {
        public void changed() {
          Scrollport.this.cont.sy = Scrollport.this.bar.val;
        }
      };
    this.cont = new Scrollcont(Coord.z, sz.sub(this.bar.sz.x, 0), this) {
        public void update() {
          Scrollport.this.bar.max = Math.max(0, (csz()).y - this.sz.y);
        }
      };
  }
  
  public static class Scrollcont extends Widget {
    public int sy = 0;
    
    public Scrollcont(Coord c, Coord sz, Widget parent) {
      super(c, sz, parent);
    }
    
    public Coord csz() {
      Coord mx = new Coord();
      for (Widget ch = this.child; ch != null; ch = ch.next) {
        if (ch.c.x + ch.sz.x > mx.x)
          ch.c.x += ch.sz.x; 
        if (ch.c.y + ch.sz.y > mx.y)
          ch.c.y += ch.sz.y; 
      } 
      return mx;
    }
    
    public void update() {}
    
    public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      Widget ret = super.makechild(type, pargs, cargs);
      update();
      return ret;
    }
    
    public Coord xlate(Coord c, boolean in) {
      if (in)
        return c.add(0, -this.sy); 
      return c.add(0, this.sy);
    }
    
    public void draw(GOut g) {
      for (Widget wdg = this.child; wdg != null; wdg = next) {
        Widget next = wdg.next;
        if (wdg.visible) {
          Coord cc = xlate(wdg.c, true);
          if (cc.y + wdg.sz.y >= 0 && cc.y <= this.sz.y)
            wdg.draw(g.reclip(cc, wdg.sz)); 
        } 
      } 
    }
  }
  
  public boolean mousewheel(Coord c, int amount) {
    if (this.ui.modshift) {
      this.cont.child.wdgmsg("xfer", new Object[] { Integer.valueOf(amount) });
    } else {
      this.bar.ch(amount * 15);
    } 
    return true;
  }
  
  public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    return this.cont.makechild(type, pargs, cargs);
  }
  
  public void resize(Coord nsz) {
    super.resize(nsz);
    this.bar.c = new Coord(this.sz.x - this.bar.sz.x, 0);
    this.bar.sz.y = nsz.y;
    this.cont.resize(this.sz.sub(this.bar.sz.x, 0));
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "wpack") {
      resize(new Coord((this.cont.contentsz()).x + this.bar.sz.x, this.sz.y));
    } else {
      super.uimsg(msg, args);
    } 
  }
}
