package haven;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import org.latikai.bots.BotManager;

public class UI {
   public static UI instance;
   public GameUI gui;
   public RootWidget root;
   protected Widget keygrab;
   protected Widget mousegrab;
   public Map<Integer, Widget> widgets = new TreeMap<>();
   public Map<Widget, Integer> rwidgets = new HashMap<>();
   public UI.Receiver rcvr;
   public BotManager bmgr;
   public Coord mc = Coord.z;
   public Coord lcc = Coord.z;
   public Session sess;
   public boolean modshift;
   public boolean modctrl;
   public boolean modmeta;
   public boolean modsuper;
   public Object lasttip;
   public long lastevent;
   public long lasttick;
   public Widget mouseon;
   public Console cons = new UI.WidgetConsole();
   protected Collection<UI.AfterDraw> afterdraws = new LinkedList<>();
   protected Collection<UI.AfterDraw> afterttdraws = new LinkedList<>();
   public MenuGrid mnu;
   public final ActAudio audio = new ActAudio();
   public int fps_goal = 50;
   protected long lastactivity = 0L;
   protected int kcode;

   public UI(Coord sz, Session sess) {
      this.lastevent = this.lasttick = System.currentTimeMillis();
      this.kcode = 0;
      instance = this;
      this.root = new RootWidget(this, sz);
      this.widgets.put(0, this.root);
      this.rwidgets.put(this.root, 0);
      this.sess = sess;
   }

   public void setreceiver(UI.Receiver rcvr) {
      this.rcvr = rcvr;
   }

   public void setbotmanager(BotManager bmgr) {
      this.bmgr = bmgr;
   }

   public void bind(Widget w, int id) {
      this.widgets.put(id, w);
      this.rwidgets.put(w, id);
   }

   public void drawafter(UI.AfterDraw ad) {
      synchronized (this.afterdraws) {
         this.afterdraws.add(ad);
      }
   }

   public void drawaftertt(UI.AfterDraw ad) {
      synchronized (this.afterttdraws) {
         this.afterttdraws.add(ad);
      }
   }

   public void lastdraw(GOut g) {
      synchronized (this.afterttdraws) {
         for (UI.AfterDraw ad : this.afterttdraws) {
            ad.draw(g);
         }

         this.afterttdraws.clear();
      }
   }

   public void tick() {
      long now = System.currentTimeMillis();
      this.root.tick((now - this.lasttick) / 1000.0);
      this.lasttick = now;
   }

   public void draw(GOut g) {
      this.root.draw(g);
      synchronized (this.afterdraws) {
         for (UI.AfterDraw ad : this.afterdraws) {
            ad.draw(g);
         }

         this.afterdraws.clear();
      }
   }

   public void newwidget(int id, String type, int parent, Object[] pargs, Object... cargs) throws InterruptedException {
      synchronized (this) {
         Widget pwdg = this.widgets.get(parent);
         if (pwdg == null) {
            throw new UI.UIException("Null parent widget " + parent + " for " + id, type, cargs);
         } else {
            Widget wdg = pwdg.makechild(type.intern(), pargs, cargs);
            this.bind(wdg, id);
            if (type.equals("gameui")) {
               Gob.fruittrees = 0;
               Gob.thornbushes = 0;
               if (Config.alwaystrack) {
                  String[] as = new String[]{"tracking"};
                  this.wdgmsg(wdg, "act", as);
               }

               String[] as = new String[]{"pot"};
               this.bmgr.startBot(as);
               as = new String[]{"bucket"};
               this.bmgr.startBot(as);
            }
         }
      }
   }

   public void grabmouse(Widget wdg) {
      this.mousegrab = wdg;
   }

   public void grabkeys(Widget wdg) {
      this.keygrab = wdg;
   }

   protected void removeid(Widget wdg) {
      if (this.rwidgets.containsKey(wdg)) {
         int id = this.rwidgets.get(wdg);
         this.widgets.remove(id);
         this.rwidgets.remove(wdg);
      }

      for (Widget child = wdg.child; child != null; child = child.next) {
         this.removeid(child);
      }
   }

   public void destroy(Widget wdg) {
      if (this.mousegrab != null && this.mousegrab.hasparent(wdg)) {
         this.mousegrab = null;
      }

      if (this.keygrab != null && this.keygrab.hasparent(wdg)) {
         this.keygrab = null;
      }

      this.removeid(wdg);
      wdg.reqdestroy();
   }

