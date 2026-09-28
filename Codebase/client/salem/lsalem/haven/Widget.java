package haven;

import dolda.jglob.Discoverable;
import dolda.jglob.Loader;
import java.awt.event.KeyEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

public class Widget {
   public UI ui;
   public Coord c;
   public Coord sz;
   public Widget next;
   public Widget prev;
   public Widget child;
   public Widget lchild;
   public Widget parent;
   public boolean focustab = false;
   public boolean focusctl = false;
   public boolean hasfocus = false;
   public boolean visible = true;
   private boolean canfocus = false;
   private boolean autofocus = false;
   public boolean canactivate = false;
   public boolean cancancel = false;
   public Widget focused;
   public Resource cursor = null;
   public Object tooltip = null;
   private Widget prevtt;
   public final Collection<Widget.Anim> anims = new LinkedList<>();
   static Map<String, Widget.Factory> types = new TreeMap<>();
   private static boolean inited = false;

   public static void initnames() {
      if (!inited) {
         for (Widget.Factory f : Loader.get(Widget.RName.class).instances(Widget.Factory.class)) {
            synchronized (types) {
               types.put(f.getClass().getAnnotation(Widget.RName.class).value(), f);
            }
         }

         inited = true;
      }
   }

   public static Widget.Factory gettype2(String name) throws InterruptedException {
      if (name.indexOf(47) < 0) {
         synchronized (types) {
            return types.get(name);
         }
      } else {
         int ver = -1;
         int p;
         if ((p = name.indexOf(58)) > 0) {
            ver = Integer.parseInt(name.substring(p + 1));
            name = name.substring(0, p);
         }

         Resource res = Resource.load(name, ver);

         while (true) {
            try {
               return res.getcode(Widget.Factory.class, true);
            } catch (Resource.Loading var6) {
               var6.res.loadwaitint();
            }
         }
      }
   }

   public static Widget.Factory gettype(String name) {
      long start = System.currentTimeMillis();

      Widget.Factory f;
      try {
         f = gettype2(name);
      } catch (InterruptedException var5) {
         throw new RuntimeException("Interrupted while loading resource widget (took " + (System.currentTimeMillis() - start) + " ms)", var5);
      }

      if (f == null) {
         throw new RuntimeException("No such widget type: " + name);
      } else {
         return f;
      }
   }

   public Widget(UI ui, Coord c, Coord sz) {
      this.ui = ui;
      this.c = c;
      this.sz = sz;
   }

   public Widget(Coord c, Coord sz, Widget parent) {
      synchronized (parent.ui) {
         this.ui = parent.ui;
         this.c = c;
         this.sz = sz;
         this.parent = parent;
         this.link();
         parent.newchild(this);
      }
   }

   private Coord relpos(String spec, Object[] args, int off) {
      int i = 0;
      Stack<Object> st = new Stack<>();

      while (i < spec.length()) {
         char op = spec.charAt(i++);
         if (Character.isDigit(op)) {
            int e = i;

            while (e < spec.length() && Character.isDigit(spec.charAt(e))) {
               e++;
            }

            st.push(Integer.parseInt(spec.substring(i - 1, e)));
            i = e;
         } else if (op == '!') {
            st.push(args[off++]);
         } else if (op == '_') {
            st.push(st.peek());
         } else if (op == '.') {
            st.pop();
         } else if (op == '^') {
            Object a = st.pop();
            Object b = st.pop();
            st.push(a);
            st.push(b);
         } else if (op == 'c') {
            int y = (Integer)st.pop();
            int x = (Integer)st.pop();
            st.push(new Coord(x, y));
         } else if (op == 'o') {
            Widget w = (Widget)st.pop();
            st.push(w.c.add(w.sz));
         } else if (op == 'p') {
            st.push(((Widget)st.pop()).c);
         } else if (op == 's') {
            st.push(((Widget)st.pop()).sz);
         } else if (op == 'w') {
            synchronized (this.ui) {
               st.push(this.ui.widgets.get((Integer)st.pop()));
            }
         } else if (op == 'x') {
            st.push(((Coord)st.pop()).x);
         } else if (op == 'y') {
            st.push(((Coord)st.pop()).y);
         } else if (op == '+') {
            Object b = st.pop();
            Object a = st.pop();
            if (a instanceof Integer && b instanceof Integer) {
               st.push((Integer)a + (Integer)b);
            } else {
               if (!(a instanceof Coord) || !(b instanceof Coord)) {
                  throw new RuntimeException("Invalid addition operands: " + a + " + " + b);
               }

               st.push(((Coord)a).add((Coord)b));
            }
         } else if (op == '-') {
            Object b = st.pop();
            Object a = st.pop();
            if (a instanceof Integer && b instanceof Integer) {
               st.push((Integer)a - (Integer)b);
            } else {
               if (!(a instanceof Coord) || !(b instanceof Coord)) {
                  throw new RuntimeException("Invalid subtraction operands: " + a + " - " + b);
               }

               st.push(((Coord)a).sub((Coord)b));
            }
         } else if (op == '*') {
            Object b = st.pop();
            Object a = st.pop();
            if (a instanceof Integer && b instanceof Integer) {
               st.push((Integer)a * (Integer)b);
            } else if (a instanceof Coord && b instanceof Integer) {
               st.push(((Coord)a).mul((Integer)b));
            } else {
               if (!(a instanceof Coord) || !(b instanceof Coord)) {
                  throw new RuntimeException("Invalid multiplication operands: " + a + " - " + b);
               }

               st.push(((Coord)a).mul((Coord)b));
            }
         } else if (op == '/') {
            Object b = st.pop();
            Object a = st.pop();
            if (a instanceof Integer && b instanceof Integer) {
               st.push((Integer)a / (Integer)b);
            } else if (a instanceof Coord && b instanceof Integer) {
               st.push(((Coord)a).div((Integer)b));
            } else {
               if (!(a instanceof Coord) || !(b instanceof Coord)) {
                  throw new RuntimeException("Invalid division operands: " + a + " - " + b);
               }

               st.push(((Coord)a).div((Coord)b));
            }
         } else if (!Character.isWhitespace(op)) {
            throw new RuntimeException("Unknown position operation: " + op);
         }
      }

      return (Coord)st.pop();
   }

