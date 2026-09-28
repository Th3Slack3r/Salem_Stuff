package haven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.media.opengl.GL2;

public class GLProgram implements Serializable {
  public final Collection<GLShader> shaders;
  
  private transient ProgOb glp;
  
  private final Map<String, Integer> umap;
  
  private final Map<String, Integer> amap;
  
  public GLProgram(Collection<GLShader> shaders) {
    this.umap = new IdentityHashMap<>();
    this.amap = new IdentityHashMap<>();
    this.shaders = new ArrayList<>(shaders);
  }
  
  private static Collection<GLShader> collapse(GLShader[][] shaders) {
    Collection<GLShader> sc = new ArrayList<>();
    for (int i = 0; i < shaders.length; i++) {
      if (shaders[i] != null)
        for (int o = 0; o < (shaders[i]).length; o++)
          sc.add(shaders[i][o]);  
    } 
    return sc;
  }
  
  public GLProgram(GLShader[][] shaders) {
    this(collapse(shaders));
    makemains(this.shaders);
  }
  
  private static void makemains(Collection<GLShader> shaders) {
    List<GLShader.VertexShader> vs = new ArrayList<>();
    List<GLShader.FragmentShader> fs = new ArrayList<>();
    for (GLShader s : shaders) {
      if (s instanceof GLShader.VertexShader) {
        vs.add((GLShader.VertexShader)s);
        continue;
      } 
      if (s instanceof GLShader.FragmentShader)
        fs.add((GLShader.FragmentShader)s); 
    } 
    shaders.add(GLShader.VertexShader.makemain(vs));
    shaders.add(GLShader.FragmentShader.makemain(fs));
  }
  
  public static class ProgOb extends GLObject {
    public final int id;
    
    public ProgOb(GL2 gl) {
      super(gl);
      this.id = gl.glCreateProgramObjectARB();
    }
    
    public void delete() {
      this.gl.glDeleteObjectARB(this.id);
    }
    
    public void link(GLProgram prog) {
      for (GLShader sh : prog.shaders)
        this.gl.glAttachShader(this.id, sh.glid(this.gl)); 
      this.gl.glLinkProgram(this.id);
      int[] buf = { 0 };
      this.gl.glGetObjectParameterivARB(this.id, 35714, buf, 0);
      if (buf[0] != 1) {
        String info = null;
        this.gl.glGetObjectParameterivARB(this.id, 35716, buf, 0);
        if (buf[0] > 0) {
          byte[] logbuf = new byte[buf[0]];
          this.gl.glGetInfoLogARB(this.id, logbuf.length, buf, 0, logbuf, 0);
          info = new String(logbuf, 0, buf[0]);
        } 
        throw new GLProgram.ProgramException("Failed to link GL program", prog, info);
      } 
    }
    
    public int uniform(String name) {
      int r = this.gl.glGetUniformLocationARB(this.id, name);
      if (r < 0)
        throw new RuntimeException("Unknown uniform name: " + name); 
      return r;
    }
    
    public int attrib(String name) {
      int r = this.gl.glGetAttribLocation(this.id, name);
      if (r < 0)
        throw new RuntimeException("Unknown uniform name: " + name); 
      return r;
    }
  }
  
  public static class ProgramException extends RuntimeException {
    public final GLProgram program;
    
    public final String info;
    
    public ProgramException(String msg, GLProgram program, String info) {
      super(msg);
      this.program = program;
      this.info = info;
    }
    
    public String toString() {
      if (this.info == null)
        return super.toString(); 
      return super.toString() + "\nLog:\n" + this.info;
    }
  }
  
  public void apply(GOut g) {
    synchronized (this) {
      if (this.glp != null && this.glp.gl != g.gl)
        dispose(); 
      if (this.glp == null) {
        this.glp = new ProgOb(g.gl);
        this.glp.link(this);
      } 
      g.gl.glUseProgramObjectARB(this.glp.id);
    } 
  }
  
  public void dispose() {
    synchronized (this) {
      if (this.glp != null) {
        ProgOb cur = this.glp;
        this.glp = null;
        cur.dispose();
      } 
      this.umap.clear();
      this.amap.clear();
    } 
  }
  
  public int uniform(String name) {
    Integer r = this.umap.get(name);
    if (r == null)
      this.umap.put(name, r = new Integer(this.glp.uniform(name))); 
    return r.intValue();
  }
  
  public int attrib(String name) {
    Integer r = this.amap.get(name);
    if (r == null)
      this.amap.put(name, r = new Integer(this.glp.attrib(name))); 
    return r.intValue();
  }
}
