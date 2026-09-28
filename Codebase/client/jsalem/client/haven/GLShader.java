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
import javax.media.opengl.GL2;

public abstract class GLShader implements Serializable {
   public final String source;
   public final String header;
   private transient GLShader.ShaderOb gls;

   public GLShader(String source, String header) {
      this.source = source;
      this.header = header;
   }

   public int glid(GL2 gl) {
      if (this.gls != null && this.gls.gl != gl) {
         this.gls.dispose();
         this.gls = null;
      }

      if (this.gls == null) {
         this.gls = this.create(gl);
      }

      return this.gls.id;
   }

   protected abstract GLShader.ShaderOb create(GL2 var1);

   public static class FragmentShader extends GLShader {
      public final String entry;
      public final int order;
      private static final Comparator<GLShader.FragmentShader> cmp = new Comparator<GLShader.FragmentShader>() {
         public int compare(GLShader.FragmentShader a, GLShader.FragmentShader b) {
            return a.order - b.order;
         }
      };

      public FragmentShader(String source, String header, String entry, int order) {
         super(source, header);
         this.entry = entry;
         this.order = order;
      }

      public FragmentShader(String source) {
         this(source, "", null, 0);
      }

      @Override
      protected GLShader.ShaderOb create(GL2 gl) {
         GLShader.ShaderOb r = new GLShader.ShaderOb(gl, 35632);
         r.compile(this);
         return r;
      }

      public static GLShader.FragmentShader makemain(List<GLShader.FragmentShader> shaders) {
         StringBuilder buf = new StringBuilder();
         Collections.sort(shaders, cmp);

         for (GLShader.FragmentShader sh : shaders) {
            buf.append(sh.header + "\n");
         }

         buf.append("\n");
         buf.append("void main()\n{\n");
         buf.append("    vec4 res = gl_Color;\n");

         for (GLShader.FragmentShader sh : shaders) {
            buf.append("    " + sh.entry + "(res);\n");
         }

         buf.append("    gl_FragColor = res;\n");
         buf.append("}\n");
         return new GLShader.FragmentShader(buf.toString());
      }

      public static GLShader.FragmentShader parse(Reader in) throws IOException {
         class FSplitter extends GLShader.Splitter {
            StringBuilder header = new StringBuilder();
            String entry;
            int order = 0;

            FSplitter(Reader in) {
               super(in);
            }

            @Override
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
         }

         FSplitter p = new FSplitter(in);
         p.parse();
         if (p.entry == null) {
            throw new RuntimeException("No entry specified in shader source.");
         } else {
            return new GLShader.FragmentShader(p.main.toString(), p.header.toString(), p.entry, p.order);
         }
      }

      public static GLShader.FragmentShader load(Class<?> base, String name) {
         InputStream in = base.getResourceAsStream(name);

         try {
            GLShader.FragmentShader e;
            try {
               e = parse(new InputStreamReader(in, Utils.ascii));
            } finally {
               in.close();
            }

            return e;
         } catch (IOException var8) {
            throw new RuntimeException(var8);
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

      @Override
      public String toString() {
         return this.info == null ? super.toString() : super.toString() + "\nLog:\n" + this.info;
      }
   }

   public static class ShaderOb extends GLObject {
      public final int id;

      public ShaderOb(GL2 gl, int type) {
         super(gl);
         this.id = gl.glCreateShaderObjectARB(type);
         GOut.checkerr(gl);
      }

      @Override
      protected void delete() {
         this.gl.glDeleteObjectARB(this.id);
      }

      public void compile(GLShader sh) {
         this.gl.glShaderSourceARB(this.id, 1, new String[]{sh.source}, new int[]{sh.source.length()}, 0);
         this.gl.glCompileShaderARB(this.id);
         int[] buf = new int[]{0};
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

   public abstract static class Splitter {
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
               int p = d.indexOf(32);
               if (p >= 0) {
                  a = d.substring(p + 1);
                  d = d.substring(0, p).trim();
               }

               d = d.intern();
               this.directive(d, a);
            } else {
               this.buf.append(ln + "\n");
            }
         }
      }

      public abstract void directive(String var1, String var2);
   }

