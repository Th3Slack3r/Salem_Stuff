package haven;

import java.util.LinkedList;

public class Fightview extends Widget {
  static int height = 5;
  
  static int iheight = 40;
  
  static int ymarg = 2;
  
  static int width = 170;
  
  static Coord avasz = new Coord(36, 36);
  
  static Coord cavac = new Coord(width - Avaview.dasz.x - 10, 10);
  
  static Coord cgivec = new Coord(cavac.x - 70, cavac.y);
  
  LinkedList<Relation> lsrel = new LinkedList<>();
  
  public Relation current = null;
  
  public Indir<Resource> blk;
  
  public Indir<Resource> batk;
  
  public Indir<Resource> iatk;
  
  public long atkc = -1L;
  
  public int off;
  
  public int def;
  
  private GiveButton curgive;
  
  private FramedAva curava;
  
  public class Relation {
    long gobid;
    
    FramedAva ava;
    
    GiveButton give;
    
    public Relation(long gobid) {
      this.gobid = gobid;
      this.ava = new FramedAva(Coord.z, Fightview.avasz, Fightview.this, gobid, "avacam");
      this.give = new GiveButton(Coord.z, Fightview.this, 0, new Coord(30, 30));
    }
    
    public void give(int state) {
      if (this == Fightview.this.current)
        Fightview.this.curgive.state = state; 
      this.give.state = state;
    }
    
    public void show(boolean state) {
      this.ava.show(state);
      this.give.show(state);
    }
    
    public void remove() {
      Fightview.this.ui.destroy(this.ava);
      Fightview.this.ui.destroy(this.give);
    }
  }
  
  @RName("frv")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Fightview(c, parent);
    }
  }
  
  public Fightview(Coord c, Widget parent) {
    super(c, new Coord(width, (iheight + ymarg) * height), parent);
  }
  
  private void setcur(Relation rel) {
    if (this.current == null && rel != null) {
      this.curgive = new GiveButton(cgivec, this, 0) {
          public void wdgmsg(String name, Object... args) {
            if (name == "click")
              Fightview.this.wdgmsg("give", new Object[] { Integer.valueOf((int)this.this$0.current.gobid), args[0] }); 
          }
        };
      this.curava = new FramedAva(cavac, Avaview.dasz, this, rel.gobid, "avacam") {
          public void wdgmsg(String name, Object... args) {
            if (name == "click")
              Fightview.this.wdgmsg("click", new Object[] { Integer.valueOf((int)this.this$0.current.gobid), args[0] }); 
          }
        };
    } else if (this.current != null && rel == null) {
      this.ui.destroy(this.curgive);
      this.ui.destroy(this.curava);
      this.curgive = null;
      this.curava = null;
    } else if (this.current != null && rel != null) {
      this.curgive.state = rel.give.state;
      this.curava.view.avagob = rel.gobid;
    } 
    this.current = rel;
  }
  
  public void destroy() {
    setcur((Relation)null);
    super.destroy();
  }
  
  public void draw(GOut g) {
    int y = 10;
    if (this.curava != null)
      y = this.curava.c.y + this.curava.sz.y + 10; 
    int x = width - 90;
    for (Relation rel : this.lsrel) {
      if (rel == this.current) {
        rel.show(false);
        continue;
      } 
      rel.ava.c = new Coord(x + 45, 4 + y);
      rel.give.c = new Coord(x + 5, 4 + y);
      rel.show(true);
      y += iheight + ymarg;
    } 
    super.draw(g);
  }
  
  public static class Notfound extends RuntimeException {
    public final long id;
    
    public Notfound(long id) {
      super("No relation for Gob ID " + id + " found");
      this.id = id;
    }
  }
  
  private Relation getrel(long gobid) {
    for (Relation rel : this.lsrel) {
      if (rel.gobid == gobid)
        return rel; 
    } 
    throw new Notfound(gobid);
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender instanceof FramedAva) {
      for (Relation rel : this.lsrel) {
        if (rel.ava == sender)
          wdgmsg("click", new Object[] { Integer.valueOf((int)rel.gobid), args[0] }); 
      } 
      return;
    } 
    if (sender instanceof GiveButton) {
      for (Relation rel : this.lsrel) {
        if (rel.give == sender)
          wdgmsg("give", new Object[] { Integer.valueOf((int)rel.gobid), args[0] }); 
      } 
      return;
    } 
    super.wdgmsg(sender, msg, args);
  }
  
  private Indir<Resource> n2r(int num) {
    if (num < 0)
      return null; 
    return this.ui.sess.getres(num);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "new") {
      Relation rel = new Relation(((Integer)args[0]).intValue());
      rel.give(((Integer)args[1]).intValue());
      this.lsrel.addFirst(rel);
      return;
    } 
    if (msg == "del") {
      Relation rel = getrel(((Integer)args[0]).intValue());
      rel.remove();
      this.lsrel.remove(rel);
      if (rel == this.current)
        setcur((Relation)null); 
      return;
    } 
    if (msg == "upd") {
      Relation rel = getrel(((Integer)args[0]).intValue());
      rel.give(((Integer)args[1]).intValue());
      return;
    } 
    if (msg == "cur") {
      try {
        Relation rel = getrel(((Integer)args[0]).intValue());
        this.lsrel.remove(rel);
        this.lsrel.addFirst(rel);
        setcur(rel);
      } catch (Notfound e) {
        setcur((Relation)null);
      } 
      return;
    } 
    if (msg == "atkc") {
      this.atkc = System.currentTimeMillis() + (((Integer)args[0]).intValue() * 60);
      return;
    } 
    if (msg == "blk") {
      this.blk = n2r(((Integer)args[0]).intValue());
      return;
    } 
    if (msg == "atk") {
      this.batk = n2r(((Integer)args[0]).intValue());
      this.iatk = n2r(((Integer)args[1]).intValue());
      return;
    } 
    if (msg == "offdef") {
      this.off = ((Integer)args[0]).intValue();
      this.def = ((Integer)args[1]).intValue();
      return;
    } 
    super.uimsg(msg, args);
  }
}