   public void destroy(int id) {
      synchronized (this) {
         if (this.widgets.containsKey(id)) {
            Widget wdg = this.widgets.get(id);
            this.destroy(wdg);
            if (wdg == this.gui) {
               this.sess.glob.purge();
               this.gui = null;
               this.cons.clearout();
               this.mnu = null;
            }
         }
      }
   }

   public void wdgmsg(Widget sender, String msg, Object... args) {
      int id;
      synchronized (this) {
         if (!this.rwidgets.containsKey(sender)) {
            System.err.print("Wdgmsg sender (" + sender.getClass().getName() + ") is not in rwidgets");
            return;
         }

         id = this.rwidgets.get(sender);
      }

      if (this.rcvr != null) {
         this.rcvr.rcvmsg(id, msg, args);
      }
   }

   public void uimsg(int id, String msg, Object... args) {
      synchronized (this) {
         Widget wdg = this.widgets.get(id);
         if (wdg != null) {
            wdg.uimsg(msg.intern(), args);
         } else {
            throw new UI.UIException("Uimsg to non-existent widget " + id, msg, args);
         }
      }
   }

   protected void setmods(InputEvent ev) {
      int mod = ev.getModifiersEx();
      Debug.kf1 = this.modshift = (mod & 64) != 0;
      Debug.kf2 = this.modctrl = (mod & 128) != 0;
      Debug.kf3 = this.modmeta = (mod & 768) != 0;
   }

   public void type(KeyEvent ev) {
      this.be_active();
      this.setmods(ev);
      ev.setKeyCode(this.kcode);
      if (this.keygrab == null) {
         if (!this.root.type(ev.getKeyChar(), ev)) {
            this.root.globtype(ev.getKeyChar(), ev);
         }
      } else {
         this.keygrab.type(ev.getKeyChar(), ev);
      }
   }

   public void keydown(KeyEvent ev) {
      this.setmods(ev);
      this.kcode = ev.getKeyCode();
      if (this.keygrab == null) {
         if (!this.root.keydown(ev)) {
            this.root.globtype('\u0000', ev);
         }
      } else {
         this.keygrab.keydown(ev);
      }
   }

   public void keyup(KeyEvent ev) {
      this.setmods(ev);
      this.kcode = 0;
      if (this.keygrab == null) {
         this.root.keyup(ev);
      } else {
         this.keygrab.keyup(ev);
      }
   }

   protected Coord wdgxlate(Coord c, Widget wdg) {
      return c.add(wdg.c.inv()).add(wdg.parent.rootpos().inv());
   }

   public boolean dropthing(Widget w, Coord c, Object thing) {
      if (w instanceof DropTarget && ((DropTarget)w).dropthing(c, thing)) {
         return true;
      } else {
         for (Widget wdg = w.lchild; wdg != null; wdg = wdg.prev) {
            Coord cc = w.xlate(wdg.c, true);
            if (c.isect(cc, wdg.sz) && this.dropthing(wdg, c.add(cc.inv()), thing)) {
               return true;
            }
         }

         return false;
      }
   }

   public long timesinceactive() {
      return System.currentTimeMillis() - this.lastactivity;
   }

   public void be_active() {
      this.lastactivity = System.currentTimeMillis();
   }

   public void mousedown(MouseEvent ev, Coord c, int button) {
      this.be_active();
      this.setmods(ev);
      this.lcc = this.mc = c;
      if (this.mousegrab == null) {
         this.root.mousedown(c, button);
      } else {
         this.mousegrab.mousedown(this.wdgxlate(c, this.mousegrab), button);
      }
   }

   public void mouseup(MouseEvent ev, Coord c, int button) {
      this.setmods(ev);
      this.mc = c;
      if (this.mousegrab == null) {
         this.root.mouseup(c, button);
      } else {
         this.mousegrab.mouseup(this.wdgxlate(c, this.mousegrab), button);
      }
   }

   public void mousemove(MouseEvent ev, Coord c) {
      this.setmods(ev);
      this.mc = c;
      if (this.mousegrab == null) {
         this.root.mousemove(c);
      } else {
         this.mousegrab.mousemove(this.wdgxlate(c, this.mousegrab));
      }
   }

   public void mousewheel(MouseEvent ev, Coord c, int amount) {
      this.setmods(ev);
      this.lcc = this.mc = c;
      if (this.mousegrab == null) {
         this.root.mousewheel(c, amount);
      } else {
         this.mousegrab.mousewheel(this.wdgxlate(c, this.mousegrab), amount);
      }
   }

