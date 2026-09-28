package haven;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.GLContext;

public class GLConfig implements Serializable, Console.Directory {
  private static final Pattern slvp = Pattern.compile("^(\\d+)\\.(\\d+)");
  
  public int glslver;
  
  public int glmajver;
  
  public int glminver;
  
  public int maxlights;
  
  public float anisotropy;
  
  public Collection<String> exts;
  
  public transient GLCapabilitiesImmutable caps;
  
  public GLSettings pref;
  
  private transient Map<String, Console.Command> cmdmap;
  
  private GLConfig() {
    this.cmdmap = new TreeMap<>();
    this.cmdmap.put("gl", new Console.Command() {
          public void run(Console cons, String[] args) throws Exception {
            if (args.length >= 3) {
              String var = args[1].intern();
              for (GLSettings.Setting<?> s : GLConfig.this.pref.settings()) {
                if (s.nm == var) {
                  s.set(args[2]);
                  GLConfig.this.pref.dirty = true;
                  return;
                } 
              } 
              throw new Exception("No such setting: " + var);
            } 
          }
        });
    this.cmdmap.put("glreset", new Console.Command() {
          public void run(Console cons, String[] args) {
            GLConfig.this.pref = GLSettings.defconf(GLConfig.this);
            GLConfig.this.pref.dirty = true;
          }
        });
  }
  
  private static int glgeti(GL gl, int param) {
    int[] buf = { 0 };
    gl.glGetIntegerv(param, buf, 0);
    GOut.checkerr(gl);
    return buf[0];
  }
  
  private static float glgetf(GL gl, int param) {
    float[] buf = { 0.0F };
    gl.glGetFloatv(param, buf, 0);
    GOut.checkerr(gl);
    return buf[0];
  }
  
  public static String glconds(GL gl, int param) {
    GOut.checkerr(gl);
    String ret = gl.glGetString(param);
    if (gl.glGetError() != 0)
      return null; 
    return ret;
  }
  
  public static GLConfig fromgl(GL gl, GLContext ctx, GLCapabilitiesImmutable caps) {
    GLConfig c = new GLConfig();
    try {
      c.glmajver = glgeti(gl, 33307);
      c.glminver = glgeti(gl, 33308);
    } catch (GLException e) {
      c.glmajver = 1;
      c.glminver = 0;
    } 
    c.maxlights = glgeti(gl, 3377);
    c.exts = Arrays.asList(gl.glGetString(7939).split(" "));
    c.caps = caps;
    c.pref = GLSettings.defconf(c);
    String slv = glconds(gl, 35724);
    if (slv != null) {
      Matcher m = slvp.matcher(slv);
      if (m.find())
        try {
          int major = Integer.parseInt(m.group(1));
          int minor = Integer.parseInt(m.group(2));
          if (major > 0 || major < 256 || minor >= 0 || minor < 256)
            c.glslver = major << 8 | minor; 
        } catch (NumberFormatException numberFormatException) {} 
    } 
    if (c.exts.contains("GL_EXT_texture_filter_anisotropic")) {
      c.anisotropy = glgetf(gl, 34047);
    } else {
      c.anisotropy = 0.0F;
    } 
    return c;
  }
  
  public boolean havefsaa() {
    return (this.exts.contains("GL_ARB_multisample") && this.caps.getSampleBuffers());
  }
  
  public boolean haveglsl() {
    return (this.exts.contains("GL_ARB_fragment_shader") && this.exts.contains("GL_ARB_vertex_shader") && this.glslver >= 276);
  }
  
  public boolean havefbo() {
    return this.exts.contains("GL_EXT_framebuffer_object");
  }
  
  public Map<String, Console.Command> findcmds() {
    return this.cmdmap;
  }
}
