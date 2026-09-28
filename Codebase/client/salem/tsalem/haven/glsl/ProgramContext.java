package haven.glsl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ProgramContext {
  public final VertexContext vctx;
  
  public final FragmentContext fctx;
  
  public final Set<Uniform> uniforms = new HashSet<>();
  
  public final Set<Attribute> attribs = new HashSet<>();
  
  public final Map<Symbol, String> symtab = new HashMap<>();
  
  public final Map<String, Symbol> rsymtab = new HashMap<>();
  
  public int symgen = 1;
  
  public boolean dump = false;
  
  private final Collection<Object> mods = new LinkedList();
  
  public static final Variable gl_LightSource = new Variable.Implicit(new Array(Struct.gl_LightSourceParameters), new Symbol.Fix("gl_LightSource"));
  
  public static final Variable gl_FrontMaterial = new Variable.Implicit(Struct.gl_MaterialParameters, new Symbol.Fix("gl_FrontMaterial"));
  
  public ProgramContext() {
    this.vctx = new VertexContext(this);
    this.fctx = new FragmentContext(this);
  }
  
  public void module(Object mod) {
    this.mods.add(mod);
  }
  
  public <T> T getmod(Class<T> cl) {
    T ret = null;
    for (Object mod : this.mods) {
      if (cl.isInstance(mod)) {
        if (ret == null) {
          ret = cl.cast(mod);
          continue;
        } 
        throw new RuntimeException("multiple modules of " + cl + " installed: " + ret + " and " + mod);
      } 
    } 
    return ret;
  }
}
