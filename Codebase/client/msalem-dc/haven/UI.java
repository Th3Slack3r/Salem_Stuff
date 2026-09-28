package haven;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class UI {
   public RootWidget root;
   private Widget keygrab;
   private Widget mousegrab;
   public Map<Integer, Widget> widgets = new TreeMap<>();
   public Map<Widget, Integer> rwidgets = new HashMap<>();
   UI.Receiver rcvr;
   public Coord mc = Coord.z;
   public Coord lcc = Coord.z;
   public Session sess;
   public boolean modshift;
   public boolean modctrl;
   public boolean modmeta;
   public boolean modsuper;
   public Object lasttip;
   long lastevent;
   long lasttick;
   public Widget mouseon;
   public Console cons = new UI.WidgetConsole();
   private Collection<UI.AfterDraw> afterdraws = new LinkedList<>();
   public final ActAudio audio = new ActAudio();

   public UI(Coord sz, Session sess) {
      this.lastevent = this.lasttick = System.currentTimeMillis();
      this.root = new RootWidget(this, sz);
      this.widgets.put(0, this.root);
      this.rwidgets.put(this.root, 0);
      this.sess = sess;
   }

   public void setreceiver(UI.Receiver rcvr) {
      this.rcvr = rcvr;
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
         }
      }
   }

   public void grabmouse(Widget wdg) {
      this.mousegrab = wdg;
   }

   public void grabkeys(Widget wdg) {
      this.keygrab = wdg;
   }

   private void removeid(Widget wdg) {
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
         }
      }
   }

   public void wdgmsg(Widget sender, String msg, Object... args) {
      int id;
      synchronized (this) {
         if (!this.rwidgets.containsKey(sender)) {
            throw new UI.UIException("Wdgmsg sender (" + sender.getClass().getName() + ") is not in rwidgets", msg, args);
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

   private void setmods(InputEvent ev) {
      int mod = ev.getModifiersEx();
      Debug.kf1 = this.modshift = (mod & 64) != 0;
      Debug.kf2 = this.modctrl = (mod & 128) != 0;
      Debug.kf3 = this.modmeta = (mod & 768) != 0;
   }

   public void type(KeyEvent ev) {
      this.setmods(ev);
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
      if (this.keygrab == null) {
         this.root.keyup(ev);
      } else {
         this.keygrab.keyup(ev);
      }
   }

   private Coord wdgxlate(Coord c, Widget wdg) {
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

   public void mousedown(MouseEvent ev, Coord c, int button) {
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

   public void destroy() {
      this.audio.clear();
   }

   public interface AfterDraw {
      void draw(GOut var1);
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

   private class WidgetConsole extends Console {
      private WidgetConsole() {
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
      }

      private void findcmds(Map<String, Console.Command> map, Widget wdg) {
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
