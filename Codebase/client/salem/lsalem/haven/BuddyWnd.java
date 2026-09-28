package haven;

import java.awt.Color;
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
   private final List<BuddyWnd.Buddy> buddies = new ArrayList<>();
   private Map<Integer, BuddyWnd.Buddy> idmap = new HashMap<>();
   private BuddyWnd.BuddyList bl;
   private Button sbalpha;
   private Button sbgroup;
   private Button sbstatus;
   private BuddyWnd.CTextEntry nicksel;
   private BuddyWnd.CTextEntry pname;
   private BuddyWnd.CTextEntry charpass;
   private BuddyWnd.Buddy editing = null;
   private TextEntry opass;
   private BuddyWnd.GroupSelector grpsel;
   private FlowerMenu menu;
   public int serial = 0;
   public static final Tex online = Resource.loadtex("gfx/hud/online");
   public static final Tex offline = Resource.loadtex("gfx/hud/offline");
   public static final Color[] gc = new Color[]{
      new Color(255, 255, 255),
      new Color(64, 255, 64),
      new Color(255, 64, 64),
      new Color(96, 160, 255),
      new Color(0, 255, 255),
      new Color(255, 255, 0),
      new Color(211, 64, 255),
      new Color(255, 128, 16)
   };
   private Comparator<BuddyWnd.Buddy> bcmp;
   private Comparator<BuddyWnd.Buddy> alphacmp = new Comparator<BuddyWnd.Buddy>() {
      private Collator c = Collator.getInstance();

      public int compare(BuddyWnd.Buddy a, BuddyWnd.Buddy b) {
         return this.c.compare(a.name, b.name);
      }
   };
   private Comparator<BuddyWnd.Buddy> groupcmp = new Comparator<BuddyWnd.Buddy>() {
      public int compare(BuddyWnd.Buddy a, BuddyWnd.Buddy b) {
         return a.group == b.group ? BuddyWnd.this.alphacmp.compare(a, b) : a.group - b.group;
      }
   };
   private Comparator<BuddyWnd.Buddy> statuscmp = new Comparator<BuddyWnd.Buddy>() {
      public int compare(BuddyWnd.Buddy a, BuddyWnd.Buddy b) {
         return a.online == b.online ? BuddyWnd.this.alphacmp.compare(a, b) : b.online - a.online;
      }
   };

   @Override
   public Iterator<BuddyWnd.Buddy> iterator() {
      synchronized (this.buddies) {
         return new ArrayList<>(this.buddies).iterator();
      }
   }

   public BuddyWnd.Buddy find(int id) {
      synchronized (this.buddies) {
         return this.idmap.get(id);
      }
   }

   public BuddyWnd(Coord c, Widget parent) {
      super(c, new Coord(200, 450), parent, "Kin");
      this.bl = new BuddyWnd.BuddyList(new Coord(6, 5), 180, 7, this);
      new Label(new Coord(0, 223), this, "Sort by:");
      this.sbstatus = new Button(new Coord(50, 220), 48, this, "Status") {
         @Override
         public void click() {
            BuddyWnd.this.setcmp(BuddyWnd.this.statuscmp);
         }
      };
      this.sbgroup = new Button(new Coord(100, 220), 48, this, "Group") {
         @Override
         public void click() {
            BuddyWnd.this.setcmp(BuddyWnd.this.groupcmp);
         }
      };
      this.sbalpha = new Button(new Coord(150, 220), 48, this, "Name") {
         @Override
         public void click() {
            BuddyWnd.this.setcmp(BuddyWnd.this.alphacmp);
         }
      };
      String sort = Utils.getpref("buddysort", "");
      if (sort.equals("")) {
         this.bcmp = this.statuscmp;
      } else {
         if (sort.equals("alpha")) {
            this.bcmp = this.alphacmp;
         }

         if (sort.equals("group")) {
            this.bcmp = this.groupcmp;
         }

         if (sort.equals("status")) {
            this.bcmp = this.statuscmp;
         }
      }

      new HRuler(new Coord(0, 245), 200, this);
      new Label(new Coord(0, 250), this, "Presentation name:");
      this.pname = new BuddyWnd.CTextEntry(new Coord(0, 265), 200, this) {
         @Override
         public void activate(String text) {
            BuddyWnd.this.wdgmsg("pname", new Object[]{text});
         }
      };
      new Button(new Coord(68, 290), 64, this, "Set") {
         @Override
         public void click() {
            BuddyWnd.this.wdgmsg("pname", new Object[]{BuddyWnd.this.pname.text});
         }
      };
      new HRuler(new Coord(0, 315), 200, this);
      new Label(new Coord(0, 320), this, "My homestead secret:");
      this.charpass = new BuddyWnd.CTextEntry(new Coord(0, 335), 200, this) {
         @Override
         public void activate(String text) {
            BuddyWnd.this.wdgmsg("pwd", new Object[]{text});
         }
      };
      new Button(new Coord(0, 360), 64, this, "Set") {
         @Override
         public void click() {
            BuddyWnd.this.sendpwd(BuddyWnd.this.charpass.text);
         }
      };
      new Button(new Coord(68, 360), 64, this, "Clear") {
         @Override
         public void click() {
            BuddyWnd.this.sendpwd("");
         }
      };
      new Button(new Coord(136, 360), 64, this, "Random") {
         @Override
         public void click() {
            BuddyWnd.this.sendpwd(BuddyWnd.this.randpwd());
         }
      };
      new HRuler(new Coord(0, 385), 200, this);
      new Label(new Coord(0, 390), this, "Make kin by homestead secret:");
      this.opass = new TextEntry(new Coord(0, 405), 200, this, "") {
         @Override
         public void activate(String text) {
            BuddyWnd.this.wdgmsg("bypwd", new Object[]{text});
            this.settext("");
         }
      };
      new Button(new Coord(68, 430), 64, this, "Add kin") {
         @Override
         public void click() {
            BuddyWnd.this.wdgmsg("bypwd", new Object[]{BuddyWnd.this.opass.text});
            BuddyWnd.this.opass.settext("");
         }
      };
   }

   private String randpwd() {
      String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      StringBuilder buf = new StringBuilder();

      for (int i = 0; i < 8; i++) {
         buf.append(charset.charAt((int)(Math.random() * charset.length())));
      }

      return buf.toString();
   }

   private void sendpwd(String pass) {
      this.wdgmsg("pwd", new Object[]{pass});
   }

   private void setcmp(Comparator<BuddyWnd.Buddy> cmp) {
      this.bcmp = cmp;
      String val = "";
      if (cmp == this.alphacmp) {
         val = "alpha";
      }

      if (cmp == this.groupcmp) {
         val = "group";
      }

      if (cmp == this.statuscmp) {
         val = "status";
      }

      Utils.setpref("buddysort", val);
      synchronized (this.buddies) {
         Collections.sort(this.buddies, this.bcmp);
      }
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg.equals("add")) {
         int id = (Integer)args[0];
         String name = ((String)args[1]).intern();
         int online = (Integer)args[2];
         int group = (Integer)args[3];
         boolean seen = (Integer)args[4] != 0;
         BuddyWnd.Buddy b = new BuddyWnd.Buddy(id, name, online, group, seen);
         synchronized (this.buddies) {
            this.buddies.add(b);
            this.idmap.put(b.id, b);
            Collections.sort(this.buddies, this.bcmp);
         }

         this.serial++;
      } else if (msg.equals("rm")) {
         int id = (Integer)args[0];
         BuddyWnd.Buddy b;
         synchronized (this.buddies) {
            b = this.idmap.get(id);
            if (b != null) {
               this.buddies.remove(b);
               this.idmap.remove(id);
            }
         }

         if (b == this.editing) {
            this.editing = null;
            this.ui.destroy(this.nicksel);
            this.ui.destroy(this.grpsel);
         }

         this.serial++;
      } else if (msg.equals("chst")) {
         int id = (Integer)args[0];
         int online = (Integer)args[1];
         BuddyWnd.Buddy b = this.find(id);
         b.online = online;
         this.ui.message(String.format("%s is %s now.", b.name, online > 0 ? "ONLINE" : "OFFLINE"), gc[b.group]);
      } else if (msg.equals("upd")) {
         int id = (Integer)args[0];
         String name = (String)args[1];
         int online = (Integer)args[2];
         int grp = (Integer)args[3];
         boolean seen = (Integer)args[4] != 0;
         BuddyWnd.Buddy b = this.find(id);
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

         this.serial++;
      } else if (msg.equals("sel")) {
         int id = (Integer)args[0];
         this.show();
         this.raise();
         this.bl.change(this.find(id));
      } else if (msg.equals("pwd")) {
         this.charpass.update((String)args[0]);
      } else if (msg.equals("pname")) {
         this.pname.update((String)args[0]);
      } else {
         super.uimsg(msg, args);
      }
   }

   @Override
   public void hide() {
      if (this.menu != null) {
         this.ui.destroy(this.menu);
         this.menu = null;
      }

      super.hide();
   }

   @Override
   public void destroy() {
      if (this.menu != null) {
         this.ui.destroy(this.menu);
         this.menu = null;
      }

      super.destroy();
   }

   @Widget.RName("buddy")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new BuddyWnd(c, parent);
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
         BuddyWnd.this.wdgmsg("rm", new Object[]{this.id});
      }

      public void endkin() {
         BuddyWnd.this.wdgmsg("rm", new Object[]{this.id});
      }

      public void chat() {
         BuddyWnd.this.wdgmsg("chat", new Object[]{this.id});
      }

      public void invite() {
         BuddyWnd.this.wdgmsg("inv", new Object[]{this.id});
      }

      public void describe() {
         BuddyWnd.this.wdgmsg("desc", new Object[]{this.id});
      }

      public void chname(String name) {
         BuddyWnd.this.wdgmsg("nick", new Object[]{this.id, name});
      }

      public void chgrp(int grp) {
         BuddyWnd.this.wdgmsg("grp", new Object[]{this.id, grp});
      }

      public Text rname() {
         if (this.rname == null || !this.rname.text.equals(this.name)) {
            this.rname = Text.render(this.name);
         }

         return this.rname;
      }
   }

   private class BuddyList extends Listbox<BuddyWnd.Buddy> {
      public BuddyList(Coord c, int w, int h, Widget parent) {
         super(c, parent, w, h, 20);
      }

      public BuddyWnd.Buddy listitem(int idx) {
         return BuddyWnd.this.buddies.get(idx);
      }

      @Override
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
         g.aimage(b.rname().tex(), new Coord(25, 10), 0.0, 0.5);
         g.chcolor();
      }

      @Override
      public void draw(GOut g) {
         if (BuddyWnd.this.buddies.size() == 0) {
            g.atext("You are alone in the world", this.sz.div(2), 0.5, 0.5);
         }

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
                  @Override
                  public void activate(String text) {
                     BuddyWnd.this.editing.chname(text);
                  }
               };
               BuddyWnd.this.grpsel = new BuddyWnd.GroupSelector(new Coord(6, 190), BuddyWnd.this, 0) {
                  @Override
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
            if (b.online == 1) {
               opts.add("Invite");
            }

            opts.add("End kinship");
         } else {
            opts.add("Forget");
         }

         if (b.seen) {
            opts.add("Describe");
         }

         if (BuddyWnd.this.menu == null) {
            BuddyWnd.this.menu = new FlowerMenu(c, this.ui.root, opts.toArray(new String[opts.size()])) {
               @Override
               public void destroy() {
                  BuddyWnd.this.menu = null;
                  super.destroy();
               }

               @Override
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

                     this.uimsg("act", new Object[]{opt.num});
                  } else {
                     this.uimsg("cancel", new Object[0]);
                  }
               }
            };
         }
      }

      public void itemclick(BuddyWnd.Buddy b, int button) {
         if (button == 1) {
            this.change(b);
         } else if (button == 3) {
            this.opts(b, this.ui.mc);
         }
      }
   }

   public static class CTextEntry extends TextEntry {
      public String lastset = "";
      public boolean ch = false;

      public CTextEntry(Coord c, int w, Widget parent) {
         super(c, w, parent, "");
      }

      public void update(String text) {
         this.settext(this.lastset = text);
         this.ch = false;
      }

      @Override
      public void changed() {
         this.ch = true;
      }

      @Override
      protected void drawbg(GOut g) {
         if (this.ch) {
            g.chcolor(248, 255, 224, 255);
            g.frect(Coord.z, this.sz);
            g.chcolor();
         } else {
            g.frect(Coord.z, this.sz);
         }
      }

      @Override
      public boolean type(char c, KeyEvent ev) {
         if (c == 27 && this.ch) {
            this.settext(this.lastset);
            this.ch = false;
            return true;
         } else {
            return super.type(c, ev);
         }
      }
   }

   public static class GroupSelector extends Widget {
      public int group;

      public GroupSelector(Coord c, Widget parent, int group) {
         super(c, new Coord(BuddyWnd.gc.length * 20 + 20, 20), parent);
         this.group = group;
      }

      @Override
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

      @Override
      public boolean mousedown(Coord c, int button) {
         if (c.y >= 2 && c.y < 17) {
            int g = (c.x - 2) / 20;
            if (g >= 0 && g < BuddyWnd.gc.length && c.x >= 2 + g * 20 && c.x < 17 + g * 20) {
               this.changed(g);
               return true;
            }
         }

         return super.mousedown(c, button);
      }

      protected void changed(int group) {
         this.group = group;
      }
   }
}
