package haven.glsl;

import haven.GLProgram;
import haven.GLShader;
import haven.GLState;
import haven.GOut;
import java.io.StringWriter;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface ShaderMacro {
  void modify(ProgramContext paramProgramContext);
  
  public static class Program extends GLProgram {
    public static boolean dumpall = false;
    
    public final transient ProgramContext built;
    
    private final transient int[][] automask;
    
    private final transient Uniform.AutoApply[] auto;
    
    private final transient boolean[] adirty;
    
    private transient int[] autolocs;
    
    private final Map<Uniform, Integer> umap;
    
    private final Map<Attribute, Integer> amap;
    
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
      this.umap = new IdentityHashMap<>();
      this.amap = new IdentityHashMap<>();
      this.built = ctx;
      List<Uniform.AutoApply> auto = new LinkedList<>();
      for (Uniform var : ctx.uniforms) {
        if (var instanceof Uniform.AutoApply)
          auto.add((Uniform.AutoApply)var); 
      } 
      this.auto = auto.<Uniform.AutoApply>toArray(new Uniform.AutoApply[0]);
      this.adirty = new boolean[this.auto.length];
      int max = -1;
      for (Uniform.AutoApply autoApply : this.auto) {
        for (GLState.Slot slot : autoApply.deps)
          max = Math.max(max, slot.id); 
      } 
      LinkedList[] arrayOfLinkedList = new LinkedList[max + 1];
      int i;
      for (i = 0; i < this.auto.length; i++) {
        for (GLState.Slot slot : (this.auto[i]).deps) {
          if (arrayOfLinkedList[slot.id] == null)
            arrayOfLinkedList[slot.id] = new LinkedList(); 
          arrayOfLinkedList[slot.id].add(Integer.valueOf(i));
        } 
      } 
      this.automask = new int[max + 1][];
      for (i = 0; i <= max; i++) {
        if (arrayOfLinkedList[i] == null) {
          this.automask[i] = new int[0];
        } else {
          this.automask[i] = new int[arrayOfLinkedList[i].size()];
          int o = 0;
          for (Iterator<?> iterator = arrayOfLinkedList[i].iterator(); iterator.hasNext(); ) {
            int s = ((Integer)iterator.next()).intValue();
            this.automask[i][o++] = s;
          } 
        } 
      } 
    }
    
    public void adirty(GLState.Slot slot) {
      if (slot.id < this.automask.length)
        for (int i : this.automask[slot.id])
          this.adirty[i] = true;  
    }
    
    public void autoapply(GOut g, boolean all) {
      if (this.autolocs == null) {
        this.autolocs = new int[this.auto.length];
        for (int j = 0; j < this.auto.length; j++)
          this.autolocs[j] = uniform(this.auto[j]); 
      } 
      for (int i = 0; i < this.auto.length; i++) {
        if (all || this.adirty[i])
          this.auto[i].apply(g, this.autolocs[i]); 
        this.adirty[i] = false;
      } 
    }
    
    public static Program build(Collection<ShaderMacro> mods) {
      ProgramContext prog = new ProgramContext();
      for (ShaderMacro mod : mods)
        mod.modify(prog); 
      Program ret = new Program(prog);
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
          r = new Integer(uniform(nm));
        } 
        this.umap.put(var, r);
      } 
      return r.intValue();
    }
    
    public int uniform(Uniform var) {
      int r = cuniform(var);
      if (r < 0)
        throw new GLProgram.ProgramException("Uniform not found in symtab: " + var, this, null); 
      return r;
    }
    
    public int cattrib(Attribute var) {
      Integer r = this.amap.get(var);
      if (r == null) {
        String nm = this.built.symtab.get(var.name);
        if (nm == null) {
          r = new Integer(-1);
        } else {
          r = new Integer(attrib(nm));
        } 
        this.amap.put(var, r);
      } 
      return r.intValue();
    }
    
    public int attrib(Attribute var) {
      int r = cattrib(var);
      if (r < 0)
        throw new GLProgram.ProgramException("Attribute not found in symtab: " + var, this, null); 
      return r;
    }
  }
}
