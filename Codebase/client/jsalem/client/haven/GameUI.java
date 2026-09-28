package haven;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Map.Entry;

public class GameUI extends ConsoleHost implements Console.Directory {
   public final String chrid;
   public final long plid;
   public MenuGrid menu;
   public Tempers tm;
   public Widget gobble;
   public MapView map;
   public LocalMiniMap mmap;
   public Fightview fv;
   public static final Text.Foundry errfoundry = new Text.Foundry(new Font("SansSerif", 1, 14), new Color(192, 0, 0));
   private Text lasterr;
   private long errtime;
   public GameUI.InvWindow invwnd;
   protected Window equwnd;
   protected Window makewnd;
   public Inventory maininv;
   public GameUI.MainMenu mainmenu;
   public BuddyWnd buddies;
   public CharWnd chrwdg;
   public Polity polity;
   public HelpWnd help;
   public OptWnd opts;
   public Store storewnd;
   public Collection<GItem> hand = new LinkedList<>();
   private WItem vhand;
   public ChatUI chat;
   public FlatnessTool flat;
   public ChatUI.Channel syslog;
   public int prog = -1;
   private boolean afk = false;
   public Indir<Resource>[] belt = new Indir[144];
   public Indir<Resource> lblk;
   public Indir<Resource> dblk;
   public GameUI.Belt beltwdg;
   public String polowner;
   public int weight;
   private Widget attrview;
   static Text.Furnace progf = new PUtils.BlurFurn(new Text.Foundry(new Font("serif", 1, 24)).aa(true), 2, 1, new Color(0, 16, 16));
   Text progt = null;
   private boolean dwalking = false;
   private Coord dwalkang = new Coord();
   private long dwalkhys;
   private float dwalkbase;
   private boolean[] dkeys = new boolean[]{false, false, false, false};
   private static final Tex menubg = Resource.loadtex("gfx/hud/menubg");
   private static final Resource errsfx = Resource.load("sfx/error");
   private Map<String, Console.Command> cmdmap;

