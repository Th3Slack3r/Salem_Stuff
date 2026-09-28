package haven;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;

public class RenderList {
  public final GLConfig cfg;
  
  private Slot[] list = new Slot[100];
  
  private int cur = 0;
  
  private Slot curp = null;
  
  private GLState.Global[] gstates = new GLState.Global[0];
  
  private static final ThreadLocal<RenderList> curref = new ThreadLocal<>();
  
  private final Iterable<Slot> slotsi;
  
  public class Slot {
    public Rendered r;
    
    public GLState.Buffer os = new GLState.Buffer(RenderList.this.cfg);
    
    public GLState.Buffer cs = new GLState.Buffer(RenderList.this.cfg);
    
    public Rendered.Order o;
    
    public boolean d;
    
    public Slot p;
  }
  
  private Slot getslot() {
    int i = this.cur++;
    if (i >= this.list.length) {
      Slot[] n = new Slot[i * 2];
      System.arraycopy(this.list, 0, n, 0, i);
      this.list = n;
    } 
    Slot s;
    if ((s = this.list[i]) == null)
      s = this.list[i] = new Slot(); 
    return s;
  }
  
  public RenderList(GLConfig cfg) {
    this.slotsi = new Iterable<Slot>() {
        public Iterator<RenderList.Slot> iterator() {
          return new Iterator<RenderList.Slot>() {
              private int i = 0;
              
              public RenderList.Slot next() {
                return RenderList.this.list[this.i++];
              }
              
              public boolean hasNext() {
                return (this.i < RenderList.this.cur);
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
      };
    this.dbc = new GLState[0];
    this.ignload = true;
    this.cfg = cfg;
  }
  
  public Iterable<Slot> slots() {
    return this.slotsi;
  }
  
  public static RenderList current() {
    return curref.get();
  }
  
  protected void setup(Slot s, Rendered r) {
    s.r = r;
    Slot pp = s.p = this.curp;
    if (pp == null)
      curref.set(this); 
    try {
      this.curp = s;
      s.d = r.setup(this);
    } finally {
      if ((this.curp = pp) == null)
        curref.remove(); 
    } 
  }
  
  protected void postsetup(Slot ps, GLState.Buffer t) {
    this.gstates = getgstates();
    Slot pp = this.curp;
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
    rewind();
    Slot s = getslot();
    t.copy(s.os);
    t.copy(s.cs);
    setup(s, r);
    postsetup(s, t);
  }
  
  public void add(Rendered r, GLState t) {
    Slot s = getslot();
    if (this.curp == null)
      throw new RuntimeException("Tried to set up relative slot with no parent"); 
    this.curp.cs.copy(s.os);
    if (t != null)
      t.prep(s.os); 
    s.os.copy(s.cs);
    setup(s, r);
  }
  
  public void add2(Rendered r, GLState.Buffer t) {
    Slot s = getslot();
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
  
  private static final Comparator<Slot> cmp = new Comparator<Slot>() {
      public int compare(RenderList.Slot a, RenderList.Slot b) {
        if (!a.d && !b.d)
          return 0; 
        if (a.d && !b.d)
          return -1; 
        if (!a.d && b.d)
          return 1; 
        int az = a.o.mainz(), bz = b.o.mainz();
        if (az != bz)
          return az - bz; 
        if (a.o != b.o)
          throw new RuntimeException("Found two different orderings with the same main-Z: " + a.o + " and " + b.o); 
        int ret = a.o.cmp().compare(a.r, b.r, a.os, b.os);
        if (ret != 0)
          return ret; 
        return System.identityHashCode(a.r) - System.identityHashCode(b.r);
      }
    };
  
  private GLState[] dbc;
  
  public boolean ignload;
  
  private GLState.Global[] getgstates() {
    IdentityHashMap<GLState.Global, GLState.Global> gstates = new IdentityHashMap<>(this.gstates.length);
    int i;
    for (i = 0; i < this.dbc.length; i++)
      this.dbc[i] = null; 
    for (i = 0; i < this.cur; i++) {
      if ((this.list[i]).d) {
        GLState.Buffer ctx = (this.list[i]).os;
        GLState[] sl = ctx.states();
        if (sl.length > this.dbc.length)
          this.dbc = new GLState[sl.length]; 
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
    return (GLState.Global[])gstates.keySet().toArray((Object[])new GLState.Global[0]);
  }
  
  public void fin() {
    for (int i = 0; i < this.cur; i++) {
      if (((this.list[i]).o = (this.list[i]).os.<GLState>get(Rendered.order)) == null)
        (this.list[i]).o = Rendered.deflt; 
      if ((this.list[i]).os.get(Rendered.skip.slot) != null)
        (this.list[i]).d = false; 
    } 
    try {
      Arrays.sort(this.list, 0, this.cur, cmp);
    } catch (Exception exception) {}
  }
  
  public static class RLoad extends Loading {
    public static RLoad wrap(final Loading l) {
      return new RLoad() {
          public boolean canwait() {
            return l.canwait();
          }
          
          public void waitfor() throws InterruptedException {
            l.waitfor();
          }
        };
    }
  }
  
  protected void render(GOut g, Rendered r) {
    try {
      r.draw(g);
    } catch (RLoad l) {
      if (this.ignload)
        return; 
      throw l;
    } 
  }
  
  public void render(GOut g) {
    for (GLState.Global gs : this.gstates)
      gs.prerender(this, g); 
    for (int i = 0; i < this.cur; i++) {
      Slot s = this.list[i];
      if (!s.d)
        break; 
      g.st.set(s.os);
      render(g, s.r);
    } 
    for (GLState.Global gs : this.gstates)
      gs.postrender(this, g); 
  }
  
  public void rewind() {
    if (this.curp != null)
      throw new RuntimeException("Tried to rewind RenderList while adding to it."); 
    this.cur = 0;
  }
  
  public void dump(PrintStream out) {
    for (Slot s : slots())
      out.println((s.d ? " " : "!") + s.r + ": " + s.os); 
  }
}