   public Widget makechild(String type, Object[] pargs, Object[] cargs) {
      Coord c;
      if (pargs[0] instanceof Coord) {
         c = (Coord)pargs[0];
      } else {
         if (!(pargs[0] instanceof String)) {
            throw new RuntimeException("Unknown child widget creation specification.");
         }

         c = this.relpos((String)pargs[0], pargs, 1);
      }

      return gettype(type).create(c, this, cargs);
   }

   public void newchild(Widget w) {
   }

   public void link() {
      synchronized (this.ui) {
         if (this.parent.lchild != null) {
            this.parent.lchild.next = this;
         }

         if (this.parent.child == null) {
            this.parent.child = this;
         }

         this.prev = this.parent.lchild;
         this.parent.lchild = this;
      }
   }

   public void linkfirst() {
      synchronized (this.ui) {
         if (this.parent.child != null) {
            this.parent.child.prev = this;
         }

         if (this.parent.lchild == null) {
            this.parent.lchild = this;
         }

         this.next = this.parent.child;
         this.parent.child = this;
      }
   }

   public void unlink() {
      synchronized (this.ui) {
         if (this.next != null) {
            this.next.prev = this.prev;
         }

         if (this.prev != null) {
            this.prev.next = this.next;
         }

         if (this.parent.child == this) {
            this.parent.child = this.next;
         }

         if (this.parent.lchild == this) {
            this.parent.lchild = this.prev;
         }

         this.next = null;
         this.prev = null;
      }
   }

   public Coord xlate(Coord c, boolean in) {
      return c;
   }

   public Coord parentpos(Widget in) {
      return in == this ? new Coord(0, 0) : this.xlate(this.parent.parentpos(in).add(this.c), true);
   }

   public Coord rootpos() {
      return this.parentpos(this.ui.root);
   }

   public Coord rootxlate(Coord c) {
      return c.sub(this.rootpos());
   }

   public boolean hasparent(Widget w2) {
      for (Widget w = this; w != null; w = w.parent) {
         if (w == w2) {
            return true;
         }
      }

      return false;
   }

   public void gotfocus() {
      if (this.focusctl && this.focused != null) {
         this.focused.hasfocus = true;
         this.focused.gotfocus();
      }
   }

   public void reqdestroy() {
      this.destroy();
   }

   public void destroy() {
      if (this.canfocus) {
         this.setcanfocus(false);
      }

      this.unlink();
      this.parent.cdestroy(this);
   }

   public void cdestroy(Widget w) {
   }

   public int wdgid() {
      Integer id = this.ui.rwidgets.get(this);
      return id == null ? -1 : id;
   }

   public void lostfocus() {
      if (this.focusctl && this.focused != null) {
         this.focused.hasfocus = false;
         this.focused.lostfocus();
      }
   }