   public GameUI(Widget parent, String chrid, long plid) {
      super(Coord.z, parent.sz, parent);
      this.ui.gui = this;
      String val = Utils.getpref("belttype", "n");
      if (val.equals("n")) {
         this.beltwdg = new GameUI.NKeyBelt(Coord.z, this);
      } else if (val.equals("f")) {
         this.beltwdg = new GameUI.FKeyBelt(Coord.z, this);
      } else {
         this.beltwdg = new GameUI.NKeyBelt(Coord.z, this);
      }

      this.cmdmap = new TreeMap<>();
      this.cmdmap.put("afk", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            GameUI.this.afk = true;
            GameUI.this.wdgmsg("afk", new Object[0]);
         }
      });
      this.cmdmap.put("act", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            Object[] ad = new Object[args.length - 1];
            System.arraycopy(args, 1, ad, 0, ad.length);
            GameUI.this.wdgmsg("act", ad);
         }
      });
      this.cmdmap.put("belt", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            if (args[1].equals("f")) {
               GameUI.this.beltwdg.destroy();
               GameUI.this.beltwdg = GameUI.this.new FKeyBelt(Coord.z, GameUI.this);
               Utils.setpref("belttype", "f");
               GameUI.this.resize(GameUI.this.sz);
            } else if (args[1].equals("n")) {
               GameUI.this.beltwdg.destroy();
               GameUI.this.beltwdg = GameUI.this.new NKeyBelt(Coord.z, GameUI.this);
               Utils.setpref("belttype", "n");
               GameUI.this.resize(GameUI.this.sz);
            }
         }
      });
      this.cmdmap.put("tool", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            Widget.gettype(args[1]).create(new Coord(200, 200), GameUI.this, new Object[0]);
         }
      });
      this.cmdmap.put("flatness", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            FlatnessTool.instance(GameUI.this);
         }
      });
      this.chrid = chrid;
      this.plid = plid;
      this.setcanfocus(true);
      this.setfocusctl(true);
      this.menu = new MenuGrid(Coord.z, this);
      new FramedAva(new Coord(2, 2), Avaview.dasz, this, plid, "avacam") {
         @Override
         public boolean mousedown(Coord c, int button) {
            return true;
         }
      };
      new Bufflist(new Coord(80, 2), this);
      this.tm = new Tempers(Coord.z, this);
      this.chat = new ChatUI(Coord.z, 0, this);
      this.syslog = new ChatUI.Log(this.chat, "System");
      this.ui.cons.out = new PrintWriter(new Writer() {
         StringBuilder buf = new StringBuilder();

         @Override
         public void write(char[] src, int off, int len) {
            this.buf.append(src, off, len);

            int p;
            while ((p = this.buf.indexOf("\n")) >= 0) {
               GameUI.this.syslog.append(this.buf.substring(0, p), Color.WHITE);
               this.buf.delete(0, p + 1);
            }
         }

         @Override
         public void close() {
         }

         @Override
         public void flush() {
         }
      });
      this.opts = new OptWnd(this.sz.sub(200, 200).div(2), this);
      this.opts.hide();
      this.makemenu();
      this.resize(this.sz);
   }

   private void updhand() {
      if (this.hand.isEmpty() && this.vhand != null || this.vhand != null && !this.hand.contains(this.vhand.item)) {
         this.ui.destroy(this.vhand);
         this.vhand = null;
      }

      if (!this.hand.isEmpty() && this.vhand == null) {
         GItem fi = this.hand.iterator().next();
         this.vhand = new ItemDrag(new Coord(15, 15), this, fi);
      }
   }

   @Override
   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      String place = ((String)pargs[0]).intern();
      if (place == "mapview") {
         Coord cc = (Coord)cargs[0];
         this.map = new MapView(Coord.z, this.sz, this, cc, this.plid);
         this.map.lower();
         if (this.mmap != null) {
            this.ui.destroy(this.mmap);
         }

         this.mmap = new LocalMiniMap(new Coord(this.sz.x - 250, 15), new Coord(146, 146), this, this.map);
         return this.map;
      } else if (place == "fight") {
         this.fv = (Fightview)gettype(type).create(new Coord(this.sz.x - Fightview.width, 0), this, cargs);
         return this.fv;
      } else if (place == "inv") {
         String nm = pargs.length > 1 ? (String)pargs[1] : null;
         if (this.invwnd == null) {
            this.invwnd = new GameUI.InvWindow(new Coord(100, 100), Coord.z, this, "Inventory", this);
            this.invwnd.hide();
         }

         if (nm == null) {
            Inventory inv = (Inventory)this.invwnd.makechild(type, new Object[0], cargs);
            this.maininv = inv;
            return inv;
         } else {
            return this.invwnd.makechild(type, new Object[]{nm}, cargs);
         }
      } else if (place == "equ") {
         this.equwnd = new GameUI.Hidewnd(new Coord(400, 10), Coord.z, this, "Equipment");
         Widget equ = gettype(type).create(Coord.z, this.equwnd, cargs);
         this.equwnd.pack();
         this.equwnd.hide();
         return equ;
      } else if (place == "hand") {
         GItem g = (GItem)gettype(type).create((Coord)pargs[1], this, cargs);
         this.hand.add(g);
         this.updhand();
         return g;
      } else if (place == "craft") {
         final Widget[] mk = new Widget[]{null};
         this.makewnd = new Window(new Coord(350, 100), Coord.z, this, "Crafting") {
            @Override
            public void wdgmsg(Widget sender, String msg, Object... args) {
               if (sender == this && msg.equals("close")) {
                  mk[0].wdgmsg("close");
               } else {
                  super.wdgmsg(sender, msg, args);
               }
            }

            @Override
            public void cdestroy(Widget w) {
               if (w == mk[0]) {
                  this.ui.destroy(this);
                  GameUI.this.makewnd = null;
               }
            }
         };
         mk[0] = gettype(type).create(Coord.z, this.makewnd, cargs);
         this.makewnd.pack();
         return mk[0];
      } else if (place == "buddy") {
         this.buddies = (BuddyWnd)gettype(type).create(new Coord(187, 50), this, cargs);
         this.buddies.hide();
         return this.buddies;
      } else if (place == "pol") {
         this.polity = (Polity)gettype(type).create(new Coord(500, 50), this, cargs);
         this.polity.hide();
         return this.polity;
      } else if (place == "chr") {
         this.chrwdg = (CharWnd)gettype(type).create(new Coord(100, 50), this, cargs);
         this.chrwdg.hide();
         this.fixattrview(this.chrwdg);
         return this.chrwdg;
      } else if (place == "chat") {
         return this.chat.makechild(type, new Object[0], cargs);
      } else if (place == "party") {
         return gettype(type).create(new Coord(2, 80), this, cargs);
      } else if (place == "misc") {
         return gettype(type).create((Coord)pargs[1], this, cargs);
      } else {
         throw new UI.UIException("Illegal gameui child", type, pargs);
      }
   }

   @Override
   public void cdestroy(Widget w) {
      if (w instanceof GItem && this.hand.contains(w)) {
         this.hand.remove(w);
         this.updhand();
      } else if (w == this.polity) {
         this.polity = null;
      } else if (w == this.chrwdg) {
         this.chrwdg = null;
         this.attrview.destroy();
      }
   }

   private void fixattrview(final CharWnd cw) {
      final IBox box = new IBox(Window.fbox.ctl, Tex.empty, Window.fbox.cbl, Tex.empty, Window.fbox.bl, Tex.empty, Window.fbox.bt, Window.fbox.bb);
      CharWnd.Attr a = (CharWnd.Attr)cw.attrwdgs.child;
      final Coord moff = new Coord(20, 0);
      this.attrview = new Widget(Coord.z, new Coord(a.expsz.x, cw.attrwdgs.sz.y).add(moff).add(10, Window.cbtni[0].getHeight() + 10).add(box.bisz()), this) {
         boolean act = false;

         {
            Widget cbtn = new IButton(Coord.z, this, Window.cbtni[0], Window.cbtni[1], Window.cbtni[2]) {
               @Override
               public void click() {
                  act(false);
               }
            };
            cbtn.c = new Coord(this.sz.x - cbtn.sz.x, box.bt.sz().y);
            int y = cbtn.c.y + cbtn.sz.y;
            Coord ctl = box.btloff().add(5, 5);

            for (CharWnd.Attr ax = (CharWnd.Attr)cw.attrwdgs.child; ax != null; ax = (CharWnd.Attr)ax.next) {
               final CharWnd.Attr ca = ax;
               new Widget(ctl.add(0, y), ax.expsz.add(moff), this) {
                  @Override
                  public void draw(GOut g) {
                     g.image(ca.res.layer(Resource.imgc).tex(), Coord.z);
                     ca.drawmeter(g, moff, ca.expsz);
                  }
               };
               y += 20;
            }

            cw.addtwdg(new IButton(Coord.z, cw, Window.rbtni[0], Window.rbtni[1], Window.rbtni[2]) {
               @Override
               public void click() {
                  act(true);
                  cw.hide();
               }
            });
            this.presize();
            this.act(Utils.getprefb("attrview", false));
         }

         @Override
         public void draw(GOut g) {
            if (GameUI.this.fv == null || GameUI.this.fv.lsrel.isEmpty()) {
               g.chcolor(0, 0, 0, 128);
               g.frect(box.btloff(), this.sz.sub(box.bisz()));
               g.chcolor();
               super.draw(g);
               box.draw(g, Coord.z, this.sz);
            }
         }

         @Override
         public void presize() {
            this.c = new Coord(GameUI.this.sz.x - this.sz.x, (GameUI.this.menu.c.y - this.sz.y) / 2);
         }

         @Override
         public boolean show(boolean show) {
            return super.show(show && this.act);
         }

         private void act(boolean act) {
            Utils.setprefb("attrview", this.act = act);
            this.show(act);
         }
      };
   }

   private void togglecw() {
      if (this.chrwdg != null) {
         if (this.chrwdg.show(!this.chrwdg.visible)) {
            this.chrwdg.raise();
            this.fitwdg(this.chrwdg);
            this.setfocus(this.chrwdg);
         }

         this.attrview.show(!this.chrwdg.visible);
      }
   }

   @Override
   public void draw(GOut g) {
      boolean beltp = !this.chat.expanded;
      this.beltwdg.show(beltp);
      super.draw(g);
      if (this.prog >= 0) {
         String progs = String.format("%d%%", this.prog);
         if (this.progt == null || !progs.equals(this.progt.text)) {
            this.progt = progf.render(progs);
         }

         g.aimage(this.progt.tex(), new Coord(this.sz.x / 2, this.sz.y * 4 / 10), 0.5, 0.5);
      }

      int by = this.sz.y;
      if (this.chat.expanded) {
         by = Math.min(by, this.chat.c.y);
      }

      if (this.beltwdg.visible) {
         by = Math.min(by, this.beltwdg.c.y);
      }

      int bx = this.mainmenu.sz.x + 10;
      if (this.cmdline != null) {
         by -= 20;
         this.drawcmd(g, new Coord(bx, by));
      } else if (this.lasterr != null) {
         if (System.currentTimeMillis() - this.errtime > 3000L) {
            this.lasterr = null;
         } else {
            g.chcolor(0, 0, 0, 192);
            g.frect(new Coord(bx - 2, by - 22), this.lasterr.sz().add(4, 4));
            g.chcolor();
            Tex var10001 = this.lasterr.tex();
            by -= 20;
            g.image(var10001, new Coord(bx, by));
         }
      }

      if (!this.chat.expanded) {
         this.chat.drawsmall(g, new Coord(bx, by), 50);
      }
   }

   @Override
   public void tick(double dt) {
      super.tick(dt);
      if (!this.afk && System.currentTimeMillis() - this.ui.lastevent > 300000L) {
         this.afk = true;
         this.wdgmsg("afk", new Object[0]);
      } else if (this.afk && System.currentTimeMillis() - this.ui.lastevent < 300000L) {
         this.afk = false;
      }

      this.dwalkupd();
   }

   @Override
   public void uimsg(String msg, Object... args) {
      if (msg == "err") {
         String err = (String)args[0];
         this.error(err);
      } else if (msg == "prog") {
         if (args.length > 0) {
            this.prog = (Integer)args[0];
         } else {
            this.prog = -1;
         }
      } else if (msg == "setbelt") {
         int slot = (Integer)args[0];
         if (args.length < 2) {
            this.belt[slot] = null;
         } else {
            this.belt[slot] = this.ui.sess.getres((Integer)args[1]);
         }
      } else if (msg == "ins") {
         this.tm.updinsanity((Integer)args[0]);
      } else if (msg == "stm") {
         int[] n = new int[4];

         for (int i = 0; i < 4; i++) {
            n[i] = (Integer)args[i];
         }

         this.tm.upds(n);
      } else if (msg == "htm") {
         int[] n = new int[4];

         for (int i = 0; i < 4; i++) {
            n[i] = (Integer)args[i];
         }

         this.tm.updh(n);
      } else if (msg == "gavail") {
         this.tm.gavail = (Integer)args[0] != 0;
      } else if (msg == "cravail") {
         if (args[0] == null) {
            this.tm.cravail(null);
         } else {
            this.tm.cravail(this.ui.sess.getres((Integer)args[0]));
         }
      } else if (msg == "gobble") {
         boolean g = (Integer)args[0] != 0;
         if (g && this.gobble == null) {
            boolean old = (Integer)args[1] == 0;
            this.tm.hide();
            this.gobble = (Widget)(old ? new OldGobble(Coord.z, this) : new Gobble(Coord.z, this));
            this.resize(this.sz);
         } else if (!g && this.gobble != null) {
            this.ui.destroy(this.gobble);
            this.gobble = null;
            this.tm.show();
         }
      } else if (Gobble.msgs.contains(msg)) {
         this.gobble.uimsg(msg, args);
      } else if (msg == "polowner") {
         String o = (String)args[0];
         boolean n = (Integer)args[1] != 0;
         if (o.length() == 0) {
            o = null;
         } else {
            o = o.intern();
         }

         if (o != this.polowner) {
            if (this.map != null) {
               if (o == null) {
                  if (this.polowner != null) {
                     this.map.setpoltext("Leaving " + this.polowner);
                  }
               } else {
                  this.map.setpoltext("Entering " + o);
               }
            }

            this.polowner = o;
         }
      } else if (msg == "dblk") {
         int id = (Integer)args[0];
         this.dblk = id < 0 ? null : this.ui.sess.getres(id);
      } else if (msg == "lblk") {
         int id = (Integer)args[0];
         this.lblk = id < 0 ? null : this.ui.sess.getres(id);
      } else if (msg == "showhelp") {
         Indir<Resource> res = this.ui.sess.getres((Integer)args[0]);
         if (this.help == null) {
            this.help = new HelpWnd(this.sz.div(2).sub(150, 200), this, res);
         } else {
            this.help.res = res;
         }
      } else if (msg == "weight") {
         this.weight = (Integer)args[0];
         if (this.invwnd != null) {
            this.invwnd.updweight();
         }
      } else {
         super.uimsg(msg, args);
      }
   }

   @Override
   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (sender == this.menu) {
         this.wdgmsg(msg, args);
      } else {
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
   }

   private void fitwdg(Widget wdg) {
      if (wdg.c.x < 0) {
         wdg.c.x = 0;
      }

      if (wdg.c.y < 0) {
         wdg.c.y = 0;
      }

      if (wdg.c.x + wdg.sz.x > this.sz.x) {
         wdg.c.x = this.sz.x - wdg.sz.x;
      }

      if (wdg.c.y + wdg.sz.y > this.sz.y) {
         wdg.c.y = this.sz.y - wdg.sz.y;
      }
   }

   private void dwalkupd() {
      Coord a = new Coord();
      if (this.dkeys[0]) {
         a = a.add(1, 0);
      }

      if (this.dkeys[1]) {
         a = a.add(0, 1);
      }

      if (this.dkeys[2]) {
         a = a.add(-1, 0);
      }

      if (this.dkeys[3]) {
         a = a.add(0, -1);
      }

      long now = System.currentTimeMillis();
      if (!a.equals(this.dwalkang) && now > this.dwalkhys) {
         if (a.x == 0 && a.y == 0) {
            this.wdgmsg("dwalk", new Object[0]);
         } else {
            float da = this.dwalkbase + (float)a.angle(Coord.z);
            this.wdgmsg("dwalk", new Object[]{(int)(da / (Math.PI * 2) * 1000.0)});
         }

         this.dwalkang = a;
      }
   }

   private int dwalkkey(char key) {
      if (key == 'W') {
         return 0;
      } else if (key == 'D') {
         return 1;
      } else if (key == 'S') {
         return 2;
      } else if (key == 'A') {
         return 3;
      } else {
         throw new Error();
      }
   }

   private void dwalkdown(char key, KeyEvent ev) {
      if (!this.dwalking) {
         this.dwalking = true;
         this.dwalkbase = -this.map.camera.angle();
         this.ui.grabkeys(this);
      }

      int k = this.dwalkkey(key);
      this.dkeys[k] = true;
      this.dwalkhys = ev.getWhen();
   }

   private void dwalkup(char key, KeyEvent ev) {
      int k = this.dwalkkey(key);
      this.dkeys[k] = false;
      this.dwalkhys = ev.getWhen() + 100L;
      if (!this.dkeys[0] && !this.dkeys[1] && !this.dkeys[2] && !this.dkeys[3]) {
         this.dwalking = false;
         this.ui.grabkeys(null);
      }
   }

   private void makemenu() {
      this.mainmenu = new GameUI.MainMenu(new Coord(0, this.sz.y - menubg.sz().y), this);
      (new Widget(Coord.z, Inventory.isqsz.add(Window.swbox.bisz()), this) {
         private final Tex none = Resource.loadtex("gfx/hud/blknone");
         private Tex mono;
         private Indir<Resource> monores;

         {
            this.tooltip = Text.render("Toggle maneuver (Ctrl+S)");
         }

         @Override
         public void draw(GOut g) {
            try {
               if (GameUI.this.lblk != null) {
                  g.image(GameUI.this.lblk.get().layer(Resource.imgc).tex(), Window.swbox.btloff());
               } else if (GameUI.this.dblk != null) {
                  if (this.monores != GameUI.this.dblk) {
                     if (this.mono != null) {
                        this.mono.dispose();
                     }

                     this.mono = new TexI(PUtils.monochromize(GameUI.this.dblk.get().layer(Resource.imgc).img, new Color(128, 128, 128)));
                     this.monores = GameUI.this.dblk;
                  }

                  g.image(this.mono, Window.swbox.btloff());
               } else {
                  g.image(this.none, Window.swbox.btloff());
               }
            } catch (Loading var3) {
            }

            g.chcolor(133, 92, 62, 255);
            Window.swbox.draw(g, Coord.z, this.sz);
            g.chcolor();
         }

         @Override
         public void presize() {
            this.c = GameUI.this.menu.c.add(GameUI.this.menu.sz.x, 0).sub(this.sz);
         }

         @Override
         public boolean globtype(char key, KeyEvent ev) {
            if (key == 19) {
               GameUI.this.act("blk");
               return true;
            } else {
               return super.globtype(key, ev);
            }
         }

         @Override
         public boolean mousedown(Coord c, int btn) {
            GameUI.this.act("blk");
            return true;
         }
      }).presize();
      if (Config.storebase != null) {
         (new IButton(Coord.z, this, Resource.loadimg("gfx/hud/cashu"), Resource.loadimg("gfx/hud/cashd"), Resource.loadimg("gfx/hud/cashh")) {
            {
               this.tooltip = Text.render("Salem Store");
            }

            @Override
            public void click() {
               if (GameUI.this.storewnd == null) {
                  GameUI.this.storewnd = new Store(Coord.z, GameUI.this, Config.storebase);
                  GameUI.this.storewnd.c = GameUI.this.storewnd.parent.sz.sub(GameUI.this.storewnd.sz).div(2);
               } else {
                  this.ui.destroy(GameUI.this.storewnd);
                  GameUI.this.storewnd = null;
               }
            }

            @Override
            public void presize() {
               this.c = GameUI.this.mainmenu.c.sub(0, this.sz.y);
            }

            @Override
            public Object tooltip(Coord c, Widget prev) {
               return this.checkhit(c) ? super.tooltip(c, prev) : null;
            }
         }).presize();
      } else if (Config.storeurl != null && WebBrowser.self != null) {
         (new IButton(Coord.z, this, Resource.loadimg("gfx/hud/cashu"), Resource.loadimg("gfx/hud/cashd"), Resource.loadimg("gfx/hud/cashh")) {
               {
                  this.tooltip = Text.render("Salem Store");
               }

               private String encode(String in) {
                  StringBuilder buf = new StringBuilder();

                  byte[] enc;
                  try {
                     enc = in.getBytes("utf-8");
                  } catch (UnsupportedEncodingException var8) {
                     throw new Error(var8);
                  }

                  for (byte c : enc) {
                     if ((c < 97 || c > 122) && (c < 65 || c > 90) && (c < 48 || c > 57) && c != 46) {
                        buf.append("%" + Utils.num2hex((c & 240) >> 4) + Utils.num2hex(c & 15));
                     } else {
                        buf.append((char)c);
                     }
                  }

                  return buf.toString();
               }

               @Override
               public void click() {
                  URL base = Config.storeurl;

                  try {
                     WebBrowser.self
                        .show(new URL(base.getProtocol(), base.getHost(), base.getPort(), base.getFile() + "?userid=" + this.encode(this.ui.sess.username)));
                  } catch (MalformedURLException var3) {
                     throw new RuntimeException(var3);
                  } catch (WebBrowser.BrowserException var4) {
                     GameUI.this.error("Could not launch web browser.");
                  }
               }

               @Override
               public void presize() {
                  this.c = GameUI.this.mainmenu.c.sub(0, this.sz.y);
               }

               @Override
               public Object tooltip(Coord c, Widget prev) {
                  return this.checkhit(c) ? super.tooltip(c, prev) : null;
               }
            })
            .presize();
      }

      if (Config.manualurl != null && WebBrowser.self != null) {
         (new IButton(Coord.z, this, Resource.loadimg("gfx/hud/manu"), Resource.loadimg("gfx/hud/mand"), Resource.loadimg("gfx/hud/manh")) {
            {
               this.tooltip = Text.render("Go to Wiki!");
            }

            @Override
            public void click() {
               URL base = Config.manualurl;

               try {
                  WebBrowser.self.show(base);
               } catch (WebBrowser.BrowserException var3) {
                  GameUI.this.error("Could not launch web browser.");
               }
            }

            @Override
            public void presize() {
               this.c = GameUI.this.mainmenu.c.sub(0, this.sz.y).add(140, 0);
            }

            @Override
            public Object tooltip(Coord c, Widget prev) {
               return this.checkhit(c) ? super.tooltip(c, prev) : null;
            }
         }).presize();
      }
   }

   @Override
   public boolean globtype(char key, KeyEvent ev) {
      char ukey = Character.toUpperCase(key);
      if (key == ':') {
         this.entercmd();
         return true;
      } else if (Config.screenurl != null && ukey == 'S' && (ev.getModifiersEx() & 768) != 0) {
         Screenshooter.take(this, Config.screenurl);
         return true;
      } else if (ukey != 'W' && ukey != 'A' && ukey != 'S' && ukey != 'D') {
         return super.globtype(key, ev);
      } else {
         this.dwalkdown(ukey, ev);
         return true;
      }
   }

   @Override
   public boolean keydown(KeyEvent ev) {
      char ukey = Character.toUpperCase(ev.getKeyChar());
      if (!this.dwalking || ukey != 'W' && ukey != 'A' && ukey != 'S' && ukey != 'D') {
         return super.keydown(ev);
      } else {
         this.dwalkdown(ukey, ev);
         return true;
      }
   }

   @Override
   public boolean keyup(KeyEvent ev) {
      char ukey = Character.toUpperCase(ev.getKeyChar());
      if (!this.dwalking || ukey != 'W' && ukey != 'A' && ukey != 'S' && ukey != 'D') {
         return super.keyup(ev);
      } else {
         this.dwalkup(ukey, ev);
         return true;
      }
   }

   @Override
   public boolean mousedown(Coord c, int button) {
      return super.mousedown(c, button);
   }

   @Override
   public void resize(Coord sz) {
      this.sz = sz;
      this.menu.c = sz.sub(this.menu.sz);
      this.tm.c = new Coord((sz.x - this.tm.sz.x) / 2, 0);
      this.chat.move(new Coord(this.mainmenu.sz.x, sz.y));
      this.chat.resize(sz.x - this.chat.c.x - this.menu.sz.x);
      if (this.gobble != null) {
         this.gobble.c = new Coord((sz.x - this.gobble.sz.x) / 2, 0);
      }

      if (this.map != null) {
         this.map.resize(sz);
      }

      if (this.fv != null) {
         this.fv.c = new Coord(sz.x - Fightview.width, 0);
      }

      this.mainmenu.c = new Coord(0, sz.y - this.mainmenu.sz.y);
      this.beltwdg.c = new Coord(this.mainmenu.sz.x + 10, sz.y - this.beltwdg.sz.y);
      super.resize(sz);
   }

   @Override
   public void presize() {
      this.resize(this.parent.sz);
   }

   @Override
   public void error(String msg) {
      this.message(msg, GameUI.MsgType.ERROR);
   }

   public void message(String msg, GameUI.MsgType type) {
      this.message(msg, getMsgColor(type));
   }

   public void message(String msg, Color msgColor) {
      this.errtime = System.currentTimeMillis();
      this.lasterr = errfoundry.render(msg, msgColor);
      this.syslog.append(msg, msgColor);
      Audio.play(errsfx);
   }

   public static Color getMsgColor(GameUI.MsgType type) {
      switch (type) {
         case INFO:
            return Color.CYAN;
         case GOOD:
            return Color.GREEN;
         case BAD:
            return Color.RED;
         case ERROR:
            return Color.RED;
         default:
            return Color.WHITE;
      }
   }

   public void act(String... args) {
      this.wdgmsg("act", args);
   }

   public void act(int mods, Coord mc, Gob gob, String... args) {
      int n = args.length;
      Object[] al = new Object[n];
      System.arraycopy(args, 0, al, 0, n);
      if (mc != null) {
         al = Utils.extend(al, al.length + 2);
         al[n++] = mods;
         al[n++] = mc;
         if (gob != null) {
            al = Utils.extend(al, al.length + 2);
            al[n++] = (int)gob.id;
            al[n++] = gob.rc;
         }
      }

      this.wdgmsg("act", al);
   }

   @Override
   public Map<String, Console.Command> findcmds() {
      return this.cmdmap;
   }

   @Widget.RName("gameui")
   public static class $_ implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         String chrid = (String)args[0];
         int plid = (Integer)args[1];
         return new GameUI(parent, chrid, plid);
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
               MapView map = GameUI.this.map;
               map.delay(map.new Hittest(mvc) {
                  @Override
                  protected void hit(Coord pc, Coord mc, MapView.ClickInfo inf) {
                     if (inf == null) {
                        GameUI.this.wdgmsg("belt", new Object[]{slot, 1, Belt.this.ui.modflags(), mc});
                     } else {
                        GameUI.this.wdgmsg("belt", new Object[]{slot, 1, Belt.this.ui.modflags(), mc, (int)inf.gob.id, inf.gob.rc});
                     }
                  }

                  @Override
                  protected void nohit(Coord pc) {
                     GameUI.this.wdgmsg("belt", new Object[]{slot, 1, Belt.this.ui.modflags()});
                  }
               });
            }
         }
      }
   }

   public class FKeyBelt extends GameUI.Belt implements DTarget, DropTarget {
      public final int[] beltkeys = new int[]{112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123};
      public int curbelt = 0;

      public FKeyBelt(Coord c, Widget parent) {
         super(c, Inventory.invsz(new Coord(12, 1)), parent);
      }

      private Coord beltc(int i) {
         return Inventory.sqoff(new Coord(i, 0));
      }

      private int beltslot(Coord c) {
         for (int i = 0; i < 12; i++) {
            if (c.isect(this.beltc(i), Inventory.isqsz)) {
               return i + this.curbelt * 12;
            }
         }

         return -1;
      }

      @Override
      public void draw(GOut g) {
         Inventory.invsq(g, Coord.z, new Coord(12, 1));

         for (int i = 0; i < 12; i++) {
            int slot = i + this.curbelt * 12;
            Coord c = this.beltc(i);

            try {
               if (GameUI.this.belt[slot] != null) {
                  g.image(GameUI.this.belt[slot].get().layer(Resource.imgc).tex(), c);
               }
            } catch (Loading var6) {
            }

            g.chcolor(156, 180, 158, 255);
            FastText.aprintf(g, c.add(Inventory.isqsz), 1.0, 1.0, "F%d", i + 1);
            g.chcolor();
         }
      }

      @Override
      public boolean mousedown(Coord c, int button) {
         int slot = this.beltslot(c);
         if (slot != -1) {
            if (button == 1) {
               GameUI.this.wdgmsg("belt", new Object[]{slot, 1, this.ui.modflags()});
            }

            if (button == 3) {
               GameUI.this.wdgmsg("setbelt", new Object[]{slot, 1});
            }

            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean globtype(char key, KeyEvent ev) {
         if (key != 0) {
            return false;
         } else {
            boolean M = (ev.getModifiersEx() & 768) != 0;

            for (int i = 0; i < this.beltkeys.length; i++) {
               if (ev.getKeyCode() == this.beltkeys[i]) {
                  if (M) {
                     this.curbelt = i;
                     return true;
                  }

                  this.keyact(i + this.curbelt * 12);
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      public boolean drop(Coord c, Coord ul) {
         int slot = this.beltslot(c);
         if (slot != -1) {
            GameUI.this.wdgmsg("setbelt", new Object[]{slot, 0});
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean iteminteract(Coord c, Coord ul) {
         return false;
      }

      @Override
      public boolean dropthing(Coord c, Object thing) {
         int slot = this.beltslot(c);
         if (slot != -1 && thing instanceof Resource) {
            Resource res = (Resource)thing;
            if (res.layer(Resource.action) != null) {
               GameUI.this.wdgmsg("setbelt", new Object[]{slot, res.name});
               return true;
            }
         }

         return false;
      }
   }

   public static class Hidewnd extends Window {
      public Hidewnd(Coord c, Coord sz, Widget parent, String cap) {
         super(c, sz, parent, cap);
      }

      @Override
      public void wdgmsg(Widget sender, String msg, Object... args) {
         if (sender == this && msg.equals("close")) {
            this.hide();
         } else {
            super.wdgmsg(sender, msg, args);
         }
      }
   }

   public static class InvWindow extends GameUI.Hidewnd {
      public final Map<Inventory, String> names = new HashMap<>();
      private Label[] labels = new Label[0];
      public final GameUI wui;
      public final Label wlbl;

      public InvWindow(Coord c, Coord sz, Widget parent, String cap, GameUI wui) {
         super(c, sz, parent, cap);
         if ((this.wui = wui) != null) {
            this.wlbl = new Label(Coord.z, this, "");
            this.updweight();
         } else {
            this.wlbl = null;
         }
      }

      private void updweight() {
         int weight = this.wui.weight;
         int cap = 25000;
         Glob.CAttr ca = this.ui.sess.glob.cattr.get("carry");
         if (ca != null) {
            cap = ca.comp;
         }

         this.wlbl.settext(String.format("Carrying %.2f/%.2f kg", weight / 1000.0, cap / 1000.0));
         this.wlbl.setcolor(weight > cap ? Color.RED : Color.WHITE);
      }

      private void repack() {
         for (Label lbl : this.labels) {
            if (lbl != null) {
               lbl.destroy();
            }
         }

         int mw = 0;

         for (Inventory inv : this.names.keySet()) {
            mw = Math.max(mw, inv.sz.x);
         }

         List<String> cn = new ArrayList<>();

         for (String nm : this.names.values()) {
            if (!cn.contains(nm)) {
               cn.add(nm);
            }
         }

         Collections.sort(cn);
         Label[] nl = new Label[cn.size()];
         int n = 0;
         int y = 0;

         for (String nmx : cn) {
            if (!nmx.equals("")) {
               nl[n] = new Label(new Coord(0, y), this, nmx);
               y = nl[n].c.y + nl[n].sz.y + 5;
            }

            int x = 0;
            int mh = 0;

            for (Entry<Inventory, String> e : this.names.entrySet()) {
               if (e.getValue().equals(nmx)) {
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

         if (this.wlbl != null) {
            this.wlbl.c = new Coord(0, y);
         }

         this.labels = nl;
         this.pack();
      }

      @Override
      public Widget makechild(String type, Object[] pargs, Object[] cargs) {
         String nm;
         if (pargs.length > 0) {
            nm = (String)pargs[0];
         } else {
            nm = "";
         }

         Inventory inv = (Inventory)gettype(type).create(Coord.z, this, cargs);
         this.names.put(inv, nm);
         this.repack();
         return inv;
      }

      @Override
      public void cdestroy(Widget w) {
         if (w instanceof Inventory && this.names.containsKey(w)) {
            Inventory inv = (Inventory)w;
            this.names.remove(inv);
            this.repack();
         }
      }

      @Override
      public void cresize(Widget w) {
         if (w instanceof Inventory && this.names.containsKey(w)) {
            this.repack();
         }
      }

      @Widget.RName("invwnd")
      public static class $_ implements Widget.Factory {
         @Override
         public Widget create(Coord c, Widget parent, Object[] args) {
            String cap = (String)args[0];
            return new GameUI.InvWindow(c, new Coord(100, 100), parent, cap, null);
         }
      }
   }

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
      public boolean pv = true;

      public MainMenu(Coord c, Widget parent) {
         super(c, GameUI.menubg.sz(), parent);
         new Img(Coord.z, GameUI.menubg, this);
         new SeasonImg(new Coord(6, 8), new Coord(146, 146), this);
         this.invb = new GameUI.MenuButton(new Coord(161, 8), this, "inv", 9, "Inventory (Tab)") {
            int seq = 0;

            @Override
            public void click() {
               if (GameUI.this.invwnd != null && GameUI.this.invwnd.show(!GameUI.this.invwnd.visible)) {
                  GameUI.this.invwnd.raise();
                  GameUI.this.fitwdg(GameUI.this.invwnd);
               }
            }

            @Override
            public void tick(double dt) {
               if (GameUI.this.maininv != null) {
                  if (GameUI.this.invwnd.visible) {
                     this.seq = GameUI.this.maininv.newseq;
                     this.flash(false);
                  } else if (GameUI.this.maininv.newseq != this.seq) {
                     this.flash(true);
                  }
               }
            }
         };
         this.equb = new GameUI.MenuButton(new Coord(161, 66), this, "equ", 5, "Equipment (Ctrl+E)") {
            @Override
            public void click() {
               if (GameUI.this.equwnd != null && GameUI.this.equwnd.show(!GameUI.this.equwnd.visible)) {
                  GameUI.this.equwnd.raise();
                  GameUI.this.fitwdg(GameUI.this.equwnd);
               }
            }
         };
         this.chrb = new GameUI.MenuButton(new Coord(161, 124), this, "chr", 20, "Studying (Ctrl+T)") {
            @Override
            public void click() {
               GameUI.this.togglecw();
            }

            @Override
            public void tick(double dt) {
               if (GameUI.this.chrwdg != null && GameUI.this.chrwdg.skavail) {
                  this.flash(true);
               } else {
                  this.flash(false);
               }
            }
         };
         this.budb = new GameUI.MenuButton(new Coord(219, 8), this, "bud", 2, "Buddy List (Ctrl+B)") {
            @Override
            public void click() {
               if (GameUI.this.buddies != null && GameUI.this.buddies.show(!GameUI.this.buddies.visible)) {
                  GameUI.this.buddies.raise();
                  GameUI.this.fitwdg(GameUI.this.buddies);
                  this.setfocus(GameUI.this.buddies);
               }
            }
         };
         this.polb = new GameUI.MenuButton(new Coord(219, 66), this, "pol", 16, "Town (Ctrl+P)") {
            final Tex gray = Resource.loadtex("gfx/hud/polgray");

            @Override
            public void draw(GOut g) {
               if (GameUI.this.polity == null) {
                  g.image(this.gray, Coord.z);
               } else {
                  super.draw(g);
               }
            }

            @Override
            public void click() {
               if (GameUI.this.polity != null && GameUI.this.polity.show(!GameUI.this.polity.visible)) {
                  GameUI.this.polity.raise();
                  GameUI.this.fitwdg(GameUI.this.polity);
                  this.setfocus(GameUI.this.polity);
               }
            }
         };
         this.optb = new GameUI.MenuButton(new Coord(219, 124), this, "opt", 15, "Options") {
            @Override
            public void click() {
               if (GameUI.this.opts.show(!GameUI.this.opts.visible)) {
                  GameUI.this.opts.raise();
                  GameUI.this.fitwdg(GameUI.this.opts);
                  this.setfocus(GameUI.this.opts);
               }
            }
         };
         this.clab = new GameUI.MenuButton(new Coord(6, 160), this, "cla", -1, "Display personal claims") {
            @Override
            public void click() {
               if (!GameUI.this.map.visol(0)) {
                  GameUI.this.map.enol(0, 1);
               } else {
                  GameUI.this.map.disol(0, 1);
               }
            }
         };
         this.towb = new GameUI.MenuButton(new Coord(24, 160), this, "tow", -1, "Display town claims") {
            @Override
            public void click() {
               if (!GameUI.this.map.visol(2)) {
                  GameUI.this.map.enol(2, 3);
               } else {
                  GameUI.this.map.disol(2, 3);
               }
            }
         };
         this.warb = new GameUI.MenuButton(new Coord(42, 160), this, "war", -1, "Display waste claims") {
            @Override
            public void click() {
               if (!GameUI.this.map.visol(4)) {
                  GameUI.this.map.enol(4);
               } else {
                  GameUI.this.map.disol(4);
               }
            }
         };
         this.ptrb = new GameUI.MenuButton(new Coord(60, 160), this, "ptr", -1, "Display homestead pointer") {
            @Override
            public void click() {
               MainMenu.this.pv = !MainMenu.this.pv;
            }
         };
         this.lndb = new GameUI.MenuButton(new Coord(78, 160), this, "lnd", 12, "Display Landscape Tool") {
            @Override
            public void click() {
               FlatnessTool.instance(GameUI.this).toggle();
            }
         };
         this.chatb = new GameUI.MenuButton(new Coord(100, 160), this, "chat", 3, "Chat (Ctrl+C)") {
            @Override
            public void click() {
               GameUI.this.chat.toggle();
            }
         };
      }
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

      @Override
      public void click() {
      }

      @Override
      public boolean globtype(char key, KeyEvent ev) {
         if (this.gkey != -1 && key == this.gkey) {
            this.click();
            return true;
         } else {
            return super.globtype(key, ev);
         }
      }

      @Override
      public void draw(GOut g) {
         super.draw(g);
         if (this.flash > 0L) {
            if (this.glowmask == null) {
               this.glowmask = new TexI(PUtils.glowmask(PUtils.glowmask(this.up.getRaster()), 10, new Color(192, 255, 64)));
            }

            g = g.reclipl(new Coord(-10, -10), g.sz.add(20, 20));
            double ph = (System.currentTimeMillis() - this.flash) / 1000.0;
            g.chcolor(255, 255, 255, (int)(128.0 * (Math.cos(ph * Math.PI * 2.0) * -0.5 + 0.5)));
            g.image(this.glowmask, Coord.z);
            g.chcolor();
         }
      }

      public void flash(boolean f) {
         if (f) {
            if (this.flash == 0L) {
               this.flash = System.currentTimeMillis();
            }
         } else {
            this.flash = 0L;
         }
      }
   }

   public class NKeyBelt extends GameUI.Belt implements DTarget, DropTarget {
      public int curbelt = 0;

      public NKeyBelt(Coord c, Widget parent) {
         super(c, Inventory.invsz(new Coord(10, 1)), parent);
      }

      private Coord beltc(int i) {
         return Inventory.sqoff(new Coord(i, 0));
      }

      private int beltslot(Coord c) {
         for (int i = 0; i < 10; i++) {
            if (c.isect(this.beltc(i), Inventory.isqsz)) {
               return i + this.curbelt * 12;
            }
         }

         return -1;
      }

      @Override
      public void draw(GOut g) {
         Inventory.invsq(g, Coord.z, new Coord(10, 1));

         for (int i = 0; i < 10; i++) {
            int slot = i + this.curbelt * 12;
            Coord c = this.beltc(i);

            try {
               if (GameUI.this.belt[slot] != null) {
                  g.image(GameUI.this.belt[slot].get().layer(Resource.imgc).tex(), c);
               }
            } catch (Loading var6) {
            }

            g.chcolor(156, 180, 158, 255);
            FastText.aprintf(g, c.add(Inventory.isqsz), 1.0, 1.0, "%d", (i + 1) % 10);
            g.chcolor();
         }
      }

      @Override
      public boolean mousedown(Coord c, int button) {
         int slot = this.beltslot(c);
         if (slot != -1) {
            if (button == 1) {
               GameUI.this.wdgmsg("belt", new Object[]{slot, 1, this.ui.modflags()});
            }

            if (button == 3) {
               GameUI.this.wdgmsg("setbelt", new Object[]{slot, 1});
            }

            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean globtype(char key, KeyEvent ev) {
         if (key != 0) {
            return false;
         } else {
            int c = ev.getKeyChar();
            if (c >= 48 && c <= 57) {
               int i = Utils.floormod(c - 48 - 1, 10);
               boolean M = (ev.getModifiersEx() & 768) != 0;
               if (M) {
                  this.curbelt = i;
               } else {
                  this.keyact(i + this.curbelt * 12);
               }

               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      public boolean drop(Coord c, Coord ul) {
         int slot = this.beltslot(c);
         if (slot != -1) {
            GameUI.this.wdgmsg("setbelt", new Object[]{slot, 0});
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean iteminteract(Coord c, Coord ul) {
         return false;
      }

      @Override
      public boolean dropthing(Coord c, Object thing) {
         int slot = this.beltslot(c);
         if (slot != -1 && thing instanceof Resource) {
            Resource res = (Resource)thing;
            if (res.layer(Resource.action) != null) {
               GameUI.this.wdgmsg("setbelt", new Object[]{slot, res.name});
               return true;
            }
         }

         return false;
      }
   }

   public static enum MsgType {
      INFO,
      GOOD,
      BAD,
      ERROR;
   }
}
