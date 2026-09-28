package haven.glsl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Context {
  public final Map<Symbol, String> symtab = new HashMap<>();
  
  public final Map<String, Symbol> rsymtab = new HashMap<>();
  
  public int symgen = 1;
  
  public List<Toplevel> vardefs = new LinkedList<>();
  
  public List<Toplevel> fundefs = new LinkedList<>();
  
  public Set<String> exts = new HashSet<>();
  
  public void output(Output out) {
    out.write("#version 120\n\n");
    for (String ext : this.exts)
      out.write("#extension " + ext + ": require\n"); 
    for (Toplevel tl : this.vardefs)
      tl.output(out); 
    if (!this.vardefs.isEmpty())
      out.write("\n"); 
    for (Toplevel tl : this.fundefs)
      tl.output(out); 
  }
}
