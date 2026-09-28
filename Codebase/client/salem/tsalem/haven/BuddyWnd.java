package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BuddyWnd extends Window implements Iterable<BuddyWnd.Buddy> {
  public static BuddyWnd instance = null;
  
  private final List<Buddy> buddies = new ArrayList<>();
  
  private final Map<Integer, Buddy> idmap = new HashMap<>();
  
  private BuddyList bl;
  
  private static Label kinCount = null;
  
  private static int kCD = 100;
  
  private static boolean kCRunning = false;
  
  private Button sbalpha;
  
  private Button sbgroup;
  
  private Button sbstatus;
  
  private CTextEntry nicksel;
  
  private CTextEntry searchName;
  
  private CTextEntry pname;
  
  private CTextEntry charpass;
  
  private Buddy editing = null;
  
  private TextEntry opass;
  
  private GroupSelector grpsel;
  
  private FlowerMenu menu;
  
  public int serial = 0;
  
  public static final Tex online = Resource.loadtex("gfx/hud/online");
  
  public static final Tex offline = Resource.loadtex("gfx/hud/offline");
  
  public static final Color[] gc = new Color[] { new Color(255, 255, 255), new Color(64, 255, 64), new Color(255, 64, 64), new Color(96, 160, 255), new Color(0, 255, 255), new Color(255, 255, 0), new Color(211, 64, 255), new Color(255, 128, 16) };
  
  private Comparator<Buddy> bcmp;
  
  private final Comparator<Buddy> alphacmp = new Comparator<Buddy>() {
      private final Collator c = Collator.getInstance();
      
      public int compare(BuddyWnd.Buddy a, BuddyWnd.Buddy b) {
        return this.c.compare(a.name, b.name);
      }
    };
  
  private final Comparator<Buddy> groupcmp = new Comparator<Buddy>() {
      public int compare(BuddyWnd.Buddy a, BuddyWnd.Buddy b) {
        if (a.group == b.group)
          return BuddyWnd.this.alphacmp.compare(a, b); 
        return a.group - b.group;
      }
    };
  
  private final Comparator<Buddy> statuscmp = new Comparator<Buddy>() {
      public int compare(BuddyWnd.Buddy a, BuddyWnd.Buddy b) {
        if (a.online == b.online)
          return BuddyWnd.this.alphacmp.compare(a, b); 
        return b.online - a.online;
      }
    };
  
  @RName("buddy")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      BuddyWnd.instance = new BuddyWnd(c, parent);
      return BuddyWnd.instance;
    }
  }
  
  public class Buddy {
    public int id;
    
    public String name;
    
    Text rname = null;
    
    public int online;
    
    public int group;
    
    public boolean seen;
    
    public Buddy(int id, String name, int online, int group, boolean seen) {
      this.id = id;
      this.name = name;
      this.online = online;
      this.group = group;
      this.seen = seen;
    }
    
    public void forget() {
      BuddyWnd.this.wdgmsg("rm", new Object[] { Integer.valueOf(this.id) });
    }
    
    public void endkin() {
      BuddyWnd.this.wdgmsg("rm", new Object[] { Integer.valueOf(this.id) });
    }
    
    public void chat() {
      BuddyWnd.this.wdgmsg("chat", new Object[] { Integer.valueOf(this.id) });
    }
    
    public void invite() {
      BuddyWnd.this.wdgmsg("inv", new Object[] { Integer.valueOf(this.id) });
    }
    
    public void describe() {
      BuddyWnd.this.wdgmsg("desc", new Object[] { Integer.valueOf(this.id) });
    }
    
    public void chname(String name) {
      BuddyWnd.this.wdgmsg("nick", new Object[] { Integer.valueOf(this.id), name });
    }
    
    public void chgrp(int grp) {
      BuddyWnd.this.wdgmsg("grp", new Object[] { Integer.valueOf(this.id), Integer.valueOf(grp) });
    }
    
    public Text rname() {
      if (this.rname == null || !this.rname.text.equals(this.name))
        this.rname = Text.render(this.name); 
      return this.rname;
    }
  }
  
  public Iterator<Buddy> iterator() {
    synchronized (this.buddies) {
      return (new ArrayList<>(this.buddies)).iterator();
    } 
  }
  
  public Buddy find(int id) {
    synchronized (this.buddies) {
      return this.idmap.get(Integer.valueOf(id));
    } 
  }
  
  public static class CTextEntry extends TextEntry {
    public String lastset = "";
    
    public boolean ch = false;
    
    public CTextEntry(Coord c, int w, Widget parent) {
      super(c, w, parent, "");
    }
    
    public void update(String text) {
      settext(this.lastset = text);
      this.ch = false;
    }
    
    protected void changed() {
      this.ch = true;
    }
    
    protected void drawbg(GOut g) {
      if (this.ch) {
        g.chcolor(32, 24, 8, 255);
        g.frect(Coord.z, this.sz);
        g.chcolor();
      } else {
        g.chcolor(0, 0, 0, 255);
        g.frect(Coord.z, this.sz);
        g.chcolor();
      } 
    }
    
    public boolean type(char c, KeyEvent ev) {
      if (c == '\033' && this.ch) {
        settext(this.lastset);
        this.ch = false;
        return true;
      } 
      return super.type(c, ev);
    }
  }
  
  public static class GroupSelector extends Widget {
    public int group;
    
    public GroupSelector(Coord c, Widget parent, int group) {
      super(c, new Coord(BuddyWnd.gc.length * 20 + 20, 20), parent);
      this.group = group;
    }
    
    public void draw(GOut g) {
      for (int i = 0; i < BuddyWnd.gc.length; i++) {
        if (i == this.group) {
          g.chcolor();
          g.frect(new Coord(i * 20, 0), new Coord(19, 19));
        } 
        g.chcolor(BuddyWnd.gc[i]);
        g.frect(new Coord(2 + i * 20, 2), new Coord(15, 15));
      } 
      g.chcolor();
    }
    
    public boolean mousedown(Coord c, int button) {
      if (c.y >= 2 && c.y < 17) {
        int g = (c.x - 2) / 20;
        if (g >= 0 && g < BuddyWnd.gc.length && c.x >= 2 + g * 20 && c.x < 17 + g * 20) {
          changed(g);
          return true;
        } 
      } 
      return super.mousedown(c, button);
    }
    
    protected void changed(int group) {
      this.group = group;
    }
  }
  
  private class BuddyList extends Listbox<Buddy> {
    public BuddyList(Coord c, int w, int h, Widget parent) {
      super(c, parent, w, h, 20);
    }
    
    public BuddyWnd.Buddy listitem(int idx) {
      return BuddyWnd.this.buddies.get(idx);
    }
    
    public int listitems() {
      return BuddyWnd.this.buddies.size();
    }
    
    public void drawitem(GOut g, BuddyWnd.Buddy b) {
      if (b.online == 1) {
        g.image(BuddyWnd.online, Coord.z);
      } else if (b.online == 0) {
        g.image(BuddyWnd.offline, Coord.z);
      } 
      g.chcolor(BuddyWnd.gc[b.group]);
      g.aimage(b.rname().tex(), new Coord(25, 10), 0.0D, 0.5D);
      g.chcolor();
    }
    
    public void draw(GOut g) {
      if (BuddyWnd.this.buddies.size() == 0)
        g.atext("You are alone in the world", this.sz.div(2), 0.5D, 0.5D); 
      super.draw(g);
    }
    
    public void change(BuddyWnd.Buddy b) {
      this.sel = b;
      if (b == null) {
        if (BuddyWnd.this.editing != null) {
          BuddyWnd.this.editing = null;
          this.ui.destroy(BuddyWnd.this.nicksel);
          this.ui.destroy(BuddyWnd.this.grpsel);
        } 
      } else {
        if (BuddyWnd.this.editing == null) {
          BuddyWnd.this.nicksel = new BuddyWnd.CTextEntry(new Coord(6, 165), 188, BuddyWnd.this) {
              public void activate(String text) {
                BuddyWnd.this.editing.chname(text);
              }
            };
          BuddyWnd.this.grpsel = new BuddyWnd.GroupSelector(new Coord(6, 190), BuddyWnd.this, 0) {
              public void changed(int group) {
                BuddyWnd.this.editing.chgrp(group);
              }
            };
          BuddyWnd.this.setfocus(BuddyWnd.this.nicksel);
        } 
        BuddyWnd.this.editing = b;
        BuddyWnd.this.nicksel.update(b.name);
        BuddyWnd.this.nicksel.buf.point = BuddyWnd.this.nicksel.buf.line.length();
        BuddyWnd.this.grpsel.group = b.group;
      } 
    }
    
    public void opts(final BuddyWnd.Buddy b, Coord c) {
      List<String> opts = new ArrayList<>();
      if (b.online >= 0) {
        opts.add("Chat");
        if (b.online == 1)
          opts.add("Invite"); 
        opts.add("End kinship");
      } else {
        opts.add("Forget");
      } 
      if (b.seen)
        opts.add("Describe"); 
      if (BuddyWnd.this.menu == null)
        BuddyWnd.this.menu = new FlowerMenu(c, this.ui.root, opts.<String>toArray(new String[opts.size()])) {
            public void destroy() {
              BuddyWnd.this.menu = null;
              super.destroy();
            }
            
            public void choose(FlowerMenu.Petal opt) {
              if (opt != null) {
                if (opt.name.equals("End kinship")) {
                  b.endkin();
                } else if (opt.name.equals("Chat")) {
                  b.chat();
                } else if (opt.name.equals("Invite")) {
                  b.invite();
                } else if (opt.name.equals("Forget")) {
                  b.forget();
                } else if (opt.name.equals("Describe")) {
                  b.describe();
                } 
                uimsg("act", new Object[] { Integer.valueOf(opt.num) });
              } else {
                uimsg("cancel", new Object[0]);
              } 
            }
          }; 
    }
    
    public void itemclick(BuddyWnd.Buddy b, int button) {
      if (button == 1) {
        change(b);
      } else if (button == 3) {
        opts(b, this.ui.mc);
      } 
    }
  }
  
  public BuddyWnd(Coord c, Widget parent) {
    super(c, new Coord(200, 515), parent, "Kin");
    int yS = 20;
    kinCount = new Label(new Coord(0, 0), this, "test 1");
    kinCount.f = new Text.Foundry(new Font("SansSerif", 1, 12));
    this.bl = new BuddyList(new Coord(6, 25), 180, 7, this);
    new Label(new Coord(0, 243), this, "Sort by:");
    this.sbstatus = new Button(new Coord(50, 240), Integer.valueOf(48), this, "Status") {
        public void click() {
          BuddyWnd.this.setcmp(BuddyWnd.this.statuscmp);
        }
      };
    this.sbgroup = new Button(new Coord(100, 240), Integer.valueOf(48), this, "Group") {
        public void click() {
          BuddyWnd.this.setcmp(BuddyWnd.this.groupcmp);
        }
      };
    this.sbalpha = new Button(new Coord(150, 240), Integer.valueOf(48), this, "Name") {
        public void click() {
          BuddyWnd.this.setcmp(BuddyWnd.this.alphacmp);
        }
      };
    String sort = Utils.getpref("buddysort", "");
    if (sort.equals("")) {
      this.bcmp = this.statuscmp;
    } else {
      if (sort.equals("alpha"))
        this.bcmp = this.alphacmp; 
      if (sort.equals("group"))
        this.bcmp = this.groupcmp; 
      if (sort.equals("status"))
        this.bcmp = this.statuscmp; 
    } 
    int y = 265;
    new HRuler(new Coord(0, y), 200, this);
    y += 5;
    new Label(new Coord(0, y), this, "Search name:");
    y += 15;
    this.searchName = new CTextEntry(new Coord(0, y), 200, this) {
        private int si;
        
        protected void changed() {
          if (this.text.length() > 0) {
            String sn = this.text;
            this.si = 0;
            if (BuddyWnd.this.buddies.size() <= 0)
              return; 
            findBuddyByName(sn);
          } else if (BuddyWnd.this.buddies.size() > 0) {
            BuddyWnd.this.bl.sb.val = 0;
            BuddyWnd.this.bl.change(BuddyWnd.this.buddies.get(0));
          } 
        }
        
        private void findBuddyByName(String sn) {
          if (BuddyWnd.this.buddies.size() <= 0)
            return; 
          if (sn.equals("*"))
            return; 
          BuddyWnd.Buddy target = null;
          boolean repeatOnce = false;
          if (this.si > 0)
            repeatOnce = true; 
          for (; this.si < BuddyWnd.this.buddies.size(); this.si++) {
            if (sn.startsWith("*")) {
              String snt = sn.substring(1);
              if ((BuddyWnd.this.buddies.get(this.si)).name.toLowerCase().contains(snt)) {
                target = BuddyWnd.this.buddies.get(this.si);
                break;
              } 
            } else if ((BuddyWnd.this.buddies.get(this.si)).name.toLowerCase().startsWith(sn.toLowerCase())) {
              target = BuddyWnd.this.buddies.get(this.si);
              break;
            } 
          } 
          if (target != null) {
            BuddyWnd.this.bl.change(target);
            BuddyWnd.this.bl.sb.val = this.si;
            BuddyWnd.this.setfocus(BuddyWnd.this.searchName);
          } 
          if (target == null && repeatOnce) {
            this.si = 0;
            findBuddyByName(sn);
          } 
        }
        
        public void activate(String text) {
          if (this.text.length() > 0) {
            String sn = this.text;
            this.si++;
            findBuddyByName(sn);
          } 
        }
      };
    y += 30;
    new Label(new Coord(0, y), this, "Presentation name:");
    y += 15;
    this.pname = new CTextEntry(new Coord(0, y), 200, this) {
        public void activate(String text) {
          BuddyWnd.this.wdgmsg("pname", new Object[] { text });
        }
      };
    y += 30;
    new Button(new Coord(68, y), Integer.valueOf(64), this, "Set") {
        public void click() {
          BuddyWnd.this.wdgmsg("pname", new Object[] { (BuddyWnd.access$1100(this.this$0)).text });
        }
      };
    y += 25;
    new HRuler(new Coord(0, y), 200, this);
    y += 5;
    new Label(new Coord(0, y), this, "My homestead secret:");
    y += 15;
    this.charpass = new CTextEntry(new Coord(0, y), 200, this) {
        public void activate(String text) {
          BuddyWnd.this.wdgmsg("pwd", new Object[] { text });
        }
      };
    y += 25;
    new Button(new Coord(0, y), Integer.valueOf(64), this, "Set") {
        public void click() {
          BuddyWnd.this.sendpwd(BuddyWnd.this.charpass.text);
        }
      };
    new Button(new Coord(68, y), Integer.valueOf(64), this, "Clear") {
        public void click() {
          BuddyWnd.this.sendpwd("");
        }
      };
    new Button(new Coord(136, y), Integer.valueOf(64), this, "Random") {
        public void click() {
          BuddyWnd.this.sendpwd(BuddyWnd.this.randpwd());
        }
      };
    y += 25;
    new HRuler(new Coord(0, y), 200, this);
    y += 5;
    new Label(new Coord(0, y), this, "Make kin by homestead secret:");
    y += 15;
    this.opass = new TextEntry(new Coord(0, y), 200, this, "") {
        public void activate(String text) {
          BuddyWnd.this.wdgmsg("bypwd", new Object[] { text });
          settext("");
        }
      };
    y += 25;
    new Button(new Coord(68, y), Integer.valueOf(64), this, "Add kin") {
        public void click() {
          BuddyWnd.this.wdgmsg("bypwd", new Object[] { (BuddyWnd.access$1500(this.this$0)).text });
          BuddyWnd.this.opass.settext("");
        }
      };
  }
  
  private String randpwd() {
    String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < 8; i++)
      buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt((int)(Math.random() * "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length()))); 
    return buf.toString();
  }
  
  private void sendpwd(String pass) {
    wdgmsg("pwd", new Object[] { pass });
  }
  
  private void setcmp(Comparator<Buddy> cmp) {
    this.bcmp = cmp;
    String val = "";
    if (cmp == this.alphacmp)
      val = "alpha"; 
    if (cmp == this.groupcmp)
      val = "group"; 
    if (cmp == this.statuscmp)
      val = "status"; 
    Utils.setpref("buddysort", val);
    synchronized (this.buddies) {
      Collections.sort(this.buddies, this.bcmp);
    } 
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg.equals("add")) {
      int id = ((Integer)args[0]).intValue();
      String name = ((String)args[1]).intern();
      int online = ((Integer)args[2]).intValue();
      int group = ((Integer)args[3]).intValue();
      boolean seen = (((Integer)args[4]).intValue() != 0);
      Buddy b = new Buddy(id, name, online, group, seen);
      synchronized (this.buddies) {
        this.buddies.add(b);
        this.idmap.put(Integer.valueOf(b.id), b);
        Collections.sort(this.buddies, this.bcmp);
      } 
      if (Config.auto_unkin_red && group == 2)
        b.endkin(); 
      this.serial++;
    } else if (msg.equals("rm")) {
      Buddy b;
      int id = ((Integer)args[0]).intValue();
      synchronized (this.buddies) {
        b = this.idmap.get(Integer.valueOf(id));
        if (b != null) {
          this.buddies.remove(b);
          this.idmap.remove(Integer.valueOf(id));
        } 
      } 
      if (b == this.editing) {
        this.editing = null;
        this.ui.destroy(this.nicksel);
        this.ui.destroy(this.grpsel);
      } 
      this.serial++;
    } else if (msg.equals("chst")) {
      int id = ((Integer)args[0]).intValue();
      int online = ((Integer)args[1]).intValue();
      Buddy b = find(id);
      b.online = online;
      this.ui.message(String.format("%s is %s now.", new Object[] { b.name, (online > 0) ? "ONLINE" : "OFFLINE" }), gc[b.group]);
      if (Config.auto_unkin_red && online > 0 && 
        b.group == 2)
        b.endkin(); 
      if (Config.chat_online_colour) {
        List<ChatUI.Selector.DarkChannel> chls = this.ui.gui.chat.chansel.chls;
        for (ChatUI.Selector.DarkChannel dc : chls) {
          if (dc.chan instanceof ChatUI.PrivChat)
            try {
              int other = ((ChatUI.PrivChat)dc.chan).other;
              if (other == id)
                dc.rname = null; 
            } catch (Exception exception) {} 
        } 
      } 
    } else if (msg.equals("upd")) {
      int id = ((Integer)args[0]).intValue();
      String name = (String)args[1];
      int online = ((Integer)args[2]).intValue();
      int grp = ((Integer)args[3]).intValue();
      boolean seen = (((Integer)args[4]).intValue() != 0);
      Buddy b = find(id);
      synchronized (b) {
        b.name = name;
        b.online = online;
        b.group = grp;
        b.seen = seen;
      } 
      if (b == this.editing) {
        this.nicksel.update(b.name);
        this.grpsel.group = b.group;
      } 
      if (Config.auto_unkin_red && online > 0 && 
        b.group == 2)
        b.endkin(); 
      this.serial++;
    } else if (msg.equals("sel")) {
      int id = ((Integer)args[0]).intValue();
      show();
      raise();
      this.bl.change(find(id));
    } else if (msg.equals("pwd")) {
      this.charpass.update((String)args[0]);
    } else if (msg.equals("pname")) {
      this.pname.update((String)args[0]);
    } else {
      super.uimsg(msg, args);
    } 
    setKinCount();
  }
  
  public void hide() {
    if (this.menu != null) {
      this.ui.destroy(this.menu);
      this.menu = null;
    } 
    super.hide();
    setKinCount();
  }
  
  public void destroy() {
    if (this.menu != null) {
      this.ui.destroy(this.menu);
      this.menu = null;
    } 
    super.destroy();
  }
  
  public static void unkinAllRedOnce() {
    if (instance != null)
      for (Buddy b : instance.buddies) {
        if (b.group == 2)
          b.endkin(); 
      }  
  }
  
  private static void setKinCount() {
    if (kCRunning) {
      kCD = 100;
    } else {
      kCRunning = true;
      (new Thread(new Runnable() {
            public void run() {
              try {
                while (BuddyWnd.kCD > 0) {
                  Thread.sleep(20L);
                  BuddyWnd.kCD--;
                } 
                int onC = 0;
                int kinC = 0;
                int totalC = 0;
                for (BuddyWnd.Buddy b : BuddyWnd.instance.buddies) {
                  if (b.online > 0)
                    onC++; 
                  if (b.online >= 0)
                    kinC++; 
                  totalC++;
                } 
                BuddyWnd.kinCount.settext("On: " + onC + "  Kin: " + kinC + " Total:  " + totalC);
              } catch (Exception exception) {}
              BuddyWnd.kCRunning = false;
            }
          },  "kinCount")).start();
    } 
  }
}