   public int modflags() {
      return (this.modshift ? 1 : 0) | (this.modctrl ? 2 : 0) | (this.modmeta ? 4 : 0) | (this.modsuper ? 8 : 0);
   }

   public int get_fps_wait_time() {
      return 1000 / this.fps_goal;
   }

   public void message(String str, GameUI.MsgType type) {
      if (this.cons != null && this.gui != null) {
         this.gui.message(str, type);
      }
   }

   public void message(String str, Color msgColor) {
      if (this.cons != null && this.gui != null) {
         this.gui.message(str, msgColor);
      }
   }

   public static boolean isCursor(String name) {
      return instance != null && instance.root != null && instance.root.cursor.name.equals(name);
   }

   public void destroy() {
      this.audio.clear();
   }

   public interface AfterDraw {
      void draw(GOut var1);
   }

   public static class Cursor {
      public static final String SIFTING = "gfx/hud/curs/sft";
      public static final String GOBBLE = "gfx/hud/curs/eat";
   }

   public interface Receiver {
      void rcvmsg(int var1, String var2, Object... var3);
   }

   public interface Runner {
      Session run(UI var1) throws InterruptedException;
   }

   public static class UIException extends RuntimeException {
      public String mname;
      public Object[] args;

      public UIException(String message, String mname, Object... args) {
         super(message);
         this.mname = mname;
         this.args = args;
      }
   }

   protected class WidgetConsole extends Console {
      protected WidgetConsole() {
         this.setcmd("q", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               HackThread.tg().interrupt();
            }
         });
         this.setcmd("lo", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               UI.this.sess.close();
            }
         });
         this.setcmd("listchars", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               for (Widget c : UI.this.widgets.values()) {
                  if (Charlist.class.isInstance(c)) {
                     for (Charlist.Char ch : ((Charlist)c).chars) {
                        System.out.println("\t" + ch.name);
                     }
                  }
               }
            }
         });
         this.setcmd("playchar", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               if (args.length < 2) {
                  System.out.println("\tPlease specify a character name!");
               } else {
                  String name = args[1];

                  for (int i = 2; i < args.length; i++) {
                     name = name + " " + args[i];
                  }

                  for (Widget c : UI.this.widgets.values()) {
                     if (Charlist.class.isInstance(c)) {
                        System.out.println("\tLogging in character " + name);
                        c.wdgmsg("play", name);
                     }
                  }
               }
            }
         });
         this.setcmd("reporttrees", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               System.out.println("\tThere are " + Gob.fruittrees + " fruit trees blossoming.");
               System.out.println("\tThere are " + Gob.thornbushes + " thornbushes with flowers.");
            }
         });
         this.setcmd("reportseason", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               String name = "unknown";
               switch (UI.this.sess.glob.season) {
                  case 0:
                     name = "coldsnap";
                     break;
                  case 1:
                     name = "everbloom";
                     break;
                  case 2:
                     name = "bloodmoon";
               }

               System.out.println("The current season is " + name);
            }
         });
         this.setcmd("toggle3d", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               UI.this.gui.map.toggle_rendering();
            }
         });
         this.setcmd("guiact", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               String[] trimmed = new String[args.length - 1];

               for (int i = 0; i < trimmed.length; i++) {
                  trimmed[i] = args[i + 1];
               }

               UI.this.gui.act(trimmed);
            }
         });
         this.setcmd("startbot", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               UI.this.bmgr.startBot(args);
            }
         });
         this.setcmd("stopbot", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               UI.this.bmgr.stopBot(args);
            }
         });
         this.setcmd("getwitchery", new Console.Command() {
            @Override
            public void run(Console cons, String[] args) {
               CharWnd cw = null;

               for (Widget w : UI.this.rwidgets.keySet()) {
                  if (w.getClass().equals(CharWnd.class)) {
                     cw = (CharWnd)w;
                  }
               }

               if (cw != null) {
                  cw.wdgmsg("buy", new Object[]{"sympatheticmagic"});
               }
            }
         });
      }

      protected void findcmds(Map<String, Console.Command> map, Widget wdg) {
         if (wdg instanceof Console.Directory) {
            Map<String, Console.Command> cmds = ((Console.Directory)wdg).findcmds();
            synchronized (cmds) {
               map.putAll(cmds);
            }
         }

         for (Widget ch = wdg.child; ch != null; ch = ch.next) {
            this.findcmds(map, ch);
         }
      }

      @Override
      public Map<String, Console.Command> findcmds() {
         Map<String, Console.Command> ret = super.findcmds();
         this.findcmds(ret, UI.this.root);
         return ret;
      }
   }
}