   public void setfocus(Widget w) {
      if (this.focusctl) {
         if (w != this.focused) {
            Widget last = this.focused;
            this.focused = w;
            if (last != null) {
               last.hasfocus = false;
            }

            w.hasfocus = true;
            if (last != null) {
               last.lostfocus();
            }

            w.gotfocus();
            if (this.ui != null && this.ui.rwidgets.containsKey(w) && this.ui.rwidgets.containsKey(this)) {
               this.wdgmsg("focus", this.ui.rwidgets.get(w));
            }
         }

         if (this.parent != null && this.canfocus) {
            this.parent.setfocus(this);
         }
      } else {
         this.parent.setfocus(w);
      }
   }

   public void setcanfocus(boolean canfocus) {
      this.autofocus = this.canfocus = canfocus;
      if (this.parent != null) {
         if (canfocus) {
            this.parent.newfocusable(this);
         } else {
            this.parent.delfocusable(this);
         }
      }
   }

   public void newfocusable(Widget w) {
      if (this.focusctl) {
         if (this.focused == null) {
            this.setfocus(w);
         }
      } else {
         this.parent.newfocusable(w);
      }
   }

   public void delfocusable(Widget w) {
      if (this.focusctl) {
         if (this.focused == w) {
            this.findfocus();
         }
      } else {
         this.parent.delfocusable(w);
      }
   }

   private void findfocus() {
      this.focused = null;

      for (Widget w = this.lchild; w != null; w = w.prev) {
         if (w.visible && w.autofocus) {
            this.focused = w;
            this.focused.hasfocus = true;
            w.gotfocus();
            break;
         }
      }
   }

   public void setfocusctl(boolean focusctl) {
      if (this.focusctl = focusctl) {
         this.findfocus();
         this.setcanfocus(true);
      }
   }

   public void setfocustab(boolean focustab) {
      if (focustab && !this.focusctl) {
         this.setfocusctl(true);
      }

      this.focustab = focustab;
   }

   public void uimsg(String msg, Object... args) {
      if (msg == "tabfocus") {
         this.setfocustab((Integer)args[0] != 0);
      } else if (msg == "act") {
         this.canactivate = (Integer)args[0] != 0;
      } else if (msg == "cancel") {
         this.cancancel = (Integer)args[0] != 0;
      } else if (msg == "autofocus") {
         this.autofocus = (Integer)args[0] != 0;
      } else if (msg == "focus") {
         Widget w = this.ui.widgets.get((Integer)args[0]);
         if (w != null && w.canfocus) {
            this.setfocus(w);
         }
      } else if (msg == "curs") {
         if (args.length == 0) {
            this.cursor = null;
         } else {
            this.cursor = Resource.load((String)args[0], (Integer)args[1]);
         }
      } else if (msg == "tip") {
         int a = 0;
         Object tt = args[a++];
         if (tt instanceof String) {
            this.tooltip = Text.render((String)tt);
         } else if (tt instanceof Integer) {
            final Indir<Resource> tres = this.ui.sess.getres((Integer)tt);
            this.tooltip = new Indir<Tex>() {
               Text t = null;

               public Tex get() {
                  if (this.t == null) {
                     Resource.Pagina pag;
                     try {
                        pag = tres.get().layer(Resource.pagina);
                     } catch (Loading var3) {
                        return null;
                     }

                     this.t = RichText.render(pag.text, 300);
                  }

                  return this.t.tex();
               }
            };
         }
      } else {
         System.err.println("Unhandled widget message: " + msg);
      }
   }

   public void wdgmsg(String msg, Object... args) {
      this.wdgmsg(this, msg, args);
   }

   public void wdgmsg(Widget sender, String msg, Object... args) {
      if (this.parent == null) {
         this.ui.wdgmsg(sender, msg, args);
      } else {
         this.parent.wdgmsg(sender, msg, args);
      }
   }

   public void tick(double dt) {
      Widget wdg = this.child;

      while (wdg != null) {
         Widget next = wdg.next;
         wdg.tick(dt);
         wdg = next;
      }

      Iterator<Widget.Anim> i = this.anims.iterator();

      while (i.hasNext()) {
         Widget.Anim anim = i.next();
         if (anim.tick(dt)) {
            i.remove();
         }
      }
   }

   public void draw(GOut g, boolean strict) {
      Widget wdg = this.child;

      while (wdg != null) {
         Widget next = wdg.next;
         if (wdg.visible) {
            Coord cc = this.xlate(wdg.c, true);
            GOut g2;
            if (strict) {
               g2 = g.reclip(cc, wdg.sz);
            } else {
               g2 = g.reclipl(cc, wdg.sz);
            }

            wdg.draw(g2);
         }

         wdg = next;
      }
   }

