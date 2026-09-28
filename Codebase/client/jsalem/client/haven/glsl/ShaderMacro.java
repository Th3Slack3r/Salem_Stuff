package haven.glsl;

import haven.GLProgram;
import haven.GLShader;
import haven.GLState;
import haven.GOut;
import java.io.StringWriter;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface ShaderMacro {
   void modify(ProgramContext var1);

   public static class Program extends GLProgram {
      public static boolean dumpall = false;
      public final transient ProgramContext built;
      private final transient int[][] automask;
      private final transient Uniform.AutoApply[] auto;
      private final transient boolean[] adirty;
      private transient int[] autolocs;
      private final Map<Uniform, Integer> umap = new IdentityHashMap<>();
      private final Map<Attribute, Integer> amap = new IdentityHashMap<>();

      private static Collection<GLShader> build(ProgramContext prog) {
         Collection<GLShader> ret = new LinkedList<>();
         StringWriter fbuf = new StringWriter();
         prog.fctx.construct(fbuf);
         ret.add(new GLShader.FragmentShader(fbuf.toString()));
         StringWriter vbuf = new StringWriter();
         prog.vctx.construct(vbuf);
         ret.add(new GLShader.VertexShader(vbuf.toString()));
         return ret;
      }

      public Program(ProgramContext ctx) {
         super(build(ctx));
         this.built = ctx;
         List<Uniform.AutoApply> auto = new LinkedList<>();

         for (Uniform var : ctx.uniforms) {
            if (var instanceof Uniform.AutoApply) {
               auto.add((Uniform.AutoApply)var);
            }
         }

         this.auto = auto.toArray(new Uniform.AutoApply[0]);
         this.adirty = new boolean[this.auto.length];
         int max = -1;

         for (Uniform.AutoApply autox : this.auto) {
            for (GLState.Slot slot : autox.deps) {
               max = Math.max(max, slot.id);
            }
         }

         LinkedList<Integer>[] buf = new LinkedList[max + 1];

         for (int i = 0; i < this.auto.length; i++) {
            for (GLState.Slot slot : this.auto[i].deps) {
               if (buf[slot.id] == null) {
                  buf[slot.id] = new LinkedList<>();
               }

               buf[slot.id].add(i);
            }
         }

         this.automask = new int[max + 1][];

         for (int i = 0; i <= max; i++) {
            if (buf[i] == null) {
               this.automask[i] = new int[0];
            } else {
               this.automask[i] = new int[buf[i].size()];
               int o = 0;

               for (int s : buf[i]) {
                  this.automask[i][o++] = s;
               }
            }
         }
      }

      public void adirty(GLState.Slot slot) {
         if (slot.id < this.automask.length) {
            for (int i : this.automask[slot.id]) {
               this.adirty[i] = true;
            }
         }
      }

      public void autoapply(GOut g, boolean all) {
         if (this.autolocs == null) {
            this.autolocs = new int[this.auto.length];

            for (int i = 0; i < this.auto.length; i++) {
               this.autolocs[i] = this.uniform(this.auto[i]);
            }
         }

         for (int i = 0; i < this.auto.length; i++) {
            if (all || this.adirty[i]) {
               this.auto[i].apply(g, this.autolocs[i]);
            }

            this.adirty[i] = false;
         }
      }

      public static ShaderMacro.Program build(Collection<ShaderMacro> mods) {
         ProgramContext prog = new ProgramContext();

         for (ShaderMacro mod : mods) {
            mod.modify(prog);
         }

         ShaderMacro.Program ret = new ShaderMacro.Program(prog);
         if (dumpall || prog.dump) {
            System.err.println(mods + ": ");

            for (GLShader sh : ret.shaders) {
               System.err.println("---> " + sh + ": ");
               System.err.print(sh.source);
            }

            System.err.println();
            System.err.println("-------- " + ret);
            System.err.println();
         }

         return ret;
      }

      @Override
      public void dispose() {
         synchronized (this) {
            super.dispose();
            this.umap.clear();
            this.amap.clear();
         }
      }

      public int cuniform(Uniform var) {
         Integer r = this.umap.get(var);
         if (r == null) {
            String nm = this.built.symtab.get(var.name);
            if (nm == null) {
               r = new Integer(-1);
            } else {
               r = new Integer(this.uniform(nm));
            }

            this.umap.put(var, r);
         }

         return r;
      }

      public int uniform(Uniform var) {
         int r = this.cuniform(var);
         if (r < 0) {
            throw new GLProgram.ProgramException("Uniform not found in symtab: " + var, this, null);
         } else {
            return r;
         }
      }

      public int cattrib(Attribute var) {
         Integer r = this.amap.get(var);
         if (r == null) {
            String nm = this.built.symtab.get(var.name);
            if (nm == null) {
               r = new Integer(-1);
            } else {
               r = new Integer(this.attrib(nm));
            }

            this.amap.put(var, r);
         }

         return r;
      }

      public int attrib(Attribute var) {
         int r = this.cattrib(var);
         if (r < 0) {
            throw new GLProgram.ProgramException("Attribute not found in symtab: " + var, this, null);
         } else {
            return r;
         }
      }
   }
}
