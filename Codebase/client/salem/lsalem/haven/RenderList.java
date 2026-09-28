package haven;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;

public class RenderList {
   public final GLConfig cfg;
   private RenderList.Slot[] list = new RenderList.Slot[100];
   private int cur = 0;
   private RenderList.Slot curp = null;
   private GLState.Global[] gstates = new GLState.Global[0];
   private static final ThreadLocal<RenderList> curref = new ThreadLocal<>();
   private final Iterable<RenderList.Slot> slotsi = new Iterable<RenderList.Slot>() {
      @Override
      public Iterator<RenderList.Slot> iterator() {
         return new Iterator<RenderList.Slot>() {
            private int i = 0;

            public RenderList.Slot next() {
               return RenderList.this.list[this.i++];
            }

            @Override
            public boolean hasNext() {
               return this.i < RenderList.this.cur;
            }

            @Override
            public void remove() {
               throw new UnsupportedOperationException();
            }
         };
      }
   };
   private static final Comparator<RenderList.Slot> cmp = new Comparator<RenderList.Slot>() {
      public int compare(RenderList.Slot a, RenderList.Slot b) {
         if (!a.d && !b.d) {
            return 0;
         } else if (a.d && !b.d) {
            return -1;
         } else if (!a.d && b.d) {
            return 1;
         } else {
            int az = a.o.mainz();
            int bz = b.o.mainz();
            if (az != bz) {
               return az - bz;
            } else if (a.o != b.o) {
               throw new RuntimeException("Found two different orderings with the same main-Z: " + a.o + " and " + b.o);
            } else {
               int ret = a.o.cmp().compare(a.r, b.r, a.os, b.os);
               return ret != 0 ? ret : System.identityHashCode(a.r) - System.identityHashCode(b.r);
            }
         }
      }
   };
   private GLState[] dbc = new GLState[0];
   public boolean ignload = true;

   public RenderList(GLConfig cfg) {
      this.cfg = cfg;
   }

   private RenderList.Slot getslot() {
      int i = this.cur++;
      if (i >= this.list.length) {
         RenderList.Slot[] n = new RenderList.Slot[i * 2];
         System.arraycopy(this.list, 0, n, 0, i);
         this.list = n;
      }

      RenderList.Slot s;
      if ((s = this.list[i]) == null) {
         s = this.list[i] = new RenderList.Slot();
      }

      return s;
   }

   public Iterable<RenderList.Slot> slots() {
      return this.slotsi;
   }

   public static RenderList current() {
      return curref.get();
   }

   protected void setup(RenderList.Slot s, Rendered r) {
      s.r = r;
      RenderList.Slot pp = s.p = this.curp;
      if (pp == null) {
         curref.set(this);
      }

      try {
         this.curp = s;
         s.d = r.setup(this);
      } finally {
         if ((this.curp = pp) == null) {
            curref.remove();
         }
      }
   }

   protected void postsetup(RenderList.Slot ps, GLState.Buffer t) {
      this.gstates = this.getgstates();
      RenderList.Slot pp = this.curp;

      try {
         this.curp = ps;

         for (GLState.Global gs : this.gstates) {
            t.copy(ps.cs);
            gs.postsetup(this);
         }
      } finally {
         this.curp = pp;
      }
   }

   public void setup(Rendered r, GLState.Buffer t) {
      this.rewind();
      RenderList.Slot s = this.getslot();
      t.copy(s.os);
      t.copy(s.cs);
      this.setup(s, r);
      this.postsetup(s, t);
   }

   public void add(Rendered r, GLState t) {
      RenderList.Slot s = this.getslot();
      if (this.curp == null) {
         throw new RuntimeException("Tried to set up relative slot with no parent");
      } else {
         this.curp.cs.copy(s.os);
         if (t != null) {
            t.prep(s.os);
         }

         s.os.copy(s.cs);
         this.setup(s, r);
      }
   }

   public void add2(Rendered r, GLState.Buffer t) {
      RenderList.Slot s = this.getslot();
      t.copy(s.os);
      s.r = r;
      s.p = this.curp;
      s.d = true;
   }

   public GLState.Buffer cstate() {
      return this.curp.cs;
   }

   public GLState.Buffer state() {
      return this.curp.os;
   }

   public void prepo(GLState t) {
      t.prep(this.curp.os);
   }

   public void prepc(GLState t) {
      t.prep(this.curp.cs);
   }

   private GLState.Global[] getgstates() {
      IdentityHashMap<GLState.Global, GLState.Global> gstates = new IdentityHashMap<>(this.gstates.length);

      for (int i = 0; i < this.dbc.length; i++) {
         this.dbc[i] = null;
      }

      for (int i = 0; i < this.cur; i++) {
         if (this.list[i].d) {
            GLState.Buffer ctx = this.list[i].os;
            GLState[] sl = ctx.states();
            if (sl.length > this.dbc.length) {
               this.dbc = new GLState[sl.length];
            }

            for (int o = 0; o < sl.length; o++) {
               GLState st = sl[o];
               if (st != this.dbc[o]) {
                  if (st instanceof GLState.GlobalState) {
                     GLState.Global gst = ((GLState.GlobalState)st).global(this, ctx);
                     gstates.put(gst, gst);
                  }

                  this.dbc[o] = st;
               }
            }
         }
      }

      return gstates.keySet().toArray(new GLState.Global[0]);
   }

   public void fin() {
      for (int i = 0; i < this.cur; i++) {
         if ((this.list[i].o = this.list[i].os.get(Rendered.order)) == null) {
            this.list[i].o = Rendered.deflt;
         }

         if (this.list[i].os.get(Rendered.skip.slot) != null) {
            this.list[i].d = false;
         }
      }

      Arrays.sort(this.list, 0, this.cur, cmp);
   }

   protected void render(GOut g, Rendered r) {
      try {
         r.draw(g);
      } catch (RenderList.RLoad var4) {
         if (!this.ignload) {
            throw var4;
         }
      }
   }

   public void render(GOut g) {
      for (GLState.Global gs : this.gstates) {
         gs.prerender(this, g);
      }

      for (int i = 0; i < this.cur; i++) {
         RenderList.Slot s = this.list[i];
         if (!s.d) {
            break;
         }

         g.st.set(s.os);
         this.render(g, s.r);
      }

      for (GLState.Global gs : this.gstates) {
         gs.postrender(this, g);
      }
   }

   public void rewind() {
      if (this.curp != null) {
         throw new RuntimeException("Tried to rewind RenderList while adding to it.");
      } else {
         this.cur = 0;
      }
   }

   public void dump(PrintStream out) {
      for (RenderList.Slot s : this.slots()) {
         out.println((s.d ? " " : "!") + s.r + ": " + s.os);
      }
   }

   public static class RLoad extends Loading {
      public static RenderList.RLoad wrap(final Loading l) {
         return new RenderList.RLoad() {
            @Override
            public boolean canwait() {
               return l.canwait();
            }

            @Override
            public void waitfor() throws InterruptedException {
               l.waitfor();
            }
         };
      }
   }

   public class Slot {
      public Rendered r;
      public GLState.Buffer os = new GLState.Buffer(RenderList.this.cfg);
      public GLState.Buffer cs = new GLState.Buffer(RenderList.this.cfg);
      public Rendered.Order o;
      public boolean d;
      public RenderList.Slot p;
   }
}
