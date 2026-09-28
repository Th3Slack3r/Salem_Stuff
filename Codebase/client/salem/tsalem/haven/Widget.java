package haven;

import dolda.jglob.Discoverable;
import dolda.jglob.Loader;
import java.awt.event.KeyEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.AbstractSet;
import java.util.ArrayList;
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
  
  public Coord render_c = null;
  
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
  
  public final Collection<Anim> anims = new LinkedList<>();
  
  static Map<String, Factory> types = new TreeMap<>();
  
  public boolean isTempHidden = false;
  
  @RName("cnt")
  public static class $Cont implements Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Widget(c, (Coord)args[0], parent);
    }
  }
  
  @RName("ccnt")
  public static class $CCont implements Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      Widget ret = new Widget(c, (Coord)args[0], parent) {
          public void presize() {
            this.c = this.parent.sz.div(2).sub(this.sz.div(2));
          }
        };
      ret.presize();
      return ret;
    }
  }
  
  @RName("fcnt")
  public static class $FCont implements Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      Widget ret = new Widget(c, Coord.z, parent) {
          Collection<Widget> fill = new ArrayList<>();
          
          public void presize() {
            resize(this.parent.sz);
            for (Widget ch : this.fill)
              ch.resize(this.sz); 
          }
          
          public Widget makechild(String type, Object[] pargs, Object[] cargs) {
            if (pargs[0] instanceof String && pargs[0].equals("fill")) {
              Widget child = Widget.gettype(type).create(Coord.z, this, cargs);
              child.resize(this.sz);
              this.fill.add(child);
              return child;
            } 
            return super.makechild(type, pargs, cargs);
          }
        };
      ret.presize();
      return ret;
    }
  }
  
  private static boolean inited = false;
  
  public static void initnames() {
    if (!inited) {
      for (Factory f : Loader.get(RName.class).instances(Factory.class)) {
        synchronized (types) {
          types.put(((RName)f.getClass().<RName>getAnnotation(RName.class)).value(), f);
        } 
      } 
      inited = true;
    } 
  }
  
  public static Factory gettype2(String name) throws InterruptedException {
    if (name.indexOf('/') < 0)
      synchronized (types) {
        return types.get(name);
      }  
    int ver = -1;
    int p;
    if ((p = name.indexOf(':')) > 0) {
      ver = Integer.parseInt(name.substring(p + 1));
      name = name.substring(0, p);
    } 
    Resource res = Resource.load(name, ver);
    while (true) {
      try {
        return res.<Factory>getcode(Factory.class, true);
      } catch (Loading l) {
        l.res.loadwaitint();
      } 
    } 
  }
  
  public static Factory gettype(String name) {
    Factory f;
    long start = System.currentTimeMillis();
    try {
      f = gettype2(name);
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while loading resource widget (took " + (System.currentTimeMillis() - start) + " ms)", e);
    } 
    if (f == null)
      throw new RuntimeException("No such widget type: " + name); 
    return f;
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
      link();
      parent.newchild(this);
    } 
  }
  
  private Coord relpos(String spec, Object[] args, int off) {
    int i = 0;
    Stack<Object> st = new Stack();
    while (i < spec.length()) {
      char op = spec.charAt(i++);
      if (Character.isDigit(op)) {
        int e;
        for (e = i; e < spec.length() && Character.isDigit(spec.charAt(e)); e++);
        st.push(Integer.valueOf(Integer.parseInt(spec.substring(i - 1, e))));
        i = e;
        continue;
      } 
      if (op == '!') {
        st.push(args[off++]);
        continue;
      } 
      if (op == '_') {
        st.push(st.peek());
        continue;
      } 
      if (op == '.') {
        st.pop();
        continue;
      } 
      if (op == '^') {
        Object a = st.pop();
        Object b = st.pop();
        st.push(a);
        st.push(b);
        continue;
      } 
      if (op == 'c') {
        int y = ((Integer)st.pop()).intValue();
        int x = ((Integer)st.pop()).intValue();
        st.push(new Coord(x, y));
        continue;
      } 
      if (op == 'o') {
        Widget w = (Widget)st.pop();
        st.push(w.c.add(w.sz));
        continue;
      } 
      if (op == 'p') {
        st.push(((Widget)st.pop()).c);
        continue;
      } 
      if (op == 's') {
        st.push(((Widget)st.pop()).sz);
        continue;
      } 
      if (op == 'w') {
        synchronized (this.ui) {
          st.push(this.ui.widgets.get(st.pop()));
        } 
        continue;
      } 
      if (op == 'x') {
        st.push(Integer.valueOf(((Coord)st.pop()).x));
        continue;
      } 
      if (op == 'y') {
        st.push(Integer.valueOf(((Coord)st.pop()).y));
        continue;
      } 
      if (op == '+') {
        Object b = st.pop();
        Object a = st.pop();
        if (a instanceof Integer && b instanceof Integer) {
          st.push(Integer.valueOf(((Integer)a).intValue() + ((Integer)b).intValue()));
          continue;
        } 
        if (a instanceof Coord && b instanceof Coord) {
          st.push(((Coord)a).add((Coord)b));
          continue;
        } 
        throw new RuntimeException("Invalid addition operands: " + a + " + " + b);
      } 
      if (op == '-') {
        Object b = st.pop();
        Object a = st.pop();
        if (a instanceof Integer && b instanceof Integer) {
          st.push(Integer.valueOf(((Integer)a).intValue() - ((Integer)b).intValue()));
          continue;
        } 
        if (a instanceof Coord && b instanceof Coord) {
          st.push(((Coord)a).sub((Coord)b));
          continue;
        } 
        throw new RuntimeException("Invalid subtraction operands: " + a + " - " + b);
      } 
      if (op == '*') {
        Object b = st.pop();
        Object a = st.pop();
        if (a instanceof Integer && b instanceof Integer) {
          st.push(Integer.valueOf(((Integer)a).intValue() * ((Integer)b).intValue()));
          continue;
        } 
        if (a instanceof Coord && b instanceof Integer) {
          st.push(((Coord)a).mul(((Integer)b).intValue()));
          continue;
        } 
        if (a instanceof Coord && b instanceof Coord) {
          st.push(((Coord)a).mul((Coord)b));
          continue;
        } 
        throw new RuntimeException("Invalid multiplication operands: " + a + " - " + b);
      } 
      if (op == '/') {
        Object b = st.pop();
        Object a = st.pop();
        if (a instanceof Integer && b instanceof Integer) {
          st.push(Integer.valueOf(((Integer)a).intValue() / ((Integer)b).intValue()));
          continue;
        } 
        if (a instanceof Coord && b instanceof Integer) {
          st.push(((Coord)a).div(((Integer)b).intValue()));
          continue;
        } 
        if (a instanceof Coord && b instanceof Coord) {
          st.push(((Coord)a).div((Coord)b));
          continue;
        } 
        throw new RuntimeException("Invalid division operands: " + a + " - " + b);
      } 
      if (Character.isWhitespace(op))
        continue; 
      throw new RuntimeException("Unknown position operation: " + op);
    } 
    return (Coord)st.pop();
  }
  
  public Widget makechild(String type, Object[] pargs, Object[] cargs) {
    Coord c;
    if (pargs[0] instanceof Coord) {
      c = (Coord)pargs[0];
    } else if (pargs[0] instanceof String) {
      c = relpos((String)pargs[0], pargs, 1);
    } else {
      throw new RuntimeException("Unknown child widget creation specification.");
    } 
    return gettype(type).create(c, this, cargs);
  }
  
  public void newchild(Widget w) {}
  
  public void link() {
    synchronized (this.ui) {
      if (this.parent.lchild != null)
        this.parent.lchild.next = this; 
      if (this.parent.child == null)
        this.parent.child = this; 
      this.prev = this.parent.lchild;
      this.parent.lchild = this;
    } 
  }
  
  public void linkfirst() {
    synchronized (this.ui) {
      if (this.parent.child != null)
        this.parent.child.prev = this; 
      if (this.parent.lchild == null)
        this.parent.lchild = this; 
      this.next = this.parent.child;
      this.parent.child = this;
    } 
  }
  
  public void unlink() {
    synchronized (this.ui) {
      if (this.next != null)
        this.next.prev = this.prev; 
      if (this.prev != null)
        this.prev.next = this.next; 
      if (this.parent.child == this)
        this.parent.child = this.next; 
      if (this.parent.lchild == this)
        this.parent.lchild = this.prev; 
      this.next = null;
      this.prev = null;
    } 
  }
  
  public Coord xlate(Coord c, boolean in) {
    return c;
  }
  
  public Coord parentpos(Widget in) {
    if (in == this)
      return new Coord(0, 0); 
    return xlate(this.parent.parentpos(in).add(this.c), true);
  }
  
  public Coord rootpos() {
    return parentpos(this.ui.root);
  }
  
  public Coord rootxlate(Coord c) {
    return c.sub(rootpos());
  }
  
  public boolean hasparent(Widget w2) {
    for (Widget w = this; w != null; w = w.parent) {
      if (w == w2)
        return true; 
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
    destroy();
  }
  
  public void destroy() {
    if (this.canfocus)
      setcanfocus(false); 
    unlink();
    this.parent.cdestroy(this);
  }
  
  public void cdestroy(Widget w) {}
  
  public int wdgid() {
    Integer id = this.ui.rwidgets.get(this);
    if (id == null)
      return -1; 
    return id.intValue();
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
        if (last != null)
          last.hasfocus = false; 
        w.hasfocus = true;
        if (last != null)
          last.lostfocus(); 
        w.gotfocus();
        if (this.ui != null && this.ui.rwidgets.containsKey(w) && this.ui.rwidgets.containsKey(this))
          wdgmsg("focus", new Object[] { this.ui.rwidgets.get(w) }); 
      } 
      if (this.parent != null && this.canfocus)
        this.parent.setfocus(this); 
    } else {
      this.parent.setfocus(w);
    } 
  }
  
  public void setcanfocus(boolean canfocus) {
    this.autofocus = this.canfocus = canfocus;
    if (this.parent != null)
      if (canfocus) {
        this.parent.newfocusable(this);
      } else {
        this.parent.delfocusable(this);
      }  
  }
  
  public void newfocusable(Widget w) {
    if (this.focusctl) {
      if (this.focused == null)
        setfocus(w); 
    } else {
      this.parent.newfocusable(w);
    } 
  }
  
  public void delfocusable(Widget w) {
    if (this.focusctl) {
      if (this.focused == w)
        findfocus(); 
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
      findfocus();
      setcanfocus(true);
    } 
  }
  
  public void setfocustab(boolean focustab) {
    if (focustab && !this.focusctl)
      setfocusctl(true); 
    this.focustab = focustab;
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "tabfocus") {
      setfocustab((((Integer)args[0]).intValue() != 0));
    } else if (msg == "act") {
      this.canactivate = (((Integer)args[0]).intValue() != 0);
    } else if (msg == "cancel") {
      this.cancancel = (((Integer)args[0]).intValue() != 0);
    } else if (msg == "autofocus") {
      this.autofocus = (((Integer)args[0]).intValue() != 0);
    } else if (msg == "focus") {
      Widget w = this.ui.widgets.get(args[0]);
      if (w != null && 
        w.canfocus)
        setfocus(w); 
    } else if (msg == "curs") {
      if (args.length == 0) {
        this.cursor = null;
      } else {
        this.cursor = Resource.load((String)args[0], ((Integer)args[1]).intValue());
      } 
    } else if (msg == "tip") {
      int a = 0;
      Object tt = args[a++];
      if (tt instanceof String) {
        this.tooltip = Text.render((String)tt);
      } else if (tt instanceof Integer) {
        final Indir<Resource> tres = this.ui.sess.getres(((Integer)tt).intValue());
        this.tooltip = new Indir<Tex>() {
            Text t = null;
            
            public Tex get() {
              if (this.t == null) {
                Resource.Pagina pag;
                try {
                  pag = ((Resource)tres.get()).<Resource.Pagina>layer(Resource.pagina);
                } catch (Loading e) {
                  return null;
                } 
                this.t = RichText.render(pag.text, 300, new Object[0]);
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
    try {
      if (msg != null)
        if (msg.equals("act")) {
          if (args != null && args.length == 2 && 
            args[0].equals("craft")) {
            String[] temp = { msg, "" + args[0], "" + args[1] };
            Makewindow.lastCraftOrBeltAction = temp;
          } 
        } else if (msg.equals("belt") && 
          args.length == 3) {
          String[] temp = { msg, "" + args[0], "" + args[1], "" + args[2] };
          Makewindow.lastCraftOrBeltAction = temp;
        }  
    } catch (Exception e) {
      Utils.msgLog(e.getMessage());
      Utils.msgLog(e.toString());
    } 
    wdgmsg(this, msg, args);
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (this.parent == null) {
      this.ui.wdgmsg(sender, msg, args);
    } else {
      this.parent.wdgmsg(sender, msg, args);
    } 
  }
  
  public void tick(double dt) {
    for (Widget wdg = this.child; wdg != null; wdg = next) {
      Widget next = wdg.next;
      wdg.tick(dt);
    } 
    for (Iterator<Anim> i = this.anims.iterator(); i.hasNext(); ) {
      Anim anim = i.next();
      if (anim.tick(dt))
        i.remove(); 
    } 
  }
  
  public void draw(GOut g, boolean strict) {
    for (Widget wdg = this.child; wdg != null; wdg = next) {
      Widget next = wdg.next;
      if (wdg.visible) {
        GOut g2;
        Coord cc = xlate(wdg.c, true);
        if (strict) {
          g2 = g.reclip(cc, wdg.sz);
        } else {
          g2 = g.reclipl(cc, wdg.sz);
        } 
        wdg.draw(g2);
      } 
    } 
  }
  
  public void draw(GOut g) {
    draw(g, true);
  }
  
  public boolean mousedown(Coord c, int button) {
    for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg.visible) {
        Coord cc = xlate(wdg.c, true);
        if (c.isect(cc, wdg.sz) && 
          wdg.mousedown(c.add(cc.inv()), button))
          return true; 
      } 
    } 
    return false;
  }
  
  public boolean mouseup(Coord c, int button) {
    for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg.visible) {
        Coord cc = xlate(wdg.c, true);
        if (c.isect(cc, wdg.sz) && 
          wdg.mouseup(c.add(cc.inv()), button))
          return true; 
      } 
    } 
    return false;
  }
  
  public boolean mousewheel(Coord c, int amount) {
    for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg.visible) {
        Coord cc = xlate(wdg.c, true);
        if (c.isect(cc, wdg.sz) && 
          wdg.mousewheel(c.add(cc.inv()), amount))
          return true; 
      } 
    } 
    return false;
  }
  
  public void mousemove(Coord c) {
    for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg.visible) {
        Coord cc = xlate(wdg.c, true);
        wdg.mousemove(c.add(cc.inv()));
      } 
    } 
  }
  
  public boolean globtype(char key, KeyEvent ev) {
    for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
      if (wdg.globtype(key, ev))
        return true; 
    } 
    return false;
  }
  
  public boolean type(char key, KeyEvent ev) {
    if (this.canactivate && 
      key == '\n') {
      wdgmsg("activate", new Object[0]);
      return true;
    } 
    if (this.cancancel && 
      key == '\033') {
      wdgmsg("cancel", new Object[0]);
      return true;
    } 
    if (this.focusctl) {
      if (this.focused != null) {
        if (this.focused.type(key, ev))
          return true; 
        if (this.focustab) {
          if (key == '\t') {
            Widget f = this.focused;
            do {
              if ((ev.getModifiers() & 0x1) == 0) {
                Widget n = f.rnext();
                f = (n == null || !n.hasparent(this)) ? this.child : n;
              } else {
                Widget p = f.rprev();
                f = (p == null || !p.hasparent(this)) ? this.lchild : p;
              } 
            } while (!f.canfocus);
            setfocus(f);
            return true;
          } 
          return false;
        } 
        return false;
      } 
      return false;
    } 
    for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
      if (wdg.visible && 
        wdg.type(key, ev))
        return true; 
    } 
    return false;
  }
  
  public boolean keydown(KeyEvent ev) {
    if (this.focusctl) {
      if (this.focused != null) {
        if (this.focused.keydown(ev))
          return true; 
        return false;
      } 
      return false;
    } 
    for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
      if (wdg.visible && 
        wdg.keydown(ev))
        return true; 
    } 
    return false;
  }
  
  public boolean keyup(KeyEvent ev) {
    if (this.focusctl) {
      if (this.focused != null) {
        if (this.focused.keyup(ev))
          return true; 
        return false;
      } 
      return false;
    } 
    for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
      if (wdg.visible && 
        wdg.keyup(ev))
        return true; 
    } 
    return false;
  }
  
  public Coord contentsz() {
    Coord max = new Coord(0, 0);
    for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
      if (wdg.visible) {
        Coord br = wdg.c.add(wdg.sz);
        if (br.x > max.x)
          max.x = br.x; 
        if (br.y > max.y)
          max.y = br.y; 
      } 
    } 
    return max;
  }
  
  public void pack() {
    resize(contentsz());
  }
  
  public void resize(Coord sz) {
    this.sz = sz;
    for (Widget ch = this.child; ch != null; ch = ch.next)
      ch.presize(); 
    if (this.parent != null)
      this.parent.cresize(this); 
  }
  
  public void cresize(Widget ch) {}
  
  public void presize() {}
  
  public void raise() {
    synchronized (this.ui) {
      unlink();
      link();
    } 
  }
  
  public void lower() {
    synchronized (this.ui) {
      unlink();
      linkfirst();
    } 
  }
  
  @Deprecated
  public <T extends Widget> T findchild(Class<T> cl) {
    for (Widget wdg = this.child; wdg != null; wdg = wdg.next) {
      if (cl.isInstance(wdg))
        return cl.cast(wdg); 
      T ret = wdg.findchild(cl);
      if (ret != null)
        return ret; 
    } 
    return null;
  }
  
  public Widget rprev() {
    if (this.lchild != null)
      return this.lchild; 
    if (this.prev != null)
      return this.prev; 
    return this.parent;
  }
  
  public Widget rnext() {
    if (this.child != null)
      return this.child; 
    if (this.next != null)
      return this.next; 
    for (Widget p = this.parent; p != null; p = p.parent) {
      if (p.next != null)
        return p.next; 
    } 
    return null;
  }
  
  public <T extends Widget> Set<T> children(final Class<T> cl) {
    return new AbstractSet<T>() {
        public int size() {
          int i = 0;
          for (Widget widget : this)
            i++; 
          return i;
        }
        
        public Iterator<T> iterator() {
          return new Iterator() {
              T cur = n(Widget.this.child);
              
              private T n(Widget w) {
                Widget n;
                if (w == null)
                  return null; 
                if (w.child != null) {
                  n = w.child;
                } else if (w.next != null) {
                  n = w.next;
                } else {
                  if (w.parent == Widget.this)
                    return null; 
                  n = w.parent;
                } 
                if (n == null || cl.isInstance(n))
                  return (T)cl.cast(n); 
                return n(n);
              }
              
              public T next() {
                if (this.cur == null)
                  throw new NoSuchElementException(); 
                T ret = this.cur;
                this.cur = n((Widget)ret);
                return ret;
              }
              
              public boolean hasNext() {
                return (this.cur != null);
              }
              
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
        Coord cc = xlate(wdg.c, true);
        Resource ret;
        if (c.isect(cc, wdg.sz) && (
          ret = wdg.getcurs(c.add(cc.inv()))) != null)
          return ret; 
      } 
    } 
    return this.cursor;
  }
  
  @Deprecated
  public Object tooltip(Coord c, boolean again) {
    return null;
  }
  
  public Object tooltip(Coord c, Widget prev) {
    if (Config.tt_off)
      return null; 
    if (prev != this)
      this.prevtt = null; 
    if (this.tooltip != null) {
      this.prevtt = null;
      return this.tooltip;
    } 
    for (Widget wdg = this.lchild; wdg != null; wdg = wdg.prev) {
      if (wdg.visible) {
        Coord cc = xlate(wdg.c, true);
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
    return tooltip(c, (prev == this));
  }
  
  public <T extends Widget> T getparent(Class<T> cl) {
    for (Widget w = this; w != null; w = w.parent) {
      if (cl.isInstance(w))
        return cl.cast(w); 
    } 
    return null;
  }
  
  public void hide() {
    this.visible = false;
    if (this.canfocus)
      this.parent.delfocusable(this); 
  }
  
  public void show() {
    this.visible = true;
    if (this.canfocus)
      this.parent.newfocusable(this); 
  }
  
  public boolean show(boolean show) {
    if (show) {
      show();
    } else {
      hide();
    } 
    return show;
  }
  
  public boolean tvisible() {
    for (Widget w = this; w != null; w = w.parent) {
      if (!w.visible)
        return false; 
    } 
    return true;
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
    
    public abstract boolean tick(double param1Double);
  }
  
  public abstract class NormAnim extends Anim {
    private double a = 0.0D;
    
    private final double s;
    
    public NormAnim(double s) {
      this.s = 1.0D / s;
    }
    
    public boolean tick(double dt) {
      this.a += dt;
      double na = this.a * this.s;
      if (na >= 1.0D) {
        ntick(1.0D);
        return true;
      } 
      ntick(na);
      return false;
    }
    
    public abstract void ntick(double param1Double);
  }
  
  @PublishedCode(name = "wdg")
  public static interface Factory {
    Widget create(Coord param1Coord, Widget param1Widget, Object[] param1ArrayOfObject);
  }
  
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Discoverable
  public static @interface RName {
    String value();
  }
}
