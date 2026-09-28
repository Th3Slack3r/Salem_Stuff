package haven;

public class HScrollport extends Widget {
  public final HSlider bar;
  
  public final Scrollcont cont;
  
  @RName("hscr")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new HScrollport(c, (Coord)args[0], parent);
    }
  }
  
  public HScrollport(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
    this.bar = new HSlider(new Coord(0, sz.y - HSlider.h), sz.x, this, 0, 0, 0) {
        public void changed() {
          HScrollport.this.cont.sx = HScrollport.this.bar.val;
        }
      };
    this.cont = new Scrollcont(Coord.z, sz.sub(0, this.bar.sz.y), this) {
        public void update() {
          HScrollport.this.bar.max = Math.max(0, (csz()).x - this.sz.x);
        }
      };
  }
  
  public static class Scrollcont extends Widget {
    public int sx = 0;
    
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
        return c.add(-this.sx, 0); 
      return c.add(this.sx, 0);
    }
    
    public void draw(GOut g) {
      for (Widget wdg = this.child; wdg != null; wdg = next) {
        Widget next = wdg.next;
        if (wdg.visible) {
          Coord cc = xlate(wdg.c, true);
          if (cc.x + wdg.sz.x >= 0 && cc.x <= this.sz.x)
            wdg.draw(g.reclip(cc, wdg.sz)); 
        } 
      } 
    }
  }
  
  public boolean mousewheel(Coord c, int amount) {
    this.bar.ch(amount * 15);
    return true;
  }
  
  public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    return this.cont.makechild(type, pargs, cargs);
  }
  
  public void resize(Coord nsz) {
    super.resize(nsz);
    this.bar.c = new Coord(0, this.sz.y - this.bar.sz.y);
    this.cont.resize(this.sz.sub(0, this.bar.sz.y));
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "hpack") {
      resize(new Coord(this.sz.x, (this.cont.contentsz()).y + this.bar.sz.y));
    } else {
      super.uimsg(msg, args);
    } 
  }
}
