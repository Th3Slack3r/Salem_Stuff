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
import javax.media.opengl.GL2;

public abstract class GLState {
   private static int slotnum = 0;
   private static GLState.Slot<?>[] deplist = new GLState.Slot[0];
   private static GLState.Slot<?>[] idlist = new GLState.Slot[0];
   public static final GLState nullstate = new GLState() {
      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }

      @Override
      public void prep(GLState.Buffer buf) {
      }
   };

   public abstract void apply(GOut var1);

   public abstract void unapply(GOut var1);

   public abstract void prep(GLState.Buffer var1);

   public void applyfrom(GOut g, GLState from) {
      throw new RuntimeException("Called applyfrom on non-conformant GLState (" + from + " -> " + this + ")");
   }

   public void applyto(GOut g, GLState to) {
   }

   public void reapply(GOut g) {
   }

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

   public static int bufdiff(GLState.Buffer f, GLState.Buffer t, boolean[] trans, boolean[] repl) {
      GLState.Slot.update();
      int cost = 0;
      f.adjust();
      t.adjust();
      if (trans != null) {
         for (int i = 0; i < trans.length; i++) {
            trans[i] = false;
            repl[i] = false;
         }
      }

      for (int i = 0; i < f.states.length; i++) {
         if (f.states[i] == null != (t.states[i] == null) || f.states[i] != null && t.states[i] != null && !f.states[i].equals(t.states[i])) {
            if (!repl[i]) {
               int cat = -1;
               int caf = -1;
               if (t.states[i] != null && f.states[i] != null) {
                  cat = f.states[i].capplyto(t.states[i]);
                  caf = t.states[i].capplyfrom(f.states[i]);
               }

               if (cat >= 0 && caf >= 0) {
                  cost += cat + caf;
                  if (trans != null) {
                     trans[i] = true;
                  }
               } else {
                  if (f.states[i] != null) {
                     cost += f.states[i].cunapply();
                  }

                  if (t.states[i] != null) {
                     cost += t.states[i].capply();
                  }

                  if (trans != null) {
                     repl[i] = true;
                  }
               }
            }

            for (GLState.Slot ds : idlist[i].grdep) {
               int id = ds.id;
               if (!repl[id]) {
                  if (trans != null) {
                     repl[id] = true;
                  }

                  if (t.states[id] != null) {
                     cost += t.states[id].cunapply();
                  }

                  if (f.states[id] != null) {
                     cost += f.states[id].capply();
                  }
               }
            }
         }
      }

      return cost;
   }

   public Rendered apply(Rendered r) {
      return new GLState.Wrapping(r);
   }

   public static GLState compose(GLState... states) {
      return new GLState.Composed(states);
   }

   static {
      Console.setscmd("applydb", new Console.Command() {
         @Override
         public void run(Console cons, String[] args) {
            GLState.Applier.debug = Utils.parsebool(args[1], false);
         }
      });
   }

   public abstract static class Abstract extends GLState {
      @Override
      public void apply(GOut g) {
      }

      @Override
      public void unapply(GOut g) {
      }
   }

   public static class Applier {
      public static boolean debug = false;
      private GLState.Buffer old;
      private GLState.Buffer cur;
      private GLState.Buffer next;
      public final GL2 gl;
      public final GLConfig cfg;
      private boolean[] trans = new boolean[0];
      private boolean[] repl = new boolean[0];
      private boolean[] adirty = new boolean[0];
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
      private int matmode = 5888;
      private int texunit = 0;
      private GLState.TexUnit[] textab = new GLState.TexUnit[0];
      private GLState.Applier.SavedProg[] ptab = new GLState.Applier.SavedProg[32];
      private int nprog = 0;
      private long lastclean = System.currentTimeMillis();

      public Applier(GL2 gl, GLConfig cfg) {
         this.gl = gl;
         this.cfg = cfg;
         this.old = new GLState.Buffer(cfg);
         this.cur = new GLState.Buffer(cfg);
         this.next = new GLState.Buffer(cfg);
      }

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
         if (Config.profile) {
            st = System.nanoTime();
         }

         if (this.trans.length < GLState.slotnum) {
            synchronized (GLState.Slot.class) {
               this.trans = new boolean[GLState.slotnum];
               this.repl = new boolean[GLState.slotnum];
               this.shaders = Utils.extend(this.shaders, GLState.slotnum);
               this.nshaders = Utils.extend(this.shaders, GLState.slotnum);
            }
         }

         GLState.bufdiff(this.cur, this.next, this.trans, this.repl);
         GLState.Slot<?>[] deplist = GLState.deplist;
         this.nproghash = this.proghash;

         for (int i = this.trans.length - 1; i >= 0; i--) {
            this.nshaders[i] = this.shaders[i];
            if (this.repl[i] || this.trans[i]) {
               GLState nst = this.next.states[i];
               ShaderMacro[] ns = nst == null ? null : nst.shaders();
               if (ns != this.nshaders[i]) {
                  this.nproghash = this.nproghash ^ System.identityHashCode(this.nshaders[i]) ^ System.identityHashCode(ns);
                  this.nshaders[i] = ns;
                  this.sdirty = true;
               }
            }
         }

         this.usedprog = this.prog != null;
         if (this.sdirty) {
            boolean usesl;
            label189:
            switch ((GLSettings.ProgMode)g.gc.pref.progmode.val) {
               case ALWAYS:
                  usesl = true;
                  break;
               case REQ:
                  usesl = false;

                  for (int ix = 0; ix < this.trans.length; ix++) {
                     GLState nst = this.next.states[ix];
                     if (this.nshaders[ix] != null && nst != null && nst.reqshaders()) {
                        usesl = true;
                        break label189;
                     }
                  }
                  break;
               case NEVER:
               default:
                  usesl = false;
            }

            ShaderMacro.Program np;
            if (usesl) {
               np = this.findprog(this.nproghash, this.nshaders);
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
               if (debug) {
                  GOut.checkerr(g.gl);
               }

               this.pdirty = true;
            }
         }

         if (this.prog != null != this.usedprog) {
            for (int ixx = 0; ixx < this.trans.length; ixx++) {
               if (this.trans[ixx]) {
                  this.repl[ixx] = true;
               }
            }
         }

         this.cur.copy(this.old);

         for (int ixxx = deplist.length - 1; ixxx >= 0; ixxx--) {
            int id = deplist[ixxx].id;
            if (id < this.repl.length && this.repl[id]) {
               if (this.cur.states[id] != null) {
                  this.cur.states[id].unapply(g);
                  if (debug) {
                     this.stcheckerr(g, "unapply", this.cur.states[id]);
                  }
               }

               this.cur.states[id] = null;
               this.proghash = this.proghash ^ System.identityHashCode(this.shaders[id]);
               this.shaders[id] = null;
            }
         }

         for (int ixxxx = 0; ixxxx < deplist.length; ixxxx++) {
            int id = deplist[ixxxx].id;
            if (id < this.repl.length && this.repl[id]) {
               if (this.next.states[id] != null) {
                  this.next.states[id].apply(g);
                  this.cur.states[id] = this.next.states[id];
                  this.proghash = this.proghash ^ System.identityHashCode(this.shaders[id]) ^ System.identityHashCode(this.nshaders[id]);
                  this.shaders[id] = this.nshaders[id];
                  if (debug) {
                     this.stcheckerr(g, "apply", this.cur.states[id]);
                  }
               }

               if (!this.pdirty && this.prog != null) {
                  this.prog.adirty(deplist[ixxxx]);
               }
            } else if (id < this.trans.length && this.trans[id]) {
               this.cur.states[id].applyto(g, this.next.states[id]);
               if (debug) {
                  this.stcheckerr(g, "applyto", this.cur.states[id]);
               }

               this.next.states[id].applyfrom(g, this.cur.states[id]);
               this.cur.states[id] = this.next.states[id];
               this.proghash = this.proghash ^ System.identityHashCode(this.shaders[id]) ^ System.identityHashCode(this.nshaders[id]);
               this.shaders[id] = this.nshaders[id];
               if (debug) {
                  this.stcheckerr(g, "applyfrom", this.cur.states[id]);
               }

               if (!this.pdirty && this.prog != null) {
                  this.prog.adirty(deplist[ixxxx]);
               }
            } else if (this.prog != null && this.pdirty && id < this.shaders.length && this.shaders[id] != null) {
               this.cur.states[id].reapply(g);
               if (debug) {
                  this.stcheckerr(g, "reapply", this.cur.states[id]);
               }
            }
         }

         if (this.ccam != this.cam || this.cwxf != this.wxf) {
            this.mv.load(this.ccam = this.cam).mul1(this.cwxf = this.wxf);
            this.matmode(5888);
            this.gl.glLoadMatrixf(this.mv.m, 0);
         }

         if (this.prog != null) {
            this.prog.autoapply(g, this.pdirty);
         }

         this.pdirty = this.sdirty = false;
         GOut.checkerr(this.gl);
         if (Config.profile) {
            this.time = this.time + (System.nanoTime() - st);
         }
      }

      private void stcheckerr(GOut g, String func, GLState st) {
         try {
            GOut.checkerr(g.gl);
         } catch (RuntimeException var5) {
            throw new GLState.Applier.ApplyException(func, st, var5);
         }
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
         GLState.TexUnit ret = this.texalloc();
         ret.act();
         this.gl.glBindTexture(3553, tex.glid(g));
         return ret;
      }

      public GLState.TexUnit texalloc(GOut g, TexMS tex) {
         GLState.TexUnit ret = this.texalloc();
         ret.act();
         this.gl.glBindTexture(37120, tex.glid(g));
         return ret;
      }

      private ShaderMacro.Program findprog(int hash, ShaderMacro[][] shaders) {
         int idx = hash & this.ptab.length - 1;

         label61:
         for (GLState.Applier.SavedProg s = this.ptab[idx]; s != null; s = s.next) {
            if (s.hash == hash) {
               int i;
               for (i = 0; i < s.shaders.length; i++) {
                  if (shaders[i] != s.shaders[i]) {
                     continue label61;
                  }
               }

               while (i < shaders.length) {
                  if (shaders[i] != null) {
                     continue label61;
                  }

                  i++;
               }

               s.used = true;
               return s.prog;
            }
         }

         Collection<ShaderMacro> mods = new LinkedList<>();

         for (int i = 0; i < shaders.length; i++) {
            if (shaders[i] != null) {
               for (int o = 0; o < shaders[i].length; o++) {
                  mods.add(shaders[i][o]);
               }
            }
         }

         ShaderMacro.Program prog = ShaderMacro.Program.build(mods);
         GLState.Applier.SavedProg sx = new GLState.Applier.SavedProg(hash, prog, shaders);
         sx.next = this.ptab[idx];
         this.ptab[idx] = sx;
         this.nprog++;
         if (this.nprog > this.ptab.length) {
            this.rehash(this.ptab.length * 2);
         }

         return prog;
      }

      private void rehash(int nlen) {
         GLState.Applier.SavedProg[] ntab = new GLState.Applier.SavedProg[nlen];

         for (int i = 0; i < this.ptab.length; i++) {
            while (this.ptab[i] != null) {
               GLState.Applier.SavedProg s = this.ptab[i];
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
               GLState.Applier.SavedProg c = this.ptab[i];

               for (GLState.Applier.SavedProg p = null; c != null; c = c.next) {
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

                  p = c;
               }
            }

            this.lastclean = now;
         }
      }

      public int numprogs() {
         return this.nprog;
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

      public static class SavedProg {
         public final int hash;
         public final ShaderMacro.Program prog;
         public final ShaderMacro[][] shaders;
         public GLState.Applier.SavedProg next;
         boolean used = true;

         public SavedProg(int hash, ShaderMacro.Program prog, ShaderMacro[][] shaders) {
            this.hash = hash;
            this.prog = prog;
            this.shaders = Utils.splice(shaders, 0);
         }
      }
   }

   public static class Buffer {
      private GLState[] states = new GLState[GLState.slotnum];
      public final GLConfig cfg;

      public Buffer(GLConfig cfg) {
         this.cfg = cfg;
      }

      public GLState.Buffer copy() {
         GLState.Buffer ret = new GLState.Buffer(this.cfg);
         System.arraycopy(this.states, 0, ret.states, 0, this.states.length);
         return ret;
      }

      public void copy(GLState.Buffer dest) {
         dest.adjust();
         System.arraycopy(this.states, 0, dest.states, 0, this.states.length);

         for (int i = this.states.length; i < dest.states.length; i++) {
            dest.states[i] = null;
         }
      }

      public void copy(GLState.Buffer dest, GLState.Slot.Type type) {
         dest.adjust();
         this.adjust();

         for (int i = 0; i < this.states.length; i++) {
            if (GLState.idlist[i].type == type) {
               dest.states[i] = this.states[i];
            }
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
         if (this.states.length <= slot.id) {
            this.adjust();
         }

         this.states[slot.id] = state;
      }

      public <T extends GLState> T get(GLState.Slot<T> slot) {
         return (T)(this.states.length <= slot.id ? null : this.states[slot.id]);
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof GLState.Buffer)) {
            return false;
         } else {
            GLState.Buffer b = (GLState.Buffer)o;
            this.adjust();
            b.adjust();

            for (int i = 0; i < this.states.length; i++) {
               if (!this.states[i].equals(b.states[i])) {
                  return false;
               }
            }

            return true;
         }
      }

      @Override
      public String toString() {
         StringBuilder buf = new StringBuilder();
         buf.append('[');

         for (int i = 0; i < this.states.length; i++) {
            if (i > 0) {
               buf.append(", ");
            }

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

   public static class Composed extends GLState.Abstract {
      public final GLState[] states;

      public Composed(GLState... states) {
         for (GLState st : states) {
            if (st == null) {
               throw new RuntimeException("null state in list of " + Arrays.<GLState>asList(states));
            }
         }

         this.states = states;
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof GLState.Composed) ? false : Arrays.equals((Object[])this.states, (Object[])((GLState.Composed)o).states);
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode((Object[])this.states);
      }

      @Override
      public void prep(GLState.Buffer buf) {
         for (GLState st : this.states) {
            st.prep(buf);
         }
      }
   }

   public static class Delegate extends GLState.Abstract {
      public GLState del;

      public Delegate(GLState del) {
         this.del = del;
      }

      @Override
      public void prep(GLState.Buffer buf) {
         this.del.prep(buf);
      }
   }

   public interface Global {
      void postsetup(RenderList var1);

      void prerender(RenderList var1, GOut var2);

      void postrender(RenderList var1, GOut var2);
   }

   public interface GlobalState {
      GLState.Global global(RenderList var1, GLState.Buffer var2);
   }

   public static class Slot<T extends GLState> {
      private static boolean dirty = false;
      private static Collection<GLState.Slot<?>> all = new LinkedList<>();
      public final GLState.Slot.Type type;
      public final int id;
      public final Class<T> scl;
      private int depid = -1;
      private final GLState.Slot<?>[] dep;
      private final GLState.Slot<?>[] rdep;
      private GLState.Slot<?>[] grdep;

      public Slot(GLState.Slot.Type type, Class<T> scl, GLState.Slot<?>[] dep, GLState.Slot<?>[] rdep) {
         this.type = type;
         this.scl = scl;
         synchronized (GLState.Slot.class) {
            this.id = GLState.slotnum++;
            dirty = true;
            GLState.Slot<?>[] nlist = new GLState.Slot[GLState.slotnum];
            System.arraycopy(GLState.idlist, 0, nlist, 0, GLState.idlist.length);
            nlist[this.id] = this;
            GLState.idlist = nlist;
            all.add(this);
         }

         if (dep == null) {
            this.dep = new GLState.Slot[0];
         } else {
            this.dep = dep;
         }

         if (rdep == null) {
            this.rdep = new GLState.Slot[0];
         } else {
            this.rdep = rdep;
         }

         for (GLState.Slot<?> ds : this.dep) {
            if (ds == null) {
               throw new NullPointerException();
            }
         }

         for (GLState.Slot<?> dsx : this.rdep) {
            if (dsx == null) {
               throw new NullPointerException();
            }
         }
      }

      public Slot(GLState.Slot.Type type, Class<T> scl, GLState.Slot... dep) {
         this(type, scl, dep, null);
      }

      private static void makedeps(Collection<GLState.Slot<?>> slots) {
         Map<GLState.Slot<?>, Set<GLState.Slot<?>>> lrdep = new HashMap<>();

         for (GLState.Slot<?> s : slots) {
            lrdep.put(s, new HashSet<>());
         }

         for (GLState.Slot<?> s : slots) {
            lrdep.get(s).addAll(Arrays.asList(s.rdep));

            for (GLState.Slot<?> ds : s.dep) {
               lrdep.get(ds).add(s);
            }
         }

         Set<GLState.Slot<?>> left = new HashSet<>(slots);
         final Map<GLState.Slot<?>, Integer> order = new HashMap<>();
         int id = left.size() - 1;
         GLState.Slot<?>[] cp = new GLState.Slot[0];

         while (!left.isEmpty()) {
            boolean err = true;
            Iterator<GLState.Slot<?>> i = left.iterator();

            label67:
            while (i.hasNext()) {
               GLState.Slot<?> s = i.next();

               for (GLState.Slot<?> ds : lrdep.get(s)) {
                  if (left.contains(ds)) {
                     continue label67;
                  }
               }

               err = false;
               order.put(s, s.depid = id--);
               Set<GLState.Slot<?>> grdep = new HashSet<>();

               for (GLState.Slot<?> dsx : lrdep.get(s)) {
                  grdep.add(dsx);

                  for (GLState.Slot<?> ds2 : dsx.grdep) {
                     grdep.add(ds2);
                  }
               }

               s.grdep = grdep.toArray(cp);
               i.remove();
            }

            if (err) {
               throw new RuntimeException("Cycle encountered while compiling state slot dependencies");
            }
         }

         Comparator<GLState.Slot> cmp = new Comparator<GLState.Slot>() {
            public int compare(GLState.Slot a, GLState.Slot b) {
               return order.get(a) - order.get(b);
            }
         };

         for (GLState.Slot<?> s : slots) {
            Arrays.sort(s.grdep, cmp);
         }
      }

      public static void update() {
         synchronized (GLState.Slot.class) {
            if (dirty) {
               makedeps(all);
               GLState.deplist = new GLState.Slot[all.size()];

               for (GLState.Slot s : all) {
                  GLState.deplist[s.depid] = s;
               }

               dirty = false;
            }
         }
      }

      @Override
      public String toString() {
         return "Slot<" + this.scl.getName() + ">";
      }

      public static enum Type {
         SYS,
         GEOM,
         DRAW;
      }
   }

   public abstract static class StandAlone extends GLState {
      public final GLState.Slot<GLState.StandAlone> slot;

      public StandAlone(GLState.Slot.Type type, GLState.Slot<?>... dep) {
         this.slot = new GLState.Slot<>(type, GLState.StandAlone.class, dep);
      }

      @Override
      public void prep(GLState.Buffer buf) {
         buf.put(this.slot, this);
      }
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
         if (this.st.textab[this.id] != null) {
            throw new RuntimeException("Texunit " + this.id + " freed twice");
         } else {
            this.st.textab[this.id] = this;
         }
      }

      public void ufree() {
         this.act();
         this.st.gl.glBindTexture(3553, 0);
         this.free();
      }
   }

   public class Wrapping implements Rendered {
      public final Rendered r;

      private Wrapping(Rendered r) {
         if (r == null) {
            throw new NullPointerException("Wrapping null in " + GLState.this);
         } else {
            this.r = r;
         }
      }

      @Override
      public void draw(GOut g) {
      }

      @Override
      public boolean setup(RenderList rl) {
         rl.add(this.r, GLState.this);
         return false;
      }
   }
}
