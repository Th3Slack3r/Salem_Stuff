package haven;

import haven.glsl.ShaderMacro;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public abstract class GLState {
  public void applyfrom(GOut g, GLState from) {
    throw new RuntimeException("Called applyfrom on non-conformant GLState (" + from + " -> " + this + ")");
  }
  
  public void applyto(GOut g, GLState to) {}
  
  public void reapply(GOut g) {}
  
  public ShaderMacro[] shaders() {
    return null;
  }
  
  public boolean reqshaders() {
    return false;
  }
  
  public int capply() {
    return 10;
  }
  
  public int cunapply() {
    return 1;
  }
  
  public int capplyfrom(GLState from) {
    return -1;
  }
  
  public int capplyto(GLState to) {
    return 0;
  }
  
  private static int slotnum = 0;
  
  private static Slot<?>[] deplist = (Slot<?>[])new Slot[0];
  
  private static Slot<?>[] idlist = (Slot<?>[])new Slot[0];
  
  public static class Slot<T extends GLState> {
    private static boolean dirty = false;
    
    private static Collection<Slot<?>> all = new LinkedList<>();
    
    public final Type type;
    
    public final int id;
    
    public final Class<T> scl;
    
    private int depid = -1;
    
    private final Slot<?>[] dep;
    
    private final Slot<?>[] rdep;
    
    private Slot<?>[] grdep;
    
    public enum Type {
      SYS, GEOM, DRAW;
    }
    
    public Slot(Type type, Class<T> scl, Slot<?>[] dep, Slot<?>[] rdep) {
      this.type = type;
      this.scl = scl;
      synchronized (Slot.class) {
        this.id = GLState.slotnum++;
        dirty = true;
        Slot[] arrayOfSlot = new Slot[GLState.slotnum];
        System.arraycopy(GLState.idlist, 0, arrayOfSlot, 0, GLState.idlist.length);
        arrayOfSlot[this.id] = this;
        GLState.idlist = (Slot<?>[])arrayOfSlot;
        all.add(this);
      } 
      if (dep == null) {
        this.dep = (Slot<?>[])new Slot[0];
      } else {
        this.dep = dep;
      } 
      if (rdep == null) {
        this.rdep = (Slot<?>[])new Slot[0];
      } else {
        this.rdep = rdep;
      } 
      for (Slot<?> ds : this.dep) {
        if (ds == null)
          throw new NullPointerException(); 
      } 
      for (Slot<?> ds : this.rdep) {
        if (ds == null)
          throw new NullPointerException(); 
      } 
    }
    
    public Slot(Type type, Class<T> scl, Slot... dep) {
      this(type, scl, (Slot<?>[])dep, null);
    }
    
    private static void makedeps(Collection<Slot<?>> slots) {
      Map<Slot<?>, Set<Slot<?>>> lrdep = new HashMap<>();
      for (Slot<?> s : slots)
        lrdep.put(s, new HashSet<>()); 
      for (Slot<?> s : slots) {
        ((Set)lrdep.get(s)).addAll(Arrays.asList(s.rdep));
        for (Slot<?> ds : s.dep)
          ((Set<Slot<?>>)lrdep.get(ds)).add(s); 
      } 
      Set<Slot<?>> left = new HashSet<>(slots);
      final Map<Slot<?>, Integer> order = new HashMap<>();
      int id = left.size() - 1;
      Slot[] arrayOfSlot = new Slot[0];
      while (!left.isEmpty()) {
        boolean err = true;
        Iterator<Slot<?>> i;
        label49: for (i = left.iterator(); i.hasNext(); ) {
          Slot<?> s = i.next();
          for (Slot<?> ds : lrdep.get(s)) {
            if (left.contains(ds))
              continue label49; 
          } 
          err = false;
          order.put(s, Integer.valueOf(s.depid = id--));
          Set<Slot<?>> grdep = new HashSet<>();
          for (Slot<?> ds : lrdep.get(s)) {
            grdep.add(ds);
            for (Slot<?> ds2 : ds.grdep)
              grdep.add(ds2); 
          } 
          s.grdep = (Slot<?>[])grdep.<Slot>toArray(arrayOfSlot);
          i.remove();
        } 
        if (err)
          throw new RuntimeException("Cycle encountered while compiling state slot dependencies"); 
      } 
      Comparator<Slot> cmp = new Comparator<Slot>() {
          public int compare(GLState.Slot a, GLState.Slot b) {
            return ((Integer)order.get(a)).intValue() - ((Integer)order.get(b)).intValue();
          }
        };
      for (Slot<?> s : slots)
        Arrays.sort(s.grdep, (Comparator)cmp); 
    }
    
    public static void update() {
      synchronized (Slot.class) {
        if (!dirty)
          return; 
        makedeps(all);
        GLState.deplist = (Slot<?>[])new Slot[all.size()];
        for (Slot<?> s : all)
          GLState.deplist[s.depid] = s; 
        dirty = false;
      } 
    }
    
    public String toString() {
      return "Slot<" + this.scl.getName() + ">";
    }
  }
  
  public static class Buffer {
    private GLState[] states = new GLState[GLState.slotnum];
    
    public final GLConfig cfg;
    
    public Buffer(GLConfig cfg) {
      this.cfg = cfg;
    }
    
    public Buffer copy() {
      Buffer ret = new Buffer(this.cfg);
      System.arraycopy(this.states, 0, ret.states, 0, this.states.length);
      return ret;
    }
    
    public void copy(Buffer dest) {
      dest.adjust();
      System.arraycopy(this.states, 0, dest.states, 0, this.states.length);
      for (int i = this.states.length; i < dest.states.length; i++)
        dest.states[i] = null; 
    }
    
    public void copy(Buffer dest, GLState.Slot.Type type) {
      dest.adjust();
      adjust();
      for (int i = 0; i < this.states.length; i++) {
        if ((GLState.idlist[i]).type == type)
          dest.states[i] = this.states[i]; 
      } 
    }
    
    private void adjust() {
      if (this.states.length < GLState.slotnum) {
        GLState[] n = new GLState[GLState.slotnum];
        System.arraycopy(this.states, 0, n, 0, this.states.length);
        this.states = n;
      } 
    }
    
    public <T extends GLState> void put(GLState.Slot<? super T> slot, T state) {
      if (this.states.length <= slot.id)
        adjust(); 
      this.states[slot.id] = (GLState)state;
    }
    
    public <T extends GLState> T get(GLState.Slot<T> slot) {
      if (this.states.length <= slot.id)
        return null; 
      return (T)this.states[slot.id];
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof Buffer))
        return false; 
      Buffer b = (Buffer)o;
      adjust();
      b.adjust();
      for (int i = 0; i < this.states.length; i++) {
        if (!this.states[i].equals(b.states[i]))
          return false; 
      } 
      return true;
    }
    
    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append('[');
      for (int i = 0; i < this.states.length; i++) {
        if (i > 0)
          buf.append(", "); 
        if (this.states[i] == null) {
          buf.append("null");
        } else {
          buf.append(this.states[i].toString());
        } 
      } 
      buf.append(']');
      return buf.toString();
    }
    
    GLState[] states() {
      return this.states;
    }
  }
  
  public static int bufdiff(Buffer f, Buffer t, boolean[] trans, boolean[] repl) {
    Slot.update();
    int cost = 0;
    f.adjust();
    t.adjust();
    if (trans != null)
      for (int j = 0; j < trans.length; j++) {
        trans[j] = false;
        repl[j] = false;
      }  
    for (int i = 0; i < f.states.length; i++) {
      if (((f.states[i] == null) ? true : false) != ((t.states[i] == null) ? true : false) || (f.states[i] != null && t.states[i] != null && !f.states[i].equals(t.states[i]))) {
        if (!repl[i]) {
          int cat = -1, caf = -1;
          if (t.states[i] != null && f.states[i] != null) {
            cat = f.states[i].capplyto(t.states[i]);
            caf = t.states[i].capplyfrom(f.states[i]);
          } 
          if (cat >= 0 && caf >= 0) {
            cost += cat + caf;
            if (trans != null)
              trans[i] = true; 
          } else {
            if (f.states[i] != null)
              cost += f.states[i].cunapply(); 
            if (t.states[i] != null)
              cost += t.states[i].capply(); 
            if (trans != null)
              repl[i] = true; 
          } 
        } 
        for (Slot ds : (idlist[i]).grdep) {
          int id = ds.id;
          if (!repl[id]) {
            if (trans != null)
              repl[id] = true; 
            if (t.states[id] != null)
              cost += t.states[id].cunapply(); 
            if (f.states[id] != null)
              cost += f.states[id].capply(); 
          } 
        } 
      } 
    } 
    return cost;
  }
  
  public static class TexUnit {
    private final GLState.Applier st;
    
    public final int id;
    
    private TexUnit(GLState.Applier st, int id) {
      this.st = st;
      this.id = id;
    }
    
    public void act() {
      this.st.texunit(this.id);
    }
    
    public void free() {
      if (this.st.textab[this.id] != null)
        throw new RuntimeException("Texunit " + this.id + " freed twice"); 
      this.st.textab[this.id] = this;
    }
    
    public void ufree() {
      act();
      this.st.gl.glBindTexture(3553, 0);
      free();
    }
  }
  
  public static class Applier {
    public static boolean debug = false;
    
    private final GLState.Buffer old;
    
    private final GLState.Buffer cur;
    
    private final GLState.Buffer next;
    
    public final GL2 gl;
    
    public final GLConfig cfg;
    
    private boolean[] trans = new boolean[0];
    
    private boolean[] repl = new boolean[0];
    
    private final boolean[] adirty = new boolean[0];
    
    private ShaderMacro[][] shaders = new ShaderMacro[0][];
    
    private ShaderMacro[][] nshaders = new ShaderMacro[0][];
    
    private int proghash = 0;
    
    private int nproghash = 0;
    
    public ShaderMacro.Program prog;
    
    public boolean usedprog;
    
    public boolean pdirty = false;
    
    public boolean sdirty = false;
    
    public long time = 0L;
    
    public Matrix4f cam = Matrix4f.id;
    
    public Matrix4f wxf = Matrix4f.id;
    
    public Matrix4f mv = Matrix4f.identity();
    
    private Matrix4f ccam = null;
    
    private Matrix4f cwxf = null;
    
    private int matmode;
    
    private int texunit;
    
    private GLState.TexUnit[] textab;
    
    private SavedProg[] ptab;
    
    private int nprog;
    
    private long lastclean;
    
    public <T extends GLState> void put(GLState.Slot<? super T> slot, T state) {
      this.next.put(slot, state);
    }
    
    public <T extends GLState> T get(GLState.Slot<T> slot) {
      return this.next.get(slot);
    }
    
    public <T extends GLState> T cur(GLState.Slot<T> slot) {
      return this.cur.get(slot);
    }
    
    public <T extends GLState> T old(GLState.Slot<T> slot) {
      return this.old.get(slot);
    }
    
    public void prep(GLState st) {
      st.prep(this.next);
    }
    
    public void set(GLState.Buffer to) {
      to.copy(this.next);
    }
    
    public void copy(GLState.Buffer dest) {
      this.next.copy(dest);
    }
    
    public GLState.Buffer copy() {
      return this.next.copy();
    }
    
    public void apply(GOut g) {
      long st = 0L;
      if (Config.profile)
        st = System.nanoTime(); 
      if (this.trans.length < GLState.slotnum)
        synchronized (GLState.Slot.class) {
          this.trans = new boolean[GLState.slotnum];
          this.repl = new boolean[GLState.slotnum];
          this.shaders = Utils.<ShaderMacro[]>extend(this.shaders, GLState.slotnum);
          this.nshaders = Utils.<ShaderMacro[]>extend(this.shaders, GLState.slotnum);
        }  
      GLState.bufdiff(this.cur, this.next, this.trans, this.repl);
      GLState.Slot[] arrayOfSlot = (GLState.Slot[])GLState.deplist;
      this.nproghash = this.proghash;
      int i;
      for (i = this.trans.length - 1; i >= 0; i--) {
        this.nshaders[i] = this.shaders[i];
        if (this.repl[i] || this.trans[i]) {
          GLState nst = this.next.states[i];
          ShaderMacro[] ns = (nst == null) ? null : nst.shaders();
          if (ns != this.nshaders[i]) {
            this.nproghash ^= System.identityHashCode(this.nshaders[i]) ^ System.identityHashCode(ns);
            this.nshaders[i] = ns;
            this.sdirty = true;
          } 
        } 
      } 
      this.usedprog = (this.prog != null);
      if (this.sdirty) {
        ShaderMacro.Program np;
        boolean usesl;
        int j;
        switch ((GLSettings.ProgMode)g.gc.pref.progmode.val) {
          case ALWAYS:
            usesl = true;
            break;
          case REQ:
            usesl = false;
            for (j = 0; j < this.trans.length; j++) {
              GLState nst = this.next.states[j];
              if (this.nshaders[j] != null && nst != null && nst.reqshaders()) {
                usesl = true;
                break;
              } 
            } 
            break;
          default:
            usesl = false;
            break;
        } 
        if (usesl) {
          np = findprog(this.nproghash, this.nshaders);
        } else {
          np = null;
        } 
        if (np != this.prog) {
          if (np != null) {
            np.apply(g);
          } else {
            g.gl.glUseProgramObjectARB(0);
          } 
          this.prog = np;
          if (debug)
            GOut.checkerr((GL)g.gl); 
          this.pdirty = true;
        } 
      } 
      if (((this.prog != null)) != this.usedprog)
        for (i = 0; i < this.trans.length; i++) {
          if (this.trans[i])
            this.repl[i] = true; 
        }  
      this.cur.copy(this.old);
      for (i = arrayOfSlot.length - 1; i >= 0; i--) {
        int id = (arrayOfSlot[i]).id;
        if (id < this.repl.length && this.repl[id]) {
          if (this.cur.states[id] != null) {
            this.cur.states[id].unapply(g);
            if (debug)
              stcheckerr(g, "unapply", this.cur.states[id]); 
          } 
          this.cur.states[id] = null;
          this.proghash ^= System.identityHashCode(this.shaders[id]);
          this.shaders[id] = null;
        } 
      } 
      for (i = 0; i < arrayOfSlot.length; i++) {
        int id = (arrayOfSlot[i]).id;
        if (id < this.repl.length && this.repl[id]) {
          if (this.next.states[id] != null) {
            this.next.states[id].apply(g);
            this.cur.states[id] = this.next.states[id];
            this.proghash ^= System.identityHashCode(this.shaders[id]) ^ System.identityHashCode(this.nshaders[id]);
            this.shaders[id] = this.nshaders[id];
            if (debug)
              stcheckerr(g, "apply", this.cur.states[id]); 
          } 
          if (!this.pdirty && this.prog != null)
            this.prog.adirty(arrayOfSlot[i]); 
        } else if (id < this.trans.length && this.trans[id]) {
          this.cur.states[id].applyto(g, this.next.states[id]);
          if (debug)
            stcheckerr(g, "applyto", this.cur.states[id]); 
          this.next.states[id].applyfrom(g, this.cur.states[id]);
          this.cur.states[id] = this.next.states[id];
          this.proghash ^= System.identityHashCode(this.shaders[id]) ^ System.identityHashCode(this.nshaders[id]);
          this.shaders[id] = this.nshaders[id];
          if (debug)
            stcheckerr(g, "applyfrom", this.cur.states[id]); 
          if (!this.pdirty && this.prog != null)
            this.prog.adirty(arrayOfSlot[i]); 
        } else if (this.prog != null && this.pdirty && id < this.shaders.length && this.shaders[id] != null) {
          this.cur.states[id].reapply(g);
          if (debug)
            stcheckerr(g, "reapply", this.cur.states[id]); 
        } 
      } 
      if (this.ccam != this.cam || this.cwxf != this.wxf) {
        this.mv.load(this.ccam = this.cam).mul1(this.cwxf = this.wxf);
        matmode(5888);
        this.gl.glLoadMatrixf(this.mv.m, 0);
      } 
      if (this.prog != null)
        this.prog.autoapply(g, this.pdirty); 
      this.pdirty = this.sdirty = false;
      GOut.checkerr((GL)this.gl);
      if (Config.profile)
        this.time += System.nanoTime() - st; 
    }
    
    public static class ApplyException extends RuntimeException {
      public final transient GLState st;
      
      public final String func;
      
      public ApplyException(String func, GLState st, Throwable cause) {
        super("Error in " + func + " of " + st, cause);
        this.st = st;
        this.func = func;
      }
    }
    
    private void stcheckerr(GOut g, String func, GLState st) {
      try {
        GOut.checkerr((GL)g.gl);
      } catch (RuntimeException e) {
        throw new ApplyException(func, st, e);
      } 
    }
    
    public Applier(GL2 gl, GLConfig cfg) {
      this.matmode = 5888;
      this.texunit = 0;
      this.textab = new GLState.TexUnit[0];
      this.ptab = new SavedProg[32];
      this.nprog = 0;
      this.lastclean = System.currentTimeMillis();
      this.gl = gl;
      this.cfg = cfg;
      this.old = new GLState.Buffer(cfg);
      this.cur = new GLState.Buffer(cfg);
      this.next = new GLState.Buffer(cfg);
    }
    
    public void matmode(int mode) {
      if (mode != this.matmode) {
        this.gl.glMatrixMode(mode);
        this.matmode = mode;
      } 
    }
    
    public void texunit(int unit) {
      if (unit != this.texunit) {
        this.gl.glActiveTexture(33984 + unit);
        this.texunit = unit;
      } 
    }
    
    public GLState.TexUnit texalloc() {
      int i;
      for (i = 0; i < this.textab.length; i++) {
        if (this.textab[i] != null) {
          GLState.TexUnit ret = this.textab[i];
          this.textab[i] = null;
          return ret;
        } 
      } 
      this.textab = new GLState.TexUnit[i + 1];
      return new GLState.TexUnit(this, i);
    }
    
    public GLState.TexUnit texalloc(GOut g, TexGL tex) {
      GLState.TexUnit ret = texalloc();
      ret.act();
      this.gl.glBindTexture(3553, tex.glid(g));
      return ret;
    }
    
    public GLState.TexUnit texalloc(GOut g, TexMS tex) {
      GLState.TexUnit ret = texalloc();
      ret.act();
      this.gl.glBindTexture(37120, tex.glid(g));
      return ret;
    }
    
    public static class SavedProg {
      public final int hash;
      
      public final ShaderMacro.Program prog;
      
      public final ShaderMacro[][] shaders;
      
      public SavedProg next;
      
      boolean used = true;
      
      public SavedProg(int hash, ShaderMacro.Program prog, ShaderMacro[][] shaders) {
        this.hash = hash;
        this.prog = prog;
        this.shaders = Utils.<ShaderMacro[]>splice(shaders, 0);
      }
    }
    
    private ShaderMacro.Program findprog(int hash, ShaderMacro[][] shaders) {
      int idx = hash & this.ptab.length - 1;
      for (SavedProg s = this.ptab[idx]; s != null; s = s.next) {
        if (s.hash == hash) {
          int j;
          for (j = 0; j < s.shaders.length; j++) {
            if (shaders[j] != s.shaders[j])
              break label36; 
          } 
          label36: while (true) {
            if (j < shaders.length) {
              if (shaders[j] != null)
                break; 
              j++;
              continue;
            } 
            s.used = true;
            return s.prog;
          } 
        } 
      } 
      Collection<ShaderMacro> mods = new LinkedList<>();
      for (int i = 0; i < shaders.length; i++) {
        if (shaders[i] != null)
          for (int o = 0; o < (shaders[i]).length; o++)
            mods.add(shaders[i][o]);  
      } 
      ShaderMacro.Program prog = ShaderMacro.Program.build(mods);
      SavedProg savedProg1 = new SavedProg(hash, prog, shaders);
      savedProg1.next = this.ptab[idx];
      this.ptab[idx] = savedProg1;
      this.nprog++;
      if (this.nprog > this.ptab.length)
        rehash(this.ptab.length * 2); 
      return prog;
    }
    
    private void rehash(int nlen) {
      SavedProg[] ntab = new SavedProg[nlen];
      for (int i = 0; i < this.ptab.length; i++) {
        while (this.ptab[i] != null) {
          SavedProg s = this.ptab[i];
          this.ptab[i] = s.next;
          int ni = s.hash & ntab.length - 1;
          s.next = ntab[ni];
          ntab[ni] = s;
        } 
      } 
      this.ptab = ntab;
    }
    
    public void clean() {
      long now = System.currentTimeMillis();
      if (now - this.lastclean > 60000L) {
        for (int i = 0; i < this.ptab.length; i++) {
          for (SavedProg c = this.ptab[i], p = null; c != null; p = c, c = c.next) {
            if (!c.used) {
              if (p != null) {
                p.next = c.next;
              } else {
                this.ptab[i] = c.next;
              } 
              c.prog.dispose();
              this.nprog--;
            } else {
              c.used = false;
            } 
          } 
        } 
        this.lastclean = now;
      } 
    }
    
    public int numprogs() {
      return this.nprog;
    }
  }
  
  private class Wrapping implements Rendered {
    private final Rendered r;
    
    private Wrapping(Rendered r) {
      if (r == null)
        throw new NullPointerException("Wrapping null in " + GLState.this); 
      this.r = r;
    }
    
    public void draw(GOut g) {}
    
    public boolean setup(RenderList rl) {
      rl.add(this.r, GLState.this);
      return false;
    }
  }
  
  public Rendered apply(Rendered r) {
    return new Wrapping(r);
  }
  
  public static abstract class Abstract extends GLState {
    public void apply(GOut g) {}
    
    public void unapply(GOut g) {}
  }
  
  public static class Composed extends Abstract {
    public final GLState[] states;
    
    public Composed(GLState... states) {
      for (GLState st : states) {
        if (st == null)
          throw new RuntimeException("null state in list of " + Arrays.asList(states)); 
      } 
      this.states = states;
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof Composed))
        return false; 
      return Arrays.equals((Object[])this.states, (Object[])((Composed)o).states);
    }
    
    public int hashCode() {
      return Arrays.hashCode((Object[])this.states);
    }
    
    public void prep(GLState.Buffer buf) {
      for (GLState st : this.states)
        st.prep(buf); 
    }
  }
  
  public static GLState compose(GLState... states) {
    return new Composed(states);
  }
  
  public static class Delegate extends Abstract {
    public GLState del;
    
    public Delegate(GLState del) {
      this.del = del;
    }
    
    public void prep(GLState.Buffer buf) {
      this.del.prep(buf);
    }
  }
  
  public static abstract class StandAlone extends GLState {
    public final GLState.Slot<StandAlone> slot;
    
    public StandAlone(GLState.Slot.Type type, GLState.Slot<?>... dep) {
      this.slot = new GLState.Slot<>(type, StandAlone.class, (GLState.Slot[])dep);
    }
    
    public void prep(GLState.Buffer buf) {
      buf.put(this.slot, this);
    }
  }
  
  public static final GLState nullstate = new GLState() {
      public void apply(GOut g) {}
      
      public void unapply(GOut g) {}
      
      public void prep(GLState.Buffer buf) {}
    };
  
  static {
    Console.setscmd("applydb", new Console.Command() {
          public void run(Console cons, String[] args) {
            GLState.Applier.debug = Utils.parsebool(args[1], false);
          }
        });
  }
  
  public abstract void apply(GOut paramGOut);
  
  public abstract void unapply(GOut paramGOut);
  
  public abstract void prep(Buffer paramBuffer);
  
  public static interface Global {
    void postsetup(RenderList param1RenderList);
    
    void prerender(RenderList param1RenderList, GOut param1GOut);
    
    void postrender(RenderList param1RenderList, GOut param1GOut);
  }
  
  public static interface GlobalState {
    GLState.Global global(RenderList param1RenderList, GLState.Buffer param1Buffer);
  }
}
