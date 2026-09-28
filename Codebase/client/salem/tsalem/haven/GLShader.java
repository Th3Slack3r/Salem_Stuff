package haven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public abstract class GLShader implements Serializable {
  public final String source;
  
  public final String header;
  
  private transient ShaderOb gls;
  
  public GLShader(String source, String header) {
    this.source = source;
    this.header = header;
  }
  
  public static class ShaderOb extends GLObject {
    public final int id;
    
    public ShaderOb(GL2 gl, int type) {
      super(gl);
      this.id = gl.glCreateShaderObjectARB(type);
      GOut.checkerr((GL)gl);
    }
    
    protected void delete() {
      this.gl.glDeleteObjectARB(this.id);
    }
    
    public void compile(GLShader sh) {
      this.gl.glShaderSourceARB(this.id, 1, new String[] { sh.source }, new int[] { sh.source.length() }, 0);
      this.gl.glCompileShaderARB(this.id);
      int[] buf = { 0 };
      this.gl.glGetObjectParameterivARB(this.id, 35713, buf, 0);
      if (buf[0] != 1) {
        String info = null;
        this.gl.glGetObjectParameterivARB(this.id, 35716, buf, 0);
        if (buf[0] > 0) {
          byte[] logbuf = new byte[buf[0]];
          this.gl.glGetInfoLogARB(this.id, logbuf.length, buf, 0, logbuf, 0);
          info = new String(logbuf, 0, buf[0]);
        } 
        throw new GLShader.ShaderException("Failed to compile shader", sh, info);
      } 
    }
  }
  
  public static class ShaderException extends RuntimeException {
    public final GLShader shader;
    
    public final String info;
    
    public ShaderException(String msg, GLShader shader, String info) {
      super(msg);
      this.shader = shader;
      this.info = info;
    }
    
    public String toString() {
      if (this.info == null)
        return super.toString(); 
      return super.toString() + "\nLog:\n" + this.info;
    }
  }
  
  public static abstract class Splitter {
    private final BufferedReader in;
    
    public final StringBuilder main = new StringBuilder();
    
    public StringBuilder buf = this.main;
    
    public Splitter(Reader r) {
      this.in = new BufferedReader(r);
    }
    
    public Splitter(InputStream i) {
      this(new InputStreamReader(i, Utils.ascii));
    }
    
    public void parse() throws IOException {
      String ln;
      while ((ln = this.in.readLine()) != null) {
        if (ln.startsWith("#pp ")) {
          String d = ln.substring(4).trim();
          String a = "";
          int p = d.indexOf(' ');
          if (p >= 0) {
            a = d.substring(p + 1);
            d = d.substring(0, p).trim();
          } 
          d = d.intern();
          directive(d, a);
          continue;
        } 
        this.buf.append(ln + "\n");
      } 
    }
    
    public abstract void directive(String param1String1, String param1String2);
  }
  
  public static class VertexShader extends GLShader {
    public final String entry;
    
    public final String[] args;
    
    public final int order;
    
    public VertexShader(String source, String header, String entry, int order, String... args) {
      super(source, header);
      this.entry = entry;
      this.order = order;
      this.args = args;
    }
    
    public VertexShader(String source) {
      this(source, "", null, 0, new String[0]);
    }
    
    protected GLShader.ShaderOb create(GL2 gl) {
      GLShader.ShaderOb r = new GLShader.ShaderOb(gl, 35633);
      r.compile(this);
      return r;
    }
    
    private boolean uses(String arg) {
      for (String a : this.args) {
        if (a.equals(arg))
          return true; 
      } 
      return false;
    }
    
    private String call() {
      String ret = this.entry + "(";
      boolean f = true;
      for (String arg : this.args) {
        if (!f)
          ret = ret + ", "; 
        ret = ret + arg;
        f = false;
      } 
      ret = ret + ")";
      return ret;
    }
    
    private static final Comparator<VertexShader> cmp = new Comparator<VertexShader>() {
        public int compare(GLShader.VertexShader a, GLShader.VertexShader b) {
          return a.order - b.order;
        }
      };
    
    public static VertexShader makemain(List<VertexShader> shaders) {
      StringBuilder buf = new StringBuilder();
      Collections.sort(shaders, cmp);
      for (VertexShader sh : shaders)
        buf.append(sh.header + "\n"); 
      buf.append("\n");
      buf.append("void main()\n{\n");
      buf.append("    vec4 fcol = gl_Color;\n");
      buf.append("    vec4 bcol = gl_Color;\n");
      buf.append("    vec4 objv = gl_Vertex;\n");
      buf.append("    vec3 objn = gl_Normal;\n");
      int i = 0;
      for (; i < shaders.size(); i++) {
        VertexShader sh = shaders.get(i);
        if (sh.uses("eyev") || sh.uses("eyen"))
          break; 
        buf.append("    " + sh.call() + ";\n");
      } 
      buf.append("    vec4 eyev = gl_ModelViewMatrix * objv;\n");
      buf.append("    vec3 eyen = gl_NormalMatrix * objn;\n");
      for (; i < shaders.size(); i++) {
        VertexShader sh = shaders.get(i);
        buf.append("    " + sh.call() + ";\n");
      } 
      buf.append("    gl_FrontColor = fcol;\n");
      buf.append("    gl_Position = gl_ProjectionMatrix * eyev;\n");
      buf.append("}\n");
      return new VertexShader(buf.toString());
    }
    
    public static VertexShader parse(Reader in) throws IOException {
      class VSplitter extends GLShader.Splitter {
        StringBuilder header = new StringBuilder();
        
        String entry;
        
        String[] args;
        
        int order = 0;
        
        VSplitter(Reader in) {
          super(in);
        }
        
        public void directive(String d, String a) {
          if (d == "header") {
            this.buf = this.header;
          } else if (d == "main") {
            this.buf = this.main;
          } else if (d == "order") {
            this.order = Integer.parseInt(a);
          } else if (d == "entry") {
            String[] args = a.split(" +");
            this.entry = args[0];
            this.args = new String[args.length - 1];
            for (int i = 1, o = 0; i < args.length; i++, o++)
              this.args[o] = args[i]; 
          } 
        }
      };
      VSplitter p = new VSplitter(in);
      p.parse();
      if (p.entry == null)
        throw new RuntimeException("No entry specified in shader source."); 
      return new VertexShader(p.main.toString(), p.header.toString(), p.entry, p.order, p.args);
    }
    
    public static VertexShader load(Class<?> base, String name) {
      InputStream in = base.getResourceAsStream(name);
      try {
        try {
          return parse(new InputStreamReader(in, Utils.ascii));
        } finally {
          in.close();
        } 
      } catch (IOException e) {
        throw new RuntimeException(e);
      } 
    }
  }
  
  public static class FragmentShader extends GLShader {
    public final String entry;
    
    public final int order;
    
    public FragmentShader(String source, String header, String entry, int order) {
      super(source, header);
      this.entry = entry;
      this.order = order;
    }
    
    public FragmentShader(String source) {
      this(source, "", null, 0);
    }
    
    protected GLShader.ShaderOb create(GL2 gl) {
      GLShader.ShaderOb r = new GLShader.ShaderOb(gl, 35632);
      r.compile(this);
      return r;
    }
    
    private static final Comparator<FragmentShader> cmp = new Comparator<FragmentShader>() {
        public int compare(GLShader.FragmentShader a, GLShader.FragmentShader b) {
          return a.order - b.order;
        }
      };
    
    public static FragmentShader makemain(List<FragmentShader> shaders) {
      StringBuilder buf = new StringBuilder();
      Collections.sort(shaders, cmp);
      for (FragmentShader sh : shaders)
        buf.append(sh.header + "\n"); 
      buf.append("\n");
      buf.append("void main()\n{\n");
      buf.append("    vec4 res = gl_Color;\n");
      for (FragmentShader sh : shaders)
        buf.append("    " + sh.entry + "(res);\n"); 
      buf.append("    gl_FragColor = res;\n");
      buf.append("}\n");
      return new FragmentShader(buf.toString());
    }
    
    public static FragmentShader parse(Reader in) throws IOException {
      class FSplitter extends GLShader.Splitter {
        StringBuilder header = new StringBuilder();
        
        String entry;
        
        int order = 0;
        
        FSplitter(Reader in) {
          super(in);
        }
        
        public void directive(String d, String a) {
          if (d == "header") {
            this.buf = this.header;
          } else if (d == "main") {
            this.buf = this.main;
          } else if (d == "order") {
            this.order = Integer.parseInt(a);
          } else if (d == "entry") {
            String[] args = a.split(" +");
            this.entry = args[0];
          } 
        }
      };
      FSplitter p = new FSplitter(in);
      p.parse();
      if (p.entry == null)
        throw new RuntimeException("No entry specified in shader source."); 
      return new FragmentShader(p.main.toString(), p.header.toString(), p.entry, p.order);
    }
    
    public static FragmentShader load(Class<?> base, String name) {
      InputStream in = base.getResourceAsStream(name);
      try {
        try {
          return parse(new InputStreamReader(in, Utils.ascii));
        } finally {
          in.close();
        } 
      } catch (IOException e) {
        throw new RuntimeException(e);
      } 
    }
  }
  
  public int glid(GL2 gl) {
    if (this.gls != null && this.gls.gl != gl) {
      this.gls.dispose();
      this.gls = null;
    } 
    if (this.gls == null)
      this.gls = create(gl); 
    return this.gls.id;
  }
  
  protected abstract ShaderOb create(GL2 paramGL2);
}
