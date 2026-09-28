package haven.glsl;

public abstract class Symbol {
   public abstract String name(Context var1);

   public static class Fix extends Symbol {
      public final String name;

      public Fix(String name) {
         this.name = name;
      }

      @Override
      public String name(Context ctx) {
         Symbol p = ctx.rsymtab.get(this.name);
         if (p == null) {
            ctx.symtab.put(this, this.name);
            ctx.rsymtab.put(this.name, this);
         } else if (p != this) {
            throw new RuntimeException("Name conflict for fix symbol `" + this.name + "'");
         }

         return this.name;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }

   public static class Gen extends Symbol {
      public final String prefix;

      public Gen(String prefix) {
         this.prefix = prefix;
      }

      public Gen() {
         this("g");
      }

      @Override
      public String name(Context ctx) {
         String nm = ctx.symtab.get(this);
         if (nm == null) {
            nm = this.prefix + ctx.symgen++;
            if (ctx.rsymtab.get(nm) != null) {
               throw new RuntimeException("Name conflict for gensym");
            }

            ctx.symtab.put(this, nm);
            ctx.rsymtab.put(nm, this);
         }

         return nm;
      }

      @Override
      public String toString() {
         return "#:" + this.prefix;
      }
   }

   public static class Shared extends Symbol {
      public final String prefix;

      public Shared(String prefix) {
         this.prefix = prefix;
      }

      public Shared() {
         this("s_g");
      }

      @Override
      public String name(Context ctx) {
         if (!(ctx instanceof ShaderContext)) {
            throw new ClassCastException("Program-shared symbols cannot be used outside a program context");
         } else {
            ProgramContext prog = ((ShaderContext)ctx).prog;
            String nm = ctx.symtab.get(this);
            if (nm == null) {
               nm = prog.symtab.get(this);
               if (nm == null) {
                  nm = this.prefix + prog.symgen++;
                  if (prog.rsymtab.get(nm) != null) {
                     throw new RuntimeException("Name conflict for shared symbol");
                  }

                  prog.symtab.put(this, nm);
                  prog.rsymtab.put(nm, this);
               }

               if (ctx.rsymtab.get(nm) != null) {
                  throw new RuntimeException("Name conflict for shared symbol");
               }

               ctx.symtab.put(this, nm);
               ctx.rsymtab.put(nm, this);
            }

            return nm;
         }
      }

      @Override
      public String toString() {
         return "#s:" + this.prefix;
      }
   }
}