   public static class VertexShader extends GLShader {
      public final String entry;
      public final String[] args;
      public final int order;
      private static final Comparator<GLShader.VertexShader> cmp = new Comparator<GLShader.VertexShader>() {
         public int compare(GLShader.VertexShader a, GLShader.VertexShader b) {
            return a.order - b.order;
         }
      };

      public VertexShader(String source, String header, String entry, int order, String... args) {
         super(source, header);
         this.entry = entry;
         this.order = order;
         this.args = args;
      }

      public VertexShader(String source) {
         this(source, "", null, 0);
      }

      @Override
      protected GLShader.ShaderOb create(GL2 gl) {
         GLShader.ShaderOb r = new GLShader.ShaderOb(gl, 35633);
         r.compile(this);
         return r;
      }

      private boolean uses(String arg) {
         for (String a : this.args) {
            if (a.equals(arg)) {
               return true;
            }
         }

         return false;
      }

      private String call() {
         String ret = this.entry + "(";
         boolean f = true;

         for (String arg : this.args) {
            if (!f) {
               ret = ret + ", ";
            }

            ret = ret + arg;
            f = false;
         }

         return ret + ")";
      }

      public static GLShader.VertexShader makemain(List<GLShader.VertexShader> shaders) {
         StringBuilder buf = new StringBuilder();
         Collections.sort(shaders, cmp);

         for (GLShader.VertexShader sh : shaders) {
            buf.append(sh.header + "\n");
         }

         buf.append("\n");
         buf.append("void main()\n{\n");
         buf.append("    vec4 fcol = gl_Color;\n");
         buf.append("    vec4 bcol = gl_Color;\n");
         buf.append("    vec4 objv = gl_Vertex;\n");
         buf.append("    vec3 objn = gl_Normal;\n");

         int i;
         for (i = 0; i < shaders.size(); i++) {
            GLShader.VertexShader sh = shaders.get(i);
            if (sh.uses("eyev") || sh.uses("eyen")) {
               break;
            }

            buf.append("    " + sh.call() + ";\n");
         }

         buf.append("    vec4 eyev = gl_ModelViewMatrix * objv;\n");
         buf.append("    vec3 eyen = gl_NormalMatrix * objn;\n");

         while (i < shaders.size()) {
            GLShader.VertexShader sh = shaders.get(i);
            buf.append("    " + sh.call() + ";\n");
            i++;
         }

         buf.append("    gl_FrontColor = fcol;\n");
         buf.append("    gl_Position = gl_ProjectionMatrix * eyev;\n");
         buf.append("}\n");
         return new GLShader.VertexShader(buf.toString());
      }

      public static GLShader.VertexShader parse(Reader in) throws IOException {
         class VSplitter extends GLShader.Splitter {
            StringBuilder header = new StringBuilder();
            String entry;
            String[] args;
            int order = 0;

            VSplitter(Reader in) {
               super(in);
            }

            @Override
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
                  int i = 1;

                  for (int o = 0; i < args.length; o++) {
                     this.args[o] = args[i];
                     i++;
                  }
               }
            }
         }

         VSplitter p = new VSplitter(in);
         p.parse();
         if (p.entry == null) {
            throw new RuntimeException("No entry specified in shader source.");
         } else {
            return new GLShader.VertexShader(p.main.toString(), p.header.toString(), p.entry, p.order, p.args);
         }
      }

      public static GLShader.VertexShader load(Class<?> base, String name) {
         InputStream in = base.getResourceAsStream(name);

         try {
            GLShader.VertexShader e;
            try {
               e = parse(new InputStreamReader(in, Utils.ascii));
            } finally {
               in.close();
            }

            return e;
         } catch (IOException var8) {
            throw new RuntimeException(var8);
         }
      }
   }
}
