package haven;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Profiler {
  private static Loop loop;
  
  public final Thread th;
  
  private boolean enabled;
  
  private final Map<Function, Function> funs;
  
  private int nticks;
  
  public Profiler(Thread th) {
    this.funs = new HashMap<>();
    this.nticks = 0;
    this.th = th;
  }
  
  public Profiler() {
    this(Thread.currentThread());
  }
  
  public void enable() {
    if (Thread.currentThread() != this.th)
      throw new RuntimeException("Enabled from non-owning thread"); 
    if (this.enabled)
      throw new RuntimeException("Enabled when already enabled"); 
    if (loop == null)
      synchronized (Loop.class) {
        if (loop == null) {
          loop = new Loop();
          loop.start();
        } 
      }  
    synchronized (loop.current) {
      loop.current.add(this);
    } 
    this.enabled = true;
  }
  
  public void disable() {
    if (Thread.currentThread() != this.th)
      throw new RuntimeException("Disabled from non-owning thread"); 
    if (!this.enabled)
      throw new RuntimeException("Disabled when already disabled"); 
    synchronized (loop.current) {
      loop.current.remove(this);
    } 
    this.enabled = false;
  }
  
  public static class Function {
    public final String cl;
    
    public final String nm;
    
    public int dticks;
    
    public int iticks;
    
    public Map<Function, Integer> tticks = new HashMap<>();
    
    public Map<Function, Integer> fticks = new HashMap<>();
    
    public Map<Integer, Integer> lticks = new HashMap<>();
    
    private int hc;
    
    public Function(StackTraceElement f) {
      this(f.getClassName(), f.getMethodName());
    }
    
    public boolean equals(Object bp) {
      if (!(bp instanceof Function))
        return false; 
      Function b = (Function)bp;
      return (b.cl.equals(this.cl) && b.nm.equals(this.nm));
    }
    
    public Function(String cl, String nm) {
      this.hc = 0;
      this.cl = cl;
      this.nm = nm;
    }
    
    public int hashCode() {
      if (this.hc == 0)
        this.hc = this.cl.hashCode() * 31 + this.nm.hashCode(); 
      return this.hc;
    }
  }
  
  private Function getfun(StackTraceElement f) {
    Function key = new Function(f);
    Function ret = this.funs.get(key);
    if (ret == null) {
      ret = key;
      this.funs.put(ret, ret);
    } 
    return ret;
  }
  
  protected void tick(StackTraceElement[] bt) {
    this.nticks++;
    Function pf = getfun(bt[0]);
    pf.dticks++;
    if (pf.lticks.containsKey(Integer.valueOf(bt[0].getLineNumber()))) {
      pf.lticks.put(Integer.valueOf(bt[0].getLineNumber()), Integer.valueOf(((Integer)pf.lticks.get(Integer.valueOf(bt[0].getLineNumber()))).intValue() + 1));
    } else {
      pf.lticks.put(Integer.valueOf(bt[0].getLineNumber()), Integer.valueOf(1));
    } 
    for (int i = 1; i < bt.length; i++) {
      StackTraceElement f = bt[i];
      Function fn = getfun(f);
      fn.iticks++;
      if (fn.tticks.containsKey(pf)) {
        fn.tticks.put(pf, Integer.valueOf(((Integer)fn.tticks.get(pf)).intValue() + 1));
      } else {
        fn.tticks.put(pf, Integer.valueOf(1));
      } 
      if (pf.fticks.containsKey(fn)) {
        pf.fticks.put(fn, Integer.valueOf(((Integer)pf.fticks.get(fn)).intValue() + 1));
      } else {
        pf.fticks.put(fn, Integer.valueOf(1));
      } 
      pf = fn;
    } 
    System.err.print(".");
  }
  
  public void outputlp(OutputStream out, String cl, String fnm) {
    Function fn = this.funs.get(new Function(cl, fnm));
    if (fn == null)
      return; 
    Map<Integer, Integer> lt = fn.lticks;
    PrintStream p = new PrintStream(out);
    List<Integer> lines = new ArrayList<>(lt.keySet());
    Collections.sort(lines);
    for (Iterator<Integer> iterator = lines.iterator(); iterator.hasNext(); ) {
      int ln = ((Integer)iterator.next()).intValue();
      p.printf("%d: %d\n", new Object[] { Integer.valueOf(ln), lt.get(Integer.valueOf(ln)) });
    } 
    p.println();
  }
  
  public void output(OutputStream out) {
    PrintStream p = new PrintStream(out);
    List<Function> funs = new ArrayList<>(this.funs.keySet());
    Collections.sort(funs, new Comparator<Function>() {
          public int compare(Profiler.Function a, Profiler.Function b) {
            return b.dticks - a.dticks;
          }
        });
    p.println("Functions sorted by direct ticks:");
    for (Function fn : funs) {
      if (fn.dticks < 1)
        continue; 
      p.print("    ");
      String nm = fn.cl + "." + fn.nm;
      p.print(nm);
      for (int i = nm.length(); i < 60; i++)
        p.print(" "); 
      p.printf("%6d (%5.2f%%)", new Object[] { Integer.valueOf(fn.dticks), Double.valueOf(100.0D * fn.dticks / this.nticks) });
      p.println();
    } 
    p.println();
    Collections.sort(funs, new Comparator<Function>() {
          public int compare(Profiler.Function a, Profiler.Function b) {
            return b.iticks + b.dticks - a.iticks + a.dticks;
          }
        });
    p.println("Functions sorted by direct and indirect ticks:");
    for (Function fn : funs) {
      p.print("    ");
      String nm = fn.cl + "." + fn.nm;
      p.print(nm);
      for (int i = nm.length(); i < 60; i++)
        p.print(" "); 
      p.printf("%6d (%5.2f%%)", new Object[] { Integer.valueOf(fn.iticks + fn.dticks), Double.valueOf(100.0D * (fn.iticks + fn.dticks) / this.nticks) });
      p.println();
    } 
    p.println();
    p.println("Per-function time spent in callees:");
    for (Function fn : funs) {
      p.printf("  %s.%s\n", new Object[] { fn.cl, fn.nm });
      List<Map.Entry<Function, Integer>> cfs = new ArrayList<>(fn.tticks.entrySet());
      if (fn.dticks > 0)
        cfs.add(new AbstractMap.SimpleEntry<>(null, Integer.valueOf(fn.dticks))); 
      Collections.sort(cfs, new Comparator<Map.Entry<Function, Integer>>() {
            public int compare(Map.Entry<Profiler.Function, Integer> a, Map.Entry<Profiler.Function, Integer> b) {
              return ((Integer)b.getValue()).intValue() - ((Integer)a.getValue()).intValue();
            }
          });
      for (Map.Entry<Function, Integer> cf : cfs) {
        String nm;
        p.print("    ");
        if (cf.getKey() == null) {
          nm = "<direct ticks>";
        } else {
          nm = ((Function)cf.getKey()).cl + "." + ((Function)cf.getKey()).nm;
        } 
        p.print(nm);
        for (int i = nm.length(); i < 60; i++)
          p.print(" "); 
        p.printf("%6d (%5.2f%%)", new Object[] { cf.getValue(), Double.valueOf(100.0D * ((Integer)cf.getValue()).intValue() / (fn.dticks + fn.iticks)) });
        p.println();
      } 
      p.println();
    } 
    p.println();
    p.println("Per-function time spent by caller:");
    for (Function fn : funs) {
      p.printf("  %s.%s\n", new Object[] { fn.cl, fn.nm });
      List<Map.Entry<Function, Integer>> cfs = new ArrayList<>(fn.fticks.entrySet());
      Collections.sort(cfs, new Comparator<Map.Entry<Function, Integer>>() {
            public int compare(Map.Entry<Profiler.Function, Integer> a, Map.Entry<Profiler.Function, Integer> b) {
              return ((Integer)b.getValue()).intValue() - ((Integer)a.getValue()).intValue();
            }
          });
      for (Map.Entry<Function, Integer> cf : cfs) {
        p.print("    ");
        String nm = ((Function)cf.getKey()).cl + "." + ((Function)cf.getKey()).nm;
        p.print(nm);
        for (int i = nm.length(); i < 60; i++)
          p.print(" "); 
        p.printf("%6d (%5.2f%%)", new Object[] { cf.getValue(), Double.valueOf(100.0D * ((Integer)cf.getValue()).intValue() / (fn.dticks + fn.iticks)) });
        p.println();
      } 
      p.println();
    } 
  }
  
  public void output(String path) {
    try {
      OutputStream out = new FileOutputStream(path);
      try {
        output(out);
      } finally {
        out.close();
      } 
    } catch (IOException e) {
      e.printStackTrace(System.out);
    } 
  }
  
  private static class Loop extends HackThread {
    private final Collection<Profiler> current = new LinkedList<>();
    
    Loop() {
      super("Profiling thread");
      setDaemon(true);
    }
    
    public void run() {
      try {
        while (true) {
          Collection<Profiler> copy;
          Thread.sleep(100L);
          synchronized (this.current) {
            copy = new ArrayList<>(this.current);
          } 
          for (Profiler p : copy) {
            StackTraceElement[] bt = p.th.getStackTrace();
            if (!p.enabled)
              continue; 
            p.tick(bt);
          } 
        } 
      } catch (InterruptedException copy) {
        return;
      } 
    }
  }
}
