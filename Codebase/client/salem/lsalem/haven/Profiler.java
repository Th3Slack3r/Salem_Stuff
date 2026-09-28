package haven;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public class Profiler {
   private static Profiler.Loop loop;
   public final Thread th;
   private boolean enabled;
   private Map<Profiler.Function, Profiler.Function> funs = new HashMap<>();
   private int nticks = 0;

   public Profiler(Thread th) {
      this.th = th;
   }

   public Profiler() {
      this(Thread.currentThread());
   }

   public void enable() {
      if (Thread.currentThread() != this.th) {
         throw new RuntimeException("Enabled from non-owning thread");
      } else if (this.enabled) {
         throw new RuntimeException("Enabled when already enabled");
      } else {
         if (loop == null) {
            synchronized (Profiler.Loop.class) {
               if (loop == null) {
                  loop = new Profiler.Loop();
                  loop.start();
               }
            }
         }

         synchronized (loop.current) {
            loop.current.add(this);
         }

         this.enabled = true;
      }
   }

   public void disable() {
      if (Thread.currentThread() != this.th) {
         throw new RuntimeException("Disabled from non-owning thread");
      } else if (!this.enabled) {
         throw new RuntimeException("Disabled when already disabled");
      } else {
         synchronized (loop.current) {
            loop.current.remove(this);
         }

         this.enabled = false;
      }
   }

   private Profiler.Function getfun(StackTraceElement f) {
      Profiler.Function key = new Profiler.Function(f);
      Profiler.Function ret = this.funs.get(key);
      if (ret == null) {
         ret = key;
         this.funs.put(key, key);
      }

      return ret;
   }

   protected void tick(StackTraceElement[] bt) {
      this.nticks++;
      Profiler.Function pf = this.getfun(bt[0]);
      pf.dticks++;
      if (pf.lticks.containsKey(bt[0].getLineNumber())) {
         pf.lticks.put(bt[0].getLineNumber(), pf.lticks.get(bt[0].getLineNumber()) + 1);
      } else {
         pf.lticks.put(bt[0].getLineNumber(), 1);
      }

      for (int i = 1; i < bt.length; i++) {
         StackTraceElement f = bt[i];
         Profiler.Function fn = this.getfun(f);
         fn.iticks++;
         if (fn.tticks.containsKey(pf)) {
            fn.tticks.put(pf, fn.tticks.get(pf) + 1);
         } else {
            fn.tticks.put(pf, 1);
         }

         if (pf.fticks.containsKey(fn)) {
            pf.fticks.put(fn, pf.fticks.get(fn) + 1);
         } else {
            pf.fticks.put(fn, 1);
         }

         pf = fn;
      }

      System.err.print(".");
   }

   public void outputlp(OutputStream out, String cl, String fnm) {
      Profiler.Function fn = this.funs.get(new Profiler.Function(cl, fnm));
      if (fn != null) {
         Map<Integer, Integer> lt = fn.lticks;
         PrintStream p = new PrintStream(out);
         List<Integer> lines = new ArrayList<>(lt.keySet());
         Collections.sort(lines);

         for (int ln : lines) {
            p.printf("%d: %d\n", ln, lt.get(ln));
         }

         p.println();
      }
   }

   public void output(OutputStream out) {
      PrintStream p = new PrintStream(out);
      List<Profiler.Function> funs = new ArrayList<>(this.funs.keySet());
      Collections.sort(funs, new Comparator<Profiler.Function>() {
         public int compare(Profiler.Function a, Profiler.Function b) {
            return b.dticks - a.dticks;
         }
      });
      p.println("Functions sorted by direct ticks:");

      for (Profiler.Function fn : funs) {
         if (fn.dticks >= 1) {
            p.print("    ");
            String nm = fn.cl + "." + fn.nm;
            p.print(nm);

            for (int i = nm.length(); i < 60; i++) {
               p.print(" ");
            }

            p.printf("%6d (%5.2f%%)", fn.dticks, 100.0 * fn.dticks / this.nticks);
            p.println();
         }
      }

      p.println();
      Collections.sort(funs, new Comparator<Profiler.Function>() {
         public int compare(Profiler.Function a, Profiler.Function b) {
            return b.iticks + b.dticks - (a.iticks + a.dticks);
         }
      });
      p.println("Functions sorted by direct and indirect ticks:");

      for (Profiler.Function fnx : funs) {
         p.print("    ");
         String nm = fnx.cl + "." + fnx.nm;
         p.print(nm);

         for (int i = nm.length(); i < 60; i++) {
            p.print(" ");
         }

         p.printf("%6d (%5.2f%%)", fnx.iticks + fnx.dticks, 100.0 * (fnx.iticks + fnx.dticks) / this.nticks);
         p.println();
      }

      p.println();
      p.println("Per-function time spent in callees:");

      for (Profiler.Function fnx : funs) {
         p.printf("  %s.%s\n", fnx.cl, fnx.nm);
         List<Entry<Profiler.Function, Integer>> cfs = new ArrayList<>(fnx.tticks.entrySet());
         if (fnx.dticks > 0) {
            cfs.add(new SimpleEntry<>(null, fnx.dticks));
         }

         Collections.sort(cfs, new Comparator<Entry<Profiler.Function, Integer>>() {
            public int compare(Entry<Profiler.Function, Integer> a, Entry<Profiler.Function, Integer> b) {
               return b.getValue() - a.getValue();
            }
         });

         for (Entry<Profiler.Function, Integer> cf : cfs) {
            p.print("    ");
            String nm;
            if (cf.getKey() == null) {
               nm = "<direct ticks>";
            } else {
               nm = cf.getKey().cl + "." + cf.getKey().nm;
            }

            p.print(nm);

            for (int i = nm.length(); i < 60; i++) {
               p.print(" ");
            }

            p.printf("%6d (%5.2f%%)", cf.getValue(), 100.0 * cf.getValue().intValue() / (fnx.dticks + fnx.iticks));
            p.println();
         }

         p.println();
      }

      p.println();
      p.println("Per-function time spent by caller:");

      for (Profiler.Function fnx : funs) {
         p.printf("  %s.%s\n", fnx.cl, fnx.nm);
         List<Entry<Profiler.Function, Integer>> cfs = new ArrayList<>(fnx.fticks.entrySet());
         Collections.sort(cfs, new Comparator<Entry<Profiler.Function, Integer>>() {
            public int compare(Entry<Profiler.Function, Integer> a, Entry<Profiler.Function, Integer> b) {
               return b.getValue() - a.getValue();
            }
         });

         for (Entry<Profiler.Function, Integer> cf : cfs) {
            p.print("    ");
            String nm = cf.getKey().cl + "." + cf.getKey().nm;
            p.print(nm);

            for (int i = nm.length(); i < 60; i++) {
               p.print(" ");
            }

            p.printf("%6d (%5.2f%%)", cf.getValue(), 100.0 * cf.getValue().intValue() / (fnx.dticks + fnx.iticks));
            p.println();
         }

         p.println();
      }
   }

   public void output(String path) {
      try {
         OutputStream out = new FileOutputStream(path);

         try {
            this.output(out);
         } finally {
            out.close();
         }
      } catch (IOException var7) {
         var7.printStackTrace(System.out);
      }
   }

   public static class Function {
      public final String cl;
      public final String nm;
      public int dticks;
      public int iticks;
      public Map<Profiler.Function, Integer> tticks = new HashMap<>();
      public Map<Profiler.Function, Integer> fticks = new HashMap<>();
      public Map<Integer, Integer> lticks = new HashMap<>();
      private int hc = 0;

      public Function(String cl, String nm) {
         this.cl = cl;
         this.nm = nm;
      }

      public Function(StackTraceElement f) {
         this(f.getClassName(), f.getMethodName());
      }

      @Override
      public boolean equals(Object bp) {
         if (!(bp instanceof Profiler.Function)) {
            return false;
         } else {
            Profiler.Function b = (Profiler.Function)bp;
            return b.cl.equals(this.cl) && b.nm.equals(this.nm);
         }
      }

      @Override
      public int hashCode() {
         if (this.hc == 0) {
            this.hc = this.cl.hashCode() * 31 + this.nm.hashCode();
         }

         return this.hc;
      }
   }

   private static class Loop extends HackThread {
      private Collection<Profiler> current = new LinkedList<>();

      Loop() {
         super("Profiling thread");
         this.setDaemon(true);
      }

      @Override
      public void run() {
         try {
            while (true) {
               Thread.sleep(100L);
               Collection<Profiler> copy;
               synchronized (this.current) {
                  copy = new ArrayList<>(this.current);
               }

               for (Profiler p : copy) {
                  StackTraceElement[] bt = p.th.getStackTrace();
                  if (p.enabled) {
                     p.tick(bt);
                  }
               }
            }
         } catch (InterruptedException var6) {
         }
      }
   }
}