   public void draw(GOut g) {
      this.draw(g, true);
   }

   public boolean mousedown(Coord c, int button) {
      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible) {
            Coord cc = this.xlate(wdg.c, true);
            if (c.isect(cc, wdg.sz) && wdg.mousedown(c.add(cc.inv()), button)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean mouseup(Coord c, int button) {
      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible) {
            Coord cc = this.xlate(wdg.c, true);
            if (c.isect(cc, wdg.sz) && wdg.mouseup(c.add(cc.inv()), button)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean mousewheel(Coord c, int amount) {
      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible) {
            Coord cc = this.xlate(wdg.c, true);
            if (c.isect(cc, wdg.sz) && wdg.mousewheel(c.add(cc.inv()), amount)) {
               return true;
            }
         }
      }

      return false;
   }

   public void mousemove(Coord c) {
      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible) {
            Coord cc = this.xlate(wdg.c, true);
            wdg.mousemove(c.add(cc.inv()));
         }
      }
   }

   public boolean globtype(char key, KeyEvent ev) {
      for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
         if (wdg.globtype(key, ev)) {
            return true;
         }
      }

      return false;
   }

   public boolean type(char key, KeyEvent ev) {
      if (this.canactivate && key == '\n') {
         this.wdgmsg("activate");
         return true;
      } else if (this.cancancel && key == 27) {
         this.wdgmsg("cancel");
         return true;
      } else if (!this.focusctl) {
         for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
            if (wdg.visible && wdg.type(key, ev)) {
               return true;
            }
         }

         return false;
      } else if (this.focused == null) {
         return false;
      } else if (this.focused.type(key, ev)) {
         return true;
      } else if (!this.focustab) {
         return false;
      } else if (key != '\t') {
         return false;
      } else {
         Widget f = this.focused;

         do {
            if ((ev.getModifiers() & 1) == 0) {
               Widget n = f.rnext();
               f = n != null && n.hasparent(this) ? n : this.child;
            } else {
               Widget p = f.rprev();
               f = p != null && p.hasparent(this) ? p : this.lchild;
            }
         } while (!f.canfocus);

         this.setfocus(f);
         return true;
      }
   }

   public boolean keydown(KeyEvent ev) {
      if (this.focusctl) {
         return this.focused != null ? this.focused.keydown(ev) : false;
      } else {
         for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
            if (wdg.visible && wdg.keydown(ev)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean keyup(KeyEvent ev) {
      if (this.focusctl) {
         return this.focused != null ? this.focused.keyup(ev) : false;
      } else {
         for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
            if (wdg.visible && wdg.keyup(ev)) {
               return true;
            }
         }

         return false;
      }
   }

   public Coord contentsz() {
      Coord max = new Coord(0, 0);

      for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
         if (wdg.visible) {
            Coord br = wdg.c.add(wdg.sz);
            if (br.x > max.x) {
               max.x = br.x;
            }

            if (br.y > max.y) {
               max.y = br.y;
            }
         }
      }

      return max;
   }

   public void pack() {
      this.resize(this.contentsz());
   }

   public void resize(Coord sz) {
      this.sz = sz;

      for (Widget ch = this.child; ch != null; ch = ch.next) {
         ch.presize();
      }

      if (this.parent != null) {
         this.parent.cresize(this);
      }
   }

   public void cresize(Widget ch) {
   }

   public void presize() {
   }

   public void raise() {
      synchronized (this.ui) {
         this.unlink();
         this.link();
      }
   }

   public void lower() {
      synchronized (this.ui) {
         this.unlink();
         this.linkfirst();
      }
   }

   @Deprecated
   public <T extends Widget> T findchild(Class<T> cl) {
      for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
         if (cl.isInstance(wdg)) {
            return cl.cast(wdg);
         }

         T ret = wdg.findchild(cl);
         if (ret != null) {
            return ret;
         }
      }

      return null;
   }

   public Widget rprev() {
      if (this.lchild != null) {
         return this.lchild;
      } else {
         return this.prev != null ? this.prev : this.parent;
      }
   }

   public Widget rnext() {
      if (this.child != null) {
         return this.child;
      } else if (this.next != null) {
         return this.next;
      } else {
         for (Widget p = this.parent; p != null; p = p.parent) {
            if (p.next != null) {
               return p.next;
            }
         }

         return null;
      }
   }

   public <T extends Widget> Set<T> children(final Class<T> cl) {
      return new AbstractSet<T>() {
         @Override
         public int size() {
            int i = 0;

            for (T w : this) {
               i++;
            }

            return i;
         }

         @Override
         public Iterator<T> iterator() {
            return new Iterator<T>() {
               T cur = (T)this.n(Widget.this.child);

               private T n(Widget w) {
                  if (w == null) {
                     return null;
                  } else {
                     Widget n;
                     if (w.child != null) {
                        n = w.child;
                     } else if (w.next != null) {
                        n = w.next;
                     } else {
                        if (w.parent == Widget.this) {
                           return null;
                        }

                        n = w.parent;
                     }

                     return (T)(n != null && !cl.isInstance(n) ? this.n(n) : cl.cast(n));
                  }
               }

               public T next() {
                  if (this.cur == null) {
                     throw new NoSuchElementException();
                  } else {
                     T ret = this.cur;
                     this.cur = (T)this.n(ret);
                     return ret;
                  }
               }

               @Override
               public boolean hasNext() {
                  return this.cur != null;
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   public Resource getcurs(Coord c) {
      for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
         if (wdg.visible) {
            Coord cc = this.xlate(wdg.c, true);
            Resource ret;
            if (c.isect(cc, wdg.sz) && (ret = wdg.getcurs(c.add(cc.inv()))) != null) {
               return ret;
            }
         }
      }

      return this.cursor;
   }

   @Deprecated
   public Object tooltip(Coord c, boolean again) {
      return null;
   }

   public Object tooltip(Coord c, Widget prev) {
      if (prev != this) {
         this.prevtt = null;
      }

      if (this.tooltip != null) {
         this.prevtt = null;
         return this.tooltip;
      } else {
         for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
            if (wdg.visible) {
               Coord cc = this.xlate(wdg.c, true);
               if (c.isect(cc, wdg.sz)) {
                  Object ret = wdg.tooltip(c.add(cc.inv()), this.prevtt);
                  if (ret != null) {
                     this.prevtt = wdg;
                     return ret;
                  }
               }
            }
         }

         this.prevtt = null;
         return this.tooltip(c, prev == this);
      }
   }

   public <T extends Widget> T getparent(Class<T> cl) {
      for (Widget w = this; w != null; w = w.parent) {
         if (cl.isInstance(w)) {
            return cl.cast(w);
         }
      }

      return null;
   }

   public void hide() {
      this.visible = false;
      if (this.canfocus) {
         this.parent.delfocusable(this);
      }
   }

   public void show() {
      this.visible = true;
      if (this.canfocus) {
         this.parent.newfocusable(this);
      }
   }

   public boolean show(boolean show) {
      if (show) {
         this.show();
      } else {
         this.hide();
      }

      return show;
   }

   public boolean tvisible() {
      for (Widget w = this; w != null; w = w.parent) {
         if (!w.visible) {
            return false;
         }
      }

      return true;
   }

   @Widget.RName("ccnt")
   public static class $CCont implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         Widget ret = new Widget(c, (Coord)args[0], parent) {
            @Override
            public void presize() {
               this.c = this.parent.sz.div(2).sub(this.sz.div(2));
            }
         };
         ret.presize();
         return ret;
      }
   }

   @Widget.RName("cnt")
   public static class $Cont implements Widget.Factory {
      @Override
      public Widget create(Coord c, Widget parent, Object[] args) {
         return new Widget(c, (Coord)args[0], parent);
      }
   }

   public abstract class Anim {
      public Anim() {
         synchronized (Widget.this.ui) {
            Widget.this.anims.add(this);
         }
      }

      public void clear() {
         synchronized (Widget.this.ui) {
            Widget.this.anims.remove(this);
         }
      }

      public abstract boolean tick(double var1);
   }

   @Resource.PublishedCode(
      name = "wdg"
   )
   public interface Factory {
      Widget create(Coord var1, Widget var2, Object[] var3);
   }

   public abstract class NormAnim extends Widget.Anim {
      private double a = 0.0;
      private final double s;

      public NormAnim(double s) {
         this.s = 1.0 / s;
      }

      @Override
      public boolean tick(double dt) {
         this.a += dt;
         double na = this.a * this.s;
         if (na >= 1.0) {
            this.ntick(1.0);
            return true;
         } else {
            this.ntick(na);
            return false;
         }
      }

      public abstract void ntick(double var1);
   }

   @Target({ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   @Discoverable
   public @interface RName {
      String value();
   }
}
