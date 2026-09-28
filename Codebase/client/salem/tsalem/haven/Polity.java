package haven;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Polity extends Window {
  public final String name;
  
  public int auth;
  
  public int acap;
  
  public int adrain;
  
  public boolean offline;
  
  private final List<Member> memb = new ArrayList<>();
  
  private final Map<Integer, Member> idmap = new HashMap<>();
  
  private final MemberList ml;
  
  private Widget mw;
  
  @RName("pol")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Polity(c, parent, (String)args[0]);
    }
  }
  
  public class Member {
    public final int id;
    
    private final Text rname = null;
    
    private Member(int id) {
      this.id = id;
    }
  }
  
  private class MemberList extends Listbox<Member> {
    final Text unk = Text.render("???");
    
    private MemberList(Coord c, int w, int h, Widget parent) {
      super(c, parent, w, h, 20);
    }
    
    public Polity.Member listitem(int idx) {
      return Polity.this.memb.get(idx);
    }
    
    public int listitems() {
      return Polity.this.memb.size();
    }
    
    public void drawitem(GOut g, Polity.Member m) {
      if (Polity.this.mw instanceof Polity.MemberWidget && ((Polity.MemberWidget)Polity.this.mw).id == m.id)
        drawsel(g); 
      BuddyWnd.Buddy b = ((GameUI)getparent((Class)GameUI.class)).buddies.find(m.id);
      Text rn = (b == null) ? this.unk : b.rname();
      g.aimage(rn.tex(), new Coord(0, 10), 0.0D, 0.5D);
    }
    
    public void change(Polity.Member pm) {
      if (pm == null) {
        Polity.this.wdgmsg("sel", new Object[] { null });
      } else {
        Polity.this.wdgmsg("sel", new Object[] { Integer.valueOf(pm.id) });
      } 
    }
  }
  
  public static abstract class MemberWidget extends Widget {
    public final int id;
    
    public MemberWidget(Coord c, Coord sz, Widget parent, int id) {
      super(c, sz, parent);
      this.id = id;
    }
  }
  
  public static final Text.Foundry nmf = (new Text.Foundry("Serif", 14)).aa(true);
  
  public static final Text.Foundry membf = new Text.Foundry("Sans", 12);
  
  private Tex rauth;
  
  public Polity(Coord c, Widget parent, String name) {
    super(c, new Coord(200, 200), parent, "Town");
    this.rauth = null;
    this.name = name;
    new Label(new Coord(0, 5), this, name, nmf);
    new Label(new Coord(0, 45), this, "Members:");
    this.ml = new MemberList(new Coord(0, 60), 200, 7, this);
    pack();
  }
  
  public void cdraw(GOut g) {
    if (this.acap > 0)
      synchronized (this) {
        g.chcolor(0, 0, 0, 255);
        g.frect(new Coord(0, 23), new Coord(200, 20));
        g.chcolor(128, 0, 0, 255);
        g.frect(new Coord(0, 24), new Coord(200 * this.auth / this.acap, 18));
        g.chcolor();
        if (this.rauth == null) {
          Color col = this.offline ? Color.RED : Color.WHITE;
          this.rauth = new TexI(Utils.outline2((Text.render(String.format("Drain: %s Auth: %s/%s", new Object[] { Integer.valueOf(this.adrain), Integer.valueOf(this.auth), Integer.valueOf(this.acap) }), col)).img, Utils.contrast(col)));
        } 
        g.aimage(this.rauth, new Coord(100, 33), 0.5D, 0.5D);
      }  
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "auth") {
      synchronized (this) {
        this.auth = ((Integer)args[0]).intValue();
        this.acap = ((Integer)args[1]).intValue();
        this.adrain = ((Integer)args[2]).intValue();
        this.offline = (((Integer)args[3]).intValue() != 0);
        this.rauth = null;
      } 
    } else if (msg == "add") {
      int id = ((Integer)args[0]).intValue();
      Member pm = new Member(id);
      synchronized (this) {
        this.memb.add(pm);
        this.idmap.put(Integer.valueOf(id), pm);
      } 
    } else if (msg == "rm") {
      int id = ((Integer)args[0]).intValue();
      synchronized (this) {
        Member pm = this.idmap.get(Integer.valueOf(id));
        this.memb.remove(pm);
        this.idmap.remove(Integer.valueOf(id));
      } 
    } else {
      super.uimsg(msg, args);
    } 
  }
  
  public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    if (pargs[0] instanceof String) {
      String p = (String)pargs[0];
      if (p.equals("m")) {
        this.mw = Widget.gettype(type).create(new Coord(0, 210), this, cargs);
        pack();
        return this.mw;
      } 
    } 
    return super.makechild(type, pargs, cargs);
  }
  
  public void cdestroy(Widget w) {
    if (w == this.mw) {
      this.mw = null;
      pack();
    } 
  }
}
